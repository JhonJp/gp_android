package com.example.admin.gpxbymodule;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
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
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class Deliverycont extends AppCompatActivity {

    NavigationView navigationView;
    GenDatabase gen;
    HomeDatabase helper;
    RatesDB rate;
    String value, account, booknum, substatid;
    TextView sendername, receivername, recadd;
    Bundle bundle;
    ListView lvbox;
    String transid;
    Spinner substat;
    int camera_request = 1;
    GridView grimg;
    FloatingActionButton capt;
    String del_stat, del_rem, del_rating;
    TextView rateval;
    ImageView sig;
    EditText receivedby;
    Spinner relationship;
    String relationshipstring,recby;
    TextView defaultdelivered;
    private int SETTINGS_ACTION = 100;

    //signature variables
    LinearLayout mContent;
    Deliverycont.signature mSignature;
    Button mClear, mGetSign, mCancel;
    public static String tempDir;
    public String current = null;
    private Bitmap mBitmap;
    View mView;
    File mypath;
    Dialog dialog;
    byte[] off;
    TextView hint,signhint;
    private String uniqueId;
    FrameLayout framsign;
    String ig;

    //image
    ArrayList<HomeList> stored_image;
    ArrayList<byte[]> capt_images;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deliverycont);
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        this.setTitle("Delivery Info.");
        gen = new GenDatabase(this);
        helper = new HomeDatabase(this);
        rate = new RatesDB(this);
        bundle = getIntent().getExtras();
        booknum = bundle.getString("bookingnumber");
        account = bundle.getString("accountnumber");

        sendername = (TextView)findViewById(R.id.sendname);
        receivername = (TextView)findViewById(R.id.rectext);
        grimg = (GridView) findViewById(R.id.grid);
        hint = (TextView)findViewById(R.id.imageshint);
        recadd = (TextView)findViewById(R.id.recaddtext);
        signhint = (TextView)findViewById(R.id.sig_indicate);
        framsign = (FrameLayout)findViewById(R.id.sign);
        lvbox = (ListView)findViewById(R.id.lv);
        capt = (FloatingActionButton)findViewById(R.id.camera);
        sig = (ImageView)findViewById(R.id.signat);
        stored_image = new ArrayList<>();
        capt_images = new ArrayList<>();

        if (getTransid() == null) {
            setTransid(generateTransNo());
        } else {
            setTransid(getTransid());
        }

        ArrayList<HomeList> listitem = getDeliveryImages(getTransid());
        if (listitem.size() <= 3 ){
            capt.setVisibility(View.VISIBLE);
            capt.setClickable(true);
        }else{
            capt.setVisibility(View.INVISIBLE);
            capt.setClickable(false);
        }

        //get bundle data
        if (bundle.getString("status").equals("1")) {
            capt.setVisibility(View.INVISIBLE);
            viewgrid(getTrans(booknum));
            byte[] signature = getSignatre(booknum);
            Log.e("sign", signature+"");
            Bitmap bm = BitmapFactory.decodeByteArray(signature,0,signature.length);
            sig.setImageBitmap(bm);
        }else{
            viewgrid();
            signhint.setVisibility(View.INVISIBLE);
            framsign.setVisibility(View.INVISIBLE);
        }

        receivername.setText(receivername(account));
        recadd.setText(fulladd(account));
        sendername.setText(sendername(getSenderAccountNum(booknum)));
        boxesmo();
        receivername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewInfo();
            }
        });
        capt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                camera_capture();

            }
        });
        scroll();

    }

    public void boxesmo(){
        try {
            ArrayList<ListItem> boxes = getBoxes(account, booknum);
            TableAdapter a = new TableAdapter(getApplicationContext(), boxes);
            lvbox.setAdapter(a);
        }catch (Exception e){}
    }

    public void scroll(){
        try{
            lvbox.setOnTouchListener(new View.OnTouchListener() {
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

    public void camera_capture() {
        try {
            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(cameraIntent, camera_request);
        } catch (Exception e) {
            ActivityCompat.requestPermissions(Deliverycont.this,
                    new String[]{Manifest.permission.CAMERA}, camera_request);
        }
    }

    public void viewgrid(){
        try {
            final ArrayList<HomeList> listitem = stored_image;
            ImageAdapter myAdapter = new ImageAdapter(getApplicationContext(), listitem);
            grimg.setAdapter(myAdapter);
            if (capt_images.size() > 0) {
                hint.setVisibility(View.INVISIBLE);
            } else {
                hint.setVisibility(View.VISIBLE);
            }
            grimg.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    byte[] getitem = listitem.get(position).getTopitem();
                    alertImage(getitem, position,"0");
                }
            });
        }catch (Exception e){}
    }

    public void viewgrid(String transid){
        try {
            final ArrayList<HomeList> listitem = getDeliveryImages(transid);
            ImageAdapter myAdapter = new ImageAdapter(this, listitem);
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

                    alertImage(getitem, position, "1");
                }
            });
        }catch (Exception e){}
    }

    public void alertImage(final byte[] image, final int idt, String stat){
        try {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(Deliverycont.this);
            LayoutInflater inflater = this.getLayoutInflater();
            View d = inflater.inflate(R.layout.imagefullview, null);
            Button del = (Button) d.findViewById(R.id.delete);
            Button cancel = (Button) d.findViewById(R.id.cancel);
            final ImageView img = (ImageView) d.findViewById(R.id.imagefull);

            Bitmap bm = BitmapFactory.decodeByteArray(image, 0, image.length);
            img.setImageBitmap(bm);

            dialogBuilder.setView(d);
            final AlertDialog alertDialog = dialogBuilder.show();
            if (stat.equals("1")){
                del.setClickable(false);
                del.setFocusable(false);
                del.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                del.setTextColor(getResources().getColor(R.color.textcolor));
            }else{
                del.setClickable(true);
                del.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        capt_images.remove(image);
                        stored_image.remove(idt);
                        viewgrid();
                        alertDialog.dismiss();
                    }
                });
            }

            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.dismiss();
                }
            });
        }catch (Exception e){}
    }

    public ArrayList<HomeList> getDeliveryImages(String transno) {
        ArrayList<HomeList> results = new ArrayList<HomeList>();
        SQLiteDatabase db = rate.getReadableDatabase();

        Cursor res = db.rawQuery(" SELECT * FROM " + rate.tbname_reserve_image + " WHERE "
                + rate.res_img_trans + " = '" + transno + "'", null);
        res.moveToFirst();
        while (!res.isAfterLast()) {
            byte[] topitem = res.getBlob(res.getColumnIndex(rate.res_img_image));
            String ids = res.getString(res.getColumnIndex(rate.res_img_id));
            HomeList list = new HomeList(topitem, ids);
            results.add(list);
            res.moveToNext();
        }
        res.close();
        // Add some more dummy data for testing
        return results;
    }

    @Override
    public void onBackPressed(){
        if (bundle.getString("status").equals("1")) {
            startActivity(new Intent(getApplicationContext(), Partner_driverpage.class));
            finish();
        }else {
            final AlertDialog.Builder builder = new AlertDialog.Builder(Deliverycont.this);
            builder.setTitle("Cancel transaction?");
            builder.setMessage("All changes will not be saved, please confirm to cancel the transaction.");
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    startActivity(new Intent(getApplicationContext(), Partner_driverpage.class));
                    finish();
                }
            }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });
            // Create the AlertDialog object and show it
            builder.create().show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.deliverycontent, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.actiondelivered) {
            if(!(bundle.getString("status").equals("1"))){
                if (capt_images.size() == 0){
                    String y = "Picture is required, please add atleast one (1).";
                    customToast(y);
                }else{
                    viewRatings();
                }
            }else {
                final ArrayList<HomeList> listitems = getDeliveryImages(getTransid());
                if ((listitems.size() == 0)) {
                    String y = "Picture is required, please add atleast one (1).";
                    customToast(y);
                } else {
                    viewRatings();
                }
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (bundle.getString("status").equals("1")) {
            menu.findItem(R.id.actiondelivered).setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    public String receivername(String fulln){
        String fullname = null;
        SQLiteDatabase db = gen.getReadableDatabase();
        Cursor v = db.rawQuery(" SELECT * FROM " + gen.tbname_customers +
                " WHERE "+gen.cust_accountnumber+ " = '"+fulln+"'", null);
        if(v.moveToNext()) {
            fullname = v.getString(v.getColumnIndex(gen.cust_fullname));
        }
        return fullname;
    }

    public String fulladd(String a){
        String fullname = null;
        SQLiteDatabase db = gen.getReadableDatabase();
        Cursor v = db.rawQuery(" SELECT * FROM " + gen.tbname_customers +
                " WHERE "+gen.cust_accountnumber+ " = '"+a+"'", null);
        if(v.moveToNext()) {
            fullname = ""+ getBrgy(v.getString(v.getColumnIndex(gen.cust_barangay))) + " "
                    + getCity(v.getString(v.getColumnIndex(gen.cust_city))) + " "
                    + getProvince(v.getString(v.getColumnIndex(gen.cust_prov))) +" "
                    + v.getString(v.getColumnIndex(gen.cust_postal));
            Log.e("full_add", a);
        }
        return fullname;
    }

    public String sendername(String a){
        String fullname = null;
        SQLiteDatabase db = gen.getReadableDatabase();
        Cursor v = db.rawQuery(" SELECT * FROM " + gen.tbname_customers +
                " WHERE "+gen.cust_accountnumber+ " = '"+a+"'", null);
        if(v.moveToNext()) {
            fullname = v.getString(v.getColumnIndex(gen.cust_fullname));
        }
        return fullname;
    }

    public String getSenderAccountNum(String a){
        String fullname = "";
        SQLiteDatabase db = gen.getReadableDatabase();
        Cursor v = db.rawQuery(" SELECT * FROM " + gen.tbname_booking +
                " WHERE "+gen.book_transaction_no+ " = '"+a+"'", null);
        if(v.moveToNext()) {
            fullname = v.getString(v.getColumnIndex(gen.book_customer));
        }
        return fullname;
    }

    public ArrayList<ListItem> getBoxes(String acc, String trans){
        ArrayList<ListItem> items = new ArrayList<>();
        SQLiteDatabase db = gen.getReadableDatabase();
        Cursor x = db.rawQuery(" SELECT * FROM "+gen.tbname_booking_consignee_box
                +" WHERE "+gen.book_con_box_account_no+" = '"+acc+"' AND "+gen.book_con_transaction_no
                +" = '"+trans+"'", null);
        x.moveToFirst();
        while(!x.isAfterLast()){
            String boxid = x.getString(x.getColumnIndex(gen.book_con_box_id));
            String boxnum = x.getString(x.getColumnIndex(gen.book_con_box_number));
            ListItem item = new ListItem(boxid, "1 box", boxnum, null);
            items.add(item);
            x.moveToNext();
        }
        x.close();
        return items;
    }

    public ArrayList<String> getYourBoxes(String acc, String trans){
        ArrayList<String> items = new ArrayList<>();
        SQLiteDatabase db = gen.getReadableDatabase();
        Cursor x = db.rawQuery(" SELECT * FROM "+gen.tbname_booking_consignee_box
                +" WHERE "+gen.book_con_box_account_no+" = '"+acc+"' AND "+gen.book_con_transaction_no
                +" = '"+trans+"'", null);
        x.moveToFirst();
        while(!x.isAfterLast()){
            String boxnum = x.getString(x.getColumnIndex(gen.book_con_box_number));
            items.add(boxnum);
            x.moveToNext();
        }
        x.close();
        return items;
    }

    public void viewRemarks(){
        final AlertDialog.Builder denom = new AlertDialog.Builder(Deliverycont.this);
        LayoutInflater inflater = getLayoutInflater();
        final View d = inflater.inflate(R.layout.deliverycontent_extend, null);
        denom.setView(d);
        final Spinner stat = (Spinner)d.findViewById(R.id.statuses);
        substat = (Spinner)d.findViewById(R.id.substatuses);
        final EditText remarks = (EditText)d.findViewById(R.id.reasoninput);
        Button canc = (Button)d.findViewById(R.id.cancels);
        Button conf = (Button)d.findViewById(R.id.confirm);
        final AlertDialog dial = denom.show();
        denom.setTitle("Remarks");
        final String[] statusnames = getAllStatus();
        ArrayAdapter<String> adapt = new ArrayAdapter<String>
                (getApplicationContext(), R.layout.spinneritem, statusnames);
        stat.setAdapter(adapt);
        stat.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ig = stat.getSelectedItem().toString();
                del_stat = returnStatID(ig);
                Log.e("status_id", del_stat);
                subStat(del_stat, d);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                ig = stat.getSelectedItem().toString();
                del_stat = returnStatID(ig);
                Log.e("status_id", del_stat);
                subStat(del_stat, d);
            }
        });

        canc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dial.dismiss();
            }
        });
        conf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (stat.getSelectedItem().toString().equals("Undelivered")){
                    if (remarks.getText().toString().equals("")){
                        String h = "Reason or Notes is required.";
                        customToast(h);
                    }else{
                        del_rem = remarks.getText().toString();
                        dial.dismiss();
                        saveFinal();
                    }
                }else {
                    del_rem = remarks.getText().toString();
                    dial.dismiss();
                    saveFinal();
                }
            }
        });
    }

    public void subStat(String i, View v){
        try {
            if(i.equals("2")){
                defaultdelivered = (TextView) v.findViewById(R.id.def);
                defaultdelivered.setVisibility(View.INVISIBLE);
                defaultdelivered.setEnabled(false);
                defaultdelivered.setClickable(false);
                substat.setEnabled(true);
                final String[] substatusnames = getAllSubStat();
                ArrayAdapter<String> subadapt = new ArrayAdapter<String>
                        (getApplicationContext(), R.layout.spinneritem, substatusnames);
                substat.setAdapter(subadapt);
                substat.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        substatid = returnSUBStatID(substat.getSelectedItem().toString());
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        substatid = returnSUBStatID(substat.getSelectedItem().toString());
                    }
                });
            } else if(i.equals("1")) {
                substat.setEnabled(false);
                final String[] substatusnames = {"Delivered"};
                ArrayAdapter<String> subadapt = new ArrayAdapter<String>
                        (getApplicationContext(), android.R.layout.select_dialog_item, substatusnames);
                substat.setAdapter(subadapt);
                defaultdelivered = (TextView) v.findViewById(R.id.def);
                defaultdelivered.setVisibility(View.INVISIBLE);
                defaultdelivered.setEnabled(false);
                defaultdelivered.setClickable(false);
                del_stat = "1";
                substatid = "0";
                del_rem = "Delivered";
            }
        }catch (Exception e){}
    }

    public void viewRatings(){
        final AlertDialog.Builder denom = new AlertDialog.Builder(Deliverycont.this);
        LayoutInflater inflater = getLayoutInflater();
        View d = inflater.inflate(R.layout.delivery_ratings, null);
        denom.setView(d);
        RatingBar rate = (RatingBar) d.findViewById(R.id.rating);
        rateval = (TextView) d.findViewById(R.id.ratevalue);
        receivedby = (EditText) d.findViewById(R.id.receivedbyinput);
        relationship = (Spinner) d.findViewById(R.id.relation);
        Button canc = (Button)d.findViewById(R.id.cancels);
        Button conf = (Button)d.findViewById(R.id.confirm);
        denom.setTitle("Receiver information");
        final AlertDialog dial = denom.show();
        final String[] rel = {"Receiver","Father","Mother","Brother","Sister","Friend","Neighbor"};
        final ArrayAdapter<String> rela = new ArrayAdapter<String>
                (getApplicationContext(), R.layout.spinneritem, rel);
        relationship.setAdapter(rela);
        relationship.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String c = rela.getItem(position);
                if (c.equals("Receiver")){
                    receivedby.setText(receivername.getText().toString());
                }else{
                    receivedby.setText("");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        rate.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                String vrate = String.valueOf(rating);
                switch (vrate){
                    case "1.0":
                        rateval.setText("Not satisfied");
                        break;
                    case "2.0":
                        rateval.setText("Poor satisfied");
                        break;
                    case "3.0":
                        rateval.setText("Satisfied enough");
                        break;
                    case "4.0":
                        rateval.setText("A little satified");
                        break;
                    case "5.0":
                        rateval.setText("Very satisfied");
                        break;
                    default:
                        rateval.setText("Not satisfied");
                        break;

                }
            }
        });
        canc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dial.dismiss();
            }
        });
        conf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                relationshipstring = relationship.getSelectedItem().toString();
                recby = receivedby.getText().toString();
                dial.dismiss();
                dialog = new Dialog(Deliverycont.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.capture_sign);
                dialog.setCancelable(true);
                dialogsign();
            }
        });
    }

    //signature view
    // Function for Digital Signature
    public void dialogsign() {
        tempDir = Environment.getExternalStorageDirectory() + "/" + getResources().getString(R.string.app_name) + "/";
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File directory = cw.getDir(getResources().getString(R.string.app_name), Context.MODE_PRIVATE);

        prepareDirectory();
        uniqueId = getTodaysDate() + "_" + getCurrentTime() + "_" + Math.random();
        current = uniqueId + ".png";
        mypath= new File(directory,current);

        mContent = (LinearLayout)dialog.findViewById(R.id.linearLayout);
        mSignature = new Deliverycont.signature(getApplicationContext(), null);
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
                mSignature.clear();
                mGetSign.setEnabled(false);
            }
        });

        mGetSign.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                boolean error = captureSignature();
                if(!error){
                    mContent.setDrawingCacheEnabled(true);
                    mSignature.save(mView);
                    dialog.dismiss();
                    viewRemarks();
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

                mFileOutStream.flush();
                mFileOutStream.close();

                Log.e("log_tag","bytes: " + off);


            }
            catch(Exception e)
            {
                Log.v("log_tag", e.toString());
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

    public void updateInvBoxnumber(String boxnum){
        SQLiteDatabase db = gen.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(gen.partinv_stat, "2");
        db.update(gen.tbname_partner_inventory, cv,
                gen.partinv_boxnumber+" = '"+boxnum+"'", null);
        Log.e("updpartnerinventory", boxnum);
        db.close();
    }

    private boolean captureSignature() {

        boolean error = false;
        String errorMessage = "";

        if(error){
            String xy =  errorMessage;
            customToast(xy);
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

            return false;
        }
    }

    public String getTrans(String bookid){
        String trans = "";
        SQLiteDatabase db = gen.getReadableDatabase();
        Cursor x = db.rawQuery("SELECT * FROM "+gen.tbname_delivery
                +" WHERE "+gen.del_booking_no+" = '"+bookid+"'", null);
        if (x.moveToNext()){
            trans = x.getString(x.getColumnIndex(gen.del_id));
        }
        return trans;
    }

    private boolean makedirs() {
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

    public String datereturn(){
        Date datetalaga = Calendar.getInstance().getTime();
        SimpleDateFormat writeDate = new SimpleDateFormat("yyyy-MM-dd");
        writeDate.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        String findate = writeDate.format(datetalaga);
        return findate;
    }

    public String timereturn(){
        Date datetalaga = Calendar.getInstance().getTime();
        SimpleDateFormat writeDate = new SimpleDateFormat("hh:mm a");
        writeDate.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        String findate = writeDate.format(datetalaga);
        return findate;
    }

    public String generateTransNo(){
        String transNo = null;
        Date datetalaga = Calendar.getInstance().getTime();
        SimpleDateFormat writeDate = new SimpleDateFormat("yyyyMMddhhmmss");
        writeDate.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        String sa = writeDate.format(datetalaga);
        transNo = "GPD-" + helper.logcount() + sa;
        return transNo;
    }

    public String getTransid() {
        return transid;
    }

    public void setTransid(String transid) {
        this.transid = transid;
    }

    public String getCustomer(String book){
        String ac = null;
        SQLiteDatabase db = gen.getReadableDatabase();
        Cursor x = db.rawQuery(" SELECT * FROM "+gen.tbname_booking
                +" WHERE "+gen.book_transaction_no+" = '"+book+"'", null);
        if (x.moveToNext()){
            ac = x.getString(x.getColumnIndex(gen.book_customer));
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

    public String getProvince(String code){
        String name = "";
        SQLiteDatabase db = rate.getReadableDatabase();
        String q = " SELECT * FROM "+rate.tbname_provinces
                +" WHERE "+rate.prov_code+" = '"+code+"'";
        Cursor x = db.rawQuery(q, null);
        if (x.moveToNext()){
            name = x.getString(x.getColumnIndex(rate.prov_name));
            Log.e("prov_name", name);
        }
        return name;
    }

    public String getCity(String code){
        String name = "";
        SQLiteDatabase db = rate.getReadableDatabase();
        String q = " SELECT * FROM "+rate.tbname_city
                +" WHERE "+rate.ct_citycode+" = '"+code+"'";
        Cursor x = db.rawQuery(q, null);
        if (x.moveToNext()){
            name = x.getString(x.getColumnIndex(rate.ct_name));
            Log.e("cityname", name);
        }
        return name;
    }

    public String getBrgy(String code){
        String name = "";
        SQLiteDatabase db = rate.getReadableDatabase();
        String q = " SELECT * FROM "+rate.tbname_brgy
                +" WHERE "+rate.brgy_code+" = '"+code+"'";
        Cursor x = db.rawQuery(q, null);
        if (x.moveToNext()){
            name = x.getString(x.getColumnIndex(rate.brgy_name));
            Log.e("brgyname", name);
        }
        return name;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        try{
            if (requestCode == SETTINGS_ACTION) {
                if (resultCode == PartDriverPreference.RESULT_CODE_THEME_UPDATED) {
                    finish();
                    startActivity(getIntent());
                    return;
                }
            }
            else if (requestCode == camera_request){
                if (requestCode == camera_request && resultCode == Activity.RESULT_OK) {
                    Bitmap photo = (Bitmap) data.getExtras().get("data");
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    photo.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    byte[] bytimg = stream.toByteArray();
                    HomeList list = new HomeList(bytimg, capt_images.size()+"");
                    capt_images.add(bytimg);
                    stored_image.add(list);
                    viewgrid();
                    Log.e("camera", "success " + bytimg + " / " + getTransid());
                }
            }else
                super.onActivityResult(requestCode, resultCode, data);
        }catch (Exception e){}
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == camera_request) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(), "camera permission granted", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void customToast(String txt){
        Toast toast = new Toast(Deliverycont.this);
        toast.setDuration(Toast.LENGTH_SHORT);
        LayoutInflater inflater = Deliverycont.this.getLayoutInflater();
        View view = inflater.inflate(R.layout.toast, null);
        TextView t = (TextView)view.findViewById(R.id.toasttxt);
        t.setText(txt);
        toast.setView(view);
        toast.setGravity(Gravity.TOP | Gravity.RIGHT, 15, 50);
        Animation animation = AnimationUtils.loadAnimation(Deliverycont.this, R.anim.enterright);
        view.startAnimation(animation);
        toast.show();
    }

    public void deleteImage(String id) {
        SQLiteDatabase db = rate.getWritableDatabase();
        db.delete(rate.tbname_reserve_image, rate.res_img_id + " = " + id, null);
        db.close();
    }

    public String[] getAllStatus() {
        SQLiteDatabase db = rate.getReadableDatabase();
        ArrayList<String> numbers = new ArrayList<String>();
        Cursor c = db.rawQuery(" SELECT * FROM " + rate.tbname_delivery_status, null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            numbers.add(c.getString(c.getColumnIndex(rate.delstat_name)));
            c.moveToNext();
        }
        return numbers.toArray(new String[numbers.size()]);
    }

    public String[] getAllSubStat() {
        SQLiteDatabase db = rate.getReadableDatabase();
        ArrayList<String> numbers = new ArrayList<String>();
        Cursor c = db.rawQuery(" SELECT * FROM " + rate.tbname_delivery_substatus, null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            numbers.add(c.getString(c.getColumnIndex(rate.delsubstat_name)));
            c.moveToNext();
        }
        return numbers.toArray(new String[numbers.size()]);
    }

    public String returnStatID(String name){
        String ids = "";
        SQLiteDatabase db = rate.getReadableDatabase();
        String sx = " SELECT * FROM "+rate.tbname_delivery_status
                +" WHERE "+rate.delstat_name+" = '"+name+"'";
        Cursor c = db.rawQuery(sx, null);
        if(c.moveToNext()){
            ids = c.getString(c.getColumnIndex(rate.delstat_id));
        }
        c.close();
        return ids;
    }

    public String returnSUBStatID(String name){
        String ids = "";
        SQLiteDatabase db = rate.getReadableDatabase();
        String sx = " SELECT * FROM "+rate.tbname_delivery_substatus
                +" WHERE "+rate.delsubstat_name+" = '"+name+"'";
        Cursor c = db.rawQuery(sx, null);
        if(c.moveToNext()){
            ids = c.getString(c.getColumnIndex(rate.delsubstat_id));
        }
        c.close();
        return ids;
    }

    //finalize and save to db
    public void saveFinal(){
        String del_rating = rateval.getText().toString();
        gen.addDelivery(getTransid(), booknum, getCustomer(booknum),
                datereturn()+" "+timereturn(), off, helper.logcount()+"", "1", del_rating, del_rem, recby, relationshipstring, del_stat);
        gen.addTransactions("Delivery", helper.logcount()+"",
                "Add new delivery with number "+getTransid(), datereturn(), getCurrentTime());

        for (String i : getYourBoxes(account, booknum)) {
            gen.addDeliveryBox( i, booknum, getReceiver(booknum, i), getOrigin(booknum, i),
                    getDest(booknum, i), getTransid(), del_stat, substatid, datereturn()+" "+timereturn());
            //updateInvBoxnumber(i);
            rate.updateDirectBox(i);
        }

        for (byte[] img : capt_images){
            rate.addGenericImage("delivery", getTransid(), img);
            rate.addReserveImage(getTransid(), img);
        }

        startActivity(new Intent(getApplicationContext(), Partner_driverpage.class));
        finish();
    }

    public byte[] getSignatre(String bookid){
        byte[] i = null;
        String y = " SELECT "+gen.del_sign+" FROM "+gen.tbname_delivery+" WHERE "+gen.del_booking_no
                +" = '"+bookid+"'";
        SQLiteDatabase db = gen.getReadableDatabase();
        Cursor c = db.rawQuery(y, null);
        if (c.moveToNext()){
            i = c.getBlob(c.getColumnIndex(gen.del_sign));
        }
        c.close();
        db.close();
        return i;

    }

    public void viewInfo(){
        try {
            final AlertDialog.Builder thisview = new AlertDialog.Builder(Deliverycont.this);
            LayoutInflater inflater = getLayoutInflater();
            View d = inflater.inflate(R.layout.information, null);
            thisview.setView(d);
            TextView name = (TextView) d.findViewById(R.id.yourname);
            TextView mail = (TextView) d.findViewById(R.id.youremail);
            final TextView prim = (TextView) d.findViewById(R.id.yourprimenum);
            final TextView secd = (TextView) d.findViewById(R.id.yoursecnum);
            final TextView thrd = (TextView) d.findViewById(R.id.yourthirdnum);
            TextView fullad = (TextView) d.findViewById(R.id.yourfulladdress);

            name.setText(receivername(account));
            mail.setText(getmail(account));
            if (getPrimNum(account).equals(null)){
                prim.setText("");
            }else{
                prim.setText(getPrimNum(account));
            }
            if (getSecNum(account).equals(null)){
                secd.setText("");
            }else{
                secd.setText(getSecNum(account));
            }

            if (getOtherNum(account).equals(null)){
                thrd.setText("");
            }else{
                thrd.setText(getOtherNum(account));
            }

            prim.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                   viewcallMess("+"+prim.getText().toString());
                }
            });
            secd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    viewcallMess("+"+secd.getText().toString());
                }
            });
            thrd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    viewcallMess("+"+thrd.getText().toString());
                }
            });


            fullad.setText(fulladd(account));

            thisview.setPositiveButton("Close", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dx, int which) {
                    dx.dismiss();
                }
            });
            thisview.setTitle("Receiver Information");
            thisview.show();
        }catch (Exception e){
            Log.e("error", e.getMessage());
        }
    }

    public void viewcallMess(final String num){
        try {
            final AlertDialog.Builder thisview = new AlertDialog.Builder(Deliverycont.this);
            LayoutInflater inflater = getLayoutInflater();
            View d = inflater.inflate(R.layout.callmess, null);
            thisview.setView(d);
            ListView list = (ListView)d.findViewById(R.id.calllist);
            String[] choice = new String[]{"Call","Message"};
            final ArrayAdapter<String> adpt =
                    new ArrayAdapter<>(getApplicationContext(), R.layout.spinneritem,
                            choice);
            list.setAdapter(adpt);
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String clicked = adpt.getItem(position);
                    Log.e("clicked", clicked);
                    switch(clicked){
                        case "Call":
                            Intent intent = new Intent(Intent.ACTION_DIAL);
                            intent.setData(Uri.parse("tel:"+num));
                            startActivity(intent);
                            break;
                        case "Message":
                            Intent smsIntent = new Intent(Intent.ACTION_SENDTO);
                            smsIntent.addCategory(Intent.CATEGORY_DEFAULT);
                            smsIntent.setType("vnd.android-dir/mms-sms");
                            smsIntent.setData(Uri.parse("sms:" + num));
                            startActivity(smsIntent);
                            break;
                    }
                }
            });

            thisview.setTitle(num);
            thisview.show();
        }catch (Exception e){
            Log.e("error", e.getMessage());
        }
    }

    //get email address
    public String getmail(String a){
        String mail = null;
        SQLiteDatabase db = gen.getReadableDatabase();
        Cursor v = db.rawQuery(" SELECT * FROM " + gen.tbname_customers +
                " WHERE "+gen.cust_accountnumber+ " = '"+a+"'", null);
        if(v.moveToNext()) {
            mail = v.getString(v.getColumnIndex(gen.cust_emailadd));
        }
        return mail;
    }

    //get primary number
    public String getPrimNum(String a){
        String mail = null;
        SQLiteDatabase db = gen.getReadableDatabase();
        Cursor v = db.rawQuery(" SELECT * FROM " + gen.tbname_customers +
                " WHERE "+gen.cust_accountnumber+ " = '"+a+"'", null);
        if(v.moveToNext()) {
            mail = v.getString(v.getColumnIndex(gen.cust_mobile));
        }
        return mail;
    }

    //get sec num
    public String getSecNum(String a){
        String mail = null;
        SQLiteDatabase db = gen.getReadableDatabase();
        Cursor v = db.rawQuery(" SELECT * FROM " + gen.tbname_customers +
                " WHERE "+gen.cust_accountnumber+ " = '"+a+"'", null);
        if(v.moveToNext()) {
            mail = v.getString(v.getColumnIndex(gen.cust_secmobile));
        }
        return mail;
    }

    //get other num
    public String getOtherNum(String a){
        String mail = null;
        SQLiteDatabase db = gen.getReadableDatabase();
        Cursor v = db.rawQuery(" SELECT * FROM " + gen.tbname_customers +
                " WHERE "+gen.cust_accountnumber+ " = '"+a+"'", null);
        if(v.moveToNext()) {
            mail = v.getString(v.getColumnIndex(gen.cust_thirdmobile));
        }
        return mail;
    }

    //shared preference
    public void preference(){
        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(this);
        String themeName = pref.getString("theme", "Theme1");
        if (themeName.equals("Default(Red)")) {
            setTheme(R.style.AppTheme);
        } else if (themeName.equals("Light Blue")) {
            setTheme(R.style.customtheme);
        }else if (themeName.equals("Green")) {
            setTheme(R.style.customgreen);
        }
    }


}
