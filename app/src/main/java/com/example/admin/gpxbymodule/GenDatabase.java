package com.example.admin.gpxbymodule;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.Html;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.IllegalFormatCodePointException;

public class GenDatabase extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "gpx_general.db";

    //gpx boxes types
    public final String tbname_boxes = "gpx_boxtype";
    public final String box_id = "id";
    public final String box_name = "name";
    public final String box_depositprice = "depositprice";
    public final String box_length = "size_length";
    public final String box_width = "size_width";
    public final String box_height = "size_height";
    public final String box_nsb = "nsb";
    public final String box_description = "description";
    public final String box_createdby = "createdby";
    public final String box_recordstatus = "recordstatus";
    public final String createTableBox = " CREATE TABLE " + tbname_boxes + "("
            + box_id + " INTEGER PRIMARY KEY UNIQUE,"
            + box_name + " TEXT UNIQUE, "
            + box_depositprice + " TEXT, "
            + box_length + " TEXT, "
            + box_width + " TEXT, "
            + box_height + " TEXT, "
            + box_nsb + " TEXT, "
            + box_description + " TEXT, "
            + box_createdby + " TEXT, "
            + box_recordstatus + " TEXT )";
    public final String dropbox = " DROP TABLE IF EXISTS " + tbname_boxes;

    //gpx reservation status
    public final String tbname_reservation_status = "gpx_reservation_status";
    public final String stat_id = "id";
    public final String stat_name = "name";
    public final String createStats = " CREATE TABLE " + tbname_reservation_status + "("
            + stat_id + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + stat_name + " TEXT )";
    public final String dropStatTB = " DROP TABLE IF EXISTS " + tbname_reservation_status;

    //gpx reservation boxtype
    public final String tbname_reservation_boxtype = "gpx_reservation_boxtype";
    public final String res_btype_id = "reservation_boxtype_id";
    public final String res_btype_boxtype_id = "boxtype_id";
    public final String res_boxtype = "boxtype";
    public final String res_quantity = "quantity";
    public final String res_deposit = "deposit";
    public final String res_reservation_id = "reservation_no";
    public final String createReserveboxtype = " CREATE TABLE " + tbname_reservation_boxtype + "("
            + res_btype_id + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + res_btype_boxtype_id + " TEXT, "
            + res_boxtype + " TEXT, "
            + res_quantity + " TEXT, "
            + res_deposit + " TEXT, "
            + res_reservation_id + " TEXT )";
    public final String dropReserveBoxtype = " DROP TABLE IF EXISTS " + tbname_reservation_boxtype;

    //gpx reservation boxtype boxnumber
    public final String tbname_reservation_boxtype_boxnumber = "gpx_reservation_boxtype_boxnumber";
    public final String res_btype_bnum_id = "id";
    public final String res_btype_bnum_boxtype = "boxtype";
    public final String res_btype_bnum_box_number = "box_number";
    public final String res_btype_bnum_box_depoprice = "depositprice";
    public final String res_btype_bnum_reservation_id = "reservation_no";
    public final String res_btype_bnum_createddate = "createddate";
    public final String res_btype_bnum_stat = "status";
    public final String createReserveboxtypeBoxnumber = " CREATE TABLE " + tbname_reservation_boxtype_boxnumber + "("
            + res_btype_bnum_id + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + res_btype_bnum_boxtype + " TEXT, "
            + res_btype_bnum_box_number + " TEXT, "
            + res_btype_bnum_box_depoprice + " TEXT, "
            + res_btype_bnum_createddate + " TEXT, "
            + res_btype_bnum_stat + " TEXT, "
            + res_btype_bnum_reservation_id + " TEXT )";
    public final String dropReserveBoxtypeBoxnumber = " DROP TABLE IF EXISTS " + tbname_reservation_boxtype_boxnumber;


    //gpx reservation table
    public final String tbname_reservation = "gpx_reservation";
    public final String reserve_id = "reservation_id";
    public final String reserve_reservation_no = "reservation_no";
    public final String reserve_customer_id = "customer_id";
    public final String reserve_createdby = "createdby";
    public final String reserve_createddate = "createddate";
    public final String reserve_assigned_to = "assigned_to";
    public final String reserve_status = "status";
    public final String reserve_upload_status = "upload_status";
    public final String createReservation = " CREATE TABLE " + tbname_reservation + "("
            + reserve_id + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + reserve_reservation_no + " TEXT UNIQUE, "
            + reserve_customer_id + " TEXT, "
            + reserve_createdby + " TEXT, "
            + reserve_createddate + " TEXT, "
            + reserve_assigned_to + " TEXT, "
            + reserve_status + " TEXT, "
            + reserve_upload_status + " TEXT )";
    public final String dropReservation = " DROP TABLE IF EXISTS " + tbname_reservation;

    //gpx reservation table
    public final String tbname_customers = "gpx_customers";
    public final String cust_id = "id";
    public final String cust_accountnumber = "account_no";
    public final String cust_firstname = "firstname";
    public final String cust_middlename = "middlename";
    public final String cust_lastname = "lastname";
    public final String cust_mobile = "mobile";
    public final String cust_secmobile = "secondary_number";
    public final String cust_thirdmobile = "another_number";
    public final String cust_phonenum = "phone";
    public final String cust_emailadd = "email";
    public final String cust_gender = "gender";
    public final String cust_birthdate = "birthdate";
    public final String cust_prov = "province";
    public final String cust_barangay = "barangay";
    public final String cust_city = "city";
    public final String cust_postal = "postal";
    public final String cust_unit = "unit";
    public final String cust_type = "type";
    public final String cust_createdby = "createdby";
    public final String cust_fullname = "name";
    public final String cust_senderaccount = "senders_account_no";
    public final String cust_recordstatus = "recordstatus";
    public final String cust_uploadstat = "upload_status";
    public final String createCustomerTB = " CREATE TABLE " + tbname_customers + "("
            + cust_id + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + cust_accountnumber + " TEXT UNIQUE, "
            + cust_firstname + " TEXT, "
            + cust_middlename + " TEXT, "
            + cust_lastname + " TEXT, "
            + cust_mobile + " TEXT, "
            + cust_secmobile + " TEXT, "
            + cust_thirdmobile + " TEXT, "
            + cust_phonenum + " TEXT, "
            + cust_emailadd + " TEXT, "
            + cust_gender + " TEXT, "
            + cust_birthdate + " TEXT, "
            + cust_prov + " TEXT, "
            + cust_barangay + " TEXT, "
            + cust_city + " TEXT, "
            + cust_postal + " TEXT, "
            + cust_unit + " TEXT, "
            + cust_type + " TEXT, "
            + cust_fullname + " TEXT, "
            + cust_senderaccount + " TEXT, "
            + cust_createdby + " TEXT, "
            + cust_uploadstat + " TEXT, "
            + cust_recordstatus + " TEXT )";
    public final String dropCustomersTB = " DROP TABLE IF EXISTS " + tbname_customers;

    // temporary table for distribution
    public final String tbname_tempDist = "gpx_distribution";
    public final String temp_id = "id";
    public final String temp_transactionnumber = "transaction_no";
    public final String temp_type = "type";
    public final String temp_typename = "typename";
    public final String temp_trucknum = "trucknumber";
    public final String temp_remarks = "remarks";
    public final String temp_status = "status";
    public final String temp_createdate = "created_date";
    public final String temp_createby = "createby";
    public final String temp_acceptstat = "acceptance_status";
    public final String temp_acceptsign = "acceptance_signature";
    public final String temp_uploadstat = "upload_status";
    public final String createTempDist = " CREATE TABLE " + tbname_tempDist + "("
            + temp_id + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + temp_transactionnumber + " TEXT UNIQUE, "
            + temp_type + " TEXT, "
            + temp_typename + " TEXT, "
            + temp_trucknum + " TEXT, "
            + temp_remarks + " TEXT, "
            + temp_status + " TEXT, "
            + temp_createdate + " TEXT, "
            + temp_createby + " TEXT, "
            + temp_acceptstat + " INTEGER, "
            + temp_acceptsign + " BLOB, "
            + temp_uploadstat + " TEXT )";
    public final String dropTempDist = " DROP TABLE IF EXISTS " + tbname_tempDist;

    // distribution table
    public final String tbname_tempboxes = "gpx_distribution_box";
    public final String dboxtemp_id = "id";
    public final String dboxtemp_boxid = "boxtype_id";
    public final String dboxtemp_invid = "inventory_id";
    public final String dboxtemp_boxnumber = "boxnumber";
    public final String dboxtemp_distributionid = "distribution_id";
    public final String dboxtemp_stat = "distribution_status";
    public final String createDBoxTemp = " CREATE TABLE " + tbname_tempboxes + "("
            + dboxtemp_id + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + dboxtemp_invid + " TEXT, "
            + dboxtemp_boxid + " TEXT, "
            + dboxtemp_boxnumber + " TEXT, "
            + dboxtemp_distributionid + " TEXT, "
            + dboxtemp_stat + " TEXT )";
    public final String dropDBoxTemp = " DROP TABLE IF EXISTS " + tbname_tempboxes;

    //table payment
    public final String tbname_payment = "gpx_payment";
    public final String pay_id = "id";
    public final String pay_or_no = "or_no";
    public final String pay_booking_id = "booking_id";
    public final String pay_reservation_id = "reservation_no";
    public final String pay_paymentterm = "paymentterm";
    public final String pay_deposit = "deposit";
    public final String pay_total_amount = "total_amount";
    public final String pay_createdby = "createdby";
    public final String pay_createddate = "createddate";
    public final String createTBPayment = " CREATE TABLE " + tbname_payment + "("
            + pay_id + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + pay_or_no + " TEXT, "
            + pay_booking_id + " TEXT, "
            + pay_reservation_id + " TEXT, "
            + pay_paymentterm + " TEXT, "
            + pay_deposit + " TEXT, "
            + pay_total_amount + " TEXT, "
            + pay_createdby + " TEXT, "
            + pay_createddate + " TEXT )";
    public final String dropTBpayment = " DROP TABLE IF EXISTS " + tbname_payment;

    //gpx temporary reservation table
    public final String tbname_temporary_reservation = "gpx_temporary_reservation";
    public final String temp_reserve_id = "temp_id";
    public final String temp_account = "temp_accountnumber";
    public final String temp_stats = "temp_status";
    public final String createTempReserve = " CREATE TABLE " + tbname_temporary_reservation + "("
            + temp_reserve_id + " INTEGER PRIMARY KEY, "
            + temp_account + " TEXT, "
            + temp_stats + " TEXT )";
    public final String dropTempReserve = " DROP TABLE IF EXISTS " + tbname_temporary_reservation;

    //gpx temporary reservation table
    public final String tbname_temporary_reservation_boxtype = "gpx_temporary_reservation_boxtype";
    public final String temp_reserve_btype_id = "temp_id";
    public final String temp_btype_account = "temp_accountnumber";
    public final String temp_btype_boxtype = "temp_boxtype";
    public final String temp_btype_quantity = "temp_quantity";
    public final String temp_btype_depoprice = "deposit_price";
    public final String createTempReserveBtype = " CREATE TABLE " + tbname_temporary_reservation_boxtype + "("
            + temp_reserve_btype_id + " INTEGER PRIMARY KEY, "
            + temp_btype_account + " TEXT, "
            + temp_btype_boxtype + " TEXT, "
            + temp_btype_quantity + " TEXT, "
            + temp_btype_depoprice + " TEXT )";
    public final String dropTempReserveBtype = " DROP TABLE IF EXISTS " + tbname_temporary_reservation_boxtype;

    //acceptance
    public final String tb_acceptance = "gpx_warehouse_inventory";
    public final String acc_id = "id";
    public final String acc_warehouse_id = "warehouse_id";
    public final String acc_name = "manufacturer_name";
    public final String acc_boxtype = "boxtype_id";
    public final String acc_quantity = "quantity";
    public final String acc_createddate = "createddate";
    public final String acc_createdby = "createdby";
    public final String acc_status = "status";
    public final String acc_upds = "upload_status";

    public final String createTableAcceptance = " CREATE TABLE " + tb_acceptance + "("
            + acc_id + " TEXT PRIMARY KEY UNIQUE, "
            + acc_warehouse_id + " TEXT, "
            + acc_name + " TEXT, "
            + acc_boxtype + " TEXT, "
            + acc_quantity + " TEXT, "
            + acc_createddate + " TEXT, "
            + acc_createdby + " TEXT, "
            + acc_status + " TEXT, "
            + acc_upds + " TEXT )";
    public final String dropAccept = "DROP TABLE IF EXISTS " + tb_acceptance;

    //remittance
    public final String tbname_remittance = "gpx_remittance";
    public final String remit_id = "id";
    public final String remit_type = "remittance_type";
    public final String remit_name = "remittance_name";
    public final String remit_accountname = "account_name";
    public final String remit_accountnumber = "account_number";
    public final String remit_amount = "remittance_amount";
    public final String remit_signature = "remittance_signature";
    public final String remit_createdby = "createdby";
    public final String remit_createddate = "createddate";
    public final String remit_status = "remittance_status";
    public final String remit_upds = "upload_status";
    //create all table in remittance
    public final String createTBremittance = " CREATE TABLE " + tbname_remittance + "("
            + remit_id + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + remit_type + " TEXT, " // TO OIC OR BANK
            + remit_name + " TEXT, " // OIC NAME OR BANK NAME
            + remit_accountname + " TEXT, " // ACCOUNT NAME FOR BANK
            + remit_accountnumber + " TEXT, " // ACCOUNT NUMBER FOR BANK
            + remit_amount + " TEXT, " // AMOUNT
            + remit_signature + " BLOB, " // SIGNATURE
            + remit_createdby + " TEXT, " // CREATED BY
            + remit_createddate + " TEXT, " // CREATED DATE
            + remit_upds + " TEXT, " // CREATED DATE
            + remit_status + " TEXT )"; // REMITTANCE STATUS
    public final String dropRemittance = " DROP TABLE IF EXISTS " + tbname_remittance;

    //remittance transaction (denom or expense)
    public final String tbname_remittance_trans = "gpx_remittance_transactions";
    public final String rem_trans_id = "id"; // unique id
    public final String rem_trans_remittanceid = "remittance_id"; // remittance id
    public final String rem_trans_type = "remittance_type"; // types denom or expenses
    public final String rem_trans_denoms = "remittance_denominations"; // (denom) denominations
    public final String rem_trans_quantity = "remittance_quantity"; // (denom) denominations quantity
    public final String rem_trans_itemname = "remittance_itemname"; // (exp) item name
    public final String rem_trans_amount = "remittance_amount"; // (exp) expense amount
    public final String rem_trans_desc = "remittance_description"; // (exp) description
    public final String rem_trans_image = "remittance_image"; // (all) image
    public final String rem_trans_stat = "remittance_stat"; // (all) stat
    //create all table in remittance transactions
    public final String createRemTrans = " CREATE TABLE " + tbname_remittance_trans + "("
            + rem_trans_id + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + rem_trans_remittanceid + " TEXT, "
            + rem_trans_type + " TEXT, "
            + rem_trans_denoms + " TEXT, "
            + rem_trans_quantity + " TEXT, "
            + rem_trans_itemname + " TEXT, "
            + rem_trans_amount + " TEXT, "
            + rem_trans_desc + " TEXT, "
            + rem_trans_stat + " TEXT, "
            + rem_trans_image + " BLOB )";
    public final String dropRemTrans = " DROP TABLE IF EXISTS " + tbname_remittance_trans;


    //booking table
    public final String tbname_booking = "gpx_booking";
    public final String book_id = "id";
    public final String book_transaction_no = "transaction_no";
    public final String book_reservation_no = "reservation_no";
    public final String book_customer = "customer";
    public final String book_book_date = "book_date";
    public final String book_schedule_date = "schedule_date";
    public final String book_assigned_to = "assigned_to";
    public final String book_booking_status = "booking_status";
    public final String book_type = "booking_type";
    public final String book_createdby = "createdby";
    public final String book_upds = "upload_status";

    //create all table in booking
    public final String createTBBooking = " CREATE TABLE " + tbname_booking + "("
            + book_id + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + book_transaction_no + " TEXT UNIQUE, "
            + book_reservation_no + " TEXT, "
            + book_customer + " TEXT, "
            + book_book_date + " TEXT, "
            + book_schedule_date + " TEXT, "
            + book_assigned_to + " TEXT, "
            + book_booking_status + " TEXT, "
            + book_type + " TEXT, "
            + book_upds + " TEXT, "
            + book_createdby + " TEXT )";
    public final String dropBooking = " DROP TABLE IF EXISTS " + tbname_booking;

    //create all table booking consignee

    public final String tbname_booking_consignee_box = "gpx_booking_consignee_box";
    public final String book_con_box_id = "id";
    public final String book_con_box_account_no = "consignee";
    public final String book_con_boxtype = "boxtype";
    public final String book_con_transaction_no = "transaction_no";
    public final String book_con_origin = "source_id";
    public final String book_con_destination = "destination_id";
    public final String book_con_box_number = "box_number";
    public final String book_con_stat = "status";
    public final String book_con_hardport = "hardport";
    public final String book_con_boxcont = "contents";

    //create all table booking consignee box
    public final String createTBBooking_Consignee_Box = " CREATE TABLE " + tbname_booking_consignee_box + "("
            + book_con_box_id + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + book_con_box_account_no + " TEXT, "
            + book_con_boxtype + " TEXT, "
            + book_con_origin + " TEXT, "
            + book_con_destination + " TEXT, "
            + book_con_transaction_no + " TEXT, "
            + book_con_box_number + " TEXT UNIQUE, "
            + book_con_hardport + " TEXT, "
            + book_con_boxcont + " TEXT, "
            + book_con_stat + " TEXT )";
    public final String dropBooking_consignee_box = " DROP TABLE IF EXISTS " + tbname_booking_consignee_box;


    public final String tbname_incident = "gpx_incident";
    public final String inc_id = "id";
    public final String inc_module = "module";
    public final String inc_boxnum = "box_number";
    public final String inc_type = "incident_type";
    public final String inc_reason = "reason";
    public final String inc_transnum = "transaction_no";
    public final String inc_createdby = "createdby";
    public final String inc_createddate = "creatdedate";
    public final String inc_stat = "status";
    public final String inc_upds = "upload_status";

    public final String createTBincident = " CREATE TABLE " + tbname_incident + "("
            + inc_id + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + inc_module + " TEXT, "
            + inc_type + " TEXT, "
            + inc_boxnum + " TEXT, "
            + inc_reason + " TEXT, "
            + inc_transnum + " TEXT, "
            + inc_createdby + " TEXT, "
            + inc_createddate + " TEXT, "
            + inc_upds + " TEXT, "
            + inc_stat + " TEXT )";
    public final String dropinc = " DROP TABLE IF EXISTS " + tbname_incident;

    public final String tbname_incimages = "gpx_incident_images";
    public final String inc_img_id = "id";
    public final String inc_img_transaction_no = "transaction_no";
    public final String inc_img_imageblob = "image";

    public final String createTBincident_images = " CREATE TABLE " + tbname_incimages + "("
            + inc_img_id + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + inc_img_transaction_no + " TEXT, "
            + inc_img_imageblob + " BLOB )";
    public final String dropincimg = " DROP TABLE IF EXISTS " + tbname_incimages;

    //checker acceptance
    public final String tbname_check_acceptance = "gpx_acceptance";
    public final String accept_id = "id";
    public final String accept_transactionid = "transaction_no";
    public final String accept_drivername = "driver_id";
    public final String accept_warehouseid = "warehouse_id";
    public final String accept_container = "truck_no";
    public final String accept_date = "createddate";
    public final String accept_createdby = "createdby";
    public final String accept_uploadstat = "status";
    public final String accept_upds = "upload_status";
    public final String createTBacceptance = " CREATE TABLE " + tbname_check_acceptance + "("
            + accept_id + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + accept_transactionid + " TEXT, "
            + accept_drivername + " TEXT, "
            + accept_warehouseid + " TEXT, "
            + accept_container + " TEXT, "
            + accept_date + " TEXT, "
            + accept_createdby + " TEXT, "
            + accept_upds + " TEXT, "
            + accept_uploadstat + " TEXT )";
    public final String dropAcceptance = " DROP TABLE IF EXISTS " + tbname_check_acceptance;

    //acceptance boxes
    public final String tbname_accept_boxes = "gpx_acceptance_boxes";
    public final String acc_box_id = "id";
    public final String acc_box_transactionno = "transaction_no";
    public final String acc_box_boxnumber = "boxnumber";
    public final String acc_box_boxtype = "boxtype_id";
    public final String acc_box_stat = "status";
    public final String createAcceptanceBoxes = " CREATE TABLE " + tbname_accept_boxes + "("
            + acc_box_id + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + acc_box_transactionno + " TEXT, "
            + acc_box_boxtype + " TEXT, "
            + acc_box_boxnumber + " TEXT, "
            + acc_box_stat + " TEXT )";
    public final String dropAcceptanceBox = " DROP TABLE IF EXISTS " + tbname_accept_boxes;

    //transactions table
    public final String tbname_transactions = "gpx_mobile_transactions";
    public final String trans_id = "id";
    public final String trans_type = "transaction_module";
    public final String trans_user = "user_id";
    public final String trans_information = "information";
    public final String trans_date = "transaction_date";
    public final String trans_time = "transaction_time";
    public final String createTBtransactions = " CREATE TABLE " + tbname_transactions + "("
            + trans_id + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + trans_type + " TEXT, "
            + trans_user + " TEXT, "
            + trans_information + " TEXT, "
            + trans_date + " TEXT, "
            + trans_time + " TEXT )";
    public final String droptransactions = " DROP TABLE IF EXISTS " + tbname_transactions;

    //barcode series table
    public final String tbname_barcode = "gpx_barcode_series";
    public final String barcode_id = "id";
    public final String barcode_boxtype = "boxtype_id";
    public final String barcode_quantity = "quantity";
    public final String barcode_series_start = "series_start";
    public final String barcode_series_end = "series_end";
    public final String createTBBarcode = " CREATE TABLE " + tbname_barcode + "("
            + barcode_id + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + barcode_boxtype + " TEXT, "
            + barcode_quantity + " TEXT, "
            + barcode_series_start + " TEXT, "
            + barcode_series_end + " TEXT )";
    public final String dropseries = " DROP TABLE IF EXISTS " + tbname_barcode;

    //all employee table
    public final String tbname_employee = "gpx_employee";
    public final String emp_id = "id";
    public final String emp_first = "firstname";
    public final String emp_mid = "middlename";
    public final String emp_last = "lastname";
    public final String emp_mail = "email";
    public final String emp_mobile = "mobile";
    public final String emp_phone = "phone";
    public final String emp_gender = "gender";
    public final String emp_birth = "birthdate";
    public final String emp_post = "position";
    public final String emp_house = "house_number_street";
    public final String emp_brgy = "barangay";
    public final String emp_city = "city";
    public final String emp_branch = "branch";
    public String createEmpTb = " CREATE TABLE " + tbname_employee + "("
            + emp_id + " INTEGER PRIMARY KEY UNIQUE,"
            + emp_first + " TEXT, "
            + emp_mid + " TEXT, "
            + emp_last + " TEXT, "
            + emp_mail + " TEXT, "
            + emp_mobile + " TEXT, "
            + emp_phone + " TEXT, "
            + emp_gender + " TEXT, "
            + emp_birth + " TEXT, "
            + emp_post + " TEXT, "
            + emp_house + " TEXT, "
            + emp_brgy + " TEXT, "
            + emp_city + " TEXT, "
            + emp_branch+ " INTEGER )";
    public String dropemp = "DROP TABLE IF EXISTS " + tbname_employee;

    //table delivery
    public final String tbname_delivery = "gpx_delivery";
    public final String del_id = "id";
    public final String del_customer = "customer_account_no";
    public final String del_booking_no = "transaction_no";
    public final String del_createddate = "createddate";
    public final String del_sign = "signature";
    public final String del_receivedby = "receivedby";
    public final String del_relationship = "relationship";
    public final String del_receiverrate = "satisfaction_rate";
    public final String del_notes = "notes";
    public final String del_createdby = "createdby";
    public final String del_upds = "upload_status";
    public String createDel = " CREATE TABLE " + tbname_delivery + "("
            + del_id + " TEXT PRIMARY KEY UNIQUE, "
            + del_customer + " TEXT, "
            + del_booking_no + " TEXT, "
            + del_createddate + " TEXT, "
            + del_receiverrate + " TEXT, "
            + del_notes + " TEXT, "
            + del_sign + " BLOB, "
            + del_receivedby + " TEXT, "
            + del_relationship + " TEXT, "
            + del_upds + " TEXT, "
            + del_createdby + " TEXT ) ";
    public String dropDel = " DROP TABLE IF EXISTS " + tbname_delivery;

    //table delivery boxes
    //for delivery inventory and status
    public final String tbname_delivery_box = "gpx_delivery_box";
    public final String del_box_id = "id";
    public final String del_box_boxnumber = "box_number";
    public final String del_box_bookid = "booking_id";
    public final String del_box_receiver = "receiver";
    public final String del_box_origin = "origin_id";
    public final String del_box_destination = "destination_id";
    public final String del_box_deliveryid = "delivery_id";
    public final String del_box_status = "status";
    public final String del_box_substatus = "sub_status";
    public final String del_box_crdate = "createddate";
    public final String del_box_remarks = "remarks";
    public String createdelBox = " CREATE TABLE " + tbname_delivery_box + "("
            + del_box_id + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + del_box_boxnumber + " TEXT UNIQUE, "
            + del_box_deliveryid + " TEXT, "
            + del_box_receiver + " TEXT, "
            + del_box_origin + " TEXT, "
            + del_box_destination + " TEXT, "
            + del_box_bookid + " TEXT, "
            + del_box_crdate + " TEXT, "
            + del_box_status + " TEXT, "
            + del_box_substatus + " TEXT ) ";
    public String dropdelBox = " DROP TABLE IF EXISTS "+tbname_delivery_box;

    //salesdriverinventory name
    public final String tbname_driver_inventory = "gpx_salesdriver_inventory";
    public final String sdinv_id = "id";
    public final String sdinv_boxtype = "boxtype";
    public final String sdinv_boxnumber = "boxnumber";
    public final String sdinv_boxtype_fillempty = "type";
    public final String sdinv_stat = "box_status";
    public String createsdinv = " CREATE TABLE " + tbname_driver_inventory + "("
            + sdinv_id + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + sdinv_boxtype + " TEXT, "
            + sdinv_boxnumber + " TEXT UNIQUE, "
            + sdinv_boxtype_fillempty + " TEXT, "
            + sdinv_stat + " TEXT ) ";
    public String dropsdinv = "DROP TABLE IF EXISTS " + tbname_driver_inventory;

    //Unload
    public final String tb_unload = "gpx_unloading";
    public final String unload_id = "id";
    public final String unload_date = "unload_date";
    public final String unload_forward = "forwarder_name";
    public final String unload_con_number = "container_no";
    public final String unload_timestart = "time_start";
    public final String unload_timeend = "time_end";
    public final String unload_plateno = "plate_no";
    public final String unload_driver = "driver_name";
    public final String unload_con_by = "createdby";
    public final String unload_eta = "arrival_time";
    public final String unload_stat = "status";
    public final String unload_upds = "upload_status";
    public String createUnload = "CREATE TABLE " + tb_unload + "("
            + unload_id + " TEXT PRIMARY KEY UNIQUE, "
            + unload_date + " DATE, "
            + unload_forward + " TEXT, "
            + unload_con_number + " TEXT, "
            + unload_timestart + " TEXT, "
            + unload_timeend + " TEXT, "
            + unload_plateno + " TEXT, "
            + unload_driver + " TEXT, "
            + unload_eta + " TEXT, "
            + unload_con_by + " TEXT,"
            + unload_upds + " TEXT,"
            + unload_stat + " TEXT )";
    public String dropUnload = "DROP TABLE IF EXISTS " + tb_unload;

    public final String tb_unloadbox = "gpx_unloading_box";
    public final String unloadbox_id = "loading_id";
    public final String unloadbox_trans = "transaction_no";
    public final String unload_boxnum = "box_num";
    public final String unload_box_stat = "status";
    public String createUnloadbox = "CREATE TABLE " + tb_unloadbox + "("
            + unloadbox_id + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + unloadbox_trans + " TEXT, "
            + unload_boxnum + " TEXT UNIQUE,"
            + unload_box_stat + " TEXT ) ";
    public String dropUnloadbox = "DROP TABLE IF EXISTS " + tb_unloadbox;

    //allowanse disbursed
    public final String tbname_allowance = "gpx_allowance";
    public String all_id = "id";
    public String all_amount = "amount";
    public String all_createdby = "createdby";
    public String all_createddate = "createddate";
    public String all_stat = "status";
    public String createAllowance = " CREATE TABLE "+tbname_allowance+"("
            + all_id+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + all_amount+ " TEXT, "
            + all_createdby+ " TEXT, "
            + all_createddate+ " TEXT, "
            + all_stat+ " TEXT )";
    public String dropallowance = " DROP TABLE IF EXISTS "+tbname_allowance;

    //checker inventory
    //types (1) = with bnumber
    public final String tbname_checker_inventory = "gpx_checker_inventory";
    public final String chinv_id = "id";
    public final String chinv_boxtype = "boxtype";
    public final String chinv_boxnumber = "boxnumber";
    public final String chinv_boxtype_fillempty = "type";
    public final String chinv_stat = "box_status";
    public String createchinv = " CREATE TABLE " + tbname_checker_inventory + "("
            + chinv_id + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + chinv_boxtype + " TEXT, "
            + chinv_boxnumber + " TEXT UNIQUE, "
            + chinv_boxtype_fillempty + " TEXT, "
            + chinv_stat + " TEXT ) ";
    public String dropchinv = "DROP TABLE IF EXISTS " + tbname_checker_inventory;

    //partner portal inventory
    public final String tbname_partner_inventory = "gpx_partner_inventory";
    public final String partinv_id = "id";
    public final String partinv_boxtype = "boxtype";
    public final String partinv_boxnumber = "boxnumber";
    public final String partinv_boxtype_fillempty = "type";
    public final String partinv_stat = "box_status";
    public String createPartInv = " CREATE TABLE " + tbname_partner_inventory + "("
            + partinv_id + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + partinv_boxtype + " TEXT, "
            + partinv_boxnumber + " TEXT UNIQUE, "
            + partinv_boxtype_fillempty + " TEXT, "
            + partinv_stat + " TEXT ) ";
    public String droppart_inv = "DROP TABLE IF EXISTS " + tbname_partner_inventory;

    //remitances amount
    public final String tbname_remitttances_amount = "gpx_rmeittance_amount";
    public final String rem_amount_id = "id";
    public final String rem_amount_amount = "remittance_amount";
    public final String rem_amount_transnum = "transaction_number";
    public final String rem_amount_stat = "status";
    public final String rem_amount_by = "createdby";
    public final String rem_amount_date = "createddate";
    public String createRemitAmount = " CREATE TABLE " + tbname_remitttances_amount + "("
            + rem_amount_id + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + rem_amount_amount + " TEXT, "
            + rem_amount_transnum + " TEXT, "
            + rem_amount_stat + " TEXT, "
            + rem_amount_date + " TEXT, "
            + rem_amount_by + " TEXT ) ";
    public String dropremitAmount = "DROP TABLE IF EXISTS " + tbname_remitttances_amount;

    //branch table
    public final String tbname_branch = "gpx_branch";
    public final String branch_id = "id";
    public final String branch_name = "name";
    public final String branch_address = "address";
    public final String branch_type = "type";
    public final String branch_recordstat = "recordstatus";
    public String createBranch = " CREATE TABLE " + tbname_branch + "("
            + branch_id + " INTEGER PRIMARY KEY UNIQUE,"
            + branch_name + " TEXT, "
            + branch_address + " TEXT,"
            + branch_type + " TEXT,"
            + branch_recordstat+ " INTEGER )";
    public String dropbranch = "DROP TABLE IF EXISTS " + tbname_branch;

    //discount table
    public final String tbname_discount = "gpx_discount";
    public final String disc_id = "id";
    public final String disc_trans_no = "transaction_no";
    public final String disc_discount = "discount";
    public final String disc_remarks = "remarks";
    public String createDiscount = " CREATE TABLE "+tbname_discount+"("
            +disc_id+" INTEGER PRIMARY KEY AUTOINCREMENT,"
            +disc_trans_no+" TEXT,"
            +disc_discount+" TEXT,"
            +disc_remarks+" TEXT )";
    public String dropDiscount = " DROP TABLE IF EXISTS "+tbname_discount;


    public GenDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(createTableBox);
        db.execSQL(createReserveboxtype);
        db.execSQL(createStats);
        db.execSQL(createReservation);
        db.execSQL(createCustomerTB);
        db.execSQL(createTempDist);
        db.execSQL(createDBoxTemp);
        db.execSQL(createTBPayment);
        db.execSQL(createTableAcceptance);
        db.execSQL(createReserveboxtypeBoxnumber);
        db.execSQL(createEmpTb);
        db.execSQL(createchinv);
        db.execSQL(createPartInv);
        db.execSQL(createBranch);
        db.execSQL(createDiscount);

        //temporaries
        db.execSQL(createTempReserve);
        db.execSQL(createTempReserveBtype);

        //remittance
        db.execSQL(createTBremittance);
        db.execSQL(createRemTrans);
        db.execSQL(createAllowance);

        //booking
        db.execSQL(createTBBooking);
        db.execSQL(createTBBooking_Consignee_Box);

        //incident
        db.execSQL(createTBincident);
        db.execSQL(createTBincident_images);

        // acceptance
        db.execSQL(createTBacceptance);
        db.execSQL(createAcceptanceBoxes);

        //transactions
        db.execSQL(createTBtransactions);

        //barcode sereis
        db.execSQL(createTBBarcode);
        db.execSQL(createDel);
        db.execSQL(createdelBox);
        db.execSQL(createsdinv);
        db.execSQL(createUnload);
        db.execSQL(createUnloadbox);
        db.execSQL(createRemitAmount);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(dropbox);
        db.execSQL(dropStatTB);
        db.execSQL(dropReservation);
        db.execSQL(dropReserveBoxtype);
        db.execSQL(dropCustomersTB);
        db.execSQL(dropTempDist);
        db.execSQL(dropDBoxTemp);
        db.execSQL(dropTBpayment);
        db.execSQL(dropAccept);
        db.execSQL(dropemp);
        db.execSQL(dropReserveBoxtypeBoxnumber);
        db.execSQL(droppart_inv);
        db.execSQL(dropchinv);
        db.execSQL(dropbranch);
        db.execSQL(dropDiscount);

        //temporaries
        db.execSQL(dropTempReserve);
        db.execSQL(dropTempReserveBtype);

        //remittance
        db.execSQL(dropRemittance);
        db.execSQL(dropRemTrans);
        db.execSQL(dropallowance);

        //booking
        db.execSQL(dropBooking);
        db.execSQL(dropBooking_consignee_box);

        //incident
        db.execSQL(dropinc);
        db.execSQL(dropincimg);

        //acceptance
        db.execSQL(dropAcceptance);
        db.execSQL(dropAcceptanceBox);

        //transactions
        db.execSQL(droptransactions);
        db.execSQL(dropseries);
        db.execSQL(dropDel);
        db.execSQL(dropdelBox);
        db.execSQL(dropsdinv);
        db.execSQL(dropUnload);
        db.execSQL(dropUnloadbox);
        db.execSQL(dropremitAmount);

        onCreate(db);

    }


    public int countcustomers() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(" SELECT * FROM " + tbname_customers, null);
        int total = c.getCount();
        return total;
    }

    public void addCustomer(String accntnum, String sender, String fname, String mname, String lname, String mobile,
                            String anmobile, String thrmobile, String phone,
                            String email, String gender, String bday, String prov, String city, String postal, String brangay,
                            String unit, String type, String by, String stat, String fullname, String updstat) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(cust_accountnumber, accntnum);
        cv.put(cust_firstname, fname);
        cv.put(cust_middlename, mname);
        cv.put(cust_lastname, lname);
        cv.put(cust_mobile, mobile);
        cv.put(cust_secmobile, anmobile);
        cv.put(cust_thirdmobile, thrmobile);
        cv.put(cust_phonenum, phone);
        cv.put(cust_emailadd, email);
        cv.put(cust_gender, gender);
        cv.put(cust_birthdate, bday);
        cv.put(cust_prov, prov);
        cv.put(cust_city, city);
        cv.put(cust_postal, postal);
        cv.put(cust_barangay, brangay);
        cv.put(cust_unit, unit);
        cv.put(cust_type, type);
        cv.put(cust_createdby, by);
        cv.put(cust_recordstatus, stat);
        cv.put(cust_fullname, fullname);
        cv.put(cust_senderaccount, sender);
        cv.put(cust_uploadstat, updstat);

        db.insert(tbname_customers, null, cv);
        db.close();

    }

    public String[] getAccountNumber(){
        Cursor cursor = getReadableDatabase().rawQuery("SELECT " + cust_accountnumber + " FROM " + tbname_customers
                + " WHERE " + cust_type + " = 'customer'", null);
        cursor.moveToFirst();
        ArrayList<String> numbers = new ArrayList<String>();
        while (!cursor.isAfterLast()) {
            numbers.add(cursor.getString(cursor.getColumnIndex(cust_accountnumber)));
            cursor.moveToNext();
        }
        cursor.close();
        return numbers.toArray(new String[numbers.size()]);
    }

    public String[] getFullnames() {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT " + cust_fullname + " FROM " + tbname_customers
                + " WHERE " + cust_type + " = 'customer'", null);
        cursor.moveToFirst();
        ArrayList<String> numbers = new ArrayList<String>();
        while (!cursor.isAfterLast()) {
            numbers.add(cursor.getString(cursor.getColumnIndex(cust_fullname)));
            cursor.moveToNext();
        }
        cursor.close();
        return numbers.toArray(new String[numbers.size()]);
    }

    public boolean getCustomerInfo(String id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor c = db.rawQuery(" SELECT * FROM " + tbname_customers + " WHERE " + cust_fullname + " = '" + id + "'",
                null);

        if (c.getCount() != 0) {
            return true;
        } else {
            return false;
        }

    }

    public boolean checkCustomer(String accntnumber) {
        // array of columns to fetch
        String[] columns = {
                cust_accountnumber,
                cust_firstname,
                cust_middlename,
                cust_lastname,
                cust_mobile,
                cust_phonenum,
                cust_emailadd,
                cust_gender,
                cust_birthdate,
                cust_barangay,
                cust_city
        };
        SQLiteDatabase db = this.getReadableDatabase();
        // selection criteria
        String selection = cust_accountnumber + " = ?";

        // selection arguments
        String[] selectionArgs = {accntnumber};

        Cursor cursor = db.query(tbname_customers, //Table to query
                columns,                    //columns to return
                selection,                  //columns for the WHERE clause
                selectionArgs,              //The values for the WHERE clause
                null,                       //group the rows
                null,                       //filter by row groups
                null);                      //The sort order

        int cursorCount = cursor.getCount();

        if (cursorCount > 0) {
            return true;
        }

        cursor.close();
        db.close();
        return false;

    }

    public void addGPXReservation(String reservenumber, String customerid, String createdby, String createddate,
                                  String assignedto, String status, String upload_stat) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(reserve_reservation_no, reservenumber);
        cv.put(reserve_customer_id, customerid);
        cv.put(reserve_createdby, createdby);
        cv.put(reserve_createddate, createddate);
        cv.put(reserve_assigned_to, assignedto);
        cv.put(reserve_status, status);
        cv.put(reserve_upload_status, upload_stat);

        db.insert(tbname_reservation, null, cv);
        Log.e("reservation",reservenumber);
        db.close();
    }

    public boolean addGPXReservationBoxtype(String bid, String boxtype, String quantity, String deposit,
                                            String reserveid) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        //cv.put(res_btype_id, id);
        cv.put(res_boxtype, boxtype);
        cv.put(res_btype_boxtype_id, bid);
        cv.put(res_quantity, quantity);
        cv.put(res_deposit, deposit);
        cv.put(res_reservation_id, reserveid);
        db.insert(tbname_reservation_boxtype, null, cv);
        Log.e("added_new_res"," boxtype: "+boxtype+", resve: "+reserveid);
        db.close();

        return true;

    }

    public void updateGPXReservationBoxtype(String boxtype, String quantity, String deposit,
                                            String reserveid) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(res_boxtype, boxtype);
        cv.put(res_quantity, quantity);
        cv.put(res_deposit, deposit);
        cv.put(res_reservation_id, reserveid);

        db.update(tbname_reservation_boxtype, cv, res_reservation_id+" = "+reserveid, null);
        db.close();
    }

    public void updateGPXReservation(String reservenumber, String customerid, String createdby, String createddate,
                                     String assignedto, String status, String upload_stat) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(reserve_reservation_no, reservenumber);
        cv.put(reserve_customer_id, customerid);
        cv.put(reserve_createdby, createdby);
        cv.put(reserve_createddate, createddate);
        cv.put(reserve_assigned_to, assignedto);
        cv.put(reserve_status, status);
        cv.put(reserve_upload_status, upload_stat);

        db.update(tbname_reservation, cv, reserve_reservation_no + " = '" + reservenumber + "'", null);
        db.close();

    }

    public ArrayList<ThreeWayHolder> getReservelist(String stat) {
        ArrayList<ThreeWayHolder> results = new ArrayList<ThreeWayHolder>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor res = db.rawQuery(" SELECT * FROM "
                + tbname_reservation +" LEFT JOIN "+tbname_customers
                +" ON "+tbname_reservation+"."+reserve_customer_id+" = "
                +tbname_customers+"."+cust_accountnumber
                + " WHERE " + reserve_status + " = '" + stat + "'"
                +" ORDER BY "+reserve_createddate+" ASC ", null);
        res.moveToFirst();
        while (!res.isAfterLast()) {
            String dataid = res.getString(res.getColumnIndex(reserve_id));
            String topitem = res.getString(res.getColumnIndex(reserve_reservation_no));
            String account = res.getString(res.getColumnIndex(cust_fullname));
            int q = 0, bx = 0;
            Cursor f = db.rawQuery(" SELECT SUM("+res_quantity+") FROM "+tbname_reservation_boxtype
                    +" WHERE "+res_reservation_id+" = '"+topitem+"' GROUP BY "+res_reservation_id, null);
            if(f.moveToFirst()){
                q = f.getInt(0);
            }
            Cursor cx = db.rawQuery(" SELECT * FROM "+tbname_reservation_boxtype_boxnumber
                    +" WHERE "+res_btype_bnum_reservation_id+" = '"+topitem+"' AND "+res_btype_bnum_box_number+" != 'NULL'", null);
            if(cx.moveToNext()){
                bx = cx.getCount();
            }
            int xy = (q - bx);

            if (xy != 0) {
                ThreeWayHolder list = new ThreeWayHolder(dataid, topitem, account, "" + xy);
                results.add(list);
            }
            res.moveToNext();
        }
        db.close();
        res.close();
        return results;
    }

    public ArrayList<ListItem> getBoxes(String id) {
        ArrayList<ListItem> results = new ArrayList<ListItem>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor res = db.rawQuery(" SELECT * FROM " + tbname_reservation_boxtype
                + " WHERE " + res_reservation_id + " = '" + id + "'", null);
        res.moveToFirst();
        while (!res.isAfterLast()) {

            String dataid = res.getString(res.getColumnIndex(res_btype_id));
            String topitem = res.getString(res.getColumnIndex(res_boxtype));
            String account = res.getString(res.getColumnIndex(res_quantity));

            ListItem list = new ListItem(dataid, topitem, account,"");
            results.add(list);

            res.moveToNext();
        }

        db.close();
        res.close();
        // Add some more dummy data for testing
        return results;
    }

    public ArrayList<ListItem> getTempBox(String trans) {
        ArrayList<ListItem> results = new ArrayList<ListItem>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor res = db.rawQuery(" SELECT "+tbname_boxes+"."+box_name+", "
                +tbname_boxes+"."+box_id+", COUNT("+tbname_tempboxes+"."+dboxtemp_boxid+"),"
                +tbname_tempboxes+".*"
                +" FROM " + tbname_tempboxes
                +" LEFT JOIN "+tb_acceptance+" ON "+tbname_tempboxes+"."+dboxtemp_invid
                +" = "+tb_acceptance+"."+acc_id+" LEFT JOIN "+tbname_boxes+" ON "
                +tbname_tempboxes+"."+dboxtemp_boxid+" = "+tbname_boxes+"."+box_id
                + " WHERE " + dboxtemp_distributionid + " = '" + trans + "' AND "
                +dboxtemp_stat+" = '0' GROUP BY "+tbname_tempboxes+"."+dboxtemp_boxid, null);
        res.moveToFirst();
        while (!res.isAfterLast()) {
            String topitem = res.getString(0);
            String id = res.getString(res.getColumnIndex(dboxtemp_id));
            String invid = res.getString(res.getColumnIndex(dboxtemp_invid));
            String subs = res.getString(2);
            ListItem list = new ListItem(id, topitem, subs, invid);
            results.add(list);
            res.moveToNext();
        }
        // Add some more dummy data for testing
        return results;
    }

    public void addTempBoxDist(String id, String btype, String invid, String boxnumber,
                               String stat){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(dboxtemp_distributionid, id);
        values.put(dboxtemp_invid, invid);
        values.put(dboxtemp_boxid, btype);
        values.put(dboxtemp_boxnumber, boxnumber);
        values.put(dboxtemp_stat, stat);
        db.insert(tbname_tempboxes, null, values);
        Log.e("dbox", ""+invid);
        db.close();

    }

    public boolean addDistribution(String trans, String type, String typename,
                                   String truck, String rem, String status, String upstat,
                                   String date, String by, int sac, byte[] sign) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(temp_transactionnumber, trans);
        cv.put(temp_type, type);
        cv.put(temp_typename, typename);
        cv.put(temp_trucknum, truck);
        cv.put(temp_remarks, rem);
        cv.put(temp_status, status);
        cv.put(temp_uploadstat, upstat);
        cv.put(temp_createdate, date);
        cv.put(temp_createby, by);
        cv.put(temp_acceptstat, sac);
        cv.put(temp_acceptsign, sign);

        db.insert(tbname_tempDist, null, cv);
        Log.e("distribution",trans);
        db.close();

        return true;
    }

    public void deleteTempBox(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(tbname_tempboxes, dboxtemp_invid + " = " + id, null);
        db.close();
    }

    public void updateTempBoxes(String id, String stat) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(dboxtemp_stat, stat);
        db.update(tbname_tempboxes, cv, dboxtemp_id + " = " + id, null);
        db.close();
    }

    public void deleteReservationBoxes(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(tbname_reservation_boxtype, res_reservation_id + " = " + id, null);
        db.close();

    }

    public void deleteReservationBoxesNumbers(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(tbname_reservation_boxtype_boxnumber, res_btype_bnum_reservation_id + " = " + id, null);
        db.close();

    }

    public void deleteReservationID(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(tbname_reservation, reserve_reservation_no + " = " + id, null);
        db.close();

    }

    public ArrayList<ListItem> getBoxReservations(String id) {
        ArrayList<ListItem> results = new ArrayList<ListItem>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor res = db.rawQuery(" SELECT "+res_quantity+","+res_deposit+","+res_btype_id+","+res_boxtype+", SUM("+res_quantity+")"+" FROM " + tbname_reservation_boxtype
                +" LEFT JOIN " + tbname_boxes +" ON " + tbname_boxes + "." +box_name+" = "+tbname_reservation_boxtype
                +"."+res_boxtype+ " WHERE " + res_reservation_id + " = '" + id + "' GROUP BY "+res_boxtype, null);
        res.moveToFirst();
        while (!res.isAfterLast()) {
            String dataid = res.getString(res.getColumnIndex(res_btype_id));
            String topitem = res.getString(res.getColumnIndex(res_boxtype));
            int subitem = 0;
            Cursor f = db.rawQuery(" SELECT * FROM "+tbname_reservation_boxtype_boxnumber+
                    " WHERE "+res_btype_bnum_reservation_id+" = '"+id+"' AND "+res_btype_bnum_stat+" = '2'" +
                    " AND "+res_btype_bnum_box_number+" != '1' AND "+res_btype_bnum_boxtype+" = '"+topitem+"'", null);
            if (f.getCount() != 0){
                subitem = ((res.getInt(4)) - (f.getCount()));
            }else{
                subitem = res.getInt(4);
            }

            float depositamount = Float.parseFloat(res.getString(res.getColumnIndex(res_deposit)));
            ListItem list = new ListItem(dataid, topitem, ""+subitem, "" + depositamount);
            results.add(list);

            res.moveToNext();
        }
        // Add some more dummy data for testing
        return results;
    }

    public ArrayList<ListItem> getCompletedReservationBoxnumber(String id) {
        ArrayList<ListItem> results = new ArrayList<ListItem>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor res = db.rawQuery(" SELECT * FROM " + tbname_reservation_boxtype_boxnumber+" LEFT JOIN "
                +tbname_reservation+" ON "+tbname_reservation+"."+reserve_reservation_no+" = "+tbname_reservation_boxtype_boxnumber
                +"."+res_btype_bnum_reservation_id+" WHERE "+tbname_reservation_boxtype_boxnumber+"."+res_btype_bnum_reservation_id+" = '"+id+"' AND "
                +tbname_reservation_boxtype_boxnumber+"."+res_btype_bnum_stat+" = '2'", null);
        res.moveToFirst();
        while (!res.isAfterLast()) {
            String dataid = res.getString(res.getColumnIndex(res_btype_bnum_id));
            String topitem = res.getString(res.getColumnIndex(res_btype_bnum_boxtype));
            String subitem = res.getString(res.getColumnIndex(res_btype_bnum_box_number));
            float depositamount = Float.parseFloat(res.getString(res.getColumnIndex(res_btype_bnum_box_depoprice)));

            ListItem list = new ListItem(dataid, topitem, subitem, "" + depositamount);
            results.add(list);

            res.moveToNext();
        }
        // Add some more dummy data for testing
        return results;
    }

    public ArrayList<ListItem> getBoxReservationBoxnumber(String id) {
        ArrayList<ListItem> results = new ArrayList<ListItem>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor res = db.rawQuery(" SELECT * FROM " + tbname_reservation_boxtype_boxnumber
                + " WHERE " + res_btype_bnum_reservation_id + " = '" + id + "' AND "+res_btype_bnum_stat+" = '1'", null);
        res.moveToFirst();
        while (!res.isAfterLast()) {
            String dataid = res.getString(res.getColumnIndex(res_btype_bnum_id));
            String topitem = res.getString(res.getColumnIndex(res_btype_bnum_boxtype));
            String subitem = res.getString(res.getColumnIndex(res_btype_bnum_box_number));
            String amount = res.getString(res.getColumnIndex(res_btype_bnum_box_depoprice));

            ListItem list = new ListItem(dataid, topitem, subitem, amount);
            results.add(list);
            res.moveToNext();
        }
        // Add some more dummy data for testing
        return results;
    }

    public ArrayList<ThreeWayHolder> getBoxReservationBoxnumberComplete(String x) {
        ArrayList<ThreeWayHolder> results = new ArrayList<ThreeWayHolder>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor res = db.rawQuery(" SELECT * FROM " + tbname_reservation
                +" LEFT JOIN "+tbname_customers+" ON "+
                tbname_customers+"."+cust_accountnumber+" = "
                +tbname_reservation+"."+reserve_customer_id+
                " LEFT JOIN "+tbname_reservation_boxtype_boxnumber
                +" ON "+tbname_reservation+"."+reserve_reservation_no
                +" = "+tbname_reservation_boxtype_boxnumber+"."+res_btype_bnum_reservation_id
                + " WHERE "+tbname_reservation_boxtype_boxnumber+"."+res_btype_bnum_stat+" = '2' OR "
                +tbname_reservation+"."+reserve_status+ " = '2' AND "
                +tbname_reservation+"."+reserve_upload_status+ " = '2' AND "
                +tbname_reservation+"."+reserve_createdby+" = '"+x+"'"
                +" GROUP BY "+tbname_reservation+"."+reserve_reservation_no
                +" ORDER BY "+tbname_reservation+"."+reserve_createddate, null);
        res.moveToFirst();
        while (!res.isAfterLast()) {
            String dataid = res.getString(res.getColumnIndex(reserve_id));
            String topitem = res.getString(res.getColumnIndex(res_btype_bnum_reservation_id));
            String subitem = res.getString(res.getColumnIndex(cust_fullname));
            int q = 0;
            Cursor f = db.rawQuery(" SELECT * FROM "+tbname_reservation_boxtype_boxnumber+
                    " WHERE "+res_btype_bnum_reservation_id+" = '"+topitem+"' AND "+res_btype_bnum_stat+" = '2'", null);
            if(f.moveToFirst())
            {
                q = f.getCount();
            }
            ThreeWayHolder list = new ThreeWayHolder(dataid, topitem, subitem, ""+q);
            results.add(list);
            res.moveToNext();
        }
        // Add some more dummy data for testing
        return results;
    }

    public void addReservationBoxtypeBoxnumber(String boxtype, String boxnumber,String depo,
                                               String createddate, String reservationid, String stat) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(res_btype_bnum_boxtype, boxtype);
        cv.put(res_btype_bnum_box_number, boxnumber);
        cv.put(res_btype_bnum_box_depoprice, depo);
        cv.put(res_btype_bnum_createddate, createddate);
        cv.put(res_btype_bnum_reservation_id, reservationid);
        cv.put(res_btype_bnum_stat, stat);

        db.insert(tbname_reservation_boxtype_boxnumber, null, cv);
        Log.e("updated", "TYPE:"+boxtype+",BOXNUMBER:"+boxnumber+",RESERVATION:"+reservationid);

        db.close();

    }

    public void deleteReservationBtypeBoxnum(String res) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(tbname_reservation_boxtype_boxnumber, res_btype_bnum_reservation_id + " = '" + res+"'", null);
        db.close();

    }

    public void updReservationBoxtypeBoxnumber(String id, String boxtype, String boxnumber,String depo,
                                               String createddate, String reservationid, String stat) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(res_btype_bnum_boxtype, boxtype);
        cv.put(res_btype_bnum_box_number, boxnumber);
        cv.put(res_btype_bnum_box_depoprice, depo);
        cv.put(res_btype_bnum_createddate, createddate);
        cv.put(res_btype_bnum_reservation_id, reservationid);
        cv.put(res_btype_bnum_stat, stat);

        db.update(tbname_reservation_boxtype_boxnumber, cv, res_btype_bnum_id + " = " + id, null);
        //Log.e("updated", "ID:"+id+",TYPE:"+boxtype+",BOXNUMBER:"+boxnumber+",RESERVATION:"+reservationid);
        db.close();

    }

    public JSONArray getResultsArray(String id) {
        SQLiteDatabase myDataBase = this.getReadableDatabase();
        String searchQuery = " SELECT " + res_btype_bnum_boxtype + "," + res_btype_bnum_box_number + ","
                + res_btype_bnum_createddate + "," + res_btype_bnum_reservation_id+", "
                + res_btype_bnum_stat + " FROM " + tbname_reservation_boxtype_boxnumber +
                " WHERE " + res_btype_bnum_reservation_id + " = '" + id + "'";
        Cursor cursor = myDataBase.rawQuery(searchQuery, null);
        JSONArray resultSet = new JSONArray();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            int totalColumn = cursor.getColumnCount();
            JSONObject rowObject = new JSONObject();
            for (int i = 0; i < totalColumn; i++) {
                if (cursor.getColumnName(i) != null) {
                    try {
                        if (cursor.getString(i) != null) {
                            Log.d("TAG_NAME", cursor.getString(i));
                            rowObject.put(cursor.getColumnName(i), cursor.getString(i));
                        } else {
                            rowObject.put(cursor.getColumnName(i), "");
                        }
                    } catch (Exception e) {
                        Log.d("TAG_NAME", e.getMessage());
                    }
                }
            }
            resultSet.put(rowObject);
            cursor.moveToNext();
        }
        cursor.close();
        return resultSet;
    }

    public void updateReservationStatus(String id, String reservation_no,
                                        String customer_id, String createdby,
                                        String createddate, String assigned_to, String status, String upload_stat) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(reserve_reservation_no, reservation_no);
        cv.put(reserve_customer_id, customer_id);
        cv.put(reserve_createdby, createdby);
        cv.put(reserve_createddate, createddate);
        cv.put(reserve_assigned_to, assigned_to);
        cv.put(reserve_status, status);
        cv.put(reserve_upload_status, upload_stat);

        db.update(tbname_reservation, cv, reserve_reservation_no + " = '" + reservation_no + "'", null);
        db.close();

        Log.e("status update", status);
    }

    public void addReservationPayment(String orno, String bookingid, String reservationid, String term,
                                      String deposit, String totalamount, String createdby,
                                      String createddate) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(pay_or_no, orno);
        cv.put(pay_booking_id, bookingid);
        cv.put(pay_reservation_id, reservationid);
        cv.put(pay_paymentterm, term);
        cv.put(pay_deposit, deposit);
        cv.put(pay_total_amount, totalamount);
        cv.put(pay_createdby, createdby);
        cv.put(pay_createddate, createddate);

        db.insert(tbname_payment, null, cv);
        db.close();

        Log.e("addedpayment", createddate);
    }

    public JSONArray getResultsPayment(String id) {
        SQLiteDatabase myDataBase = this.getReadableDatabase();


        String searchQuery = "SELECT " + pay_or_no + "," + pay_booking_id + ","
                + pay_reservation_id + "," + pay_paymentterm + ","
                + pay_deposit + "," + pay_total_amount + ","
                + pay_createdby + "," + pay_createddate + " FROM " + tbname_payment + " WHERE " + pay_reservation_id + " = '" + id + "' GROUP BY "+pay_reservation_id;
        Cursor cursor = myDataBase.rawQuery(searchQuery, null);

        JSONArray resultSet = new JSONArray();

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {

            int totalColumn = cursor.getColumnCount();
            JSONObject rowObject = new JSONObject();

            for (int i = 0; i < totalColumn; i++) {
                if (cursor.getColumnName(i) != null) {

                    try {
                        if (cursor.getString(i) != null) {
                            Log.d("TAG_NAME", cursor.getString(i));
                            rowObject.put(cursor.getColumnName(i), cursor.getString(i));
                        } else {
                            rowObject.put(cursor.getColumnName(i), "");
                        }
                    } catch (Exception e) {
                        Log.d("TAG_NAME", e.getMessage());
                    }
                }

            }

            resultSet.put(rowObject);
            cursor.moveToNext();
        }

        cursor.close();
        // Log.e("result set", resultSet.toString());
        return resultSet;
    }

    public void updateReserveUploadStat(String id, String stat) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(reserve_upload_status, stat);

        String where = reserve_id + " = '" + id + "'";

        db.update(tbname_reservation, cv, where, null);
        db.close();
    }

    public void deleteTempBoxReserve(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(tbname_reservation_boxtype, res_btype_id + " = " + id, null);
        db.close();
    }

    public JSONArray getBoxtypes(String id) {

            SQLiteDatabase myDataBase = this.getReadableDatabase();
            String searchQuery = "SELECT " + res_btype_id + "," + res_btype_boxtype_id + ","
                    + res_boxtype + ","
                    + " SUM(" + res_quantity + ")," + "SUM(" + res_deposit + "),"
                    + res_reservation_id + " FROM " + tbname_reservation_boxtype + " WHERE " + res_reservation_id + " = '" + id + "'" +
                    " GROUP BY " + res_reservation_id + " AND " + res_btype_boxtype_id;
            Cursor c = myDataBase.rawQuery(searchQuery, null);

            JSONArray resultSet = new JSONArray();

            c.moveToFirst();
            while (!c.isAfterLast()) {
                try {

                    JSONObject json = new JSONObject();
                    String res_id = c.getString(0);
                    String boxtype_id = c.getString(1);
                    String boxtype = c.getString(2);
                    String quantity = c.getString(3);
                    String deposit = c.getString(4);
                    String reservation_no = c.getString(5);
                    json.put("reservation_boxtype_id", res_id);
                    json.put("boxtype_id", boxtype_id);
                    json.put("boxtype", boxtype);
                    json.put("quantity", quantity);
                    json.put("deposit", deposit);
                    json.put("reservation_no", reservation_no);

                    resultSet.put(json);
                }catch(Exception e){}
                c.moveToNext();
            }

            c.close();
            // Log.e("result set", resultSet.toString());
            return resultSet;
    }

    //acceptance
