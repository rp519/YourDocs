# YourDocs - Developer Notes

## Architecture Overview

YourDocs follows **Clean Architecture** principles with clear separation of concerns:

```
┌─────────────────────────────────────────────┐
│           Presentation Layer (UI)           │
│  - Jetpack Compose screens                 │
│  - ViewModels (state management)           │
│  - Navigation                               │
└─────────────────┬───────────────────────────┘
                  │
┌─────────────────▼───────────────────────────┐
│           Domain Layer (Business Logic)     │
│  - Use Cases (business operations)         │
│  - Domain Models (Folder, Document)        │
│  - Repository Interfaces                   │
└─────────────────┬───────────────────────────┘
                  │
┌─────────────────▼───────────────────────────┐
│           Data Layer (Storage)              │
│  - Room Database (metadata persistence)    │
│  - FileStorageManager (document storage)   │
│  - Repository Implementations              │
└─────────────────────────────────────────────┘
```

### Key Design Decisions

1. **Room Database for Metadata**
   - While the spec suggested a JSON file, Room provides:
     - ACID transactions (prevents corruption)
     - Efficient queries (sorted folders, counts)
     - Type-safe DAOs
     - Automatic schema management
   - For migration, Room data can easily be exported to JSON
   - Schema version is tracked in the database itself

2. **Dependency Injection with Hilt**
   - Simplifies testing by allowing mock injection
   - Manages lifecycle-aware components automatically
   - Reduces boilerplate compared to manual DI

3. **Use Case Pattern**
   - Each business operation is encapsulated in a use case
   - Makes business logic testable independently of UI/data layers
   - Easy to reuse operations across different screens

## Data Model

### Database Schema (Version 1)

**Folders Table:**
```sql
CREATE TABLE folders (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    is_pinned INTEGER NOT NULL,
    is_locked INTEGER NOT NULL,
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL
)
```

**Documents Table:**
```sql
CREATE TABLE documents (
    id TEXT PRIMARY KEY,
    folder_id TEXT NOT NULL,
    original_name TEXT NOT NULL,
    stored_file_name TEXT NOT NULL,
    mime_type TEXT NOT NULL,
    size_bytes INTEGER NOT NULL,
    source TEXT NOT NULL,
    page_count INTEGER,
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL,
    FOREIGN KEY(folder_id) REFERENCES folders(id) ON DELETE CASCADE
)
```

### File Storage Structure

```
/data/data/com.yourdocs/files/YourDocs/
├── documents/
│   ├── 1234567890_a1b2c3d4.pdf
│   ├── 1234567891_e5f6g7h8.jpg
│   └── ...
└── metadata.json (for export/migration)
```

Files are stored with unique names: `{timestamp}_{uuid}.{extension}`
This prevents naming conflicts and preserves the original extension.

## Security Architecture

### PIN Storage
- PINs are stored using **EncryptedSharedPreferences**
- Uses AES256-GCM encryption
- Keys are backed by Android Keystore on supported devices (API 23+)
- Per-folder PINs stored as: `folder_{folderId}_pin`

### Biometric Authentication
- AndroidX Biometric library provides unified API
- Falls back to PIN if biometric not available/enrolled
- Rate limiting implemented to prevent brute force attacks
- After 5 failed attempts: 30-second cooldown
- After 10 failed attempts: 5-minute cooldown

### Threat Model
- **Protects against:** Casual access, opportunistic snooping
- **Does NOT protect against:** Sophisticated attacks, device compromise, forensic analysis
- **Recommendation:** Users with highly sensitive documents should use full-disk encryption + strong device PIN

## Migration/Export Strategy

### Export Process
1. User navigates to Settings → Export Data
2. App generates `metadata.json` containing:
   ```json
   {
     "version": 1,
     "exported_at": "2024-01-15T10:30:00Z",
     "folders": [...],
     "documents": [...]
   }
   ```
3. User shares the entire `YourDocs/` folder via:
   - File manager (copy to external storage)
   - Cloud storage upload
   - ADB pull (for advanced users)

### Import Process
1. User places `YourDocs/` folder in the new device's app data location
2. App detects existing data on first launch
3. Validates `metadata.json` schema version
4. Rebuilds Room database from JSON
5. Reconciles files on disk with metadata
6. **Security caveat:** Encrypted PINs don't transfer (hardware-backed keys)
   - Locked folders require PIN re-entry
   - Alternative: Implement secure recovery key system (future enhancement)

## Testing Strategy

