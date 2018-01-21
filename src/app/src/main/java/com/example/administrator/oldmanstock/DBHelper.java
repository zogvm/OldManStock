package com.example.administrator.oldmanstock;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DBHelper extends SQLiteOpenHelper
{
    private static final String DATABASE_NAME = "zogStock.db";
    private static final int DATABASE_VERSION = 1;

    public DBHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        // TODO Auto-generated method stub
        db.execSQL("CREATE TABLE IF NOT EXISTS tb_StockCode"
                + "(_id INTEGER PRIMARY KEY AUTOINCREMENT, code nvarchar(16) UNIQUE)");

        //--------------
        db.execSQL("CREATE TABLE IF NOT EXISTS tb_StockLast"
                + "(_id INTEGER PRIMARY KEY AUTOINCREMENT, time_sec INTEGER,code nvarchar(16) ,name nvarchar(16)," +
                "todayOpen double ,yesterdayClose double ,cur double ,top double ,bottom double ,deal INTEGER, dealGold double," +
                "buy1price double,buy1deal INTEGER,buy2price double,buy2deal INTEGER,buy3price double,buy3deal INTEGER,buy4price double,buy4deal INTEGER,buy5price double,buy5deal INTEGER,"  +
                "sell1price double,sell1deal INTEGER,sell2price double,sell2deal INTEGER,sell3price double,sell3deal INTEGER,sell4price double,sell4deal INTEGER,sell5price double,sell5deal INTEGER" +
                ")");
        //--------------
        db.execSQL("CREATE TABLE IF NOT EXISTS tb_StockDetail"
                + "(_id INTEGER PRIMARY KEY AUTOINCREMENT, time_sec INTEGER,code nvarchar(16),name nvarchar(16)," +
                "todayOpen double ,yesterdayClose double ,cur double ,top double ,bottom double ,deal INTEGER, dealGold double," +
                "buy1price double,buy1deal INTEGER,buy2price double,buy2deal INTEGER,buy3price double,buy3deal INTEGER,buy4price double,buy4deal INTEGER,buy5price double,buy5deal INTEGER,"  +
                "sell1price double,sell1deal INTEGER,sell2price double,sell2deal INTEGER,sell3price double,sell3deal INTEGER,sell4price double,sell4deal INTEGER,sell5price double,sell5deal INTEGER" +
                ")");

        db.execSQL("CREATE index IF NOT EXISTS tb_StockDetail_time_index "
                + " on tb_StockDetail(code,time_sec)");

        //--------------
        db.execSQL("CREATE TABLE IF NOT EXISTS tb_News"
                + "(_id INTEGER PRIMARY KEY, content_text nvarchar(520),time_sec INTEGER,isPlay INTEGER DEFAULT 0)");

        db.execSQL("CREATE index IF NOT EXISTS tb_News_index "
                + " on tb_News(isPlay)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        // TODO Auto-generated method stub
      //  db.execSQL("ALTER TABLE thing ADD COLUMN other STRING");
    }

}