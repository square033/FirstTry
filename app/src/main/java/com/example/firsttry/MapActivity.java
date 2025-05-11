package com.example.firsttry;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.FrameLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Handler;
import android.os.Looper;


public class MapActivity extends AppCompatActivity {

    private View positionDot;
    private ImageView mapImage;
    private MapCanvasView mapCanvasView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);



        positionDot = findViewById(R.id.position_dot);
        mapImage = findViewById(R.id.map_image);
        mapCanvasView = findViewById(R.id.map_canvas);

        // 전달된 좌표 값 받기
        int x = getIntent().getIntExtra("current_x", 0);
        int y = getIntent().getIntExtra("current_y", 0);

        // 도면 좌표로 변환 및 위치 점 표시
        mapImage.post(() -> {
            int width = mapImage.getWidth();
            int height = mapImage.getHeight();

            int px = (int) ((float) x / 93 * width);   // 가로 14칸 기준
            int py = (int) ((float) y / 135 * height);  // 세로 11칸 기준

            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) positionDot.getLayoutParams();
            params.leftMargin = px - positionDot.getWidth() / 2;
            params.topMargin = py - positionDot.getHeight() / 2;
            positionDot.setLayoutParams(params);
            positionDot.setVisibility(View.VISIBLE);
        });
        mapCanvasView.setCurrentPosition(getIntent().getIntExtra("current_y", 0),
                getIntent().getIntExtra("current_x", 0));

        // 검색 기능 추가
        EditText searchInput = findViewById(R.id.productSearchInput);
        Button searchButton = findViewById(R.id.searchButton);

        searchButton.setOnClickListener(v -> {
            String product = searchInput.getText().toString().trim();
            if (!product.isEmpty()) {
                mapCanvasView.navigateToProduct(product);
            } else {
                Toast.makeText(this, "상품명을 입력하세요", Toast.LENGTH_SHORT).show();
            }
        });
        startAutoRefresh();
    }
    private void refreshPositionFromPreference() {
        SharedPreferences prefs = getSharedPreferences("location_pref", MODE_PRIVATE);
        int x = prefs.getInt("current_x", 0);
        int y = prefs.getInt("current_y", 0);

        // 좌표 적용
        mapCanvasView.setCurrentPosition(y, x);

        mapImage.post(() -> {
            int width = mapImage.getWidth();
            int height = mapImage.getHeight();

            int px = (int) ((float) x / 93 * width);
            int py = (int) ((float) y / 135 * height);

            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) positionDot.getLayoutParams();
            params.leftMargin = px - positionDot.getWidth() / 2;
            params.topMargin = py - positionDot.getHeight() / 2;
            positionDot.setLayoutParams(params);
            positionDot.setVisibility(View.VISIBLE);
        });
    }

    private void startAutoRefresh() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            refreshPositionFromPreference();  // ✅ 주기적으로 좌표 반영
            startAutoRefresh();               // 반복
        }, 2000);  // 2초 간격
    }
}