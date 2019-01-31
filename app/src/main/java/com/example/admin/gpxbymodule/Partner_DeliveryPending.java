package com.example.admin.gpxbymodule;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class Partner_DeliveryPending extends Fragment {

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
        try{
            search.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    customtype();
                    Log.e("text watch","before change");
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    adapter.getFilter().filter(s.toString());
                    Log.e("text watch","on change");
                }

                @Override
                public void afterTextChanged(Editable s) {
                    Log.e("text watch","after change");
                }
            });
        }catch (Exception e){}

        scroll();

        if (helper.getFullname(helper.logcount()+"").contains("  ")){
            yourname = helper.getFullname(helper.logcount()+"").replace("  ", " ");
        }
        customtype();

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        getActivity().setTitle("Pending delivery");
        setHasOptionsMenu(true);
    }

    public void scroll(){
        try{
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
            final ArrayList<ThreeWayHolder> result = getDirectDelivers(getDirectBoxes());
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
                    Log.e("bookingnumber", booknumber);
                    Log.e("accountnumber", getAccntNoNumber(bn));
                    bundle.putString("status", "0");
                    //Add the bundle to the intent
                    i.putExtras(bundle);
                    startActivity(i);
                    getActivity().finish();
                }
            });
        }catch (Exception e){}
    }

