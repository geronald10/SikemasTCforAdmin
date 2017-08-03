package absensi.anif.its.ac.id.sikemastcforadmin.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import absensi.anif.its.ac.id.sikemastcforadmin.R;
import absensi.anif.its.ac.id.sikemastcforadmin.activity.tambah_data_diri.TambahDataDiriActivity;
import absensi.anif.its.ac.id.sikemastcforadmin.activity.tambah_data_tandatangan.TambahDataTandaTanganActivity;
import absensi.anif.its.ac.id.sikemastcforadmin.activity.tambah_data_wajah.InstruksiTambahDataWajahActivity;
import absensi.anif.its.ac.id.sikemastcforadmin.activity.tambah_data_wajah.TambahDataWajahActivity;
import absensi.anif.its.ac.id.sikemastcforadmin.utilities.NetworkUtils;
import absensi.anif.its.ac.id.sikemastcforadmin.utilities.VolleySingleton;
import ch.zhaw.facerecognitionlibrary.Helpers.FileHelper;
import cn.pedant.SweetAlert.SweetAlertDialog;

public class KelolaDataDiriActivity extends AppCompatActivity {

    private final String TAG = KelolaDataDiriActivity.class.getSimpleName();
    public static final int ACTIVITY_INSTRUKSI_CODE = 10;
    public static final int ACTIVITY_TAMBAH_DATA_WAJAH_CODE = 20;

    private int jumlahDataSetServer;
    private ArrayList<String> imageUrlList;
    private ArrayList<String> encodedImageList;
    private FileHelper fh;

    private Context mContext;
    private SweetAlertDialog pDialog;
    private TextView tvNRP, tvNama;
    private EditText edtInputNRP;
    private ConstraintLayout clInputUser, clKelolaDataDiri;

