<p align="center">
  <img src="https://github.com/NeoNexus402/Loiter/raw/main/fastlane/metadata/android/en-US/images/loiter.png" width="150" alt="Loiter logo" />
</p>

<p align="center">
  <strong>Loiter</strong> — an advanced, independent YouTube Music client for Android.
</p>

<p align="center">
  <a href="https://github.com/NeoNexus402/Loiter/releases"><img src="https://img.shields.io/github/v/release/NeoNexus402/Loiter?style=for-the-badge&labelColor=0d1117" alt="Latest release" /></a>
  <a href="https://github.com/NeoNexus402/Loiter/blob/main/LICENSE"><img src="https://img.shields.io/github/license/NeoNexus402/Loiter?style=for-the-badge&labelColor=0d1117" alt="License" /></a>
  <a href="https://github.com/NeoNexus402/Loiter/stargazers"><img src="https://img.shields.io/github/stars/NeoNexus402/Loiter?style=for-the-badge&labelColor=0d1117" alt="Stars" /></a>
</p>

> **Regional restriction** — YouTube Music must be available in your region. If it is not, use a VPN or proxy connected to a supported region.

---

## About

Loiter is a YouTube Music client built from the ground up as a fork of
[Metrolist](https://github.com/MetrolistGroup/Metrolist). It keeps the core
streaming, download, and library features, then goes its own way with a
distinct layout-theme system, a single dynamic accent color, smoother motion,
and a faster, lighter feel.

---

## Features

### Playback

- Stream any song or video from YouTube Music
- Background playback
- Download and cache for offline use
- Skip silence
- Sleep timer

### Audio

- Audio normalization
- Tempo and pitch control
- Equalizer with AutoEQ profile import

### Library and Account

- Full library management
- Local and synced playlists
- Import playlists (CSV, M3U)
- Reorder songs in a playlist or the queue
- YouTube Music account login
- Library synchronization
- Edit song titles and artist names
- Explicit content tagging

### Lyrics and Discovery

- Live synced lyrics (LyricsPlus, LRCLIB, Kugou)
- AI-powered lyrics translation and romanization
- Japanese lyrics romanization support
- Share lyrics as text or image
- Personalized quick picks
- Search songs, albums, artists, videos, and playlists

### Themes and Customization

- Layout themes: **Loiter**, **Metrolist**, **Blackhole**, with YouTube Music and Spotify coming soon
- Each theme locks irrelevant settings automatically
- Light, dark, black, and pure-black theme modes
- Dynamic colors with 19 accent palettes
- Player layouts: Default and Modern
- MiniPlayer layouts: Default and Overlay
- NavBar styles: Default, Compact, and Pill
- Player button colors and slider style
- Slim bottom navbar option

### Interface

- Home screen, playlist, and music recognizer widgets
- Material 3 with Material 3 Expressive patterns
- Swipe gestures for track navigation
- Drag-to-reorder in playlists and the queue
- Copy link and share song functionality
- Music recognition (Shazam API)

### Social

- Listen together with friends in real time
- Discord Rich Presence integration

---

## What makes Loiter different

- **Its own theme system** — Loiter, Metrolist, and Blackhole layouts each bring
  their own typography, spacing, and controls. YouTube Music and Spotify
  layouts are on the way.
- **Smart feature locking** — each theme hides the settings that do not apply
  to it (slider style, navbar style, player style, dynamic theme, and more).
- **Neutral color scheme** — non-Loiter themes use a gray-black palette with
  accent colors reserved for the key UI elements.
- **A single, song-aware accent color** — the accent is pulled from the artwork
  of the currently playing song and animates smoothly everywhere when it
  changes.
- **Gradient player backgrounds** — album-art-driven gradients.
- **A real-time audio visualizer** — an FFT-based spectrum analyzer built on
  ExoPlayer's AudioProcessor pipeline.
- **Album art border** — toggle borders around the album artwork.
- **Lean and fluid** — tightened animation specs, Material motion curves, and
  cleaned-up code keep the app light and responsive.

---

## New in v1.2.1

### Player

- The accent color now follows the current song's artwork, and colors are
  brightened so controls, icons, and text stay legible on any background.
- Theme color changes animate smoothly across the whole app, Home included.
- The Up Next preview stays in sync with the live queue and updates the moment
  you skip a track.
- Up Next rows now transition smoothly: the played row fades out, the rows
  below glide up, and the next queued track fades in.
- Up Next rows were redesigned with aligned numbering for a cleaner look.
- The system back button or back gesture on the expanded player now collapses
  it back to the miniplayer.
- Clear **Loading lyrics** and **Lyrics not found** states in the player.

### Home

- Quick Picks is always pinned to the top of the Home screen.

### Performance

- Smoother screen-to-screen transitions driven by Material motion curves.
- Removed dead code and tightened animation specs for a leaner, faster app.

---

## Coming soon

- Additional layout themes (YouTube Music, Spotify)
- More MiniPlayer and Player layout options
- Expanded theme customization controls

---

## Screenshots

Screenshots are organized by layout theme. The Loiter, Metrolist, and Blackhole
themes each have their own gallery, while the YouTube Music and Spotify themes
are still in development.

### Loiter theme

<div align="center">
  <img src="https://github.com/NeoNexus402/Loiter/blob/main/fastlane/metadata/android/en-US/images/screenshots/loiter_theme_1.png" alt="Loiter theme screenshot 1" width="30%" />
  <img src="https://github.com/NeoNexus402/Loiter/blob/main/fastlane/metadata/android/en-US/images/screenshots/loiter_theme_2.png" alt="Loiter theme screenshot 2" width="30%" />
  <img src="https://github.com/NeoNexus402/Loiter/blob/main/fastlane/metadata/android/en-US/images/screenshots/loiter_theme_3.png" alt="Loiter theme screenshot 3" width="30%" />
</div>

### Metrolist theme

<div align="center">
  <img src="https://github.com/NeoNexus402/Loiter/blob/main/fastlane/metadata/android/en-US/images/screenshots/metrolist_theme_1.png" alt="Metrolist theme screenshot 1" width="30%" />
  <img src="https://github.com/NeoNexus402/Loiter/blob/main/fastlane/metadata/android/en-US/images/screenshots/metrolist_theme_2.png" alt="Metrolist theme screenshot 2" width="30%" />
  <img src="https://github.com/NeoNexus402/Loiter/blob/main/fastlane/metadata/android/en-US/images/screenshots/metrolist_theme_3.png" alt="Metrolist theme screenshot 3" width="30%" />
</div>

### Blackhole theme

<div align="center">
  <img src="https://github.com/NeoNexus402/Loiter/blob/main/fastlane/metadata/android/en-US/images/screenshots/blackhole_theme_1.png" alt="Blackhole theme screenshot 1" width="30%" />
  <img src="https://github.com/NeoNexus402/Loiter/blob/main/fastlane/metadata/android/en-US/images/screenshots/blackhole_theme_2.png" alt="Blackhole theme screenshot 2" width="30%" />
  <img src="https://github.com/NeoNexus402/Loiter/blob/main/fastlane/metadata/android/en-US/images/screenshots/blackhole_theme_3.png" alt="Blackhole theme screenshot 3" width="30%" />
</div>

### YouTube Music theme

_Coming soon._

### Spotify theme

_Coming soon._

---

## Acknowledgments

Loiter would not exist without the people behind the projects it builds on.

### Main inspirations

| Project | Authors |
| --- | --- |
| **Metrolist** | [Mo Agamy](https://github.com/mostafaalagamy), [Metrolist Group](https://github.com/MetrolistGroup) |
| **InnerTune** | [Zion Huang](https://github.com/z-huang), [Malopieds](https://github.com/Malopieds) |
| **OuterTune** | [Davide Garberi](https://github.com/DD3Boh), [Michael Zh](https://github.com/mikooomich) |

### Libraries and integrations

| Project | Contribution |
| --- | --- |
| [Kizzy](https://github.com/dead8309/Kizzy) | Discord Rich Presence implementation and inspiration |
| [Better Lyrics](https://better-lyrics.boidu.dev) | Time-synced lyrics with word-by-word highlighting and YouTube Music integration |
| [SimpMusic Lyrics](https://github.com/maxrave-dev/SimpMusic) | Lyrics data through the SimpMusic Lyrics API |
| [metroserver](https://github.com/MetrolistGroup/metroserver) | Listen-together real-time backend |
| [MusicRecognizer](https://github.com/aleksey-saenko/MusicRecognizer) | Music recognition feature and Shazam API integration |
| [ExoVisualizer](https://github.com/dzolnai/ExoVisualizer) | Audio visualizer approach using ExoPlayer AudioProcessor and FFT |

And the entire open-source community. Every library, tool, and API that powers
this project is appreciated.

---

## Disclaimer

This project is **not affiliated with, funded by, authorized by, endorsed by,
or associated with** YouTube, Google LLC, Metrolist Group LLC, or any of their
affiliates and subsidiaries. Loiter is a fork of the Metrolist project.

All trademarks, service marks, and intellectual property rights referenced here
belong to their respective owners.

---

Loiter is maintained by Ratul Acharya ([@NeoNexus402](https://github.com/NeoNexus402)).
Originally forked from Metrolist by [Mo Agamy](https://github.com/mostafaalagamy).