package com.example.admin.gpxbymodule;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class ListAdapter extends BaseAdapter {
    private ArrayList<ListItem> listData;
    private LayoutInflater layoutInflater;



    public ListAdapter(Context aContext, ArrayList<ListItem> listData) {
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
            convertView = layoutInflater.inflate(R.layout.customlist, null);
            holder = new ViewHolder();
            holder.amount = (TextView) convertView.findViewById(R.id.getamount);
            holder.headlineView = (TextView) convertView.findViewById(R.id.topitem);
            holder.idhold = (TextView) convertView.findViewById(R.id.idget);
            holder.reporterNameView = (TextView) convertView.findViewById(R.id.subitem);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.idhold.setText(listData.get(position).getId());
        holder.headlineView.setText(Html.fromHtml("<small>"
                +listData.get(position).getTopitem()
                +"</small>"));
        holder.reporterNameView.setText(Html.fromHtml("<small>"
                +listData.get(position).getSubitem()
                +"</small>"));
        return convertView;
    }

    static class ViewHolder {
        TextView idhold;
        TextView headlineView;
        TextView reporterNameView;
        TextView amount;
    }

}
