package com.example.admin.gpxbymodule;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.util.AttributeSet;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;

public class Booking_payment extends Fragment implements Runnable {

    FrameLayout capture;
    ListView lv;
    int gid, camera_request = 10, ACTIVITY_PRINT = 1;
    Camera camera;
    Booking book;
    IntentIntegrator scanIntegrator;
    ArrayList<ListItem> result;
    ImageButton addpay,discadd;
    GenDatabase gen;
    GridView grimg;
    HomeDatabase helper;
    RatesDB rate;
    View d;
    TextView sendername, depost, totalamt, hint, prate,discount;

    //signature variables
    LinearLayout mContent;
    Booking_payment.signature mSignature;
    Button mClear, mGetSign, mCancel;
    public static String tempDir;
    public String current = null;
    private Bitmap mBitmap;
    View mView;
    File mypath;
    Dialog dialog;
    byte[] off;
    private String uniqueId;
    double payhere,totaldiscount;
    byte[] data;
    ArrayList<String> receivers, boxnumbers;


    //bluetooth printer
    protected static final String TAG = "TAG";
    private static final int REQUEST_CONNECT_DEVICE = 100;
    private static final int REQUEST_ENABLE_BT = 20;
    Button mScan, mPrint, mDisc;

    public static final int MULTIPLE_PERMISSIONS = 10;
    BluetoothAdapter mBluetoothAdapter;
    private UUID applicationUUID = UUID
            .fromString("00001101-0000-1000-8000-00805F9B34FB");
    private ProgressDialog mBluetoothConnectProgressDialog;
    private BluetoothSocket mBluetoothSocket;
    BluetoothDevice mBluetoothDevice;

