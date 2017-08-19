package absensi.anif.its.ac.id.sikemastcforadmin.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import absensi.anif.its.ac.id.sikemastcforadmin.R;
import absensi.anif.its.ac.id.sikemastcforadmin.activity.tambah_data_wajah.TrainingDataWajahActivity;
import absensi.anif.its.ac.id.sikemastcforadmin.utilities.NetworkUtils;
import absensi.anif.its.ac.id.sikemastcforadmin.utilities.SikemasSessionManager;
import absensi.anif.its.ac.id.sikemastcforadmin.utilities.VolleySingleton;
import ch.zhaw.facerecognitionlibrary.Helpers.FileHelper;
import cn.pedant.SweetAlert.SweetAlertDialog;

public class MainActivity extends AppCompatActivity {

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    private final String TAG = MainActivity.class.getSimpleName();
    private List<String> fileNameList;
    private int numberOfFile;
    private SweetAlertDialog pDialog;
    private SikemasSessionManager session;
    private SliderLayout mSlider;

    private static final int PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        session = new SikemasSessionManager(this);
        session.checkLogin();

        if (Build.VERSION.SDK_INT >= 23) {
            if (checkCameraPermission() && checkStorageRPermission() && checkStorageWPermission()) {
                Log.e("permission", "Permission already granted.");
            } else {
                requestPermission();
            }
        }

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView title = (TextView) mToolbar.findViewById(R.id.toolbarTitle);
        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/FredokaOne-Regular.ttf");
        title.setTypeface(typeface);
        title.setText(R.string.app_short_name);
        setSupportActionBar(mToolbar);

