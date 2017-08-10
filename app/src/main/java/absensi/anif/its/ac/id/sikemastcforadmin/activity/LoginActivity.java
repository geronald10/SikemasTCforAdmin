package absensi.anif.its.ac.id.sikemastcforadmin.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import absensi.anif.its.ac.id.sikemastcforadmin.R;
import absensi.anif.its.ac.id.sikemastcforadmin.utilities.NetworkUtils;
import absensi.anif.its.ac.id.sikemastcforadmin.utilities.SikemasSessionManager;
import absensi.anif.its.ac.id.sikemastcforadmin.utilities.VolleySingleton;
import cn.pedant.SweetAlert.SweetAlertDialog;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = LoginActivity.class.getSimpleName();

    private Context mContext;
    private EditText edtEmail;
    private EditText edtPassword;
    private SweetAlertDialog pDialog;
    private SikemasSessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mContext = this;

        // Set up the login form.
        edtEmail = (EditText) findViewById(R.id.edt_email_login);
        edtPassword = (EditText) findViewById(R.id.edt_password_login);
        Button btnSignIn = (Button) findViewById(R.id.btn_sign_in);

        // Session manager
        session = new SikemasSessionManager(mContext);

        // Check if user is already logged in or not
        if (session.isLoggedIn()) {
            String loggedUserId = session.getUserDetails().get(SikemasSessionManager.KEY_USER_ROLE);
            switch (loggedUserId) {
                case "2":
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                    break;
                default:
                    showErrorNotAuthorized();
                    break;
            }
        }

        btnSignIn.setOnClickListener(operation);
    }

    View.OnClickListener operation = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int id = view.getId();
            switch (id) {
                case R.id.btn_sign_in:
                    String email = edtEmail.getText().toString().trim();
                    String password = edtPassword.getText().toString().trim();

                    if (!email.isEmpty() && !password.isEmpty()) {
                        checkLogin(email, password);
                    } else {
                        Toast.makeText(getApplicationContext(), "Please enter the credentials!", Toast.LENGTH_LONG).show();
                    }
                    break;
            }
        }
    };

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void checkLogin(final String email, final String password) {
        showLoading();
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                NetworkUtils.LOGIN_SIKEMAS, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Login Response: " + response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String code = jsonObject.getString("code");
                    String message = jsonObject.getString("status");
                    // Check for error node in json
                    if (code.equals("1")) {
                        // user successfully logged in. Create Login session
                        JSONObject user = jsonObject.getJSONObject("user");
                        String userId = user.getString("user_id");
                        String name = user.getString("nama");
                        String email = user.getString("email");
                        String role = user.getString("role");
                        if (role.equals("2")) {
                            showSuccessResult();
                            session.createLoginSession(userId, name, email, role);
                            checkUserRole(role);
                            finish();
                        } else
                            showErrorNotAuthorized();
                    } else {
                        // Error in login. Get the error message
                        showErrorNotFound();
                    }
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                    showErrorResult();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Login Error: " + error.getMessage());
                showErrorNotFound();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("user_email", email);
                params.put("user_password", password);

                return params;
            }
        };
        //Adding request to the queue
        VolleySingleton.getmInstance(getApplicationContext()).addToRequestQueue(stringRequest);
    }

    private void checkUserRole(String userRole) {
        switch (userRole) {
            case "2":
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    private void showLoading() {
        pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText("Loading");
        pDialog.setCancelable(false);
        pDialog.show();
    }

    private void showSuccessResult() {
        if (pDialog.isShowing()) {
            pDialog.dismiss();
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
                    .setContentText("Data admin tidak ditemukan")
                    .show();
        }
    }

    private void showErrorNotAuthorized() {
        if (pDialog.isShowing()) {
            pDialog.dismiss();
            new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Tidak diijinkan!")
                    .setContentText("Pengguna tidak memiliki akses admin")
                    .show();
        }
    }
}
