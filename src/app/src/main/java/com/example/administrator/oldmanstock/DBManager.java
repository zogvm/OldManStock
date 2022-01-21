package com.example.administrator.oldmanstock;

/**
 * Created by Administrator on 2017/12/28.
 */

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

//http://blog.csdn.net/codeeer/article/details/30237597
public class DBManager
{
    private DBHelper helper;
    private SQLiteDatabase db;
    ArrayList<StockCode> gCodeList= new ArrayList<StockCode>();
    ArrayList<StockData> gDataList= new ArrayList<StockData>();

    public DBManager(Context context)
    {
        helper = new DBHelper(context);
        db = helper.getWritableDatabase();
    }

    //------------------------------code ------------------------------------

    public void addStockCode(String code)
    {
        ContentValues cv = new ContentValues();
        //往ContentValues对象存放数据，键-值对模式
        cv.put("code", code );
        db.insertWithOnConflict("tb_StockCode", null, cv,SQLiteDatabase.CONFLICT_IGNORE);
    }

    public void addStockCodeList(List<String> codeList)
    {
       db.beginTransaction();  //手动设置开始事务
        try{
            //批量处理操作
            for(String code:codeList){
                addStockCode(code);
            }
            db.setTransactionSuccessful(); //设置事务处理成功，不设置会自动回滚不提交
        }catch(Exception e){

        }finally{
            db.endTransaction(); //处理完成
        }
    }

    public void addStockCodeList_home()
    {
        ArrayList<String> codeList=new  ArrayList<String>();
        codeList.add("000001");
        codeList.add("399001");
        codeList.add("600537");
        codeList.add("000531");

        codeList.add("000413");
        codeList.add("601222");
        codeList.add("000537");
        codeList.add("600026");
        codeList.add("600030");
        codeList.add("601607");

        codeList.add("002230");
        codeList.add("000776");
        codeList.add("600098");

        addStockCodeList(codeList);
    }

    public void deleteStockCode(StockCode code)
    {
        db.delete("tb_StockCode", "code= ? ", new String[] {code._code });
    }

    public List<StockCode> queryStockCode()
    {
        if(gCodeList.isEmpty())
        {
            Cursor c = db.rawQuery("SELECT * FROM tb_StockCode", null);
            if (c.moveToLast()) {
                while (!c.isBeforeFirst()) {
                    StockCode code = new StockCode();
                    code._code = c.getString(c.getColumnIndex("code"));
                    gCodeList.add(code);
                    c.moveToPrevious();
                }
            }
            c.close();
        }

        return gCodeList;
    }

    //------------------------------data ------------------------------------
    public void addStockData(StockData data)
    {
        ContentValues cv = new ContentValues();
        //往ContentValues对象存放数据，键-值对模式
        cv.put("time_sec", data._time_sec );
        cv.put("code", data._code );
        cv.put("name", data._name );
        cv.put("todayOpen", data._todayOpen );
        cv.put("yesterdayClose", data._yesterdayClose );

        cv.put("cur", data._cur );
        cv.put("top", data._top );
        cv.put("bottom", data._bottom );

        cv.put("deal", data._deal );
        cv.put("dealGold", data._dealGold );

        cv.put("buy1price", data._buy[0]._price  );
        cv.put("buy1deal", data._buy[0]._deal );

        cv.put("buy2price", data._buy[1]._price  );
        cv.put("buy2deal", data._buy[1]._deal );

        cv.put("buy3price", data._buy[2]._price  );
        cv.put("buy3deal", data._buy[2]._deal );

        cv.put("buy4price", data._buy[3]._price  );
        cv.put("buy4deal", data._buy[3]._deal );

        cv.put("buy5price", data._buy[4]._price  );
        cv.put("buy5deal", data._buy[4]._deal );

        cv.put("sell1price", data._sell[0]._price  );
        cv.put("sell1deal", data._sell[0]._deal );

        cv.put("sell2price", data._sell[1]._price  );
        cv.put("sell2deal", data._sell[1]._deal );

        cv.put("sell3price", data._sell[2]._price  );
        cv.put("sell3deal", data._sell[2]._deal );

        cv.put("sell4price", data._sell[3]._price  );
        cv.put("sell4deal", data._sell[3]._deal );

        cv.put("sell5price", data._sell[4]._price  );
        cv.put("sell5deal", data._sell[4]._deal );

       // db.insertWithOnConflict("tb_StockDetail", null, cv,SQLiteDatabase.CONFLICT_IGNORE); //for fast
        db.replace("tb_StockLast",null,cv);

    }

