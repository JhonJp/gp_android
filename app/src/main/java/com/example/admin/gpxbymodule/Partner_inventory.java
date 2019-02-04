package com.example.admin.gpxbymodule;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;

public class Partner_inventory extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    NavigationView navigationView;
    HomeDatabase helper;
    GenDatabase gen;
    RatesDB rate;
    String value;
    ListView lv;
    ArrayList<ListItem> listitem;
    TextView to;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_partner_inventory);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        helper = new HomeDatabase(this);
        gen = new GenDatabase(this);
        rate = new RatesDB(this);
        lv = (ListView)findViewById(R.id.lv);
        to = (TextView)findViewById(R.id.total);
        try {
            if (helper.logcount() != 0) {
                value = helper.getRole(helper.logcount());
                Log.e("role ", value);
            }

        }catch (Exception e){}

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        spinnerlist();
        sidenavMain();
        subMenu();
        setNameMail();

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

        NavAdapter ad = new NavAdapter(Partner_inventory.this, listitem);
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
                        final AlertDialog.Builder builder = new AlertDialog.Builder(Partner_inventory.this);
                        builder.setMessage("Confirm logout?")
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {

                                        helper.logout();

                                        startActivity(new Intent(Partner_inventory.this, Login.class));
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
                        startActivity(new Intent(Partner_inventory.this, Home.class));
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
        try {
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            switch (data) {
                case "Acceptance":
                    if (value.equals("OIC")) {
                        startActivity(new Intent(this, Acceptance.class));
                        finish();
                    } else if (value.equals("Warehouse Checker")) {
                        startActivity(new Intent(this, Acceptance.class));
                        finish();
                    } else if (value.equals("Partner Portal")) {
                        startActivity(new Intent(this, Partner_acceptance.class));
                        finish();
                    }
                    break;
                case "Distribution":
                    if (value.equals("Partner Portal")){
                        startActivity(new Intent(this, Partner_distribution.class));
                        finish();
                    }else{
                        startActivity(new Intent(this, Distribution.class));
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
                    bundle.putString("module", "Inventory");
                    //Add the bundle to the intent
                    i.putExtras(bundle);
                    startActivity(i);
                    finish();
                    break;
                case "Transactions":
                    if (value.equals("OIC")) {
                        startActivity(new Intent(this, Oic_Transactions.class));
                        finish();
                    } else if (value.equals("Sales Driver")) {
                        startActivity(new Intent(this, Driver_Transactions.class));
                        finish();
                    } else if (value.equals("Warehouse Checker")) {
                        startActivity(new Intent(this, Checker_transactions.class));
                        finish();
                    }
                    break;
                case "Inventory":
                    if (value.equals("OIC")) {
                        startActivity(new Intent(this, Oic_inventory.class));
                        finish();
                    } else if (value.equals("Sales Driver")) {
                        startActivity(new Intent(this, Driver_Inventory.class));
                        finish();
                    } else if (value.equals("Warehouse Checker")) {
                        startActivity(new Intent(this, Checker_Inventory.class));
                        finish();
                    } else if (value.equals("Partner Portal")) {
                        drawer.closeDrawer(GravityCompat.START);
                    }
                    break;
                case "Loading/Unloading":
                    if (value.equals("Warehouse Checker")) {
                        startActivity(new Intent(this, Load_home.class));
                        finish();
                    } else if (value.equals("Partner Portal")) {
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
        }catch (Exception e){}
    }

    public void spinnerlist(){
        try {
            listitem = getBoxes("1", "0");
            TableAdapter ad = new TableAdapter(this, listitem);
            lv.setAdapter(ad);
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String topi = listitem.get(position).getTopitem();
                    returnViewItems( topi,"1", "0");
                }
            });
            to.setText(Html.fromHtml("<small>Overall total: </small>" +
                    "<b>" + countAll("1", "0") + " box(s) </b>"));
        }catch (Exception e){}
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
        getMenuInflater().inflate(R.menu.partner_inventory, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id ==  R.id.passincident){
            Intent i = new Intent(this, Incident.class);
            Bundle bundle = new Bundle();
            bundle.putString("module", "Inventory");
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
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public String readBranch(){
        String branchname = "";
        SQLiteDatabase db = rate.getReadableDatabase();
        Cursor x = db.rawQuery(" SELECT * FROM "+rate.tbname_branch
                +" WHERE "+rate.branch_id+" = '"+helper.getBranch(""+helper.logcount())+"'", null);
        if (x.moveToNext()){
            branchname = x.getString(x.getColumnIndex(rate.branch_name));
        }
        return branchname;
    }

    public ArrayList<ListItem> getBoxes(String type, String stat){
        ArrayList<ListItem> results = new ArrayList<>();
        try {
            SQLiteDatabase db = gen.getReadableDatabase();
            String r = " SELECT " + gen.tbname_boxes + "." + gen.box_name + ", "
                    + gen.tbname_partner_inventory + "." + gen.partinv_id + ", "
                    + " COUNT (" + gen.tbname_partner_inventory + "." + gen.partinv_boxtype + ")"
                    + " FROM " + gen.tbname_partner_inventory
                    + " LEFT JOIN " + gen.tbname_boxes + " ON "
                    + gen.tbname_boxes + "." + gen.box_id + " = " + gen.tbname_partner_inventory
                    + "." + gen.partinv_boxtype
                    + " WHERE " + gen.partinv_boxtype_fillempty + " = '" + type + "' AND "
                    + gen.partinv_stat + " = '" + stat + "' GROUP BY " + gen.partinv_boxtype;
            Cursor c = db.rawQuery(r, null);
            c.moveToFirst();
            while (!c.isAfterLast()) {
                String id = c.getString(1);
                String topitem = c.getString(c.getColumnIndex(gen.box_name));
                String subitem = c.getString(2);
                ListItem list = new ListItem(id, topitem, subitem, null);
                results.add(list);
                c.moveToNext();
            }
        }catch (Exception e){}
        return results;
    }

    public String countAll(String type, String stat){
        SQLiteDatabase db = gen.getReadableDatabase();
        String que = " SELECT * FROM "+gen.tbname_partner_inventory
                +" WHERE "+gen.partinv_boxtype_fillempty+" = '"+type+"' AND "
                +gen.partinv_stat+" = '"+stat+"'";
        Cursor c = db.rawQuery(que, null);
        return c.getCount()+"";
    }

    public void returnViewItems(String id, String type, String s){
        ArrayList<ListItem> poparray;
        final Dialog dialog = new Dialog(Partner_inventory.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.driver_inventoryview);

        final TextView bt = (TextView) dialog.findViewById(R.id.btypetitle);
        ListView poplist = (ListView)dialog.findViewById(R.id.list);

        poparray = getBoxesNumbers(getIDBoxItem(id), type, s);

        TableAdapter tb = new TableAdapter(getApplicationContext(), poparray);
        poplist.setAdapter(tb);

        bt.setText(id);

        ImageButton close = (ImageButton) dialog.findViewById(R.id.close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public ArrayList<ListItem> getBoxesNumbers(String id, String type, String stat) {
        ArrayList<ListItem> result = new ArrayList<>();
        SQLiteDatabase db = gen.getReadableDatabase();
        String get = " SELECT * FROM "+gen.tbname_partner_inventory
                +" LEFT JOIN "+gen.tbname_boxes+" ON "+gen.tbname_boxes
                +"."+gen.box_id+" = "+gen.tbname_partner_inventory+"."+gen.partinv_boxtype
                +" WHERE "+gen.partinv_boxtype+" = '"+id+"' AND "+gen.tbname_partner_inventory
                +"."+gen.partinv_stat+" = '"+stat+"' AND "+gen.tbname_partner_inventory
                +"."+gen.partinv_boxtype_fillempty+" = '"+type+"'";
        Cursor f = db.rawQuery(get, null);
        f.moveToFirst();
        while (!f.isAfterLast()){
            String bid = f.getString(f.getColumnIndex(gen.partinv_id));
            String bname = f.getString(f.getColumnIndex(gen.box_name));
            String bnum = f.getString(f.getColumnIndex(gen.partinv_boxnumber));
            ListItem item = new ListItem(bid, bname, bnum, "");
            result.add(item);

            f.moveToNext();
        }
        return result;
    }

    public String getIDBoxItem(String id){
        String n = "";
        SQLiteDatabase db = gen.getReadableDatabase();
        String x = " SELECT * FROM "+gen.tbname_boxes
                +" WHERE "+gen.box_name+" = '"+id+"'";
        Cursor v = db.rawQuery(x, null);
        if (v.moveToNext()){
            n = v.getString(v.getColumnIndex(gen.box_id));
        }
        return n;
    }

}
