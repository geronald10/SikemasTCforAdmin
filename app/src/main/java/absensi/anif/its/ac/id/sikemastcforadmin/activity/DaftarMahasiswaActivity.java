package absensi.anif.its.ac.id.sikemastcforadmin.activity;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import absensi.anif.its.ac.id.sikemastcforadmin.R;
import absensi.anif.its.ac.id.sikemastcforadmin.activity.tambah_data_diri.TambahDataDiriActivity;
import absensi.anif.its.ac.id.sikemastcforadmin.adapter.MahasiswaAdapter;
import absensi.anif.its.ac.id.sikemastcforadmin.model.Mahasiswa;
import absensi.anif.its.ac.id.sikemastcforadmin.utilities.NetworkUtils;
import absensi.anif.its.ac.id.sikemastcforadmin.utilities.VolleySingleton;

public class DaftarMahasiswaActivity extends AppCompatActivity implements
        MahasiswaAdapter.MahasiswaAdapterOnClickHandler,
        SearchView.OnQueryTextListener {

    private final String TAG = DaftarMahasiswaActivity.class.getSimpleName();
    private Context mContext;
    private List<Mahasiswa> mahasiswaList;
    private MahasiswaAdapter mahasiswaAdapter;
    private RecyclerView mRecyclerView;

    private ProgressBar mLoadingIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pilih_identitas);
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

        mRecyclerView = (RecyclerView) findViewById(R.id.rvPilihMahasiswa);
        mLoadingIndicator = (ProgressBar) findViewById(R.id.pbLoadingIndikator);
        LinearLayoutManager layoutManager =
                new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);

        mahasiswaList = new ArrayList<>();
        getAllMahasiswaList();

        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);
        mahasiswaAdapter = new MahasiswaAdapter(mContext, this, mahasiswaList);
        mRecyclerView.setAdapter(mahasiswaAdapter);
    }

    public void getAllMahasiswaList() {
        Log.d(TAG, "call getAllMahasiswaList Method");
        showLoading();
        StringRequest stringRequest = new StringRequest(Request.Method.GET,
                NetworkUtils.GET_ALL_DATA_MAHASISWA,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, response);
                        try {
                            mahasiswaList.clear();
                            JSONObject jsonObject = new JSONObject(response);
                            JSONArray listMahasiswa = jsonObject.getJSONArray("listmhs");
                            for (int i=0; i<listMahasiswa.length(); i++) {
                                JSONObject mahasiswa = listMahasiswa.getJSONObject(i);
                                String nrpMahasiswa = mahasiswa.getString("nrp");
                                String namaMahasiswa = mahasiswa.getString("nama");
                                String noTeleponMahasiswa = mahasiswa.getString("no_telpon");
                                int statusDataDiri;
                                if (noTeleponMahasiswa.equals("null"))
                                    statusDataDiri = 0;
                                else
                                    statusDataDiri = 1;

                                Mahasiswa newMahasiswa = new Mahasiswa(nrpMahasiswa, namaMahasiswa, statusDataDiri);
                                mahasiswaList.add(newMahasiswa);
                            }
                            mahasiswaAdapter.notifyDataSetChanged();
                            showDataList();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), "JSON error: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
        VolleySingleton.getmInstance(getApplicationContext()).addToRequestQueue(stringRequest);
    }

    @Override
    public void onClick(String nrpMahasiswa, String namaMahasiswa) {
        Intent intent = new Intent(DaftarMahasiswaActivity.this, TambahDataDiriActivity.class);
        intent.putExtra("nrpMahasiswa", nrpMahasiswa);
        intent.putExtra("namaMahasiswa", namaMahasiswa);
        startActivity(intent);
    }

    private void showLoading() {
        mRecyclerView.setVisibility(View.GONE);
        mLoadingIndicator.setVisibility(View.VISIBLE);
    }

    private void showDataList() {
        mLoadingIndicator.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_pilih_identitas, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(this);
        searchView.setQueryHint("Search by Name");

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh:
                getAllMahasiswaList();
        }
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        mahasiswaAdapter.getFilter().filter(newText);
        return true;
    }
}
