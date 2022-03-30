package com.example.bluetooth;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.ArrayList;

public class MyAdapter extends BaseAdapter {
    Context context;
    int layoutId;
    ArrayList<MyData> myDataArr;
    LayoutInflater Inflater;

    MyAdapter(Context _context, int _layoutId, ArrayList<MyData> _myDataArr){
        context = _context;
        layoutId = _layoutId;
        myDataArr = _myDataArr;
        Inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return myDataArr.size();
    }

    @Override
    public String getItem(int position) {
        return myDataArr.get(position).menu;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final int pos = position;
        if (convertView == null)  {
            convertView = Inflater.inflate(layoutId, parent, false);
        }

        TextView nameTv = (TextView)convertView.findViewById(R.id.menu);
        nameTv.setText(myDataArr.get(position).menu);

        TextView phoneTv = (TextView)convertView.findViewById(R.id.amount);
        phoneTv.setText(myDataArr.get(position).amount);

        return convertView;
    }
}