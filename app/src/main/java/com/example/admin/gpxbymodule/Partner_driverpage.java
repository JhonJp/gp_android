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
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class Partner_driverpage extends AppCompatActivity {

    HomeDatabase helper;
    GenDatabase gen;
    RatesDB rates;
    BottomNavigationView botnav;
    ProgressDialog progressBar;
    LinearList adapter;
    ArrayList<String> delids;
    String drivername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.partner_driverpage);
        this.setTitle("Delivery");

        helper = new HomeDatabase(this);
        gen = new GenDatabase(this);
        rates = new RatesDB(this);
        botnav = (BottomNavigationView)findViewById(R.id.bottom_navigation);
        delids = new ArrayList<>();
        drivername = helper.getFullname(helper.logcount()+"").replace("  "," ");

        //
        loadFragment(new Partner_DeliveryPending());
        nav();

    }

    private boolean loadFragment(Fragment fragment) {
        //switching fragment
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.deliveryframe, fragment)
                    .commit();
            return true;
        }
        return false;
    }

    public void nav(){
        botnav.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @TargetApi(Build.VERSION_CODES.M)
                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public boolean onNavigationItemSelected(@NonNull final MenuItem item) {
                        int id = item.getItemId();
                        switch (id) {
                            case R.id.pending_delivery:
                                item.setChecked(true);
                                loadFragment(new Partner_DeliveryPending());
                                break;
                            case R.id.complete_delivery:
                                item.setChecked(true);
                                loadFragment(new Partner_DeliveryComplete());
                                break;
                            case R.id.others:
                                item.setChecked(true);
                                loadFragment(new Partner_deliveryOthers());
                                break;
                        }
                        return false;
                    }
                });
    }

    @Override
    public void onBackPressed() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm exit?");
        builder.setMessage("Please confirm if you want to close the app.");
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
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
        builder.setCancelable(false);
        builder.create().show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.partner_driver, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        try {
            int id = item.getItemId();
            if (id == R.id.drop_sync_send) {
                sendPost();
                loadingPost(getWindow().getDecorView().getRootView());
            }
            else if (id == R.id.drop_sync_get) {
                getPost();
                loadingGet(getWindow().getDecorView().getRootView());
            } else if (id == R.id.homelogout) {
                logoutWindow();
            }
        }catch (Exception e){}
        return super.onOptionsItemSelected(item);
    }

    public void logoutWindow(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm logout?");
        builder.setMessage("You are about to be logout in the application.");
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                helper.logout();
                startActivity(new Intent(Partner_driverpage.this, Login.class));
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
        progressBar.setMessage("Downloading data.....");
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

                    gen.addBooking( transaction_no, reservation_no, customer,
                            book_date, "2", "1", createdby, "2");

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
                        //add consignee booking
                        gen.addConsigneeBooking( consignee, boxtype, source_id, destination_id,
                                transaction_no, box_number, "2",hardport, bcont);
                        //update inventory
                        gen.updateInvBoxnumber("1", box_number, "2");

                        Log.e("boxbooking",
                                "boxtype: "+boxtype +
                                        " Origin: "+source_id +
                                        " Destination: "+destination_id);
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

    public void getPost() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                //get booking informations
                getBookings();
                getBoxRates();
                getCustomers();
                getsource("source");
                getsource("destination");

                //get your deliveries
                getDeliveries(drivername);

                //get delivered data
                getDeliveredData();
                //get delivery images
                getProof();
                //get delivery boxes
                getDeliveredBox();

            }
        });

        thread.start();
    }

    //box rates
    public void getBoxRates(){
        try {
            String resp = null;
            String link = helper.getUrl();
            String urlget = "http://"+link+"/api/boxrate/get.php";
            URL url = new URL(urlget);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            // read the response
            InputStream in = new BufferedInputStream(conn.getInputStream());
            resp = convertStreamToString(in);

            if (resp != null) {
                Log.e("Rates", "Rates: " + resp);
                JSONArray jsonArray = new JSONArray(resp);
                for(int i=0; i<jsonArray.length(); i++){
                    JSONObject json_data = jsonArray.getJSONObject(i);
                    String btypeid = json_data.getString("boxtype_id");
                    String cbm = json_data.getString("cbm");
                    String source = json_data.getString("source_id");
                    String dest = json_data.getString("destination_id");
                    String cur = json_data.getString("currency_id");
                    String am = json_data.getString("amount");
                    String rec = json_data.getString("recordstatus");

                    rates.addNewRates(btypeid, cbm, source, dest, cur, am, rec);

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

    // get your deliveries
    public void getDeliveries(String name){
        //thread rate
        try {
            String resp = null;
            String link = helper.getUrl();
            String urlget = "http://"+link+"/api/distribution/getbydrivername.php?name="+name;
            URL url = new URL(urlget);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            // read the response
            InputStream in = new BufferedInputStream(conn.getInputStream());
            resp = convertStreamToString(in);
            if (resp != null) {
                Log.e("Driverpage", " : " + resp);
                JSONArray jsonArray = new JSONArray(resp);
                for(int i=0; i<jsonArray.length(); i++){
                    JSONObject json_data = jsonArray.getJSONObject(i);
                    String[] bnum = json_data.getString("box_number").split(",");
                    for (String bn: bnum ){
                        rates.addDirectBox(bn, "0");
                    }
                }
            } else {
                Log.e("Error", "Couldn't get data from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String r = "Couldn't get data from server, trying again.";
                        customToast(r);
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getDeliveredData(){
        //thread rate
        try {
            String resp = null;
            String link = helper.getUrl();
            String urlget = "http://"+link+"/api/delivery/get.php?id="+helper.logcount();
            URL url = new URL(urlget);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            // read the response
            InputStream in = new BufferedInputStream(conn.getInputStream());
            resp = convertStreamToString(in);
            if (resp != null) {
                Log.e("deliveries", " : " + resp);
                JSONArray jsonArray = new JSONArray(resp);
                for(int i=0; i<jsonArray.length(); i++){
                    JSONObject json_data = jsonArray.getJSONObject(i);
                    String id = json_data.getString("id");
                    String transaction_no = json_data.getString("transaction_no");
                    String customer = json_data.getString("customer");
                    String createddate = json_data.getString("createddate");
                    String signature = json_data.getString("signature");
                    String receivedby = json_data.getString("receivedby");
                    String relationship = json_data.getString("relationship");
                    String remarks = json_data.getString("remarks");
                    String createdby = json_data.getString("createdby");
                    String base64String = "data:image/png;base64,"+signature;
                    String base64Image = base64String.split(",")[1];

                    byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
                    Log.e("base64tobyte", decodedString+"");
                    if (!checkDelId(id)) {
                        gen.addDelivery(id, transaction_no, customer,
                                createddate, decodedString, helper.logcount() + "", "1", "", remarks, receivedby, relationship);
                    }
                }

            } else {
                Log.e("Error", "Couldn't get data from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String r = "Couldn't get data from server, trying again.";
                        customToast(r);
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getDeliveredBox(){
        //thread rate
        try {
            String resp = null;
            String link = helper.getUrl();
            String urlget = "http://"+link+"/api/delivery/getbox.php";
            URL url = new URL(urlget);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            // read the response
            InputStream in = new BufferedInputStream(conn.getInputStream());
            resp = convertStreamToString(in);
            if (resp != null) {
                Log.e("deliverybox", " : " + resp);
                JSONArray jsonArray = new JSONArray(resp);
                for(int i=0; i<jsonArray.length(); i++){
                    JSONObject json_data = jsonArray.getJSONObject(i);
                    String id = json_data.getString("id");
                    String box_number = json_data.getString("box_number");
                    String transaction_no = json_data.getString("transaction_no");
                    String receiver = json_data.getString("receiver");
                    String origin = json_data.getString("origin");
                    String destination = json_data.getString("destination");
                    String delivery_id = json_data.getString("delivery_id");
                    String createddate = json_data.getString("createddate");
                    String status = json_data.getString("status");

                    if (!checkDelBox(box_number)){
                        String substat = null;
                        if (status.equals("2")){
                            substat = "1";
                        }
                        gen.addDeliveryBox(box_number, transaction_no, receiver, origin,
                                destination, delivery_id, status, substat, createddate);
                        if (checkDirects(box_number)){
                            rates.updateDirectBox(box_number);
                        }
                    }
                }

                if (conn.getResponseMessage().equals("OK")){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.dismiss();
                            final AlertDialog.Builder builder = new AlertDialog.Builder(Partner_driverpage.this);
                            builder.setTitle("Information confirmation")
                                    .setMessage("Data has been updated, proceed with upload data to server?")
                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            loadFragment(new Partner_DeliveryPending());
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
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String r = "Couldn't get data from server, trying again.";
                        customToast(r);
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getProof(){
        //thread rate
        try {
            String resp = null;
            String link = helper.getUrl();
            String urlget = "http://"+link+"/api/delivery/getproof.php";
            URL url = new URL(urlget);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            // read the response
            InputStream in = new BufferedInputStream(conn.getInputStream());
            resp = convertStreamToString(in);
            if (resp != null) {
                Log.e("proofs", " : " + resp);
                JSONArray jsonArray = new JSONArray(resp);
                for(int i=0; i<jsonArray.length(); i++){
                    JSONObject json_data = jsonArray.getJSONObject(i);
                    String id = json_data.getString("id");
                    String delivery_id = json_data.getString("delivery_id");
                    String images = json_data.getString("images");
                    String base64String = "data:image/png;base64,"+images;
                    String base64Image = base64String.split(",")[1];

                    byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);

                    rates.addReserveImage(delivery_id, decodedString);

                }

            } else {
                Log.e("Error", "Couldn't get data from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String r = "Couldn't get data from server, trying again.";
                        customToast(r);
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //get customers info
    public void getCustomers(){
        //START THREAD FOR CUSTOMERS
        try {
            String customers = null;
            String link = helper.getUrl();
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
                    String name = firstname + " "+ lastname;

                    gen.addCustomer(account_no,"", firstname, middlename, lastname, mobile,
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
        //END THREAD FOR CUSTOMERS
    }

    //get source and destinations
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
                    rates.addSD(id, name, type, stat);
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

//    syncing data reservations
//    connecting to internet

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

    public void threadDelivery(){
        SQLiteDatabase db = gen.getReadableDatabase();
        String q = " SELECT * FROM "+gen.tbname_delivery
                +" WHERE "+gen.del_createdby+" = '"+helper.logcount()+"' AND "+gen.del_upds+" = '1'";
        Cursor cx = db.rawQuery(q, null);
        if (cx.getCount() != 0) {
            try {
                String link = helper.getUrl();
                URL url = new URL("http://" + link + "/api/delivery/save.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);
                conn.setDoInput(true);
                JSONObject jsonParam = new JSONObject();
                db = gen.getReadableDatabase();
                JSONArray finalarray = new JSONArray();

                String query = " SELECT * FROM " +gen.tbname_delivery
                        +" WHERE "+gen.del_createdby+" = '"+helper.logcount()+"' AND "+gen.del_upds+" = '1'";
                Cursor c = db.rawQuery(query, null);
                c.moveToFirst();

                while (!c.isAfterLast()) {
                    JSONObject json = new JSONObject();
                    String id = c.getString(c.getColumnIndex(gen.del_id));
                    String trans = c.getString(c.getColumnIndex(gen.del_booking_no));
                    String cust = c.getString(c.getColumnIndex(gen.del_customer));
                    String date = c.getString(c.getColumnIndex(gen.del_createddate));
                    String recby = c.getString(c.getColumnIndex(gen.del_receivedby));
                    String relation = c.getString(c.getColumnIndex(gen.del_relationship));
                    byte[] sign = c.getBlob(c.getColumnIndex(gen.del_sign));
                    String by = c.getString(c.getColumnIndex(gen.del_createdby));
                    String remar = c.getString(c.getColumnIndex(gen.del_notes));
                    Bitmap bitmap = BitmapFactory.decodeByteArray(sign, 0, sign.length);
                    byte[] bitmapdata = getBytesFromBitmap(bitmap);

                    // get the base 64 string
                    String imgString = Base64.encodeToString(bitmapdata, Base64.NO_WRAP);
                    json.put("id", id);
                    json.put("transaction_no", trans);
                    json.put("customer", cust);
                    json.put("createddate", date);
                    json.put("signature", imgString);
                    json.put("receivedby", recby);
                    json.put("relationship", relation);
                    json.put("createdby", by);
                    json.put("remarks", remar);
                    json.put("delivery_box", getDeliveryBox(id));
                    json.put("delivery_image", getImage(id));
                    delids.add(id);
                    finalarray.put(json);
                    c.moveToNext();
                }
                c.close();
                jsonParam.accumulate("data", finalarray);
                Log.e("JSON_data", jsonParam.toString());
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
                                    = new AlertDialog.Builder(Partner_driverpage.this);
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
                    conn.disconnect();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.dismiss();
                            final AlertDialog.Builder builder
                                    = new AlertDialog.Builder(Partner_driverpage.this);
                            builder.setTitle("Information confirmation")
                                    .setMessage("Data upload has been successful, thank you.")
                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            updateDel(delids);
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
                    final AlertDialog.Builder builder
                            = new AlertDialog.Builder(Partner_driverpage.this);
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

    public JSONArray getDeliveryBox(String id) {
        SQLiteDatabase db = gen.getReadableDatabase();
        JSONArray resultSet = new JSONArray();
        String raw = " SELECT * FROM " + gen.tbname_delivery_box
                +" WHERE " +gen.del_box_deliveryid+" = '"+id+"'";
        Cursor c = db.rawQuery( raw, null);
        c.moveToFirst();
        try {
            while (!c.isAfterLast()) {

                int totalColumn = c.getColumnCount();
                JSONObject rowObject = new JSONObject();

                for (int i = 0; i < totalColumn; i++) {
                    if (c.getColumnName(i) != null) {
                        try {
                            if (c.getString(i) != null) {
                                Log.d("TAG_NAME", c.getString(i));
                                rowObject.put(c.getColumnName(i), c.getString(i));
                            } else {
                                rowObject.put(c.getColumnName(i), "");
                            }
                        } catch (Exception e) {
                            Log.d("TAG_NAME", e.getMessage());
                        }
                    }
                }
                resultSet.put(rowObject);
                c.moveToNext();
            }
        }catch (Exception e){
            Log.e("error", e.getMessage());
        }
        c.close();
        //Log.e("result set", resultSet.toString());
        return resultSet;
    }

    public JSONArray getImage(String id) {
        SQLiteDatabase myDataBase = rates.getReadableDatabase();
        String raw = "SELECT "+rates.res_img_image+" FROM " + rates.tbname_reserve_image
                + " WHERE "+rates.res_img_trans+" = '"+id+"'";
        Cursor c = myDataBase.rawQuery(raw, null);
        JSONArray resultSet = new JSONArray();
        c.moveToFirst();
        try {
            while (!c.isAfterLast()) {
                JSONObject js = new JSONObject();
                byte[] image = c.getBlob(c.getColumnIndex(rates.res_img_image));
                Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
                byte[] bitmapdata = getBytesFromBitmap(bitmap);

                // get the base 64 string
                String imgString = Base64.encodeToString(bitmapdata, Base64.NO_WRAP);

                js.put("image", imgString);

                resultSet.put(js);
                c.moveToNext();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        c.close();
       // Log.e("result_set", resultSet.toString());
        return resultSet;
    }

    // convert from bitmap to byte array
    public byte[] getBytesFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
        return stream.toByteArray();
    }

    //send sync data
    public void sendPost() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {

                threadDelivery();

            }
        });

        thread.start();
    }

    //update distribution upload status
    public void updateDel(ArrayList<String> ids){
        SQLiteDatabase db = gen.getWritableDatabase();
        for (String id : ids) {
            ContentValues cv = new ContentValues();
            cv.put(gen.del_upds, "2");
            db.update(gen.tbname_delivery, cv,gen.del_id+" = '"+id+"' AND "+
                    gen.del_upds + " = '1'", null);
            Log.e("upload", "uploaded delivery");
        }
        db.close();
    }

    public boolean checkDelId(String id){
        SQLiteDatabase db = gen.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM "+gen.tbname_delivery
        +" WHERE "+gen.del_id+" = '"+id+"'", null);
        if (c.getCount() == 0){
            return false;
        }else{
            return true;
        }
    }

    public boolean checkDelBox(String boxnumber){
        SQLiteDatabase db = gen.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM "+gen.tbname_delivery_box
        +" WHERE "+gen.del_box_boxnumber+" = '"+boxnumber+"'", null);
        if (c.getCount() == 0){
            return false;
        }else{
            return true;
        }
    }

    public boolean checkDirects(String boxnumber){
        SQLiteDatabase db = rates.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM "+rates.tbname_fordirects
        +" WHERE "+rates.direct_boxnumber+" = '"+boxnumber+"'", null);
        if (c.getCount() == 0){
            return false;
        }else{
            return true;
        }
    }

}
