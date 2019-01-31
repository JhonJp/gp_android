package com.example.admin.gpxbymodule;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class Acceptance_empty extends Fragment {


    HomeDatabase helper;
    SQLiteDatabase db;
    GenDatabase gen;
    RatesDB rate;
    ListView lv;
    Spinner manname;
    EditText quant;
    TextView total, hideid, bname;
    String role;
    Button accadd;
    ProgressDialog progressBar;
    Spinner spin, spinwarehouse;
    NavigationView navigationView;
    ArrayAdapter<String> warehouseadapter;
    TextView datenow, hinttxt;
    int boxid, wareid;
    ImageButton addm;
    ArrayList<ListItem> listitem;
    String trans;
    LinearList adapter;
    ArrayList<String> boxnames, quantities;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.emptyacceptance, null);

        helper = new HomeDatabase(getContext());
        gen = new GenDatabase(getContext());
        rate = new RatesDB(getContext());

        lv = (ListView) v.findViewById(R.id.lv);
        accadd = (Button) v.findViewById(R.id.accept_add);
        addm = (ImageButton) v.findViewById(R.id.addman);
        datenow = (TextView)v.findViewById(R.id.acceptancedate);
        hinttxt = (TextView)v.findViewById(R.id.thinttxt);
        datenow.setText(datereturn());

        manname = (Spinner) v.findViewById(R.id.items);
        quant = (EditText) v.findViewById(R.id.quantinput);
        total = (TextView)v.findViewById(R.id.total);
        spin = (Spinner)v.findViewById(R.id.trans);
        spinwarehouse = (Spinner)v.findViewById(R.id.warehouse);
        boxnames = new ArrayList<>();
        quantities = new ArrayList<>();

        if (helper.logcount() != 0){
            role = helper.getRole(helper.logcount());
            Log.e("role ", role);
        }
        if (this.getTrans() != null){
            this.setTrans(this.getTrans());
        }else {
            this.setTrans(generate());
        }
        spinManf();
        spinnerlist();
        warehousespinner();
        addacc();
        acc();
        addMan();
        scrolllist();

        return v;
    }

    public void warehousespinner(){
        try {
            final String[] warehouses = rate.getWarehouseName(helper.getBranch(helper.logcount() + ""));
            warehouseadapter =
                    new ArrayAdapter<>(getContext(), R.layout.spinneritem,
                            warehouses);
            Log.e("warehouses", warehouses.length + "");
            spinwarehouse.setAdapter(warehouseadapter);
            warehouseadapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
            spinwarehouse.setPrompt("Select warehouse");
            spinwarehouse.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    wareid = getWarehouseId(spinwarehouse.getSelectedItem().toString());
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        }catch (Exception e){}
    }

    public void spinManf(){
        final String[] names = rate.getMan();
        if (names.length == 0){
            hinttxt.setText("No manufacturers yet");
        }else{
            hinttxt.setVisibility(View.INVISIBLE);
            hinttxt.setClickable(false);
        }
        Log.e("names", names.length+"");
        ArrayAdapter<String> warehouseadapter =
                new ArrayAdapter<>(getContext(), R.layout.spinneritem,
                        names);
        //Log.e("man_names", names.toString() + "");
        manname.setAdapter(warehouseadapter);
        warehouseadapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
    }

    public void spinnerlist(){
        try {
            final ArrayList<LinearItem> result = getBoxes();
            adapter = new LinearList(getContext(), result);
            spin.setAdapter(adapter);
            spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    bname = (TextView) view.findViewById(R.id.c_account);
                    hideid = (TextView) view.findViewById(R.id.dataid);
                    boxid = Integer.valueOf(hideid.getText().toString());
                    Log.e("selectedboxid", boxid+"");
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    hideid = (TextView)parent.findViewById(R.id.dataid);
                    boxid = Integer.valueOf(hideid.getText().toString());
                    Log.e("selectedboxid", boxid+"");
                }
            });
        }catch (Exception e){}
    }

    public void acc(){
        final ArrayList<ListItem> results = new ArrayList<ListItem>();
        final ArrayList<String> names = boxnames;
        final ArrayList<String> qs = quantities;
        if (boxnames.size() != 0) {
            for (int i = 0; i < names.size(); i++) {
                ListItem item = new ListItem(i+"",names.get(i), qs.get(i),"");
                results.add(item);
            }
        }
        final ListAdapter ad = new ListAdapter(getContext(), results);
        lv.setAdapter(ad);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final String selname = names.get(position);
                final String selq = names.get(position);
                final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Delete this data ?")
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                boxnames.remove(selname);
                                quantities.remove(selq);
                                acc();
                                dialog.dismiss();
                            }
                        });
                // Create the AlertDialog object and show it
                builder.create().show();
            }
        });
    }

    public String datereturn(){
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

    public void addacc(){
        accadd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    final String[] names = rate.getMan();
                    String type = bname.getText().toString();
                    int quantity = 0;
                    if (quant.getText().toString().equals("")){
                        String f = "Empty fields, please enter valid values.";
                        customToast(f);
                    }else {
                        quantity = Integer.parseInt(quant.getText().toString());
                    }
                    if ((type.equals("")) || (names.length == 0) || (quantity == 0)) {
                        String f = "Empty fields, please enter valid values.";
                        customToast(f);
                    } else if ((names.length == 0) || (quantity == 0)) {
                        String f = "Empty fields, please enter valid values.";
                        customToast(f);
                    }else {
                        boxnames.add(type+"");
                        quantities.add(quantity+"");
                        acc();
                        total.setText("Total: "+sum());
                        quant.setText("");
                    }
                }catch(Exception e){}
            }
        });
    }

    public String generate(){
        Date datetalaga = Calendar.getInstance().getTime();
        SimpleDateFormat writeDate = new SimpleDateFormat("yyyyMMddhhmmss");
        writeDate.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        String sa = writeDate.format(datetalaga);

        String transNo = helper.logcount() + sa;
        this.setTrans(transNo);

        return transNo;
    }

    public void floatadd(){
        try{
            String name = manname.getSelectedItem().toString();
            //String q = quant.getText().toString();
            SQLiteDatabase db = gen.getReadableDatabase();
            Cursor x = db.rawQuery(" SELECT * FROM "+gen.tb_acceptance+" WHERE "+gen.acc_id+
                    " = '"+trans+"'", null);
            if (x.getCount() == 0 ) {
                for (int i = 0; i < boxnames.size(); i++){
                    gen.addAcceptanceEmpty( getTrans(), wareid+"",
                            name, getBoxId(boxnames.get(i))+""
                            , quantities.get(i), datereturn(), helper.logcount()+"",
                            "2", "1");
                    this.setTrans(null);
                    transactionNumberNew();
                }
                gen.addTransactions("Acceptance", helper.logcount()+"",
                        "Acceptance from "+name, datereturn(), returntime());
            }
        }catch (Exception e){}

    }

    public void transactionNumberNew(){
        if (this.getTrans() != null){
            this.setTrans(this.getTrans());
        }else {
            this.setTrans(generate());
        }
    }

    public double sum(){
        double sum = 0;
        for(String d : quantities)
            sum += Double.valueOf(d);
        return sum;
    }

    public int getBoxId(String name){
        int id = 0;
        SQLiteDatabase db = gen.getReadableDatabase();
        Cursor cx = db.rawQuery(" SELECT "+gen.box_id+" FROM "+ gen.tbname_boxes
                +" WHERE "+gen.box_name+" = '"+name+"'", null);
        if (cx.moveToNext()){
            id = cx.getInt(cx.getColumnIndex(gen.box_id));
        }
        return id;
    }

    public int getWarehouseId(String name){
        int id = 0;
        SQLiteDatabase db = rate.getReadableDatabase();
        Cursor cx = db.rawQuery(" SELECT "+rate.ware_id+" FROM "+ rate.tbname_warehouse
                +" WHERE "+rate.ware_name+" = '"+name+"'", null);
        if (cx.moveToNext()){
            id = cx.getInt(cx.getColumnIndex(rate.ware_id));
        }
        return id;
    }

    public void customToast(String txt){
        Toast toast = new Toast(getContext());
        toast.setDuration(Toast.LENGTH_SHORT);
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(
                getContext().LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.toast, null);
        TextView t = (TextView)view.findViewById(R.id.toasttxt);
        t.setText(txt);
        toast.setView(view);
        toast.setGravity(Gravity.TOP | Gravity.RIGHT, 15, 50);
        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.enterright);
        view.startAnimation(animation);
        toast.show();
    }

    public void addMan(){
        try {
            addm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
                    LayoutInflater inflater = Acceptance_empty.this.getLayoutInflater();
                    View d = inflater.inflate(R.layout.manadd, null);
                    dialogBuilder.setTitle("Please add manufacturer.");
                    Button add = (Button) d.findViewById(R.id.confirm);
                    Button cancel = (Button) d.findViewById(R.id.cancel);
                    final EditText itemname = (EditText) d.findViewById(R.id.man_name);
                    dialogBuilder.setView(d);
                    final AlertDialog alertDialog = dialogBuilder.show();
                    add.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String t = itemname.getText().toString();
                            if (t.equals("")) {
                                String x = "Please add a manufacturer name.";
                                customToast(x);
                            } else {
                                rate.addMan(itemname.getText().toString(), datereturn());
                                spinManf();
                                alertDialog.dismiss();
                            }
                        }
                    });

                    cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            alertDialog.dismiss();
                        }
                    });
                }
            });
        }catch (Exception e){}
    }

    public void scrolllist(){
        lv.setOnTouchListener(new View.OnTouchListener() {
            // Setting on Touch Listener for handling the touch inside ScrollView
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle("Acceptance");
        setHasOptionsMenu(true);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.saveoicacceptance).setVisible(true);
        menu.findItem(R.id.savecheckacceptance).setVisible(false);
        menu.findItem(R.id.acceptancelist).setVisible(true);
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.saveoicacceptance) {
                floatadd();
                boxnames.clear();
                quantities.clear();
                getActivity().recreate();
                String ty = "Transaction has been successful, thank you.";
                customToast(ty);

        }else if (id == R.id.acceptancelist) {
            Intent i = new Intent(getContext(), Acceptancelist.class);
            //Create the bundle to pass
            Bundle bundle = new Bundle();
            //Add your data from getFactualResults method to bundle
            bundle.putString("type", "0");
            i.putExtras(bundle);
            startActivity(i);
            getActivity().finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public ArrayList<LinearItem> getBoxes() {
        SQLiteDatabase db = gen.getReadableDatabase();
        ArrayList<LinearItem> numbers = new ArrayList<LinearItem>();
        Cursor c = db.rawQuery(" SELECT * FROM " + gen.tbname_boxes
                +" WHERE "+gen.box_nsb+" = '0'", null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            String id = c.getString(c.getColumnIndex(gen.box_id));
            String name = c.getString(c.getColumnIndex(gen.box_name));
            String len = c.getString(c.getColumnIndex(gen.box_length));
            String wid = c.getString(c.getColumnIndex(gen.box_width));
            String hei = c.getString(c.getColumnIndex(gen.box_height));
            String sub = Html.fromHtml("<small>( "+ len +" * " +wid+" * "+hei+" )</small>").toString();
            LinearItem list = new LinearItem(id, name,sub);
            numbers.add(list);
            c.moveToNext();
        }
        return numbers;
    }

    //populate box in an array to listview
    public ArrayList<ListItem> populateBoxes() {
        ArrayList<ListItem> numbers = new ArrayList<ListItem>();
        for (int i = 0; i < boxnames.size(); i++){
            ListItem item = new ListItem(i+"",boxnames.get(i), quantities.get(i),"");
         numbers.add(item);
        }
        return numbers;
    }

    public String getTrans() {
        return trans;
    }

    public void setTrans(String trans) {
        this.trans = trans;
    }

}
