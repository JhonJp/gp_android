package com.example.admin.gpxbymodule;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class Deposit extends Fragment {

    EditText amount;

    AutoCompleteTextView accntnumber;
    SQLiteDatabase db;
    GenDatabase gen;
    RatesDB rate;
    ImageButton add;
    ListView lv;
    int id;
    TextView reserv, names;
    TextView total;
    String accnt;
    HomeDatabase helper;
    Date datetalaga;
    Reserve reserve;
    String reservationnumber;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.deposit, null);

        reserve = (Reserve)getActivity();

        lv = (ListView)view.findViewById(R.id.lv);
        total = (TextView)view.findViewById(R.id.totalcount);
        reserv = (TextView)view.findViewById(R.id.reservenum);
        names = (TextView)view.findViewById(R.id.nametext);
        gen = new GenDatabase(getContext());
        helper = new HomeDatabase(getContext());
        rate = new RatesDB(getContext());

        try {
            if (reserve.getAccnt() != null) {
                accnt = reserve.getAccnt();
            }

            reserv.setText(reserve.getReservationnum());
            names.setText(reserve.getName());
            total.setText(Html.fromHtml("<small><i>Total : </small></i>"
                    + "<b> " + sum() + "</b> box(s) "));

            customtype();
            lv.setOnTouchListener(new View.OnTouchListener() {
                // Setting on Touch Listener for handling the touch inside ScrollView
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    // Disallow the touch request for parent scroll on touch of child view
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    return false;
                }
            });

        Log.e("reserve num :", reserve.getReservationnum());

        }catch (Exception e){}

        return  view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle("Reservation Info");
        setHasOptionsMenu(true);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.savebooking).setVisible(true);
        menu.findItem(R.id.loadprev).setVisible(false);
        menu.findItem(R.id.loadprevpay).setVisible(true);
        menu.findItem(R.id.btnnext).setVisible(false);
        menu.findItem(R.id.btnnextpay).setVisible(false);

        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        try {
            int id = item.getItemId();
            if (id == R.id.loadprev) {
                loadFragment(new PaymentFragment());
            } else if (id == R.id.savebooking) {
                alert();
            }
        }catch (Exception e){}
            return super.onOptionsItemSelected(item);
    }

    public boolean loadFragment(Fragment fragment) {
        //switching fragment
        if (fragment != null) {
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_base, fragment)
                    .commit();

            return true;
        }
        return false;
    }

    public void customtype(){
        try {
            final ArrayList<ListItem> listitem = gen.getBoxes(reserve.getReservationnum());
            TableAdapter myAdapter = new TableAdapter(getContext(), listitem);
            lv.setAdapter(myAdapter);
        }catch (Exception e){}
    }

    public int sum(){
        db = gen.getReadableDatabase();
        Cursor cur = db.rawQuery(" SELECT SUM("+gen.res_quantity+") FROM "+gen.tbname_reservation_boxtype+
                " WHERE "+gen.res_reservation_id+" = '"+reserve.getReservationnum()+"'", null);
        if(cur.moveToFirst())
        {
            return cur.getInt(0);
        }
        return 0;
    }

    public String datereturn(){
        datetalaga = Calendar.getInstance().getTime();
        SimpleDateFormat writeDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        writeDate.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        String findate = writeDate.format(datetalaga);

        return findate;
    }

    public void alert(){
        try {
            if (reserve.getName() == null) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Reservation error.");
                builder.setMessage(Html.fromHtml("<b>Note: </b>" +
                        "Your reservation has no customer."))
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });
                // Create the AlertDialog object and show it
                builder.create().show();
            } else {
                final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Confirm your reservation.?");
                builder.setMessage(Html.fromHtml("<b>Note: </b>" +
                        " Please confirm if the information is correct and then save reservation."))
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                finalconfirmreservation();
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });
                // Create the AlertDialog object and show it
                builder.create().show();
            }
        }catch (Exception e){}
    }

    public void finalconfirmreservation(){
        try {
            SQLiteDatabase db = gen.getReadableDatabase();
            Cursor x = db.rawQuery(" SELECT * FROM " + gen.tbname_reservation
                    + " WHERE " + gen.reserve_reservation_no + " = '" + reserve.getReservationnum() + "'", null);
            if (x.getCount() != 0) {
                getAllReserves(reserve.getReservationnum());
                gen.updateGPXReservation(reserve.getReservationnum(), accnt, helper.logcount() + "", datereturn(),
                        helper.logcount() + "", "1", "1");
                Log.e("edit", reserve.getReservationnum());
            } else {
                getAllReserves(reserve.getReservationnum());
                gen.addGPXReservation(reserve.getReservationnum(), accnt, helper.logcount() + "", datereturn(),
                        helper.logcount() + "", "1", "1");
            }
            Log.e("reservation", reserve.getReservationnum());

            String type = "Reservations";
            gen.addTransactions(type, "" + helper.logcount(),
                    "New reservation with number " + reserve.getReservationnum(), datelang(), returntime());


            reserve.setAccnt(null);
            reserve.setReservationnum(null);
            startActivity(new Intent(getContext(), Reservelist.class));
            getActivity().finish();
        }catch (Exception e){}
    }

    public void getAllReserves(String trans){
        SQLiteDatabase db = gen.getWritableDatabase();
        Cursor x = db.rawQuery(" SELECT * FROM "+gen.tbname_reservation_boxtype
                +" WHERE "+gen.res_reservation_id+" = '"+trans+"'", null);
        if (x.moveToNext()){
            String boxtype = x.getString(x.getColumnIndex(gen.res_boxtype));
            //updateBoxQuantity(getAvailablewarehouse(helper.getBranch(helper.logcount()+"")), getBoxId(boxtype)+"");
        }
    }

