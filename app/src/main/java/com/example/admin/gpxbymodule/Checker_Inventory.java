package com.example.admin.gpxbymodule;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
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
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class Checker_Inventory extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    HomeDatabase helper;
    GenDatabase gendata;
    RatesDB rate;
    SQLiteDatabase db;
    String value;
    NavigationView navigationView;
    Spinner warehouse, bt;
    ListView lv;
    TextView to;
    String wareid;
    ProgressDialog progressBar;
    ArrayList<ListItem> listitem;
    Three_tableAd three_ad;
    FloatingActionButton addfab;
    Handler handler;
    Thread th;
    TextView tophead, subhead, pricehead, tprice;
    double totalp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checker_inventory);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        helper = new HomeDatabase(getApplicationContext());
        gendata = new GenDatabase(getApplicationContext());
        rate = new RatesDB(getApplicationContext());
        lv = (ListView)findViewById(R.id.lv);
        to = (TextView)findViewById(R.id.total);
        tprice = (TextView)findViewById(R.id.totalprice);
        tophead = (TextView)findViewById(R.id.toptype);
        subhead = (TextView)findViewById(R.id.subtop);
        pricehead = (TextView)findViewById(R.id.subprice);
        addfab = (FloatingActionButton)findViewById(R.id.fab);
        warehouse = (Spinner)findViewById(R.id.warehouse);
        bt = (Spinner)findViewById(R.id.type);

        warehousespinner();

        if (helper.logcount() != 0){
            value = helper.getRole(helper.logcount());
            Log.e("role ", value);
        }
        getBoxesFromDistributionChecker(readBranch());

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        addfab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), Acceptance.class));
                finish();
            }
        });
        scrolllist();
        setNameMail();
        sidenavMain();
        subMenu();

    }

    public void sidenavMain(){
        ListView lv=(ListView)findViewById(R.id.optionsList);//initialization of listview

        final ArrayList<HomeList> listitem = helper.getData(value);

        NavAdapter ad = new NavAdapter(this, listitem);
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
                        final AlertDialog.Builder builder =
                                new AlertDialog.Builder(Checker_Inventory.this);
                        builder.setMessage("Confirm logout?")
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {

                                        helper.logout();

                                        startActivity(new Intent(Checker_Inventory.this, Login.class));
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
                        startActivity(new Intent(Checker_Inventory.this, Login.class));
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
                    startActivity(new Intent(this, Driver_Inventory.class));
                    finish();
                }else if (value.equals("Partner Portal")){
                    startActivity(new Intent(this, Partner_inventory.class));
                    finish();
                }
                else if(value.equals("Warehouse Checker")){
                    drawer.closeDrawer(Gravity.START);
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

    public void warehousespinner(){
        final String[] warehouses = rate.getWarehouseName(helper.getBranch(helper.logcount()+""));
        ArrayAdapter<String> warehouseadapter =
                new ArrayAdapter<>(getApplicationContext(), R.layout.spinneritem,
                        warehouses);
        warehouse.setAdapter(warehouseadapter);
        warehouseadapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        warehouse.setPrompt("Select warehouse");
        warehouse.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                wareid = warehouse.getSelectedItem().toString();
                boxtypes(wareid);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent){
                wareid = warehouse.getSelectedItem().toString();
                boxtypes(wareid);
            }
        });

    }

    public void boxtypes(final String ids){
        try {
            final String[] ware = new String[]{"Empty", "Filled", "With boxnumber","Barcodes"};
            final ArrayAdapter<String> w =
                    new ArrayAdapter<>(getApplicationContext(), R.layout.spinneritem,
                            ware);
            bt.setAdapter(w);
            w.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
            bt.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String x = w.getItem(position);
                    Log.e("wareitem", x);
                    TableAdapter ad;
                    final ArrayList<ListItem> item;
                    switch (x) {
                        case "Empty":
                            tophead.setText("Box type");
                            subhead.setText("Quantity");
                            pricehead.setText("Price");
                            item = countBoxes(getWarehouseId(ids), helper.logcount() + "");
                            three_ad = new Three_tableAd(getApplicationContext(), item);
                            three_ad.notifyDataSetChanged();
                            lv.setAdapter(three_ad);
                            to.setText(Html.fromHtml("<small>Overall total: </small>" +
                                    "<b>" + sumAll(getWarehouseId(ids) + "",
                                    helper.logcount() + "") + " box(s) </b>"));
                            tprice.setVisibility(View.VISIBLE);
                            tprice.setText("Total price : "+totalp+"");
                            break;
                        case "Filled":
                            tophead.setText("#");
                            subhead.setText("Box type");
                            pricehead.setText("Quantity");
                            item = filledBox(helper.logcount() + "", getWarehouseId(ids));
                            three_ad = new Three_tableAd(getApplicationContext(), item);
                            three_ad.notifyDataSetChanged();
                            lv.setAdapter(three_ad);
                            to.setText(Html.fromHtml("<small>Overall total: </small>" +
                                    "<b>" + sumAllfilled() + " box(s) </b>"));
                            tprice.setVisibility(View.INVISIBLE);
                            break;
                        case "With boxnumber":
                            tophead.setText("#");
                            subhead.setText("Box type");
                            pricehead.setText("Quantity");
                            item = getBoxesDistributed("1", "0");
                            three_ad = new Three_tableAd(getApplicationContext(), item);
                            three_ad.notifyDataSetChanged();
                            lv.setAdapter(three_ad);
                            to.setText(Html.fromHtml("<small>Overall total: </small>" +
                                    "<b>" + countWithNum("1","0") + " box(s) </b>"));
                            tprice.setVisibility(View.INVISIBLE);
                            break;
                        default:
                            tophead.setText("Box type");
                            subhead.setText("Quantity");
                            pricehead.setText("Price");
                            item = countBoxes(getWarehouseId(ids), helper.logcount() + "");
                            three_ad = new Three_tableAd(getApplicationContext(), item);
                            three_ad.notifyDataSetChanged();
                            lv.setAdapter(three_ad);
                            to.setText(Html.fromHtml("<small>Overall total: </small>" +
                                    "<b>" + sumAll(getWarehouseId(ids) + "",
                                    helper.logcount() + "") + " box(s) </b>"));
                            tprice.setText(totalp+"");
                            break;
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        }catch (Exception e){}
    }

    //get boxes from inventory
    public ArrayList<ListItem> countBoxes(String warehouse, String user){
        totalp = 0;
        ArrayList<ListItem> results = new ArrayList<>();
        SQLiteDatabase db = gendata.getReadableDatabase();
        String u = " SELECT * FROM "+gendata.tb_acceptance+" WHERE "+gendata.acc_warehouse_id
                +" = '"+warehouse+"' AND "
                +gendata.acc_createdby+" = '"+user+"' AND "+gendata.acc_quantity+" != '0'";
        Cursor c = db.rawQuery(u, null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            String sub = c.getString(c.getColumnIndex(gendata.acc_id));
            String co = c.getString(c.getColumnIndex(gendata.acc_quantity));
            String topitem = c.getString(c.getColumnIndex(gendata.acc_boxtype));
            String bname = "";
            String y = " SELECT "+gendata.box_name+" FROM "+gendata.tbname_boxes
                    +" WHERE "+gendata.box_id+" = '"+topitem+"'";
            Cursor d = db.rawQuery(y, null);
            if (d.moveToNext()){
                bname = d.getString(d.getColumnIndex(gendata.box_name));
            }
            double coprice = Double.valueOf(co);
            ListItem list = new ListItem(sub, bname, ""+co, boxPrice(topitem, coprice)+"");
            results.add(list);
            totalp += boxPrice(topitem, coprice);
            c.moveToNext();
        }
        // Add some more dummy data for testing
        return results;
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

    public String getWarehouseId(String name){
        String id = null;
        SQLiteDatabase db = rate.getReadableDatabase();
        Cursor cx = db.rawQuery(" SELECT "+rate.ware_id+" FROM "+ rate.tbname_warehouse
                +" WHERE "+rate.ware_name+" = '"+name+"'", null);
        if (cx.moveToNext()){
            id = cx.getString(cx.getColumnIndex(rate.ware_id));
        }
        return id;
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
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.bookinglist, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.syncbooking) {
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

    //sum all quantity
    public int sumAll(String id, String user){
        int co = 0;
        SQLiteDatabase db = gendata.getReadableDatabase();
        String xy = " SELECT SUM("+gendata.tb_acceptance+"."+gendata.acc_quantity+") FROM "
                +gendata.tb_acceptance
                +" WHERE "+gendata.acc_warehouse_id+" = '"+id+"' AND "
                +gendata.acc_createdby+" = '"+user+"'";
        Cursor cur = db.rawQuery(xy, null);
        if(cur.moveToFirst())
        {
            co = cur.getInt(0);
        }
        return co;
    }

    public int sumAllfilled(){
        int co = 0;
        SQLiteDatabase db = gendata.getReadableDatabase();
        String r = "SELECT * FROM " + gendata.tbname_accept_boxes
                + " LEFT JOIN " + gendata.tbname_check_acceptance
                + " ON " + gendata.tbname_check_acceptance + "." + gendata.accept_transactionid
                + " = " + gendata.tbname_accept_boxes + "." + gendata.acc_box_transactionno
                + " WHERE " + gendata.tbname_check_acceptance + "." + gendata.accept_createdby + " = '" + helper.logcount() + "'";
        Cursor cur = db.rawQuery(r, null);
        co = cur.getCount();
        return co;
    }

    public int countWithNum(String type, String stat){
        int co = 0;
        SQLiteDatabase db = gendata.getReadableDatabase();
        String r = " SELECT " + gendata.tbname_boxes + "." + gendata.box_name + ", "
                + gendata.tbname_checker_inventory + "." + gendata.chinv_id + ", "
                + gendata.tbname_checker_inventory + "." + gendata.chinv_boxtype + ", "
                + " COUNT (" + gendata.tbname_checker_inventory + "." + gendata.chinv_boxtype + ")"
                + " FROM " + gendata.tbname_checker_inventory
                + " LEFT JOIN " + gendata.tbname_boxes + " ON "
                + gendata.tbname_boxes + "." + gendata.box_id + " = " + gendata.tbname_checker_inventory
                + "." + gendata.chinv_boxtype
                + " WHERE " + gendata.chinv_boxtype_fillempty + " = '" + type + "' AND "
                + gendata.chinv_stat + " = '" + stat + "'";
        Cursor cur = db.rawQuery(r, null);
        if(cur.moveToFirst())
        {
            co = cur.getInt(3);
        }
        return co;
    }

    public ArrayList<ListItem>filledBox(String id, String wareid){
        ArrayList<ListItem> results = new ArrayList<>();
        try {
            SQLiteDatabase db = gendata.getReadableDatabase();
            String r = "SELECT " + gendata.tbname_boxes + "." + gendata.box_id + ","
                    + gendata.tbname_boxes + "." + gendata.box_name
                    + ", COUNT(" + gendata.tbname_boxes + "." + gendata.box_name + "), "
                    + gendata.tbname_accept_boxes + "." + gendata.acc_box_boxnumber
                    + " FROM " + gendata.tbname_accept_boxes
                    + " LEFT JOIN " + gendata.tbname_check_acceptance
                    + " ON " + gendata.tbname_check_acceptance + "." + gendata.accept_transactionid
                    + " = " + gendata.tbname_accept_boxes + "." + gendata.acc_box_transactionno
                    + " LEFT JOIN " + gendata.tbname_boxes
                    + " ON " + gendata.tbname_boxes + "." + gendata.box_id + " = "
                    + gendata.tbname_accept_boxes + "." + gendata.acc_box_boxtype
                    + " WHERE " + gendata.tbname_check_acceptance + "." + gendata.accept_createdby + " = '" + id + "'" +
                    "AND " + gendata.tbname_check_acceptance + "." + gendata.accept_warehouseid + " = '" + wareid + "' GROUP BY "
                    + gendata.tbname_accept_boxes + "." + gendata.acc_box_boxtype;
            Cursor c = db.rawQuery(r, null);
            if (c.getCount() != 0) {
                c.moveToFirst();
                int i = 1;
                while (!c.isAfterLast()) {
                    String ids = c.getString(0);
                    String sub = c.getString(2);
                    String topitem = c.getString(1);
                    ListItem list = new ListItem(ids, i+"", topitem, sub);
                    results.add(list);
                    i++;
                    c.moveToNext();
                }
            }
        }catch (Exception e){}
        return results;
    }

    public ArrayList<ListItem> getBoxesDistributed(String type, String stat){
        ArrayList<ListItem> results = new ArrayList<>();
        try {
            SQLiteDatabase db = gendata.getReadableDatabase();
            String r = " SELECT " + gendata.tbname_boxes + "." + gendata.box_name + ", "
                    + gendata.tbname_checker_inventory + "." + gendata.chinv_id + ", "
                    + gendata.tbname_checker_inventory + "." + gendata.chinv_boxtype + ", "
                    + " COUNT (" + gendata.tbname_checker_inventory + "." + gendata.chinv_boxtype + ")"
                    + " FROM " + gendata.tbname_checker_inventory
                    + " LEFT JOIN " + gendata.tbname_boxes + " ON "
                    + gendata.tbname_boxes + "." + gendata.box_id + " = " + gendata.tbname_checker_inventory
                    + "." + gendata.chinv_boxtype
                    + " WHERE " + gendata.chinv_boxtype_fillempty + " = '" + type + "' AND "
                    + gendata.chinv_stat + " = '" + stat + "' GROUP BY " + gendata.chinv_boxtype;
            Cursor c = db.rawQuery(r, null);
            if (c.getCount() != 0 ) {
                c.moveToFirst();
                int i = 1;
                while (!c.isAfterLast()) {
                    String id = c.getString(1);
                    String topitem = c.getString(0);
                    String subitem = c.getString(3);
                    String boxids = c.getString(2);
                    ListItem list = new ListItem(id, i+"", topitem, subitem);
                    results.add(list);
                    i++;
                    c.moveToNext();
                }
            }
        }catch (Exception e){}
        return results;
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

    public void threadwarehouseInventory(){
        SQLiteDatabase db = gendata.getReadableDatabase();
        String q = " SELECT * FROM "+gendata.tb_acceptance+" WHERE "
                +gendata.acc_createdby+" = '"+helper.logcount()+"' AND "+gendata.acc_upds+" = '1'";
        Cursor cx = db.rawQuery(q, null);
        if (cx.getCount() != 0) {
            //THREAD FOR warehouse API
            try {
                String link = helper.getUrl();
                URL url = new URL("http://" + link + "/api/warehouseinventory/save.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);
                conn.setDoInput(true);

                JSONObject jsonParam = new JSONObject();
                jsonParam.accumulate("data", getWarehouseInventory(helper.logcount()+""));

                Log.e("JSON", jsonParam.toString());
                DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                os.writeBytes(jsonParam.toString());
                os.flush();
                os.close();
                Log.i("STATUS", String.valueOf(conn.getResponseCode()));
                Log.i("MSG", conn.getResponseMessage());
                if (!conn.getResponseMessage().equals("OK")){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.dismiss();
                            final AlertDialog.Builder builder
                                    = new AlertDialog.Builder(Checker_Inventory.this);
                            builder.setTitle("Upload failed")
                                    .setMessage("Data upload failed, please try again later. thank you.")
                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.dismiss();
                                        }
                                    });
                            // Create the AlertDialog object and show it
                            builder.create().show();
                        }
                    });
                }else{
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.dismiss();
                            final AlertDialog.Builder builder
                                    = new AlertDialog.Builder(Checker_Inventory.this);
                            builder.setTitle("Information confirmation")
                                    .setMessage("Data upload has been successful, thank you.")
                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            updateWarehouseInv("2");
                                            dialog.dismiss();
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
                    final AlertDialog.Builder builder = new AlertDialog.Builder(Checker_Inventory.this);
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
        //END THREAD WAREHOUSE API
    }

    //json data of inventory
    public JSONArray getWarehouseInventory(String x) {
        SQLiteDatabase myDataBase = gendata.getReadableDatabase();
        String raw = "SELECT "+gendata.tb_acceptance+"."+gendata.acc_id+", "
                +gendata.tb_acceptance+"."+gendata.acc_warehouse_id+", "
                +gendata.tb_acceptance+"."+gendata.acc_name+", "
                +gendata.tb_acceptance+"."+gendata.acc_boxtype+", "
                +gendata.tb_acceptance+"."+gendata.acc_quantity+", "
                +gendata.tb_acceptance+"."+gendata.acc_createddate+", "
                +gendata.tb_acceptance+"."+gendata.acc_createdby+", "
                +gendata.tb_acceptance+"."+gendata.acc_status+""
                +" FROM " + gendata.tb_acceptance+" WHERE "
                +gendata.acc_createdby+" = '"+x+"' AND "+gendata.acc_upds+" = '1'";
        Cursor cursor = myDataBase.rawQuery(raw, null);

        JSONArray resultSet = new JSONArray();
        JSONArray boxtypes = new JSONArray();

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            try {
                JSONObject json = new JSONObject();
                String id = cursor.getString(cursor.getColumnIndex(gendata.acc_id));
                String warehouse = cursor.getString(cursor.getColumnIndex(gendata.acc_warehouse_id));
                String name = cursor.getString(cursor.getColumnIndex(gendata.acc_name));
                String by = cursor.getString(cursor.getColumnIndex(gendata.acc_createdby));
                String date = cursor.getString(cursor.getColumnIndex(gendata.acc_createddate));
                String stat = cursor.getString(cursor.getColumnIndex(gendata.acc_status));
                String bty = cursor.getString(cursor.getColumnIndex(gendata.acc_boxtype));
                String q = cursor.getString(cursor.getColumnIndex(gendata.acc_quantity));
                json.put("id", id);
                json.put("warehouse_id", warehouse);
                json.put("manufacturer_name", name);
                json.put("boxtype_id", bty);
                json.put("quantity", q);
                json.put("createddate", date);
                json.put("createdby", by);
                json.put("status", stat);
                json.put("price", totalp);
                resultSet.put(json);

                cursor.moveToNext();
            }catch (Exception e){}
        }

        cursor.close();
        //Log.e("result set", resultSet.toString());
        return resultSet;
    }

    //send sync data
    public void sendPost(){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {

                threadwarehouseInventory();

            }
        });

        thread.start();
    }

    public void returnViewItems(String id){
        ArrayList<ListItem> poparray;
        final Dialog dv = new Dialog(Checker_Inventory.this);
        dv.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dv.setCancelable(true);
        dv.setContentView(R.layout.driver_inventoryview);

        final TextView bt = (TextView) dv.findViewById(R.id.btypetitle);
        ListView poplist = (ListView)dv.findViewById(R.id.list);

        poparray = getBoxesNumbers(id);

        TableAdapter tb = new TableAdapter(getApplicationContext(), poparray);
        poplist.setAdapter(tb);

        bt.setText(getBoxname(id));

        ImageButton close = (ImageButton) dv.findViewById(R.id.close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dv.dismiss();
            }
        });
        dv.show();
    }

    public void returnViewItemsWithNum(String id){
        ArrayList<ListItem> poparray;
        final Dialog dialog = new Dialog(Checker_Inventory.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.driver_inventoryview);

        final TextView bt = (TextView) dialog.findViewById(R.id.btypetitle);
        ListView poplist = (ListView)dialog.findViewById(R.id.list);

        poparray = getBoxesNumbersBoxtypes(id);

        TableAdapter tb = new TableAdapter(getApplicationContext(), poparray);
        poplist.setAdapter(tb);

        bt.setText(getBoxname(id));

        ImageButton close = (ImageButton) dialog.findViewById(R.id.close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public ArrayList<ListItem> getBoxesNumbers(String id) {
        ArrayList<ListItem> result = new ArrayList<>();
        SQLiteDatabase db = gendata.getReadableDatabase();
        String get = " SELECT * FROM "+gendata.tbname_accept_boxes
                +" WHERE "+gendata.acc_box_boxtype+" = '"+id+"' AND "
                +gendata.acc_box_stat+" = '2'";
        Cursor f = db.rawQuery(get, null);
        f.moveToFirst();
        while (!f.isAfterLast()){
            String bid = f.getString(f.getColumnIndex(gendata.acc_box_id));
            String bname = getBoxname(f.getString(f.getColumnIndex(gendata.acc_box_boxtype)));
            String bnum = f.getString(f.getColumnIndex(gendata.acc_box_boxnumber));
            ListItem item = new ListItem(bid, bname, bnum,"");
            result.add(item);
            f.moveToNext();
        }
        return result;
    }

    public ArrayList<ListItem> getBoxesNumbersBoxtypes(String id) {
        ArrayList<ListItem> result = new ArrayList<>();
        SQLiteDatabase db = gendata.getReadableDatabase();
        String get = " SELECT * FROM "+gendata.tbname_checker_inventory
                +" LEFT JOIN "+gendata.tbname_boxes
                +" ON "+gendata.tbname_boxes+"."+gendata.box_id+" = "+gendata.tbname_checker_inventory
                +"."+gendata.chinv_boxtype
                +" WHERE "+gendata.chinv_boxtype+" = '"+id+"'";
        Cursor f = db.rawQuery(get, null);
        f.moveToFirst();
        while (!f.isAfterLast()){
            String bid = f.getString(f.getColumnIndex(gendata.chinv_id));
            String bname = f.getString(f.getColumnIndex(gendata.box_name));
            String bnum = f.getString(f.getColumnIndex(gendata.chinv_boxnumber));
            ListItem item = new ListItem(bid, bname, bnum,"");
            result.add(item);
            f.moveToNext();
        }
        return result;
    }

    public String getBoxname(String id){
        String name = "";
        SQLiteDatabase db = gendata.getReadableDatabase();
        Cursor x = db.rawQuery(" SELECT * FROM "+gendata.tbname_boxes
        +" WHERE "+gendata.box_id+" = '"+id+"'", null);
        if (x.moveToNext()){
            name = x.getString(x.getColumnIndex(gendata.box_name));
        }
        return name;
    }

    public void getBoxesFromDistributionChecker(String x){
            SQLiteDatabase db = gendata.getReadableDatabase();
            Cursor c = db.rawQuery(" SELECT * FROM " + gendata.tbname_tempboxes
                    + " JOIN " + gendata.tbname_tempDist
                    + " WHERE " + gendata.tbname_tempDist + "."
                    + gendata.temp_typename + " = '" + x + "'", null);
            c.moveToFirst();
            while (!c.isAfterLast()) {
                String topitem = c.getString(c.getColumnIndex(gendata.dboxtemp_boxid));
                String bnum = c.getString(c.getColumnIndex(gendata.dboxtemp_boxnumber));
                gendata.addtoCheckerInv(topitem, bnum, "1", "0");
                Log.e("boxtoinv", "type:" + topitem + ", boxnum: " + bnum);
                c.moveToNext();
            }
            c.close();
            db.close();
    }

    public String readBranch(){
        String branchname = "";
        SQLiteDatabase db = rate.getReadableDatabase();
        Cursor x = db.rawQuery(" SELECT * FROM "+rate.tbname_branch
                +" WHERE "+rate.branch_id+" = '"+helper.getBranch(""+helper.logcount())+"'", null);
        if (x.moveToNext()){
            branchname = x.getString(x.getColumnIndex(rate.branch_name));
        }
        return branchname;
    }

    public void updateWarehouseInv(String stat){
        SQLiteDatabase db = gendata.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(gendata.acc_upds, stat);
        db.update(gendata.tb_acceptance, cv,
                gendata.acc_upds+" = '1'", null);
        Log.e("upload", "uploaded inventory");
        db.close();
    }

    public double boxPrice(String boxid, double quant){
        double pr = 0;
        SQLiteDatabase db = gendata.getReadableDatabase();
        String x = " SELECT "+gendata.box_depositprice+" FROM "+gendata.tbname_boxes
                +" WHERE "+gendata.box_id+" = '"+boxid+"'";
        Cursor c = db.rawQuery(x, null);
        if (c.moveToNext()){
            double tr = Double.valueOf(c.getString(c.getColumnIndex(gendata.box_depositprice)));
            pr = (tr * quant);
        }
        c.close();
        return pr;
    }

}
