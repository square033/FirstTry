package com.example.firsttry;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.*;

public class MapCanvasView extends View {

    private static final int[][] GRID_MAP = {
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {1, 1, 1, 1, 1, 1, 0, 1, 1, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 1, 1, 1, 0, 0, 1, 0, 1, 1},
            {1, 1, 0, 1, 0, 0, 1, 1, 1, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 1, 1, 1, 1, 0, 1, 1, 1, 1},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
    };

    private int cellSize;
    private Paint wallPaint;
    private Paint pathPaint;
    private List<int[]> path = new ArrayList<>();
    private int[] currentPos = {2, 1};
    private Map<String, int[]> productMap = new HashMap<>();
    private Bitmap markerBitmap, flagBitmap;
    private float pathProgress = 0f;

    public MapCanvasView(Context context, AttributeSet attrs) {
        super(context, attrs);

        wallPaint = new Paint();
        wallPaint.setColor(Color.GRAY);

        pathPaint = new Paint();
        pathPaint.setColor(Color.BLUE);
        pathPaint.setStrokeWidth(10);
        pathPaint.setStyle(Paint.Style.STROKE);
        pathPaint.setStrokeCap(Paint.Cap.ROUND);

        markerBitmap = scaleBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_cart), 32, context);
        flagBitmap = scaleBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_flag), 32, context);

        productMap.put("딸기", new int[]{1, 6});
        productMap.put("우유", new int[]{4, 7});
        productMap.put("초코송이", new int[]{5, 2});
        productMap.put("돼지고기", new int[]{8, 5});
    }

    private Bitmap scaleBitmap(Bitmap bitmap, int dp, Context context) {
        float scale = context.getResources().getDisplayMetrics().density;
        int px = (int) (dp * scale + 0.5f);
        return Bitmap.createScaledBitmap(bitmap, px, px, true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        cellSize = getWidth() / 10;

        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 10; col++) {
                if (GRID_MAP[row][col] == 1) {
                    canvas.drawRect(col * cellSize, row * cellSize,
                            (col + 1) * cellSize, (row + 1) * cellSize, wallPaint);
                }
            }
        }

        int maxSteps = Math.round(path.size() * pathProgress);
        for (int i = 0; i < maxSteps - 1; i++) {
            int[] from = path.get(i);
            int[] to = path.get(i + 1);
            float startX = from[1] * cellSize + cellSize / 2f;
            float startY = from[0] * cellSize + cellSize / 2f;
            float stopX = to[1] * cellSize + cellSize / 2f;
            float stopY = to[0] * cellSize + cellSize / 2f;
            canvas.drawLine(startX, startY, stopX, stopY, pathPaint);
        }

        if (!path.isEmpty()) {
            int[] goal = path.get(path.size() - 1);
            float gx = goal[1] * cellSize + cellSize / 2f - flagBitmap.getWidth() / 2f;
            float gy = goal[0] * cellSize + cellSize / 2f - flagBitmap.getHeight() / 2f;
            canvas.drawBitmap(flagBitmap, gx, gy, null);
        }

        float dotX = currentPos[1] * cellSize + cellSize / 2f - markerBitmap.getWidth() / 2f;
        float dotY = currentPos[0] * cellSize + cellSize / 2f - markerBitmap.getHeight() / 2f;
        canvas.drawBitmap(markerBitmap, dotX, dotY, null);
    }

    public void navigateToProduct(String name) {
        String keyword = name.trim().toLowerCase();
        Log.d("길찾기", "검색된 상품: " + keyword);

        for (String key : productMap.keySet()) {
            if (key.toLowerCase().equals(keyword)) {
                int[] goal = productMap.get(key);
                List<int[]> newPath = bfs(currentPos, goal);
                if (newPath != null) {
                    path = newPath;
                    pathProgress = 0f;
                    invalidate();  // 초기 invalidate
                    startPathAnimation();
                }
                return;
            }
        }

        Toast.makeText(getContext(), "해당 상품을 찾을 수 없습니다", Toast.LENGTH_SHORT).show();
    }

    private void startPathAnimation() {
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(800);
        animator.addUpdateListener(animation -> {
            pathProgress = (float) animation.getAnimatedValue();
            invalidate();
        });
        animator.start();
    }

    private List<int[]> bfs(int[] start, int[] goal) {
        boolean[][] visited = new boolean[10][10];
        Map<String, String> parent = new HashMap<>();
        Queue<int[]> queue = new LinkedList<>();

        queue.offer(start);
        visited[start[0]][start[1]] = true;

        int[][] dirs = {{-1,0},{1,0},{0,-1},{0,1}};

        while (!queue.isEmpty()) {
            int[] cur = queue.poll();
            if (cur[0] == goal[0] && cur[1] == goal[1]) break;

            for (int[] d : dirs) {
                int nr = cur[0] + d[0];
                int nc = cur[1] + d[1];
                if (nr >= 0 && nr < 10 && nc >= 0 && nc < 10
                        && !visited[nr][nc] && GRID_MAP[nr][nc] == 0) {
                    visited[nr][nc] = true;
                    parent.put(nr + "," + nc, cur[0] + "," + cur[1]);
                    queue.offer(new int[]{nr, nc});
                }
            }
        }

        String key = goal[0] + "," + goal[1];
        if (!parent.containsKey(key)) return null;

        List<int[]> revPath = new ArrayList<>();
        while (!key.equals(start[0] + "," + start[1])) {
            int[] pos = parseKey(key);
            revPath.add(0, pos);
            key = parent.get(key);
        }
        revPath.add(0, start);
        return revPath;
    }

    private int[] parseKey(String key) {
        String[] parts = key.split(",");
        return new int[]{Integer.parseInt(parts[0]), Integer.parseInt(parts[1])};
    }

    public void setCurrentPosition(int row, int col) {
        currentPos = new int[]{row, col};
        invalidate();
    }
}
