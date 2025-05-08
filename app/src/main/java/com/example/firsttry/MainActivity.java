package com.example.firsttry;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import android.content.SharedPreferences;
import android.content.Intent;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.*;
import com.minew.beaconset.*;

import java.util.*;

public class MainActivity extends AppCompatActivity {

    public static int[] currentPosition = new int[]{-1, -1};
    private static final int PERMISSION_REQUEST_CODE = 100;
    private MinewBeaconManager beaconManager;

    private final Map<String, int[]> beaconMap = new HashMap<>() {{
        put("C3:00:00:3F:97:D9", new int[]{7, 4});    // 비콘 6
        put("C3:00:00:35:97:F0", new int[]{7, 19});  // 비콘 7
        put("C3:00:00:3F:97:EF", new int[]{15, 12});   // 비콘 8
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        SharedPreferences prefs = getSharedPreferences("login_pref", MODE_PRIVATE);

        // 회원 동기화
        syncFirebaseMembersToSQLite();

        // 사용자 이름 환영 메시지
        String userName = getIntent().getStringExtra("user_name");
        if (userName == null) {
            userName = prefs.getString("user_name", null);
        }

        if (userName != null && prefs.getBoolean("just_logged_in", false)) {
            Toast.makeText(this, userName + " 고객님 안녕하세요!", Toast.LENGTH_SHORT).show();
            prefs.edit().putBoolean("just_logged_in", false).apply();
        }

        // 버튼 연결
        findViewById(R.id.mypage_button).setOnClickListener(v -> {
            String phoneTail = prefs.getString("phone_tail", null);
            Intent intent = new Intent(MainActivity.this,
                    phoneTail != null ? MyPageActivity.class : LoginActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.show_map_button).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MapActivity.class);
            intent.putExtra("row", currentPosition[0]);  // row (y좌표)
            intent.putExtra("col", currentPosition[1]);  // col (x좌표)
            startActivity(intent);

        });

        findViewById(R.id.payment_button).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, QRscanActivity.class));
        });

        checkAndRequestPermissions();
    }

    private void checkAndRequestPermissions() {
        List<String> permissions = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
                    != PackageManager.PERMISSION_GRANTED)
                permissions.add(Manifest.permission.BLUETOOTH_SCAN);
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                    != PackageManager.PERMISSION_GRANTED)
                permissions.add(Manifest.permission.BLUETOOTH_CONNECT);
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH)
                    != PackageManager.PERMISSION_GRANTED)
                permissions.add(Manifest.permission.BLUETOOTH);
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN)
                    != PackageManager.PERMISSION_GRANTED)
                permissions.add(Manifest.permission.BLUETOOTH_ADMIN);
        }

        if (!permissions.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    permissions.toArray(new String[0]), PERMISSION_REQUEST_CODE);
        } else {
            startBeaconScan();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean granted = Arrays.stream(grantResults)
                    .allMatch(result -> result == PackageManager.PERMISSION_GRANTED);
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
    }

    private class MyMinewBeaconManagerListener implements MinewBeaconManagerListener {
        @Override
        public void onAppearBeacons(List<MinewBeacon> list) {
        }

        @Override
        public void onDisappearBeacons(List<MinewBeacon> list) {
        }

        @Override
        public void onUpdateBluetoothState(BluetoothState bluetoothState) {
            Log.d("블루투스 상태", "현재 상태: " + bluetoothState.toString());
        }

        @Override
        public void onRangeBeacons(List<MinewBeacon> list) {
            if (list == null || list.isEmpty()) return;

            MinewBeacon strongest = null;
            String strongestMac = null;
            double minAbsRssi = Double.MAX_VALUE;

            Log.d("비콘디버그", "==== 비콘 전체 RSSI 목록 ====");
            for (MinewBeacon beacon : list) {
                String mac = beacon.getMacAddress();
                double rssi = beacon.getRssi();
                double distance = beacon.getDistance();

                Log.d("비콘디버그", "MAC: " + mac + " | RSSI: " + rssi + " | 거리: " + String.format("%.2f", distance));

                for (String key : beaconMap.keySet()) {
                    if (key.equalsIgnoreCase(mac)) {
                        double absRssi = Math.abs(rssi);  // 절댓값으로 비교
                        if (absRssi < minAbsRssi) {
                            minAbsRssi = absRssi;
                            strongest = beacon;
                            strongestMac = key;
                        }
                    }
                }
            }

            if (strongest != null && strongestMac != null) {
                int[] pos = beaconMap.get(strongestMac);
                currentPosition = pos;

                Log.d("가장강한비콘", "선택된 MAC: " + strongestMac + " | RSSI: " + strongest.getRssi() + "dBm");
                Log.d("디버그위치", "x: " + currentPosition[0] + ", y: " + currentPosition[1]);
            }
        }
    }

    private void syncFirebaseMembersToSQLite() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("members");
        DBHelper dbHelper = new DBHelper(this);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot child : snapshot.getChildren()) {
                    String name = child.child("name").getValue(String.class);
                    String phone = child.child("phone").getValue(String.class);
                    if (phone != null && phone.length() >= 4) {
                        String tail = phone.substring(phone.length() - 4);
                        if (!dbHelper.checkUserByFullPhone(phone)) {
                            dbHelper.insertUser(name, tail, phone);
                            Log.d("동기화", "등록됨: " + name + ", " + phone);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "회원 정보 실패: " + error.getMessage());
            }
        });
    }
}