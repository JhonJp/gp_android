package com.example.admin.gpxbymodule;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class Partner_deliveryOthers extends Fragment {

    GenDatabase gen;
    RatesDB rate;
    HomeDatabase helper;
    String value;
    ListView lv;
    AutoCompleteTextView search;
    Partner_deliveryCustomAdapter adapter;
    String yourname;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.deliverypending, null);
        gen = new GenDatabase(getContext());
        rate = new RatesDB(getContext());
        helper = new HomeDatabase(getContext());
        lv = (ListView)view.findViewById(R.id.lv);
        search = (AutoCompleteTextView)view.findViewById(R.id.searchableinput);
        search.setSelected(false);
        if (helper.getFullname(helper.logcount()+"").contains("  ")){
            yourname = helper.getFullname(helper.logcount()+"").replace("  ", " ");
        }
        try {
            search.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    //customtype();
                    Log.e("text watch","before change");
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    //adapter.getFilter().filter(s.toString());
                    Log.e("text watch","on change");
                }

                @Override
                public void afterTextChanged(Editable s) {
                    Log.e("text watch","after change");
                }
            });
        }catch (Exception e){}

        scroll();
        customtype();
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle("Returned delivery");
        setHasOptionsMenu(false);
    }

    public void scroll(){
        try {
            lv.setOnTouchListener(new View.OnTouchListener() {
                // Setting on Touch Listener for handling the touch inside ScrollView
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    // Disallow the touch request for parent scroll on touch of child view
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    return false;
                }
            });
        }catch (Exception e){}
    }

    public void customtype(){
        try{
            final ArrayList<ThreeWayHolder> result = getOthers(helper.logcount()+"");
            adapter = new Partner_deliveryCustomAdapter(getContext(), result);
            lv.setAdapter(adapter);
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    TextView top = (TextView)view.findViewById(R.id.dataid);
                    TextView sub = (TextView)view.findViewById(R.id.c_name);
                    TextView name = (TextView)view.findViewById(R.id.c_account);
                    String booknumber = top.getText().toString();
                    String bn = name.getText().toString();
                    Intent i = new Intent(getContext(), Deliverycont.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("bookingnumber", booknumber);
                    bundle.putString("accountnumber", getAccntNoNumber(bn));
                    bundle.putString("status", "1");
                    //Add the bundle to the intent
                    i.putExtras(bundle);
                    startActivity(i);
                    getActivity().finish();
                }
            });

        }catch (Exception e){}
    }

    //get unloaded boxes
    public ArrayList<String> getUnloadedBoxBooking(){
        ArrayList<String> boxnum = new ArrayList<>();
        SQLiteDatabase db = gen.getReadableDatabase();
        Cursor x = db.rawQuery(" SELECT * FROM "+gen.tb_unloadbox, null);
        x.moveToFirst();
        while (!x.isAfterLast()){
            boxnum.add(x.getString(x.getColumnIndex(gen.unload_boxnum)));
            x.moveToNext();
        }
        x.close();
        return boxnum;
    }

    //compare boxnumber to booking, return booking number
    public ArrayList<String> getBookingInfo(ArrayList<String> boxnum){
        ArrayList<String> bookingnum = new ArrayList<>();
        for (String item : boxnum ) {
            SQLiteDatabase db = gen.getReadableDatabase();
            Cursor x = db.rawQuery(" SELECT * FROM " + gen.tbname_booking_consignee_box
                    + " WHERE " + gen.book_con_box_number + " = '" + item + "'", null);
            x.moveToFirst();
            while (!x.isAfterLast()) {
                String y = x.getString(x.getColumnIndex(gen.book_con_transaction_no));
                if (!bookingnum.contains(y)) {
                    bookingnum.add(y);
                }
                x.moveToNext();
            }
            x.close();
        }
        return bookingnum;
    }

    public String getFullnameCustomer(String acc){
        String fullname = null;
        SQLiteDatabase db = gen.getReadableDatabase();
        Cursor v = db.rawQuery(" SELECT * FROM " + gen.tbname_customers +
                " WHERE "+gen.cust_accountnumber+ " = '"+acc+"'", null);
        if(v.moveToNext()) {
            fullname = v.getString(v.getColumnIndex(gen.cust_fullname));
        }
        return fullname;
    }

    //threeway adapter working on pending
    public ArrayList<ThreeWayHolder> getOthers(String by){
        ArrayList<ThreeWayHolder> results = new ArrayList<>();
        SQLiteDatabase db = gen.getReadableDatabase();
        String x = " SELECT * FROM " + gen.tbname_delivery
                +" LEFT JOIN "+gen.tbname_delivery_box
                +" ON "+gen.tbname_delivery+"."+gen.del_id+" = "
                +gen.tbname_delivery_box+"."+gen.del_box_deliveryid
                +" WHERE "+gen.tbname_delivery+"."+gen.del_createdby+" = '"+by+"' AND "
                +gen.tbname_delivery_box+"."+gen.del_box_status+" = '2'"
                +" GROUP BY "+gen.tbname_delivery+"."+gen.del_id;
        Cursor res = db.rawQuery(x, null);
        res.moveToFirst();
        while (!res.isAfterLast()){
            String name = "";
            String id = res.getString(res.getColumnIndex(gen.del_box_deliveryid));
            String booknum = res.getString(res.getColumnIndex(gen.del_booking_no));
            String topitem = res.getString(res.getColumnIndex(gen.del_customer));
            String subitem = res.getString(res.getColumnIndex(gen.del_box_receiver));
            String date = res.getString(res.getColumnIndex(gen.del_createddate));
            String stat = res.getString(res.getColumnIndex(gen.del_box_status));
            ThreeWayHolder list = new ThreeWayHolder(booknum, getFullnameCustomer(subitem),
                    date, "Returned");
            results.add(list);

            res.moveToNext();
        }
        // Add some more dummy data for testing
        return results;
    }

    //get boxes
    public ArrayList<String> getDirectBoxes(String id){
        ArrayList<String> boxnum = new ArrayList<>();
        SQLiteDatabase db = rate.getReadableDatabase();
        Cursor x = db.rawQuery(" SELECT * FROM "+rate.tbname_part_distribution
                +" LEFT JOIN "+rate.tbname_part_distribution_box
                +" ON "+rate.tbname_part_distribution+"."+rate.partdist_transactionnumber
                +" = "+rate.tbname_part_distribution_box+"."+rate.partdist_box_distributionid
                +" WHERE "+rate.tbname_part_distribution+"."+rate.partdist_type+" = 'Direct' AND "
                +rate.partdist_drivername +" = '"+id+"'", null);
        x.moveToFirst();
        while (!x.isAfterLast()){
            String bn = x.getString(x.getColumnIndex(rate.partdist_box_boxnumber));
            boxnum.add(bn);

            x.moveToNext();
        }
        x.close();
        return boxnum;
    }

    public String getAccntNoNumber(String fulln){
        String fullname = null;
        SQLiteDatabase db = gen.getReadableDatabase();
        Cursor v = db.rawQuery(" SELECT * FROM " + gen.tbname_customers +
                " WHERE "+gen.cust_fullname+ " = '"+fulln+"' AND "
                +gen.cust_type+" = 'receiver'", null);
        if(v.moveToNext()) {
            fullname = v.getString(v.getColumnIndex(gen.cust_accountnumber));
        }
        return fullname;
    }

    public String getReceiver(String book, String bnum){
        String ac = null;
        SQLiteDatabase db = gen.getReadableDatabase();
        Cursor x = db.rawQuery(" SELECT * FROM "+gen.tbname_booking_consignee_box
                +" WHERE "+gen.book_con_transaction_no+" = '"+book+"' AND "+gen.book_con_box_number+" = '"+bnum+"'", null);
        if (x.moveToNext()){
            ac = x.getString(x.getColumnIndex(gen.book_con_box_account_no));
        }
        return ac;
    }

}
