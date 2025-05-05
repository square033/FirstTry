package com.example.firsttry;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "User.db";
    private static final int DATABASE_VERSION = 2;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 사용자 테이블 생성
        String createUserTable = "CREATE TABLE users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT," +
                "phone_tail TEXT," +
                "point INTEGER DEFAULT 0)";
        db.execSQL(createUserTable);

        // 결제 테이블 생성
        String createPaymentsTable = "CREATE TABLE payments (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "payment_id TEXT," +
                "user_id TEXT," +
                "item TEXT," +
                "amount INTEGER," +
                "date TEXT," +
                "method TEXT)";

        db.execSQL(createPaymentsTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS users");
        db.execSQL("DROP TABLE IF EXISTS payments");
        onCreate(db);
    }

    // 사용자 등록
    public void insertUser(String name, String phoneTail) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("phone_tail", phoneTail);
        db.insert("users", null, values);
        db.close();
    }

    // 사용자 존재 여부 확인
    public boolean checkUserByPhoneTail(String phoneTail) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM users WHERE phone_tail = ?", new String[]{phoneTail});
        boolean exists = cursor.moveToFirst();
        cursor.close();
        db.close();
        return exists;
    }

    // 사용자 이름 불러오기
    public String getUserName(String phoneTail) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT name FROM users WHERE phone_tail = ?", new String[]{phoneTail});
        String name = null;
        if (cursor.moveToFirst()) {
            name = cursor.getString(0);
        }
        cursor.close();
        db.close();
        return name;
    }

    // 사용자 포인트 불러오기
    public int getUserPoint(String phoneTail) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT point FROM users WHERE phone_tail = ?", new String[]{phoneTail});
        int point = 0;
        if (cursor.moveToFirst()) {
            point = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return point;
    }
    public void addUserPoint(String phoneTail, int pointToAdd) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE users SET point = point + ? WHERE phone_tail = ?", new Object[]{pointToAdd, phoneTail});
        db.close();
    }




    public void insertPayment(String paymentId, String userPhoneTail, String item, int amount, String date, String method) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("payment_id", paymentId);
        values.put("user_id", userPhoneTail);
        values.put("item", item);
        values.put("amount", amount);
        values.put("date", date);
        values.put("method", method);
        db.insert("payments", null, values);
        db.close();
    }


    // 사용자 결제 내역 반환
    public Cursor getUserPayments(String userPhoneTail) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM payments WHERE user_id = ?", new String[]{userPhoneTail});
    }
}
