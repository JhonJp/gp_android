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
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class Distribution extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    BottomNavigationView bottomnav;
    HomeDatabase helper;
    RatesDB rate;
    SQLiteDatabase db;
    GenDatabase gen;
    String role;
    String trans;
    String disttype;
    String distname;
    String disttrucknumber;
    String warehouse;
    String remarks;
    NavigationView navigationView;
    ProgressDialog progressBar;
    int inventorysize;
    ArrayList<String> boxnumbers;
    ArrayList<String> inventoryIDS;
    ArrayList<String> boxIDS;
    ArrayList<String> transnums;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_distribution);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        helper = new HomeDatabase(this);
        rate = new RatesDB(this);
        gen = new GenDatabase(this);
        transnums = new ArrayList<>();

     if (helper.logcount() != 0){
            role = helper.getRole(helper.logcount());
            Log.e("role ", role);
        }

        loadFragment(new Destination_transactions());

        generateTransNo();

        bottomnav = (BottomNavigationView)findViewById(R.id.distnav);
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
        final ArrayList<HomeList> listitem = helper.getData(role);
        NavAdapter ad = new NavAdapter(this, listitem);
        lv.setAdapter(ad);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String data = listitem.get(position).getSubitem();
                select(data);
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
                        final AlertDialog.Builder builder = new AlertDialog.Builder(Distribution.this);
                        builder.setMessage("Confirm logout?")
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {

                                        helper.logout();

                                        startActivity(new Intent(Distribution.this, Login.class));
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
                    drawer.closeDrawer(Gravity.START);
                }else if (role.equals("Warehouse Checker")){
                    drawer.closeDrawer(Gravity.START);
                }else if(role.equals("Partner Portal")){
                    startActivity(new Intent(this, Partner_distribution.class));
                    finish();
                }
                break;
            case "Remittance":
                if (role.equals("OIC")){
                    startActivity(new Intent(this, Remittancetooic.class));
                    finish();
                }else if (role.equals("Sales Driver")){
                    startActivity(new Intent(this, Remittancetooic.class));
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
                    startActivity(new Intent(this, Oic_inventory.class));
                    finish();
                }
                else if (role.equals("Sales Driver")){
                    startActivity(new Intent(this, Driver_Inventory.class));
                    finish();
                }
                else if(role.equals("Warehouse Checker")){
                    startActivity(new Intent(this, Checker_Inventory.class));
                    finish();
                }
                else if(role.equals("Partner Portal")){
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

    @Override
    public void onBackPressed() {
        int id = bottomnav.getSelectedItemId();
        if (id == R.id.transactioninfo ) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Cancel transaction?")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            if (getInventoryIDS() != null){
                                for (String inds : getInventoryIDS()){
                                    //addQuan(inds, "1");
                                }
                                getBoxIDS().clear();
                                getInventoryIDS().clear();
                                getBoxnumbers().clear();
                            }
                            startActivity(new Intent(getApplicationContext(), Home.class));
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
        }else{
            startActivity(new Intent(getApplicationContext(), Home.class));
            finish();
        }
    }

    private boolean loadFragment(Fragment fragment) {
        //switching fragment
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.destinationframe, fragment)
                    .commit();
            return true;
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.distribution, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.distprev){
            loadFragment(new Destination_transactions());
            bottomnav.setSelectedItemId(R.id.transactioninfo);
        }
        else if(id == R.id.distnext){
            Log.e("frag", "Destinations");
            loadFragment(new Destination_info());
        }else if (id == R.id.syncdist){
           network();
        }
        else if (id ==  R.id.passincident){
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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
                                loadFragment(new Destination_transactions());

                                Log.d("reservation: ","Transactions fragment loaded");
                                break;
                            case R.id.distributioninfo:
                                item.setChecked(true);
                                loadFragment(new Distributionlist());
                                break;
                            default:
                                loadFragment(new Destination_transactions());
                                break;
                        }
                        return false;
                    }
                });
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

    public String generateTransNo(){
        String transNo = null;
        if (this.getTrans() != null){
            this.setTrans(this.getTrans());
        }else {
            Date datetalaga = Calendar.getInstance().getTime();
            SimpleDateFormat writeDate = new SimpleDateFormat("yyyyMMddhhmmss");
            writeDate.setTimeZone(TimeZone.getTimeZone("GMT+8"));
            String sa = writeDate.format(datetalaga);

            transNo = "GPDIST-" + helper.logcount() + sa;
            this.setTrans(transNo);
        }
        return transNo;
    }

    public String getBoxId(String barcode){
        String name = null;
        SQLiteDatabase db = gen.getReadableDatabase();
        String query = "SELECT * FROM "+gen.tbname_boxes+" LEFT JOIN "
                +gen.tbname_barcode+" ON "+gen.tbname_boxes+"."+gen.box_id+" = "+ gen.tbname_barcode+"."+gen.barcode_boxtype
                +" WHERE '"+barcode+"' BETWEEN "+gen.barcode_series_start+" AND "+gen.barcode_series_end+"";
        Cursor c = db.rawQuery(query, null);
        if (c.getCount() != 0){
            c.moveToNext();
            name = c.getString(c.getColumnIndex(gen.box_id));
        }
        // Log.e("query", query);
        return name;
    }

    //quantity updates
    public void addQuan(String id) {
        try {
            int finq = 0;
            SQLiteDatabase db = gen.getWritableDatabase();
            Cursor c = db.rawQuery(" SELECT * FROM " + gen.tb_acceptance
                    + " WHERE " + gen.acc_id + " = '" + id + "'", null);
            if (c.moveToNext()) {
                String acid = c.getString(c.getColumnIndex(gen.acc_id));
                String acq = c.getString(c.getColumnIndex(gen.acc_quantity));
                finq = (Integer.parseInt(acq) + 1);
                updateQAcceptance(acid, finq+"");
            }
            c.close();
            db.close();
        }catch (Exception e){}
    }

    public void updateQAcceptance(String id, String qu){
        SQLiteDatabase db = gen.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(gen.acc_quantity, qu);
        db.update(gen.tb_acceptance, cv,
                gen.acc_id+" = '"+id+"'", null);
        Log.e("updated_acceptance", "id: "+id+", quantity: "+qu);
        db.close();
    }

    @Override
    protected void onDestroy() {
        if (getInventoryIDS() != null){
            for (String inds : getInventoryIDS()){
                addQuan(inds);
            }
            getBoxIDS().clear();
            getInventoryIDS().clear();
            getBoxnumbers().clear();
        }
        super.onDestroy();
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

    //getters and setters
    public int getInventorysize() {
        return inventorysize;
    }

    public void setInventorysize(int inventorysize) {
        this.inventorysize = inventorysize;
    }

    public String getTrans() {
        return trans;
    }

    public void setTrans(String trans) {
        this.trans = trans;
    }

    public String getDisttype() {
        return disttype;
    }

    public void setDisttype(String disttype) {
        this.disttype = disttype;
    }

    public String getDistname() {
        return distname;
    }

    public void setDistname(String distname) {
        this.distname = distname;
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

    public String getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(String warehouse) {
        this.warehouse = warehouse;
    }

    public void threadDistribution(){
        //THREAD FOR distribution API
        try {
            db = gen.getReadableDatabase();
            String q = " SELECT * FROM " + gen.tbname_tempDist
                    + " WHERE " + gen.temp_createby + " = '" + helper.logcount() + "' AND "
                    +gen.temp_uploadstat+" = '1'";
            Cursor x = db.rawQuery(q, null);
            if (x.getCount() != 0 ) {
                String link = helper.getUrl();
                URL url = new URL("http://" + link + "/api/distribution/save.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);
                conn.setDoInput(true);

                JSONObject jsonParam = new JSONObject();
                db = gen.getReadableDatabase();

                JSONArray finalarray = new JSONArray();
                JSONArray reserve = null, img = null;

                String query = " SELECT * FROM " + gen.tbname_tempDist
                        + " WHERE " + gen.temp_createby + " = '" + helper.logcount() + "' AND "
                        +gen.temp_uploadstat+" = '1'";
                Cursor c = db.rawQuery(query, null);
                String trans = null;
                c.moveToFirst();
                while (!c.isAfterLast()) {
                    JSONObject json = new JSONObject();
                    trans = c.getString(c.getColumnIndex(gen.temp_transactionnumber));
                    String type = c.getString(c.getColumnIndex(gen.temp_type));
                    String typename = c.getString(c.getColumnIndex(gen.temp_typename));
                    String truck = c.getString(c.getColumnIndex(gen.temp_trucknum));
                    String remarks = c.getString(c.getColumnIndex(gen.temp_remarks));
                    String d = c.getString(c.getColumnIndex(gen.temp_createdate));
                    String by = c.getString(c.getColumnIndex(gen.temp_createby));
                    String accstat = c.getString(c.getColumnIndex(gen.temp_acceptstat));
                    json.put("id", trans);
                    json.put("type", type);
                    json.put("mode_of_shipment", "");
                    json.put("name", typename);
                    json.put("driver_name", "");
                    json.put("truck_no", truck);
                    json.put("remarks", remarks);
                    json.put("created_date", d);
                    json.put("created_by", by);
                    json.put("acceptance_status", accstat);
                    reserve = gen.getDistributionsBox(trans);
                    img = getDistributionImage(trans);
                    json.put("distribution_box", reserve);
                    json.put("distribution_image", img);
                    transnums.add(trans);
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
                            final AlertDialog.Builder builder = new AlertDialog.Builder(Distribution.this);
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
                            final AlertDialog.Builder builder = new AlertDialog.Builder(Distribution.this);
                            builder.setTitle("Information confirmation")
                                    .setMessage("Data upload has been successful, thank you.")
                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            updateDist(transnums);
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
                        final AlertDialog.Builder builder = new AlertDialog.Builder(Distribution.this);
                        builder.setTitle("Information notification")
                                .setMessage("You dont have enough data to be uploaded yet, please try again later.")
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        //END THREAD DISTRIBUTION API
    }

    //update distribution upload status
    public void updateDist(ArrayList<String> list){
        for (String tr: list) {
            SQLiteDatabase db = gen.getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put(gen.temp_uploadstat, "2");
            db.update(gen.tbname_tempDist, cv,
                    gen.temp_transactionnumber + " = '"+tr+"'", null);
            Log.e("upload", "uploaded dist");
            db.close();
        }
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
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
        return stream.toByteArray();
    }

    public ArrayList<String> getBoxnumbers() {
        return boxnumbers;
    }

    public void setBoxnumbers(ArrayList<String> boxnumbers) {
        this.boxnumbers = boxnumbers;
    }

    public void sendPost() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {

                threadDistribution();

            }
        });

        thread.start();
    }

    public ArrayList<String> getInventoryIDS() {
        return inventoryIDS;
    }

    public void setInventoryIDS(ArrayList<String> inventoryIDS) {
        this.inventoryIDS = inventoryIDS;
    }

    public ArrayList<String> getBoxIDS() {
        return boxIDS;
    }

    public void setBoxIDS(ArrayList<String> boxIDS) {
        this.boxIDS = boxIDS;
    }


}
