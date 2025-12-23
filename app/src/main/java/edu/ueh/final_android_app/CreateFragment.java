package edu.ueh.final_android_app;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.video.FileOutputOptions;
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

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import edu.ueh.final_android_app.models.Video;
import edu.ueh.final_android_app.util.CommonUtil;
import edu.ueh.final_android_app.util.FireStorageUtil;
import edu.ueh.final_android_app.util.FirebaseUtil;

public class CreateFragment extends Fragment {
    private PreviewView previewView;
    private VideoView videoView;
    private LinearLayout captionContainer;
    private ImageButton btnRecord, btnSendCaption, btnStorage, btnReset;
    private ProcessCameraProvider cameraProvider;
    private VideoCapture<Recorder> videoCapture;
    private Recording recording;
    private EditText etCaption;
    private File currentFile;

    private ActivityResultLauncher<String[]> permissionLauncher;
    private ActivityResultLauncher<String> pickVideoLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create, container, false);
        previewView = view.findViewById(R.id.previewView);
        videoView = view.findViewById(R.id.videoView);
        btnRecord = view.findViewById(R.id.btnRecord);
        btnSendCaption = view.findViewById(R.id.btnSendCaption);
        etCaption = view.findViewById(R.id.etCaption);
        captionContainer = view.findViewById(R.id.captionContainer);
        btnStorage = view.findViewById(R.id.btnFolder);
        btnReset = view.findViewById(R.id.btnReset);

        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
            boolean cameraGranted = Boolean.TRUE.equals(result.get(Manifest.permission.CAMERA));
            boolean micGranted = Boolean.TRUE.equals(result.get(Manifest.permission.RECORD_AUDIO));

            if (cameraGranted && micGranted) {
                startCamera();
            } else {
                Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show();
            }
        });

        pickVideoLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri == null) return;

            // Copy file về cache
            currentFile = copyUriToFile(uri);

            if (currentFile != null) {
                // Tắt camera preview
                previewView.setVisibility(View.GONE);
                btnRecord.setVisibility(View.GONE);

                // Hiện video preview
                videoView.setVisibility(View.VISIBLE);
                videoView.setVideoURI(uri);
                videoView.start();

                captionContainer.setVisibility(View.VISIBLE);
            }
        });

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            permissionLauncher.launch(new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE});
        }

        return view;
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindCameraUseCases();
            } catch (Exception ignored) {
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private void bindCameraUseCases() {
        CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        Recorder recorder = new Recorder.Builder().setQualitySelector(QualitySelector.from(Quality.HD)).build();
        videoCapture = VideoCapture.withOutput(recorder);

        cameraProvider.unbindAll();
        cameraProvider.bindToLifecycle(this, cameraSelector, preview, videoCapture);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        btnRecord.setOnClickListener(v -> toggleRecording());
        btnSendCaption.setOnClickListener(v -> uploadVideoToCloud());
        btnStorage.setOnClickListener(v -> selectVideoToUpload());
        btnReset.setOnClickListener(v -> resetView());
    }

    private void toggleRecording() {
        if (recording != null) {
            recording.stop();
            recording = null;
            btnRecord.setImageResource(R.drawable.start_record);
            btnRecord.setColorFilter(ContextCompat.getColor(requireContext(), R.color.white));
            return;
        }

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO});
            return;
        }

        stopUpload();

        // === Create temp file for upload ===
        File outputDir = requireContext().getCacheDir();
        currentFile = new File(outputDir, "video_" + System.currentTimeMillis() + ".mp4");

        FileOutputOptions options = new FileOutputOptions.Builder(currentFile).build();

        recording = videoCapture.getOutput().prepareRecording(requireContext(), options).withAudioEnabled().start(ContextCompat.getMainExecutor(requireContext()), event -> {
            if (event instanceof VideoRecordEvent.Finalize) {
                if (currentFile.length() == 0) {
                    Toast.makeText(requireContext(), "Video empty (CameraX fail)", Toast.LENGTH_LONG).show();
                    return;
                }

                startUpload();
            }
        });

        btnRecord.setImageResource(R.drawable.stop_record);
        btnRecord.setColorFilter(ContextCompat.getColor(requireContext(), R.color.red));
    }

    private void selectVideoToUpload() {
        pickVideoLauncher.launch("video/*");
    }

    private void resetView() {
        // Stop recording nếu đang ghi
        if (recording != null) {
            recording.stop();
            recording = null;
        }

        // Stop video nếu đang play
        if (videoView.getVisibility() == View.VISIBLE) {
            videoView.stopPlayback();
            videoView.setVisibility(View.GONE);
        }

        // Reset file
        currentFile = null;

        // Clear caption
        etCaption.setText("");
        stopUpload();

        // Reset UI
        previewView.setVisibility(View.VISIBLE);
        btnRecord.setVisibility(View.VISIBLE);

        // Restart camera to ensure fresh state
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
            bindCameraUseCases();
        }

        Toast.makeText(requireContext(), "Reset", Toast.LENGTH_SHORT).show();
    }


    private void uploadVideoToCloud() {
        if (etCaption.getText().toString().isEmpty()) {
            Toast.makeText(requireContext(), "Empty caption", Toast.LENGTH_SHORT).show();
            return;
        }

        stopUpload();

        FireStorageUtil fireStorageUtil = new FireStorageUtil(requireContext());
        fireStorageUtil.uploadVideo(currentFile, new FireStorageUtil.UploadListener() {
            @Override
            public void onSuccess(String fileId) {
                uploadVideoObject(fileId);
            }
            @Override
            public void onError(Exception e) {
                Toast.makeText(requireContext(), "Upload failed", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void resetUi(){
        videoView.setVisibility(View.GONE);
        previewView.setVisibility(View.VISIBLE);
        btnRecord.setVisibility(View.VISIBLE);

        videoView.stopPlayback();
    }

    private void uploadVideoObject(String videoUrl) {
        if (etCaption.getText().toString().isEmpty()) {
            Toast.makeText(requireContext(), "No video selected or empty caption", Toast.LENGTH_SHORT).show();
            return;
        }

        Video newVideo = new Video(etCaption.getText().toString(), videoUrl, CommonUtil.currentUser, CommonUtil.getNow(), new ArrayList<String>(), CommonUtil.currentUser.getAvatarUrl());

        FirebaseUtil firebaseUtil = new FirebaseUtil();
        firebaseUtil.saveVideoToFirestore(newVideo, documentReference -> {
            Toast.makeText(requireContext(), "Video uploaded", Toast.LENGTH_SHORT).show();
            resetUi();
        }, e -> {
            Toast.makeText(requireContext(), "Something went wrong!", Toast.LENGTH_SHORT).show();
            resetUi();
        });
    }

    private void startUpload(){
        captionContainer.setVisibility(View.VISIBLE);
        btnReset.setVisibility(View.VISIBLE);
    }
    private void stopUpload(){
        captionContainer.setVisibility(View.GONE);
        btnReset.setVisibility(View.GONE);
    }

    private File copyUriToFile(Uri uri) {
        try {
            File outputFile = new File(requireContext().getCacheDir(), "selected_" + System.currentTimeMillis() + ".mp4");

            try (InputStream in = requireContext().getContentResolver().openInputStream(uri); OutputStream out = new FileOutputStream(outputFile)) {

                byte[] buffer = new byte[8192];
                int len;

                while ((len = in.read(buffer)) != -1) {
                    out.write(buffer, 0, len);
                }
            }

            return outputFile;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}