        if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK)
                == Configuration.SCREENLAYOUT_SIZE_LARGE ||
                (getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK)
                        == Configuration.SCREENLAYOUT_SIZE_XLARGE) {
            mSlider = (SliderLayout) findViewById(R.id.slider);

            HashMap<String, String> image_maps = new HashMap<>();
            image_maps.put("Selamat Datang Mahasiswa Baru Teknik Informatika ITS 2017",
                    "https://www.dropbox.com/s/6pm7gthkhg8yuei/TeknikInformatikaITS%281%29.jpg?raw=1");
            image_maps.put("Aplikasi Kehadiran Mahasiswa Teknik Informatika ITS",
                    "https://www.dropbox.com/s/id2j5iug3ieivwc/aplikasi_sikemas.png?raw=1");
            image_maps.put("Jurusan Teknik Informatika ITS",
                    "https://www.dropbox.com/s/f9uab293kc27id6/JurusanTeknikInformatikaITS.jpg?raw=1");

            for (String name : image_maps.keySet()) {
                TextSliderView textSliderView = new TextSliderView(this);
                textSliderView
                        .description(name)
                        .image(image_maps.get(name))
                        .setScaleType(BaseSliderView.ScaleType.FitCenterCrop);
                mSlider.addSlider(textSliderView);
            }

            mSlider.setPresetTransformer(SliderLayout.Transformer.Default);
            mSlider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
            mSlider.setCustomAnimation(new DescriptionAnimation());
            mSlider.setDuration(7000);
        }

        Intent intent = getIntent();
        String training = intent.getStringExtra("training");
        if (training != null && !training.isEmpty()) {
            Toast.makeText(getApplicationContext(), training, Toast.LENGTH_SHORT).show();
            uploadFile(getAllEncodedFileFormat());
            intent.removeExtra("training");
        }

        Button btnLihatDaftarMhs = (Button) findViewById(R.id.btn_lihat_daftar_mahasiswa);
        Button btnTambahDataDiri = (Button) findViewById(R.id.btn_tambah_data_mahasiswa);
        Button btnTrainingData = (Button) findViewById(R.id.btn_generate_training_file);

        if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK)
                == Configuration.SCREENLAYOUT_SIZE_LARGE ||
                (getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK)
                        == Configuration.SCREENLAYOUT_SIZE_XLARGE) {
            btnTambahDataDiri.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(this, R.drawable.ic_141_exam_xlarge),
                    null, null, null);
            btnLihatDaftarMhs.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(this, R.drawable.ic_141_open_book_xlarge),
                    null, null, null);
            btnTrainingData.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(this, R.drawable.ic_141_browser_xlarge),
                    null, null, null);
        } else {
            btnTambahDataDiri.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(this, R.drawable.ic_141_exam),
                    null, null, null);
            btnLihatDaftarMhs.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(this, R.drawable.ic_141_open_book),
                    null, null, null);
            btnTrainingData.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(this, R.drawable.ic_141_browser),
                    null, null, null);
        }

        btnLihatDaftarMhs.setOnClickListener(action);
        btnTambahDataDiri.setOnClickListener(action);
        btnTrainingData.setOnClickListener(action);
    }

    View.OnClickListener action = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_lihat_daftar_mahasiswa:
                    Intent intentToDaftarMahasiswa = new Intent(MainActivity.this, DaftarMahasiswaActivity.class);
                    startActivity(intentToDaftarMahasiswa);
                    break;
                case R.id.btn_tambah_data_mahasiswa:
                    Intent intentToTambahDataDiri = new Intent(MainActivity.this, KelolaDataDiriActivity.class);
                    startActivity(intentToTambahDataDiri);
                    break;
                case R.id.btn_generate_training_file:
                    Intent intentToTrainingData = new Intent(MainActivity.this, TrainingDataWajahActivity.class);
                    startActivity(intentToTrainingData);
                    break;
            }
        }
    };

    public ArrayList<String> getAllEncodedFileFormat() {
        ArrayList<String> encodedFile = new ArrayList<>();
        fileNameList = new ArrayList<>();

        File dir = new File(FileHelper.SVM_PATH);
        File[] files = dir.listFiles();
        numberOfFile = files.length;
        Log.d("number of files", String.valueOf(numberOfFile));

        if (numberOfFile > 0) {
            for (File file : files) {
                try {
                    FileInputStream fileInputStreamReader = new FileInputStream(file);
                    byte[] bytes = new byte[(int) file.length()];
                    fileInputStreamReader.read(bytes);
                    encodedFile.add(Base64.encodeToString(bytes, Base64.DEFAULT));
                    fileNameList.add(file.getName());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return encodedFile;
        } else {
            return null;
        }
    }

    // Upload file to server
    private void uploadFile(final ArrayList<String> encodedFileList) {
        Log.d(TAG, "call uploadImages Method");
        showUploadLoading();
        StringRequest stringRequest;
        numberOfFile = 0;
        for (int i = 0; i < encodedFileList.size(); i++) {
            final int index = i;
            stringRequest = new StringRequest(Request.Method.POST, NetworkUtils.UPLOAD_DATA_TRAINING_SIKEMAS,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            //Disimissing the progress dialog
                            numberOfFile++;
                            //Showing toast message of the response
                            if (numberOfFile == encodedFileList.size()) {
                                showSuccessUploadResult();
                            }
                            Log.d("VolleyResponse", "Dapat ResponseVolley Upload Images");
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            // Dismissing the progress dialog
                            showErrorResult();
                        }
                    }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    // Get encoded Image
                    String file = encodedFileList.get(index);
                    String fileName = fileNameList.get(index);
                    Map<String, String> params = new HashMap<>();
                    // Adding parameters
                    params.put("binaryfile", file);
                    params.put("filename", fileName);

                    return params;
                }
            };
            //Adding request to the queue
            VolleySingleton.getmInstance(getApplicationContext()).addToRequestQueue(stringRequest);
        }
    }

    private void showUploadLoading() {
        pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText("Mengunggah Data Training..");
        pDialog.setCancelable(false);
        pDialog.show();
    }

    private void showSuccessUploadResult() {
        if (pDialog.isShowing()) {
            pDialog.dismiss();
            new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                    .setTitleText("Berhasil!")
                    .setContentText("Data training berhasil diunggah")
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            sweetAlertDialog.dismissWithAnimation();
                        }
                    })
                    .show();
        }
    }

    private void showErrorResult() {
        if (pDialog.isShowing()) {
            pDialog.dismiss();
            new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Gagal!")
                    .setContentText("Terjadi kesalahan server")
                    .show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                logout();
        }
        return true;
    }

    private void logout() {
        Toast.makeText(getApplicationContext(), "Logout user!", Toast.LENGTH_LONG).show();
        session.logoutUser();
        finish();
    }

    @Override
    protected void onStop() {
        // To prevent a memory leak on rotation, make sure to call stopAutoCycle()
        // on the slider before activity or fragment is destroyed
        if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK)
                == Configuration.SCREENLAYOUT_SIZE_LARGE ||
                (getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK)
                        == Configuration.SCREENLAYOUT_SIZE_XLARGE) {
            mSlider.stopAutoCycle();
        }
        super.onStop();
    }

    private boolean checkCameraPermission() {
        int result = ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.CAMERA);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private boolean checkStorageRPermission() {
        int result = ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private boolean checkStorageWPermission() {
        int result = ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.INTERNET,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE},
                PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this,
                            "Permission accepted", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(MainActivity.this,
                            "Permission denied", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }
}
