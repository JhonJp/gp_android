package com.example.admin.gpxbymodule;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class NavAdapter extends BaseAdapter {
    private ArrayList<HomeList> listData;
    private LayoutInflater layoutInflater;

    public NavAdapter(Context aContext, ArrayList<HomeList> listData) {
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
            convertView = layoutInflater.inflate(R.layout.navlist, null);
            holder = new ViewHolder();
            holder.top = (ImageView) convertView.findViewById(R.id.topitem);
            holder.reporterNameView = (TextView) convertView.findViewById(R.id.subitem);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        byte[] image = listData.get(position).getTopitem();

        Bitmap bm = BitmapFactory.decodeByteArray(image,0,image.length);

        holder.top.setImageBitmap(bm);
        holder.reporterNameView.setText(listData.get(position).getSubitem());
        return convertView;
    }

    static class ViewHolder {
        ImageView top;
        TextView reporterNameView;
        TextView reportedDateView;
    }
}
