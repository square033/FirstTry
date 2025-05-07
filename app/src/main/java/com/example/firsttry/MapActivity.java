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
        mapImage = findViewById(R.id.map_image);
        mapCanvasView = findViewById(R.id.map_canvas);  // XML에 있는 View 연결

        // 전달된 좌표 초기 표시 (선택사항)
        double x = getIntent().getDoubleExtra("x", 0);
        double y = getIntent().getDoubleExtra("y", 0);
        GlobalLocation.updateGridPosition(x, y, 0.64);

        mapImage.post(() -> updateUserPosition());

        // 검색 기능 연결
        EditText searchInput = findViewById(R.id.productSearchInput);
        Button searchButton = findViewById(R.id.searchButton);

        searchButton.setOnClickListener(v -> {
            String product = searchInput.getText().toString().trim();
            if (!product.isEmpty()) {
                Toast.makeText(this, "[경로 안내 기능은 Canvas 기반으로 별도 구현 필요]", Toast.LENGTH_SHORT).show();
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
        double x = GlobalLocation.x;
        double y = GlobalLocation.y;
        if (x < 0 || y < 0) return;

        GlobalLocation.updateGridPosition(x, y, 0.64);

        int cellWidth = mapImage.getWidth() / 18;
        int cellHeight = mapImage.getHeight() / 25;
        int[] pxPos = GlobalLocation.toPixel(cellWidth, cellHeight);

        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) positionDot.getLayoutParams();
        params.leftMargin = pxPos[0] - positionDot.getWidth() / 2;
        params.topMargin = pxPos[1] - positionDot.getHeight() / 2;
        positionDot.setLayoutParams(params);
        positionDot.setVisibility(View.VISIBLE);

        // 내부 지도에 위치도 갱신
        int row = (int)(y / 0.64);
        int col = (int)(x / 0.64);
        mapCanvasView.setCurrentPosition(row, col);
    }
}