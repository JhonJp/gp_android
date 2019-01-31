package com.example.admin.gpxbymodule;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.app.Dialog;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class Loading extends Fragment {

    Button add;
    RatesDB rate;
    SQLiteDatabase db;
    HomeDatabase helper;
    GenDatabase gen;
    IntentIntegrator scanIntegrator;
    TextView loaddate,eta, etd;
    EditText contain;
    AutoCompleteTextView shipper;
    ImageButton addit;
    Calendar calendar;
    ListView lv;
    TextView total;
    String trans;
    String value;
    Load_home loadhome;
    FloatingActionButton fab;
    NavigationView navigationView;
    ProgressDialog progressBar;
    LinearLayout dum;
    DatePickerFragment date;
    ArrayList<String> result;
    ImageButton ld, adeta, adetd;
    ArrayList<String> boxes;
    int error = 0;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.loading_fragment, null);
        add = (Button)view.findViewById(R.id.add);
        loaddate = (TextView)view.findViewById(R.id.load_dateship_input);
        shipper = (AutoCompleteTextView)view.findViewById(R.id.shipperinput);
        eta = (TextView)view.findViewById(R.id.etainput);
        etd = (TextView)view.findViewById(R.id.etdinput);
        contain = (EditText) view.findViewById(R.id.containernuminput);
        total = (TextView)view.findViewById(R.id.total);
        lv = (ListView)view.findViewById(R.id.lv);
        dum = (LinearLayout)view.findViewById(R.id.dummy);
        ld = (ImageButton)view.findViewById(R.id.addloaddate);
        adeta = (ImageButton)view.findViewById(R.id.addeta);
        adetd = (ImageButton)view.findViewById(R.id.addetd);
        addit = (ImageButton)view.findViewById(R.id.dropload);
        dum.requestFocus();
        boxes = new ArrayList<>();


        loadhome = (Load_home)getActivity();

        rate = new RatesDB(getContext());
        gen = new GenDatabase(getContext());
        helper = new HomeDatabase(getContext());

        try {
            if (loadhome.getLoadtrans() == null) {
                loadhome.setLoadtrans(generateTrans());
                Log.e("transnum", loadhome.getLoadtrans());
            } else {
                loadhome.setLoadtrans(loadhome.getLoadtrans());
                Log.e("transnum", loadhome.getLoadtrans());
            }
            autoCom();
            loaddate.setText(datereturn());
            eta.setText(datereturn());
            etd.setText(datereturn());
            clicks();
            add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    scanpermit();
                }
            });
        }catch (Exception e){}

        load();
        scrolllist();
        captureimage();

        return view;
    }

    public void autoCom(){
        String[] numbers = rate.getAllItemsThree();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>
                (getContext(), android.R.layout.select_dialog_item, numbers);
        shipper.setThreshold(1);
        shipper.setAdapter(adapter);
    }

    public void clicks(){
        try {
            loaddate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDatePicker();
                }
            });
            ld.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDatePicker();
                }
            });
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
            addit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addnewItem();
                }
            });
        }catch (Exception e){}
    }

    public void addnewItem(){
        final AlertDialog.Builder denom = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getLayoutInflater();
        View d = inflater.inflate(R.layout.add_shipper,null);
        denom.setView(d);
        final EditText am = (EditText)d.findViewById(R.id.allowanceinput);
        denom.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!am.getText().toString().equals("")) {
                    if (checkName(am.getText().toString())) {
                        addItemsTodb(am.getText().toString());
                        shipper.setText(am.getText().toString());
                        autoCom();
                        dialog.dismiss();
                    }else{
                        String h = " Name already exists, please add another.";
                        customToast(h);
                    }
                }
            }
        });
        denom.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        denom.setTitle("Add new shipper");
        denom.show();
    }

    public void addItemsTodb(String name){
        SQLiteDatabase db = rate.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(rate.expit_name, name);
        cv.put(rate.expit_type, "3");
        db.insert(rate.tbname_exp_item, null, cv);
        db.close();
    }

    public boolean checkName(String n){
        SQLiteDatabase db = rate.getReadableDatabase();
        String y = " SELECT * FROM "+rate.tbname_exp_item
                +" WHERE "+rate.expit_name+" = '"+n+"'";
        Cursor x = db.rawQuery(y, null);
        if (x.getCount() != 0 ){
            return false;
        }else{
            return true;
        }
    }

    //load date
    private void showDatePicker() {
        date = new DatePickerFragment();
        Calendar calender = Calendar.getInstance();
        Bundle args = new Bundle();
        args.putInt("year", calender.get(Calendar.YEAR));
        args.putInt("month", calender.get(Calendar.MONTH));
        args.putInt("day", calender.get(Calendar.DAY_OF_MONTH));
        date.setArguments(args);
        date.setCallBack(ondate);
        date.show(getFragmentManager(), "Date Picker");
    }

    private void showDateETA() {
        date = new DatePickerFragment();
        Calendar calender = Calendar.getInstance();
        Bundle args = new Bundle();
        args.putInt("year", calender.get(Calendar.YEAR));
        args.putInt("month", calender.get(Calendar.MONTH));
        args.putInt("day", calender.get(Calendar.DAY_OF_MONTH));
        date.setArguments(args);
        date.setCallBack(dateeta);
        date.show(getFragmentManager(), "Date Picker");
    }

    private void showDateETD() {
        date = new DatePickerFragment();
        Calendar calender = Calendar.getInstance();
        Bundle args = new Bundle();
        args.putInt("year", calender.get(Calendar.YEAR));
        args.putInt("month", calender.get(Calendar.MONTH));
        args.putInt("day", calender.get(Calendar.DAY_OF_MONTH));
        date.setArguments(args);
        date.setCallBack(dateetd);
        date.show(getFragmentManager(), "Date Picker");
    }

    DatePickerDialog.OnDateSetListener dateetd = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            etd.setText(String.valueOf(year) + "-" + String.valueOf(monthOfYear+1)
                    + "-" + String.valueOf(dayOfMonth));
            date.dismiss();
        }
    };

    DatePickerDialog.OnDateSetListener dateeta = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            eta.setText(String.valueOf(year) + "-" + String.valueOf(monthOfYear+1)
                    + "-" + String.valueOf(dayOfMonth));
            date.dismiss();
        }
    };

    DatePickerDialog.OnDateSetListener ondate = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            loaddate.setText(String.valueOf(year) + "-" + String.valueOf(monthOfYear+1)
                    + "-" + String.valueOf(dayOfMonth));
            date.dismiss();
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle("Loading");
        setHasOptionsMenu(true);
    }

    public void captureimage(){
        try {
            add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    capture();
                }
            });
        }catch (Exception e){}
    }

    public void capture(){
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED) {

            ActivityCompat.requestPermissions(getActivity(), new String[] {Manifest.permission.CAMERA}, 1);
        }else{
            scanIntegrator = IntentIntegrator.forSupportFragment(Loading.this);
            scanIntegrator.setPrompt("Scan barcode");
            scanIntegrator.setBeepEnabled(true);
            scanIntegrator.setOrientationLocked(true);
            scanIntegrator.setCaptureActivity(CaptureActivityPortrait.class);
            scanIntegrator.initiateScan();
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.loadfragsave).setVisible(true);
        menu.findItem(R.id.unloadfragsave).setVisible(false);
        menu.findItem(R.id.listunloads).setVisible(false);
        menu.findItem(R.id.listloads).setVisible(true);
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.loadfragsave) {
            checkFieldsAndSave();
        }
        else if (id == R.id.listloads) {
            startActivity(new Intent(getContext(), Loadinglist.class));
            getActivity().finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void load(){
        try {
            result = boxes;
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),
                    android.R.layout.simple_list_item_1, result);
            lv.setAdapter(adapter);
            total.setText("Total : " + result.size() + " box(s)");
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                    final String ids = result.get(position);
                    final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Delete this data ?")
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    result.remove(result.get(position).indexOf(ids));
                                    load();
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
            scanIntegrator = IntentIntegrator.forSupportFragment(Loading.this);
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
                    if (checkFromAcceptance(bn)) {
                        if (checkBoxnum(bn)) {
                                if (!boxes.contains(bn)) {
                                    boxes.add(bn);
                                    load();
                                }else{
                                    String b = "Box number has been scanned, please try another.";
                                    customToast(b);
                                }
                        } else {
                            String b = "Box number or barcode exists in the database.";
                            customToast(b);
                        }
                    } else {
                        String b = "Box number or barcode is invalid, please add it in acceptance.";
                        customToast(b);
                    }
                }
                else
                super.onActivityResult(requestCode, resultCode, data);

        }catch (Exception e){}
    }

    public void scrolllist(){
        try {
            lv.setOnTouchListener(new View.OnTouchListener() {
                // Setting on Touch Listener for handling the touch inside ScrollView
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    // Disallow the touch request for parent scroll on touch of child view
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    return false;
                }
            });
        }catch (Exception e){}
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

    public String generateTrans(){
        Date datetalaga = Calendar.getInstance().getTime();
        SimpleDateFormat writeDate = new SimpleDateFormat("yyyyMMddHHmmss");
        writeDate.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        String reservationnumber = helper.logcount()+""+writeDate.format(datetalaga);

        Log.e("trans", reservationnumber);
        return reservationnumber;
    }

    public boolean checkFromAcceptance(String bn){
        SQLiteDatabase db = gen.getReadableDatabase();
        Cursor x = db.rawQuery(" SELECT * FROM "+gen.tbname_accept_boxes
                +" WHERE "+gen.acc_box_boxnumber+" = '"+bn+"'", null);
        if (x.getCount() == 0){
            return false;
        }else {
            return true;
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        if (loadhome.getLoadtrans() != null){
            loadhome.setLoadtrans(loadhome.getLoadtrans());
        }
    }

    public String datereturn(){
        Date datetalaga = Calendar.getInstance().getTime();
        SimpleDateFormat writeDate = new SimpleDateFormat("yyyy-MM-dd");
        writeDate.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        String findate = writeDate.format(datetalaga);

        return findate;
    }

    public boolean checkBoxnum(String bn){
        SQLiteDatabase db = rate.getReadableDatabase();
        Cursor x = db.rawQuery(" SELECT * FROM "+rate.tb_loadbox
                +" WHERE "+rate.load_boxnum+" = '"+bn+"' AND "+rate.load_box_stat+" = '2'", null);
        if (x.getCount() == 0){
            return true;
        }else {
            return false;
        }
    }

    public void checkFieldsAndSave(){
        try {
            String ldate = loaddate.getText().toString();
            String shname = shipper.getText().toString();
            String cont = contain.getText().toString();
            String etafield = eta.getText().toString();
            String etdfield = etd.getText().toString();

            if ((ldate.equals("")) || (shname.equals("")) || (cont.equals("")) ||
                    (etafield.equals("")) || (etdfield.equals(""))) {
                String fields = "Form fields are missing, kindly fill it up correctly. Thank you.";
                customToast(fields);
            } else if (result.size() == 0) {
                String fields = "Please scan a barcode for loading, Thank you.";
                customToast(fields);
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date eta = sdf.parse(etafield);
                Date etad = sdf.parse(etdfield);
                if (etad.after(eta)){
                    String fields = "Please enter a date not less than ETD, Thank you.";
                    customToast(fields);
                }else {
                    for (String bn : boxes){
                        rate.addload(loadhome.getLoadtrans(), bn , "2");
                    }
                    rate.addFinalload(loadhome.getLoadtrans(), ldate, shname,
                            cont, etafield, etdfield, helper.logcount() + "", "1", "1");
                    String inctype = "Loading";
                    gen.addTransactions(inctype, "" + helper.logcount(),
                            "New loading with transaction number " + loadhome.getLoadtrans(),
                            datereturn(), datereturn());

                    loadhome.setLoadtrans(null);
                    loadhome.setLoadtrans(generateTrans());
                    loadingPost(getView());
                    load();
                    dum.requestFocus();
                }
            }
        }catch (Exception e){}
    }

    public void loadingPost(final View v){
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
                    Thread.sleep(5000);
                }
                catch (Exception e) { } // Just catch the InterruptedException

                handler.post(new Runnable() {
                    public void run() {
                        progressBar.dismiss();
                        final AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                        builder.setTitle("Information confirmation")
                                .setMessage("Data has been saved successfully, thank you.")
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        startActivity(new Intent(getContext(), Loadinglist.class));
                                        getActivity().finish();
                                        dialog.dismiss();
                                    }
                                });
                        // Create the AlertDialog object and show it
                        builder.create().show();
                    }
                });
            }
        }).start();
    }

}

