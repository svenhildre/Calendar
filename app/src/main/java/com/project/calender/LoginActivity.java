package com.project.calender;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button loginButton;
    private TextView registerTextView;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // XML dosyasındaki bileşenler örnekleniyor
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        registerTextView = findViewById(R.id.registerTextView);

        // Firebase Authentication örneği oluşturuluyor
        mAuth = FirebaseAuth.getInstance();

        // Kullanıcının oturum açık olup olmadığını kontrol edin
        if (mAuth.getCurrentUser() != null) {
            // Kullanıcı oturum açık, CalendarActivity'yi başlatın
            Intent intent = new Intent(LoginActivity.this, CalendarActivity.class);
            startActivity(intent);
            finish();
        }

        // Kayıt olma metnine tıklandığında RegisterActivity başlatılır
        registerTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        // Giriş düğmesine tıklandığında login() metodu çağrılır
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });
    }

    private void login() {
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        // E-posta ve şifre alanları boş olmamalı
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Lütfen E-Mailinizi giriniz", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Lütfen Şifrenizi Giriniz", Toast.LENGTH_SHORT).show();
            return;
        }

        // Firebase Authentication ile kullanıcı girişi yap
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Giriş başarılı, CalendarActivity'yi başlat
                            Intent intent = new Intent(LoginActivity.this, CalendarActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            // Giriş başarısız, hata mesajı göster
                            Toast.makeText(LoginActivity.this, "Giriş Başarısız", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}