    public void addStockDataList(List<StockData> dataList)
    {
        if(dataList.size()>=gDataList.size())
        {
            gDataList.clear();
            for(StockData data:dataList){
                gDataList.add(data);
            }
        }

        return ;
//
//
//        db.beginTransaction();  //手动设置开始事务
//        try{
//            //批量处理操作
//            for(StockData data:dataList){
//                addStockData(data);
//            }
//            db.setTransactionSuccessful(); //设置事务处理成功，不设置会自动回滚不提交
//        }catch(Exception e){
//
//        }finally{
//            db.endTransaction(); //处理完成
//        }
    }

    public StockData queryStockLast(String code) {
        StockData data = new StockData();
        data._code = code;
        //SELECT * FROM tb_StockDetail where code= ? ORDER BY time_sec DESC  limit 1
        Cursor c = db.query("tb_StockLast", new String[]{"*"}, "code=?", new String[]{code},
                null, null, "time_sec DESC", "1");

        if (c.moveToLast())
        {
            while (!c.isBeforeFirst())
            {
                data = getStockData(c);
                break;
            }
        }
        c.close();
        return data;
    }

    public  ArrayList<StockData> queryAllStockLast(List<StockCode> codeList)
    {
        return gDataList;
//
//        ArrayList<StockData> dataList = new ArrayList<StockData>();
//
//        for( StockCode code:codeList)
//        {
//            StockData data = queryStockLast(code._code);
//            dataList.add(data);
//        }
//        return dataList;
    }

    public  ArrayList<StockData> queryTodayStockData(String code)
    {
        long today_time=getTodayTimeSec();

        ArrayList<StockData> dataList = new ArrayList<StockData>();
        Cursor c = db.rawQuery("SELECT * FROM tb_StockDetail where code=? and time_sec >?  limit 1",
                new String[] { code,   Long.toString(today_time) });
        if (c.moveToLast()) {
            while (!c.isBeforeFirst()) {
                StockData data = getStockData(c);
                dataList.add(data);
                c.moveToPrevious();
            }
        }
        c.close();
        return dataList;
    }

    public StockData getStockData(Cursor c)
    {
        StockData data = new StockData();
        data._time_sec = c.getLong(c.getColumnIndex("time_sec"));
        data._code = c.getString(c.getColumnIndex("code"));
        data._name = c.getString(c.getColumnIndex("name"));

        data._todayOpen = c.getDouble(c.getColumnIndex("todayOpen"));
        data._yesterdayClose = c.getDouble(c.getColumnIndex("yesterdayClose"));

        data._cur = c.getDouble(c.getColumnIndex("cur"));
        data._top = c.getDouble(c.getColumnIndex("top"));
        data._bottom = c.getDouble(c.getColumnIndex("bottom"));

        data._deal = c.getLong(c.getColumnIndex("deal"));
        data._dealGold = c.getDouble(c.getColumnIndex("dealGold"));

        data._buy[0]._price = c.getDouble(c.getColumnIndex("buy1price"));
        data._buy[0]._deal = c.getLong(c.getColumnIndex("buy1deal"));

        data._buy[1]._price = c.getDouble(c.getColumnIndex("buy2price"));
        data._buy[1]._deal = c.getLong(c.getColumnIndex("buy2deal"));

        data._buy[2]._price = c.getDouble(c.getColumnIndex("buy3price"));
        data._buy[2]._deal = c.getLong(c.getColumnIndex("buy3deal"));

        data._buy[3]._price = c.getDouble(c.getColumnIndex("buy4price"));
        data._buy[3]._deal = c.getLong(c.getColumnIndex("buy4deal"));

        data._buy[4]._price = c.getDouble(c.getColumnIndex("buy5price"));
        data._buy[4]._deal = c.getLong(c.getColumnIndex("buy5deal"));

        data._sell[0]._price = c.getDouble(c.getColumnIndex("sell1price"));
        data._sell[0]._deal = c.getLong(c.getColumnIndex("sell1deal"));

        data._sell[1]._price = c.getDouble(c.getColumnIndex("sell2price"));
        data._sell[1]._deal = c.getLong(c.getColumnIndex("sell2deal"));

        data._sell[2]._price = c.getDouble(c.getColumnIndex("sell3price"));
        data._sell[2]._deal = c.getLong(c.getColumnIndex("sell3deal"));

        data._sell[3]._price = c.getDouble(c.getColumnIndex("sell4price"));
        data._sell[3]._deal = c.getLong(c.getColumnIndex("sell4deal"));

        data._sell[4]._price = c.getDouble(c.getColumnIndex("sell5price"));
        data._sell[4]._deal = c.getLong(c.getColumnIndex("sell5deal"));

        return data;
    }


