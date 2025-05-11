package com.example.firsttry;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class MapCanvasView extends View {

    private int[][] tileMap;
    private int cellSize;
    private Paint pathPaint;
    private List<int[]> path = new ArrayList<>();
    private int[] currentPos = {74, 74};
    private Map<String, int[]> productMap = new HashMap<>();
    private Bitmap markerBitmap, flagBitmap;
    private float pathProgress = 0f;

    private Map<Integer, Bitmap> tileBitmaps = new HashMap<>();

    public MapCanvasView(Context context, AttributeSet attrs) {
        super(context, attrs);

        pathPaint = new Paint();
        pathPaint.setColor(0xFF2196F3);
        pathPaint.setStrokeWidth(10);
        pathPaint.setStyle(Paint.Style.STROKE);
        pathPaint.setStrokeCap(Paint.Cap.ROUND);

        markerBitmap = scaleBitmap(
                BitmapFactory.decodeResource(getResources(), R.drawable.ic_cart), 16, context);
        flagBitmap = scaleBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_flag), 16, context);

        tileMap = loadTileMapFromJson(context, "floor_4_B.tmj");
        loadTileBitmaps(context);

        productMap.put("우유", new int[]{16, 61});
        productMap.put("사과", new int[]{22, 85});
        productMap.put("식빵", new int[]{117, 4});
        productMap.put("샴푸", new int[]{130, 17});
    }

    private void putTile(int gid, int resId, int dp, Context context) {
        tileBitmaps.put(gid, scaleBitmap(BitmapFactory.decodeResource(getResources(), resId), dp, context));
    }

    private void loadTileBitmaps(Context context) {
        // 기본 타일 크기 기준 (32dp)
        putTile(1, R.drawable.wall_black, 32, context);
        putTile(2, R.drawable.wall_white, 32, context);
        putTile(3, R.drawable.wall_orange, 32, context);
        putTile(4, R.drawable.wall_skyblue, 32, context);
        putTile(5, R.drawable.wall_stairs, 32, context);
        putTile(6, R.drawable.wall_grey, 32, context);
        putTile(7, R.drawable.wall_brown, 32, context);
        putTile(8, R.drawable.wall_pink, 32, context);
        putTile(9, R.drawable.wall_blue, 32, context);
        putTile(10, R.drawable.wall_table, 32, context);
        putTile(11, R.drawable.wall_chair, 32, context);
        putTile(12, R.drawable.wall_chair_left, 32, context);
        putTile(13, R.drawable.wall_chair_right, 32, context);
        putTile(14, R.drawable.wall_chair_down, 32, context);
        putTile(15, R.drawable.wall_table_right, 32, context);
        putTile(16, R.drawable.wall_table_down, 32, context);
        putTile(17, R.drawable.wall_lightgreen, 32, context);
        putTile(18, R.drawable.wall_door_32x64, 32, context);

        // 크기 큰 타일들은 스케일 다운해서 넣기
        putTile(20, R.drawable.toilet_female_96x96, 32, context);
        putTile(29, R.drawable.toilet_male_96x96, 32, context);
        putTile(38, R.drawable.elevator_logo_128x96, 32, context);
        putTile(50, R.drawable.elevator_door_rotated_32x64, 32, context);
        putTile(52, R.drawable.elevator_door_64x32, 32, context);

        // 바닥 타일들
        putTile(54, R.drawable.school_floor_tile_texture, 32, context);
        putTile(55, R.drawable.school_floor_tile_texture_brighter, 32, context);
        putTile(56, R.drawable.school_floor_tile_texture_gray, 32, context);
    }

    private Bitmap scaleBitmap(Bitmap bitmap, int dp, Context context) {
        float scale = context.getResources().getDisplayMetrics().density;
        int px = (int) (dp * scale + 0.5f);
        return Bitmap.createScaledBitmap(bitmap, px, px, true);
    }

    private int[][] loadTileMapFromJson(Context context, String filename) {
        try {
            InputStream is = context.getAssets().open(filename);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) builder.append(line);
            reader.close();

            JSONObject json = new JSONObject(builder.toString());
            JSONArray layers = json.getJSONArray("layers");
            JSONObject layer = layers.getJSONObject(0);
            JSONArray data = layer.getJSONArray("data");

            int width = json.getInt("width");
            int height = json.getInt("height");
            int[][] map = new int[height][width];

            for (int i = 0; i < data.length(); i++) {
                int gid = data.getInt(i);
                int row = i / width;
                int col = i % width;
                map[row][col] = gid;
            }

            Log.d("TileMap", "Loaded map size: " + height + " x " + width);
            Log.d("TileMap", "Sample GIDs: " + map[0][0] + ", " + map[0][1] + ", " + map[0][2]);
            return map;

        } catch (Exception e) {
            Log.e("MapLoader", "맵 로드 실패: " + e.getMessage());
            return new int[1][1];
        }
    }

