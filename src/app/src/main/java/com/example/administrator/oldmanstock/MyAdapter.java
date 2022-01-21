package com.example.administrator.oldmanstock;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Administrator on 2018/01/03.
 */
//http://www.cnblogs.com/ityizhainan/p/5976845.html

public class MyAdapter extends SimpleAdapter {

    Context context;
    ArrayList<Map<String, Object>> data;
    int resource;
    String[] from;
    int[] to;

    public MyAdapter(Context context,
                     ArrayList<Map<String, Object>> data, int resource,
                     String[] from, int[] to) {

        super(context, data, resource, from, to);
        this.context = context;
        this.data = data;
        this.resource = resource;
        this.from = from;
        this.to = to;
        // TODO Auto-generated constructor stub
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        // TODO Auto-generated method stub
        if(convertView==null){
            LayoutInflater inflater= LayoutInflater.from(context);
            convertView=inflater.inflate(R.layout.list_item, null);
        }

        TextView textTitle=(TextView)convertView.findViewById(R.id.ItemTitle);
        TextView textName=(TextView)convertView.findViewById(R.id.ItemName);
        TextView textYesterClose=(TextView)convertView.findViewById(R.id.ItemYesterClose);
        TextView textTodayOpen=(TextView)convertView.findViewById(R.id.ItemTodayOpen);

        TextView textCur=(TextView)convertView.findViewById(R.id.ItemCur);
        TextView textTime=(TextView)convertView.findViewById(R.id.ItemTime);
        TextView textRiseFall=(TextView)convertView.findViewById(R.id.ItemRiseFall);
        TextView textRiseFallRate=(TextView)convertView.findViewById(R.id.ItemRiseFallRate);
        TextView textTop=(TextView)convertView.findViewById(R.id.ItemTop);
        TextView textTopRate=(TextView)convertView.findViewById(R.id.ItemTopRate);
        TextView textBottom=(TextView)convertView.findViewById(R.id.ItemBottom);
        TextView textBottomRate=(TextView)convertView.findViewById(R.id.ItemBottomRate);
        TextView textDeal=(TextView)convertView.findViewById(R.id.ItemDeal);
        TextView textDealGold=(TextView)convertView.findViewById(R.id.ItemDealGold);

        //-------------set text--------
        textTitle.setText(data.get(position).get("ItemTitle").toString());
        textName.setText(data.get(position).get("ItemName").toString());
        textYesterClose.setText(data.get(position).get("ItemYesterClose").toString());
        textTodayOpen.setText(data.get(position).get("ItemTodayOpen").toString());

        String str=data.get(position).get("ItemCur").toString();
        textCur.setText(str);
        if(str.length()>6)
            textCur.setTextSize(30);
        else    if(str.length()>5)
            textCur.setTextSize(40);
        else
            textCur.setTextSize(50);

        textTime.setText(data.get(position).get("ItemTime").toString());

        str=data.get(position).get("ItemRiseFall").toString();
        textRiseFall.setText(str);

        if(str.length()>6)
            textRiseFall.setTextSize(30);
        else    if(str.length()>5)
            textRiseFall.setTextSize(40);
        else
            textRiseFall.setTextSize(50);

        textRiseFallRate.setText(data.get(position).get("ItemRiseFallRate").toString());

        str=data.get(position).get("ItemTop").toString();
        textTop.setText(str);

        if(str.length()>6)
            textTop.setTextSize(30);
        else    if(str.length()>5)
            textTop.setTextSize(40);
        else
            textTop.setTextSize(50);

        textTopRate.setText(data.get(position).get("ItemTopRate").toString());

        str=data.get(position).get("ItemBottom").toString();
        textBottom.setText(str);

        if(str.length()>6)
            textBottom.setTextSize(30);
        else    if(str.length()>5)
            textBottom.setTextSize(40);
        else
            textBottom.setTextSize(50);

        textBottomRate.setText(data.get(position).get("ItemBottomRate").toString());
        textDeal.setText(data.get(position).get("ItemDeal").toString());
        textDealGold.setText(data.get(position).get("ItemDealGold").toString());


        //-------------set color--------
        if (textRiseFall.getText().toString().contains("-"))
        {
            //green
           textCur.setTextColor(context.getResources().getColor(R.color.stock_green));
            textRiseFall.setTextColor(context.getResources().getColor(R.color.stock_green));
            textRiseFallRate.setTextColor(context.getResources().getColor(R.color.stock_lowGreen));
        }

        if (!textRiseFall.getText().toString().contains("-"))
        {
            //red
              textCur.setTextColor(context.getResources().getColor(R.color.stock_red));
            textRiseFall.setTextColor(context.getResources().getColor(R.color.stock_red));
            textRiseFallRate.setTextColor(context.getResources().getColor(R.color.stock_yellow));
        }

        if (textTopRate.getText().toString().contains("-"))
        {
            //green
              textTop.setTextColor(context.getResources().getColor(R.color.stock_green));
            textTopRate.setTextColor(context.getResources().getColor(R.color.stock_lowGreen));
        }

        if (!textTopRate.getText().toString().contains("-"))
        {
            //red
              textTop.setTextColor(context.getResources().getColor(R.color.stock_red));
            textTopRate.setTextColor(context.getResources().getColor(R.color.stock_yellow));
        }

        if (textBottomRate.getText().toString().contains("-"))
        {
            //green
              textBottom.setTextColor(context.getResources().getColor(R.color.stock_green));
            textBottomRate.setTextColor(context.getResources().getColor(R.color.stock_lowGreen));
        }

        if (!textBottomRate.getText().toString().contains("-"))
        {
            //red
              textBottom.setTextColor(context.getResources().getColor(R.color.stock_red));
            textBottomRate.setTextColor(context.getResources().getColor(R.color.stock_yellow));
        }

        return convertView;
    }


}
