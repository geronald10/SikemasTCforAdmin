package absensi.anif.its.ac.id.sikemastcforadmin.activity.tambah_data_tandatangan;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Build;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import absensi.anif.its.ac.id.sikemastcforadmin.R;
import absensi.anif.its.ac.id.sikemastcforadmin.activity.MainActivity;
import absensi.anif.its.ac.id.sikemastcforadmin.activity.tambah_data_wajah.TambahDataWajahActivity;
import absensi.anif.its.ac.id.sikemastcforadmin.utilities.NetworkUtils;
import absensi.anif.its.ac.id.sikemastcforadmin.utilities.VolleySingleton;
import cn.pedant.SweetAlert.SweetAlertDialog;

public class TambahDataTandaTanganActivity extends AppCompatActivity {

    private static final String TAG = TambahDataTandaTanganActivity.class.getSimpleName();

    private String userTerlogin;
    private String userId;
    private SweetAlertDialog pDialog;
    private ArrayList<String> encodedImageList;
    private signature mSignature;
    private LinearLayout mContent;
    private TextView tvCounter;
    private View view;
    private Button btn_tambah_tandatangan_simpan;
    private String StoredPath;
    private File dir;
    private Bitmap bitmap;
    int clickcount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tambah_data_tanda_tangan);
        Context mContext = this;

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView title = (TextView) mToolbar.findViewById(R.id.toolbarTitle);
        title.setVisibility(View.GONE);

        mToolbar.setTitle("Tambah Data");
        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Intent intent = getIntent();
        userTerlogin = intent.getStringExtra("identitas_mahasiswa");
        userId = intent.getStringExtra("nrpMahasiswa");

        //folder
        String DIRECTORY = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/signatureverification/";
        StoredPath = DIRECTORY + userTerlogin;
        Log.d(TAG, "StorePath " + StoredPath);
        dir = new File(StoredPath);

        mContent = (LinearLayout) findViewById(R.id.linearLayout_tambah_tandatangan);
        mSignature = new signature(getApplicationContext(), null);
        mSignature.setBackgroundColor(Color.WHITE);
        mContent.addView(mSignature, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        view = mContent;

        tvCounter = (TextView) findViewById(R.id.tvCounter);
        Button btn_tambah_tandatangan_cancel = (Button) findViewById(R.id.btn_tambah_tandatangan_cancel);
        Button btn_tambah_tandatangan_clear = (Button) findViewById(R.id.btn_tambah_tandatangan_clear);
        btn_tambah_tandatangan_simpan = (Button) findViewById(R.id.btn_tambah_tandatangan_simpan);

        btn_tambah_tandatangan_simpan.setEnabled(false);
        btn_tambah_tandatangan_cancel.setOnClickListener(action);
        btn_tambah_tandatangan_clear.setOnClickListener(action);
        btn_tambah_tandatangan_simpan.setOnClickListener(action);
    }

    View.OnClickListener action = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_tambah_tandatangan_clear:
                    Log.d(TAG, "Panel Cleared");
                    mSignature.clear();
                    btn_tambah_tandatangan_simpan.setEnabled(false);
                    break;
                case R.id.btn_tambah_tandatangan_cancel:
                    Log.d(TAG, "Panel Closed");
                    finish();
                    break;
                case R.id.btn_tambah_tandatangan_simpan:
                    Log.d(TAG, "clickcount " + clickcount);
                    clickcount = clickcount + 1;
                    tvCounter.setText(String.valueOf(clickcount));
                    if (clickcount <= 5) {
                        Log.d(TAG, "Panel Saved");
                        view.setDrawingCacheEnabled(true);
                        mSignature.save(view);
                    }
                    if (clickcount == 5) {
                        encodedImageList = new ArrayList<>();
                        encodedImageList = getAllEncodedImageFormat();
                        uploadImages(encodedImageList);
                        btn_tambah_tandatangan_simpan.setEnabled(true);
                    }
                    mSignature.clear();
                    break;
            }
        }
    };

    public class signature extends View {
        private static final float STROKE_WIDTH = 5f;
        private static final float HALF_STROKE_WIDTH = STROKE_WIDTH / 2;
        private Paint paint = new Paint();
        private Path path = new Path();
        private float lastTouchX;
        private float lastTouchY;
        private final RectF dirtyRect = new RectF();

        public signature(Context context, AttributeSet attrs) {
            super(context, attrs);
            paint.setAntiAlias(true);
            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeWidth(STROKE_WIDTH);
        }

        public void save(View v) {
            if (bitmap == null) {
                bitmap = Bitmap.createBitmap(this.getWidth(), this.getHeight(), Bitmap.Config.RGB_565);
            }
            Canvas canvas = new Canvas(bitmap);
            try {
                dir = new File(StoredPath);
                String filename = userTerlogin + "-" + clickcount + ".png";
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                // file output binary
                File myFile = new File(dir.getAbsolutePath(), filename);
                myFile.createNewFile();
                FileOutputStream mFileOutStream = new FileOutputStream(myFile);
                v.draw(canvas);

                // Mengkonversi file output ke gambar .png
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, mFileOutStream);
                mFileOutStream.flush();
                mFileOutStream.close();

            } catch (Exception e) {
                Log.v("log_tag", e.toString());
            }
        }
        public void clear() {
            path.reset();
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawPath(path, paint);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float eventX = event.getX();
            float eventY = event.getY();
            btn_tambah_tandatangan_simpan.setEnabled(true);

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    path.moveTo(eventX, eventY);
                    lastTouchX = eventX;
                    lastTouchY = eventY;
                    return true;

                case MotionEvent.ACTION_MOVE:

                case MotionEvent.ACTION_UP:

                    resetDirtyRect(eventX, eventY);
                    int historySize = event.getHistorySize();
                    for (int i = 0; i < historySize; i++) {
                        float historicalX = event.getHistoricalX(i);
                        float historicalY = event.getHistoricalY(i);
                        expandDirtyRect(historicalX, historicalY);
                        path.lineTo(historicalX, historicalY);
                    }
                    path.lineTo(eventX, eventY);
                    break;

                default:
                    // debug("Ignored touch event: " + event.toString());
                    //return false;
            }

            invalidate((int) (dirtyRect.left - HALF_STROKE_WIDTH),
                    (int) (dirtyRect.top - HALF_STROKE_WIDTH),
                    (int) (dirtyRect.right + HALF_STROKE_WIDTH),
                    (int) (dirtyRect.bottom + HALF_STROKE_WIDTH));

            lastTouchX = eventX;
            lastTouchY = eventY;

            return true;
        }

        private void expandDirtyRect(float historicalX, float historicalY) {
            if (historicalX < dirtyRect.left) {
                dirtyRect.left = historicalX;
            } else if (historicalX > dirtyRect.right) {
                dirtyRect.right = historicalX;
            }

            if (historicalY < dirtyRect.top) {
                dirtyRect.top = historicalY;
            } else if (historicalY > dirtyRect.bottom) {
                dirtyRect.bottom = historicalY;
            }
        }

        private void resetDirtyRect(float eventX, float eventY) {
            dirtyRect.left = Math.min(lastTouchX, eventX);
            dirtyRect.right = Math.max(lastTouchX, eventX);
            dirtyRect.top = Math.min(lastTouchY, eventY);
            dirtyRect.bottom = Math.max(lastTouchY, eventY);
        }

    }

    public ArrayList<String> getAllEncodedImageFormat() {
        ArrayList<String> encodedImage = new ArrayList<>();

        File dir = new File(StoredPath);
        File[] files = dir.listFiles();
        int numberOfFiles = files.length;
        Log.d("number of files", String.valueOf(numberOfFiles));

        if (files != null && numberOfFiles > 0) {
            for (File image : files) {
                String imagePath = image.getAbsolutePath();
                Log.d("imagePath", imagePath);
                Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                byte[] imageBytes = baos.toByteArray();
                encodedImage.add(Base64.encodeToString(imageBytes, Base64.DEFAULT));
            }
            return encodedImage;
        } else {
            return null;
        }
    }

    // Upload image to server
    private void uploadImages(final ArrayList<String> encodedImagesList) {

        StringRequest stringRequest;
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        Log.d(TAG, "masuk fungsi upload");

        showUploadLoading();
        final int[] numberOfPhotos = {0};
        for (int i = 0; i < encodedImagesList.size(); i++) {
            final int index = i;
            stringRequest = new StringRequest(Request.Method.POST, NetworkUtils.UPLOAD_DATASET_TANDATANGAN_SIKEMAS,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            //Disimissing the progress dialog
                            numberOfPhotos[0]++;
                            //Showing toast message of the response
                            if (numberOfPhotos[0] == encodedImagesList.size()) {
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
                    String image = encodedImagesList.get(index);
                    Map<String, String> params = new HashMap<>();
                    // Adding parameters
                    params.put("image", image);
                    params.put("image_name", userTerlogin + "-" + (index+1));
                    params.put("user_id", userId);

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
                            Intent returnIntent = new Intent(TambahDataTandaTanganActivity.this, MainActivity.class);
                            returnIntent.putExtra("result_message", "Berhasil menambahkan data tandatangan");
                            setResult(Activity.RESULT_OK, returnIntent);
                            Log.d("returnintent", String.valueOf(Activity.RESULT_OK));
                            finish();
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
