package com.example.admin.gpxbymodule;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.text.Html;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
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
import java.util.ArrayList;

public class Driver_Inventory extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    HomeDatabase helper;
    GenDatabase gendata;
    RatesDB rate;
    String value;
    TextView bookcount, reservecount;
    SQLiteDatabase db;
    NavigationView navigationView;
    ListView lv;
    ArrayList<ListItem> listitem;
    ArrayList<LinearItem> linear;
    Spinner spin;
    TextView to;
    ProgressDialog progressBar;
    TextView topt,subt;
    String topi;
    int equals = 0;
    TableAdapter ad;
    private int SETTINGS_ACTION = 100;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        preference();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_inventory);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        helper = new HomeDatabase(getApplicationContext());
        gendata = new GenDatabase(getApplicationContext());
        rate = new RatesDB(getApplicationContext());
        lv = (ListView)findViewById(R.id.lv);
        spin = (Spinner)findViewById(R.id.trans);
        to = (TextView)findViewById(R.id.total);
        topt = (TextView)findViewById(R.id.toptype);
        subt = (TextView)findViewById(R.id.subtop);

        if (helper.logcount() != 0){
            value = helper.getRole(helper.logcount());
            Log.e("role ", value);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        getBoxesFromDistribution(helper.getFullname(helper.logcount()+""));
        Log.e("drivername", helper.getFullname(helper.logcount()+""));
        scrolllist();
        spinnerlist();
        setNameMail();
        sidenavMain();
        subMenu();
    }

    public void sidenavMain(){
        ListView lv=(ListView)findViewById(R.id.optionsList);//initialization of listview

        final ArrayList<HomeList> listitem = helper.getData(value);

        NavAdapter ad = new NavAdapter(Driver_Inventory.this, listitem);
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
                        final AlertDialog.Builder builder = new AlertDialog.Builder(Driver_Inventory.this);
                        builder.setMessage("Confirm logout?")
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        helper.logout();
                                        startActivity(new Intent(Driver_Inventory.this, Login.class));
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
                    startActivity(new Intent(this, Remitt.class));
                    finish();
                }else if (value.equals("Sales Driver")){
                    startActivity(new Intent(this, Remitt.class));
                    finish();
                }
                break;
            case "Incident Report":
                Intent i = new Intent(this, Incident.class);
                Bundle bundle = new Bundle();
                bundle.putString("module", "Inventory");
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
                    drawer.closeDrawer(Gravity.START);
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
                startActivity(new Intent(this, Bookinglist.class));
                finish();
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

    public void returnViewItems(String id,String type, String s){
        ArrayList<ListItem> poparray;
        final Dialog dialog = new Dialog(Driver_Inventory.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.driver_inventoryview);

        final TextView bt = (TextView) dialog.findViewById(R.id.btypetitle);
        ListView poplist = (ListView)dialog.findViewById(R.id.list);

        poparray = getBoxesNumbers(getIDBoxItem(id), type, s);

        TableAdapter tb = new TableAdapter(getApplicationContext(), poparray);
        poplist.setAdapter(tb);

        bt.setText(id);

        ImageButton close = (ImageButton) dialog.findViewById(R.id.close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public void scrolllist(){
        lv.setOnTouchListener(new View.OnTouchListener() {
            // Setting on Touch Listener for handling the touch inside ScrollView
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });
    }

    public ArrayList<ListItem> getBoxesNumbers(String id, String type, String stat) {
        ArrayList<ListItem> result = new ArrayList<>();
        SQLiteDatabase db = gendata.getReadableDatabase();
        String get = " SELECT * FROM "+gendata.tbname_driver_inventory
                +" LEFT JOIN "+gendata.tbname_boxes+" ON "+gendata.tbname_boxes
                +"."+gendata.box_id+" = "+gendata.tbname_driver_inventory+"."+gendata.sdinv_boxtype
                +" WHERE "+gendata.sdinv_boxtype+" = '"+id+"' AND "+gendata.tbname_driver_inventory
                +"."+gendata.sdinv_stat+" = '"+stat+"' AND "+gendata.tbname_driver_inventory
                +"."+gendata.sdinv_boxtype_fillempty+" = '"+type+"'";
        Cursor f = db.rawQuery(get, null);
        f.moveToFirst();
        int i = 1;
        while (!f.isAfterLast()){
            String bid = f.getString(f.getColumnIndex(gendata.sdinv_id));
            String bname = f.getString(f.getColumnIndex(gendata.box_name));
            String bnum = f.getString(f.getColumnIndex(gendata.sdinv_boxnumber));
            if (!checkAcceptance(bnum)) {
                ListItem item = new ListItem(bid, i + "", bnum, "");
                result.add(item);
                equals = i;
                i++;
            }
            f.moveToNext();
        }
        return result;
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

    public void spinnerlist(){
        try {
            String[] items = new String[]{"Empty box", "Filled box","Barcodes"};
            ArrayAdapter<String> adapter =
                    new ArrayAdapter<>(getApplicationContext(), R.layout.spinneritem,
                            items);
            spin.setAdapter(adapter);
            adapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);

            spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent,
                                           View view, int position, long id) {
                    String x = spin.getSelectedItem().toString();
                    switch (x) {
                        case "Empty box":
                            topt.setText("Boxtype");
                            subt.setText("Quantity");
                            listitem = getEmptyBoxes("0", "0");
                            ad = new TableAdapter(getApplicationContext(), listitem);
                            lv.setAdapter(ad);
                            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    String topi = listitem.get(position).getTopitem();
                                    returnViewItems(topi,"0", "0");
                                    Log.e("topiteminvemp", topi);
                                }
                            });
                            to.setVisibility(View.VISIBLE);
                            to.setText(Html.fromHtml("<small>Overall total: </small>" +
                                    "<b>" + countAll("0", "0") + " box(s) </b>"));
                            break;
                        case "Filled box":
                            topt.setText("Boxtype");
                            subt.setText("Action");
                            listitem = getFilledNotInAcceptance("1", "2");
                            ad = new TableAdapter(getApplicationContext(), listitem);
                            lv.setAdapter(ad);
                            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    topi = listitem.get(position).getTopitem();
                                    returnViewItems(topi,"1", "2");
                                    Log.e("topiteminvemp", topi);
                                }
                            });
                            to.setVisibility(View.INVISIBLE);
                            break;
                        case "Barcodes":
                            topt.setText("Boxtype");
                            subt.setText("Box No.");
                            listitem = getBarcodes("0");
                            TableAdapter ad = new TableAdapter(Driver_Inventory.this, listitem);
                            lv.setAdapter(ad);
                            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    Log.e("clicked", "no");
                                }
                            });
                            to.setVisibility(View.VISIBLE);
                            to.setText(Html.fromHtml("<small>Overall total: </small>" +
                                    "<b>" + countAllBarcode("0") + " pcs. </b>"));
                            break;
                        default :
                            topt.setText("Boxtype");
                            subt.setText("Quantity");
                            listitem = getEmptyBoxes("0", "0");
                            ad = new TableAdapter(getApplicationContext(), listitem);
                            lv.setAdapter(ad);
                            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    String topi = listitem.get(position).getTopitem();
                                    returnViewItems(topi,"0", "0");
                                    Log.e("topiteminvemp", topi);
                                }
                            });
                            to.setVisibility(View.VISIBLE);
                            to.setText(Html.fromHtml("<small>Overall total: </small>" +
                                    "<b>" + countAll("0", "0") + " box(s) </b>"));
                            break;
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    topt.setText("Boxtype");
                    subt.setText("Quantity");
                    listitem = getEmptyBoxes("0", "0");
                    ad = new TableAdapter(getApplicationContext(), listitem);
                    lv.setAdapter(ad);
                    lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            String topi = listitem.get(position).getTopitem();
                            returnViewItems(topi,"0", "0");
                            Log.e("topiteminvemp", topi);
                        }
                    });
                    to.setVisibility(View.VISIBLE);
                    to.setText(Html.fromHtml("<small>Overall total: </small>" +
                            "<b>" + countAll("0", "0") + " box(s) </b>"));
                }
            });
        }catch (Exception e){}
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
            startActivity(new Intent(this, Home.class));
            finish();
        } else {
            startActivity(new Intent(this, Home.class));
            finish();
        }
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

    public void getBoxesFromDistribution(String x){
        try {
            SQLiteDatabase db = gendata.getReadableDatabase();
            Cursor c = db.rawQuery(" SELECT * FROM " + gendata.tbname_tempboxes
                    + " LEFT JOIN " + gendata.tbname_tempDist+" ON "
                    +gendata.tbname_tempDist+"."+gendata.temp_transactionnumber
                    +" = "+gendata.tbname_tempboxes+"."+gendata.dboxtemp_distributionid
                    + " WHERE " + gendata.tbname_tempDist + "." + gendata.temp_typename
                    + " = '" + x + "'", null);
            c.moveToFirst();
            while (!c.isAfterLast()) {
                String topitem = c.getString(c.getColumnIndex(gendata.dboxtemp_boxid));
                String bnum = c.getString(c.getColumnIndex(gendata.dboxtemp_boxnumber));
                Log.e("error", checkAcceptance(bnum)+"");
                if (!checkAcceptance(bnum)) {
                   gendata.addtoDriverInv(topitem, bnum, "0", "0");
                    Log.e("boxtoinv", "type:" + topitem + ", boxnum: " + bnum);
                }else{
                    deleteDriverInv(bnum);
                }
                c.moveToNext();
            }
        }catch (Exception e){}
    }

    public String countAll(String type, String stat){
        SQLiteDatabase db = gendata.getReadableDatabase();
        String que = " SELECT * FROM "+gendata.tbname_driver_inventory
                +" WHERE "+gendata.sdinv_boxtype_fillempty+" = '"+type+"' AND "
                +gendata.sdinv_stat+" = '"+stat+"' AND "+gendata.sdinv_stat+" != '3'";
        Cursor c = db.rawQuery(que, null);
        return c.getCount()+"";
    }

    public String countAllBarcode(String stat){
        SQLiteDatabase db = rate.getReadableDatabase();
        String r = " SELECT * FROM "+rate.tbname_barcode_driver_inventory
                +" WHERE "+rate.barcodeDriverInv_status+" = '"+stat+"'";
        Cursor c = db.rawQuery(r, null);
        return c.getCount()+"";
    }

    public ArrayList<ListItem> getEmptyBoxes(String type, String stat){
        ArrayList<ListItem> results = new ArrayList<>();
        try {
            SQLiteDatabase db = gendata.getReadableDatabase();
            String r = " SELECT " + gendata.tbname_boxes + "." + gendata.box_name + ", "
                    + gendata.tbname_driver_inventory + "." + gendata.sdinv_id + ", "
                    + " COUNT (" + gendata.tbname_driver_inventory + "." + gendata.sdinv_boxtype + ")"
                    + " FROM " + gendata.tbname_driver_inventory
                    + " LEFT JOIN " + gendata.tbname_boxes + " ON "
                    + gendata.tbname_boxes + "." + gendata.box_id + " = " + gendata.tbname_driver_inventory
                    + "." + gendata.sdinv_boxtype
                    + " WHERE " + gendata.sdinv_boxtype_fillempty + " = '" + type + "' AND "
                    + gendata.sdinv_stat + " = '" + stat + "' GROUP BY " + gendata.sdinv_boxtype;
            Cursor c = db.rawQuery(r, null);
            c.moveToFirst();
            while (!c.isAfterLast()) {
                String id = c.getString(1);
                String topitem = c.getString(c.getColumnIndex(gendata.box_name));
                String subitem = c.getString(2);
                ListItem list = new ListItem(id, topitem, subitem, null);
                results.add(list);
                c.moveToNext();
            }
        }catch (Exception e){}
        return results;
    }

    public ArrayList<ListItem> getBarcodes(String stat){
        ArrayList<ListItem> results = new ArrayList<>();
        try {
            SQLiteDatabase db = rate.getReadableDatabase();
            String r = " SELECT * FROM "+rate.tbname_barcode_driver_inventory
                    +" WHERE "+rate.barcodeDriverInv_status+" = '"+stat+"'";
            Cursor c = db.rawQuery(r, null);
            c.moveToFirst();
            int i = 1;
            while (!c.isAfterLast()) {
                String id = c.getString(c.getColumnIndex(rate.barcodeDriverInv_id));
                String subitem = c.getString(c.getColumnIndex(rate.barcodeDriverInv_boxnumber));
                if (checkInBooking(subitem)){
                    rate.updateBarDriverInv(subitem,"1");
                }else {
                    ListItem list = new ListItem(id, i + ". Open", subitem, null);
                    results.add(list);
                    i++;
                }
                c.moveToNext();
            }
        }catch (Exception e){}
        return results;
    }

    public ArrayList<ListItem> getFilledNotInAcceptance(String type, String stat){
        ArrayList<ListItem> results = new ArrayList<>();
        try {
            SQLiteDatabase db = gendata.getReadableDatabase();
            String r = " SELECT " + gendata.tbname_boxes + "." + gendata.box_name + ", "
                    + gendata.tbname_driver_inventory + "." + gendata.sdinv_id + ", "
                    + " COUNT (" + gendata.tbname_driver_inventory + "." + gendata.sdinv_boxtype + "), "
                    + gendata.tbname_driver_inventory + "." + gendata.sdinv_boxnumber +","
                    + gendata.tbname_boxes + "." + gendata.box_id +""
                    + " FROM " + gendata.tbname_driver_inventory
                    + " LEFT JOIN " + gendata.tbname_boxes + " ON "
                    + gendata.tbname_boxes + "." + gendata.box_id + " = " + gendata.tbname_driver_inventory
                    + "." + gendata.sdinv_boxtype
                    + " WHERE " + gendata.sdinv_boxtype_fillempty + " = '" + type + "' AND "
                    + gendata.sdinv_stat + " = '" + stat + "' GROUP BY " + gendata.sdinv_boxtype;
            Cursor c = db.rawQuery(r, null);
            c.moveToFirst();
            int i = 1;
            while (!c.isAfterLast()) {
                String id = c.getString(1);
                String topitem = c.getString(c.getColumnIndex(gendata.box_name));
                String numb = c.getString(c.getColumnIndex(gendata.sdinv_boxnumber));
                if (!checkAcceptance(numb)) {
                    ListItem list = new ListItem(id, topitem + "",
                            Html.fromHtml("<b><i><u>view</u></i></b>") + "", null);
                    results.add(list);
                    i++;
                }
                c.moveToNext();
            }
        }catch (Exception e){}
        return results;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.driver__inventory, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.syncdriverinventory) {
            network();
        }
        else if (id ==  R.id.passincident){
            Intent i = new Intent(this, Incident.class);
            Bundle bundle = new Bundle();
            bundle.putString("module", "Inventory");
            //Add the bundle to the intent
            i.putExtras(bundle);
            startActivity(i);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    //sync methods
    public void network(){
        if (isNetworkAvailable()== true){
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
        // Create a Handler instance on the main thread
        final Handler handler = new Handler();

        // Create and start a new Thread
        new Thread(new Runnable() {
            public void run() {
                try{
                    getSalesDriverDistribution();
                    Thread.sleep(10000);
                }
                catch (Exception e) { } // Just catch the InterruptedException

                handler.post(new Runnable() {
                    public void run() {
                        progressBar.dismiss();
                        final AlertDialog.Builder builder
                                = new AlertDialog.Builder(Driver_Inventory.this);
                        builder.setTitle("Information confirmation")
                                .setMessage("Data update has been successful, thank you.")
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
            }
        }).start();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void getSalesDriverDistribution(){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String resp = null;
                    String link = helper.getUrl();
                    String urlget = "http://"+link+"/api/distribution/get_salesdriver.php";
                    URL url = new URL(urlget);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    // read the response
                    InputStream in = new BufferedInputStream(conn.getInputStream());
                    resp = convertStreamToString(in);

                    if (resp != null) {
                        Log.e("Distributions", "distributions: " + resp);
                        JSONArray jsonArray = new JSONArray(resp);
                        for(int i=0; i<jsonArray.length(); i++){
                            JSONObject json_data = jsonArray.getJSONObject(i);
                            String id = json_data.getString("id");
                            String distribution_type = json_data.getString("distribution_type");
                            String destination_name = json_data.getString("destination_name");
                            String truck_number = json_data.getString("truck_number");
                            String remarks = json_data.getString("remarks");
                            String createddate = json_data.getString("createddate");
                            String createdby = json_data.getString("createdby");
                            String[] box_number = json_data.getString("box_number").split(",");
                            String[] boxtypeid = json_data.getString("boxtype_id").split(",");

                            gendata.addDistribution(id,distribution_type, destination_name,truck_number,
                                    remarks, "1", "2", createddate, createdby, 1, null);
                            for (int ix = 0; ix < boxtypeid.length; ix++){
                                gendata.addTempBoxDist( id, boxtypeid[ix],"", box_number[ix], "2");

                                Log.e("boxesdist","boxtype: "+boxtypeid[ix]+"" +
                                        ", boxnumber: "+box_number[ix]);
                            }
                        }
                        if (!conn.getResponseMessage().equals("OK")){
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progressBar.dismiss();
                                    final AlertDialog.Builder builder
                                            = new AlertDialog.Builder(Driver_Inventory.this);
                                    builder.setTitle("Information confirmation")
                                            .setMessage("Data update has failed, please try again later. thank you.")
                                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    dialog.dismiss();
                                                }
                                            });
                                    // Create the AlertDialog object and show it
                                    builder.create().show();
                                }
                            });
                        }

                    } else {
                        Log.e("Error", "Couldn't get data from server.");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    public String getDriverFullName(String id){
        String full = "";
        SQLiteDatabase db = gendata.getReadableDatabase();
        String que = " SELECT * FROM "+gendata.tbname_employee
                +" WHERE "+gendata.emp_id+" = '"+id+"'";
        Cursor x = db.rawQuery(que, null);
        if (x.moveToNext()){
            full = x.getString(x.getColumnIndex(gendata.emp_first))+" "+x.getString(x.getColumnIndex(gendata.emp_last));
        }
        return full;
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

    public String getIDBoxItem(String id){
        String n = "";
        SQLiteDatabase db = gendata.getReadableDatabase();
        String x = " SELECT * FROM "+gendata.tbname_boxes
                +" WHERE "+gendata.box_name+" = '"+id+"'";
        Cursor v = db.rawQuery(x, null);
        if (v.moveToNext()){
            n = v.getString(v.getColumnIndex(gendata.box_id));
        }
        return n;
    }

    public boolean checkAcceptance(String bn){
        SQLiteDatabase db = gendata.getReadableDatabase();
        String x = " SELECT * FROM "+gendata.tbname_accept_boxes
                +" WHERE "+gendata.acc_box_boxnumber+" = '"+bn+"'";
        Cursor c = db.rawQuery(x, null);
        if (c.getCount() > 0 ){
             return true;
        }else{
            return false;
        }
    }

    public boolean checkCheckerInv(String bn){
        SQLiteDatabase db = gendata.getReadableDatabase();
        String x = " SELECT * FROM "+gendata.tbname_checker_inventory
                +" WHERE "+gendata.chinv_boxnumber+" = '"+bn+"'";
        Cursor c = db.rawQuery(x, null);
        if (c.getCount() != 0 ){
             return true;
        }else{
            return false;
        }
    }

    public void deleteDriverInv(String bn){
        SQLiteDatabase db = gendata.getWritableDatabase();
        db.delete(gendata.tbname_driver_inventory,
                gendata.sdinv_boxnumber+" = '"+bn+"'", null);
        Log.e("delete-sdinv", bn);
        db.close();
    }

    public boolean checkInBooking(String bn){
        SQLiteDatabase db = gendata.getReadableDatabase();
        String x = " SELECT * FROM "+gendata.tbname_booking_consignee_box
                +" WHERE "+gendata.book_con_box_number+" = '"+bn+"'";
        Cursor c = db.rawQuery(x, null);
        if (c.getCount() != 0 ){
             return true;
        }else{
            return false;
        }
    }

    //shared preference
    public void preference(){
        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(this);
        String themeName = pref.getString("theme", "Theme1");
        if (themeName.equals("Default(Red)")) {
            setTheme(R.style.AppTheme);
        } else if (themeName.equals("Light Blue")) {
            setTheme(R.style.customtheme);
        }else if (themeName.equals("Green")) {
            setTheme(R.style.customgreen);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SETTINGS_ACTION) {
            if (resultCode == Preferences.RESULT_CODE_THEME_UPDATED) {
                finish();
                startActivity(getIntent());
                return;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}
