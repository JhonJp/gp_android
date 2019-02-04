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
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
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
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class Remittancelist extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    HomeDatabase helper;
    GenDatabase gen;
    SQLiteDatabase db;
    GridView grimg;
    String value;
    NavigationView navigationView;
    ListView lvi;
    TextView hint;
    LinearList adapter;
    AutoCompleteTextView search;
    TextView idget;
    ProgressDialog progressBar;
    ArrayList<String> ids;
    ArrayList<String> expids;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remittancelist);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        helper = new HomeDatabase(this);
        gen = new GenDatabase(this);

        lvi = (ListView)findViewById(R.id.lvinclist);
        search = (AutoCompleteTextView)findViewById(R.id.searchableinput);
        search.setSelected(false);
        ids = new ArrayList<>();
        expids = new ArrayList<>();

        if (helper.logcount() != 0){
            value = helper.getRole(helper.logcount());
        }
        addList();
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
               addList();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.e("text watch","after change");
            }
        });

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        sidenavMain();
        subMenu();
        setNameMail();

    }

    public void sidenavMain(){
        ListView lv=(ListView)findViewById(R.id.optionsList);//initialization of listview
        final ArrayList<HomeList> listitem = helper.getData(value);
        NavAdapter ad = new NavAdapter(Remittancelist.this, listitem);
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
                        final AlertDialog.Builder builder = new AlertDialog.Builder(Remittancelist.this);
                        builder.setMessage("Confirm logout?")
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        helper.logout();
                                        startActivity(new Intent(Remittancelist.this, Login.class));
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
                bundle.putString("module", "Remittance");
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

    public void addList(){
        try {
            String ty = null;
            if (value.equals("OIC")) {
                ty = "BANK";
            } else {
                ty = "OIC";
            }
            final ArrayList<LinearItem> result = gen.getAllRemittance(ty);
            adapter = new LinearList(getApplicationContext(), result);
            lvi.setAdapter(adapter);
            //delete remittance
        lvi.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                idget = (TextView)view.findViewById(R.id.dataid);
                final AlertDialog.Builder builder = new AlertDialog.Builder(Remittancelist.this);
                String delete = "Delete";
                builder.setTitle("Delete this data.")
                        .setMessage(Html.fromHtml("<b>note:</b><i>Once data is" +
                                " deleted you can not retrieve it.</i>"))
                        .setPositiveButton(delete.toUpperCase(), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                deleteRemittance(idget.getText().toString());
                                addList();
                            }
                        });
                // Create an alert
                builder.create().show();
                return true;
            }
        });
            lvi.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    TextView textname = (TextView) view.findViewById(R.id.c_account);
                    idget = (TextView) view.findViewById(R.id.dataid);

                    Log.e("mtop", idget.getText().toString());
                    final Dialog dialog = new Dialog(Remittancelist.this);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setCancelable(false);
                    dialog.setContentView(R.layout.remitinflate);
                    TextView type = (TextView) dialog.findViewById(R.id.remtype);
                    TextView reason = (TextView) dialog.findViewById(R.id.remamount);
                    TextView n = (TextView) dialog.findViewById(R.id.remnameinput);

                    type.setText(Html.fromHtml("" + getType(idget.getText().toString()) + ""));
                    reason.setText(Html.fromHtml("" + getAmount(idget.getText().toString()) + "</u>"));
                    n.setText(Html.fromHtml("" + textname.getText().toString() + ""));

                    Button close = (Button) dialog.findViewById(R.id.close);
                    close.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });

                    dialog.show();
                }
            });
        }catch (Exception e){}
    }

    public void deleteRemittance(String id){
        SQLiteDatabase db = gen.getWritableDatabase();
        db.delete(gen.tbname_remittance,
                gen.remit_id + " = '" + id + "'", null);
        db.close();

    }

    public String getType(String id){
        String tr = null;
        SQLiteDatabase db = gen.getReadableDatabase();
        Cursor c = db.rawQuery(" SELECT * FROM "+gen.tbname_remittance
                +" WHERE "+gen.remit_id+" = '"+id+"'", null);
        if (c.moveToNext()){
            tr = c.getString(c.getColumnIndex(gen.remit_type));
        }
        return tr;
    }

    public String getAmount(String id){
        String tr = null;
        SQLiteDatabase db = gen.getReadableDatabase();
        Cursor c = db.rawQuery(" SELECT * FROM "+gen.tbname_remittance
                +" WHERE "+gen.remit_id+" = '"+id+"'", null);
        if (c.moveToNext()){
            tr = c.getString(c.getColumnIndex(gen.remit_amount));
        }
        return tr;
    }

    @Override
    public void onBackPressed(){
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
            super.onBackPressed();
            if (value.equals("OIC")){
                startActivity(new Intent(this, Remittancetooic.class));
                finish();
            }else if (value.equals("Sales Driver")){
                startActivity(new Intent(this, Remittancetooic.class));
                finish();
            }
        } else {
            if (value.equals("OIC")){
                startActivity(new Intent(this, Remittancetooic.class));
                finish();
            }else if (value.equals("Sales Driver")){
                startActivity(new Intent(this, Remittancetooic.class));
                finish();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.incidentlist, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        if (id == R.id.syncincident){
            network();
        }
        return super.onOptionsItemSelected(item);
    }

    public void network(){
        if (isNetworkAvailable()== true){
            loadingPost(getWindow().getDecorView().getRootView());
            //Toast.makeText(getApplicationContext(),"Connected to the internet.", Toast.LENGTH_LONG).show();
        }else
        {
            Toast.makeText(getApplicationContext(),"Please check internet connection.", Toast.LENGTH_LONG).show();
        }
    }

    public void threadRemittanceDriver(){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                db = gen.getReadableDatabase();
                String query = " SELECT * FROM "+gen.tbname_remittance+" WHERE "+gen.remit_createdby
                        +" = '"+helper.logcount()+"'";
                Cursor cx = db.rawQuery(query, null);
                if (cx.getCount() != 0) {
                    //THREAD FOR incident API
                    try {
                        String link = helper.getUrl();
                        URL url = new URL("http://" + link + "/api/remittance/salesdriver_save.php");
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setRequestMethod("POST");
                        conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                        conn.setRequestProperty("Accept", "application/json");
                        conn.setDoOutput(true);
                        conn.setDoInput(true);

                        JSONObject jsonParam = new JSONObject();
                        jsonParam.accumulate("data", getRemittance("OIC"));

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
                                    final AlertDialog.Builder builder
                                            = new AlertDialog.Builder(Remittancelist.this);
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
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }else{
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.dismiss();
                            final AlertDialog.Builder builder
                                    = new AlertDialog.Builder(Remittancelist.this);
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
                //END THREAD remittance API
            }
        });
        thread.start();
    }

    public void sendRemittanceOIC(){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                db = gen.getReadableDatabase();
                String query = " SELECT * FROM "+gen.tbname_remittance+" WHERE "+gen.remit_createdby
                        +" = '"+helper.logcount()+"' AND "+gen.remit_upds+" = '1'";
                Cursor cx = db.rawQuery(query, null);
                if (cx.getCount() != 0) {
                    //THREAD FOR incident API
                    try {
                        String link = helper.getUrl();
                        URL url = new URL("http://" + link + "/api/remittance/save.php");
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setRequestMethod("POST");
                        conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                        conn.setRequestProperty("Accept", "application/json");
                        conn.setDoOutput(true);
                        conn.setDoInput(true);

                        JSONObject jsonParam = new JSONObject();
                        jsonParam.accumulate("data", getRemittance("BANK"));

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
                                    final AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
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
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }else{
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.dismiss();
                            final AlertDialog.Builder builder
                                    = new AlertDialog.Builder(Remittancelist.this);
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
                //END THREAD remittance API
            }
        });
        thread.start();
    }

    public void sendExpenses(){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                db = gen.getReadableDatabase();
                String query = "SELECT * FROM " + gen.tbname_remittance_trans+ " WHERE "
                        +gen.rem_trans_type+" = 'expense' AND "
                        +gen.rem_trans_stat+" = '2'";
                Cursor cx = db.rawQuery(query, null);
                if (cx.getCount() != 0) {
                    //THREAD FOR incident API
                    try {
                        String link = helper.getUrl();
                        URL url = new URL("http://" + link + "/api/remittance/expense.php");
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setRequestMethod("POST");
                        conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                        conn.setRequestProperty("Accept", "application/json");
                        conn.setDoOutput(true);
                        conn.setDoInput(true);

                        JSONObject jsonParam = new JSONObject();
                        jsonParam.accumulate("data", getExpenses());

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
                                    final AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
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
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }else{
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.dismiss();
                            final AlertDialog.Builder builder
                                    = new AlertDialog.Builder(Remittancelist.this);
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
                //END THREAD remittance API
            }
        });
        thread.start();
    }

    public JSONArray getRemittance(String type) {
        SQLiteDatabase myDataBase = gen.getReadableDatabase();
        String raw = " SELECT * FROM " + gen.tbname_remittance+" WHERE "+gen.remit_type+" = '"+type+"'" +
                " AND "+gen.remit_upds+" = '1'";
        Cursor cursor = myDataBase.rawQuery(raw, null);
        JSONArray resultSet = new JSONArray();
        cursor.moveToFirst();
        try {
            if (type.equals("OIC")){
                while (!cursor.isAfterLast()) {
                        JSONObject json = new JSONObject();
                        String id = cursor.getString(cursor.getColumnIndex(gen.remit_id));
                        String ty = cursor.getString(cursor.getColumnIndex(gen.remit_type));
                        String name = cursor.getString(cursor.getColumnIndex(gen.remit_name));
                        String accname = cursor.getString(cursor.getColumnIndex(gen.remit_accountname));
                        String accnum = cursor.getString(cursor.getColumnIndex(gen.remit_accountnumber));
                        String am = cursor.getString(cursor.getColumnIndex(gen.remit_amount));
                        String sd = cursor.getString(cursor.getColumnIndex(gen.remit_createdby));
                        String d = cursor.getString(cursor.getColumnIndex(gen.remit_createddate));
                        String stat = cursor.getString(cursor.getColumnIndex(gen.remit_status));
                            json.put("id", id);
                            json.put("remittance_type", ty);
                            json.put("oic", name);
                            json.put("account_name", accname);
                            json.put("account_number", accnum);
                            json.put("amount", am);
                            json.put("salers_driver_id", sd);
                            json.put("createddate", d);
                            json.put("remittance_status", stat);
                        resultSet.put(json);
                        cursor.moveToNext();

                }
            }else {
                while (!cursor.isAfterLast()) {
                    JSONObject json = new JSONObject();
                    String id = cursor.getString(cursor.getColumnIndex(gen.remit_id));
                    String branch = helper.getBranch(helper.logcount()+"");
                    String chart = "3";
                    String bank = cursor.getString(cursor.getColumnIndex(gen.remit_name));
                    String by = helper.logcount()+"";
                    String amountdriver = cursor.getString(cursor.getColumnIndex(gen.remit_amount));
                    String amountoic = cursor.getString(cursor.getColumnIndex(gen.remit_amount));
                    String verby = cursor.getString(cursor.getColumnIndex(gen.remit_createdby));
                    String desc = "Remittance of OIC";
                    String docs = "";
                    String stat = cursor.getString(cursor.getColumnIndex(gen.remit_status));
                    String crdate = cursor.getString(cursor.getColumnIndex(gen.remit_createddate));
                    String crby = cursor.getString(cursor.getColumnIndex(gen.remit_createdby));
                    json.put("id", "");
                    json.put("branch_source", branch);
                    json.put("chart_accounts", chart);
                    json.put("bank", bank);
                    json.put("remitted_by", by);
                    json.put("remitted_amount_sales_driver", amountdriver);
                    json.put("remitted_amount_oic", amountoic);
                    json.put("verified_by", verby);
                    json.put("description", desc);
                    json.put("documents", docs);
                    json.put("status", "Ok");
                    json.put("createddate", crdate);
                    json.put("createdby", crby);
                    json.put("recordstat","1");
                    ids.add(id);
                    resultSet.put(json);
                    cursor.moveToNext();
                }
            }
        }catch (Exception e){}
        cursor.close();
        //Log.e("result set", resultSet.toString());
        return resultSet;
    }

    public JSONArray getExpenses(){
        SQLiteDatabase myDataBase = gen.getReadableDatabase();
        String raw = "SELECT * FROM " + gen.tbname_remittance_trans+ " WHERE "
                +gen.rem_trans_type+" = 'expense' AND "
                +gen.rem_trans_stat+" = '2'";
        Cursor c = myDataBase.rawQuery(raw, null);
        JSONArray resultSet = new JSONArray();
        c.moveToFirst();
        try {
            while (!c.isAfterLast()) {
                JSONObject js = new JSONObject();
                String ids = c.getString(c.getColumnIndex(gen.rem_trans_id));
                String am = c.getString(c.getColumnIndex(gen.rem_trans_amount));
                String desc = c.getString(c.getColumnIndex(gen.rem_trans_itemname));

                js.put("id", "");
                js.put("employee_id", helper.logcount());
                js.put("amount", am);
                js.put("chart_accounts", "3");
                js.put("description", desc);
                js.put("status", "Approved");
                js.put("due_date", datereturn());
                js.put("approved_by", helper.logcount());
                js.put("approved_date", datereturn());
                js.put("documents", "");
                expids.add(ids);
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

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
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
                    if (value.equals("OIC")){
                        sendPost();
                    }else{
                        threadRemittanceDriver();
                    }
                    Thread.sleep(10000);
                }
                catch (Exception e) { } // Just catch the InterruptedException

                handler.post(new Runnable() {
                    public void run() {
                        if (ids.size() == 0){
                            String g = "You do not have enough data to upload.";
                            customToast(g);
                        }else {
                            progressBar.dismiss();
                            final AlertDialog.Builder builder
                                    = new AlertDialog.Builder(Remittancelist.this);
                            builder.setTitle("Information confirmation")
                                    .setMessage("Data upload has been successful, thank you.")
                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            updateRemittance(ids);
                                            updateExpense(expids);
                                            expids.clear();
                                            ids.clear();
                                            dialog.dismiss();
                                            recreate();
                                        }
                                    });
                            // Create the AlertDialog object and show it
                            builder.create();
                            builder.setCancelable(false);
                            builder.show();
                        }
                    }
                });
            }
        }).start();
    }

    public String datereturn(){
        Date datetalaga = Calendar.getInstance().getTime();
        SimpleDateFormat writeDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        writeDate.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        String findate = writeDate.format(datetalaga);

        return findate;
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

    public void updateRemittance(ArrayList<String> id){
        SQLiteDatabase db = gen.getWritableDatabase();
        for (String tr : id) {
            ContentValues cv = new ContentValues();
            cv.put(gen.remit_upds, "2");
            db.update(gen.tbname_remittance, cv,
                    gen.remit_id+" = '"+tr+"' AND "+
                            gen.remit_upds + " = '1'", null);
            Log.e("remttance", "remittance update");
        }
        db.close();
    }

    public void updateExpense(ArrayList<String> id){
        SQLiteDatabase db = gen.getWritableDatabase();
        for (String tr : id) {
            ContentValues cv = new ContentValues();
            cv.put(gen.rem_trans_stat, "3");
            db.update(gen.tbname_remittance_trans, cv,
                    gen.rem_trans_id+" = '"+tr+"' AND "+
                            gen.rem_trans_stat + " = '2'", null);
            Log.e("remttance", "expense update");
        }
        db.close();
    }

    public void sendPost() {
        Thread thr = new Thread(new Runnable() {
            @Override
            public void run() {

                sendRemittanceOIC();

                sendExpenses();

            }
        });

        thr.start();
    }

}
