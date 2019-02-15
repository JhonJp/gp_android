package com.example.admin.gpxbymodule;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.os.ResultReceiver;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.util.AttributeSet;
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
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.Result;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

public class ReservationData extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    TextView reservnum, topitem,sub, amountdepo;
    HomeDatabase helper;
    GenDatabase gen;
    RatesDB rate;
    double taxrate;
    IntentIntegrator scanIntegrator;
    String value, reserveid;
    ListView list;
    String boxesnumbers, reservationid, boxtype, selectedid;
    EditText boxnum;
    int scan_code = 1, camera_request = 0;
    float baseamount = 0;
    Camera camera;
    float pendamount;
    Date datetalaga;
    ArrayList<ListItem> result;
    int totalamount = 0;
    float trialamount = 0;
    ArrayList<String> ids, boxnums;
    private static final String LIST_STATE = "listState";
    private Parcelable mListState = null;
    String orno = "",bookingid = null,term = "Partial",finaldate;
    NavigationView navigationView;

    String btype;
    String find;

    //signature variables
    LinearLayout mContent;
    ReservationData.signature mSignature;
    Button mClear, mGetSign, mCancel;
    public static String tempDir;
    public String current = null;
    private Bitmap mBitmap;
    View mView;
    File mypath;
    Dialog dialog;
    byte[] off;
    private String uniqueId;
    CheckBox tax;

    //capture image
    ArrayList<byte[]> capt_images;
    GridView grim;
    TextView hints;
    FloatingActionButton getimg;
    AlertDialog alertd;
    ArrayList<HomeList> stored_image;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reservation_data);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        reservnum = (TextView)findViewById(R.id.reservationnumber);
        helper = new HomeDatabase(getApplicationContext());
        gen = new GenDatabase(getApplicationContext());
        rate = new RatesDB(getApplicationContext());
        list = (ListView)findViewById(R.id.lv);
        amountdepo = (TextView) findViewById(R.id.amountdeposit);
        tax = (CheckBox)findViewById(R.id.taxcheck);

        ids = new ArrayList<>();
        ids.clear();
        boxnums = new ArrayList<>();
        capt_images = new ArrayList<>();
        stored_image = new ArrayList<HomeList>();

        boxnums.clear();
        try{
            if (helper.logcount() != 0){
                value = helper.getRole(helper.logcount());
                Log.e("role ", value);
            }

            Bundle bundle = getIntent().getExtras();
            reservationid = bundle.getString("reservationno");
            reserveid = bundle.getString("reservationid");

            reservnum.setText(reservationid);
            querycheck(reservationid);


        }catch (Exception e){}

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        list.setOnTouchListener(new View.OnTouchListener() {
            // Setting on Touch Listener for handling the touch inside ScrollView
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });

        setNameMail();
        sidenavMain();
        subMenu();
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

    public void sidenavMain(){
        ListView lv=(ListView)findViewById(R.id.optionsList);//initialization of listview

        final ArrayList<HomeList> listitem = helper.getData(value);

        NavAdapter ad = new NavAdapter(getApplicationContext(), listitem);
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
                        final AlertDialog.Builder builder = new AlertDialog.Builder(ReservationData.this);
                        builder.setMessage("Confirm logout?")
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        helper.logout();

                                        startActivity(new Intent(getApplicationContext(), Login.class));
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
                bundle.putString("module", "Reservation");
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
                startActivity(new Intent(this, Partner_Maindelivery.class));
                finish();
                break;
            case "Barcode Releasing":
                startActivity(new Intent(this, BoxRelease.class));
                finish();
                break;
        }
    }

    public void reserveBoxes(){
        try {
            result = gen.getBoxReservationBoxnumber(reservationid);
            final ListAdapter adapter = new ListAdapter(getApplicationContext(), result);
            list.setAdapter(adapter);
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    selectedid = result.get(position).getId();
                    btype = result.get(position).getTopitem();
                    baseamount = Float.parseFloat(result.get(position).getAmount());

                    sub = (TextView) view.findViewById(R.id.subitem);
                    if (!ids.contains(selectedid)) {
                        ids.add(selectedid);
                        pendamount += baseamount;
                    }
                    scanpermit();
                }
            });
            int withnum = getTotalWithBoxnumber(reservationid);
            Log.e("withnum", "" + withnum);
            tax.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.e("checkbox", tax.isChecked()+"");
                    if (tax.isChecked()){
                        tax.setChecked(true);
                        taxrate = ((pendamount)*(0.05));
                        amountdepo.setText(((taxrate)+(pendamount))+"0");
                    }else{
                        tax.setChecked(false);
                        taxrate = 0;
                        amountdepo.setText(((taxrate)+(pendamount))+"0");
                    }
                }
            });
            amountdepo.setText(((taxrate)+(pendamount))+"0");
        }catch (Exception e){}
    }

    public void querycheck(String r){
        try{
            SQLiteDatabase db = gen.getReadableDatabase();
            Cursor sc = db.rawQuery(" SELECT * FROM "+gen.tbname_reservation_boxtype_boxnumber
            +" WHERE "+gen.res_btype_bnum_reservation_id+" = '"+r+"'", null);
            if (sc.getCount() == 0){
                getAllReserve(r);
            }else{
                reserveBoxes();
            }
        }catch (Exception e){}
    }

    @Override
    public void onBackPressed() {
        try{
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Cancel transaction?");
            builder.setMessage("note: Box numbers will be reset.")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            if (ids.size() != 0) {
                                if (!ids.contains(selectedid)) {
                                    updateWithNumber(ids, "1", "0");
                                    //reserveBoxes();
                                } else {
                                    updateWithNumber(ids, "1", "0");
                                    //reserveBoxes();
                                    Log.e("ids", "Selected id is present");
                                }
                            }
                            startActivity(new Intent(getApplicationContext(), Reservelist.class));
                            finish();
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });
            // Create an alert
            builder.create().show();
        }catch (Exception e){}
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //deleteAllStatOne(reservationid);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.reservation_data, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.actionsaveall){
            if (getTotalWithBoxnumber(reservationid) == 0){
                String q = "You have not scanned a box number.";
                customToast(q);
            }else {
                if (getTotalWithBoxnumber(reservationid) != getAllReserves(reservationid)){
                    //dialogsign();
                    String x = "Please add box numbers in all boxes that you have reserved.";
                    customToast(x);
                    //viewReceipt("1");
                }else if(getTotalWithBoxnumber(reservationid) == getAllReserves(reservationid)) {
                    //dialogsign();
                    //viewReceipt("0");
                    viewGenericImage();
                }
            }
        }
        else if (id ==  R.id.passincident){
            Intent i = new Intent(this, Incident.class);
            Bundle bundle = new Bundle();
            bundle.putString("module", "Reservation");
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

    public void scanpermit(){
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(ReservationData.this, new String[]
                    {Manifest.permission.CAMERA}, scan_code);
        }else{
            scanIntegrator = new IntentIntegrator(ReservationData.this);
            scanIntegrator.setPrompt("Scan barcode");
            scanIntegrator.setBeepEnabled(true);
            scanIntegrator.setOrientationLocked(true);
            scanIntegrator.setCaptureActivity(CaptureActivityPortrait.class);
            scanIntegrator.initiateScan();

        }
    }

    public void saveReservationPayment(){
        try {
            HomeDatabase home = new HomeDatabase(getApplicationContext());
            String reservation_no = null, customer_id = null, createdby = null, createddate = null, assigned_to = null;
            SQLiteDatabase db = gen.getReadableDatabase();

            Cursor cv = db.rawQuery(" SELECT * FROM " + gen.tbname_reservation + " WHERE " + gen.reserve_reservation_no + " = '" + reservationid + "'", null);
            cv.moveToFirst();
            while (!cv.isAfterLast()) {
                reservation_no = cv.getString(cv.getColumnIndex(gen.reserve_reservation_no));
                customer_id = cv.getString(cv.getColumnIndex(gen.reserve_customer_id));
                createdby = cv.getString(cv.getColumnIndex(gen.reserve_createdby));
                assigned_to = cv.getString(cv.getColumnIndex(gen.reserve_assigned_to));
                cv.moveToNext();
            }
            cv.close();
            selectall(reservationid);

            for (String num : boxnums) {
                gen.updateInvBoxnumber("0", num, "3");
            }

            for (byte[] img : capt_images) {
                //rate.addReserveImage(reservationid, img);
                rate.addGenericImage("reservation", reservationid, img);
            }

            gen.updateReservationStatus(reservation_no, reservation_no, customer_id, createdby, datereturn(), assigned_to,
                    "2", "1");
            gen.addReservationPayment(orno, bookingid, reservationid, term, "" + ((taxrate)+(pendamount)),
                    "0", "" + home.logcount(), datereturn());

            if (checkAmountTrans(reservationid)){
                gen.updateRemAmount(reservationid, pendamount+"",
                        helper.logcount()+"", "0");
            }else {
                gen.addRemitAmount(pendamount + "", reservationid, "0",
                        helper.logcount() + "", dateOnly());
            }

            ids.clear();
            capt_images.clear();
            stored_image.clear();
            startActivity(new Intent(getApplicationContext(), Reservelist.class));
            finish();
        }catch (Exception e){}

    }

    public void camera_capture() {
        try {
            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(cameraIntent, camera_request);
        } catch (Exception e) {
            ActivityCompat.requestPermissions(ReservationData.this,
                    new String[]{Manifest.permission.CAMERA}, camera_request);
        }
    }

    public void viewReceipt(final String c){
        try {
            final AlertDialog.Builder denom = new AlertDialog.Builder(ReservationData.this);
            LayoutInflater inflater = getLayoutInflater();
            View d = inflater.inflate(R.layout.sample_receipt, null);
            denom.setView(d);
            denom.setCancelable(false);
            TextView txtdt = (TextView)d.findViewById(R.id.textdate);
            LinearLayout disc_page = (LinearLayout)d.findViewById(R.id.disc);
            disc_page.setVisibility(View.INVISIBLE);
            ListView lvs = (ListView) d.findViewById(R.id.lvsintopay);
            TextView booknum = (TextView)d.findViewById(R.id.booknumtxt);
            TextView bookhint = (TextView)d.findViewById(R.id.booknumhint);
            TextView sendr = (TextView)d.findViewById(R.id.sendertxt);
            TextView driv = (TextView)d.findViewById(R.id.drivernameTxt);
            TextView dep = (TextView)d.findViewById(R.id.depotxt);
            TextView rat = (TextView)d.findViewById(R.id.ratetxt);
            TextView addit = (TextView)d.findViewById(R.id.addittxt);
            TextView tot = (TextView)d.findViewById(R.id.totalpaytxt);
            TextView ratetxt = (TextView)d.findViewById(R.id.ratehint);
            result = gen.getBoxReservationBoxnumber(reservationid);
            final ListAdapter adapter = new ListAdapter(getApplicationContext(), result);
            lvs.setAdapter(adapter);
            driv.setText(helper.getFullname(helper.logcount()+""));
            txtdt.setText(datereturn());
            booknum.setText(reservationid);
            bookhint.setText("Reservation number");
            sendr.setText(getAccntName(getAccnt(reservationid)));
            dep.setText(pendamount+"0");
            ratetxt.setText("Tax payable"); //tax text word
            rat.setText(taxrate+"0"); //tax rate value
            addit.setText("0.00");
            tot.setText(((taxrate)+(pendamount))+"0");
            denom.setCancelable(false);
            denom.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface d, int which) {
                    d.dismiss();
                    // Dialog Function
                    dialog = new Dialog(ReservationData.this);
                    // Removing the features of Normal Dialogs
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setContentView(R.layout.capture_sign);
                    dialog.setCancelable(true);
                    dialogsign(c);
                }
            });
            denom.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            denom.show();

        }catch (Exception e){}
    }

    public String getAccnt(String accnt){
        String ac = "";
        SQLiteDatabase db = gen.getReadableDatabase();
        String c = " SELECT * FROM "+gen.tbname_reservation
                +" WHERE "+gen.reserve_reservation_no+" = '"+accnt+"'";
        Cursor x = db.rawQuery(c, null);
        if (x.moveToNext()){
            ac = x.getString(x.getColumnIndex(gen.reserve_customer_id));
        }
        return ac;
    }

    public String getAccntName(String accntnumber){
        String ac = "";
        SQLiteDatabase db = gen.getReadableDatabase();
        String c = " SELECT * FROM "+gen.tbname_customers
                +" WHERE "+gen.cust_accountnumber+" = '"+accntnumber+"'";
        Cursor x = db.rawQuery(c, null);
        if (x.moveToNext()){
            ac = x.getString(x.getColumnIndex(gen.cust_fullname));
        }
        return ac;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        try{
            if (requestCode == camera_request){
                if (requestCode == camera_request && resultCode == Activity.RESULT_OK) {
                    Bitmap photo = (Bitmap) data.getExtras().get("data");
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    photo.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    byte[] bytimg = stream.toByteArray();
                    HomeList list = new HomeList(bytimg,"");
                    capt_images.add(bytimg);
                    stored_image.add(list);
                    viewGenericImage();
                    Log.e("camera", "success " + bytimg + " / " + reservationid);
                }
            }else{
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
                if (result.getContents() != null){
                    String bn = result.getContents();
                    String nsb = "NSB";
                    if (btype.toLowerCase().contains(nsb.toLowerCase())) {
                            checkBoxnumNSB(bn);
                    }else if (compareBoxName(btype, getNameFromInventory(bn))) {
                        //Log.e("btype", btype);
                        Log.e("fromINV", getNameFromInventory(bn));
                            checkBoxnum(bn);
                        Log.e("boxnumbers", boxnums.toString());
                    } else {
                        String xc = "The assigned boxtype of the boxnumber is " +
                                "not the same to the reserved boxtype, please try another. ";
                        customToast(xc);
                    }
                    Log.e("boxnumber", bn);
                }
            }
        }catch (Exception e){}
    }

    public int getBoxId(String id){
        int i = 0;
        SQLiteDatabase db = gen.getReadableDatabase();
        Cursor c = db.rawQuery(" SELECT * FROM "+gen.tbname_boxes+" WHERE "+gen.box_name+" = '"+id+"'", null);
        if (c.moveToNext()){
            i = c.getInt(c.getColumnIndex(gen.box_id));
        }
        return i;
    }

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
        }else if (requestCode == camera_request) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(), "camera permission granted", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    public String datereturn(){
        datetalaga = Calendar.getInstance().getTime();
        SimpleDateFormat writeDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        writeDate.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        find = writeDate.format(datetalaga);

        return find;
    }

    public String dateOnly(){
        datetalaga = Calendar.getInstance().getTime();
        SimpleDateFormat writeDate = new SimpleDateFormat("yyyy-MM-dd");
        writeDate.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        find = writeDate.format(datetalaga);

        return find;
    }

    public void getAllReserve(String res){
        try {
            SQLiteDatabase db = gen.getWritableDatabase();
            Cursor y = db.rawQuery(" SELECT * FROM " + gen.tbname_reservation_boxtype_boxnumber +
                    " WHERE " + gen.res_btype_bnum_reservation_id + " = '" + res + "'", null);
            Cursor x = db.rawQuery(" SELECT * FROM " + gen.tbname_reservation_boxtype
                    + " WHERE " + gen.res_reservation_id + " = '" + res + "'", null);
            if (y.getCount() == 0) {
                x.moveToFirst();
                while (!x.isAfterLast()) {
                    String topitem = x.getString(x.getColumnIndex(gen.res_boxtype));
                    String subitem = x.getString(x.getColumnIndex(gen.res_quantity));
                    float depositamount = Float.parseFloat(x.getString(x.getColumnIndex(gen.res_deposit)));
                    int quant = Integer.parseInt(subitem);
                    float amount = (depositamount / quant);
                    String subs = "NULL";
                    if (quant != 1) {
                        for (int i = 1; i <= quant; i++) {
                            gen.addReservationBoxtypeBoxnumber(topitem, subs, "" + amount, datereturn(), res, "1");
                        }
                    } else {
                        gen.addReservationBoxtypeBoxnumber(topitem, subs, "" + amount, datereturn(), res, "1");
                    }
                    x.moveToNext();
                }
                reserveBoxes();
            } else {
                reserveBoxes();
            }
        }catch (Exception e){}
    }

    public int getTotalWithBoxnumber(String trans){
        SQLiteDatabase db = gen.getReadableDatabase();
        Cursor q = db.rawQuery(" SELECT * FROM "+gen.tbname_reservation_boxtype_boxnumber
                +" WHERE "+gen.res_reservation_id+" = '"+trans+"' AND "
                +gen.res_btype_bnum_box_number+" != 'NULL' AND "+gen.res_btype_bnum_stat+" = '1'", null);

        return q.getCount();
    }

    //get box name from distributed boxes
    public String getNameFromInventory(String bn){
        String name = null;
        SQLiteDatabase db = gen.getReadableDatabase();
        String que = "SELECT * FROM "+gen.tbname_driver_inventory
                +" LEFT JOIN "+gen.tbname_boxes+" ON "
                +gen.tbname_boxes+"."+gen.box_id+" = "+gen.tbname_driver_inventory+"."+gen.sdinv_boxtype
                +" WHERE "+gen.sdinv_boxnumber+" = '"+bn+"'";
        Cursor v = db.rawQuery(que, null);
        if (v.moveToNext()){
            name = v.getString(v.getColumnIndex(gen.box_name));
            Log.e("boxname", "btype: "+btype+", name: "+name);
        }
        return name;
    }

    //compare boxtype from inventory and reservation
    public boolean compareBoxName(String bt, String name){
        if (bt.equals(name)){
            return true;
        }else{
            return false;
        }
    }

    public void checkBoxnum(String barcode){
        SQLiteDatabase db = gen.getReadableDatabase();
        String query = " SELECT * FROM " + gen.tbname_reservation_boxtype_boxnumber
                + " WHERE " + gen.res_btype_bnum_box_number
                + " = '" + barcode + "' AND "+gen.res_btype_bnum_stat+" = '2'";
        Cursor c = db.rawQuery(query, null);
        if (c.getCount() == 0) {
                String que = " SELECT * FROM " + gen.tbname_booking_consignee_box
                        + " WHERE " + gen.book_con_box_number
                        + " = '" + barcode + "'";
                Cursor cx = db.rawQuery(que, null);
                if (cx.getCount() == 0) {
                    if (!boxnums.contains(barcode)) {
                        boxnums.add(barcode);
                    }
                    if (!ids.contains(selectedid)) {
                        update(selectedid, barcode, "1");
                        ids.add(selectedid);
                        reserveBoxes();
                    } else {
                        update(selectedid, barcode, "1");
                        reserveBoxes();
                        Log.e("ids", "Selected id is present booking");
                    }
                    Log.e("update", ids.toString());
                    Log.e("updatebnum", boxnums.toString());
                } else {
                    String b = "Box number has been used in booking, please scan another barcode.";
                    customToast(b);
                }
        } else {
            String b = "Box number has been used, please scan another barcode.";
            customToast(b);
            Log.e("update", ids.toString());
            Log.e("updatebnum", boxnums.toString());
        }
    }

    public void checkBoxnumNSB(String barcode){
        SQLiteDatabase db = gen.getReadableDatabase();
        String query = " SELECT * FROM " + gen.tbname_reservation_boxtype_boxnumber
                + " WHERE " + gen.res_btype_bnum_box_number
                + " = '" + barcode + "'";
        Cursor c = db.rawQuery( query, null);
        if (c.getCount() == 0) {
                String que = " SELECT * FROM " + gen.tbname_booking_consignee_box
                        + " WHERE " + gen.book_con_box_number
                        + " = '" + barcode + "'";
                Cursor cx = db.rawQuery(que, null);
                if (cx.getCount() == 0) {
                    gen.addtoDriverInv(getBoxId(btype) + "", barcode, "0", "0");
                    if (!boxnums.contains(barcode)) {
                        boxnums.add(barcode);
                    }
                    if (!ids.contains(selectedid)) {
                        update(selectedid, barcode, "1");
                        ids.add(selectedid);
                        reserveBoxes();
                    } else {
                        update(selectedid, barcode, "1");
                        reserveBoxes();
                        Log.e("ids", "Selected id is present booking");
                    }
                    Log.e("updateids", ids.toString());
                    Log.e("updatebnum", boxnums.toString());
                } else {
                    String b = "Box number has been used in booking, please scan another barcode.";
                    customToast(b);
                }
        } else {
            String b = "Box number has been used, please scan another barcode.";
            customToast(b);
            Log.e("update", ids.toString());
            Log.e("updatebnum", boxnums.toString());
        }
    }

    public void customToast(String txt){
        Toast toast = new Toast(ReservationData.this);
        toast.setDuration(Toast.LENGTH_SHORT);
        LayoutInflater inflater = ReservationData.this.getLayoutInflater();
        View view = inflater.inflate(R.layout.toast, null);
        TextView t = (TextView)view.findViewById(R.id.toasttxt);
        t.setText(txt);
        toast.setView(view);
        toast.setGravity(Gravity.TOP | Gravity.RIGHT, 15, 50);
        Animation animation = AnimationUtils.loadAnimation(ReservationData.this, R.anim.enterright);
        view.startAnimation(animation);
        toast.show();
    }

    public void update(String id, String bn, String stat){
        try{
            SQLiteDatabase db = gen.getWritableDatabase();
            Cursor x = db.rawQuery(" SELECT * FROM "+gen.tbname_reservation_boxtype_boxnumber
                    +" WHERE "+gen.res_btype_bnum_id+" = '"+id+"'", null);
            if (x.moveToNext()){
                String type = x.getString(x.getColumnIndex(gen.res_btype_bnum_boxtype));
                String depo = x.getString(x.getColumnIndex(gen.res_btype_bnum_box_depoprice));
                String res = x.getString(x.getColumnIndex(gen.res_btype_bnum_reservation_id));
                gen.updReservationBoxtypeBoxnumber(id, type, bn, depo, datereturn(), res, stat);
                //Log.e("upd", ""+id);
            }
            x.close();
        }catch (Exception e){}
    }

    public int getAllReserves(String res){
        SQLiteDatabase db = gen.getWritableDatabase();
        Cursor y = db.rawQuery(" SELECT * FROM " + gen.tbname_reservation_boxtype_boxnumber
                + " WHERE " + gen.res_btype_bnum_reservation_id + " = '" + res + "'", null);
        return y.getCount();
    }

    public void updateWithNumber(ArrayList<String> id, String stat, String act){
        int ity = 0;
        for (String item: id) {
            Log.e("ids", item);
            updStat(item, stat, act);
        }
    }

    public void deletWithNumber(ArrayList<String> id){
        for (String item: id) {
            Log.e("ids", item);
            updateStat(item, "NULL", "1");
        }
    }

    public void deleteAllStatOne(String r){
        SQLiteDatabase db = gen.getWritableDatabase();
        db.delete(gen.tbname_reservation_boxtype_boxnumber,
                gen.res_btype_bnum_reservation_id+" = '"+r+"'", null);
        Log.e("delete", "delete all "+r);
        db.close();
    }

    public void updStat(String id, String stat, String act){
        if (act == "1") {
            SQLiteDatabase db = gen.getWritableDatabase();
            Cursor x = db.rawQuery(" SELECT * FROM " + gen.tbname_reservation_boxtype_boxnumber
                    + " WHERE " + gen.res_btype_bnum_id + " = '" + id + "'", null);
            if (x.moveToNext()) {
                String type = x.getString(x.getColumnIndex(gen.res_btype_bnum_boxtype));
                String bn = x.getString(x.getColumnIndex(gen.res_btype_bnum_box_number));
                String depo = x.getString(x.getColumnIndex(gen.res_btype_bnum_box_depoprice));
                String res = x.getString(x.getColumnIndex(gen.res_btype_bnum_reservation_id));
                gen.updReservationBoxtypeBoxnumber(id, type, bn, depo, datereturn(), res, stat);
                Log.e("updatednotall", "" + bn);
            }
            x.close();
        }else{
            SQLiteDatabase db = gen.getWritableDatabase();
            Cursor x = db.rawQuery(" SELECT * FROM " + gen.tbname_reservation_boxtype_boxnumber
                    + " WHERE " + gen.res_btype_bnum_id + " = '" + id + "'", null);
            if (x.moveToNext()) {
                String type = x.getString(x.getColumnIndex(gen.res_btype_bnum_boxtype));
                String depo = x.getString(x.getColumnIndex(gen.res_btype_bnum_box_depoprice));
                String res = x.getString(x.getColumnIndex(gen.res_btype_bnum_reservation_id));
                gen.updReservationBoxtypeBoxnumber(id, type, "NULL", depo, datereturn(), res, stat);
                Log.e("updated", "" + id);
            }
            x.close();
        }
    }

    public void updateStat(String id, String bnum, String stat){
        SQLiteDatabase db = gen.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(gen.res_btype_bnum_box_number, bnum);
        cv.put(gen.res_btype_bnum_stat, stat);
        db.update(gen.tbname_reservation_boxtype_boxnumber, cv,
                gen.res_btype_bnum_id+" = '"+id+"'", null);
        Log.e("updbn", bnum);
        db.close();
    }

    public void selectall(String res){
        try{
            SQLiteDatabase db = gen.getWritableDatabase();
            Cursor x = db.rawQuery(" SELECT * FROM "+gen.tbname_reservation_boxtype_boxnumber
                    +" WHERE "+gen.res_btype_bnum_reservation_id+" = '"+res+"' AND "
                    +gen.res_btype_bnum_stat+" = '1'", null);
            x.moveToFirst();
            while (!x.isAfterLast()){
                String id = x.getString(x.getColumnIndex(gen.res_btype_bnum_id));
                String type = x.getString(x.getColumnIndex(gen.res_btype_bnum_boxtype));
                String bn = x.getString(x.getColumnIndex(gen.res_btype_bnum_box_number));
                String depo = x.getString(x.getColumnIndex(gen.res_btype_bnum_box_depoprice));
                String stat = "2";

                gen.updReservationBoxtypeBoxnumber(id, type, bn, depo, datereturn(), res, stat);
                Log.e("updated", ""+id);
                x.moveToNext();
            }
            x.close();
        }catch (Exception e){}
    }

    //signature view
    // Function for Digital Signature
    public void dialogsign(final String c) {
        tempDir = Environment.getExternalStorageDirectory() + "/" + getResources().getString(R.string.app_name) + "/";
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File directory = cw.getDir(getResources().getString(R.string.app_name), Context.MODE_PRIVATE);

        prepareDirectory();
        uniqueId = getTodaysDate() + "_" + getCurrentTime() + "_" + Math.random();
        current = uniqueId + ".png";
        mypath= new File(directory,current);


        mContent = (LinearLayout)dialog.findViewById(R.id.linearLayout);
        mSignature = new ReservationData.signature(getApplicationContext(), null);
        mSignature.setBackgroundColor(Color.WHITE);
        mContent.addView(mSignature, ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
        mClear = (Button)dialog.findViewById(R.id.clearsign);
        mGetSign = (Button)dialog.findViewById(R.id.savesign);
        mGetSign.setEnabled(false);
        mCancel = (Button)dialog.findViewById(R.id.cancelsign);
        mView = mContent;

        mClear.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                Log.v("log_tag", "Panel Cleared");
                mSignature.clear();
                mGetSign.setEnabled(false);
            }
        });

        mGetSign.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                //Log.v("log_tag", "Panel Saved");
                boolean error = captureSignature();
                if(!error){
                    mContent.setDrawingCacheEnabled(true);
                    mSignature.save(mView);
                    if (c.equals("1")) {
                        updateWithNumber(ids, "2", "1");
                        gen.addReservationPayment(orno, bookingid, reservationid, term, "" + ((taxrate)+(pendamount)),
                                "0", "" + helper.logcount(), ""+find);
                        Log.e("payment",((taxrate)+(pendamount))+"");
                        if (checkAmountTrans(reservationid)){
                            gen.updateRemAmount(reservationid, pendamount+"",
                                    helper.logcount()+"", "0");
                        }else {
                            gen.addRemitAmount(pendamount + "", reservationid, "0",
                                    helper.logcount() + "", dateOnly());
                        }

                        Log.e("selectedid", ""+ids);
                        ids.clear();
                        dialog.dismiss();
                        startActivity(new Intent(getApplicationContext(), Reservelist.class));
                        finish();
                    }else{
                        saveReservationPayment();
                        ids.clear();
                        startActivity(new Intent(getApplicationContext(), Reservelist.class));
                        finish();
                    }
                    dialog.dismiss();
                }
            }
        });

        mCancel.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                Log.v("log_tag", "Panel Canceled");
                mSignature.clear();
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public class signature extends View
    {
        public static final float STROKE_WIDTH = 5f;
        public static final float HALF_STROKE_WIDTH = STROKE_WIDTH / 2;
        public Paint paint = new Paint();
        public Path path = new Path();

        public float lastTouchX;
        public float lastTouchY;
        public final RectF dirtyRect = new RectF();

        public signature(Context context, AttributeSet attrs)
        {
            super(context, attrs);
            paint.setAntiAlias(true);
            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeWidth(STROKE_WIDTH);
        }

        public void save(View v)
        {
            if(mBitmap == null)
            {
                mBitmap =  Bitmap.createBitmap (mContent.getWidth(), mContent.getHeight(), Bitmap.Config.RGB_565);;
            }
            Canvas canvas = new Canvas(mBitmap);
            try
            {
                ByteArrayOutputStream off_byte = new ByteArrayOutputStream();
                FileOutputStream mFileOutStream = new FileOutputStream(mypath);

                v.draw(canvas);
                mBitmap.compress(Bitmap.CompressFormat.PNG, 100, mFileOutStream);
                mBitmap.compress(Bitmap.CompressFormat.PNG, 100, off_byte);
                off = off_byte.toByteArray();
                rate.addSign(reservationid, off);
                //bytes.setText(off+"");

                mFileOutStream.flush();
                mFileOutStream.close();

            }
            catch(Exception e)
            {
                Log.v("log_tag", e.toString());
            }
        }

        public void clear()
        {
            path.reset();
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas)
        {
            canvas.drawPath(path, paint);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event)
        {
            float eventX = event.getX();
            float eventY = event.getY();
            mGetSign.setEnabled(true);

            switch (event.getAction())
            {
                case MotionEvent.ACTION_DOWN:
                    path.moveTo(eventX, eventY);
                    lastTouchX = eventX;
                    lastTouchY = eventY;
                    return true;

                case MotionEvent.ACTION_MOVE:

                case MotionEvent.ACTION_UP:

                    resetDirtyRect(eventX, eventY);
                    int historySize = event.getHistorySize();
                    for (int i = 0; i < historySize; i++)
                    {
                        float historicalX = event.getHistoricalX(i);
                        float historicalY = event.getHistoricalY(i);
                        expandDirtyRect(historicalX, historicalY);
                        path.lineTo(historicalX, historicalY);
                    }
                    path.lineTo(eventX, eventY);
                    break;

                default:
                    debug("Ignored touch event: " + event.toString());
                    return false;
            }

            invalidate((int) (dirtyRect.left - HALF_STROKE_WIDTH),
                    (int) (dirtyRect.top - HALF_STROKE_WIDTH),
                    (int) (dirtyRect.right + HALF_STROKE_WIDTH),
                    (int) (dirtyRect.bottom + HALF_STROKE_WIDTH));

            lastTouchX = eventX;
            lastTouchY = eventY;

            return true;
        }

        private void debug(String string){
        }

        private void expandDirtyRect(float historicalX, float historicalY)
        {
            if (historicalX < dirtyRect.left)
            {
                dirtyRect.left = historicalX;
            }
            else if (historicalX > dirtyRect.right)
            {
                dirtyRect.right = historicalX;
            }

            if (historicalY < dirtyRect.top)
            {
                dirtyRect.top = historicalY;
            }
            else if (historicalY > dirtyRect.bottom)
            {
                dirtyRect.bottom = historicalY;
            }
        }

        private void resetDirtyRect(float eventX, float eventY)
        {
            dirtyRect.left = Math.min(lastTouchX, eventX);
            dirtyRect.right = Math.max(lastTouchX, eventX);
            dirtyRect.top = Math.min(lastTouchY, eventY);
            dirtyRect.bottom = Math.max(lastTouchY, eventY);
        }

    }

    private boolean captureSignature() {

        boolean error = false;
        String errorMessage = "";

        if(error){
            Toast toast = Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.TOP, 105, 50);
            toast.show();
        }

        return error;
    }

    private String getTodaysDate() {

        final Calendar c = Calendar.getInstance();
        int todaysDate =     (c.get(Calendar.YEAR) * 10000) +
                ((c.get(Calendar.MONTH) + 1) * 100) +
                (c.get(Calendar.DAY_OF_MONTH));
        Log.w("DATE:",String.valueOf(todaysDate));
        return(String.valueOf(todaysDate));

    }

    private String getCurrentTime() {

        final Calendar c = Calendar.getInstance();
        int currentTime =     (c.get(Calendar.HOUR_OF_DAY) * 10000) +
                (c.get(Calendar.MINUTE) * 100) +
                (c.get(Calendar.SECOND));
        Log.w("TIME:",String.valueOf(currentTime));
        return(String.valueOf(currentTime));

    }

    private boolean prepareDirectory()
    {
        try
        {
            if (makedirs())
            {
                return true;
            } else {
                return false;
            }
        } catch (Exception e)
        {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Could not initiate File System.. Is Sdcard mounted properly?",
                    Toast.LENGTH_LONG).show();
            return false;
        }
    }

    private boolean makedirs()
    {
        File tempdir = new File(tempDir);
        if (!tempdir.exists())
            tempdir.mkdirs();

        if (tempdir.isDirectory())
        {
            File[] files = tempdir.listFiles();
            for (File file : files)
            {
                if (!file.delete())
                {
                    System.out.println("Failed to delete " + file);
                }
            }
        }
        return (tempdir.isDirectory());
    }

    public boolean checkAmountTrans(String trans){
        SQLiteDatabase db = gen.getReadableDatabase();
        String u = " SELECT * FROM "+gen.tbname_remitttances_amount
                +" WHERE "+gen.rem_amount_transnum+" = '"+trans+"' AND "+gen.rem_amount_stat+" = '0'";
        Cursor x = db.rawQuery(u, null);
        if (x.getCount() != 0){
            return true;
        }else{
            return false;
        }
    }

    //image transactions
    public void viewGenericImage(){
        try{
            final AlertDialog.Builder views = new AlertDialog.Builder(ReservationData.this);
            LayoutInflater inflater = getLayoutInflater();
            View d = inflater.inflate(R.layout.generic_image,null);
            views.setView(d);
            //initialize variables
            grim = (GridView)d.findViewById(R.id.grid);
            hints = (TextView)d.findViewById(R.id.imageshint);
            Button ok = (Button)d.findViewById(R.id.confirm);
            Button canc = (Button)d.findViewById(R.id.cancel);
            getimg = (FloatingActionButton)d.findViewById(R.id.addimage);
            alertd = views.create();
            alertd.show();
            getimg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (capt_images.size() >= 3){
                        String ty = "Maximum image attachment has been reached.";
                        customToast(ty);
                    }else {
                        alertd.dismiss();
                        camera_capture();
                    }
                }
            });
            viewgrid();
            ok.setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (capt_images.size() == 0){
                        String ty = "Please add image proof.";
                        customToast(ty);
                    }else{
                        alertd.dismiss();
                        viewReceipt("0");
                    }
                }
            });
            canc.setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertd.dismiss();
                }
            });
            views.setCancelable(true);
            Log.e("images", capt_images.size()+"");
        }catch (Exception e){
            Log.e("error", e.getMessage());
        }
    }

    public void viewgrid(){
        try {
            final ArrayList<HomeList> listitem = stored_image;
            ImageAdapter myAdapter = new ImageAdapter(getApplicationContext(), listitem);
            grim.setAdapter(myAdapter);
            if (capt_images.size() > 0) {
                hints.setVisibility(View.INVISIBLE);
            } else {
                hints.setVisibility(View.VISIBLE);
            }
            grim.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    byte[] getitem = listitem.get(position).getTopitem();
                    alertImage(getitem, position);
                }
            });
        }catch (Exception e){}
    }

    public void alertImage(final byte[] image, final int idt){
        try {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(ReservationData.this);
            LayoutInflater inflater = this.getLayoutInflater();
            View d = inflater.inflate(R.layout.imagefullview, null);
            Button del = (Button) d.findViewById(R.id.delete);
            Button cancel = (Button) d.findViewById(R.id.cancel);
            final ImageView img = (ImageView) d.findViewById(R.id.imagefull);

            Bitmap bm = BitmapFactory.decodeByteArray(image, 0, image.length);
            img.setImageBitmap(bm);

            dialogBuilder.setView(d);
            final AlertDialog alertDialog = dialogBuilder.show();

            del.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    capt_images.remove(image);
                    stored_image.remove(idt);
                    Log.e("index_img", " size-"+capt_images.size());
                    viewgrid();
                    alertDialog.dismiss();
                }
            });

            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.dismiss();
                }
            });
        }catch (Exception e){}
    }

}
