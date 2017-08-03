package absensi.anif.its.ac.id.sikemastcforadmin.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import absensi.anif.its.ac.id.sikemastcforadmin.R;
import absensi.anif.its.ac.id.sikemastcforadmin.activity.tambah_data_wajah.TrainingDataWajahActivity;
import absensi.anif.its.ac.id.sikemastcforadmin.utilities.NetworkUtils;
import absensi.anif.its.ac.id.sikemastcforadmin.utilities.VolleySingleton;
import cn.pedant.SweetAlert.SweetAlertDialog;

public class MainActivity extends AppCompatActivity {

    private final String TAG = MainActivity.class.getSimpleName();
    private String DIRECTORY = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES) + "/facerecognition/data/SVM/";
    private List<String> fileNameList;
    private int numberOfFile;
    private SweetAlertDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView title = (TextView) mToolbar.findViewById(R.id.toolbarTitle);
        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/FredokaOne-Regular.ttf");
        title.setTypeface(typeface);
        title.setText(R.string.app_short_name);

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

        btnLihatDaftarMhs.setOnClickListener(action);
        btnTambahDataDiri.setOnClickListener(action);
        btnTrainingData.setOnClickListener(action);
    }

    View.OnClickListener action = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_lihat_daftar_mahasiswa:
                    Intent intentToDaftarMahasiswa = new Intent(MainActivity.this, PilihIdentitasActivity.class);
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

        File dir = new File(DIRECTORY);
        File[] files = dir.listFiles();
        numberOfFile = files.length;
        Log.d("number of files", String.valueOf(numberOfFile));

        if (numberOfFile > 0) {
            for (File file : files) {
                try {
                    FileInputStream fileInputStreamReader = new FileInputStream(file);
                    byte[] bytes = new byte[(int)file.length()];
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
}
