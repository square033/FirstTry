package com.example.firsttry;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class FullPhoneLoginActivity extends AppCompatActivity {

    private EditText editFullPhone;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_phone_login);

        editFullPhone = findViewById(R.id.edit_full_phone);
        Button btnLogin = findViewById(R.id.btn_full_login);
        dbHelper = new DBHelper(this);

        btnLogin.setOnClickListener(v -> {
            String fullPhone = editFullPhone.getText().toString().trim();

            if (fullPhone.length() < 10) {
                Toast.makeText(this, "전체 전화번호를 정확히 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean exists = dbHelper.checkUserByFullPhone(fullPhone);

            if (exists) {
                String tail = fullPhone.substring(fullPhone.length() - 4);
                String name = dbHelper.getUserNameByFullPhone(fullPhone);  // 🔥 이름 가져오기

                // 로그인 성공 처리
                SharedPreferences prefs = getSharedPreferences("login_pref", MODE_PRIVATE);
                prefs.edit()
                        .putString("phone_tail", tail)
                        .putString("phone_full", fullPhone)
                        .putString("user_name", name)  // 🔥 이름도 저장
                        .apply();

                Toast.makeText(this, "로그인 성공!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("user_name", name);  // ✅ 이름 전달
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }

            else {
                Toast.makeText(this, "일치하는 회원 정보가 없습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
