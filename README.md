VisualAssist — Android Application for Visually Impaired Users

VisualAssist is an Android application designed to assist visually impaired individuals by identifying real-world objects using the phone’s camera and providing:

Real-time object detection

Voice guidance (Text-to-Speech)

Directional awareness → left, right, center

Vibration feedback for confidence levels

The application uses Google ML Kit, CameraX, and Android Text-to-Speech to deliver fast and accurate feedback.

 Features
 1. Real-Time Object Detection

Uses ML Kit Image Labeling to detect objects instantly.

Works with the rear camera using CameraX API.

 2. Audio Feedback

Speaks out detected objects.

Example:
“There is a laptop on your left.”

 3. Direction Awareness

Determines where the object is located:

Left

Right

Center

 4. Vibration Feedback

Stronger vibration ≈ higher confidence detection.

 5. Noise-Free Experience

Ignores unnecessary labels (wall, room, ceiling, etc.).

🛠️ Tech Stack
Component	Technology
Programming Language	Java
ML Framework	Google ML Kit
Camera API	AndroidX CameraX
UI	XML, Material Components
Text-To-Speech	Android TTS Engine
Vibration	Android Vibrator API
 How it Works? (Short Explanation)

The user opens the app.

Camera starts automatically using CameraX.

Each frame is analyzed by ML Kit Image Labeling.

App identifies the object + confidence %.

Random X position determines LEFT/RIGHT/CENTER detection.

App speaks the result and vibrates for feedback.

The detected text is shown at the bottom of the screen.

Project Structure
app/
│── java/com.example.visualassist/
│     ├── MainActivity.java    # Core app logic
│
│── res/
│     ├── layout/activity_main.xml
│     ├── drawable/rounded_preview.xml
│     ├── mipmap/ (App Icons)
│     ├── values/themes.xml    # App theme
│
│── AndroidManifest.xml        # Permissions + activity declaration
│── build.gradle               # Dependencies


📄 Installation
Requirements:

Android 8+ (API 26 or higher)

Camera permission

Internet NOT required — works fully offline

Run the project:
Clone → Open in Android Studio → Run

 Testing

Tested on real Android device (Camera + TTS + ML Kit)

Verified object detection accuracy

Debugged frame handling + crash fixes

 Applications

Assistance for visually impaired individuals

Object awareness in home/office

AI-based smart assistance

Real-time camera-based recognition

 Future Enhancements

Text reading (OCR)

Face detection + person count

Navigation assistance

Obstacle detection

Add flashlight toggle for low light

 References

Google ML Kit Documentation:
https://developers.google.com/ml-kit

CameraX API Guide
https://developer.android.com/training/camerax

Android Text-to-Speech
https://developer.android.com/reference/android/speech/tts/TextToSpeech

Developed by

Kishor D S
B.E. in Computer Science
Project: VisualAssist – Android-based object detection assistant for visually impaired
