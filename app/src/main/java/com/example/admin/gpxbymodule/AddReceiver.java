package com.example.admin.gpxbymodule;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class AddReceiver extends Fragment {


    EditText fname, midname, lname,
            unit, pnum,mobnum,email, postal, sendaccount;
    TextView birthdate;
    RatesDB rates;
    AddCustomer cust;
    HomeDatabase helper;
    GenDatabase generaldb;
    Spinner selectgender, province, city, brgy;
    DatePickerFragment date;
    ImageButton bdate;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.addnewreceiver, null);
        selectgender = (Spinner)view.findViewById(R.id.gender);

        rates = new RatesDB(getContext());
        helper = new HomeDatabase(getContext());
        generaldb = new GenDatabase(getContext());
        cust = (AddCustomer)getActivity();

        cust.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checking();
                Log.e("fragment", "new receiver");
            }
        });

        selectgender = (Spinner)view.findViewById(R.id.gender);
        bdate = (ImageButton) view.findViewById(R.id.addate);
        sendaccount = (EditText)view.findViewById(R.id.customaccountinput);
        fname = (EditText)view.findViewById(R.id.customerfirstnameinput);
        midname = (EditText)view.findViewById(R.id.customermiddlenameinput);
        lname = (EditText)view.findViewById(R.id.customerlastnameinput);
        birthdate = (TextView) view.findViewById(R.id.birthdateinput);

        //addresses
        province = (Spinner)view.findViewById(R.id.provinput);
        city = (Spinner)view.findViewById(R.id.cityinput);
        brgy = (Spinner)view.findViewById(R.id.barangayinput);
        unit = (EditText)view.findViewById(R.id.openinput);
        postal = (EditText)view.findViewById(R.id.openfield);

        //contacts
        email = (EditText)view.findViewById(R.id.customeremailinput);
        pnum = (EditText)view.findViewById(R.id.customerphoneinput);
        mobnum = (EditText)view.findViewById(R.id.customermobileinput);

        textChange();
        spinnerGender();
        spinnerProvince();
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle("New Customer");
        setHasOptionsMenu(false);
    }

    public void spinnerGender(){
        String[] items = new String[]{"Male", "Female"};
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(getContext(), R.layout.spinneritem,
                        items);
        selectgender.setAdapter(adapter);
    }

    public void textChange(){
        try {
            birthdate.setText(datereturn());
            birthdate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDatePicker();
                }
            });
            bdate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDatePicker();
                }
            });

        }catch (Exception e){}
    }

    public void spinnerProvince(){
        try {
            final String[] prov = rates.getAllProv();
            ArrayAdapter<String> adapter =
                    new ArrayAdapter<>(getContext(), R.layout.spinneritem,
                            prov);
            province.setAdapter(adapter);
            province.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String code = rates.getProvCode(province.getSelectedItem().toString());
                    getCode(code);

                    Log.e("selected", code);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    String code = rates.getProvCode(province.getSelectedItem().toString());
                    getCode(code);
                    Log.e("selected", code);
                }
            });
        }catch (Exception e){}
    }

    public void getCode(String c){
        try {
            String[] cities = rates.getAllCities(c);
            ArrayAdapter<String> adapter =
                    new ArrayAdapter<>(getContext(), R.layout.spinneritem,
                            cities);
            city.setAdapter(adapter);
            city.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String c = rates.getCityCode(city.getSelectedItem().toString());
                    getCodeBrgy(c);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    String c = rates.getCityCode(city.getSelectedItem().toString());
                    getCodeBrgy(c);
                }
            });
            Log.e("code", c);
        }catch (Exception e){}
    }

    public void getCodeBrgy(String c){
        try {
            String[] cities = rates.getAllBrgy(c);
            ArrayAdapter<String> adapter =
                    new ArrayAdapter<>(getContext(), R.layout.spinneritem,
                            cities);
            brgy.setAdapter(adapter);
            brgy.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    postal.setText(getBrgyCode(parent.getItemAtPosition(position).toString()));
                    Log.e("brgycode", parent.getSelectedItem().toString());
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    postal.setText(getBrgyCode(parent.getSelectedItem().toString()));
                    Log.e("brgycode", parent.getSelectedItem().toString());
                }
            });
            Log.e("code", c);
        }catch (Exception e){}
    }

    public void checking(){
        try {
            String variable = cust.customtype.getSelectedItem().toString();
            String acc = sendaccount.getText().toString();
            String first = fname.getText().toString();
            String mname = midname.getText().toString();
            String last = lname.getText().toString();
            String gend = selectgender.getSelectedItem().toString();
            String bday = birthdate.getText().toString();

            //addresses string
            String prov = getProvCode(province.getSelectedItem().toString());
            String cty = getCityCode(city.getSelectedItem().toString());
            String barang = getBrgyCode(brgy.getSelectedItem().toString());
            String getunit = unit.getText().toString();
            String post = postal.getText().toString();

            //contacts string
            String phonenum = pnum.getText().toString();
            String mobilenum = mobnum.getText().toString();
            String mail = email.getText().toString();
            if (!mail.equals("")) {
                if(!isEmailValid(mail)) {
                    String invalidemail = "Your email is invalid, please change it.";
                    customToast(invalidemail);
                }
            }else{
                mail = email.getText().toString();
            }

            if (first.equals("") || mname.equals("") || last.equals("")) {
                String x = "Please fill up the name fields.";
                customToast(x);
            } else if (prov.equals("") || barang.equals("") || cty.equals("") || getunit.equals("")) {
                String x = "Please complete your address.";
                customToast(x);
            } else if (phonenum.equals("") || mobilenum.equals("")) {
                String x = "Please add email, phone and mobile number.";
                customToast(x);
            }else {
                Date datetalaga = Calendar.getInstance().getTime();
                SimpleDateFormat writeDate = new SimpleDateFormat("yyyyMMddHHmmss");
                writeDate.setTimeZone(TimeZone.getTimeZone("GMT+8"));
                String sa = writeDate.format(datetalaga);

                int total_count = generaldb.countcustomers();

                String accnumber = "GP-" + helper.logcount() + "" + sa;
                //contacts string
                String fullname = first + " " + last;

                generaldb.addCustomer(accnumber, acc, first, mname, last, mobilenum, phonenum,
                        mail, gend, bday, prov, cty, post, barang, getunit, variable,
                        "" + helper.logcount(), "1", fullname, "1");

                String addtype = "New Customer";

                generaldb.addTransactions(addtype, "" + helper.logcount(),
                        "Added new receiver with account number " + accnumber, datereturn(), returntime());

                alert();
            }
        }catch (Exception e){}
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

    public void alert(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Account has been added.");
        builder.setMessage("You may proceed on transactions.")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        startActivity(new Intent(getContext(), Home.class));
                        getActivity().finish();
                    }
                });
        // Create the AlertDialog object and show it
        builder.create().show();
    }

    public void customToast(String txt){
        Toast toast = new Toast(getActivity().getApplicationContext());
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

    private boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
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

    DatePickerDialog.OnDateSetListener ondate = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            birthdate.setText(String.valueOf(year) + "-" + String.valueOf(monthOfYear+1)
                    + "-" + String.valueOf(dayOfMonth));
            date.dismiss();
        }
    };

    public String getProvCode(String name){
        String code = null;
        try{
            SQLiteDatabase db = rates.getReadableDatabase();
            String que = " SELECT * FROM "+rates.tbname_provinces
                    +" WHERE "+rates.prov_name+" = '"+name+"'";
            Cursor c = db.rawQuery(que,null);
            if (c.moveToNext()){
                code = c.getString(c.getColumnIndex(rates.prov_code));
            }
        }catch (Exception e){}
        return code;
    }

    public String getCityCode(String name){
        String code = null;
        try{
            SQLiteDatabase db = rates.getReadableDatabase();
            String que = " SELECT * FROM "+rates.tbname_city
                    +" WHERE "+rates.ct_name+" = '"+name+"'";
            Cursor c = db.rawQuery(que,null);
            if (c.moveToNext()){
                code = c.getString(c.getColumnIndex(rates.ct_citycode));
            }
        }catch (Exception e){}
        return code;
    }

    public String getBrgyCode(String name){
        String code = null;
        try{
            SQLiteDatabase db = rates.getReadableDatabase();
            String que = " SELECT * FROM "+rates.tbname_brgy
                    +" WHERE "+rates.brgy_name+" = '"+name+"'";
            Cursor c = db.rawQuery(que,null);
            if (c.moveToNext()){
                code = c.getString(c.getColumnIndex(rates.brgy_code));
            }
        }catch (Exception e){}
        return code;
    }

}
