package com.example.admin.gpxbymodule;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;

public class CustomChecks extends BaseAdapter{
    private ArrayList<LinearItem> listData;
    private LayoutInflater layoutInflater;

    public CustomChecks(Context aContext, ArrayList<LinearItem> listData) {
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
        final ViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.customcheckbox, null);
            holder = new ViewHolder();
            holder.headlineView = (TextView) convertView.findViewById(R.id.c_account);
            holder.check = (CheckBox) convertView.findViewById(R.id.doneCheckBox);
            holder.dataid = (TextView) convertView.findViewById(R.id.dataid);
            holder.reporterNameView = (TextView) convertView.findViewById(R.id.c_name);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.dataid.setText(listData.get(position).getId());
        holder.headlineView.setText(
                Html.fromHtml("<small>"
                        +listData.get(position).getTopitem()
                        +"</small>"));
        holder.reporterNameView.setText(
                Html.fromHtml("<small>"
                        +listData.get(position).getSubitem()
                        +"</small>"));
        return convertView;
    }

    static class ViewHolder {
        TextView headlineView;
        TextView dataid;
        CheckBox check;
        TextView reporterNameView;
    }

}
