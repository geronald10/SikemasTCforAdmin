package absensi.anif.its.ac.id.sikemastcforadmin.activity.tambah_data_diri;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

import absensi.anif.its.ac.id.sikemastcforadmin.R;
import absensi.anif.its.ac.id.sikemastcforadmin.activity.MainActivity;
import absensi.anif.its.ac.id.sikemastcforadmin.utilities.NetworkUtils;
import absensi.anif.its.ac.id.sikemastcforadmin.utilities.VolleySingleton;
import cn.pedant.SweetAlert.SweetAlertDialog;

public class TambahDataDiriActivity extends AppCompatActivity {

    private final String TAG = TambahDataDiriActivity.class.getSimpleName();
    private TextView tvNRP, tvNama;
    private TextInputLayout tilNoHp, tilPassword, tilKonfirmPassword;
    private EditText edtNoHp, edtPassword, edtKonfirmasiPassword;
    private String nrp, nama;
    private SweetAlertDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tambah_data_diri);

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

        Intent intentExtra = getIntent();
        nrp = intentExtra.getStringExtra("nrpMahasiswa");
        nama = intentExtra.getStringExtra("namaMahasiswa");

        tvNRP = (TextView) findViewById(R.id.tvNrp);
        tvNama = (TextView) findViewById(R.id.tvNama);
        tilNoHp = (TextInputLayout) findViewById(R.id.tilNomorTelpon);
        tilPassword = (TextInputLayout) findViewById(R.id.tilPassword);
        tilKonfirmPassword = (TextInputLayout) findViewById(R.id.tilKonfirmasiPassword);
        edtNoHp = (EditText) findViewById(R.id.edtNoTelepon);
        edtPassword = (EditText) findViewById(R.id.edtPassword);
        edtKonfirmasiPassword = (EditText) findViewById(R.id.edtKonfirmasiPassword);

        tvNRP.setText(nrp);
        tvNama.setText(nama);
        edtNoHp.addTextChangedListener(new MyTextWatcher(edtNoHp));
        edtPassword.addTextChangedListener(new MyTextWatcher(edtPassword));
        edtKonfirmasiPassword.addTextChangedListener(new MyTextWatcher(edtKonfirmasiPassword));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_tambah_data_diri, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.reset:
                edtNoHp.getText().clear();
                edtNoHp.clearFocus();
                edtPassword.getText().clear();
                edtPassword.clearFocus();
                edtKonfirmasiPassword.getText().clear();
                edtKonfirmasiPassword.clearFocus();
                tilNoHp.setErrorEnabled(false);
                tilPassword.setErrorEnabled(false);
                tilKonfirmPassword.setErrorEnabled(false);
                break;
            case R.id.check:
                kirimForm();
        }
        return true;
    }

    private boolean validasiNoHp() {
        if (edtNoHp.getText().toString().trim().isEmpty()) {
            tilNoHp.setError(getString(R.string.error_msg_noHP));
            edtNoHp.requestFocus();
            return false;
        } else {
            tilNoHp.setErrorEnabled(false);
        }
        return true;
    }

    private boolean validasiPassword() {
        if (edtPassword.getText().toString().trim().isEmpty()) {
            tilPassword.setError(getString(R.string.error_msg_password));
            edtPassword.requestFocus();
            return false;
        } else {
            tilPassword.setErrorEnabled(false);
        }
        return true;
    }

    private boolean validasiKonfirmasiPassword() {
        if (edtKonfirmasiPassword.getText().toString().trim().isEmpty()) {
            tilKonfirmPassword.setError(getString(R.string.error_msg_konfirm_password));
            edtKonfirmasiPassword.requestFocus();
            return false;
        } else {
            tilKonfirmPassword.setErrorEnabled(false);
        }
        if (!edtPassword.getText().toString().equals(edtKonfirmasiPassword.getText().toString())) {
            tilKonfirmPassword.setError(getString(R.string.error_msg_konfirm_password_2));
            edtKonfirmasiPassword.requestFocus();
            return false;
        } else {
            tilKonfirmPassword.setErrorEnabled(false);
        }
        return true;
    }

    private void kirimForm() {
        if (!validasiNoHp())
            return;
        if (!validasiPassword())
            return;
        if (!validasiKonfirmasiPassword())
            return;
        kirimFormDataDiri(nrp, edtNoHp.getText().toString(), edtPassword.getText().toString());
    }

    private void kirimFormDataDiri(final String nrp, final String noHP, final String password) {
        Log.d(TAG, "call kirimFormDataDiri Method");
        showLoading();
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                NetworkUtils.KIRIM_DATA_MAHASISWA,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        showSuccessResult();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                showErrorResult();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("nrp", nrp);
                params.put("no_telpon", noHP);
                params.put("password", password);

                return params;
            }
        };
        VolleySingleton.getmInstance(getApplicationContext()).addToRequestQueue(stringRequest);
    }

    private void showLoading() {
        pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText("Mencari...");
        pDialog.setCancelable(false);
        pDialog.show();
    }

    private void showSuccessResult() {
        if (pDialog.isShowing()) {
            pDialog.dismiss();
            new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                    .setTitleText("Berhasil!")
                    .setContentText("Data diri Anda berhasil ditambahkan")
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            sweetAlertDialog.dismissWithAnimation();
                            Intent returnIntent = new Intent(TambahDataDiriActivity.this, MainActivity.class);
                            setResult(Activity.RESULT_OK, returnIntent);
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
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            sweetAlertDialog.dismissWithAnimation();
                            finish();
                        }
                    })
                    .show();
        }
    }

    private class MyTextWatcher implements TextWatcher {

        private View view;

        private MyTextWatcher(View view) {
            this.view = view;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            switch (view.getId()) {
                case R.id.edtNoTelepon:
                    validasiNoHp();
                    break;
                case R.id.edtPassword:
                    validasiPassword();
                    break;
                case R.id.edtKonfirmasiPassword:
                    validasiKonfirmasiPassword();
                    break;
            }
        }
    }
}
