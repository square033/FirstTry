package com.example.firsttry;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.FrameLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MapActivity extends AppCompatActivity {

    private View positionDot;
    private ImageView mapImage;
    private final Handler handler = new Handler();
    private final int interval = 500;
    private MapCanvasView mapCanvasView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        positionDot = findViewById(R.id.position_dot);
        positionDot.setVisibility(View.GONE);  // 빨간 점 안 보이게 하기
        
        mapImage = findViewById(R.id.map_image);
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
        int row = MainActivity.currentPosition[0];
        int col = MainActivity.currentPosition[1];
        mapCanvasView.setCurrentPosition(row, col);
    }
}