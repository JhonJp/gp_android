package com.example.admin.gpxbymodule;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Splashscreen extends AppCompatActivity {

    RatesDB rates;
    HomeDatabase helper;
    GenDatabase gen;
    ProgressBar progress;
    SQLiteDatabase db;
    TextView loadmessage;
    int progressStat = 0;
    ImageView logo;
    int code = 100;
    Handler handler = new Handler();

    public static final int MULTIPLE_PERMISSIONS = 10;

    String[] permissions= new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.INTERNET,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.splash);

        if (checkPermissions()){
            //  permissions  granted.
        }

        helper = new HomeDatabase(this);
        gen = new GenDatabase(this);
        rates = new RatesDB(this);

        progress = (ProgressBar) findViewById(R.id.progressBar);
        loadmessage = (TextView) findViewById(R.id.messageload);
        logo = (ImageView) findViewById(R.id.imagelogo);
        progress.getProgressDrawable().setColorFilter(
                Color.GREEN, android.graphics.PorterDuff.Mode.SRC_IN);
        db = rates.getReadableDatabase();
        final Cursor c = db.rawQuery(" SELECT * FROM " + rates.tbname_city, null);

        //temporary
//        if (c.getCount() == 0) {
//            defaultLoad();
//        } else {
//            defaultLoad();
//        }

        if (c.getCount() == 0) {
            myTask task = new myTask();
            task.execute();
        } else {
            defaultLoad();
        }

