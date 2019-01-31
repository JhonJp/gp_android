package com.example.admin.gpxbymodule;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
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

public class AddNewCustomer extends Fragment {


    EditText fname, midname, lname,
            unit, pnum,mobnum,email, village, city, open;
    RatesDB rates;
    HomeDatabase helper;
    GenDatabase generaldb;
    AddCustomer cust;
    Spinner selectgender;
    DatePickerFragment date;
    ImageButton bdate;
    TextView birthdate;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.newcustomer, null);

        rates = new RatesDB(getContext());
        helper = new HomeDatabase(getContext());
        generaldb = new GenDatabase(getContext());

        cust = (AddCustomer)getActivity();
        cust.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checking();
                Log.e("fragment", "new customer");
            }
        });
        selectgender = (Spinner)view.findViewById(R.id.gender);
        fname = (EditText)view.findViewById(R.id.customerfirstnameinput);
        midname = (EditText)view.findViewById(R.id.customermiddlenameinput);
        lname = (EditText)view.findViewById(R.id.customerlastnameinput);
        birthdate = (TextView) view.findViewById(R.id.birthdateinput);
        bdate = (ImageButton) view.findViewById(R.id.addate);

        //address
        open = (EditText)view.findViewById(R.id.openinput);
        village = (EditText)view.findViewById(R.id.villageinput);
        city = (EditText)view.findViewById(R.id.cityinput);

        //contacts
        email = (EditText)view.findViewById(R.id.customeremailinput);
        pnum = (EditText)view.findViewById(R.id.customerphoneinput);
        mobnum = (EditText)view.findViewById(R.id.customermobileinput);

        textChange();
        spinnerGender();

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

    public void checking(){
        try {
            String variable = cust.customtype.getSelectedItem().toString();
            String first = fname.getText().toString();
            String mname = midname.getText().toString();
            String last = lname.getText().toString();
            String gend = selectgender.getSelectedItem().toString();
            String bday = birthdate.getText().toString();

            //addresses string
            String cty = city.getText().toString();
            String vill = village.getText().toString();
            String getunit = open.getText().toString();

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
            } else if (vill.equals("") || cty.equals("") || getunit.equals("")) {
                String x = "Please complete your address.";
                customToast(x);
            } else if (phonenum.equals("") || mobilenum.equals("")) {
                String x = "Please add email, phone and mobile number.";
                customToast(x);
            } else {
                Date datetalaga = Calendar.getInstance().getTime();
                SimpleDateFormat writeDate = new SimpleDateFormat("yyyyMMddHHmmss");
                writeDate.setTimeZone(TimeZone.getTimeZone("GMT+8"));
                String sa = writeDate.format(datetalaga);

                String accnumber = "GP-" + helper.logcount() + "" + sa;
                //contacts string
                String fullname = first + " " + last;

                generaldb.addCustomer(accnumber, "", first, mname, last, mobilenum, phonenum,
                        mail, gend, bday, "", cty, "",
                        vill, getunit, variable, "" + helper.logcount(), "1", fullname, "1");

                String addtype = "New Customer";

                generaldb.addTransactions(addtype, "" + helper.logcount(),
                        "Added new customer with account number " + accnumber, datereturn(), returntime());

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
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                            startActivity(new Intent(getContext(), Home.class));
                            getActivity().finish();
                    }
                });
        // Create the AlertDialog object and show it
        builder.create().show();
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

}
