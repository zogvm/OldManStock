package com.example.administrator.oldmanstock;

/**
 * Created by Administrator on 2018/01/04.
 */

public class NewsData {

    public long _id;
    public String _content_text;
    public long _time_sec;

    public NewsData()
    {
    }

    public NewsData( long id,String text,long time_sec)
    {
        this._id=id;
        this._content_text=text;
        this._time_sec=time_sec;
    }


    public NewsData(NewsData one)
    {
        this._id=one._id;
        this._content_text=one._content_text;
        this._time_sec=one._time_sec;
    }
}
