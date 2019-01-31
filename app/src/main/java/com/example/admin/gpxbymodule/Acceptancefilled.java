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
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

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

public class Acceptancefilled extends Fragment {

    HomeDatabase helper;
    GenDatabase gen;
    RatesDB rate;
    SQLiteDatabase db;
    String value;
    ListView lv;
    Button add;
    TextView tot, hideid;
    int error = 0;
    String trans;
    ProgressDialog progressBar;
    EditText container;
    int requestcode = 100;
    IntentIntegrator scanIntegrator;
    NavigationView navigationView;
    Spinner drivername, ware;
    ArrayList<LinearItem> name;
    ArrayList<String> boxnumbers;
    int CAMERA_REQUEST = 1;
    String selectedwarehouse;
    String[] warehouses;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup cont,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.filledacceptance, null);

        helper = new HomeDatabase(getContext());
        gen = new GenDatabase(getContext());
        rate = new RatesDB(getContext());

        lv = (ListView)v.findViewById(R.id.lv);
        container = (EditText)v.findViewById(R.id.accept_container_input);
        add = (Button)v.findViewById(R.id.accept_add);
        drivername = (Spinner)v.findViewById(R.id.drivers);
        ware = (Spinner)v.findViewById(R.id.idwarehouse);
        tot = (TextView)v.findViewById(R.id.total);
        boxnumbers = new ArrayList<>();

        if (helper.logcount() != 0){
            value = helper.getRole(helper.logcount());
            Log.e("role ", value);
        }

        generateTransNo();
        customtype();
        addBoxnumber();
        scrolllist();
        namespinner();
        warehousespinner();
        Log.e("warehouseaccept","acceptfilled");
        return v;
    }

    public void customtype(){
        final ArrayList<String> result = boxnumbers;
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_list_item_1, result);
        lv.setAdapter(adapter);

        tot.setText(Html.fromHtml("<small>Total : </small>")+""+result.size()+" box(s) ");
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                final String ids = result.get(position);
                final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Delete this data ?")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                result.remove(result.get(position).indexOf(ids));
                                customtype();
                                dialog.dismiss();
                            }
                        }).setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }});
                // Create the AlertDialog object and show it
                builder.create().show();
            }
        });
    }

    public String generateTransNo(){
        String transNo = null;
        if (this.getTrans() != null){
            transNo = this.getTrans();
        }else {
            Date datetalaga = Calendar.getInstance().getTime();
            SimpleDateFormat writeDate = new SimpleDateFormat("yyyyMMddhhmmss");
            writeDate.setTimeZone(TimeZone.getTimeZone("GMT+8"));
            String sa = writeDate.format(datetalaga);

            transNo = "GPACC-" + helper.logcount() + sa;
            this.setTrans(transNo);
        }
        return transNo;
    }

    public void warehousespinner(){
        try {
            warehouses = rate.getWarehouseName(helper.getBranch(helper.logcount() + ""));
            ArrayAdapter<String> warehouseadapter =
                    new ArrayAdapter<>(getContext(), R.layout.spinneritem,
                            warehouses);
            ware.setAdapter(warehouseadapter);
            warehouseadapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
            ware.setPrompt("Select warehouse");
            ware.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    selectedwarehouse = ware.getSelectedItem().toString();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    selectedwarehouse = ware.getSelectedItem().toString();
                }
            });
        }catch(Exception e){}

    }

    public void namespinner(){
        try {
            name = gen.getSalesDriver("Sales Driver", helper.getBranch(helper.logcount()+""));
            LinearList list = new LinearList(getContext(), name);
            drivername.setAdapter(list);
            drivername.setPrompt("Select sales driver");
            drivername.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    hideid = (TextView) view.findViewById(R.id.dataid);
                    Log.e("driverid", hideid.getText().toString());
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    hideid = (TextView) parent.findViewById(R.id.dataid);
                    Log.e("selectedid", hideid.getText().toString());
                }
            });
        }catch (Exception e){}
    }

    public String getTrans() {
        return trans;
    }

    public void setTrans(String trans) {
        this.trans = trans;
    }

    public void addBoxnumber(){
        try {
            add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    scanpermit();
                }
            });
        }catch (Exception e){}
    }

    public void scanpermit(){
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[] {Manifest.permission.CAMERA}, requestcode);
        }else{
            scanIntegrator = IntentIntegrator.forSupportFragment(Acceptancefilled.this);
            scanIntegrator.setPrompt("Scan barcode");
            scanIntegrator.setBeepEnabled(true);
            scanIntegrator.setOrientationLocked(true);
            scanIntegrator.setCaptureActivity(CaptureActivityPortrait.class);
            scanIntegrator.initiateScan();
        }
    }

    public void scrolllist(){
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

    public void setNameMail(){
        View header = navigationView.getHeaderView(0);
        TextView user = (TextView)header.findViewById(R.id.yourname);
        TextView mail = (TextView)header.findViewById(R.id.yourmail);
        user.setText(helper.getFullname(helper.logcount()+""));
        mail.setText(helper.getRole(helper.logcount())+" / branch "+helper.getBranch(""+helper.logcount()));
    }

    public String datelang(){
        Date datetalaga = Calendar.getInstance().getTime();
        SimpleDateFormat writeDate = new SimpleDateFormat("yyyy-MM-dd");
        writeDate.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        String findate = writeDate.format(datetalaga);

        return findate;
    }

    public String returntime(){
        Date datetalaga = Calendar.getInstance().getTime();
        SimpleDateFormat writeDate = new SimpleDateFormat("HH:mm:ss");
        writeDate.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        String time = writeDate.format(datetalaga);

        return time;
    }

    @Override
    public void onActivityResult(int rc, int resultCode, Intent data){
        super.onActivityResult(rc, resultCode, data);
        try {
            //if (value.equals("Warehouse Checker")){
                conditionForCheckerAcceptance(rc, resultCode, data);
//            }else if (value.equals("Partner Portal")){
//
//            }
        }catch (Exception e){}
    }

    public void conditionForCheckerAcceptance(int r, int code, Intent d){
        IntentResult result = IntentIntegrator.parseActivityResult(r, code, d);
        if (result.getContents() != null) {
            String bn = result.getContents();
            if (checkBoxId(bn)) {
                if (checkIfExist(bn)){
                        if (!boxnumbers.contains(bn)) {
                            boxnumbers.add(bn);
                            customtype();
                        }else{
                            String t = "Barcode has ben scanned, please try another.";
                            customToast(t);
                        }
                } else {
                    String t = "Barcode has ben used in the previous transactions, please try again.";
                    customToast(t);
                }
            } else {
                String t = "Barcode is invalid, please try another.";
                customToast(t);
            }
        } else {
            super.onActivityResult(r, code, d);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,@NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == requestcode) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getContext(), "camera permission granted", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getContext(), "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    public int sum(){
        db = gen.getReadableDatabase();
        Cursor cur = db.rawQuery(" SELECT * FROM "+gen.tbname_accept_boxes+
                " WHERE "+gen.acc_box_transactionno+" = '"+getTrans()+"' AND "+gen.acc_box_stat
                +" = '1'", null);

        Log.e("trans", getTrans());
        return cur.getCount();
    }

    public String getBox(String barcode){
        String name = null;
        SQLiteDatabase db = gen.getReadableDatabase();
        String query = "SELECT * FROM "+gen.tbname_booking_consignee_box+" LEFT JOIN "+gen.tbname_boxes
                +" ON "+gen.tbname_boxes+"."+gen.box_name+" = "+gen.tbname_booking_consignee_box+"."+gen.book_con_boxtype
                +" WHERE "+gen.book_con_box_number+" = '"+barcode+"'";
        Cursor c = db.rawQuery(query, null);
        if (c.getCount() != 0){
            c.moveToNext();
            name = c.getString(c.getColumnIndex(gen.book_con_box_id));
            Log.e("boxname", name);
        }
        return name;
    }

    public boolean checkBoxId(String barcode){
        SQLiteDatabase db = gen.getReadableDatabase();
        String query = " SELECT * FROM "+gen.tbname_booking_consignee_box+" WHERE "
                +gen.book_con_box_number+" = '"+barcode+"'";
        Cursor c = db.rawQuery(query, null);
        if (c.getCount() != 0){
            return true;
        }else{
            return false;
        }
    }

    public boolean checkIfExist(String barcode){
        SQLiteDatabase db = gen.getReadableDatabase();
        String q = " SELECT * FROM "+gen.tbname_accept_boxes
                +" WHERE "+gen.acc_box_boxnumber+" = '"+barcode+"' AND "
                +gen.acc_box_stat+" = '2'";
        Cursor xc = db.rawQuery(q, null);
        if (xc.getCount() == 0 ){
            return true;
        }
        return false;
    }

    private int getWarehouseID(String name){
        int id = 0;
        SQLiteDatabase db = rate.getReadableDatabase();
        Cursor x = db.rawQuery(" SELECT * FROM "+rate.tbname_warehouse
                +" WHERE "+rate.ware_name+" = '"+name+"'", null);
        if (x.moveToNext()){
            id = x.getInt(x.getColumnIndex(rate.ware_id));
        }
        return id;
    }

    public void customToast(String txt){
        Toast toast = new Toast(getContext());
        toast.setDuration(Toast.LENGTH_SHORT);
        LayoutInflater inflater = this.getLayoutInflater();
        View view = inflater.inflate(R.layout.toast, null);
        TextView t = (TextView)view.findViewById(R.id.toasttxt);
        t.setText(txt);
        toast.setView(view);
        toast.setGravity(Gravity.TOP | Gravity.RIGHT, 15, 50);
        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.enterright);
        view.startAnimation(animation);
        toast.show();
    }

    public void saveAcceptance(){
        String trans = getTrans();
        String driver = hideid.getText().toString();
        String warehouse = getWarehouseID(selectedwarehouse)+"";
        String containername = container.getText().toString();
        String stat = "1";
        if (containername.equals("")){
            String t = "Save failed, container number is empty.";
            customToast(t);
        }else {
            for (String bn : boxnumbers){
                gen.addAcceptanceBoxnumber( trans, getBox(bn),
                               bn, "2");
            }
            gen.addNewAcceptance(trans, driver, warehouse,
                    containername, datelang()+" "+returntime(),
                    helper.logcount()+"",  stat, "1");

            gen.addTransactions("Acceptance", "" + helper.logcount(),
                    "New acceptance " + trans, datelang(), returntime());
            container.setText("");
            setTrans(null);
        }
    }

    //syncing data reservations
    //connecting to internet
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager)getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void network(){
        if (isNetworkAvailable()== true){
            loadingPost(getView());
        }else
        {
            Toast.makeText(getContext(),"Please check internet connection.", Toast.LENGTH_LONG).show();
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
        // Create a Handler instance on the main thread
        final Handler handler = new Handler();

// Create and start a new Thread
        new Thread(new Runnable() {
            public void run() {
                try{
                    sendPost();
                    Thread.sleep(5000);
                }
                catch (Exception e) { } // Just catch the InterruptedException

                handler.post(new Runnable() {
                    public void run() {
                        progressBar.dismiss();
                        final AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                        builder.setTitle("Send data")
                                .setMessage("Data has been sync, thank you.")
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
        }).start();
    }

    public void loadingPostSave(final View v){
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
                    Thread.sleep(5000);
                }
                catch (Exception e) { } // Just catch the InterruptedException

                handler.post(new Runnable() {
                    public void run() {
                        progressBar.dismiss();
                        final AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                        builder.setTitle("Saved data")
                                .setMessage("Data has been saved, thank you.")
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        Intent i = new Intent(getContext(), Acceptancelist.class);
                                        //Create the bundle to pass
                                        Bundle bundle = new Bundle();
                                        //Add your data from getFactualResults method to bundle
                                        bundle.putString("type", "1");
                                        i.putExtras(bundle);
                                        startActivity(i);
                                        getActivity().finish();
                                    }
                                });
                        // Create the AlertDialog object and show it
                        builder.create().show();
                    }
                });
            }
        }).start();
    }

    public void threadAcceptance(){
        db = gen.getReadableDatabase();
        String query = " SELECT * FROM "+gen.tbname_check_acceptance+" WHERE "+gen.accept_uploadstat+" = '1'";
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
                //os.writeBytes(URLEncoder.encode(jsonParam.toString(), "UTF-8"));
                os.writeBytes(jsonParam.toString());

                os.flush();
                os.close();

                Log.i("STATUS", String.valueOf(conn.getResponseCode()));
                Log.i("MSG", conn.getResponseMessage());
            } catch (Exception e) {
                e.printStackTrace();
            }
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
        String raw = "SELECT * FROM " + gen.tbname_check_acceptance+" WHERE "+gen.accept_uploadstat+" = '1'";
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

                js.put("id", id);
                js.put("transaction_no", trans);
                js.put("salesdriver_id", driver);
                js.put("warehouse_id", wareh);
                js.put("truck_no", tru);
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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle("Acceptance");
        setHasOptionsMenu(true);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.saveoicacceptance).setVisible(false);
        menu.findItem(R.id.savecheckacceptance).setVisible(true);
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.savecheckacceptance) {
            saveAcceptance();
            loadingPostSave(getView());
        }else if (id == R.id.acceptancelist) {
            Intent i = new Intent(getContext(), Acceptancelist.class);
            //Create the bundle to pass
            Bundle bundle = new Bundle();
            //Add your data from getFactualResults method to bundle
            bundle.putString("type", "1");
            i.putExtras(bundle);
            startActivity(i);
            getActivity().finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void updateAllboxnumbers(String trans){
        SQLiteDatabase db = gen.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(gen.acc_box_stat, "2");
        db.update(gen.tbname_accept_boxes, cv, gen.acc_box_transactionno+" = '"+trans+"'", null);
        Log.e("statupdate", trans);
        db.close();
    }

    public void delExistStatOne(String boxnum){
        SQLiteDatabase db = gen.getWritableDatabase();
        db.delete(gen.tbname_accept_boxes,
                gen.acc_box_boxnumber+" = "+boxnum, null);
        Log.e("deleted", boxnum);
        db.close();
    }

}
