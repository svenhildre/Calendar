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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;


public class RegisterActivity extends AppCompatActivity {

    private EditText mNameField;
    private EditText mSurnameField;
    private EditText mUsernameField;
    private EditText mEmailField;
    private EditText mPasswordField;
    private EditText mTCField;
    private EditText mPhoneField;
    private EditText mAddressField;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Firebase örnekleri oluşturuluyor
        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        // XML dosyasındaki bileşenler örnekleniyor
        mNameField = findViewById(R.id.nameField);
        mSurnameField = findViewById(R.id.surnameField);
        mUsernameField = findViewById(R.id.usernameField);
        mEmailField = findViewById(R.id.emailField);
        mPasswordField = findViewById(R.id.passwordField);
        mTCField = findViewById(R.id.tcField);
        mPhoneField = findViewById(R.id.phoneField);
        mAddressField = findViewById(R.id.addressField);

        // Kayıt ol butonuna tıklama işlemi
        Button mRegisterButton = findViewById(R.id.registerButton);
        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Kullanıcının girdiği bilgiler alınıyor
                String name = mNameField.getText().toString();
                String surname = mSurnameField.getText().toString();
                String username = mUsernameField.getText().toString();
                String email = mEmailField.getText().toString();
                String password = mPasswordField.getText().toString();
                String tc = mTCField.getText().toString();
                String phone = mPhoneField.getText().toString();
                String address = mAddressField.getText().toString();

                // Kullanıcının girdiği bilgilerin geçerli olup olmadığı kontrol ediliyor
                if (TextUtils.isEmpty(name) || TextUtils.isEmpty(surname) || TextUtils.isEmpty(username) ||
                        TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(tc) ||
                        TextUtils.isEmpty(phone) || TextUtils.isEmpty(address)) {
                    Toast.makeText(RegisterActivity.this, "Lütfen tüm alanları doldurun", Toast.LENGTH_SHORT).show();
                } else {
                    // Yeni bir kullanıcı hesabı oluşturuluyor
                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Kullanıcının UID'si alınıyor
                                        String userID = mAuth.getCurrentUser().getUid();

                                        // Kullanıcının bilgileri Firestore veritabanına kaydediliyor
                                        Map<String, Object> userMap = new HashMap<>();
                                        userMap.put("name", name);
                                        userMap.put("surname", surname);
                                        userMap.put("username", username);
                                        userMap.put("email", email);
                                        userMap.put("tc", tc);
                                        userMap.put("phone", phone);
                                        userMap.put("address", address);

                                        mFirestore.collection("users").document(userID)
                                                .set(userMap)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        // Kayıt işlemi başarılı olduğunda ana ekrana yönlendirme yapılıyor
                                                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                                        startActivity(intent);
                                                        finish();
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(RegisterActivity.this, "Kayıt işlemi başarısız oldu", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    } else {
                                        Toast.makeText(RegisterActivity.this, "Kayıt işlemi başarısız oldu", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });

        // Giriş bağlantısına tıklama işlemi
        TextView mLoginLink = findViewById(R.id.loginTextView);
        mLoginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Giriş ekranına yönlendirme işlemi
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}