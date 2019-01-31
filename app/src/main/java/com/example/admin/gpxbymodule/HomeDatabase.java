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

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class HomeDatabase extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "home.db";

    //all modules
    public final String tbname_modules = "Modules";
    public final String mod_id = "Id";
    public final String mod_name = "ModuleName";
    public final String mod_image = "ModuleImage";
    public final String mod_user = "ModuleUser";

    //all modules
    public final String tbname_submenu = "SubMenu";
    public final String menu_id = "Id";
    public final String menu_image = "ModuleImage";
    public final String menu_name = "ModuleName";

    //table user role
    public final String tbname_userrole = "gpx_userlogin";
    public final String role_id = "id";
    public final String role_employeeid = "employee_id";
    public final String role_name = "username";
    public final String role_email = "email";
    public final String role_fullname = "fullname";
    public final String role_role = "role";
    public final String role_branch = "branch";

    //table user role
    public final String tbname_tbroles = "Roles";
    public final String roles_id = "id";
    public final String roles_name = "name";
    public final String roles_description = "description";
    public final String roles_status = "recordstatus";

    //table config link
    public final String tbname_configlink = "Configuration";
    public final String config_id = "id";
    public final String config_link = "Website_link";
    public final String createConfigLink = " CREATE TABLE " + tbname_configlink + "("
            + config_id + " INTEGER PRIMARY KEY AUTOINCREMENT," + config_link + " TEXT )";
    private String droplink = "DROP TABLE IF EXISTS " + tbname_configlink;

    //create table modules
    private String createModules = " CREATE TABLE " + tbname_modules + "("
            + mod_id + " INTEGER PRIMARY KEY AUTOINCREMENT," + mod_name + " TEXT,"
            + mod_image + " BLOB, "+mod_user+" TEXT )";


    //create table roles
    private String createRoles = " CREATE TABLE " + tbname_tbroles + "("
            + roles_id + " INTEGER PRIMARY KEY UNIQUE,"
            + roles_name + " TEXT,"
            + roles_description + " TEXT, "
            +roles_status+" TEXT )";

    //create table modules
    private String createSubMenu = " CREATE TABLE " + tbname_submenu + "("
            + menu_id + " INTEGER PRIMARY KEY AUTOINCREMENT," + menu_image + " TEXT, "+ menu_name + " TEXT )";

    //create table modules
    private String createRole = " CREATE TABLE " + tbname_userrole + "("
            + role_id + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + role_employeeid + " INTEGER, "
            + role_name + " TEXT, "
            + role_email + " TEXT, "
            + role_fullname + " TEXT, "
            + role_branch + " TEXT, "
            + role_role + " TEXT )";

    //drop queries
    private String DROP_modules = "DROP TABLE IF EXISTS " + tbname_modules;
    private String DROP_submenu = "DROP TABLE IF EXISTS " + tbname_submenu;
    private String DROP_role = "DROP TABLE IF EXISTS " + tbname_userrole;
    private String DROP_rolestb = "DROP TABLE IF EXISTS " + tbname_tbroles;

    public HomeDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(createModules);
        db.execSQL(createSubMenu);
        db.execSQL(createRole);
        db.execSQL(createRoles);
        db.execSQL(createConfigLink);

        //db.execSQL(moduleInsert);
        Log.d("database", "Database has been created.");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DROP_modules);
        db.execSQL(DROP_submenu);
        db.execSQL(DROP_role);
        db.execSQL(DROP_rolestb);
        db.execSQL(droplink);

        // Create tables again
        onCreate(db);

    }

    public boolean count(){
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(" SELECT * FROM "+tbname_modules, null);

        if (c.getCount() == 0){
            return true;
        }else{
            return false;
        }
    }

    public ArrayList<HomeList> getData(String accnt){
        ArrayList<HomeList> results = new ArrayList<HomeList>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor res = db.rawQuery(" SELECT * FROM "+tbname_modules+" WHERE "+mod_user+ " = '"+accnt+"'",null);
        res.moveToFirst();
        while(!res.isAfterLast()) {

            byte[] topitem = res.getBlob(res.getColumnIndex(mod_image));
            String subitem = res.getString(res.getColumnIndex(mod_name));

            HomeList list = new HomeList(topitem, subitem);
            results.add(list);

            res.moveToNext();
        }
        // Add some more dummy data for testing
        return results;
    }

    public String[] getModuleNames(String type){
        Cursor cursor = getReadableDatabase().rawQuery("SELECT * FROM "+ tbname_modules+ " WHERE "+mod_user+ " = '"+type+"'", null);
        cursor.moveToFirst();
        ArrayList<String> names = new ArrayList<String>();
        while(!cursor.isAfterLast()) {
            names.add(cursor.getString(cursor.getColumnIndex(mod_name)));
            cursor.moveToNext();
        }
        cursor.close();
        return names.toArray(new String[names.size()]);
    }

    public ArrayList<HomeList> getSubmenu(){
        ArrayList<HomeList> results = new ArrayList<HomeList>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor res = db.rawQuery(" SELECT * FROM "+tbname_submenu,null);
        res.moveToFirst();
        while(!res.isAfterLast()) {

            byte[] top = res.getBlob(res.getColumnIndex(menu_image));
            String subitem = res.getString(res.getColumnIndex(menu_name));

            HomeList list = new HomeList(top, subitem);
            results.add(list);

            res.moveToNext();
        }
        // Add some more dummy data for testing
        return results;
    }

    public void addModule(String name, byte[] image, String user){
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(mod_name, name);
        values.put(mod_image, image);
        values.put(mod_user, user);

        db.insert(tbname_modules, null, values);
        db.close();

        Log.d("add data", ""+values);
    }

    public void addSubMenu(byte[] image, String name){
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(menu_image, image);
        values.put(menu_name, name);

        db.insert(tbname_submenu, null, values);
        db.close();

        Log.d("add data", ""+values);
    }

    public void loggedRole(String id, String user,String mail, String fullname,
                           String role, String branch){
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(role_employeeid, id);
        values.put(role_name, user);
        values.put(role_email, mail);
        values.put(role_fullname, fullname);
        values.put(role_role, role);
        values.put(role_branch, branch);

        db.insert(tbname_userrole, null, values);
        Log.e("insert_role", user);
        db.close();
    }

    public String getEmail(String id){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(" SELECT * FROM "+tbname_userrole
                +" WHERE "+role_employeeid+" = '"+id+"'", null);
        c.moveToNext();
        return c.getString(c.getColumnIndex(role_email));
    }

    public String getFullname(String id){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(" SELECT * FROM "+tbname_userrole
                +" WHERE "+role_employeeid+" = '"+id+"'", null);
        c.moveToNext();
        return c.getString(c.getColumnIndex(role_fullname));
    }

    public int logcount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(" SELECT * FROM " + tbname_userrole, null);
        c.moveToNext();
        int roleid = 0;
        if (c.getCount() != 0) {
            roleid = c.getInt(c.getColumnIndex(role_employeeid));
        }
        c.close();
        return roleid;

    }

    public String getRole(int id){
        String role = null;
        SQLiteDatabase db = this.getReadableDatabase();
            Cursor getrole = db.rawQuery(" SELECT * FROM "+tbname_tbroles+" LEFT JOIN "+tbname_userrole
                    +" ON "+tbname_tbroles+"."+roles_id+" = "+tbname_userrole+"."+role_role
                    +" WHERE "+tbname_userrole+"."+role_employeeid+" = '"+id+"'", null);
            if (getrole.moveToNext()){
                role = getrole.getString(getrole.getColumnIndex(roles_name));
            }
        return role;
    }

    public void logout(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(" DELETE FROM "+ tbname_userrole);
        Log.e("logout", logcount()+"");
        db.close();
    }

    public boolean countRole(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(" SELECT * FROM "+ tbname_tbroles, null);
        if (c.getCount() == 0){
            return true;
        }
        return false;
    }

    public void addLink(String link){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(config_link, link);
        db.insert(tbname_configlink, null, cv);
        db.close();
    }

    public String getUrl(){
        SQLiteDatabase db = this.getReadableDatabase();
        String url = null;
        Cursor c = db.rawQuery(" SELECT "+config_link+ " FROM "+tbname_configlink, null);
        if (c.moveToNext()){
            url = c.getString(c.getColumnIndex(config_link));
        }
        return url;
    }

    public boolean deletelink(String link){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(tbname_configlink, config_link+" = '"+link+"'", null);
        db.close();
        return true;
    }

    public String getBranch(String id){
        String i = null;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(" SELECT * FROM "+tbname_userrole
                +" WHERE "+role_employeeid+" = '"+id+"'", null);
        if (c.moveToNext()){
            i = c.getString(c.getColumnIndex(role_branch));
        }
        return i;
    }


}
