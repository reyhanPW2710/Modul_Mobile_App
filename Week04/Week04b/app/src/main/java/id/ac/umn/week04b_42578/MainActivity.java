package id.ac.umn.week04b_42578;

//import androidx.activity.result.ActivityResult;
//import androidx.activity.result.ActivityResultCallback;
//import androidx.activity.result.ActivityResultLauncher;
//import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {
    private Button btnHalaman1, btnHalaman2;
    private EditText etNama;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnHalaman1 = findViewById(R.id.main_button_change_1);
        btnHalaman2 = findViewById(R.id.main_button_change_2);
        etNama = findViewById(R.id.etNama);

        btnHalaman1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentkirim = new Intent(MainActivity.this, SecondActivity.class);
                String data = etNama.getText().toString();
                intentkirim.putExtra("KirimanNama", data);
                startActivity(intentkirim);
            }
        });

        btnHalaman2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentshow = new Intent(MainActivity.this, ThirdActivity.class);
                String data = etNama.getText().toString();
                intentshow.putExtra("PaketdariMain", data);
                startActivity(intentshow);
            }
        });
    }
}