package com.example.firsttry;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.FrameLayout;
import android.view.ViewGroup;
import androidx.appcompat.app.AppCompatActivity;


public class MapActivity extends AppCompatActivity {

    private View positionDot;
    private ImageView mapImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        positionDot = findViewById(R.id.position_dot);
        mapImage = findViewById(R.id.map_image);

        // 전달된 좌표 값 받기
        double x = getIntent().getDoubleExtra("x", 0);
        double y = getIntent().getDoubleExtra("y", 0);

        // 도면 좌표로 변환
        mapImage.post(() -> {
              int width = mapImage.getWidth();
            int height = mapImage.getHeight();

            int px = (int) (x / 4.0 * width);
            int py = (int) (y / 3.0 * height);

            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) positionDot.getLayoutParams();
            params.leftMargin = px - positionDot.getWidth() / 2;
            params.topMargin = py - positionDot.getHeight() / 2;
            positionDot.setLayoutParams(params);
            positionDot.setVisibility(View.VISIBLE);
        });
    }
}


