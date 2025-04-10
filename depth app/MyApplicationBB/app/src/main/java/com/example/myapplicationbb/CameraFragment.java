package com.example.myapplicationbb;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Rational;
import android.util.Size;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.io.InputStream;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.core.UseCaseGroup;
import androidx.camera.core.ViewPort;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.google.common.util.concurrent.ListenableFuture;
import com.example.myapplicationbb.network.MeasurementApi;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class CameraFragment extends Fragment implements SensorEventListener {
    private PreviewView previewView;
    private ImageView photoPreview;
    private ImageCapture imageCapture;
    private EditText distanceInput;
    private Button confirmPointsButton;
    private Uri currentPhotoUri;
    private List<float[]> selectedPoints;
    private float[] gravity;
    //private float[] savedGravity; // 用于保存拍照时的重力加速度值
    private boolean hasGravityData;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private final Executor executor = Executors.newSingleThreadExecutor();
    private static final int REQUEST_CODE_PERMISSIONS = 10;
    private static final int REQUEST_IMAGE_PICK = 100;
    private static final String[] REQUIRED_PERMISSIONS = new String[]{Manifest.permission.CAMERA};
    private ActivityResultLauncher<Intent> galleryLauncher;

    // 相机初始化相关
    private View cameraLoadingContainer;
    private boolean isCameraInitialized = false;
    private static final int CAMERA_INIT_DELAY = 300; // 延迟初始化时间（毫秒）
    private ProcessCameraProvider cameraProvider;
    private Handler mainHandler = new Handler(Looper.getMainLooper());
    private EditText ageInput;
    private EditText sexInput;
    private Boolean isMale;
    // 在 CameraFragment 类中添加以下成员变量
   // private List<AccelData> accelDataBuffer = new ArrayList<>(); // 存储加速度数据

    private static final float SIGMA = 0.2f; // 高斯函数标准差
    private boolean isSampling = false; // 是否正在采样
    private long samplingStartTime = 0; // 采样开始时间

    private static final int SAMPLING_WINDOW_MS = 1000; // 1秒采样窗口
    private float[] savedGravity = new float[3];
    private List<AccelData> accelDataBuffer = Collections.synchronizedList(new ArrayList<>());


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_camera, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        previewView = view.findViewById(R.id.preview_view);
        // 初始化相机加载UI
        cameraLoadingContainer = view.findViewById(R.id.camera_loading_container);
        // 初始时隐藏预览视图，显示加载UI
        previewView.setVisibility(View.GONE);
        cameraLoadingContainer.setVisibility(View.VISIBLE);

        photoPreview = view.findViewById(R.id.photo_preview);
        distanceInput = view.findViewById(R.id.distance_input);
        confirmPointsButton = view.findViewById(R.id.confirm_points_button);
        Button submitButton = view.findViewById(R.id.submit_button);
        Button retakeButton = view.findViewById(R.id.retake_button);

        ageInput = view.findViewById(R.id.age_input);
        sexInput = view.findViewById(R.id.sex_input);

        selectedPoints = new ArrayList<>();
        gravity = new float[3];
        savedGravity = new float[3];
        hasGravityData = false;

        sensorManager = (SensorManager) requireContext().getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // 添加图片点击事件监听
        photoPreview.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (selectedPoints.size() >= 2) {
                    Toast.makeText(requireContext(), "已选择两个点，请先确认或重新开始", Toast.LENGTH_SHORT).show();
                    return true;
                }

                // 记录触摸点坐标（临时）
                float[] touchPoint = new float[]{event.getX(), event.getY()};

                // 获取图片的实际尺寸，将触摸坐标转换为实际像素坐标
                try {
                    // 获取图片的实际尺寸
                    float imageWidth = photoPreview.getDrawable().getIntrinsicWidth();
                    float imageHeight = photoPreview.getDrawable().getIntrinsicHeight();
                    float viewWidth = photoPreview.getWidth();
                    float viewHeight = photoPreview.getHeight();

                    // 获取ImageView的ScaleType
                    ImageView.ScaleType scaleType = photoPreview.getScaleType();

                    // 计算归一化坐标
                    float normalizedX, normalizedY;

                    if (scaleType == ImageView.ScaleType.CENTER_CROP) {
                        // 计算缩放比例
                        float scale = Math.max(viewWidth / imageWidth, viewHeight / imageHeight);

                        // 计算图片在ImageView中的实际显示尺寸
                        float scaledWidth = imageWidth * scale;
                        float scaledHeight = imageHeight * scale;

                        // 计算图片在ImageView中的偏移量
                        float offsetX = (viewWidth - scaledWidth) / 2;
                        float offsetY = (viewHeight - scaledHeight) / 2;

                        // 将触摸坐标转换为图片上的归一化坐标
                        normalizedX = (touchPoint[0] - offsetX) / scaledWidth;
                        normalizedY = (touchPoint[1] - offsetY) / scaledHeight;
                    } else {
                        // 默认使用FIT_CENTER逻辑
                        float scale = Math.min(viewWidth / imageWidth, viewHeight / imageHeight);

                        // 计算图片在ImageView中的实际显示尺寸
                        float scaledWidth = imageWidth * scale;
                        float scaledHeight = imageHeight * scale;

                        // 计算图片在ImageView中的偏移量
                        float offsetX = (viewWidth - scaledWidth) / 2;
                        float offsetY = (viewHeight - scaledHeight) / 2;

                        // 将触摸坐标转换为图片上的归一化坐标
                        normalizedX = (touchPoint[0] - offsetX) / scaledWidth;
                        normalizedY = (touchPoint[1] - offsetY) / scaledHeight;
                    }

                    // 确保坐标在[0,1]范围内
                    normalizedX = Math.max(0, Math.min(normalizedX, 1));
                    normalizedY = Math.max(0, Math.min(normalizedY, 1));

                    // 计算实际像素坐标
                    int pixelX = Math.round(normalizedX * imageWidth);
                    int pixelY = Math.round(normalizedY * imageHeight);

                    // 创建包含归一化坐标和像素坐标的点
                    // 使用归一化坐标是为了在确认时能够准确转换为像素坐标
                    float[] point = new float[]{normalizedX, normalizedY};
                    selectedPoints.add(point);

                    // 在图片上显示选中的点
                    drawPointOnImage(point);

                    // 显示实际像素坐标
                    Toast.makeText(requireContext(), "已添加点: 像素坐标 (" + pixelX + ", " + pixelY + ")", Toast.LENGTH_SHORT).show();

                    // 记录日志
                    Log.d("CameraFragment", "选点: 归一化坐标 (" + normalizedX + ", " + normalizedY + ") -> 像素坐标 (" + pixelX + ", " + pixelY + ")");
                } catch (Exception e) {
                    // 如果转换失败，使用原始触摸坐标
                    selectedPoints.add(touchPoint);
                    drawPointOnImage(touchPoint);
                    Toast.makeText(requireContext(), "已添加点: (" + touchPoint[0] + ", " + touchPoint[1] + ")", Toast.LENGTH_SHORT).show();
                    Log.e("CameraFragment", "坐标转换失败: " + e.getMessage());
                }

                if (selectedPoints.size() == 2) {
                    confirmPointsButton.setVisibility(View.VISIBLE);
                }

                return true;
            }
            return false;
        });

        confirmPointsButton.setOnClickListener(v -> {
            submitButton.setVisibility(View.VISIBLE);
            confirmPointsButton.setEnabled(false);

            // 获取图片的实际尺寸，将归一化坐标转换为实际像素坐标
            try {

                // 使用与触摸选点相同的方式获取图片尺寸
                float imageWidth = photoPreview.getDrawable().getIntrinsicWidth();
                float imageHeight = photoPreview.getDrawable().getIntrinsicHeight();


                // 保存原始归一化坐标的副本（用于日志显示和后续恢复）
                List<float[]> normalizedPoints = new ArrayList<>();
                for (float[] point : selectedPoints) {
                    normalizedPoints.add(new float[]{point[0], point[1]});
                }

                // 将归一化坐标转换为实际像素坐标
                List<float[]> pixelPoints = new ArrayList<>();
                for (float[] point : selectedPoints) {
                    float[] pixelPoint = new float[2];
                    // 将归一化坐标转换为像素坐标
                    pixelPoint[0] = point[0] * imageWidth;
                    pixelPoint[1] = point[1] * imageHeight;
                    pixelPoints.add(pixelPoint);
                }

                // 用像素坐标替换归一化坐标
                selectedPoints = pixelPoints;

                // 记录坐标转换日志
                Log.d("CameraFragment", "坐标转换成功：图片尺寸 " + imageWidth + "x" + imageHeight);

                // 构建包含所有点坐标的消息
                StringBuilder pointsMessage = new StringBuilder();

                for (int i = 0; i < normalizedPoints.size(); i++) {
                    Log.d("CameraFragment", "点" + (i+1) + " 归一化坐标: (" + normalizedPoints.get(i)[0] + ", " + normalizedPoints.get(i)[1] + ") -> "
                            + "像素坐标: (" + selectedPoints.get(i)[0] + ", " + selectedPoints.get(i)[1] + ")");

                    // 添加到消息中
                    pointsMessage.append("点").append(i+1).append("像素坐标: (").append(Math.round(selectedPoints.get(i)[0]))
                            .append(", ").append(Math.round(selectedPoints.get(i)[1])).append(")\n");
                }

                // 显示所有点的像素坐标
                Toast.makeText(requireContext(), pointsMessage.toString().trim(), Toast.LENGTH_SHORT).show();
            }  catch (Exception e) {
                Log.e("CameraFragment", "处理坐标转换时出错: " + e.getMessage(), e);
                Toast.makeText(requireContext(), "坐标转换出错，将使用原始坐标", Toast.LENGTH_SHORT).show();
            }

            // 确认选点后，不再清除和重绘标记点，避免图片偏移
            // 只需要将现有标记点设为不可点击，防止用户移除
            ViewGroup parentView = (ViewGroup) photoPreview.getParent();
            if (parentView instanceof ViewGroup) {
                for (int i = 0; i < parentView.getChildCount(); i++) {
                    View child = parentView.getChildAt(i);
                    if (child instanceof ImageView && child.getId() != R.id.photo_preview) {
                        // 设置标记点不可点击
                        child.setClickable(false);
                    }
                }
            }

            // 记录日志
            Log.d("CameraFragment", "已确认选点，标记点已锁定");
        });

        submitButton.setOnClickListener(v -> submitMeasurement());

        // 添加重新拍摄按钮的点击事件
        retakeButton.setOnClickListener(v -> {
            // 保存年龄值，以便重新拍摄时不重置
            String ageValue = ageInput.getText().toString();

            // 重置UI状态
            previewView.setVisibility(View.VISIBLE);
            View photoPreviewContainer = requireView().findViewById(R.id.photo_preview_container);
            photoPreviewContainer.setVisibility(View.GONE);
            photoPreview.setVisibility(View.GONE);
            selectedPoints.clear();
            confirmPointsButton.setVisibility(View.GONE);
            confirmPointsButton.setEnabled(true);
            submitButton.setVisibility(View.GONE);

            // 重置输入框，但保留年龄值
            distanceInput.setText("");
            sexInput.setText("");
            ageInput.setText(ageValue);

            // 清除之前添加的标记
            ViewGroup parentView = (ViewGroup) photoPreview.getParent();
            if (parentView instanceof ViewGroup) {
                for (int i = 0; i < parentView.getChildCount(); i++) {
                    View child = parentView.getChildAt(i);
                    if (child instanceof ImageView && child.getId() != R.id.photo_preview) {
                        parentView.removeView(child);
                        i--;
                    }
                }
            }

            // 显示加载UI
            cameraLoadingContainer.setVisibility(View.VISIBLE);
            previewView.setVisibility(View.GONE);

            // 重新初始化相机
            isCameraInitialized = false;
            // 延迟一小段时间再重新启动相机，避免资源冲突
            mainHandler.postDelayed(() -> {
                if (isAdded() && !requireActivity().isFinishing()) {
                    startCamera();
                }
            }, 500); // 使用稍长的延迟确保资源释放完毕

        });

        galleryLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        currentPhotoUri = result.getData().getData();
                        showPhotoPreview();
                    }
                });

        // 检查相机权限并启动相机
        if (allPermissionsGranted()) {
            // 延迟一小段时间再启动相机，确保UI已完全加载
            mainHandler.postDelayed(this::startCamera, 100);
        } else {
            ActivityCompat.requestPermissions(requireActivity(), REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }

        view.findViewById(R.id.capture_button).setOnClickListener(v -> takePhoto());
        view.findViewById(R.id.gallery_button).setOnClickListener(v -> openGallery());
    }

    /**
     * 获取最佳相机分辨率
     * 根据预览视图的宽高比筛选出最匹配的相机传感器支持的分辨率
     */
    private Size getBestResolution(float targetRatio) {
        try {
            CameraManager cameraManager = (CameraManager) requireContext().getSystemService(Context.CAMERA_SERVICE);
            String[] cameraIds = cameraManager.getCameraIdList();
            String backCameraId = null;

            // 查找后置相机ID
            for (String id : cameraIds) {
                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(id);
                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_BACK) {
                    backCameraId = id;
                    break;
                }
            }

            if (backCameraId == null) {
                Log.e("CameraFragment", "找不到后置相机");
                return null;
            }

            // 获取相机支持的分辨率列表
            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(backCameraId);
            Size[] outputSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                    .getOutputSizes(android.graphics.ImageFormat.JPEG);

            if (outputSizes == null || outputSizes.length == 0) {
                Log.e("CameraFragment", "无法获取相机支持的分辨率");
                return null;
            }

            // 按分辨率从高到低排序
            Arrays.sort(outputSizes, (a, b) -> Integer.compare(b.getWidth() * b.getHeight(), a.getWidth() * a.getHeight()));

            // 找到与目标宽高比最接近的分辨率
            Size bestSize = null;
            float minRatioDiff = Float.MAX_VALUE;

            for (Size size : outputSizes) {
                float ratio = (float) size.getWidth() / size.getHeight();
                float ratioDiff = Math.abs(ratio - targetRatio);

                if (ratioDiff < minRatioDiff) {
                    minRatioDiff = ratioDiff;
                    bestSize = size;
                }

                // 如果找到完全匹配的，直接返回
                if (minRatioDiff < 0.01f) {
                    break;
                }
            }

            if (bestSize != null) {
                Log.d("CameraFragment", "选择的最佳分辨率: " + bestSize.getWidth() + "x" + bestSize.getHeight() +
                        ", 宽高比: " + ((float) bestSize.getWidth() / bestSize.getHeight()));
            }

            return bestSize;
        } catch (Exception e) {
            Log.e("CameraFragment", "获取相机分辨率失败: " + e.getMessage());
            return null;
        }
    }

    private void startCamera() {
        // 显示加载UI
        previewView.setVisibility(View.GONE);
        cameraLoadingContainer.setVisibility(View.VISIBLE);

        // 延迟初始化相机，给系统一些时间准备相机资源
        mainHandler.postDelayed(() -> {
            if (!isAdded() || getActivity() == null || getActivity().isFinishing()) {
                return; // Fragment已经分离，不继续初始化
            }

            ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());

            cameraProviderFuture.addListener(() -> {
                try {
                    if (!isAdded()) return; // 再次检查Fragment是否仍然附加到Activity

                    cameraProvider = cameraProviderFuture.get();

                    // 获取预览视图的宽高比
                    float previewRatio = (float) previewView.getWidth() / previewView.getHeight();
                    if (previewView.getWidth() == 0 || previewView.getHeight() == 0) {
                        // 如果预览视图尚未测量，使用屏幕宽高比
                        previewRatio = (float) requireActivity().getWindow().getDecorView().getWidth() /
                                requireActivity().getWindow().getDecorView().getHeight();
                    }

                    // 获取最佳分辨率
                    Size bestResolution = getBestResolution(previewRatio);

                    // 创建预览用例
                    Preview.Builder previewBuilder = new Preview.Builder();
                    if (bestResolution != null) {
                        previewBuilder.setTargetResolution(bestResolution);
                    }
                    Preview preview = previewBuilder.build();
                    preview.setSurfaceProvider(previewView.getSurfaceProvider());

                    // 创建图像捕获用例
                    ImageCapture.Builder imageCaptureBuilder = new ImageCapture.Builder()
                            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY);
                    if (bestResolution != null) {
                        imageCaptureBuilder.setTargetResolution(bestResolution);
                    }
                    imageCapture = imageCaptureBuilder.build();

                    CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                    // 创建ViewPort，确保预览和捕获使用相同的视图区域
                    ViewPort viewPort = new ViewPort.Builder(new Rational(previewView.getWidth(), previewView.getHeight()),
                            previewView.getDisplay().getRotation()).build();

                    // 创建UseCaseGroup，将预览和图像捕获绑定到同一视图区域
                    UseCaseGroup useCaseGroup = new UseCaseGroup.Builder()
                            .addUseCase(preview)
                            .addUseCase(imageCapture)
                            .setViewPort(viewPort)
                            .build();

                    cameraProvider.unbindAll();
                    cameraProvider.bindToLifecycle(getViewLifecycleOwner(), cameraSelector, useCaseGroup);

                    // 相机初始化成功，隐藏加载UI，显示预览
                    previewView.setVisibility(View.VISIBLE);
                    cameraLoadingContainer.setVisibility(View.GONE);
                    isCameraInitialized = true;

                } catch (ExecutionException | InterruptedException e) {
                    // 更详细的错误处理
                    String errorMessage = "无法启动相机: " + e.getMessage();
                    if (isAdded()) {
                        new AlertDialog.Builder(requireContext())
                                .setTitle("相机初始化失败")
                                .setMessage(errorMessage)
                                .setPositiveButton("重试", (dialog, which) -> {
                                    startCamera(); // 重试初始化相机
                                })
                                .setNegativeButton("取消", (dialog, which) -> {
                                    // 返回上一个Fragment
                                    if (getActivity() != null) {
                                        getActivity().getSupportFragmentManager().popBackStack();
                                    }
                                })
                                .show();
                    }
                }
            }, ContextCompat.getMainExecutor(requireContext()));
        }, CAMERA_INIT_DELAY);
    }

    private void takePhoto() {
        if (imageCapture == null) return;

        // 启动加速度采样
        startAccelSampling();

        File photoFile = new File(requireContext().getExternalCacheDir(),
                new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.CHINA)
                        .format(System.currentTimeMillis()) + ".jpg");

        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(outputOptions, executor,
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        // 等待采样完成（最多1.1秒）
                        waitForSamplingCompletion();

                        Uri savedUri = Uri.fromFile(photoFile);
                        requireActivity().runOnUiThread(() -> {
                            showSamplingResult();
                            currentPhotoUri = savedUri;
                            showPhotoPreview();
                        });
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(requireContext(), "拍照失败", Toast.LENGTH_SHORT).show());
                    }
                });
    }

    private void startAccelSampling() {synchronized (accelDataBuffer) {
        accelDataBuffer.clear();
        isSampling = true;
        samplingStartTime = System.currentTimeMillis();
    }

        // 设置采样超时
        mainHandler.postDelayed(() -> {
            synchronized (accelDataBuffer) {
                isSampling = false;
                calculateWeightedAccel();
            }
        }, SAMPLING_WINDOW_MS + 100); // 增加100ms容差
    }

    private void calculateWeightedAccel() { if (accelDataBuffer.isEmpty()) {
        Log.w("CameraFragment", "加速度采样数据为空");
        savedGravity = new float[3];
        return;
    }

        // 计算中心时间（采样窗口中间点）
        final long centerTime = samplingStartTime + SAMPLING_WINDOW_MS / 2;

        float[] sum = new float[3];
        float totalWeight = 0f;

        for (AccelData data : accelDataBuffer) {
            // 转换为相对于中心时间的秒数
            float t = (data.timestamp - centerTime) / 1000f;

            // 计算高斯权重
            float weight = (float) Math.exp(-(t * t) / (2 * SIGMA * SIGMA));

            // 累加各轴数据
            for (int i = 0; i < 3; i++) {
                sum[i] += data.values[i] * weight;
            }
            totalWeight += weight;
        }

        // 计算加权平均值
        if (totalWeight > 0) {
            savedGravity = new float[]{
                    sum[0] / totalWeight,
                    sum[1] / totalWeight,
                    sum[2] / -totalWeight
            };
            Log.d("CameraFragment", String.format(
                    "去噪加速度: X=%.4f, Y=%.4f, Z=%.4f (基于%d个样本)",
                    savedGravity[0], savedGravity[1], savedGravity[2],
                    accelDataBuffer.size()
            ));
        }
    }

    private void showSamplingResult() {
        Toast.makeText(requireContext(), "照片已保存", Toast.LENGTH_SHORT).show();

        // 保持和原版完全一致的显示格式
        if (savedGravity != null && savedGravity.length >= 3) {
            String gravityInfo = String.format(Locale.CHINA,
                    "重力加速度: X=%.4f, Y=%.4f, Z=%.4f",  // 保持原有文字描述
                    savedGravity[0],
                    savedGravity[1],
                    savedGravity[2]
            );

            // 使用原版Toast样式（不修改字体大小和布局）
            Toast.makeText(requireContext(), gravityInfo, Toast.LENGTH_LONG).show();
        } else {
            // 保持原有错误提示
            Toast.makeText(requireContext(), "未获取到重力加速度数据", Toast.LENGTH_LONG).show();
        }
    }


    private void waitForSamplingCompletion() {
        int retry = 0;
        while (isSampling && retry++ < 20) { // 最多等待1秒
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    //private void waitForSamplingCompletion() {
       // final long start = System.currentTimeMillis();
     //   while (isSampling && (System.currentTimeMillis() - start < 1100)) {
       //     try {
       //        Thread.sleep(50);
          //  } catch (InterruptedException e) {
           //     Thread.currentThread().interrupt();
           // }
       // }
   // }

    private void openGallery() {
        // 确保确认选点按钮处于可用状态
        confirmPointsButton.setEnabled(true);
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    private void showPhotoPreview() {
        previewView.setVisibility(View.GONE);
        View photoPreviewContainer = requireView().findViewById(R.id.photo_preview_container);
        photoPreviewContainer.setVisibility(View.VISIBLE);
        photoPreview.setVisibility(View.VISIBLE);
        photoPreview.setImageURI(currentPhotoUri);

        // 确保确认选点按钮处于可用状态
        confirmPointsButton.setEnabled(true);

        // 统一使用FIT_CENTER作为ScaleType，与预览视图保持一致
        photoPreview.setScaleType(ImageView.ScaleType.FIT_CENTER);
        Log.d("CameraFragment", "设置照片预览ScaleType为FIT_CENTER，与相机预览保持一致");

        try {
            // 获取图片的实际尺寸（仅用于日志记录）
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            InputStream inputStream = requireContext().getContentResolver().openInputStream(currentPhotoUri);
            BitmapFactory.decodeStream(inputStream, null, options);
            inputStream.close();

            int imageWidth = options.outWidth;
            int imageHeight = options.outHeight;
            Log.d("CameraFragment", "照片尺寸: " + imageWidth + "x" + imageHeight + ", 宽高比: " +
                    ((float) imageWidth / imageHeight));
        } catch (Exception e) {
            Log.e("CameraFragment", "获取照片尺寸失败: " + e.getMessage());
        }

        // 清除之前的选点
        selectedPoints.clear();

        // 清除之前添加的标记
        ViewGroup parentView = (ViewGroup) photoPreview.getParent();
        if (parentView instanceof ViewGroup) {
            // 找到所有标记并移除
            for (int i = 0; i < parentView.getChildCount(); i++) {
                View child = parentView.getChildAt(i);
                if (child instanceof ImageView && child.getId() != R.id.photo_preview) {
                    parentView.removeView(child);
                    i--; // 因为移除了一个元素，所以索引需要减一
                }
            }
        }
    }

    private void submitMeasurement() {
        if (selectedPoints.size() != 2 || currentPhotoUri == null || !hasGravityData) {
            Toast.makeText(requireContext(), "请确保已选择两个点并获取传感器数据", Toast.LENGTH_SHORT).show();
            return;
        }

        String distanceStr = distanceInput.getText().toString();
        String ageStr = ageInput.getText().toString();
        Boolean isMale = Boolean.valueOf(sexInput.getText().toString());

        if (distanceStr.isEmpty()) {
            Toast.makeText(requireContext(), "请输入拍摄距离", Toast.LENGTH_SHORT).show();
            return;
        }
        if (ageStr.isEmpty()) {
            Toast.makeText(requireContext(), "请输入年龄", Toast.LENGTH_SHORT).show();
            return;
        }

     

        float knownDistance = Float.parseFloat(distanceStr);
        int age = Integer.parseInt(ageStr);
      

        // 显示提交参数的日志
        // 注意：此时selectedPoints已经在confirmPointsButton点击事件中转换为像素坐标
        // 只显示参数确认对话框，在用户点击"确认提交"按钮后才会真正提交数据
        showSubmitParamsDialog(currentPhotoUri, selectedPoints, savedGravity, knownDistance, age, isMale);
    }



    @Override
    public void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        // 如果相机尚未初始化且Fragment已经可见，则初始化相机
        if (!isCameraInitialized && isAdded() && getUserVisibleHint()) {
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                ActivityCompat.requestPermissions(requireActivity(), REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        // 在Fragment停止时释放相机资源
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 移除所有待处理的Handler回调，防止内存泄漏
        if (mainHandler != null) {
            mainHandler.removeCallbacksAndMessages(null);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            // 更新实时重力数据
            System.arraycopy(event.values, 0, gravity, 0, 3);
            hasGravityData = true;

            // 如果正在采样，记录数据
            if (isSampling) {
                synchronized (accelDataBuffer) {
                    accelDataBuffer.add(new AccelData(
                            System.currentTimeMillis(),
                            event.values
                    ));
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // 不需要处理精度变化
    }

    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(requireContext(), permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                Toast.makeText(requireContext(), "需要相机权限才能使用此功能", Toast.LENGTH_SHORT).show();
                requireActivity().finish();
            }
        }
    }



    private void drawPointOnImage(float[] point) {
        try {
            // 获取photoPreview的父视图 - MaterialCardView
            ViewGroup cardView = (ViewGroup) photoPreview.getParent();
            if (cardView == null) return;

            // 设置标记的大小
            int markerSize = getResources().getDimensionPixelSize(R.dimen.point_marker_size);


            // 调整点的位置，考虑图片在ImageView中的实际显示情况
            float[] adjustedPoint;

            // 检查点是否已经是归一化坐标（确认选点后的情况）
            if (point[0] >= 0 && point[0] <= 1 && point[1] >= 0 && point[1] <= 1) {
                // 点已经是归一化坐标，直接转换为屏幕坐标
                float imageWidth = photoPreview.getDrawable().getIntrinsicWidth();
                float imageHeight = photoPreview.getDrawable().getIntrinsicHeight();
                float viewWidth = photoPreview.getWidth();
                float viewHeight = photoPreview.getHeight();

                ImageView.ScaleType scaleType = photoPreview.getScaleType();

                if (scaleType == ImageView.ScaleType.CENTER_CROP) {
                    float scale = Math.max(viewWidth / imageWidth, viewHeight / imageHeight);
                    float scaledWidth = imageWidth * scale;
                    float scaledHeight = imageHeight * scale;
                    float offsetX = (viewWidth - scaledWidth) / 2;
                    float offsetY = (viewHeight - scaledHeight) / 2;

                    adjustedPoint = new float[]{
                            offsetX + point[0] * scaledWidth,
                            offsetY + point[1] * scaledHeight
                    };
                } else {
                    // 默认使用FIT_CENTER逻辑
                    float scale = Math.min(viewWidth / imageWidth, viewHeight / imageHeight);
                    float scaledWidth = imageWidth * scale;
                    float scaledHeight = imageHeight * scale;
                    float offsetX = (viewWidth - scaledWidth) / 2;
                    float offsetY = (viewHeight - scaledHeight) / 2;

                    adjustedPoint = new float[]{
                            offsetX + point[0] * scaledWidth,
                            offsetY + point[1] * scaledHeight
                    };
                }
            } else {
                // 点是原始像素坐标，需要调整
                adjustedPoint = adjustPointToPhotoPreview(point);
            }

            // 在图片上绘制选中的点
            ImageView marker = new ImageView(requireContext());
            marker.setImageResource(R.drawable.ic_point_marker);
            marker.setId(View.generateViewId());

            // 创建布局参数
            ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(
                    markerSize,
                    markerSize
            );

            // 将标记添加到与photoPreview相同的父视图中
            cardView.addView(marker, params);

            // 设置标记的位置，使其覆盖在photoPreview上的触摸点
            marker.setX(adjustedPoint[0] - markerSize / 2);
            marker.setY(adjustedPoint[1] - markerSize / 2);

            // 确保标记在最上层显示
            marker.bringToFront();
            // 只刷新标记视图，避免整个cardView重绘导致图片偏移
            marker.invalidate();

            // 防止添加标记导致图片偏移
            photoPreview.setScaleType(photoPreview.getScaleType()); // 保持当前缩放类型不变

            // 添加标记点击事件，允许用户移除标记
            marker.setOnClickListener(v -> {
                // 如果已经确认选点，则不允许移除
                if (!confirmPointsButton.isEnabled()) {
                    Toast.makeText(requireContext(), "已确认选点，无法移除", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 从视图中移除标记
                cardView.removeView(marker);

                // 从选中点列表中移除对应的点
                for (int i = 0; i < selectedPoints.size(); i++) {
                    float[] p = selectedPoints.get(i);
                    // 使用近似比较，因为浮点数可能有精度误差
                    if (Math.abs(p[0] - point[0]) < 0.01 && Math.abs(p[1] - point[1]) < 0.01) {
                        selectedPoints.remove(i);
                        break;
                    }
                }

                // 更新UI状态
                if (selectedPoints.size() < 2) {
                    confirmPointsButton.setVisibility(View.GONE);
                }
            });
        } catch (Exception e) {
            Toast.makeText(requireContext(), "添加标记失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private float[] adjustPointToPhotoPreview(float[] point) {
        // 获取图片在ImageView中的实际显示区域
        if (photoPreview.getDrawable() == null) {
            return point; // 如果没有图片，直接返回原始坐标
        }

        float imageWidth = photoPreview.getDrawable().getIntrinsicWidth();
        float imageHeight = photoPreview.getDrawable().getIntrinsicHeight();
        float viewWidth = photoPreview.getWidth();
        float viewHeight = photoPreview.getHeight();

        if (imageWidth <= 0 || imageHeight <= 0 || viewWidth <= 0 || viewHeight <= 0) {
            return point; // 防止除以零错误
        }

        // 获取ImageView的ScaleType
        ImageView.ScaleType scaleType = photoPreview.getScaleType();

        // 根据不同的ScaleType计算坐标
        if (scaleType == ImageView.ScaleType.CENTER_CROP) {
            // 计算缩放比例
            float scale = Math.max(viewWidth / imageWidth, viewHeight / imageHeight);

            // 计算图片在ImageView中的实际显示尺寸
            float scaledWidth = imageWidth * scale;
            float scaledHeight = imageHeight * scale;

            // 计算图片在ImageView中的偏移量
            float offsetX = (viewWidth - scaledWidth) / 2;
            float offsetY = (viewHeight - scaledHeight) / 2;

            // 将触摸坐标转换为图片上的归一化坐标
            float normalizedX = (point[0] - offsetX) / scaledWidth;
            float normalizedY = (point[1] - offsetY) / scaledHeight;

            // 确保坐标在[0,1]范围内
            normalizedX = Math.max(0, Math.min(normalizedX, 1));
            normalizedY = Math.max(0, Math.min(normalizedY, 1));

            // 不再需要更新selectedPoints，因为在选点时已经保存了归一化坐标

            // 返回调整后的像素坐标（用于显示标记）
            return new float[]{offsetX + normalizedX * scaledWidth, offsetY + normalizedY * scaledHeight};
        } else {
            // 默认使用FIT_CENTER逻辑
            // 计算缩放比例
            float scale = Math.min(viewWidth / imageWidth, viewHeight / imageHeight);

            // 计算图片在ImageView中的实际显示尺寸
            float scaledWidth = imageWidth * scale;
            float scaledHeight = imageHeight * scale;

            // 计算图片在ImageView中的偏移量
            float offsetX = (viewWidth - scaledWidth) / 2;
            float offsetY = (viewHeight - scaledHeight) / 2;

            // 将触摸坐标转换为图片上的归一化坐标
            float normalizedX = (point[0] - offsetX) / scaledWidth;
            float normalizedY = (point[1] - offsetY) / scaledHeight;

            // 确保坐标在[0,1]范围内
            normalizedX = Math.max(0, Math.min(normalizedX, 1));
            normalizedY = Math.max(0, Math.min(normalizedY, 1));

            // 不再需要更新selectedPoints，因为在选点时已经保存了归一化坐标

            // 返回调整后的像素坐标（用于显示标记）
            return new float[]{offsetX + normalizedX * scaledWidth, offsetY + normalizedY * scaledHeight};
        }
    }


    /**
     * 显示提交参数的对话框
     * @param imageUri 图片URI
     * @param points 选中的点
     * @param gravityData 重力传感器数据
     * @param distance 已知距离
     * @param age 年龄
     * @param isMale 体重
     */
    private void showSubmitParamsDialog(Uri imageUri, List<float[]> points, float[] gravityData, float distance, int age, Boolean isMale) {
        // 创建一个包含ScrollView的TextView，以便显示大量文本
        ScrollView scrollView = new ScrollView(requireContext());
        TextView textView = new TextView(requireContext());
        textView.setPadding(30, 30, 30, 30);
        scrollView.addView(textView);

        // 构建参数信息文本
        StringBuilder paramsInfo = new StringBuilder();
        paramsInfo.append("===== 提交参数信息 =====\n\n");

        // 图片信息
        paramsInfo.append("【图片信息】\n");
        paramsInfo.append("URI: ").append(imageUri.toString()).append("\n\n");

        // 选中的点信息
        paramsInfo.append("【选中的点坐标】\n");
        for (int i = 0; i < points.size(); i++) {
            float[] point = points.get(i);
            paramsInfo.append("点").append(i + 1).append(": (");
            paramsInfo.append(String.format(Locale.CHINA, "%.2f", point[0])).append(", ");
            paramsInfo.append(String.format(Locale.CHINA, "%.2f", point[1])).append(")\n");
        }
        paramsInfo.append("\n");

        // 如果是像素坐标，显示额外信息
        if (points.size() > 0 && (points.get(0)[0] > 1 || points.get(0)[1] > 1)) {
            paramsInfo.append("【注意】坐标已转换为实际像素值\n\n");
        }

        // 重力传感器数据
        paramsInfo.append("【重力传感器数据】\n");
        paramsInfo.append("X: ").append(String.format(Locale.CHINA, "%.4f", gravityData[0])).append("\n");
        paramsInfo.append("Y: ").append(String.format(Locale.CHINA, "%.4f", gravityData[1])).append("\n");
        paramsInfo.append("Z: ").append(String.format(Locale.CHINA, "%.4f", gravityData[2])).append("\n\n");

        // 已知距离
        paramsInfo.append("【已知距离】\n");
        paramsInfo.append(distance).append(" 厘米\n\n");

        // 年龄和体重
        paramsInfo.append("【年龄】\n");
        paramsInfo.append(age).append(" 岁\n\n");



        paramsInfo.append("【性别】\n");
        paramsInfo.append(isMale).append(" 男或女\n");

        // 设置文本
        textView.setText(paramsInfo.toString());

        // 创建并显示对话框
        new AlertDialog.Builder(requireContext())
                .setTitle("测量参数确认")
                .setView(scrollView)
                .setPositiveButton("确认提交", (dialog, which) -> {
                    // 只有在用户点击确认提交按钮后才真正发送数据
                    MeasurementApi api = new MeasurementApi();
                    api.submitMeasurement(
                            requireContext(),
                            imageUri,
                            points,
                            gravityData,
                            distance,
                            age,
                            isMale,
                            new Callback() {
                                @Override
                                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                    requireActivity().runOnUiThread(() -> {
                                        // 使用AlertDialog代替Toast，显示更详细的错误信息
                                        new AlertDialog.Builder(requireContext())
                                                .setTitle("提交失败")
                                                .setMessage("错误信息：" + e.getMessage())
                                                .setPositiveButton("确定", null)
                                                .show();
                                    });
                                }

                                @Override
                                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                    final String responseBody = response.body().string();
                                    requireActivity().runOnUiThread(() -> {
                                        try {
                                            // 检查Fragment是否仍然附加到Activity
                                            if (!isAdded()) {
                                                return;
                                            }

                                            // 创建并显示结果Fragment
                                            MeasurementResultFragment resultFragment = new MeasurementResultFragment();
                                            Bundle args = new Bundle();
                                            args.putString("measurement_result", responseBody);
                                            // 添加年龄和性别信息
                                            args.putInt("age", age);
                                            args.putBoolean("isMale", isMale);
                                            resultFragment.setArguments(args);

                                            // 导航到结果页面
                                            if (getActivity() != null && !getActivity().isFinishing()) {
                                                getActivity().getSupportFragmentManager()
                                                        .beginTransaction()
                                                        .replace(R.id.nav_host_fragment, resultFragment)
                                                        .addToBackStack(null)
                                                        .commit();
                                            }

                                            // 重置UI状态
                                            previewView.setVisibility(View.VISIBLE);
                                            View photoPreviewContainer = requireView().findViewById(R.id.photo_preview_container);
                                            photoPreviewContainer.setVisibility(View.GONE);
                                            photoPreview.setVisibility(View.GONE);
                                            selectedPoints.clear();
                                            confirmPointsButton.setVisibility(View.GONE);
                                            confirmPointsButton.setEnabled(true);
                                            distanceInput.setText("");
                                        } catch (Exception e) {
                                            Toast.makeText(requireContext(), "处理结果时出错: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }
                    );
                })
                .setNegativeButton("取消", null)
                .show();
    }
}