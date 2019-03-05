package com.example.admin.gpxbymodule;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
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
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class Acceptancelist extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    NavigationView navigationView;
    GenDatabase gen;
    HomeDatabase helper;
    RatesDB rate;
    ProgressDialog progressBar;
    SQLiteDatabase db;
    String value, type;
    ListView lv;
    LinearList adapter;
    ArrayList<LinearItem> result;
    AutoCompleteTextView search;
    Runnable r;
    private int SETTINGS_ACTION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        preference();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acceptancelist);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        gen = new GenDatabase(this);
        rate = new RatesDB(this);
        helper = new HomeDatabase(this);
        search = (AutoCompleteTextView)findViewById(R.id.searchableinput);
        lv = (ListView)findViewById(R.id.lv);
        Bundle bundle = getIntent().getExtras();
        type = bundle.getString("type");
        try {

            if (helper.logcount() != 0) {
                value = helper.getRole(helper.logcount());
                Log.e("role ", value);
            }
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
        }catch(Exception e){}
        customtype();
        scroll();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        sidenavMain();
        subMenu();
        setNameMail();
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
                        final AlertDialog.Builder builder = new AlertDialog.Builder(Acceptancelist.this);
                        builder.setMessage("Confirm logout?")
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {

                                        helper.logout();

                                        startActivity(new Intent(Acceptancelist.this, Login.class));
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
                        startActivity(new Intent(Acceptancelist.this, Home.class));
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
                }else if(value.equals("Partner Portal")){
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
                    bundle.putString("module", "Acceptance");
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

    public void customtype(){
        Bundle bundle = getIntent().getExtras();
        type = bundle.getString("type");
        Log.e("type", type);
        if (type.equals("0")){
            result = getEmptyAcceptance();
            //result = getAllAcceptance();
            adapter = new LinearList(getApplicationContext(), result);
            lv.setAdapter(adapter);
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    extendViewing(view, "0");
                }
            });
            lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(Acceptancelist.this);
                    builder.setTitle("Information")
                            .setMessage(Html.fromHtml("<b>note : </b>" +
                                    "Are you sure you want to delete this data? " +
                                    "Once deleted, the data cannot be retrieved."))
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    delAccEmpty(result.get(position).getId());
                                    customtype();
                                    dialog.dismiss();
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    builder.create().show();
                    return true;
                }
            });
        }else if (type.equals("1")) {
            result = getAllAcceptance();
            adapter = new LinearList(getApplicationContext(), result);
            lv.setAdapter(adapter);
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    extendViewing(view, "1");
                }
            });
        }
    }

    public void extendViewing(View v, String t){
        String mtop = "";
        if (t.equals("0")){
            TextView manname = (TextView) v.findViewById(R.id.c_account);
            mtop = manname.getText().toString();
            ArrayList<ListItem> poparray;
            final Dialog dialog = new Dialog(Acceptancelist.this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(false);
            dialog.setContentView(R.layout.distdatalayout);
            TextView top = (TextView) dialog.findViewById(R.id.reservationnumber);
            TextView owner = (TextView) dialog.findViewById(R.id.owner);
            TextView whom = (TextView) dialog.findViewById(R.id.ownerinfo);
            TextView truck = (TextView) dialog.findViewById(R.id.truckinput);
            TextView trucktitle = (TextView) dialog.findViewById(R.id.trucknumtitle);
            ListView poplist = (ListView)dialog.findViewById(R.id.list);

            poparray = getAcceptanceInfoEmpty(mtop);

            TableAdapter tb = new TableAdapter(getApplicationContext(), poparray);
            poplist.setAdapter(tb);

            top.setText("Acceptance Information");
            trucktitle.setText("Box information");
            owner.setText("Manufacturer name");
            whom.setText(Html.fromHtml(""+mtop+""));
            truck.setVisibility(View.INVISIBLE);

            Button close = (Button)dialog.findViewById(R.id.close);
            close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            dialog.show();
        }else if (t.equals("1")){
            TextView textView = (TextView) v.findViewById(R.id.dataid);
            mtop = textView.getText().toString();
            ArrayList<ListItem> poparray;
            final Dialog dialog = new Dialog(Acceptancelist.this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(false);
            dialog.setContentView(R.layout.distdatalayout);
            TextView top = (TextView) dialog.findViewById(R.id.reservationnumber);
            TextView owner = (TextView) dialog.findViewById(R.id.owner);
            TextView whom = (TextView) dialog.findViewById(R.id.ownerinfo);
            TextView truck = (TextView) dialog.findViewById(R.id.truckinput);
            ListView poplist = (ListView)dialog.findViewById(R.id.list);

            poparray = getAcceptanceInfo(mtop);

            TableAdapter tb = new TableAdapter(getApplicationContext(), poparray);
            poplist.setAdapter(tb);

            top.setText("Acceptance Information");
            owner.setText("Transaction number");
            whom.setText(Html.fromHtml(""+mtop+""));
            truck.setText(getTruck(mtop));

            Button close = (Button)dialog.findViewById(R.id.close);
            close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            dialog.show();
        }
    }

    public ArrayList<ListItem> getAcceptanceInfo(String trans){
        ArrayList<ListItem> results = new ArrayList<>();
        SQLiteDatabase db = gen.getReadableDatabase();
        Cursor res = db.rawQuery(" SELECT * FROM " + gen.tbname_accept_boxes + " WHERE "
                + gen.acc_box_transactionno + " = '" + trans + "'", null);
        res.moveToFirst();
        int x = 1;
        while (!res.isAfterLast()) {
            String ids = res.getString(res.getColumnIndex(gen.acc_box_id));
            String top = res.getString(res.getColumnIndex(gen.acc_box_boxnumber));
            ListItem list = new ListItem(ids, x+"", top,"");
            results.add(list);
            x++;
            res.moveToNext();
        }
        res.close();
        return results;
    }

    public ArrayList<ListItem> getAcceptanceInfoEmpty(String man){
        ArrayList<ListItem> results = new ArrayList<>();
        SQLiteDatabase db = gen.getReadableDatabase();
        Cursor res = db.rawQuery(" SELECT * FROM " + gen.tb_acceptance
                +" LEFT JOIN "+gen.tbname_boxes+" ON "
                +gen.tbname_boxes+"."+gen.box_id+" = "+gen.tb_acceptance+"."+gen.acc_boxtype
                + " WHERE " + gen.acc_name + " = '" + man + "'", null);
        res.moveToFirst();
        while (!res.isAfterLast()) {
            String ids = res.getString(res.getColumnIndex(gen.acc_id));
            String sub = res.getString(res.getColumnIndex(gen.acc_quantity));
            String top = res.getString(res.getColumnIndex(gen.box_name));
            ListItem list = new ListItem(ids, top, sub,"");
            results.add(list);
            res.moveToNext();
        }
        res.close();
        return results;
    }

    private String getTruck(String trans){
        String name = "";
        SQLiteDatabase db = gen.getReadableDatabase();
        Cursor x = db.rawQuery(" SELECT * FROM "+gen.tbname_check_acceptance
        +" WHERE "+gen.accept_transactionid+" = '"+trans+"'", null);
        if (x.moveToNext()){
            name = x.getString(x.getColumnIndex(gen.accept_container));
        }
        return name;
    }

    @Override
    public void onBackPressed(){
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
            if (value.equals("Partner Portal")){
                startActivity(new Intent(this, Partner_acceptance.class));
                finish();
            }
            else {
                startActivity(new Intent(this, Acceptance.class));
                finish();
            }
        } else {
            if (value.equals("Partner Portal")){
                startActivity(new Intent(this, Partner_acceptance.class));
                finish();
            }
            else {
                startActivity(new Intent(this, Acceptance.class));
                finish();
            }
            super.onBackPressed();
        }
    }

    public void scroll(){
        lv.setOnTouchListener(new View.OnTouchListener() {
            // Setting on Touch Listener for handling the touch inside ScrollView
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Disallow the touch request for parent scroll on touch of child view
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });
    }

    public ArrayList<LinearItem> getAllAcceptance(){
        ArrayList<LinearItem> results = new ArrayList<>();
        SQLiteDatabase db = gen.getReadableDatabase();
        Cursor res = db.rawQuery(" SELECT * FROM " + gen.tbname_check_acceptance+" tca "
                +" LEFT JOIN "+gen.tbname_employee+" ge ON ge."+gen.emp_id+" = tca."+gen.accept_drivername
                +" WHERE tca."+gen.accept_createdby+" = '"+helper.logcount()+"'", null);
        res.moveToFirst();
        String topitem = null;
        while (!res.isAfterLast()) {
            String id = res.getString(res.getColumnIndex(gen.accept_transactionid));
            if (value.equals("Partner Portal") || value.equals("Partner Driver")){
                topitem = res.getString(res.getColumnIndex(gen.accept_drivername));
            }else {
                topitem = res.getString(res.getColumnIndex(gen.emp_first)) + " "
                        + res.getString(res.getColumnIndex(gen.emp_last));
            }
            String sub = res.getString(res.getColumnIndex(gen.accept_date));
            LinearItem list = new LinearItem(id, topitem, sub);
            results.add(list);
            res.moveToNext();
        }
        res.close();
        // Add some more dummy data for testing
        return results;
    }

    public ArrayList<LinearItem> getEmptyAcceptance(){
        ArrayList<LinearItem> results = new ArrayList<>();
        SQLiteDatabase db = gen.getReadableDatabase();
        Cursor res = db.rawQuery(" SELECT * FROM " + gen.tb_acceptance
                +" WHERE "+gen.acc_createdby+" = '"+helper.logcount()+"' GROUP BY "
                +gen.acc_createddate+" AND "+gen.acc_name, null);
        res.moveToFirst();
        while (!res.isAfterLast()) {
            String id = res.getString(res.getColumnIndex(gen.acc_id));
            String topitem = res.getString(res.getColumnIndex(gen.acc_name));
            String sub = res.getString(res.getColumnIndex(gen.acc_createddate));
            LinearItem list = new LinearItem(id, topitem, sub);
            results.add(list);
            res.moveToNext();
        }
        res.close();
        // Add some more dummy data for testing
        return results;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.checker__acceptance, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.acceptancelist).setVisible(false);
        Bundle bundle = getIntent().getExtras();
        String type = bundle.getString("type");
        if (type.equals("0")){
            menu.findItem(R.id.syncacceptance).setVisible(true);
        }else{
            menu.findItem(R.id.syncacceptance).setVisible(true);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (type.equals("0")){
            if (id == R.id.syncacceptance) {
                startActivity(new Intent(getApplicationContext(), Oic_inventory.class));
                finish();
            }
            else if (id ==  R.id.passincident){
                Intent i = new Intent(this, Incident.class);
                Bundle bundle = new Bundle();
                bundle.putString("module", "Acceptance");
                //Add the bundle to the intent
                i.putExtras(bundle);
                startActivity(i);
                finish();
            }
        }else{
            if (id == R.id.syncacceptance) {
                network();
            }
            else if (id ==  R.id.passincident){
                Intent i = new Intent(this, Incident.class);
                Bundle bundle = new Bundle();
                bundle.putString("module", "Acceptance");
                //Add the bundle to the intent
                i.putExtras(bundle);
                startActivity(i);
                finish();
            }
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

    public void delAcc(String id){
        SQLiteDatabase db = gen.getWritableDatabase();
        db.delete(gen.tbname_check_acceptance, gen.accept_transactionid+" = '"+id+"'", null);
        db.close();
    }

    public void delAccBox(String id){
        SQLiteDatabase db = gen.getWritableDatabase();
        db.delete(gen.tbname_accept_boxes, gen.acc_box_transactionno+" = '"+id+"'", null);
        db.close();
    }

    public void delAccEmpty(String id){
        SQLiteDatabase db = gen.getWritableDatabase();
        db.delete(gen.tb_acceptance, gen.acc_id+" = '"+id+"'", null);
        db.close();
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

    public void threadAcceptance(){
        db = gen.getReadableDatabase();
        String query = " SELECT * FROM "+gen.tbname_check_acceptance
                +" WHERE "+gen.accept_uploadstat+" = '1' AND "
                +gen.accept_createdby+" = '"+helper.logcount()+"'";
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
                if (!conn.getResponseMessage().equals("OK")){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.dismiss();
                            final AlertDialog.Builder builder = new AlertDialog.Builder(Acceptancelist.this);
                            builder.setTitle("Upload failed")
                                    .setMessage("Data upload has failed, please try again later. thank you.")
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
                }else{
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.dismiss();
                            final AlertDialog.Builder builder = new AlertDialog.Builder(Acceptancelist.this);
                            builder.setTitle("Information confirmation")
                                    .setMessage("Data upload has been successful, thank you.")
                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            updateAcceptances("2");
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
                    final AlertDialog.Builder builder = new AlertDialog.Builder(Acceptancelist.this);
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
        //END THREAD incident API
    }

    //send sync data
    public void sendPost() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {

                threadAcceptance();

            }
        });

        thread.start();
    }

    public JSONArray getAcceptanceList() {
        SQLiteDatabase myDataBase = gen.getReadableDatabase();
        String raw = "SELECT * FROM " + gen.tbname_check_acceptance
                +" WHERE "+gen.accept_uploadstat+" = '1' AND "
                +gen.accept_createdby+" = '"+helper.logcount()+"'";
        Cursor c = myDataBase.rawQuery(raw, null);
        JSONArray resultSet = new JSONArray();
        c.moveToFirst();
        try {
            while (!c.isAfterLast()) {
                JSONObject js = new JSONObject();
                String id = c.getString(c.getColumnIndex(gen.accept_id));
                String trans = c.getString(c.getColumnIndex(gen.accept_transactionid));
                String driver = c.getString(c.getColumnIndex(gen.accept_drivername));
                String wareh = c.getString(c.getColumnIndex(gen.accept_warehouseid));
                String tru = c.getString(c.getColumnIndex(gen.accept_container));
                String date = c.getString(c.getColumnIndex(gen.accept_date));
                String by = c.getString(c.getColumnIndex(gen.accept_createdby));

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

    //update all customer upload status to 2 after sync
    public void updateAcceptances(String stat){
        SQLiteDatabase db = gen.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(gen.accept_uploadstat, stat);
        db.update(gen.tbname_check_acceptance, cv,
                gen.accept_uploadstat+" = '1'", null);
        Log.e("upload", "uploaded customers");
        db.close();
    }

    public JSONArray getAcceptedBox(String tr) {
        SQLiteDatabase myDataBase = gen.getReadableDatabase();
        String raw = " SELECT * FROM " + gen.tbname_accept_boxes + " WHERE "
                + gen.acc_box_transactionno + " = '" + tr + "'";
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
