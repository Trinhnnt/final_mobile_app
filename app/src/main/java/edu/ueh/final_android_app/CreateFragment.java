package edu.ueh.final_android_app;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;

import androidx.camera.video.FileOutputOptions;
import androidx.camera.video.MediaStoreOutputOptions;
import androidx.camera.video.OutputOptions;
import androidx.camera.video.Quality;
import androidx.camera.video.QualitySelector;
import androidx.camera.video.Recorder;
import androidx.camera.video.Recording;
import androidx.camera.video.VideoCapture;
import androidx.camera.video.VideoRecordEvent;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.util.Objects;

public class CreateFragment extends Fragment {
    private PreviewView previewView;
    private ImageButton btnRecord;
    private ProcessCameraProvider cameraProvider;
    private VideoCapture<Recorder> videoCapture;
    private Recording recording;

    private ActivityResultLauncher<String[]> permissionLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create, container, false);
        previewView = view.findViewById(R.id.previewView);
        btnRecord = view.findViewById(R.id.btnRecord);

        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    boolean cameraGranted = Boolean.TRUE.equals(result.get(Manifest.permission.CAMERA));
                    boolean micGranted = Boolean.TRUE.equals(result.get(Manifest.permission.RECORD_AUDIO));

                    if (cameraGranted && micGranted) {
                        startCamera();
                    } else {
                        Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        btnRecord.setOnClickListener(v -> toggleRecording());

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            permissionLauncher.launch(new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            });
        }

        return view;
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(requireContext());
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindCameraUseCases();
            } catch (Exception ignored) {}
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private void bindCameraUseCases() {
        CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        Recorder recorder = new Recorder.Builder()
                .setQualitySelector(QualitySelector.from(Quality.HD))
                .build();
        videoCapture = VideoCapture.withOutput(recorder);

        cameraProvider.unbindAll();
        cameraProvider.bindToLifecycle(this, cameraSelector, preview, videoCapture);
    }

    private void toggleRecording() {
        if (recording != null) {
            recording.stop();
            recording = null;
            btnRecord.setImageResource(R.drawable.start_record);
            btnRecord.setColorFilter(ContextCompat.getColor(requireContext(), R.color.white));
            return;
        }

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            permissionLauncher.launch(new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO
            });
            return;
        }

        // === Create temp file for upload ===
        File outputDir = requireContext().getCacheDir();
        File outputFile = new File(outputDir, "video_" + System.currentTimeMillis() + ".mp4");

        FileOutputOptions options = new FileOutputOptions.Builder(outputFile).build();

        recording = videoCapture.getOutput()
                .prepareRecording(requireContext(), options)
                .withAudioEnabled()
                .start(ContextCompat.getMainExecutor(requireContext()), event -> {
                    if (event instanceof VideoRecordEvent.Finalize) {
                        Uri uri = Uri.fromFile(outputFile);
                        Toast.makeText(requireContext(), "Video ready for upload", Toast.LENGTH_SHORT).show();

                        // TODO: Upload to Firebase Storage
//                        uploadVideoToFirebase(uri);
                    }
                });

        btnRecord.setImageResource(R.drawable.stop_record);
        btnRecord.setColorFilter(ContextCompat.getColor(requireContext(), R.color.red));
    }

//    private void uploadVideoToFirebase(Uri uri) {
//        // Example Firebase upload
//        FirebaseStorage storage = FirebaseStorage.getInstance();
//        StorageReference ref = storage.getReference().child("videos/" + uri.getLastPathSegment());
//        ref.putFile(uri)
//                .addOnSuccessListener(taskSnapshot -> Toast.makeText(requireContext(), "Uploaded", Toast.LENGTH_SHORT).show())
//                .addOnFailureListener(e -> Toast.makeText(requireContext(), "Upload failed", Toast.LENGTH_SHORT).show());
//    }
}