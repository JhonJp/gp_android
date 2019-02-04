package com.example.admin.gpxbymodule;

import android.annotation.TargetApi;
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
import android.text.Html;
import android.text.TextWatcher;
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

public class Partner_Maindelivery extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    GenDatabase gen;
    RatesDB rate;
    HomeDatabase helper;
    String value;
    ListView lv;
    AutoCompleteTextView search;
    LinearList adapter;
    NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maindelivery);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Direct");

        helper = new HomeDatabase(this);
        gen = new GenDatabase(this);
        rate = new RatesDB(this);

        if (helper.logcount() != 0){
            value = helper.getRole(helper.logcount());
            Log.e("role ", value);
        }
        lv = (ListView)findViewById(R.id.lv);
        search = (AutoCompleteTextView)findViewById(R.id.searchableinput);
        search.setSelected(false);
        try{
            search.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    customtype();
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
        }catch (Exception e){}

        scroll();
        customtype();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        sidenavMain();
        setNameMail();
        subMenu();
    }

    public void customtype(){
        try{
            final ArrayList<LinearItem> result =
                    getDirectDeliver();
            adapter = new LinearList(getApplicationContext(), result);
            lv.setAdapter(adapter);
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    extendViewing(view);
                }
            });

        }catch (Exception e){}
    }

    public void extendViewing(View v) {
        String mtop = "", driver = "";
        TextView manname = (TextView) v.findViewById(R.id.dataid);
        TextView dri = (TextView) v.findViewById(R.id.c_account);
        mtop = manname.getText().toString();
        driver = dri.getText().toString();
        ArrayList<ListItem> poparray;
        final Dialog dialog = new Dialog(Partner_Maindelivery.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.distdatalayout);
        TextView top = (TextView) dialog.findViewById(R.id.reservationnumber);
        TextView owner = (TextView) dialog.findViewById(R.id.owner);
        TextView whom = (TextView) dialog.findViewById(R.id.ownerinfo);
        TextView truck = (TextView) dialog.findViewById(R.id.truckinput);
        TextView trucktitle = (TextView) dialog.findViewById(R.id.trucknumtitle);
        ListView poplist = (ListView)dialog.findViewById(R.id.list);

        poparray = getAcceptanceInfo(mtop);

        TableAdapter tb = new TableAdapter(getApplicationContext(), poparray);
        poplist.setAdapter(tb);

        top.setText("Delivery information");
        trucktitle.setText("Box information");
        owner.setText("Driver name");
        whom.setText(Html.fromHtml(""+driver+""));
        truck.setVisibility(View.INVISIBLE);

        Button close = (Button)dialog.findViewById(R.id.close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public ArrayList<ListItem> getAcceptanceInfo(String trans){
        ArrayList<ListItem> results = new ArrayList<>();
        SQLiteDatabase db = rate.getReadableDatabase();
        Cursor res = db.rawQuery(" SELECT * FROM " + rate.tbname_part_distribution_box
                + " WHERE " + rate.partdist_box_distributionid + " = '" + trans + "'", null);
        res.moveToFirst();
        int x = 1;
        while (!res.isAfterLast()) {
            String ids = res.getString(res.getColumnIndex(rate.partdist_box_id));
            String top = res.getString(res.getColumnIndex(rate.partdist_box_boxnumber));
            ListItem list = new ListItem(ids, x+"", top,"");
            results.add(list);
            x++;
            res.moveToNext();
        }
        res.close();
        return results;
    }

    public void scroll(){
        try{
            lv.setOnTouchListener(new View.OnTouchListener() {
                // Setting on Touch Listener for handling the touch inside ScrollView
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    // Disallow the touch request for parent scroll on touch of child view
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    return false;
                }
            });
        }catch (Exception e){}
    }

    public void sidenavMain(){
        ListView lv=(ListView)findViewById(R.id.optionsList);//initialization of listview

        final ArrayList<HomeList> listitem = helper.getData(value);

        NavAdapter ad = new NavAdapter(Partner_Maindelivery.this, listitem);
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
                        final AlertDialog.Builder builder = new AlertDialog.Builder(Partner_Maindelivery.this);
                        builder.setMessage("Confirm logout?")
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        helper.logout();

                                        startActivity(new Intent(Partner_Maindelivery.this, Login.class));
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
                bundle.putString("module", "Delivery");
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
                drawer.closeDrawer(Gravity.START);
                break;
            case "Barcode Releasing":
                startActivity(new Intent(this, BoxRelease.class));
                finish();
                break;
        }
    }

    public void setNameMail(){
        View header = navigationView.getHeaderView(0);
        TextView user = (TextView)header.findViewById(R.id.yourname);
        TextView mail = (TextView)header.findViewById(R.id.yourmail);
        user.setText(helper.getFullname(helper.logcount()+""));
        mail.setText(helper.getRole(helper.logcount())+" / branch "+helper.getBranch(""+helper.logcount()));
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
        int id = item.getItemId();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public ArrayList<LinearItem> getDirectDeliver(){
        ArrayList<LinearItem> results = new ArrayList<LinearItem>();
        SQLiteDatabase db = rate.getReadableDatabase();
        String y = " SELECT * FROM "+rate.tbname_part_distribution
                +" WHERE "+rate.partdist_type+" LIKE '%Direct%' AND "
                +rate.partdist_createby+" = '"+helper.logcount()+"'";
        Cursor res = db.rawQuery(y, null);
        res.moveToFirst();
        while (!res.isAfterLast()){
            String finsub = "";
            String id = res.getString(res.getColumnIndex(rate.partdist_transactionnumber));
            String topitem = res.getString(res.getColumnIndex(rate.partdist_drivername));
            String subitem = res.getString(res.getColumnIndex(rate.partdist_createdate));
            String stat = res.getString(res.getColumnIndex(rate.partdist_uploadstat));
            if (stat.equals("1")){
                finsub = subitem+" / pending upload";
            }else{
                finsub = subitem+" / uploaded";
            }
            LinearItem list = new LinearItem(id, topitem, finsub);
            results.add(list);

            res.moveToNext();
        }
        return results;
    }

    //syncing data reservations
    //connecting to internet
//    private boolean isNetworkAvailable() {
//        ConnectivityManager connectivityManager
//                = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
//        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
//    }
//
//    public void network(){
//        if (isNetworkAvailable()== true){
//            sendPost();
//            loadingPost(getWindow().getDecorView().getRootView());
//        }else
//        {
//            Toast.makeText(getApplicationContext(),"Please check internet connection.", Toast.LENGTH_LONG).show();
//        }
//    }
//
//    public void loadingPost(final View v){
//        // prepare for a progress bar dialog
//        int max = 100;
//        progressBar = new ProgressDialog(v.getContext());
//        progressBar.setCancelable(false);
//        progressBar.setMessage("In Progress ...");
//        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//        progressBar.setMax(max);
//        for (int i = 0; i <= max; i++) {
//            progressBar.setProgress(i);
//            if (i == max ){
//                progressBar.dismiss();
//            }
//            progressBar.show();
//        }
//    }
//
//    public void threadDelivery(){
//        SQLiteDatabase db = gen.getReadableDatabase();
//        String q = " SELECT * FROM "+gen.tbname_delivery
//                +" WHERE "+gen.del_createdby+" = '"+helper.logcount()+"' AND "+gen.del_upds+" = '1'";
//        Cursor cx = db.rawQuery(q, null);
//        if (cx.getCount() != 0) {
//            try {
//                String link = helper.getUrl();
//                URL url = new URL("http://" + link + "/api/delivery/save.php");
//                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//                conn.setRequestMethod("POST");
//                conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
//                conn.setRequestProperty("Accept", "application/json");
//                conn.setDoOutput(true);
//                conn.setDoInput(true);
//                JSONObject jsonParam = new JSONObject();
//                db = gen.getReadableDatabase();
//                JSONArray finalarray = new JSONArray();
//                JSONArray pay = null, reserve = null, boxtypes = null;
//
//                String query = " SELECT * FROM " +gen.tbname_delivery
//                        +" WHERE "+gen.del_createdby+" = '"+helper.logcount()+"' AND "+gen.del_upds+" = '1'";
//                Cursor c = db.rawQuery(query, null);
//                c.moveToFirst();
//
//
//                String reserveno = null;
//                while (!c.isAfterLast()) {
//                    JSONObject json = new JSONObject();
//                    String id = c.getString(c.getColumnIndex(gen.del_id));
//                    String trans = c.getString(c.getColumnIndex(gen.del_booking_no));
//                    String cust = c.getString(c.getColumnIndex(gen.del_customer));
//                    String date = c.getString(c.getColumnIndex(gen.del_createddate));
//                    byte[] sign = c.getBlob(c.getColumnIndex(gen.del_sign));
//                    String by = c.getString(c.getColumnIndex(gen.del_createdby));
//
//                    json.put("id", id);
//                    json.put("transaction_no", trans);
//                    json.put("customer", cust);
//                    json.put("createddate", date);
//                    json.put("signature", sign);
//                    json.put("createdby", by);
//                    json.put("delivery_box", getDeliveryBox(id));
//                    json.put("delivery_image", getImage(id));
//                    delids.add(id);
//                    finalarray.put(json);
//                    c.moveToNext();
//                }
//                c.close();
//                jsonParam.accumulate("data", finalarray);
//                Log.e("JSON", jsonParam.toString());
//                DataOutputStream os = new DataOutputStream(conn.getOutputStream());
//                os.writeBytes(jsonParam.toString());
//                os.flush();
//                os.close();
//
//                Log.i("STATUS", String.valueOf(conn.getResponseCode()));
//                Log.i("MSG", conn.getResponseMessage());
//                if (!conn.getResponseMessage().equals("OK")){
//                    conn.disconnect();
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            progressBar.dismiss();
//                            final AlertDialog.Builder builder
//                                    = new AlertDialog.Builder(Partner_Maindelivery.this);
//                            builder.setTitle("Upload failed")
//                                    .setMessage("Data sync has failed, please try again later. thank you.")
//                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//                                        public void onClick(DialogInterface dialog, int id) {
//                                            dialog.dismiss();
//                                        }
//                                    });
//                            // Create the AlertDialog object and show it
//                            builder.create().show();
//                        }
//                    });
//                }else{
//                    conn.disconnect();
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            progressBar.dismiss();
//                            final AlertDialog.Builder builder
//                                    = new AlertDialog.Builder(Partner_Maindelivery.this);
//                            builder.setTitle("Information confirmation")
//                                    .setMessage("Data upload has been successful, thank you.")
//                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//                                        public void onClick(DialogInterface dialog, int id) {
//                                            updateDel(delids);
//                                            dialog.dismiss();
//                                        }
//                                    });
//                            // Create the AlertDialog object and show it
//                            builder.create();
//                            builder.setCancelable(false);
//                            builder.show();
//                        }
//                    });
//                }
//                conn.disconnect();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }else{
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    progressBar.dismiss();
//                    final AlertDialog.Builder builder
//                            = new AlertDialog.Builder(Partner_Maindelivery.this);
//                    builder.setTitle("Information confirmation")
//                            .setMessage("You dont have data to be uploaded yet, please add new transactions. Thank you.")
//                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//                                public void onClick(DialogInterface dialog, int id) {
//                                    dialog.dismiss();
//                                }
//                            });
//                    builder.create();
//                    builder.setCancelable(false);
//                    builder.show();
//                }
//            });
//        }
//    }
//
//    public JSONArray getDeliveryBox(String id) {
//        SQLiteDatabase myDataBase = gen.getReadableDatabase();
//        String raw = " SELECT * FROM " + gen.tbname_delivery_box+" WHERE "
//                +gen.del_box_deliveryid+" = '"+id+"'";
//        Cursor c = myDataBase.rawQuery(raw, null);
//        JSONArray resultSet = new JSONArray();
//        c.moveToFirst();
//        while (!c.isAfterLast()) {
//            try {
//
//                JSONObject json = new JSONObject();
//                String ids = c.getString(c.getColumnIndex(gen.del_box_deliveryid));
//                String boxnum = c.getString(c.getColumnIndex(gen.del_box_boxnumber));
//                String rec = c.getString(c.getColumnIndex(gen.del_box_receiver));
//                String orig = c.getString(c.getColumnIndex(gen.del_box_origin));
//                String dest = c.getString(c.getColumnIndex(gen.del_box_destination));
//                String crdate = c.getString(c.getColumnIndex(gen.del_box_crdate));
//                String rem = c.getString(c.getColumnIndex(gen.del_box_remarks));
//                String stat = c.getString(c.getColumnIndex(gen.del_box_status));
//
//                json.put("delivery_id", ids);
//                json.put("box_number", boxnum);
//                json.put("receiver",  rec);
//                json.put("origin", orig);
//                json.put("destination", dest);
//                json.put("createddate", crdate);
//                json.put("remarks", rem);
//                json.put("status", stat);
//                resultSet.put(json);
//                c.moveToNext();
//
//            }catch (Exception e){}
//        }
//        c.close();
//        //Log.e("result set", resultSet.toString());
//        return resultSet;
//    }
//
//    public JSONArray getImage(String id) {
//        SQLiteDatabase myDataBase = rate.getReadableDatabase();
//        String raw = "SELECT * FROM " + rate.tbname_reserve_image
//                + " WHERE "+rate.res_img_trans+" = '"+id+"' LIMIT 3";
//        Cursor c = myDataBase.rawQuery(raw, null);
//        JSONArray resultSet = new JSONArray();
//        c.moveToFirst();
//        try {
//            while (!c.isAfterLast()) {
//                JSONObject js = new JSONObject();
//                byte[] image = c.getBlob(c.getColumnIndex(rate.res_img_image));
//                Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
//                byte[] bitmapdata = getBytesFromBitmap(bitmap);
//
//                // get the base 64 string
//                String imgString = Base64.encodeToString(bitmapdata, Base64.NO_WRAP);
//
//                js.put("image", imgString);
//                resultSet.put(js);
//                c.moveToNext();
//            }
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        c.close();
//       // Log.e("result_set", resultSet.toString());
//        return resultSet;
//    }
//
//    // convert from bitmap to byte array
//    public byte[] getBytesFromBitmap(Bitmap bitmap) {
//        ByteArrayOutputStream stream = new ByteArrayOutputStream();
//        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
//        return stream.toByteArray();
//    }
//
//    //send sync data
//    public void sendPost() {
//        Thread thread = new Thread(new Runnable() {
//            @Override
//            public void run() {
//
//                threadDelivery();
//
//            }
//        });
//
//        thread.start();
//    }
//
//    //update distribution upload status
//    public void updateDel(ArrayList<String> ids){
//        SQLiteDatabase db = gen.getWritableDatabase();
//        for (String id : ids) {
//            ContentValues cv = new ContentValues();
//            cv.put(gen.del_upds, "2");
//            db.update(gen.tbname_delivery, cv,gen.del_id+" = '"+id+"' AND "+
//                    gen.del_upds + " = '1'", null);
//            Log.e("upload", "uploaded delivery");
//        }
//        db.close();
//    }

}
