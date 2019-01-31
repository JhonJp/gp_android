package com.example.admin.gpxbymodule;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
import java.util.HashMap;
import java.util.TimeZone;

public class Destination_transactions extends Fragment {

    EditText disttype;
    IntentIntegrator scanIntegrator;
    Button add;
    ListView lv;
    GenDatabase gen;
    HomeDatabase helper;
    RatesDB rate;
    int requestcode = 1, selid = 0;
    TextView t, hideid;
    Camera camera;
    Distribution dist;
    String[] name;
    ArrayList<String> ids;
    TextView tv;
    String tbox, typ, sm;
    Spinner spin,warehousespin, distname;
    String spinid;
    ArrayList <String> numbers, boxids, invids;
    ThreeWayAdapter ad;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.destination_transinfo, null);
        Log.e("frag", "Transactions");

        spin = (Spinner)view.findViewById(R.id.trans);
        warehousespin = (Spinner)view.findViewById(R.id.types);
        distname = (Spinner)view.findViewById(R.id.distnameinput);
        add = (Button)view.findViewById(R.id.adddist);
        lv = (ListView)view.findViewById(R.id.lv);
        t = (TextView)view.findViewById(R.id.total);

        gen = new GenDatabase(getContext());
        rate = new RatesDB(getContext());
        helper = new HomeDatabase(getContext());
        dist = (Distribution)getActivity();
        numbers = new ArrayList<>();
        boxids = new ArrayList<>();
        invids = new ArrayList<>();
        if (dist.getTrans() != null) {
            dist.setTrans(dist.getTrans());
            Log.e("transactionnum", dist.getTrans());
        }
        if (dist.getBoxnumbers() != null){
            numbers = dist.getBoxnumbers();
        }
        if (dist.getInventoryIDS() != null){
            invids = dist.getInventoryIDS();
        }
        if (dist.getBoxIDS() != null){
            boxids = dist.getBoxIDS();
        }
        spinnerlist();
        customlists();
        boxinventory();
        capture();
        //listTempBox();
        lv.setOnTouchListener(new View.OnTouchListener() {
            // Setting on Touch Listener for handling the touch inside ScrollView
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Disallow the touch request for parent scroll on touch of child view
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });

        return  view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle("Transaction Info");
        setHasOptionsMenu(true);
    }

    public void capture(){
        try {
            add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ArrayList<ThreeWayHolder> ware = gen.getBoxInv(helper.logcount() + "");
                    if (ware.size() == 0) {
                        String y = "You do not have enough inventory.";
                        customToast(y);
                    } else {
                        barcode();
                    }
                }
            });
        }catch (Exception e){}
    }

    public void barcode(){
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED) {

            ActivityCompat.requestPermissions(getActivity(), new String[] {Manifest.permission.CAMERA}, 100);
        }else{
            scanIntegrator = IntentIntegrator.forSupportFragment(Destination_transactions.this);
            scanIntegrator.setPrompt("Scan barcode");
            scanIntegrator.setBeepEnabled(true);
            scanIntegrator.setOrientationLocked(true);
            scanIntegrator.setCaptureActivity(CaptureActivityPortrait.class);
            scanIntegrator.initiateScan();
        }
    }

    public void boxinventory(){
        try {
            final ArrayList<ThreeWayHolder> ware = gen.getBoxInv(helper.logcount() + "");
            ad = new ThreeWayAdapter(getContext(), ware);
            warehousespin.setAdapter(ad);
            if (selid != 0){
                int sz = ware.size();
                //Log.e("size", (sz - 1)+", "+(selid));
                if((sz - 1) == (selid)){
                    warehousespin.setSelection(selid);
                }else {
                    warehousespin.setSelection(0);
                }
            }else{
                warehousespin.setSelection(0);
            }
            warehousespin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    selid = parent.getSelectedItemPosition();
                    tbox = ware.get(position).getId();
                    typ = ware.get(position).getTopitem();
                    Log.e("selid", "" + selid);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    selid = parent.getSelectedItemPosition();
                }
            });
        }catch (Exception e){}
    }

    public void namespinner(String type){
        try {
            if (type.equals("GP - Branch")) {
                //condition for branch names
                name = rate.getAllBranch("GP - Branch", helper.getBranch(helper.logcount() + ""));
                ArrayAdapter<String> branchadapter =
                        new ArrayAdapter<>(getContext(), R.layout.spinneritem,
                                name);
                distname.setAdapter(branchadapter);
                branchadapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
                distname.setPrompt("Select " + type.toLowerCase());
            } else {
                //condition for Sales driver
                Log.e("branchid",helper.getBranch(helper.logcount() + ""));
                ArrayList<LinearItem> name = gen.getSalesDriver("Sales Driver", helper.getBranch(helper.logcount() + ""));
                LinearList list = new LinearList(getContext(), name);
                distname.setAdapter(list);
                distname.setPrompt("Select sales driver");
                distname.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        hideid = (TextView) view.findViewById(R.id.c_account);
//                        Log.e("selectedid", hideid.getText().toString());
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        hideid = (TextView) parent.findViewById(R.id.c_account);
                       // Log.e("selectedid", hideid.getText().toString());
                    }
                });
            }
        }catch (Exception e){}
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.distprev).setVisible(false);
        menu.findItem(R.id.distnext).setVisible(true);
        menu.findItem(R.id.action_savedistribute).setVisible(false);
        menu.findItem(R.id.syncdist).setVisible(false);
        super.onPrepareOptionsMenu(menu);
    }

    public void spinnerlist(){
        try {
            String[] items = new String[]{"GP - Branch", "Sales Driver"};
            ArrayAdapter<String> adapter =
                    new ArrayAdapter<>(getContext(), R.layout.spinneritem,
                            items);
            spin.setAdapter(adapter);
            adapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
            if (dist.getDisttype() != null) {
                spin.setSelection(adapter.getPosition(dist.getDisttype()));
            }
            spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    namespinner(spin.getSelectedItem().toString());
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    namespinner(spin.getSelectedItem().toString());
                }
            });
        }catch (Exception e){}
    }

    public void customlists(){
        final ArrayList<ListItem> results = new ArrayList<ListItem>();
        final ArrayList<String> resultnums = numbers;
        final ArrayList<String> resids = boxids;
        final ArrayList<String> resinvids = invids;
        if (resids.size() != 0) {
            for (int i = 0; i < resids.size(); i++) {
                ListItem list = new ListItem(i + "", getBoxname(resids.get(i)), resultnums.get(i), resinvids.get(i));
                results.add(list);
            }
        }
        final ListAdapter myAdapter = new ListAdapter(getContext(), results);
        lv.setAdapter(myAdapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                final String ids = resids.get(position);
                final String numids = resultnums.get(position);
                final String inv = resinvids.get(position);
                builder.setTitle("Delete this data ?")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                results.remove(ids);
                                resids.remove(ids);
                                resultnums.remove(numids);
                                addQuan(inv);
                                invids.remove(inv);
                                myAdapter.notifyDataSetChanged();
                                boxinventory();
                                customlists();
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
        t.setText(Html.fromHtml("<small>Total : </small>")+""+resids.size()+" box(s) ");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        try {
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if (result.getContents() != null) {
                if (checkBnumFromInv(result.getContents())){
                    String n = "with boxnumber";
                    if (sm.contains(n)){
                        if (!numbers.contains(result.getContents())) {
                            numbers.add(result.getContents());
                            boxids.add(getIdBoxtype(typ));
                            //boxnumbers.add(result.getContents());
                            invids.add(tbox);
                            //gen.addTempBoxDist(dist.getTrans(), getIdBoxtype(typ), tbox, result.getContents(), "0");
                            minStat(tbox);
                            ad.notifyDataSetChanged();
                            //updateWarehouseInv(result.getContents(), "1");
                            warehousespin.setSelection(selid);
                            boxinventory();
                            //listTempBox();
                            customlists();
                            Log.e("boxtype", "" + getIdBoxtype(typ));
                        }else{
                            String x = "Box number has been scanned, please try another boxnumber.";
                            customToast(x);
                        }
                    }else {
                        if (!checkBnum(result.getContents())) {
                            String x = "Box number exists, please try another boxnumber.";
                            customToast(x);
                        } else {
                            if (!numbers.contains(result.getContents())) {
                                numbers.add(result.getContents());
                                boxids.add(getIdBoxtype(typ));
                                //boxnumbers.add(result.getContents());
                                invids.add(tbox);
                                //gen.addTempBoxDist(dist.getTrans(), getIdBoxtype(typ), tbox, result.getContents(), "0");
                                minStat(tbox);
                                ad.notifyDataSetChanged();
                                warehousespin.setSelection(selid);
                                boxinventory();
                                //listTempBox();
                                customlists();
                            }else{
                                String x = "Box number has been scanned, please try another boxnumber.";
                                customToast(x);
                            }
                        }
                    }
                }else{
                    if (!checkBnum(result.getContents())) {
                        String x = "Box number exists in distribution records, please try another boxnumber.";
                        customToast(x);
                    } else {
                        if (!numbers.contains(result.getContents())) {
                            numbers.add(result.getContents());
                            boxids.add(getIdBoxtype(typ));
                            //boxnumbers.add(result.getContents());
                            invids.add(tbox);
                            //gen.addTempBoxDist(dist.getTrans(), getIdBoxtype(typ), tbox, result.getContents(), "0");
                            minStat(tbox);
                            ad.notifyDataSetChanged();
                            warehousespin.setSelection(selid);
                            boxinventory();
                            customlists();
                        }else{
                            String x = "Box number has been scanned, please try another boxnumber.";
                            customToast(x);
                        }
                    }
                }
            } else
                super.onActivityResult(requestCode, resultCode, data);
        }catch (Exception e){}
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == requestcode) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Toast.makeText(getContext(), "camera permission granted", Toast.LENGTH_LONG).show();
            } else {
                String x = "Please allow the camera permission.";
                customToast(x);
            }
        }
    }

    @Override
    public void onPause(){
        try {
            if (dist.getTrans() != null) {
                dist.setTrans(dist.getTrans());
            }
            if (distname.getSelectedItem().toString() == null) {
                String t = "Distribution name is empty.";
                customToast(t);
            } else {
                String f_type = spin.getSelectedItem().toString();
                if (f_type.equals("GP - Branch")) {
                    dist.setDistname(distname.getSelectedItem().toString());
                    Log.e("distname", distname.getSelectedItem().toString());
                } else {
                    dist.setDistname(hideid.getText().toString());
                    Log.e("salesdriver", hideid.getText().toString());
                }
            }
            dist.setDisttype(spin.getSelectedItem().toString());
            ArrayList<ThreeWayHolder> ware = gen.getBoxInv(helper.logcount() + "");
            dist.setInventorysize(ware.size());

            dist.setBoxnumbers(numbers);
            dist.setInventoryIDS(invids);
            dist.setBoxIDS(boxids);

        }catch (Exception e){}
        super.onPause();
    }

    public String datereturn(){
        Date datetalaga = Calendar.getInstance().getTime();
        SimpleDateFormat writeDate = new SimpleDateFormat("yyyy-MM-dd");
        writeDate.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        String findate = writeDate.format(datetalaga);

        return findate;
    }

    public int countTempBox(){
        int total = 0;
        SQLiteDatabase db = gen.getReadableDatabase();
        Cursor c = db.rawQuery(" SELECT * FROM "+gen.tbname_tempboxes
                +" WHERE "+gen.dboxtemp_distributionid+ " = '"+dist.getTrans()+"'",null);
        if (c.getCount() != 0 ){
            total = c.getCount();
        }
        return total;
    }

    @Override
    public void onResume(){
        if (dist.getTrans() != null){
            dist.setTrans(dist.getTrans());
        }
        if (dist.getBoxnumbers() != null){
            numbers = dist.getBoxnumbers();
        }
        if (dist.getInventoryIDS() != null){
            invids = dist.getInventoryIDS();
        }
        if (dist.getBoxIDS() != null){
            boxids = dist.getBoxIDS();
        }
        super.onResume();
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

    public boolean checkBnum(String bnum){
        SQLiteDatabase db = gen.getReadableDatabase();
        Cursor c = db.rawQuery(" SELECT * FROM "+gen.tbname_tempboxes
                +" WHERE "+gen.dboxtemp_boxnumber+" = '"+bnum+"'", null);
        if (c.getCount() == 0){
            return true;
        }
        return false;
    }

    public boolean checkBnumFromInv(String bnum){
        SQLiteDatabase db = gen.getReadableDatabase();
        Cursor c = db.rawQuery(" SELECT * FROM "+gen.tbname_checker_inventory
                +" WHERE "+gen.chinv_boxnumber+" = '"+bnum+"'", null);
        if (c.getCount() != 0){
            return true;
        }else {
            return false;
        }
    }

    public void minStat(String id) {
        int finq = 0;
        SQLiteDatabase db = gen.getWritableDatabase();
        Cursor c = db.rawQuery(" SELECT * FROM " + gen.tb_acceptance
                + " WHERE " + gen.acc_id + " = '" + id + "'", null);
        if (c.moveToNext()) {
            String acid = c.getString(c.getColumnIndex(gen.acc_id));
            String acq = c.getString(c.getColumnIndex(gen.acc_quantity));
            finq = (Integer.parseInt(acq) - 1);
            updateQAcceptance(acid, finq+"");
        }
        c.close();
        db.close();
    }

    public String getIdBoxtype(String name){
        String idbox = null;
        SQLiteDatabase db = gen.getReadableDatabase();
        Cursor x = db.rawQuery(" SELECT * FROM "+gen.tbname_boxes
                +" WHERE "+gen.box_name+" = '"+name+"'", null);
        if (x.moveToNext()){
            idbox = x.getString(x.getColumnIndex(gen.box_id));
        }
        x.close();
        return idbox;
    }

    //quantity updates
    public void addQuan(String id) {
        try {
            int finq = 0;
            SQLiteDatabase db = gen.getWritableDatabase();
            Cursor c = db.rawQuery(" SELECT * FROM " + gen.tb_acceptance
                    + " WHERE " + gen.acc_id + " = '" + id + "'", null);
            if (c.moveToNext()) {
                String acid = c.getString(c.getColumnIndex(gen.acc_id));
                String acq = c.getString(c.getColumnIndex(gen.acc_quantity));
                finq = (Integer.parseInt(acq) + 1);
                updateQAcceptance(acid, finq+"");
            }
            c.close();
            db.close();
        }catch (Exception e){}
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

    public String getBoxname(String id){
        String name = "";
        SQLiteDatabase db = gen.getReadableDatabase();
        String que = " SELECT * FROM "+gen.tbname_boxes
                +" WHERE "+gen.box_id+" = '"+id+"'";
        Cursor x = db.rawQuery(que, null);
        if (x.moveToNext()){
            name = x.getString(x.getColumnIndex(gen.box_name));
        }
        return name;
    }

    public void updateWarehouseInv(String bn, String stat){
        SQLiteDatabase db = gen.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(gen.chinv_stat, stat);
        db.update(gen.tbname_checker_inventory, cv,
                gen.chinv_boxnumber+" = '"+bn+"'", null);
        Log.e("updateinv", "invent update "+bn);
        db.close();
    }

}

