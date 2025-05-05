package com.example.firsttry;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

public class QRscanActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ZXing QR 스캔 설정
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setPrompt("QR 코드를 스캔하세요");
        integrator.setBeepEnabled(true);
        integrator.setOrientationLocked(false);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() != null) {
                String scannedData = result.getContents();

                if (scannedData.startsWith("http://") || scannedData.startsWith("https://")) {
                    // URL이면 브라우저로 열기
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(scannedData));
                    startActivity(browserIntent);
                } else {
                    // JSON 여부 확인
                    try {
                        JSONObject json = new JSONObject(scannedData);
                        Intent intent = new Intent(this, ReceiptActivity.class);
                        intent.putExtra("qr_json", json.toString());
                        startActivity(intent);
                    } catch (JSONException e) {
                        // JSON 아님 → 그냥 텍스트로 Toast 출력
                        Toast.makeText(this, "QR 내용: " + scannedData, Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                Toast.makeText(this, "QR 스캔이 취소되었습니다", Toast.LENGTH_SHORT).show();
            }

            finish(); // 결과 처리 후 종료
        }
    }
}
