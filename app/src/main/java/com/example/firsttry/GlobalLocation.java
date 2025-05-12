package com.example.firsttry;

public class GlobalLocation {
    public static double x = -1;
    public static double y = -1;

    public static int gridRow = -1;
    public static int gridCol = -1;

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
}