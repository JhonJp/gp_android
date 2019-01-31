package com.example.admin.gpxbymodule;

import android.Manifest;
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
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
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
import java.util.List;
import java.util.TimeZone;

public class Partdist_transactioninfo extends Fragment {

    EditText disttype;
    FloatingActionButton add;
    ListView lv;
    GenDatabase gen;
    HomeDatabase helper;
    RatesDB rate;
    int requestcode = 1;
    TextView t, hideid, hinttxt;
    Camera camera;
    Partner_distribution dist;
    ArrayList<String> numbers;
    String tbox, typ, desti;
    Spinner spinfilter;
    String spinid;
    CheckBox chbox;
    ListViewItemCheckboxBaseAdapter adapter;
    ArrayList<String> ware;
    IntentIntegrator scanIntegrator;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.partdist_transinfo, null);
        Log.e("frag", "Transactions");

        add = (FloatingActionButton)view.findViewById(R.id.scanbox);
        lv = (ListView)view.findViewById(R.id.list_view_with_checkbox);
        t = (TextView)view.findViewById(R.id.total);

        gen = new GenDatabase(getContext());
        rate = new RatesDB(getContext());
        helper = new HomeDatabase(getContext());
        dist = (Partner_distribution) getActivity();
        numbers = new ArrayList<>();
        if (dist.getFrag() == null){
            dist.setFrag("trans");
        }
        else{
            dist.setFrag("trans");
        }
        if (dist.getTrans() != null) {
            dist.setTrans(dist.getTrans());
            Log.e("transactionnum", dist.getTrans());
        }
        if (dist.getBoxnums() != null){
            numbers = dist.getBoxnums();
        }

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanpermit();
            }
        });
        boxinventory();
        checkGetters();

        return  view;
    }

    public void checkGetters(){
        if (dist.getDistname() == null){
            dist.setDistname(null);
        }else{
            dist.setDistname(dist.getDistname());
        }
        if (dist.getDisttype() == null){
            dist.setDisttype(null);
        }else{
            dist.setDisttype(dist.getDisttype());
        }
        if (dist.getDisttrucknumber() == null){
            dist.setDisttrucknumber(null);
        }else{
            dist.setDisttrucknumber(dist.getDisttrucknumber());
        }
        if (dist.getRemarks() == null){
            dist.setRemarks(null);
        }else{
            dist.setRemarks(dist.getRemarks());
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle("Boxnumbers");
        setHasOptionsMenu(true);
    }

    public void boxinventory(){
        try {
            ware = numbers;
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),
                    android.R.layout.simple_list_item_1, ware);
            lv.setAdapter(adapter);
            itemcount();
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                    final String ids = ware.get(position);
                    final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Delete this data ?")
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    ware.remove(ware.get(position).indexOf(ids));
                                    boxinventory();
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

        }catch (Exception e){}
    }

    public void scanpermit(){
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(getActivity(), new String[] {Manifest.permission.CAMERA}, 100);
        }else{
            scanIntegrator = IntentIntegrator.forSupportFragment(Partdist_transactioninfo.this);
            scanIntegrator.setPrompt("Scan barcode");
            scanIntegrator.setBeepEnabled(true);
            scanIntegrator.setOrientationLocked(true);
            scanIntegrator.setCaptureActivity(CaptureActivityPortrait.class);
            scanIntegrator.initiateScan();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if (result.getContents() != null) {
                String bn = result.getContents();
                if (checkFromInventory(bn)) {
//                    if (checkFromDist(bn)) {
                        if (!numbers.contains(bn)){
                            numbers.add(bn);
                            boxinventory();
                        }else{
                            String b = "Box number has been scanned.";
                            customToast(b);
                        }
//                    } else {
//                        String b = "Box number exists in the distribution database.";
//                        customToast(b);
//                    }
                } else {
                    String b = "Box number is not in unloading database.";
                    customToast(b);
                }
            }
            else
                super.onActivityResult(requestCode, resultCode, data);

        }catch (Exception e){}
    }

    public void itemcount(){
        if (numbers.size() != 0) {
            t.setText(numbers.size()+" item(s)");
        }else{
            t.setText("0 item(s)");
        }
    }

    public boolean checkFromDist(String bn){
        SQLiteDatabase db = rate.getReadableDatabase();
        Cursor x = db.rawQuery(" SELECT * FROM "+rate.tbname_part_distribution_box
                +" WHERE "+rate.partdist_box_boxnumber+" = '"+bn+"'", null);
        if (x.getCount() != 0){
            return false;
        }else {
            return true;
        }
    }

    public boolean checkFromInventory(String bn){
        SQLiteDatabase db = gen.getReadableDatabase();
        Cursor x = db.rawQuery(" SELECT * FROM "+gen.tbname_partner_inventory
                +" WHERE "+gen.partinv_boxnumber+" = '"+bn+"'", null);
        if (x.getCount() != 0){
            return true;
        }else {
            return false;
        }
    }

    public void deleteItem(String id){
        SQLiteDatabase db = rate.getWritableDatabase();
        db.delete(rate.tbname_part_distribution_box,
                rate.partdist_box_id+" = '"+id+"'", null);
        Log.e("deletedid", id);
        db.close();
    }

    public void deleteItemBox(String box){
        SQLiteDatabase db = rate.getWritableDatabase();
        db.delete(rate.tbname_part_distribution_box,
                rate.partdist_box_boxnumber+" = '"+box+"'", null);
        Log.e("deletedbox", box);
        db.close();
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

    //save distribution
    public void savePartDistribution() {
        try {
            if (dist.getDistname() == null) {
                String x = "Distribution type or name is empty.";
                customToast(x);
            } else {
                String f_trans = "";
                String f_type = dist.getDisttype();
                if (f_type.equals("Partner - Hub")){
                    f_trans = dist.getTransactionnumhub();
                }else{
                    f_trans = dist.getTrans();
                }
                String mode = dist.getMode();
                String eta = dist.getEtanow();
                String f_driver = dist.getDrivername();
                String f_name = dist.getDistname();
                String f_truck = dist.getDisttrucknumber();
                String f_remarks = dist.getRemarks();
                if ((f_type == null) || (f_name == null)) {
                    String x = "Distribution type and item is required.";
                    customToast(x);
                } else if ((f_truck.equals(""))) {
                    String x = "Truck number is required.";
                    customToast(x);
                } else {
                    if (numbers.size() == 0) {
                        String x = "Please add box number to be distributed.";
                        customToast(x);
                    } else if (rate.addDistribution(f_trans, f_type, mode, f_name, f_driver, f_truck,
                            f_remarks, eta,"1", "1", datereturn(),
                            helper.logcount() + "", "1")) {
                        if (f_type.equals("Direct")){
                            for (String bn : numbers){
                                rate.addPartDistributionBox(f_trans,
                                        getBoxtypeId(bn),
                                        bn, "1");
                                deleteBoxnumberInventory(bn);
                            }
                        }else{
                            for (String bn : numbers){
                                rate.addPartDistributionBox(f_trans,
                                        getBoxtypeId(bn),
                                        bn, "1");
                                deleteBoxnumberInventory(bn);
                            }
                        }
                        String type = "Distribution";
                        String user = "" + helper.logcount();
                        gen.addTransactions(type, user, "Distribution to " + dist.getDistname(),
                                datereturn(), returntime());

                        dist.setTrans(null);
                        dist.setRemarks(null);
                        dist.setDisttrucknumber(null);
                        dist.setDistname(null);
                        dist.setDisttype(null);
                        alert(0);
                    } else {
                        alert(1);
                    }
                }
            }
        }catch(Exception e){
        }
    }

    public String returntime(){
        Date datetalaga = Calendar.getInstance().getTime();
        SimpleDateFormat writeDate = new SimpleDateFormat("HH:mm:ss");
        writeDate.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        String time = writeDate.format(datetalaga);

        return time;
    }

    public String getBoxtypeId(String bn){
        String tp = "";
        SQLiteDatabase db = gen.getReadableDatabase();
        Cursor x = db.rawQuery(" SELECT * FROM " + gen.tbname_partner_inventory
                + " WHERE " + gen.partinv_boxnumber + " = '" + bn + "'", null);
        if (x.moveToNext()){
            tp = x.getString(x.getColumnIndex(gen.partinv_boxtype));
        }
        x.close();
        return tp;
    }

    public void deleteBoxnumberInventory(String bn) {
        SQLiteDatabase db = gen.getWritableDatabase();
        db.delete(gen.tbname_partner_inventory,
                gen.partinv_boxnumber + " = '"+bn+"' AND "
                        +gen.partinv_stat+" = '0'", null);
        db.close();
    }

    public void alert(int ok){
        try {
            if (ok == 0) {
                String a = "Transaction has been successful, thank you.";
                customToast(a);
                dist.getBoxnums().clear();
                numbers.clear();
                getActivity().recreate();
            } else {
                String a = "Transaction failed, please try again.";
                customToast(a);
            }
        }catch (Exception e){}
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.distprev).setVisible(true);
        menu.findItem(R.id.action_savedistribute).setVisible(true);
        menu.findItem(R.id.distnext).setVisible(false);
        menu.findItem(R.id.syncdist).setVisible(false);
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_savedistribute){
            savePartDistribution();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPause(){
        super.onPause();
        try {
            if (dist.getTrans() != null) {
                dist.setTrans(dist.getTrans());
            }
            dist.setBoxnums(numbers);
            dist.setRemarks(dist.getRemarks());
            dist.setDisttrucknumber(dist.getDisttrucknumber());

        }catch (Exception e){}
    }

    public String datereturn(){
        Date datetalaga = Calendar.getInstance().getTime();
        SimpleDateFormat writeDate = new SimpleDateFormat("yyyy-MM-dd");
        writeDate.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        String findate = writeDate.format(datetalaga);

        return findate;
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

    public String getDestinationName(String id){
        String name = "";
        SQLiteDatabase db = rate.getReadableDatabase();
        String x = " SELECT * FROM "+rate.tbname_sourcedes
                +" WHERE "+rate.sd_id+" = '"+id+"' AND "+rate.sd_type+" = 'destination'";
        Cursor f = db.rawQuery(x, null);
        if (f.moveToNext()){
            name = f.getString(f.getColumnIndex(rate.sd_name));
        }
        return name;
    }

    public ArrayList<String> getBoxesInfo(String booknum){
        ArrayList<String> bookingnum = new ArrayList<>();
            SQLiteDatabase db = gen.getReadableDatabase();
            Cursor x = db.rawQuery(" SELECT * FROM " + gen.tbname_booking_consignee_box
                    +" LEFT JOIN "+gen.tbname_partner_inventory
                    +" ON "+gen.tbname_partner_inventory+"."+gen.partinv_boxnumber+" = "
                    +gen.tbname_booking_consignee_box+"."+gen.book_con_box_number
                    + " WHERE " + gen.book_con_transaction_no + " = '" + booknum + "' AND "
                    +gen.tbname_partner_inventory+"."+gen.partinv_stat+" != '0'", null);
            x.moveToFirst();
            while (!x.isAfterLast()) {
                String y = x.getString(x.getColumnIndex(gen.book_con_box_number));
                if (!bookingnum.contains(y)) {
                    bookingnum.add(y);
                }
                x.moveToNext();
            }
            x.close();
        return bookingnum;
    }

    public String getBoxtypeInfo(String booknum){
        String tp = "";
            SQLiteDatabase db = gen.getReadableDatabase();
            Cursor x = db.rawQuery(" SELECT * FROM " + gen.tbname_booking_consignee_box
                    + " WHERE " + gen.book_con_box_number + " = '" + booknum + "'", null);
            if (x.moveToNext()){
                tp = x.getString(x.getColumnIndex(gen.book_con_boxtype));
            }
            x.close();
            return tp;
    }

    public String getBoxId(String boxname){
        String tp = "";
            SQLiteDatabase db = gen.getReadableDatabase();
            Cursor x = db.rawQuery(" SELECT * FROM " + gen.tbname_boxes
                    + " WHERE " + gen.box_name + " = '" + boxname + "'", null);
            if (x.moveToNext()){
                tp = x.getString(x.getColumnIndex(gen.box_id));
            }
            x.close();
            return tp;
    }

    //get all unloaded items in order to be distributed
//    public ArrayList<ListViewItemDTO> getAllUnloaded(String destid){
//        ArrayList<ListViewItemDTO> results = new ArrayList<ListViewItemDTO>();
//        SQLiteDatabase db = rate.getReadableDatabase();
//            String q = " SELECT * FROM " + rate.tbname_partnerboxes_todistribute
//                    + " JOIN " + rate.tbname_sourcedes + " ON "
//                    + rate.tbname_partnerboxes_todistribute + "." + rate.partnerboxes_destination
//                    + " = " + rate.tbname_sourcedes + "." + rate.sd_id
//                    + " WHERE " + rate.partnerboxes_actstat + " = '0' AND "
//                    + rate.tbname_partnerboxes_todistribute + "." + rate.partnerboxes_destination
//                    +" = '"+destid+"'";
//            Cursor res = db.rawQuery(q, null);
//            res.moveToFirst();
//            while (!res.isAfterLast()) {
//                String bnumid = res.getString(res.getColumnIndex(rate.partnerboxes_id));
//                String bnum = res.getString(res.getColumnIndex(rate.partnerboxes_boxnum));
//                String dest = res.getString(res.getColumnIndex(rate.sd_name));
//
//                ListViewItemDTO dto = new ListViewItemDTO();
//                dto.setChecked(false);
//                dto.setItemText(bnum);
//                dto.setItemsub(dest);
//
//                results.add(dto);
//                res.moveToNext();
//            }
//
//        // Add some more dummy data for testing
//        return results;
//    }

    public void getBoxesWithTrans(String trans, String destid){
        SQLiteDatabase db = rate.getReadableDatabase();
        String x = " SELECT * FROM "+rate.tbname_partnerboxes_todistribute
                +" WHERE "+rate.partnerboxes_booktrans+" = '"+trans+"' AND "
                +rate.partnerboxes_destination+" = '"+destid+"'";
        Cursor c = db.rawQuery(x, null);
        c.moveToFirst();
        while(!c.isAfterLast()){
            String bn = c.getString(c.getColumnIndex(rate.partnerboxes_boxnum));
            if (!numbers.contains(bn)){
                numbers.add(bn);
            }else {
                String e = "Box number has been selected, please try another.";
                customToast(e);
            }
            c.moveToNext();
        }
        c.close();
    }

    public String branchName(){
        String branchname = "";
        SQLiteDatabase db = rate.getReadableDatabase();
        Cursor x = db.rawQuery(" SELECT * FROM "+rate.tbname_branch
                +" WHERE "+rate.branch_id+" = '"+helper.getBranch(""+helper.logcount())+"'", null);
        if (x.moveToNext()){
            branchname = x.getString(x.getColumnIndex(rate.branch_name));
        }
        x.close();
        return branchname;
    }

    public String getDestID(String name){
        String id = "";
        SQLiteDatabase db = rate.getReadableDatabase();
        Cursor x = db.rawQuery(" SELECT * FROM "+rate.tbname_sourcedes
                +" WHERE "+rate.sd_name+" = '"+name+"'", null);
        if (x.moveToNext()){
            id = x.getString(x.getColumnIndex(rate.sd_id));
        }
        x.close();
        return id;
    }

    public String[] getDestinations(){
        SQLiteDatabase db = rate.getReadableDatabase();
        ArrayList<String> numbers = new ArrayList<String>();
        Cursor c = db.rawQuery(" SELECT * FROM " + rate.tbname_sourcedes
                +" WHERE "+rate.sd_type+" = 'destination'", null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            numbers.add(c.getString(c.getColumnIndex(rate.sd_name)));
            c.moveToNext();
        }
        return numbers.toArray(new String[numbers.size()]);
    }

    public void updateBnum(ArrayList<String> bnumbers){
        SQLiteDatabase db = gen.getWritableDatabase();
        for (String bn : bnumbers) {
            ContentValues cv = new ContentValues();
            cv.put(gen.partinv_stat, "3");
            db.update(gen.tbname_partner_inventory, cv,
                    gen.partinv_boxnumber + " = '" + bn + "'", null);
        }
        db.close();
    }


}
