package com.example.admin.gpxbymodule;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
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
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class Destination_info extends Fragment {

    GenDatabase gen;
    HomeDatabase helper;
    RatesDB rate;
    EditText trucknum, remarks;
    FrameLayout captive;
    int id;
    int CAMERA_REQUEST = 1;
    Distribution dist;
    RatesDB rates;
    GridView grid;
    int wareid;
    TextView hint;

    //image
    ArrayList<HomeList> stored_image;
    ArrayList<byte[]> capt_images;
    ProgressDialog progressBar;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.destination_address, null);
        Log.e("frag", "Transactions");
        trucknum = (EditText)view.findViewById(R.id.trucknumber);
        remarks = (EditText)view.findViewById(R.id.dist_boxremarks_input);
        captive = (FrameLayout) view.findViewById(R.id.imagecapt);
        grid = (GridView)view.findViewById(R.id.grid);
        hint = (TextView)view.findViewById(R.id.imageshint);
        dist = (Distribution)getActivity();

        gen = new GenDatabase(getContext());
        rate = new RatesDB(getContext());
        rates = new RatesDB(getContext());
        helper = new HomeDatabase(getContext());
        capt_images = new ArrayList<>();
        stored_image = new ArrayList<HomeList>();
        if (dist.getCapt_images() != null){
            capt_images = dist.getCapt_images();
        }
        if (dist.getStoreimages() != null){
            stored_image = dist.getStoreimages();
        }
        capt();
        viewgrid();

        trucknum.setText(dist.getDisttrucknumber());
        remarks.setText(dist.getRemarks());
        if (dist.getTrans() != null){
            dist.setTrans(dist.getTrans());
            Log.e("transactionnum", dist.getTrans());
        }

        Log.e("numbers", dist.getBoxnumbers()+"");
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle("Destination Info");
        setHasOptionsMenu(true);
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
    public void onPause(){
        try {
            String truck = trucknum.getText().toString();
            String rem = remarks.getText().toString();

            if (truck.equals("")) {
                String x = "Truck number is required.";
                customToast(x);
            }
            if (dist.getTrans() != null) {
                dist.setTrans(dist.getTrans());
            }
            if (stored_image.size() != 0){
                dist.setStoreimages(stored_image);
            }
            if (capt_images.size() != 0){
                dist.setCapt_images(capt_images);
            }

            dist.setDisttrucknumber(trucknum.getText().toString());
            dist.setRemarks(remarks.getText().toString());

        }catch (Exception e){}
        super.onPause();
    }

    public void capt() {
        try {
            captive.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                    if (capt_images.size() >= 5){
                        String ty = "Maximum image attachment has been reached.";
                        customToast(ty);
                    }else {
                        try{
                            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                            startActivityForResult(cameraIntent, CAMERA_REQUEST);
                            } catch (Exception e) {
                                    ActivityCompat.requestPermissions(getActivity(),
                                            new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST);
                        }
                    }
                    } catch (Exception e) {
                        ActivityCompat.requestPermissions(getActivity(),
                                new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST);
                    }
                }
            });
        }catch (Exception e){}
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_savedistribute){
            if (capt_images.size() == 0 || stored_image.size() == 0){
                String x = "Please add image attachment for this transaction.";
                customToast(x);
            }else {
                extendconfirm();
            }
        }
        return super.onOptionsItemSelected(item);
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
                    Thread.sleep(3000);
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
                                        startActivity(new Intent(getContext(), Distribution.class));
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

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                photo.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] bytimg = stream.toByteArray();
                String trans = dist.getTrans();
                HomeList list = new HomeList(bytimg, capt_images.size()+"");
                capt_images.add(bytimg);
                stored_image.add(list);
                viewgrid();
                Log.e("camera", "success " + bytimg + " / " + trans);
            }
        }catch (Exception e){}
    }

    public void viewgrid(){
        try {
            final ArrayList<HomeList> listitem = stored_image;
            ImageAdapter myAdapter = new ImageAdapter(getContext(), listitem);
            grid.setAdapter(myAdapter);
            if (capt_images.size() > 0) {
                hint.setVisibility(View.INVISIBLE);
            } else {
                hint.setVisibility(View.VISIBLE);
            }
            grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    byte[] getitem = listitem.get(position).getTopitem();
                    alertImage(getitem, position);
                }
            });
        }catch (Exception e){}
    }

    public void alertImage(final byte[] image, final int idt){
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
                    capt_images.remove(image);
                    stored_image.remove(idt);
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

    //save distribution
    public void extendconfirm(){
        try {
            if ((dist.getDisttype() == null) || (dist.getDistname() == null)) {
                String x = "Distribution type or name is empty.";
                customToast(x);
            } else {
                String f_trans = dist.getTrans();
                String f_type = dist.getDisttype();
                String f_name = dist.getDistname();
                String f_truck = trucknum.getText().toString();
                String f_remarks = remarks.getText().toString();
                if ((f_type == null) || (f_name == null)) {
                    String x = "Distribution type and item is required.";
                    customToast(x);
                } else if ((f_truck.equals(""))) {
                    String x = "Truck number is required.";
                    customToast(x);
                } else {
                    if (dist.getBoxnumbers().size() == 0) {
                        String x = "Please add box to be distributed.";
                        customToast(x);
                    } else {
                        if (gen.addDistribution(f_trans, f_type, f_name, f_truck,
                                f_remarks, "1", "1", datereturn(),
                                helper.logcount() + "", 0, null)) {

                            for (String bn : dist.getBoxnumbers()){
                                updateBxInv(bn);
                                updBarcodeInv(bn);
                            }

                            for (byte[] img : capt_images){
                                rate.addGenericImage("distribution", f_trans, img);
                            }

                            //lopp through boxnumbers and save to distribution table
                            for (int i = 0; i < dist.getBoxIDS().size(); i++){
                                gen.addtoCheckerInv(dist.getBoxIDS().get(i),
                                        dist.getBoxnumbers().get(i),"1","1");
                                gen.addTempBoxDist(dist.getTrans(),
                                        dist.getBoxIDS().get(i),
                                        dist.getInventoryIDS().get(i),
                                        dist.getBoxnumbers().get(i), "1");

                                Log.e("distribution", "trans: "+dist.getTrans()+", "
                                +"boxid: "+dist.getBoxIDS().get(i)+", invid: "+dist.getInventoryIDS().get(i)
                                +", boxnumber: "+dist.getBoxnumbers().get(i));

                            }

                            loopBarcode(dist.getWarehouse(), dist.getTrans());
                            String type = "Distribution";
                            String user = "" + helper.logcount();
                            gen.addTransactions(type, user, "Distribution to " + dist.getDistname(), datereturn(), returntime());

                            dist.setTrans(null);
                            dist.setRemarks(null);
                            dist.setDisttrucknumber(null);
                            dist.setDistname(null);
                            dist.setDisttype(null);
                            dist.getBoxnumbers().clear();
                            dist.getBoxIDS().clear();
                            dist.getInventoryIDS().clear();
                            dist.setBoxnumbers(null);
                            dist.setBoxIDS(null);
                            dist.setInventoryIDS(null);
                            dist.setCapt_images(null);
                            dist.setStoreimages(null);
                            capt_images.clear();
                            stored_image.clear();

                            loadingPost(getView());

                        } else {
                            String a = "Transaction failed, please try again.";
                            customToast(a);
                        }
                    }
                }
            }
        }catch (Exception e){}
    }

    public void updateBxInv(String bn){
        SQLiteDatabase db = gen.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(gen.chinv_stat, "1");
        db.update(gen.tbname_checker_inventory, cv,
                gen.chinv_boxnumber+" = '"+bn+"'", null);
        db.close();
    }

    public void updBarcodeInv(String bn){
        SQLiteDatabase db = rate.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(rate.barcodeinv_boxnumber, bn);
        cv.put(rate.barcodeinv_status, "1");
        db.update(rate.tbname_barcode_inventory, cv,
                rate.barcodeinv_boxnumber+" = '"+bn+"'", null);
        Log.e("barcode_inv_update", bn);
        db.close();
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

    public void loopBarcode(String wid, String trans){
        try {
            SQLiteDatabase db = gen.getReadableDatabase();

            Cursor res = db.rawQuery(" SELECT * FROM " + gen.tbname_tempboxes
                    + " WHERE " + gen.dboxtemp_distributionid + " = '" + trans + "'", null);
            res.moveToFirst();
            while (!res.isAfterLast()) {
                String topitem = res.getString(res.getColumnIndex(gen.dboxtemp_boxnumber));
                String boxid = getBoxId(topitem);
                //updateBoxQuantity(wid, boxid);
                res.moveToNext();
            }
            res.close();
            db.close();
        }catch (Exception e){}
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

    public String getBoxId(String barcode) {
        String name = null;
        SQLiteDatabase db = gen.getReadableDatabase();
        String query = "SELECT * FROM " + gen.tbname_boxes + " LEFT JOIN "
                + gen.tbname_barcode + " ON " + gen.tbname_boxes + "." + gen.box_id + " = " + gen.tbname_barcode + "." + gen.barcode_boxtype
                + " WHERE '" + barcode + "' BETWEEN " + gen.barcode_series_start + " AND " + gen.barcode_series_end + "";
        Cursor c = db.rawQuery(query, null);
        if (c.getCount() != 0) {
            c.moveToNext();
            name = c.getString(c.getColumnIndex(gen.box_id));
        }
        return name;
    }
}
