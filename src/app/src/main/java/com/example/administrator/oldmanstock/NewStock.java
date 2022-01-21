package com.example.administrator.oldmanstock;

import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.loopj.android.http.*;
import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class NewStock extends AppCompatActivity {


    public class NewStockData
    {
        public String _date;
        public String _name;
        public String _code;
        public String _price;
        public String _num;

        NewStockData()
        {
        }

        public NewStockData(String date, String name, String code, String price, String num)
        {
            this._date = date;
            this._name = name;
            this._code = code;
            this._price = price;
            this._num = num;
        }
    };

    private ListView m_listView;
    private SimpleAdapter m_listAdapter = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //去掉标题栏
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        setContentView(R.layout.new_stock);
        m_listView = (ListView) findViewById(R.id.newStock_listView);
        queryCFI();

    }

    //http://blog.csdn.net/hil2000/article/details/13949513
    //http://loopj.com/android-async-http/
    //https://github.com/loopj/android-async-http
    //http://www.aichengxu.com/android/2576806.htm
    //http://mvnrepository.com/artifact/cz.msebera.android/httpclient/4.3.6

    public void BuildNewStockList(List<NewStockData>  datalist)
    {
        if(datalist.isEmpty())
            return;

        ArrayList<Map<String, Object>> mylist = new ArrayList<Map<String, Object>>();

        for (NewStockData data : datalist)
        {
            HashMap<String, Object> map = new HashMap<String, Object>();

            map.put("newStock_Date", data._date );
            map.put("newStock_Code", data._code );

            map.put("newStock_Name", data._name );
            map.put("newStock_Price", data._price );

            map.put("newStock_Num", data._num );

            mylist.add(map);
        }

        m_listAdapter = new SimpleAdapter(this,
                mylist,
                R.layout.newstock_list_item,
                new String[] { "newStock_Date", "newStock_Code",
                        "newStock_Name", "newStock_Price",
                        "newStock_Num",
                },
                new int[] { R.id.newStock_Date, R.id.newStock_Code,
                        R.id.newStock_Name,   R.id.newStock_Price,
                        R.id.newStock_Num
                });

        m_listView.setAdapter(m_listAdapter);
    }


    public void queryCFI()
    {
        AsyncHttpClient client = new AsyncHttpClient();
        client.setTimeout(10000);
        client.get(this,"http://newstock.cfi.cn/", new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {
                // Initiated the request
            }

            @Override
            public void onRetry(int retryNo) {
                // Request was retried
            }

            @Override
            public void onProgress(long bytesWritten, long totalSize) {
                // Progress notification
            }

            @Override
            public void onFinish() {
                // Completed the request (either success or failure)
            }

            @Override
            public void onSuccess(int i, cz.msebera.android.httpclient.Header[] headers, byte[] bytes) {
                //Toast.makeText(getApplicationContext(),"newstock.cfi.cn ok", Toast.LENGTH_LONG).show();
                List<NewStockData>  datalist=parseCFI(bytes);
                // Toast.makeText(getApplicationContext(),""+datalist.size(), Toast.LENGTH_LONG).show();
                BuildNewStockList(datalist);
            }

            @Override
            public void onFailure(int i, cz.msebera.android.httpclient.Header[] headers, byte[] bytes, Throwable throwable) {
                Toast.makeText(getApplicationContext(),"newstock.cfi.cn fail", Toast.LENGTH_LONG).show();
            }
        });

    }
    public List<NewStockData> parseCFI(byte[] bytes)
    {
        ArrayList<NewStockData>  datalist=new ArrayList<NewStockData>();

        String html = new String(bytes, Charset.forName("UTF-8"));

        if(html.isEmpty())
            return datalist;

        Document doc = Jsoup.parse(html);;
        Element content = doc.getElementById("nodecontent_67");
        // writeSDFile(content.html().getBytes());
        Elements trs=  content.getElementsByTag("tr");

        int a=0;
        for(Element tr:trs  )
        {
            //跳过第一行
            a++;
            if(1==a)
            {
                continue;
            }

            NewStockData data=new NewStockData();

            Elements nobrs=tr.getElementsByTag("nobr");
            if(nobrs.size()>0)
            {
                data._date=nobrs.get(0).text();
                //待定
                if(data._date.equalsIgnoreCase(this.getString(R.string.key_daiding))
                        || data._date.contains(this.getString(R.string.key_wan)))
                {
                    continue;
                }

                data._date=data._date
                        .replace(this.getString(R.string.key_zhou),"")
                        .replace(this.getString(R.string.key_yue),"-")
                        .replace(this.getString(R.string.key_ri)," ")
                        .trim();
            }
            else
            {
                continue;
            }

            Elements tds=tr.getElementsByTag("td");
            if(tds.size()>5)
            {
                data._name=tds.get(0).text().trim();
                data._code=tds.get(2).text()
                        .replace(this.getString(R.string.key_hu),"")
                        .replace(this.getString(R.string.key_shen),"")
                        .trim();
                data._price=tds.get(3).text().replace(this.getString(R.string.key_yugu),"").trim();
                data._num=tds.get(5).text()
                        .replace(this.getString(R.string.key_wangu),"")
                        .replace(this.getString(R.string.key_yugu),"")
                        .trim();
            }
            else
            {
                continue;
            }

            datalist.add(0,data);
        }
        return  datalist;
    }

    //test
    public void writeSDFile(byte[] bytes)
    {
        String sdCardDir = Environment.getExternalStorageDirectory().getAbsolutePath();
        File saveFile = new File(sdCardDir, "aaaa.txt");
        FileOutputStream outStream = null;
        try {
            outStream = new FileOutputStream(saveFile);
        } catch (FileNotFoundException e) {
        }
        try {
            outStream.write(bytes);
        } catch (IOException e) {
        }
        try {
            outStream.close();
        } catch (IOException e) {
        }
    }
}


