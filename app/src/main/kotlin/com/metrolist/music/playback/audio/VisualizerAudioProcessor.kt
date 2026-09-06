package com.metrolist.music.playback.audio

import androidx.media3.common.C
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.util.UnstableApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@UnstableApi
class VisualizerAudioProcessor : AudioProcessor {

    companion object {
        private const val FFT_SIZE = 1024
        private val BAND_LIMITS = intArrayOf(
            20, 60, 100, 150, 220, 320, 460, 660,
            940, 1350, 1900, 2700, 3900, 5600, 8000, 12000,
        )
        private const val SMOOTHING = 0.25f
        private val EMPTY_BUFFER: ByteBuffer = ByteBuffer.allocateDirect(0).order(ByteOrder.nativeOrder())
    }

    private var sampleRate = 0
    private var channelCount = 0
    private var isActive = false
    private var inputEnded = false

    @Volatile
    var enabled: Boolean = true

    private val pcmBuffer = ShortArray(FFT_SIZE)
    private var pcmPos = 0
    private var outputBuffer: ByteBuffer = EMPTY_BUFFER

    private val real = FloatArray(FFT_SIZE)
    private val imag = FloatArray(FFT_SIZE)
    private val smoothedBars = FloatArray(16)
    private val peakDecay = FloatArray(16) { 1e-4f }

    private val window = FloatArray(FFT_SIZE) { i ->
        0.5f * (1f - cos(2f * PI.toFloat() * i / (FFT_SIZE - 1)))
    }

    private val barsList = MutableList(16) { 0f }

    private val _bars = MutableStateFlow(List(16) { 0f })
    val bars: StateFlow<List<Float>> = _bars.asStateFlow()

    override fun configure(inputAudioFormat: AudioProcessor.AudioFormat): AudioProcessor.AudioFormat {
        if (inputAudioFormat.encoding != C.ENCODING_PCM_16BIT) {
            throw AudioProcessor.UnhandledAudioFormatException(inputAudioFormat)
        }
        sampleRate = inputAudioFormat.sampleRate
        channelCount = inputAudioFormat.channelCount
        isActive = true
        pcmPos = 0
        return inputAudioFormat
    }

    override fun isActive(): Boolean = isActive

    override fun queueInput(inputBuffer: ByteBuffer) {
        val pos = inputBuffer.position()
        val limit = inputBuffer.limit()
        val size = limit - pos
        if (size == 0) return

        if (outputBuffer.capacity() < size) {
            outputBuffer = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder())
        } else {
            outputBuffer.clear()
        }

        val frameSize = channelCount * 2
        val framesAvail = size / frameSize
        val framesToAccum = minOf(framesAvail, FFT_SIZE - pcmPos)

        for (i in 0 until framesToAccum) {
            var sum = 0
            for (ch in 0 until channelCount) {
                sum += inputBuffer.getShort(pos + i * frameSize + ch * 2).toInt()
            }
            pcmBuffer[pcmPos] = (sum / channelCount).toShort()
            pcmPos++
        }

        inputBuffer.position(pos)
        outputBuffer.put(inputBuffer)
        inputBuffer.position(limit)
        outputBuffer.flip()

        if (pcmPos >= FFT_SIZE) {
            processFft()
            pcmPos = 0
        }
    }

    private fun processFft() {
        if (!enabled) {
            pcmPos = 0
            return
        }
        val nyquist = sampleRate / 2
        if (nyquist == 0) return

        for (i in 0 until FFT_SIZE) {
            real[i] = pcmBuffer[i].toFloat() / 32768f * window[i]
            imag[i] = 0f
        }

        doFft(real, imag)

        for (band in 0 until 16) {
            val lowFreq = BAND_LIMITS[band]
            val highFreq = if (band < 15) BAND_LIMITS[band + 1] else nyquist

            val lowIdx =
                (lowFreq.toFloat() / nyquist * FFT_SIZE / 2).toInt().coerceIn(1, FFT_SIZE / 2 - 1)
            val highIdx =
                (highFreq.toFloat() / nyquist * FFT_SIZE / 2).toInt().coerceIn(1, FFT_SIZE / 2 - 1)

            var sum = 0f
            var count = 0
            for (i in lowIdx..highIdx) {
                sum += sqrt(real[i] * real[i] + imag[i] * imag[i])
                count++
            }

            val raw = if (count > 0) sum / count else 0f

            val peak = peakDecay[band]
            if (raw > peak) {
                peakDecay[band] = raw
            } else {
                peakDecay[band] = peak * 0.995f
            }
            val tracked = peakDecay[band].coerceAtLeast(1e-6f)

            val normalized = (raw / tracked).coerceIn(0f, 1f)

            smoothedBars[band] =
                smoothedBars[band] * SMOOTHING + normalized * (1f - SMOOTHING)
        }

        for (i in 0 until 16) barsList[i] = smoothedBars[i]
        _bars.value = barsList.toList()
    }

    private fun doFft(r: FloatArray, i: FloatArray) {
        val n = r.size
        var j = 0
        for (k in 0 until n) {
            if (k < j) {
                val tr = r[k]; r[k] = r[j]; r[j] = tr
                val ti = i[k]; i[k] = i[j]; i[j] = ti
            }
            var m = n / 2
            while (m >= 1 && j >= m) {
                j -= m; m /= 2
            }
            j += m
        }
        var step = 1
        while (step < n) {
            val half = step * 2
            val w = -PI / step
            val wrCos = cos(w).toFloat()
            val wiSin = sin(w).toFloat()
            var k = 0
            while (k < n) {
                var wr = 1f
                var wi = 0f
                for (l in 0 until step) {
                    val idx = k + l
                    val tr = wr * r[idx + step] - wi * i[idx + step]
                    val ti = wr * i[idx + step] + wi * r[idx + step]
                    r[idx + step] = r[idx] - tr
                    i[idx + step] = i[idx] - ti
                    r[idx] += tr
                    i[idx] += ti
                    val nwr = wr * wrCos - wi * wiSin
                    wi = wr * wiSin + wi * wrCos
                    wr = nwr
                }
                k += half
            }
            step = half
        }
    }

    override fun queueEndOfStream() {
        inputEnded = true
    }

    override fun getOutput(): ByteBuffer {
        val buf = outputBuffer
        outputBuffer = EMPTY_BUFFER
        return buf
    }

    override fun isEnded(): Boolean = inputEnded && !outputBuffer.hasRemaining()

    override fun flush() {
        outputBuffer = EMPTY_BUFFER
        inputEnded = false
        pcmPos = 0
    }

    override fun reset() {
        flush()
        sampleRate = 0
        channelCount = 0
        isActive = false
        peakDecay.fill(1e-4f)
        smoothedBars.fill(0f)
    }
}
