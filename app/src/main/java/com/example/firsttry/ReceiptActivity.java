package com.example.firsttry;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ReceiptActivity extends AppCompatActivity {

    private TextView receiptView;
    private DBHelper dbHelper;
    //private String userPhoneTail;
    private String userPhoneFull;
    private JSONArray items;
    private int totalAmount;
    private String date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt);

        receiptView = findViewById(R.id.receipt_text);
        dbHelper = new DBHelper(this);

        SharedPreferences prefs = getSharedPreferences("login_pref", MODE_PRIVATE);
        //userPhoneTail = prefs.getString("phone_tail", null);
        userPhoneFull = prefs.getString("phone_full", null);
        if (userPhoneFull == null) {
            receiptView.setText("\u26a0\ufe0f 로그인 정보 없음");
            return;
        }

        String jsonStr = getIntent().getStringExtra("qr_json");
        date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        try {
            JSONObject data = new JSONObject(jsonStr);
            String user = data.getString("user");
            totalAmount = data.getInt("total");
            items = data.getJSONArray("items");

            StringBuilder displayText = new StringBuilder();
            displayText.append("[사용자] ").append(user).append("\n");
            displayText.append("[총 결제 금액] ").append(String.format("%,d원", totalAmount)).append("\n\n");
            displayText.append("[구매 내역]\n");

            for (int i = 0; i < items.length(); i++) {
                JSONObject item = items.getJSONObject(i);
                String name = item.getString("name");
                int qty = item.getInt("quantity");
                int price = item.getInt("price");
                int sum = qty * price;

                displayText.append("\u2022 ").append(name).append(" x").append(qty)
                        .append(" - ").append(String.format("%,d원", sum)).append("\n");
            }

            receiptView.setText(displayText.toString());

        } catch (JSONException e) {
            receiptView.setText("\u26a0\ufe0f JSON 오류: " + e.getMessage());
            return;
        }

        findViewById(R.id.btn_store).setOnClickListener(v -> showStaffCodeDialog());
        findViewById(R.id.btn_easy).setOnClickListener(v -> {
            Intent intent = new Intent(ReceiptActivity.this, EasyPayActivity.class);
            intent.putExtra("qr_json", getIntent().getStringExtra("qr_json"));
            startActivity(intent);
        });
    }

    private void showStaffCodeDialog() {
        EditText input = new EditText(this);
        input.setHint("직원 코드 입력");

        new AlertDialog.Builder(this)
                .setTitle("직원 확인")
                .setMessage("직원 인증코드를 입력하세요")
                .setView(input)
                .setPositiveButton("확인", (dialog, which) -> {
                    String code = input.getText().toString().trim();
                    if (code.equals("3564")) {
                        saveToDatabase("현장결제");
                    } else {
                        Toast.makeText(this, "\u274c 인증 실패: 잘못된 코드", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("취소", null)
                .show();
    }

    private void saveToDatabase(String method) {
        try {
            String paymentId = "pay_" + System.currentTimeMillis();
            int totalPoint = 0;

            for (int i = 0; i < items.length(); i++) {
                JSONObject item = items.getJSONObject(i);
                String name = item.getString("name");
                int qty = item.getInt("quantity");
                int price = item.getInt("price");
                int sum = qty * price;

                // 항목명에 [결제수단] 포함 없애지기
                String itemText = method.equals("현장결제") ? name + " x" + qty : name + " x" + qty + " [" + method + "]";
                dbHelper.insertPayment(paymentId, userPhoneFull, itemText, sum, date, method);

                totalPoint += sum / 20;  // 5% 적립
            }

            dbHelper.addUserPoint(userPhoneFull, totalPoint);

            Toast.makeText(this, method + " 완료 및 저장됨!", Toast.LENGTH_LONG).show();

            Intent intent = new Intent(ReceiptActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();

        } catch (JSONException e) {
            Toast.makeText(this, "저장 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
