package com.example.admin.gpxbymodule;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
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
import java.util.ArrayList;

public class Booking extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    BottomNavigationView bottomNavigationView;
    HomeDatabase helper;
    GenDatabase generaldb;
    SQLiteDatabase db;
    String value;
    double additionalpay;
    String reserveno;
    String fullname, link;
    String accntno;
    double payamount;
    String transNo;
    RatesDB rate;

    public ArrayList<String> getBoxids() {
        return boxids;
    }

    public void setBoxids(ArrayList<String> boxids) {
        this.boxids = boxids;
    }

    ArrayList<String> boxids;

    public ArrayList<String> getBoxnumbers() {
        return boxnumbers;
    }

    public void setBoxnumbers(ArrayList<String> boxnumbers) {
        this.boxnumbers = boxnumbers;
    }

    ArrayList<String> boxnumbers;

    public ArrayList<String> getClicksids() {
        return clicksids;
    }

    public void setClicksids(ArrayList<String> clicksids) {
        this.clicksids = clicksids;
    }

    ArrayList<String> clicksids;

    public int getClickcount() {
        return clickcount;
    }

    public void setClickcount(int clickcount) {
        this.clickcount = clickcount;
    }

    int clickcount;

    public int getActualcount() {
        return actualcount;
    }

    public void setActualcount(int actualcount) {
        this.actualcount = actualcount;
    }

    int actualcount;
    ProgressDialog progressBar;
    NavigationView navigationView;
    Bundle bundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.booking);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        generaldb = new GenDatabase(getApplicationContext());

        //loading the default fragment
        loadFragment(new Booking_info());
        helper = new HomeDatabase(this);
        rate = new RatesDB(getApplicationContext());
        link = helper.getUrl();

        if (helper.logcount() != 0){
            value = helper.getRole(helper.logcount());
            Log.e("role ", value);
        }

        //get data from booking list
        bundle = getIntent().getExtras();

        //Extract the data…
        if ((bundle.getString("transno") != null) || (bundle.getString("fullname") != null)
                || (bundle.getString("reservenum") != null)){
            this.setTransNo(bundle.getString("transno"));
            this.setReserveno(bundle.getString("reservenum"));
            this.setFullname(bundle.getString("fullname"));
        }

        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        nav();


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
    }

    public void sidenavMain(){
        ListView lv=(ListView)findViewById(R.id.optionsList);//initialization of listview

        final ArrayList<HomeList> listitem = helper.getData(value);

        NavAdapter ad = new NavAdapter(Booking.this, listitem);
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
                        final AlertDialog.Builder builder = new AlertDialog.Builder(Booking.this);
                        builder.setMessage("Confirm logout?")
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        helper.logout();

                                        startActivity(new Intent(Booking.this, Login.class));
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
                else if(value.equals("Warehouse Checker")) {
                    startActivity(new Intent(this, Checker_Inventory.class));
                    finish();
                }else if(value.equals("Partner Portal")) {
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

    @Override
    public void onBackPressed(){
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
                alert();
            } else {
                alert();
            }
    }

    public void alert(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //Extract the data…
        if ((bundle.getString("transno") != null) || (bundle.getString("fullname") != null)
                || (bundle.getString("reservenum") != null)){
            this.setTransNo(bundle.getString("transno"));
            this.setReserveno(bundle.getString("reservenum"));
            this.setFullname(bundle.getString("fullname"));
            generaldb.deleteDiscountsOnDestroy(bundle.getString("transno"));
            builder.setMessage("Cancel transaction?");
        }else{
            builder.setMessage("Cancel booking?");
        }
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //generaldb.deleteDiscountsOnDestroy(getTransNo());
                deleteTransactions("1");
                startActivity(new Intent(getApplicationContext(), Bookinglist.class));
                finish();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        // Create the AlertDialog object and show it
        builder.create().show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        deleteTransactions("1");
    }

    private boolean loadFragment(Fragment fragment) {
        //switching fragment
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.bookingframe, fragment)
                    .commit();
            return true;
        }
        return false;
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
        getMenuInflater().inflate(R.menu.booking, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.btnnext){
            loadFragment(new Receiver());
            bottomNavigationView.setSelectedItemId(R.id.receiver);
        }
        else if (id == R.id.btnnextpay){
            loadFragment(new Booking_payment());
            bottomNavigationView.setSelectedItemId(R.id.box_info);
        }
        else if (id == R.id.loadprev){
            loadFragment(new Booking_info());
            bottomNavigationView.setSelectedItemId(R.id.booking_section_info);
        }
        else if (id == R.id.loadprevpay) {
            loadFragment(new Receiver());
            bottomNavigationView.setSelectedItemId(R.id.receiver);
        }
        else if (id ==  R.id.passincident){
            Intent i = new Intent(this, Incident.class);
            Bundle bundle = new Bundle();
            bundle.putString("module", "Booking");
            //Add the bundle to the intent
            i.putExtras(bundle);
            startActivity(i);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void nav(){
        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @TargetApi(Build.VERSION_CODES.M)
                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public boolean onNavigationItemSelected(@NonNull final MenuItem item) {
                        int id = item.getItemId();
                        switch (id) {
                            case R.id.booking_section_info:
                                item.setChecked(true);
                                loadFragment(new Booking_info());
                                break;
                            case R.id.receiver:
                                item.setChecked(true);
                                loadFragment(new Receiver());
                                break;
                            case R.id.box_info:
                                final AlertDialog.Builder builder = new AlertDialog.Builder(Booking.this);
                                builder.setTitle("View payment summary");
                                builder.setMessage(Html.fromHtml("<b>note:</b>"+
                                        "<i> Next window displays the summary for payment.</i>"))
                                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                item.setChecked(true);
                                                loadFragment(new Booking_payment());
                                            }
                                        });
                                // Create the AlertDialog object and show it
                                builder.create().show();
                                break;
                        }
                        return false;
                    }
                });
    }

    //setters and getters
    public String getReserveno() {
        return reserveno;
    }

    public void setReserveno(String reserveno) {
        this.reserveno = reserveno;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getAccntno() {
        return accntno;
    }

    public void setAccntno(String accntno) {
        this.accntno = accntno;
    }

    public String getTransNo() {
        return transNo;
    }

    public void setTransNo(String transNo) {
        this.transNo = transNo;
    }

    public void setNameMail(){
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

    public double getPayamount() {
        return payamount;
    }

    public void setPayamount(double payamount) {
        this.payamount = payamount;
    }

    public double getAdditionalpay() {
        return additionalpay;
    }

    public void setAdditionalpay(double additionalpay) {
        this.additionalpay = additionalpay;
    }

    public void deleteTransactions(String stat){
        SQLiteDatabase db = generaldb.getWritableDatabase();
        db.delete(generaldb.tbname_booking_consignee_box,
                generaldb.book_con_stat+" = '0'",null);
        Log.e("delete_book_con", stat);
        db.close();
    }

}
