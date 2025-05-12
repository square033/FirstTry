package com.example.firsttry;

import java.util.*;

public class PositionEstimator {
    public static double x = -1;
    public static double y = -1;

    public static int gridRow = -1;
    public static int gridCol = -1;

    private static final double TILE_SIZE = 0.4; // 각 타일은 40cm = 0.4m
    private static KalmanFilter kf = new KalmanFilter();

    /**
     * Weighted Directional Trilateration 방식 적용: anchor + 방향 벡터 평균 보정
     */
    public static void updatePositionWithBeacons(Map<String, int[]> beaconMap, List<BeaconData> observedBeacons) {
        if (observedBeacons == null || observedBeacons.isEmpty()) return;

        // RSSI 기준 상위 3개 선택
        List<BeaconData> filtered = new ArrayList<>();
        for (BeaconData b : observedBeacons) {
            if (beaconMap.containsKey(b.mac) && b.distance > 0.001) {
                filtered.add(b);
            }
        }
        filtered.sort(Comparator.comparingDouble(b -> Math.abs(b.rssi)));
        if (filtered.size() > 3) {
            filtered = filtered.subList(0, 3);
        }

        // anchor는 가장 신호가 강한 비콘
        BeaconData anchorBeacon = filtered.get(0);
        double[] anchorPos = toDouble(beaconMap.get(anchorBeacon.mac));
        anchorPos[0] *= TILE_SIZE;
        anchorPos[1] *= TILE_SIZE;
        double anchorX = anchorPos[0];
        double anchorY = anchorPos[1];

        double sumDx = 0;
        double sumDy = 0;
        int count = 0;

        for (BeaconData b : filtered) {
            if (b.mac.equals(anchorBeacon.mac)) continue;  // anchor는 제외

            double[] pos = toDouble(beaconMap.get(b.mac));
            pos[0] *= TILE_SIZE;
            pos[1] *= TILE_SIZE;
            double dx = pos[0] - anchorX;
            double dy = pos[1] - anchorY;
            double len = Math.sqrt(dx * dx + dy * dy);

            if (len > 0.001 && b.distance > 0.001) {
                double weight = 1.0 / b.distance;
                sumDx += (dx / len) * weight;
                sumDy += (dy / len) * weight;
                count++;
            }
        }

        double avgDx = count > 0 ? sumDx / count : 0;
        double avgDy = count > 0 ? sumDy / count : 0;

        double correctedX = anchorX + avgDx;
        double correctedY = anchorY + avgDy;

        double[] filteredPos = kf.filter(correctedX, correctedY);
        x = filteredPos[0];
        y = filteredPos[1];

        android.util.Log.d("보정로그_디버그", "[보정위치 a] x: " + correctedX + ", y: " + correctedY);
        android.util.Log.d("보정로그_디버그", "[필터위치 b] x: " + x + ", y: " + y);
    }

    private static double[] toDouble(int[] arr) {
        return new double[]{arr[1], arr[0]}; // col(x), row(y)
    }

    public static void updateGridPosition(double xCoord, double yCoord, double cellSize) {
        x = xCoord;
        y = yCoord;
        gridRow = (int)(yCoord / cellSize);
        gridCol = (int)(xCoord / cellSize);
    }

    public static int[] toPixel(int cellWidth, int cellHeight) {
        int px = gridCol * cellWidth + cellWidth / 2;
        int py = gridRow * cellHeight + cellHeight / 2;
        return new int[]{px, py};
    }

    public static class BeaconData {
        public String mac;
        public double rssi;
        public double distance;

        public BeaconData(String mac, double rssi, double distance) {
            this.mac = mac;
            this.rssi = rssi;
            this.distance = distance;
        }
    }

    static class KalmanFilter {
        private double estimateX = 0;
        private double estimateY = 0;
        private double errorEstimate = 1;
        private final double q = 0.01;  // Process noise
        private final double r = 1;     // Measurement noise

        public double[] filter(double measuredX, double measuredY) {
            double k = errorEstimate / (errorEstimate + r);
            estimateX = estimateX + k * (measuredX - estimateX);
            estimateY = estimateY + k * (measuredY - estimateY);
            errorEstimate = (1 - k) * errorEstimate + q;

            return new double[]{estimateX, estimateY};
        }
    }
}