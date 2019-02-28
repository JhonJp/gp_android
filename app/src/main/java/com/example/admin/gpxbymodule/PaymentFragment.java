package com.example.admin.gpxbymodule;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.internal.BottomNavigationItemView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Html;
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
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class PaymentFragment extends Fragment {

    EditText amount,quantity;
    AutoCompleteTextView box;
    Button add;
    SQLiteDatabase db;
    BottomNavigationItemView bottomNavigationItemView;
    String s_amount;
    ListView lv;
    int gid, requestcode = 1;
    Camera camera;
    String accnt;
    GenDatabase gen;
    Reserve reserve;
    HomeDatabase helper;
    RatesDB rate;
    Spinner dropdown;
    LinearList adapter;
    TextView hideid, selectedboxtype;
    int boxid;
    String boxtype_selected;

    @SuppressLint("ClickableViewAccessibility")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.payment_layout, null);

        reserve = (Reserve)getActivity();
        lv = (ListView)view.findViewById(R.id.lv);
        dropdown = (Spinner)view.findViewById(R.id.trans);
        quantity = (EditText)view.findViewById(R.id.book_quantity_input);
        add = (Button)view.findViewById(R.id.box_select_add);
        gen = new GenDatabase(getContext());
        rate = new RatesDB(getContext());
        helper = new HomeDatabase(getContext());
        reserve.customtype.setEnabled(false);
        try{
            if (reserve.getAccnt() != null) {
                accnt = reserve.getAccnt();
                Log.e("account",reserve.getAccnt());
            }else{
                String t = "Customer name or account number is empty.";
                customToast(t);
            }

            customtype();
            spinnerlist();

            add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String btype = boxtype_selected;
                    String quant = quantity.getText().toString();
                    Log.e("boxtype_sel", btype);
                    if(btype.equals("") || quant.equals("")){
                        String t = "Please fill up correctly.";
                        customToast(t);
                    }else if (reserve.getName() == null){
                        String t ="Customer name or account number is empty.";
                        customToast(t);
                    }
                    else{
                        SQLiteDatabase db = gen.getReadableDatabase();
                        Cursor cx = db.rawQuery(" SELECT "+gen.box_depositprice+" FROM "+gen.tbname_boxes
                                +" WHERE "+gen.box_name+" = '"+btype+"'", null);
                        String price = null;
                        if (cx.moveToNext()){
                            price = cx.getString(cx.getColumnIndex(gen.box_depositprice));
                        }
                        if (Integer.parseInt(quant) > 1){
                            int max = Integer.parseInt(quant);
                            for (int i = 1; i <= max;i++){
                                //updateBoxQuantity(getAvailablewarehouse(helper.getBranch(helper.logcount()+"")),getBoxId(btype)+"");
                                gen.addGPXReservationBoxtype(boxid+"", btype, "1",
                                        price, reserve.getReservationnum());
                                Log.e("generate_id", generateId()+"");
                            }
                        } else {
                            //updateBoxQuantity(getAvailablewarehouse(helper.getBranch(helper.logcount()+"")), btype);
                            gen.addGPXReservationBoxtype(boxid+"", btype, quant,
                                    price, reserve.getReservationnum());
                            Log.e("generate_id", generateId()+"");
                        }
                        quantity.setText(null);

                        customtype();
                    }
                }
            });

            lv.setOnTouchListener(new View.OnTouchListener() {
                // Setting on Touch Listener for handling the touch inside ScrollView
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    return false;
                }
            });

        }catch (Exception e){}

        return  view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle("Box Info");
        setHasOptionsMenu(true);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.savebooking).setVisible(false);
        menu.findItem(R.id.btnnext).setVisible(false);
        menu.findItem(R.id.loadprevpay).setVisible(false);
        menu.findItem(R.id.btnnextpay).setVisible(true);
        menu.findItem(R.id.loadprev).setVisible(true);
        super.onPrepareOptionsMenu(menu);
    }

    public void customtype(){
        try{
            final ArrayList<ListItem> listitem = gen.getBoxes(reserve.getReservationnum());
            ListAdapter myAdapter = new ListAdapter(getContext(), listitem);
            lv.setAdapter(myAdapter);
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    final String idselected = listitem.get(position).getId();
                    Log.e("id", idselected);
                    final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Update or delete this data ?")
                            .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    gen.deleteTempBoxReserve(idselected);
                                    customtype();
                                    dialog.dismiss();
                                }
                            }).setNegativeButton("Update",new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            upd(idselected);
                            gen.deleteTempBoxReserve(idselected);
                            customtype();
                            dialog.dismiss();
                        }});
                    // Create the AlertDialog object and show it
                    builder.create().show();
                }
            });
        }catch (Exception e){}

    }

    public void upd(String id){
        try{
            SQLiteDatabase db = gen.getReadableDatabase();
            Cursor c = db.rawQuery(" SELECT * FROM "+gen.tbname_reservation_boxtype+
                    " WHERE "+gen.res_btype_id+" = '"+id+"'", null);
            if (c.moveToNext()){
                quantity.setText(c.getString(c.getColumnIndex(gen.res_quantity)));
            }
            c.close();
        }catch (Exception e){}
    }

    public void spinnerlist(){
        try {
            final ArrayList<LinearItem> result = getBoxes();
            adapter = new LinearList(getContext(), result);
            dropdown.setAdapter(adapter);
            dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    hideid = (TextView) view.findViewById(R.id.dataid);
                    selectedboxtype = (TextView) view.findViewById(R.id.c_account);
                    boxid = Integer.valueOf(hideid.getText().toString());
                    boxtype_selected = selectedboxtype.getText().toString();
                    Log.e("selectedboxid", boxid+"");
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    hideid = (TextView)parent.findViewById(R.id.dataid);
                    selectedboxtype = (TextView) parent.findViewById(R.id.c_account);
                    boxid = Integer.valueOf(hideid.getText().toString());
                    boxtype_selected = selectedboxtype.getText().toString();
                    Log.e("selectedboxid", boxid+"");
                }
            });
        }catch (Exception e){}
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.savebooking){

        }
        else if(id == R.id.btnnext){
            loadFragment(new Deposit());
        }
        else if(id == R.id.loadprev){
            loadFragment(new CustomerFragment());
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean loadFragment(Fragment fragment) {
        //switching fragment
        if (fragment != null) {
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_base, fragment)
                    .commit();

            return true;
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        try{
            if (result.getContents() != null) {
                box.setText(result.getContents());
            }
            else
                super.onActivityResult(requestCode, resultCode, data);
        }catch (Exception e){}
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
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

    @Override
    public void onPause() {
        try{
            if (reserve.getName() == null){
                String t = "Customer name or account number is empty.";
                customToast(t);
            }else{
                reserve.setName(reserve.getName());
            }
            super.onPause();
        }catch (Exception e){}
    }

    public int generateId(){
        int id = 0;
        SQLiteDatabase db = gen.getReadableDatabase();
        Cursor x = db.rawQuery(" SELECT * FROM "+gen.tbname_reservation_boxtype, null);
        if (x.getCount() != 0 ){
            id = (x.getCount() + idbaseday());
        }else{
            id = x.getCount();
        }
        return id;
    }

    public int idbaseday(){
        Date datetalaga = Calendar.getInstance().getTime();
        SimpleDateFormat writeDate = new SimpleDateFormat("ss");
        writeDate.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        int findate = Integer.parseInt(writeDate.format(datetalaga));

        return findate;
    }

    public void getAllReserves(String trans){
        SQLiteDatabase db = gen.getWritableDatabase();
        Cursor x = db.rawQuery(" SELECT * FROM "+gen.tbname_reservation_boxtype
                +" WHERE "+gen.res_reservation_id+" = '"+trans+"'", null);
        if (x.moveToNext()){
            String boxtype = x.getString(x.getColumnIndex(gen.res_boxtype));
            //updateBoxQuantity(getAvailablewarehouse(helper.getBranch(helper.logcount()+"")), getBoxId(boxtype)+"");
        }
    }

    public void updateBoxQuantity(String warehouse, String boxid){
        try{
            SQLiteDatabase db = gen.getWritableDatabase();
            Cursor cx = db.rawQuery(" SELECT * FROM "+gen.tb_acceptance
                    +" WHERE " +gen.acc_boxtype+" = '"+boxid+"' AND "+gen.acc_warehouse_id+" = '"+warehouse+"' AND "
                    +gen.acc_quantity+" != '0' LIMIT 1 ", null);
            cx.moveToFirst();
            while (!cx.isAfterLast()) {
                String ac_id = cx.getString(cx.getColumnIndex(gen.acc_id));
                String ac_q = cx.getString(cx.getColumnIndex(gen.acc_quantity));
                int qcount = Integer.parseInt(ac_q);

                int finalcount = (qcount - 1);
                //updateQAcceptance(ac_id, finalcount+"");

                cx.moveToNext();
            }
        }catch (Exception e){}
    }

    public int getBoxId(String boxtype){
        int id = 0;
        SQLiteDatabase db = gen.getReadableDatabase();
        String query = "SELECT * FROM "+gen.tbname_boxes
                +" WHERE "+gen.box_name+" = '"+boxtype+"'";
        Cursor c = db.rawQuery(query, null);
        if (c.getCount() != 0){
            c.moveToNext();
            id = c.getInt(c.getColumnIndex(gen.box_id));
        }
        // Log.e("query", query);
        return id;
    }

    public void updateQAcceptance(String id, String qu){
        SQLiteDatabase db = gen.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(gen.acc_quantity, qu);
        db.update(gen.tb_acceptance, cv,
                gen.acc_id+" = '"+id+"'", null);
        Log.e("updated_acceptance", "id: "+id+", quantity: "+qu);
        db.close();
    }

    public String getAvailablewarehouse(String branch){
        String id = null;
        SQLiteDatabase db = rate.getReadableDatabase();
        Cursor x = db.rawQuery(" SELECT * FROM "+rate.tbname_warehouse
                +" WHERE "+rate.ware_branchid+" = '"+branch+"' LIMIT 1 ", null);
        if (x.moveToNext()){
            id = x.getString(x.getColumnIndex(rate.ware_id));
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

}