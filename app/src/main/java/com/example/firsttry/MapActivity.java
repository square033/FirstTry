package com.example.firsttry;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.FrameLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Map;

public class MapActivity extends AppCompatActivity {

    private View positionDot;
    private ImageView mapImage;
    private final Handler handler = new Handler();
    private final int interval = 500;
    private MapCanvasView mapCanvasView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs = getSharedPreferences("location_pref", MODE_PRIVATE);
        int currentX = prefs.getInt("current_x", -1);
        int currentY = prefs.getInt("current_y", -1);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        positionDot = findViewById(R.id.position_dot);
        positionDot.setVisibility(View.GONE);  // 빨간 점 안 보이게 하기
        
        // mapImage = findViewById(R.id.map_image);
        mapCanvasView = findViewById(R.id.map_canvas);  // XML에 있는 View 연결

        // 검색 기능 연결
        EditText searchInput = findViewById(R.id.productSearchInput);
        Button searchButton = findViewById(R.id.searchButton);

        searchButton.setOnClickListener(v -> {
            String product = searchInput.getText().toString().trim();
            if (!product.isEmpty()) {
                mapCanvasView.navigateToProduct(product); // 실제 경로 안내 호출
            } else {
                Toast.makeText(this, "상품명을 입력하세요", Toast.LENGTH_SHORT).show();
            }
        });

        Button arriveButton = findViewById(R.id.arriveButton);
        arriveButton.setOnClickListener(v -> {
            String productName = prefs.getString("last_product_name", null);

            if (productName != null) {
                Map<String, int[]> productMap = new HashMap<>();
                productMap.put("ㅏ", new int[]{1, 74});
                productMap.put("ㅑ", new int[]{13, 68});
                productMap.put("ㅓ", new int[]{33, 78});
                productMap.put("ㅕ", new int[]{55, 68});
                productMap.put("ㅗ", new int[]{71, 78});
                productMap.put("ㅛ", new int[]{91, 78});
                productMap.put("ㅜ", new int[]{106, 68});
                productMap.put("ㅣ", new int[]{125, 65});

                int[] coord = productMap.get(productName);
                if (coord != null) {
                    prefs.edit()
                            .putInt("current_x", coord[1])
                            .putInt("current_y", coord[0])
                            .apply();

                    Toast.makeText(this, "상품 위치로 이동 완료!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "상품 위치를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "최근 검색한 상품이 없습니다.", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.post(updateRunnable);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(updateRunnable);
    }

    private final Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            updateUserPosition();
            handler.postDelayed(this, interval);
        }
    };

    private void updateUserPosition() {
        SharedPreferences prefs = getSharedPreferences("location_pref", MODE_PRIVATE);
        int row = prefs.getInt("current_y", -1);  // y
        int col = prefs.getInt("current_x", -1);  // x
        mapCanvasView.setCurrentPosition(row, col);
    }

}