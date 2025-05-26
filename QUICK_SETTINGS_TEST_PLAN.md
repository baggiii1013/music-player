# Quick Settings Integration Test Plan

## Pre-Test Setup
1. Install the APK on an Android 15+ device
2. Grant necessary permissions (storage, notification access)
3. Add the Music Quick Settings tile to the notification panel

## Test Scenarios

### Test 1: Basic Quick Settings Tile Functionality
**Steps:**
1. Open the music app
2. Play a song 
3. Pull down notification panel and access Quick Settings
4. Verify the Music tile shows current song info
5. Tap the tile to toggle play/pause
6. Verify the tile state updates correctly
7. Verify the app UI reflects the state change

**Expected Results:**
- Tile shows current song title and artist
- Tile icon reflects current play/pause state
- Tapping tile toggles playback
- App UI updates to match tile actions

### Test 2: MediaSession Integration
**Steps:**
1. Play music from the app
2. Use external controls (headphone buttons, car controls, other media controllers)
3. Verify the Quick Settings tile updates accordingly
4. Verify the app UI updates accordingly

**Expected Results:**
- External controls work correctly
- Quick Settings tile reflects external control actions
- App UI stays synchronized with MediaSession state

### Test 3: Service Communication
**Steps:**
1. Open the app and start playing music
2. Check service connection status (via logs or debug info)
3. Use Quick Settings to control playback
4. Verify callback communication works

**Expected Results:**
- Service binds successfully to MusicPlayerManager
- Callbacks trigger UI updates when Quick Settings controls are used
- No crashes or connection issues

### Test 4: Edge Cases
**Steps:**
1. Test with no song playing
2. Test when app is killed but service is running
3. Test rapid toggling of Quick Settings tile
4. Test with system media controls during Quick Settings usage

**Expected Results:**
- Graceful handling of edge cases
- No crashes or inconsistent states
- Proper fallback behavior

## Verification Commands

### Check if service is running:
```bash
adb shell ps | grep com.kaustubh.musicplayer
```

### Check MediaSession:
```bash
adb shell dumpsys media_session
```

### View logs:
```bash
adb logcat | grep -E "(MusicService|MusicPlayerManager|MusicQuickSettingsTile)"
```

## Known Issues to Monitor:
1. Callback interface synchronization
2. Service binding timing
3. MediaSession state consistency
4. UI thread safety

## Success Criteria:
✅ Quick Settings tile appears and functions correctly  
✅ Play/pause control works from Quick Settings  
✅ App UI updates when controlled via Quick Settings  
✅ MediaSession integration works with external controls  
✅ No crashes or memory leaks  
✅ Service communication is stable  

## Installation Command:
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```
