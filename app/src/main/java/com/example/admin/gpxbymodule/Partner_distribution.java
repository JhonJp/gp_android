package com.example.admin.gpxbymodule;

import android.annotation.TargetApi;
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
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Base64;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class Partner_distribution extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    NavigationView navigationView;
    HomeDatabase helper;
    GenDatabase gen;
    RatesDB rate;
    String value;
    String trans;
    String disttype;
    String disttrucknumber;
    String remarks;
    String distname;
    String drivername;
    String transactionnumhub;
    BottomNavigationView bottomnav;
    ProgressDialog progressBar;
    SQLiteDatabase db;
    ArrayList<String> boxnums;
    ArrayList<String> booktrans;
    String frag;
    String etanow;
    String etdnow;
    String mode;

    public String getEtanow() {
        return etanow;
    }

    public void setEtanow(String etanow) {
        this.etanow = etanow;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_partner_distribution);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        helper = new HomeDatabase(this);
        gen = new GenDatabase(this);
        rate = new RatesDB(this);
        boxnums = new ArrayList<>();
        try {
            if (helper.logcount() != 0) {
                value = helper.getRole(helper.logcount());
                Log.e("role ", value);
            }
            generateTransNo();
            loadFragment(new Partdist_address());
            bottomnav = (BottomNavigationView)findViewById(R.id.distnav);
            nav();

        }catch (Exception e){}

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

    public void sidenavMain(){
        ListView lv=(ListView)findViewById(R.id.optionsList);//initialization of listview
        final ArrayList<HomeList> listitem = helper.getData(value);

        NavAdapter ad = new NavAdapter(Partner_distribution.this, listitem);
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
                        final AlertDialog.Builder builder = new AlertDialog.Builder(Partner_distribution.this);
                        builder.setMessage("Confirm logout?")
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {

                                        helper.logout();

                                        startActivity(new Intent(Partner_distribution.this, Login.class));
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
                        startActivity(new Intent(Partner_distribution.this, Home.class));
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
        try {
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            switch (data) {
                case "Acceptance":
                    if (value.equals("OIC")) {
                        startActivity(new Intent(this, Acceptance.class));
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
                        drawer.closeDrawer(Gravity.START);
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
                    bundle.putString("module", "Distribution");
                    //Add the bundle to the intent
                    i.putExtras(bundle);
                    startActivity(i);
                    finish();
                    break;
                case "Transactions":
                    if (value.equals("OIC")) {
                        startActivity(new Intent(this, Oic_Transactions.class));
                        finish();
                    } else if (value.equals("Sales Driver")) {
                        startActivity(new Intent(this, Driver_Transactions.class));
                        finish();
                    } else if (value.equals("Warehouse Checker")) {
                        startActivity(new Intent(this, Checker_transactions.class));
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
        }catch (Exception e){}
    }

    public String generateTransNo(){
        String transNo = null;
        if (this.getTrans() != null){
            this.setTrans(this.getTrans());
        }else {
            Date datetalaga = Calendar.getInstance().getTime();
            SimpleDateFormat writeDate = new SimpleDateFormat("yyyyMMddhhmmss");
            writeDate.setTimeZone(TimeZone.getTimeZone("GMT+8"));
            String sa = writeDate.format(datetalaga);

            transNo = "PARTD-" + helper.logcount() + sa;
            this.setTrans(transNo);
        }
        return transNo;
    }

    public String getTrans() {
        return trans;
    }

    public void setTrans(String trans) {
        this.trans = trans;
    }

    public void nav(){
        bottomnav.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @TargetApi(Build.VERSION_CODES.M)
                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.transactioninfo:
                                item.setChecked(true);
                                loadFragment(new Partdist_address());
                                break;
                            case R.id.distributioninfo:
                                item.setChecked(true);
                                loadFragment(new Distributionlist());
                                break;
                            default:
                                loadFragment(new Partdist_transactioninfo());
                                break;
                        }
                        return false;
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.distribution, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.distprev){
            loadFragment(new Partdist_address());
            bottomnav.setSelectedItemId(R.id.transactioninfo);
        }
        else if(id == R.id.distnext){
            loadFragment(new Partdist_transactioninfo());
        }else if (id == R.id.syncdist) {
            network();
        }else if (id ==  R.id.passincident){
            Intent i = new Intent(this, Incident.class);
            Bundle bundle = new Bundle();
            bundle.putString("module", "Distribution");
            //Add the bundle to the intent
            i.putExtras(bundle);
            startActivity(i);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    public String getDisttype() {
        return disttype;
    }

    public void setDisttype(String disttype) {
        this.disttype = disttype;
    }

    private boolean loadFragment(Fragment fragment) {
        //switching fragment
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.destinationframe, fragment, "fragment")
                    .addToBackStack("fragment")
                    .commit();
            return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        int id = bottomnav.getSelectedItemId();
        if (id == R.id.transactioninfo ){
            final AlertDialog.Builder builder = new AlertDialog.Builder(Partner_distribution.this);
            builder.setTitle("Cancel transaction?");
            builder.setMessage("All changes will not be saved, please confirm to cancel the transaction.");
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    Log.e("boxnums", getBoxnums().toString());
                    startActivity(new Intent(getApplicationContext(), Home.class));
                    finish();
                }
            }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });
            // Create the AlertDialog object and show it
            builder.create().show();
        }else{
            startActivity(new Intent(getApplicationContext(), Home.class));
            finish();
        }
    }

    public void deleteItemBox(String box){
        SQLiteDatabase db = rate.getWritableDatabase();
        db.delete(rate.tbname_part_distribution_box,
                rate.partdist_box_boxnumber+" = '"+box+"' AND "
                +rate.partdist_box_stat+" = '0'", null);
        Log.e("deletedbox", box);
        db.close();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public String getDisttrucknumber() {
        return disttrucknumber;
    }

    public void setDisttrucknumber(String disttrucknumber) {
        this.disttrucknumber = disttrucknumber;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getDistname() {
        return distname;
    }

    public void setDistname(String distname) {
        this.distname = distname;
    }

    public void network(){
        if (isNetworkAvailable()){
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

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void threadDistribution(){
        try {
            db = rate.getReadableDatabase();
            String q = " SELECT * FROM " + rate.tbname_part_distribution
                    +" WHERE "+rate.partdist_uploadstat +" = '1' AND "+rate.partdist_acceptstat+" = '1'";
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
            db = rate.getReadableDatabase();

            JSONArray finalarray = new JSONArray();
            JSONArray reserve = null, img = null;
            String trans = null;
                db = rate.getReadableDatabase();
                String query = " SELECT * FROM " + rate.tbname_part_distribution
                        +" WHERE "+rate.partdist_uploadstat +" = '1' AND "
                        +rate.partdist_acceptstat+" = '1'";
                Cursor c = db.rawQuery(query, null);
                c.moveToFirst();
                while (!c.isAfterLast()) {
                    JSONObject json = new JSONObject();
                    trans = c.getString(c.getColumnIndex(rate.partdist_transactionnumber));
                    String mode = c.getString(c.getColumnIndex(rate.partdist_mode));
                    String type = c.getString(c.getColumnIndex(rate.partdist_type));
                    String typename = c.getString(c.getColumnIndex(rate.partdist_typename));
                    String driver = c.getString(c.getColumnIndex(rate.partdist_drivername));
                    String truck = c.getString(c.getColumnIndex(rate.partdist_trucknum));
                    String remarks = c.getString(c.getColumnIndex(rate.partdist_remarks));
                    String d = c.getString(c.getColumnIndex(rate.partdist_createdate));
                    String by = c.getString(c.getColumnIndex(rate.partdist_createby));
                    String eta = c.getString(c.getColumnIndex(rate.partdist_eta));
                    String etd = c.getString(c.getColumnIndex(rate.partdist_etd));
                    String accstat = c.getString(c.getColumnIndex(rate.partdist_acceptstat));
                    json.put("id", trans);
                    json.put("mode_of_shipment", mode);
                    json.put("type", type);
                    json.put("name", typename);
                    json.put("driver_name", driver);
                    json.put("truck_no", truck);
                    json.put("remarks", remarks);
                    json.put("created_date", d);
                    json.put("created_by", by);
                    json.put("eta", eta);
                    json.put("etd", etd);
                    json.put("acceptance_status", accstat);
                    reserve = rate.getDistributionsBox(trans);
                    img = getDistributionImage(trans);
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
                                    = new AlertDialog.Builder(Partner_distribution.this);
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
                                    new AlertDialog.Builder(Partner_distribution.this);
                            builder.setTitle("Information confirmation")
                                    .setMessage("Data upload has been successful, thank you.")
                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            updateDist("2");
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
                                = new AlertDialog.Builder(Partner_distribution.this);
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

    //send sync data
    public void sendPost() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                threadDistribution();
            }
        });
        thread.start();
    }

    public void customToast(String txt){
        Toast toast = new Toast(this);
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

    public JSONArray getDistributionImage(String id) {
        SQLiteDatabase myDataBase = rate.getReadableDatabase();
        String raw = "SELECT * FROM " + rate.tbname_dstimages
                + " WHERE "+rate.distimage_trans+" = '"+id+"'";
        Cursor c = myDataBase.rawQuery(raw, null);
        JSONArray resultSet = new JSONArray();
        c.moveToFirst();
        try {
            while (!c.isAfterLast()) {
                JSONObject js = new JSONObject();
                String ids = c.getString(c.getColumnIndex(rate.distimage_id));
                String tr = c.getString(c.getColumnIndex(rate.distimage_trans));
                byte[] im = c.getBlob(c.getColumnIndex(rate.distimage_image));
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

    // convert from bitmap to byte array
    public byte[] getBytesFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        return stream.toByteArray();
    }

    public ArrayList<String> getBoxnums() {
        return boxnums;
    }

    public void setBoxnums(ArrayList<String> boxnums) {
        this.boxnums = boxnums;
    }

    public void updateBnumToRevert(ArrayList<String> bnumbers){
        SQLiteDatabase db = gen.getWritableDatabase();
        for (String bn : bnumbers) {
            ContentValues cv = new ContentValues();
            cv.put(gen.partinv_stat, "0");
            db.update(gen.tbname_partner_inventory, cv,
                    gen.partinv_boxnumber + " = '" + bn + "'", null);
            //deleteItemBox(bn);
        }
        db.close();
    }

    public String getFrag() {
        return frag;
    }

    public void setFrag(String frag) {
        this.frag = frag;
    }

    //update distribution upload status
    public void updateDist(String stat){
        SQLiteDatabase db = rate.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(rate.partdist_uploadstat, stat);
        db.update(rate.tbname_part_distribution, cv,
                rate.partdist_uploadstat+" = '1'", null);
        Log.e("upload", "uploaded dist");
        db.close();
    }

    public ArrayList<String> getBooktrans() {
        return booktrans;
    }

    public void setBooktrans(ArrayList<String> booktrans) {
        this.booktrans = booktrans;
    }

    public String getDest(String book, String bnum){
        String ac = null;
        SQLiteDatabase db = gen.getReadableDatabase();
        Cursor x = db.rawQuery(" SELECT * FROM "+gen.tbname_booking_consignee_box
                +" WHERE "+gen.book_con_transaction_no+" = '"+book+"' AND "+gen.book_con_box_number+" = '"+bnum+"'", null);
        if (x.moveToNext()){
            ac = x.getString(x.getColumnIndex(gen.book_con_destination));
        }
        return ac;
    }

    public String getBookingTrans(String box){
        String ac = null;
        SQLiteDatabase db = gen.getReadableDatabase();
        Cursor x = db.rawQuery(" SELECT * FROM "+gen.tbname_booking_consignee_box
                +" LEFT JOIN "+gen.tbname_booking+" ON "+gen.tbname_booking_consignee_box
                +"."+gen.book_con_transaction_no+" = "+gen.tbname_booking+"."+gen.book_transaction_no
                +" WHERE "+gen.tbname_booking_consignee_box+"."+gen.book_con_box_number+" = '"+box+"'", null);
        if (x.moveToNext()){
            ac = x.getString(x.getColumnIndex(gen.book_transaction_no));
        }
        return ac;
    }

    public String getDrivername() {
        return drivername;
    }

    public void setDrivername(String drivername) {
        this.drivername = drivername;
    }

    public String getTransactionnumhub() {
        return transactionnumhub;
    }

    public void setTransactionnumhub(String transactionnumhub) {
        this.transactionnumhub = transactionnumhub;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getEtdnow() {
        return etdnow;
    }

    public void setEtdnow(String etdnow) {
        this.etdnow = etdnow;
    }


}
