package com.example.admin.gpxbymodule;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class BoxRelease extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    HomeDatabase helper;
    GenDatabase gen;
    SQLiteDatabase db;
    String value;
    RatesDB rate;
    NavigationView navigationView;
    String trans_no;
    Spinner driver;
    TextView driverid;
    ArrayList<String> barcodes;
    Button add;
    IntentIntegrator scanIntegrator;
    int scan_code = 100;
    ListView lv;
    TextView tol;
    String dri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_box_release);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        gen = new GenDatabase(this);
        helper = new HomeDatabase(this);
        rate = new RatesDB(this);
        driver = (Spinner)findViewById(R.id.drivernames);
        add = (Button)findViewById(R.id.addbarc);
        lv = (ListView) findViewById(R.id.lv);
        tol = (TextView) findViewById(R.id.total);
        barcodes = new ArrayList<>();

        //transactions
        if (helper.logcount() != 0) {
            value = helper.getRole(helper.logcount());
        }

        //check getter and setter
        if (getTrans_no() != null){
            setTrans_no(getTrans_no());
        }else{
            setTrans_no(generateTransNo()); //set transaction number
        }
        try {
            Log.e("transaction_no", getTrans_no());
            populateDriverNames();
            addBoxnumber();
            customType();
        }catch (Exception e){
            Log.e("error", e.getMessage());
        }

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

    //populate listview
    public void customType(){
        try {
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1, barcodes);
            lv.setAdapter(adapter);
            tol.setText("Total : " + barcodes.size() + " pcs. ");
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                    final String ids = barcodes.get(position);
                    final AlertDialog.Builder builder = new AlertDialog.Builder(BoxRelease.this);
                    builder.setTitle("Delete this data ?")
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    barcodes.remove(barcodes.get(position).indexOf(ids));
                                    customType();
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
        }catch (Exception e){
            Log.e("error", e.getMessage());
        }
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
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(BoxRelease.this, new String[]
                    {Manifest.permission.CAMERA}, scan_code);
        }else{
            scanIntegrator = new IntentIntegrator(BoxRelease.this);
            scanIntegrator.setPrompt("Scan barcode");
            scanIntegrator.setBeepEnabled(true);
            scanIntegrator.setOrientationLocked(true);
            scanIntegrator.setCaptureActivity(CaptureActivityPortrait.class);
            scanIntegrator.initiateScan();

        }
    }

    public void populateDriverNames(){
        final ArrayList<LinearItem> name = gen.getSalesDriver("Sales Driver", helper.getBranch(helper.logcount() + ""));
        final LinearList list = new LinearList(this, name);
        driver.setAdapter(list);
        driver.setPrompt("Select sales driver");
        driver.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                driverid = (TextView) parent.findViewById(R.id.dataid);
                dri = driverid.getText().toString();
                        Log.e("driverid", driverid.getText().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                driverid = (TextView) parent.findViewById(R.id.dataid);
                dri = driverid.getText().toString();
                Log.e("driverid", driverid.getText().toString());
            }
        });
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

        NavAdapter ad = new NavAdapter(BoxRelease.this, listitem);
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
                        final AlertDialog.Builder builder = new AlertDialog.Builder(BoxRelease.this);
                        builder.setMessage("Confirm logout?")
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        helper.logout();

                                        startActivity(new Intent(BoxRelease.this, Login.class));
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
                bundle.putString("module", "Barcode Releasing");
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
                else if(value.equals("Warehouse Checker")) {
                    startActivity(new Intent(this, Checker_Inventory.class));
                    finish();
                }else if(value.equals("Partner Portal")) {
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
                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawer.closeDrawer(Gravity.START);
                break;
        }
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.box_release, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.savebarcode_release) {
            confirmSave();
        }else if (id == R.id.barcodelist){
            startActivity(new Intent(this, BoxReleaseList.class));
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


    //getter and setter
    public String getTrans_no() {
        return trans_no;
    }

    public void setTrans_no(String trans_no) {
        this.trans_no = trans_no;
    }

    public String generateTransNo(){
        String transNo = null;
        Date datetalaga = Calendar.getInstance().getTime();
        SimpleDateFormat writeDate = new SimpleDateFormat("yyyyMMddhhmmss");
        writeDate.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        String sa = writeDate.format(datetalaga);

        transNo = helper.logcount() + sa;
        return transNo;
    }

    //activity results
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == scan_code) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(), "camera permission granted", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        try{
//            if (requestCode == scan_code) {
                IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
                if (result.getContents() != null){
                    String bn = result.getContents();
                    if (checkifInventory(bn)) {
                        if (!barcodes.contains(bn)) {
                            barcodes.add(bn);
                            customType();
                        } else {
                            String x = "Barcode has been scanned, please try another.";
                            customToast(x);
                        }
                    }else{
                        String x = "Barcode is not in your inventory, please try another.";
                        customToast(x);
                    }
                    Log.e("boxnumber", bn);
                }
//            }

        }catch (Exception e){
            Log.e("error", e.getMessage());}
    }

    //custom alert
    public void customToast(String txt){
        Toast toast = new Toast(BoxRelease.this);
        toast.setDuration(Toast.LENGTH_SHORT);
        LayoutInflater inflater = BoxRelease.this.getLayoutInflater();
        View view = inflater.inflate(R.layout.toast, null);
        TextView t = (TextView)view.findViewById(R.id.toasttxt);
        t.setText(txt);
        toast.setView(view);
        toast.setGravity(Gravity.TOP | Gravity.RIGHT, 15, 50);
        Animation animation = AnimationUtils.loadAnimation(BoxRelease.this, R.anim.enterright);
        view.startAnimation(animation);
        toast.show();
    }

    //save transaction
    public void confirmSave(){
        try{
            if (barcodes.size() == 0){
                String c = "Please scan a barcode.";
                customToast(c);
            }else {
                String trans = this.getTrans_no();
                String driv = dri;
                String datetime = dateandtime();
                String by = helper.logcount() + "";

                for (String code : barcodes) {
                    rate.addBarcodeDistributionBoxnumber(trans, code, "0");
                    updateBarcodeInv(code);
                }
                rate.addBarcodeDistribution(trans, driv, datetime, by, "1", "0");

                String y = "Transaction has been saved, thank you.";
                customToast(y);
                barcodes.clear();
                startActivity(new Intent(this, BoxReleaseList.class));
                finish();
            }

        }catch (Exception e){}
    }

    public boolean checkifInventory(String bn){
        SQLiteDatabase db = rate.getReadableDatabase();
        String x = " SELECT * FROM "+rate.tbname_barcode_inventory
                +" WHERE "+rate.barcodeinv_boxnumber+" = '"+bn+"' AND "
                +rate.barcodeinv_status+" = '0'";
        Cursor c = db.rawQuery(x, null);
        if (c.getCount() != 0){
            return true;
        }else{
            return false;
        }
    }

    public String dateandtime(){
        Date datetalaga = Calendar.getInstance().getTime();
        SimpleDateFormat writeDate = new SimpleDateFormat("yyyy-MM-dd h:mm");
        writeDate.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        String findate = writeDate.format(datetalaga);

        return findate;
    }

    public void updateBarcodeInv(String bn){
        SQLiteDatabase db = rate.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(rate.barcodeinv_status, "1");
        db.update(rate.tbname_barcode_inventory, cv, rate.barcodeinv_boxnumber+" = '"+bn+"'", null);
        Log.e("upd_barcodeinv", bn);
        db.close();
    }

}
