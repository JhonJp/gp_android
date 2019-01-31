package com.example.admin.gpxbymodule;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class Distributionlist extends Fragment {

    GenDatabase gen;
    HomeDatabase helper;
    RatesDB rate;
    RatesDB rates;
    GridView grid;
    ListView lv;
    ThreeWayAdapter adapter;
    String role;
    AutoCompleteTextView search;
    ArrayList<String> bnums;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.distlist, null);

        gen = new GenDatabase(getContext());
        rate = new RatesDB(getContext());
        rates = new RatesDB(getContext());
        helper = new HomeDatabase(getContext());

        lv = (ListView)view.findViewById(R.id.lvreservelist);
        search = (AutoCompleteTextView)view.findViewById(R.id.searchableinput);
        search.setSelected(false);
        bnums = new ArrayList<>();

        try {

            if (helper.logcount() != 0) {
                role = helper.getRole(helper.logcount());
            }
            if (role.equals("Partner Portal")) {
                customtypePartner();
            } else {
                customtype();
            }

            search.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    if (role.equals("Partner Portal")) {
                        customtypePartner();
                    } else {
                        customtype();
                    }
                    Log.e("text watch", "before change");
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    adapter.getFilter().filter(s.toString());
                    Log.e("text watch", "on change");
                }

                @Override
                public void afterTextChanged(Editable s) {
                    Log.e("text watch", "after change");
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

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle("Distribution list");
        setHasOptionsMenu(true);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.distprev).setVisible(false);
        menu.findItem(R.id.action_savedistribute).setVisible(false);
        menu.findItem(R.id.distnext).setVisible(false);
        menu.findItem(R.id.syncdist).setVisible(true);
        super.onPrepareOptionsMenu(menu);
    }

    public void customtype(){
        try {
            final ArrayList<ThreeWayHolder> result = gen.getDistributions(helper.logcount() + "");
            adapter = new ThreeWayAdapter(getContext(), result);
            lv.setAdapter(adapter);
            item();
        }catch (Exception e){}
    }

    public void customtypePartner(){
        try {
            final ArrayList<ThreeWayHolder> result = rate.getDistributions(helper.logcount() + "");
            adapter = new ThreeWayAdapter(getContext(), result);
            lv.setAdapter(adapter);
            itemPartner();
        }catch (Exception e){}
    }

    public void item(){
        try {
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    TextView textView = (TextView) view.findViewById(R.id.c_account);
                    TextView idtext = (TextView) view.findViewById(R.id.dataid);
                    final String mtop = textView.getText().toString();
                    final String ids = idtext.getText().toString();
                    ArrayList<ListItem> poparray;
                    Log.e("mtop", mtop);
                    final Dialog dialog = new Dialog(getActivity());
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setCancelable(false);
                    dialog.setContentView(R.layout.distdatalayout);
                    TextView whom = (TextView) dialog.findViewById(R.id.ownerinfo);
                    TextView truck = (TextView) dialog.findViewById(R.id.truckinput);
                    ListView poplist = (ListView) dialog.findViewById(R.id.list);
                    Log.e("distid", ids);
                    poparray = getDistBoxes(ids);

                    TableAdapter tb = new TableAdapter(getContext(), poparray);
                    poplist.setAdapter(tb);

                    whom.setText(Html.fromHtml("" + mtop + ""));
                    truck.setText(Html.fromHtml("<b>" + getTrucknum(ids) + "</b>" +
                            "<br>" +
                            "<br>" +
                            "<br>" +
                            "<b>Remarks : </b><br>" +
                            "<t>" + getRemarks(ids) + ""));

                    Button close = (Button) dialog.findViewById(R.id.close);
                    close.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });

                    dialog.show();
                }

            });

            lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    TextView textView = (TextView) view.findViewById(R.id.dataid);
                    final String mtop = textView.getText().toString();
                    final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Delete this transaction?")
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    deleteDist(mtop);
                                    deleteDistBoxes(mtop);
                                    customtype();
                                    dialog.dismiss();
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                }
                            });
                    // Create the AlertDialog object and show it
                    builder.create().show();
                    return true;
                }
            });
        }catch (Exception e){}
    }

    public void itemPartner(){
        try {
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    TextView textView = (TextView) view.findViewById(R.id.c_account);
                    TextView idtext = (TextView) view.findViewById(R.id.dataid);
                    final String mtop = textView.getText().toString();
                    final String ids = idtext.getText().toString();
                    ArrayList<ListItem> poparray;
                    Log.e("mtop", mtop);
                    final Dialog dialog = new Dialog(getActivity());
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setCancelable(false);
                    dialog.setContentView(R.layout.distdatalayout);
                    TextView whom = (TextView) dialog.findViewById(R.id.ownerinfo);
                    TextView truck = (TextView) dialog.findViewById(R.id.truckinput);
                    ListView poplist = (ListView) dialog.findViewById(R.id.list);
                    Log.e("distid", ids);
                    poparray = getDistBoxesPartner(ids);

                    TableAdapter tb = new TableAdapter(getContext(), poparray);
                    poplist.setAdapter(tb);

                    whom.setText(Html.fromHtml("" + mtop + ""));
                    truck.setText(Html.fromHtml("<b>" + getTrucknumPartner(ids) + "</b>" +
                            "<br>" +
                            "<br>" +
                            "<br>" +
                            "<b>Remarks : </b><br>" +
                            "<t>" + getRemarksPartner(ids) + ""));

                    Button close = (Button) dialog.findViewById(R.id.close);
                    close.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });

                    dialog.show();
                }

            });

            lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    TextView textView = (TextView) view.findViewById(R.id.dataid);
                    final String mtop = textView.getText().toString();
                    final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Delete this transaction?")
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    deleteDistPartner(mtop);
                                    updateBnumToRevert(getBoxnumbersPartner(mtop));
                                    deleteDistBoxesPartner(mtop);
                                    customtypePartner();
                                    dialog.dismiss();
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                }
                            });
                    // Create the AlertDialog object and show it
                    builder.create().show();
                    return true;
                }
            });
        }catch (Exception e){}
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

    public void deleteDistBoxes(String id) {
        try {
            SQLiteDatabase db = gen.getWritableDatabase();
            db.delete(gen.tbname_tempboxes, gen.dboxtemp_distributionid + " = '" + id + "'", null);
            db.close();
        }catch (Exception e){}
    }

    public void deleteDist(String id) {
        try {
            SQLiteDatabase db = gen.getWritableDatabase();
            db.delete(gen.tbname_tempDist, gen.temp_transactionnumber + " = '" + id + "'", null);
            db.close();
        }catch (Exception e){}
    }

    public String getTrucknum(String id){
        String tr = null;
        SQLiteDatabase db = gen.getReadableDatabase();
        Cursor t = db.rawQuery(" SELECT * FROM "+gen.tbname_tempDist+" WHERE "
        +gen.temp_transactionnumber+" = '"+id+"'", null);
        if (t.moveToNext()){
            tr = t.getString(t.getColumnIndex(gen.temp_trucknum));
        }
        return tr;
    }

    public String getRemarks(String id){
        String tr = null;
        SQLiteDatabase db = gen.getReadableDatabase();
        Cursor t = db.rawQuery(" SELECT * FROM "+gen.tbname_tempDist+" WHERE "
        +gen.temp_transactionnumber+" = '"+id+"'", null);
        if (t.moveToNext()){
            tr = t.getString(t.getColumnIndex(gen.temp_remarks));
        }
        return tr;
    }

    public ArrayList<ListItem> getDistBoxes(String trans){
        ArrayList<ListItem> results = new ArrayList<ListItem>();
        SQLiteDatabase db = gen.getReadableDatabase();
        String y = " SELECT * FROM " + gen.tbname_tempboxes
                +" LEFT JOIN "+gen.tb_acceptance+" ON "+gen.tbname_tempboxes+"."+gen.dboxtemp_invid
                +" = "+gen.tb_acceptance+"."+gen.acc_id+" LEFT JOIN "+gen.tbname_boxes+" ON "
                +gen.tbname_tempboxes+"."+gen.dboxtemp_boxid+" = "+gen.tbname_boxes+"."+gen.box_id
                + " WHERE " + gen.tbname_tempboxes+"."+gen.dboxtemp_distributionid + " = '" + trans + "'";
        Cursor res = db.rawQuery(y, null);
        res.moveToFirst();
        while (!res.isAfterLast()) {
            String topitem = res.getString(res.getColumnIndex(gen.box_name));
            String id = res.getString(res.getColumnIndex(gen.dboxtemp_id));
            String invid = res.getString(res.getColumnIndex(gen.dboxtemp_invid));
            String subs = res.getString(res.getColumnIndex(gen.dboxtemp_boxnumber));

            ListItem list = new ListItem(id, topitem, subs, invid);
            results.add(list);
            res.moveToNext();
        }
        // Add some more dummy data for testing
        return results;
    }

    public void deleteDistBoxesPartner(String id) {
        try {
            SQLiteDatabase db = rate.getWritableDatabase();
            db.delete(rate.tbname_part_distribution_box,
                    rate.partdist_box_distributionid + " = '" + id + "'", null);
            db.close();
        }catch (Exception e){}
    }

    public void deleteDistPartner(String id) {
        try {
            SQLiteDatabase db = rate.getWritableDatabase();
            db.delete(rate.tbname_part_distribution,
                    rate.partdist_transactionnumber + " = '" + id + "'", null);
            db.close();
        }catch (Exception e){}
    }

    public String getTrucknumPartner(String id){
        String tr = null;
        SQLiteDatabase db = rate.getReadableDatabase();
        Cursor t = db.rawQuery(" SELECT * FROM "+rate.tbname_part_distribution
                +" WHERE " +rate.partdist_transactionnumber+" = '"+id+"'", null);
        if (t.moveToNext()){
            tr = t.getString(t.getColumnIndex(rate.partdist_trucknum));
        }
        return tr;
    }

    public String getRemarksPartner(String id){
        String tr = null;
        SQLiteDatabase db = rate.getReadableDatabase();
        Cursor t = db.rawQuery(" SELECT * FROM "+rate.tbname_part_distribution
                +" WHERE "+rate.partdist_transactionnumber+" = '"+id+"'", null);
        if (t.moveToNext()){
            tr = t.getString(t.getColumnIndex(rate.partdist_remarks));
        }
        return tr;
    }

    public ArrayList<ListItem> getDistBoxesPartner(String trans){
        ArrayList<ListItem> results = new ArrayList<ListItem>();
        SQLiteDatabase db = rate.getReadableDatabase();
        String y = " SELECT * FROM " + rate.tbname_part_distribution_box
                +" LEFT JOIN "+rate.tbname_boxes+" ON "
                +rate.tbname_part_distribution_box+"."+rate.partdist_box_boxid
                +" = "+rate.tbname_boxes+"."+rate.box_id
                + " WHERE " + rate.tbname_part_distribution_box+"."+rate.partdist_box_distributionid
                + " = '" + trans + "'";
        Cursor res = db.rawQuery(y, null);
        res.moveToFirst();
        int i = 1;
        while (!res.isAfterLast()) {
            String topitem = i+"";
            String id = res.getString(res.getColumnIndex(rate.partdist_box_id));
            String subs = res.getString(res.getColumnIndex(rate.partdist_box_boxnumber));
            ListItem list = new ListItem(id, topitem, subs, "");
            results.add(list);
            i++;
            res.moveToNext();
        }
        // Add some more dummy data for testing
        return results;
    }

    public ArrayList<String> getBoxnumbersPartner(String trans){
        ArrayList<String> nums = new ArrayList<>();
        SQLiteDatabase db = rate.getReadableDatabase();
        String y = " SELECT * FROM " + rate.tbname_part_distribution_box
                +" LEFT JOIN "+rate.tbname_boxes+" ON "
                +rate.tbname_part_distribution_box+"."+rate.partdist_box_boxid
                +" = "+rate.tbname_boxes+"."+rate.box_id
                + " WHERE " + rate.tbname_part_distribution_box+"."+rate.partdist_box_distributionid
                + " = '" + trans + "'";
        Cursor res = db.rawQuery(y, null);
        res.moveToFirst();
        while (!res.isAfterLast()) {
            String topitem = res.getString(res.getColumnIndex(rate.box_name));
            String id = res.getString(res.getColumnIndex(rate.partdist_box_id));
            String subs = res.getString(res.getColumnIndex(rate.partdist_box_boxnumber));
            if (!nums.contains(subs)) {
                nums.add(subs);
            }
            res.moveToNext();
        }
        return nums;
    }

    public void updateBnumToRevert(ArrayList<String> bn){
        for (String d : bn) {
            deleteInventory(d);
            saveInventory(d);
        }
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

    public void deleteInventory(String bn){
        SQLiteDatabase db = gen.getWritableDatabase();
        db.delete(gen.tbname_partner_inventory, gen.partinv_boxnumber+" = '"+bn+"'", null);
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

}
