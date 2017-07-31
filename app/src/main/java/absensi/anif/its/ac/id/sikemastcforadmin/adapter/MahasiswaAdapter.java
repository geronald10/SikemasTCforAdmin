package absensi.anif.its.ac.id.sikemastcforadmin.adapter;

import android.content.Context;
import android.database.Cursor;
import android.media.Image;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import absensi.anif.its.ac.id.sikemastcforadmin.R;
import absensi.anif.its.ac.id.sikemastcforadmin.model.Mahasiswa;

public class MahasiswaAdapter extends RecyclerView.Adapter<MahasiswaAdapter.MahasiswaAdapterViewHolder>
        implements Filterable {

    private MahasiswaFilter mahasiswaFilter;
    private List<Mahasiswa> mahasiswaList;
    private List<Mahasiswa> filteredMahasiswaList;
    private Context mContext;

    final private MahasiswaAdapterOnClickHandler mClickHandler;

    @Override
    public Filter getFilter() {
        if(mahasiswaFilter == null)
            mahasiswaFilter = new MahasiswaFilter(this, mahasiswaList);
        return mahasiswaFilter;
    }

    public interface MahasiswaAdapterOnClickHandler {
        void onClick(String nrpMahasiswa, String namaMahasiswa);
    }

    public MahasiswaAdapter(Context context, MahasiswaAdapterOnClickHandler clickHandler, List<Mahasiswa> listMahasiswa) {
        mContext = context;
        mClickHandler = clickHandler;
        mahasiswaList = listMahasiswa;
        this.filteredMahasiswaList = listMahasiswa;
    }

    @Override
    public MahasiswaAdapter.MahasiswaAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.content_pilih_identitas, viewGroup, false);
        view.setFocusable(true);
        return new MahasiswaAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MahasiswaAdapter.MahasiswaAdapterViewHolder holder, int position) {
        Mahasiswa currentMahasiswa = mahasiswaList.get(position);
        String nrpMahasiswa = currentMahasiswa.getNrp();
        String namaMahasiswa = currentMahasiswa.getNama();
        int statusDataDiri = currentMahasiswa.getStatusDataDiri();

        holder.tvNrp.setText(nrpMahasiswa);
        holder.tvNama.setText(namaMahasiswa);
        switch (statusDataDiri) {
            case 0:
                holder.ibTambahDataDiri.setImageResource(R.drawable.ic_add_circle);
                holder.ibTambahDataDiri.setColorFilter(ContextCompat.getColor(mContext, R.color.colorStatusIdle));
                holder.ivStatusDataDiri.setImageResource(R.drawable.circle_gray);
                break;
            case 1:
                holder.ibTambahDataDiri.setImageResource(R.drawable.ic_check);
                holder.ibTambahDataDiri.setColorFilter(ContextCompat.getColor(mContext, R.color.colorStatusHadir));
                holder.ivStatusDataDiri.setImageResource(R.drawable.circle_green);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return mahasiswaList.size();
    }

    class MahasiswaAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView tvNrp;
        private TextView tvNama;
        private ImageView ivStatusDataDiri;
        private ImageButton ibTambahDataDiri;

        public MahasiswaAdapterViewHolder(View itemView) {
            super(itemView);
            tvNrp = (TextView) itemView.findViewById(R.id.tv_nrp_peserta);
            tvNama = (TextView) itemView.findViewById(R.id.tv_nama_peserta);
            ivStatusDataDiri = (ImageView) itemView.findViewById(R.id.iv_status_data_diri);
            ibTambahDataDiri = (ImageButton) itemView.findViewById(R.id.ib_tambah_data_diri);

            ibTambahDataDiri.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            String nrpMahasiswa = mahasiswaList.get(adapterPosition).getNrp();
            String namaMahasiswa = mahasiswaList.get(adapterPosition).getNama();
            int statusDataDiri = mahasiswaList.get(adapterPosition).getStatusDataDiri();
            if (statusDataDiri == 0) {
                switch (v.getId()) {
                    case R.id.ib_tambah_data_diri:
                        mClickHandler.onClick(nrpMahasiswa, namaMahasiswa);
                        break;
                }
            } else {
                Toast.makeText(mContext, "Data sudah ditambahkan", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class MahasiswaFilter extends Filter {

        private final MahasiswaAdapter mahasiswaAdapter;
        private final List<Mahasiswa> mahasiswaList;
        private final List<Mahasiswa> filteredMahasiswaList;

        public MahasiswaFilter(MahasiswaAdapter mahasiswaAdapter, List<Mahasiswa> mahasiswaList) {
            super();
            this.mahasiswaAdapter = mahasiswaAdapter;
            this.mahasiswaList = new LinkedList<>(mahasiswaList);
            this.filteredMahasiswaList = new ArrayList<>();
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            filteredMahasiswaList.clear();
            final FilterResults results = new FilterResults();

            if (constraint.length() == 0) {
                filteredMahasiswaList.addAll(mahasiswaList);
            } else {
                final String filterPattern = constraint.toString().toLowerCase().trim();
                for (final Mahasiswa mahasiswa : mahasiswaList) {
                    if (mahasiswa.getNama().toLowerCase().contains(filterPattern)) {
                        filteredMahasiswaList.add(mahasiswa);
                    }
                }
            }
            results.values = filteredMahasiswaList;
            results.count = filteredMahasiswaList.size();
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mahasiswaAdapter.filteredMahasiswaList.clear();
            mahasiswaAdapter.filteredMahasiswaList.addAll((ArrayList<Mahasiswa>) results.values);
            mahasiswaAdapter.notifyDataSetChanged();
        }
    }

    public void clear() {
        mahasiswaList.clear();
        notifyDataSetChanged();
    }
}
