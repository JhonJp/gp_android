package com.example.admin.gpxbymodule;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class Booking_info extends Fragment {

    AutoCompleteTextView reserveno, customername;
    EditText address, phoneno, mobileno, email;
    GenDatabase gen;
    SQLiteDatabase db;
    HomeDatabase home;
    Booking book;
    String fullname, accountno;
    ImageButton addcust;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.booking_info, null);

        gen = new GenDatabase(getContext());
        home = new HomeDatabase(getContext());

        book = (Booking) getActivity();

        reserveno = (AutoCompleteTextView)view.findViewById(R.id.book_reserveinput);
        customername = (AutoCompleteTextView)view.findViewById(R.id.cust_customername);
        address = (EditText)view.findViewById(R.id.book_address_input);
        phoneno = (EditText)view.findViewById(R.id.book_phonenum_input);
        mobileno = (EditText)view.findViewById(R.id.book_mobilenum_input);
        email = (EditText)view.findViewById(R.id.book_email_input);
        addcust = (ImageButton)view.findViewById(R.id.add_cust);

        if (book.getReserveno() != null){
            reserveno.setText(book.getReserveno());
            Log.e("reservenum", book.getReserveno());
        }

        if(book.getTransNo() != null ){
            autoall(book.getTransNo());
            Log.e("bookingtrans", book.getTransNo());
        }else{
            book.setTransNo(generateTransNo());
            Log.e("bookingtrans", book.getTransNo());
        }

        autocom();
        autoFullname();
        autoifFullname();

        addnewcust();

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle("Customer Info");
        setHasOptionsMenu(true);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.savebooking).setVisible(false);
        menu.findItem(R.id.loadprev).setVisible(false);
        menu.findItem(R.id.loadprevpay).setVisible(false);
        menu.findItem(R.id.btnnextpay).setVisible(false);
        menu.findItem(R.id.btnnext).setVisible(true);
        super.onPrepareOptionsMenu(menu);
    }

    //reservation numbers
    public void autocom(){
        try {
            String[] numbers = gen.getAllReservationNumber();
            ArrayAdapter<String> adapter = new ArrayAdapter<String>
                    (getContext(), android.R.layout.select_dialog_item, numbers);

            reserveno.setThreshold(1);
            reserveno.setAdapter(adapter);
            reserveno.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String val = (String) parent.getItemAtPosition(position);
                    reserveno.setText(val);
                    if (!(reserveno.getText().toString().equals(""))) {
                        SQLiteDatabase db = gen.getReadableDatabase();
                        Cursor cursor = db.rawQuery(" SELECT * FROM " + gen.tbname_reservation +
                                " WHERE " + gen.reserve_reservation_no + " = '" + val + "'", null);
                        String accntno = null;
                        if (cursor.getCount() != 0) {
                            cursor.moveToNext();
                            accntno = cursor.getString(cursor.getColumnIndex(gen.reserve_customer_id));
                        }
                        cursor.close();
                        if (gen.checkCustomer(accntno)) {
                            Cursor v = db.rawQuery(" SELECT * FROM " + gen.tbname_customers +
                                    " WHERE " + gen.cust_accountnumber + " = '" + accntno + "' AND "
                                    + gen.cust_type + " = 'customer'", null);
                            if (v.getCount() != 0) {
                                v.moveToNext();
                                String fname = v.getString(v.getColumnIndex(gen.cust_fullname));

                                String completeaddress = v.getString(v.getColumnIndex(gen.cust_unit)) + ", "
                                        + v.getString(v.getColumnIndex(gen.cust_barangay)) + ", "
                                        + v.getString(v.getColumnIndex(gen.cust_city)) + ", "
                                        + v.getString(v.getColumnIndex(gen.cust_prov)) + " "
                                        + v.getString(v.getColumnIndex(gen.cust_postal));

                                String phonenum = v.getString(v.getColumnIndex(gen.cust_phonenum));
                                String mobilenum = v.getString(v.getColumnIndex(gen.cust_mobile));
                                String mail = v.getString(v.getColumnIndex(gen.cust_emailadd));

                                customername.setText(fname);
                                address.setText(completeaddress);
                                phoneno.setText(phonenum);
                                mobileno.setText(mobilenum);
                                email.setText(mail);

                            }
                            v.close();
                        }
                    }
                }
            });
        }catch (Exception e){}
    }

    //add new customer
    public void addnewcust(){
        try {
            addcust.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent mIntent = new Intent(getContext(), AddCustomer.class);
                    mIntent.putExtra("key", "booking");
                    startActivity(mIntent);
                    getActivity().finish();
                }
            });
        }catch (Exception e){}
    }

    //get fullnames of customers in database
    public void autoFullname(){
        try {
            String[] names = gen.getFullnames();
            ArrayAdapter<String> adapter = new ArrayAdapter<String>
                    (getContext(), android.R.layout.select_dialog_item, names);

            customername.setThreshold(1);
            customername.setAdapter(adapter);
            customername.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String val = (String) parent.getItemAtPosition(position);
                    customername.setText(val);
                    if (gen.getCustomerInfo(val)) {
                        SQLiteDatabase db = gen.getReadableDatabase();
                        Cursor v = db.rawQuery(" SELECT * FROM " + gen.tbname_customers +
                                " WHERE " + gen.cust_fullname + " = '" + val + "' AND "
                                + gen.cust_type + " = 'customer'", null);
                        if (v.moveToNext()) {
                            String fname = v.getString(v.getColumnIndex(gen.cust_fullname));
                            String account = v.getString(v.getColumnIndex(gen.cust_accountnumber));

                            String completeaddress = v.getString(v.getColumnIndex(gen.cust_unit)) + ", "
                                    + v.getString(v.getColumnIndex(gen.cust_barangay)) + ", "
                                    + v.getString(v.getColumnIndex(gen.cust_city)) + ", "
                                    + v.getString(v.getColumnIndex(gen.cust_prov)) + " "
                                    + v.getString(v.getColumnIndex(gen.cust_postal));

                            String phonenum = v.getString(v.getColumnIndex(gen.cust_phonenum));
                            fullname = v.getString(v.getColumnIndex(gen.cust_fullname));
                            accountno = v.getString(v.getColumnIndex(gen.cust_accountnumber));
                            String mobilenum = v.getString(v.getColumnIndex(gen.cust_mobile));
                            String mail = v.getString(v.getColumnIndex(gen.cust_emailadd));

                            customername.setText(fname);
                            address.setText(completeaddress);
                            phoneno.setText(phonenum);
                            mobileno.setText(mobilenum);
                            email.setText(mail);
                        }
                    }
                }
            });
        }catch (Exception e){}
    }

    public void autoifFullname(){
        try {
            if (book.getFullname() != null) {
                if (gen.getCustomerInfo(book.getFullname())) {
                    SQLiteDatabase db = gen.getReadableDatabase();
                    Cursor v = db.rawQuery(" SELECT * FROM " + gen.tbname_customers +
                            " WHERE " + gen.cust_fullname + " = '" + book.getFullname() + "' AND "
                            + gen.cust_type + " = 'customer'", null);
                    if (v.moveToNext()) {
                        String account = v.getString(v.getColumnIndex(gen.cust_accountnumber));
                        String fname = v.getString(v.getColumnIndex(gen.cust_fullname));

                        String completeaddress = v.getString(v.getColumnIndex(gen.cust_unit)) + ", "
                                + v.getString(v.getColumnIndex(gen.cust_barangay)) + ", "
                                + v.getString(v.getColumnIndex(gen.cust_city)) + ", "
                                + v.getString(v.getColumnIndex(gen.cust_prov)) + " "
                                + v.getString(v.getColumnIndex(gen.cust_postal));

                        String phonenum = v.getString(v.getColumnIndex(gen.cust_phonenum));
                        fullname = v.getString(v.getColumnIndex(gen.cust_fullname));
                        String mobilenum = v.getString(v.getColumnIndex(gen.cust_mobile));
                        String mail = v.getString(v.getColumnIndex(gen.cust_emailadd));

                        customername.setText(fname);
                        address.setText(completeaddress);
                        phoneno.setText(phonenum);
                        mobileno.setText(mobilenum);
                        email.setText(mail);
                    }
                }
            }
        }catch (Exception e){}
    }

    public String generateTransNo(){
        String transNo = null;
        if (book.getTransNo() != null){
            transNo = book.getTransNo();
        }else {
            Date datetalaga = Calendar.getInstance().getTime();
            SimpleDateFormat writeDate = new SimpleDateFormat("yyyyMMddhhmmss");
            writeDate.setTimeZone(TimeZone.getTimeZone("GMT+8"));
            String sa = writeDate.format(datetalaga);

            transNo = home.logcount() + sa;
        }
        return transNo;
    }

    @Override
    public void onPause(){
        try {
            if (book.getTransNo() == null) {
                book.setTransNo(generateTransNo());
            } else {
                book.setTransNo(book.getTransNo());
                Log.e("bookingtrans", book.getTransNo());
            }

            if (!(customername.getText().toString().equals(""))) {
                book.setFullname(customername.getText().toString());
                Log.e("fullname", book.getFullname());

                String ffull = getAccntNo(customername.getText().toString());
                book.setAccntno(ffull);
            }

            if (reserveno.getText().toString() != null) {
                book.setReserveno(reserveno.getText().toString());
            }
            book.setFullname(customername.getText().toString());
            String ffull = getAccntNo(customername.getText().toString());
            book.setAccntno(ffull);

            super.onPause();
        }catch (Exception e){}
    }

    public String getAccntNo(String fulln){
        String fullname = null;
        if (gen.getCustomerInfo(book.getFullname())) {
            SQLiteDatabase db = gen.getReadableDatabase();
            Cursor v = db.rawQuery(" SELECT "+gen.cust_accountnumber+" FROM " + gen.tbname_customers +
                    " WHERE "+gen.cust_fullname+ " = '"+fulln+"'", null);
            if(v.moveToNext()) {
                fullname = v.getString(v.getColumnIndex(gen.cust_accountnumber));
            }
        }
        return fullname;
    }

    public void autoall(String transno){
        try {
            book.setTransNo(transno);
            SQLiteDatabase db = gen.getReadableDatabase();
            Cursor c = db.rawQuery(" SELECT * FROM " + gen.tbname_booking
                    + " WHERE " + gen.book_transaction_no + " = '" + transno + "'", null);
            if (c.moveToNext()) {
                String reservenum = c.getString(c.getColumnIndex(gen.book_reservation_no));
                String account = c.getString(c.getColumnIndex(gen.book_customer));
                String full = getFullname(account);

                reserveno.setText(reservenum);
                customername.setText(full);

                book.setFullname(full);
                book.setAccntno(account);
                book.setReserveno(reservenum);

            }
            c.close();
        }catch (Exception e){}
    }

    public String getFullname(String accountno){
        String fullname = null;
        SQLiteDatabase db = gen.getReadableDatabase();
        Cursor x = db.rawQuery(" SELECT "+gen.cust_fullname+" FROM "
                +gen.tbname_customers+" WHERE "+gen.cust_accountnumber
                +" = '"+accountno+"'", null);

        if (x.moveToNext()){
            fullname = x.getString(x.getColumnIndex(gen.cust_fullname));
            book.setFullname(fullname);
        }
        autoifFullname();
        return fullname;
    }

}