### Unit Tests
- **Use Cases:** Business logic validation (input validation, edge cases)
- **Repositories:** Mock DAOs, test data transformations
- **ViewModels:** Test state management and user interactions
- Libraries: JUnit, Truth, Mockito-Kotlin, Coroutines Test

### Integration Tests (Future)
- **Database:** Room in-memory database tests
- **File Storage:** Test document save/retrieve/delete operations

### UI Tests (Future)
- Compose UI testing for critical user flows
- Libraries: Compose Testing, Espresso

## Current Implementation Status

### ✅ Phase 1: Folder CRUD + Metadata (COMPLETE)
- Domain models (Folder, Document)
- Room database with DAOs
- Repository pattern
- Use cases for folder operations
- File storage manager
- Home screen with folder list
- Create, rename, delete, pin/unpin folders
- Atomic metadata persistence via Room transactions

### 🔲 Phase 2: File Import + Document Listing (TODO)
- Import from internal storage
- Import from gallery
- Document repository
- Folder detail screen
- Document preview

### 🔲 Phase 3: Camera Capture → PDF (TODO)
- ML Kit Document Scanner integration
- Multi-page capture
- PDF generation
- Page management (reorder, delete)

### 🔲 Phase 4: Security (TODO)
- PIN setup/verification UI
- EncryptedSharedPreferences integration
- Biometric authentication
- Rate limiting
- Folder lock/unlock

### 🔲 Phase 5: Migration (TODO)
- JSON export
- Import/restore flow
- Validation and reconciliation
- PIN re-entry for locked folders

## Building the Project

### Prerequisites
- Android Studio Ladybug or later
- Kotlin 2.2.10
- Gradle 9.1.0
- Android SDK 26+ (minSdk)
- Android SDK 35 (compileSdk)

### Build Steps
1. Open project in Android Studio
2. Sync Gradle files
3. Run configuration: `app`
4. Select device/emulator (API 26+)
5. Click Run

### Running Tests
```bash
./gradlew test              # Unit tests
./gradlew connectedCheck    # Instrumented tests (future)
```

## Performance Considerations

1. **Database Queries**
   - Folders query uses LEFT JOIN with COUNT for efficiency
   - Indexed foreign keys for fast document lookups
   - Flow-based reactive queries for UI updates

2. **File Operations**
   - All I/O operations run on Dispatchers.IO
   - Large file copies use streaming (no memory buffer)
   - Deleted documents cleaned up immediately

3. **Memory Management**
   - Coil handles image loading with automatic caching
   - PDF preview uses paging to avoid loading entire file
   - Room queries return Flows to prevent memory leaks

## Known Limitations & Future Enhancements

### Current Limitations
- No document search functionality yet
- No document tags or categories
- No cloud sync (by design for v1)
- PIN recovery requires manual intervention after migration

### Planned Enhancements
- Full-text search across document contents (OCR)
- Document tags and smart folders
- Batch operations (multi-select)
- Document sharing via intents
- Backup encryption for migration security
- Wear OS companion app for quick access
- Widget for home screen shortcuts

## Code Style & Conventions

- **Naming:** PascalCase for classes, camelCase for properties/functions
- **Documentation:** KDoc for public APIs
- **Nullability:** Explicit nullable types, avoid !! operator
- **Coroutines:** Use structured concurrency, avoid GlobalScope
- **Compose:** Stateless composables with state hoisting
- **Architecture:** Repository pattern, unidirectional data flow

## Dependencies

All dependencies are specified in `gradle/libs.versions.toml` with pinned versions for reproducibility. Major dependencies:

- **UI:** Jetpack Compose, Material 3
- **Architecture:** Hilt, Navigation Compose, Lifecycle
- **Storage:** Room 2.7.0
- **Camera:** CameraX 1.4.1, ML Kit Document Scanner
- **Security:** AndroidX Security Crypto, Biometric
- **Testing:** JUnit, Truth, Mockito-Kotlin

## Maintenance Notes

### Database Migrations
Currently using `fallbackToDestructiveMigration()` for development. For production:
1. Create migration scripts in `DatabaseModule`
2. Test migrations with `MigrationTestHelper`
3. Increment database version

### Proguard
Production builds use R8 with rules in `proguard-rules.pro`. Key rules:
- Keep Room entities and DAOs
- Keep serialization classes
- Keep ML Kit internal classes

## Contact & Contributing

For questions or contributions, follow standard Android development practices:
- Write tests for new features
- Follow existing code style
- Update documentation for architectural changes
