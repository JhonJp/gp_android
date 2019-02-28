package com.example.admin.gpxbymodule;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

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
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

public class Home extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    HomeDatabase helper;
    GenDatabase gendata;
    RatesDB ratesDB;
    SQLiteDatabase db;
    Date datetalaga;
    GridView grid;
    LinearLayout screenheight;
    RequestQueue queue;
    String[] web;
    String value;
    ProgressDialog progressBar;
    Toolbar toolbar;
    NavigationView navigationView;
    Runnable r;
    String zeros;
    Thread thr;
    ArrayList<String> toupdid, booknums, incids, loadids, unloadids, distids;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        grid = (GridView)findViewById(R.id.grid);
        helper = new HomeDatabase(getApplicationContext());
        gendata = new GenDatabase(getApplicationContext());
        ratesDB = new RatesDB(getApplicationContext());
        queue = Volley.newRequestQueue(getApplicationContext());

        try {

            toupdid = new ArrayList<>();
            booknums = new ArrayList<>();
            incids = new ArrayList<>();
            loadids = new ArrayList<>();
            unloadids = new ArrayList<>();
            distids = new ArrayList<>();
            if (helper.logcount() != 0) {
                value = helper.getRole(helper.logcount());
            }

            if (value.equals("Administrator")){
                value = "OIC";
            }

            web = helper.getModuleNames(value);

            if (helper.count()) {
                ins();
            }

            final ArrayList<HomeList> listitem = helper.getData(value);

            HomeAdapter myAdapter = new HomeAdapter(Home.this, listitem);

            grid.setAdapter(myAdapter);

            grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String data = listitem.get(position).getSubitem();
                    select(data);
                }
            });

            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.homefab);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (isNetworkAvailable()== true){
                        Toast.makeText(getApplicationContext(),"Connected to the internet.", Toast.LENGTH_LONG).show();
                        sendPost();
                        loadingPost(getWindow().getDecorView().getRootView());
                    }else
                    {
                        Toast.makeText(getApplicationContext(),"Please check internet connection.", Toast.LENGTH_LONG).show();
                    }
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

        //nav header
        setNameMail();
        sidenavMain();
        subMenu();

    }

    public void sidenavMain(){
        ListView lv=(ListView)findViewById(R.id.optionsList);//initialization of listview

        final ArrayList<HomeList> listitem = helper.getData(value);

        NavAdapter ad = new NavAdapter(Home.this, listitem);
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
                        final AlertDialog.Builder builder = new AlertDialog.Builder(Home.this);
                        builder.setMessage("Confirm logout?")
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {

                                        helper.logout();

                                        startActivity(new Intent(Home.this, Login.class));
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
                        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                        drawer.closeDrawer(GravityCompat.START);
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
        try {
            switch (data) {
                case "Acceptance":
                    if (value.equals("OIC")) {
                        startActivity(new Intent(Home.this, Acceptance.class));
                        finish();
                    } else if (value.equals("Warehouse Checker")) {
                        startActivity(new Intent(this, Acceptance.class));
                        finish();
                    } else if (value.equals("Partner Portal")) {
                        startActivity(new Intent(this, Partner_acceptance.class));
                        finish();
                    }
                    break;
                case "Distribution":
                    if (value.equals("Partner Portal")){
                        startActivity(new Intent(this, Partner_distribution.class));
                        finish();
                    }else{
                        startActivity(new Intent(this, Distribution.class));
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
                    bundle.putString("module", null);
                    //Add the bundle to the intent
                    i.putExtras(bundle);
                    startActivity(i);
                    finish();
                    break;
                case "Transactions":
                    if (value.equals("OIC")) {
                        startActivity(new Intent(Home.this, Oic_Transactions.class));
                        finish();
                    } else if (value.equals("Sales Driver")) {
                        startActivity(new Intent(Home.this, Driver_Transactions.class));
                        finish();
                    } else if (value.equals("Warehouse Checker")) {
                        startActivity(new Intent(Home.this, Checker_transactions.class));
                        finish();
                    }
                    break;
                case "Inventory":
                    if (value.equals("OIC")) {
                        startActivity(new Intent(this, Oic_inventory.class));
                        finish();
                    } else if (value.equals("Sales Driver")) {
                        startActivity(new Intent(this, Driver_Inventory.class));
                        finish();
                    } else if (value.equals("Warehouse Checker")) {
                        startActivity(new Intent(this, Checker_Inventory.class));
                        finish();
                    } else if (value.equals("Partner Portal")) {
                        startActivity(new Intent(this, Partner_inventory.class));
                        finish();
                    }
                    break;
                case "Loading/Unloading":
                    if (value.equals("Warehouse Checker")) {
                        startActivity(new Intent(this, Load_home.class));
                        finish();
                    } else if (value.equals("Partner Portal")) {
                        startActivity(new Intent(this, Load_home.class));
                        finish();
                    }
                    break;
                case "Reservation":
                        startActivity(new Intent(Home.this, Reservelist.class));
                        finish();
                    break;
                case "Booking":
                        startActivity(new Intent(Home.this, Bookinglist.class));
                        finish();
                    break;
                case "Direct":
                        startActivity(new Intent(Home.this, Partner_Maindelivery.class));
                        finish();
                    break;
                case "Barcode Releasing":
                    startActivity(new Intent(this, BoxRelease.class));
                    finish();
                    break;
            }
        }catch (Exception e){}
    }

    public void setNameMail(){
        String branchname = null;
        View header = navigationView.getHeaderView(0);
        TextView user = (TextView)header.findViewById(R.id.yourname);
        TextView mail = (TextView)header.findViewById(R.id.yourmail);
        user.setText(helper.getFullname(helper.logcount()+""));
        SQLiteDatabase db = ratesDB.getReadableDatabase();
        Cursor x = db.rawQuery(" SELECT * FROM "+ratesDB.tbname_branch
                +" WHERE "+ratesDB.branch_id+" = '"+helper.getBranch(""+helper.logcount())+"'", null);
        if (x.moveToNext()){
            branchname = x.getString(x.getColumnIndex(ratesDB.branch_name));
        }
        x.close();
        mail.setText(helper.getRole(helper.logcount())+" / "+branchname);

    }

    @Override
    public void onBackPressed() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm exit?");
        builder.setMessage("Please confirm if you want to close the application.");
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        System.exit(0);
                        finish();
                    }
                });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        // Create the AlertDialog object and show it
        builder.create().show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        try {
            int id = item.getItemId();
            if (id == R.id.syncdown){
                network();
            }
            else if (id == R.id.drop_sync) {
                if (isNetworkAvailable()== true){
                    Toast.makeText(getApplicationContext(),"Connected to the internet.", Toast.LENGTH_LONG).show();
                    sendPost();
                    loadingPost(getWindow().getDecorView().getRootView());
                }else
                {
                    Toast.makeText(getApplicationContext(),"Please check internet connection.", Toast.LENGTH_LONG).show();
                }
            } else if (id == R.id.homelogout) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Confirm logout?");
                builder.setMessage("You are about to exit the application.");
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                helper.logout();
                                startActivity(new Intent(Home.this, Login.class));
                                finish();
                            }
                        });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });
                // Create the AlertDialog object and show it
                builder.create().show();
            }
        }catch (Exception e){}
        return super.onOptionsItemSelected(item);
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

    // inserting default modules
    public void ins(){
        try {
            Resources res = getResources();

            //drawable officer in charge user
            Drawable officer_acceptance = res.getDrawable(R.drawable.acceptance);
            Drawable officer_distribution = res.getDrawable(R.drawable.distribution);
            Drawable officer_incident = res.getDrawable(R.drawable.accident);
            Drawable officer_inventory = res.getDrawable(R.drawable.inventory);
            Drawable officer_remittance = res.getDrawable(R.drawable.remittance);
            Drawable officer_transactions = res.getDrawable(R.drawable.transactions);

            Bitmap off_acc = ((BitmapDrawable) officer_acceptance).getBitmap();
            Bitmap off_dist = ((BitmapDrawable) officer_distribution).getBitmap();
            Bitmap off_inc = ((BitmapDrawable) officer_incident).getBitmap();
            Bitmap off_inv = ((BitmapDrawable) officer_inventory).getBitmap();
            Bitmap off_rem = ((BitmapDrawable) officer_remittance).getBitmap();
            Bitmap off_tra = ((BitmapDrawable) officer_transactions).getBitmap();

            ByteArrayOutputStream off_byte_acc = new ByteArrayOutputStream();
            ByteArrayOutputStream off_byte_dist = new ByteArrayOutputStream();
            ByteArrayOutputStream off_byte_inc = new ByteArrayOutputStream();
            ByteArrayOutputStream off_byte_inv = new ByteArrayOutputStream();
            ByteArrayOutputStream off_byte_rem = new ByteArrayOutputStream();
            ByteArrayOutputStream off_byte_tra = new ByteArrayOutputStream();

            off_acc.compress(Bitmap.CompressFormat.PNG, 100, off_byte_acc);
            off_dist.compress(Bitmap.CompressFormat.PNG, 100, off_byte_dist);
            off_inc.compress(Bitmap.CompressFormat.PNG, 100, off_byte_inc);
            off_inv.compress(Bitmap.CompressFormat.PNG, 100, off_byte_inv);
            off_rem.compress(Bitmap.CompressFormat.PNG, 100, off_byte_rem);
            off_tra.compress(Bitmap.CompressFormat.PNG, 100, off_byte_tra);

            byte[] off_final_acc = off_byte_acc.toByteArray();
            byte[] off_final_dist = off_byte_dist.toByteArray();
            byte[] off_final_inc = off_byte_inc.toByteArray();
            byte[] off_final_inv = off_byte_inv.toByteArray();
            byte[] off_final_rem = off_byte_rem.toByteArray();
            byte[] off_final_tra = off_byte_tra.toByteArray();

            String officer = "OIC";

            helper.addModule("Acceptance", off_final_acc, officer);
            helper.addModule("Distribution", off_final_dist, officer);
            helper.addModule("Barcode Releasing", off_final_tra, officer);
            helper.addModule("Inventory", off_final_inv, officer);
            helper.addModule("Remittance", off_final_rem, officer);
            helper.addModule("Incident Report", off_final_inc, officer);

            //drawable sales driver user
            Drawable driver_reservation = res.getDrawable(R.drawable.reservation);
            Drawable driver_booking = res.getDrawable(R.drawable.booking);
            Drawable driver_remittance = res.getDrawable(R.drawable.remittance);
            Drawable driver_inventory = res.getDrawable(R.drawable.inventory);
            Drawable driver_incident = res.getDrawable(R.drawable.accident);
            Drawable driver_transactions = res.getDrawable(R.drawable.transactions);

            Bitmap driver_res = ((BitmapDrawable) driver_reservation).getBitmap();
            Bitmap driver_book = ((BitmapDrawable) driver_booking).getBitmap();
            Bitmap driver_rem = ((BitmapDrawable) driver_remittance).getBitmap();
            Bitmap driver_inv = ((BitmapDrawable) driver_inventory).getBitmap();
            Bitmap driver_inc = ((BitmapDrawable) driver_incident).getBitmap();
            Bitmap driver_tra = ((BitmapDrawable) driver_transactions).getBitmap();

            ByteArrayOutputStream driver_byte_res = new ByteArrayOutputStream();
            ByteArrayOutputStream driver_byte_book = new ByteArrayOutputStream();
            ByteArrayOutputStream driver_byte_rem = new ByteArrayOutputStream();
            ByteArrayOutputStream driver_byte_inv = new ByteArrayOutputStream();
            ByteArrayOutputStream driver_byte_inc = new ByteArrayOutputStream();
            ByteArrayOutputStream driver_byte_tra = new ByteArrayOutputStream();

            driver_res.compress(Bitmap.CompressFormat.PNG, 100, driver_byte_res);
            driver_book.compress(Bitmap.CompressFormat.PNG, 100, driver_byte_book);
            driver_rem.compress(Bitmap.CompressFormat.PNG, 100, driver_byte_rem);
            driver_inv.compress(Bitmap.CompressFormat.PNG, 100, driver_byte_inv);
            driver_inc.compress(Bitmap.CompressFormat.PNG, 100, driver_byte_inc);
            driver_tra.compress(Bitmap.CompressFormat.PNG, 100, driver_byte_tra);

            byte[] drive_final_res = driver_byte_res.toByteArray();
            byte[] drive_final_book = driver_byte_book.toByteArray();
            byte[] drive_final_rem = driver_byte_rem.toByteArray();
            byte[] drive_final_inv = driver_byte_inv.toByteArray();
            byte[] drive_final_inc = driver_byte_inc.toByteArray();
            byte[] drive_final_tra = driver_byte_tra.toByteArray();


            String driver = "Sales Driver";

            helper.addModule("Reservation", drive_final_res, driver);
            helper.addModule("Remittance", drive_final_rem, driver);
            helper.addModule("Booking", drive_final_book, driver);
            helper.addModule("Inventory", drive_final_inv, driver);
            helper.addModule("Incident Report", drive_final_inc, driver);
            helper.addModule("Transactions", drive_final_tra, driver);

            //drawable warehouse checker user
            Drawable checker_acceptance = res.getDrawable(R.drawable.acceptedbox);
            Drawable checker_inventory = res.getDrawable(R.drawable.inventory);
            Drawable checker_unloading = res.getDrawable(R.drawable.unload);
            Drawable checker_remittance = res.getDrawable(R.drawable.remittance);
            Drawable checker_incident = res.getDrawable(R.drawable.accident);
            Drawable checker_transactions = res.getDrawable(R.drawable.transactions);

            Bitmap check_acc = ((BitmapDrawable) checker_acceptance).getBitmap();
            Bitmap check_inv = ((BitmapDrawable) checker_inventory).getBitmap();
            Bitmap check_unl = ((BitmapDrawable) checker_unloading).getBitmap();
            Bitmap check_rem = ((BitmapDrawable) checker_remittance).getBitmap();
            Bitmap check_inc = ((BitmapDrawable) checker_incident).getBitmap();
            Bitmap check_trans = ((BitmapDrawable) checker_transactions).getBitmap();

            ByteArrayOutputStream check_byte_acc = new ByteArrayOutputStream();
            ByteArrayOutputStream check_byte_inv = new ByteArrayOutputStream();
            ByteArrayOutputStream check_byte_unl = new ByteArrayOutputStream();
            ByteArrayOutputStream check_byte_rem = new ByteArrayOutputStream();
            ByteArrayOutputStream check_byte_inc = new ByteArrayOutputStream();
            ByteArrayOutputStream check_byte_trans = new ByteArrayOutputStream();

            check_acc.compress(Bitmap.CompressFormat.PNG, 100, check_byte_acc);
            check_inv.compress(Bitmap.CompressFormat.PNG, 100, check_byte_inv);
            check_unl.compress(Bitmap.CompressFormat.PNG, 100, check_byte_unl);
            check_rem.compress(Bitmap.CompressFormat.PNG, 100, check_byte_rem);
            check_inc.compress(Bitmap.CompressFormat.PNG, 100, check_byte_inc);
            check_trans.compress(Bitmap.CompressFormat.PNG, 100, check_byte_trans);

            byte[] check_final_acc = check_byte_acc.toByteArray();
            byte[] check_final_inv = check_byte_inv.toByteArray();
            byte[] check_final_loa = check_byte_trans.toByteArray();
            byte[] check_final_unl = check_byte_unl.toByteArray();
            byte[] check_final_inc = check_byte_inc.toByteArray();

            String checker = "Warehouse Checker";

            helper.addModule("Acceptance", check_final_acc, checker);
            helper.addModule("Loading/Unloading", check_final_unl, checker);
            helper.addModule("Distribution", off_final_dist, checker);
            helper.addModule("Barcode Releasing", check_final_loa, checker);
            helper.addModule("Inventory", check_final_inv, checker);
            helper.addModule("Incident Report", check_final_inc, checker);

            //drawable partner portal user
            Drawable partner_load = res.getDrawable(R.drawable.unload);
            Drawable partner_acc = res.getDrawable(R.drawable.acceptedbox);
            Drawable partner_deliver = res.getDrawable(R.drawable.delivery);
            Drawable partner_invent = res.getDrawable(R.drawable.inventory);
            Drawable partner_trans = res.getDrawable(R.drawable.transactions);
            Drawable partner_accident = res.getDrawable(R.drawable.accident);

            Bitmap part_load = ((BitmapDrawable) partner_load).getBitmap();
            Bitmap part_acc = ((BitmapDrawable) partner_acc).getBitmap();
            Bitmap part_deliver = ((BitmapDrawable) partner_deliver).getBitmap();
            Bitmap part_inventory = ((BitmapDrawable) partner_invent).getBitmap();
            Bitmap part_transaction = ((BitmapDrawable) partner_trans).getBitmap();
            Bitmap part_accinc = ((BitmapDrawable) partner_accident).getBitmap();

            ByteArrayOutputStream part_byte_load = new ByteArrayOutputStream();
            ByteArrayOutputStream part_byte_acc = new ByteArrayOutputStream();
            ByteArrayOutputStream part_byte_del = new ByteArrayOutputStream();
            ByteArrayOutputStream part_byte_inv = new ByteArrayOutputStream();
            ByteArrayOutputStream part_byte_trans = new ByteArrayOutputStream();
            ByteArrayOutputStream part_byte_accinc = new ByteArrayOutputStream();

            part_load.compress(Bitmap.CompressFormat.PNG, 100, part_byte_load);
            part_acc.compress(Bitmap.CompressFormat.PNG, 100, part_byte_acc);
            part_deliver.compress(Bitmap.CompressFormat.PNG, 100, part_byte_del);
            part_inventory.compress(Bitmap.CompressFormat.PNG, 100, part_byte_inv);
            part_transaction.compress(Bitmap.CompressFormat.PNG, 100, part_byte_trans);
            part_accinc.compress(Bitmap.CompressFormat.PNG, 100, part_byte_accinc);

            byte[] part_final_load = part_byte_load.toByteArray();
            byte[] part_final_acc = part_byte_acc.toByteArray();
            byte[] part_final_del = part_byte_del.toByteArray();
            byte[] part_final_inv = part_byte_inv.toByteArray();
            byte[] part_final_trans = part_byte_trans.toByteArray();
            byte[] part_final_accinc = part_byte_accinc.toByteArray();

            String partner = "Partner Portal";

            helper.addModule("Loading/Unloading", part_final_load, partner);
            helper.addModule("Acceptance", part_final_acc, partner);
            helper.addModule("Direct", part_final_del, partner);
            helper.addModule("Distribution", off_final_dist, partner);
            helper.addModule("Inventory", part_final_inv, partner);
            helper.addModule("Incident Report", part_final_trans, partner);

            Drawable info = res.getDrawable(R.drawable.information);
            Bitmap infor = ((BitmapDrawable) info).getBitmap();
            ByteArrayOutputStream g = new ByteArrayOutputStream();
            infor.compress(Bitmap.CompressFormat.PNG, 100, g);
            byte[] final_info = g.toByteArray();

            Drawable home = res.getDrawable(R.drawable.home);
            Bitmap bithome = ((BitmapDrawable) home).getBitmap();
            ByteArrayOutputStream streamhome = new ByteArrayOutputStream();
            bithome.compress(Bitmap.CompressFormat.PNG, 100, streamhome);
            byte[] final_home = streamhome.toByteArray();

            helper.addSubMenu(final_home, "Home");
            helper.addSubMenu(final_info, "Add Customer");
            helper.addSubMenu(final_info, "Log Out");

        }catch (Exception e){}

    }

    //send sync data
    public void sendPost() {
        thr = new Thread(new Runnable() {
            @Override
            public void run() {
                if (value.equals("Sales Driver")) {
                    // start pass data for reservations
                    threadReservations();

                    // start pass data for Bookings
                    threadBooking();
                }

                // start pass data for New Customers
                threadCustomers();

                if (value.equals("Warehouse Checker") || value.equals("OIC")) {
                    // start pass data for Distributions
                    threadDistribution();

                    // start pass data for Warehouse Inventory
                    threadwarehouseInventory();

                    //start warehouse acceptance data
                    getAllAcceptedBoxes();

                }

                // start pass data for Incidents
                threadIncident();

                // start pass data for Loadings
                threadLoading();

                if (value.equals("Partner Portal")) {
                    // start pass data for Unloading
                    threadUnloading();
                }

                // start pass data for Partner Distribution
                threadDistributionPartner();

            }
        });

        thr.start();
    }

    //get data
    public void getPost(){
        try {
            thr = new Thread(new Runnable() {
                @Override
                public void run() {

                    //get box contents
                    getBoxContents();

                    //get expense types
                    getExpenseTypes();

                    if (!(value.equals("Sales Driver") || value.equals("Partner Portal"))) {
                        //get barcode generated by user
                        getBarCodeInventory();
                    }
                    if (!(value.equals("Partner Portal"))) {
                        //get nsb rates for calculation
                        getNSBrates();
                    }

                    //get barcode driver
                    getBarCodeDriverInventory();

                    //get boxrates
                    getBoxRates();

                    //thread destsource
                    getsource("source");

                    getsource("destination");

                    //START THREAD FOR CUSTOMERS
                    if (value.equals("Partner Portal")) {
                        getAllCustomers();
                    } else if (value.equals("Partner Driver")) {
                        getAllCustomers();
                    } else {
                        getCustomers();
                    }
                    //get boxtypes dta
                    getBoxtypesData();

                    //get employee records
                    getEmployees();

                    //START DISTRIBUTION BRANCH
                    getBranchDistribution();

                    //get pending reservations
                    if (value.equals("Sales Driver")) {
                        getReservations();
                    }

                    //get warehouse acceptance FILLED BOX
                    getWarehouseAcceptance();

                    if (value.equals("Sales Driver")) {
                        //get boxes distributed to driver
                        getBoxesFromDistribution(helper.getFullname(helper.logcount() + ""));
                    }

                    if (value.equals("Warehouse Checker")) {
                        //get boxes distributed to branch
                        getBoxesFromDistributionChecker(readBranch());
                    }

                    //distribution to sales driver
                    getSalesDriverDistribution();

                    //get barcode distribution to driver
                    getBarcodesDistributiontoDriver();

                    if (!(value.equals("Partner Portal"))) {
                        //get warehouse names
                        getWarehouse();
                    }

                    // get booking transactions
                    getBookings();

                    //get loaded boxes
                    getLoadItems();

                    //get unloaded items or boxes
                    getUnLoadsItem();

                    //distribution to PARTNER HUB
                    getHubDistributions();

                    //get Undelivered boxes
                    getUndeliveredBoxes();

                    //distribution to AREA
                    getAreaDistributions();

                    //distribution direct
                    getDirectDistribution();


                }
            });
            thr.start();
        }catch(Exception e){
            progressBar.dismiss();
        }
    }

    //get customers from api per branch
    public void getCustomers(){
        try {
            String link = helper.getUrl();
            String customers = null;
            String urlget = "http://"+link+"/api/customer/getbybranch.php?branchid="+helper.getBranch(""+helper.logcount());
            URL url = new URL(urlget);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            // read the response
            InputStream in = new BufferedInputStream(conn.getInputStream());
            customers = convertStreamToString(in);

            if (customers != null) {

                Log.e("Customers", "Customers: " + customers);

                JSONArray jsonArray = new JSONArray(customers);

                for(int i=0; i<jsonArray.length(); i++){

                    JSONObject json_data = jsonArray.getJSONObject(i);

                    String account_no = json_data.getString("account_no");
                    String firstname = json_data.getString("firstname");
                    String middlename = json_data.getString("middlename");
                    String lastname = json_data.getString("lastname");
                    String mobile = json_data.getString("mobile");
                    String secmob = json_data.getString("secondary_number");
                    String thrmob = json_data.getString("another_number");
                    String phone = json_data.getString("phone");
                    String email = json_data.getString("email");
                    String gender = json_data.getString("gender");
                    String birthdate = json_data.getString("birthdate");

                    //address
                    String prov = json_data.getString("province");
                    String openfield = json_data.getString("house_number_street");
                    String barangay = json_data.getString("barangay");
                    String city = json_data.getString("city");
                    String postal = json_data.getString("postal_code");


                    String type = json_data.getString("type");
                    String createdby = json_data.getString("createdby");
                    String recordstatus = json_data.getString("recordstatus");
                    String sender_account_no = json_data.getString("sender_account_no");
                    String name = firstname + " "+ lastname;

                    if (sender_account_no.toLowerCase().equals("null")){
                        sender_account_no = "";
                    }
                    gendata.addCustomer( account_no, sender_account_no, firstname, middlename, lastname, mobile,
                            secmob, thrmob, phone, email, gender, birthdate, prov, city,postal,
                            barangay, openfield, type, createdby, recordstatus, name, "2");

                }

            } else {
                Log.e("Error", "Couldn't get data from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String r = "Couldn't get data from server, trying again....";
                        customToast(r);
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //get box rates
    public void getBoxRates(){
        try {
            String resp = null;
            String link = helper.getUrl();
            String urlget = "http://" + link + "/api/boxrate/get.php";
            URL url = new URL(urlget);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            // read the response
            InputStream in = new BufferedInputStream(conn.getInputStream());
            resp = convertStreamToString(in);

            if (resp != null) {
                Log.e("Rates", "Rates: " + resp);
                JSONArray jsonArray = new JSONArray(resp);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject json_data = jsonArray.getJSONObject(i);
                    String btypeid = json_data.getString("boxtype_id");
                    String cbm = json_data.getString("cbm");
                    String source = json_data.getString("source_id");
                    String dest = json_data.getString("destination_id");
                    String cur = json_data.getString("currency_id");
                    String am = json_data.getString("amount");
                    String rec = json_data.getString("recordstatus");

                    ratesDB.addNewRates(btypeid, cbm, source, dest, cur, am, rec);

                }

            } else {
                Log.e("Error", "Couldn't get data from server.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //get boxtypes data
    public void getBoxtypesData(){
        try {
            String resp = null;
            String link = helper.getUrl();
            String urlget = "http://" + link + "/api/boxtype/get.php";
            URL url = new URL(urlget);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            // read the response
            InputStream in = new BufferedInputStream(conn.getInputStream());
            resp = convertStreamToString(in);

            if (resp != null) {

                Log.e("Boxes", "" + resp);

                JSONArray jsonArray = new JSONArray(resp);

                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject json_data = jsonArray.getJSONObject(i);

                    String id = json_data.getString("id");
                    String boxname = json_data.getString("name");
                    String depoprice = json_data.getString("depositprice");
                    String l = json_data.getString("size_length");
                    String w = json_data.getString("size_width");
                    String h = json_data.getString("size_height");
                    String nsb = json_data.getString("nsb");
                    String desc = json_data.getString("description");
                    String createdby = json_data.getString("createdby");
                    String recordstatus = json_data.getString("recordstatus");

                    SQLiteDatabase dbcheck = gendata.getReadableDatabase();
                    Cursor c = dbcheck.rawQuery("SELECT * FROM " + gendata.tbname_boxes
                            + " WHERE " + gendata.box_id + " = '" + id + "'", null);
                    if (c.getCount() != 0) {
                        updateBoxType(id + "", boxname.toUpperCase(), depoprice, l,
                                w, h, nsb, desc, createdby, recordstatus);
                        updateBoxTypeInRatesTable(id + "", boxname.toUpperCase(), depoprice, l,
                                w, h, nsb, desc, createdby, recordstatus);
                    } else {
                        gendata.addBoxes(id + "", boxname.toUpperCase(), depoprice, l,
                                w, h, nsb, desc, createdby, recordstatus);
                        ratesDB.addBoxes(id, boxname.toUpperCase(), depoprice, l, w, h, nsb,
                                desc, createdby, recordstatus);
                        Log.e("boxid", id + "");
                    }
                    c.close();
                    dbcheck.close();
                }

            } else {
                Log.e("Error", "Couldn't get data from server.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // get employee
    public void getEmployees(){
        try {
            String resp = null;
            String link = helper.getUrl();
            String urlget = "http://" + link + "/api/employee/get.php";
            URL url = new URL(urlget);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            // read the response
            InputStream in = new BufferedInputStream(conn.getInputStream());
            resp = convertStreamToString(in);

            if (resp != null) {

                Log.e("employee", " : " + resp);

                JSONArray jsonArray = new JSONArray(resp);

                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject json_data = jsonArray.getJSONObject(i);

                    String id = json_data.getString("id");
                    String fname = json_data.getString("firstname");
                    String mid = json_data.getString("middlename");
                    String last = json_data.getString("lastname");
                    String mail = json_data.getString("email");
                    String mob = json_data.getString("mobile");
                    String ph = json_data.getString("phone");
                    String gend = json_data.getString("gender");
                    String bday = json_data.getString("birthdate");
                    String post = json_data.getString("role");
                    String hnum = json_data.getString("house_number_street");
                    String brgy = json_data.getString("barangay");
                    String ct = json_data.getString("city");
                    String branch = json_data.getString("branch");

                    gendata.addEmployee(id, fname, mid, last, mail,
                            mob, ph, gend, bday, post, hnum, brgy, ct, branch);

                    ratesDB.addEmployee(id, fname, mid, last, mail,
                            mob, ph, gend, bday, post, hnum, brgy, ct, branch);
                }

            } else {
                Log.e("Error", "Couldn't get data from server.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //get customers general
    public void getAllCustomers(){
        try {
            String link = helper.getUrl();
            String customers = null;
            String urlget = "http://"+link+"/api/customer/get.php";
            URL url = new URL(urlget);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            // read the response
            InputStream in = new BufferedInputStream(conn.getInputStream());
            customers = convertStreamToString(in);

            if (customers != null) {

                Log.e("Customers", "Customers: " + customers);

                JSONArray jsonArray = new JSONArray(customers);

                for(int i=0; i<jsonArray.length(); i++){

                    JSONObject json_data = jsonArray.getJSONObject(i);

                    String account_no = json_data.getString("account_no");
                    String firstname = json_data.getString("firstname");
                    String middlename = json_data.getString("middlename");
                    String lastname = json_data.getString("lastname");
                    String mobile = json_data.getString("mobile");
                    String secmob = json_data.getString("secondary_number");
                    String thrmob = json_data.getString("another_number");
                    String phone = json_data.getString("phone");
                    String email = json_data.getString("email");
                    String gender = json_data.getString("gender");
                    String birthdate = json_data.getString("birthdate");

                    //address
                    String prov = json_data.getString("province");
                    String openfield = json_data.getString("house_number_street");
                    String barangay = json_data.getString("barangay");
                    String city = json_data.getString("city");
                    String postal = json_data.getString("postal_code");


                    String type = json_data.getString("type");
                    String createdby = json_data.getString("createdby");
                    String recordstatus = json_data.getString("recordstatus");
                    String sender_account_no = json_data.getString("sender_account_no");
                    String name = firstname + " "+ lastname;
                    if (sender_account_no.toLowerCase().equals("null")){
                        sender_account_no = "";
                    }
                    gendata.addCustomer( account_no, sender_account_no, firstname, middlename, lastname, mobile,
                            secmob, thrmob, phone, email, gender, birthdate, prov, city,postal,
                            barangay, openfield, type, createdby, recordstatus, name, "2");

                }

            } else {
                Log.e("Error", "Couldn't get data from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String r = "Couldn't get data from server, trying again....";
                        customToast(r);
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getAllAcceptedBoxes(){
        db = gendata.getReadableDatabase();
        String query = " SELECT * FROM "+gendata.tbname_check_acceptance
                +" WHERE "+gendata.accept_uploadstat+" = '1' AND "
                +gendata.accept_createdby+" = '"+helper.logcount()+"'";
        Cursor cx = db.rawQuery(query, null);
        if (cx.getCount() != 0) {
            //THREAD FOR incident API
            try {
                String link = helper.getUrl();
                URL url = new URL("http://" + link + "/api/warehouseacceptance/save.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);
                conn.setDoInput(true);

                JSONObject jsonParam = new JSONObject();
                jsonParam.accumulate("data", getAcceptanceList());

                Log.e("JSON", jsonParam.toString());
                DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                os.writeBytes(jsonParam.toString());
                os.flush();
                os.close();
                Log.i("STATUS", String.valueOf(conn.getResponseCode()));
                Log.i("MSG", conn.getResponseMessage());

                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else{
            Log.e("no_data", "NO data acceptance");
        }
        //END THREAD incident API
    }

    public JSONArray getAcceptanceList() {
        SQLiteDatabase myDataBase = gendata.getReadableDatabase();
        String raw = "SELECT * FROM " + gendata.tbname_check_acceptance
                +" WHERE "+gendata.accept_uploadstat+" = '1' AND "
                +gendata.accept_createdby+" = '"+helper.logcount()+"'";
        Cursor c = myDataBase.rawQuery(raw, null);
        JSONArray resultSet = new JSONArray();
        c.moveToFirst();
        try {
            while (!c.isAfterLast()) {
                JSONObject js = new JSONObject();
                String id = c.getString(c.getColumnIndex(gendata.accept_id));
                String trans = c.getString(c.getColumnIndex(gendata.accept_transactionid));
                String driver = c.getString(c.getColumnIndex(gendata.accept_drivername));
                String wareh = c.getString(c.getColumnIndex(gendata.accept_warehouseid));
                String tru = c.getString(c.getColumnIndex(gendata.accept_container));
                String date = c.getString(c.getColumnIndex(gendata.accept_date));
                String by = c.getString(c.getColumnIndex(gendata.accept_createdby));

                js.put("id", id);
                js.put("transaction_no", trans);
                js.put("salesdriver_id", driver);
                js.put("warehouse_id", wareh);
                js.put("truck_no", tru);
                js.put("createddate", date);
                js.put("createdby", by);
                js.put("acceptance_box", getAcceptedBox(trans));
                resultSet.put(js);
                c.moveToNext();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        c.close();
        //Log.e("result set", resultSet.toString());
        return resultSet;
    }

    public JSONArray getAcceptedBox(String tr) {
        SQLiteDatabase myDataBase = gendata.getReadableDatabase();
        String raw = " SELECT * FROM " + gendata.tbname_accept_boxes + " WHERE "
                + gendata.acc_box_transactionno + " = '" + tr + "'";
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
        return resultSet;
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

    //connecting to internet
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void network(){
        if (isNetworkAvailable()== true){
            Toast.makeText(getApplicationContext(),"Connected to the internet.", Toast.LENGTH_LONG).show();
            getPost();
            loadingGet(getWindow().getDecorView().getRootView());
        }else
        {
            Toast.makeText(getApplicationContext(),"Please check internet connection.", Toast.LENGTH_LONG).show();
        }
    }

    public void loadingGet(final View v){
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

    public String getAccntNumber(String name){
        String accnt = null;
        SQLiteDatabase db = gendata.getReadableDatabase();
        Cursor c = db.rawQuery(" SELECT * FROM "+gendata.tbname_customers+
                " WHERE "+gendata.cust_fullname+" = '"+name+"'", null);
        c.moveToFirst();
        while (!c.isAfterLast()){
            accnt = c.getString(c.getColumnIndex(gendata.cust_accountnumber));

            c.moveToNext();
        }
        return accnt;
    }

    public void threadDistribution(){
        //THREAD FOR distribution API
        try {
            String q = " SELECT * FROM " + gendata.tbname_tempDist
                    + " WHERE " + gendata.temp_createby + " = '" + helper.logcount() + "' AND "
                    + gendata.temp_uploadstat + " = '1' AND "+gendata.temp_acceptstat+" = '1'";
            Cursor x = db.rawQuery(q, null);
            if (x.getCount() != 0) {
                String link = helper.getUrl();
                URL url = new URL("http://" + link + "/api/distribution/save.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);
                conn.setDoInput(true);

                JSONObject jsonParam = new JSONObject();
                db = gendata.getReadableDatabase();

                JSONArray finalarray = new JSONArray();
                JSONArray reserve = null, img = null;


                String query = " SELECT * FROM " + gendata.tbname_tempDist
                        + " WHERE " + gendata.temp_createby + " = '" + helper.logcount() + "' AND "
                        + gendata.temp_uploadstat + " = '1' AND "+gendata.temp_acceptstat+" = '1'";
                Cursor c = db.rawQuery(query, null);
                String trans = null;
                c.moveToFirst();
                while (!c.isAfterLast()) {
                    JSONObject json = new JSONObject();
                    trans = c.getString(c.getColumnIndex(gendata.temp_transactionnumber));
                    String type = c.getString(c.getColumnIndex(gendata.temp_type));
                    String typename = c.getString(c.getColumnIndex(gendata.temp_typename));
                    String truck = c.getString(c.getColumnIndex(gendata.temp_trucknum));
                    String remarks = c.getString(c.getColumnIndex(gendata.temp_remarks));
                    String d = c.getString(c.getColumnIndex(gendata.temp_createdate));
                    String by = c.getString(c.getColumnIndex(gendata.temp_createby));
                    String accstat = c.getString(c.getColumnIndex(gendata.temp_acceptstat));
                    json.put("id", trans);
                    json.put("mode_of_shipment", "");
                    json.put("type", type);
                    json.put("name", typename);
                    json.put("driver_name", "");
                    json.put("truck_no", truck);
                    json.put("eta", "");
                    json.put("etd", "");
                    json.put("remarks", remarks);
                    json.put("created_date", d);
                    json.put("created_by", by);
                    json.put("acceptance_status", accstat);
                    reserve = gendata.getDistributionsBox(trans);
                    img = getDistributionImage(trans);
                    json.put("distribution_box", reserve);
                    json.put("distribution_image", img);
                    finalarray.put(json);
                    c.moveToNext();
                }
                c.close();
                jsonParam.accumulate("data", finalarray);

                Log.e("JSONdist", jsonParam.toString());
                DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                //os.writeBytes(URLEncoder.encode(jsonParam.toString(), "UTF-8"));
                os.writeBytes(jsonParam.toString());
                os.flush();
                os.close();

                Log.e("STATUS", String.valueOf(conn.getResponseCode()));
                Log.e("MSG", conn.getResponseMessage());

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //END THREAD DISTRIBUTION API
    }

    public void threadDistributionPartner(){
        try {
            db = ratesDB.getReadableDatabase();
            String q = " SELECT * FROM " + ratesDB.tbname_part_distribution
                    +" WHERE "+ratesDB.partdist_uploadstat +" = '1' AND "+ratesDB.partdist_acceptstat+" = '1'";
            Cursor x = db.rawQuery(q, null);
            if (x.getCount() != 0) {
                String link = helper.getUrl();
                URL url = new URL("http://" + link + "/api/distribution/save.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);
                conn.setDoInput(true);

                JSONObject jsonParam = new JSONObject();
                db = ratesDB.getReadableDatabase();

                JSONArray finalarray = new JSONArray();
                JSONArray reserve = null, img = null;
                String trans = null;
                db = ratesDB.getReadableDatabase();
                String query = " SELECT * FROM " + ratesDB.tbname_part_distribution
                        +" WHERE "+ratesDB.partdist_uploadstat +" = '1' AND "+ratesDB.partdist_acceptstat+" = '1'";
                Cursor c = db.rawQuery(query, null);
                c.moveToFirst();
                while (!c.isAfterLast()) {
                    JSONObject json = new JSONObject();
                    trans = c.getString(c.getColumnIndex(ratesDB.partdist_transactionnumber));
                    String mode = c.getString(c.getColumnIndex(ratesDB.partdist_mode));
                    String type = c.getString(c.getColumnIndex(ratesDB.partdist_type));
                    String typename = c.getString(c.getColumnIndex(ratesDB.partdist_typename));
                    String eta = c.getString(c.getColumnIndex(ratesDB.partdist_eta));
                    String etd = c.getString(c.getColumnIndex(ratesDB.partdist_etd));
                    String driver = c.getString(c.getColumnIndex(ratesDB.partdist_drivername));
                    String truck = c.getString(c.getColumnIndex(ratesDB.partdist_trucknum));
                    String remarks = c.getString(c.getColumnIndex(ratesDB.partdist_remarks));
                    String d = c.getString(c.getColumnIndex(ratesDB.partdist_createdate));
                    String by = c.getString(c.getColumnIndex(ratesDB.partdist_createby));
                    String accstat = c.getString(c.getColumnIndex(ratesDB.partdist_acceptstat));
                    json.put("id", trans);
                    json.put("mode_of_shipment", mode);
                    json.put("type", type);
                    json.put("name", typename);
                    json.put("driver_name", driver);
                    json.put("truck_no", truck);
                    json.put("eta", eta);
                    json.put("etd", etd);
                    json.put("remarks", remarks);
                    json.put("created_date", d);
                    json.put("created_by", by);
                    json.put("acceptance_status", accstat);
                    reserve = ratesDB.getDistributionsBox(trans);
                    img = getDistributionpartnerImage(trans);
                    json.put("distribution_box", reserve);
                    json.put("distribution_image", img);
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

                if (!conn.getResponseMessage().equals("OK")) {
                    conn.disconnect();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.dismiss();
                            final AlertDialog.Builder builder
                                    = new AlertDialog.Builder(Home.this);
                            builder.setTitle("Upload failed")
                                    .setMessage("Data sync has failed, please try again later. thank you.")
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
                            final AlertDialog.Builder builder =
                                    new AlertDialog.Builder(Home.this);
                            builder.setTitle("Information confirmation")
                                    .setMessage("Data upload has been successful, thank you.")
                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            allUpdates();
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
            }else{
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.dismiss();
                        final AlertDialog.Builder builder
                                = new AlertDialog.Builder(Home.this);
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        //END THREAD DISTRIBUTION API
    }

    public void updateDistpart(String stat){
        SQLiteDatabase db = ratesDB.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(ratesDB.partdist_uploadstat, stat);
        db.update(ratesDB.tbname_part_distribution, cv,
                ratesDB.partdist_uploadstat+" = '1'", null);
        Log.e("upload", "uploaded partdist");
        db.close();
    }

    public JSONArray getDistributionpartnerImage(String id) {
        SQLiteDatabase myDataBase = ratesDB.getReadableDatabase();
        String raw = "SELECT * FROM " + ratesDB.tbname_dstimages
                + " WHERE "+ratesDB.distimage_trans+" = '"+id+"'";
        Cursor c = myDataBase.rawQuery(raw, null);
        JSONArray resultSet = new JSONArray();
        c.moveToFirst();
        try {
            while (!c.isAfterLast()) {
                JSONObject js = new JSONObject();
                String ids = c.getString(c.getColumnIndex(ratesDB.distimage_id));
                String tr = c.getString(c.getColumnIndex(ratesDB.distimage_trans));
                byte[] im = c.getBlob(c.getColumnIndex(ratesDB.distimage_image));
                Bitmap bitmap = BitmapFactory.decodeByteArray(im, 0, im.length);
                byte[] bitmapdata = getBytesFromBitmap(bitmap);

                // get the base 64 string
                String imgString = Base64.encodeToString(bitmapdata, Base64.NO_WRAP);

                js.put("id", ids);
                js.put("distribution_id", tr);
                js.put("image", imgString);

                resultSet.put(js);
                c.moveToNext();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        c.close();
        //Log.e("result set", resultSet.toString());
        return resultSet;
    }

    public JSONArray getDistributionImage(String id) {
        SQLiteDatabase myDataBase = ratesDB.getReadableDatabase();
        String raw = "SELECT * FROM " + ratesDB.tbname_generic_imagedb
                + " WHERE "+ratesDB.gen_trans+" = '"+id+"' AND "+ratesDB.gen_module+" = 'distribution'";
        Cursor c = myDataBase.rawQuery(raw, null);
        JSONArray resultSet = new JSONArray();
        c.moveToFirst();
        try {
            while (!c.isAfterLast()) {
                JSONObject js = new JSONObject();
                String ids = c.getString(c.getColumnIndex(ratesDB.gen_id));
                String tr = c.getString(c.getColumnIndex(ratesDB.gen_trans));
                String module = c.getString(c.getColumnIndex(ratesDB.gen_module));
                byte[] im = c.getBlob(c.getColumnIndex(ratesDB.gen_image));
                Bitmap bitmap = BitmapFactory.decodeByteArray(im, 0, im.length);
                byte[] bitmapdata = getBytesFromBitmap(bitmap);
                // get the base 64 string
                String imgString = Base64.encodeToString(bitmapdata, Base64.NO_WRAP);

                js.put("id", ids);
                js.put("distribution_id", tr);
                js.put("module", module);
                js.put("image", imgString);

                resultSet.put(js);
                c.moveToNext();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        c.close();
        //Log.e("result set", resultSet.toString());
        return resultSet;
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
                    toupdid.add(reserveno);
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
            } catch (Exception e) {
                e.printStackTrace();
            }
            //END THREAD RESERVATION API
        }
    }

    public JSONArray getImage(String id) {
        SQLiteDatabase myDataBase = ratesDB.getReadableDatabase();
        String raw = "SELECT * FROM " + ratesDB.tbname_generic_imagedb
                + " WHERE "+ratesDB.gen_trans+" = '"+id+"' AND "+ratesDB.gen_module+" = 'reservation'";
        Cursor c = myDataBase.rawQuery(raw, null);
        JSONArray resultSet = new JSONArray();
        c.moveToFirst();
        try {
            while (!c.isAfterLast()) {
                JSONObject js = new JSONObject();
                String module = c.getString(c.getColumnIndex(ratesDB.gen_module));
                byte[] image = c.getBlob(c.getColumnIndex(ratesDB.gen_image));
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

                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
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
        SQLiteDatabase myDataBase = ratesDB.getReadableDatabase();
        String raw = " SELECT * FROM " + ratesDB.tbname_generic_imagedb
                +" WHERE "+ratesDB.gen_trans+" = '"+trans+"' AND "+ratesDB.gen_module+" = 'acceptance_empty'";
        Cursor cursor = myDataBase.rawQuery(raw, null);
        JSONArray resultSet = new JSONArray();
        cursor.moveToFirst();
        try {
            while (!cursor.isAfterLast()) {
                JSONObject js = new JSONObject();
                String tr = cursor.getString(cursor.getColumnIndex(ratesDB.gen_trans));
                String module = cursor.getString(cursor.getColumnIndex(ratesDB.gen_module));
                byte[] image = cursor.getBlob(cursor.getColumnIndex(ratesDB.gen_image));
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

    public void threadCustomers(){
        db = gendata.getReadableDatabase();
        String query = " SELECT * FROM " + gendata.tbname_customers + " WHERE "
                + gendata.cust_createdby + " = '" + helper.logcount() + "' AND " + gendata.cust_uploadstat
                + " = '1'";
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

                Log.e("JSONcust", jsonParam.toString());
                DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                os.writeBytes(jsonParam.toString());
                os.flush();
                os.close();

                Log.e("STATUS", String.valueOf(conn.getResponseCode()));
                Log.e("MSG", conn.getResponseMessage());

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void getWarehouse(){
        //START THREAD FOR warehouse
        try {
            String resp = null;
            String link = helper.getUrl();
            String series = "http://"+link+"/api/warehouse/get.php";
            URL url = new URL(series);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            // read the response
            InputStream in = new BufferedInputStream(conn.getInputStream());
            resp = convertStreamToString(in);

            if (resp != null) {

                Log.e("Warehouses", "warehouse names : " + resp);

                try {
                    JSONArray jsonArray = new JSONArray(resp);

                    for(int i=0; i<jsonArray.length(); i++){

                        JSONObject json_data = jsonArray.getJSONObject(i);

                        String ware_id = json_data.getString("id");
                        String ware_name = json_data.getString("name");
                        String ware_branch = json_data.getString("branch_id");
                        String ware_stat = json_data.getString("recordstatus");

                        ratesDB.addWarehouse(ware_id, ware_name, ware_branch, ware_stat);

                        Log.e("addwarehouse", "id:"+ware_id+", name:"+ware_name+"," +
                                " stat:"+ware_stat);

                    }

                } catch (final JSONException e) {
                    Log.e("Error", "Json parsing error: " + e.getMessage());
                }

            } else {
                Log.e("Error", "Couldn't get data from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String t = "Couldn't get data from server.";
                        customToast(t);
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //END THREAD FOR barcode series
    }

    public void threadBooking(){
        SQLiteDatabase db = gendata.getReadableDatabase();
        String q = " SELECT * FROM " + gendata.tbname_booking
                + " LEFT JOIN " + gendata.tbname_payment + " ON "
                + gendata.tbname_booking + "." + gendata.book_transaction_no
                + " = " + gendata.tbname_payment + "." + gendata.pay_booking_id
                + " WHERE " + gendata.tbname_payment + "." + gendata.pay_booking_id
                + " != '' AND " + gendata.tbname_booking + "." + gendata.book_createdby
                + " = '" + helper.logcount() + "' AND " + gendata.tbname_booking + "."
                + gendata.book_upds + " = '1'";
        Cursor cx = db.rawQuery(q, null);
        if (cx.getCount() != 0) {
            try {
                String link = helper.getUrl();
                URL url = new URL("http://" + link + "/api/booking/save.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);
                conn.setDoInput(true);
                JSONObject jsonParam = new JSONObject();
                db = gendata.getReadableDatabase();
                JSONArray finalarray = new JSONArray();
                JSONArray pay = null, reserve = null, boxtypes = null, disc = null;

                String query = " SELECT * FROM " + gendata.tbname_booking
                        + " LEFT JOIN " + gendata.tbname_payment + " ON "
                        + gendata.tbname_booking + "." + gendata.book_transaction_no
                        + " = " + gendata.tbname_payment + "." + gendata.pay_booking_id
                        + " WHERE " + gendata.tbname_payment + "." + gendata.pay_booking_id
                        + " != '' AND " + gendata.tbname_booking + "."
                        + gendata.book_createdby + " = '" + helper.logcount() + "' AND "
                        + gendata.tbname_booking + "." + gendata.book_upds + " = '1'";
                Cursor c = db.rawQuery(query, null);
                c.moveToFirst();

                while (!c.isAfterLast()) {
                    JSONObject json = new JSONObject();
                    String id = c.getString(c.getColumnIndex(gendata.book_id));
                    String trans = c.getString(c.getColumnIndex(gendata.book_transaction_no));
                    String resnum = c.getString(c.getColumnIndex(gendata.book_reservation_no));
                    String customer = c.getString(c.getColumnIndex(gendata.book_customer));
                    String bookdate = c.getString(c.getColumnIndex(gendata.book_book_date));
                    String sched = c.getString(c.getColumnIndex(gendata.book_schedule_date));
                    String asto = c.getString(c.getColumnIndex(gendata.book_assigned_to));
                    String bookstat = c.getString(c.getColumnIndex(gendata.book_booking_status));
                    String booktype = c.getString(c.getColumnIndex(gendata.book_type));
                    String crby = c.getString(c.getColumnIndex(gendata.book_createdby));

                    json.put("id", id);
                    json.put("transaction_no", trans);
                    json.put("reservation_no", resnum);
                    json.put("customer", customer);
                    json.put("booking_date", bookdate);
                    json.put("schedule_date", sched);
                    json.put("assigned_to", asto);
                    json.put("booking_stat", bookstat);
                    json.put("booking_type", booktype);
                    json.put("created_by", crby);
                    boxtypes = getAllBookingConsignee(trans);
                    reserve = getBookingPayment(trans);
                    disc = getBookingDiscount(trans);

                    json.put("booking_box", boxtypes);
                    json.put("payment", reserve);
                    json.put("discounts", disc);
                    json.put("customer_information", getBookingCustomer(customer));
                    json.put("booking_image", getBookingImage(trans));
                    booknums.add(trans);
                    finalarray.put(json);

                    c.moveToNext();
                }
                c.close();
                jsonParam.accumulate("data", finalarray);
                Log.e("JSONbook", jsonParam.toString());
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

    public JSONArray getBookingCustomer(String id) {
        SQLiteDatabase myDataBase = gendata.getReadableDatabase();
        String raw = "SELECT * FROM " + gendata.tbname_customers
                +" WHERE "+gendata.cust_accountnumber+" = '"+id+"'";
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

    public JSONArray getBookingImage(String id) {
        SQLiteDatabase myDataBase = ratesDB.getReadableDatabase();
        String raw = "SELECT * FROM " + ratesDB.tbname_generic_imagedb
                + " WHERE "+ratesDB.gen_trans+" = '"+id+"' AND "+ratesDB.gen_module+" = 'booking'";
        Cursor c = myDataBase.rawQuery(raw, null);
        JSONArray resultSet = new JSONArray();
        c.moveToFirst();
        try {
            while (!c.isAfterLast()) {
                JSONObject js = new JSONObject();
                String module = c.getString(c.getColumnIndex(ratesDB.gen_module));
                String trans = c.getString(c.getColumnIndex(ratesDB.gen_trans));
                byte[] image = c.getBlob(c.getColumnIndex(ratesDB.gen_image));
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
        Log.e("result_set", resultSet.toString());
        return resultSet;
    }

    public JSONArray getBookingDiscount(String id) {
        SQLiteDatabase myDataBase = gendata.getReadableDatabase();
        String raw = "SELECT * FROM " + gendata.tbname_discount
                + " WHERE "+gendata.disc_trans_no+" = '"+id+"'";
        Cursor c = myDataBase.rawQuery(raw, null);
        JSONArray resultSet = new JSONArray();
        c.moveToFirst();
        try {
            while (!c.isAfterLast()) {
                JSONObject js = new JSONObject();
                String trans = c.getString(c.getColumnIndex(gendata.disc_trans_no));
                String disc = c.getString(c.getColumnIndex(gendata.disc_discount));
                String remarks = c.getString(c.getColumnIndex(gendata.disc_remarks));

                js.put("transaction_no", trans);
                js.put("discount", disc);
                js.put("remarks", remarks);
                resultSet.put(js);
                c.moveToNext();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        c.close();
        Log.e("result_set", resultSet.toString());
        return resultSet;
    }

    public void threadIncident(){
        db = gendata.getReadableDatabase();
        String query = " SELECT * FROM " + gendata.tbname_incident
                + " WHERE " + gendata.inc_createdby + " = '" + helper.logcount() + "' AND "
                +gendata.inc_upds+" = '1'";
        Cursor cx = db.rawQuery(query, null);
        if (cx.getCount() != 0) {
            //THREAD FOR incident API
            try {
                String link = helper.getUrl();
                URL url = new URL("http://" + link + "/api/ticket/save.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);
                conn.setDoInput(true);

                JSONObject jsonParam = new JSONObject();
                jsonParam.accumulate("data", getAllIncidents());

                Log.e("JSONinc", jsonParam.toString());
                DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                //os.writeBytes(URLEncoder.encode(jsonParam.toString(), "UTF-8"));
                os.writeBytes(jsonParam.toString());
                os.flush();
                os.close();

                Log.i("STATUS", String.valueOf(conn.getResponseCode()));
                Log.i("MSG", conn.getResponseMessage());

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //END THREAD incident API
    }

    public void threadLoading(){
        db = ratesDB.getReadableDatabase();
        String q = " SELECT * FROM " + ratesDB.tb_loading + " WHERE " + ratesDB.load_stat + " = '1' AND "
                + ratesDB.load_createdby + " = '" + helper.logcount() + "' AND "
                + ratesDB.load_upds + " = '1'";
        Cursor cx = db.rawQuery(q, null);
        if (cx.getCount() != 0) {
            try {
                String link = helper.getUrl();
                URL url = new URL("http://" + link + "/api/loading/save.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);
                conn.setDoInput(true);
                JSONObject jsonParam = new JSONObject();
                db = ratesDB.getReadableDatabase();
                JSONArray finalarray = new JSONArray();
                JSONArray boxtypes = null;

                String query = " SELECT * FROM " + ratesDB.tb_loading + " WHERE "
                        + ratesDB.load_stat + " = '1' AND "
                        + ratesDB.load_createdby + " = '" + helper.logcount() + "' AND "
                        + ratesDB.load_upds + " = '1'";
                Cursor c = db.rawQuery(query, null);
                c.moveToFirst();

                while (!c.isAfterLast()) {
                    JSONObject json = new JSONObject();
                    String id = c.getString(c.getColumnIndex(ratesDB.load_id));
                    String date = c.getString(c.getColumnIndex(ratesDB.load_date));
                    String shipper = c.getString(c.getColumnIndex(ratesDB.load_shipper));
                    String container = c.getString(c.getColumnIndex(ratesDB.load_container));
                    String eta = c.getString(c.getColumnIndex(ratesDB.load_eta));
                    String etd = c.getString(c.getColumnIndex(ratesDB.load_etd));
                    String by = c.getString(c.getColumnIndex(ratesDB.load_createdby));
                    json.put("id", id);
                    json.put("load_date", date);
                    json.put("load_shipper", shipper);
                    json.put("load_container", container);
                    json.put("load_eta", eta);
                    json.put("load_etd", etd);
                    json.put("createdby", by);
                    json.put("loading_boxes", getAllLoadbox(id));
                    loadids.add(id);
                    finalarray.put(json);
                    c.moveToNext();
                }
                c.close();
                jsonParam.accumulate("data", finalarray);
                Log.e("JSONloads", jsonParam.toString());
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

    public void threadUnloading(){
        SQLiteDatabase db = gendata.getReadableDatabase();
        String q = " SELECT * FROM "
                + gendata.tb_unload + " WHERE " + gendata.unload_stat + " = '1' AND "
                + gendata.unload_con_by + " = '" + helper.logcount() + "' AND "
                + gendata.unload_upds + " = '1'";
        Cursor cx = db.rawQuery(q, null);
        if (cx.getCount() != 0) {
            try {
                String link = helper.getUrl();
                URL url = new URL("http://" + link + "/api/unloading/save.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);
                conn.setDoInput(true);
                JSONObject jsonParam = new JSONObject();
                db = gendata.getReadableDatabase();
                JSONArray finalarray = new JSONArray();
                JSONArray boxtypes = null;

                String query = " SELECT * FROM " + gendata.tb_unload
                        + " WHERE " + gendata.unload_stat + " = '1' AND "
                        + gendata.unload_con_by + " = '" + helper.logcount() + "' AND "
                        + gendata.unload_upds + " = '1'";

                Cursor c = db.rawQuery(query, null);
                c.moveToFirst();
                while (!c.isAfterLast()) {
                    JSONObject json = new JSONObject();
                    String id = c.getString(c.getColumnIndex(gendata.unload_id));
                    String date = c.getString(c.getColumnIndex(gendata.unload_date));
                    String forwarder = c.getString(c.getColumnIndex(gendata.unload_forward));
                    String container = c.getString(c.getColumnIndex(gendata.unload_con_number));
                    String eta = c.getString(c.getColumnIndex(gendata.unload_eta));
                    String start = c.getString(c.getColumnIndex(gendata.unload_timestart));
                    String end = c.getString(c.getColumnIndex(gendata.unload_timeend));
                    String by = c.getString(c.getColumnIndex(gendata.unload_con_by));
                    json.put("id", id);
                    json.put("unload_date", date);
                    json.put("unload_shipper", forwarder);
                    json.put("unload_container", container);
                    json.put("unload_eta", eta);
                    json.put("time_start", start);
                    json.put("time_end", end);
                    json.put("createdby", by);
                    json.put("unloading_boxes", getAllUnloadBox(id));
                    json.put("unloading_boxes_image", getUnloadImage(id));
                    unloadids.add(id);
                    finalarray.put(json);
                    c.moveToNext();
                }
                c.close();
                jsonParam.accumulate("data", finalarray);
                Log.e("JSONunloads", jsonParam.toString());
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

    public JSONArray getAllUnloadBox(String id) {
        SQLiteDatabase myDataBase = gendata.getReadableDatabase();
        String raw = " SELECT * FROM "
                + gendata.tb_unloadbox+" WHERE "+gendata.unloadbox_trans+" = '"+id+"'";
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
        return resultSet;
    }

    public JSONArray getUnloadImage(String trans) {
        SQLiteDatabase myDataBase = ratesDB.getReadableDatabase();
        String raw = " SELECT * FROM " + ratesDB.tbname_unloadingbox_image
                +" WHERE "+ratesDB.unbi_trans+" = '"+trans+"'";
        Cursor cursor = myDataBase.rawQuery(raw, null);
        JSONArray resultSet = new JSONArray();
        cursor.moveToFirst();
        try {
            while (!cursor.isAfterLast()) {
                JSONObject js = new JSONObject();
                String tr = cursor.getString(cursor.getColumnIndex(ratesDB.unbi_trans));
                String bnum = cursor.getString(cursor.getColumnIndex(ratesDB.unbi_boxnumber));
                byte[] image = cursor.getBlob(cursor.getColumnIndex(ratesDB.unbi_image));
                Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
                byte[] bitmapdata = getBytesFromBitmap(bitmap);

                // get the base 64 string
                String imgString = Base64.encodeToString(bitmapdata, Base64.NO_WRAP);

                js.put("transaction_number", tr);
                js.put("boxnumber", bnum);
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

    public JSONArray getAllLoadbox(String id) {
        SQLiteDatabase myDataBase = ratesDB.getReadableDatabase();
        String raw = " SELECT * FROM " + ratesDB.tb_loadbox+" WHERE "+ratesDB.loadbox_trans+" = '"+id+"'";
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

    public void getsource(String x){
        try {
            String link = helper.getUrl();
            String response = null;
            String geturl = "http://"+link+"/api/"+x+"/get.php";
            URL url = new URL(geturl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            // read the response
            InputStream in = new BufferedInputStream(conn.getInputStream());
            response = convertStreamToString(in);
            Log.e("response", response);

            if (!(response.equals(null))) {
                JSONArray jsonArray = new JSONArray(response);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject json_data = jsonArray.getJSONObject(i);

                    String id = json_data.getString("id");
                    String name = json_data.getString("name");
                    String type = json_data.getString("type");
                    String stat = json_data.getString("recordstatus");
                    ratesDB.addSD(id, name, type, stat);
                }

            } else {
                Log.e("data", "Else error ");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String t = "Sync error, please try again.";
                        getPost();
                        customToast(t);
                    }
                });

            }
        } catch(Exception e){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String t = "Sync error, please try again.";
                    getPost();
                    customToast(t);
                }
            });
        }
    }

    public void customToast(String txt){
        Toast toast = new Toast(getApplicationContext());
        toast.setDuration(Toast.LENGTH_SHORT);
        LayoutInflater inflater = this.getLayoutInflater();
        View view = inflater.inflate(R.layout.toast, null);
        TextView t = (TextView)view.findViewById(R.id.toasttxt);
        t.setText(txt);
        toast.setView(view);
        toast.setGravity(Gravity.TOP | Gravity.RIGHT, 15, 50);
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.enterright);
        view.startAnimation(animation);
        toast.show();
    }

    public JSONArray getAllBookingConsignee(String id) {
        SQLiteDatabase myDataBase = gendata.getReadableDatabase();
        String raw = "SELECT * FROM " + gendata.tbname_booking_consignee_box+" WHERE "+gendata.book_con_transaction_no+" = '"+id+"'";
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

    public JSONArray getBookingPayment(String id) {
        SQLiteDatabase myDataBase = gendata.getReadableDatabase();
        String raw = "SELECT * FROM " + gendata.tbname_payment
                +" WHERE "+gendata.pay_booking_id+" = '"+id+"' GROUP BY "+gendata.pay_booking_id;
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

    //get all incident report
    public JSONArray getAllIncidents() {
        SQLiteDatabase myDataBase = gendata.getReadableDatabase();
        String raw = " SELECT * FROM " + gendata.tbname_incident
                +" WHERE "+gendata.inc_upds+" = '1' AND "
                +gendata.inc_createdby+" = '"+helper.logcount()+"'";
        Cursor c = myDataBase.rawQuery(raw, null);
        JSONArray resultSet = new JSONArray();
        c.moveToFirst();
        try {
            while (!c.isAfterLast()) {
                JSONObject js = new JSONObject();
                String idtrue = c.getString(c.getColumnIndex(gendata.inc_id));
                String id = c.getString(c.getColumnIndex(gendata.inc_transnum));
                String type = c.getString(c.getColumnIndex(gendata.inc_type));
                String reason = c.getString(c.getColumnIndex(gendata.inc_reason));
                String by = c.getString(c.getColumnIndex(gendata.inc_createdby));
                String d = c.getString(c.getColumnIndex(gendata.inc_createddate));

                js.put("id", id);
                js.put("incident_type", type);
                js.put("reason", reason);
                js.put("createdby", by);
                js.put("created_date", d);
                js.put("images", getIncidentImage(id));
                incids.add(id);
                resultSet.put(js);
                c.moveToNext();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        c.close();
        //Log.e("result set", resultSet.toString());
        return resultSet;
    }

    //get incident images by transaction
    public JSONArray getIncidentImage(String tr) {
        SQLiteDatabase myDataBase = gendata.getReadableDatabase();
        String raw = " SELECT * FROM " + gendata.tbname_incimages + " WHERE "
                + gendata.inc_img_transaction_no + " = '" + tr + "'";
        Cursor c = myDataBase.rawQuery(raw, null);
        JSONArray resultSet = new JSONArray();
        c.moveToFirst();
        try {
            while (!c.isAfterLast()) {
                JSONObject js = new JSONObject();
                byte[] image = c.getBlob(c.getColumnIndex(gendata.inc_img_imageblob));
                js.put("incident_image", image);
                resultSet.put(js);
                c.moveToNext();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        c.close();
        //Log.e("result set", resultSet.toString());
        return resultSet;
    }

    //send to api warehouse inventory content
    public JSONArray sendAllWarehouseInventory() {
        SQLiteDatabase myDataBase = gendata.getReadableDatabase();
        String raw = "SELECT * FROM " + gendata.tb_acceptance;
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

    //api call for loaded boxes
    public void getLoadItems(){
        //START THREAD FOR warehouse
        try {
            String resp = null;
            String link = helper.getUrl();
            String series = "http://"+link+"/api/loading/get.php";
            URL url = new URL(series);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            // read the response
            InputStream in = new BufferedInputStream(conn.getInputStream());
            resp = convertStreamToString(in);

            if (resp != null) {

                Log.e("Loads", " : " + resp);

                    JSONArray jsonArray = new JSONArray(resp);

                    for(int i=0; i<jsonArray.length(); i++){

                        JSONObject json_data = jsonArray.getJSONObject(i);
                        String id = json_data.getString("id");
                        String loaddate = json_data.getString("loaded_date");
                        String ship = json_data.getString("shipping_name");
                        String cont = json_data.getString("container_no");
                        String eta = json_data.getString("eta");
                        String etd = json_data.getString("etd");
                        String by = json_data.getString("createdby");
                        String[] boxnum = json_data.getString("box_number").split(",");
                        if (checkLoadId(id)){
                            ratesDB.addFinalload(id, loaddate, ship, cont, eta, etd, by, "1", "2");
                            for (String x : boxnum){
                                ratesDB.addload( id, x, "2");
                                updateBnCheckInv(x);
                            }
                        }else{
                            ratesDB.updFinalload(id, loaddate, ship, cont, eta, etd, by, "1", "2");
                            for (String x : boxnum){
                                ratesDB.updload( id, x, "2");
                                updateBnCheckInv(x);
                            }
                        }
                    }

            } else {
                Log.e("Error", "Couldn't get data from server.");
            }
        } catch (Exception e) {
            Log.e("Error", "error: " + e.getMessage());
        }
        //END THREAD FOR barcode series
    }

    //api call for unloaded items
    public void getUnLoadsItem(){
        //START THREAD FOR warehouse
        try {
            String resp = null;
            String link = helper.getUrl();
            String series = "http://"+link+"/api/unloading/get.php";
            URL url = new URL(series);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            // read the response
            InputStream in = new BufferedInputStream(conn.getInputStream());
            resp = convertStreamToString(in);
            if (resp != null) {
                Log.e("Unloads", " : " + resp);
                    JSONArray jsonArray = new JSONArray(resp);
                    for(int i=0; i<jsonArray.length(); i++){
                        JSONObject json_data = jsonArray.getJSONObject(i);
                        String id = json_data.getString("id");
                        String unload_date = json_data.getString("unload_date");
                        String container_no = json_data.getString("container_no");
                        String forwarder_name = json_data.getString("forwarder_name");
                        String arrival_time = json_data.getString("arrival_time");
                        String time_start = json_data.getString("time_start");
                        String time_end = json_data.getString("time_end");
                        String plate = json_data.getString("plate_no");
                        String driver = json_data.getString("driver_name");
                        String createdby = json_data.getString("createdby");
                        String qty = json_data.getString("qty");
                        String[] boxnum = json_data.getString("box_number").split(",");
                        if (checkUnLoadId(id)){
                            gendata.addFinalUnload(id, unload_date, container_no,
                                    forwarder_name, time_start, time_end, plate, driver,
                                    arrival_time, createdby, "1", "2");
                            for (String x : boxnum){
                                gendata.addUnload( id, x, "2");
                                if (createdby.equals(helper.logcount()+"")) {
                                    saveInventoryPartner(x);
                                }
                            }
                        }else{
                            gendata.updFinalUnloaded(id, unload_date, container_no,
                                    forwarder_name, time_start, time_end, plate, driver, arrival_time, createdby, "1", "2");
                            for (String x : boxnum){
                                gendata.updUnloadedBox( id, x, "2");
                                if (createdby.equals(helper.logcount()+"")) {
                                    saveInventoryPartner(x);
                                }
                            }

                        }
                    }
            } else {
                Log.e("Error", "Couldn't get data from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String t = "Couldn't get data from server.";
                        customToast(t);
                    }
                });
            }
        } catch (Exception e) {}
        //END THREAD FOR barcode series
    }

    public void getSubStat(){
        try {
            String resp = null;
            String link = helper.getUrl();
            String series = "http://"+link+"/api/delivery/getsubstat.php";
            URL url = new URL(series);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            // read the response
            InputStream in = new BufferedInputStream(conn.getInputStream());
            resp = convertStreamToString(in);

            if (resp != null) {

                Log.e("SubStat", " : " + resp);

                JSONArray jsonArray = new JSONArray(resp);

                for(int i=0; i<jsonArray.length(); i++){

                    JSONObject json_data = jsonArray.getJSONObject(i);
                    String id = json_data.getString("id");
                    String name = json_data.getString("name");
                    String statid = json_data.getString("status_id");
                    ratesDB.addDeliverySubStatus(id, name, statid);
                }

            } else {
                Log.e("Error", "Couldn't get data from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String t = "Couldn't get data from server.";
                        customToast(t);
                    }
                });
            }
        } catch (Exception e) {
            Log.e("Error", "error: " + e.getMessage());
        }
    }

    public boolean checkNumPartner(String bn){
        SQLiteDatabase db = gendata.getReadableDatabase();
        Cursor x = db.rawQuery(" SELECT * FROM "+gendata.tbname_partner_inventory
                +" WHERE "+gendata.partinv_boxnumber+" = '"+bn+"'", null);
        if (x.getCount() == 0){
            return true;
        }else{
            return false;
        }
    }

    public String getBoxNamesss(String barcode){
        String name = null;
        SQLiteDatabase db = gendata.getReadableDatabase();
        String query = "SELECT * FROM "+gendata.tbname_booking_consignee_box+" LEFT JOIN "+gendata.tbname_boxes
                +" ON "+gendata.tbname_boxes+"."+gendata.box_name+" = "+gendata.tbname_booking_consignee_box+"."+gendata.book_con_boxtype
                +" WHERE "+gendata.book_con_box_number+" = '"+barcode+"'";
        Cursor c = db.rawQuery(query, null);
        if (c.getCount() != 0){
            c.moveToNext();
            name = c.getString(c.getColumnIndex(gendata.book_con_box_id));
            Log.e("boxname", name);
        }
        return name;
    }

    public void saveInventoryPartner(String bn){
        SQLiteDatabase db = gendata.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(gendata.partinv_boxnumber, bn);
        cv.put(gendata.partinv_boxtype_fillempty, "1");
        cv.put(gendata.partinv_boxtype, getBox(bn));
        cv.put(gendata.partinv_stat, "0");
        db.insert(gendata.tbname_partner_inventory, null, cv);
        db.close();
    }

    public String getBox(String barcode){
        String name = null;
        SQLiteDatabase db = gendata.getReadableDatabase();
        String query = "SELECT * FROM "+gendata.tbname_booking_consignee_box+" LEFT JOIN "
                +gendata.tbname_boxes
                +" ON "+gendata.tbname_boxes+"."+gendata.box_name+" = "
                +gendata.tbname_booking_consignee_box+"."+gendata.book_con_boxtype
                +" WHERE "+gendata.book_con_box_number+" = '"+barcode+"'";
        Cursor c = db.rawQuery(query, null);
        if (c.getCount() != 0){
            c.moveToNext();
            name = c.getString(c.getColumnIndex(gendata.book_con_box_id));
            Log.e("boxname", name);
        }
        return name;
    }

    //checking for loading ids
    public boolean checkLoadId(String id){
        boolean ok = false;
        SQLiteDatabase db = ratesDB.getReadableDatabase();
        Cursor x = db.rawQuery(" SELECT * FROM "+ratesDB.tb_loading
        +" WHERE "+ratesDB.load_id+" = '"+id+"'", null);
        if (x.getCount() == 0){
            ok = true;

        }else{
            ok = false;
        }

        db.close();
        return ok;
    }

    //checking for unloading id
    public boolean checkUnLoadId(String id){
        SQLiteDatabase db = gendata.getReadableDatabase();
        Cursor x = db.rawQuery(" SELECT * FROM "+gendata.tb_unload
        +" WHERE "+gendata.unload_id+" = '"+id+"'", null);
        if (x.getCount() == 0){
            return true;
        }else{
            return false;
        }
    }

    //api call for box contents
    public void getBoxContents(){
        //START THREAD FOR booking data
        try {
            String resp = null;
            String link = helper.getUrl();
            String series = "http://"+link+"/api/booking/getcontents.php";
            URL url = new URL(series);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            // read the response
            InputStream in = new BufferedInputStream(conn.getInputStream());
            resp = convertStreamToString(in);
            Log.e("boxcontent", ""+resp);
            if (resp != null) {
                JSONArray jsonArray = new JSONArray(resp);
                for(int i=0; i<jsonArray.length(); i++){
                    JSONObject json_data = jsonArray.getJSONObject(i);
                    String id = json_data.getString("id");
                    String description = json_data.getString("name");

                    ratesDB.addBoxContent(id, description);
                }

            } else {
                Log.e("Error", "Couldn't get data from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String t = "Couldn't get data from server.";
                        customToast(t);
                    }
                });
            }
        } catch (Exception e) {
            Log.e("Error", " error: " + e.getMessage());
        }
        //END THREAD FOR booking
    }

    //get expense types
    public void getExpenseTypes(){
        //START THREAD FOR expense type data
        try {
            String resp = null;
            String link = helper.getUrl();
            String series = "http://"+link+"/api/source/getexpensetypes.php";
            URL url = new URL(series);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            // read the response
            InputStream in = new BufferedInputStream(conn.getInputStream());
            resp = convertStreamToString(in);
            Log.e("expensetypes", ""+resp);
            if (resp != null) {
                JSONArray jsonArray = new JSONArray(resp);
                for(int i=0; i<jsonArray.length(); i++){
                    JSONObject json_data = jsonArray.getJSONObject(i);
                    String id = json_data.getString("id");
                    String description = json_data.getString("name");
                    addItemsExpenseTodb(description);
                }

            } else {
                Log.e("Error", "Couldn't get data from server.");
            }
        } catch (Exception e) {
            Log.e("Error", " " + e.getMessage());
        }
        //END THREAD FOR expense type
    }

    public void getNSBrates(){
        //START THREAD FOR booking data
        try {
            String resp = null;
            String link = helper.getUrl();
            String series = "http://"+link+"/api/boxrate/getnsbrate.php";
            URL url = new URL(series);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            // read the response
            InputStream in = new BufferedInputStream(conn.getInputStream());
            resp = convertStreamToString(in);
            Log.e("nsbrates", ""+resp);
            if (resp != null) {
                JSONArray jsonArray = new JSONArray(resp);
                for(int i=0; i<jsonArray.length(); i++){
                    JSONObject json_data = jsonArray.getJSONObject(i);
                    String boxid = json_data.getString("boxid");
                    String sourceid = json_data.getString("sourceid");
                    String destinationid = json_data.getString("destinationid");
                    String nsbrate = json_data.getString("rate");

                    if (checkNSBrate(boxid, sourceid, destinationid, nsbrate)){
                        gendata.updateNSBrates(boxid, sourceid, destinationid, nsbrate);
                    }else{
                        gendata.addNSBrate(boxid, sourceid, destinationid, nsbrate);
                    }
                }

            } else {
                Log.e("Error", "Couldn't get data from server.");
            }
        } catch (Exception e) {
            Log.e("Error", " error: " + e.getMessage());
        }
        //END THREAD FOR booking
    }

    //barcode to inventory
    public void getBarCodeInventory(){
        //START THREAD FOR booking data
        try {
            String resp = null;
            String link = helper.getUrl();
            String series = "http://"+link+"/api/boxnumberseries/getyours.php?id="+helper.logcount();
            URL url = new URL(series);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            // read the response
            InputStream in = new BufferedInputStream(conn.getInputStream());
            resp = convertStreamToString(in);
            Log.e("barcodes", ""+resp);
            if (resp != null) {
                JSONArray jsonArray = new JSONArray(resp);
                for(int i=0; i<jsonArray.length(); i++){
                    JSONObject json_data = jsonArray.getJSONObject(i);
                    String series_start = json_data.getString("series_start");
                    String series_end = json_data.getString("series_end");
                    String quantity = json_data.getString("quantity");
                    DateFormat df = new SimpleDateFormat("yy"); // Just the year, with 2 digits
                    String formattedDate = df.format(Calendar.getInstance().getTime());
                    Log.e("year_only",formattedDate);

                    for (int str = Integer.parseInt(series_start);str <= Integer.parseInt(series_end); str++){

                        if (String.valueOf(str).length() < 2 ){
                               zeros = "0000000";
                            }else if (String.valueOf(str).length() < 3 ){
                                zeros = "000000";
                            }else if (String.valueOf(str).length() < 4 ){
                                zeros = "00000";
                            }else if (String.valueOf(str).length() < 5 ){
                                zeros = "0000";
                            }else if (String.valueOf(str).length() < 6 ){
                                zeros = "000";
                            }else if (String.valueOf(str).length() < 7 ){
                                zeros = "00";
                            }else if (String.valueOf(str).length() < 8 ){
                                zeros = "0";
                            }else if (String.valueOf(str).length() == 8 ){
                                zeros = "0";
                            }
                            String barcode = formattedDate+""+zeros+""+str;
                            ratesDB.addBarcodeInventory(barcode,"0");
                            //Log.e("barcode_inv", barcode);

                    }
                }

            } else {
                Log.e("Error", "Couldn't get data from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String t = "Couldn't get data from server.";
                        customToast(t);
                    }
                });
            }
        } catch (Exception e) {
            Log.e("Error", " error: " + e.getMessage());
        }
        //END THREAD FOR booking
    }

    //get driver open barcode
    public void getBarCodeDriverInventory(){
        //START THREAD FOR booking data
        try {
            String resp = null;
            String link = helper.getUrl();
            String series = "http://"+link+"/api/boxnumberseries/getbydriver.php?id="+helper.logcount();
            URL url = new URL(series);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            // read the response
            InputStream in = new BufferedInputStream(conn.getInputStream());
            resp = convertStreamToString(in);
            Log.e("barcodes", ""+resp);
            if (resp != null) {
                JSONArray jsonArray = new JSONArray(resp);
                for(int i=0; i<jsonArray.length(); i++){
                    JSONObject json_data = jsonArray.getJSONObject(i);
                    String id = json_data.getString("id");
                    String trans = json_data.getString("transaction_no");
                    String[] box_number = json_data.getString("box_number").split(",");
                    String stat = "0";
                    if (id != null && trans != null) {
                        for (int ix = 0; ix < box_number.length; ix++) {
                            ratesDB.addBarcodeDriverInventory(box_number[ix], stat);
                        }
                    }
                }

            } else {
                Log.e("Error", "Couldn't get data from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String t = "Couldn't get data from server.";
                        customToast(t);
                    }
                });
            }
        } catch (Exception e) {
            Log.e("Error", " error: " + e.getMessage());
        }
        //END THREAD FOR booking
    }

    //api call for bookings
    public void getBookings(){
        //START THREAD FOR booking data
        try {
            String resp = null;
            String link = helper.getUrl();
            String series = "http://"+link+"/api/booking/get.php";
            URL url = new URL(series);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            // read the response
            InputStream in = new BufferedInputStream(conn.getInputStream());
            resp = convertStreamToString(in);
            Log.e("booking", ""+resp);
            if (resp != null) {
                    JSONArray jsonArray = new JSONArray(resp);
                    for(int i=0; i<jsonArray.length(); i++){
                        JSONObject json_data = jsonArray.getJSONObject(i);
                        String transaction_no = json_data.getString("transaction_no");
                        String customer = json_data.getString("customer");
                        String book_date = json_data.getString("book_date");
                        String createdby = json_data.getString("createdby");
                        String reservation_no = json_data.getString("reservation_no");
                        String booking_status = json_data.getString("booking_status");
                        String booking_type = json_data.getString("booking_type");

                        if (checkBookingExist(transaction_no)){
                            gendata.updBooking( transaction_no, reservation_no, customer,
                                    book_date, booking_status, booking_type, createdby, "2");
                            JSONArray jarray = json_data.getJSONArray("gpx_booking_consignee_box");
                            for(int x=0; x<jarray.length(); x++){
                                JSONObject jx = jarray.getJSONObject(x);
                                String consignee = jx.getString("consignee");
                                String source_id = jx.getString("source_id");
                                String destination_id = jx.getString("destination_id");
                                String boxtype = jx.getString("boxtype");
                                String box_number = jx.getString("box_number");
                                String hardport = jx.getString("hardport");
                                String bcont = jx.getString("box_content");
                                String status = jx.getString("status");

                                //add consignee booking
                                gendata.updConsigneeBookingExist(consignee, boxtype, source_id, destination_id,
                                        transaction_no, box_number, status, hardport, bcont);

                                if (checkIfInInventory(box_number)){
                                    gendata.updateInvBoxnumber("1", box_number, "2");
                                }else {
                                    //add inventory
                                    gendata.addtoDriverInv(getBoxtypeID(boxtype), box_number, "1", "2");
                                }
                            }
                        }else{
                            gendata.addBooking( transaction_no, reservation_no, customer,
                                    book_date, booking_status, booking_type, createdby, "2");

                            JSONArray jarray = json_data.getJSONArray("gpx_booking_consignee_box");
                            for(int x=0; x<jarray.length(); x++){
                                JSONObject jx = jarray.getJSONObject(x);
                                String consignee = jx.getString("consignee");
                                String source_id = jx.getString("source_id");
                                String destination_id = jx.getString("destination_id");
                                String boxtype = jx.getString("boxtype");
                                String box_number = jx.getString("box_number");
                                String hardport = jx.getString("hardport");
                                String bcont = jx.getString("box_content");
                                String status = jx.getString("status");

                                //add consignee booking
                                gendata.addConsigneeBooking( consignee, boxtype, source_id, destination_id,
                                        transaction_no, box_number, status, hardport, bcont);

                                if (checkIfInInventory(box_number)){
                                    gendata.updateInvBoxnumber("1", box_number, "2");
                                }else {
                                    //add inventory
                                    gendata.addtoDriverInv(getBoxtypeID(boxtype), box_number, "1", "2");
                                }
                            }
                        }
                    }

            } else {
                Log.e("Error", "Couldn't get data from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String t = "Couldn't get data from server.";
                        customToast(t);
                    }
                });
            }
        } catch (Exception e) {
            Log.e("Error", " error: " + e.getMessage());
        }
        //END THREAD FOR booking
    }

    //api callback for warehouse acceptance
    public void getWarehouseAcceptance(){
        //START THREAD FOR warehouse
        try {
            String resp = null;
            String link = helper.getUrl();
            String series = "http://"+link+"/api/warehouseacceptance/get.php";
            URL url = new URL(series);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            // read the response
            InputStream in = new BufferedInputStream(conn.getInputStream());
            resp = convertStreamToString(in);

            if (resp != null) {
                Log.e("warehouseacceptance", " : " + resp);
                JSONArray jsonArray = new JSONArray(resp);
                for(int i=0; i<jsonArray.length(); i++){
                    JSONObject json_data = jsonArray.getJSONObject(i);
                    String warehouse_name = json_data.getString("warehouse_name");
                    String truck_no = json_data.getString("truck_no");
                    String trans = json_data.getString("id");
                    String deliver_by = json_data.getString("deliver_by");
                    String driver_id = json_data.getString("driver_id");
                    String accepted_by = json_data.getString("accepted_by");
                    String accepted_date = json_data.getString("accepted_date");
                    String[] box_number = json_data.getString("box_number").split(",");

                    if (checkAcceptance(trans)){
                        Log.e("error","error");
                        String username = helper.getFullname(helper.logcount()+"");
                        if (compareNames(accepted_by, username)){
                            accepted_by = helper.logcount()+"";
                        }
                        gendata.updAcceptanceExist(trans, driver_id, warehouse_name, truck_no, accepted_date,
                                accepted_by, "2" , "2");

                        Log.e("warehousename", warehouse_name);
                        for (String x : box_number){
                            gendata.updAcceptanceBoxnumber( trans, getBoxtypeFromDist(x), x,"2");
                            Log.e("box_acceptance", getBoxtypeFromDist(x)+", "+x);
                            updateBxInv(x);
                            deleteDriverInv(x);
                        }
                    }else{
                        String username = helper.getFullname(helper.logcount()+"");
                        if (compareNames(accepted_by, username)){
                            accepted_by = helper.logcount()+"";
                        }
                        gendata.addNewAcceptance(trans, driver_id, warehouse_name, truck_no, accepted_date,
                                accepted_by, "2" , "2");

                        Log.e("warehousename", warehouse_name);
                        for (String x : box_number){
                            gendata.addAcceptanceBoxnumber( trans, getBoxtypeFromDist(x), x,"2");
                            Log.e("box_acceptance", getBoxtypeFromDist(x)+", "+x);
                            updateBxInv(x);
                            deleteDriverInv(x);
                        }
                    }
                }
            } else {
                Log.e("Error", "Couldn't get data from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String r = "Couldn't get data from server, trying again....";
                        customToast(r);
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateBxInv(String bn){
        SQLiteDatabase db = gendata.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(gendata.chinv_stat, "1");
        db.update(gendata.tbname_checker_inventory, cv,
                gendata.chinv_boxnumber+" = '"+bn+"'", null);
        Log.e("update_inv",bn);
        db.close();
    }

    public void deleteDriverInv(String bn){
        SQLiteDatabase db = gendata.getWritableDatabase();
        db.delete(gendata.tbname_driver_inventory,
                gendata.sdinv_boxnumber+" = '"+bn+"'", null);
        Log.e("delete-sdinv", bn);
        db.close();
    }

    public boolean compareNames(String full, String other){
        if (full.toLowerCase().equals(other.toLowerCase())){
            return true;
        }else{
            return false;
        }
    }

    public String getBoxtypeFromBooking(String bn){
        String id = null;
        SQLiteDatabase db = gendata.getReadableDatabase();
        Cursor c = db.rawQuery(" SELECT * FROM "+gendata.tbname_booking_consignee_box
        +" WHERE "+gendata.book_con_box_number+" = '"+bn+"'", null);
        if (c.moveToNext()){
            id = c.getString(c.getColumnIndex(gendata.book_con_boxtype));
        }
        c.close();
        db.close();
        return id;
    }

    public String getBoxtypeIdFromBooking(String name){
        String id = null;
        SQLiteDatabase db = gendata.getReadableDatabase();
        Cursor c = db.rawQuery(" SELECT * FROM "+gendata.tbname_boxes
        +" WHERE "+gendata.box_name+" = '"+name+"'", null);
        if (c.moveToNext()){
            id = c.getString(c.getColumnIndex(gendata.box_id));
        }
        c.close();
        db.close();
        return id;
    }

    //get the sales driver distributed box
    public void getSalesDriverDistribution(){
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
                Log.e("DistributionsDriver", ": " + resp);
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

                    if (!checkDist(id)) {
                        gendata.addDistribution(id, distribution_type, destination_name, truck_number,
                                remarks, "1", "2", createddate, createdby, 1, null);
                        for (int ix = 0; ix < box_number.length; ix++) {
                            gendata.addTempBoxDist(id, boxtypeid[ix], "", box_number[ix], "2");
                            if (checkBarcodeBnum(box_number[ix])){
                                updateBarcodeInv(box_number[ix]);
                            }else if(checkYourInv(box_number[ix])){
                                updateBxInv(box_number[ix]);
                            }
                            Log.e("boxnumber",box_number[ix]);
                        }
                    }else{
                        gendata.updDist(id, distribution_type, destination_name,truck_number,
                                remarks, "1", "2", createddate, createdby);
                        for (int ix = 0; ix < box_number.length; ix++){
                            gendata.updDistBoxDist( id, boxtypeid[ix],"", box_number[ix], "2");
                            if (checkBarcodeBnum(box_number[ix])){
                                updateBarcodeInv(box_number[ix]);
                            }else if(checkYourInv(box_number[ix])){
                                updateBxInv(box_number[ix]);
                            }

                            Log.e("boxnumber",box_number[ix]);
                        }
                    }
                }

            } else {
                Log.e("Error", "Couldn't get data from server.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void getDirectDistribution(){
        try {
            String resp = null;
            String link = helper.getUrl();
            String urlget = "http://"+link+"/api/distribution/getdirect.php";
            URL url = new URL(urlget);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            // read the response
            InputStream in = new BufferedInputStream(conn.getInputStream());
            resp = convertStreamToString(in);

            if (resp != null) {
                Log.e("Direct", ": " + resp);
                JSONArray jsonArray = new JSONArray(resp);
                for(int i=0; i<jsonArray.length(); i++){
                    JSONObject json_data = jsonArray.getJSONObject(i);
                    String id = json_data.getString("id");
                    String distribution_type = json_data.getString("distribution_type");
                    String destination_name = json_data.getString("destination_name");
                    String mode = json_data.getString("mode_of_shipment");
                    String driver_name = json_data.getString("driver_name");
                    String truck_number = json_data.getString("truck_number");
                    String remarks = json_data.getString("remarks");
                    String createddate = json_data.getString("createddate");
                    String eta = json_data.getString("eta");
                    String etd = json_data.getString("etd");
                    String createdby = json_data.getString("createdby");
                    String[] box_number = json_data.getString("box_number").split(",");
                    String[] boxtypeid = json_data.getString("boxtype_id").split(",");

                    if (!checkDist(id)) {
                        gendata.addDistribution(id, distribution_type, destination_name, truck_number,
                                remarks, "1", "2", createddate, createdby, 1, null);
                        ratesDB.addDistribution(id, distribution_type, mode, destination_name, driver_name, truck_number,
                                remarks, etd, eta,"1", "2", createddate,
                                createdby, "2", 1, null);
                        for (int ix = 0; ix < box_number.length; ix++) {
                            gendata.addTempBoxDist(id, boxtypeid[ix], "", box_number[ix], "2");
                            ratesDB.addPartDistributionBox(id,
                                    getBoxtypeFromXX(box_number[ix]),
                                    box_number[ix], "1");
                            deleteBoxnumberInventory(box_number[ix]);
                        }
                    }else{
                        gendata.updDist(id, distribution_type, destination_name,truck_number,
                                remarks, "1", "2", createddate, createdby);
                        for (int ix = 0; ix < box_number.length; ix++){
                            gendata.updDistBoxDist( id, boxtypeid[ix],"", box_number[ix], "2");

                            Log.e("boxnumber",box_number[ix]);
                        }
                    }
                }

                if (conn.getResponseMessage().equals("OK")){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.dismiss();
                            final AlertDialog.Builder builder = new AlertDialog.Builder(Home.this);
                            builder.setTitle("Information confirmation")
                                    .setMessage("Data has been updated, thank you.")
                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.dismiss();
                                        }
                                    })
                                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.dismiss();
                                        }
                                    });
                            builder.create();
                            builder.setCancelable(false);
                            builder.show();
                        }
                    });
                }else{
                    conn.disconnect();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.dismiss();
                            final AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
                            builder.setTitle("Information confirmation")
                                    .setMessage("Data download has failed, please try again later. thank you.")
                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
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

            } else {
                Log.e("Error", "Couldn't get data from server.");
            }
        } catch (Exception e) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressBar.dismiss();
                    final AlertDialog.Builder builder = new AlertDialog.Builder(Home.this);
                    builder.setTitle("Information confirmation")
                            .setMessage("Data has been updated, thank you.")
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
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

    //get barcodes distributed to driver
    public void getBarcodesDistributiontoDriver(){
        try {
            String resp = null;
            String link = helper.getUrl();
            String urlget = "http://"+link+"/api/boxnumberseries/getdistributedtodriver.php?id="+helper.logcount();
            URL url = new URL(urlget);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            // read the response
            InputStream in = new BufferedInputStream(conn.getInputStream());
            resp = convertStreamToString(in);

            if (resp != null) {
                Log.e("BarcodesToDriver", ":" + resp);
                JSONArray jsonArray = new JSONArray(resp);
                for(int i=0; i<jsonArray.length(); i++){
                    JSONObject json_data = jsonArray.getJSONObject(i);
                    String id = json_data.getString("id");
                    String transaction_no = json_data.getString("transaction_no");
                    String driver_id = json_data.getString("driver_id");
                    String status = json_data.getString("status");
                    String createddate = json_data.getString("createddate");
                    String createdby = json_data.getString("createdby");
                    String[] box_number = json_data.getString("box_number").split(",");

                    for (int ix = 0; ix < box_number.length; ix++) {
                        ratesDB.addBarcodeDistributionBoxnumber(transaction_no,
                                box_number[ix], "1");
                        updateBarcodeInv(box_number[ix]);
                    }
                    ratesDB.addBarcodeDistribution(transaction_no, driver_id, createddate, createdby, "2", status);

                }

            } else {
                Log.e("Error", "Couldn't get data from server.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //get your boxes from distribution
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
                gendata.addtoDriverInv(topitem, bnum, "0", "0");
                Log.e("boxtoinv", "type:" + topitem + ", boxnum: " + bnum);
                c.moveToNext();
            }
        }catch (Exception e){}
    }

    //get warehouse inventory
//    public void getWarehouseInventory(){
//        try {
//            String resp = null;
//            String link = helper.getUrl();
//            String urlget = "http://"+link+"/api/warehouseinventory/get.php";
//            URL url = new URL(urlget);
//            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//            conn.setRequestMethod("GET");
//            // read the response
//            InputStream in = new BufferedInputStream(conn.getInputStream());
//            resp = convertStreamToString(in);
//
//            if (resp != null) {
//                Log.e("WarehouseInventory", " : " + resp);
//                JSONArray jsonArray = new JSONArray(resp);
//                for(int i=0; i<jsonArray.length(); i++){
//                    JSONObject json_data = jsonArray.getJSONObject(i);
//                    String warehouse_name = json_data.getString("warehouse_name");
//                    String manufacturer_name = json_data.getString("manufacturer_name");
//                    String getid = json_data.getString("id");
//                    String createdby = json_data.getString("createdby");
//                    String createddate = json_data.getString("createddate");
//                    String[] box_type = json_data.getString("box_type").split(",");
//                    String[] quantity = json_data.getString("quantity").split(",");
//
//                    for (int ix = 0; ix < box_type.length; ix++){
//                        SQLiteDatabase db = gendata.getReadableDatabase();
//                        String query = " SELECT * FROM "+gendata.tb_acceptance
//                                +" WHERE "+gendata.acc_id+" = '"+getid+"' AND "
//                                +gendata.acc_name+" = '"+manufacturer_name+"' AND "
//                                +gendata.acc_boxtype+" = '"+getBoxtypeID(box_type[ix])+"'";
//                        Cursor acceptance = db.rawQuery( query,null);
//                        if (acceptance.getCount() == 0) {
//                            gendata.addAccBox( getid, getWarehouseId(warehouse_name), manufacturer_name,
//                                    getBoxtypeID(box_type[ix]),
//                                    quantity[ix], createddate, createdby, "2", "2");
//                            Log.e("insert", "boxtype: " + getBoxtypeID(box_type[ix]) + "" +
//                                    ", quantity: " + quantity[ix]+", warehouse: " + getWarehouseId(warehouse_name));
//                        }else{
//                            acceptance.moveToNext();
//                            String id =
//                                    acceptance.getString(acceptance.getColumnIndex(gendata.acc_id));
//                            String q =
//                                    acceptance.getString(acceptance.getColumnIndex(gendata.acc_quantity));
//                            int fullval = (Integer.valueOf(q)+ (Integer.valueOf(quantity[ix])));
//                            gendata.updAccBox( getid, getWarehouseId(warehouse_name), manufacturer_name,
//                                    getBoxtypeID(box_type[ix]), fullval+"", createddate, createdby,
//                                    "2", "2");
//                            Log.e("update", "boxtype: " + getBoxtypeID(box_type[ix]) + "" +
//                                    ", quantity: " + quantity[ix]+", warehouse: " + getWarehouseId(warehouse_name));
//                        }
//                    }
//                }
//
//            } else {
//                Log.e("Error", "Couldn't get data from server.");
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        String r = "Couldn't get data from server, trying again....";
//                        getPost();
//                        customToast(r);
//                    }
//                });
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    public String getBoxtypeFromDist(String bnum){
        String btype = null;
        SQLiteDatabase db = gendata.getReadableDatabase();
        String que = " SELECT * FROM "+gendata.tbname_tempboxes
                +" WHERE "+gendata.dboxtemp_boxnumber+" = '"+bnum+"'";
        Cursor x = db.rawQuery(que, null);
        if (x.moveToNext()){
            btype = x.getString(x.getColumnIndex(gendata.dboxtemp_boxid));
        }
        return btype;
    }

    public String getOrigin(String boxtype){
        String origin = "";
        SQLiteDatabase db = ratesDB.getReadableDatabase();
        String que = " SELECT * FROM "+ratesDB.tbname_rates
                +" WHERE "+ratesDB.rate_boxtype+" = '"+boxtype+"'";
        Cursor x = db.rawQuery( que,null);
        if (x.moveToNext()){
            origin = x.getString(x.getColumnIndex(ratesDB.rate_source_id));
        }
        return origin;
    }

    public String getDestination(String boxtype){
        String origin = "";
        SQLiteDatabase db = ratesDB.getReadableDatabase();
        String que = " SELECT * FROM "+ratesDB.tbname_rates
                +" WHERE "+ratesDB.rate_boxtype+" = '"+boxtype+"'";
        Cursor x = db.rawQuery( que,null);
        if (x.moveToNext()){
            origin = x.getString(x.getColumnIndex(ratesDB.rate_destination_id));
        }
        return origin;
    }

    public String getBoxtypeID(String boxtype){
        String origin = "";
        SQLiteDatabase db = gendata.getReadableDatabase();
        String que = " SELECT * FROM "+gendata.tbname_boxes
                +" WHERE "+gendata.box_name+" LIKE '%"+boxtype+"%'";
        Cursor x = db.rawQuery( que,null);
        if (x.moveToNext()){
            origin = x.getString(x.getColumnIndex(gendata.box_id));
        }
        return origin;
    }

    public String getWarehouseId(String name){
        String id = "";
        SQLiteDatabase db = ratesDB.getReadableDatabase();
        Cursor cx = db.rawQuery(" SELECT "+ratesDB.ware_id+" FROM "+ ratesDB.tbname_warehouse
                +" WHERE "+ratesDB.ware_name+" = '"+name+"'", null);
        if (cx.moveToNext()){
            id = cx.getString(cx.getColumnIndex(ratesDB.ware_id));
        }
        return id;
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
                                    + " WHERE " + gendata.res_btype_id + " = '" + boxid + "' AND "
                                    + gendata.reserve_reservation_no + " = '" + reservation_no + "' AND "
                                    + gendata.res_btype_bnum_boxtype + " = '" + boxtype + "' AND "
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
            } else {
                Log.e("Error", "Couldn't get data from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String r = "Couldn't get data from server, trying again....";
                        customToast(r);
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    public void getBranchDistribution(){
        //START BRANCH DISTRIBUTION THREAD
        try {
            String resp = null;
            String link = helper.getUrl();
            String urlget = "http://"+link+"/api/distribution/get_branch.php";
            URL url = new URL(urlget);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            // read the response
            InputStream in = new BufferedInputStream(conn.getInputStream());
            resp = convertStreamToString(in);

            if (resp != null) {
                Log.e("DistributionsBranch", " : " + resp);
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
                    if (checkDist(id)){
                        gendata.updDist(id, distribution_type, destination_name,truck_number,
                                remarks, "1", "2", createddate, createdby);
                        Log.e("distribution", id);

                        for (int ix = 0; ix < boxtypeid.length; ix++){
                            gendata.updDistBoxDist( id, boxtypeid[ix],"", box_number[ix], "2");
                            if (checkBarcodeBnum(box_number[ix])){
                                updateBarcodeInv(box_number[ix]);
                            }else if(checkYourInv(box_number[ix])){
                                updateBxInv(box_number[ix]);
                            }
                        }

                    }else{
                        gendata.addDistribution(id, distribution_type, destination_name,truck_number,
                                remarks, "1", "2", createddate, createdby, 1, null);
                        Log.e("distribution", id);
                        for (int ix = 0; ix < boxtypeid.length; ix++){
                            gendata.addTempBoxDist( id, boxtypeid[ix],"", box_number[ix], "2");
                            if (checkBarcodeBnum(box_number[ix])){
                                updateBarcodeInv(box_number[ix]);
                            }else if(checkYourInv(box_number[ix])){
                                updateBxInv(box_number[ix]);
                            }
                        }
                    }

                }
            } else {
                Log.e("Error", "Couldn't get data from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Couldn't get data from server.",
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //END THREAD FOR branch
    }

    public void getHubDistributions(){
        try {
            String resp = null;
            String link = helper.getUrl();
            String urlget = "http://"+link+"/api/distribution/get.php";
            URL url = new URL(urlget);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            // read the response
            InputStream in = new BufferedInputStream(conn.getInputStream());
            resp = convertStreamToString(in);

            if (resp != null) {
                Log.e("DistributionsHub", ":" + resp);
                JSONArray jsonArray = new JSONArray(resp);
                for(int i=0; i<jsonArray.length(); i++){
                    JSONObject json_data = jsonArray.getJSONObject(i);

                    String id = json_data.getString("id");
                    String distribution_type = json_data.getString("distribution_type");
                    String destination_name = json_data.getString("destination_name");
                    String mode = json_data.getString("mode_of_shipment");
                    String driver_name = json_data.getString("driver_name");
                    String truck_number = json_data.getString("truck_number");
                    String remarks = json_data.getString("remarks");
                    String createddate = json_data.getString("createddate");
                    String eta = json_data.getString("eta");
                    String etd = json_data.getString("etd");
                    String createdby = json_data.getString("createdby");
                    String[] box_number = json_data.getString("box_number").split(",");
                    String[] boxtypeid = json_data.getString("boxtype_id").split(",");

                    if (checkDist(id)){
                        gendata.updDist(id,distribution_type, destination_name,truck_number,
                                remarks, "1", "2", createddate, createdby);

                        for (int ix = 0; ix < boxtypeid.length; ix++){
                            gendata.updDistBoxDist( id, boxtypeid[ix],"", box_number[ix], "2");
                        }

                    }else{
                        gendata.addDistribution(id,distribution_type, destination_name,truck_number,
                                remarks, "1", "2", createddate, createdby, 0, null);
                        ratesDB.addDistribution(id, distribution_type, mode, destination_name, driver_name, truck_number,
                                remarks, etd, eta,"1", "2", createddate,
                                createdby, "2", 1, null);
                        for (int ix = 0; ix < box_number.length; ix++) {
                            gendata.addTempBoxDist(id, boxtypeid[ix], "", box_number[ix], "2");
                            ratesDB.addPartDistributionBox(id,
                                    getBoxtypeFromXX(box_number[ix]),
                                    box_number[ix], "1");
                            deleteBoxnumberInventory(box_number[ix]);
                            Log.e("boxnumber",box_number[ix]);
                        }
                    }
                }

            } else {
                Log.e("Error", "Couldn't get data from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Couldn't get data from server.",
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getAreaDistributions(){
        try {
            String resp = null;
            String link = helper.getUrl();
            String urlget = "http://"+link+"/api/distribution/getbyarea.php";
            URL url = new URL(urlget);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            // read the response
            InputStream in = new BufferedInputStream(conn.getInputStream());
            resp = convertStreamToString(in);

            if (resp != null) {
                Log.e("DistributionsArea", ":" + resp);
                JSONArray jsonArray = new JSONArray(resp);
                for(int i=0; i<jsonArray.length(); i++){
                    JSONObject json_data = jsonArray.getJSONObject(i);

                    String id = json_data.getString("id");
                    String distribution_type = json_data.getString("distribution_type");
                    String destination_name = json_data.getString("destination_name");
                    String mode = json_data.getString("mode_of_shipment");
                    String driver_name = json_data.getString("driver_name");
                    String truck_number = json_data.getString("truck_number");
                    String remarks = json_data.getString("remarks");
                    String createddate = json_data.getString("createddate");
                    String eta = json_data.getString("eta");
                    String etd = json_data.getString("etd");
                    String createdby = json_data.getString("createdby");
                    String[] box_number = json_data.getString("box_number").split(",");
                    String[] boxtypeid = json_data.getString("boxtype_id").split(",");

                    if (checkDist(id)){
                        gendata.updDist(id,distribution_type, destination_name,truck_number,
                                remarks, "1", "2", createddate, createdby);

                        for (int ix = 0; ix < boxtypeid.length; ix++){
                            gendata.updDistBoxDist( id, boxtypeid[ix],"", box_number[ix], "2");
                        }

                    }else{
                        gendata.addDistribution(id,distribution_type, destination_name,truck_number,
                                remarks, "1", "2", createddate, createdby, 0, null);
                        ratesDB.addDistribution(id, distribution_type, mode, destination_name, driver_name, truck_number,
                                remarks, etd, eta,"1", "2", createddate,
                                createdby, "2", 1, null);
                        for (int ix = 0; ix < box_number.length; ix++) {
                            gendata.addTempBoxDist(id, boxtypeid[ix], "", box_number[ix], "2");
                            ratesDB.addPartDistributionBox(id,
                                    getBoxtypeFromXX(box_number[ix]),
                                    box_number[ix], "1");
                            deleteBoxnumberInventory(box_number[ix]);
                            Log.e("boxnumber",box_number[ix]);
                        }
                    }
                }

            } else {
                Log.e("Error", "Couldn't get data from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Couldn't get data from server.",
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getUndeliveredBoxes(){
        try {
            String resp = null;
            String link = helper.getUrl();
            String urlget = "http://"+link+"/api/delivery/getundelivered.php";
            URL url = new URL(urlget);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            // read the response
            InputStream in = new BufferedInputStream(conn.getInputStream());
            resp = convertStreamToString(in);

            if (resp != null) {
                Log.e("Undelivered", ":" + resp);
                JSONArray jsonArray = new JSONArray(resp);
                for(int i=0; i<jsonArray.length(); i++){

                    JSONObject json_data = jsonArray.getJSONObject(i);

                    String[] box_number = json_data.getString("box_number").split(",");
                    for (int ib = 0; ib < box_number.length; ib++){
                        gendata.addUndelivered(box_number[ib]);
                    }

                }

            } else {
                Log.e("Error", "Couldn't get data from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Couldn't get data from server.",
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getBoxesFromDistributionChecker(String x){
        SQLiteDatabase db = gendata.getReadableDatabase();
        Cursor c = db.rawQuery(" SELECT gt.* FROM " + gendata.tbname_tempboxes+" gt "
                + " LEFT JOIN " + gendata.tbname_tempDist+" gtt ON gtt."+gendata.temp_transactionnumber
                +" = gt."+gendata.dboxtemp_distributionid
                + " WHERE gtt."+ gendata.temp_typename + " = '" + x + "'", null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            String topitem = c.getString(c.getColumnIndex(gendata.dboxtemp_boxid));
            String bnum = c.getString(c.getColumnIndex(gendata.dboxtemp_boxnumber));
            if (!checkAcceptance(bnum)) {
                gendata.addtoCheckerInv(topitem, bnum, "1", "0");
            }else{
                gendata.addtoCheckerInv(topitem, bnum, "1", "1");
            }
            Log.e("boxtoinv", "type:" + topitem + ", boxnum: " + bnum);
            c.moveToNext();
        }
        c.close();
        db.close();
    }

    public String readBranch(){
        String branchname = "";
        SQLiteDatabase db = ratesDB.getReadableDatabase();
        Cursor x = db.rawQuery(" SELECT * FROM "+ratesDB.tbname_branch
                +" WHERE "+ratesDB.branch_id+" = '"+helper.getBranch(""+helper.logcount())+"'", null);
        if (x.moveToNext()){
            branchname = x.getString(x.getColumnIndex(ratesDB.branch_name));
        }
        return branchname;
    }

    // convert from bitmap to byte array
    public byte[] getBytesFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
        return stream.toByteArray();
    }

    //upload updates
    //update distribution upload status
    public void updateDist(ArrayList<String> ids){
        if (ids.size() != 0) {
            for (String id : ids) {
                SQLiteDatabase db = gendata.getWritableDatabase();
                ContentValues cv = new ContentValues();
                cv.put(gendata.temp_uploadstat, "2");
                db.update(gendata.tbname_tempDist, cv,
                        gendata.temp_transactionnumber + " = '" + id + "' AND " +
                                gendata.temp_uploadstat + " = '1'", null);
                Log.e("upload", "uploaded dist");
            }
            db.close();
        }
    }

    //update reservations upload status
    public void updateReserveStat(ArrayList<String> trans){
        if (trans.size() != 0) {
            for (String tr : trans) {
                SQLiteDatabase db = gendata.getWritableDatabase();
                ContentValues cv = new ContentValues();
                cv.put(gendata.reserve_upload_status, "2");
                db.update(gendata.tbname_reservation, cv,
                        gendata.reserve_reservation_no + " = '" + tr + "' AND " +
                                gendata.reserve_upload_status + " = '1'", null);
                Log.e("upload", "uploaded reservations");
            }
            db.close();
        }
    }

    //update all customer upload status to 2 after sync
    public void updateAllCustomers(String stat){
        SQLiteDatabase db = gendata.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(gendata.cust_uploadstat, stat);
        db.update(gendata.tbname_customers, cv,
                gendata.cust_uploadstat+" = '1'", null);
        Log.e("upload", "uploaded customers");
        db.close();
    }

    //update incident upload status
    public void updateIncStat(ArrayList<String> id){
        if (id.size() != 0) {
            SQLiteDatabase db = gendata.getWritableDatabase();
            for (String ids : id) {
                ContentValues cv = new ContentValues();
                cv.put(gendata.inc_upds, "2");
                db.update(gendata.tbname_incident, cv,
                        gendata.inc_id + " = '" + ids + "' AND " +
                                gendata.inc_upds + " = '1'", null);
                Log.e("upload", "uploaded incidents");
            }
            db.close();
        }
    }

    //update loading uploads
    public void updateLoadsStat(ArrayList<String> id){
        if (id.size() != 0) {
            SQLiteDatabase db = ratesDB.getWritableDatabase();
            for (String ids : id) {
                ContentValues cv = new ContentValues();
                cv.put(ratesDB.load_upds, "2");
                db.update(ratesDB.tb_loading, cv,
                        ratesDB.load_id + " = '" + ids + "' AND " +
                                ratesDB.load_upds + " = '1'", null);
                Log.e("upload", "uploaded loading");
            }
            db.close();
        }
    }

    //update unloading uploads
    public void updateunLoadsStat(ArrayList<String> id){
        if (id.size() != 0) {
            SQLiteDatabase db = gendata.getWritableDatabase();
            for (String ids : id) {
                ContentValues cv = new ContentValues();
                cv.put(gendata.unload_upds, "2");
                db.update(gendata.tb_unload, cv, gendata.unload_id + " = '" + ids + "' AND " +
                        gendata.unload_upds + " = '1'", null);
                Log.e("upload", "uploaded unloading");
            }
            db.close();
        }
    }

    //update warehouse uploads
    public void updateWarehouseInv(String stat){
        SQLiteDatabase db = gendata.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(gendata.acc_status, stat);
        db.update(gendata.tb_acceptance, cv,
                gendata.acc_status+" = '1'", null);
        Log.e("upload", "uploaded unloading");
        db.close();
    }

    //update booking upload status
    public void updateBookingStat(ArrayList<String> trans){
        if (trans.size() != 0) {
            SQLiteDatabase db = gendata.getWritableDatabase();
            for (String tr : trans) {
                ContentValues cv = new ContentValues();
                cv.put(gendata.book_upds, "2");
                db.update(gendata.tbname_booking, cv,
                        gendata.book_transaction_no + " = '" + tr + "' AND " +
                                gendata.book_upds + " = '1'", null);
                Log.e("upload", "uploaded booking");
            }
            db.close();
        }
    }

    public boolean checkIfInInventory(String bn){
        SQLiteDatabase db = gendata.getReadableDatabase();
        String q = " SELECT * FROM "+gendata.tbname_driver_inventory
                +" WHERE "+gendata.sdinv_boxnumber+" = '"+bn+"'";
        Cursor x = db.rawQuery( q, null);
        if (x.getCount() != 0 ){
            return true;
        }else{
            return false;
        }
    }

    public void updateBoxType(String id, String bname,String depo, String l, String w, String h,String nsb,
                              String desc, String cr, String rs){
        SQLiteDatabase db = gendata.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(gendata.box_name, bname);
        cv.put(gendata.box_depositprice, depo);
        cv.put(gendata.box_length, l);
        cv.put(gendata.box_width, w);
        cv.put(gendata.box_height, h);
        cv.put(gendata.box_nsb, nsb);
        cv.put(gendata.box_description, desc);
        cv.put(gendata.box_createdby, cr);
        cv.put(gendata.box_recordstatus, rs);
        db.update(gendata.tbname_boxes, cv,gendata.box_id+" = '"+id+"'", null);
        db.close();
    }

    public void updateBoxTypeInRatesTable(String id, String bname,String depo, String l, String w,
                                          String h,String nsb,String desc, String cr, String rs){
        SQLiteDatabase db = ratesDB.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(ratesDB.box_name, bname);
        cv.put(ratesDB.box_depositprice, depo);
        cv.put(ratesDB.box_length, l);
        cv.put(ratesDB.box_width, w);
        cv.put(ratesDB.box_height, h);
        cv.put(ratesDB.box_nsb, nsb);
        cv.put(ratesDB.box_description, desc);
        cv.put(ratesDB.box_createdby, cr);
        cv.put(ratesDB.box_recordstatus, rs);
        db.update(ratesDB.tbname_boxes, cv,ratesDB.box_id+" = '"+id+"'", null);
        db.close();
    }

    //check boxnumber consignee booking
    public boolean checkBoxBooking(String bn){
        boolean ok = false;
        SQLiteDatabase db = gendata.getReadableDatabase();
        String x = "SELECT * FROM "+gendata.tbname_booking_consignee_box
                +" WHERE "+gendata.book_con_box_number+" = '"+bn+"' AND "
                +gendata.book_con_stat+" = '2'";
        Cursor c = db.rawQuery(x,null);
        if (c.getCount() != 0){
            ok = true;
        }else{
            ok = false;
        }
        return ok;
    }

    public void allUpdates(){
        updateWarehouseInv("2");
        updateAllCustomers("2");
        updateDist(distids);
        updateBookingStat(booknums);
        updateIncStat(incids);
        updateReserveStat(toupdid);
        updateLoadsStat(loadids);
        updateunLoadsStat(unloadids);
        distids.clear();
        booknums.clear();
        incids.clear();
        toupdid.clear();
        loadids.clear();
        unloadids.clear();
        //updateDistpart("2");
    }

    public boolean checkDist(String id){
        SQLiteDatabase db = gendata.getReadableDatabase();
        Cursor c = db.rawQuery(" SELECT * FROM "+gendata.tbname_tempDist
                +" WHERE "+gendata.temp_transactionnumber+" = '"+id+"'", null);
        if (c.getCount() != 0){
            return true;
        }else{
            return false;
        }
    }

    public boolean checkAcceptance(String id){
        boolean ok = false;
        SQLiteDatabase db = gendata.getReadableDatabase();
        Cursor c = db.rawQuery(" SELECT * FROM "+gendata.tbname_check_acceptance
        +" WHERE "+gendata.accept_transactionid+" = '"+id+"'", null);
        if (c.getCount() != 0){
            ok = true;
        }else{
            ok = false;
        }
        db.close();
        return ok;

    }

    public boolean checkBarcodeBnum(String bn){
        SQLiteDatabase db = ratesDB.getReadableDatabase();
        String x = " SELECT * FROM "+ratesDB.tbname_barcode_inventory
                +" WHERE "+ratesDB.barcodeinv_boxnumber+" = '"+bn+"'";
        Cursor c = db.rawQuery(x, null);
        if (c.getCount() != 0 ){
            return true;
        }else{
            return false;
        }
    }

    public boolean checkBarcodeDistDriver(String trans){
        SQLiteDatabase db = ratesDB.getReadableDatabase();
        String x = " SELECT * FROM "+ratesDB.tbname_barcode_dist
                +" WHERE "+ratesDB.bardist_trans+" = '"+trans+"'";
        Cursor c = db.rawQuery(x, null);
        if (c.getCount() != 0 ){
            return true;
        }else{
            return false;
        }
    }

    public boolean checkYourInv(String bn){
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

    public boolean checkBookingExist(String id){
        boolean ok = false;
        SQLiteDatabase db = gendata.getReadableDatabase();
        Cursor c = db.rawQuery(" SELECT * FROM "+gendata.tbname_booking
        +" WHERE "+gendata.book_transaction_no+" = '"+id+"'", null);
        if (c.getCount() != 0){
            ok = true;
        }else{
            ok = false;
        }
        db.close();
        return ok;
    }

    public boolean checkNSBrate(String boxid, String source, String des, String rate){
        boolean ok = false;
        SQLiteDatabase db = gendata.getReadableDatabase();
        Cursor c = db.rawQuery(" SELECT * FROM "+gendata.tbname_nsbrate
        +" WHERE "+gendata.nsbr_boxid+" = '"+boxid+"' AND "+gendata.nsbr_sourceid+" = '"+source+"'"
                +" AND "+gendata.nsbr_destid+" = '"+des+"' AND "+gendata.nsbr_rate+" = '"+rate+"'", null);
        if (c.getCount() != 0){
            ok = true;
        }else{
            ok = false;
        }
        db.close();
        return ok;
    }

    public void updateBarcodeInv(String bn){
        SQLiteDatabase db = ratesDB.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(ratesDB.barcodeinv_status, "1");
        db.update(ratesDB.tbname_barcode_inventory, cv,
                ratesDB.barcodeinv_boxnumber+" = '"+bn+"'", null);
        Log.e("update_barcode_inv",bn);
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

    public void addItemsExpenseTodb(String name){
        SQLiteDatabase db = ratesDB.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(ratesDB.expit_name, name);
        cv.put(ratesDB.expit_type, "0");
        db.insert(ratesDB.tbname_exp_item, null, cv);
        db.close();
    }

    public void updateDistBarcode(String trans){
        SQLiteDatabase db = ratesDB.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(ratesDB.bardist_accptstat, "1");
        db.update(ratesDB.tbname_barcode_dist,cv, ratesDB.bardist_trans+" = '"+trans+"'", null);
        Log.e("upd_dist", trans);
        db.close();
    }

    public void updateBnCheckInv(String bn){
        SQLiteDatabase db = gendata.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(gendata.acc_box_stat, "10");
        db.update(gendata.tbname_accept_boxes, cv,
                gendata.acc_box_boxnumber+" = '"+bn+"'", null);
        Log.e("update", bn);
        db.close();
    }

    public String getBoxtypeFromXX(String barcode){
        String name = null;
        SQLiteDatabase db = gendata.getReadableDatabase();
        String query = "SELECT * FROM "+gendata.tbname_booking_consignee_box+" LEFT JOIN "
                +gendata.tbname_boxes
                +" ON "+gendata.tbname_boxes+"."+gendata.box_name+" = "
                +gendata.tbname_booking_consignee_box+"."+gendata.book_con_boxtype
                +" WHERE "+gendata.book_con_box_number+" = '"+barcode+"'";
        Cursor c = db.rawQuery(query, null);
        if (c.getCount() != 0){
            c.moveToNext();
            name = c.getString(c.getColumnIndex(gendata.book_con_box_id));
            Log.e("boxname", name);
        }
        return name;
    }

    public void deleteBoxnumberInventory(String bn){
        SQLiteDatabase db = gendata.getWritableDatabase();
        db.delete(gendata.tbname_partner_inventory,
                gendata.partinv_boxnumber + " = '"+bn+"'", null);
        Log.e("part_inv_delte", bn);
        db.close();
    }

    public boolean checkInDistributionPartner(String bn){
        SQLiteDatabase db = ratesDB.getReadableDatabase();
        Cursor c = db.rawQuery(" SELECT * FROM "+ratesDB.tbname_part_distribution_box
                +" WHERE "+ratesDB.partdist_box_boxnumber+" = '"+bn+"'", null);
        if (c.getCount() != 0){
            deleteBoxnumberInventory(bn);
            return true;
        }else{
            return false;
        }

    }

    public void updatePartnerInv(String bn){
        SQLiteDatabase db = gendata.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(gendata.partinv_stat, "10");
        db.update(gendata.tbname_partner_inventory,cv,
                gendata.partinv_boxnumber + " = '"+bn+"'", null);
        Log.e("part_inv_updte", bn);
        db.close();
    }

}
