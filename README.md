# 🎵 Music Player - Spotify-inspired Android App

A beautiful music player for Android with an advanced equalizer, featuring a lavender, white, and black color theme that closely resembles Spotify's interface.

## ✨ Features

### 🎨 **Beautiful Design**
- Spotify-inspired user interface
- Lavender, white, and black color scheme
- Material Design components
- Custom icon set for all controls

### 🎵 **Music Playback**
- MediaPlayer integration for high-quality audio
- Background music service with notification controls
- Play, pause, skip, previous functionality
- Shuffle and repeat modes (planned)

### 🎛️ **Advanced Equalizer**
- 5-band frequency equalizer (60Hz, 230Hz, 910Hz, 3.6kHz, 14kHz)
- Bass boost effect control
- Audio virtualization for enhanced listening
- Real-time audio processing

### 📱 **User Experience**
- Bottom navigation for easy access
- Music library with song list
- Playing indicator for current track
- Search and sort functionality (planned)

## 🏗️ **Architecture**

### Core Components
- **MainActivity**: Main navigation controller
- **HomeFragment**: Music library display
- **EqualizerFragment**: Audio controls and effects
- **MusicService**: Background playback service
- **MusicPlayerManager**: Singleton music controller

### Key Files
```
app/src/main/java/com/kaustubh/musicplayer/
├── MainActivity.kt
├── fragments/
│   ├── HomeFragment.kt
│   └── EqualizerFragment.kt
├── service/
│   └── MusicService.kt
├── player/
│   └── MusicPlayerManager.kt
├── adapters/
│   └── SongAdapter.kt
└── models/
    └── Song.kt
```

## 🚀 **Getting Started**

### Prerequisites
- Android Studio
- Android SDK (API level 21+)
- Java JDK 11 or higher

### Building the Project
1. Open the project in Android Studio
2. Sync Gradle files
3. Build the project using the "Build Debug APK" task
4. Install on your Android device or emulator

### VS Code Tasks Available
- **Build Debug APK**: Compiles the application for testing

## 🎯 **Upcoming Features**

### Short Term
- [ ] Playlist creation and management
- [ ] Search functionality implementation
- [ ] Settings and preferences
- [ ] Album art loading from media files

### Long Term
- [ ] Online streaming integration
- [ ] Music recommendation engine
- [ ] Social features
- [ ] Advanced audio effects

## 🔧 **Technical Details**

### Permissions Required
- `READ_EXTERNAL_STORAGE` / `READ_MEDIA_AUDIO`: Access music files
- `MODIFY_AUDIO_SETTINGS`: Equalizer functionality
- `WAKE_LOCK`: Background playback
- `FOREGROUND_SERVICE`: Background music service

### Audio Processing
- Uses Android's `MediaPlayer` for playback
- `Equalizer` API for frequency adjustment
- `BassBoost` for enhanced low frequencies
- `Virtualizer` for spatial audio effects

## 🎨 **Design System**

### Colors
- **Primary**: Lavender (#B794F6)
- **Background**: Dark (#121212)
- **Surface**: Slightly lighter dark (#1E1E1E)
- **Text**: White/Light gray for contrast

### Icons
Custom-designed icons for:
- Play/Pause controls
- Skip previous/next
- Shuffle and repeat
- Equalizer controls
- Search and settings

## 📱 **Screenshots**

*Screenshots will be added once the app is running on a device*

## 🤝 **Contributing**

This project is currently in development. Future contributions welcome for:
- UI/UX improvements
- Additional audio effects
- Performance optimizations
- New features

## 📄 **License**

This project is for educational and personal use.

---

**Built with ❤️ using Kotlin and Android SDK**
