package com.example.admin.gpxbymodule;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

public class HomeAdapter extends BaseAdapter {
    private ArrayList<HomeList> listData;
    private LayoutInflater layoutInflater;

    public HomeAdapter(Context aContext, ArrayList<HomeList> listData) {
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

    public View getView(final int position, View convertView, final ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.single_grid, null);
            holder = new ViewHolder();
            holder.image = (ImageButton) convertView.findViewById(R.id.gridimage);
            holder.text = (TextView) convertView.findViewById(R.id.gridtext);
            holder.frame = (FrameLayout)convertView.findViewById(R.id.gridframe);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        byte[] image = listData.get(position).getTopitem();

        Bitmap bm = BitmapFactory.decodeByteArray(image,0,image.length);
        holder.image.setImageBitmap(bm);
        holder.image.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ((GridView) parent).performItemClick(v, position, 0);
                // Let the event be handled in onItemClick()
            }
        });

        holder.text.setText(listData.get(position).getSubitem());
        holder.text.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ((GridView) parent).performItemClick(v, position, 0);
                // Let the event be handled in onItemClick()
            }
        });

        holder.frame.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ((GridView) parent).performItemClick(v, position, 0);
                // Let the event be handled in onItemClick()
            }
        });

        return convertView;
    }

    static class ViewHolder {
        ImageButton image;
        TextView text;
        FrameLayout frame;
    }
}