    //-------------------News---------------
    public void addNews(NewsData data)
    {
        ContentValues cv = new ContentValues();
        //往ContentValues对象存放数据，键-值对模式
        cv.put("_id", data._id );
        cv.put("content_text", data._content_text );
        cv.put("time_sec", data._time_sec );

        db.insertWithOnConflict("tb_News", null, cv,SQLiteDatabase.CONFLICT_IGNORE);
    }

    public List<NewsData> queryNews(boolean getNoPlay)
    {
        ArrayList<NewsData> dataList = new ArrayList<NewsData>();

        Cursor c;
        if(getNoPlay)
        {
            long today_time=getTodayTimeSec_subOneHour();
            c = db.query("tb_News", new String[]{"*"}, "isPlay=? and time_sec>?", new String[]{Integer.toString(0),Long.toString(today_time)},
                   null, null, "time_sec ASC","1");

        }
        else
        {
            long today_time=getTodayTimeSec();
            c = db.query("tb_News", new String[]{"*"}, "time_sec>?", new String[]{Long.toString(today_time)},
                    null, null, "time_sec ASC","30");
        }

        if (c.moveToLast()) {
            while (!c.isBeforeFirst()) {
                NewsData data = new NewsData();
                data._id = c.getLong(c.getColumnIndex("_id"));
                data._content_text = c.getString(c.getColumnIndex("content_text"));
                data._time_sec = c.getLong(c.getColumnIndex("time_sec"));
                dataList.add(data);
                c.moveToPrevious();
            }
        }
        c.close();
        return dataList;
    }

    public void addNewsList(List<NewsData> dataList)
    {
        db.beginTransaction();  //手动设置开始事务
        try{
            //批量处理操作
            for(NewsData data:dataList){
                addNews(data);
            }
            db.setTransactionSuccessful(); //设置事务处理成功，不设置会自动回滚不提交
        }catch(Exception e){

        }finally{
            db.endTransaction(); //处理完成
        }
    }

    public void updateNews(NewsData data)
    {
        ContentValues cv = new ContentValues();
        //往ContentValues对象存放数据，键-值对模式
        cv.put("isPlay", 1);
        db.update("tb_News", cv,"_id= ? ", new String[] {Long.toString(data._id) });

    }

    public void deleteNews()
    {
        long today_time=getTodayTimeSec();
       // db.delete("tb_News","time_sec< ? ", new String[] {Long.toString(today_time) });
        db.delete("tb_News",null,null);
        db.execSQL("DELETE FROM tb_News");
    }


    public  long getTodayTimeSec()
    {
        long time=System.currentTimeMillis();
        final Calendar mCalendar= Calendar.getInstance();
        mCalendar.setTimeInMillis(time);
       // mCalendar.set(Calendar.HOUR,0);
        mCalendar.set(Calendar.MINUTE,0);
        mCalendar.set(Calendar.SECOND,0);
        return  mCalendar.getTimeInMillis();
    }


    public  long getTodayTimeSec_subOneHour()
    {
        long time=System.currentTimeMillis();
        final Calendar mCalendar= Calendar.getInstance();
        mCalendar.setTimeInMillis(time);
        mCalendar.add(Calendar.HOUR,-1);
        return  mCalendar.getTimeInMillis();
    }

    /**
     * close database
     */
    public void closeDB()
    {
        db.close();
    }
}