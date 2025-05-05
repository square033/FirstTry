package com.example.firsttry;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
public class LoginActivity extends AppCompatActivity {

    private EditText editPhoneTail;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editPhoneTail = findViewById(R.id.edit_phone_tail);
        dbHelper = new DBHelper(this);

        // ✅ 1. 초기 회원 등록
        SharedPreferences initPrefs = getSharedPreferences("init_pref", MODE_PRIVATE);
        boolean isInitialized = initPrefs.getBoolean("user_initialized", false);

        if (!isInitialized) {
            dbHelper.insertUser("홍길동", "1111");
            dbHelper.insertUser("이영희", "2222");
            SharedPreferences.Editor editor = initPrefs.edit();
            editor.putBoolean("user_initialized", true);
            editor.apply();
        }

        // ✅ 2. 로그인 처리 로직
        findViewById(R.id.btn_login).setOnClickListener(v -> {
            String phoneTail = editPhoneTail.getText().toString().trim();

            if (phoneTail.length() != 4) {
                Toast.makeText(this, "전화번호 뒷자리 4자리를 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (dbHelper.checkUserByPhoneTail(phoneTail)) {
                String name = dbHelper.getUserName(phoneTail);
                Toast.makeText(this, "환영합니다, " + name + "님!", Toast.LENGTH_SHORT).show();

                // 로그인 정보 저장
                SharedPreferences prefs = getSharedPreferences("login_pref", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("phone_tail", phoneTail);
                editor.apply();

                // 메인 화면으로 이동
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "사용자를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
        });

    }}