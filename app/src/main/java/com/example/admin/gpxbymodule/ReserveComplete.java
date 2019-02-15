package com.example.admin.gpxbymodule;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class ReserveComplete extends Fragment {

    HomeDatabase helper;
    GenDatabase gendata;
    RatesDB rate;
    String value;
    AutoCompleteTextView search;
    ProgressDialog progressBar;
    ListView lv;
    String link;
    ThreeWayAdapter adapter;
    ArrayList<String> trns;
    Thread thread;
    Runnable r;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.activity_reservelist, null);

        gendata = new GenDatabase(getContext());
        helper = new HomeDatabase(getContext());
        rate = new RatesDB(getContext());
        lv = (ListView)view.findViewById(R.id.lvreservelist);
        search = (AutoCompleteTextView)view.findViewById(R.id.searchableinput);
        search.setSelected(false);
        link = helper.getUrl();
        trns = new ArrayList<>();

        customtype();
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

            lv.setOnTouchListener(new View.OnTouchListener() {
                // Setting on Touch Listener for handling the touch inside ScrollView
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    // Disallow the touch request for parent scroll on touch of child view
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    return false;
                }
            });


            FloatingActionButton fab = (FloatingActionButton)view.findViewById(R.id.addnewreserve);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(getContext(), Reserve.class);
                    //Create the bundle to pass
                    Bundle bundle = new Bundle();
                    //Add your data from getFactualResults method to bundle
                    bundle.putString("reservationno", "");
                    //Add the bundle to the intent
                    i.putExtras(bundle);
                    startActivity(i);
                    getActivity().finish();
                }
            });
        }catch (Exception e){}


        return  view;
    }

    public void customtype(){
        try{
            final ArrayList<ThreeWayHolder> result
                    = gendata.getBoxReservationBoxnumberComplete(helper.logcount()+"");
            adapter = new ThreeWayAdapter(getContext(), result);
            lv.setAdapter(adapter);
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String res = result.get(position).getTopitem();
                    String name = result.get(position).getSubitem();

                    showDialog(res, name);

                }
            });
            lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    final String iddel = result.get(position).getId();
                    TextView textView = (TextView) view.findViewById(R.id.c_account);
                    final String top = textView.getText().toString();

                    final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    String delete = "Delete";
                    builder.setTitle("Delete this data.")
                            .setMessage(Html.fromHtml("<b>note:</b><i>You can not retrieve the data if deleted.</i>"))
                            .setNegativeButton( delete.toUpperCase(), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    deleteReservationID(iddel);
                                    customtype();
                                    dialog.dismiss();
                                }
                            });
                    // Create an alert
                    builder.create().show();
                    return false;
                }
            });
        }catch (Exception e){}
    }

    public void deleteReservationID(String id) {
        SQLiteDatabase db = gendata.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(gendata.reserve_status, "0");
        db.update(gendata.tbname_reservation, cv,
                gendata.reserve_reservation_no + " = '" + id+"'", null);
        Log.e("deleted_res", id);
        db.close();

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle("Completed Reservations");
        setHasOptionsMenu(true);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.syncreservation){
            network();
        }
        return super.onOptionsItemSelected(item);
    }

    public void showDialog(final String reservation, final String who){
        try{
            final Dialog dialog = new Dialog(getActivity());
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(false);
            dialog.setContentView(R.layout.reservationdata);

            final TextView reservationnum = (TextView) dialog.findViewById(R.id.reservationnumber);
            TextView whom = (TextView) dialog.findViewById(R.id.ownerinfo);
            TextView address = (TextView) dialog.findViewById(R.id.own_addressinput);
            TextView stat = (TextView) dialog.findViewById(R.id.reserve_stat);
            ListView poplist = (ListView)dialog.findViewById(R.id.list);

            ArrayList<ListItem> poparray = gendata.getCompletedReservationBoxnumber(reservation);

            TableAdapter tb = new TableAdapter(getContext(), poparray);
            poplist.setAdapter(tb);

            reservationnum.setText(reservation);

            whom.setText(Html.fromHtml(""+who+""));
            address.setText(Html.fromHtml(""+getAddress(who)+""));

            Button ok = (Button) dialog.findViewById(R.id.addboxnum);
            Button close = (Button) dialog.findViewById(R.id.close);
            close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            SQLiteDatabase db = gendata.getReadableDatabase();
            Cursor x = db.rawQuery(" SELECT * FROM "+gendata.tbname_booking
                    +" WHERE "+gendata.book_reservation_no+" = '"+reservation+"'", null);
            if (x.getCount() == 0) {
                ok.setText("Book now");
                stat.setText("paid");
                ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(getContext(), Booking.class);
                        //Create the bundle to pass
                        Bundle bundle = new Bundle();
                        //Add your data from getFactualResults method to bundle
                        bundle.putString("transno", null);
                        bundle.putString("fullname", who);
                        bundle.putString("reservenum", reservation);
                        //Add the bundle to the intent
                        i.putExtras(bundle);
                        startActivity(i);
                        getActivity().finish();
                        customtype();
                        dialog.dismiss();
                    }
                });
            }else{
                ok.setText("Book now");
                stat.setText("Booked");
                ok.setClickable(false);
                ok.setBackgroundColor(Color.GRAY);
                ok.setTextColor(Color.LTGRAY);
            }
            dialog.show();
        }catch (Exception e){}
    }

    public String getAddress(String name){
        String fulladdress = null;
        SQLiteDatabase db = gendata.getReadableDatabase();
        Cursor cx = db.rawQuery(" SELECT * FROM "+gendata.tbname_customers
                +" LEFT JOIN "+gendata.tbname_reservation+" ON "
                +gendata.tbname_reservation+"."+gendata.reserve_customer_id
                +" = "+gendata.tbname_customers+"."+gendata.cust_accountnumber
                +" WHERE "+gendata.cust_fullname+" = '"+name+"' AND "
                +gendata.cust_type+" = 'customer'", null);
        if (cx.moveToNext()){
            fulladdress = cx.getString(cx.getColumnIndex(gendata.cust_unit))
                    +" "+cx.getString(cx.getColumnIndex(gendata.cust_barangay))
                    +" "+cx.getString(cx.getColumnIndex(gendata.cust_city))
                    +" "+cx.getString(cx.getColumnIndex(gendata.cust_prov));
        }
        return fulladdress;
    }

    //syncing data reservations
    //connecting to internet
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void network(){
        if (isNetworkAvailable()== true){
            sendPost();
            loadingPost(getView().getRootView());
        }else
        {
            Toast.makeText(getContext(),"Please check internet connection.", Toast.LENGTH_LONG).show();
        }
    }

    public void loadingPost(final View v){
        // prepare for a progress bar dialog
        int max = 100;
        progressBar = new ProgressDialog(v.getContext());
        progressBar.setCancelable(false);
        progressBar.setMessage("In Progress ...");
        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressBar.setMax(max);
        for (int i = 0; i <= max; i++) {
            progressBar.setProgress(i);

            if (i == max ){
                progressBar.dismiss();
            }
            progressBar.show();
        }

    }

    public void threadReservations() {
        SQLiteDatabase db = gendata.getReadableDatabase();
        String q = " SELECT * FROM " + gendata.tbname_reservation
                +" WHERE "+gendata.reserve_upload_status+" = '1'";
        Cursor cx = db.rawQuery(q, null);
        if (cx.getCount() != 0) {
            //THREAD FOR RESERVATION API
            try {
                String link = helper.getUrl();
                URL url = new URL("http://" + link + "/api/reservation/save.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);
                conn.setDoInput(true);

                JSONObject jsonParam = new JSONObject();
//                    for (int i = 0; i < 10; i++) {
                db = gendata.getReadableDatabase();
                JSONArray finalarray = new JSONArray();
                JSONArray pay = null, reserve = null, boxtypes = null;
                String query = " SELECT * FROM " + gendata.tbname_reservation
                        +" LEFT JOIN "+gendata.tbname_reservation_boxtype_boxnumber
                        +" ON "+gendata.tbname_reservation_boxtype_boxnumber+"."+gendata.res_btype_bnum_reservation_id
                        +" = "+gendata.tbname_reservation+"."+gendata.reserve_reservation_no
                        +" WHERE "+gendata.tbname_reservation_boxtype_boxnumber+"."+gendata.res_btype_bnum_box_number
                        +" != 'NULL' AND "
                        +gendata.tbname_reservation+"." +gendata.reserve_upload_status+" = '1' GROUP BY "+gendata.reserve_id;
                Cursor c = db.rawQuery(query, null);
                c.moveToFirst();
                String reserveno = null;
                while (!c.isAfterLast()) {
                    JSONObject json = new JSONObject();
                    String reservationid = c.getString(c.getColumnIndex(gendata.reserve_id));
                    reserveno = c.getString(c.getColumnIndex(gendata.reserve_reservation_no));
                    String statusid = c.getString(c.getColumnIndex(gendata.reserve_status));
                    String statusname = null;
                    String account_no = c.getString(c.getColumnIndex(gendata.reserve_customer_id));
                    String createdby = c.getString(c.getColumnIndex(gendata.reserve_createdby));
                    String createddate = c.getString(c.getColumnIndex(gendata.reserve_createddate));
                    String assigned_to = c.getString(c.getColumnIndex(gendata.reserve_assigned_to));
                    db = gendata.getReadableDatabase();
                    String sec = " SELECT COUNT("+gendata.res_btype_bnum_reservation_id+") FROM "+gendata.tbname_reservation_boxtype_boxnumber
                            +" WHERE "+gendata.res_btype_bnum_reservation_id+" = '"+reserveno+"' AND "
                            +gendata.res_btype_bnum_box_number+" != 'NULL'";
                    Cursor dofsec = db.rawQuery(sec, null);
                    int dosec = 0;
                    if (dofsec.moveToFirst()){
                        dosec = dofsec.getInt(0);
                    }
                    String fir = " SELECT SUM("+gendata.res_quantity+") FROM "+gendata.tbname_reservation_boxtype
                            +" WHERE "+gendata.res_reservation_id+" = '"+reserveno+"'";
                    Cursor dofir = db.rawQuery(fir, null);
                    int allres = 0;
                    if (dofir.moveToFirst()){
                        allres = dofir.getInt(0);
                    }
                    Log.e("counts", "allres: "+allres+", nulls: "+dosec);

                    if (dosec != allres){
                        statusid = "1";
                        statusname = "Pending";
                    }else{
                        statusid = "2";
                        statusname = "Paid";
                    }

                    json.put("reservation_id", reservationid);
                    json.put("reservation_no", reserveno);
                    json.put("statusid", statusid);
                    json.put("statusname", statusname);
                    json.put("account_no", account_no);
                    json.put("customername", getCustomerName(account_no));
                    json.put("createdby", createdby);
                    json.put("createddate", createddate);
                    json.put("assigned_to", assigned_to);

                    reserve = gendata.getResultsArray(reserveno);
                    boxtypes = gendata.getBoxtypes(reserveno);
                    json.put("reservation_boxtype", boxtypes);
                    json.put("reservation_boxtype_box_number", reserve);

                    pay = gendata.getResultsPayment(reserveno);
                    json.put("payment", pay);
                    json.put("reservation_image", getImage(reserveno));
                    trns.add(reserveno);
                    finalarray.put(json);
                    c.moveToNext();
                }
                c.close();
                jsonParam.accumulate("data", finalarray);

                Log.e("JSON", jsonParam.toString());
                DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                os.writeBytes(jsonParam.toString());
                os.flush();
                os.close();

                Log.e("STATUS", String.valueOf(conn.getResponseCode()));
                Log.e("MSG", conn.getResponseMessage());
                if (!conn.getResponseMessage().equals("OK")){
                    conn.disconnect();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.dismiss();
                            final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                            builder.setTitle("Upload failed")
                                    .setMessage("Data sync has failed, please try again later. thank you.")
                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.dismiss();
                                            getActivity().recreate();
                                            Log.e("threadout", "mainthread");
                                        }
                                    });
                            // Create the AlertDialog object and show it
                            builder.create();
                            builder.setCancelable(false);
                            builder.show();
                        }
                    });
                }else {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.dismiss();
                            final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                            builder.setTitle("Information confirmation")
                                    .setMessage("Data upload has been successful, thank you.")
                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            updateReserveStat(trns);
                                            dialog.dismiss();
                                            customtype();
                                        }
                                    });
                            // Create the AlertDialog object and show it
                            builder.create();
                            builder.setCancelable(false);
                            builder.show();

                        }
                    });
                }
                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
            //END THREAD RESERVATION API
        }else{
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressBar.dismiss();
                    final AlertDialog.Builder builder
                            = new AlertDialog.Builder(getContext());
                    builder.setTitle("Information confirmation")
                            .setMessage("You dont have data to be uploaded yet, please add new transactions. Thank you.")
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                }
                            });
                    builder.create();
                    builder.setCancelable(false);
                    builder.show();
                }
            });
        }
    }

    public void threadCustomers(){
        SQLiteDatabase db = gendata.getReadableDatabase();
        String query = " SELECT * FROM "+gendata.tbname_customers+" WHERE "
                +gendata.cust_createdby+" = '"+helper.logcount()+"'";
        Cursor cx = db.rawQuery(query, null);
        if (cx.getCount() != 0) {
            try {
                String link = helper.getUrl();
                URL url = new URL("http://" + link + "/api/customer/save.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);
                conn.setDoInput(true);

                JSONObject jsonParam = new JSONObject();
                jsonParam.accumulate("data", gendata.getAllCustomers(helper.logcount() + ""));

                Log.e("JSON", jsonParam.toString());
                DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                os.writeBytes(jsonParam.toString());

                os.flush();
                os.close();

                Log.i("STATUS", String.valueOf(conn.getResponseCode()));
                Log.i("MSG", conn.getResponseMessage());

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public String getCustomerName(String accountnumber){
        String name = null;
        SQLiteDatabase db = gendata.getReadableDatabase();
        Cursor c = db.rawQuery(" SELECT * FROM "+gendata.tbname_customers+
                " WHERE "+gendata.cust_accountnumber+" = '"+accountnumber+"'", null);
        c.moveToFirst();
        while (!c.isAfterLast()){
            name = c.getString(c.getColumnIndex(gendata.cust_firstname))+" "
                    +c.getString(c.getColumnIndex(gendata.cust_lastname));

            c.moveToNext();
        }
        return name;
    }

    //send sync data
    public void sendPost() {
        thread = new Thread(new Runnable() {
            @Override
            public void run() {

                threadCustomers();

                threadReservations();

            }
        });
        thread.start();
    }

    public JSONArray getImage(String id) {
        SQLiteDatabase myDataBase = rate.getReadableDatabase();
        String raw = "SELECT * FROM " + rate.tbname_generic_imagedb
                + " WHERE "+rate.gen_trans+" = '"+id+"' AND "+rate.gen_module+" = 'reservation'";
        Cursor c = myDataBase.rawQuery(raw, null);
        JSONArray resultSet = new JSONArray();
        c.moveToFirst();
        try {
            while (!c.isAfterLast()) {
                JSONObject js = new JSONObject();
                String module = c.getString(c.getColumnIndex(rate.gen_module));
                byte[] image = c.getBlob(c.getColumnIndex(rate.gen_image));
                Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
                byte[] bitmapdata = getBytesFromBitmap(bitmap);
                // get the base 64 string
                String imgString = Base64.encodeToString(bitmapdata, Base64.NO_WRAP);
                js.put("module", module);
                js.put("image", imgString);
                resultSet.put(js);
                c.moveToNext();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        c.close();
        return resultSet;
    }

    // convert from bitmap to byte array
    public byte[] getBytesFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
        return stream.toByteArray();
    }

    //update reservations upload status
    public void updateReserveStat(ArrayList<String> trans){
        SQLiteDatabase db = gendata.getWritableDatabase();
        for (String tr : trans) {
            ContentValues cv = new ContentValues();
            cv.put(gendata.reserve_upload_status, "2");
            db.update(gendata.tbname_reservation, cv,
                    gendata.reserve_reservation_no+" = '"+tr+"' AND "+
                            gendata.reserve_upload_status + " = '1'", null);
            Log.e("upload", "uploaded reservations "+tr);
        }
        db.close();
    }

}
