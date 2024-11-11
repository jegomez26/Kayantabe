package edu.sti.kayantabe;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class RegistrationOptionsActivity extends AppCompatActivity {

    private Button btn_cust, btn_sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registration_options);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btn_cust = findViewById(R.id.option_customer);
        btn_sp = findViewById(R.id.option_service_provider);

        btn_sp.setOnClickListener(v -> {
            Intent intent = new Intent(RegistrationOptionsActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        btn_cust.setOnClickListener(v -> {
            Intent intent = new Intent(RegistrationOptionsActivity.this, CustomerRegistrationActivity.class);
            startActivity(intent);
        });
    }
}