// ... (생략된 상단 코드)

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int rowCount = tileMap.length;
        int colCount = tileMap[0].length;
        cellSize = Math.min(getWidth() / colCount, getHeight() / rowCount);

        int mapWidth = colCount * cellSize;
        int mapHeight = rowCount * cellSize;
        int offsetX = (getWidth() - mapWidth) / 2;
        int offsetY = (getHeight() - mapHeight) / 2;

        for (int row = 0; row < rowCount; row++) {
            for (int col = 0; col < colCount; col++) {
                int gid = tileMap[row][col];
                Rect dest = new Rect(
                        offsetX + col * cellSize,
                        offsetY + row * cellSize,
                        offsetX + (col + 1) * cellSize,
                        offsetY + (row + 1) * cellSize
                );

                Bitmap tileBitmap = tileBitmaps.get(gid);
                if (tileBitmap != null) {
                    canvas.drawBitmap(tileBitmap, null, dest, null);
                } else {
                    Paint emptyPaint = new Paint();
                    emptyPaint.setColor(Color.LTGRAY);
                    canvas.drawRect(dest, emptyPaint);
                }
            }
        }

        int maxSteps = Math.round(path.size() * pathProgress);
        for (int i = 0; i < maxSteps - 1; i++) {
            int[] from = path.get(i);
            int[] to = path.get(i + 1);
            float startX = offsetX + from[1] * cellSize + cellSize / 2f;
            float startY = offsetY + from[0] * cellSize + cellSize / 2f;
            float stopX = offsetX + to[1] * cellSize + cellSize / 2f;
            float stopY = offsetY + to[0] * cellSize + cellSize / 2f;
            canvas.drawLine(startX, startY, stopX, stopY, pathPaint);
        }

        if (!path.isEmpty()) {
            int[] goal = path.get(path.size() - 1);
            float gx = offsetX + goal[1] * cellSize + cellSize / 2f - flagBitmap.getWidth() / 2f;
            float gy = offsetY + goal[0] * cellSize + cellSize / 2f - flagBitmap.getHeight() / 2f;
            canvas.drawBitmap(flagBitmap, gx, gy, null);
        }

        float dotX = offsetX + currentPos[1] * cellSize + cellSize / 2f - markerBitmap.getWidth() / 2f;
        float dotY = offsetY + currentPos[0] * cellSize + cellSize / 2f - markerBitmap.getHeight() / 2f;
        canvas.drawBitmap(markerBitmap, dotX, dotY, null);
    }


    public void navigateToProduct(String name) {
        String keyword = name.trim().toLowerCase();
        Log.d("길찾기", "검색된 상품: " + keyword);

        for (String key : productMap.keySet()) {
            if (key.toLowerCase().equals(keyword)) {
                int[] goal = productMap.get(key);
                Log.d("길찾기", "Start GID: " + tileMap[currentPos[0]][currentPos[1]] + ", Goal GID: " + tileMap[goal[0]][goal[1]]);
                List<int[]> newPath = bfs(currentPos, goal);
                if (newPath != null) {
                    path = newPath;
                    pathProgress = 0f;
                    invalidate();
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
        int rowCount = tileMap.length;
        int colCount = tileMap[0].length;

        boolean[][] visited = new boolean[rowCount][colCount];
        Map<String, String> parent = new HashMap<>();
        Queue<int[]> queue = new LinkedList<>();

        queue.offer(start);
        visited[start[0]][start[1]] = true;

        int[][] dirs = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

        while (!queue.isEmpty()) {
            int[] cur = queue.poll();
            if (cur[0] == goal[0] && cur[1] == goal[1]) break;

            for (int[] d : dirs) {
                int nr = cur[0] + d[0];
                int nc = cur[1] + d[1];
                if (nr >= 0 && nr < rowCount && nc >= 0 && nc < colCount
                        && !visited[nr][nc] && isPassable(tileMap[nr][nc])) {
                    visited[nr][nc] = true;
                    parent.put(nr + "," + nc, cur[0] + "," + cur[1]);
                    queue.offer(new int[]{nr, nc});
                }
            }
        }

        String key = goal[0] + "," + goal[1];
        if (!parent.containsKey(key)) {
            Log.d("길찾기", "도착지까지 경로를 찾을 수 없습니다.");
            return null;
        }

        List<int[]> revPath = new ArrayList<>();
        while (!key.equals(start[0] + "," + start[1])) {
            int[] pos = parseKey(key);
            revPath.add(0, pos);
            key = parent.get(key);
        }
        revPath.add(0, start);
        return revPath;
    }

    private boolean isPassable(int gid) {
        return !(gid == 1 || gid == 17 || (gid >= 11 && gid <= 14));
    }

    private int[] parseKey(String key) {
        try {
            String[] parts = key.split(",");
            return new int[]{Integer.parseInt(parts[0]), Integer.parseInt(parts[1])};
        } catch (Exception e) {
            Log.e("MapCanvasView", "좌표 변환 실패: " + key);
            return new int[]{0, 0};
        }
    }

    public void setCurrentPosition(int row, int col) {
        boolean inBounds = row >= 0 && row < tileMap.length && col >= 0 && col < tileMap[0].length;
        if (inBounds) {
            currentPos = new int[]{row, col};
            invalidate();
        }
    }
}
