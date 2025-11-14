package com.example.visualassist;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.util.Size;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@ExperimentalGetImage
public class MainActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_REQUEST = 1001;
    private static final long COOLDOWN_MS = 4000;
    private static final long SPEAK_DELAY_MS = 800;

    private PreviewView previewView;
    private TextView tvDetectedObject;

    private ExecutorService cameraExecutor;
    private TextToSpeech tts;
    private Vibrator vibrator;

    private String lastDirection = "";
    private long lastSpeakTime = 0;

    private final Map<String, Long> lastSpokenMap = new HashMap<>();

    // Labels we do NOT want to speak
    private final Set<String> ignoredLabels = new HashSet<>(Arrays.asList(
            "room", "floor", "indoor", "wall", "home", "house",
            "interior design", "ceiling", "furniture"
    ));

    // Wrong → Correct object mapping
    private final Map<String, String> correctionMap = new HashMap<String, String>() {{
        put("guitar", "laptop");
        put("musical instrument", "laptop");
        put("string instrument", "laptop");
        put("cool", "person");
        put("hair", "person");
        put("fashion", "person");
        put("keyboard", "laptop");
        put("computer keyboard", "laptop");
        put("screen", "monitor");
        put("television", "monitor");
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        previewView = findViewById(R.id.previewView);
        tvDetectedObject = findViewById(R.id.tvDetectedObject);

        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.ENGLISH);
                tts.setSpeechRate(1.0f);
            }
        });

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        cameraExecutor = Executors.newSingleThreadExecutor();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST
            );
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setTargetResolution(new Size(640, 480))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                imageAnalysis.setAnalyzer(cameraExecutor, this::analyzeImage);
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageAnalysis);

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void analyzeImage(@NonNull ImageProxy imageProxy) {
        if (imageProxy.getImage() == null) {
            imageProxy.close();
            return;
        }

        InputImage image = InputImage.fromMediaImage(
                imageProxy.getImage(),
                imageProxy.getImageInfo().getRotationDegrees()
        );

        ImageLabeling.getClient(
                        new ImageLabelerOptions.Builder().setConfidenceThreshold(0.7f).build()
                )
                .process(image)
                .addOnSuccessListener(this::handleLabels)
                .addOnFailureListener(e -> Log.e("VisualAssist", "Labeling failed", e))
                .addOnCompleteListener(task -> imageProxy.close());
    }

    private void handleLabels(List<ImageLabel> labels) {
        if (labels == null || labels.isEmpty()) {
            runOnUiThread(() -> tvDetectedObject.setText("No object detected"));
            return;
        }

        ImageLabel top = labels.get(0);
        String label = top.getText().toLowerCase(Locale.ENGLISH);
        float confidence = top.getConfidence() * 100f;

        if (correctionMap.containsKey(label))
            label = correctionMap.get(label);

        if (ignoredLabels.contains(label) || confidence < 70f) return;

        float randomX = (float) (Math.random() * previewView.getWidth());
        float center = previewView.getWidth() / 2f;
        String direction = getDirectionFromX(randomX, center);

        String text = String.format(Locale.ENGLISH,
                "%s detected on your %s (%.0f%%)", label, direction, confidence);

        runOnUiThread(() -> tvDetectedObject.setText(text));

        long now = System.currentTimeMillis();
        long lastTime = lastSpokenMap.getOrDefault(label, 0L);

        if ((now - lastTime > COOLDOWN_MS) && (now - lastSpeakTime > SPEAK_DELAY_MS)) {
            speakAndVibrate(generateSpeech(label, direction), confidence);
            lastDirection = direction;
            lastSpeakTime = now;
            lastSpokenMap.put(label, now);
        }
    }

    private String generateSpeech(String label, String direction) {
        switch (direction) {
            case "left": return "There is a " + label + " on your left";
            case "right": return "There is a " + label + " on your right";
            default: return "A " + label + " is in front of you";
        }
    }

    private String getDirectionFromX(float x, float center) {
        if (x < center - center * 0.25) return "left";
        else if (x > center + center * 0.25) return "right";
        else return "center";
    }

    private void speakAndVibrate(String text, float confidence) {
        if (tts != null)
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);

        if (vibrator != null && vibrator.hasVibrator()) {
            int duration = (confidence > 85) ? 140 : 240;
            vibrator.vibrate(VibrationEffect.createOneShot(duration,
                    VibrationEffect.DEFAULT_AMPLITUDE));
        }
    }
}