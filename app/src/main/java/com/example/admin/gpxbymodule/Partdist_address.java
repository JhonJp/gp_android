package com.example.admin.gpxbymodule;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class Partdist_address extends Fragment {

    GenDatabase gen;
    HomeDatabase helper;
    RatesDB rate;
    EditText trucknum, remarks;
    FrameLayout captive;
    int id;
    String[] name;
    int CAMERA_REQUEST = 1;
    Partner_distribution dist;
    EditText trans;
    AutoCompleteTextView drivern;
    RatesDB rates;
    int wareid;
    TextView hint, etd, eta;
    Spinner distname, spin, drive;
    ArrayList<LinearItem> drivernames;
    IntentIntegrator scanIntegrator;
    LinearLayout dummy;
    Spinner modes;
    DatePickerFragment dateeta,dateetd;
    Calendar calendar;
    TimePickerDialog timePickerDialog;
    ImageButton adeta,adetd;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.partdist_address, null);
        trucknum = (EditText)view.findViewById(R.id.trucknumber);
        remarks = (EditText)view.findViewById(R.id.dist_boxremarks_input);
        drivern = (AutoCompleteTextView)view.findViewById(R.id.drivername_input);
        trans = (EditText)view.findViewById(R.id.transnuminput);
        hint = (TextView)view.findViewById(R.id.imageshint);
        eta = (TextView)view.findViewById(R.id.etainput);
        etd = (TextView)view.findViewById(R.id.etdinput);
        distname = (Spinner)view.findViewById(R.id.distnameinput);
        spin = (Spinner)view.findViewById(R.id.trans);
        modes = (Spinner)view.findViewById(R.id.modeship);
        dist = (Partner_distribution) getActivity();
        dummy = (LinearLayout)view.findViewById(R.id.dummyfocus);
        adeta = (ImageButton)view.findViewById(R.id.addeta);
        adetd = (ImageButton)view.findViewById(R.id.addetd);

        gen = new GenDatabase(getContext());
        rate = new RatesDB(getContext());
        rates = new RatesDB(getContext());
        helper = new HomeDatabase(getContext());
        trucknum.setText(dist.getDisttrucknumber());
        remarks.setText(dist.getRemarks());
        dummy.requestFocus();
        if (dist.getTrans() != null){
            dist.setTrans(dist.getTrans());
            Log.e("transactionnum", dist.getTrans());
        }

        if (dist.getFrag() == null){
            dist.setFrag("address");
        }
        else{
            dist.setFrag("address");
        }
        if (dist.getDrivername() != null){
            drivern.setText(dist.getDrivername());
        }

        if (dist.getEtanow() != null){
            eta.setText(dist.getEtanow());
        }

        if (dist.getDisttype() != null) {
            if (dist.getDisttype().equals("Partner - Hub")) {
                if (dist.getTransactionnumhub() != null) {
                    trans.setText(dist.getTransactionnumhub());
                }
            }
        }
        if (dist.getBoxnums().size() != 0){
            dist.setBoxnums(dist.getBoxnums());
        }else{
            dist.setBoxnums(dist.getBoxnums());
        }
        spinnerlist();
        autoNameDriver();

        eta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDateETA();
            }
        });
        adeta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDateETA();
            }
        });
        etd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDateETD();
            }
        });
        adetd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDateETD();
            }
        });

        capt();
        return  view;
    }

    public void capt() {
        try {
            trans.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus){
                        scanpermit();
                    }
                }
            });
            trans.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    scanpermit();
                }
            });

        }catch (Exception e){}
    }

    public void spinnerlist(){
        try {

            final String[] modeitems = new String[]{"Via Sea","Via Air","Via Land"};
            ArrayAdapter<String> modeadapter =
                    new ArrayAdapter<>(getContext(), R.layout.spinneritem,
                            modeitems);
            modes.setAdapter(modeadapter);
            modeadapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
            if (dist.getMode() != null) {
                modes.setSelection(modeadapter.getPosition(dist.getMode()));
            }
            final String[] items = new String[]{"Direct","Partner - Hub","Partner - Area"};
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
                    String choice = spin.getSelectedItem().toString();
                    namespinner(choice);
                    Log.e("choice", choice);
                    trans.clearFocus();
                    autoTrans(choice);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    String choice = spin.getSelectedItem().toString();
                    namespinner(choice);
                    trans.clearFocus();
                    Log.e("choice", choice);
                    autoTrans(choice);

                }
            });
        }catch (Exception e){}
    }

    private void showDateETA() {
        dateeta = new DatePickerFragment();
        Calendar calender = Calendar.getInstance();
        Bundle args = new Bundle();
        args.putInt("year", calender.get(Calendar.YEAR));
        args.putInt("month", calender.get(Calendar.MONTH));
        args.putInt("day", calender.get(Calendar.DAY_OF_MONTH));
        dateeta.setArguments(args);
        dateeta.setCallBack(datei);
        dateeta.show(getFragmentManager(), "Date Picker");
    }

    private void showDateETD() {
        dateetd = new DatePickerFragment();
        Calendar calender = Calendar.getInstance();
        Bundle args = new Bundle();
        args.putInt("year", calender.get(Calendar.YEAR));
        args.putInt("month", calender.get(Calendar.MONTH));
        args.putInt("day", calender.get(Calendar.DAY_OF_MONTH));
        dateetd.setArguments(args);
        dateetd.setCallBack(datetd);
        dateetd.show(getFragmentManager(), "Date Picker");
    }

    DatePickerDialog.OnDateSetListener datei = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, final int year, final int monthOfYear,
                              final int dayOfMonth) {
            calendar = Calendar.getInstance();
            timePickerDialog = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker timePicker, int hourOfDay, int minutes) {
                    eta.setText(String.valueOf(year) + "-" + String.valueOf(monthOfYear+1)
                            + "-" + String.valueOf(dayOfMonth)+" "+hourOfDay+":"+minutes);
                }
            }, calendar.get(Calendar.HOUR_OF_DAY),calendar.get(Calendar.MINUTE), false);
            timePickerDialog.show();
            dateeta.dismiss();
        }
    };

    DatePickerDialog.OnDateSetListener datetd = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, final int year, final int monthOfYear,
                              final int dayOfMonth) {
            calendar = Calendar.getInstance();
            timePickerDialog = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker timePicker, int hourOfDay, int minutes) {
                    etd.setText(String.valueOf(year) + "-" + String.valueOf(monthOfYear+1)
                            + "-" + String.valueOf(dayOfMonth)+" "+hourOfDay+":"+minutes);
                }
            }, calendar.get(Calendar.HOUR_OF_DAY),calendar.get(Calendar.MINUTE), false);
            timePickerDialog.show();
            dateetd.dismiss();
        }
    };

    public void autoTrans(String choice){
        if (!choice.equals("Partner - Hub")){
            trans.setEnabled(false);
        }else{
            trans.setEnabled(true);
            trans.setFocusable(true);
            trans.setClickable(true);
        }
    }

    public String[] getYourBranch(String type){
        Cursor cursor = rate.getReadableDatabase().rawQuery("SELECT "+rate.branch_name+" FROM "
                + rate.tbname_branch+" WHERE "+rate.branch_type+" = '"+type+"'", null);
        cursor.moveToFirst();
        ArrayList<String> names = new ArrayList<String>();
        while(!cursor.isAfterLast()) {
            names.add(cursor.getString(cursor.getColumnIndex(rate.branch_name)));
            cursor.moveToNext();
        }
        cursor.close();
        return names.toArray(new String[names.size()]);
    }

    public void namespinner(String type){
        try {
            if (type.equals("Direct")){
                name = new String[]{getBranchName()};
                ArrayAdapter<String> branchadapter =
                        new ArrayAdapter<>(getContext(), R.layout.spinneritem,
                                name);
                distname.setAdapter(branchadapter);
                branchadapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
                distname.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        dist.setDistname(distname.getSelectedItem().toString());
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        dist.setDistname(distname.getSelectedItem().toString());
                    }
                });
            }else {
                //condition for branch names
                name = getYourBranch(type);
                ArrayAdapter<String> branchadapter =
                        new ArrayAdapter<>(getContext(), R.layout.spinneritem,
                                name);
                distname.setAdapter(branchadapter);
                branchadapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
                distname.setPrompt("Select " + type.toLowerCase());
                distname.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        dist.setDistname(distname.getSelectedItem().toString());
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        dist.setDistname(distname.getSelectedItem().toString());
                    }
                });
            }
        }catch (Exception e){}
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle("Destination Info");
        setHasOptionsMenu(true);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.distprev).setVisible(false);
        menu.findItem(R.id.distnext).setVisible(true);
        menu.findItem(R.id.action_savedistribute).setVisible(false);
        menu.findItem(R.id.syncdist).setVisible(false);
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onPause(){
        try {
            String eata = eta.getText().toString();
            String eatd = etd.getText().toString();
            String truck = trucknum.getText().toString();
            String rem = remarks.getText().toString();
            String driver = drivern.getText().toString();
            String transactionnum = trans.getText().toString();
            String mode = modes.getSelectedItem().toString();

            if ((truck.equals("")) || (driver.equals(""))) {
                String x = "Driver name and truck number is required.";
                customToast(x);
            }
            if ((eata.equals("ETA")) || (eatd.equals("ETD"))) {
                String x = "Please indicate ETA and ETD.";
                customToast(x);
            }
            if (dist.getTrans() != null) {
                dist.setTrans(dist.getTrans());
            }
            String f_type = spin.getSelectedItem().toString();
            if (f_type.equals("Partner - Hub")) {
                dist.setDistname(distname.getSelectedItem().toString());
                if ((transactionnum.equals(""))) {
                    String t = "Transaction number is empty.";
                    customToast(t);
                } else {
                    dist.setTransactionnumhub(transactionnum);
                }
            } else if (f_type.equals("Partner - Area")) {
                dist.setDistname(distname.getSelectedItem().toString());
            }

            dist.setEtanow(eata);
            dist.setEtdnow(eatd);
            dist.setMode(mode);
            dist.setTransactionnumhub(transactionnum);
            dist.setDrivername(driver);
            dist.setDisttype(spin.getSelectedItem().toString());
            dist.setDisttrucknumber(truck);
            dist.setRemarks(rem);

            if (dist.getBoxnums().size() != 0){
                dist.setBoxnums(dist.getBoxnums());
            }else{
                dist.setBoxnums(dist.getBoxnums());
            }
            Log.e("boxnumbersadd", dist.getBoxnums().toString());
            Log.e("drivername", dist.getDrivername());
            Log.e("transactionum", dist.getTransactionnumhub());
            Log.e("distname", dist.getDistname());
            Log.e("disttype", dist.getDisttype());
            Log.e("disttruck", dist.getDisttrucknumber());
            Log.e("distrem",dist.getRemarks());

        }catch (Exception e){}
        super.onPause();
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

    public String datereturn(){
        Date datetalaga = Calendar.getInstance().getTime();
        SimpleDateFormat writeDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
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

    public void saveInventory(String bn){
        SQLiteDatabase db = gen.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(gen.partinv_boxnumber, bn);
        cv.put(gen.partinv_boxtype_fillempty, "1");
        cv.put(gen.partinv_boxtype, getBox(bn));
        cv.put(gen.partinv_stat, "1");
        db.insert(gen.tbname_partner_inventory, null, cv);
        db.close();
    }

    public void alert(int ok){
        try {
            if (ok == 0) {
                String a = "Transaction has been successful, thank you.";
                customToast(a);
                getActivity().recreate();
                new Distributionlist();
            } else {
                String a = "Transaction failed, please try again.";
                customToast(a);
            }
        }catch (Exception e){}
    }

    public String getBookTrans(String bn){
        String tp = "";
        SQLiteDatabase db = rate.getReadableDatabase();
        Cursor x = db.rawQuery(" SELECT * FROM " + rate.tbname_partnerboxes_todistribute
                + " WHERE " + rate.partnerboxes_boxnum + " = '" + bn + "'", null);
        if (x.moveToNext()){
            tp = x.getString(x.getColumnIndex(rate.partnerboxes_booktrans));
        }
        x.close();
        return tp;
    }

    public void updateStatBN(String bn){
        SQLiteDatabase db = rate.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(rate.partnerboxes_actstat, "1");
        db.update(rate.tbname_partnerboxes_todistribute,
                cv, rate.partnerboxes_boxnum+" = '"+bn+"'", null);
        db.close();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_REQUEST) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Toast.makeText(getContext(), "camera permission granted", Toast.LENGTH_LONG).show();
                Intent cameraIntent = new
                        Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);

            } else {
                String x = "Please allow the camera permission.";
                customToast(x);
            }

        }
    }

    public void scanpermit(){
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(getActivity(), new String[] {Manifest.permission.CAMERA}, 100);
        }else{
            scanIntegrator = IntentIntegrator.forSupportFragment(Partdist_address.this);
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
                if (checkFocusTrans()){
                    trans.setText(bn);
                    dummy.requestFocus();
                    trans.clearFocus();
                }
            }
            else
                dummy.requestFocus();
                super.onActivityResult(requestCode, resultCode, data);

        }catch (Exception e){}
    }

    public String getBranchName(){
        String branchname = null;
        SQLiteDatabase db = rate.getReadableDatabase();
        Cursor x = db.rawQuery(" SELECT * FROM "+rate.tbname_branch
                +" WHERE "+rate.branch_id+" = '"+helper.getBranch(""+helper.logcount())+"'", null);
        if (x.moveToNext()){
            branchname = x.getString(x.getColumnIndex(rate.branch_name));
        }
        x.close();
        return branchname;
    }

    public boolean checkFocusTrans(){
        if(getActivity().getCurrentFocus().getId() == trans.getId()){
            Log.e("focus", trans.hasFocus()+"");
            return true;
        }else{
            Log.e("focus", trans.hasFocus()+"");
            return false;
        }
    }

    public boolean checkFocusDriver(){
        if(getActivity().getCurrentFocus().getId() == drivern.getId()){
            Log.e("focus", drivern.hasFocus()+"");
            return true;
        }else{
            Log.e("focus", drivern.hasFocus()+"");
            return false;
        }
    }

    public String[] getSalesDriver(String post, String branch) {
        SQLiteDatabase db = gen.getReadableDatabase();
        ArrayList<String> names = new ArrayList<String>();
        Cursor c = db.rawQuery(" SELECT * FROM " + gen.tbname_employee
                +" LEFT JOIN "+gen.tbname_branch
                +" ON "+gen.tbname_branch+"."+gen.branch_id+" = "+gen.tbname_employee+"."+gen.emp_branch
                +" WHERE "+gen.emp_post+" = '"+post+"' AND "+gen.emp_branch+" = '"+branch+"'", null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            String name = c.getString(c.getColumnIndex(gen.emp_first))+" "
                    +c.getString(c.getColumnIndex(gen.emp_last));
            names.add(name);
            c.moveToNext();
        }
        c.close();
        return names.toArray(new String[names.size()]);
    }

    public void autoNameDriver(){
        try {
            String[] names = getSalesDriver("Partner Driver", helper.getBranch(helper.logcount()+""));
            ArrayAdapter<String> adapter = new ArrayAdapter<String>
                    (getContext(), android.R.layout.simple_list_item_1, names);
            drivern.setThreshold(1);
            drivern.setAdapter(adapter);
            drivern.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String val = (String) parent.getItemAtPosition(position);
                    drivern.setText(val);
                }
            });
            Log.e("drivers", Arrays.toString(names)+" ,"+helper.getBranch(helper.logcount()+""));

        }catch (Exception e){
            Log.e("error", e.getMessage());
        }
    }

}
