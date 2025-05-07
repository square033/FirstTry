package com.example.firsttry;

public class GlobalLocation {
    public static double x = -1;
    public static double y = -1;

    public static int row = -1;
    public static int col = -1;

    public static int[] toPixel(int cellWidth, int cellHeight) {
        int px = col * cellWidth + cellWidth / 2;
        int py = row * cellHeight + cellHeight / 2;
        return new int[]{px, py};
    }

    public static void updateGridPosition(double meterX, double meterY, double cellMeterSize) {
        col = (int)(meterX / cellMeterSize);
        row = (int)(meterY / cellMeterSize);
    }
}
