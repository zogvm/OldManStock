package com.example.administrator.oldmanstock;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import android.widget.TextView;
import android.widget.Toast;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.SynthesizerListener;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {
    private DBManager m_dbmgr;
    private ListView m_listView;
   // private SimpleAdapter m_listAdapter = null;
   private MyAdapter m_listAdapter = null;

    ArrayList<Map<String, Object>> m_StockLastDataList = new ArrayList<Map<String, Object>>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //去掉标题栏
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        setContentView(R.layout.activity_main);
        m_listView = (ListView) findViewById(R.id.listView);

        m_dbmgr = new DBManager(this);
        //特殊定制
        m_dbmgr.addStockCodeList_home();
        m_dbmgr.deleteNews();

        m_listView.setOnItemClickListener(new OnItemClickListenerImpl());

        //初始化声音
        InitTts();

        //立即刷一次
        updateListViewSINA(true);
       // updateListViewWS(true);
        //触发定时器
        handlerSINA.postDelayed(runnableSINA, 9800);
       // handlerWS.postDelayed(runnableWS, 50000);
      //  getPixelDisplayMetricsII();
    }

    @Override
    protected void onDestroy()
    {
        if( null != mTts ){
            mTts.stopSpeaking();
            // 退出时释放连接
            mTts.destroy();
        }
        m_dbmgr.closeDB();
        super.onDestroy();
    }

    //-------------------list view------------------
    public String D2S(double a)
    {
        return new DecimalFormat("0.00").format(a);
    }

    public void updateStockLast()
    {
        updateList();

        if(m_listAdapter!=null)
        {
            //更新
            m_listAdapter.notifyDataSetChanged();
        }
        else
        {
            createAdapter();
        }
    }

    public void updateList()
    {
        m_StockLastDataList.clear();

        List<StockCode> codeList = m_dbmgr.queryStockCode();
        List<StockData> dataList=m_dbmgr.queryAllStockLast(codeList);

        for (StockData data : dataList)
        {
            if (data._name.isEmpty())
                continue;

            HashMap<String, Object> map = new HashMap<String, Object>();

            map.put("ItemTitle", data._code );
            map.put("ItemName", data._name );

            map.put("ItemTodayOpen", D2S(data._todayOpen) );
            map.put("ItemYesterClose", D2S(data._yesterdayClose) );

            map.put("ItemCur", D2S(data._cur) );

            SimpleDateFormat dateFormat =new SimpleDateFormat("HH:mm:ss");
            map.put("ItemTime", dateFormat.format(new Date(data._time_sec)) );

            if(data._todayOpen <0.1)
            {
                map.put("ItemRiseFall", "--");
                map.put("ItemRiseFallRate", "--");
            }
            else
            {
                map.put("ItemRiseFall", D2S(data._cur - data._yesterdayClose));
                map.put("ItemRiseFallRate", D2S((data._cur - data._yesterdayClose) /data._yesterdayClose*100.0)+"%");
            }

            if(data._todayOpen <0.1)
            {
                map.put("ItemTop", "--");
                map.put("ItemTopRate", "--");
            }
            else
            {
                map.put("ItemTop", D2S(data._top));
                map.put("ItemTopRate",D2S((data._top - data._yesterdayClose) /data._yesterdayClose *100.0)+"%");
            }


            if(data._todayOpen <0.1)
            {
                map.put("ItemBottom", "--");
                map.put("ItemBottomRate", "--");
            }
            else
            {
                map.put("ItemBottom", D2S(data._bottom));
                map.put("ItemBottomRate", D2S((data._bottom - data._yesterdayClose) /data._yesterdayClose*100.0)+"%" );
            }

            map.put("ItemDeal", D2S(data._deal /(100*10000.0)));
            map.put("ItemDealGold", D2S(data._dealGold/(10000*10000.0)) );

            m_StockLastDataList.add(map);

            if(data._code.equalsIgnoreCase("000001"))
            {
                if((data._cur - data._yesterdayClose) <0)
                {
                    ((TextView)findViewById(R.id.ZhiShu)).setTextColor(getResources().getColor(R.color.stock_green));
                }
                else
                {
                    ((TextView)findViewById(R.id.ZhiShu)).setTextColor(getResources().getColor(R.color.stock_red));
                }

                ((TextView)findViewById(R.id.ZhiShu))
                        .setText(D2S(data._cur)+"  "+ D2S((data._cur - data._yesterdayClose) /data._yesterdayClose*100.0)+"%");
            }
        }

    }

    public void createAdapter()
    {
        //  m_listAdapter = new SimpleAdapter(this,
        m_listAdapter = new MyAdapter(this,
                m_StockLastDataList,
                R.layout.list_item,
                new String[] { "ItemTitle", "ItemName",
                        "ItemYesterClose", "ItemTodayOpen",
                        "ItemCur",       "ItemTime",
                        "ItemRiseFall", "ItemRiseFallRate",
                        "ItemTop", "ItemTopRate",
                        "ItemBottom", "ItemBottomRate",
                        "ItemDeal", "ItemDealGold"
                },
                new int[] { R.id.ItemTitle, R.id.ItemName,
                        R.id.ItemYesterClose,   R.id.ItemTodayOpen,
                        R.id.ItemCur,  R.id.ItemTime,
                        R.id.ItemRiseFall, R.id.ItemRiseFallRate,
                        R.id.ItemTop,   R.id.ItemTopRate,
                        R.id.ItemBottom,   R.id.ItemBottomRate,
                        R.id.ItemDeal,   R.id.ItemDealGold
                });

        m_listView.setAdapter(m_listAdapter);

    }


    //---------------menu-------------------
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        menu.add(0, 1, 1, R.string.menu_newStock);
        menu.add(0, 2, 2, R.string.menu_add);
        menu.add(0, 3, 3, R.string.menu_about);
        menu.add(0, 4, 4, R.string.menu_exit);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId() )
        {
            case 1:
                showNewStockDialog();
                break;
            case 2:
                showAddDialog();
                break;
            case 3:
                new AlertDialog.Builder(this).setTitle("About").setMessage("Powered By zog").show();
                break;
            case 4:
                finish();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    //---------------refresh list view-------------------

    Handler handlerSINA = new Handler();
    Runnable runnableSINA = new Runnable() {

        @Override
        public void run() {
            // handler自带方法实现定时器
            try {
                updateListViewSINA(false);
                handlerSINA.postDelayed(this, 9800);

            } catch (Exception e) {
                // TODO Auto-generated catch block
            }
        }
    };
    public void updateListViewSINA(boolean first)
    {
        //-----------show  time-----------
        SimpleDateFormat formatter=new   SimpleDateFormat("MM-dd  HH:mm:ss");
        Date  curDate=new Date(System.currentTimeMillis());//获取当前时间
        String  str=formatter.format(curDate);
        ((TextView)findViewById(R.id.Clock)).setText(str);

        //-------query-----------
        if(first)
        {
            querySINA(false);
        }
        else
        {
            querySINA(true);
        }
    }
    //---------------refresh list view-------------------

    Handler handlerWS = new Handler();
    Runnable runnableWS= new Runnable() {

        @Override
        public void run() {
            // handler自带方法实现定时器
            try {
               // updateListViewWS(false);
               // handlerWS.postDelayed(this, 50000);

            } catch (Exception e) {
                // TODO Auto-generated catch block
            }
        }
    };
    public void updateListViewWS(boolean first)
    {
        //-------query-----------
        if(first)
        {
            queryWallstreetcn_live("global-channel", 30);
            queryWallstreetcn_live("a-stock-channel", 30);
            showWallstreetcn_live(true);
        }
        else
        {
            queryWallstreetcn_live("global-channel", 1);
            queryWallstreetcn_live("a-stock-channel", 1);
            showWallstreetcn_live(true);
        }
    }

    //---------------add stock code dialog-------------------
    protected void showAddDialog()
    {
        LayoutInflater factory = LayoutInflater.from(this);
        final View textEntryView = factory.inflate(R.layout.add_dialog, null);
        final EditText editTextStockCode = (EditText) textEntryView.findViewById(R.id.EditTextStockCode);


        AlertDialog.Builder ad = new AlertDialog.Builder(this);
        ad.setTitle(R.string.title_addStockCode);
        ad.setIcon(android.R.drawable.ic_dialog_alert);
        ad.setView(textEntryView);

        //http://blog.csdn.net/h7870181/article/details/8332991
        // hide input
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(textEntryView.getWindowToken(), 0);

        ad.setPositiveButton(R.string.button_cancel, new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int i)
            {

            }
        });
        ad.setNegativeButton(R.string.button_ok, new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int i)
            {
               String code= editTextStockCode.getText().toString();
                if(code.length()!=6)
                {
                    Toast.makeText(getApplicationContext(),"len!=6", Toast.LENGTH_LONG).show();
                    return;
                }
                m_dbmgr.addStockCode(code);
                updateListViewSINA(true);
            }
        });
        ad.setCancelable(true);
        ad.show();
    }


    //---------------click item -------------------
    private class OnItemClickListenerImpl implements OnItemClickListener
    {
        ImageView dayImage;
        ImageView minImage;
        @SuppressWarnings("unchecked")
        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
        {

            HashMap<String, Object> map = (HashMap<String, Object>) m_listView
                    .getItemAtPosition(arg2);
            String ItemTitle =  (String) map.get("ItemTitle");

            final StockCode code=new StockCode(ItemTitle);

            LayoutInflater factory = LayoutInflater.from(MainActivity.this);
            final View detailView = factory.inflate(R.layout.detail_dialog, null);
            dayImage = (ImageView) detailView.findViewById(R.id.DayImage);
            minImage = (ImageView) detailView.findViewById(R.id.MinImage);

            AlertDialog.Builder ad = new AlertDialog.Builder(MainActivity.this);
            ad.setTitle(ItemTitle);
            ad.setView(detailView);

            querySinaDayImg(ItemTitle);
            querySinaMinImg(ItemTitle);

            ad.setNeutralButton(R.string.button_delete, new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    m_dbmgr.deleteStockCode(code);
                    updateListViewSINA(true);
                }
            });

            ad.setNegativeButton(R.string.button_detail, new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    //todo
                    updateListViewSINA(true);
                }
            });

            ad.setPositiveButton(R.string.button_cancel, new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    // do something
                }
            });

            ad.setCancelable(true);
            ad.show();
        }

        public void querySinaDayImg(String code)
        {
            AsyncHttpClient client = new AsyncHttpClient();

            String url=sinaCodeStr(code);
            if(url.isEmpty())
            {
                return;
            }
            else
            {
                url="http://image.sinajs.cn/newchart/daily/n/"+url+".gif";
            }
            client.setTimeout(9000);
            client.get(url, new AsyncHttpResponseHandler() {
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
                public void onSuccess(int i, Header[] headers, byte[] bytes) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0,bytes.length);
                    dayImage.setImageBitmap(bitmap);
                }

                @Override
                public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
                    Toast.makeText(getApplicationContext(),"SinaDayImg fail", Toast.LENGTH_LONG).show();
                }
            });
        }

        public void querySinaMinImg(String code)
        {
            AsyncHttpClient client = new AsyncHttpClient();

            String url=sinaCodeStr(code);
            if(url.isEmpty())
            {
                return;
            }
            else
            {
                url="http://image.sinajs.cn/newchart/min/n/"+url+".gif";
            }
            client.setTimeout(9000);
            client.get(url, new AsyncHttpResponseHandler() {
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
                public void onSuccess(int i, Header[] headers, byte[] bytes) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0,bytes.length);
                    minImage.setImageBitmap(bitmap);
                }

                @Override
                public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
                    Toast.makeText(getApplicationContext(),"SinaMinImg fail", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    //------------------------------------
    //显示另一个active界面
    //http://blog.csdn.net/lixiang0522/article/details/7824490

    protected void showNewStockDialog()
    {
        Intent intent = new Intent(this, NewStock.class);
        startActivity(intent);
    }

//    public class MainActivity extends Activity {
//        public final static String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";
//……
//    }
//    Intent intent = new Intent(this, DisplayMessageActivity.class);
//    intent.putExtra(EXTRA_MESSAGE, "oldmanstock");
//    startActivity(intent);

    //------------------------------


    //-------------- sina ----------------
    //https://zhidao.baidu.com/question/166686795.html
//    http://hq.sinajs.cn/list=sz399001,sz399002,
//    var hq_str_sz399001="深证成指,11079.642,11040.450,11133.801,11141.028,11072.868,0.000,0.000,7797084012,103355135553.711,0,0.000,0,0.000,0,0.000,0,0.000,0,0.000,0,0.000,0,0.000,0,0.000,0,0.000,0,0.000,2018-01-02,10:55:21,00";
//    var hq_str_sz399002="深成指R,13235.955,13189.137,13300.655,13309.289,13227.863,0.000,0.000,4771427339,64046488910.660,0,0.000,0,0.000,0,0.000,0,0.000,0,0.000,0,0.000,0,0.000,0,0.000,0,0.000,0,0.000,2018-01-02,10:55:21,00";


    String sinaCodeStr(String code)
    {
        if(!code.isEmpty())
        {
            if(code.charAt(0)=='0')
            {
                if(Integer.parseInt( code)>200)
                {
                    return "sz"+code;
                }
                else
                {
                    return "sh"+code;
                }
            }
            else if(code.charAt(0)=='3')
            {
                return "sz"+code ;
            }
            else if(code.charAt(0)=='6')
            {
                return "sh"+code;
            }
            else
            {
                return  "";
            }
        }
        else
            return "";
    }

    public void querySINA(boolean checkTime)
    {
        //---------- time limit-------------
        long time=System.currentTimeMillis();
        Calendar mCalendar= Calendar.getInstance();
        mCalendar.setTimeInMillis(time);
        double hour = mCalendar.get(Calendar.HOUR_OF_DAY)+ mCalendar.get(Calendar.MINUTE)/60.0;

        if(checkTime)
        {
            if((hour>8.9 && hour<11.6)
                    ||(hour>12.9 && hour <15.1))
            {
                //nothing
            }
            else
            {
                updateStockLast();
                return;
            }
        }

        //-----------------------
        List<StockCode> codeList = m_dbmgr.queryStockCode();
        if(codeList.isEmpty())
            return;

        String codeStr="";
        for(StockCode code:codeList)
        {
            String s=sinaCodeStr(code._code);
            if(s.isEmpty())
            {
                Toast.makeText(getApplicationContext(),code._code+" unkown sh sz", Toast.LENGTH_LONG).show();
            }
            else
            {
                codeStr+=s+",";
            }
        }

        //-----------------------
        AsyncHttpClient client = new AsyncHttpClient();
        client.setTimeout(9000);
        client.addHeader("Referer","finance.sina.com.cn");
        client.get(this,"http://hq.sinajs.cn/list="+codeStr, new AsyncHttpResponseHandler() {
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
            public void onSuccess(int i, Header[] headers, byte[] bytes) {
                //Toast.makeText(getApplicationContext(),"sinajs ok", Toast.LENGTH_LONG).show();
                parseSINA(bytes);
                // Toast.makeText(getApplicationContext(),""+datalist.size(), Toast.LENGTH_LONG).show();
                updateStockLast();
            }

            @Override
            public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
                updateStockLast();
                Toast.makeText(getApplicationContext(),"sinajs fail", Toast.LENGTH_LONG).show();
            }
        });

    }

    public void parseSINA(byte[] bytes)
    {
        String html = new String(bytes, Charset.forName("GBK"));
        if(html.isEmpty())
            return ;
        if(html.contains("FAILED"))
            return ;

        ArrayList<StockData> dataList=new  ArrayList<StockData>();

        String[] strList=html.split(";");
        for(String str:strList)
        {
            str=str.trim(); // cut enter
            if(str.startsWith("var hq_str_"))
            {
                StockData data=new StockData();
                str=str.replace("var hq_str_","");

                data._code=str.substring(2,8);

                if(str.length()<20)
                {
                    //删去
                    StockCode t= new StockCode(data._code);
                    m_dbmgr.deleteStockCode(t);
                    continue;
                }

                int i=0;
                i=str.indexOf("\"");
                str=str.substring(i+1);
                i= str.indexOf("\"");
                str=str.substring(0,i);

                String[] sList=str.split(",");

                if(sList.length>=32)
                {
                    data._name=sList[0];
                    data._todayOpen=Double.valueOf(sList[1]);
                    data._yesterdayClose=Double.valueOf(sList[2]);
                    data._cur=Double.valueOf(sList[3]);
                    data._top=Double.valueOf(sList[4]);
                    data._bottom=Double.valueOf(sList[5]);

                    data._deal=Long.valueOf(sList[8]);
                    data._dealGold=Double.valueOf(sList[9]);


                    data._buy[0]._deal =  Long.valueOf(sList[10]);
                    data._buy[0]._price=Double.valueOf(sList[11]);

                    data._buy[1]._deal =  Long.valueOf(sList[12]);
                    data._buy[1]._price=Double.valueOf(sList[13]);

                    data._buy[2]._deal =  Long.valueOf(sList[14]);
                    data._buy[2]._price = Double.valueOf(sList[15]);

                    data._buy[3]._deal =  Long.valueOf(sList[16]);
                    data._buy[3]._price = Double.valueOf(sList[17]);

                    data._buy[4]._deal =  Long.valueOf(sList[18]);
                    data._buy[4]._price = Double.valueOf(sList[19]);

                    data._sell[0]._deal =Long.valueOf(sList[20]);
                    data._sell[0]._price =Double.valueOf(sList[21]);

                    data._sell[1]._deal =Long.valueOf(sList[22]);
                    data._sell[1]._price =Double.valueOf(sList[23]);

                    data._sell[2]._deal =Long.valueOf(sList[24]);
                    data._sell[2]._price =Double.valueOf(sList[25]);

                    data._sell[3]._deal =Long.valueOf(sList[26]);
                    data._sell[3]._price =Double.valueOf(sList[27]);

                    data._sell[4]._deal =Long.valueOf(sList[28]);
                    data._sell[4]._price =Double.valueOf(sList[29]);

                    //
                    str =  sList[30]+" "+sList[31];
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date date = null;
                    try {
                        date = format.parse(str);
                    } catch (ParseException e) {
                        continue;
                    }
                    data._time_sec= date.getTime();

                    // long time=System.currentTimeMillis();
                    //Toast.makeText(getApplicationContext(),""+ data._time_sec+":"+time, Toast.LENGTH_LONG).show();

                    //加入数据库
                    dataList.add(data);
                }
            }
        }
        //加入数据库
        m_dbmgr.addStockDataList(dataList);

    }

    //------------Wallstreetcn live------------------
    //  a-stock-channel
    //https://api-prod.wallstreetcn.com/apiv1/content/lives?channel=global-channel&client=pc&cursor=1515049304&limit=20
    //https://api-prod.wallstreetcn.com/apiv1/content/lives/pc 全部
    //https://api-prod.wallstreetcn.com/apiv1/content/lives?channel=a-stock-channel
    public void queryWallstreetcn_live(String channel,int limit)
    {
        AsyncHttpClient client = new AsyncHttpClient();
        client.setTimeout(9000);

        String url;
        if(limit>0)
        {
            url="https://api-prod.wallstreetcn.com/apiv1/content/lives?channel="+channel+"&limit="+limit;
        }
        else
        {
            url="https://api-prod.wallstreetcn.com/apiv1/content/lives?channel="+channel;
        }

        client.get(this,url, new AsyncHttpResponseHandler() {
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
            public void onSuccess(int i, Header[] headers, byte[] bytes) {
                //Toast.makeText(getApplicationContext(),"Wallstreetcn_live ok", Toast.LENGTH_LONG).show();
                parseWallstreetcn_live(bytes);
                // Toast.makeText(getApplicationContext(),""+datalist.size(), Toast.LENGTH_LONG).show();

            }

            @Override
            public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {

                Toast.makeText(getApplicationContext(),"Wallstreetcn_live fail", Toast.LENGTH_LONG).show();
            }
        });

    }

    public void parseWallstreetcn_live(byte[] bytes) {
        String html = new String(bytes, Charset.forName("UTF-8"));
        if (html.isEmpty())
            return;

        try {
            JSONObject  jsonParser  = new JSONObject (html);
            long code=jsonParser.getLong("code");
            String msg=jsonParser.getString("message");
            if(20000==code
                    && msg.equalsIgnoreCase("OK"))
            {
                ArrayList<NewsData> dataList=new ArrayList<NewsData>();
                JSONArray items= jsonParser.getJSONObject("data").getJSONArray("items");

                for(int i = 0; i < items.length() ; i++)
                {
                    JSONObject item = (JSONObject)items.opt(i);

                    NewsData data=new NewsData();
                    data._content_text = item.getString("content_text").trim();
                    data._id= item.getLong("id");
                    data._time_sec = item.getLong("display_time")*1000;
                    //加数据库
                    dataList.add(data);

                }
                //加数据库
                m_dbmgr.addNewsList(dataList);
            }

        } catch (JSONException e)
        {
            Toast.makeText(getApplicationContext(),"Wallstreetcn_live JSON fail", Toast.LENGTH_LONG).show();
        }
    }

    void showWallstreetcn_live(boolean getNoPlay)
    {
        if(SpeechPlaying)
            return;

        List<NewsData> dataList = m_dbmgr.queryNews(getNoPlay);
        for(NewsData data:dataList)
        {
            //show
            //SimpleDateFormat formatter=new   SimpleDateFormat("[yyyy-MM-dd  HH:mm]");
            SimpleDateFormat formatter=new   SimpleDateFormat("[HH:mm]");
            Date  curDate=new Date(data._time_sec);//获取当前时间
            String  timeStr=formatter.format(curDate);

          //  Toast.makeText(getApplicationContext(), timeStr+"\n"+data._content_text , Toast.LENGTH_LONG).show();

            playText( timeStr+"\n"+data._content_text);
            m_dbmgr.updateNews(data);
        }

    }

    //-------------------------------Tts----------
    // 语音合成对象
    private SpeechSynthesizer mTts=null;
    private boolean canSpeech=false;
    // 缓冲进度
    private int mPercentForBuffering = 0;
    // 播放进度
    private int mPercentForPlaying = 0;
    //播放状态
    boolean SpeechPlaying=false;

    // 默认发音人
    private String voicer = "xiaoyan";

    // 引擎类型
    private String mEngineType = SpeechConstant.TYPE_CLOUD;

    private void InitTts()
    {
        // 注意：此接口在非主进程调用会返回null对象，如需在非主进程使用语音功能，请增加参数：SpeechConstant.FORCE_LOGIN+"=true"
        // 参数间使用“,”分隔。
        // 设置你申请的应用appid
        StringBuffer param = new StringBuffer();
        param.append("appid=5a52d6ac");
        param.append(",");
        param.append(SpeechConstant.ENGINE_MODE + "=" + SpeechConstant.MODE_MSC);
        // param.append(",");
        // param.append(SpeechConstant.FORCE_LOGIN + "=true");
        SpeechUtility.createUtility(this, param.toString());

        // 初始化合成对象
        mTts = SpeechSynthesizer.createSynthesizer(this, mTtsInitListener);
    }

    private void playText(String text)
    {
        if(mTts!=null && canSpeech)
        {
            int code = mTts.startSpeaking(text, mTtsListener);
            if (code != ErrorCode.SUCCESS) {
                showTip("语音合成失败,错误码: " + code);
            }
        }
        else
        {
            showTip("无法语音合成 appid是否过期");
        }

    }
    /**
     * 初始化监听。
     */
    private InitListener mTtsInitListener = new InitListener() {
        @Override
        public void onInit(int code) {

            if (code != ErrorCode.SUCCESS) {
                showTip("初始化失败,错误码："+code);
            } else {
                // 初始化成功，之后可以调用startSpeaking方法
                // 注：有的开发者在onCreate方法中创建完合成对象之后马上就调用startSpeaking进行合成，
                // 正确的做法是将onCreate中的startSpeaking调用移至这里
                canSpeech=true;
                setParam();
            }
        }
    };

    /**
     * 合成回调监听。
     */
    private SynthesizerListener mTtsListener = new SynthesizerListener() {

        @Override
        public void onSpeakBegin()
        {
            SpeechPlaying=true;
            showTip("开始播放");
        }

        @Override
        public void onSpeakPaused()
        {
            SpeechPlaying=false;
            showTip("暂停播放");
        }

        @Override
        public void onSpeakResumed()
        {
            SpeechPlaying=true;
            showTip("继续播放");
        }

        @Override
        public void onBufferProgress(int percent, int beginPos, int endPos,
                                     String info) {
            // 合成进度
            mPercentForBuffering = percent;
            showTip(String.format("%d,%d",
                    mPercentForBuffering, mPercentForPlaying));
        }

        @Override
        public void onSpeakProgress(int percent, int beginPos, int endPos) {
            // 播放进度
            mPercentForPlaying = percent;
            showTip(String.format("%d,%d",
                    mPercentForBuffering, mPercentForPlaying));
        }

        @Override
        public void onCompleted(SpeechError error) {
            SpeechPlaying=false;
            if (error == null) {
                showTip("播放完成");
            } else if (error != null) {
                showTip(error.getPlainDescription(true));
            }
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            // 若使用本地能力，会话id为null
            //	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
            //		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
            //		Log.d(TAG, "session id =" + sid);
            //	}
        }
    };
    private void showTip(final String str) {

       // Toast.makeText(this,str,Toast.LENGTH_LONG).show();
    }
    /**
     * 参数设置
     * @return
     */
    private void setParam(){
        // 清空参数
        mTts.setParameter(SpeechConstant.PARAMS, null);
        // 根据合成引擎设置相应参数
        if(mEngineType.equals(SpeechConstant.TYPE_CLOUD)) {
            mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
            // 设置在线合成发音人
            mTts.setParameter(SpeechConstant.VOICE_NAME, voicer);
            //设置合成语速
            mTts.setParameter(SpeechConstant.SPEED, "50");
            //设置合成音调
            mTts.setParameter(SpeechConstant.PITCH, "50");
            //设置合成音量
            mTts.setParameter(SpeechConstant.VOLUME, "100");
        }else {
            mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL);
            // 设置本地合成发音人 voicer为空，默认通过语记界面指定发音人。
            mTts.setParameter(SpeechConstant.VOICE_NAME, "");
            /**
             * TODO 本地合成不设置语速、音调、音量，默认使用语记设置
             * 开发者如需自定义参数，请参考在线合成参数设置
             */
        }
        //设置播放器音频流类型
        mTts.setParameter(SpeechConstant.STREAM_TYPE, "3");
        // 设置播放合成音频打断音乐播放，默认为true
        mTts.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "true");

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        mTts.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH, Environment.getExternalStorageDirectory()+"/msc/tts.wav");
    }


    //----------------------------
    //http://blog.csdn.net/shulianghan/article/details/19698511

    //屏幕的宽高, 单位像素
    private int screenWidth;
    private int screenHeight;

    //屏幕的密度
    private float density;  //只有四种情况 : 0.75/ 1.0/ 1.5/ 2.0
    private int densityDpi; //只有四种情况 : 120/ 160/ 240/ 320

    //水平垂直精确密度
    private float xdpi; //水平方向上的准确密度, 即每英寸的像素点
    private float ydpi; //垂直方向上的准确密度, 即没音村的像素点

    private void getPixelDisplayMetricsII() {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels;

        density = dm.density;
        densityDpi = dm.densityDpi;

        xdpi = dm.xdpi;
        ydpi = dm.ydpi;

        float aa=        px2dip_w(1280);
        Toast.makeText(getApplicationContext(), ""+density+" "+densityDpi+" "+xdpi+" "+ydpi+" "+aa , Toast.LENGTH_LONG).show();

    }

    float dip2px_w(float dip)
    {
        float px=0;
        px = dip * density*xdpi / densityDpi;
        return px;
    }

    float dip2px_h(float dip)
    {
        float px=0;
        px = dip * density*ydpi / densityDpi;
        return px;
    }

    float px2dip_w(float px)
    {
        float dip=0;
        dip = px * densityDpi/(density*xdpi);
        return dip;
    }

    float px2dip_h(float px)
    {
        float dip=0;
        dip = px * densityDpi/(density*ydpi);
        return dip;
    }
    //----------------------------
}
