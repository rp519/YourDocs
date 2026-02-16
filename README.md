# YourDocs

**Fast, offline-first document organization for Android.**

YourDocs is a mobile app that stores and organizes important documents locally on your phone, making them accessible instantly without browsing the system file manager.

## Features

### 📁 Simple Folder Organization
- Create unlimited folders to organize your documents
- Pin important folders to keep them at the top
- Quick folder search and navigation
- View document counts and last updated dates

### 📄 Multiple Document Sources
- Import files from internal storage
- Import photos from gallery
- Capture documents with camera → auto-convert to PDF

### 📸 Smart Document Scanning
- ML Kit-powered document detection
- Multi-page capture support
- Auto-crop and enhance
- Manual crop/rotate controls

### 🔒 Built-in Security
- PIN protection for sensitive folders
- Biometric authentication (fingerprint/face)
- Rate limiting against brute force attacks
- Hardware-backed encryption on supported devices

### 📦 Easy Migration
- All data stored in a single app folder
- Export your entire library as JSON + files
- Restore on a new device in seconds
- No cloud dependency required

## Screenshots

*[Screenshots to be added after UI completion]*

## Installation

### Requirements
- Android 8.0 (API 26) or higher
- Camera permission (for document capture)
- Storage permission (for file import)
- Biometric hardware (optional, for biometric auth)

### Build from Source
1. Clone the repository
2. Open in Android Studio Ladybug or later
3. Sync Gradle files
4. Run on device or emulator

```bash
git clone https://github.com/yourusername/yourdocs.git
cd yourdocs
./gradlew assembleDebug
```

## Tech Stack

- **Language:** Kotlin 2.2.10
- **UI:** Jetpack Compose + Material 3
- **Architecture:** Clean Architecture with MVVM
- **Database:** Room 2.7.0 (SQLite)
- **DI:** Hilt
- **Camera:** CameraX + ML Kit Document Scanner
- **Security:** AndroidX Security Crypto + Biometric

See [DEVELOPER_NOTES.md](DEVELOPER_NOTES.md) for detailed architecture documentation.

## Usage

### Creating a Folder
1. Tap the **+** button on the home screen
2. Enter a folder name
3. Tap **Create**

### Adding Documents
1. Open a folder
2. Tap **Add Document**
3. Choose source:
   - **Import File** - Browse device storage
   - **From Gallery** - Select from photos
   - **Camera** - Capture a new document

### Locking a Folder
1. Open folder options menu (⋮)
2. Select **Set PIN**
3. Enter a 4-6 digit PIN
4. Enable biometric unlock (optional)

### Migrating to New Phone
1. Go to **Settings** → **Export Data**
2. Note the app data folder location
3. Copy the `YourDocs` folder to your computer/cloud
4. On the new device, install YourDocs
5. Place the `YourDocs` folder in the app data location
6. Open the app - it will detect and restore your data

## Development Status

### ✅ Completed (Phase 1)
- Folder CRUD operations
- Metadata persistence with Room
- Home screen with folder list
- Pin/unpin folders
- Delete confirmation dialogs
- Clean architecture foundation

### 🚧 In Progress
- Phase 2: File import + document listing
- Phase 3: Camera capture → PDF
- Phase 4: Security (PIN/biometric)
- Phase 5: Migration import/export

### 📅 Planned
- Document search
- Document tags
- Batch operations
- Share documents
- Cloud backup option (optional)

## Testing

```bash
# Run unit tests
./gradlew test

# Run instrumented tests (requires device/emulator)
./gradlew connectedCheck

# Run specific test
./gradlew test --tests CreateFolderUseCaseTest
```

## Contributing

Contributions are welcome! Please:
1. Fork the repository
2. Create a feature branch
3. Write tests for new functionality
4. Follow existing code style
5. Submit a pull request

## Security

YourDocs is designed for **casual privacy protection**, not military-grade security. It protects against:
- Opportunistic snooping
- Casual unauthorized access
- Accidental disclosure

It does NOT protect against:
- Sophisticated attacks
- Device forensics
- Malware with root access

For highly sensitive documents, use device encryption + strong device PIN in addition to YourDocs security features.

## Privacy

- **No internet permissions** - All data stays on device
- **No analytics or tracking** - Complete privacy
- **No ads** - Clean, focused experience
- **Open source** - Audit the code yourself

## License

[To be determined - suggest Apache 2.0 or MIT]

## Acknowledgments

- Built with [Jetpack Compose](https://developer.android.com/jetpack/compose)
- Document scanning powered by [ML Kit](https://developers.google.com/ml-kit)
- Security via [AndroidX Security](https://developer.android.com/jetpack/androidx/releases/security)

## Support

For bugs and feature requests, please [open an issue](https://github.com/yourusername/yourdocs/issues).

---

**Note:** This is an MVP implementation. Some features are still in development. See [DEVELOPER_NOTES.md](DEVELOPER_NOTES.md) for implementation status and roadmap.
