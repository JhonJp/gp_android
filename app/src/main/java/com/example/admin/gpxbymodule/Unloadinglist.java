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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
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
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;
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

public class Unloadinglist extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    NavigationView navigationView;
    HomeDatabase helper;
    GenDatabase gen;
    RatesDB rate;
    SQLiteDatabase db;
    String value;
    ListView lv;
    LinearList adapter;
    ProgressDialog progressBar;
    AutoCompleteTextView search;
    ArrayList<String> unids;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unloadinglist);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);rate = new RatesDB(this);
        helper = new HomeDatabase(this);
        gen = new GenDatabase(this);
        lv = (ListView)findViewById(R.id.lv);
        search = (AutoCompleteTextView)findViewById(R.id.searchableinput);

        try{

            unids = new ArrayList<>();
            if (helper.logcount() != 0){
                value = helper.getRole(helper.logcount());
                Log.e("role ", value);
            }
            fabclick();
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
        }catch (Exception e){}
        customtype();
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
                        final AlertDialog.Builder builder = new AlertDialog.Builder(Unloadinglist.this);
                        builder.setMessage("Confirm logout?")
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        helper.logout();
                                        startActivity(new Intent(Unloadinglist.this, Login.class));
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
                        startActivity(new Intent(Unloadinglist.this, Home.class));
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
                    bundle.putString("module", "Unloading");
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

    public void fabclick(){
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), Load_home.class));
                finish();
            }
        });
    }

    public void customtype(){
        try {
            final ArrayList<LinearItem> result = gen.getUnloads(helper.logcount() + "");
            adapter = new LinearList(getApplicationContext(), result);
            lv.setAdapter(adapter);
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    extendView(view);
                }
            });
            item();
        }catch (Exception e){}
    }

    public void extendView(View v){
        try{
            TextView textView = (TextView) v.findViewById(R.id.dataid);
            final String mtop = textView.getText().toString();
            ArrayList<ListItem> poparray;
            final Dialog dialog = new Dialog(Unloadinglist.this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(false);
            dialog.setContentView(R.layout.distdatalayout);
            TextView top = (TextView) dialog.findViewById(R.id.reservationnumber);
            TextView owner = (TextView) dialog.findViewById(R.id.owner);
            TextView trucktitle = (TextView) dialog.findViewById(R.id.trucknumtitle);
            TextView whom = (TextView) dialog.findViewById(R.id.ownerinfo);
            TextView truck = (TextView) dialog.findViewById(R.id.truckinput);
            ListView poplist = (ListView)dialog.findViewById(R.id.list);

            poparray = getUnloadingInfo(mtop);

            TableAdapter tb = new TableAdapter(getApplicationContext(), poparray);
            poplist.setAdapter(tb);

            top.setText("Unloading Information");
            owner.setText("Transaction number");
            trucktitle.setText("Shipping line");
            whom.setText(Html.fromHtml(""+mtop+""));
            truck.setText(getForwarder(mtop));

            Button close = (Button)dialog.findViewById(R.id.close);
            close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            dialog.show();
        }catch (Exception e){}
    }

    public void item(){
        try{
            lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    TextView textView = (TextView) view.findViewById(R.id.dataid);
                    final String mtop = textView.getText().toString();
                    final AlertDialog.Builder builder = new AlertDialog.Builder(Unloadinglist.this);
                    builder.setTitle("Delete this transaction?");
                    builder.setMessage(Html.fromHtml("<b>note : </b>" +
                            "<i>Are you sure you want to delete this data? " +
                            "Once deleted, the data cannot be retrieved.</i>"))
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    deleteUnloading(mtop);
                                    deleteUnloadBoxes(mtop);
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

    public void deleteUnloadBoxes(String id) {
        SQLiteDatabase db = gen.getWritableDatabase();
        db.delete(gen.tb_unloadbox, gen.unloadbox_trans + " = '" + id+"'", null);
        db.close();
    }

    public void deleteUnloading(String id) {
        SQLiteDatabase db = gen.getWritableDatabase();
        db.delete(gen.tb_unload, gen.unload_id + " = '" +id+"'", null);
        db.close();
    }

    public String getForwarder(String id){
        String tr = null;
        SQLiteDatabase db =gen.getReadableDatabase();
        Cursor t = db.rawQuery(" SELECT * FROM "+gen.tb_unload+" WHERE "
                +gen.unload_id+" = '"+id+"'", null);
        if (t.moveToNext()){
            tr = t.getString(t.getColumnIndex(gen.unload_forward));
        }
        return tr;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
            startActivity(new Intent(getApplicationContext(), Load_home.class));
            finish();
        } else {
            startActivity(new Intent(getApplicationContext(), Load_home.class));
            finish();
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.unloadinglist, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            network();
        }
        else if (id ==  R.id.passincident){
            Intent i = new Intent(this, Incident.class);
            Bundle bundle = new Bundle();
            bundle.putString("module", "Unloading");
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

    public ArrayList<ListItem> getUnloadingInfo(String trans){
        ArrayList<ListItem> results = new ArrayList<>();
        SQLiteDatabase db = gen.getReadableDatabase();
        Cursor res = db.rawQuery(" SELECT * FROM " + gen.tb_unloadbox + " WHERE "
                + gen.unloadbox_trans + " = '" + trans + "'", null);
        res.moveToFirst();
        int i = 1;
        while (!res.isAfterLast()) {
            String ids = res.getString(res.getColumnIndex(gen.unloadbox_id));
            String sub = "1";
            String top = res.getString(res.getColumnIndex(gen.unload_boxnum));
            ListItem list = new ListItem(ids, i+"", top,"");
            results.add(list);
            i++;
            res.moveToNext();
        }
        res.close();
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

    public void threadUnloading(){
        db = gen.getReadableDatabase();
        String q = " SELECT * FROM "+ gen.tb_unload+" WHERE "+gen.unload_stat+" = '1' AND "
                +gen.unload_upds+" = '1'";
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
                db = gen.getReadableDatabase();
                JSONArray finalarray = new JSONArray();
                JSONArray boxtypes = null;

                String query = " SELECT * FROM " + gen.tb_unload+" WHERE "+gen.unload_stat+" = '1'";
                Cursor c = db.rawQuery(query, null);
                c.moveToFirst();
                while (!c.isAfterLast()) {
                    JSONObject json = new JSONObject();
                    String id = c.getString(c.getColumnIndex(gen.unload_id));
                    String date = c.getString(c.getColumnIndex(gen.unload_date));
                    String forwarder = c.getString(c.getColumnIndex(gen.unload_forward));
                    String container = c.getString(c.getColumnIndex(gen.unload_con_number));
                    String eta = c.getString(c.getColumnIndex(gen.unload_eta));
                    String start = c.getString(c.getColumnIndex(gen.unload_timestart));
                    String end = c.getString(c.getColumnIndex(gen.unload_timeend));
                    String by = c.getString(c.getColumnIndex(gen.unload_con_by));
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
                    unids.add(id);
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
                            final AlertDialog.Builder builder
                                    = new AlertDialog.Builder(Unloadinglist.this);
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
                            final AlertDialog.Builder builder = new AlertDialog.Builder(Unloadinglist.this);
                            builder.setTitle("Information confirmation")
                                    .setMessage("Data upload has been successful, thank you.")
                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            updateunLoadsStat(unids);
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
                    final AlertDialog.Builder builder
                            = new AlertDialog.Builder(Unloadinglist.this);
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

    //send sync data
    public void sendPost() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                threadUnloading();
            }
        });

        thread.start();
    }

    //unloading
    public JSONArray getAllUnloadBox(String id) {
        SQLiteDatabase myDataBase = gen.getReadableDatabase();
        String raw = " SELECT * FROM " + gen.tb_unloadbox+" WHERE "+gen.unloadbox_trans+" = '"+id+"'";
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

    public JSONArray getUnloadImage(String trans) {
        SQLiteDatabase myDataBase = rate.getReadableDatabase();
        String raw = " SELECT * FROM " + rate.tbname_unloadingbox_image
                +" WHERE "+rate.unbi_trans+" = '"+trans+"'";
        Cursor cursor = myDataBase.rawQuery(raw, null);
        JSONArray resultSet = new JSONArray();
        cursor.moveToFirst();
        try {
            while (!cursor.isAfterLast()) {
                JSONObject js = new JSONObject();
                String tr = cursor.getString(cursor.getColumnIndex(rate.unbi_trans));
                String bnum = cursor.getString(cursor.getColumnIndex(rate.unbi_boxnumber));
                byte[] image = cursor.getBlob(cursor.getColumnIndex(rate.unbi_image));
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

    // convert from bitmap to byte array
    public byte[] getBytesFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
        return stream.toByteArray();
    }

    public void updateunLoadsStat(ArrayList<String> id){
        SQLiteDatabase db = gen.getWritableDatabase();
        for (String ids : id) {
            ContentValues cv = new ContentValues();
            cv.put(gen.unload_upds, "2");
            db.update(gen.tb_unload, cv,gen.unload_id+" = '"+ids+"' AND "+
                    gen.unload_upds + " = '1'", null);
            Log.e("upload", "uploaded unloading");
        }
        db.close();
    }

}
