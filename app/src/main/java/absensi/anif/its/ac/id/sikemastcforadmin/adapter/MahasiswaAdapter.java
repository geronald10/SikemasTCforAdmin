package absensi.anif.its.ac.id.sikemastcforadmin.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

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

    @Override
    public Filter getFilter() {
        if(mahasiswaFilter == null)
            mahasiswaFilter = new MahasiswaFilter(this, mahasiswaList);
        return mahasiswaFilter;
    }

    public MahasiswaAdapter(Context context, List<Mahasiswa> listMahasiswa) {
        mContext = context;
        mahasiswaList = listMahasiswa;
        this.filteredMahasiswaList = listMahasiswa;
    }

    @Override
    public MahasiswaAdapter.MahasiswaAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.content_identitas_mahasiswa, viewGroup, false);
        view.setFocusable(true);
        return new MahasiswaAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MahasiswaAdapter.MahasiswaAdapterViewHolder holder, int position) {
        Mahasiswa currentMahasiswa = mahasiswaList.get(position);
        String nrpMahasiswa = currentMahasiswa.getNrp();
        String namaMahasiswa = currentMahasiswa.getNama();
        int statusDataDiri = currentMahasiswa.getStatusDataDiri();
        int statusDataWajah = currentMahasiswa.getStatusDataWajah();
        int statusDataTtd = currentMahasiswa.getStatusDataTtd();

        holder.tvNrp.setText(nrpMahasiswa);
        holder.tvNama.setText(namaMahasiswa);

        int result = 0;

        switch (statusDataDiri) {
            case 0:
                holder.ivStatusDataDiri.setImageResource(R.drawable.ic_remove);
                break;
            case 1:
                holder.ivStatusDataDiri.setImageResource(R.drawable.ic_check);
                result += 1;
                break;
        }

        if (statusDataWajah == 10) {
            holder.ivStatusDataWajah.setImageResource(R.drawable.ic_check);
            result += 1;
        }
        else if (statusDataWajah == 0)
            holder.ivStatusDataWajah.setImageResource(R.drawable.ic_remove);
        else if (statusDataWajah < 10)
            holder.ivStatusDataWajah.setImageResource(R.drawable.ic_warning);
        else
            holder.ivStatusDataWajah.setImageResource(R.drawable.ic_close);

        if (statusDataTtd == 5) {
            holder.ivStatusDataTtd.setImageResource(R.drawable.ic_check);
            result += 1;
        }
        else if (statusDataTtd == 0)
            holder.ivStatusDataTtd.setImageResource(R.drawable.ic_remove);
        else if (statusDataTtd < 5)
            holder.ivStatusDataTtd.setImageResource(R.drawable.ic_warning);
        else
            holder.ivStatusDataTtd.setImageResource(R.drawable.ic_close);

        switch (result) {
            case 0:
                holder.ivStatusData.setImageResource(R.color.colorStatusIdle);
                break;
            case 1:
                holder.ivStatusData.setImageResource(R.color.colorStatusIjin);
                break;
            case 2:
                holder.ivStatusData.setImageResource(R.color.colorStatusIjin);
                break;
            case 3:
                holder.ivStatusData.setImageResource(R.color.colorStatusHadir);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return mahasiswaList.size();
    }

    class MahasiswaAdapterViewHolder extends RecyclerView.ViewHolder {

        private TextView tvNrp;
        private TextView tvNama;
        private ImageView ivStatusData;
        private ImageView ivStatusDataDiri;
        private ImageView ivStatusDataWajah;
        private ImageView ivStatusDataTtd;

        public MahasiswaAdapterViewHolder(View itemView) {
            super(itemView);
            tvNrp = (TextView) itemView.findViewById(R.id.tv_nrp_peserta);
            tvNama = (TextView) itemView.findViewById(R.id.tv_nama_peserta);
            ivStatusData = (ImageView) itemView.findViewById(R.id.iv_status_data);
            ivStatusDataDiri = (ImageView) itemView.findViewById(R.id.iv_status_data_diri);
            ivStatusDataWajah = (ImageView) itemView.findViewById(R.id.iv_status_data_wajah);
            ivStatusDataTtd = (ImageView) itemView.findViewById(R.id.iv_status_data_ttd);
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
