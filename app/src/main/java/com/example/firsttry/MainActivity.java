package com.example.firsttry;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import android.content.SharedPreferences;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Intent;

import com.minew.beaconset.BluetoothState;
import com.minew.beaconset.MinewBeacon;
import com.minew.beaconset.MinewBeaconConnection;
import com.minew.beaconset.MinewBeaconManager;
import com.minew.beaconset.MinewBeaconManagerListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private double[] currentPosition = new double[]{-1, -1};
    private double[] lastPosition = new double[]{-1, -1};

    private static final int PERMISSION_REQUEST_CODE = 100;

    private MinewBeaconManager beaconManager;
    private MinewBeaconConnection beaconConnection;

    static class BeaconInfo {
        String macAddress;
        double x, y;

        public BeaconInfo(String macAddress, double x, double y) {
            this.macAddress = macAddress;
            this.x = x;
            this.y = y;
        }
    }
    // 거리 필터용 맵 (MAC 주소 기준)
    private final HashMap<String, Double> filteredDistanceMap = new HashMap<>();

    // 로우패스 필터 적용 함수
    private final List<BeaconInfo> beaconMap = List.of(
            new BeaconInfo("C3:00:00:3F:C5:A1", 0.0, 0.0),
            new BeaconInfo("C3:00:00:3F:C5:A2", 3.2,0),
            new BeaconInfo("C3:00:00:3F:C5:A3", 6.4, 0),
            new BeaconInfo("C3:00:00:35:97:DA", 0, 3.2),
            new BeaconInfo("C3:00:00:35:97:D7", 6.4,3.2),
            new BeaconInfo("C3:00:00:3F:97:D9", 0, 6.4),
            new BeaconInfo("C3:00:00:35:97:F0", 3.2,6.4),
            new BeaconInfo("C3:00:00:3F:97:EF", 6.4, 6.4)
    );


    private double applyLowPassFilter(String macAddress, double rawDistance, double alpha) {
        double filtered = rawDistance;
        if (filteredDistanceMap.containsKey(macAddress)) {
            double previous = filteredDistanceMap.get(macAddress);
            filtered = previous + alpha * (rawDistance - previous);
        }
        filteredDistanceMap.put(macAddress, filtered);
        return filtered;
    }


    private BeaconInfo findBeaconInfoByMac(String mac) {
        for (BeaconInfo info : beaconMap) {
            if (info.macAddress.equalsIgnoreCase(mac)) {
                return info;
            }
        }
        return null;
    }
    private double[] calculateMultilaterationPosition(List<MinewBeacon> beacons) {
        if (beacons.size() < 3) return lastPosition;

        MinewBeacon refBeacon = beacons.get(0);
        BeaconInfo ref = findBeaconInfoByMac(refBeacon.getMacAddress());
        double d0 = applyLowPassFilter(refBeacon.getMacAddress(), refBeacon.getDistance(), 0.2);

        int N = beacons.size();
        double[][] A = new double[N - 1][2];
        double[] b = new double[N - 1];

        for (int i = 1; i < N; i++) {
            MinewBeacon bcn = beacons.get(i);
            BeaconInfo bi = findBeaconInfoByMac(bcn.getMacAddress());
            double di = applyLowPassFilter(bcn.getMacAddress(), bcn.getDistance(), 0.2);

            A[i - 1][0] = 2 * (bi.x - ref.x);
            A[i - 1][1] = 2 * (bi.y - ref.y);
            b[i - 1] = Math.pow(d0, 2) - Math.pow(di, 2)
                    - Math.pow(ref.x, 2) + Math.pow(bi.x, 2)
                    - Math.pow(ref.y, 2) + Math.pow(bi.y, 2);
        }

        double x = 0, y = 0;
        try {
            org.apache.commons.math3.linear.RealMatrix AMatrix = new org.apache.commons.math3.linear.Array2DRowRealMatrix(A);
            org.apache.commons.math3.linear.RealVector bVector = new org.apache.commons.math3.linear.ArrayRealVector(b);
            org.apache.commons.math3.linear.RealVector solution =
                    new org.apache.commons.math3.linear.QRDecomposition(AMatrix).getSolver().solve(bVector);

            x = solution.getEntry(0);
            y = solution.getEntry(1);
        } catch (Exception e) {
            Log.w("위치오류", "위치 계산 실패: " + e.getMessage());
            return lastPosition;
        }

        // 칼만 필터 제거하고 그대로 반환하고 싶으면 아래 주석처리
        // double filteredX = kalmanX.update(x);
        // double filteredY = kalmanY.update(y);
        // lastPosition[0] = filteredX;
        // lastPosition[1] = filteredY;

        lastPosition[0] = x;
        lastPosition[1] = y;
        return new double[]{x, y};
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // SharedPreferences는 한 번만 선언해서 아래 모두에서 재사용
        SharedPreferences prefs = getSharedPreferences("login_pref", MODE_PRIVATE);

        // Firebase → SQLite 회원 정보 동기화
        syncFirebaseMembersToSQLite();

        // 인텐트 → 쉐어드 순으로 이름 가져오기
        String userName = getIntent().getStringExtra("user_name");
        if (userName == null) {
            userName = prefs.getString("user_name", null);
        }

        boolean justLoggedIn = prefs.getBoolean("just_logged_in", false);

        if (userName != null && justLoggedIn) {
            Toast.makeText(this, userName + " 고객님 안녕하세요!", Toast.LENGTH_SHORT).show();
            prefs.edit().putBoolean("just_logged_in", false).apply();
        }

        // 버튼 리스너
        Button myPageBtn = findViewById(R.id.mypage_button);
        myPageBtn.setOnClickListener(v -> {
            String phoneTail = prefs.getString("phone_tail", null);

            Intent intent;
            if (phoneTail != null) {
                intent = new Intent(MainActivity.this, MyPageActivity.class);
            } else {
                intent = new Intent(MainActivity.this, LoginActivity.class);
            }
            startActivity(intent);
        });

        Button showMapButton = findViewById(R.id.show_map_button);
        showMapButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MapActivity.class);
            intent.putExtra("x", 2.0);  // 초기 좌표 (원하면 currentPosition[0] 등 넣어도 됨)
            intent.putExtra("y", 1.0);
            //실제좌표넘기기
            //intent.putExtra("x", currentPosition[0]);
            //intent.putExtra("y", currentPosition[1]);
            startActivity(intent);
        });

        Button paymentBtn = findViewById(R.id.payment_button);
        paymentBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, QRscanActivity.class);
            startActivity(intent);
        });

        checkAndRequestPermissions();  // 🔔 위치 권한 및 비콘 스캔 시작
    }

    private void checkAndRequestPermissions() {
        List<String> permissions = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.BLUETOOTH_SCAN);
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.BLUETOOTH_CONNECT);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.BLUETOOTH);
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.BLUETOOTH_ADMIN);
            }
        }
        if (!permissions.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissions.toArray(new String[0]), PERMISSION_REQUEST_CODE);
        } else {
            startBeaconScan();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean granted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    granted = false;
                    break;
                }
            }
            if (granted) startBeaconScan();
            else Toast.makeText(this, "권한이 거부되었습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private void startBeaconScan() {
        beaconManager = MinewBeaconManager.getInstance(this);
        beaconManager.startService();
        beaconManager.setRangeInterval(500);
        beaconManager.setMinewbeaconManagerListener(new MyMinewBeaconManagerListener());
        beaconManager.startScan();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (beaconManager != null) beaconManager.stopScan();
        if (beaconConnection != null) beaconConnection.disconnect();
    }

    private class MyMinewBeaconManagerListener implements MinewBeaconManagerListener {
        @Override public void onAppearBeacons(List<MinewBeacon> list) {}
        @Override public void onDisappearBeacons(List<MinewBeacon> list) {}

        @Override
        public void onRangeBeacons(List<MinewBeacon> list) {
            List<MinewBeacon> validBeacons = new ArrayList<>();
            for (MinewBeacon beacon : list) {
                for (BeaconInfo info : beaconMap) {
                    if (beacon.getMacAddress().equalsIgnoreCase(info.macAddress)) {
                        validBeacons.add(beacon);
                    }
                }
            }

            if (validBeacons.size() >= 3) {
                // 거리 기준 정렬
                validBeacons.sort((b1, b2) -> Double.compare(b1.getDistance(), b2.getDistance()));

                // 가까운 최대 6~8개 비콘만 사용
                List<MinewBeacon> used = validBeacons.subList(0, Math.min(validBeacons.size(), 8));

                currentPosition = calculateMultilaterationPosition(used);

                Log.d("내위치", "다중 위치: (" + currentPosition[0] + ", " + currentPosition[1] + ")");
            }
        }
        public void onUpdateBluetoothState(BluetoothState state) {
            Log.d("블루투스 상태", "현재 상태: " + state.toString());
        }
        }

    private void syncFirebaseMembersToSQLite() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("members");
        DBHelper dbHelper = new DBHelper(this);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot child : snapshot.getChildren()) {
                    String id = child.getKey();
                    String name = child.child("name").getValue(String.class);
                    String phone = child.child("phone").getValue(String.class);

                    if (phone != null && phone.length() >= 4) {
                        String tail = phone.substring(phone.length() - 4); // 뒷자리 추출

                        if (!dbHelper.checkUserByFullPhone(phone)) {
                            dbHelper.insertUser(name, tail, phone);  // ✅ 전체 번호도 같이 저장
                            Log.d("동기화", "Firebase → SQLite 등록됨: " + name + ", " + tail + ", " + phone);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "회원 정보 불러오기 실패: " + error.getMessage());
            }
        });
    }
}

