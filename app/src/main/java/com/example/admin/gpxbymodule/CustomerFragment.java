package com.example.admin.gpxbymodule;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class CustomerFragment extends Fragment{

    EditText address, phonenum, mobilenum, email,amount;
    String s_account, s_customer, s_address, s_phone, s_mobile, s_email,s_amount;

    AutoCompleteTextView accntnumber,customername;
    SQLiteDatabase db;
    ImageButton add;
    Date datetalaga;
    TextView date;
    GenDatabase helper;
    String accnt;
    Reserve reserve;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,  ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.customer_fragment, null);

        add = (ImageButton) view.findViewById(R.id.add_cust);
        date = (TextView)view.findViewById(R.id.date);
        helper = new GenDatabase(getContext());
        reserve = (Reserve)getActivity();

        datetalaga = Calendar.getInstance().getTime();

        SimpleDateFormat writeDate = new SimpleDateFormat("dd/MM/yyyy");
        writeDate.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        String s = writeDate.format(datetalaga);

        date.setText(s);

        accntnumber = (AutoCompleteTextView) view.findViewById(R.id.cust_acctnum);
        customername = (AutoCompleteTextView) view.findViewById(R.id.cust_customername);
        address = (EditText)view.findViewById(R.id.cust_address);
        phonenum = (EditText)view.findViewById(R.id.cust_phone);
        mobilenum = (EditText)view.findViewById(R.id.cust_mobilenum);
        email = (EditText)view.findViewById(R.id.cust_enter_email);

        auto();
        autoFullname();

        try {
            add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                        Intent mIntent = new Intent(getContext(), AddCustomer.class);
                        mIntent.putExtra("key", "reservation");
                        startActivity(mIntent);
                        getActivity().finish();
                }
            });

            if (reserve.getAccnt() != null) {
                autoaccount();
                Log.e("accountnum", reserve.getAccnt());
            }
        }catch (Exception e){}

        return  view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle("Customer Info");
        setHasOptionsMenu(true);
    }

    @Override
    public void onPause() {
        try {
            if ((reserve.getAccnt() != null)) {
                reserve.setAccnt(reserve.getAccnt());
            }

            String name = customername.getText().toString();
            if (name.equals("") || accntnumber.getText().toString().equals("")) {
                String t = "Customer name or account number is empty.";
                customToast(t);
            } else {
                reserve.setName(name);
                s_account = accntnumber.getText().toString();
                reserve.setAccnt(s_account);
            }
            //Log.e("accountnum", reserve.getAccnt());

            super.onPause();
        }catch (Exception e){}
    }

    public void auto(){
        try {
            String[] numbers = helper.getAccountNumber();
            ArrayAdapter<String> adapter = new ArrayAdapter<String>
                    (getContext(), android.R.layout.select_dialog_item, numbers);

            accntnumber.setThreshold(1);
            accntnumber.setAdapter(adapter);
            accntnumber.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String val = (String) parent.getItemAtPosition(position);
                    accntnumber.setText(val);
                    if (helper.checkCustomer(val)) {
                        db = helper.getReadableDatabase();
                        Cursor cursor = db.rawQuery(" SELECT * FROM " + helper.tbname_customers
                                + " WHERE " + helper.cust_accountnumber + " = '" + val + "' AND "
                                +helper.cust_type+" = 'customer'", null);
                        if (cursor.moveToNext()) {
                            accntnumber.setText(cursor.getString(cursor.getColumnIndex(helper.cust_accountnumber)));
                            String fullname = cursor.getString(cursor.getColumnIndex(helper.cust_firstname)) + " "
                                    + cursor.getString(cursor.getColumnIndex(helper.cust_lastname));

                            customername.setText(fullname);

                            String completeaddress = cursor.getString(cursor.getColumnIndex(helper.cust_unit)) + ", "
                                    + cursor.getString(cursor.getColumnIndex(helper.cust_barangay)) + ", "
                                    + cursor.getString(cursor.getColumnIndex(helper.cust_city)) + ", "
                                    + cursor.getString(cursor.getColumnIndex(helper.cust_prov)) + " "
                                    + cursor.getString(cursor.getColumnIndex(helper.cust_postal));
                            address.setText(completeaddress);

                            phonenum.setText(cursor.getString(cursor.getColumnIndex(helper.cust_phonenum)));
                            mobilenum.setText(cursor.getString(cursor.getColumnIndex(helper.cust_mobile)));
                            email.setText(cursor.getString(cursor.getColumnIndex(helper.cust_emailadd)));

                            //Log.d("counttempo", "AddNewCustomer Info displayed");
                        }
                    }
                }
            });
        }catch (Exception e){}
    }

    public void autoaccount() {
        try {
            if (helper.checkCustomer(reserve.getAccnt())) {
                db = helper.getReadableDatabase();
                Cursor cursor = db.rawQuery(" SELECT * FROM " + helper.tbname_customers
                        + " WHERE " + helper.cust_accountnumber + " = '" + reserve.getAccnt() + "' AND "
                        +helper.cust_type+" = 'customer'", null);
                if (cursor.moveToNext()) {
                    accntnumber.setText(cursor.getString(cursor.getColumnIndex(helper.cust_accountnumber)));
                    String fullname = cursor.getString(cursor.getColumnIndex(helper.cust_firstname)) + " "
                            + cursor.getString(cursor.getColumnIndex(helper.cust_lastname));

                    customername.setText(fullname);
                    String completeaddress = cursor.getString(cursor.getColumnIndex(helper.cust_unit)) + ", "
                            + cursor.getString(cursor.getColumnIndex(helper.cust_barangay)) + ", "
                            + cursor.getString(cursor.getColumnIndex(helper.cust_city)) + ", "
                            + cursor.getString(cursor.getColumnIndex(helper.cust_prov)) + " "
                            + cursor.getString(cursor.getColumnIndex(helper.cust_postal));
                    address.setText(completeaddress);

                    phonenum.setText(cursor.getString(cursor.getColumnIndex(helper.cust_phonenum)));
                    mobilenum.setText(cursor.getString(cursor.getColumnIndex(helper.cust_mobile)));
                    email.setText(cursor.getString(cursor.getColumnIndex(helper.cust_emailadd)));
                }
            }
        }catch (Exception e){}
    }

    public void autoFullname(){
        try {
            String[] names = helper.getFullnames();
            ArrayAdapter<String> adapter = new ArrayAdapter<String>
                    (getContext(), android.R.layout.select_dialog_item, names);
            customername.setThreshold(1);
            customername.setAdapter(adapter);
            customername.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String val = (String) parent.getItemAtPosition(position);
                    customername.setText(val);
                    if (helper.getCustomerInfo(val)) {
                        db = helper.getReadableDatabase();
                        Cursor cursor = db.rawQuery(" SELECT * FROM " + helper.tbname_customers
                                + " WHERE " + helper.cust_fullname + " = '" + val + "' AND "
                                +helper.cust_type+" = 'customer'", null);
                        if (cursor.moveToNext()) {
                            accntnumber.setText(cursor.getString(cursor.getColumnIndex(helper.cust_accountnumber)));
                            String fullname = cursor.getString(cursor.getColumnIndex(helper.cust_firstname)) + " "
                                    + cursor.getString(cursor.getColumnIndex(helper.cust_lastname));

                            customername.setText(fullname);

                            String completeaddress = cursor.getString(cursor.getColumnIndex(helper.cust_unit)) + ", "
                                    + cursor.getString(cursor.getColumnIndex(helper.cust_barangay)) + ", "
                                    + cursor.getString(cursor.getColumnIndex(helper.cust_city)) + ", "
                                    + cursor.getString(cursor.getColumnIndex(helper.cust_prov)) + " "
                                    + cursor.getString(cursor.getColumnIndex(helper.cust_postal));
                            address.setText(completeaddress);

                            phonenum.setText(cursor.getString(cursor.getColumnIndex(helper.cust_phonenum)));
                            mobilenum.setText(cursor.getString(cursor.getColumnIndex(helper.cust_mobile)));
                            email.setText(cursor.getString(cursor.getColumnIndex(helper.cust_emailadd)));

                            //Log.d("counttempo", "AddNewCustomer Info displayed");
                        }
                    }
                }
            });
        }catch (Exception e){}
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.savebooking).setVisible(false);
        menu.findItem(R.id.loadprev).setVisible(false);
        menu.findItem(R.id.loadprevpay).setVisible(false);
        menu.findItem(R.id.btnnext).setVisible(true);
        menu.findItem(R.id.btnnextpay).setVisible(false);

        super.onPrepareOptionsMenu(menu);
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

}