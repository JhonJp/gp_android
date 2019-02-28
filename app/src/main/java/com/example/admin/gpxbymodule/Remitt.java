package com.example.admin.gpxbymodule;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.text.Html;
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
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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
    TextView img, exptotal, denomt, eximg, amounthold;
    Button send;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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

        exptotal = (TextView)findViewById(R.id.exptotalamount);
        denomt = (TextView)findViewById(R.id.denomtotal);
        amounthold = (TextView)findViewById(R.id.amount);
        send = (Button)findViewById(R.id.sendremittance);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        fabclicks();


        if (helper.logcount() != 0){
            value = helper.getRole(helper.logcount());
            Log.e("role ", value);
        }
        if (value.equals("OIC")) {
            this.setTitle("Remittance");

        }else{
            this.setTitle("Remittance");
            //insertRemittanceDriverAmount();
            amounthold.setText(((getAmounttoRemit()) +"0"));

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
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
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
                }
            });
        }catch (Exception e){}
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
            if (type.equals("denom")){
                String topitem = res.getString(res.getColumnIndex(gen.rem_trans_denoms));
                String subitem = res.getString(res.getColumnIndex(gen.rem_trans_denoms))+" * "
                        +res.getString(res.getColumnIndex(gen.rem_trans_quantity))+" = "
                        +res.getString(res.getColumnIndex(gen.rem_trans_amount));
                DataModel rem = new DataModel(topitem, subitem, id);
                results.add(rem);
            }else if (type.equals("expense")){
                String topitem = res.getString(res.getColumnIndex(gen.rem_trans_itemname));
                String subitem = res.getString(res.getColumnIndex(gen.rem_trans_amount));
                DataModel rem = new DataModel(topitem, subitem, id);
                results.add(rem);
            }
            else if (type.equals("fromDriver")){
                String topitem = "From Driver";
                String subitem = res.getString(res.getColumnIndex(gen.rem_trans_amount));
                DataModel rem = new DataModel(topitem, subitem, id);
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
                final AlertDialog.Builder denom = new AlertDialog.Builder(Remitt.this);
                LayoutInflater inflater = getLayoutInflater();
                View d = inflater.inflate(R.layout.remittance_layoutswitch,null);
                denom.setView(d);
                final Spinner types = (Spinner)d.findViewById(R.id.itemtype);
                String[] items = new String[]{"Select here","New Expenses", "Allowance disbursed"};
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
                        if (getselected.equals("New Expenses")){
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

    public void viewDenom(){
        final AlertDialog.Builder denom = new AlertDialog.Builder(Remitt.this);
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
                    // all_list();
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

    public void viewExp(){
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(Remitt.this);
        LayoutInflater inflater = getLayoutInflater();
        View d = inflater.inflate(R.layout.expenseadd,null);
        dialogBuilder.setView(d);
        final Spinner itemname = (Spinner)d.findViewById(R.id.items);
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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
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

}
