package com.example.admin.gpxbymodule;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
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
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
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
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class Unload_main extends Fragment {

    HomeDatabase helper;
    SQLiteDatabase db;
    GenDatabase gen;
    RatesDB rate;
    Button add;
    IntentIntegrator scanIntegrator;
    EditText con, plate_no, drivername;
    AutoCompleteTextView forward;
    String name, number;
    Calendar calendar;
    TextView total, loaddate, eta;
    ListView lv;
    String value;
    Load_home loadhome;
    String trans;
    FloatingActionButton fab;
    NavigationView navigationView;
    ProgressDialog progressBar;
    int CAMERA_REQUEST = 500;
    LinearLayout dum;
    int error = 0;
    DatePickerFragment date;
    ArrayList<String> result;
    ArrayList<String> boxes;
    ImageButton adeta, addit;
    TimePickerDialog timePickerDialog;
    TextView timestart, timeend;
    FrameLayout timeclick;
    boolean unlimage = false;
    String boxnumbertest = null;
    byte[] imgtest = null;
    ArrayList<byte[]> imagesperbox;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.unloading_fragment, null);
        add = (Button)view.findViewById(R.id.addunload);
        loaddate = (TextView) view.findViewById(R.id.load_dateship_input);
        eta = (TextView) view.findViewById(R.id.etainput);
        timestart = (TextView) view.findViewById(R.id.timestartinput);
        timeend = (TextView) view.findViewById(R.id.timeendinput);
        forward = (AutoCompleteTextView) view.findViewById(R.id.frowarderinput) ;
        con = (EditText) view.findViewById(R.id.containernuminput);
        drivername = (EditText) view.findViewById(R.id.driverinput);
        plate_no = (EditText) view.findViewById(R.id.platenoinput);
        total = (TextView)view.findViewById(R.id.total);
        timeclick = (FrameLayout) view.findViewById(R.id.timeendclick);
        dum = (LinearLayout)view.findViewById(R.id.dummy);
        lv = (ListView)view.findViewById(R.id.lv);
        adeta = (ImageButton)view.findViewById(R.id.addeta);
        addit = (ImageButton)view.findViewById(R.id.dropload);
        loadhome = (Load_home)getActivity();
        boxes = new ArrayList<>();
        imagesperbox = new ArrayList<>();

        rate = new RatesDB(getContext());
        gen = new GenDatabase(getContext());
        helper = new HomeDatabase(getContext());
        try{
            if (loadhome.getUnloadtrans() == null){
                loadhome.setUnloadtrans(generateTrans());
                Log.e("transnum", loadhome.getUnloadtrans());
            }else{
                loadhome.setUnloadtrans(loadhome.getUnloadtrans());
                Log.e("transnum", loadhome.getUnloadtrans());
            }
        }catch (Exception e){}

        loaddate.setText(datereturn());
        eta.setText(datereturn()+" "+returnHourMin());

        timeclick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayTimePicker();
            }
        });
        //forward.setText(getBranchName());
        textChanges();
        autoCom();
        bar();
        unload();
        scrolllist();

        return view;
    }

    public void autoCom(){
        String[] numbers = rate.getAllItemsThree();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>
                (getContext(), android.R.layout.select_dialog_item, numbers);
        forward.setThreshold(1);
        forward.setAdapter(adapter);
    }

    public void unload () {
        try {
            final ArrayList<LinearItem> results = new ArrayList<LinearItem>();
            final ArrayList<String> resultnums = boxes;
            final ArrayList<byte[]> resimge = imagesperbox;
            if (resultnums.size() != 0) {
                for (int i = 0; i < resultnums.size(); i++) {
                    String img = Html.fromHtml("<small>"+resimge.get(i)+"</small>").toString();
                    LinearItem list = new LinearItem(i + "", resultnums.get(i),img);
                    results.add(list);
                }
            }
            final LinearList myAdapter = new LinearList(getContext(), results);
            lv.setAdapter(myAdapter);
            total.setText("Total : " + resultnums.size() + " box(s)");
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                    final String ids = resultnums.get(position);
                    final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Delete this data ?")
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    boxes.remove(resultnums.get(position).indexOf(ids));
                                    imagesperbox.remove(result.get(position).indexOf(ids));
                                    unload();
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

    public void bar(){
        try {
            add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //viewBoxes(null,null);
                    scanpermit();
                }
            });
        }catch (Exception e){}
    }

    public void scanpermit(){
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED) {

            ActivityCompat.requestPermissions(getActivity(), new String[] {Manifest.permission.CAMERA}, 100);
        }else{
            scanIntegrator = IntentIntegrator.forSupportFragment(Unload_main.this);
            scanIntegrator.setPrompt("Scan");
            scanIntegrator.setBeepEnabled(true);
            scanIntegrator.setOrientationLocked(true);
            scanIntegrator.setCaptureActivity(CaptureActivityPortrait.class);
            scanIntegrator.initiateScan();
        }
    }

    public void scrolllist(){
        try{
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
        denom.setTitle("Add new forwarder");
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

    DatePickerDialog.OnDateSetListener dateeta = new DatePickerDialog.OnDateSetListener() {
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
        setHasOptionsMenu(true);
        getActivity().setTitle("Unloading");
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.loadfragsave).setVisible(false);
        menu.findItem(R.id.unloadfragsave).setVisible(true);
        menu.findItem(R.id.listunloads).setVisible(true);
        menu.findItem(R.id.listloads).setVisible(false);
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.unloadfragsave) {
            checkFieldsAndSave();
        }
        else if (id == R.id.listunloads) {
            startActivity(new Intent(getContext(), Unloadinglist.class));
            getActivity().finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getContext(), "camera permission granted", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getContext(), "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }else if (requestCode == CAMERA_REQUEST) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getContext(), "camera permission granted", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getContext(), "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        try {
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                photo.compress(Bitmap.CompressFormat.PNG, 100, stream);

                byte[] bytimg = stream.toByteArray();
                Log.e("camera", "success " + bytimg + "/bn "+boxnumbertest);

                if (boxnumbertest != null){
                    imgtest = null;
                    imgtest = bytimg;
                    viewBoxes(boxnumbertest, bytimg);
                }else{
                    imgtest = null;
                    imgtest = bytimg;
                    viewBoxes(null, bytimg);
                }
            }else{
                Log.e("requestcode", requestCode + "");
                if (result.getContents() != null) {
                    String bn = result.getContents();
                    Log.e("boxnumber", bn);
                    if (checkIfExist(bn)) {
                        if (!boxes.contains(bn)) {
//                            boxes.add(bn);
                            if (!(timestart.getText().toString().equals("Time start"))) {
                                timestart.setText(timestart.getText().toString());
                            } else {
                                timestart.setText(returnHourMin());
                            }
                            boxnumbertest = bn;
                            Log.e("imgtest", imgtest+"");
                            if (imgtest != null){
                                viewBoxes(bn, imgtest);
                            }else {
                                viewBoxes(bn, null);
                            }
    //                        //unload();
                        } else {
                            String b = "Box number has been scanned, please try another.";
                            customToast(b);
                        }
                    } else {
                        String t = "Boxnumber exists in unloading database.";
                        customToast(t);
                    }
                } else{
                    String t = "No box number detected, please try again.";
                    customToast(t);
                }
            }
        } catch (Exception e) {}
    }

    public boolean checkNum(String bn){
        SQLiteDatabase db = gen.getReadableDatabase();
        Cursor x = db.rawQuery(" SELECT * FROM "+gen.tbname_partner_inventory
                +" WHERE "+gen.partinv_boxnumber+" = '"+bn+"'", null);
        if (x.getCount() == 0){
            return true;
        }else{
            return false;
        }
    }

    public void addBoxnum(String bn){
        SQLiteDatabase db = gen.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(gen.partinv_boxnumber, bn);
        cv.put(gen.partinv_boxtype_fillempty, "1");
        cv.put(gen.partinv_boxtype, getBox(bn));
        cv.put(gen.partinv_stat, "0");
        db.insert(gen.tbname_partner_inventory, null, cv);
        db.close();
    }

    public void deleteBoxnumberInventory(String bn) {
        SQLiteDatabase db = gen.getWritableDatabase();
        db.delete(gen.tbname_partner_inventory,
                gen.partinv_boxnumber + " = '"+bn+"' AND "
                        +gen.partinv_stat+" = '0'", null);
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

    public void viewBoxes(String number, final byte[] image){
        final AlertDialog.Builder boxdisplay = new AlertDialog.Builder(getContext());
        final LayoutInflater inflater = getLayoutInflater();
        final View d = inflater.inflate(R.layout.unl_box,null);
        boxdisplay.setView(d);
        final TextView boxnumber = (TextView) d.findViewById(R.id.boxnum);
        final ImageView addimage = (ImageView) d.findViewById(R.id.imgbox);
        final ImageButton addbox = (ImageButton) d.findViewById(R.id.editbox);
        Button ok = (Button)d.findViewById(R.id.confirm);
        Button cancel = (Button)d.findViewById(R.id.cancel);
        final AlertDialog td = boxdisplay.create();
        if (number != null){
            boxnumber.setText(number);
        }
        if (image != null){
            Bitmap bm = BitmapFactory.decodeByteArray(image,0,image.length);
            addimage.setImageBitmap(bm);
        }

        addbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                td.dismiss();
                scanpermit();
            }
        });

        addimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    td.dismiss();
                    Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, CAMERA_REQUEST);
                } catch (Exception e) {
                    td.dismiss();
                    ActivityCompat.requestPermissions(getActivity(),
                            new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST);
                }
            }
        });

        //confirm button
       ok.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               if (image == null){
                   String ni = "Please box image.";
                   customToast(ni);
               }else {
                   String bn = boxnumber.getText().toString();
                   if (!boxes.contains(bn)) {
                       boxes.add(bn);
                   }
                   if (!imagesperbox.contains(image)){
                       imagesperbox.add(image);
                   }
                   imgtest = null;
                   unload();
                   td.dismiss();
               }
           }
       });

       //cancel button
       cancel.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               td.dismiss();
           }
       });

        td.show();
    }

    public String getBookingTrans(String box){
        String ac = null;
        SQLiteDatabase db = gen.getReadableDatabase();
        Cursor x = db.rawQuery(" SELECT * FROM "+gen.tbname_booking_consignee_box
                +" LEFT JOIN "+gen.tbname_booking+" ON "+gen.tbname_booking_consignee_box
                +"."+gen.book_con_transaction_no+" = "+gen.tbname_booking+"."+gen.book_transaction_no
                +" WHERE "+gen.tbname_booking_consignee_box+"."+gen.book_con_box_number+" = '"+box+"'", null);
        if (x.moveToNext()){
            ac = x.getString(x.getColumnIndex(gen.book_transaction_no));
        }
        return ac;
    }

    public String getReceiver(String book, String bnum){
        String ac = null;
        SQLiteDatabase db = gen.getReadableDatabase();
        Cursor x = db.rawQuery(" SELECT * FROM "+gen.tbname_booking_consignee_box
                +" WHERE "+gen.book_con_transaction_no+" = '"+book+"' AND "+gen.book_con_box_number+" = '"+bnum+"'", null);
        if (x.moveToNext()){
            ac = x.getString(x.getColumnIndex(gen.book_con_box_account_no));
        }
        return ac;
    }

    public String getOrigin(String book, String bnum){
        String ac = null;
        SQLiteDatabase db = gen.getReadableDatabase();
        Cursor x = db.rawQuery(" SELECT * FROM "+gen.tbname_booking_consignee_box
                +" WHERE "+gen.book_con_transaction_no+" = '"+book+"' AND "+gen.book_con_box_number+" = '"+bnum+"'", null);
        if (x.moveToNext()){
            ac = x.getString(x.getColumnIndex(gen.book_con_origin));
        }
        return ac;
    }

    public String getDest(String book, String bnum){
        String ac = null;
        SQLiteDatabase db = gen.getReadableDatabase();
        Cursor x = db.rawQuery(" SELECT * FROM "+gen.tbname_booking_consignee_box
                +" WHERE "+gen.book_con_transaction_no+" = '"+book+"' AND "+gen.book_con_box_number+" = '"+bnum+"'", null);
        if (x.moveToNext()){
            ac = x.getString(x.getColumnIndex(gen.book_con_destination));
        }
        return ac;
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

    public boolean checkFromLoads(String bn){
        SQLiteDatabase db = rate.getReadableDatabase();
        Cursor x = db.rawQuery(" SELECT * FROM "+rate.tb_loadbox
        +" WHERE "+rate.load_boxnum+" = '"+bn+"' AND "+rate.load_box_stat+" = '2'", null);
        if (x.getCount() != 0){
            return true;
        }else {
            return false;
        }
    }

    public boolean checkIfExist(String barcode){
        SQLiteDatabase db = gen.getReadableDatabase();
        String q = " SELECT * FROM "+gen.tb_unloadbox
                +" WHERE "+gen.unload_boxnum+" = '"+barcode+"' AND "+gen.unload_box_stat+" = '2'";
        Cursor xc = db.rawQuery(q, null);
        if (xc.getCount() == 0 ){
            return true;
        }
        return false;
    }

    public boolean checkIfExistinOne(String barcode){
        SQLiteDatabase db = gen.getReadableDatabase();
        String q = " SELECT * FROM "+gen.tb_unloadbox
                +" WHERE "+gen.unload_boxnum+" = '"+barcode+"' AND "
                +gen.unload_box_stat+" = '1'";
        Cursor xc = db.rawQuery(q, null);
        if (xc.getCount() == 0 ){
            return true;
        }
        return false;
    }

    public void checkFieldsAndSave(){
        try{
            String ldate = loaddate.getText().toString();
            String shname = forward.getText().toString();
            String cont = con.getText().toString();
            String tstart = timestart.getText().toString();
            String plate = plate_no.getText().toString();
            String driver = drivername.getText().toString();
            String tend = timeend.getText().toString();
            String etafield = eta.getText().toString();

            if ((ldate.equals("")) || (shname.equals("")) || (cont.equals("")) || (etafield.equals(""))){
                String fields = "Form fields are missing, kindly fill it up correctly. Thank you.";
                customToast(fields);
            }
            else if (boxes.size() == 0){
                String fields = "Please scan a barcode for unloading, thank you.";
                customToast(fields);
            }
            else{
                for (String bn : boxes){
                    for (byte[] im : imagesperbox) {
                        rate.addNewUnloadingImage(loadhome.getUnloadtrans(), bn, im);
                    }
                    gen.addUnload(loadhome.getUnloadtrans(), bn, "2");
                    saveInventory(bn);
                }
                gen.addFinalUnload(loadhome.getUnloadtrans(), ldate, shname,
                        cont, tstart, tend, plate, driver, etafield, helper.logcount()+"", "1", "1");

                String inctype = "Unloading";

                gen.addTransactions(inctype, "" + helper.logcount(),
                        "New unloading with transaction number "
                                + loadhome.getUnloadtrans(), datereturn(), returntime());

                loadhome.setUnloadtrans(null);
                loadhome.setUnloadtrans(generateTrans());
                dum.requestFocus();
                unload();
                loadingPost(getView());
            }
        }catch (Exception e){
            Log.e("saving_trans", e.getMessage());
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

    public void textChanges(){
        try{
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
            addit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addnewItem();
                }
            });
        }catch (Exception e){}
    }

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

    public String returnHourMin(){
        Date datetalaga = Calendar.getInstance().getTime();
        SimpleDateFormat writeDate = new SimpleDateFormat("hh:mm");
        writeDate.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        String time = writeDate.format(datetalaga);

        return time;
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
                                        startActivity(new Intent(getContext(), Unloadinglist.class));
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

    public void updateBnum(String trans,String bn, String stat){
        SQLiteDatabase db = gen.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(gen.unloadbox_trans, trans);
        cv.put(gen.unload_boxnum, bn);
        cv.put(gen.unload_box_stat, stat);
        db.update(gen.tb_unloadbox, cv, gen.unload_boxnum+" = '"+bn+"'", null);
        Log.e("updateunload", bn);
        db.close();
    }

    public void updateBnumFinal(String bn, String stat){
        SQLiteDatabase db = gen.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(gen.unload_boxnum, bn);
        cv.put(gen.unload_box_stat, stat);
        db.update(gen.tb_unloadbox, cv, gen.unload_boxnum+" = '"+bn+"'", null);
        Log.e("updateunload", bn);
        db.close();
    }

    public String getBranchName(){
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

    public void displayTimePicker(){
        calendar = Calendar.getInstance();
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        int currentMin = calendar.get(Calendar.MINUTE);
        SimpleDateFormat writeDate = new SimpleDateFormat("hh:mm");
        timePickerDialog = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hourOfDay, int minutes) {
                String finalhour = null;
                String finalmin = null;
                if (hourOfDay < 10 ){
                    finalhour = "0"+hourOfDay+"";
                }else{
                    finalhour = hourOfDay+"";
                }
                if (minutes < 10){
                    finalmin = "0"+minutes;
                }else{
                    finalmin = minutes+"";
                }
                timeend.setText(finalhour+":"+finalmin);
            }
        },currentHour , currentMin, false);
        timePickerDialog.show();
    }

}
