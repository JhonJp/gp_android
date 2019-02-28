package com.example.admin.gpxbymodule;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
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
import android.widget.AutoCompleteTextView;
import android.widget.FrameLayout;
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
import java.util.List;

public class Reservelist extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    HomeDatabase helper;
    String value;
    GenDatabase gendata;
    ProgressDialog progressBar;
    BottomNavigationView botnav;
    NavigationView navigationView;
    String reservenumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reserve_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        helper = new HomeDatabase(getApplicationContext());
        gendata = new GenDatabase(getApplicationContext());

        botnav = (BottomNavigationView)findViewById(R.id.bottom_navigation);
        loadFragment(new ReservePending());
        nav();

        try {
            if (helper.logcount() != 0) {
                value = helper.getRole(helper.logcount());
                Log.e("role ", value);
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
                        final AlertDialog.Builder builder = new AlertDialog.Builder(Reservelist.this);
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
                bundle.putString("module", "Reservation");
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
                else if (value.equals("Partner Portal")){
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
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);

            startActivity(new Intent(getApplicationContext(), Home.class));
            finish();

        } else {
            startActivity(new Intent(getApplicationContext(), Home.class));
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.reservelist, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id ==  R.id.passincident){
            Intent i = new Intent(this, Incident.class);
            Bundle bundle = new Bundle();
            bundle.putString("module", "Reservation");
            //Add the bundle to the intent
            i.putExtras(bundle);
            startActivity(i);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void nav(){
        try{
            botnav.setOnNavigationItemSelectedListener(
                    new BottomNavigationView.OnNavigationItemSelectedListener() {
                        @TargetApi(Build.VERSION_CODES.M)
                        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                        @Override
                        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.pending:
                                    item.setChecked(true);
                                    loadFragment(new ReservePending());
                                    break;
                                case R.id.complete:
                                    item.setChecked(true);
                                    loadFragment(new ReserveComplete());
                                    break;
                                default:
                                    loadFragment(new ReservePending());
                            }
                            return false;
                        }
                    });
        }catch (Exception e){}
    }

    public boolean loadFragment(Fragment fragment) {
        //switching fragment
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.reservelistfrag, fragment)
                    .commit();

            return true;
        }
        return false;
    }


    public String getReservenumber() {
        return reservenumber;
    }

    public void setReservenumber(String reservenumber) {
        this.reservenumber = reservenumber;
    }

}
