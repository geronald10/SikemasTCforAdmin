package absensi.anif.its.ac.id.sikemastcforadmin;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    Button btnLihatDaftarMhs, btnTambahDataDiri, btnTambahDataWajah, btnTambahDataTtd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView title = (TextView) mToolbar.findViewById(R.id.toolbarTitle);
//        SearchView searchView = (SearchView) mToolbar.findViewById(R.id.searchView);
        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/FredokaOne-Regular.ttf");
        title.setTypeface(typeface);
        title.setText(R.string.app_short_name);
//        searchView.setVisibility(View.GONE);

        btnLihatDaftarMhs = (Button) findViewById(R.id.btn_lihat_daftar_mahasiswa);
        btnTambahDataDiri = (Button) findViewById(R.id.btn_tambah_data_diri);
        btnTambahDataWajah = (Button) findViewById(R.id.btn_tambah_data_wajah);
        btnTambahDataTtd = (Button) findViewById(R.id.btn_tambah_data_tandatangan);

        btnLihatDaftarMhs.setOnClickListener(action);
        btnTambahDataDiri.setOnClickListener(action);
        btnTambahDataWajah.setOnClickListener(action);
        btnTambahDataTtd.setOnClickListener(action);
    }

    View.OnClickListener action = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_lihat_daftar_mahasiswa:
//                    Intent intent = new Intent();
//                    startActivity(intent);
                    break;
                case R.id.btn_tambah_data_diri:
                    Intent intent = new Intent(MainActivity.this, PilihIdentitasActivity.class);
                    startActivity(intent);
                    break;
                case R.id.btn_tambah_data_wajah:
                    break;
                case R.id.btn_tambah_data_tandatangan:
                    break;
            }
        }
    };
}