//    public ArrayList<ListItem> getDefaultBoxes(String name) {
//        ArrayList<ListItem> results = new ArrayList<ListItem>();
//        SQLiteDatabase db = this.getReadableDatabase();
//
//        Cursor res = db.rawQuery(" SELECT "+ tb_acceptance + "."+acc_id+
//                ", SUM("+ tb_acceptance + "." +acc_quantity+"), "
//                + tbname_boxes + "."+box_name+" FROM " + tb_acceptance + " LEFT JOIN " + tbname_boxes +
//                " ON " + tbname_boxes + "." + box_id + " = " + tb_acceptance + "." + acc_boxtype
//                + " WHERE " + tb_acceptance + "." +acc_status + " = '1' AND "
//                + tb_acceptance + "." +acc_name + " = '" + name + "'", null);
//        res.moveToFirst();
//        if (res.getCount() != 0){
//            while (!res.isAfterLast()) {
//                String dataid = res.getString(0);
//                String topitem = res.getString(2);
//                String subitem = res.getString(1);
//                if ((topitem != null) && (subitem != null)){
//                    ListItem list = new ListItem(dataid, topitem, subitem, "");
//                    results.add(list);
//                }
//                res.moveToNext();
//            }
//        }
//        // Add some more dummy data for testing
//        return results;
//    }

    public void addAcceptanceEmpty(String trans, String warehouseid, String name,String boxtypeid,
                                   String quantity,
                                   String createddate, String createdby, String stat, String upds) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues acc = new ContentValues();
        acc.put(acc_id, trans);
        acc.put(acc_warehouse_id, warehouseid);
        acc.put(acc_name, name);
        acc.put(acc_boxtype, boxtypeid);
        acc.put(acc_quantity, quantity);
        acc.put(acc_createddate, createddate);
        acc.put(acc_createdby, createdby);
        acc.put(acc_status, stat);
        acc.put(acc_upds, upds);

        db.insert(tb_acceptance, null, acc);
        Log.e(" acceptance", "trans: "+trans);
        db.close();
    }