//document.writeln("
//<div id="ncontent_67" style="display:none;">
//<table style="width:950px;height:px;">
//<tbody>
//<tr>
//<td>股票名称</td>
//<td>新股申购日</td>
//<td>申购代码</td>
//<td>发行价</td>
//<td>发行量/股</td>
//<td>申购限额</td>
//<td>申购测算</td>
//<td>发行市盈率</td>
//<td>中签率</td>
//<td>中签号</td>
//<td>招股书</td>
//</tr>
//<tr>
//<td><a href="http://quote.cfi.cn/quote.aspx?stockid=20477" style="color:green">成都银行 </a></td>
//<td>
//<nobr>
//      01月17日
//</nobr></td>
//<td>沪:780838 </td>
//<td>6.99</td>
//<td><a href="http://quote.cfi.cn/fxysg/20477/601838.html" style="color:green">3.61亿</a></td>
//<td>10.8万股</td>
//<td><a href="http://quote.cfi.cn/ipocalculator.aspx"><img src="pic/calculator.png" style="border:0px">申购测算</a></td>
//<td><a href="http://quote.cfi.cn/cwfxzb/20477/601838.html" style="color:green">9.99倍</a></td>
//<td>--</td>
//<td>--</td>
//<td><a href="http://gg.cfi.cn/cbgg/20477/601838.html">招股书</a></td>
//</tr>
//<tr>
//<td><a href="http://quote.cfi.cn/quote.aspx?stockid=28893" style="color:green">德邦股份 </a></td>
//<td>
//<nobr>
//      01月04日周四
//</nobr></td>
//<td>沪:732056 </td>
//<td>预估:4.84</td>
//<td><a href="http://quote.cfi.cn/fxysg/28893/603056.html" style="color:green">1.00亿</a></td>
//<td>3万股</td>
//<td><a href="http://quote.cfi.cn/ipocalculator.aspx"><img src="pic/calculator.png" style="border:0px">申购测算</a></td>
//<td>--</td>
//<td>--</td>
//<td>--</td>
//<td><a href="http://gg.cfi.cn/cbgg/28893/603056.html">招股书</a></td>
//</tr>
//<tr>
//<td><a href="http://quote.cfi.cn/quote.aspx?stockid=60438" style="color:green">西菱动力(创) </a></td>
//<td>
//<nobr>
//      01月04日周四
//</nobr></td>
//<td>深:300733 </td>
//<td>12.9</td>
//<td><a href="http://quote.cfi.cn/fxysg/60438/300733.html" style="color:green">4000万</a></td>
//<td>1.6万股</td>
//<td><a href="http://quote.cfi.cn/ipocalculator.aspx"><img src="pic/calculator.png" style="border:0px">申购测算</a></td>
//<td><a href="http://quote.cfi.cn/cwfxzb/60438/300733.html" style="color:green">22.98倍</a></td>
//<td>--</td>
//<td>--</td>
//<td><a href="http://gg.cfi.cn/cbgg/60438/300733.html">招股书</a></td>
//</tr>
//<tr>
//<td><a href="http://quote.cfi.cn/quote.aspx?stockid=55293" style="color:green">盈趣科技 </a></td>
//<td>
//<nobr>
//      01月04日周四
//</nobr></td>
//<td>深:002925 </td>
//<td>预估:22.50</td>
//<td><a href="http://quote.cfi.cn/fxysg/55293/002925.html" style="color:green">7500万</a></td>
//<td>2.25万股</td>
//<td><a href="http://quote.cfi.cn/ipocalculator.aspx"><img src="pic/calculator.png" style="border:0px">申购测算</a></td>
//<td>--</td>
//<td>--</td>
//<td>--</td>
//<td><a href="http://gg.cfi.cn/cbgg/55293/002925.html">招股书</a></td>
//</tr>
//<tr>
//<td><a href="http://quote.cfi.cn/quote.aspx?stockid=19670" style="color:green">美凯龙 </a></td>
//<td>
//<nobr>
//      01月03日周三
//</nobr></td>
//<td>沪:780828 </td>
//<td>预估:10.23</td>
//<td><a href="http://quote.cfi.cn/fxysg/19670/601828.html" style="color:green">3.15亿</a></td>
//<td>9.4万股</td>
//<td><a href="http://quote.cfi.cn/ipocalculator.aspx"><img src="pic/calculator.png" style="border:0px">申购测算</a></td>
//<td>--</td>
//<td>--</td>
//<td>--</td>
//<td><a href="http://gg.cfi.cn/cbgg/19670/601828.html">招股书</a></td>
//</tr>
//<tr>
//<td><a href="http://quote.cfi.cn/quote.aspx?stockid=73607" style="color:green">百华悦邦(创) </a></td>
//<td>
//<nobr>
//      12月28日周四
//<span style="color:rgb(153,51,255)">完</span>
//</nobr></td>
//<td>深:300736 </td>
//<td>19.18</td>
//<td><a href="http://quote.cfi.cn/fxysg/73607/300736.html" style="color:green">1357.72万</a></td>
//<td>1.35万股</td>
//<td><a href="http://quote.cfi.cn/ipocalculator.aspx"><img src="pic/calculator.png" style="border:0px">申购测算</a></td>
//<td><a href="http://quote.cfi.cn/cwfxzb/73607/300736.html" style="color:green">22.99倍</a></td>
//<td>0.0128%</td>
//<td>--</td>
//<td><a href="http://gg.cfi.cn/cbgg/73607/300736.html">招股书</a></td>
//</tr>
//<tr>
//<td><a href="http://quote.cfi.cn/quote.aspx?stockid=35203" style="color:green">万达地产 </a></td>
//<td>
//<nobr>
//      待定
//</nobr></td>
//<td>沪:未公布 </td>
//<td>待定</td>
//<td><a href="http://quote.cfi.cn/fxysg/35203/未公布.html" style="color:green">待定</a></td>
//<td>待定</td>
//<td><a href="http://quote.cfi.cn/ipocalculator.aspx"><img src="pic/calculator.png" style="border:0px">申购测算</a></td>
//<td>--</td>
//<td>--</td>
//<td>--</td>
//<td>--</td>
//</tr>
//<tr>
//<td><a href="http://quote.cfi.cn/quote.aspx?stockid=21141" style="color:green">欧派家居 </a></td>
//<td>
//<nobr>
//      待定
//</nobr></td>
//<td>沪:未公布 </td>
//<td>待定</td>
//<td><a href="http://quote.cfi.cn/fxysg/21141/0.html" style="color:green">待定</a></td>
//<td>待定</td>
//<td><a href="http://quote.cfi.cn/ipocalculator.aspx"><img src="pic/calculator.png" style="border:0px">申购测算</a></td>
//<td>--</td>
//<td>--</td>
//<td>--</td>
//<td><a href="http://gg.cfi.cn/cbgg/21141/0.html">招股书</a></td>
//</tr>
//<tr>
//<td><a href="http://quote.cfi.cn/quote.aspx?stockid=35353" style="color:green">仲景食品 </a></td>
//<td>
//<nobr>
//      待定
//</nobr></td>
//<td>深:未公布 </td>
//<td>待定</td>
//<td><a href="http://quote.cfi.cn/fxysg/35353/未公布.html" style="color:green">待定</a></td>
//<td>待定</td>
//<td><a href="http://quote.cfi.cn/ipocalculator.aspx"><img src="pic/calculator.png" style="border:0px">申购测算</a></td>
//<td>--</td>
//<td>--</td>
//<td>--</td>
//<td>--</td>
//</tr>
//<tr>
//<td><a href="http://quote.cfi.cn/quote.aspx?stockid=36580" style="color:green">阜特科技 </a></td>
//<td>
//<nobr>
//      待定
//</nobr></td>
//<td>深:未公布 </td>
//<td>待定</td>
//<td><a href="http://quote.cfi.cn/fxysg/36580/未公布.html" style="color:green">待定</a></td>
//<td>待定</td>
//<td><a href="http://quote.cfi.cn/ipocalculator.aspx"><img src="pic/calculator.png" style="border:0px">申购测算</a></td>
//<td>--</td>
//<td>--</td>
//<td>--</td>
//<td>--</td>
//</tr>
//<tr>
//<td><a href="http://quote.cfi.cn/quote.aspx?stockid=67473" style="color:green">达特照明 </a></td>
//<td>
//<nobr>
//      待定
//</nobr></td>
//<td>深:832709 </td>
//<td>待定</td>
//<td><a href="http://quote.cfi.cn/fxysg/67473/832709.html" style="color:green">待定</a></td>
//<td>待定</td>
//<td><a href="http://quote.cfi.cn/ipocalculator.aspx"><img src="pic/calculator.png" style="border:0px">申购测算</a></td>
//<td>--</td>
//<td>--</td>
//<td>--</td>
//<td><a href="http://gg.cfi.cn/cbgg/67473/832709.html">招股书</a></td>
//</tr>
//<tr>
//<td><a href="http://quote.cfi.cn/quote.aspx?stockid=22650" style="color:green">昆山华恒 </a></td>
//<td>
//<nobr>
//      待定
//</nobr></td>
//<td>深:未公布 </td>
//<td>待定</td>
//<td><a href="http://quote.cfi.cn/fxysg/22650/未公布.html" style="color:green">待定</a></td>
//<td>待定</td>
//<td><a href="http://quote.cfi.cn/ipocalculator.aspx"><img src="pic/calculator.png" style="border:0px">申购测算</a></td>
//<td>--</td>
//<td>--</td>
//<td>--</td>
//<td>--</td>
//</tr>
//<tr>
//<td><a href="http://quote.cfi.cn/quote.aspx?stockid=67870" style="color:green">流金岁月 </a></td>
//<td>
//<nobr>
//      待定
//</nobr></td>
//<td>深:834021 </td>
//<td>待定</td>
//<td><a href="http://quote.cfi.cn/fxysg/67870/834021.html" style="color:green">待定</a></td>
//<td>待定</td>
//<td><a href="http://quote.cfi.cn/ipocalculator.aspx"><img src="pic/calculator.png" style="border:0px">申购测算</a></td>
//<td>--</td>
//<td>--</td>
//<td>--</td>
//<td><a href="http://gg.cfi.cn/cbgg/67870/834021.html">招股书</a></td>
//</tr>
//<tr>
//<td><a href="http://quote.cfi.cn/quote.aspx?stockid=71603" style="color:green">通宝光电 </a></td>
//<td>
//<nobr>
//      待定
//</nobr></td>
//<td>深:833137 </td>
//<td>待定</td>
//<td><a href="http://quote.cfi.cn/fxysg/71603/833137.html" style="color:green">待定</a></td>
//<td>待定</td>
//<td><a href="http://quote.cfi.cn/ipocalculator.aspx"><img src="pic/calculator.png" style="border:0px">申购测算</a></td>
//<td>--</td>
//<td>--</td>
//<td>--</td>
//<td><a href="http://gg.cfi.cn/cbgg/71603/833137.html">招股书</a></td>
//</tr>
//<tr>
//<td><a href="http://quote.cfi.cn/quote.aspx?stockid=22856" style="color:green">千乘影视 </a></td>
//<td>
//<nobr>
//      待定
//</nobr></td>
//<td>深:未公布 </td>
//<td>待定</td>
//<td><a href="http://quote.cfi.cn/fxysg/22856/未公布.html" style="color:green">待定</a></td>
//<td>待定</td>
//<td><a href="http://quote.cfi.cn/ipocalculator.aspx"><img src="pic/calculator.png" style="border:0px">申购测算</a></td>
//<td>--</td>
//<td>--</td>
//<td>--</td>
//<td>--</td>
//</tr>
//<tr>
//<td><a href="http://quote.cfi.cn/quote.aspx?stockid=66093" style="color:green">景津环保 </a></td>
//<td>
//<nobr>
//      待定
//</nobr></td>
//<td>沪:未公布 </td>
//<td>待定</td>
//<td><a href="http://quote.cfi.cn/fxysg/66093/未公布.html" style="color:green">待定</a></td>
//<td>待定</td>
//<td><a href="http://quote.cfi.cn/ipocalculator.aspx"><img src="pic/calculator.png" style="border:0px">申购测算</a></td>
//<td>--</td>
//<td>--</td>
//<td>--</td>
//<td>--</td>
//</tr>
//<tr>
//<td><a href="http://quote.cfi.cn/quote.aspx?stockid=22785" style="color:green">华自科技 </a></td>
//<td>
//<nobr>
//      待定
//</nobr></td>
//<td>深:未公布 </td>
//<td>待定</td>
//<td><a href="http://quote.cfi.cn/fxysg/22785/未公布.html" style="color:green">待定</a></td>
//<td>待定</td>
//<td><a href="http://quote.cfi.cn/ipocalculator.aspx"><img src="pic/calculator.png" style="border:0px">申购测算</a></td>
//<td>--</td>
//<td>--</td>
//<td>--</td>
//<td>--</td>
//</tr>
//<tr>
//<td><a href="http://quote.cfi.cn/quote.aspx?stockid=35337" style="color:green">友缘股份 </a></td>
//<td>
//<nobr>
//      待定
//</nobr></td>
//<td>深:未公布 </td>
//<td>待定</td>
//<td><a href="http://quote.cfi.cn/fxysg/35337/未公布.html" style="color:green">待定</a></td>
//<td>待定</td>
//<td><a href="http://quote.cfi.cn/ipocalculator.aspx"><img src="pic/calculator.png" style="border:0px">申购测算</a></td>
//<td>--</td>
//<td>--</td>
//<td>--</td>
//<td>--</td>
//</tr>
//<tr>
//<td><a href="http://quote.cfi.cn/quote.aspx?stockid=19544" style="color:green">洪汇股份 </a></td>
//<td>
//<nobr>
//      待定
//</nobr></td>
//<td>深:未公布 </td>
//<td>待定</td>
//<td><a href="http://quote.cfi.cn/fxysg/19544/0.html" style="color:green">2700万</a></td>
//<td>预估:1.00万股</td>
//<td><a href="http://quote.cfi.cn/ipocalculator.aspx"><img src="pic/calculator.png" style="border:0px">申购测算</a></td>
//<td>--</td>
//<td>--</td>
//<td>--</td>
//<td><a href="http://gg.cfi.cn/cbgg/19544/0.html">招股书</a></td>
//</tr>
//<tr>
//<td><a href="http://quote.cfi.cn/quote.aspx?stockid=21729" style="color:green">安必平 </a></td>
//<td>
//<nobr>
//      待定
//</nobr></td>
//<td>深:未公布 </td>
//<td>待定</td>
//<td><a href="http://quote.cfi.cn/fxysg/21729/未公布.html" style="color:green">2335万</a></td>
//<td>预估:0.90万股</td>
//<td><a href="http://quote.cfi.cn/ipocalculator.aspx"><img src="pic/calculator.png" style="border:0px">申购测算</a></td>
//<td>--</td>
//<td>--</td>
//<td>--</td>
//<td>--</td>
//</tr>
//<tr>
//<td><a href="http://quote.cfi.cn/quote.aspx?stockid=74745" style="color:green">广东南方 </a></td>
//<td>
//<nobr>
//      待定
//</nobr></td>
//<td>深:未公布 </td>
//<td>待定</td>
//<td><a href="http://quote.cfi.cn/fxysg/74745/未公布.html" style="color:green">待定</a></td>
//<td>待定</td>
//<td><a href="http://quote.cfi.cn/ipocalculator.aspx"><img src="pic/calculator.png" style="border:0px">申购测算</a></td>
//<td>--</td>
//<td>--</td>
//<td>--</td>
//<td>--</td>
//</tr>
//<tr>
//<td><a href="http://quote.cfi.cn/quote.aspx?stockid=27632" style="color:green">今创集团 </a></td>
//<td>
//<nobr>
//      待定
//</nobr></td>
//<td>沪:732680 </td>
//<td>预估:32.69</td>
//<td><a href="http://quote.cfi.cn/fxysg/27632/603680.html" style="color:green">4200万</a></td>
//<td>1.2万股</td>
//<td><a href="http://quote.cfi.cn/ipocalculator.aspx"><img src="pic/calculator.png" style="border:0px">申购测算</a></td>
//<td>--</td>
//<td>--</td>
//<td>--</td>
//<td><a href="http://gg.cfi.cn/cbgg/27632/603680.html">招股书</a></td>
//</tr>
//<tr>
//<td><a href="http://quote.cfi.cn/quote.aspx?stockid=66092" style="color:green">金山软件 </a></td>
//<td>
//<nobr>
//      待定
//</nobr></td>
//<td>深:未公布 </td>
//<td>待定</td>
//<td><a href="http://quote.cfi.cn/fxysg/66092/未公布.html" style="color:green">待定</a></td>
//<td>待定</td>
//<td><a href="http://quote.cfi.cn/ipocalculator.aspx"><img src="pic/calculator.png" style="border:0px">申购测算</a></td>
//<td>--</td>
//<td>--</td>
//<td>--</td>
//<td>--</td>
//</tr>
//<tr>
//<td><a href="http://quote.cfi.cn/quote.aspx?stockid=19772" style="color:green">美尚生态 </a></td>
//<td>
//<nobr>
//      待定
//</nobr></td>
//<td>深:未公布 </td>
//<td>待定</td>
//<td><a href="http://quote.cfi.cn/fxysg/19772/0.html" style="color:green">待定</a></td>
//<td>待定</td>
//<td><a href="http://quote.cfi.cn/ipocalculator.aspx"><img src="pic/calculator.png" style="border:0px">申购测算</a></td>
//<td>--</td>
//<td>--</td>
//<td>--</td>
//<td><a href="http://gg.cfi.cn/cbgg/19772/0.html">招股书</a></td>
//</tr>
//<tr>
//<td><a href="http://quote.cfi.cn/quote.aspx?stockid=19673" style="color:green">南京中油 </a></td>
//<td>
//<nobr>
//      待定
//</nobr></td>
//<td>深:未公布 </td>
//<td>待定</td>
//<td><a href="http://quote.cfi.cn/fxysg/19673/0.html" style="color:green">3100万</a></td>
//<td>预估:1.20万股</td>
//<td><a href="http://quote.cfi.cn/ipocalculator.aspx"><img src="pic/calculator.png" style="border:0px">申购测算</a></td>
//<td>--</td>
//<td>--</td>
//<td>--</td>
//<td><a href="http://gg.cfi.cn/cbgg/19673/0.html">招股书</a></td>
//</tr>
//<tr>
//<td><a href="http://quote.cfi.cn/quote.aspx?stockid=22826" style="color:green">丽晶科技 </a></td>
//<td>
//<nobr>
//      待定
//</nobr></td>
//<td>深:未公布 </td>
//<td>待定</td>
//<td><a href="http://quote.cfi.cn/fxysg/22826/未公布.html" style="color:green">待定</a></td>
//<td>待定</td>
//<td><a href="http://quote.cfi.cn/ipocalculator.aspx"><img src="pic/calculator.png" style="border:0px">申购测算</a></td>
//<td>--</td>
//<td>--</td>
//<td>--</td>
//<td>--</td>
//</tr>
//<tr>
//<td><a href="http://quote.cfi.cn/quote.aspx?stockid=21581" style="color:green">德力西 </a></td>
//<td>
//<nobr>
//      待定
//</nobr></td>
//<td>沪:未公布 </td>
//<td>待定</td>
//<td><a href="http://quote.cfi.cn/fxysg/21581/未公布.html" style="color:green">3334万</a></td>
//<td>预估:1.30万股</td>
//<td><a href="http://quote.cfi.cn/ipocalculator.aspx"><img src="pic/calculator.png" style="border:0px">申购测算</a></td>
//<td>--</td>
//<td>--</td>
//<td>--</td>
//<td>--</td>
//</tr>
//<tr>
//<td><a href="http://quote.cfi.cn/quote.aspx?stockid=67562" style="color:green">国泰君安 </a></td>
//<td>
//<nobr>
//      待定
//</nobr></td>
//<td>沪:未公布 </td>
//<td>待定</td>
//<td><a href="http://quote.cfi.cn/fxysg/67562/未公布.html" style="color:green">待定</a></td>
//<td>待定</td>
//<td><a href="http://quote.cfi.cn/ipocalculator.aspx"><img src="pic/calculator.png" style="border:0px">申购测算</a></td>
//<td>--</td>
//<td>--</td>
//<td>--</td>
//<td>--</td>
//</tr>
//<tr>
//<td><a href="http://quote.cfi.cn/quote.aspx?stockid=60435" style="color:green">睿思凯 </a></td>
//<td>
//<nobr>
//      待定
//</nobr></td>
//<td>深:832389 </td>
//<td>待定</td>
//<td><a href="http://quote.cfi.cn/fxysg/60435/832389.html" style="color:green">待定</a></td>
//<td>待定</td>
//<td><a href="http://quote.cfi.cn/ipocalculator.aspx"><img src="pic/calculator.png" style="border:0px">申购测算</a></td>
//<td>--</td>
//<td>--</td>
//<td>--</td>
//<td><a href="http://gg.cfi.cn/cbgg/60435/832389.html">招股书</a></td>
//</tr>
//<tr>
//<td><a href="http://quote.cfi.cn/quote.aspx?stockid=59084" style="color:green">北洋传媒 </a></td>
//<td>
//<nobr>
//      待定
//</nobr></td>
//<td>沪:未公布 </td>
//<td>待定</td>
//<td><a href="http://quote.cfi.cn/fxysg/59084/未公布.html" style="color:green">待定</a></td>
//<td>待定</td>
//<td><a href="http://quote.cfi.cn/ipocalculator.aspx"><img src="pic/calculator.png" style="border:0px">申购测算</a></td>
//<td>--</td>
//<td>--</td>
//<td>--</td>
//<td>--</td>
//</tr>
//<tr>
//<td colspan="11" style="text-align:left;word-break:break-all;"><a style="text-decoration:none;color:white;">|&lt;</a><span style="margin-left:1em;"></span><a style="text-decoration:none;color:white;"><font style="font-weight:bold;">&lt;</font></a><span style="margin-left:1em;"></span><span style="background-color:rgb(192,192,192);width:15px;word-break:keep-all;text-align:center;border-color:black;border-style:solid;border-width:1px;">1</span><span style="margin-left:1em;"></span><a style="text-decoration:none;cursor:pointer;" onclick="nodepage(67,2,'');">2</a><span style="margin-left:1em;"></span><a style="text-decoration:none;cursor:pointer;" onclick="nodepage(67,3,'');">3</a><span style="margin-left:1em;"></span><a style="text-decoration:none;cursor:pointer;" onclick="nodepage(67,4,'');">4</a><span style="margin-left:1em;"></span><a style="text-decoration:none;cursor:pointer;" onclick="nodepage(67,5,'');">5</a><span style="margin-left:1em;"></span><a style="text-decoration:none;cursor:pointer;" onclick="nodepage(67,6,'');">6</a><span style="margin-left:1em;"></span><a style="text-decoration:none;cursor:pointer;" onclick="nodepage(67,7,'');">7</a><span style="margin-left:1em;"></span><a style="text-decoration:none;cursor:pointer;" onclick="nodepage(67,8,'');">8</a><span style="margin-left:1em;"></span><a style="text-decoration:none;cursor:pointer;" onclick="nodepage(67,9,'');">9</a><span style="margin-left:1em;"></span><a style="text-decoration:none;cursor:pointer;" onclick="nodepage(67,10,'');">10</a><span style="margin-left:1em;"></span><a style="text-decoration:none;cursor:pointer;" onclick="nodepage(67,11,'');">11</a><span style="margin-left:1em;"></span><a style="text-decoration:none;cursor:pointer;" onclick="nodepage(67,12,'');">12</a><span style="margin-left:1em;"></span><a style="text-decoration:none;cursor:pointer;" onclick="nodepage(67,13,'');">13</a><span style="margin-left:1em;"></span><a style="text-decoration:none;cursor:pointer;" onclick="nodepage(67,14,'');">14</a><span style="margin-left:1em;"></span><a style="text-decoration:none;cursor:pointer;" onclick="nodepage(67,15,'');">15</a><span style="margin-left:1em;"></span><a style="text-decoration:none;cursor:pointer;" onclick="nodepage(67,16,'');">16</a><span style="margin-left:1em;"></span><a style="text-decoration:none;cursor:pointer;" onclick="nodepage(67,17,'');">17</a><span style="margin-left:1em;"></span><a style="text-decoration:none;cursor:pointer;" onclick="nodepage(67,18,'');">18</a><span style="margin-left:1em;"></span><a style="text-decoration:none;cursor:pointer;" onclick="nodepage(67,19,'');">19</a><span style="margin-left:1em;"></span><a style="text-decoration:none;cursor:pointer;" onclick="nodepage(67,20,'');">20</a><span style="margin-left:1em;"></span><a style="text-decoration:none;cursor:pointer;" onclick="nodepage(67,21,'');">21</a><span style="margin-left:1em;"></span><a style="text-decoration:none;cursor:pointer;" onclick="nodepage(67,22,'');">22</a><span style="margin-left:1em;"></span><a style="text-decoration:none;cursor:pointer;" onclick="nodepage(67,23,'');">23</a><span style="margin-left:1em;"></span><a style="text-decoration:none;cursor:pointer;" onclick="nodepage(67,24,'');">24</a><span style="margin-left:1em;"></span><a style="text-decoration:none;cursor:pointer;" onclick="nodepage(67,25,'');">25</a><span style="margin-left:1em;"></span><a style="text-decoration:none;cursor:pointer;" onclick="nodepage(67,26,'');">26</a><span style="margin-left:1em;"></span><a style="text-decoration:none;cursor:pointer;" onclick="nodepage(67,27,'');">27</a><span style="margin-left:1em;"></span><a style="text-decoration:none;cursor:pointer;" onclick="nodepage(67,28,'');">28</a><span style="margin-left:1em;"></span><a style="text-decoration:none;cursor:pointer;" onclick="nodepage(67,29,'');">29</a><span style="margin-left:1em;"></span><a style="text-decoration:none;cursor:pointer;" onclick="nodepage(67,30,'');">30</a><span style="margin-left:1em;"></span><a style="text-decoration:none;cursor:pointer;" onclick="nodepage(67,31,'');">31</a><span style="margin-left:1em;"></span><a style="text-decoration:none;cursor:pointer;" onclick="nodepage(67,32,'');">32</a><span style="margin-left:1em;"></span><a style="text-decoration:none;cursor:pointer;" onclick="nodepage(67,33,'');">33</a><span style="margin-left:1em;"></span><a style="text-decoration:none;cursor:pointer;" onclick="nodepage(67,34,'');">34</a><span style="margin-left:1em;"></span><a style="text-decoration:none;cursor:pointer;" onclick="nodepage(67,35,'');">35</a><span style="margin-left:1em;"></span><a style="text-decoration:none;cursor:pointer;" onclick="nodepage(67,36,'');">36</a><span style="margin-left:1em;"></span><a style="text-decoration:none;cursor:pointer;" onclick="nodepage(67,37,'');">37</a><span style="margin-left:1em;"></span><a style="text-decoration:none;cursor:pointer;" onclick="nodepage(67,38,'');">38</a><span style="margin-left:1em;"></span><a style="text-decoration:none;cursor:pointer;" onclick="nodepage(67,39,'');">39</a><span style="margin-left:1em;"></span><a style="text-decoration:none;cursor:pointer;" onclick="nodepage(67,40,'');">40</a><span style="margin-left:1em;"></span><a style="text-decoration:none;cursor:pointer;" onclick="nodepage(67,2,'');"><font style="font-weight:bold;">&gt;</font></a><span style="margin-left:1em;"></span><a style="text-decoration:none;cursor:pointer;" onclick="nodepage(67,40,'');">&gt;|</a><span style="margin-left:1em;"></span></td>
//</tr>
//</tbody>
//</table>
//<!--缓存生成:14:12:07，耗时:15.622ms-->
//<!--67-success-->
//</div>
//<script>if(document.all.item('nodecontent_67') != null) {document.all.item('nodecontent_67').innerHTML =document.all.item('ncontent_67').innerHTML; }</script>");
//<!--getpage2017/12/30 14:13:02-->