package com.example.admin.gpxbymodule;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import android.widget.ListAdapter;
import android.widget.PopupWindow;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Login extends AppCompatActivity {

    Button submit;
    EditText username,pass;
    HomeDatabase helper;
    GenDatabase gen;
    String user, pwd;
    String retlink;
    PopupWindow pw;
    RatesDB rates;
    ProgressDialog progressBar;
    String emp_branch, employee_id,name, email,fullname, role;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        username = (EditText) findViewById(R.id.emailusername);
        pass = (EditText) findViewById(R.id.password);
        helper = new HomeDatabase(this);
        gen = new GenDatabase(this);
        rates = new RatesDB(this);
        pw = new PopupWindow(getApplicationContext());

        queryLink();
        insertRoles();

        if(countLogged()){
            SQLiteDatabase db = helper.getReadableDatabase();
            Cursor c = db.rawQuery(" SELECT * FROM "+helper.tbname_userrole, null);
            if (c.moveToNext()){
                String r = c.getString(c.getColumnIndex(helper.role_role));
                if (r.equals("Partner Driver")){
                    startActivity(new Intent(getApplicationContext(), Partner_driverpage.class));
                    finish();
                }else{
                    Log.e("employee_role", helper.getRole(helper.logcount())+"");
                    filterBranch(helper.getBranch(helper.logcount()+""),
                            helper.getRole(helper.logcount())+"");
                }
            }
            c.close();
            db.close();
        }

        submit = (Button) findViewById(R.id.signin);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    user = username.getText().toString();
                    pwd = pass.getText().toString();
                    if (helper.getUrl() == null) {
                        message();
                    } else {
                        network();
                    }
                }catch (Exception e){}
            }
        });
    }

    public void message(){
        String t = "Login error, please configure your web address.";
        loginerror(t);
    }

    public void insertRoles() {
        if (helper.countRole()) {
            SQLiteDatabase db = helper.getWritableDatabase();
            db.execSQL(" INSERT INTO `" + helper.tbname_tbroles + "`(`id`, `name`, `description`, `recordstatus`)" +
                    "VALUES ('1','Administrator','Administrator','1')," +
                    "('2','Sales Driver','Sales Driver','1')," +
                    "('3','OIC','OIC','1')," +
                    "('4','Warehouse Checker','Warehouse Checker','1')," +
                    "('5','Partner Portal','Partner Portal','1'),"+
                    "('6','Partner Driver','Partner Portal Driver','1')");
        }
    }

    @Override
    public void onBackPressed() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm exit?");
        builder.setMessage("Please confirm if you want to close the application.");
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) { dialog.dismiss();
                    }
                });
        // Create the AlertDialog object and return it
        builder.create().show();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.loginmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.editlink) {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(Login.this);
            LayoutInflater inflater = Login.this.getLayoutInflater();
            View d = inflater.inflate(R.layout.configlink,null);

            Button save = (Button)d.findViewById(R.id.savelink);
            Button cancel = (Button)d.findViewById(R.id.cancellink);
            final EditText link = (EditText)d.findViewById(R.id.linktype);
            link.setText(helper.getUrl());
            dialogBuilder.setView(d);
            final AlertDialog alertDialog = dialogBuilder.show();
            save.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SQLiteDatabase db = helper.getWritableDatabase();
                    Cursor x = db.rawQuery(" SELECT * FROM "+helper.tbname_configlink+" LIMIT 1", null);
                    if (x.getCount() != 0){
                        String u = helper.getUrl();
                        helper.deletelink(u);
                        helper.addLink(link.getText().toString());
                    }else{
                        helper.addLink(link.getText().toString());
                    }
                    alertDialog.dismiss();
                }
            });

            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.dismiss();
                }
            });
        }

        return super.onOptionsItemSelected(item);
    }

    //connecting to internet
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void network(){
        if (isNetworkAvailable() == true)
        {
            getPost();
            loadingPost(getWindow().getDecorView().getRootView());
        }
        else
        {
            String t = "Please check internet connection.";
            loginerror(t);
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

    public boolean countLogged(){
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery(" SELECT * FROM "+ helper.tbname_userrole, null);
        if (c.getCount() != 0){
            return true;
        }else {
            return false;
        }
    }

    public void filterBranch(String id, String role){
        SQLiteDatabase db = rates.getReadableDatabase();
        String x = " SELECT * FROM "+rates.tbname_branch
                +" WHERE "+rates.branch_id+" = '"+id+"'";
        Cursor c = db.rawQuery(x, null);
        if (c.moveToNext()){
            String partner = "Partner";
            String driver = "Sales Driver";
            String bname = c.getString(c.getColumnIndex(rates.branch_type));
            if (bname.toLowerCase().contains(partner.toLowerCase())){
                if (role.equals(driver)){
                    startActivity(new Intent(getApplicationContext(), Partner_driverpage.class));
                    finish();
                }else{
                    startActivity(new Intent(getApplicationContext(), Home.class));
                    finish();
                }
            }else{
                startActivity(new Intent(getApplicationContext(), Home.class));
                finish();
            }
        }
    }

    public void getPost(){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                //get employees
                threadEmployee();
                //get thread branch
                threadBranch();
                //thread delivery status
                threadDeliveryStatus();

                //thread for substatus
                getSubStat();

                //thread for login
                try {
                    String link = helper.getUrl();
                    String response = null;
                    String geturl = "http://"+link+"/api/employee/login.php?username=" + user + "&password=" + pwd;
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
                                name = json_data.getString("username");
                                email = json_data.getString("email");
                                fullname = json_data.getString("firstname")+" "+json_data.getString("lastname");
                                role = json_data.getString("role_id");
                                employee_id = json_data.getString("employee_id");
                                emp_branch = json_data.getString("branch");

                                if (!(role.equals("6"))) {
                                    if (conn.getResponseMessage().equals("OK")) {
                                        helper.loggedRole(employee_id, name, email, fullname, role, emp_branch);
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                progressBar.dismiss();
                                                startActivity(new Intent(getApplicationContext(), Home.class));
                                                finish();
                                            }
                                        });
                                    } else {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                progressBar.dismiss();
                                                String l = "Login error, please check your credentials and try again.";
                                                loginerror(l);
                                            }
                                        });
                                    }
                                }else{
                                    if (conn.getResponseMessage().equals("OK")) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                helper.loggedRole(employee_id, name, email, fullname, role, emp_branch);
                                                progressBar.dismiss();
                                                startActivity(new Intent(getApplicationContext(), Partner_driverpage.class));
                                                finish();
                                            }
                                        });
                                    } else {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                progressBar.dismiss();
                                                String l = "Login error, please check your credentials and try again.";
                                                loginerror(l);
                                            }
                                        });
                                    }
                                }
                            }
                            conn.disconnect();
                        } else {
                            Log.e("data", "Else error ");
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progressBar.dismiss();
                                    String l = "Login error, please check your credentials and try again.";
                                    loginerror(l);
                                }
                            });

                        }
                    } catch(Exception e){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.dismiss();
                            String t = "Login error, please check your web address.";
                            loginerror(t);
                        }
                    });
                    }
            }
        });

        thread.start();
    }

    public void queryLink(){
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor getl = db.rawQuery(" SELECT * FROM "+helper.tbname_configlink, null);
        if (getl.getCount() == 0){
            insertURLDemo();
        }else{
            getl.moveToNext();
            retlink = getl.getString(getl.getColumnIndex(helper.config_link));
            Log.e("link_given", retlink);
        }
    }

    public void insertURLDemo(){
        SQLiteDatabase db = helper.getWritableDatabase();
        String link = "10.10.1.57/gpxbeta";
        //String link = "beta.gpexpresscargo.com";
        helper.addLink(link);
        Log.e("link_add", link);
    }

    public void loginerror(String x){
        try {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(Login.this);
            LayoutInflater inflater = Login.this.getLayoutInflater();
            View d = inflater.inflate(R.layout.loginerror, null);
            Button save = (Button) d.findViewById(R.id.savelink);
            TextView t = (TextView) d.findViewById(R.id.textView4);
            t.setText(x);
            dialogBuilder.setView(d);
            final AlertDialog alertDialog = dialogBuilder.show();
            save.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.dismiss();
                }
            });
        }catch (Exception e){}
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

    public void threadBranch(){
                //START THREAD FOR branch
                try {
                    String resp = null;
                    String link = helper.getUrl();
                    String urlget = "http://"+link+"/api/branch/get.php";
                    URL url = new URL(urlget);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    InputStream in = new BufferedInputStream(conn.getInputStream());
                    resp = convertStreamToString(in);

                    if (resp != null) {
                        Log.e("branch", ": " + resp);
                            JSONArray jsonArray = new JSONArray(resp);

                            for(int i=0; i<jsonArray.length(); i++){

                                JSONObject json_data = jsonArray.getJSONObject(i);

                                String id = json_data.getString("id");
                                String name = json_data.getString("name");
                                String address = json_data.getString("address");
                                String stat = json_data.getString("recordstatus");
                                String type = json_data.getString("type");

                                rates.addBranch(id, name, address, type, stat);
                                gen.addBranch(id, name, address, type, stat);
                            }

                    } else {
                        Log.e("Error", "Couldn't get data from server.");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(),
                                        "Couldn't get data from server.",
                                        Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //END THREAD FOR branch
    }

    public void threadDeliveryStatus(){
                //START THREAD FOR branch
                try {
                    String resp = null;
                    String link = helper.getUrl();
                    String urlget = "http://"+link+"/api/delivery/getstatus.php";
                    URL url = new URL(urlget);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    InputStream in = new BufferedInputStream(conn.getInputStream());
                    resp = convertStreamToString(in);

                    if (resp != null) {
                        Log.e("delivery_status", ": " + resp);
                            JSONArray jsonArray = new JSONArray(resp);

                            for(int i=0; i<jsonArray.length(); i++){

                                JSONObject json_data = jsonArray.getJSONObject(i);

                                String id = json_data.getString("id");
                                String name = json_data.getString("name");
                                String stat = json_data.getString("recordstatus");

                                rates.addDeliveryStatus(id, name, stat);
                            }

                    } else {
                        Log.e("Error", "Couldn't get data from server.");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(),
                                        "Couldn't get data from server.",
                                        Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //END THREAD FOR branch
    }

    public void threadEmployee(){
        //START THREAD FOR employee
        try {
            String resp = null;
            String link = helper.getUrl();
            String urlget = "http://"+link+"/api/employee/get.php";
            URL url = new URL(urlget);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            // read the response
            InputStream in = new BufferedInputStream(conn.getInputStream());
            resp = convertStreamToString(in);

            if (resp != null) {

                Log.e("employee", " : " + resp);

                JSONArray jsonArray = new JSONArray(resp);

                for(int i=0; i<jsonArray.length(); i++){

                    JSONObject json_data = jsonArray.getJSONObject(i);

                    String id = json_data.getString("id");
                    String fname = json_data.getString("firstname");
                    String mid = json_data.getString("middlename");
                    String last = json_data.getString("lastname");
                    String mail = json_data.getString("email");
                    String mob = json_data.getString("mobile");
                    String ph = json_data.getString("phone");
                    String gend = json_data.getString("gender");
                    String bday = json_data.getString("birthdate");
                    String post = json_data.getString("role");
                    String hnum = json_data.getString("house_number_street");
                    String brgy = json_data.getString("barangay");
                    String ct = json_data.getString("city");
                    String branch = json_data.getString("branch");

                    gen.addEmployee(id, fname, mid, last, mail,
                            mob, ph, gend, bday, post, hnum, brgy, ct, branch);
                }

            } else {
                Log.e("Error", "Couldn't get data from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String r = "Couldn't get data from server, trying again....";
                        getPost();
                        customToast(r);
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //END THREAD FOR employee
    }

    public void getSubStat(){
        try {
            String resp = null;
            String link = helper.getUrl();
            String series = "http://"+link+"/api/delivery/getsubstat.php";
            URL url = new URL(series);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            // read the response
            InputStream in = new BufferedInputStream(conn.getInputStream());
            resp = convertStreamToString(in);

            if (resp != null) {

                Log.e("SubStat", " : " + resp);

                JSONArray jsonArray = new JSONArray(resp);

                for(int i=0; i<jsonArray.length(); i++){

                    JSONObject json_data = jsonArray.getJSONObject(i);
                    String id = json_data.getString("id");
                    String name = json_data.getString("name");
                    String statid = json_data.getString("status_id");
                    rates.addDeliverySubStatus(id, name, statid);
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
            Log.e("Error", "error: " + e.getMessage());
        }
    }

    //filter user sales driver by branch or location
    //prevent sales driver of partner portal to login because the user has no module views
    public boolean checkSalesDriver(String empid, String branch){
        SQLiteDatabase db = gen.getReadableDatabase();
        boolean check = false;
        Cursor c = db.rawQuery(" SELECT * FROM "+gen.tbname_branch
                +" LEFT JOIN "+gen.tbname_employee
                +" WHERE "+gen.tbname_employee+"."+gen.emp_id+" = '"+empid+"' AND "
                +gen.tbname_branch+"."+gen.branch_id+" = '"+branch+"'", null);
        if (c.moveToNext()){
            String prt = "Partner";
            String rolecomp = "Sales Driver";
            String ty = c.getString(c.getColumnIndex(gen.branch_type));
            String role = c.getString(c.getColumnIndex(gen.emp_post));
            if ((role.toLowerCase().equals(rolecomp.toLowerCase()))
                     && (ty.toLowerCase().contains(prt.toLowerCase()))){
                check = false;
            }else{
                check = true;
            }
        }
        return check;
    }

}
