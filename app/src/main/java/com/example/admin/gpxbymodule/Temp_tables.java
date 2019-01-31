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
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class Temp_tables extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "temporaries";

    //create table rates
    public final String tbname_rates = "gpx_boxrate";
    public final String rate_id = "gpx_boxrate";
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
            + rate_id + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + rate_boxtype + " TEXT UNIQUE, "
            + rate_cbm + " TEXT, "
            + rate_source_id + " TEXT, "
            + rate_destination_id + " TEXT, "
            + rate_currency_id + " TEXT, "
            + rate_amount + " TEXT, "
            + rate_recordstatus + " TEXT )";
    //drop queries
    private String DROP_rates = "DROP TABLE IF EXISTS " + tbname_rates;

    public Temp_tables(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        //rates
        db.execSQL(createRates);

        //db.execSQL(moduleInsert);
        Log.d("database", "Database has been created.");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        db.execSQL(DROP_rates);

        // Create tables again
        onCreate(db);

    }

}
