package com.example.admin.gpxbymodule;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;

public class BoxReleaseList extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    NavigationView navigationView;
    HomeDatabase helper;
    GenDatabase gen;
    SQLiteDatabase db;
    String value;
    RatesDB rate;
    ListView lv;
    AutoCompleteTextView search;
    LinearList adapter;
    ProgressDialog progressBar;
    ArrayList<String> ids;

    //signature variables
    LinearLayout mContent;
    BoxReleaseList.signature mSignature;
    Button mClear, mGetSign, mCancel;
    public static String tempDir;
    public String current = null;
    private Bitmap mBitmap;
    View mView;
    File mypath;
    Dialog dx;
    byte[] off;
    private String uniqueId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_box_release_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //variables
        gen = new GenDatabase(this);
        helper = new HomeDatabase(this);
        rate = new RatesDB(this);
        lv = (ListView) findViewById(R.id.lv);
        ids = new ArrayList<>();

        //transactions
        if (helper.logcount() != 0) {
            value = helper.getRole(helper.logcount());
        }
        scroll();
        customtype();
        //floating action button
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(BoxReleaseList.this, BoxRelease.class));
                finish();
            }
        });

        //search auto function
        try {
            search.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    customtype();
                    Log.e("text watch", "before change");
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    adapter.getFilter().filter(s.toString());
                    Log.e("text watch", "on change");
                }

                @Override
                public void afterTextChanged(Editable s) {
                    Log.e("text watch", "after change");
                }
            });
        }catch (Exception e){}

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        setNameMail();
        subMenu();
        sidenavMain();
    }

    public void setNameMail(){
        RatesDB rate = new RatesDB(getApplicationContext());
        String branchname = null;
        View header = navigationView.getHeaderView(0);
        TextView user = (TextView)header.findViewById(R.id.yourname);
        TextView mail = (TextView)header.findViewById(R.id.yourmail);
        user.setText(helper.getFullname(helper.logcount()+""));
        SQLiteDatabase db = rate.getReadableDatabase();
        Cursor x = db.rawQuery(" SELECT * FROM "+rate.tbname_branch
                +" WHERE "+rate.branch_id+" = '"+helper.getBranch(""+helper.logcount())+"'", null);
        if (x.moveToNext()){
            branchname = x.getString(x.getColumnIndex(rate.branch_name));
        }
        x.close();
        mail.setText(helper.getRole(helper.logcount())+" / "+branchname);

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
        try {
            final ArrayList<LinearItem> result = rate.getAllBarcodeReleased(helper.logcount() + "");
            LinearList ad = new LinearList(getApplicationContext(), result);
            lv.setAdapter(ad);
            item();

        }catch (Exception e){}
    }

    public void item(){
        try {
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    TextView textView = (TextView) view.findViewById(R.id.c_account);
                    TextView idtext = (TextView) view.findViewById(R.id.dataid);
                    final String mtop = textView.getText().toString();
                    final String ids = idtext.getText().toString();
                    ArrayList<ListItem> poparray;
                    Log.e("mtop", ids);
                    final Dialog dialog = new Dialog(BoxReleaseList.this);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setCancelable(false);
                    dialog.setContentView(R.layout.distdatalayout);
                    TextView reshead = (TextView) dialog.findViewById(R.id.reservationnumber);
                    TextView whom = (TextView) dialog.findViewById(R.id.ownerinfo);
                    TextView trucktitle = (TextView) dialog.findViewById(R.id.trucknumtitle);
                    TextView truck = (TextView) dialog.findViewById(R.id.truckinput);
                    ListView poplist = (ListView) dialog.findViewById(R.id.list);
                    //Log.e("distid", ids);
                    reshead.setText(ids);
                    trucktitle.setText("Created by");
                    poparray = getDistBoxes(ids);

                    TableAdapter tb = new TableAdapter(getApplicationContext(), poparray);
                    poplist.setAdapter(tb);

                    whom.setText(Html.fromHtml("" + getDriverName(getDriverId(ids)) + ""));
                    truck.setText(Html.fromHtml("<b>" + getDriverName(getCreatedby(ids)) + "</b>" + ""));

                    Button close = (Button) dialog.findViewById(R.id.close);
                    if (getAccStat(mtop) == 0){
                        close.setText("Confirm transaction");
                        close.setTextColor(getResources().getColor(R.color.textcolor));
                        close.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                        close.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dx = new Dialog(BoxReleaseList.this);
                                // Removing the features of Normal Dialogs
                                dx.requestWindowFeature(Window.FEATURE_NO_TITLE);
                                dx.setContentView(R.layout.capture_sign);
                                dx.setCancelable(true);
                                dialogsign(ids);
                                dialog.dismiss();
                            }
                        });
                    }else{
                        close.setText("Close");
                        close.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });
                    }
                    dialog.setCancelable(true);
                    dialog.show();
                }

            });

            lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    TextView textView = (TextView) view.findViewById(R.id.dataid);
                    final String mtop = textView.getText().toString();
                    final AlertDialog.Builder builder = new AlertDialog.Builder(BoxReleaseList.this);
                    builder.setTitle("Delete this transaction?")
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    deleteDistBarcode(mtop);
                                    deleteDistBarcodeBoxnumbers(mtop);
                                    customtype();
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
                    return true;
                }
            });
        }catch (Exception e){}
    }

    public ArrayList<ListItem> getDistBoxes(String trans){
        ArrayList<ListItem> results = new ArrayList<ListItem>();
        SQLiteDatabase db = rate.getReadableDatabase();
        String y = " SELECT * FROM "+rate.tbname_barcode_dist_boxnumber
                +" WHERE "+rate.bardist_bnum_trans+" = '"+trans+"'";
        Cursor res = db.rawQuery(y, null);
        res.moveToFirst();
        int i = 1;
        while (!res.isAfterLast()) {
            String topitem = res.getString(res.getColumnIndex(rate.bardist_bnum_boxnumber));
            String id = res.getString(res.getColumnIndex(rate.bardist_bnum_id));

            ListItem list = new ListItem(id, i+"", topitem, "");
            results.add(list);
            i++;
            res.moveToNext();
        }
        // Add some more dummy data for testing
        return results;
    }

    public void sidenavMain(){
        ListView lv=(ListView)findViewById(R.id.optionsList);//initialization of listview

        final ArrayList<HomeList> listitem = helper.getData(value);

        NavAdapter ad = new NavAdapter(getApplicationContext(), listitem);
        lv.setAdapter(ad);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selected = listitem.get(position).getSubitem();

                select(selected);

            }
        });
    }

    public void subMenu(){
        ListView lv=(ListView)findViewById(R.id.submenu);//initialization of listview

        final ArrayList<HomeList> listitem = helper.getSubmenu();

        NavAdapter ad = new NavAdapter(getApplicationContext(), listitem);
        lv.setAdapter(ad);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String sel = listitem.get(position).getSubitem();
                switch (sel){
                    case "Log Out":
                        final AlertDialog.Builder builder = new AlertDialog.Builder(BoxReleaseList.this);
                        builder.setMessage("Confirm logout?")
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        helper.logout();

                                        startActivity(new Intent(getApplicationContext(), Login.class));
                                        finish();
                                    }
                                })
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.dismiss();
                                    }
                                });
                        // Create the AlertDialog object and show it
                        builder.create().show();
                        break;
                    case "Home":
                        startActivity(new Intent(getApplicationContext(), Home.class));
                        finish();
                        break;
                    case "Add Customer":
                        Intent mIntent = new Intent(getApplicationContext(), AddCustomer.class);
                        mIntent.putExtra("key", "");
                        startActivity(mIntent);
                        finish();
                        break;
                }
            }
        });
    }

    public void select(String data){
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        switch (data){
            case "Acceptance":
                if (value.equals("OIC")){
                    startActivity(new Intent(this, Acceptance.class));
                    finish();
                }
                else if(value.equals("Warehouse Checker")){
                    startActivity(new Intent(this, Acceptance.class));
                    finish();
                }
                else if(value.equals("Partner Portal")){
                    startActivity(new Intent(this, Partner_acceptance.class));
                    finish();
                }
                break;
            case "Distribution":
                if (value.equals("OIC")){
                    startActivity(new Intent(this, Distribution.class));
                    finish();
                }else if (value.equals("Warehouse Checker")){
                    startActivity(new Intent(this, Distribution.class));
                    finish();
                }else if (value.equals("Partner Portal")){
                    startActivity(new Intent(this, Partner_distribution.class));
                    finish();
                }
                break;
            case "Remittance":
                if (value.equals("OIC")){
                    startActivity(new Intent(this, Remittancetooic.class));
                    finish();
                }else if (value.equals("Sales Driver")){
                    startActivity(new Intent(this, Remittancetooic.class));
                    finish();
                }
                break;
            case "Incident Report":
                Intent i = new Intent(this, Incident.class);
                Bundle bundle = new Bundle();
                bundle.putString("module", "Booking");
                //Add the bundle to the intent
                i.putExtras(bundle);
                startActivity(i);
                finish();
                break;
            case "Transactions":
                if (value.equals("OIC")){
                    startActivity(new Intent(this, Oic_Transactions.class));
                    finish();
                }
                else if (value.equals("Sales Driver")){
                    startActivity(new Intent(this, Driver_Transactions.class));
                    finish();
                }else if (value.equals("Warehouse Checker")){
                    startActivity(new Intent(this, Checker_transactions.class));
                    finish();
                }
                break;
            case "Inventory":
                if (value.equals("OIC")){
                    startActivity(new Intent(this, Oic_inventory.class));
                    finish();
                }
                else if (value.equals("Sales Driver")){
                    startActivity(new Intent(this, Driver_Inventory.class));
                    finish();
                }
                else if(value.equals("Warehouse Checker")){
                    startActivity(new Intent(this, Checker_Inventory.class));
                    finish();
                }
                else if(value.equals("Partner Portal")){
                    startActivity(new Intent(this, Partner_inventory.class));
                    finish();
                }
                break;
            case "Loading/Unloading":
                if(value.equals("Warehouse Checker")){
                    startActivity(new Intent(this, Load_home.class));
                    finish();
                }
                else if (value.equals("Partner Portal")){
                    startActivity(new Intent(this, Load_home.class));
                    finish();
                }
                break;
            case "Reservation":
                startActivity(new Intent(this, Reservelist.class));
                finish();
                break;
            case "Booking":
                drawer.closeDrawer(Gravity.START);
                break;
            case "Direct":
                startActivity(new Intent(this, Partner_Maindelivery.class));
                finish();
                break;
            case "Barcode Releasing":
                startActivity(new Intent(this, BoxRelease.class));
                finish();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
            startActivity(new Intent(this, BoxRelease.class));
            finish();
        } else {
            startActivity(new Intent(this, BoxRelease.class));
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.box_release_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.sync_list) {
            network();
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //syncing data reservations
    //connecting to internet
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void network(){
        if (isNetworkAvailable()== true){
            sendPost();
            loadingPost(getWindow().getDecorView().getRootView());
        }else
        {
            Toast.makeText(getApplicationContext(),"Please check internet connection.", Toast.LENGTH_LONG).show();
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

    //send sync data
    public void sendPost() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {

                threadBarcodeRelease();

            }
        });

        thread.start();
    }

    public void threadBarcodeRelease(){
        SQLiteDatabase db = rate.getReadableDatabase();
        String q = " SELECT * FROM " + rate.tbname_barcode_dist
                +" WHERE "+rate.bardist_upds+" = '1' AND "+rate.bardist_accptstat+" = '1'";
        Cursor cx = db.rawQuery(q, null);
        if (cx.getCount() != 0) {
            try {
                String link = helper.getUrl();
                URL url = new URL("http://" + link + "/api/distribution/savebarcoderelease.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);
                conn.setDoInput(true);
                JSONObject jsonParam = new JSONObject();
                db = rate.getReadableDatabase();
                JSONArray finalarray = new JSONArray();
                JSONArray pay = null, reserve = null, boxtypes = null, disc = null;

                String query = " SELECT * FROM " + rate.tbname_barcode_dist
                        +" WHERE "+rate.bardist_upds+" = '1' AND "+rate.bardist_accptstat+" = '1'";
                Cursor c = db.rawQuery(query, null);
                c.moveToFirst();

                while (!c.isAfterLast()) {
                    JSONObject json = new JSONObject();
                    String id = c.getString(c.getColumnIndex(rate.bardist_id));
                    String trans = c.getString(c.getColumnIndex(rate.bardist_trans));
                    String driver = c.getString(c.getColumnIndex(rate.bardist_driverid));
                    String date = c.getString(c.getColumnIndex(rate.bardist_createddate));
                    String by = c.getString(c.getColumnIndex(rate.bardist_createdby));

                    json.put("id", id);
                    json.put("transaction_no", trans);
                    json.put("driver_id", driver);
                    json.put("createddate", date);
                    json.put("createdby", by);
                    boxtypes = getAllBoxNum(trans);

                    json.put("barcodes", boxtypes);
                    ids.add(trans);
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

                Log.i("STATUS", String.valueOf(conn.getResponseCode()));
                Log.i("MSG", conn.getResponseMessage());

                if (!conn.getResponseMessage().equals("OK")){
                    conn.disconnect();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.dismiss();
                            final AlertDialog.Builder builder = new AlertDialog.Builder(BoxReleaseList.this);
                            builder.setTitle("Upload failed")
                                    .setMessage("Data sync has failed, please try again later. thank you.")
                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.dismiss();
                                            recreate();
                                        }
                                    });
                            // Create the AlertDialog object and show it
                            builder.create();
                            builder.setCancelable(false);
                            builder.show();
                        }
                    });
                }else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.dismiss();
                            final AlertDialog.Builder builder = new AlertDialog.Builder(BoxReleaseList.this);
                            builder.setTitle("Information confirmation")
                                    .setMessage("Data upload has been successful, thank you.")
                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            updateReleaseList(ids);
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
        }else{
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressBar.dismiss();
                    final AlertDialog.Builder builder = new AlertDialog.Builder(BoxReleaseList.this);
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

    //update booking upload status
    public void updateReleaseList(ArrayList<String> trans){
        if (trans.size() != 0) {
            SQLiteDatabase db = rate.getWritableDatabase();
            for (String tr : trans) {
                ContentValues cv = new ContentValues();
                cv.put(rate.bardist_upds, "2");
                db.update(rate.tbname_barcode_dist, cv,
                        rate.bardist_trans + " = '" + tr + "' AND " +
                                rate.bardist_upds + " = '1'", null);
                Log.e("upload", "uploaded barcode release");
            }
            db.close();
        }
    }

    public JSONArray getAllBoxNum(String id) {
        SQLiteDatabase myDataBase = rate.getReadableDatabase();
        String raw = "SELECT * FROM " + rate.tbname_barcode_dist_boxnumber
                +" WHERE "+rate.bardist_bnum_trans+" = '"+id+"'";
        Cursor cursor = myDataBase.rawQuery(raw, null);
        JSONArray resultSet = new JSONArray();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            int totalColumn = cursor.getColumnCount();
            JSONObject rowObject = new JSONObject();
            for (int i = 0; i < totalColumn; i++) {
                if (cursor.getColumnName(i) != null) {
                    try {
                        if (cursor.getString(i) != null) {
                            Log.d("TAG_NAME", cursor.getString(i));
                            rowObject.put(cursor.getColumnName(i), cursor.getString(i));
                        } else {
                            rowObject.put(cursor.getColumnName(i), "");
                        }
                    } catch (Exception e) {
                        Log.d("TAG_NAME", e.getMessage());
                    }
                }
            }
            resultSet.put(rowObject);
            cursor.moveToNext();
        }

        cursor.close();
        //Log.e("result set", resultSet.toString());
        return resultSet;
    }

    public int getAccStat(String trans){
        int x = 0;
        SQLiteDatabase db = rate.getReadableDatabase();
        String query = "SELECT * FROM "+rate.tbname_barcode_dist
                +" WHERE "+rate.bardist_trans+" = '"+trans+"'";
        Cursor c = db.rawQuery(query, null);
        if (c.moveToNext()){
            int i  = c.getInt(c.getColumnIndex(rate.bardist_accptstat));
            if (i == 0){
                x = 0;
            }else{
                x = i;
            }
        }
        return x;
    }

    public String getDriverId(String trans){
        String id = null;
        SQLiteDatabase db = rate.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM "+rate.tbname_barcode_dist
        +" WHERE "+rate.bardist_trans+" = '"+trans+"'", null);
        if (c.moveToNext()){
            id = c.getString(c.getColumnIndex(rate.bardist_driverid));
        }
        return id;
    }

    public String getCreatedby(String trans){
        String id = null;
        SQLiteDatabase db = rate.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM "+rate.tbname_barcode_dist
        +" WHERE "+rate.bardist_trans+" = '"+trans+"'", null);
        if (c.moveToNext()){
            id = c.getString(c.getColumnIndex(rate.bardist_createdby));
        }
        return id;
    }

    public String getDriverName(String id){
        String name = null;
        SQLiteDatabase db = gen.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM "+gen.tbname_employee
        +" WHERE "+gen.emp_id+" = '"+id+"'", null);
        if (c.moveToNext()){
            name = c.getString(c.getColumnIndex(gen.emp_first))+" "+c.getString(c.getColumnIndex(gen.emp_last));
        }
        return name;
    }

    public void deleteDistBarcode(String trans){
        SQLiteDatabase db = rate.getWritableDatabase();
        db.delete(rate.tbname_barcode_dist, rate.bardist_trans+" = '"+trans+"'", null);
        db.close();
    }

    public void deleteDistBarcodeBoxnumbers(String trans){
        SQLiteDatabase db = rate.getWritableDatabase();
        db.delete(rate.tbname_barcode_dist_boxnumber, rate.bardist_bnum_trans+" = '"+trans+"'", null);
        db.close();
    }

    public void updateDistBarcode(String trans){
        SQLiteDatabase db = rate.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(rate.bardist_accptstat, "1");
        db.update(rate.tbname_barcode_dist,cv, rate.bardist_trans+" = '"+trans+"'", null);
        Log.e("upd_dist", trans);
        db.close();
    }


    //signature view
    // Function for Digital Signature
    public void dialogsign(final String trans){
        tempDir = Environment.getExternalStorageDirectory() + "/" + getResources().getString(R.string.app_name) + "/";
        ContextWrapper cw = new ContextWrapper(BoxReleaseList.this);
        File directory = cw.getDir(getResources().getString(R.string.app_name), Context.MODE_PRIVATE);

        prepareDirectory();
        uniqueId = getTodaysDate() + "_" + getCurrentTime() + "_" + Math.random();
        current = uniqueId + ".png";
        mypath= new File(directory,current);


        mContent = (LinearLayout)dx.findViewById(R.id.linearLayout);
        mSignature = new BoxReleaseList.signature(BoxReleaseList.this, null);
        mSignature.setBackgroundColor(Color.WHITE);
        mContent.addView(mSignature, ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
        mClear = (Button)dx.findViewById(R.id.clearsign);
        mGetSign = (Button)dx.findViewById(R.id.savesign);
        mGetSign.setEnabled(false);
        mCancel = (Button)dx.findViewById(R.id.cancelsign);
        mView = mContent;

        mClear.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                Log.v("log_tag", "Panel Cleared");
                mSignature.clear();
                mGetSign.setEnabled(false);
            }
        });

        mGetSign.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                //Log.v("log_tag", "Panel Saved");
                boolean error = captureSignature();
                if(!error){
                    mContent.setDrawingCacheEnabled(true);
                    mSignature.save(mView);
                    updateDistBarcode(trans);
                    dx.dismiss();
                }
            }
        });

        mCancel.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                Log.v("log_tag", "Panel Canceled");
                mSignature.clear();
                dx.dismiss();
            }
        });
        dx.show();
    }

    public class signature extends View
    {
        public static final float STROKE_WIDTH = 5f;
        public static final float HALF_STROKE_WIDTH = STROKE_WIDTH / 2;
        public Paint paint = new Paint();
        public Path path = new Path();

        public float lastTouchX;
        public float lastTouchY;
        public final RectF dirtyRect = new RectF();

        public signature(Context context, AttributeSet attrs)
        {
            super(context, attrs);
            paint.setAntiAlias(true);
            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeWidth(STROKE_WIDTH);
        }

        public void save(View v)
        {
            if(mBitmap == null)
            {
                mBitmap =  Bitmap.createBitmap (mContent.getWidth(), mContent.getHeight(), Bitmap.Config.RGB_565);;
            }
            Canvas canvas = new Canvas(mBitmap);
            try
            {
                ByteArrayOutputStream off_byte = new ByteArrayOutputStream();
                FileOutputStream mFileOutStream = new FileOutputStream(mypath);

                v.draw(canvas);
                mBitmap.compress(Bitmap.CompressFormat.PNG, 100, mFileOutStream);
                mBitmap.compress(Bitmap.CompressFormat.PNG, 100, off_byte);
                off = off_byte.toByteArray();
                //bytes.setText(off+"");

                mFileOutStream.flush();
                mFileOutStream.close();
                //String url = MediaStore.Images.Media.insertImage(ReservationData.this.getApplicationContext().getContentResolver(), mBitmap, "title", null);
                Log.e("log_tag","bytes: " + off);

            }
            catch(Exception e)
            {
                Log.v("log_tag", e.toString());
            }
        }

        public void clear()
        {
            path.reset();
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas)
        {
            canvas.drawPath(path, paint);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event)
        {
            float eventX = event.getX();
            float eventY = event.getY();
            mGetSign.setEnabled(true);

            switch (event.getAction())
            {
                case MotionEvent.ACTION_DOWN:
                    path.moveTo(eventX, eventY);
                    lastTouchX = eventX;
                    lastTouchY = eventY;
                    return true;

                case MotionEvent.ACTION_MOVE:

                case MotionEvent.ACTION_UP:

                    resetDirtyRect(eventX, eventY);
                    int historySize = event.getHistorySize();
                    for (int i = 0; i < historySize; i++)
                    {
                        float historicalX = event.getHistoricalX(i);
                        float historicalY = event.getHistoricalY(i);
                        expandDirtyRect(historicalX, historicalY);
                        path.lineTo(historicalX, historicalY);
                    }
                    path.lineTo(eventX, eventY);
                    break;

                default:
                    debug("Ignored touch event: " + event.toString());
                    return false;
            }

            invalidate((int) (dirtyRect.left - HALF_STROKE_WIDTH),
                    (int) (dirtyRect.top - HALF_STROKE_WIDTH),
                    (int) (dirtyRect.right + HALF_STROKE_WIDTH),
                    (int) (dirtyRect.bottom + HALF_STROKE_WIDTH));

            lastTouchX = eventX;
            lastTouchY = eventY;

            return true;
        }

        private void debug(String string){
        }

        private void expandDirtyRect(float historicalX, float historicalY)
        {
            if (historicalX < dirtyRect.left)
            {
                dirtyRect.left = historicalX;
            }
            else if (historicalX > dirtyRect.right)
            {
                dirtyRect.right = historicalX;
            }

            if (historicalY < dirtyRect.top)
            {
                dirtyRect.top = historicalY;
            }
            else if (historicalY > dirtyRect.bottom)
            {
                dirtyRect.bottom = historicalY;
            }
        }

        private void resetDirtyRect(float eventX, float eventY)
        {
            dirtyRect.left = Math.min(lastTouchX, eventX);
            dirtyRect.right = Math.max(lastTouchX, eventX);
            dirtyRect.top = Math.min(lastTouchY, eventY);
            dirtyRect.bottom = Math.max(lastTouchY, eventY);
        }

    }

    private boolean captureSignature() {

        boolean error = false;
        String errorMessage = "";

        if(error){
            Toast toast = Toast.makeText(BoxReleaseList.this, errorMessage, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.TOP, 105, 50);
            toast.show();
        }

        return error;
    }

    private String getTodaysDate() {

        final Calendar c = Calendar.getInstance();
        int todaysDate =     (c.get(Calendar.YEAR) * 10000) +
                ((c.get(Calendar.MONTH) + 1) * 100) +
                (c.get(Calendar.DAY_OF_MONTH));
        Log.w("DATE:",String.valueOf(todaysDate));
        return(String.valueOf(todaysDate));

    }

    private String getCurrentTime() {

        final Calendar c = Calendar.getInstance();
        int currentTime =     (c.get(Calendar.HOUR_OF_DAY) * 10000) +
                (c.get(Calendar.MINUTE) * 100) +
                (c.get(Calendar.SECOND));
        Log.w("TIME:",String.valueOf(currentTime));
        return(String.valueOf(currentTime));

    }

    private boolean prepareDirectory()
    {
        try
        {
            if (makedirs())
            {
                return true;
            } else {
                return false;
            }
        } catch (Exception e)
        {
            e.printStackTrace();
            Toast.makeText(BoxReleaseList.this, "Could not initiate File System.. Is Sdcard mounted properly?",
                    Toast.LENGTH_LONG).show();
            return false;
        }
    }

    private boolean makedirs()
    {
        File tempdir = new File(tempDir);
        if (!tempdir.exists())
            tempdir.mkdirs();

        if (tempdir.isDirectory())
        {
            File[] files = tempdir.listFiles();
            for (File file : files)
            {
                if (!file.delete())
                {
                    System.out.println("Failed to delete " + file);
                }
            }
        }
        return (tempdir.isDirectory());
    }

}