//    public void deleteBAcc(String id) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        db.delete(tb_acceptance, acc_id + " = '" +id+"'", null);
//        db.close();
//    }

//    public ArrayList<ListItem> countBoxes(String warehouse, String user){
//        int totalq = 0;
//        ArrayList<ListItem> results = new ArrayList<>();
//        SQLiteDatabase db = this.getReadableDatabase();
//        Cursor c = db.rawQuery("SELECT * FROM "+tb_acceptance
//                +" WHERE "+acc_warehouse_id+" = '"
//                +warehouse+"' AND "+acc_createdby+" = '"+user+"' AND "+acc_quantity+" != '0'", null);
//        c.moveToFirst();
//        while (!c.isAfterLast()) {
//            String sub = c.getString(c.getColumnIndex(acc_id));
//            String co = c.getString(c.getColumnIndex(acc_quantity));
//            String topitem = c.getString(c.getColumnIndex(acc_boxtype));
//            String bname = "";
//            String y = " SELECT "+box_name+" FROM "+tbname_boxes
//                    +" WHERE "+box_id+" = '"+topitem+"'";
//            Cursor d = db.rawQuery(y, null);
//            if (d.moveToNext()){
//                bname = d.getString(d.getColumnIndex(box_name));
//            }
//            ListItem list = new ListItem(sub, bname, ""+co, "");
//            results.add(list);
//            c.moveToNext();
//        }
//        // Add some more dummy data for testing
//        return results;
//    }

    //booking transactions
    public void addBooking(String transno, String reserveno, String custno, String bookdate,
                           String bookstat, String type, String createdby, String upds) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(book_transaction_no, transno);
        cv.put(book_reservation_no, reserveno);
        cv.put(book_customer, custno);
        cv.put(book_book_date, bookdate);
        cv.put(book_booking_status, bookstat);
        cv.put(book_type, type);
        cv.put(book_createdby, createdby);
        cv.put(book_upds, upds);

        db.insert(tbname_booking, null, cv);
        db.close();
        Log.e("booking save", " saved "+transno);
    }

    public ArrayList<LinearItem> getAllBooking(String x) {
        ArrayList<LinearItem> results = new ArrayList<LinearItem>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor res = db.rawQuery(" SELECT * FROM " + tbname_booking
                + " WHERE "+book_createdby+" = '"+x+"' ORDER BY "+book_id+" DESC ", null);
        res.moveToFirst();
        while (!res.isAfterLast()) {
            String name = "";
            String topitem = res.getString(res.getColumnIndex(book_transaction_no));
            String subitem = res.getString(res.getColumnIndex(book_customer));
            Cursor cx = db.rawQuery(" SELECT " + cust_fullname + " FROM " + tbname_customers + " WHERE "
                    + cust_accountnumber + " = '" + subitem + "'", null);
            if (cx.getCount() != 0) {
                cx.moveToNext();
                name = cx.getString(cx.getColumnIndex(cust_fullname));
            }

            String id = res.getString(res.getColumnIndex(book_id));

            LinearItem list = new LinearItem(id, topitem, name);
            results.add(list);

            res.moveToNext();
        }
        // Add some more dummy data for testing
        return results;
    }

    //delete booking
    public boolean deleteBooking(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(tbname_booking, book_id + " = " + id, null);
        db.close();

        return true;
    }

    public String[] getAllReservationNumber() {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT " + reserve_reservation_no + " FROM " + tbname_reservation
                + " WHERE " + reserve_status + " = '2'", null);
        cursor.moveToFirst();
        ArrayList<String> numbers = new ArrayList<String>();
        while (!cursor.isAfterLast()) {
            numbers.add(cursor.getString(cursor.getColumnIndex(reserve_reservation_no)));
            cursor.moveToNext();
        }
        cursor.close();
        return numbers.toArray(new String[numbers.size()]);
    }

    public String[] getReceiverName() {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT " + cust_fullname + " FROM " + tbname_customers
                + " WHERE " + cust_type + " = 'receiver'", null);
        cursor.moveToFirst();
        ArrayList<String> numbers = new ArrayList<String>();
        while (!cursor.isAfterLast()) {
            numbers.add(cursor.getString(cursor.getColumnIndex(cust_fullname)));
            cursor.moveToNext();
        }
        cursor.close();
        return numbers.toArray(new String[numbers.size()]);
    }

    public String[] getBoxTypes() {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<String> numbers = new ArrayList<String>();
        Cursor c = db.rawQuery(" SELECT * FROM " + tbname_boxes, null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            numbers.add(c.getString(c.getColumnIndex(box_name)));
            c.moveToNext();
        }
        return numbers.toArray(new String[numbers.size()]);
    }

    public ArrayList<ThreeWayHolder> getBoxInv(String id){
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<ThreeWayHolder> numbers = new ArrayList<ThreeWayHolder>();

        //get from acceptance
        Cursor c = db.rawQuery(" SELECT "+tb_acceptance+"."+acc_id
                +","+tb_acceptance+"."+acc_quantity+","+
                tbname_boxes+"."+box_length+","+
                tbname_boxes+"."+box_width+","+
                tbname_boxes+"."+box_height+","+
                tbname_boxes+"."+box_name+" FROM " + tb_acceptance
                +" LEFT JOIN "+tbname_boxes
                +" ON "+tbname_boxes+"."+box_id+" = "+tb_acceptance+"."+acc_boxtype
                +" WHERE "+tb_acceptance+"."+acc_createdby+" = '"+id+"' AND "
                +tb_acceptance+"."+acc_quantity+" != '0'", null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            String accid = c.getString(0);
            String boxtype = c.getString(c.getColumnIndex(box_name));
            String sub = c.getString(c.getColumnIndex(acc_quantity));
            String len = c.getString(c.getColumnIndex(box_length));
            String wid = c.getString(c.getColumnIndex(box_width));
            String hei = c.getString(c.getColumnIndex(box_height));
            String subitem = Html.fromHtml("<small> ( "+ len +" * " +wid+" * "+hei+" )</small>").toString();
            ThreeWayHolder list = new ThreeWayHolder(accid, boxtype, subitem, sub);
            numbers.add(list);
            c.moveToNext();
        }

        //get from inventory
        String que = " SELECT COUNT("+tbname_checker_inventory + "." + chinv_boxtype + "), "
                +tbname_boxes+"."+"*, "
                +tbname_checker_inventory+"."+chinv_id+" FROM "+tbname_checker_inventory
                +" LEFT JOIN "+tbname_boxes +" ON "
                +tbname_boxes+"."+box_id+" = "+tbname_checker_inventory+"."+chinv_boxtype
                +" WHERE "+chinv_boxtype_fillempty+" = '1' AND "+chinv_stat+" = '0' GROUP BY "
                +chinv_boxtype;
        Cursor xc = db.rawQuery(que, null);
        xc.moveToFirst();
        while (!xc.isAfterLast()) {
            String accid = xc.getString(xc.getColumnIndex(box_id));
            String boxtype = xc.getString(xc.getColumnIndex(box_name));
            String sub = xc.getString(0);
            String len = xc.getString(xc.getColumnIndex(box_length));
            String wid = xc.getString(xc.getColumnIndex(box_width));
            String hei = xc.getString(xc.getColumnIndex(box_height));
            String subitem = Html.fromHtml("<small> with boxnumber ( "+ len +" * " +wid+" * "+hei+" )</small>").toString();
            ThreeWayHolder list = new ThreeWayHolder(accid, boxtype, subitem, sub);
            numbers.add(list);
            xc.moveToNext();
        }

        return numbers;
    }

    //box types
    public void addBoxes(String id, String name, String depoprice, String l, String w, String h,
                         String nsb,String description,String createdby, String recstat) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(box_id, id);
        cv.put(box_name, name);
        cv.put(box_depositprice, depoprice);
        cv.put(box_length, l);
        cv.put(box_width, w);
        cv.put(box_height, h);
        cv.put(box_nsb, nsb);
        cv.put(box_description, description);
        cv.put(box_createdby, createdby);
        cv.put(box_recordstatus, recstat);

        db.insert(tbname_boxes, null, cv);
        db.close();
    }

    //incident modules
    public boolean addIncident(String trans, String type, String reason,
                               String by, String date, String stat, String upds, String mod, String bnum) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(inc_transnum, trans);
        cv.put(inc_module, mod);
        cv.put(inc_boxnum, bnum);
        cv.put(inc_type, type);
        cv.put(inc_reason, reason);
        cv.put(inc_stat, stat);
        cv.put(inc_createdby, by);
        cv.put(inc_createddate, date);
        cv.put(inc_upds, upds);

        db.insert(tbname_incident, null, cv);
        db.close();

        return true;
    }

    public void addIncImages(String trans, byte[] image) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(inc_img_transaction_no, trans);
        cv.put(inc_img_imageblob, image);

        db.insert(tbname_incimages, null, cv);

        Log.e(" add img", "images added");
        db.close();
    }

    public ArrayList<HomeList> getImages(String transno) {
        ArrayList<HomeList> results = new ArrayList<HomeList>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor res = db.rawQuery(" SELECT * FROM " + tbname_incimages + " WHERE "
                + inc_img_transaction_no + " = '" + transno + "'", null);
        res.moveToFirst();
        while (!res.isAfterLast()) {
            byte[] topitem = res.getBlob(res.getColumnIndex(inc_img_imageblob));
            String ids = res.getString(res.getColumnIndex(inc_img_id));
            HomeList list = new HomeList(topitem, ids);
            results.add(list);

            res.moveToNext();
        }
        res.close();
        // Add some more dummy data for testing
        return results;
    }

    public void deleteImage(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(tbname_incimages, inc_img_id + " = " + id, null);
        db.close();
    }

    public void deleteIncident(String id){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(tbname_incident, inc_id + " = " + id, null);
        db.close();
    }

    public void deleteAllImage(String id){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(tbname_incimages, inc_img_transaction_no + " = " + id, null);
        db.close();
    }

    public void updateReservationNumber(String num, String stat){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(reserve_status, stat);

        db.update(tbname_reservation, cv, reserve_reservation_no+" = '"+num+"'", null);
        db.close();
        Log.e("reservestat", stat);
    }

    // acceptance transactions
    public void addNewAcceptance(String trans, String driver,String warehouse, String container,
                                 String date, String by, String stat, String upds){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(accept_transactionid, trans);
        cv.put(accept_drivername, driver);
        cv.put(accept_warehouseid, warehouse);
        cv.put(accept_container, container);
        cv.put(accept_date, date);
        cv.put(accept_createdby, by);
        cv.put(accept_uploadstat, stat);
        cv.put(accept_upds, upds);

        db.insert(tbname_check_acceptance, null, cv);
        Log.e("new_acceptance", trans);
        db.close();
    }

    public void addAcceptanceBoxnumber(String trans, String btype, String boxnum, String stat) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(acc_box_transactionno, trans);
        cv.put(acc_box_boxtype, btype);
        cv.put(acc_box_boxnumber, boxnum);
        cv.put(acc_box_stat, stat);

        db.insert(tbname_accept_boxes, null, cv);
        Log.e("add_filled_box", boxnum );
        db.close();
    }

    public ArrayList<LinearItem> getAcceptedBoxes(String transno) {
        ArrayList<LinearItem> results = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor res = db.rawQuery(" SELECT * FROM " + tbname_accept_boxes + " WHERE "
                + acc_box_transactionno + " = '" + transno + "' AND "+acc_box_stat+" = '1'", null);
        res.moveToFirst();
        while (!res.isAfterLast()) {
            String topitem = res.getString(res.getColumnIndex(acc_box_boxnumber));
            String ids = res.getString(res.getColumnIndex(acc_box_id));
            String sub = "";
            LinearItem list = new LinearItem(ids, topitem, sub);
            results.add(list);
            res.moveToNext();
        }
        res.close();
        // Add some more dummy data for testing
        return results;
    }

    public void deleteOneBoxnumber(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(tbname_accept_boxes, acc_box_id + " = " + id, null);
        db.close();
    }

    //transactions table
    //insert to transactions table
    public void addTransactions(String type, String user, String info, String date, String time) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(trans_type, type);
        cv.put(trans_user, user);
        cv.put(trans_information, info);
        cv.put(trans_date, date);
        cv.put(trans_time, time);

        Log.e("trns", "type:" + type + " ,user:" + user + ",info:" + info + ", date:" + date + ", time:" + time);
        db.insert(tbname_transactions, null, cv);
        db.close();
    }

    //get specific list by user and type
    public ArrayList<LinearItem> getSpecificTrans(String type, String user) {
        ArrayList<LinearItem> results = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor res = db.rawQuery(" SELECT * FROM " + tbname_transactions + " WHERE "
                + trans_type + " = '" + type + "' AND " + trans_user + " = '" + user + "'", null);
        res.moveToFirst();
        while (!res.isAfterLast()) {
            String topitem = res.getString(res.getColumnIndex(trans_information));
            String ids = res.getString(res.getColumnIndex(trans_id));
            String sub = res.getString(res.getColumnIndex(trans_date)) + " "
                    + res.getString(res.getColumnIndex(trans_time));
            LinearItem list = new LinearItem(ids, topitem, sub);
            results.add(list);
            res.moveToNext();
        }
        res.close();
        // Add some more dummy data for testing
        return results;
    }

    //get specific list by user only
    public ArrayList<LinearItem> getSpecificTransByUserID(String user) {
        ArrayList<LinearItem> results = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor res = db.rawQuery(" SELECT * FROM " + tbname_transactions + " WHERE "
                + trans_user + " = '" + user + "'", null);
        res.moveToFirst();
        while (!res.isAfterLast()) {
            String topitem = res.getString(res.getColumnIndex(trans_information));
            String ids = res.getString(res.getColumnIndex(trans_id));
            String sub = res.getString(res.getColumnIndex(trans_date)) + " "
                    + res.getString(res.getColumnIndex(trans_time));
            LinearItem list = new LinearItem(ids, topitem, sub);
            results.add(list);
            res.moveToNext();
        }
        res.close();
        // Add some more dummy data for testing
        return results;
    }

    //get booking boxes by transaction number
    public ArrayList<ListItem> getAllBoxInTransaNo(String trans){
        ArrayList<ListItem> results = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery(" SELECT * FROM " + tbname_booking_consignee_box + " WHERE "
                + book_con_transaction_no + " = '" + trans + "'", null);
        res.moveToFirst();
        while (!res.isAfterLast()) {
            String acount = res.getString(res.getColumnIndex(book_con_box_account_no));
            String ids = res.getString(res.getColumnIndex(book_con_box_id));
            String sub = res.getString(res.getColumnIndex(book_con_box_number));
            String a = res.getString(res.getColumnIndex(book_con_boxtype));
            String topitem = "", temptop = "";
            Cursor getname = db.rawQuery("SELECT " + cust_fullname + " FROM " + tbname_customers
                    + " WHERE " + cust_accountnumber + " = '" + acount + "'", null);
            if (getname.moveToNext()) {
                temptop = getname.getString(getname.getColumnIndex(cust_fullname));
            }
            ListItem list = new ListItem(ids, temptop, sub, a);
            results.add(list);
            res.moveToNext();
        }
        res.close();
        // Add some more dummy data for testing
        return results;
    }

    //add consignee booking
    public void addConsigneeBooking(String con, String boxtype, String orig, String dest, String trans,
                                    String boxnum, String stat,String hardport, String boxcont) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(book_con_box_account_no, con);
        cv.put(book_con_boxtype, boxtype);
        cv.put(book_con_origin, orig);
        cv.put(book_con_destination, dest);
        cv.put(book_con_transaction_no, trans);
        cv.put(book_con_box_number, boxnum);
        cv.put(book_con_hardport, hardport);
        cv.put(book_con_stat, stat);
        cv.put(book_con_boxcont, boxcont);
        db.insert(tbname_booking_consignee_box, null, cv);
        Log.e("consignee", boxnum);
        db.close();
    }

    public void deleteConsigneeTemp(String boxnum) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(tbname_booking_consignee_box, book_con_box_number + " = '" + boxnum + "'", null);
        db.close();
    }

    public String[] getYourReceivers(String acc) {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT * FROM " + tbname_customers
                + " WHERE " + cust_senderaccount + " = '" + acc + "'", null);
        cursor.moveToFirst();
        ArrayList<String> numbers = new ArrayList<String>();
        while (!cursor.isAfterLast()) {
            numbers.add(cursor.getString(cursor.getColumnIndex(cust_fullname)));
            cursor.moveToNext();
        }
        cursor.close();
        return numbers.toArray(new String[numbers.size()]);
    }

    public void addBarcodeSeries(String box, String q, String start, String end) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(barcode_boxtype, box);
        cv.put(barcode_quantity, q);
        cv.put(barcode_series_start, start);
        cv.put(barcode_series_end, end);

        db.insert(tbname_barcode, null, cv);
        db.close();
    }

    public JSONArray getWarehouseInventory(String x) {
        SQLiteDatabase myDataBase = this.getReadableDatabase();
        String raw = "SELECT * FROM " + tb_acceptance+" WHERE "+acc_createdby+" = '"+x+"' AND "
                +acc_status+" = '1'";
        Cursor cursor = myDataBase.rawQuery(raw, null);

        JSONArray resultSet = new JSONArray();

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {

            int totalColumn = cursor.getColumnCount();
            JSONObject rowObject = new JSONObject();

            for (int i = 0; i < totalColumn; i++) {
                if (cursor.getColumnName(i) != null) {

                    try {
                        if (cursor.getString(i) != null) {
                            Log.d("TAG_NAME", cursor.getString(i));
                            rowObject.put(cursor.getColumnName(i), cursor.getString(i));
                        } else {
                            rowObject.put(cursor.getColumnName(i), "");
                        }
                    } catch (Exception e) {
                        Log.d("TAG_NAME", e.getMessage());
                    }
                }

            }

            resultSet.put(rowObject);
            cursor.moveToNext();
        }

        cursor.close();
        //Log.e("result set", resultSet.toString());
        return resultSet;
    }

    public JSONArray getDistributionsBox(String id) {
        SQLiteDatabase myDataBase = this.getReadableDatabase();
        String raw = "SELECT "+dboxtemp_id+","+dboxtemp_invid+","+dboxtemp_boxid+","
                +dboxtemp_boxnumber+","+dboxtemp_distributionid+" FROM " + tbname_tempboxes+ " WHERE "+dboxtemp_distributionid+" = '"+id+"'";
        Cursor cursor = myDataBase.rawQuery(raw, null);
        JSONArray resultSet = new JSONArray();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            int totalColumn = cursor.getColumnCount();
            JSONObject rowObject = new JSONObject();
            for (int i = 0; i < totalColumn; i++) {
                if (cursor.getColumnName(i) != null) {
                    try {
                        if (cursor.getString(i) != null) {
                            Log.d("TAG_NAME", cursor.getString(i));
                            rowObject.put(cursor.getColumnName(i), cursor.getString(i));
                        } else {
                            rowObject.put(cursor.getColumnName(i), "");
                        }
                    } catch (Exception e) {
                        Log.d("TAG_NAME", e.getMessage());
                    }
                }

            }
            resultSet.put(rowObject);
            cursor.moveToNext();
        }

        cursor.close();
        //Log.e("result set", resultSet.toString());
        return resultSet;
    }

    //get specific list by user only
    public ArrayList<ThreeWayHolder> getDistributions(String by){
        ArrayList<ThreeWayHolder> results = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery(" SELECT * FROM " + tbname_tempDist
                +" WHERE "+temp_createby+" = '"+by+"'", null);
        res.moveToFirst();
        while (!res.isAfterLast()) {
            String disttype = res.getString(res.getColumnIndex(temp_type));
            String ac = res.getString(res.getColumnIndex(temp_typename));
            String id = res.getString(res.getColumnIndex(temp_transactionnumber));
            String sub = res.getString(res.getColumnIndex(temp_trucknum));
            String topitem = res.getString(res.getColumnIndex(temp_typename));
            String quant = "";
            Cursor aX = db.rawQuery(" SELECT COUNT("+dboxtemp_boxid+") FROM "+tbname_tempboxes
                    +" WHERE "+dboxtemp_distributionid+" = '"+id+"' GROUP BY "+dboxtemp_distributionid, null);
            if (aX.moveToFirst()){
                quant = aX.getString(0);
            }
            ThreeWayHolder list = new ThreeWayHolder(id, topitem, sub, quant);
            results.add(list);
            res.moveToNext();
        }
        res.close();
        // Add some more dummy data for testing
        return results;
    }

    public ArrayList<LinearItem> getAllIncident(){
        ArrayList<LinearItem> results = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor res = db.rawQuery(" SELECT * FROM " + tbname_incident
                +" WHERE "+inc_stat+" = '0'", null);
        res.moveToFirst();
        while (!res.isAfterLast()) {
            String id = res.getString(res.getColumnIndex(inc_id));
            String topitem = res.getString(res.getColumnIndex(inc_type));
            String sub = res.getString(res.getColumnIndex(inc_upds));
            String it = null;
            if (sub.equals("1")){
                it = "Pending";
            }else{
                it = "Uploaded";
            }
            LinearItem list = new LinearItem(id, topitem, it);
            results.add(list);
            res.moveToNext();
        }
        res.close();
        // Add some more dummy data for testing
        return results;
    }

    //update payment booking
    public void updatePayment(String resnum, String orno, String bookingid, String term,
                              String deposit, String totalamount, String createddate){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(pay_or_no, orno);
        cv.put(pay_booking_id, bookingid);
        cv.put(pay_paymentterm, term);
        cv.put(pay_deposit, deposit);
        cv.put(pay_total_amount, totalamount);
        cv.put(pay_createddate, createddate);

        db.update(tbname_payment, cv, pay_reservation_id+ " = "+resnum, null);

        Log.e(" updatepayment", "payment: "+totalamount+", deposit: "+deposit);
        db.close();
    }

    public void updateIncidentStat(String id, String stat){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(inc_stat, stat);

        db.update(tbname_incident,cv,inc_id +" = "+id, null);
        Log.e("updatedinc", "id :"+id);
        db.close();
    }

    public void addEmployee(String id, String first, String mid, String last, String mail,
                            String mobile, String phone, String gender, String birth, String post,
                            String house, String brgy, String cty, String branch){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(emp_id, id);
        cv.put(emp_first, first);
        cv.put(emp_mid, mid);
        cv.put(emp_last, last);
        cv.put(emp_mail, mail);
        cv.put(emp_mobile, mobile);
        cv.put(emp_phone, phone);
        cv.put(emp_gender, gender);
        cv.put(emp_birth, birth);
        cv.put(emp_post, post);
        cv.put(emp_house, house);
        cv.put(emp_brgy, brgy);
        cv.put(emp_city, cty);
        cv.put(emp_branch, branch);
        db.insert(tbname_employee, null, cv);
        db.close();

    }

    public ArrayList<LinearItem> getOICname(){
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<LinearItem> numbers = new ArrayList<LinearItem>();
        String q = " SELECT * FROM "+tbname_employee
                +" WHERE "+emp_post+" = 'OIC'";
        Cursor c = db.rawQuery(q, null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            String accid = c.getString(c.getColumnIndex(emp_id));
            String name = c.getString(c.getColumnIndex(emp_first))+" "
                    +c.getString(c.getColumnIndex(emp_last));
            String sub = c.getString(c.getColumnIndex(emp_id));
            LinearItem list = new LinearItem(accid, name,"employee id "+sub);
            numbers.add(list);
            c.moveToNext();
        }
        return numbers;
    }

    public JSONArray getAllCustomers(String id) {
        SQLiteDatabase myDataBase = this.getReadableDatabase();
        String raw = "SELECT * FROM " + tbname_customers+" WHERE "+cust_createdby+" = '"+id+"'";
        Cursor cursor = myDataBase.rawQuery(raw, null);
        JSONArray resultSet = new JSONArray();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {

            int totalColumn = cursor.getColumnCount();
            JSONObject rowObject = new JSONObject();

            for (int i = 0; i < totalColumn; i++) {
                if (cursor.getColumnName(i) != null) {
                    try {
                        if (cursor.getString(i) != null) {
                            Log.d("TAG_NAME", cursor.getString(i));
                            rowObject.put(cursor.getColumnName(i), cursor.getString(i));
                        } else {
                            rowObject.put(cursor.getColumnName(i), "");
                        }
                    } catch (Exception e) {
                        Log.d("TAG_NAME", e.getMessage());
                    }
                }
            }
            resultSet.put(rowObject);
            cursor.moveToNext();
        }

        cursor.close();
        //Log.e("result set", resultSet.toString());
        return resultSet;
    }

    public void delBookingBox(String id){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(tbname_booking_consignee_box, book_con_box_id + " = '" + id + "'", null);
        db.close();
    }

    public ArrayList<LinearItem> getSalesDriver(String post, String branch) {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<LinearItem> numbers = new ArrayList<LinearItem>();
        Cursor c = db.rawQuery(" SELECT *,"
                +tbname_employee+"."+emp_id+" FROM " + tbname_employee
                +" LEFT JOIN "+tbname_branch
                +" ON "+tbname_branch+"."+branch_id+" = "+tbname_employee+"."+emp_branch
                +" WHERE "+emp_post+" = '"+post+"' AND "+emp_branch
                +" = '"+branch+"'", null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            String id = c.getString(c.getColumnIndex(emp_id));
            String name = c.getString(c.getColumnIndex(emp_first))+" "
                    +c.getString(c.getColumnIndex(emp_last));
            String sub = c.getString(c.getColumnIndex(branch_name));
            LinearItem list = new LinearItem(id,name, sub);
            numbers.add(list);
            c.moveToNext();
        }
        return numbers;
    }

    public ArrayList<LinearItem> getBookingUnload(ArrayList<String> trans){
        ArrayList<LinearItem> results = new ArrayList<LinearItem>();
        SQLiteDatabase db = this.getReadableDatabase();
        for (String item : trans){
            Cursor res = db.rawQuery(" SELECT * FROM " + tbname_booking
                    +" JOIN "+tbname_booking_consignee_box+" ON "
                    +tbname_booking_consignee_box+"."+book_con_transaction_no
                    +" = "+tbname_booking+"."+book_transaction_no
                    + " WHERE "+tbname_booking+"."+book_transaction_no + " = '" + item + "'"
                    +" GROUP BY "+tbname_booking_consignee_box+"."+book_con_box_account_no, null);
            res.moveToFirst();
            while (!res.isAfterLast()){
                String name = "";
                String topitem = res.getString(res.getColumnIndex(book_transaction_no));
                String subitem = res.getString(res.getColumnIndex(book_con_box_account_no));
                Cursor cx = db.rawQuery(" SELECT * FROM " + tbname_customers + " WHERE "
                        + cust_accountnumber + " = '" + subitem + "'", null);
                if (cx.getCount() != 0) {
                    cx.moveToNext();
                    name = cx.getString(cx.getColumnIndex(cust_fullname));
                }
                String id = res.getString(res.getColumnIndex(book_id));
                String boxnum = res.getString(res.getColumnIndex(book_con_box_number));
                Cursor y = db.rawQuery(" SELECT * FROM "+tbname_delivery_box
                +" WHERE "+del_box_boxnumber+" = '"+boxnum+"'", null);
                if (y.getCount() == 0) {
                    LinearItem list = new LinearItem(id, topitem, name);
                    results.add(list);
                }
                    res.moveToNext();
            }
        }
        // Add some more dummy data for testing
        return results;
    }

    public void addDelivery(String id, String booknum, String customer,
                            String date, byte[] sign, String by,
                            String upds, String rating, String notes,
                            String receivedby, String relationship)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(del_id, id);
        cv.put(del_booking_no, booknum);
        cv.put(del_customer, customer);
        cv.put(del_createddate, date);
        cv.put(del_sign, sign);
        cv.put(del_createdby, by);
        cv.put(del_receivedby, receivedby);
        cv.put(del_relationship, relationship);
        cv.put(del_upds, upds);
        cv.put(del_receiverrate, rating);
        cv.put(del_notes, notes);
        db.insert(tbname_delivery, null, cv);
        db.close();
        Log.e("deliveryadded", id);
    }

    public void addDeliveryBox(String box, String id,
                               String rec, String orig, String dest,
                               String deliveryid, String stat, String substat, String date){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(del_box_boxnumber, box);
        cv.put(del_box_bookid, id);
        cv.put(del_box_receiver, rec);
        cv.put(del_box_origin, orig);
        cv.put(del_box_destination, dest);
        cv.put(del_box_deliveryid, deliveryid);
        cv.put(del_box_status, stat);
        cv.put(del_box_substatus, substat);
        cv.put(del_box_crdate, date);
        db.insert(tbname_delivery_box, null, cv);
        db.close();
        Log.e("deliveryboxadded", deliveryid);
    }

    public void addtoDriverInv(String boxtype,
                               String boxnumber, String type, String stat){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(sdinv_boxtype, boxtype);
        cv.put(sdinv_boxnumber, boxnumber);
        cv.put(sdinv_boxtype_fillempty, type);
        cv.put(sdinv_stat, stat);
        db.insert(tbname_driver_inventory, null, cv);
        db.close();
    }

    public void updateInvBoxnumber(String type, String boxnum, String stat){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(sdinv_stat, stat);
        cv.put(sdinv_boxtype_fillempty, type);
        db.update(tbname_driver_inventory, cv,
                sdinv_boxnumber+" = '"+boxnum+"'", null);
        Log.e("updboxnuminventory", boxnum);
        db.close();
    }

    //unloading
    public void addUnload(String trans, String boxnum, String stat) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(unloadbox_trans, trans);
        cv.put(unload_boxnum, boxnum);
        cv.put(unload_box_stat, stat);

        db.insert(tb_unloadbox, null, cv);
        db.close();
    }

    public void addFinalUnload(String id, String unloaddate, String forwarder, String container,
                               String timestart, String timeend, String plate, String driver,
                               String eta, String by,String stat, String upds){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(unload_id, id);
        cv.put(unload_date, unloaddate);
        cv.put(unload_forward, forwarder);
        cv.put(unload_con_number, container);
        cv.put(unload_timestart, timestart);
        cv.put(unload_timeend, timeend);
        cv.put(unload_plateno, plate);
        cv.put(unload_driver, driver);
        cv.put(unload_eta, eta);
        cv.put(unload_con_by, by);
        cv.put(unload_stat, stat);
        cv.put(unload_upds, upds);

        db.insert(tb_unload, null, cv);
        Log.e("unloading", id);
        db.close();
    }

    //get unloaded boxes
    public ArrayList<LinearItem> getUnloadBoxes(String transno) {
        ArrayList<LinearItem> results = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor res = db.rawQuery(" SELECT * FROM " + tb_unloadbox + " WHERE "
                + unloadbox_trans + " = '" + transno + "'", null);
        res.moveToFirst();
        while (!res.isAfterLast()) {
            String topitem = res.getString(res.getColumnIndex(unload_boxnum));
            String ids = res.getString(res.getColumnIndex(unloadbox_id));
            String sub = "";
            LinearItem list = new LinearItem(ids, topitem, sub);
            results.add(list);
            res.moveToNext();
        }
        res.close();
        // Add some more dummy data for testing
        return results;
    }

    public void deleteOneBoxnumberUnload(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(tb_unloadbox, unloadbox_id + " = " + id, null);
        db.close();
    }

    public ArrayList<LinearItem> getUnloads(String by) {
        ArrayList<LinearItem> results = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor res = db.rawQuery(" SELECT * FROM " + tb_unload
                +" WHERE "+unload_con_by+" = '"+by+"'", null);
        res.moveToFirst();
        while (!res.isAfterLast()) {
            String sub = res.getString(res.getColumnIndex(unload_forward));
            String topitem = sub+"("+res.getString(res.getColumnIndex(unload_con_number))+")";
            String id = res.getString(res.getColumnIndex(unload_id));
            String date = res.getString(res.getColumnIndex(unload_date));
            LinearItem list = new LinearItem(id, topitem, date);
            results.add(list);
            res.moveToNext();
        }
        res.close();
        return results;
    }

    public void addNewRemittance(String type, String name, String accname, String accnum,
                                 String amount, byte[] signature, String by,
                                 String date, String stat, String upds){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(remit_type, type);
        cv.put(remit_name, name);
        cv.put(remit_accountname, accname);
        cv.put(remit_accountnumber, accnum);
        cv.put(remit_amount, amount);
        cv.put(remit_signature, signature);
        cv.put(remit_createdby, by);
        cv.put(remit_createddate, date);
        cv.put(remit_status, stat);
        cv.put(remit_upds, upds);
        db.insert(tbname_remittance, null, cv);
        Log.e("remittance", "type: "+type+", name: "+name+", amount: "+amount);
        db.close();
    }

    public void addRemitTrans(String id, String type, String denom, String quant, String item,
                              String amount, String desc, byte[] image, String stat){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(rem_trans_remittanceid, id);
        cv.put(rem_trans_type, type);
        cv.put(rem_trans_denoms, denom);
        cv.put(rem_trans_quantity, quant);
        cv.put(rem_trans_itemname, item);
        cv.put(rem_trans_amount, amount);
        cv.put(rem_trans_desc, desc);
        cv.put(rem_trans_image, image);
        cv.put(rem_trans_stat, stat);
        db.insert(tbname_remittance_trans,null,cv);
        Log.e("remtrans", type);
        db.close();
    }

    public ArrayList<LinearItem> getAllRemittance(String type){
        ArrayList<LinearItem> results = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        if (type.equals("OIC")) {
            Cursor res = db.rawQuery(" SELECT " + tbname_remittance + "." + remit_id
                    + ", " + tbname_remittance + "." + remit_status + ", "
                    + tbname_employee + "." + emp_first
                    + ", " + tbname_employee + "." + emp_last + " FROM " + tbname_remittance
                    + " LEFT JOIN " + tbname_employee + " ON " + tbname_remittance + "." + remit_name
                    + " = " + tbname_employee + "." + emp_id +" WHERE "
                    +tbname_remittance+"."+remit_type+" = 'OIC'", null);
            res.moveToFirst();
            while (!res.isAfterLast()) {
                String id = res.getString(0);
                String fullname = res.getString(res.getColumnIndex(emp_first)) + " " + res.getString(res.getColumnIndex(emp_last));
                String sub = res.getString(res.getColumnIndex(remit_status));
                String it = null;
                if (sub.equals("1")) {
                    it = "Uploaded";
                } else {
                    it = "Pending";
                }
                LinearItem list = new LinearItem(id, fullname, it);
                results.add(list);
                res.moveToNext();
            }
            res.close();
        }else{
            SQLiteDatabase x = this.getReadableDatabase();
            Cursor res = x.rawQuery(" SELECT * FROM "+tbname_remittance+" WHERE "+remit_type
                    +" = 'BANK'", null);
            res.moveToFirst();
            while (!res.isAfterLast()) {
                String id = res.getString(res.getColumnIndex(remit_id));
                String fullname = res.getString(res.getColumnIndex(remit_name));
                String sub = res.getString(res.getColumnIndex(remit_status));
                String it = null;
                if (sub.equals("1")) {
                    it = "Uploaded";
                } else {
                    it = "Pending";
                }
                LinearItem list = new LinearItem(id, fullname, it);
                results.add(list);
                res.moveToNext();
            }
            res.close();
        }
        // Add some more dummy data for testing
        return results;
    }

    public void addAllowance(String amount, String by, String date, String stat){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(all_amount, amount);
        cv.put(all_createdby, by);
        cv.put(all_createddate, date);
        cv.put(all_stat, stat);
        db.insert(tbname_allowance, null, cv);
        Log.e("allowance", amount);
        db.close();

    }


    public void addtoCheckerInv(String boxtype,
                                String boxnumber, String type, String stat){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(chinv_boxtype, boxtype);
        cv.put(chinv_boxnumber, boxnumber);
        cv.put(chinv_boxtype_fillempty, type);
        cv.put(chinv_stat, stat);
        db.insert(tbname_checker_inventory, null, cv);
        db.close();
    }

    public void addtoPartnerInv(String boxtype,
                                String boxnumber, String type, String stat){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(partinv_boxtype, boxtype);
        cv.put(partinv_boxnumber, boxnumber);
        cv.put(partinv_boxtype_fillempty, type);
        cv.put(partinv_stat, stat);
        db.insert(tbname_partner_inventory, null, cv);
        db.close();
    }

    public void addRemitAmount(String amount, String trnum, String stat, String by, String date){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(rem_amount_amount, amount);
        cv.put(rem_amount_transnum, trnum);
        cv.put(rem_amount_stat, stat);
        cv.put(rem_amount_by, by);
        cv.put(rem_amount_date, date);
        db.insert(tbname_remitttances_amount, null, cv);
        Log.e("remitamount", trnum);
        db.close();
    }

    public void updateRemAmount(String trans, String amount, String by, String stat){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(rem_amount_stat, stat);
        cv.put(rem_amount_amount, amount);
        db.update(tbname_remitttances_amount, cv,
                rem_amount_by+" = '"+by+"' AND "
                        +rem_amount_transnum+" = '"+trans+"'", null );
        db.close();
    }

    public void updateRemAmountStat(String by, String stat){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(rem_amount_stat, stat);
        db.update(tbname_remitttances_amount, cv,
                rem_amount_by+" = '"+by+"' AND "
                        +rem_amount_stat+" = '0'", null );
        db.close();
    }

    public void addBranch(String id, String name, String address, String type, String stat){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(branch_id, id);
        cv.put(branch_name, name);
        cv.put(branch_address, address);
        cv.put(branch_type, type);
        cv.put(branch_recordstat, stat);
        db.insert(tbname_branch, null, cv);
        Log.e("addbranch", id);
        db.close();
    }

    public void addDiscount(String trans, String discount,String remarks){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(disc_trans_no, trans);
        cv.put(disc_discount, discount);
        cv.put(disc_remarks, remarks);
        db.insert(tbname_discount, null,cv);
        Log.e("discount",discount);
        db.close();
    }

    public void deleteDiscountsOnDestroy(String trans){
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            db.delete(tbname_discount, disc_trans_no + " = '" + trans + "'", null);
            Log.e("del_discount", trans);
            db.close();
        }catch (Exception e){
            Log.e("exception",e.getMessage());
        }
    }

    public void updDist(String trans, String type, String typename,
                                   String truck, String rem, String status, String upstat,
                                   String date, String by) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(temp_transactionnumber, trans);
        cv.put(temp_type, type);
        cv.put(temp_typename, typename);
        cv.put(temp_trucknum, truck);
        cv.put(temp_remarks, rem);
        cv.put(temp_status, status);
        cv.put(temp_uploadstat, upstat);
        cv.put(temp_createdate, date);
        cv.put(temp_createby, by);

        db.update(tbname_tempDist, cv, temp_transactionnumber+" = '"+trans+"'", null);
        Log.e("update_distribution",trans);
        db.close();
    }

    public void updDistBoxDist(String id, String btype, String invid, String boxnumber,
                               String stat){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(dboxtemp_distributionid, id);
        values.put(dboxtemp_invid, invid);
        values.put(dboxtemp_boxid, btype);
        values.put(dboxtemp_boxnumber, boxnumber);
        values.put(dboxtemp_stat, stat);
        db.update(tbname_tempboxes, values, dboxtemp_distributionid+" = '"+id+"'", null);
        Log.e("update_dbox", ""+invid);
        db.close();

    }

    public void updAcceptanceExist(String trans, String driver,String warehouse, String container,
                                 String date, String by, String stat, String upds){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(accept_transactionid, trans);
        cv.put(accept_drivername, driver);
        cv.put(accept_warehouseid, warehouse);
        cv.put(accept_container, container);
        cv.put(accept_date, date);
        cv.put(accept_createdby, by);
        cv.put(accept_uploadstat, stat);
        cv.put(accept_upds, upds);

        db.update(tbname_check_acceptance, cv, accept_transactionid+" = '"+trans+"'", null);
        Log.e("update_acceptance", trans);
        db.close();
    }

    public void updAcceptanceBoxnumber(String trans, String btype, String boxnum, String stat) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(acc_box_transactionno, trans);
        cv.put(acc_box_boxtype, btype);
        cv.put(acc_box_boxnumber, boxnum);
        cv.put(acc_box_stat, stat);

        db.update(tbname_accept_boxes, cv, acc_box_transactionno+" = '"+trans+"'", null);
        Log.e("update_filled_box", boxnum );
        db.close();
    }

    public void updBooking(String transno, String reserveno, String custno, String bookdate,
                           String bookstat, String type, String createdby, String upds) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(book_transaction_no, transno);
        cv.put(book_reservation_no, reserveno);
        cv.put(book_customer, custno);
        cv.put(book_book_date, bookdate);
        cv.put(book_booking_status, bookstat);
        cv.put(book_type, type);
        cv.put(book_createdby, createdby);
        cv.put(book_upds, upds);

        db.update(tbname_booking, cv, book_transaction_no+" = '"+transno+"'", null);
        db.close();
        Log.e("update_booking", ""+transno);
    }


    public void updConsigneeBooking(String id, String con,String orig, String dest, String trans,
                                    String boxnum, String stat,String hardport, String bcont) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(book_con_box_account_no, con);
        cv.put(book_con_origin, orig);
        cv.put(book_con_destination, dest);
        cv.put(book_con_transaction_no, trans);
        cv.put(book_con_box_number, boxnum);
        cv.put(book_con_stat, stat);
        cv.put(book_con_hardport, hardport);
        cv.put(book_con_boxcont, bcont);

        db.update(tbname_booking_consignee_box, cv, book_con_box_id + " = " + id, null);
        db.close();
    }

    public void updConsigneeBookingExist(String con, String boxtype, String orig, String dest, String trans,
                                    String boxnum, String stat, String hardport, String bcont) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(book_con_box_account_no, con);
        cv.put(book_con_boxtype, boxtype);
        cv.put(book_con_origin, orig);
        cv.put(book_con_destination, dest);
        cv.put(book_con_transaction_no, trans);
        cv.put(book_con_box_number, boxnum);
        cv.put(book_con_stat, stat);
        cv.put(book_con_hardport, hardport);
        cv.put(book_con_boxcont, bcont);
        db.update(tbname_booking_consignee_box, cv, book_con_transaction_no+" = '"+trans+"'", null);
        Log.e("update_consignee", boxnum);
        db.close();
    }

    public void updFinalUnloaded(String id, String unloaddate, String forwarder, String container,
                               String timestart, String timeend,String plate, String driver,
                                 String eta, String by,String stat, String upds){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(unload_id, id);
        cv.put(unload_date, unloaddate);
        cv.put(unload_forward, forwarder);
        cv.put(unload_con_number, container);
        cv.put(unload_timestart, timestart);
        cv.put(unload_timeend, timeend);
        cv.put(unload_plateno, plate);
        cv.put(unload_driver, driver);
        cv.put(unload_eta, eta);
        cv.put(unload_con_by, by);
        cv.put(unload_stat, stat);
        cv.put(unload_upds, upds);

        db.update(tb_unload, cv, unload_id+" = '"+id+"'", null);
        Log.e("update_unloading", id);
        db.close();
    }

    public void updUnloadedBox(String trans, String boxnum, String stat) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(unloadbox_trans, trans);
        cv.put(unload_boxnum, boxnum);
        cv.put(unload_box_stat, stat);

        db.update(tb_unloadbox, cv, unloadbox_trans+" = '"+trans+"'", null);
        db.close();
    }

    public void updateDistById(String id, byte[] sign){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(temp_acceptstat, "1");
        cv.put(temp_acceptsign, sign);
        db.update(tbname_tempDist, cv, temp_transactionnumber+" = '"+id+"'", null);
        Log.e("update_distaccepted", id);
        db.close();
    }

}
