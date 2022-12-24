package id.ac.umn.week11_42578;

import java.io.Serializable;

public class Mahasiswa implements Serializable {
    private String nim;
    private String nama;
    private String email;
    private String nomorhp;

    public Mahasiswa(String nim, String nama, String email, String nomorhp){
        this.nim = nim;
        this.nama = nama;
        this.email = email;
        this.nomorhp = nomorhp;
    }

    public void setNim(String nim) {
        this.nim = nim;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setNomorhp(String nomorhp) {
        this.nomorhp = nomorhp;
    }

    public String getNim(){
        return this.nim;
    }

    public String getNama(){
        return this.nama;
    }

    public String getEmail(){
        return this.email;
    }

    public String getNomorhp(){
        return this.nomorhp;
    }

}