//        if (c.getCount() < 42000) {
//            myTask task = new myTask();
//            task.execute();
//        } else {
//            defaultLoad();
//        }

        c.close();
        db.close();

    }

    //connecting to internet
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public boolean network() {
        if (isNetworkAvailable() == true) {
            String in = "Connected to the internet.";
            customToast(in);
            return true;
        } else {
            String in = "Please check internet connection.";
            customToast(in);
            return false;
        }
    }

    private String readFromFileProvince() {
        String ret = "";
        try {
            InputStream inputStream = getResources().openRawResource(R.raw.provinces);
            //Log.e("readFromFile", "File path: " + inputStream + "/regions.json");
            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();
                while ((receiveString = bufferedReader.readLine()) != null) {
                    stringBuilder.append(receiveString);
                }
                inputStream.close();
                ret = stringBuilder.toString();
            }
        } catch (FileNotFoundException e) {
            Log.e("readFromFile", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("readFromFile", "Can not read file: " + e.toString());
        }
        return ret;
    }

    public void readJsonProvince(final String json) {
        try {
            JSONArray jsonArray = new JSONArray(json);
            progress.setMax(jsonArray.length());
            for (int i = 0; i < jsonArray.length(); i++) {
                progress.setProgress(i);
                JSONObject json_data = jsonArray.getJSONObject(i);
                String provDesc = json_data.getString("provDesc");
                String regCode = json_data.getString("regCode");
                String provCode = json_data.getString("provCode");
                String destination_id = json_data.getString("destination_id");
                String hardport = json_data.getString("hardport");

                rates.addProvince(provDesc, provCode, regCode, destination_id, hardport);
                Log.e("progress", progress.getProgress()+" || "+provDesc);

            }

        } catch (final JSONException e) {
            Log.e("data", "Json parsing error: " + e.getMessage());

        }
    }

    private String readFromFileCity() {
        String ret = "";
        try {
            InputStream inputStream = getResources().openRawResource(R.raw.cities);
            //Log.e("readFromFile", "File path: " + inputStream + "/regions.json");
            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();
                while ((receiveString = bufferedReader.readLine()) != null) {
                    stringBuilder.append(receiveString);
                }
                inputStream.close();
                ret = stringBuilder.toString();
            }
        } catch (FileNotFoundException e) {
            Log.e("readFromFile", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("readFromFile", "Can not read file: " + e.toString());
        }
        return ret;
    }

    public void readJsonCity(final String json){
        try {
            JSONArray jsonArray = new JSONArray(json);
            progress.setMax(jsonArray.length() - 1);
            for (int i = 0; i < jsonArray.length(); i++) {
                progress.setProgress(i);
                JSONObject json_data = jsonArray.getJSONObject(i);
                String name = json_data.getString("citymunDesc");
                String code = json_data.getString("provCode");
                String citycode = json_data.getString("citymunCode");

                rates.addCity( name, code, citycode);

                Log.e("progress", progress.getProgress()+"");

                if (name.equals("TUBAJON")) {
                    Log.e("finished", "task finished city");
                    startActivity(new Intent(this, Login.class));
                    finish();
                    //readJsonBrgy(readFromFileBrgy());
                }
            }

        } catch (final JSONException e) {
            Log.e("data", "Json parsing error: " + e.getMessage());
        }
    }

    private String readFromFileBrgy() {
        String ret = "";
        try {
            InputStream inputStream = getResources().openRawResource(R.raw.brgy);
            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();
                while ((receiveString = bufferedReader.readLine()) != null) {
                    stringBuilder.append(receiveString);
                }
                inputStream.close();
                ret = stringBuilder.toString();
            }
        } catch (FileNotFoundException e) {
            Log.e("readFromFile", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("readFromFile", "Can not read file: " + e.toString());
        }
        return ret;
    }

    public void readJsonBrgy(final String json) {
        try {
            JSONArray jsonArray = new JSONArray(json);
            progress.setMax(jsonArray.length() - 1);
            for (int i = 0; i < jsonArray.length(); i++) {
                progress.setProgress(i);
                JSONObject json_data = jsonArray.getJSONObject(i);

                String code = json_data.getString("brgyCode");
                String name = json_data.getString("brgyDesc");
                String procode = json_data.getString("provCode");
                String citycode = json_data.getString("citymunCode");

                rates.addBrgy(code, name, procode, citycode);

                db = rates.getReadableDatabase();
                Cursor c = db.rawQuery(" SELECT * FROM " + rates.tbname_brgy, null);
                Log.e("progress", c.getCount()+"");
                c.close();
                if (progress.getProgress() == progress.getMax()) {
                    startActivity(new Intent(this, Login.class));
                    finish();
                    Log.e("finished", "task finished brgy");
                }
            }

        } catch (final JSONException e) {
            Log.e("data", "Json parsing error: " + e.getMessage());

        }

    }

    public void defaultLoad(){
        progress.setMax(100);
        new Thread(new Runnable() {
            public void run() {
                while (progressStat < 100) {
                    progressStat += 5;
                    handler.post(new Runnable() {
                        public void run() {
                            progress.setProgress(progressStat);
                            loadmessage.setText(
                                    Html.fromHtml("Loading <small><i>("
                                            +progressStat+"%)</i></small>"));
                            if (progress.getProgress() == 100 ){
                                Log.e("finished", "task finished default");
                                startActivity(new Intent(getApplicationContext(), Login.class));
                                finish();
                            }
                        }
                    });
                    try {
                        // Sleep for 200 milliseconds.
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private  boolean checkPermissions() {
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String p:permissions) {
            result = ContextCompat.checkSelfPermission(getApplicationContext(),p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),MULTIPLE_PERMISSIONS );
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissionsList[], int[] grantResults) {
        switch (requestCode) {
            case MULTIPLE_PERMISSIONS:{
                if (grantResults.length > 0) {
                    String permissionsDenied = "";
                    for (String per : permissionsList) {
                        if(grantResults[0] == PackageManager.PERMISSION_DENIED){
                            permissionsDenied += "\n" + per;

                        }

                    }
                    // Show permissionsDenied
//                    updateViews();
                }
                return;
            }
        }
    }

    public class myTask extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            Log.e("preexe", this.getStatus().toString());
        }

        @Override
        protected String doInBackground(String... params) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    readJsonProvince(readFromFileProvince());
                    readJsonCity(readFromFileCity());
                }
            }).start();

            return "done";
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
        }

    }

    public void customToast(String txt){
        Toast toast = new Toast(getApplicationContext());
        toast.setDuration(Toast.LENGTH_SHORT);
        LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(
                getApplicationContext().LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.toast, null);
        TextView t = (TextView)view.findViewById(R.id.toasttxt);
        t.setText(txt);
        toast.setView(view);
        toast.setGravity(Gravity.TOP | Gravity.RIGHT, 15, 15);
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.enterright);
        view.startAnimation(animation);
        toast.show();
    }

    public void getprovinces(){
        try {
            String resp = null;
            String link = helper.getUrl();
            String urlget = "http://10.10.1.57/gpxbeta/api/address/getprovince.php";
            URL url = new URL(urlget);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            // read the response
            InputStream in = new BufferedInputStream(conn.getInputStream());
            resp = convertStreamToString(in);

            if (resp != null) {
                Log.e("Provinces", "" + resp);
                JSONArray jsonArray = new JSONArray(resp);
                progress.setMax(jsonArray.length() - 1);
                for(int i=0; i<jsonArray.length(); i++){
                    progress.setProgress(i);
                    JSONObject json_data = jsonArray.getJSONObject(i);
                    String provDesc = json_data.getString("provDesc");
                    String regCode = json_data.getString("regCode");
                    String provCode = json_data.getString("provCode");
                    String destination_id = json_data.getString("destination_id");
                    String hardport = json_data.getString("amount");

                    rates.addProvince(provDesc, provCode, regCode, destination_id, hardport);
                    if (progress.getProgress() == (jsonArray.length() - 1)) {
                        readJsonCity(readFromFileCity());
                    }
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

}


