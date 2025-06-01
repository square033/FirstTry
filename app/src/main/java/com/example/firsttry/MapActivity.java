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



                Map<String, int[]> productMap = new HashMap<>();
                productMap.put("생수", new int[]{1, 74});
                productMap.put("장난감", new int[]{48, 72});
                productMap.put("초코송이", new int[]{80, 72});
                productMap.put("인형", new int[]{120, 72});
                productMap.put("햄버거세트", new int[]{123, 15});
                mapCanvasView.setProductMap(productMap);

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