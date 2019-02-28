package com.example.admin.gpxbymodule;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.text.Html;
import android.util.Base64;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class Oic_inventory extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    HomeDatabase helper;
    RatesDB rate;
    String role;
    GenDatabase gen;
    ListView lv;
    FloatingActionButton fab;
    TextView total;
    NavigationView navigationView;
    Spinner warehouse, barcx;
    ProgressDialog progressBar;
    double totalp;
    TextView tophead, subtop, pr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oic_inventory);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        helper = new HomeDatabase(this);
        rate = new RatesDB(this);
        gen = new GenDatabase(getApplicationContext());
        lv = (ListView)findViewById(R.id.lv);
        total = (TextView)findViewById(R.id.total);
        tophead = (TextView)findViewById(R.id.toptype);
        subtop = (TextView)findViewById(R.id.subtop);
        pr = (TextView)findViewById(R.id.subprice);
        warehouse = (Spinner)findViewById(R.id.warehouse);
        barcx = (Spinner)findViewById(R.id.barcode_empty);

        boxclass();

        try{
            if (helper.logcount() != 0){
                role = helper.getRole(helper.logcount());
                Log.e("role ", role);
            }
        }catch (Exception e){}

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        setNameMail();
        sidenavMain();
        subMenu();
        scrolllist();

        try{
            fab = (FloatingActionButton) findViewById(R.id.addnew);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(getApplicationContext(), Acceptance.class));
                    finish();
                }
            });
        }catch (Exception e){}

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

    public void sidenavMain(){
        ListView lv=(ListView)findViewById(R.id.optionsList);//initialization of listview

        final ArrayList<HomeList> listitem = helper.getData(role);

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
                        final AlertDialog.Builder builder = new AlertDialog.Builder(Oic_inventory.this);
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

    public void select(String data){
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        switch (data){
            case "Acceptance":
                if (role.equals("OIC")){
                    startActivity(new Intent(this, Acceptance.class));
                    finish();
                }
                else if(role.equals("Warehouse Checker")){
                    startActivity(new Intent(this, Acceptance.class));
                    finish();
                }
                else if(role.equals("Partner Portal")){
                    startActivity(new Intent(this, Partner_acceptance.class));
                    finish();
                }
                break;
            case "Distribution":
                if (role.equals("OIC")){
                    startActivity(new Intent(this, Distribution.class));
                    finish();
                }else if (role.equals("Warehouse Checker")){
                    startActivity(new Intent(this, Distribution.class));
                    finish();
                }else if (role.equals("Partner Portal")){
                    startActivity(new Intent(this, Partner_distribution.class));
                    finish();
                }
                break;
            case "Remittance":
                if (role.equals("OIC")){
                    startActivity(new Intent(this, Remitt.class));
                    finish();
                }else if (role.equals("Sales Driver")){
                    startActivity(new Intent(this, Remitt.class));
                    finish();
                }
                break;
            case "Barcode Releasing":
                startActivity(new Intent(this, BoxRelease.class));
                finish();
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
                if (role.equals("OIC")){
                    startActivity(new Intent(this, Oic_Transactions.class));
                    finish();
                }
                else if (role.equals("Sales Driver")){
                    startActivity(new Intent(this, Driver_Transactions.class));
                    finish();
                }else if (role.equals("Warehouse Checker")){
                    startActivity(new Intent(this, Checker_transactions.class));
                    finish();
                }
                break;
            case "Inventory":
                if (role.equals("OIC")){
                    drawer.closeDrawer(Gravity.START);
                }
                else if (role.equals("Sales Driver")){
                    startActivity(new Intent(this, Driver_Inventory.class));
                    finish();
                }
                else if(role.equals("Warehouse Checker")){
                    startActivity(new Intent(this, Checker_Inventory.class));
                    finish();
                }
                else if (role.equals("Partner Portal")){
                    startActivity(new Intent(this, Partner_inventory.class));
                    finish();
                }
                break;
            case "Loading/Unloading":
                if(role.equals("Warehouse Checker")){
                    startActivity(new Intent(this, Load_home.class));
                    finish();
                }
                else if (role.equals("Partner Portal")){
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
        }
    }

    public void all(String x){
        final ArrayList<ListItem> listitem = countBoxes(x,helper.logcount()+"");
        Three_tableAd ad = new Three_tableAd(this, listitem);
        lv.setAdapter(ad);
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

    public void warehousespinner(){
        try {
            final String[] warehouses
                    = rate.getWarehouseName(helper.getBranch(helper.logcount() + ""));
            ArrayAdapter<String> warehouseadapter =
                    new ArrayAdapter<>(getApplicationContext(), R.layout.spinneritem,
                            warehouses);
            warehouse.setAdapter(warehouseadapter);
            warehouseadapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
            warehouse.setPrompt("Select warehouse");
            warehouse.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String wareid = getWarehouseId(warehouse.getSelectedItem().toString());
                    all(wareid);
                    total.setText(Html.fromHtml("<small>Overall total: </small>" +
                            "<b>"+sumAll(wareid,helper.logcount()+"")+" box(s) </b>"));
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    String wareid = getWarehouseId(warehouse.getSelectedItem().toString());
                    all(wareid);
                    total.setText(Html.fromHtml("<small>Overall total: </small>" +
                            "<b>"+sumAll(wareid,helper.logcount()+"")+" box(s) </b>"));
                }
            });
        }catch (Exception e){}
    }

    public void boxclass(){
        try {
            final String[] boar = new String[]{"Accepted Box", "Barcodes"};
            final ArrayAdapter<String> w =
                    new ArrayAdapter<>(getApplicationContext(), R.layout.spinneritem,
                            boar);
            barcx.setAdapter(w);
            w.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
            barcx.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view,
                                           int position, long id) {
                    String x = w.getItem(position);
                    TableAdapter ad;
                    final ArrayList<ListItem> item;
                    switch (x) {
                        case "Accepted Box":
                            tophead.setText("Box type");
                            subtop.setText("Quantity");
                            pr.setText("Price");
                            warehousespinner();
                            break;
                        case "Barcodes":
                            tophead.setText("#");
                            subtop.setText("Box type");
                            pr.setText("Box No.");
                            final ArrayList<ListItem> listitem = getBarcodes("0");
                            Three_tableAd adpt = new Three_tableAd(getApplicationContext(), listitem);
                            lv.setAdapter(adpt);
                            total.setText(Html.fromHtml("<small>Overall total: </small>" +
                                    "<b>" + countBarcodes("0") + " barcode(s) </b>"));
                            break;
                        default:
                            tophead.setText("Box type");
                            subtop.setText("Quantity");
                            pr.setText("Price");
                            warehousespinner();
                            break;
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        }catch (Exception e){}
    }

    public ArrayList<ListItem> getBarcodes(String stats){
        ArrayList<ListItem> results = new ArrayList<>();
        try {
            SQLiteDatabase db = rate.getReadableDatabase();
            String r = "SELECT * FROM "+rate.tbname_barcode_inventory
                    +" WHERE "+rate.barcodeinv_status+" = '"+stats+"'";
            Cursor c = db.rawQuery(r, null);
            if (c.getCount() != 0 ) {
                c.moveToFirst();
                int i = 1;
                while (!c.isAfterLast()) {
                    String id = c.getString(c.getColumnIndex(rate.barcodeinv_id));
                    String subitem = c.getString(c.getColumnIndex(rate.barcodeinv_boxnumber));
                    String topitem = "Open";
//                    String boxids = c.getString(2);
                    ListItem list = new ListItem(id, i+"", topitem, subitem);
                    results.add(list);
                    i++;
                    c.moveToNext();
                }
            }
        }catch (Exception e){}
        return results;
    }

    public int countBarcodes(String stats){
        SQLiteDatabase db = rate.getReadableDatabase();
        String r = "SELECT * FROM "+rate.tbname_barcode_inventory
                +" WHERE "+rate.barcodeinv_status+" = '"+stats+"'";
        Cursor c = db.rawQuery(r, null);

        return c.getCount();
    }

    //get boxes from inventory
    public ArrayList<ListItem> countBoxes(String warehouse, String user){
        int totalq = 0;
        ArrayList<ListItem> results = new ArrayList<>();
        SQLiteDatabase db = gen.getReadableDatabase();
        Cursor c = db.rawQuery(" SELECT * FROM "+gen.tb_acceptance
                +" WHERE "+gen.acc_warehouse_id+" = '"+warehouse+"' AND "
                +gen.acc_createdby+" = '"+user+"' AND "+gen.acc_quantity+" != '0'", null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            String sub = c.getString(c.getColumnIndex(gen.acc_id));
            String co = c.getString(c.getColumnIndex(gen.acc_quantity));
            String topitem = c.getString(c.getColumnIndex(gen.acc_boxtype));
            String bname = "";
            String y = " SELECT "+gen.box_name+" FROM "+gen.tbname_boxes
                    +" WHERE "+gen.box_id+" = '"+topitem+"'";
            Cursor d = db.rawQuery(y, null);
            if (d.moveToNext()){
                bname = d.getString(d.getColumnIndex(gen.box_name));
            }
            double coprice = Double.valueOf(co);
            ListItem list = new ListItem(sub, bname, ""+co, boxPrice(topitem, coprice)+"");
            results.add(list);
            totalp += boxPrice(topitem, coprice);;
            c.moveToNext();
        }
        // Add some more dummy data for testing
        return results;
    }

    //sum all quantity
    public int sumAll(String id, String user){
        int co = 0;
        SQLiteDatabase db = gen.getReadableDatabase();
        String xy = " SELECT SUM("+gen.tb_acceptance+"."+gen.acc_quantity+") FROM "+gen.tb_acceptance
                +" WHERE "+gen.acc_warehouse_id+" = '"+id+"' AND "
                +gen.acc_createdby+" = '"+user+"'";
        Cursor cur = db.rawQuery(xy, null);
        if(cur.moveToFirst())
        {
            co = cur.getInt(0);
        }
        return co;
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(getApplicationContext(), Home.class));
        finish();
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
        SQLiteDatabase db = gen.getReadableDatabase();
        String q = " SELECT * FROM "+gen.tb_acceptance+" WHERE "
                +gen.acc_createdby+" = '"+helper.logcount()+"' AND "+gen.acc_upds+" = '1'";
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
                Log.e("STATUS", String.valueOf(conn.getResponseCode()));
                Log.e("MSG", conn.getResponseMessage());
                if (conn.getResponseMessage().equals("OK")){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.dismiss();
                            final AlertDialog.Builder builder
                                    = new AlertDialog.Builder(Oic_inventory.this);
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
                }else{
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.dismiss();
                            final AlertDialog.Builder builder
                                    = new AlertDialog.Builder(Oic_inventory.this);
                            builder.setTitle("Upload failed")
                                    .setMessage("Data upload has failed, please try again later. thank you.")
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
                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else{
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressBar.dismiss();
                    final AlertDialog.Builder builder = new AlertDialog.Builder(Oic_inventory.this);
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

    public JSONArray getWarehouseInventory(String x) {
        SQLiteDatabase myDataBase = gen.getReadableDatabase();
        String raw = "SELECT "+gen.tb_acceptance+"."+gen.acc_id+", "
                +gen.tb_acceptance+"."+gen.acc_warehouse_id+", "
                +gen.tb_acceptance+"."+gen.acc_name+", "
                +gen.tb_acceptance+"."+gen.acc_boxtype+", "
                +gen.tb_acceptance+"."+gen.acc_quantity+", "
                +gen.tb_acceptance+"."+gen.acc_createddate+", "
                +gen.tb_acceptance+"."+gen.acc_createdby+", "
                +gen.tb_acceptance+"."+gen.acc_status+""
                +" FROM " + gen.tb_acceptance+" WHERE "
                +gen.acc_createdby+" = '"+x+"' AND "+gen.acc_upds+" = '1'";
        Cursor cursor = myDataBase.rawQuery(raw, null);

        JSONArray resultSet = new JSONArray();
        JSONArray boxtypes = new JSONArray();

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            try {
                JSONObject json = new JSONObject();
                String id = cursor.getString(cursor.getColumnIndex(gen.acc_id));
                String warehouse = cursor.getString(cursor.getColumnIndex(gen.acc_warehouse_id));
                String name = cursor.getString(cursor.getColumnIndex(gen.acc_name));
                String by = cursor.getString(cursor.getColumnIndex(gen.acc_createdby));
                String date = cursor.getString(cursor.getColumnIndex(gen.acc_createddate));
                String stat = cursor.getString(cursor.getColumnIndex(gen.acc_status));
                String bty = cursor.getString(cursor.getColumnIndex(gen.acc_boxtype));
                String q = cursor.getString(cursor.getColumnIndex(gen.acc_quantity));
                json.put("id", id);
                json.put("warehouse_id", warehouse);
                json.put("manufacturer_name", name);
                json.put("boxtype_id", bty);
                json.put("quantity", q);
                json.put("createddate", date);
                json.put("createdby", by);
                json.put("status", stat);
                json.put("price", boxPrice(bty, Double.valueOf(q))+"");
                json.put("purchase_order", getEmptyAcceptanceImage(id));
                resultSet.put(json);

                cursor.moveToNext();
            }catch (Exception e){}
        }

        cursor.close();
        //Log.e("result set", resultSet.toString());
        return resultSet;
    }

    public JSONArray getEmptyAcceptanceImage(String trans) {
        SQLiteDatabase myDataBase = rate.getReadableDatabase();
        String raw = " SELECT * FROM " + rate.tbname_generic_imagedb
                +" WHERE "+rate.gen_trans+" = '"+trans+"' AND "+rate.gen_module+" = 'acceptance_empty'";
        Cursor cursor = myDataBase.rawQuery(raw, null);
        JSONArray resultSet = new JSONArray();
        cursor.moveToFirst();
        try {
            while (!cursor.isAfterLast()) {
                JSONObject js = new JSONObject();
                String tr = cursor.getString(cursor.getColumnIndex(rate.gen_trans));
                String module = cursor.getString(cursor.getColumnIndex(rate.gen_module));
                byte[] image = cursor.getBlob(cursor.getColumnIndex(rate.gen_image));
                Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
                byte[] bitmapdata = getBytesFromBitmap(bitmap);

                // get the base 64 string
                String imgString = Base64.encodeToString(bitmapdata, Base64.NO_WRAP);

                js.put("transaction_number", tr);
                js.put("module", module);
                js.put("image", imgString);
                resultSet.put(js);
                cursor.moveToNext();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        cursor.close();
//Log.e("result set", resultSet.toString());
        return resultSet;
    }

    // convert from bitmap to byte array
    public byte[] getBytesFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        return stream.toByteArray();
    }

    public void updateWarehouseInv(String stat){
        SQLiteDatabase db = gen.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(gen.acc_upds, stat);
        db.update(gen.tb_acceptance, cv,
                gen.acc_upds+" = '1'", null);
        Log.e("upload", "uploaded inventory");
        db.close();
    }

    //send sync data
    public void sendPost() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {

                threadwarehouseInventory();

            }
        });

        thread.start();
    }

    public double boxPrice(String boxid, double quant){
        double pr = 0;
        SQLiteDatabase db = gen.getReadableDatabase();
        String x = " SELECT "+gen.box_depositprice+" FROM "+gen.tbname_boxes
                +" WHERE "+gen.box_id+" = '"+boxid+"'";
        Cursor c = db.rawQuery(x, null);
        if (c.moveToNext()){
            double tr = Double.valueOf(c.getString(c.getColumnIndex(gen.box_depositprice)));
            pr = (tr * quant);
        }
        c.close();
        return pr;
    }

}
