package id.ac.umn.week11_42578;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class DetilActivity extends AppCompatActivity {
    private EditText etNim, etNama, etEmail, etNomorHP;
    private  int posisiUpdate = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detil);
        etNim= findViewById(R.id.etNim);
        etNama = findViewById(R.id.etNama);
        etEmail = findViewById(R.id.etEmail);
        etNomorHP = findViewById(R.id.etNomorHp);
    }
    public void simpanData(View view){
        String mNIM = etNim.getText().toString();
        String mNama = etNama.getText().toString();
        String mEmail = etEmail.getText().toString();
        String mNoHp = etNomorHP.getText().toString();

        if(mNIM.length() <= 0 || mNama.length() <= 0 ||
                mEmail.length()<= 0 || mNoHp.length() <=0){
            Toast.makeText(this, "Semua harus diisi", Toast.LENGTH_LONG).show();
        }
        else{
            Intent intentJawab = new Intent();
            if(etNim.isEnabled()){
                mNIM = mNIM.trim();
                mNIM= ("000000"+ mNIM).substring(mNIM.length());
            }else{
                intentJawab.putExtra("POSISI",posisiUpdate);
            }
            Mahasiswa mhs = new Mahasiswa(mNIM, mNama, mEmail, mNoHp);
            intentJawab.putExtra("MAHASISWA", mhs);
            setResult(RESULT_OK, intentJawab);
            finish();
        }

    }

    public void batal(View view){
        Intent intentJawab = new Intent();
        setResult(RESULT_CANCELED, intentJawab);
        finish();
    }
}