package com.example.administrator.oldmanstock;



public class StockData
{
    public class StockNow
    {
        public double _price;
        public long _deal;

        public StockNow()
        {

        }
        public StockNow(double price, long deal)
        {
            this._price = price;
            this._deal = deal;
        }

    };

    public long  _time_sec;
    public String _code;
    public String _name="";
    public double _todayOpen;
    public double _yesterdayClose;

    public double _cur;
    public double _top;
    public double _bottom;

    public long _deal;
    public double _dealGold;

    int number=5;
    StockNow[] _buy=new StockNow[number];
    StockNow[] _sell=new StockNow[number];

    public StockData()
    {
        for(int i=0;i<number;i++)
        {
            this._buy[i]=new StockNow();
            this._sell[i]=new StockNow();

        }
    }

    public StockData(StockData one)
    {
        // TODO Auto-generated constructor stub
        this._time_sec = one._time_sec;
        this._code = one._code;
        this._name = one._name;
        this._todayOpen = one._todayOpen;
        this._yesterdayClose = one._yesterdayClose;

        this._cur = one._cur;
        this._top = one._top;
        this._bottom = one._bottom;

        this._deal = one._deal;
        this._dealGold = one._dealGold;

        for (int i=0;i<number;i++)
        {
            this._buy[i] = one._buy[i];
            this._sell[i] = one._sell[i];
        }
    }

}