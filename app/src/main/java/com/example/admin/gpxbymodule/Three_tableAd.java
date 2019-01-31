package com.example.admin.gpxbymodule;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class Three_tableAd extends BaseAdapter {
    private ArrayList<ListItem> listData;
    private LayoutInflater layoutInflater;

    public Three_tableAd(Context aContext, ArrayList<ListItem> listData) {
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
            convertView = layoutInflater.inflate(R.layout.three_table, null);
            holder = new ViewHolder();
            holder.amount = (TextView) convertView.findViewById(R.id.getamount);
            holder.headlineView = (TextView) convertView.findViewById(R.id.topitem);
            holder.idhold = (TextView) convertView.findViewById(R.id.idget);
            holder.reporterNameView = (TextView) convertView.findViewById(R.id.subitem);
            holder.pr = (TextView) convertView.findViewById(R.id.price);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.idhold.setText(listData.get(position).getId());
        holder.headlineView.setText(Html.fromHtml("<small>"
                +listData.get(position).getTopitem()
                +"</small>"));
        holder.reporterNameView.setText(
                Html.fromHtml("<small>"
                        +listData.get(position).getSubitem()
                        +"</small>"));
        holder.pr.setText(
                Html.fromHtml("<small>"
                        +listData.get(position).getAmount()
                        +"</small>"));

        return convertView;
    }

    static class ViewHolder {
        TextView idhold;
        TextView headlineView;
        TextView reporterNameView;
        TextView amount;
        TextView pr;
    }

}
