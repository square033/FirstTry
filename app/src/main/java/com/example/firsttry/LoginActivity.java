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

        // ✅ 1. 초기 회원 등록 (초기 1회만)
        SharedPreferences initPrefs = getSharedPreferences("init_pref", MODE_PRIVATE);
        boolean isInitialized = initPrefs.getBoolean("user_initialized", false);

        if (!isInitialized) {
            dbHelper.insertUser("홍길동", "1111", "01012341111");
            dbHelper.insertUser("이영희", "2222", "01012342222");
            dbHelper.insertUser("김철수", "1111", "01011111111");
            SharedPreferences.Editor editor = initPrefs.edit();
            editor.putBoolean("user_initialized", true);
            editor.apply();
        }

        // ✅ 2. 로그인 처리
        findViewById(R.id.btn_login).setOnClickListener(v -> {
            String phoneTail = editPhoneTail.getText().toString().trim();
            String phoneFull = dbHelper.getUserFullPhone(phoneTail);

            if (phoneTail.length() != 4) {
                Toast.makeText(this, "전화번호 뒷자리 4자리를 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            int count = dbHelper.countUsersByPhoneTail(phoneTail);

            if (count == 0) {
                Toast.makeText(this, "해당 전화번호로 등록된 회원이 없습니다.", Toast.LENGTH_SHORT).show();
            } else if (count > 1) {
                // 중복된 경우 전체 번호 입력 화면으로 전환
                Toast.makeText(this, "동일한 뒷자리 회원이 여러 명 있습니다.\n전체 번호를 입력해주세요.", Toast.LENGTH_LONG).show();

                Intent intent = new Intent(this, FullPhoneLoginActivity.class);
                intent.putExtra("phone_tail", phoneTail);
                startActivity(intent);
                finish();
            } else {
                // 로그인 성공 처리
                String name = dbHelper.getUserName(phoneTail); // 이름 가져오기

                SharedPreferences prefs = getSharedPreferences("login_pref", MODE_PRIVATE);
                prefs.edit()
                        .putString("phone_tail", phoneTail)
                        .putString("phone_full", phoneFull)
                        .putString("user_name", name)  // ✅ 여기에 저장해야 MainActivity에서 쓸 수 있어!
                        .putBoolean("just_logged_in", true)
                        .apply();

                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);  // 🔥 추가
                startActivity(intent);

                finish();
            }
        });
    }
}
