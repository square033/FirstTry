package com.example.firsttry;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EasyPayActivity extends AppCompatActivity {

    private DBHelper dbHelper;
    //private String userPhoneTail;
    private String userPhoneFull;
    private JSONArray items;
    private String date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_easy_pay);

        dbHelper = new DBHelper(this);

        SharedPreferences prefs = getSharedPreferences("login_pref", MODE_PRIVATE);
        String userPhoneFull = prefs.getString("phone_full", null);
        if (userPhoneFull == null) {
            Toast.makeText(this, "로그인 정보 없음", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String jsonStr = getIntent().getStringExtra("qr_json");
        date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        try {
            JSONObject data = new JSONObject(jsonStr);
            items = data.getJSONArray("items");
        } catch (JSONException e) {
            Toast.makeText(this, "JSON 오류", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 버튼 연결
        findViewById(R.id.btn_kakao).setOnClickListener(v -> save("카카오페이"));
        findViewById(R.id.btn_payco).setOnClickListener(v -> save("페이코"));
        findViewById(R.id.btn_toss).setOnClickListener(v -> save("토스페이"));
        findViewById(R.id.btn_naver).setOnClickListener(v -> save("네이버페이"));
        findViewById(R.id.btn_card).setOnClickListener(v -> save("카드결제"));
    }

    private void save(String method) {
        try {
            String paymentId = "pay_" + System.currentTimeMillis();
            int totalPoint = 0;

            for (int i = 0; i < items.length(); i++) {
                JSONObject item = items.getJSONObject(i);
                String name = item.getString("name");
                int qty = item.getInt("quantity");
                int price = item.getInt("price");
                int sum = qty * price;

                String itemText = name + " x" + qty + " [" + method + "]";
                dbHelper.insertPayment(paymentId, userPhoneFull, itemText, sum, date, method);

                totalPoint += sum / 20; // 5% 적립
            }

            dbHelper.addUserPoint(userPhoneFull, totalPoint);

            Toast.makeText(this, method + " 완료!", Toast.LENGTH_LONG).show();

            Intent intent = new Intent(EasyPayActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();

        } catch (JSONException e) {
            Toast.makeText(this, "저장 실패", Toast.LENGTH_SHORT).show();
        }
    }
}