//    @Override
//    public void onPrepareOptionsMenu(Menu menu) {
//        menu.findItem(R.id.syncdelivery).setVisible(false);
//        super.onPrepareOptionsMenu(menu);
//    }

    public String getAccntNo(String fulln){
        String fullname = null;
        SQLiteDatabase db = gen.getReadableDatabase();
        Cursor v = db.rawQuery(" SELECT * FROM " + gen.tbname_customers +
                " WHERE "+gen.cust_accountnumber+ " = '"+fulln+"' AND "
                +gen.cust_type+" = 'receiver'", null);
        if(v.moveToNext()) {
            fullname = v.getString(v.getColumnIndex(gen.cust_fullname));
        }
        return fullname;
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

    public String getTransNumber(String bn){
        String trans = null;
        SQLiteDatabase db = gen.getReadableDatabase();
        String x = " SELECT * FROM "+gen.tbname_booking_consignee_box
                +" WHERE "+gen.book_con_box_number+" = '"+bn+"'";
        Cursor c = db.rawQuery(x, null);
        if (c.moveToNext()){
            trans = c.getString(c.getColumnIndex(gen.book_con_transaction_no));
        }
        return trans;
    }

    public String getReceiver(String bn){
        String trans = null;
        SQLiteDatabase db = gen.getReadableDatabase();
        String x = " SELECT * FROM "+gen.tbname_booking_consignee_box
                +" WHERE "+gen.book_con_box_number+" = '"+bn+"'";
        Cursor c = db.rawQuery(x, null);
        if (c.moveToNext()){
            trans = c.getString(c.getColumnIndex(gen.book_con_box_account_no));
        }
        return trans;
    }

    public ArrayList<ThreeWayHolder> getDirectDelivers(ArrayList<String> numbers){
        ArrayList<ThreeWayHolder> results = new ArrayList<>();
        ArrayList<String> booknums = new ArrayList<>();
        ArrayList<String> names = new ArrayList<>();
        SQLiteDatabase db = gen.getReadableDatabase();
        for (String item : numbers){
            String x = " SELECT * FROM " + gen.tbname_booking
                    +" JOIN "+gen.tbname_booking_consignee_box+" ON "
                    +gen.tbname_booking_consignee_box+"."+gen.book_con_transaction_no
                    +" = "+gen.tbname_booking+"."+gen.book_transaction_no
                    + " WHERE "+gen.tbname_booking_consignee_box+"."+gen.book_con_box_number + " = '" + item + "'"
                    +" GROUP BY "+gen.tbname_booking_consignee_box+"."+gen.book_con_box_account_no
                    +" AND "+gen.tbname_booking+"."+gen.book_transaction_no;
            Cursor res = db.rawQuery(x, null);
            res.moveToFirst();
            while (!res.isAfterLast()){
                String name = "";
                String address = "";
                String finaddress = "";
                String topitem = res.getString(res.getColumnIndex(gen.book_transaction_no));
                String subitem = res.getString(res.getColumnIndex(gen.book_con_box_account_no));
                Cursor cx = db.rawQuery(" SELECT * FROM " + gen.tbname_customers + " WHERE "
                        + gen.cust_accountnumber + " = '" + subitem + "'", null);
                if (cx.getCount() != 0) {
                    cx.moveToNext();
                    name = cx.getString(cx.getColumnIndex(gen.cust_fullname));
                    address = ""+ getBrgy(cx.getString(cx.getColumnIndex(gen.cust_barangay))) + ", "
                            + getCity(cx.getString(cx.getColumnIndex(gen.cust_city))) + ", "
                            + getProvince(cx.getString(cx.getColumnIndex(gen.cust_prov))) +" "
                            + cx.getString(cx.getColumnIndex(gen.cust_postal));
                    finaddress = Html.fromHtml("<small>"+address+"</small>").toString();
                }
                if (!(booknums.contains(topitem) && names.contains(name))){
                    ThreeWayHolder list = new ThreeWayHolder(topitem, name,
                            finaddress, getBoxesCount(subitem,topitem)+"");
                    booknums.add(topitem);
                    names.add(name);
                    results.add(list);
                }
                res.moveToNext();
            }
        }
        // Add some more dummy data for testing
        return results;
    }

    public String getProvince(String code){
        String name = "";
        SQLiteDatabase db = rate.getReadableDatabase();
        String q = " SELECT * FROM "+rate.tbname_provinces
                +" WHERE "+rate.prov_code+" = '"+code+"'";
        Cursor x = db.rawQuery(q, null);
        if (x.moveToNext()){
            name = x.getString(x.getColumnIndex(rate.prov_name));
            Log.e("prov_name", name);
        }
        return name;
    }

    public String getCity(String code){
        String name = "";
        SQLiteDatabase db = rate.getReadableDatabase();
        String q = " SELECT * FROM "+rate.tbname_city
                +" WHERE "+rate.ct_citycode+" = '"+code+"'";
        Cursor x = db.rawQuery(q, null);
        if (x.moveToNext()){
            name = x.getString(x.getColumnIndex(rate.ct_name));
            Log.e("cityname", name);
        }
        return name;
    }

    public String getBrgy(String code){
        String name = "";
        SQLiteDatabase db = rate.getReadableDatabase();
        String q = " SELECT * FROM "+rate.tbname_brgy
                +" WHERE "+rate.brgy_code+" = '"+code+"'";
        Cursor x = db.rawQuery(q, null);
        if (x.moveToNext()){
            name = x.getString(x.getColumnIndex(rate.brgy_name));
            Log.e("brgyname", name);
        }
        return name;
    }

    public int getBoxesCount(String acc, String trans){
        SQLiteDatabase db = gen.getReadableDatabase();
        Cursor x = db.rawQuery(" SELECT * FROM "+gen.tbname_booking_consignee_box
                +" WHERE "+gen.book_con_box_account_no+" = '"+acc+"' AND "+gen.book_con_transaction_no
                +" = '"+trans+"'", null);
        return x.getCount();
    }

    //get boxes
    public ArrayList<String> getDirectBoxes(){
        ArrayList<String> boxnum = new ArrayList<>();
        SQLiteDatabase db = rate.getReadableDatabase();
        Cursor x = db.rawQuery(" SELECT * FROM "+rate.tbname_fordirects
                +" WHERE "+rate.direct_stat+" = '0'", null);
        x.moveToFirst();
        while (!x.isAfterLast()){
            String bn = x.getString(x.getColumnIndex(rate.direct_boxnumber));
            if (checkFromDelivery(bn)) {
                boxnum.add(bn);
            }
            x.moveToNext();
        }
        x.close();
        return boxnum;
    }

    public boolean checkFromDelivery(String bn){
        SQLiteDatabase db = gen.getReadableDatabase();
        String x = " SELECT * FROM "+gen.tbname_delivery_box
                +" WHERE "+gen.del_box_boxnumber+" = '"+bn+"'";
        Cursor c = db.rawQuery(x, null);
        if (c.getCount() == 0){
            return true;
        }else{
            return false;
        }
    }

}
