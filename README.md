# Snapshot

### ⧉ The Problem

Videos often capture the same people many times across short scenes, camera movements, and group shots. Finding each person and choosing a good frame manually is slow and easy to get wrong.

This makes it difficult to turn a video into a clean, shareable recap that shows who appeared and how often.

#

### ⧉ The Solution

Snapshot transforms a portrait video into a people-focused, shareable collage:

- Extracts video frames and detects faces entirely on-device
- Tracks continuous appearances, including multiple people in the same frame
- Uses TensorFlow Lite face embeddings to group appearances by person
- Scores detected faces to select the best representative shot for each person
- Creates a vertical, Instagram Story-style collage with appearance counts
- Saves analyses locally and lets users save or share the finished collage

| Kotlin | Jetpack Compose | Material 3 | ML Kit |
| ------ | --------------- | ---------- | ------ |

| TensorFlow Lite | Room | MVVM | Kotlin Coroutines | Hilt |
| --------------- | ---- | ---- | ----------------- | ---- |

#

### ⧉ Key Features

- **On-device processing** — video frames, face detection, identity matching, and collage generation run locally on the device.
- **Appearance tracking** — separates continuous appearances when a person leaves the frame or a scene transition occurs.
- **Multi-person support** — tracks and counts people who appear together in the same frame.
- **Best-shot selection** — ranks candidate face shots by pose, sharpness, eye openness, smile probability, and framing.
- **Vertical collage** — generates a `1080 × 1920` collage with layouts that adapt to the number of people found.
- **Analysis history** — persists prior analyses, collage files, and person metadata locally with Room.
- **Save and share** — exports the collage to the device gallery or Android’s system share sheet.

#

### ⧉ How It Works

1. Choose a video from the device.
2. Snapshot samples frames at approximately 5 FPS using `MediaMetadataRetriever`.
3. ML Kit detects faces and supplies face bounds, head pose, eye openness, and smile information.
4. TensorFlow Lite produces an L2-normalized embedding for every detected face.
5. The appearance tracker combines consecutive matching detections into continuous appearance segments.
6. The person grouper clusters non-overlapping segments into unique people using cosine similarity.
7. Snapshot selects the strongest portrait for every person, creates the collage, and stores the analysis.

#

### ⧉ Representative Shot Scoring

To avoid blurry or awkward selections, each detected face is assigned a composite score based on:

- Frontal head pose
- Image sharpness, calculated with Laplacian variance
- Open-eye probability
- Smile probability
- Natural, unclipped framing

The highest-quality candidate is cropped with head-and-shoulders context and used in the final collage.

#

### ⧉ Architecture

```text
com.example.snapshot/
├── data/
│   ├── local/          Room database, DAOs, and entities
│   └── repository/     Video-processing orchestration
├── di/                 Hilt dependency-injection modules
├── ml/                 ML Kit detection, TFLite embeddings, quality scoring
├── model/              Processing and analysis data models
├── processor/          Frame extraction, tracking, grouping, collage rendering
├── ui/                 Compose screens, components, theme, MainViewModel
└── util/               Bitmap, similarity, gallery, and sharing helpers
```

The app uses MVVM. `MainViewModel` exposes UI state through `StateFlow`, `VideoRepository` runs the processing pipeline, and Hilt provides the database and processing dependencies.

#

### ⧉ Face Embedding Model

The bundled `facenet.tflite` model is loaded from `app/src/main/assets`. The app also recognizes compatible model names such as `mobile_face_net.tflite`, `facenet_512.tflite`, and `face_recognition.tflite`.

At runtime, Snapshot reads the model’s input and output tensor dimensions, resizes face crops, normalizes RGB pixel values to `[-1, 1]`, and L2-normalizes the resulting embedding. If no compatible model loads, the app falls back to a local descriptor so the pipeline can still run.

#

### ⧉ Tech Stack

| Layer | Technology |
| ----- | ---------- |
| Language | Kotlin |
| UI | Jetpack Compose, Material 3 |
| Architecture | MVVM, StateFlow |
| Asynchronous work | Kotlin Coroutines and Flow |
| Face detection | Google ML Kit |
| Face recognition | TensorFlow Lite |
| Persistence | Room |
| Dependency injection | Hilt |
| Image loading | Coil |

#

### ⧉ Requirements

- Android Studio Ladybug or newer
- JDK 17
- Android device or emulator running API 26 or later
- Android SDK matching the project configuration (`compileSdk 37`, `targetSdk 36`)

#

### ⧉ Build and Run

1. Clone the repository and open it in Android Studio.
2. Allow Gradle to sync and install the required SDK components if prompted.
3. Connect an Android device or start an emulator running API 26+.
4. Run the `app` configuration from Android Studio, or use:

   ```powershell
   .\gradlew.bat installDebug
   ```

5. Open Snapshot, select a video, wait for the analysis to finish, then view, save, or share the generated collage.

#

### ⧉ Project Status

Snapshot is an Android prototype for local video analysis and people collages. Results can vary with video resolution, lighting, motion blur, occlusion, and the embedding model in use.
