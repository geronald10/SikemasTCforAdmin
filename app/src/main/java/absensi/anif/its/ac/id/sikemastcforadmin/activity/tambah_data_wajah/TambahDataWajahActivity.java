package absensi.anif.its.ac.id.sikemastcforadmin.activity.tambah_data_wajah;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.ImageView;
import android.widget.TextView;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import absensi.anif.its.ac.id.sikemastcforadmin.activity.MainActivity;
import absensi.anif.its.ac.id.sikemastcforadmin.R;
import absensi.anif.its.ac.id.sikemastcforadmin.utilities.LibraryPreference;
import ch.zhaw.facerecognitionlibrary.Helpers.CustomCameraView;
import ch.zhaw.facerecognitionlibrary.Helpers.FileHelper;
import ch.zhaw.facerecognitionlibrary.Helpers.MatName;
import ch.zhaw.facerecognitionlibrary.Helpers.MatOperation;
import ch.zhaw.facerecognitionlibrary.PreProcessor.PreProcessorFactory;

public class TambahDataWajahActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    public static final int TIME = 0;
    public static final int MANUALLY = 1;
    private CustomCameraView mAddSetWajahView;
    private long timerDiff;
    private long lastTime;
    private PreProcessorFactory ppF;
    private FileHelper fh;
    private File privatePath;
    private String folder;
    private String subfolder;
    private String name;
    private int total;
    private int numberOfPictures;
    private int method;
    private boolean capturePressed;
    private boolean front_camera;
    private boolean night_portrait;
    private int exposure_compensation;

    //    private Switch switchCaptureMode;