    public static byte[] SET_LINE_SPACING_30 = {0x1B, 0x33, 30};
    public static byte[] FEED_LINE = {10};
    OutputStream os;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.booking_payment, null);

        gen = new GenDatabase(getContext());
        helper = new HomeDatabase(getContext());
        rate = new RatesDB(getContext());

        sendername = (TextView)view.findViewById(R.id.sender);
        depost = (TextView)view.findViewById(R.id.depositamount);
        discount = (TextView)view.findViewById(R.id.discinput);
        totalamt = (TextView)view.findViewById(R.id.totalamount);
        book  = (Booking)getActivity();
        capture = (FrameLayout)view.findViewById(R.id.imagecapt);
        addpay = (ImageButton)view.findViewById(R.id.additional);
        discadd = (ImageButton)view.findViewById(R.id.disc_add);
        grimg = (GridView) view.findViewById(R.id.grid);
        hint = (TextView)view.findViewById(R.id.imageshint);
        prate = (TextView)view.findViewById(R.id.payrateinput);
        lv = (ListView)view.findViewById(R.id.lv);
        receivers = new ArrayList<>();
        boxnumbers = new ArrayList<>();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //initBluetooth();
        sendername.setText(book.getFullname());

        customtype();
        //for bluetooth printing(comment for now)
        //initBluetooth();
        try {

            lv.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    return false;
                }
            });
            viewsPayment();

            addpay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    viewaddpay();
                }
            });
            discadd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    viewDiscount();
                }
            });

            if (book.getAdditionalpay() == 0){
                book.setAdditionalpay(payhere);
            }else{
                book.setAdditionalpay(book.getAdditionalpay());
            }

            capture.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    camera_capture();
                }
            });
            viewgrid();
            Log.e("finalamount", book.getPayamount()+"");

            discount.setText(totaldiscount+"0");

        }catch (Exception e){}

        if (!checkDefItems()) {
            addDefaultItems();
        }
        getReceivers(book.getTransNo());
        return view;
    }

    public void viewsPayment(){
        if (book.getReserveno() != null) {
            if (getDeposit(book.getReserveno()) != null) {
                Double t = (book.getPayamount() - (Double.parseDouble(getDeposit(book.getReserveno()))));
                depost.setText("" + getDeposit(book.getReserveno()) + "0");
                if (t < 0) {
                    totalamt.setText("00.00");
                } else {
                    totalamt.setText("" + (t + book.getAdditionalpay())+"0");
                }
                prate.setText(book.getPayamount()+"0");
                totalamt.setText((book.getPayamount() + book.getAdditionalpay()) + "0");
            } else {
                depost.setText("0.00");
                double pa = book.getPayamount();
                prate.setText((pa) + "0");
                totalamt.setText((book.getPayamount() + book.getAdditionalpay()) + "0");
            }
        } else {
            depost.setText("0.00");
            prate.setText((book.getPayamount()) + "0");
            totalamt.setText((book.getPayamount() + book.getAdditionalpay()) + "0");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle("Booking");
        setHasOptionsMenu(true);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu){
        menu.findItem(R.id.savebooking).setVisible(true);
        menu.findItem(R.id.loadprev).setVisible(false);
        menu.findItem(R.id.btnnext).setVisible(false);
        menu.findItem(R.id.loadprevpay).setVisible(false);
        menu.findItem(R.id.btnnextpay).setVisible(false);
        super.onPrepareOptionsMenu(menu);
    }

    public void viewReceipt(){
        try {
            if (book.getClickcount() != result.size()) {
                String x = "Please make sure to re-add the receiver name if you are editing this transaction," +
                        " thank you.";
                loginerror(x);
            } else {
                final AlertDialog.Builder denom = new AlertDialog.Builder(getContext());
                LayoutInflater inflater = getLayoutInflater();
                d = inflater.inflate(R.layout.sample_receipt, null);
                denom.setView(d);
                d.setBackgroundColor(Color.WHITE);
                d.setDrawingCacheEnabled(true);
                d.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                d.layout(0, 0, d.getMeasuredWidth(), d.getMeasuredHeight());
                d.buildDrawingCache(true);
                final TextView txtdt = (TextView)d.findViewById(R.id.textdate);
                ListView lvs = (ListView) d.findViewById(R.id.lvsintopay);
                TextView booknum = (TextView)d.findViewById(R.id.booknumtxt);
                TextView bookhint = (TextView)d.findViewById(R.id.booknumhint);
                TextView driv = (TextView)d.findViewById(R.id.drivernameTxt);
                TextView sendr = (TextView)d.findViewById(R.id.sendertxt);
                TextView dep = (TextView)d.findViewById(R.id.depotxt);
                TextView rat = (TextView)d.findViewById(R.id.ratetxt);
                TextView addit = (TextView)d.findViewById(R.id.addittxt);
                TextView tot = (TextView)d.findViewById(R.id.totalpaytxt);
                final TextView disctext = (TextView)d.findViewById(R.id.disc_txt);
                try {
                    if (book.getTransNo() != null) {
                        result = gen.getAllBoxInTransaNo(book.getTransNo());
                    }
                    TableAdapter adapter = new TableAdapter(getContext(), result);
                    lvs.setAdapter(adapter);
                }catch (Exception e){}
                txtdt.setText(datereturn());
                driv.setText(helper.getFullname(helper.logcount()+""));
                booknum.setText(book.getTransNo());
                bookhint.setText("Booking number");
                sendr.setText(sendername.getText());
                dep.setText(depost.getText());
                rat.setText(prate.getText());
                addit.setText(book.getAdditionalpay()+"0");
                tot.setText(totalamt.getText());
                disctext.setText(totaldiscount+"0");
                denom.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dial, int which) {
                        //save file for temporary for printing
//                            tempDir = Environment.getExternalStorageDirectory() + "/" + getResources().getString(R.string.app_name) + "/";
//                            ContextWrapper cw = new ContextWrapper(getContext());
//                            File directory = cw.getDir(getResources().getString(R.string.app_name), Context.MODE_PRIVATE);
//                            Bitmap b = Bitmap.createBitmap(d.getDrawingCache());
//                            d.setDrawingCacheEnabled(false);
//                            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
//                            b.compress(Bitmap.CompressFormat.PNG, 100, bytes);
//                            uniqueId = getTodaysDate() + "_" + getCurrentTime() + "_" + Math.random();
//                            current = uniqueId + ".png";
//                            mypath= new File(directory,current);
//                            data = bytes.toByteArray();
//                            saveImage(d, b, current);

                        //end save file for printing
                        String datetime = txtdt.getText().toString();
                        String bnum = book.getTransNo();
                        String yname = sendername.getText().toString();
                        String depo = depost.getText().toString();
                        String cha = prate.getText().toString();
                        String tot = totalamt.getText().toString();
                        String driv = helper.getFullname(helper.logcount()+"");
                        String addon = book.getAdditionalpay()+"0";
                        String disc = disctext.getText().toString();

                        //for bluetooth printing (comment for now)
                        //bluetoothThread(printData(datetime, bnum, yname,receivers, boxnumbers,depo,disc,cha,addon,tot,driv));

                        dial.dismiss();
                        // Dialog Function
                        dialog = new Dialog(getContext());
                        // Removing the features of Normal Dialogs
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialog.setContentView(R.layout.capture_sign);
                        dialog.setCancelable(true);
                        dialogsign();
                    }
                });
                denom.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                denom.show();
            }
        }catch (Exception e){}
    }

    public void camera_capture() {
        try {
            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(cameraIntent, camera_request);
        } catch (Exception e) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.CAMERA}, camera_request);
        }
    }

    public void customtype(){
        try {
            if (book.getTransNo() != null) {
                result = gen.getAllBoxInTransaNo(book.getTransNo());
            }
            TableAdapter adapter = new TableAdapter(getContext(), result);
            lv.setAdapter(adapter);
        }catch (Exception e){}
    }

    public void loginerror(String x){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = this.getLayoutInflater();
        View d = inflater.inflate(R.layout.loginerror,null);
        Button save = (Button)d.findViewById(R.id.savelink);
        TextView t = (TextView)d.findViewById(R.id.textView4);
        t.setText(x);
        dialogBuilder.setView(d);
        final AlertDialog alertDialog = dialogBuilder.show();
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
    }

    public void addnewItemExpense(){
        final AlertDialog.Builder denom = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getLayoutInflater();
        View d = inflater.inflate(R.layout.additem,null);
        denom.setView(d);
        final EditText am = (EditText)d.findViewById(R.id.allowanceinput);
        denom.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String yourid = helper.logcount()+"";
                if (!am.getText().toString().equals("")) {
                    addItemsTodb(am.getText().toString());
                    dialog.dismiss();
                    viewaddpay();
                }
            }
        });
        denom.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        denom.setTitle("Add new item");
        denom.show();
    }

    public void viewaddpay(){
        final AlertDialog.Builder denom = new AlertDialog.Builder(getContext());
        final LayoutInflater inflater = getLayoutInflater();
        final View d = inflater.inflate(R.layout.additional_payment,null);
        denom.setView(d);
        final EditText am = (EditText)d.findViewById(R.id.allowanceinput);
        Spinner itempay = (Spinner)d.findViewById(R.id.addit_paymentspin);
        final ImageButton addi = (ImageButton)d.findViewById(R.id.additem);

        //populate items
        String[] sr = rate.getAllItemsZero();
        ArrayAdapter<String> sourceadapter =
                new ArrayAdapter<>(getContext(), R.layout.spinneritem,
                        sr);
        itempay.setAdapter(sourceadapter);
        sourceadapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        denom.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!am.getText().toString().equals("")) {
                    payhere += Double.valueOf(am.getText().toString());
                    Log.e("payment","Additional "+payhere);
                    book.setAdditionalpay(payhere);
                    viewsPayment();
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
        denom.setTitle("Add-ons");
        final AlertDialog td = denom.create();
        td.show();
        addi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addnewItemExpense();
                td.dismiss();
            }
        });
    }

    public void viewDiscount(){
        final AlertDialog.Builder disc = new AlertDialog.Builder(getContext());
        final LayoutInflater inflater = getLayoutInflater();
        final View d = inflater.inflate(R.layout.discount_view,null);
        disc.setView(d);
        final EditText am = (EditText)d.findViewById(R.id.disc_remarkinput);
        final EditText discount_amount = (EditText)d.findViewById(R.id.disc_disocunt_input);

        disc.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface d, int which) {
                String discam = discount_amount.getText().toString();
                String rem = am.getText().toString();
                double totalpay = Double.valueOf(totalamt.getText().toString());
                totaldiscount += Double.valueOf(discam);
                double d_disc = (Double.valueOf(discount.getText().toString()) + (Double.valueOf(discam)));
                double equal = ((totalpay)-(d_disc));
                gen.addDiscount(book.getTransNo(), discam, rem);
                totalamt.setText(equal+"0");
                discount.setText(totaldiscount+"0");
                d.dismiss();
            }
        });
        disc.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dx, int which) {
                dx.dismiss();
            }
        });
        disc.setTitle("Discount");
        final AlertDialog td = disc.create();
        td.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.savebooking){
            viewReceipt();
        }
        return super.onOptionsItemSelected(item);
    }

    public String getDeposit(String reservationum){
        String depo = null;
        SQLiteDatabase db = gen.getReadableDatabase();
        Cursor c = db.rawQuery(" SELECT * FROM "+gen.tbname_payment
        +" WHERE "+gen.pay_reservation_id+" = '"+reservationum+"'", null);
        if (c.moveToNext()){
            depo = c.getString(c.getColumnIndex(gen.pay_deposit));
        }
        return depo;
    }

    public void saveFinalBooking(){

        try {
            String transno = book.getTransNo();
            String reserveno = book.getReserveno();
            String custno = book.getAccntno();
            double finalamount = Double.valueOf(prate.getText().toString());
            String bookdate = datereturn();
            String bookstat = "1";
            String type = "1";
            String createdby = "" + helper.logcount();
            String stat = "1";
            if (custno.equals(null)) {
                String t ="There is no customer name";
                customToast(t);
            } else {
                if (reserveno.equals("")) {
                    if (book.getBoxnumbers().size() != 0 ){
                        for (String bn : book.getBoxnumbers()){
                            if (checkinInv(bn)){
                                gen.updateInvBoxnumber("1", bn, "2");
                            }else{
                                Log.e("bookids", book.getBoxids().toString());
                                for (String i : book.getBoxids()){
                                    gen.addtoDriverInv(i+"", bn, "1", "2");
                                    rate.updateBarDriverInv(bn,"1");
                                }
                            }
                        }
                    }
                    SQLiteDatabase db = gen.getWritableDatabase();
                    Cursor v = db.rawQuery(" SELECT * FROM "
                            + gen.tbname_booking + " WHERE "
                            + gen.book_transaction_no + " = '" + transno + "'", null);
                    if (v.getCount() != 0) {
                        v.moveToNext();
                        String id = v.getString(v.getColumnIndex(gen.book_id));

                        gen.deleteBooking(id);

                        gen.addBooking(transno, reserveno, custno,
                                bookdate, bookstat, type, createdby, "1");
                        updatePayment(reserveno, transno, finalamount+"");
                        updConsigneeBookingStat(transno, "2");
                        if (checkAmountTrans(transno)){
                            gen.updateRemAmount(transno, finalamount+"",
                                    helper.logcount()+"", "0");
                        }else {
                            gen.addRemitAmount(finalamount + "", transno, "0",
                                    helper.logcount() + "", dateOnly());
                        }

                        Log.e("wthoutres_amount", finalamount+"");

                        book.setAccntno(null);
                        book.setReserveno(null);
                        book.setFullname(null);
                        book.setTransNo(null);
                        book.setPayamount(0);
                    } else {
                        gen.addBooking(transno, reserveno, custno,
                                bookdate, bookstat, type, createdby, "1");
                        gen.addReservationPayment(null, transno, null, "Full",
                                null, finalamount+"", createdby, datereturn());
                        updConsigneeBookingStat(transno, "2");
                        if (checkAmountTrans(transno)){
                            gen.updateRemAmount(transno, finalamount+"",
                                    helper.logcount()+"", "0");
                        }else {
                            gen.addRemitAmount(finalamount + "", transno, "0",
                                    helper.logcount() + "", dateOnly());
                        }
                        Log.e("notinbook_finamount", finalamount+"");

                        book.setAccntno(null);
                        book.setReserveno(null);
                        book.setFullname(null);
                        book.setTransNo(null);
                        book.setPayamount(0);
                    }
                } else {
                    gen.updateReservationNumber(reserveno, "2");
                    if (book.getBoxnumbers().size() != 0 ){
                        for (String bn : book.getBoxnumbers()){
                            if (checkinInv(bn)){
                                gen.updateInvBoxnumber("1", bn, "2");
                            }else{
                                for (String i : book.getBoxids()){
                                    gen.addtoDriverInv(i+"", bn, "1", "2");
                                }
                            }
                        }
                    }
                    SQLiteDatabase db = gen.getWritableDatabase();
                    Cursor v = db.rawQuery(" SELECT * FROM " + gen.tbname_booking + " WHERE " + gen.book_transaction_no + " = '" + transno + "'", null);
                    if (v.getCount() != 0) {
                        v.moveToNext();
                        String id = v.getString(v.getColumnIndex(gen.book_id));
                        gen.deleteBooking(id);
                        updatePayment(reserveno, transno, finalamount+"");
                        gen.addBooking(transno, reserveno, custno,
                                bookdate, bookstat, type, createdby, "1");
                        updConsigneeBookingStat(transno, "2");
                        String typo = "Booking";
                        if (checkAmountTrans(transno)){
                            gen.updateRemAmount(transno, finalamount+"",
                                    helper.logcount()+"", "0");
                        }else {
                            gen.addRemitAmount(finalamount + "", transno, "0",
                                    helper.logcount() + "", dateOnly());
                        }
                        book.setAccntno(null);
                        book.setReserveno(null);
                        book.setFullname(null);
                        book.setTransNo(null);
                        book.setPayamount(0);
                    } else {
                        gen.addBooking(transno, reserveno, custno,
                                bookdate, bookstat, type, createdby, "1");
                        updConsigneeBookingStat(transno, "2");
                        updatePayment(reserveno, transno, finalamount+"");
                        String typo = "Booking";
                        gen.addTransactions(typo, "" + helper.logcount(),
                                "Added new booking, transaction number " + transno, datereturn(), timer());
                        if (checkAmountTrans(transno)){
                            gen.updateRemAmount(transno, finalamount+"",
                                    helper.logcount()+"", "0");
                        }else {
                            gen.addRemitAmount(finalamount + "", transno, "0",
                                    helper.logcount() + "", dateOnly());
                        }
                        book.setAccntno(null);
                        book.setReserveno(null);
                        book.setFullname(null);
                        book.setTransNo(null);
                        book.setPayamount(0);
                    }
                }
            }
            book.getClicksids().clear();
        }catch (Exception e){}
    }

    public void updConsigneeBookingStat(String trans, String stat) {
        SQLiteDatabase db = gen.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(gen.book_con_stat, stat);
        db.update(gen.tbname_booking_consignee_box, cv,
                gen.book_con_transaction_no + " = '" + trans+"'", null);
        db.close();
    }

    public String datereturn(){
        Date datetalaga = Calendar.getInstance().getTime();
        SimpleDateFormat writeDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        writeDate.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        String findate = writeDate.format(datetalaga);

        return findate;
    }

    public String dateOnly(){
        Date datetalaga = Calendar.getInstance().getTime();
        SimpleDateFormat writeDate = new SimpleDateFormat("yyyy-MM-dd");
        writeDate.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        String findate = writeDate.format(datetalaga);

        return findate;
    }

    public String timer(){
        Date datetalaga = Calendar.getInstance().getTime();
        SimpleDateFormat writeDate = new SimpleDateFormat("HH:mm:ss");
        writeDate.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        String findate = writeDate.format(datetalaga);

        return findate;
    }

    public String getId(String reserveno){
        String id = null;
        SQLiteDatabase db = gen.getReadableDatabase();
        Cursor c = db.rawQuery(" SELECT "+gen.reserve_id+" FROM "+gen.tbname_reservation+" WHERE "+gen.reserve_reservation_no
        +" = '"+reserveno+"'", null);
        if (c.moveToNext()){
            id = c.getString(c.getColumnIndex(gen.reserve_id));
        }
        return id;
    }

    public void updatePayment(String resnum, String bookid, String tamount){
        try {
            SQLiteDatabase db = gen.getWritableDatabase();
            Cursor y = db.rawQuery(" SELECT * FROM " + gen.tbname_payment
                    + " WHERE " + gen.pay_reservation_id + " = '" + resnum + "'", null);
            if (y.moveToNext()) {
                String or = y.getString(y.getColumnIndex(gen.pay_or_no));
                String term = "Full";
                String depo = y.getString(y.getColumnIndex(gen.pay_deposit));

                gen.updatePayment(resnum, or, bookid, term, depo, tamount, datereturn());

            }
            y.close();
            db.close();
        }catch (Exception e){}
    }

    //signature view
    // Function for Digital Signature
    public void dialogsign(){
        tempDir = Environment.getExternalStorageDirectory() + "/" + getResources().getString(R.string.app_name) + "/";
        ContextWrapper cw = new ContextWrapper(getContext());
        File directory = cw.getDir(getResources().getString(R.string.app_name), Context.MODE_PRIVATE);

        prepareDirectory();
        uniqueId = getTodaysDate() + "_" + getCurrentTime() + "_" + Math.random();
        current = uniqueId + ".png";
        mypath= new File(directory,current);


        mContent = (LinearLayout)dialog.findViewById(R.id.linearLayout);
        mSignature = new Booking_payment.signature(getContext(), null);
        mSignature.setBackgroundColor(Color.WHITE);
        mContent.addView(mSignature, ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
        mClear = (Button)dialog.findViewById(R.id.clearsign);
        mGetSign = (Button)dialog.findViewById(R.id.savesign);
        mGetSign.setEnabled(false);
        mCancel = (Button)dialog.findViewById(R.id.cancelsign);
        mView = mContent;

        mClear.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                Log.v("log_tag", "Panel Cleared");
                mSignature.clear();
                mGetSign.setEnabled(false);
            }
        });

        mGetSign.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                //Log.v("log_tag", "Panel Saved");
                boolean error = captureSignature();
                if(!error){
                    mContent.setDrawingCacheEnabled(true);
                    mSignature.save(mView);
                    //sms();
                    saveFinalBooking();
                    dialog.dismiss();
                    startActivity(new Intent(getContext(), Bookinglist.class));
                    getActivity().finish();
                }
            }
        });

        mCancel.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                Log.v("log_tag", "Panel Canceled");
                mSignature.clear();
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public class signature extends View
    {
        public static final float STROKE_WIDTH = 5f;
        public static final float HALF_STROKE_WIDTH = STROKE_WIDTH / 2;
        public Paint paint = new Paint();
        public Path path = new Path();

        public float lastTouchX;
        public float lastTouchY;
        public final RectF dirtyRect = new RectF();

        public signature(Context context, AttributeSet attrs)
        {
            super(context, attrs);
            paint.setAntiAlias(true);
            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeWidth(STROKE_WIDTH);
        }

        public void save(View v)
        {
            if(mBitmap == null)
            {
                mBitmap =  Bitmap.createBitmap (mContent.getWidth(), mContent.getHeight(), Bitmap.Config.RGB_565);;
            }
            Canvas canvas = new Canvas(mBitmap);
            try
            {
                ByteArrayOutputStream off_byte = new ByteArrayOutputStream();
                FileOutputStream mFileOutStream = new FileOutputStream(mypath);

                v.draw(canvas);
                mBitmap.compress(Bitmap.CompressFormat.PNG, 100, mFileOutStream);
                mBitmap.compress(Bitmap.CompressFormat.PNG, 100, off_byte);
                off = off_byte.toByteArray();
                rate.addBookSign(book.getTransNo(), off);
                //bytes.setText(off+"");

                mFileOutStream.flush();
                mFileOutStream.close();
                //String url = MediaStore.Images.Media.insertImage(ReservationData.this.getApplicationContext().getContentResolver(), mBitmap, "title", null);

            }
            catch(Exception e)
            {
                Log.e("log_tag", e.toString());
            }
        }

        public void clear()
        {
            path.reset();
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas)
        {
            canvas.drawPath(path, paint);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event)
        {
            float eventX = event.getX();
            float eventY = event.getY();
            mGetSign.setEnabled(true);

            switch (event.getAction())
            {
                case MotionEvent.ACTION_DOWN:
                    path.moveTo(eventX, eventY);
                    lastTouchX = eventX;
                    lastTouchY = eventY;
                    return true;

                case MotionEvent.ACTION_MOVE:

                case MotionEvent.ACTION_UP:

                    resetDirtyRect(eventX, eventY);
                    int historySize = event.getHistorySize();
                    for (int i = 0; i < historySize; i++)
                    {
                        float historicalX = event.getHistoricalX(i);
                        float historicalY = event.getHistoricalY(i);
                        expandDirtyRect(historicalX, historicalY);
                        path.lineTo(historicalX, historicalY);
                    }
                    path.lineTo(eventX, eventY);
                    break;

                default:
                    debug("Ignored touch event: " + event.toString());
                    return false;
            }

            invalidate((int) (dirtyRect.left - HALF_STROKE_WIDTH),
                    (int) (dirtyRect.top - HALF_STROKE_WIDTH),
                    (int) (dirtyRect.right + HALF_STROKE_WIDTH),
                    (int) (dirtyRect.bottom + HALF_STROKE_WIDTH));

            lastTouchX = eventX;
            lastTouchY = eventY;

            return true;
        }

        private void debug(String string){
        }

        private void expandDirtyRect(float historicalX, float historicalY)
        {
            if (historicalX < dirtyRect.left)
            {
                dirtyRect.left = historicalX;
            }
            else if (historicalX > dirtyRect.right)
            {
                dirtyRect.right = historicalX;
            }

            if (historicalY < dirtyRect.top)
            {
                dirtyRect.top = historicalY;
            }
            else if (historicalY > dirtyRect.bottom)
            {
                dirtyRect.bottom = historicalY;
            }
        }

        private void resetDirtyRect(float eventX, float eventY)
        {
            dirtyRect.left = Math.min(lastTouchX, eventX);
            dirtyRect.right = Math.max(lastTouchX, eventX);
            dirtyRect.top = Math.min(lastTouchY, eventY);
            dirtyRect.bottom = Math.max(lastTouchY, eventY);
        }

    }

    private boolean captureSignature() {

        boolean error = false;
        String errorMessage = "";

        if(error){
            Toast toast = Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.TOP, 105, 50);
            toast.show();
        }

        return error;
    }

    private String getTodaysDate() {

        final Calendar c = Calendar.getInstance();
        int todaysDate =     (c.get(Calendar.YEAR) * 10000) +
                ((c.get(Calendar.MONTH) + 1) * 100) +
                (c.get(Calendar.DAY_OF_MONTH));
        Log.w("DATE:",String.valueOf(todaysDate));
        return(String.valueOf(todaysDate));

    }

    private String getCurrentTime() {

        final Calendar c = Calendar.getInstance();
        int currentTime =     (c.get(Calendar.HOUR_OF_DAY) * 10000) +
                (c.get(Calendar.MINUTE) * 100) +
                (c.get(Calendar.SECOND));
        Log.w("TIME:",String.valueOf(currentTime));
        return(String.valueOf(currentTime));

    }

    private boolean prepareDirectory()
    {
        try
        {
            if (makedirs())
            {
                return true;
            } else {
                return false;
            }
        } catch (Exception e)
        {
            e.printStackTrace();
            Toast.makeText(getContext(), "Could not initiate File System.. Is Sdcard mounted properly?",
                    Toast.LENGTH_LONG).show();
            return false;
        }
    }

    private boolean makedirs()
    {
        File tempdir = new File(tempDir);
        if (!tempdir.exists())
            tempdir.mkdirs();

        if (tempdir.isDirectory())
        {
            File[] files = tempdir.listFiles();
            for (File file : files)
            {
                if (!file.delete())
                {
                    System.out.println("Failed to delete " + file);
                }
            }
        }
        return (tempdir.isDirectory());
    }

    public void sms(){
        Thread thread =  new Thread(new Runnable() {
            @Override
            public void run() {
                //THREAD FOR sms API
                try {
                    String resp = null;
                    String msg = "Thank you for choosing GP EXPRESS. Your transaction number is "+book.getTransNo()+"";
                    URL url = new URL("https://www.isms.com.my/isms_send.php?un=moimarksantos&pwd=Microbizone1&dstno="+getNumber(book.getAccntno())+"&msg="+msg+"&type=1&sendid=MICROBIZ&agreedterm=YES");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    // read the response
                    InputStream in = new BufferedInputStream(conn.getInputStream());
                    resp = convertStreamToString(in);
                    if (!resp.equals("2000 = SUCCESS")){
                        startActivity(new Intent(getContext(), Bookinglist.class));
                        getActivity().finish();
                    }else{
                        String x = "Save booking error, please try again.";
                        customToast(x);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            //END THREAD sms API
        });
        thread.start();
    }

    private String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return sb.toString();
    }

    public String getNumber(String cn){
        String i = "639998792184";
        SQLiteDatabase db = gen.getReadableDatabase();
        Cursor x = db.rawQuery(" SELECT * FROM "+gen.tbname_customers
        +" WHERE "+gen.cust_accountnumber+" = '"+cn+"'", null);
        if (x.moveToNext()){
            i = x.getString(x.getColumnIndex(gen.cust_mobile));
        }
        return i;
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == camera_request){
                if (requestCode == camera_request && resultCode == Activity.RESULT_OK) {
                    Bitmap photo = (Bitmap) data.getExtras().get("data");
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    photo.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    byte[] bytimg = stream.toByteArray();
                    if(rate.addReserveImage(book.getTransNo(), bytimg)){
                        String x = "Image has been saved.";
                        customToast(x);
                        viewgrid();
                    }else{
                        String x = "Image save failed.";
                        customToast(x);
                    }
                    Log.e("camera", "success " + bytimg + " / " + book.getTransNo());
                }
            }else if(requestCode == REQUEST_CONNECT_DEVICE){
                if (resultCode == Activity.RESULT_OK) {
                    Bundle mExtra = data.getExtras();
                    String mDeviceAddress = mExtra.getString("DeviceAddress");
                    Log.v(TAG, "Coming incoming address " + mDeviceAddress);
                    mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(mDeviceAddress);
                    mBluetoothConnectProgressDialog = ProgressDialog.show(getContext(),
                            "Connecting...", mBluetoothDevice.getName() + " : "
                                    + mBluetoothDevice.getAddress(), true, true);
                    Thread mBlutoothConnectThread = new Thread(this);
                    mBlutoothConnectThread.start();
                    // pairToDevice(mBluetoothDevice); This method is replaced by
                    // progress dialog with thread
                }
            }
            else if(requestCode == REQUEST_ENABLE_BT){
                if (resultCode == Activity.RESULT_OK) {
                    ListPairedDevices();
                    Intent connectIntent = new Intent(getContext(),
                            DeviceListActivity.class);
                    startActivityForResult(connectIntent, REQUEST_CONNECT_DEVICE);
                } else {
                    //Toast.makeText(MainActivity.this, "Message", Toast.LENGTH_SHORT).show();
                }
            }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                            int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == camera_request) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getContext(), "camera permission granted", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getContext(), "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void viewgrid(){
        try {
            final ArrayList<HomeList> listitem = rate.getBookingImages(book.getTransNo());
            ImageAdapter myAdapter = new ImageAdapter(getContext(), listitem);
            grimg.setAdapter(myAdapter);
            if (grimg.getAdapter().getCount() > 0) {
                hint.setVisibility(View.INVISIBLE);
            } else {
                hint.setVisibility(View.VISIBLE);
            }
            grimg.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    byte[] getitem = listitem.get(position).getTopitem();
                    String iditem = listitem.get(position).getSubitem();
                    alertImage(getitem, iditem);
                }
            });
        }catch (Exception e){}
    }

    public void alertImage(byte[] image, final String idt){
        try {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
            LayoutInflater inflater = this.getLayoutInflater();
            View d = inflater.inflate(R.layout.imagefullview, null);
            Button del = (Button) d.findViewById(R.id.delete);
            Button cancel = (Button) d.findViewById(R.id.cancel);
            final ImageView img = (ImageView) d.findViewById(R.id.imagefull);

            Bitmap bm = BitmapFactory.decodeByteArray(image, 0, image.length);
            img.setImageBitmap(bm);

            dialogBuilder.setView(d);
            final AlertDialog alertDialog = dialogBuilder.show();

            del.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    rate.deleteImageBooking(idt);
                    viewgrid();
                    alertDialog.dismiss();
                }
            });

            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.dismiss();
                }
            });
        }catch (Exception e){}
    }

    public boolean checkinInv(String bn){
        SQLiteDatabase db = gen.getReadableDatabase();
        String u = "SELECT * FROM "+gen.tbname_driver_inventory
                +" WHERE "+gen.sdinv_boxnumber+" = '"+bn+"'";
        Cursor x = db.rawQuery(u, null);
        if (x.getCount() != 0){
            return true;
        }else{
            return false;
        }

    }

    public void addItemsTodb(String name){
        SQLiteDatabase db = rate.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(rate.expit_name, name);
        cv.put(rate.expit_type, "1");
        db.insert(rate.tbname_exp_item, null, cv);
        db.close();
    }

    public void addDefaultItems(){
        ArrayList<String> itemnames = new ArrayList<>();
        itemnames.add("Crating");
        itemnames.add("Packaging");
        itemnames.add("Extended");
        itemnames.add("Others");
        for(String name:itemnames){
            addItemsTodb(name);
        }
    }

    //check default items
    public boolean checkDefItems(){
        SQLiteDatabase db = rate.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM "+rate.tbname_exp_item, null);
        if (c.getCount() != 0){
            return true;
        }else{
            return false;
        }
    }

    public boolean checkAmountTrans(String trans){
        SQLiteDatabase db = gen.getReadableDatabase();
        String u = " SELECT * FROM "+gen.tbname_remitttances_amount
                +" WHERE "+gen.rem_amount_transnum+" = '"+trans+"' AND "+gen.rem_amount_stat+" = '0'";
        Cursor x = db.rawQuery(u, null);
        if (x.getCount() != 0){
            return true;
        }else{
            return false;
        }
    }

    private void saveImage(View v, Bitmap bitmapImage, String name){
        Canvas canvas = new Canvas(bitmapImage);
        try
        {
            FileOutputStream mFileOutStream = new FileOutputStream(mypath);

            v.draw(canvas);
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, mFileOutStream);

            mFileOutStream.flush();
            mFileOutStream.close();
            MediaStore.Images.Media.insertImage(getActivity().getApplicationContext().getContentResolver(),
                    bitmapImage, "title", null);

        }
        catch(Exception e)
        {
            Log.e("log_tag", e.toString());
        }
    }

    public void initBluetooth(){
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(getContext(), "Bluetooth print", Toast.LENGTH_SHORT).show();
        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(
                        BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent,
                        REQUEST_ENABLE_BT);
            } else {
                ListPairedDevices();
                Intent connectIntent = new Intent(getContext(),
                        DeviceListActivity.class);
                startActivityForResult(connectIntent,
                        REQUEST_CONNECT_DEVICE);
            }
        }
    }

    private void ListPairedDevices() {
        Set<BluetoothDevice> mPairedDevices = mBluetoothAdapter
                .getBondedDevices();
        if (mPairedDevices.size() > 0) {
            for (BluetoothDevice mDevice : mPairedDevices) {
                Log.e(TAG, "PairedDevices: " + mDevice.getName() + "  "
                        + mDevice.getAddress());
            }
        }
    }

    public void run() {
        try {
            mBluetoothSocket = mBluetoothDevice
                    .createRfcommSocketToServiceRecord(applicationUUID);
            mBluetoothAdapter.cancelDiscovery();
            mBluetoothSocket.connect();
            mHandler.sendEmptyMessage(0);
        } catch (IOException eConnectException) {
            Log.e(TAG, "CouldNotConnectToSocket", eConnectException);
            closeSocket(mBluetoothSocket);
            return;
        }
    }

    private void closeSocket(BluetoothSocket nOpenSocket) {
        try {
            nOpenSocket.close();
            Log.e(TAG, "SocketClosed");
        } catch (IOException ex) {
            Log.e(TAG, "CouldNotCloseSocket");
        }
    }

    public Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            mBluetoothConnectProgressDialog.dismiss();
            Toast.makeText(getContext(), "Device connected", Toast.LENGTH_SHORT).show();
        }
    };

    public static byte intToByteArray(int value) {
        byte[] b = ByteBuffer.allocate(4).putInt(value).array();

        for (int k = 0; k < b.length; k++) {
            System.out.println("Selva  [" + k + "] = " + "0x"
                    + UnicodeFormatter.byteToHex(b[k]));
        }

        return b[3];
    }

    public byte[] sel(int val) {
        ByteBuffer buffer = ByteBuffer.allocate(2);
        buffer.putInt(val);
        buffer.flip();
        return buffer.array();
    }

    public void bluetoothThread(final String data){
        Thread t = new Thread() {
            public void run() {
                try {
                    os = mBluetoothSocket.getOutputStream();

                    writeWithFormat(data.getBytes(), new Formatter().get(), Formatter.centerAlign());
                    // Setting height
                    int gs = 250;
                    os.write(intToByteArray(gs));
                    int h = 250;
                    os.write(intToByteArray(h));
                    int n = 250;
                    os.write(intToByteArray(n));

                    // Setting Width
                    int gs_width = 50;
                    os.write(intToByteArray(gs_width));
                    int w = 119;
                    os.write(intToByteArray(w));
                    int n_width = 3;
                    os.write(intToByteArray(n_width));

                    os.write(FEED_LINE);
                    os.write(FEED_LINE);
                    os.write(FEED_LINE);
                    os.write(FEED_LINE);
                    os.write(FEED_LINE);
                    os.write(FEED_LINE);
                    os.write(FEED_LINE);
                    os.write(FEED_LINE);
                    os.write(FEED_LINE);
                    os.write(FEED_LINE);
                    os.write(FEED_LINE);
                    os.write(FEED_LINE);
                    os.write(FEED_LINE);
                    os.write(FEED_LINE);
                    os.write(FEED_LINE);

                } catch (Exception e) {
                    Log.e("print", e.getMessage());
                }
            }
        };
        t.start();
    }

    public String printData(String datetme, String booknum,String yname, ArrayList<String> rec,
                            ArrayList<String> numbers,String depo,String disc,
                            String charge, String addons,String total,String driver){

        String BILL="            XXXX GP EXPRESS CARGO XXXX  \n"
                + "                 "+datetme+"     \n " +
                "         This serves as your partial receipt    \n";
        BILL = BILL
                + "---------------------------------------------------------\n";
        BILL = BILL + "  Booking number :  "+ booknum + "\n";
        BILL = BILL + "  Your name :  "+ yname + "\n";
        BILL = BILL
                + "---------------------------------------------------------\n";
        BILL = BILL + String.format("%1$-10s %2$10s %3$13s %4$10s", "   #", "Receiver", "Boxnumber","");

        BILL = BILL + "\n";
        BILL = BILL
                + "---------------------------------------------------------";

        for (int i = 0;i < numbers.size();i++) {
            BILL = BILL + "\n   " + String.format("%1$-10s %2$10s %3$11s %4$10s", "" + i + "", "" + rec.get(i) + "", "" + numbers.get(i) + "", "");
        }
        BILL = BILL
                + "\n---------------------------------------------------------\n";
        BILL = BILL +"  "+ String.format("%1$-10s %2$10s %3$13s %4$10s", "Payment", "","","");
        BILL = BILL
                + "\n---------------------------------------------------------";
        BILL = BILL + "\n   " + String.format("%1$-10s %2$10s %3$11s %4$10s","Discount","",""+depo+"","");
        BILL = BILL + "\n   " + String.format("%1$-10s %2$10s %3$11s %4$10s","Deposit","",""+depo+"","");
        BILL = BILL + "\n   " + String.format("%1$-10s %2$10s %3$11s %4$10s","Charge","",""+charge+"","");
        BILL = BILL + "\n   " + String.format("%1$-10s %2$10s %3$11s %4$10s","Add-ons","",""+addons+"","");
        BILL = BILL
                + "\n---------------------------------------------------------";
        BILL = BILL + "\n   " + String.format("%1$-10s %2$10s %3$11s %4$10s","Total charge","",""+total+"","");
        BILL = BILL + "\n\n ";

        BILL = BILL
                + "--------------------------------------------------------\n";
        BILL = BILL + "\n   " + String.format("%1$-10s %2$10s %3$11s %4$10s","Driver name","",""+driver+"","");
        BILL = BILL +"\n\n            XXXX GP EXPRESS CARGO XXXX  \n";
        BILL = BILL + "\n\n";

        return BILL;
    }

    public void getReceivers(String trans){
        SQLiteDatabase db = gen.getReadableDatabase();
        Cursor res = db.rawQuery(" SELECT * FROM " + gen.tbname_booking_consignee_box + " WHERE "
                + gen.book_con_transaction_no + " = '" + trans + "'", null);
        res.moveToFirst();
        while (!res.isAfterLast()) {
            String acount = res.getString(res.getColumnIndex(gen.book_con_box_account_no));
            String ids = res.getString(res.getColumnIndex(gen.book_con_box_id));
            String sub = res.getString(res.getColumnIndex(gen.book_con_box_number));
            String a = res.getString(res.getColumnIndex(gen.book_con_boxtype));
            String topitem = "", temptop = "";
            Cursor getname = db.rawQuery("SELECT " + gen.cust_fullname + " FROM " + gen.tbname_customers
                    + " WHERE " + gen.cust_accountnumber + " = '" + acount + "'", null);
            if (getname.moveToNext()) {
                temptop = getname.getString(getname.getColumnIndex(gen.cust_fullname));
            }

            receivers.add(temptop);
            boxnumbers.add(sub);
            res.moveToNext();
        }
        res.close();
    }

    /**
     * Class for formatting
     */
    public static class Formatter {
        /** The format that is being build on */
        private byte[] mFormat;

        public Formatter() {
            // Default:
            mFormat = new byte[]{27, 33, 0};
        }

        /**
         * Method to get the Build result
         *
         * @return the format
         */
        public byte[] get() {
            return mFormat;
        }

        public Formatter bold() {
            // Apply bold:
            mFormat[2] = ((byte) (0x8 | mFormat[2]));
            return this;
        }

        public Formatter small() {
            mFormat[2] = ((byte) (0x1 | mFormat[2]));
            return this;
        }

        public Formatter height() {
            mFormat[2] = ((byte) (0x10 | mFormat[2]));
            return this;
        }

        public Formatter width() {
            mFormat[2] = ((byte) (0x20 | mFormat[2]));
            return this;
        }

        public Formatter underlined() {
            mFormat[2] = ((byte) (0x80 | mFormat[2]));
            return this;
        }

        public static byte[] rightAlign(){
            return new byte[]{0x1B, 'a', 0x02};
        }

        public static byte[] leftAlign(){
            return new byte[]{0x1B, 'a', 0x00};
        }

        public static byte[] centerAlign(){
            return new byte[]{0x1B, 'a', 0x01};
        }
    }

    public boolean writeWithFormat(byte[] buffer, final byte[] pFormat, final byte[] pAlignment) {
        try {
            // Notify printer it should be printed with given alignment:
            os.write(pAlignment);
            // Notify printer it should be printed in the given format:
            os.write(pFormat);
            // Write the actual data:
            os.write(buffer, 0, buffer.length);

            // Share the sent message back to the UI Activity
            //App.getInstance().getHandler().obtainMessage(MESSAGE_WRITE, buffer.length, -1, buffer).sendToTarget();
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Exception during write", e);
            return false;
        }
    }

}
