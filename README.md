# SimpleMediaRecord

A simple Android app to **record** and **play back** audio with focus management, device selection, and smooth user experience.

---

## ✨ Features

### 🎙️ Audio Recording

- **Permission Required**:
    
    - `android.permission.RECORD_AUDIO`
        
- **Recording Features**:
    
    - Uses **mono channel** for efficient recording.
        
    - Supports **popular audio formats**.
        
    - Displays the **current active microphone** during recording:
        
        - For **Android 12+**: uses `AudioManager.getActiveRecordingConfigurations()`.
            
        - For **Android <12**: checks the first element from the active recording list.
            
    - **Pause** and **resume** recording at any time.
        
    - **Handles Audio Focus** during recording:
        
        - Short interruptions (e.g., notification sounds, system sounds) → **pause** recording.
            
        - Long interruptions (e.g., music app starts, incoming phone call) → **stop** recording.
            
        - When focus is regained (e.g., call ends) → **resume** recording.
            

### 🔊 Audio Playback

- **Playback Features**:
    
    - **Requests audio focus** when playing back:
        
        - Short interruptions → **pause** playback.
            
        - Long interruptions → **stop** playback.
            
        - Focus regained → **resume** playback.
            
    - **Allows output device selection during playback** (for Android 12+):
        
        - Uses `setCommunicationDevice()` to switch between speaker, wired headset, Bluetooth, etc.
            
        - **Note**: For Bluetooth A2DP devices, **media route** needs to be set separately.
            
    - **On Android <12**:
        
        - Output device change is **only supported before** playback starts (using `AudioAttributes.Builder.setUsage()`).
            
        - (Changing during playback is not implemented yet, but sample code has been tested.)
            

---

## 🛠️ Tech Stack

- **Kotlin**
    
- **AudioRecord** for recording audio
    
- **MediaPlayer** for playback
    
- **AudioManager** for managing active recordings and audio focus
    
- **MVVM architecture** (ViewModel, Repository)
    
- **Hilt** for Dependency Injection
    
- **Flow/StateFlow** for reactive state handling
    
---

## Screenshot

<img src="https://github.com/user-attachments/assets/98af2a0d-86d7-424a-bd98-44bc3090ccc1" width="300">

<img src="https://github.com/user-attachments/assets/96311175-d252-4b55-997a-0bc8d2c36ee3" width="300">

<img src="https://github.com/user-attachments/assets/d7cad0a6-fc07-4114-8636-f78556c1ecbd" width="300">

---

## 📜 Notes

- **Audio focus** is carefully managed to ensure good user experience across interruptions.
    
- **Device switching** is smooth on Android 12+, partially limited on Android <12.
    
- The app aims to give maximum control over recording/playback while handling different Android versions gracefully.
