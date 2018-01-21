package com.example.administrator.oldmanstock;

/**
 * Created by Administrator on 2017/12/28.
 */

public class StockCode
{
    public String _code;

    public StockCode()
    {
    }

    public StockCode(String code)
    {
        this._code = code;
    }


    public StockCode(StockCode one)
    {
        // TODO Auto-generated constructor stub
        this._code = one._code;
    }

}