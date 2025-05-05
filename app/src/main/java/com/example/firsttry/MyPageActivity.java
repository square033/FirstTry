package com.example.firsttry;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button;
import android.content.Intent;

public class MyPageActivity extends AppCompatActivity {

    private TextView nameText, phoneText, pointText, historyText;
    private DBHelper dbHelper;
    private String loggedInUserPhoneTail;// 로그인 시 저장된 사용자 ID를 불러와야 함

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mypage);

        nameText = findViewById(R.id.name_text);
        phoneText = findViewById(R.id.phone_text);
        pointText = findViewById(R.id.point_text);
        historyText = findViewById(R.id.history_text);

        SharedPreferences prefs = getSharedPreferences("login_pref", MODE_PRIVATE);
        loggedInUserPhoneTail = prefs.getString("phone_tail", null);

        if (loggedInUserPhoneTail == null) {
            // 저장된 로그인 정보 없음 → 로그인 화면으로 이동
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }


        dbHelper = new DBHelper(this);
        Button logoutBtn = findViewById(R.id.btn_logout);
        logoutBtn.setOnClickListener(v -> logout());


        loadUserInfo();
        loadPaymentHistory();
    }
    private void logout() {
        SharedPreferences prefs = getSharedPreferences("login_pref", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();

        Intent intent = new Intent(MyPageActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void loadUserInfo() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM users WHERE phone_tail = ?", new String[]{loggedInUserPhoneTail});

        if (cursor.moveToFirst()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            String phone = cursor.getString(cursor.getColumnIndexOrThrow("phone_tail"));
            int point = cursor.getInt(cursor.getColumnIndexOrThrow("point"));

            nameText.setText("이름: " + name);
            phoneText.setText("전화번호: " + phone);
            pointText.setText("포인트: " + point);
        }
        cursor.close();
    }

    private void loadPaymentHistory() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT DISTINCT payment_id FROM payments WHERE user_id = ? ORDER BY date DESC", new String[]{loggedInUserPhoneTail});
        StringBuilder history = new StringBuilder();

        while (cursor.moveToNext()) {
            String paymentId = cursor.getString(0);

            Cursor paymentCursor = db.rawQuery("SELECT * FROM payments WHERE payment_id = ? ORDER BY id", new String[]{paymentId});
            int total = 0;
            String method = "";
            String date = "";
            StringBuilder items = new StringBuilder();

            while (paymentCursor.moveToNext()) {
                String item = paymentCursor.getString(paymentCursor.getColumnIndexOrThrow("item"));
                int amount = paymentCursor.getInt(paymentCursor.getColumnIndexOrThrow("amount"));
                date = paymentCursor.getString(paymentCursor.getColumnIndexOrThrow("date"));
                method = paymentCursor.getString(paymentCursor.getColumnIndexOrThrow("method"));
                total += amount;
                items.append(item).append(" - ").append(amount).append("원\n");
            }

            int point = total / 20; // 예: 5% 적립

            history.append("📅 ").append(date).append("\n")
                    .append(items)
                    .append("총액: ").append(total).append("원\n")
                    .append("포인트 적립: ").append(point).append("P\n")
                    .append("결제수단: ").append(method).append("\n\n");

            paymentCursor.close();
        }

        cursor.close();
        historyText.setText(history.toString());
    }


}