//    public void updateBoxQuantity(String warehouse, String boxid){
//        try {
//            SQLiteDatabase db = gen.getWritableDatabase();
//            Cursor cx = db.rawQuery(" SELECT * FROM " + gen.tb_acceptance + " WHERE "
//                    + gen.acc_boxtype + " = '" + boxid + "' AND " + gen.acc_warehouse_id + " = '" + warehouse + "' AND "
//                    + gen.acc_quantity + " != '0' LIMIT 1 ", null);
//            cx.moveToFirst();
//            while (!cx.isAfterLast()) {
//                String ac_id = cx.getString(cx.getColumnIndex(gen.acc_id));
//                String ac_ware_id = cx.getString(cx.getColumnIndex(gen.acc_warehouse_id));
//                String ac_name = cx.getString(cx.getColumnIndex(gen.acc_name));
//                String ac_boxtype = cx.getString(cx.getColumnIndex(gen.acc_boxtype));
//                String ac_q = cx.getString(cx.getColumnIndex(gen.acc_quantity));
//                String ac_cdate = cx.getString(cx.getColumnIndex(gen.acc_createddate));
//                String ac_cby = cx.getString(cx.getColumnIndex(gen.acc_createdby));
//                String ac_stat = cx.getString(cx.getColumnIndex(gen.acc_status));
//                String ac_upds = cx.getString(cx.getColumnIndex(gen.acc_upds));
//                int qcount = Integer.parseInt(ac_q);
//
//                int finalcount = (qcount - 1);
//
//                gen.updAccBox(ac_id, ac_ware_id, ac_name, ac_boxtype, finalcount + "", ac_cdate
//                        , ac_cby, ac_stat,ac_upds);
//
//                cx.moveToNext();
//            }
//        }catch (Exception e){}
//    }

    public int getBoxId(String boxtype){
        int id = 0;
        SQLiteDatabase db = gen.getReadableDatabase();
        String query = "SELECT * FROM "+gen.tbname_boxes
                +" WHERE "+gen.box_name+" = '"+boxtype+"'";
        Cursor c = db.rawQuery(query, null);
        if (c.getCount() != 0){
            c.moveToNext();
            id = c.getInt(c.getColumnIndex(gen.box_id));
        }
        // Log.e("query", query);
        return id;
    }

    public String getAvailablewarehouse(String branch){
        String id = null;
        try {
            SQLiteDatabase db = rate.getReadableDatabase();
            Cursor x = db.rawQuery(" SELECT * FROM " + rate.tbname_warehouse
                    + " WHERE " + rate.ware_branchid + " = '" + branch + "' LIMIT 1 ", null);
            if (x.moveToNext()) {
                id = x.getString(x.getColumnIndex(rate.ware_id));
            }
        }catch (Exception e){}
        return id;
    }

    public String datelang(){
        Date datetalaga = Calendar.getInstance().getTime();
        SimpleDateFormat writeDate = new SimpleDateFormat("yyyy-MM-dd");
        writeDate.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        String findate = writeDate.format(datetalaga);

        return findate;
    }

    public String returntime(){
        Date datetalaga = Calendar.getInstance().getTime();
        SimpleDateFormat writeDate = new SimpleDateFormat("HH:mm:ss");
        writeDate.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        String time = writeDate.format(datetalaga);

        return time;
    }

}