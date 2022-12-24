package id.ac.umn.week11_42578;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.NonNull;
import android.util.Log;;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {
    private final String KEY = "server";
    private final int SETTING_REQUEST = 1;
    private SharedPreferences mPreferences;
    private String sharedPrefFile;
    private Context context;
    protected static String server;
    private RecyclerView rv;
    static MahasiswaListAdapter adapter;
    private static final int REQUEST_TAMBAH = 2;
    private static final int REQUEST_EDIT = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        sharedPrefFile = context.getPackageName();

        mPreferences = getSharedPreferences(sharedPrefFile, MODE_PRIVATE);
        server = mPreferences.getString(KEY, "https://if570w11umn.berkatjaya.id/");
        rv = (RecyclerView) findViewById(R.id.rvMahasiswa);
        adapter = new MahasiswaListAdapter(this);
        rv.setAdapter(adapter);
        rv.setLayoutManager(new LinearLayoutManager(this));

        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        if (connMgr != null) {
            networkInfo = connMgr.getActiveNetworkInfo();
        }
        if (networkInfo != null && networkInfo.isConnected()) {
            new AsyncAmbilSemuaMahasiswa().execute();
        } else {
            Toast.makeText(MainActivity.this, "Tidak ada Koneksi Jaringan", Toast.LENGTH_LONG).show();
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent addIntent = new Intent(MainActivity.this, DetilActivity.class);
                startActivityForResult(addIntent, REQUEST_TAMBAH);
            }
        });
        ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int posisi = viewHolder.getAdapterPosition();
                Mahasiswa mhs = adapter.getMahasiswaAtPosition(posisi);
                if (direction == ItemTouchHelper.LEFT) {
                    new AsyncHapusMahasiswa(posisi).execute(mhs.getNim());
                } else if (direction == ItemTouchHelper.RIGHT) {
                    Intent editIntent = new Intent(MainActivity.this, DetilActivity.class);
                    editIntent.putExtra("MAHASISWA", mhs);
                    editIntent.putExtra("POSISI", posisi);
                    startActivityForResult(editIntent, REQUEST_EDIT);
                }
            }
        });
        helper.attachToRecyclerView(rv);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settingIntent = new Intent(MainActivity.this, SettingActivity.class);
            startActivityForResult(settingIntent, SETTING_REQUEST);
            return true;
        } else if (id == R.id.action_cari) {
            Intent cariIntent = new Intent(MainActivity.this, CariActivity.class);
            startActivity(cariIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onPause(){
        super.onPause();
        SharedPreferences.Editor preferencesEditor = mPreferences.edit();
        preferencesEditor.putString(KEY, server);
        preferencesEditor.apply();
    }

    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (reqCode == SETTING_REQUEST) {
                SharedPreferences.Editor preferencesEditor = mPreferences.edit();
                preferencesEditor.putString(KEY, server);
                preferencesEditor.apply();
                Toast.makeText(context, "Setting telah disimpan", Toast.LENGTH_LONG).show();
            } else {
                Mahasiswa mhs = (Mahasiswa) data.getSerializableExtra("MAHASISWA");
                if (reqCode == REQUEST_TAMBAH) {
                    new AsyncTambahMahasiswa().execute(mhs);
                } else if (reqCode == REQUEST_EDIT) {
                    int posisi = data.getIntExtra("POSISI", -1);
                    new AsyncUpdateMahasiswa(posisi).execute(mhs);
                }
            }
        } else {
            adapter.notifyDataSetChanged();
        }
    }

    void tutupKoneksi(BufferedReader reader, HttpsURLConnection con) {
        if (con != null) {
            con.disconnect();
        }
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException e) {
                Log.e("WEBSERVICE", "Exception: " + e.getMessage().toString());
            }
        }
    }

    String sambungKeServer(String actionPage, String postString){
        HttpsURLConnection con = null;
        StringBuilder sb = null;
        BufferedReader reader = null;
        String urlParameters = postString;
        byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);
        int postDataLength = postData.length;
        try {
            sb = new StringBuilder();
            String urlString = MainActivity.server + actionPage;
            URL url = new URL(urlString);
            con = (HttpsURLConnection) url.openConnection();
            con.setReadTimeout(10000);
            con.setConnectTimeout(15000);
            con.setRequestMethod("POST");
            con.setDoOutput(true);
            con.setDoInput(true);
            con.setInstanceFollowRedirects(false);
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            con.setRequestProperty("charset", "utf-8");
            con.setRequestProperty("Content-Length", Integer.toString(postDataLength));
            con.setUseCaches(false);
            con.connect();
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.write(postData);
            wr.flush();
            wr.close();
            InputStream is = con.getInputStream();
            reader = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
                sb.append("In");
            }
            if (sb.length() == 0) {
                return null;
            }
            String mhsJSONString = sb.toString();
            JSONObject jsonObject = new JSONObject(mhsJSONString);
            int sukses = (int) jsonObject.getInt("success");
            if (sukses == 1) {
                return "sukses";
            } else {
                return null;
            }
        } catch (Exception e) {
            Log.e("WEBSERVICE", "Exception: " + e.getMessage().toString());
        } finally {
            tutupKoneksi(reader, con);
        }
        return null;
    }

    class AsyncAmbilSemuaMahasiswa extends AsyncTask<Void, Void, List<Mahasiswa>> {
        @Override
        protected List<Mahasiswa> doInBackground(Void... voids) {
            HttpsURLConnection con = null;
            StringBuilder sb = null;
            BufferedReader reader = null;
            List<Mahasiswa> mahasiswas = new ArrayList<>();
            try {
                sb = new StringBuilder();
                String urlString = MainActivity.server + "ambil_semua_mahasiswa.php";
                URL url = new URL(urlString);
                con = (HttpsURLConnection) url.openConnection();
                con.connect();
                InputStream is = con.getInputStream();
                reader = new BufferedReader(new InputStreamReader(is));
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                    sb.append("\n");
                }
                if (sb.length() == 0) {
                    return null;
                }
                String mhsJSONString = sb.toString();
                JSONObject jsonObject = new JSONObject(mhsJSONString);
                int sukses = (int) jsonObject.getInt("success");
                if(sukses == 1) {
                    JSONArray mhsJSONArray = (JSONArray) jsonObject.get("mahasiswas");
                    for (int i = 0; i < mhsJSONArray.length(); i++) {
                        JSONObject mhsJSONobj = (JSONObject) mhsJSONArray.get(i);
                        String nim = mhsJSONobj.getString("nim");
                        String nama = mhsJSONobj.getString("nama");
                        String email = mhsJSONobj.getString("email");
                        String nomorHp = mhsJSONobj.getString("nomorHp");
                        Mahasiswa mhs = new Mahasiswa(nim, nama, email, nomorHp);
                        mahasiswas.add(mhs);
                    }
                    return mahasiswas;
                } else {
                    return null;
                }
            } catch (Exception e) {
                Log.e("WEBSERVICE", "Exception: " + e.getMessage().toString());
            } finally {
                tutupKoneksi(reader, con);
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<Mahasiswa> mahasiswas){
            super.onPostExecute(mahasiswas);
            adapter.setDaftarMahasiswa(mahasiswas);
        }
    }

    class AsyncHapusMahasiswa extends AsyncTask<String, Void, String> {
        private int mPosisi;

        AsyncHapusMahasiswa(int posisi) {
            mPosisi = posisi;
        }

        @Override
        protected String doInBackground(String... strings) {
            return sambungKeServer("hapus_mahasiswa.php", "nim=" + strings[0]);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if ((s != null) && (s.equalsIgnoreCase("sukses"))) {
                adapter.hapusData(mPosisi);
                Toast.makeText(MainActivity.this, "Mahasiswa Berhasil dihapus", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(MainActivity.this, "Mahasiswa gagal dihapus", Toast.LENGTH_LONG).show();
            }
        }
    }

    class AsyncTambahMahasiswa extends AsyncTask<Mahasiswa, Void, String>{
        Mahasiswa mhs;
        @Override
        protected String doInBackground(Mahasiswa... mahasiswas) {
            mhs = mahasiswas[0];
            Log.d("DEBUG", mhs+"");
            String urlParameters = "nim="+mhs.getNim()+"&"+
                    "nama="+mhs.getNama()+"&"+
                    "email="+mhs.getEmail()+"&"+
                    "nomorHp="+mhs.getNomorhp();
            return sambungKeServer("tambah_mahasiswa.php", urlParameters);
        }
        @Override
        protected void onPostExecute(String s){
            super.onPostExecute(s);
            if((s != null) && (s.equalsIgnoreCase("sukses"))){
                adapter.tambahData(mhs);
                Toast.makeText(MainActivity.this, "Mahasiswa Berhasil ditambahkan", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Mahasiswa gagal ditambahkan", Toast.LENGTH_LONG).show();
            }
        }
    }

    class AsyncUpdateMahasiswa extends AsyncTask<Mahasiswa, Void, String> {
        Mahasiswa mhs;
        int mPosisi;

        AsyncUpdateMahasiswa(int posisi) {
            mPosisi = posisi;
        }

        @Override
        protected String doInBackground(Mahasiswa... mahasiswas) {
            mhs = mahasiswas[0];
            String urlParameters = "nim=" + mhs.getNim() + "&" +
                    "nama=" + mhs.getNama() + "&" +
                    "email=" + mhs.getEmail() + "&" +
                    "nomorHp=" + mhs.getNomorhp();
            return sambungKeServer("update_mahasiswa.php", urlParameters);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if ((s != null) && (s.equalsIgnoreCase("sukses"))) {
                adapter.updateData(mPosisi, mhs);
                Toast.makeText(MainActivity.this, "Mahasiswa Berhasil diupdate", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Mahasiswa gagal diupdate", Toast.LENGTH_LONG).show();
            }
        }
    }
}