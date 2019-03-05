package com.example.admin.gpxbymodule;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class Remitt extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    HomeDatabase helper;
    String value;
    NavigationView navigationView;
    GenDatabase gen;
    RatesDB rate;
    ProgressDialog progressBar;
    String link;
    ArrayList<DataModel> dataModels;
    ListView listView;
    private static CustomAdapter adapter;
    FloatingActionButton add1;
    String getselected;
    int camera_request = 1, exp_request = 0;
    byte[] bytimg, bytexp;
    ImageView eximg;
    TextView exptotal, netcollect,remamount, amounthold, toptype, tophint;
    Button send;

    //signature variables
    LinearLayout mContent;
    Remitt.signature mSignature;
    Button mClear, mGetSign, mCancel;
    public static String tempDir;
    public String current = null;
    private Bitmap mBitmap;
    View mView;
    File mypath;
    Dialog dialog;
    byte[] off;
    private String uniqueId;

    //bank form
    String bank,acc_name,acc_num;
    EditText bankname,accntname,accntnum;
    TextView headoicname;
    String oicname = "",oicfullname = "";
    private int SETTINGS_ACTION = 100;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        preference();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remitt);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        helper = new HomeDatabase(this);
        gen = new GenDatabase(this);
        rate = new RatesDB(this);
        listView=(ListView)findViewById(R.id.list);
        add1 = (FloatingActionButton)findViewById(R.id.fab);
        exptotal = (TextView)findViewById(R.id.total_exp);
        amounthold = (TextView)findViewById(R.id.amount);
        toptype = (TextView)findViewById(R.id.toptypeexp);
        tophint = (TextView)findViewById(R.id.txttop);
        remamount = (TextView)findViewById(R.id.rem_amount);
        netcollect = (TextView)findViewById(R.id.net_collect);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        fabclicks();

        if (helper.logcount() != 0){
            value = helper.getRole(helper.logcount());
            Log.e("role ", value);
        }

        if (value.equals("OIC")) {

            tophint.setText("Items Breakdown");
        }else{
            //insertRemittanceDriverAmount();
            tophint.setText("Expenses Breakdown");

        }

        all_list();
        setNameMail();
        subMenu();
        sidenavMain();


    }

    //populate all list
    public void all_list(){
        try {
            final ArrayList<DataModel> result = getAllRemittanceTrans(helper.logcount() + "");
            adapter = new CustomAdapter(result,getApplicationContext());
            listView.setAdapter(adapter);
            itemLongClicked(result);
            itemClicked(result);
            if (value.equals("OIC")){
                toptype.setText("Total items");
                amounthold.setText(((sumAllFromDriver()) +"0"));
                exptotal.setText(sumAllFromDriver() + "0");
                netcollect.setText(calculateNetOIC(sumAllFromDriver(), 0) + "0");
                remamount.setText(calculateNetOIC(sumAllFromDriver(), 0) + "0");
            }else {
                amounthold.setText(((getAmounttoRemit()) +"0"));
                exptotal.setText(sumExpense() + "0");
                netcollect.setText(calculateNet(sumExpense(), getAmounttoRemit()) + "0");
                remamount.setText(calculateNet(sumExpense(), getAmounttoRemit()) + "0");
            }
        }catch (Exception e){
            Log.e("error-remittance", e.getMessage());
        }
    }

    //item longclick
    public void itemLongClicked(final ArrayList<DataModel> result){
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final String data = result.get(position).getId();
                final AlertDialog.Builder builder = new AlertDialog.Builder(Remitt.this);
                builder.setTitle("Delete this data?");
                builder.setMessage("Please confirm to delete this data.");
                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Log.e("datamodel",data);
                        deleteRemitId(data);
                        all_list();
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
                // Create the AlertDialog object and show it
                builder.create().show();
                return true;
            }
        });
    }

    public void itemClicked(final ArrayList<DataModel> res){
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final byte[] data = res.get(position).getImg();
                final String dataid = res.get(position).getId();
                alertImage(data, dataid);
            }
        });
    }

    //generator data
    public ArrayList<DataModel> getAllRemittanceTrans(String by){
        ArrayList<DataModel> results = new ArrayList<>();
        SQLiteDatabase db = gen.getReadableDatabase();
        Cursor res = db.rawQuery(" SELECT * FROM " + gen.tbname_remittance_trans
                +" WHERE "+gen.rem_trans_remittanceid+" = '"+by+"' AND "+gen.rem_trans_stat
                +" = '1'", null);
        res.moveToFirst();
        while (!res.isAfterLast()) {
            String type = res.getString(res.getColumnIndex(gen.rem_trans_type));
            String id = res.getString(res.getColumnIndex(gen.rem_trans_id));
            if (type.equals("expense")){
                String topitem = res.getString(res.getColumnIndex(gen.rem_trans_itemname));
                String subitem = res.getString(res.getColumnIndex(gen.rem_trans_amount));
                byte[] imgs = res.getBlob(res.getColumnIndex(gen.rem_trans_image));
                DataModel rem = new DataModel(topitem, subitem, id, imgs);
                results.add(rem);
            }
            else if (type.equals("fromDriver")){
                String topitem = res.getString(res.getColumnIndex(gen.rem_trans_itemname));
                String subitem = res.getString(res.getColumnIndex(gen.rem_trans_amount));
                DataModel rem = new DataModel(topitem, subitem, id,null);
                results.add(rem);
            }
            res.moveToNext();
        }
        res.close();
        return results;
    }

    public void fabclicks(){
        add1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (value.equals("OIC")){
                    viewDrivers();
                }else {
                    viewExp();
                }
            }
        });
    }

    public void sidenavMain(){
        ListView lv=(ListView)findViewById(R.id.optionsList);//initialization of listview

        final ArrayList<HomeList> listitem = helper.getData(value);

        NavAdapter ad = new NavAdapter(Remitt.this, listitem);
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
                        final AlertDialog.Builder builder = new AlertDialog.Builder(Remitt.this);
                        builder.setMessage("Confirm logout?")
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {

                                        helper.logout();

                                        startActivity(new Intent(Remitt.this, Login.class));
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
                }else if(value.equals("Partner Portal")){
                    startActivity(new Intent(this, Partner_distribution.class));
                    finish();
                }
                break;
            case "Remittance":
                if (value.equals("OIC")){
                    drawer.closeDrawer(Gravity.START);
                }else if (value.equals("Sales Driver")){
                    drawer.closeDrawer(Gravity.START);
                }
                break;
            case "Incident Report":
                Intent i = new Intent(this, Incident.class);
                Bundle bundle = new Bundle();
                bundle.putString("module", "Remittance");
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

    public void viewExp(){
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(Remitt.this);
        LayoutInflater inflater = getLayoutInflater();
        View d = inflater.inflate(R.layout.expenseadd,null);
        dialogBuilder.setView(d);
        final Spinner itemname = (Spinner)d.findViewById(R.id.items);
        final EditText amount = (EditText)d.findViewById(R.id.amountinput);
        eximg = (ImageView) d.findViewById(R.id.exp_img);
        final String[] result = rate.getAllItems();
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(getApplicationContext(), R.layout.spinneritem,
                        result);
        itemname.setAdapter(adapter);

        FrameLayout capt = (FrameLayout)d.findViewById(R.id.iframe);
        capt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exp_capture();
            }
        });

        dialogBuilder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String item = itemname.getSelectedItem().toString();
                String am = amount.getText().toString();
                if ((item.equals("")) || (am.equals(""))){
                    String r = "Please fill out the fields correctly.";
                    customAlert(r);
                }else{
                    double a = Double.valueOf(amounthold.getText().toString());
                    if (a <= 0){
                        String r = "You have zero amount, please add amount.";
                        customAlert(r);
                    }else {
                        gen.addRemitTrans("" + helper.logcount(), "expense", "", "",
                                item, am, "", bytexp, "1");
                        bytexp = null;
                        all_list();
                        dialog.dismiss();
                    }
                }
            }
        });
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialogBuilder.setTitle("Add new expense");
        final AlertDialog td = dialogBuilder.create();
        td.show();
    }

    public void viewDrivers(){
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(Remitt.this);
        LayoutInflater inflater = getLayoutInflater();
        View d = inflater.inflate(R.layout.expenseadd,null);
        dialogBuilder.setView(d);
        final Spinner itemname = (Spinner)d.findViewById(R.id.items);
        final EditText amount = (EditText)d.findViewById(R.id.amountinput);
        eximg = (ImageView) d.findViewById(R.id.exp_img);
        final String[] result = getAllDrivers(helper.getBranch(helper.logcount()+""));
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(getApplicationContext(), R.layout.spinneritem,
                        result);
        itemname.setAdapter(adapter);

        FrameLayout capt = (FrameLayout)d.findViewById(R.id.iframe);
        capt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exp_capture();
            }
        });

        dialogBuilder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String item = itemname.getSelectedItem().toString();
                String am = amount.getText().toString();
                if ((item.equals("")) || (am.equals(""))){
                    String r = "Please fill out the fields correctly.";
                    customAlert(r);
                }else{
                    gen.addRemitTrans("" + helper.logcount(), "fromDriver", "", "",
                            item, am, "", bytexp, "1");
                    bytexp = null;
                    all_list();
                    dialog.dismiss();
                }
            }
        });
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialogBuilder.setTitle("Add new item");
        final AlertDialog td = dialogBuilder.create();
        td.show();
    }

    public void viewDisbursed(){
        final AlertDialog.Builder denom = new AlertDialog.Builder(Remitt.this);
        LayoutInflater inflater = getLayoutInflater();
        View d = inflater.inflate(R.layout.addallowance,null);
        denom.setView(d);
        final EditText am = (EditText)d.findViewById(R.id.allowanceinput);
        denom.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String yourid = helper.logcount()+"";
                if ((!am.getText().toString().equals("")) || (!am.getText().toString().equals("0"))){
                    String den = am.getText().toString();
                    double totalamount = Double.valueOf(am.getText().toString());
                    gen.addAllowance(totalamount+"", helper.logcount()+"", datereturn(), "1");
                    all_list();
                    dialog.dismiss();
                }
            }
        });
        denom.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        denom.setTitle("Add allowance disbursed");
        denom.show();
    }

    public void customAlert(String txt){
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

    public String datereturn(){
        Date datetalaga = Calendar.getInstance().getTime();
        SimpleDateFormat writeDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        writeDate.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        String findate = writeDate.format(datetalaga);

        return findate;
    }

    public void exp_capture() {
        try {
            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(cameraIntent, exp_request);
        } catch (Exception e) {
            ActivityCompat.requestPermissions(Remitt.this,
                    new String[]{Manifest.permission.CAMERA}, exp_request);
        }
    }

    public void camera_capture() {
        try {
            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(cameraIntent, camera_request);
        } catch (Exception e) {
            ActivityCompat.requestPermissions(Remitt.this,
                    new String[]{Manifest.permission.CAMERA}, camera_request);
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
        getMenuInflater().inflate(R.menu.remitt, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.save_remit) {
            if (value.equals("OIC")){
                viewtoBank();
            }else{
                viewtoOIC();
            }
        }else if (id == R.id.remit_list){
            startActivity(new Intent(getApplicationContext(), Remittancelist.class));
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        try{
            if (requestCode == SETTINGS_ACTION) {
                if (resultCode == Preferences.RESULT_CODE_THEME_UPDATED) {
                    finish();
                    startActivity(getIntent());
                    return;
                }
            }
            else if (requestCode == camera_request){
                if (requestCode == camera_request && resultCode == Activity.RESULT_OK) {
                    Bitmap photo = (Bitmap) data.getExtras().get("data");
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    photo.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    bytimg = stream.toByteArray();
                    if (bytimg != null) {
                        //img.setImageBitmap(photo);
                    }
                    Log.e("camera", "success " + bytimg );
                }
            }
            else if (requestCode == exp_request  && resultCode == Activity.RESULT_OK){
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                photo.compress(Bitmap.CompressFormat.PNG, 100, stream);
                bytexp = stream.toByteArray();
                if (bytexp != null) {
                    eximg.setImageBitmap(photo);
                }
                Log.e("cameraexp", "success " + bytexp );
            }else
                super.onActivityResult(requestCode, resultCode, data);

        }catch (Exception e){}
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == camera_request) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(), "camera permission granted", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }else if (requestCode == exp_request) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(), "camera permission granted", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    //sums
    public double sumExpense(){
        double result = 0;
        SQLiteDatabase db = gen.getReadableDatabase();
        String query = " SELECT SUM("+gen.rem_trans_amount+") FROM "
                +gen.tbname_remittance_trans+" WHERE "+gen.rem_trans_type
                +" = 'expense' AND "+gen.rem_trans_stat+" = '1' AND "+gen.rem_trans_remittanceid
                +" = '"+helper.logcount()+"'";
        Cursor x = db.rawQuery( query, null);
        if (x.moveToFirst()) {
            result = x.getDouble(0);
        }
        return  result;
    }

    public double sumAllFromDriver(){
        double result = 0;
        SQLiteDatabase db = gen.getReadableDatabase();
        String query = " SELECT SUM("+gen.rem_trans_amount+") FROM "
                +gen.tbname_remittance_trans+" WHERE "+gen.rem_trans_type
                +" = 'fromDriver' AND "+gen.rem_trans_stat+" = '1' AND "+gen.rem_trans_remittanceid
                +" = '"+helper.logcount()+"'";
        Cursor x = db.rawQuery( query, null);
        if (x.moveToFirst()) {
            result = x.getDouble(0);
        }
        return  result;
    }

    public void deleteRemitId(String id){
        SQLiteDatabase db = gen.getWritableDatabase();
        db.delete(gen.tbname_remittance_trans,
                gen.rem_trans_id+" = '"+id+"'", null);
        Log.e("remId", id);
        db.close();
    }

    //allowance
    public double getAllowance(){
        double amount = 0;
        SQLiteDatabase db = gen.getReadableDatabase();
        String query = " SELECT SUM("+gen.all_amount+") FROM "+gen.tbname_allowance
                +" WHERE "+gen.all_createdby+" = '"+helper.logcount()+"' AND "+gen.all_stat
                +" = '1'";
        Cursor c = db.rawQuery( query, null);
        if (c.moveToFirst()){
            amount = c.getDouble(0);
        }
        return amount;
    }

    //get amount to remit based on collection
    public double getAmounttoRemit(){
        double a = 0;
        SQLiteDatabase db = gen.getReadableDatabase();
        String query = " SELECT SUM("+gen.rem_amount_amount+") FROM "+gen.tbname_remitttances_amount
                +" WHERE "+gen.rem_amount_stat+" = '0' AND "
                +gen.rem_amount_by+" = '"+helper.logcount()+"'";
        Cursor c = db.rawQuery(query, null);
        if (c.moveToFirst()){
            a = c.getDouble(0);
        }
        return a;
    }

    // update remittance amount
    public void updateAll(String x){
        SQLiteDatabase db = gen.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(gen.rem_trans_stat, "2");
        db.update(gen.tbname_remittance_trans, cv,
                gen.rem_trans_remittanceid+" = '"+x+"' AND "+gen.rem_trans_stat+" = '1'", null);
        db.close();
    }

    //signature contents
    //signature view
    // Function for Digital Signature
    public void dialogsign(final String c){
        tempDir = Environment.getExternalStorageDirectory() + "/"
                + getResources().getString(R.string.app_name) + "/";
        ContextWrapper cw = new ContextWrapper(Remitt.this);
        File directory = cw.getDir(getResources().getString(R.string.app_name),
                Context.MODE_PRIVATE);

        prepareDirectory();
        uniqueId = getTodaysDate() + "_" + getCurrentTime() + "_" + Math.random();
        current = uniqueId + ".png";
        //mypath= new File(directory,current);


        mContent = (LinearLayout)dialog.findViewById(R.id.linearLayout);
        mSignature = new Remitt.signature(Remitt.this, null);
        mSignature.setBackgroundColor(Color.WHITE);
        mContent.addView(mSignature, ViewGroup.LayoutParams.FILL_PARENT,
                ViewGroup.LayoutParams.FILL_PARENT);
        TextView signmess = (TextView)dialog.findViewById(R.id.txtsign);
        TextView signame = (TextView)dialog.findViewById(R.id.txtname);
        mClear = (Button)dialog.findViewById(R.id.clearsign);
        mGetSign = (Button)dialog.findViewById(R.id.savesign);
        mGetSign.setEnabled(false);
        mCancel = (Button)dialog.findViewById(R.id.cancelsign);
        if (c.equals("tooic")) {
            signmess.setText(Html.fromHtml(
                    "<i><small>This is to acknowledge receipt of the remittance (deposit slip / cash). " +
                            "I confirm having checked the breakdown and supporting documents.</small></i>"));
            signame.setText("Name: " + oicfullname);
        }else{
            signame.setVisibility(View.INVISIBLE);
            signmess.setVisibility(View.INVISIBLE);
        }
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
                    if (c.equals("tooic")) {
                        String type = "OIC";
                        String trtype = "Remittance";
                        String amount = netcollect.getText().toString();
                        gen.addNewRemittance(type, oicname, "", "", amount,
                                off, helper.logcount() + "", datereturn(), "0", "1");
                        updateAll(helper.logcount()+"");
                        gen.updateRemAmountStat(helper.logcount()+"", "1");

                        gen.addTransactions(trtype, ""+helper.logcount(),
                                "Added new remittance to "+type, datereturn(), getCurrentTime());
                        all_list();
                        recreate();
                    }else if (c.equals("tobank")){
                        String ty = "BANK";
                        String trtype = "Remittance";
                        String amount = amounthold.getText().toString();
                        gen.addNewRemittance(ty, bank, acc_name, acc_num, amount, off,
                                helper.logcount()+"", datereturn(), "0", "1");
                        updateAll(helper.logcount()+"");
                        gen.addTransactions(trtype, ""+helper.logcount(),
                                "Added new remittance to "+ty, datereturn(), getCurrentTime());
                        all_list();
                        recreate();
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
                //bytes.setText(off+"");

                mFileOutStream.flush();
                mFileOutStream.close();
                //String url = MediaStore.Images.Media.insertImage(ReservationData.this.getApplicationContext().getContentResolver(), mBitmap, "title", null);
                Log.e("log_tag","bytes: " + off);

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

    //view per user //sales driver or OIC
    public void viewtoBank(){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(Remitt.this);
        LayoutInflater inflater = getLayoutInflater();
        View d = inflater.inflate(R.layout.tobank,null);
        dialogBuilder.setView(d);
        bankname = (EditText)d.findViewById(R.id.banknameinput);
        accntname = (EditText)d.findViewById(R.id.accntnameinput);
        accntnum = (EditText)d.findViewById(R.id.accntnumberinput);

        dialogBuilder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface d, int which) {
                bank = bankname.getText().toString();
                acc_name = accntname.getText().toString();
                acc_num = accntnum.getText().toString();
                if ((bank.equals("")) || (acc_name.equals("")) || (acc_num.equals(""))){
                    String r = "Please fill out the fields correctly.";
                    customAlert(r);
                }else{
                    dialog = new Dialog(Remitt.this);
                    // Removing the features of Normal Dialogs
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setContentView(R.layout.capt_remit_sign);
                    dialog.setCancelable(true);
                    dialogsign("tobank");
                    d.dismiss();
                    Log.e("tobank", "bankname: "+bank+", accname: "+acc_name
                            +"accnum: "+acc_num);
                }
            }
        });
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialogBuilder.setCancelable(false);
        dialogBuilder.setTitle("Complete remittance");
        dialogBuilder.show();
    }

    public void viewtoOIC(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(Remitt.this);
        builder.setTitle("Confirmation to proceed.");
        LayoutInflater inflater = getLayoutInflater();
        View d = inflater.inflate(R.layout.remittance_layoutswitch,null);
        builder.setView(d);
        final Spinner types = (Spinner)d.findViewById(R.id.itemtype);
        String message = "The selected OIC shall sign and will be responsible for the stated amount.";
        final ArrayList<LinearItem> name = gen.getOICname(helper.getBranch(helper.logcount()+""));
        LinearList list = new LinearList(getApplicationContext(), name);
        types.setAdapter(list);
        types.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                headoicname = (TextView)view.findViewById(R.id.dataid);
                oicname = headoicname.getText().toString();
                oicfullname = name.get(position).getTopitem();
                Log.e("oicname", oicfullname);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                headoicname = (TextView)parent.findViewById(R.id.dataid);
                Log.e("oicname", oicname);
            }
        });
        builder.setMessage(message)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface d, int id) {
                        dialog = new Dialog(Remitt.this);
                        // Removing the features of Normal Dialogs
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialog.setContentView(R.layout.capt_remit_sign);
                        dialog.setCancelable(true);
                        dialogsign("tooic");
                        d.dismiss();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        builder.setCancelable(false);
        builder.create().show();
    }

    public double calculateNet(double exp, double amnt){
        double total_net = 0;
        total_net = ((amnt) - (exp));
        return total_net;
    }

    public double calculateNetOIC(double exp, double amnt){
        double total_net = 0;
        total_net = ((amnt) + (exp));
        return total_net;
    }

    //alert full image
    public void alertImage(byte[] image, final String idt){
        try {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(Remitt.this);
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
                    gen.deleteImage(idt);
                    all_list();
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

    //get all drivers
    public String[] getAllDrivers(String branch) {
        SQLiteDatabase db = gen.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + gen.tbname_employee
                + " WHERE " + gen.emp_post + " = 'Sales Driver' AND "+gen.emp_branch+" = '"+branch+"'", null);
        cursor.moveToFirst();
        ArrayList<String> numbers = new ArrayList<String>();
        while (!cursor.isAfterLast()) {
            String fullname = cursor.getString(cursor.getColumnIndex(gen.emp_first))+" "
                    +cursor.getString(cursor.getColumnIndex(gen.emp_last));
            numbers.add(fullname);
            cursor.moveToNext();
        }
        cursor.close();
        return numbers.toArray(new String[numbers.size()]);
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
