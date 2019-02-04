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
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.AttributeSet;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
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

    //signature variables
    LinearLayout mContent;
    Distributionlist.signature mSignature;
    Button mClear, mGetSign, mCancel;
    public static String tempDir;
    public String current = null;
    private Bitmap mBitmap;
    View mView;
    File mypath;
    Dialog dx;
    byte[] off;
    private String uniqueId;

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
                    if (getAccStat(ids) == 0){
                        close.setText("Confirm transaction");
                        close.setTextColor(getResources().getColor(R.color.textcolor));
                        close.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                        close.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                dx = new Dialog(getContext());
                                // Removing the features of Normal Dialogs
                                dx.requestWindowFeature(Window.FEATURE_NO_TITLE);
                                dx.setContentView(R.layout.capture_sign);
                                dx.setCancelable(true);
                                dialogsign(ids);
                                //gen.updateDistById(ids);
                                dialog.dismiss();
                            }
                        });
                    }else{
                        close.setText("Close");
                        close.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });
                    }
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
                    if (getAccStatPartner(ids) == 0){
                        close.setText("Confirm transaction");
                        close.setTextColor(getResources().getColor(R.color.textcolor));
                        close.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                        close.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                rate.updateDistById(ids);
                                dialog.dismiss();
                            }
                        });
                    }else{
                        close.setText("Close");
                        close.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });
                    }

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

    public int getAccStat(String trans){
        int x = 0;
        SQLiteDatabase db = gen.getReadableDatabase();
        String query = "SELECT * FROM "+gen.tbname_tempDist
                +" WHERE "+gen.temp_transactionnumber+" = '"+trans+"'";
        Cursor c = db.rawQuery(query, null);
        if (c.moveToNext()){
            int i  = c.getInt(c.getColumnIndex(gen.temp_acceptstat));
            if (i == 0){
                x = 0;
            }else{
                x = i;
            }
        }
        return x;
    }

    public int getAccStatPartner(String trans){
        int x = 0;
        SQLiteDatabase db = rate.getReadableDatabase();
        String query = "SELECT * FROM "+rate.tbname_part_distribution
                +" WHERE "+rate.partdist_transactionnumber+" = '"+trans+"'";
        Cursor c = db.rawQuery(query, null);
        if (c.moveToNext()){
            int i  = c.getInt(c.getColumnIndex(rate.partdist_acceptstat));
            if (i == 0){
                x = 0;
            }else{
                x = i;
            }
        }
        return x;
    }

    //signature view
    // Function for Digital Signature
    public void dialogsign(final String id){
        tempDir = Environment.getExternalStorageDirectory() + "/" + getResources().getString(R.string.app_name) + "/";
        ContextWrapper cw = new ContextWrapper(getContext());
        File directory = cw.getDir(getResources().getString(R.string.app_name), Context.MODE_PRIVATE);

        prepareDirectory();
        uniqueId = getTodaysDate() + "_" + getCurrentTime() + "_" + Math.random();
        current = uniqueId + ".png";
        mypath= new File(directory,current);


        mContent = (LinearLayout)dx.findViewById(R.id.linearLayout);
        mSignature = new Distributionlist.signature(getContext(), null);
        mSignature.setBackgroundColor(Color.WHITE);
        mContent.addView(mSignature, ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
        mClear = (Button)dx.findViewById(R.id.clearsign);
        mGetSign = (Button)dx.findViewById(R.id.savesign);
        mGetSign.setEnabled(false);
        mCancel = (Button)dx.findViewById(R.id.cancelsign);
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
                    gen.updateDistById(id, off);
                    dx.dismiss();
                }
            }
        });

        mCancel.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                Log.v("log_tag", "Panel Canceled");
                mSignature.clear();
                dx.dismiss();
            }
        });
        dx.show();
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
                //bytes.setText(off+"");

                mFileOutStream.flush();
                mFileOutStream.close();
                //String url = MediaStore.Images.Media.insertImage(ReservationData.this.getApplicationContext().getContentResolver(), mBitmap, "title", null);
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

}
