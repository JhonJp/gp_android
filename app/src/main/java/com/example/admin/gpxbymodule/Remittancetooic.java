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
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
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
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

public class Remittancetooic extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    HomeDatabase helper;
    String value;
    NavigationView navigationView;
    GenDatabase gen;
    RatesDB rate;
    ProgressDialog progressBar;
    String link;
    ListView lv;
    RemittanceAdapter adapter;
    FloatingActionButton add;
    String getselected;
    int camera_request = 1, exp_request = 0;
    byte[] bytimg, bytexp;
    TextView img, exptotal, denomt, eximg, amounthold, headoicname;
    String oicname = "";
    Button send;
    EditText bankname,accntname,accntnum;

    //signature variables
    LinearLayout mContent;
    Remittancetooic.signature mSignature;
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

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remittancetooic);
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
        lv = (ListView)findViewById(R.id.remitlists);
        add = (FloatingActionButton)findViewById(R.id.fab);
        exptotal = (TextView)findViewById(R.id.exptotalamount);
        denomt = (TextView)findViewById(R.id.denomtotal);
        amounthold = (TextView)findViewById(R.id.amount);
        send = (Button)findViewById(R.id.sendremittance);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if (helper.logcount() != 0){
            value = helper.getRole(helper.logcount());
            Log.e("role ", value);
        }

        fabclicks();
        all_list();
        confirmRemit();

        if (value.equals("OIC")) {
            this.setTitle("Remittance to BANK");
            Log.e("expenses", sumExpense() + "");
            double amountwith = (sumFromDriver() - sumExpense());
            Log.e("fromDriver", sumFromDriver() + "");
            amounthold.setText((amountwith + getAllowance()) + "0");
        }else{
            this.setTitle("Remittance to OIC");
            //insertRemittanceDriverAmount();
            amounthold.setText((getAmounttoRemit() - sumExpense()) + (getAllowance()) + "0");

        }

        addDefaultItems();

        sidenavMain();
        subMenu();
        setNameMail();

    }

    public void sidenavMain(){
        ListView lv=(ListView)findViewById(R.id.optionsList);//initialization of listview

        final ArrayList<HomeList> listitem = helper.getData(value);

        NavAdapter ad = new NavAdapter(Remittancetooic.this, listitem);
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
                        final AlertDialog.Builder builder = new AlertDialog.Builder(Remittancetooic.this);
                        builder.setMessage("Confirm logout?")
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {

                                        helper.logout();

                                        startActivity(new Intent(Remittancetooic.this, Login.class));
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

    public void camera_capture() {
        try {
            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(cameraIntent, camera_request);
        } catch (Exception e) {
            ActivityCompat.requestPermissions(Remittancetooic.this,
                    new String[]{Manifest.permission.CAMERA}, camera_request);
        }
    }

    public void exp_capture() {
        try {
            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(cameraIntent, exp_request);
        } catch (Exception e) {
            ActivityCompat.requestPermissions(Remittancetooic.this,
                    new String[]{Manifest.permission.CAMERA}, exp_request);
        }
    }

    public void all_list(){
        try {
            final ArrayList<RemittanceHolder> result = getAllRemittanceTrans(helper.logcount() + "");
            adapter = new RemittanceAdapter(getApplicationContext(), result);
            lv.setAdapter(adapter);
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    final String iddel = result.get(position).getId();
                    final AlertDialog.Builder builder = new AlertDialog.Builder(Remittancetooic.this);
                    builder.setTitle("Delete thi data?");
                    builder.setMessage("Please confirm to delete this data.");
                    builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    deleteRemitId(iddel);
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
                }
            });
            exptotal.setText(Html.fromHtml("<small>"+sumExpense()+"0</small>"));
            denomt.setText(Html.fromHtml("<small>"+sumDenom()+"0</small>"));
        }catch (Exception e){}
    }

    public void fabclicks(){
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder denom = new AlertDialog.Builder(Remittancetooic.this);
                LayoutInflater inflater = getLayoutInflater();
                View d = inflater.inflate(R.layout.remittance_layoutswitch,null);
                denom.setView(d);
                final Spinner types = (Spinner)d.findViewById(R.id.itemtype);
                String[] items = new String[]{"Select here","New Denomination", "New Expenses", "Allowance disbursed"};
                ArrayAdapter<String> adapter =
                        new ArrayAdapter<>(getApplicationContext(), R.layout.spinneritem, items);
                adapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
                types.setAdapter(adapter);
                types.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        getselected = types.getSelectedItem().toString();
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
                denom.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                denom.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (getselected.equals("New Denomination")){
                            dialog.dismiss();
                            viewDenom();
                        }
                        else if (getselected.equals("New Expenses")){
                            dialog.dismiss();
                            viewExp();
                        }
                        else if (getselected.equals("Allowance disbursed")){
                            dialog.dismiss();
                            viewDisbursed();
                        }
                        else{
                            String x = "Please select transaction.";
                            customAlert(x);
                        }
                    }
                });
                denom.setTitle("Select transaction");
                denom.show();
            }
        });
    }

    public void viewDenom(){
        final AlertDialog.Builder denom = new AlertDialog.Builder(Remittancetooic.this);
        LayoutInflater inflater = getLayoutInflater();
        View d = inflater.inflate(R.layout.denomination,null);
        denom.setView(d);
        final EditText denomtype = (EditText)d.findViewById(R.id.denomtypeinput);
        img = (TextView)d.findViewById(R.id.images);
        final EditText quan = (EditText)d.findViewById(R.id.denomquantinput);
        FrameLayout capt = (FrameLayout)d.findViewById(R.id.imagecapt);
        capt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                camera_capture();
            }
        });
        if (bytimg != null) {
            img.setText("Image : " + bytimg);
        }
        denom.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String yourid = helper.logcount()+"";
                if ((!quan.getText().toString().equals(""))
                        && (!denomtype.getText().toString().equals(""))
                        && (!quan.getText().toString().equals("0"))
                        && (!denomtype.getText().toString().equals("0"))){
                    String den = denomtype.getText().toString();
                    String q = quan.getText().toString();
                    double totalamount = ((Integer.valueOf(den)) * Integer.valueOf(q));
                    gen.addRemitTrans(yourid, "denom", den, q, "",
                            totalamount+"", "", bytimg, "1");
                    bytimg = null;
                    dialog.dismiss();
                    recreate();
                    all_list();
                }
            }
        });
        denom.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        denom.setTitle("Add new denomination");
        denom.show();
    }

    public void viewDisbursed(){
        final AlertDialog.Builder denom = new AlertDialog.Builder(Remittancetooic.this);
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
                    dialog.dismiss();
                    recreate();
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

    public void addnewItemExpense(){
        final AlertDialog.Builder denom = new AlertDialog.Builder(Remittancetooic.this);
        LayoutInflater inflater = getLayoutInflater();
        View d = inflater.inflate(R.layout.additem,null);
        denom.setView(d);
        final EditText am = (EditText)d.findViewById(R.id.allowanceinput);
        denom.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String yourid = helper.logcount()+"";
                if (!am.getText().toString().equals("")) {
                    addItemsTodb(am.getText().toString());
                    dialog.dismiss();
                    viewExp();
                }
            }
        });
        denom.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        denom.setTitle("Add new expense item");
        denom.show();
    }

    public void viewExp(){
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(Remittancetooic.this);
        LayoutInflater inflater = getLayoutInflater();
        View d = inflater.inflate(R.layout.expenseadd,null);
        dialogBuilder.setView(d);
        final Spinner itemname = (Spinner)d.findViewById(R.id.items);
        final ImageButton addi = (ImageButton)d.findViewById(R.id.additem);
        final EditText amount = (EditText)d.findViewById(R.id.amountinput);
        final EditText description = (EditText)d.findViewById(R.id.descrip);
        eximg = (TextView) d.findViewById(R.id.expimg);
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
                String desc = description.getText().toString();
                if ((item.equals("")) || (am.equals("")) || (desc.equals(""))){
                    String r = "Please fill out the fields correctly.";
                    customAlert(r);
                }else{
                    double a = Double.valueOf(amounthold.getText().toString());
                    if (a <= 0){
                        String r = "You have zero amount, please add amount.";
                        customAlert(r);
                    }else {
                        gen.addRemitTrans("" + helper.logcount(), "expense", "", "",
                                item, am, desc, bytexp, "1");
                        bytexp = null;
                        all_list();
                        dialog.dismiss();
                        recreate();
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
        addi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addnewItemExpense();
                td.dismiss();
            }
        });
    }

    public void viewtoBank(){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(Remittancetooic.this);
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
                    dialog = new Dialog(Remittancetooic.this);
                    // Removing the features of Normal Dialogs
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setContentView(R.layout.capture_sign);
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
        dialogBuilder.setTitle("Complete remittance");
        dialogBuilder.show();
    }

    public void viewtoOIC(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(Remittancetooic.this);
        builder.setTitle("Confirmation to proceed.");
        LayoutInflater inflater = getLayoutInflater();
        View d = inflater.inflate(R.layout.remittance_layoutswitch,null);
        builder.setView(d);
        final Spinner types = (Spinner)d.findViewById(R.id.itemtype);
        ArrayList<LinearItem> name = gen.getOICname();
        LinearList list = new LinearList(getApplicationContext(), name);
        types.setAdapter(list);
        types.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                headoicname = (TextView)view.findViewById(R.id.dataid);
                oicname = headoicname.getText().toString();
                Log.e("oicname", oicname);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                headoicname = (TextView)parent.findViewById(R.id.dataid);
                Log.e("oicname", oicname);
            }
        });
        builder.setMessage(Html.fromHtml("<i>"+
                "Please confirm if you wish to remit the amount stated."+"</i>"))
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface d, int id) {
                        dialog = new Dialog(Remittancetooic.this);
                        // Removing the features of Normal Dialogs
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialog.setContentView(R.layout.capture_sign);
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
        builder.create().show();
    }

    public String datereturn(){
        Date datetalaga = Calendar.getInstance().getTime();
        SimpleDateFormat writeDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        writeDate.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        String findate = writeDate.format(datetalaga);

        return findate;
    }

    public void confirmRemit(){
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (amounthold.getText().toString().equals("0.00")){
                    String err = "Amount is equal to zero, please add amount thank you.";
                    customAlert(err);
                }else {
                    if (value.equals("OIC")) {
                        viewtoBank();
                    } else {
                        viewtoOIC();
                    }
                }
            }
        });
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

    @Override
    public void onBackPressed(){
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
        getMenuInflater().inflate(R.menu.remittancetooic, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (!value.equals("OIC")) {
            menu.findItem(R.id.remitsync).setVisible(false);
            menu.findItem(R.id.remitlistview).setVisible(true);
        }else{
            menu.findItem(R.id.remitsync).setVisible(true);
            menu.findItem(R.id.remitlistview).setVisible(true);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.remitlistview) {
            startActivity(new Intent(getApplicationContext(), Remittancelist.class));
            finish();
        }else if (id == R.id.remitsync){
            network();
        }else if (id ==  R.id.passincident){
            Intent i = new Intent(this, Incident.class);
            Bundle bundle = new Bundle();
            bundle.putString("module", "Remittance");
            //Add the bundle to the intent
            i.putExtras(bundle);
            startActivity(i);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item){
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        try{
            if (requestCode == camera_request){
                if (requestCode == camera_request && resultCode == Activity.RESULT_OK) {
                    Bitmap photo = (Bitmap) data.getExtras().get("data");
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    photo.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    bytimg = stream.toByteArray();
                    if (bytimg != null) {
                        img.setText("Image : " + bytimg);
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
                        eximg.setText("Image : " + bytexp);
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

    public double sumDenom(){
        double result = 0;
        SQLiteDatabase db = gen.getReadableDatabase();
        String query = " SELECT SUM("+gen.rem_trans_quantity+") FROM "
                +gen.tbname_remittance_trans+" WHERE "+gen.rem_trans_type
                +" = 'denom' AND "+gen.rem_trans_stat+" = '1' AND "+gen.rem_trans_remittanceid
                +" = '"+helper.logcount()+"'";
        Cursor x = db.rawQuery( query, null);
        if (x.moveToFirst()) {
            result = x.getDouble(0);
        }
        return  result;
    }

    public double sumFromDriver(){
        double result = 0;
        SQLiteDatabase db = gen.getReadableDatabase();
        String query = " SELECT SUM("+gen.rem_trans_amount+") FROM "
                +gen.tbname_remittance_trans+" WHERE "+gen.rem_trans_type
                +" = 'fromDriver' AND "+gen.rem_trans_stat+" = '1' AND "
                +gen.rem_trans_remittanceid+" = '"+helper.logcount()+"'";
        Cursor x = db.rawQuery( query, null);
        if (x.moveToFirst()) {
            result = x.getDouble(0);
        }
        return  result;
    }

    public ArrayList<RemittanceHolder> getAllRemittanceTrans(String by){
        ArrayList<RemittanceHolder> results = new ArrayList<>();
        SQLiteDatabase db = gen.getReadableDatabase();
        Cursor res = db.rawQuery(" SELECT * FROM " + gen.tbname_remittance_trans
                +" WHERE "+gen.rem_trans_remittanceid+" = '"+by+"' AND "+gen.rem_trans_stat
                +" = '1'", null);
        res.moveToFirst();
        while (!res.isAfterLast()) {
            String type = res.getString(res.getColumnIndex(gen.rem_trans_type));
            String id = res.getString(res.getColumnIndex(gen.rem_trans_id));
            if (type.equals("denom")){
                String topitem = res.getString(res.getColumnIndex(gen.rem_trans_denoms));
                String subitem = res.getString(res.getColumnIndex(gen.rem_trans_denoms))+" * "
                        +res.getString(res.getColumnIndex(gen.rem_trans_quantity))+" = "
                                    +res.getString(res.getColumnIndex(gen.rem_trans_amount));
                RemittanceHolder rem = new RemittanceHolder( id, topitem, subitem, type);
                results.add(rem);
            }else if (type.equals("expense")){
                String topitem = res.getString(res.getColumnIndex(gen.rem_trans_itemname));
                String subitem = "amount is: "+res.getString(res.getColumnIndex(gen.rem_trans_amount));
                RemittanceHolder rem = new RemittanceHolder( id, topitem, subitem, type);
                results.add(rem);
            }
            else if (type.equals("fromDriver")){
                String topitem = "from Driver";
                String subitem = "amount is: "+res.getString(res.getColumnIndex(gen.rem_trans_amount));
                RemittanceHolder rem = new RemittanceHolder( id, topitem, subitem, type);
                results.add(rem);
            }
            res.moveToNext();
        }
        res.close();
        return results;
    }

    public void deleteRemitId(String id){
        SQLiteDatabase db = gen.getWritableDatabase();
        db.delete(gen.tbname_remittance_trans,
                gen.rem_trans_id+" = '"+id+"'", null);
        Log.e("remId", id);
        db.close();
    }

    public void updateAll(String x){
        SQLiteDatabase db = gen.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(gen.rem_trans_stat, "2");
        db.update(gen.tbname_remittance_trans, cv,
                gen.rem_trans_remittanceid+" = '"+x+"' AND "+gen.rem_trans_stat+" = '1'", null);
        db.close();
    }

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

    public void updateAllAllowance(){
        SQLiteDatabase db = gen.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(gen.all_stat, "0");
        db.update(gen.tbname_allowance, cv, gen.all_createdby+" = '"+helper.logcount()+"'" +
                " AND "+gen.all_stat+" = '1'",null);
        db.close();
    }

    //signature view
    // Function for Digital Signature
    public void dialogsign(final String c){
        tempDir = Environment.getExternalStorageDirectory() + "/" + getResources().getString(R.string.app_name) + "/";
        ContextWrapper cw = new ContextWrapper(Remittancetooic.this);
        File directory = cw.getDir(getResources().getString(R.string.app_name), Context.MODE_PRIVATE);

        prepareDirectory();
        uniqueId = getTodaysDate() + "_" + getCurrentTime() + "_" + Math.random();
        current = uniqueId + ".png";
        mypath= new File(directory,current);


        mContent = (LinearLayout)dialog.findViewById(R.id.linearLayout);
        mSignature = new Remittancetooic.signature(Remittancetooic.this, null);
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
                    if (c.equals("tooic")) {
                        String type = "OIC";
                        String trtype = "Remittance";
                        String amount = amounthold.getText().toString();
                        gen.addNewRemittance(type, oicname, "", "", amount,
                                off, helper.logcount() + "", datereturn(), "0", "1");
                        updateAll(helper.logcount()+"");
                        gen.updateRemAmountStat(helper.logcount()+"", "1");

                        gen.addTransactions(trtype, ""+helper.logcount(),
                                "Added new remittance to "+type, datereturn(), getCurrentTime());

                        updateAllAllowance();
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

                        updateAllAllowance();
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

    public void getRemittanceOIC(){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                //START THREAD FOR remittance from driver
                try {
                    String resp = null;
                    String link = helper.getUrl();
                    String series = "http://"+link+"/api/remittance/oic_get.php?id="+helper.logcount();
                    URL url = new URL(series);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    // read the response
                    InputStream in = new BufferedInputStream(conn.getInputStream());
                    resp = convertStreamToString(in);

                    if (resp != null) {
                        Log.e("oicremittances", "oicremittances : " + resp);
                        JSONArray jsonArray = new JSONArray(resp);
                        for(int i=0; i<jsonArray.length(); i++){
                            JSONObject json_data = jsonArray.getJSONObject(i);
                            String amount = json_data.getString("amount");
//                            gen.addRemitTrans(helper.logcount()+"", "fromDriver",
//                                    "","", "",amount, "",null,"1");
//                            Log.e("oicremittance", " amount:"+amount);
                        }

                        if (!conn.getResponseMessage().equals("OK")){
                            conn.disconnect();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progressBar.dismiss();
                                    final AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
                                    builder.setTitle("Information confirmation")
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
                        }
                    } else {
                        Log.e("Error", "Couldn't get data from server.");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String t = "Couldn't get data from server.";
                                customAlert(t);
                            }
                        });
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                //END THREAD FOR remittances from driver series
            }
        });
        thread.start();
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
    private boolean isNetworkAvailable(){
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void network(){
        if (isNetworkAvailable()== true){
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
                    getRemittanceOIC();
                    Thread.sleep(10000);
                }
                catch (Exception e) { } // Just catch the InterruptedException

                handler.post(new Runnable() {
                    public void run() {
                        progressBar.dismiss();
                        final AlertDialog.Builder builder =
                                new AlertDialog.Builder(Remittancetooic.this);
                        builder.setTitle("Information confirmation")
                                .setMessage("Data has been successfully updated, thank you.")
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        recreate();
                                        dialog.dismiss();
                                    }
                                })
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
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
        }).start();
    }

    public void addItemsTodb(String name){
        SQLiteDatabase db = rate.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(rate.expit_name, name);
        cv.put(rate.expit_type, "0");
        db.insert(rate.tbname_exp_item, null, cv);
        db.close();
    }

    public void addDefaultItems(){
        ArrayList<String> itemnames = new ArrayList<>();
        itemnames.add("Gasoline");
        itemnames.add("Parking");
        itemnames.add("Snacks");
        itemnames.add("Meal");
        for(String name:itemnames){
            addItemsTodb(name);
        }
    }

}
