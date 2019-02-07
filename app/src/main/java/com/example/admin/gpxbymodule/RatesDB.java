package com.example.admin.gpxbymodule;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class RatesDB extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "rates.db";

    //create table rates
    public final String tbname_rates = "gpx_boxrate";
    public final String rate_id = "id";
    public final String rate_boxtype = "boxtype_id";
    public final String rate_size_length = "size_length";
    public final String rate_size_width = "size_width";
    public final String rate_size_height = "size_height";
    public final String rate_cbm = "cbm";
    public final String rate_source_id = "source_id";
    public final String rate_destination_id = "destination_id";
    public final String rate_currency_id = "currency_id";
    public final String rate_amount = "amount";
    public final String rate_recordstatus = "recordstatus";
    private String createRates = " CREATE TABLE " + tbname_rates + "("
            + rate_id + " INTEGER PRIMARY KEY,"
            + rate_boxtype + " TEXT, "
            + rate_cbm + " TEXT, "
            + rate_source_id + " TEXT, "
            + rate_destination_id + " TEXT, "
            + rate_currency_id + " TEXT, "
            + rate_amount + " TEXT, "
            + rate_recordstatus + " TEXT )";
    //drop queries
    private String DROP_rates = "DROP TABLE IF EXISTS " + tbname_rates;

    //provinces table
    public final String tbname_provinces = "gpx_phil_provinces";
    public final String prov_id = "id";
    public final String prov_name = "name";
    public final String prov_code = "code";
    public final String prov_regcode = "regioncode";
    public final String prov_destinationid = "destination_id";
    public final String prov_hardport = "hardport";
    private String createProvinces = " CREATE TABLE " + tbname_provinces + "("
            + prov_id + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + prov_name + " TEXT UNIQUE, "
            + prov_code + " TEXT, "
            + prov_destinationid + " TEXT, "
            + prov_hardport + " TEXT, "
            + prov_regcode + " TEXT )";
    //drop queries
    private String dropprovinces = "DROP TABLE IF EXISTS " + tbname_provinces;

    //cities table
    public final String tbname_city = "gpx_phil_city";
    public final String ct_id = "id";
    public final String ct_name = "name";
    public final String ct_prov = "province";
    public final String ct_citycode = "city_code";
    private String createCities = " CREATE TABLE " + tbname_city + "("
            + ct_id + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + ct_name + " TEXT, "
            + ct_prov + " TEXT, "
            + ct_citycode + " TEXT )";
    //drop queries
    private String dropcity = "DROP TABLE IF EXISTS " + tbname_city;

    //cities table
    public final String tbname_brgy = "gpx_phil_brgy";
    public final String brgy_id = "id";
    public final String brgy_code = "brgy_code";
    public final String brgy_name = "name";
    public final String brgy_prov = "prov_code";
    public final String brgy_ctcode = "city_code";
    private String createBrgy = " CREATE TABLE " + tbname_brgy + "("
            + brgy_id + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + brgy_code + " TEXT UNIQUE, "
            + brgy_name + " TEXT, "
            + brgy_prov + " TEXT, "
            + brgy_ctcode + " TEXT )";
    //drop queries
    private String dropbrgy = "DROP TABLE IF EXISTS " + tbname_brgy;

    // distribution images
    public final String tbname_dstimages = "gpx_distribution_images";
    public final String distimage_id = "id";
    public final String distimage_trans = "transaction_no";
    public final String distimage_image = "image";
    public String createDistImage = " CREATE TABLE " + tbname_dstimages + "("
            + distimage_id + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + distimage_trans + " TEXT, "
            + distimage_image + " BLOB )";
    //drop queries
    private String dropdistimage = "DROP TABLE IF EXISTS " + tbname_dstimages;

    //warehouses
    public final String tbname_warehouse = "gpx_warehouse";
    public final String ware_id = "id";
    public final String ware_name = "name";
    public final String ware_branchid = "branch_id";
    public final String ware_recordstatus = "recordstatus";
    public String createtbwarehouse = " CREATE TABLE " + tbname_warehouse + "("
            + ware_id + " INTEGER UNIQUE,"
            + ware_name + " TEXT, "
            + ware_branchid + " TEXT, "
            + ware_recordstatus + " INTEGER )";

    //drop queries
    public String dropwarehouse = "DROP TABLE IF EXISTS " + tbname_warehouse;

    //employee inventory
    public final String tbname_employee_inventory = "gpx_employee_inventory_box";
    public final String inv_emp_id = "id";
    public final String inv_emp_employee_id = "employee_id";
    public final String inv_emp_warehouse_id = "warehouse_id";
    public final String inv_emp_boxtype_id = "boxtype_id";
    public final String inv_emp_quantity = "quantity";
    public final String inv_emp_createddate = "createddate";
    public final String inv_emp_createdby = "createdby";
    public String createEmployeeInv = " CREATE TABLE " + tbname_employee_inventory + "("
            + inv_emp_id + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + inv_emp_employee_id + " INTEGER, "
            + inv_emp_warehouse_id + " INTEGER, "
            + inv_emp_boxtype_id + " INTEGER, "
            + inv_emp_quantity + " INTEGER, "
            + inv_emp_createddate + " TEXT, "
            + inv_emp_createdby + " INTEGER )";
    //drop queries
    public String dropempinv = "DROP TABLE IF EXISTS " + tbname_employee_inventory;

    //reservation signatures
    public final String tbname_reserve_sign = "gpx_reservation_signature";
    public final String res_sign_id = "id";
    public final String res_sign_reservation_no = "reservation_no";
    public final String res_sign_sign = "signature";
    public String createReserveSign = " CREATE TABLE " + tbname_reserve_sign + "("
            + res_sign_id + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + res_sign_reservation_no + " INTEGER, "
                + res_sign_sign + " BLOB )";
    public String dropreservesign = "DROP TABLE IF EXISTS " + tbname_reserve_sign;

    //booking signatures
    public final String tbname_book_sign = "gpx_booking_signature";
    public final String book_sign_id = "id";
    public final String book_sign_booking_no = "booking_no";
    public final String book_sign_sign = "signature";
    public String createBookSign = " CREATE TABLE " + tbname_book_sign + "("
            + book_sign_id + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + book_sign_booking_no + " INTEGER, "
            + book_sign_sign + " BLOB )";
    public String dropBooksign = "DROP TABLE IF EXISTS " + tbname_book_sign;

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

    //source and destinations
    public final String tbname_sourcedes = "gpx_source_destination";
    public final String sd_id = "id";
    public final String sd_name = "name";
    public final String sd_type = "type";
    public final String sd_recordstatus = "recordstatus";
    public final String createSd = " CREATE TABLE "+tbname_sourcedes+" ( "
            + sd_id +" INTEGER PRIMARY KEY UNIQUE, "
            + sd_name+ " TEXT, "
            + sd_type+ " TEXT, "
            + sd_recordstatus+ " INTEGER )";
    public final String dropsd = " DROP TABLE IF EXISTS "+tbname_sourcedes;


    //loading
    public final String tb_loading = "gpx_loading";
    public final String load_id = "id";
    public final String load_date = "loaded_date";
    public final String load_shipper = "shipping_name";
    public final String load_container = "container_no";
    public final String load_eta = "eta";
    public final String load_etd = "etd";
    public final String load_stat = "status";
    public final String load_upds = "upload_status";
    public final String load_createdby = "createdby";
    public String createLoading = "CREATE TABLE " + tb_loading + "("
            + load_id + " TEXT PRIMARY KEY UNIQUE, "
            + load_date + " DATE, "
            + load_shipper + " TEXT, "
            + load_container + " TEXT, "
            + load_eta + " TEXT , "
            + load_etd + " TEXT , "
            + load_stat + " TEXT , "
            + load_upds + " TEXT , "
            + load_createdby +  " TEXT )" ;
    public String dropLoading = "DROP TABLE IF EXISTS " + tb_loading;

    //table loadbox
    public final String tb_loadbox = "gpx_loading_box";
    public final String loadbox_id = "loading_id";
    public final String loadbox_trans = "transaction_no";
    public final String load_boxnum = "box_num";
    public final String load_box_stat = "status";
    public String createLoadbox = "CREATE TABLE " + tb_loadbox + "("
            + loadbox_id + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + loadbox_trans + " TEXT, "
            + load_boxnum + " TEXT UNIQUE, "
            + load_box_stat + " TEXT ) ";
    public String dropLoadbox = "DROP TABLE IF EXISTS " + tb_loadbox;

    //manufacturers name
    public final String tbname_man = "gpx_manufacturers";
    public final String man_id = "id";
    public final String man_fullname = "manufacturer_name";
    public final String man_createddate = "createddate";
    public String createman = "CREATE TABLE " + tbname_man + "("
            + man_id + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + man_fullname + " TEXT, "
            + man_createddate + " TEXT ) ";
    public String dropman = "DROP TABLE IF EXISTS " + tbname_man;

    //reservation image
    public final String tbname_reserve_image = "gpx_reservation_images";
    public final String res_img_id = "id";
    public final String res_img_trans = "transaction_no";
    public final String res_img_image = "image";
    public String createResImage = " CREATE TABLE " + tbname_reserve_image + "("
            + res_img_id + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + res_img_trans + " TEXT, "
            + res_img_image + " BLOB )";
    public String dropResImg = "DROP TABLE IF EXISTS " + tbname_reserve_image;

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

    //expense items selection table
    public final String tbname_exp_item = "gpx_expense_items";
    public final String expit_id = "item_id";
    public final String expit_name = "item_name";
    public final String expit_type = "item_type";
    public final String createExpit = " CREATE TABLE " + tbname_exp_item + "("
            + expit_id + " INTEGER PRIMARY KEY UNIQUE,"
            + expit_type+ " TEXT, "
            + expit_name + " TEXT UNIQUE )";
    public final String dropexpit = " DROP TABLE IF EXISTS " + tbname_exp_item;

    // temporary table for partner distribution
    public final String tbname_part_distribution = "gpx_distribution_partner";
    public final String partdist_id = "id";
    public final String partdist_transactionnumber = "transaction_no";
    public final String partdist_type = "type";
    public final String partdist_mode = "shipment_mode";
    public final String partdist_eta = "eta";
    public final String partdist_typename = "typename";
    public final String partdist_trucknum = "trucknumber";
    public final String partdist_drivername = "drivername";
    public final String partdist_remarks = "remarks";
    public final String partdist_status = "status";
    public final String partdist_createdate = "created_date";
    public final String partdist_createby = "createby";
    public final String partdist_acceptstat = "acceptance_status";
    public final String partdist_acceptsign = "acceptance_signature";
    public final String partdist_uploadstat = "upload_status";
    public final String create_part_dist = " CREATE TABLE " + tbname_part_distribution + "("
            + partdist_id + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + partdist_transactionnumber + " TEXT UNIQUE, "
            + partdist_type + " TEXT, "
            + partdist_mode + " TEXT, "
            + partdist_typename + " TEXT, "
            + partdist_drivername+ " TEXT, "
            + partdist_trucknum + " TEXT, "
            + partdist_eta + " TEXT, "
            + partdist_remarks + " TEXT, "
            + partdist_status + " TEXT, "
            + partdist_createdate + " TEXT, "
            + partdist_createby + " TEXT, "
            + partdist_acceptstat + " INTEGER, "
            + partdist_acceptsign + " BLOB, "
            + partdist_uploadstat + " TEXT )";
    public final String drop_part_dist = " DROP TABLE IF EXISTS " + tbname_part_distribution;

    //temporary distribution table
    public final String tbname_part_distribution_box = "gpx_partner_distribution_box";
    public final String partdist_box_id = "id";
    public final String partdist_box_boxid = "boxtype_id";
    public final String partdist_box_boxnumber = "boxnumber";
    public final String partdist_box_distributionid = "distribution_id";
    public final String partdist_box_stat = "distribution_status";
    public final String create_part_dist_box = " CREATE TABLE " + tbname_part_distribution_box + "("
            + partdist_box_id + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + partdist_box_boxid + " TEXT, "
            + partdist_box_boxnumber + " TEXT, "
            + partdist_box_distributionid + " TEXT, "
            + partdist_box_stat + " TEXT )";
    public final String drop_part_dist_box = " DROP TABLE IF EXISTS " + tbname_part_distribution_box;

    //partner acceptance
    public final String tbname_part_acc = "gpx_partner_acceptance";
    public final String pacc_id = "id";
    public final String pacc_branch = "branch_name";
    public final String pacc_driver = "driver_name";
    public final String pacc_trnum = "truck_number";
    public final String pacc_trans = "transaction_number";
    public final String pacc_createAcc = " CREATE TABLE " + tbname_part_acc + "("
            + pacc_id + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + pacc_trans + " TEXT UNIQUE, "
            + pacc_branch + " TEXT, "
            + pacc_driver + " TEXT, "
            + pacc_trnum + " TEXT )";
    public final String drop_pacc = " DROP TABLE IF EXISTS " + tbname_part_acc;

    //partner acceptance
    public final String tbname_part_acc_box = "gpx_partner_acceptance_box";
    public final String pacc_box_id = "id";
    public final String pacc_box_trans = "transaction_id";
    public final String pacc_box_bnumber = "boxnumber";
    public final String pacc_createAcc_box = " CREATE TABLE " + tbname_part_acc_box + "("
            + pacc_box_id + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + pacc_box_trans + " TEXT, "
            + pacc_box_bnumber + " TEXT UNIQUE )";
    public final String drop_pacc_box = " DROP TABLE IF EXISTS " + tbname_part_acc_box;

    // from unloading to be distributed to areas or hub
    public final String tbname_partnerboxes_todistribute = "gpx_boxes_todistribute";
    public final String partnerboxes_id = "id";
    public final String partnerboxes_boxnum = "boxnumber";
    public final String partnerboxes_booktrans = "booking_number";
    public final String partnerboxes_destination = "destination_id";
    public final String partnerboxes_actstat = "activity_status";
    public final String partnerbox_createTable = " CREATE TABLE " + tbname_partnerboxes_todistribute + "("
            + partnerboxes_id + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + partnerboxes_boxnum + " TEXT UNIQUE, "
            + partnerboxes_booktrans + " TEXT, "
            + partnerboxes_destination + " TEXT, "
            + partnerboxes_actstat + " TEXT )";
    public final String drop_partnerbox = " DROP TABLE IF EXISTS " + tbname_partnerboxes_todistribute;

    //delivery status
    public final String tbname_delivery_status = "gpx_delivery_status";
    public final String delstat_id = "id";
    public final String delstat_name = "name";
    public final String delstat_record = "recordstatus";
    public final String createDelStat = " CREATE TABLE " + tbname_delivery_status + "("
            + delstat_id + " INTEGER PRIMARY KEY UNIQUE, "
            + delstat_name + " TEXT, "
            + delstat_record + " TEXT )";
    public final String dropDelStat = " DROP TABLE IF EXISTS " + tbname_delivery_status;

    //delivery status
    public final String tbname_delivery_substatus = "gpx_delivery_substatus";
    public final String delsubstat_id = "id";
    public final String delsubstat_name = "name";
    public final String delsubstat_statid = "status_id";
    public final String createDelSubStat = " CREATE TABLE " + tbname_delivery_substatus + "("
            + delsubstat_id + " INTEGER PRIMARY KEY UNIQUE, "
            + delsubstat_name + " TEXT, "
            + delsubstat_statid + " TEXT )";
    public final String dropDelSubStat = " DROP TABLE IF EXISTS " + tbname_delivery_substatus;

    //for direct delivers
    public final String tbname_fordirects = "gpx_directs";
    public final String directid = "id";
    public final String direct_boxnumber = "boxnumber";
    public final String direct_stat = "status";
    public final String createDirects  = " CREATE TABLE "+tbname_fordirects+" ("
            +directid+" INTEGER PRIMARY KEY AUTOINCREMENT, "
            +direct_boxnumber+" TEXT UNIQUE, "
            +direct_stat+" TEXT )";
    public final String dropDirect = " DROP TABLE IF EXISTS "+tbname_fordirects;

    //unloading image by boxnumber
    public final String tbname_unloadingbox_image = "gpx_unloadingbox_image";
    public final String unbi_id = "id";
    public final String unbi_trans = "transaction_number";
    public final String unbi_boxnumber = "boxnumber";
    public final String unbi_image = "image";
    public final String createUnbi  = " CREATE TABLE "+tbname_unloadingbox_image+" ("
            +unbi_id+" INTEGER PRIMARY KEY AUTOINCREMENT, "
            +unbi_trans+" TEXT, "
            +unbi_boxnumber+" TEXT, "
            +unbi_image+" BLOB )";
    public final String dropUnbi = " DROP TABLE IF EXISTS "+tbname_unloadingbox_image;

    //box contents
    public final String tbname_boxcont = "gpx_boxcontent";
    public final String bcont_id = "id";
    public final String bcont_desc = "description";
    public final String createBCont  = " CREATE TABLE "+tbname_boxcont+" ("
            +bcont_id+" INTEGER PRIMARY KEY UNIQUE, "
            +bcont_desc+" TEXT )";
    public final String dropbcont = " DROP TABLE IF EXISTS "+tbname_boxcont;

    //barcode distribution
    public final String tbname_barcode_dist = "gpx_barcode_distribution";
    public final String bardist_id = "id";
    public final String bardist_trans = "transaction_no";
    public final String bardist_driverid = "driver_id";
    public final String bardist_createddate = "createddate";
    public final String bardist_createdby = "createdby";
    public final String bardist_upds = "upload_status";
    public final String bardist_accptstat = "acceptance_status";
    public final String createBarcodeDist  = " CREATE TABLE "+tbname_barcode_dist+" ("
            +bardist_id+" INTEGER PRIMARY KEY AUTOINCREMENT, "
            +bardist_trans+" TEXT UNIQUE, "
            +bardist_driverid+" INTEGER, "
            +bardist_createddate+" TEXT, "
            +bardist_upds+" INTEGER, "
            +bardist_accptstat+" INTEGER, "
            +bardist_createdby+" TEXT )";
    public final String dropBarcodeDist = " DROP TABLE IF EXISTS "+tbname_barcode_dist;

    //barcode distribution box number
    public final String tbname_barcode_dist_boxnumber = "gpx_barcode_distribution_boxnumber";
    public final String bardist_bnum_id = "id";
    public final String bardist_bnum_trans = "transaction_no";
    public final String bardist_bnum_boxnumber = "box_number";
    public final String bardist_bnum_status = "status";
    public final String createBarDistBoxnumber  = " CREATE TABLE "+tbname_barcode_dist_boxnumber+" ("
            +bardist_bnum_id+" INTEGER PRIMARY KEY AUTOINCREMENT, "
            +bardist_bnum_trans+" TEXT, "
            +bardist_bnum_boxnumber+" TEXT UNIQUE, "
            +bardist_bnum_status+" INTEGER )";
    public final String dropBarDistBoxnumber = " DROP TABLE IF EXISTS "+tbname_barcode_dist_boxnumber;

    //barcode inventory
    public final String tbname_barcode_inventory = "gpx_barcode_inventory";
    public final String barcodeinv_id = "id";
    public final String barcodeinv_boxnumber = "box_number";
    public final String barcodeinv_status = "status";
    public final String createBarcodeInventory  = " CREATE TABLE "+tbname_barcode_inventory+" ("
            +barcodeinv_id+" INTEGER PRIMARY KEY AUTOINCREMENT, "
            +barcodeinv_boxnumber+" TEXT UNIQUE, "
            +barcodeinv_status+" INTEGER )";
    public final String dropBarcodeInventory = " DROP TABLE IF EXISTS "+tbname_barcode_inventory;

    //barcode inventory
    public final String tbname_barcode_driver_inventory = "gpx_barcode_driver_inventory";
    public final String barcodeDriverInv_id = "id";
    public final String barcodeDriverInv_boxnumber = "box_number";
    public final String barcodeDriverInv_status = "status";
    public final String createBarcodeDriverInventory  = " CREATE TABLE "+tbname_barcode_driver_inventory+" ("
            +barcodeDriverInv_id+" INTEGER PRIMARY KEY AUTOINCREMENT, "
            +barcodeDriverInv_boxnumber+" TEXT UNIQUE, "
            +barcodeDriverInv_status+" INTEGER )";
    public final String dropBarcodeDriverInventory = " DROP TABLE IF EXISTS "+tbname_barcode_driver_inventory;

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

    public RatesDB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        //rates
        db.execSQL(createRates);
        db.execSQL(createBarcodeDist);
        db.execSQL(createBarDistBoxnumber);
        db.execSQL(createBarcodeInventory);
        db.execSQL(createBarcodeDriverInventory);
        db.execSQL(createEmpTb);
        db.execSQL(createSd);
        db.execSQL(createTableBox);
        db.execSQL(createExpit);
        db.execSQL(createDelStat);
        db.execSQL(createDelSubStat);
        db.execSQL(createUnbi);
        db.execSQL(createBCont);

        //province
        db.execSQL(createProvinces);
        db.execSQL(createBrgy);
        db.execSQL(createman);
        db.execSQL(createDirects);

        //city
        db.execSQL(createCities);
        db.execSQL(createDistImage);

        //warehouse
        db.execSQL(createtbwarehouse);
        db.execSQL(createEmployeeInv);
        db.execSQL(createReserveSign);
        db.execSQL(createBranch);
        db.execSQL(createBookSign);
        db.execSQL(createLoading);
        db.execSQL(createLoadbox);
        db.execSQL(createResImage);
        db.execSQL(create_part_dist);
        db.execSQL(create_part_dist_box);
        db.execSQL(pacc_createAcc);
        db.execSQL(pacc_createAcc_box);
        db.execSQL(partnerbox_createTable);

        //db.execSQL(moduleInsert);
        Log.d("database", "Database has been created.");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        db.execSQL(DROP_rates);
        db.execSQL(dropcity);
        db.execSQL(dropbcont);
        db.execSQL(dropprovinces);
        db.execSQL(dropdistimage);
        db.execSQL(dropwarehouse);
        db.execSQL(dropDelStat);
        db.execSQL(dropDelSubStat);
        db.execSQL(dropempinv);
        db.execSQL(dropbrgy);
        db.execSQL(dropreservesign);
        db.execSQL(dropbranch);
        db.execSQL(dropBooksign);
        db.execSQL(dropBarcodeDist);
        db.execSQL(dropBarDistBoxnumber);
        db.execSQL(dropBarcodeInventory);
        db.execSQL(dropBarcodeDriverInventory);
        db.execSQL(dropemp);
        db.execSQL(dropsd);
        db.execSQL(dropLoading);
        db.execSQL(dropLoadbox);
        db.execSQL(dropman);
        db.execSQL(dropResImg);
        db.execSQL(dropbox);
        db.execSQL(dropexpit);
        db.execSQL(drop_part_dist);
        db.execSQL(drop_part_dist_box);
        db.execSQL(drop_pacc);
        db.execSQL(drop_pacc_box);
        db.execSQL(drop_partnerbox);
        db.execSQL(dropDirect);
        db.execSQL(dropUnbi);

        // Create tables again
        onCreate(db);

    }

    //add rates
    public void addNewRates(String box, String cbm, String source,
                            String destination, String currency, String amount, String stat){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(rate_boxtype, box);
        cv.put(rate_cbm, cbm);
        cv.put(rate_source_id, source);
        cv.put(rate_destination_id, destination);
        cv.put(rate_currency_id, currency);
        cv.put(rate_amount, amount);
        cv.put(rate_recordstatus, stat);

        db.insert(tbname_rates, null, cv);
        Log.e("new rate", box);
        db.close();
    }

    public void addProvince(String name, String code, String region, String destid, String hardport){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(prov_name, name);
        cv.put(prov_code, code);
        cv.put(prov_regcode, region);
        cv.put(prov_destinationid, destid);
        cv.put(prov_hardport, hardport);
        Log.e("added province", ":"+name);
        db.insert(tbname_provinces, null, cv);
        db.close();
    }

    public void addCity(String name, String prov, String citycode){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(ct_name, name);
        cv.put(ct_prov, prov);
        cv.put(ct_citycode, citycode);

        Log.e("added city", ":"+name);
        db.insert(tbname_city, null, cv);
        db.close();
    }

    public void addBrgy(String code, String name, String prov, String citycode){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(brgy_code, code);
        cv.put(brgy_name, name);
        cv.put(brgy_prov, prov);
        cv.put(brgy_ctcode, citycode);

        Log.e("added brgy", ":"+name);
        db.insert(tbname_brgy, null, cv);
        db.close();
    }

    public String[] getAllProv(){
        Cursor cursor = getReadableDatabase().rawQuery("SELECT "+prov_name+" FROM "+ tbname_provinces
                , null);
        cursor.moveToFirst();
        ArrayList<String> numbers = new ArrayList<String>();
        while(!cursor.isAfterLast()) {
            numbers.add(cursor.getString(cursor.getColumnIndex(prov_name)));
            cursor.moveToNext();
        }
        cursor.close();
        return numbers.toArray(new String[numbers.size()]);
    }

    public String[] getAllBoxContents(){
        Cursor cursor = getReadableDatabase().rawQuery("SELECT "+bcont_desc+" FROM "+ tbname_boxcont
                , null);
        cursor.moveToFirst();
        ArrayList<String> numbers = new ArrayList<String>();
        while(!cursor.isAfterLast()) {
            numbers.add(cursor.getString(cursor.getColumnIndex(bcont_desc)));
            cursor.moveToNext();
        }
        cursor.close();
        return numbers.toArray(new String[numbers.size()]);
    }

    public String getProvCode(String prov){
        String code = null;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cx = db.rawQuery("SELECT "+prov_code+" FROM "+tbname_provinces+" WHERE "+prov_name+" = '"+prov+"'",
                null);
        if (cx.moveToNext()){
            code = cx.getString(cx.getColumnIndex(prov_code));
        }
        return code;
    }

    public String getCityCode(String city){
        String code = null;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cx = db.rawQuery("SELECT "+ct_citycode+" FROM "+tbname_city
                        +" WHERE "+ct_name+" = '"+city+"'",
                null);
        if (cx.moveToNext()){
            code = cx.getString(cx.getColumnIndex(ct_citycode));
        }
        return code;
    }

    public String[] getAllCities(String provcode){
        Cursor cursor = getReadableDatabase().rawQuery("SELECT "+ct_name+" FROM "+ tbname_city
               +" WHERE "+ct_prov+" = '"+provcode+"'" , null);
        cursor.moveToFirst();
        ArrayList<String> numbers = new ArrayList<String>();
        while(!cursor.isAfterLast()) {
            numbers.add(cursor.getString(cursor.getColumnIndex(ct_name)));
            cursor.moveToNext();
        }
        cursor.close();
        return numbers.toArray(new String[numbers.size()]);
    }

    public String[] getAllBranch(String type, String branch){
        Cursor cursor = getReadableDatabase().rawQuery("SELECT "+branch_name+" FROM "
                + tbname_branch+" WHERE "+branch_type+" = '"+type+"'", null);
        cursor.moveToFirst();
        ArrayList<String> names = new ArrayList<String>();
        while(!cursor.isAfterLast()) {
            names.add(cursor.getString(cursor.getColumnIndex(branch_name)));
            cursor.moveToNext();
        }
        cursor.close();
        return names.toArray(new String[names.size()]);
    }

    public String[] getSources(){
        Cursor cursor = getReadableDatabase().rawQuery("SELECT * FROM "
                + tbname_sourcedes+" WHERE "+sd_type+" = 'source'", null);
        cursor.moveToFirst();
        ArrayList<String> names = new ArrayList<String>();
        while(!cursor.isAfterLast()) {
            names.add(cursor.getString(cursor.getColumnIndex(sd_name)));
            cursor.moveToNext();
        }
        cursor.close();
        return names.toArray(new String[names.size()]);
    }

    public String[] getDest(){
        Cursor cursor = getReadableDatabase().rawQuery("SELECT * FROM "
                + tbname_sourcedes+" WHERE "+sd_type+" = 'destination'", null);
        cursor.moveToFirst();
        ArrayList<String> names = new ArrayList<String>();
        while(!cursor.isAfterLast()) {
            names.add(cursor.getString(cursor.getColumnIndex(sd_name)));
            cursor.moveToNext();
        }
        cursor.close();
        return names.toArray(new String[names.size()]);
    }

    public String[] getAllBrgy(String city){
        Cursor cursor = getReadableDatabase().rawQuery("SELECT "+brgy_name+" FROM "+ tbname_brgy
               +" WHERE "+brgy_ctcode+" = '"+city+"'" , null);
        cursor.moveToFirst();
        ArrayList<String> numbers = new ArrayList<String>();
        while(!cursor.isAfterLast()) {
            numbers.add(cursor.getString(cursor.getColumnIndex(brgy_name)));
            cursor.moveToNext();
        }
        cursor.close();
        return numbers.toArray(new String[numbers.size()]);
    }

    // distribution image
    public void addDistImage(String trans, byte[] image){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(distimage_trans, trans);
        cv.put(distimage_image, image);

        db.insert(tbname_dstimages, null, cv);
        db.close();
    }

    public ArrayList<HomeList> getImagesDist(String transno){
        ArrayList<HomeList> results = new ArrayList<HomeList>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor res = db.rawQuery(" SELECT * FROM "+tbname_dstimages+" WHERE "
                +distimage_trans+" = '"+transno+"'",null);
        res.moveToFirst();
        while(!res.isAfterLast()) {
            byte[] topitem = res.getBlob(res.getColumnIndex(distimage_image));
            String ids = res.getString(res.getColumnIndex(distimage_id));
            HomeList list = new HomeList(topitem, ids);
            results.add(list);
            res.moveToNext();
        }
        res.close();
        // Add some more dummy data for testing
        return results;
    }

    public boolean deleteDistImage(String id){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(tbname_dstimages, distimage_id+" = "+id, null);
        Cursor c = db.rawQuery(" SELECT * FROM "+tbname_dstimages
                +" WHERE "+distimage_id+" = '"+id+"'", null);
        if (c.getCount() == 0){
            c.close();
            db.close();
            return true;
        }else{
            c.close();
            db.close();
            return false;
        }
    }

    //warehouse
    public String[] getWarehouseName(String id) {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<String> numbers = new ArrayList<String>();
        Cursor c = db.rawQuery(" SELECT * FROM " + tbname_warehouse
                +" WHERE "+ware_branchid+" = '"+id+"'", null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            numbers.add(c.getString(c.getColumnIndex(ware_name)));
            c.moveToNext();
        }
        return numbers.toArray(new String[numbers.size()]);
    }

    public void addWarehouse(String id, String name, String branch, String stat){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(ware_id, id);
        cv.put(ware_name, name);
        cv.put(ware_branchid, branch);
        cv.put(ware_recordstatus, stat);

        db.insert(tbname_warehouse, null, cv);
        Log.e("addwarehouse", "id:"+id+", name:"+name+"," +
                " branch:"+branch);
        db.close();
    }

    public void addSign(String reservenum, byte[] sign){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(res_sign_reservation_no, reservenum);
        cv.put(res_sign_sign, sign);

        db.insert(tbname_reserve_sign, null, cv);
        db.close();
    }

    public void addBookSign(String num, byte[] sign){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(book_sign_booking_no, num);
        cv.put(book_sign_sign, sign);

        db.insert(tbname_book_sign, null, cv);
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

    public void addSD(String id, String name, String type, String stat){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(sd_id, id);
        cv.put(sd_name, name);
        cv.put(sd_type, type);
        cv.put(sd_recordstatus, stat);
        db.insert(tbname_sourcedes, null, cv);
        db.close();
    }

    //loading get boxes
    public ArrayList<LinearItem> getBox(String transno) {
        ArrayList<LinearItem> results = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor res = db.rawQuery(" SELECT * FROM " + tb_loadbox + " WHERE "
                + loadbox_trans + " = '" + transno + "'", null);
        res.moveToFirst();
        while (!res.isAfterLast()) {
            String topitem = res.getString(res.getColumnIndex(load_boxnum));
            String ids = res.getString(res.getColumnIndex(loadbox_id));
            String sub = "";
            LinearItem list = new LinearItem(ids, topitem, sub);
            results.add(list);
            res.moveToNext();
        }
        res.close();
        // Add some more dummy data for testing
        return results;
    }

    //delete one loaded box
    public void deleteOne(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(tb_loadbox, loadbox_id + " = " + id, null);
        db.close();
    }

    //load_table add
    public void addload( String trans, String box_num, String stat) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(loadbox_trans, trans);
        cv.put(load_boxnum, box_num);
        cv.put(load_box_stat, stat);

        db.insert(tb_loadbox, null, cv);
        Log.e("added", box_num);
        db.close();
    }

    //add final loading data
    public void addFinalload(String id, String loaddate, String shipper, String container,
                     String etal, String etdl, String by, String stat, String upds) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(load_id, id);
        cv.put(load_date, loaddate);
        cv.put(load_shipper, shipper);
        cv.put(load_container, container);
        cv.put(load_eta, etal);
        cv.put (load_etd, etdl);
        cv.put (load_createdby, by);
        cv.put (load_stat, stat);
        cv.put (load_upds, upds);
        db.insert(tb_loading, null, cv);
        Log.e("loading"," Add new Loading "+id );
        db.close();
    }
    //listLoad
    public ArrayList<LinearItem> getloading(String by) {
        ArrayList<LinearItem> results = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor res = db.rawQuery(" SELECT * FROM " + tb_loading
                +" WHERE "+load_createdby+" = '"+by+"'", null);
        res.moveToFirst();
        while (!res.isAfterLast()) {
            String c = res.getString(res.getColumnIndex(load_container));
            String topitem = res.getString(res.getColumnIndex(load_shipper))
            +"("+ Html.fromHtml("<small>"+c+"</small>")+")";
            String id = res.getString(res.getColumnIndex(load_id));
            String sub = res.getString(res.getColumnIndex(load_date));
            LinearItem list = new LinearItem(id, topitem, sub);
            results.add(list);
            res.moveToNext();
        }
        res.close();
// Add some more dummy data for testing
        return results;
    }

    public void addMan(String name, String date){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(man_fullname, name);
        cv.put(man_createddate, date);
        db.insert(tbname_man, null, cv);
        Log.e("addman", name);
        db.close();
    }

    public String[] getMan() {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<String> numbers = new ArrayList<String>();
        Cursor c = db.rawQuery(" SELECT * FROM " + tbname_man, null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            numbers.add(c.getString(c.getColumnIndex(man_fullname)));
            c.moveToNext();
        }
        return numbers.toArray(new String[numbers.size()]);
    }

    public String[] getAllItemsZero() {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<String> numbers = new ArrayList<String>();
        Cursor c = db.rawQuery(" SELECT * FROM " + tbname_exp_item
                +" WHERE "+expit_type+" = '1'", null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            numbers.add(c.getString(c.getColumnIndex(expit_name)));
            c.moveToNext();
        }
        return numbers.toArray(new String[numbers.size()]);
    }

    public String[] getAllItems() {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<String> numbers = new ArrayList<String>();
        Cursor c = db.rawQuery(" SELECT * FROM " + tbname_exp_item
                +" WHERE "+expit_type+" = '0'", null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            numbers.add(c.getString(c.getColumnIndex(expit_name)));
            c.moveToNext();
        }
        return numbers.toArray(new String[numbers.size()]);
    }

    public String[] getAllItemsThree() {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<String> numbers = new ArrayList<String>();
        Cursor c = db.rawQuery(" SELECT * FROM " + tbname_exp_item
                +" WHERE "+expit_type+" = '3'", null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            numbers.add(c.getString(c.getColumnIndex(expit_name)));
            c.moveToNext();
        }
        return numbers.toArray(new String[numbers.size()]);
    }

    public boolean addReserveImage(String trans, byte[] image){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(res_img_trans, trans);
        cv.put(res_img_image, image);

        db.insert(tbname_reserve_image, null, cv);
        db.close();
        return true;
    }

    public ArrayList<HomeList> getBookingImages(String transno) {
        ArrayList<HomeList> results = new ArrayList<HomeList>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery(" SELECT * FROM " + tbname_reserve_image + " WHERE "
                + res_img_trans + " = '" + transno + "'", null);
        res.moveToFirst();
        while (!res.isAfterLast()) {
            byte[] topitem = res.getBlob(res.getColumnIndex(res_img_image));
            String ids = res.getString(res.getColumnIndex(res_img_id));
            HomeList list = new HomeList(topitem, ids);
            results.add(list);
            res.moveToNext();
        }
        res.close();
        // Add some more dummy data for testing
        return results;
    }

    public void deleteImageBooking(String id){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(tbname_reserve_image, res_img_id + " = " + id, null);
        db.close();
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

    public void addPartDistributionBox(String id, String btype, String boxnumber,
                               String stat){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(partdist_box_distributionid, id);
        values.put(partdist_box_boxid, btype);
        values.put(partdist_box_boxnumber, boxnumber);
        values.put(partdist_box_stat, stat);
        db.insert(tbname_part_distribution_box, null, values);
        Log.e("partbox", ""+boxnumber
        );
        db.close();

    }

    public boolean addDistribution(String trans, String type,String mode, String typename,String driver,
                                   String truck, String rem,String eta, String status, String upstat,
                                   String date, String by, String upds, int sac, byte[] sign) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(partdist_transactionnumber, trans);
        cv.put(partdist_type, type);
        cv.put(partdist_mode, mode);
        cv.put(partdist_typename, typename);
        cv.put(partdist_drivername, driver);
        cv.put(partdist_trucknum, truck);
        cv.put(partdist_remarks, rem);
        cv.put(partdist_eta, eta);
        cv.put(partdist_status, status);
        cv.put(partdist_uploadstat, upstat);
        cv.put(partdist_createdate, date);
        cv.put(partdist_createby, by);
        cv.put(partdist_uploadstat, upds);
        cv.put(partdist_acceptstat, sac);
        cv.put(partdist_acceptsign, sign);

        db.insert(tbname_part_distribution, null, cv);
        Log.e("distribution",trans);
        db.close();

        return true;
    }

    //get specific list by user only
    public ArrayList<ThreeWayHolder> getDistributions(String by){
        ArrayList<ThreeWayHolder> results = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery(" SELECT * FROM " + tbname_part_distribution
                +" WHERE "+partdist_createby+" = '"+by+"'", null);
        res.moveToFirst();
        while (!res.isAfterLast()) {
            String disttype = res.getString(res.getColumnIndex(partdist_type));
            String ac = res.getString(res.getColumnIndex(partdist_typename));
            String id = res.getString(res.getColumnIndex(partdist_transactionnumber));
            String sub = res.getString(res.getColumnIndex(partdist_trucknum));
            String topitem = res.getString(res.getColumnIndex(partdist_typename));
            String quant = "";
            Cursor aX = db.rawQuery(" SELECT COUNT("+partdist_box_boxid+") FROM "+tbname_part_distribution_box
                    +" WHERE "+partdist_box_distributionid+" = '"+id+"' GROUP BY "+partdist_box_distributionid, null);
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

    public void updateTempBoxes(String id, String stat) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(partdist_box_stat, stat);
        db.update(tbname_part_distribution_box, cv,partdist_box_id + " = " + id, null);
        db.close();
    }

    public JSONArray getDistributionsBox(String id) {
        SQLiteDatabase myDataBase = this.getReadableDatabase();
        String raw = "SELECT "+partdist_box_id+","+partdist_box_boxid+","
                +partdist_box_boxnumber+","+partdist_box_distributionid+" FROM "
                + tbname_part_distribution_box+ " WHERE "+partdist_box_distributionid+" = '"+id+"'";
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

    public void addBoxnumPartAcceptance(String trans, String bn){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(pacc_box_trans, trans);
        cv.put(pacc_box_bnumber, bn);
        db.insert(tbname_part_acc_box, null, cv);
        Log.e("partacc", bn);
        db.close();
    }

    public void addPartAcceptance(String trans,String branch, String driver, String truck){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(pacc_trans, trans);
        cv.put(pacc_branch, branch);
        cv.put(pacc_driver, driver);
        cv.put(pacc_trnum, truck);
        db.insert(tbname_part_acc, null, cv);
        Log.e("partacc", trans);
        db.close();
    }

    public void addPartnerBox(String boxnumber, String booktrans,
                              String destination, String stat){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(partnerboxes_boxnum, boxnumber);
        cv.put(partnerboxes_booktrans, booktrans);
        cv.put(partnerboxes_destination, destination);
        cv.put(partnerboxes_actstat, stat);
        db.insert(tbname_partnerboxes_todistribute, null, cv);
        Log.e("new_partbox", "bnum: "+boxnumber+", trans: "+booktrans);
        db.close();
    }

    public void addDeliveryStatus(String id, String name, String recstat){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(delstat_id, id);
        cv.put(delstat_name, name);
        cv.put(delstat_record, recstat);
        db.insert(tbname_delivery_status, null, cv);
        db.close();
    }

    public void addDeliverySubStatus(String id, String name, String statid){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(delsubstat_id, id);
        cv.put(delsubstat_name, name);
        cv.put(delsubstat_statid, statid);
        db.insert(tbname_delivery_substatus, null, cv);
        db.close();
    }

    public void addDirectBox(String boxnumber, String stat){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(direct_boxnumber, boxnumber);
        cv.put(direct_stat, stat);
        db.insert(tbname_fordirects, null, cv);
        Log.e("direct", boxnumber);
        db.close();
    }

    public void updateDirectBox(String boxnumber){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(direct_stat, "1");
        db.update(tbname_fordirects,cv, direct_boxnumber+" = '"+boxnumber+"'", null);
        Log.e("updatedirect", boxnumber);
        db.close();
    }

    public void addNewUnloadingImage(String trans, String boxnumber, byte[] image){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(unbi_trans, trans);
        cv.put(unbi_boxnumber, boxnumber);
        cv.put(unbi_image, image);
        db.insert(tbname_unloadingbox_image, null, cv);
        Log.e("unloading_image", boxnumber);
        db.close();
    }

    public void updFinalload(String id, String loaddate, String shipper, String container,
                             String etal, String etdl, String by, String stat, String upds) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(load_id, id);
        cv.put(load_date, loaddate);
        cv.put(load_shipper, shipper);
        cv.put(load_container, container);
        cv.put(load_eta, etal);
        cv.put (load_etd, etdl);
        cv.put (load_createdby, by);
        cv.put (load_stat, stat);
        cv.put (load_upds, upds);
        db.update(tb_loading, cv, load_id+" = '"+id+"'", null);
        Log.e("update_loading",""+id );
        db.close();
    }

    public void updload( String trans, String box_num, String stat) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(loadbox_trans, trans);
        cv.put(load_boxnum, box_num);
        cv.put(load_box_stat, stat);

        db.update(tb_loadbox, cv, loadbox_trans+" = '"+trans+"'", null);
        Log.e("update_loadbox", box_num);
        db.close();
    }

    public void updateDistById(String id){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(partdist_acceptstat, "1");
        db.update(tbname_part_distribution, cv, partdist_transactionnumber+" = '"+id+"'", null);
        Log.e("update_distaccepted", id);
        db.close();
    }

    public void addBoxContent(String id, String desc){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(bcont_id, id);
        cv.put(bcont_desc, desc);
        db.insert(tbname_boxcont, null, cv);
        Log.e("boxcont", id+"/"+desc);
        db.close();
    }

    //barcode inventory
    public void addBarcodeInventory(String boxnumber, String status){
        SQLiteDatabase db =  this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(barcodeinv_boxnumber, boxnumber);
        cv.put(barcodeinv_status, status);
        db.insert(tbname_barcode_inventory, null, cv);
        Log.e("barcode_inv", boxnumber);
        db.close();
    }

    public void addBarcodeDriverInventory(String boxnumber, String status){
        SQLiteDatabase db =  this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(barcodeDriverInv_boxnumber, boxnumber);
        cv.put(barcodeDriverInv_status, status);
        db.insert(tbname_barcode_driver_inventory, null, cv);
        Log.e("barcode_driver_inv", boxnumber);
        db.close();
    }

    //barcode distribution
    public void addBarcodeDistribution(String trans, String driverid, String createddate,
                                       String createdby, String upds, String accpt){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(bardist_trans, trans);
        cv.put(bardist_driverid, driverid);
        cv.put(bardist_createddate, createddate);
        cv.put(bardist_createdby, createdby);
        cv.put(bardist_upds, upds);
        cv.put(bardist_accptstat, accpt);

        db.insert(tbname_barcode_dist, null, cv);
        Log.e("barcode_dist", trans);
        db.close();
    }

    public void addBarcodeDistributionBoxnumber(String trans, String boxnumber,String status){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(bardist_bnum_trans, trans);
        cv.put(bardist_bnum_boxnumber, boxnumber);
        cv.put(bardist_bnum_status, status);
        db.insert(tbname_barcode_dist_boxnumber, null, cv);
        Log.e("barcode_dist_boxnumber", boxnumber);
        db.close();
    }

    public ArrayList<LinearItem> getAllBarcodeReleased(String by) {
        ArrayList<LinearItem> results = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String y = " SELECT * FROM " + tbname_barcode_dist
                +" LEFT JOIN "+tbname_employee+" ON "
                +tbname_barcode_dist+"."+bardist_driverid+" = "+tbname_employee+"."+emp_id
                +" WHERE "+bardist_createdby+" = '"+by+"'";
        Cursor res = db.rawQuery( y ,null);
        res.moveToFirst();
        while (!res.isAfterLast()) {
            String topitem = res.getString(res.getColumnIndex(emp_first))+" "
                    +res.getString(res.getColumnIndex(emp_last));
            String ids = res.getString(res.getColumnIndex(bardist_trans));
            String sub = res.getString(res.getColumnIndex(bardist_createddate));
            LinearItem list = new LinearItem(ids, topitem, sub);
            results.add(list);
            res.moveToNext();
        }
        res.close();
        // Add some more dummy data for testing
        return results;
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

    public void updateBarDriverInv(String bn, String stat){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(barcodeDriverInv_status, stat);
        db.update(tbname_barcode_driver_inventory, cv, barcodeDriverInv_boxnumber+" = '"+bn+"'", null);
        Log.e("upd_driver_inv", bn);
        db.close();
    }

}