//    private ImageButton btnCapture;
    private ImageView capturedImagePreview;
    private TextView counter;
    private TextView userTerlogin;
    private LibraryPreference preferences;
    private SharedPreferences flagStatus;

    static {
        if (!OpenCVLoader.initDebug()) {
            // Handle initialization error
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        setContentView(R.layout.activity_tambah_data_wajah);
        privatePath = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        flagStatus = getSharedPreferences("flag_status", 0);

        preferences = new LibraryPreference(this);
        preferences.createCameraSettings();
        HashMap<String, String> cameraSettings = preferences.getCameraSettings();

        Intent intent = getIntent();
        folder = intent.getStringExtra("Folder");
        if (folder.equals("Test")) {
            subfolder = intent.getStringExtra("Subfolder");
        }
        name = intent.getStringExtra("user_terlogin");
        method = intent.getIntExtra("method", 0);

        if (method == MANUALLY) {
            capturePressed = true;
        } else {
            capturePressed = false;
        }

        fh = new FileHelper();
        total = 0;
        numberOfPictures = 10;
        lastTime = new Date().getTime();

        timerDiff = Integer.valueOf(cameraSettings.get(LibraryPreference.KEY_TIMERDIFF));
        mAddSetWajahView = (CustomCameraView) findViewById(R.id.AddSetWajahPreview);
        front_camera = Boolean.valueOf(cameraSettings.get(LibraryPreference.KEY_FRONT_CAMERA));
        night_portrait = Boolean.valueOf(cameraSettings.get(LibraryPreference.KEY_NIGHT_PORTRAIT_MODE));
        exposure_compensation = Integer.valueOf(cameraSettings.get(LibraryPreference.KEY_EXPOSURE));

        if (front_camera) {
            mAddSetWajahView.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_FRONT);
        } else {
            mAddSetWajahView.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_BACK);
        }
        mAddSetWajahView.setVisibility(SurfaceView.VISIBLE);
        mAddSetWajahView.setCvCameraViewListener(this);

        int maxCameraViewWidth = Integer.parseInt(cameraSettings.get(LibraryPreference.KEY_CAMERA_VIEW_WIDTH));
        int maxCameraViewHeight = Integer.parseInt(cameraSettings.get(LibraryPreference.KEY_CAMERA_VIEW_HEIGHT));
        mAddSetWajahView.setMaxFrameSize(maxCameraViewWidth, maxCameraViewHeight);

        userTerlogin = (TextView) findViewById(R.id.tv_user_detail);
        capturedImagePreview = (ImageView) findViewById(R.id.iv_captured_image);
        counter = (TextView) findViewById(R.id.tv_counter);
        counter.setText("0");
        userTerlogin.setText(name);
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        if (night_portrait) {
            mAddSetWajahView.setNightPortrait();
        }

        if (exposure_compensation != 50 && 0 <= exposure_compensation && exposure_compensation <= 100)
            mAddSetWajahView.setExposure(exposure_compensation);
    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Mat imgRgba = inputFrame.rgba();
        Mat imgCopy = new Mat();
        imgRgba.copyTo(imgCopy);
        // Selfie / Mirror mode
        if (front_camera) {
            Core.flip(imgRgba, imgRgba, 1);
        }

        long time = new Date().getTime();
        if ((method == MANUALLY) || (method == TIME) && (lastTime + timerDiff < time)) {
            lastTime = time;
            // Check that only 1 face is found. Skip if any or more than 1 are found.
            final List<Mat> images = ppF.getCroppedImage(imgCopy);
            if (images != null && images.size() == 1) {
                Mat img = images.get(0);
                if (img != null) {
                    Rect[] faces = ppF.getFacesForRecognition();
                    //Only proceed if 1 face has been detected, ignore if 0 or more than 1 face have been detected
                    if ((faces != null) && (faces.length == 1)) {
                        faces = MatOperation.rotateFaces(imgRgba, faces, ppF.getAngleForRecognition());
                        if (((method == MANUALLY) && capturePressed) || (method == TIME)) {
                            final MatName m = new MatName(name + "_" + total, img);
                            if (folder.equals("Test")) {
                                String wholeFolderPath = fh.TEST_PATH + name + "/" + subfolder;
                                new File(wholeFolderPath).mkdirs();
                                fh.saveMatToImage(m, wholeFolderPath + "/");
                            } else {
                                String wholeFolderPath = fh.TRAINING_PATH + name;
                                new File(privatePath, wholeFolderPath).mkdirs();
                                fh.saveMatToImage(m, wholeFolderPath + "/");
                            }

                            for (int i = 0; i < faces.length; i++) {
                                MatOperation.drawRectangleAndLabelOnPreview(imgRgba, faces[i], String.valueOf(total + 1), front_camera);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Mat mat = m.getMat();
                                        Bitmap bitmap = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
                                        Utils.matToBitmap(mat, bitmap);
                                        capturedImagePreview.setImageBitmap(bitmap);
                                    }
                                });
                            }
                            total++;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    counter.setText(String.valueOf(total));
                                }
                            });

                            // Stop after numberOfPictures (settings option)
                            if (total == numberOfPictures) {
                                Intent returnIntent = new Intent(TambahDataWajahActivity.this, MainActivity.class);
                                Log.d("total", String.valueOf(total));
                                Log.d("numberOfPicture", String.valueOf(numberOfPictures));
                                returnIntent.putExtra("result_message", "Berhasil menambahkan data wajah");
                                returnIntent.putExtra("number_of_pictures", total);
                                setResult(Activity.RESULT_OK, returnIntent);
                                Log.d("returnintent", String.valueOf(Activity.RESULT_OK));
                                finish();
                            }
                            capturePressed = false;
                        } else {
                            for (int i = 0; i < faces.length; i++) {
                                MatOperation.drawRectangleOnPreview(imgRgba, faces[i], front_camera);
                            }
                        }
                    }
                }
            }
        }
        return imgRgba;
    }

    @Override
    public void onResume() {
        super.onResume();

        ppF = new PreProcessorFactory(this);
        mAddSetWajahView.enableView();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mAddSetWajahView != null)
            mAddSetWajahView.disableView();
    }

    public void onDestroy() {
        super.onDestroy();
        if (mAddSetWajahView != null)
            mAddSetWajahView.disableView();
    }
}