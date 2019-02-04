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
import android.util.Base64OutputStream;
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
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Blob;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class Incidentlist extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    HomeDatabase helper;
    GenDatabase gen;
    SQLiteDatabase db;
    GridView grimg;
    String value;
    NavigationView navigationView;
    ListView lvi;
    TextView hint;
    TextView type, reason;
    LinearList adapter;
    AutoCompleteTextView search;
    TextView idget;
    ProgressDialog progressBar;
    ArrayList<String> ids,incids;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incidentlist);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        helper = new HomeDatabase(this);
        gen = new GenDatabase(this);

        lvi = (ListView)findViewById(R.id.lvinclist);
        search = (AutoCompleteTextView)findViewById(R.id.searchableinput);
        search.setSelected(false);
        ids = new ArrayList<>();
        incids = new ArrayList<>();

        if (helper.logcount() != 0){
            value = helper.getRole(helper.logcount());
        }

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

        addList();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), Incident.class);
                Bundle bundle = new Bundle();
                bundle.putString("module", null);
                //Add the bundle to the intent
                i.putExtras(bundle);
                startActivity(i);
                finish();
            }
        });

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

    public void addList() {
        final ArrayList<LinearItem> result = gen.getAllIncident();
        adapter = new LinearList(getApplicationContext(), result);
        lvi.setAdapter(adapter);
        lvi.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView textView = (TextView) view.findViewById(R.id.c_account);
                idget = (TextView) view.findViewById(R.id.dataid);

                Log.e("mtop", idget.getText().toString());
                final Dialog dialog = new Dialog(Incidentlist.this);
                dialog.setTitle(" Incident report information");
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setCancelable(true);
                dialog.setContentView(R.layout.incidentinflate);
                type = (TextView)dialog.findViewById(R.id.inctypeinput);
                reason = (TextView)dialog.findViewById(R.id.reasoninput);
                grimg = (GridView)dialog.findViewById(R.id.grid);
                hint = (TextView)dialog.findViewById(R.id.imageshint);

                viewgrid(idget.getText().toString());
                type.setText(Html.fromHtml(""+getType(idget.getText().toString())+""));
                reason.setText(Html.fromHtml(""+getReason(idget.getText().toString())+""));

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
        lvi.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(Incidentlist.this);
                String delete = "Delete";
                builder.setTitle("Delete this data.")
                        .setMessage(Html.fromHtml("<b>note:</b><i>Once data is" +
                                " deleted you can not retrieve it.</i>"))
                        .setPositiveButton(delete.toUpperCase(), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                gen.deleteIncident(idget.getText().toString());
                                gen.deleteAllImage(getTransNum(idget.getText().toString()));
                                addList();
                            }
                        });
                // Create an alert
                builder.create().show();
                return true;
            }
        });
    }

    public void sidenavMain(){
        ListView lv=(ListView)findViewById(R.id.optionsList);//initialization of listview
        final ArrayList<HomeList> listitem = helper.getData(value);
        NavAdapter ad = new NavAdapter(Incidentlist.this, listitem);
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
                        final AlertDialog.Builder builder = new AlertDialog.Builder(Incidentlist.this);
                        builder.setMessage("Confirm logout?")
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        helper.logout();
                                        startActivity(new Intent(Incidentlist.this, Login.class));
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
                bundle.putString("module", null);
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

    public void viewgrid(String x){
        try {
            final ArrayList<HomeList> listitem = gen.getImages(getTransNum(x));
            ImageAdapter myAdapter = new ImageAdapter(this, listitem);
            grimg.setAdapter(myAdapter);
            if (grimg.getAdapter().getCount() > 0) {
                hint.setVisibility(View.INVISIBLE);
            } else {
                hint.setVisibility(View.VISIBLE);
            }
        }catch (Exception e){}
    }

    public String getTransNum(String id){
        String tr = null;
        try {
            SQLiteDatabase db = gen.getReadableDatabase();
            Cursor c = db.rawQuery(" SELECT * FROM " + gen.tbname_incident
                    + " WHERE " + gen.inc_id + " = '" + id + "'", null);
            if (c.moveToNext()) {
                tr = c.getString(c.getColumnIndex(gen.inc_transnum));
            }
        }catch (Exception e){}
        return tr;
    }

    public String getReason(String id){
        String tr = null;
        try {
            SQLiteDatabase db = gen.getReadableDatabase();
            Cursor c = db.rawQuery(" SELECT * FROM " + gen.tbname_incident
                    + " WHERE " + gen.inc_id + " = '" + id + "'", null);
            if (c.moveToNext()) {
                tr = c.getString(c.getColumnIndex(gen.inc_reason));
            }
        }catch (Exception e){}
        return tr;
    }

    public String getType(String id){
        String tr = null;
        try {
            SQLiteDatabase db = gen.getReadableDatabase();
            Cursor c = db.rawQuery(" SELECT * FROM " + gen.tbname_incident
                    + " WHERE " + gen.inc_id + " = '" + id + "'", null);
            if (c.moveToNext()) {
                tr = c.getString(c.getColumnIndex(gen.inc_type));
            }
        }catch (Exception e){}
        return tr;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
            Intent i = new Intent(this, Incident.class);
            Bundle bundle = new Bundle();
            bundle.putString("module", null);
            //Add the bundle to the intent
            i.putExtras(bundle);
            startActivity(i);
            finish();
        } else {
            Intent i = new Intent(this, Incident.class);
            Bundle bundle = new Bundle();
            bundle.putString("module", null);
            //Add the bundle to the intent
            i.putExtras(bundle);
            startActivity(i);
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.incidentlist, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.syncincident){
            network();
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item){
        int id = item.getItemId();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void network(){
        if (isNetworkAvailable() == true){
            threadIncident();
            loadingPost(getWindow().getDecorView().getRootView());
        }else
        {
            Toast.makeText(getApplicationContext(),"Please check internet connection.", Toast.LENGTH_LONG).show();
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    //threads
    public void threadIncident(){
        Thread  thread = new Thread(new Runnable() {
            @Override
            public void run() {
                SQLiteDatabase db = gen.getReadableDatabase();
                String query = " SELECT * FROM "+gen.tbname_incident
                        +" WHERE "+gen.inc_createdby+" = '"+helper.logcount()+"' AND "
                        +gen.inc_upds+" = '1'";
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
                                            = new AlertDialog.Builder(Incidentlist.this);
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
                        }else{
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progressBar.dismiss();
                                    final AlertDialog.Builder builder
                                            = new AlertDialog.Builder(Incidentlist.this);
                                    builder.setTitle("Information confirmation")
                                            .setMessage("Data upload has been successful, thank you.")
                                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    updateIncStat(incids);
                                                    dialog.dismiss();
                                                    addList();
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
                            final AlertDialog.Builder builder = new AlertDialog.Builder(Incidentlist.this);
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
        });
        thread.start();
    }

    public JSONArray getAllIncidents() {
        SQLiteDatabase myDataBase = gen.getReadableDatabase();
        String raw = "SELECT * FROM " + gen.tbname_incident
                +" WHERE "+gen.inc_createdby+" = '"+helper.logcount()+"' AND "
                +gen.inc_upds+" = '1'";
        Cursor c = myDataBase.rawQuery(raw, null);
        JSONArray resultSet = new JSONArray();
        c.moveToFirst();
        try {
            while (!c.isAfterLast()) {
                JSONObject js = new JSONObject();
                String idtrue = c.getString(c.getColumnIndex(gen.inc_id));
                if (!ids.contains(idtrue)) {
                    ids.add(idtrue);
                }
                String id = c.getString(c.getColumnIndex(gen.inc_transnum));
                String type = c.getString(c.getColumnIndex(gen.inc_type));
                String reason = c.getString(c.getColumnIndex(gen.inc_reason));
                String mod = c.getString(c.getColumnIndex(gen.inc_module));
                String boxnum = c.getString(c.getColumnIndex(gen.inc_boxnum));
                String by = c.getString(c.getColumnIndex(gen.inc_createdby));
                String d = c.getString(c.getColumnIndex(gen.inc_createddate));

                js.put("id", id);
                js.put("incident_type", type);
                js.put("reason", reason);
                js.put("module", mod);
                js.put("box_number", boxnum);
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

    public JSONArray getIncidentImage(String tr){
        SQLiteDatabase myDataBase = gen.getReadableDatabase();
        String raw = " SELECT * FROM " + gen.tbname_incimages + " WHERE "
                + gen.inc_img_transaction_no + " = '" + tr + "'";
        Cursor c = myDataBase.rawQuery(raw, null);
        JSONArray resultSet = new JSONArray();
        c.moveToFirst();
        try {
            while (!c.isAfterLast()) {
                JSONObject js = new JSONObject();
                byte[] image = c.getBlob(c.getColumnIndex(gen.inc_img_imageblob));
                Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
                byte[] bitmapdata = getBytesFromBitmap(bitmap);

                // get the base 64 string
                String imgString = Base64.encodeToString(bitmapdata, Base64.NO_WRAP);

                js.put("incident_image", imgString);

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

    // convert from bitmap to byte array
    public byte[] getBytesFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
        return stream.toByteArray();
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

    //update incident upload status
    public void updateIncStat(ArrayList<String> id){
        for (String ids : id) {
            SQLiteDatabase db = gen.getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put(gen.inc_upds, "2");
            db.update(gen.tbname_incident, cv,
                    gen.inc_upds+" = '1'", null);
            Log.e("upload", "uploaded incidents");
            db.close();
        }
    }

}
