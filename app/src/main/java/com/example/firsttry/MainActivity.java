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
import java.util.Comparator;

import android.content.Intent;

import com.minew.beaconset.BluetoothState;
import com.minew.beaconset.MinewBeacon;
import com.minew.beaconset.MinewBeaconConnection;
import com.minew.beaconset.MinewBeaconManager;
import com.minew.beaconset.MinewBeaconManagerListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

class KalmanFilter {
    private double q = 0.0001;
    private double r = 0.1;
    private double x = 0;
    private double p = 1;
    private double k;

    public double update(double measurement) {
        p = p + q;
        k = p / (p + r);
        x = x + k * (measurement - x);
        p = (1 - k) * p;
        return x;
    }

    public void reset(double value) {
        this.x = value;
        this.p = 1;
    }
}

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 100;
    private MinewBeaconManager beaconManager;
    private String lastDetectedZone = null;
    private final LinkedList<String> recentZones = new LinkedList<>();
    private final int MIN_CONFIDENCE_COUNT = 5;
    private String stableZone = null;  // 확정된 위치
    private final Map<String, KalmanFilter> rssiFilters = new HashMap<>();
    private double getFilteredRssi(MinewBeacon beacon) {
        String mac = beacon.getMacAddress();
        KalmanFilter filter = rssiFilters.get(mac);

        if (filter == null) {
            filter = new KalmanFilter();
            filter.reset(beacon.getRssi());
            rssiFilters.put(mac, filter);
        }

        return filter.update(beacon.getRssi());
    }





    private final HashMap<String, String> beaconToZoneMap = new HashMap<>() {{
        put("C3:00:00:3F:C5:A1", "A");
        put("C3:00:00:3F:C5:A2", "B");
        put("C3:00:00:3F:C5:A3", "C");
        put("C3:00:00:35:97:DA", "D");
        put("C3:00:00:35:97:D7", "E");
        put("C3:00:00:35:97:D9", "F");
        put("C3:00:00:35:97:F0", "G");
        put("C3:00:00:35:97:EF", "H");
        put("C3:00:00:3F:CB:0A", "I");
        put("C3:00:00:3F:CB:0B", "J");
        put("C3:00:00:3F:CB:0D", "K");
        put("C3:00:00:35:97:1A", "L");
        put("C3:00:00:35:97:19", "M");

    }};
    private final HashMap<String, int[]> zoneGridMap = new HashMap<>() {{
        put("E", new int[]{1, 74});  // (y, x)
        put("F", new int[]{15, 74});
        put("G", new int[]{31, 74});
        put("H", new int[]{45, 74});
        put("I", new int[]{61, 74});
        put("J", new int[]{75, 74});
        put("K", new int[]{91, 74});
        put("L", new int[]{105, 74});
        put("M", new int[]{118, 74}); //여기까지 강의실쪽
        put("A", new int[]{120, 60});
        put("B", new int[]{122, 45});
        put("C", new int[]{128, 30});
        put("D", new int[]{128, 15});

//        put("E-F", new int[]{7, 74});
//        put("F-G", new int[]{23, 74});
//        put("G-H", new int[]{38, 74});
//        put("H-I", new int[]{53, 74});
//        put("I-J", new int[]{68, 74});
//        put("J-K", new int[]{83, 74});
//        put("K-L", new int[]{98, 74});
//        put("L-M", new int[]{113, 74});
//        put("M-A", new int[]{121, 78});
//        put("A-B", new int[]{121, 52});
//        put("B-C", new int[]{125, 37});
//        put("C-D", new int[]{128, 23});
    }};


    private class MyMinewBeaconManagerListener implements MinewBeaconManagerListener {
        @Override
        public void onUpdateBluetoothState(BluetoothState bluetoothState) {}
        @Override
        public void onAppearBeacons(List<MinewBeacon> list) {}
        @Override
        public void onDisappearBeacons(List<MinewBeacon> list) {}
        @Override
        public void onRangeBeacons(List<MinewBeacon> beacons) {
            if (beacons == null || beacons.isEmpty()) return;

            List<MinewBeacon> validBeacons = new ArrayList<>();
            for (MinewBeacon beacon : beacons) {
                if (beaconToZoneMap.containsKey(beacon.getMacAddress())) {
                    validBeacons.add(beacon);
                }
            }

            if (!validBeacons.isEmpty()) {
                Collections.sort(validBeacons, new Comparator<MinewBeacon>() {
                    @Override
                    public int compare(MinewBeacon b1, MinewBeacon b2) {
                        double rssi1 = getFilteredRssi(b1);
                        double rssi2 = getFilteredRssi(b2);
                        return Double.compare(rssi2, rssi1); // 높은 RSSI가 우선
                    }
                });


                String zone = beaconToZoneMap.get(validBeacons.get(0).getMacAddress());

                recentZones.add(zone);
                if (recentZones.size() > 10) {
                    recentZones.removeFirst();
                }

                // 최근 8개 분석
                if (recentZones.size() >= 7) {
                    HashMap<String, Integer> zoneCount = new HashMap<>();
                    for (int i = recentZones.size() - 7; i < recentZones.size(); i++) {
                        String z = recentZones.get(i);
                        int currentCount = zoneCount.containsKey(z) ? zoneCount.get(z) : 0;
                        zoneCount.put(z, currentCount + 1);
                    }


                    // 1. 단일 zone이 8회 → 해당 zone 확정
                    for (String zoneKey : zoneCount.keySet()) {
                        if (zoneCount.get(zoneKey) >= 7&& !zoneKey.equals(stableZone)) {
                            stableZone = zoneKey;
                            lastDetectedZone = zoneKey;
                            int[] coord = zoneGridMap.get(zoneKey);
                            updateLocation(coord);
                            Log.d("위치확정", "확정된 zone (단일): " + zoneKey);
                            return;
                        }
                    }

//                    // 2. 두 개 zone이 3회 이상 등장 → 중간 zone 고려
//                    if (zoneCount.size() == 2) {
//                        List<String> topZones = new ArrayList<>(zoneCount.keySet());
//                        String z1 = topZones.get(0);
//                        String z2 = topZones.get(1);
//                        int c1 = zoneCount.get(z1);
//                        int c2 = zoneCount.get(z2);
//
//                        if (c1 >= 3 && c2 >= 3) {
//                            List<String> sorted = new ArrayList<>();
//                            sorted.add(z1);
//                            sorted.add(z2);
//                            Collections.sort(sorted);
//                            String midZoneKey = sorted.get(0) + "-" + sorted.get(1);  // A-B 형식
//
//                            if (zoneGridMap.containsKey(midZoneKey) && !midZoneKey.equals(stableZone)) {
//                                stableZone = midZoneKey;
//                                lastDetectedZone = midZoneKey;
//                                int[] coord = zoneGridMap.get(midZoneKey);
//                                updateLocation(coord);
//                                Log.d("중간위치", "중간 zone: " + midZoneKey);
//                                return;
//                            }
//                        }
//                    }
                }
            }

        }
        private void updateLocation(int[] coord) {
            SharedPreferences prefs = getSharedPreferences("location_pref", MODE_PRIVATE);
            prefs.edit().putInt("current_x", coord[1]).putInt("current_y", coord[0]).apply();
        }

    }


    private int[] currentPosition;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        checkAndRequestPermissions();

        // SharedPreferences는 한 번만 선언해서 아래 모두에서 재사용
        SharedPreferences prefs = getSharedPreferences("login_pref", MODE_PRIVATE);
        int currentX = prefs.getInt("current_x", -1);
        int currentY = prefs.getInt("current_y", -1);
        currentPosition = new int[]{currentX, currentY};

      //  Log.d("MapActivity", "현재 위치: (" + currentPosition[0] + ", " + currentPosition[1] + ")");

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
            if (lastDetectedZone != null) {
                int[] coord = zoneGridMap.get(lastDetectedZone);
                if (coord != null) {
                    Intent intent = new Intent(MainActivity.this, MapActivity.class);
                    intent.putExtra("current_x", coord[1]);  // x
                    intent.putExtra("current_y", coord[0]);  // y
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "위치 좌표를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "위치를 아직 인식하지 못했어요!", Toast.LENGTH_SHORT).show();
            }
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
        beaconManager.setRangeInterval(300);
        beaconManager.setMinewbeaconManagerListener(new MyMinewBeaconManagerListener());
        beaconManager.startScan();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (beaconManager != null) beaconManager.stopScan();
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