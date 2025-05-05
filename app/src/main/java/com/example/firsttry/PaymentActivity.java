// PaymentActivity.java
package com.example.firsttry;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class PaymentActivity extends AppCompatActivity {

    private TextView qrResultText;
    private Button payButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        qrResultText = findViewById(R.id.qr_result_text);
        payButton = findViewById(R.id.pay_button);

        // QR 데이터 받기
        String qrData = getIntent().getStringExtra("qr_result");
        if (qrData != null) {
            qrResultText.setText("결제 정보:\n" + qrData); // QR 내용 표시
        }

        payButton.setOnClickListener(v -> {
            // 실제 결제 처리 로직 (예: DB 저장)
        });
    }
}
