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
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class ReservePending extends Fragment {

    HomeDatabase helper;
    GenDatabase gendata;
    RatesDB rate;
    String value;
    AutoCompleteTextView search;
    ProgressDialog progressBar;
    ListView lv;
    String link;
    ThreeWayAdapter adapter;
    String iddel;
    SQLiteDatabase db;
    Reservelist res;

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
        res = (Reservelist)getActivity();

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
            final ArrayList<ThreeWayHolder> result = gendata.getReservelist("1");
            adapter = new ThreeWayAdapter(getContext(), result);
            lv.setAdapter(adapter);
            lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                   iddel = result.get(position).getId();
                    TextView textView = (TextView) view.findViewById(R.id.c_account);
                    final String top = textView.getText().toString();
                    Log.e("topitem",top);

                    final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    String delete = "Delete";
                    String edit = "Edit";
                    builder.setTitle("Delete or edit this data.")
                            .setMessage(Html.fromHtml("<b>note:</b><i>You can edit or delete this data.</i>"))
                            .setPositiveButton(edit.toUpperCase(), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    Intent i = new Intent(getContext(), Reserve.class);
                                    //Create the bundle to pass
                                    Bundle bundle = new Bundle();
                                    bundle.putString("reservationno", top);
                                    //Add the bundle to the intent
                                    i.putExtras(bundle);
                                    dialog.dismiss();
                                    startActivity(i);
                                    getActivity().finish();
                                }
                            })
                            .setNegativeButton( delete.toUpperCase(), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    Log.e("mtop", top);
                                    deleteReservationID(top);
                                    //deleteReservationBoxesNumbers(top);
                                    //deleteReservationBoxes(top);
                                    customtype();
                                    dialog.dismiss();
                                }
                            });
                    // Create an alert
                    builder.create().show();
                    return true;
                }
            });

            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    TextView textView = (TextView) view.findViewById(R.id.c_account);
                    TextView kanino = (TextView) view.findViewById(R.id.c_name);
                    final String mtop = textView.getText().toString();
                    final String hwo = kanino.getText().toString();
                    Log.e("topitem",mtop);
                    res.setReservenumber(mtop);

                    Log.e("reservenum", res.getReservenumber());

                    ArrayList<ListItem> poparray;
                    Log.e("mtop", mtop);
                    final Dialog dialog = new Dialog(getActivity());
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setCancelable(false);
                    dialog.setContentView(R.layout.reservationdata);

                    final TextView reservationnum = (TextView) dialog.findViewById(R.id.reservationnumber);
                    TextView whom = (TextView) dialog.findViewById(R.id.ownerinfo);
                    TextView address = (TextView) dialog.findViewById(R.id.own_addressinput);
                    ListView poplist = (ListView)dialog.findViewById(R.id.list);

                    poparray = gendata.getBoxReservations(mtop);

                    TableAdapter tb = new TableAdapter(getContext(), poparray);
                    poplist.setAdapter(tb);

                    reservationnum.setText(mtop);

                    whom.setText(Html.fromHtml(""+hwo+""));
                    address.setText(Html.fromHtml(""+getAddress(hwo)+""));

                    Button ok = (Button) dialog.findViewById(R.id.addboxnum);
                    Button close = (Button) dialog.findViewById(R.id.close);
                    close.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });

                    ok.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i = new Intent(getContext(), ReservationData.class);
                            //Create the bundle to pass
                            Bundle bundle = new Bundle();
                            bundle.putString("reservationno", mtop);
                            bundle.putString("reservationid", iddel);
                            //Add the bundle to the intent
                            i.putExtras(bundle);
                            dialog.dismiss();
                            startActivity(i);
                            getActivity().finish();
                        }
                    });
                    dialog.show();
                }
            });
        }catch (Exception e){}
    }

    public void deleteReservationBoxes(String id) {
        SQLiteDatabase db = gendata.getWritableDatabase();
        db.delete(gendata.tbname_reservation_boxtype, gendata.res_reservation_id + " = '" + id+"'", null);
        Log.e("deleted_resbtyp", id);
        db.close();

    }

    public void deleteReservationBoxesNumbers(String id) {
        SQLiteDatabase db = gendata.getWritableDatabase();
        db.delete(gendata.tbname_reservation_boxtype_boxnumber, gendata.res_btype_bnum_reservation_id + " = '" + id+"'", null);
        Log.e("deleted_resbtypnum", id);
        db.close();

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
        getActivity().setTitle("Pending Reservations");
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

    public String getAddress(String name){
        String fulladdress = null;
        try{
            SQLiteDatabase db = gendata.getReadableDatabase();
            Cursor cx = db.rawQuery(" SELECT * FROM "+gendata.tbname_customers
                    +" WHERE "+gendata.cust_fullname+" = '"+name+"' AND "
                    +gendata.cust_type+" = 'customer'", null);
            if (cx.moveToNext()){
                fulladdress = cx.getString(cx.getColumnIndex(gendata.cust_unit))
                        +" "+cx.getString(cx.getColumnIndex(gendata.cust_barangay))
                        +" "+cx.getString(cx.getColumnIndex(gendata.cust_city))
                        +" "+cx.getString(cx.getColumnIndex(gendata.cust_prov));
            }
        }catch (Exception e){}
        return fulladdress;
    }

    public String datereturn(){
        Date datetalaga = Calendar.getInstance().getTime();
        SimpleDateFormat writeDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        writeDate.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        String find = writeDate.format(datetalaga);

        return find;
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

        // Create a Handler instance on the main thread
        final Handler handler = new Handler();

        // Create and start a new Thread
        new Thread(new Runnable() {
            public void run() {
                try{
                    sendPost();
                    Thread.sleep(15000);
                }
                catch (Exception e) { } // Just catch the InterruptedException

                handler.post(new Runnable() {
                    public void run() {
                        progressBar.dismiss();
                        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setTitle("Information confirmation")
                                .setMessage("Data has been updated successfully, thank you.")
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.dismiss();
                                        getActivity().recreate();
                                    }
                                });
                        // Create the AlertDialog object and show it
                        builder.create();
                        builder.setCancelable(false);
                        builder.show();
                    }
                });
            }
        }).start();
    }

    public void getReservations(){
        try {
            String response = null;
            String link = helper.getUrl();
            String geturl = "http://"+link+"/api/reservation/pending.php?id="+helper.logcount();
            URL url = new URL(geturl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            // read the response
            InputStream in = new BufferedInputStream(conn.getInputStream());
            response = convertStreamToString(in);

            if (response != null) {
                Log.e("Reservations", "Reservations: " + response);
                String dataid;
                JSONArray jsonArray = new JSONArray(response);

                for(int i=0; i<jsonArray.length(); i++){
                    JSONObject json_data = jsonArray.getJSONObject(i);
                    dataid = json_data.getString("reservation_id"); // reserve id
                    String reservation_no = json_data.getString("reservation_no"); // reservation number
                    String customerid = json_data.getString("account_no"); // customer id
                    String createdby = json_data.getString("createdby"); // created by
                    String createddate = json_data.getString("createddate"); // created date
                    String statusname = json_data.getString("statusid"); //status
                    String employeename = json_data.getString("assigned_to"); // assigned to

                    if (checkReservationid(reservation_no)) {
                        gendata.addGPXReservation(reservation_no, customerid,
                                createdby, createddate, employeename, statusname, "1");
                        Log.e("reservation", reservation_no);

                        JSONArray jarray = json_data.getJSONArray("reservation_boxtype_details");
                        for (int x = 0; x < jarray.length(); x++) {
                            JSONObject jx = jarray.getJSONObject(x);
                            int boxid = Integer.parseInt(jx.getString("reservation_boxtype_id"));
                            String quantity = jx.getString("quantity");
                            String deposit = jx.getString("deposit");
                            String boxtype = getBoxname(jx.getString("boxtype_id"));
                            String boxtype_id = jx.getString("boxtype_id");
                            db = gendata.getReadableDatabase();
                            Cursor c = db.rawQuery(" SELECT * FROM " + gendata.tbname_reservation_boxtype
                                    + " WHERE " + gendata.res_reservation_id + " = '" + reservation_no + "' AND "
                                    + gendata.res_boxtype + " = '" + boxtype + "' AND "
                                    + gendata.res_quantity + " = '" + quantity + "'", null);
                            if (c.getCount() == 0) {
                                gendata.addGPXReservationBoxtype(boxtype_id, boxtype, quantity, deposit,
                                        reservation_no);
                                Log.e("reservationboxtype", reservation_no);
                            } else {
                                Log.e("error insert", "ID present");
                            }
                        }
                    }
                }
                if (!conn.getResponseMessage().equals("OK")){
                    conn.disconnect();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.dismiss();
                            final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                            builder.setTitle("Getting data failed")
                                    .setMessage("Data sync has failed, please try again later. thank you.")
                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.dismiss();
                                            getActivity().recreate();
                                        }
                                    });
                            // Create the AlertDialog object and show it
                            builder.create().show();
                        }
                    });
                }
            } else {
                Log.e("Error", "Couldn't get data from server.");
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(),
                                "Couldn't get data from server.",
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int generateId(){
        int id = 0;
        SQLiteDatabase db = gendata.getReadableDatabase();
        Cursor x = db.rawQuery(" SELECT * FROM "+gendata.tbname_reservation_boxtype, null);
        if (x.getCount() != 0 ){
            id = (x.getCount() + idbaseday());
        }else{
            id = x.getCount();
        }
        return id;
    }

    public int idbaseday(){
        Date datetalaga = Calendar.getInstance().getTime();
        SimpleDateFormat writeDate = new SimpleDateFormat("ss");
        writeDate.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        int findate = Integer.parseInt(writeDate.format(datetalaga));

        return findate;
    }

    public String getBoxname(String id){
        String name = "";
        SQLiteDatabase db = gendata.getReadableDatabase();
        String que = " SELECT * FROM "+gendata.tbname_boxes
                +" WHERE "+gendata.box_id+" = '"+id+"'";
        Cursor x = db.rawQuery(que, null);
        if (x.moveToNext()){
            name = x.getString(x.getColumnIndex(gendata.box_name));
        }
        return name;
    }

    //send sync data
    public void sendPost() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {

                getReservations();

            }
        });

        thread.start();
    }

    private String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return sb.toString();
    }

    public boolean checkReservationid(String num){
        SQLiteDatabase db = gendata.getReadableDatabase();
        Cursor c = db.rawQuery(" SELECT * FROM "+gendata.tbname_reservation
                +" WHERE "+gendata.reserve_reservation_no+" = '"+num+"'", null);
        if(c.getCount() != 0){
            return false;
        }else{
            return true;
        }
    }

}
