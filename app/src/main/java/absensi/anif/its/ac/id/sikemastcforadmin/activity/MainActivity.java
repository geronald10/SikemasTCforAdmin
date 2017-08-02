package absensi.anif.its.ac.id.sikemastcforadmin.activity;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import absensi.anif.its.ac.id.sikemastcforadmin.R;

public class MainActivity extends AppCompatActivity {

    Button btnLihatDaftarMhs, btnTambahDataDiri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView title = (TextView) mToolbar.findViewById(R.id.toolbarTitle);
        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/FredokaOne-Regular.ttf");
        title.setTypeface(typeface);
        title.setText(R.string.app_short_name);

        btnLihatDaftarMhs = (Button) findViewById(R.id.btn_lihat_daftar_mahasiswa);
        btnTambahDataDiri = (Button) findViewById(R.id.btn_tambah_data_mahasiswa);

        btnLihatDaftarMhs.setOnClickListener(action);
        btnTambahDataDiri.setOnClickListener(action);
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
                    Intent intent = new Intent(MainActivity.this, KelolaDataDiriActivity.class);
                    startActivity(intent);
                    break;
            }
        }
    };
}
