package com.example.admin.gpxbymodule;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class SingleItem extends BaseAdapter {
    private ArrayList<ListItem> listData;
    private LayoutInflater layoutInflater;



    public SingleItem(Context aContext, ArrayList<ListItem> listData) {
        this.listData = listData;
        layoutInflater = LayoutInflater.from(aContext);
    }

    @Override
    public int getCount() {
        return listData.size();
    }

    @Override
    public Object getItem(int position) {
        return listData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.singleitemlist, null);
            holder = new ViewHolder();
            holder.content = (TextView) convertView.findViewById(R.id.datacontent);
            holder.idhold = (TextView) convertView.findViewById(R.id.dataid);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.idhold.setText(listData.get(position).getId());
        holder.content.setText(listData.get(position).getTopitem());
        return convertView;
    }

    static class ViewHolder {
        TextView idhold;
        TextView content;
    }

}
