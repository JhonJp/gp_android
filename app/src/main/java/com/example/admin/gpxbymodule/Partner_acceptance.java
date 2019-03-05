package com.example.admin.gpxbymodule;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class Partner_acceptance extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    HomeDatabase helper;
    String value;
    NavigationView navigationView;
    GenDatabase gen;
    RatesDB rate;
    ProgressDialog progressBar;
    String link, trans;
    ArrayList<String> boxnumbers;
    String[] warehouses;
    String selectedwarehouse;
    Spinner ware;
    AutoCompleteTextView drivername;
    int requestcode = 100;
    IntentIntegrator scanIntegrator;
    TextView tot, ybranch;
    Button add;
    private int SETTINGS_ACTION = 1;
    ListView lv;
    EditText container, transactionnum;
    LinearLayout dummy;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        preference();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_partner_acceptance);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        helper = new HomeDatabase(this);
        gen = new GenDatabase(this);
        rate = new RatesDB(this);
        tot = (TextView)findViewById(R.id.total);
        ybranch = (TextView)findViewById(R.id.yourbranchinput);
        dummy = (LinearLayout) findViewById(R.id.dummyfocus);
        container = (EditText)findViewById(R.id.accept_container_input);
        transactionnum = (EditText)findViewById(R.id.accept_transactionnum);
        lv = (ListView)findViewById(R.id.lv);
        drivername = (AutoCompleteTextView) findViewById(R.id.drivername_input);
        ware = (Spinner)findViewById(R.id.idwarehouse);
        add = (Button)findViewById(R.id.accept_add);
        boxnumbers = new ArrayList<>();
        dummy.requestFocus();
        if (helper.logcount() != 0){
            value = helper.getRole(helper.logcount());
            Log.e("role ", value);
        }
        ybranch.setText(getBranch());

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        generateTransNo();
        warehousespinner();
        customtype();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        addBoxnumber();
        sidenavMain();
        subMenu();
        setNameMail();
        scrolllist();
        checkTransNull();
        autoNameDriver();
        transactionnum.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus){
                    scanpermitTruck();
                }
            }
        });
        transactionnum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanpermitTruck();
            }
        });

    }

    public void checkTransNull(){
        String tr = transactionnum.getText().toString();
        if (!tr.equals("")){
            container.setText(autoTruck(tr));
            drivername.setText(autoTruckDriver(tr));
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

    public void sidenavMain(){
        ListView lv=(ListView)findViewById(R.id.optionsList);//initialization of listview

        final ArrayList<HomeList> listitem = helper.getData(value);

        NavAdapter ad = new NavAdapter(Partner_acceptance.this, listitem);
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
                        final AlertDialog.Builder builder = new AlertDialog.Builder(Partner_acceptance.this);
                        builder.setMessage("Confirm logout?")
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {

                                        helper.logout();

                                        startActivity(new Intent(Partner_acceptance.this, Login.class));
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
                    drawer.closeDrawer(Gravity.START);
                }
                break;
            case "Distribution":
                if (value.equals("OIC")){
                    startActivity(new Intent(this, Distribution.class));
                    finish();
                }else if (value.equals("Warehouse Checker")){
                    startActivity(new Intent(this, Distribution.class));
                    finish();
                }else if(value.equals("Partner Portal")){
                    startActivity(new Intent(this, Partner_distribution.class));
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
                bundle.putString("module", "Acceptance");
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

    public String getBranch(){
        String bname = "";
        SQLiteDatabase db = rate.getReadableDatabase();
        Cursor x = db.rawQuery(" SELECT * FROM "+rate.tbname_branch
                +" WHERE "+rate.branch_id+" = '"+helper.getBranch(""+helper.logcount())+"'", null);
        if (x.moveToNext()){
            bname = x.getString(x.getColumnIndex(rate.branch_name));
        }
        x.close();
        return bname;
    }

    @Override
    public void onBackPressed() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Cancel transaction?");
        builder.setMessage("Please confirm if you want to cancel the transactions that has been made.");
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (boxnumbers.size() != 0 ){
                    for (String n : boxnumbers){
                        deleteBoxnumberInventory(n);
                    }
                }
                startActivity(new Intent(getApplicationContext(), Home.class));
                finish();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    public void customtype(){
        final ArrayList<String> result = boxnumbers;
        ArrayAdapter<String> adapter
                = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, result);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                final String ids = result.get(position);
                final AlertDialog.Builder builder
                        = new AlertDialog.Builder(Partner_acceptance.this);
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

        tot.setText(Html.fromHtml("<small>Total : </small>")+""+result.size()+" box(s) ");
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
            ActivityCompat.requestPermissions(Partner_acceptance.this,
                    new String[] {Manifest.permission.CAMERA}, requestcode);
        }else{
            scanIntegrator = new IntentIntegrator(Partner_acceptance.this);
            scanIntegrator.setPrompt("Scan barcode");
            scanIntegrator.setBeepEnabled(true);
            scanIntegrator.setOrientationLocked(true);
            scanIntegrator.setCaptureActivity(CaptureActivityPortrait.class);
            scanIntegrator.initiateScan();
        }
    }

    public void scanpermitTruck(){
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(Partner_acceptance.this,
                    new String[] {Manifest.permission.CAMERA}, requestcode);
        }else{
            scanIntegrator = new IntentIntegrator(Partner_acceptance.this);
            scanIntegrator.setPrompt("Scan");
            scanIntegrator.setBeepEnabled(true);
            scanIntegrator.setOrientationLocked(true);
            scanIntegrator.setCaptureActivity(CaptureActivityPortrait.class);
            scanIntegrator.initiateScan();
        }
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

            transNo = "PARTACC-" + helper.logcount() + sa;
            this.setTrans(transNo);
        }
        return transNo;
    }

    public void warehousespinner(){
        try {
            warehouses = getHubBranches("Partner - Hub");
            ArrayAdapter<String> warehouseadapter =
                    new ArrayAdapter<>(getApplicationContext(), R.layout.spinneritem,
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

    public String[] getHubBranches(String type) {
        SQLiteDatabase db = rate.getReadableDatabase();
        ArrayList<String> numbers = new ArrayList<String>();
        Cursor c = db.rawQuery(" SELECT * FROM " + rate.tbname_branch
                +" WHERE "+rate.branch_type+" = '"+type+"'", null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            numbers.add(c.getString(c.getColumnIndex(rate.branch_name)));
            c.moveToNext();
        }
        return numbers.toArray(new String[numbers.size()]);
    }

    public String getTrans() {
        return trans;
    }

    public void setTrans(String trans) {
        this.trans = trans;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.partner_acceptance, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.savepart_acc) {
            if (saveAcceptance() == 1){
                String h = "Transaction has been saved successfully, thank you.";
                customToast(h);
                recreate();
            }else{
                String h = "Save failed, please complete the form thank you.";
                customToast(h);
            }
        }
        else if (id ==  R.id.passincident){
            Intent i = new Intent(this, Incident.class);
            Bundle bundle = new Bundle();
            bundle.putString("module", "Acceptance");
            //Add the bundle to the intent
            i.putExtras(bundle);
            startActivity(i);
            finish();
        }
        else if (id ==  R.id.acceptancelist){
            Intent i = new Intent(getApplicationContext(), Acceptancelist.class);
            Bundle bundle = new Bundle();
            bundle.putString("type", "1");
            i.putExtras(bundle);
            startActivity(i);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onActivityResult(int rc, int resultCode, Intent data){
        super.onActivityResult(rc, resultCode, data);
        try {
            if (rc == SETTINGS_ACTION) {
                if (resultCode == Preferences.RESULT_CODE_THEME_UPDATED) {
                    finish();
                    startActivity(getIntent());
                    return;
                }
            }else {
                conditionForCheckerAcceptance(rc, resultCode, data);
            }
        }catch (Exception e){}
    }

    public void conditionForCheckerAcceptance(int r, int code, Intent d){
        IntentResult result = IntentIntegrator.parseActivityResult(r, code, d);
        if (result.getContents() != null) {
            String bn = result.getContents();
            if (checkFocusTrans()){
                transactionnum.setText(bn);
                dummy.requestFocus();
                transactionnum.clearFocus();
                checkTransNull();
            }else {
                if (checkNumFromDist(bn)) {
                    if (getBranch().equals(getDestinationOfBox(bn))) {

                            if (!boxnumbers.contains(bn)) {
                                boxnumbers.add(bn);
                                //addBoxnum(bn);
                                customtype();
                            }

                    } else {
                        String h = "Box number destination error, please try another boxnumber.";
                        customToast(h);
                    }
                } else if(checkNumFromUndelivered(bn)){
                    if (getBranch().equals(getDestinationOfBox(bn))) {

                            if (!boxnumbers.contains(bn)) {
                                boxnumbers.add(bn);
                                //addBoxnum(bn);
                                customtype();
                            }

                    } else {
                        String h = "Box number destination error, please try another boxnumber.";
                        customToast(h);
                    }
                }
                else {
                    String h = "Box number is not in distribution records, please try again.";
                    customToast(h);
                }
            }

        } else {
            dummy.requestFocus();
            super.onActivityResult(r, code, d);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,@NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == requestcode) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(), "camera permission granted", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void customToast(String txt){
        Toast toast = new Toast(Partner_acceptance.this);
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

    public void saveInventory(String bn){
        SQLiteDatabase db = gen.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(gen.partinv_boxnumber, bn);
        cv.put(gen.partinv_boxtype_fillempty, "1");
        cv.put(gen.partinv_boxtype, getBox(bn));
        cv.put(gen.partinv_stat, "0");
        db.insert(gen.tbname_partner_inventory, null, cv);
        db.close();
    }

    public void updateInventory(String bn){
        SQLiteDatabase db = gen.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(gen.partinv_boxnumber, bn);
        cv.put(gen.partinv_boxtype_fillempty, "1");
        cv.put(gen.partinv_boxtype, getBox(bn));
        cv.put(gen.partinv_stat, "0");
        db.update(gen.tbname_partner_inventory, cv, gen.partinv_boxnumber+" = '"+bn+"'", null);
        db.close();
    }

    public void updateBnum(String bn){
        SQLiteDatabase db = gen.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(gen.partinv_stat, "1");
        db.update(gen.tbname_partner_inventory, cv,
                gen.partinv_boxnumber+" = '"+bn+"' AND "+gen.partinv_stat+" = '0'", null);
        db.close();
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

    public void deleteOneBoxnumber(String id) {
        SQLiteDatabase db = gen.getWritableDatabase();
        db.delete(gen.tbname_partner_inventory, gen.partinv_id + " = " + id, null);
        db.close();
    }

    public void deleteBoxnumberInventory(String bn) {
        SQLiteDatabase db = gen.getWritableDatabase();
        db.delete(gen.tbname_partner_inventory,
                gen.partinv_boxnumber + " = '"+bn+"' AND "
                +gen.partinv_stat+" = '0'", null);
        db.close();
    }

    public boolean checkNum(String bn){
        SQLiteDatabase db = gen.getReadableDatabase();
        Cursor x = db.rawQuery(" SELECT * FROM "+gen.tbname_partner_inventory
        +" WHERE "+gen.partinv_boxnumber+" = '"+bn+"'", null);
        if (x.getCount() == 0){
            return true;
        }else{
            return false;
        }
    }

    public boolean checkNumFromDist(String bn){
        SQLiteDatabase db = gen.getReadableDatabase();
        Cursor x = db.rawQuery(" SELECT * FROM "+gen.tbname_tempboxes
        +" WHERE "+gen.dboxtemp_boxnumber+" = '"+bn+"'", null);
        if (x.getCount() == 0){
            return false;
        }else{
            return true;
        }
    }

    public boolean checkNumFromUndelivered(String bn){
        SQLiteDatabase db = gen.getReadableDatabase();
        Cursor x = db.rawQuery(" SELECT * FROM "+gen.tbname_undelivered
        +" WHERE "+gen.und_bn+" = '"+bn+"'", null);
        if (x.getCount() == 0){
            return false;
        }else{
            return true;
        }
    }

    public String getDestinationOfBox(String bn){
        String dest = null;
        SQLiteDatabase db = gen.getReadableDatabase();
        Cursor x = db.rawQuery(" SELECT * FROM "+gen.tbname_tempboxes
                +" LEFT JOIN "+gen.tbname_tempDist
                +" ON "+gen.temp_transactionnumber+" = "+gen.dboxtemp_distributionid
                +" WHERE "+gen.dboxtemp_boxnumber+" = '"+bn+"' AND "+gen.dboxtemp_distributionid
                +" LIKE '%PARTD-%'", null);
        if (x.moveToNext()){
            dest = x.getString(x.getColumnIndex(gen.temp_typename));
        }
        return dest;
    }

    public String getDistTypeByBN(String bn){
        String type = null;
        SQLiteDatabase db = gen.getReadableDatabase();
        Cursor x = db.rawQuery(" SELECT * FROM "+gen.tbname_tempboxes
                +" LEFT JOIN "+gen.tbname_tempDist
                +" ON "+gen.temp_transactionnumber+" = "+gen.dboxtemp_distributionid
                +" WHERE "+gen.dboxtemp_boxnumber+" = '"+bn+"' AND "+gen.dboxtemp_distributionid
                +" LIKE '%PARTD-%'", null);
        if (x.moveToNext()){
            type = x.getString(x.getColumnIndex(gen.temp_type));
        }
        return type;
    }

    public int saveAcceptance(){
        int ok = 0;
        try {
            String trans = getTrans();
            String driver = drivername.getText().toString();
            String warehouse = getWarehouseID(ybranch.getText().toString()) + "";
            String containername = container.getText().toString();
            String stat = "1";
            if (boxnumbers.size() == 0) {
                String t = "Save failed, you did not scan a boxnumber.";
                customToast(t);
                ok = 0;
            } else {
                if (containername.equals("")) {
                    String t = "Save failed, container number is empty.";
                    customToast(t);
                    ok = 0;
                } else {
                    for (String bn : boxnumbers) {
                        if (!checkNum(bn)) {
                            gen.addAcceptanceBoxnumber(trans, getBox(bn),
                                    bn, "2");
                            updateInventory(bn);
                            deleteInUndelivered(bn);
                        } else {
                            gen.addAcceptanceBoxnumber(trans, getBox(bn),
                                    bn, "2");
                            saveInventory(bn);
                            deleteInUndelivered(bn);
                        }
                    }
                    gen.addNewAcceptance(trans, driver, warehouse,
                            containername, datelang() + " " + returntime(),
                            helper.logcount() + "", stat, "1");

                    gen.addTransactions("Acceptance", "" + helper.logcount(),
                            "New acceptance " + trans, datelang(), returntime());
                    container.setText("");
                    drivername.setText("");
                    transactionnum.setText("");
                    setTrans(null);
                    boxnumbers.clear();
                    ok = 1;
                }
            }
        }catch (Exception e){
            Log.e("partner-acceptance", e.getMessage());
        }
            return ok;

    }

    @Override
    protected void onDestroy() {
        if (boxnumbers.size() != 0 ){
            boxnumbers.clear();
        }
        super.onDestroy();
    }

    //date only
    public String datelang(){
        Date datetalaga = Calendar.getInstance().getTime();
        SimpleDateFormat writeDate = new SimpleDateFormat("yyyy-MM-dd");
        writeDate.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        String findate = writeDate.format(datetalaga);

        return findate;
    }

    //time only
    public String returntime(){
        Date datetalaga = Calendar.getInstance().getTime();
        SimpleDateFormat writeDate = new SimpleDateFormat("HH:mm:ss");
        writeDate.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        String time = writeDate.format(datetalaga);

        return time;
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

    public String[] getSalesDriver(String post, String branch) {
        SQLiteDatabase db = gen.getReadableDatabase();
        ArrayList<String> names = new ArrayList<String>();
        Cursor c = db.rawQuery(" SELECT * FROM " + gen.tbname_employee
                +" LEFT JOIN "+gen.tbname_branch
                +" ON "+gen.tbname_branch+"."+gen.branch_id+" = "+gen.tbname_employee+"."+gen.emp_branch
                +" WHERE "+gen.emp_post+" = '"+post+"' AND "+gen.emp_branch+" = '"+branch+"'", null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            String name = c.getString(c.getColumnIndex(gen.emp_first))+" "
                    +c.getString(c.getColumnIndex(gen.emp_last));
            names.add(name);
            c.moveToNext();
        }
        c.close();
        return names.toArray(new String[names.size()]);
    }

    public void autoNameDriver(){
        try {
            String[] names = getSalesDriver("Partner Driver", helper.getBranch(helper.logcount()+""));
            ArrayAdapter<String> adapter = new ArrayAdapter<String>
                    (getApplicationContext(), android.R.layout.simple_list_item_1, names);
            drivername.setThreshold(1);
            drivername.setAdapter(adapter);
            drivername.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String val = (String) parent.getItemAtPosition(position);
                    drivername.setText(val);
                }
            });
            Log.e("drivers", Arrays.toString(names)+" ,"+helper.getBranch(helper.logcount()+""));

        }catch (Exception e){
            Log.e("error", e.getMessage());
        }
    }

    public boolean checkifTransNull() {
        boolean check = false;
        if (!transactionnum.getText().toString().equals("")) {
            String trans = transactionnum.getText().toString();
            SQLiteDatabase db = rate.getReadableDatabase();
            String x = " SELECT * FROM "+rate.tbname_part_distribution
                    +" WHERE "+rate.partdist_transactionnumber+" = '"+trans+"'";
            Cursor c = db.rawQuery(x, null);
            if (c.getCount() != 0){
                check = true;
            }
        }
        return check;
    }

    public boolean checkFocus(){
        if(this.getCurrentFocus().getId() == container.getId()){
            // your view is in focus

            Log.e("focus", container.hasFocus()+"");
            return true;
        }else{
            // not in the focus

            Log.e("focus", container.hasFocus()+"");
            return false;
        }
    }

    public boolean checkFocusTrans(){
        if(this.getCurrentFocus().getId() == transactionnum.getId()){
            // your view is in focus

            Log.e("focus", container.hasFocus()+"");
            return true;
        }else{
            // not in the focus

            Log.e("focus", container.hasFocus()+"");
            return false;
        }
    }

    public boolean checkFocusDriver(){
        if(this.getCurrentFocus().getId() == drivername.getId()){
            Log.e("focus", drivername.hasFocus()+"");
            return true;
        }else{
            Log.e("focus", drivername.hasFocus()+"");
            return false;
        }
    }

    public String autoTruck(String trans){
        SQLiteDatabase db = rate.getReadableDatabase();
        String truck = null;
        String x = " SELECT * FROM "+rate.tbname_part_distribution
                +" WHERE "+rate.partdist_transactionnumber+" = '"+trans+"'";
        Cursor c = db.rawQuery(x, null);
        if (c.moveToNext()){
            truck = c.getString(c.getColumnIndex(rate.partdist_trucknum));
        }
        return truck;
    }

    public String autoTruckDriver(String trans){
        SQLiteDatabase db = rate.getReadableDatabase();
        String name = null;
        String x = " SELECT * FROM "+rate.tbname_part_distribution
                +" WHERE "+rate.partdist_transactionnumber+" = '"+trans+"'";
        Cursor c = db.rawQuery(x, null);
        if (c.moveToNext()){
            name = c.getString(c.getColumnIndex(rate.partdist_drivername));
        }
        return name;
    }

    public void deleteInUndelivered(String bn){
        SQLiteDatabase db = gen.getWritableDatabase();
        db.delete(gen.tbname_undelivered, gen.und_bn+" = '"+bn+"'",null);
        Log.e("delete","undelivered "+bn);
        db.close();
    }

    //shared preference
    public void preference(){
        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(this);
        String themeName = pref.getString("theme", "Theme1");
        if (themeName.equals("Default(Red)")) {
            setTheme(R.style.AppTheme);
        } else if (themeName.equals("Light Blue")) {
            setTheme(R.style.customtheme);
        }else if (themeName.equals("Green")) {
            setTheme(R.style.customgreen);
        }
    }


}
