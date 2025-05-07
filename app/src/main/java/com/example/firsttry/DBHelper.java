package com.example.firsttry;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "User.db";
    private static final int DATABASE_VERSION = 4;

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
                "phone_full TEXT," +
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
    public void insertUser(String name, String phoneTail, String phoneFull) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("phone_tail", phoneTail);
        values.put("phone_full", phoneFull);
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

    // 전화번호 뒷자리 중복 사용자 수 확인
    public int countUsersByPhoneTail(String phoneTail) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM users WHERE phone_tail = ?", new String[]{phoneTail});
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return count;
    }

    // 전체 전화번호 확인 (예: '01012345678')
    public boolean checkUserByFullPhone(String fullPhone) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM users WHERE phone_full = ?", new String[]{fullPhone});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public String getUserFullPhone(String phoneTail) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT phone_full FROM users WHERE phone_tail = ?", new String[]{phoneTail});
        String phoneFull = null;
        if (cursor.moveToFirst()) {
            phoneFull = cursor.getString(0);
        }
        cursor.close();
        return phoneFull;
    }

    public String getUserNameByFullPhone(String fullPhone) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT name FROM users WHERE phone_full = ?", new String[]{fullPhone});
        String name = null;
        if (cursor.moveToFirst()) {
            name = cursor.getString(0);
        }
        cursor.close();
        return name;
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
    public void addUserPointByFullPhone(String phoneFull, int pointToAdd) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE users SET point = point + ? WHERE phone_full = ?", new Object[]{pointToAdd, phoneFull});
        db.close();
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

    public int getUserPointByFullPhone(String phoneFull) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT point FROM users WHERE phone_full = ?", new String[]{phoneFull});
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

    public void insertPayment(String paymentId, String userPhoneFull, String item, int amount, String date, String method) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("payment_id", paymentId);
        values.put("user_id", userPhoneFull);
        values.put("item", item);
        values.put("amount", amount);
        values.put("date", date);
        values.put("method", method);
        db.insert("payments", null, values);
        db.close();
    }


    // 사용자 결제 내역 반환
    public Cursor getUserPayments(String userPhoneFull) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM payments WHERE user_id = ?", new String[]{userPhoneFull});
    }
}
