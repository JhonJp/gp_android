package com.example.admin.gpxbymodule;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;

public class Partner_deliveryCustomAdapter extends BaseAdapter implements Filterable {
    private ArrayList<ThreeWayHolder> listData;
    private LayoutInflater layoutInflater;
    private RatesDB rate;

    public Partner_deliveryCustomAdapter(Context aContext, ArrayList<ThreeWayHolder> listData) {
        this.listData = listData;
        layoutInflater = LayoutInflater.from(aContext);
        this.rate = new RatesDB(aContext);
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
            convertView = layoutInflater.inflate(R.layout.threeway, null);
            holder = new ViewHolder();
            holder.headlineView = (TextView) convertView.findViewById(R.id.c_account);
            holder.dataid = (TextView) convertView.findViewById(R.id.dataid);
            holder.reporterNameView = (TextView) convertView.findViewById(R.id.c_name);
            holder.quant = (TextView) convertView.findViewById(R.id.quantity);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.dataid.setText(listData.get(position).getId());
        holder.headlineView.setText(listData.get(position).getTopitem());
        holder.reporterNameView.setText(Html.fromHtml("<small>"
                +listData.get(position).getSubitem()
                +"</small>"));
        String stat = listData.get(position).getQuantity();
        if (stat.equals("Returned")){
            holder.quant.setBackgroundResource(R.drawable.pending);
        }
        holder.quant.setText(listData.get(position).getQuantity());
        return convertView;
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint,FilterResults results) {
                listData = (ArrayList<ThreeWayHolder>) results.values; // has the filtered values
                notifyDataSetChanged();  // notifies the data with new filtered values
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                ArrayList<ThreeWayHolder> FilteredArrList = new ArrayList<ThreeWayHolder>();

                if (listData == null) {
                    listData = new ArrayList<ThreeWayHolder>(listData);
                }
                if (constraint == null || constraint.length() == 0) {
                    // set the Original result to return
                    results.count = listData.size();
                    results.values = listData;
                } else {
                    constraint = constraint.toString().toLowerCase();
                    for (int i = 0; i < listData.size(); i++) {
                        String data = listData.get(i).getTopitem();
                        String sub = listData.get(i).getSubitem();
                        if (data.toLowerCase().startsWith(constraint.toString())
                                || sub.toLowerCase().startsWith(constraint.toString())) {
                            FilteredArrList.add(new ThreeWayHolder(listData.get(i).getId(),
                                    listData.get(i).getTopitem(), listData.get(i).getSubitem(),
                                    listData.get(i).getQuantity()));
                        }
                    }
                    // set the Filtered result to return
                    results.count = FilteredArrList.size();
                    results.values = FilteredArrList;
                }
                return results;
            }
        };
        return filter;
    }

    static class ViewHolder {
        TextView headlineView;
        TextView dataid;
        TextView reporterNameView;
        TextView quant;
    }

}