    private String nrpMahasiswa, namaMahasiswa;
    private int statusDataDiri, statusDataWajah, statusDataTtd, numberOfPhotos = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kelola_data_diri);
        mContext = this;

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView title = (TextView) mToolbar.findViewById(R.id.toolbarTitle);
        title.setVisibility(View.GONE);
        setSupportActionBar(mToolbar);

        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        edtInputNRP = (EditText) findViewById(R.id.edtInputUser);
        Button btnInputNRP = (Button) findViewById(R.id.btnInputUser);
        btnInputNRP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getMahasiswaByNRP(edtInputNRP.getText().toString().trim());
            }
        });
    }

    private void showLoading() {
        pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText("Loading");
        pDialog.setCancelable(false);
        pDialog.show();
    }

    private void showUploadLoading() {
        pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText("Mengunggah Data..");
        pDialog.setCancelable(false);
        pDialog.show();
    }

    private void showSuccessUploadResult() {
        if (pDialog.isShowing()) {
            pDialog.dismiss();
            new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                    .setTitleText("Berhasil!")
                    .setContentText("Data wajah berhasil diunggah")
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            sweetAlertDialog.dismissWithAnimation();
                            refresh();
                        }
                    })
                    .show();
        }
    }

    private void showSuccessResult() {
        if (pDialog.isShowing()) {
            pDialog.dismiss();
            new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                    .setTitleText("Berhasil!")
                    .setContentText("Data diri Anda berhasil ditemukan")
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            sweetAlertDialog.dismissWithAnimation();
                            showKelolaDataDiri();
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

    private void showErrorNotFound() {
        if (pDialog.isShowing()) {
            pDialog.dismiss();
            new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Tidak ditemukan!")
                    .setContentText("Data diri Anda tidak ditemukan")
                    .show();
        }
    }

    private void showKelolaDataDiri() {
        clInputUser = (ConstraintLayout) findViewById(R.id.clInputUser);
        clKelolaDataDiri = (ConstraintLayout) findViewById(R.id.clKelolaDataDiri);
        clInputUser.setVisibility(View.GONE);
        clKelolaDataDiri.setVisibility(View.VISIBLE);

        tvNRP = (TextView) findViewById(R.id.tvNrp);
        tvNama = (TextView) findViewById(R.id.tvNama);
        Button btnTambahDataDiri = (Button) findViewById(R.id.btnTambahDataMahasiswa);
        Button btnTambahDataWajah = (Button) findViewById(R.id.btnTambahDataWajah);
        Button btnTambahDataTtd = (Button) findViewById(R.id.btnTambahDataTtd);

        tvNRP.setText(nrpMahasiswa);
        tvNama.setText(namaMahasiswa);
        switch (statusDataDiri) {
            case 0:
                Log.d(TAG, "Masuk Sini 0");
                btnTambahDataDiri.setCompoundDrawablesWithIntrinsicBounds(ContextCompat
                                .getDrawable(this, R.drawable.ic_141_exam), null,
                        ContextCompat.getDrawable(this, R.drawable.ic_add_circle), null);
                break;
            case 1:
                Log.d(TAG, "Masuk Sini 1");
                btnTambahDataDiri.setCompoundDrawablesWithIntrinsicBounds(ContextCompat
                                .getDrawable(this, R.drawable.ic_141_exam), null,
                        ContextCompat.getDrawable(this, R.drawable.ic_check), null);
                break;
        }
        switch (statusDataWajah) {
            case 0:
                Log.d(TAG, "Masuk Sini 0");
                btnTambahDataWajah.setCompoundDrawablesWithIntrinsicBounds(ContextCompat
                                .getDrawable(this, R.drawable.ic_101_photograph_57), null,
                        ContextCompat.getDrawable(this, R.drawable.ic_add_circle), null);
                break;
            case 1:
                Log.d(TAG, "Masuk Sini 1");
                btnTambahDataWajah.setCompoundDrawablesWithIntrinsicBounds(ContextCompat
                                .getDrawable(this, R.drawable.ic_101_photograph_57), null,
                        ContextCompat.getDrawable(this, R.drawable.ic_check), null);
                break;
        }
        switch (statusDataTtd) {
            case 0:
                Log.d(TAG, "Masuk Sini 0");
                btnTambahDataTtd.setCompoundDrawablesWithIntrinsicBounds(ContextCompat
                                .getDrawable(this, R.drawable.ic_141_pen_1), null,
                        ContextCompat.getDrawable(this, R.drawable.ic_add_circle), null);
                break;
            case 1:
                Log.d(TAG, "Masuk Sini 1");
                btnTambahDataTtd.setCompoundDrawablesWithIntrinsicBounds(ContextCompat
                                .getDrawable(this, R.drawable.ic_141_pen_1), null,
                        ContextCompat.getDrawable(this, R.drawable.ic_check), null);
                break;
        }

        btnTambahDataDiri.setOnClickListener(action);
        btnTambahDataWajah.setOnClickListener(action);
        btnTambahDataTtd.setOnClickListener(action);
    }

    private void refresh() {
        getMahasiswaByNRP(tvNRP.getText().toString().trim());
    }

    View.OnClickListener action = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btnTambahDataMahasiswa:
                    if (statusDataDiri == 0) {
                        Intent intent = new Intent(KelolaDataDiriActivity.this, TambahDataDiriActivity.class);
                        intent.putExtra("nrpMahasiswa", nrpMahasiswa);
                        intent.putExtra("namaMahasiswa", namaMahasiswa);
                        startActivity(intent);
                    } else {
                        Toast.makeText(getApplicationContext(), "Data Diri sudah ditambahkan", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case R.id.btnTambahDataWajah:
                    if (statusDataWajah == 0) {
                        Intent intent = new Intent(KelolaDataDiriActivity.this, InstruksiTambahDataWajahActivity.class);
                        FileHelper newFile = new FileHelper();
                        if (isNameAlreadyUsed(newFile.getTrainingList(), nrpMahasiswa + " - " + namaMahasiswa)) {
                            Log.d("TrainingList", String.valueOf(newFile.getTrainingList()));
                            Toast.makeText(getApplicationContext(), "Data wajah ditemukan", Toast.LENGTH_SHORT).show();
                        } else {
                            startActivityForResult(intent, ACTIVITY_INSTRUKSI_CODE);
                        }
                        break;
                    } else {
                        Toast.makeText(getApplicationContext(), "Data Wajah sudah ditambahkan", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case R.id.btnTambahDataTtd:
                    if (statusDataTtd == 0) {
                        Intent intent = new Intent(KelolaDataDiriActivity.this, TambahDataTandaTanganActivity.class);
                        intent.putExtra("nrpMahasiswa", nrpMahasiswa);
                        intent.putExtra("identitas_mahasiswa", nrpMahasiswa + " - " + namaMahasiswa);
                        startActivity(intent);
                    } else {
                        Toast.makeText(getApplicationContext(), "Data Wajah sudah ditambahkan", Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("cek", String.valueOf(requestCode));

        if (requestCode == ACTIVITY_INSTRUKSI_CODE) {
            Log.d("cek", String.valueOf(resultCode));
            if (resultCode == Activity.RESULT_OK) {
                Log.d(String.valueOf(resultCode), "masuk percabangan");
                Intent intentToTambahWajah = new Intent(mContext, TambahDataWajahActivity.class);
                intentToTambahWajah.putExtra("user_terlogin", nrpMahasiswa + " - " + namaMahasiswa);
                intentToTambahWajah.putExtra("method", TambahDataWajahActivity.TIME);
                intentToTambahWajah.putExtra("Folder", "Training");
                startActivityForResult(intentToTambahWajah, ACTIVITY_TAMBAH_DATA_WAJAH_CODE);
            }
            else if (resultCode == Activity.RESULT_CANCELED)
                Toast.makeText(mContext, "Tidak ditemukan hasil, kesalahan dalam request internal aplikasi",
                        Toast.LENGTH_SHORT).show();
        }
        else if (requestCode == ACTIVITY_TAMBAH_DATA_WAJAH_CODE) {
            Log.d("cek", String.valueOf(resultCode));
            if (resultCode == Activity.RESULT_OK) {
                String resultMessage = data.getStringExtra("result_message");
                Toast.makeText(mContext, resultMessage, Toast.LENGTH_SHORT).show();
                encodedImageList = new ArrayList<>();
                encodedImageList = getAllEncodedImageFormat();
                uploadImages(encodedImageList);
            }
            else if (resultCode == Activity.RESULT_CANCELED)
                Toast.makeText(mContext, "Tidak ditemukan hasil, kesalahan dalam request internal aplikasi",
                        Toast.LENGTH_SHORT).show();
        }
    }

    // Check apakah nama folder sudah digunakan
    private boolean isNameAlreadyUsed(File[] list, String name) {
        boolean used = false;
        if (list != null && list.length > 0) {
            for (File person : list) {
                // The last token is the name --> Folder name = Person name
                String[] tokens = person.getAbsolutePath().split("/");
                final String foldername = tokens[tokens.length - 1];
                if (foldername.equals(name)) {
                    used = true;
                    break;
                }
            }
        }
        return used;
    }

    // Get encoded image format from image
    private ArrayList<String> getAllEncodedImageFormat() {
        ArrayList<String> encodedImage = new ArrayList<>();
        FileHelper imageFiles = new FileHelper(nrpMahasiswa + " - " + namaMahasiswa);
        File[] imageList = imageFiles.getTrainingList();
        Log.d("get All Image", String.valueOf(imageList.length));

        if (imageList.length > 0) {
            for (File image : imageList) {
                String imagePath = image.getAbsolutePath();
                Log.d("imagePath", imagePath);
                Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                byte[] imageBytes = baos.toByteArray();
                encodedImage.add(Base64.encodeToString(imageBytes, Base64.DEFAULT));
            }
        } else {
            return null;
        }
        return encodedImage;
    }

    // Upload image file to server
    private void uploadImages(final ArrayList<String> encodedImagesList) {
        Log.d(TAG, "call uploadImages Method");
        showUploadLoading();
        StringRequest stringRequest;
        numberOfPhotos = 0;
        for (int i = 0; i < encodedImagesList.size(); i++) {
            final int index = i;
            stringRequest = new StringRequest(Request.Method.POST, NetworkUtils.UPLOAD_DATA_WAJAH,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            numberOfPhotos++;
                            //Showing toast message of the response
                            if (numberOfPhotos == encodedImagesList.size()) {
                                Log.d(TAG, "Photo Terupload " + numberOfPhotos);
                                showSuccessUploadResult();
                            }
                            Log.d("VolleyResponse", "Dapat ResponseVolley Upload Images");
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            // Dismissing the progress dialog
                            pDialog.dismiss();
                            Log.d("VolleyErroyResponse", "Error");
                            //Showing toast
                            Toast.makeText(KelolaDataDiriActivity.this, volleyError.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    // Get encoded Image
                    String image = encodedImagesList.get(index);
                    Map<String, String> params = new HashMap<>();
                    // Adding parameters
                    params.put("image", image);
                    params.put("image_name", nrpMahasiswa + " - " + namaMahasiswa + "_" + index);
                    params.put("user_id", nrpMahasiswa);

                    //returning parameters
                    return params;
                }
            };
            //Adding request to the queue
            VolleySingleton.getmInstance(getApplicationContext()).addToRequestQueue(stringRequest);
        }
    }

    public void getMahasiswaByNRP(final String nrp) {
        Log.d(TAG, "call getAllMahasiswaList Method");
        showLoading();
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                NetworkUtils.GET_DATA_MAHASISWA_BY_NRP,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            JSONArray dataMahasiswa = jsonObject.getJSONArray("listmhs");
                            if (dataMahasiswa.length() == 0)
                                showErrorNotFound();
                            else {
                                for (int i = 0; i < dataMahasiswa.length(); i++) {
                                    JSONObject mahasiswa = dataMahasiswa.getJSONObject(i);
                                    nrpMahasiswa = mahasiswa.getString("nrp");
                                    namaMahasiswa = mahasiswa.getString("nama");
                                    String noTeleponMahasiswa = mahasiswa.getString("no_telpon");
                                    int dataWajah = mahasiswa.getJSONArray("wajah").length();
                                    int dataTtd = mahasiswa.getJSONArray("signature").length();
                                    statusDataDiri = 0;
                                    statusDataWajah = 0;
                                    statusDataTtd = 0;
                                    if (noTeleponMahasiswa.equals("null"))
                                        statusDataDiri = 0;
                                    else
                                        statusDataDiri = 1;
                                    if (dataWajah == 0)
                                        statusDataWajah = 0;
                                    else
                                        statusDataWajah = 1;
                                    if (dataTtd == 0)
                                        statusDataTtd = 0;
                                    else
                                        statusDataTtd = 1;
                                    Log.d(TAG, statusDataDiri + " " + statusDataWajah + " " + statusDataTtd);
                                }
                                showSuccessResult();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            showErrorResult();
                            Log.e(TAG, "Error JSON: " + e.getMessage());
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Error Volley: " + error.getMessage());
                showErrorResult();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<>();
                params.put("nrp", nrp);

                return params;
            }
        };
        VolleySingleton.getmInstance(getApplicationContext()).addToRequestQueue(stringRequest);
    }

}
