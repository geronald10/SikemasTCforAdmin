package absensi.anif.its.ac.id.sikemastcforadmin.model;

import java.io.Serializable;

public class Mahasiswa implements Serializable {

    public String nrp;
    public String nama;
    public String password;
    public String noHandphone;
    public int statusDataDiri;
    public int statusDataWajah;
    public int statusDataTtd;

    public Mahasiswa(String nrp, String nama, int statusDataDiri, int statusDataWajah, int statusDataTtd) {
        this.nrp = nrp;
        this.nama = nama;
        this.statusDataDiri = statusDataDiri;
        this.statusDataWajah = statusDataWajah;
        this.statusDataTtd = statusDataTtd;
    }

    public Mahasiswa(String nrp, String nama, int statusDataDiri) {
        this.nrp = nrp;
        this.nama = nama;
        this.statusDataDiri = statusDataDiri;
    }

    public Mahasiswa(String nrp, String nama, String password, String noHandphone) {
        this.nrp = nrp;
        this.nama = nama;
        this.password = password;
        this.noHandphone = noHandphone;
    }

    public String getNrp() {
        return nrp;
    }

    public void setNrp(String nrp) {
        this.nrp = nrp;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNoHandphone() {
        return noHandphone;
    }

    public void setNoHandphone(String noHandphone) {
        this.noHandphone = noHandphone;
    }

    public int getStatusDataDiri() {
        return statusDataDiri;
    }

    public void setStatusDataDiri(int statusDataDiri) {
        this.statusDataDiri = statusDataDiri;
    }

    public int getStatusDataWajah() {
        return statusDataWajah;
    }

    public void setStatusDataWajah(int statusDataWajah) {
        this.statusDataWajah = statusDataWajah;
    }

    public int getStatusDataTtd() {
        return statusDataTtd;
    }

    public void setStatusDataTtd(int statusDataTtd) {
        this.statusDataTtd = statusDataTtd;
    }
}