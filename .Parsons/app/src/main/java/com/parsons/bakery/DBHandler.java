package com.parsons.bakery;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class DBHandler extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_NAME = "info";
    private static final String ID = "id";
    private static final String COLUMN_ORDER_BY_OPTIONS = "order_of_options";

    private static final String TABLE_RECENT = "recent";
    private static final String COLUMN_RECENT_NAME = "name";
    private static final String COLUMN_RECENT_VISITS = "visits";
    private static final String COLUMN_LAST_VISIT = "last_visit";

    private static final String TABLE_CATEGORIES = "categories";
    private static final String COLUMN_CATEGORY_NAME = "name";
    private static final String COLUMN_CATEGORY_IMAGE = "img";
    private static final String COLUMN_CATEGORY_LEVEL = "level";
    private static final String COLUMN_CATEGORY_HAS_LEVELS = "has_levels";

    private static final String TABLE_MENU = "menu";
    private static final String COLUMN_MENU_NAME = "name";
    private static final String COLUMN_MENU_CATEGORY = "category";
    private static final String COLUMN_MENU_IMAGE = "img";
    private static final String COLUMN_MENU_DESCRIPTION = "description";
    private static final String COLUMN_MENU_REQ = "req";
    private static final String COLUMN_MENU_INNER_CATEGORY = "inner_category";
    private static final String COLUMN_MENU_USE_INNER = "use_inner";

    private static final String TABLE_CUSTOMIZATIONS = "customizations";
    private static final String COLUMN_CUSTOMIZATIONS_TYPE = "type";
    private static final String COLUMN_CUSTOMIZATIONS_OPTIONS = "options";
    private static final String COLUMN_CUSTOMIZATIONS_ITEM = "item";
    private static final String COLUMN_CUSTOMIZATIONS_TITLE = "title";
    private static final String COLUMN_CUSTOMIZATIONS_IS_REQUIRED = "is_required";

    private static final String TABLE_DOWNLOADED = "downloaded";
    private static final String COLUMN_LAST_ACCESSED = "lastAccessed";
    private static final String COLUMN_URL = "url";

    private static final String TABLE_CART = "cart";
    private static final String COLUMN_CART_NAME = "name";
    private static final String COLUMN_CART_CUSTOMIZATIONS = "customizations";
    private static final String COLUMN_CART_COUNT = "count";
    private static final String COLUMN_CART_OTHER = "other";

    private static final String TABLE_PREVIOUS_ORDERS = "previous_orders";
    private static final String COLUMN_PREVIOUS_ORDERS_ORDER = "theOrder";
    private static final String COLUMN_PREVIOUS_ORDERS_TIME = "time";

    private static final String TABLE_ACCT = "acct";
    private static final String COLUMN_ACCT_NAME = "name";
    private static final String COLUMN_ACCT_USERNAME = "username";
    private static final String COLUMN_ACCT_IS_BAKER = "is_baker";
    private static final String COLUMN_ACCT_UNIQUE_ID = "unique_id";

    private static final String TABLE_ORDERS = "orders";
    private static final String COLUMN_ORDERS_ORDER_PLACED = "order_placed";
    private static final String COLUMN_ORDERS_TIME_PLACED = "time_placed";
    private static final String COLUMN_ORDERS_NEEDS_VERIFICATION = "needs_verification";

    private static final String TABLE_PEOPLE = "people";
    private static final String COLUMN_PEOPLE_NAME ="name";
    private static final String COLUMN_PEOPLE_PAST_MESSAGES = "past_messages";
    private static final String COLUMN_PEOPLE_UNIQUE_ID = "unique_id";

    public static final String RecentCreate = "CREATE TABLE " + TABLE_RECENT + "(" +
            ID + " INTEGER NOT NULL PRIMARY KEY, " +
            COLUMN_RECENT_NAME + " VARCHAR(255), " +
            COLUMN_RECENT_VISITS + " INTEGER, " +
            COLUMN_LAST_VISIT + " VARCHAR(255))";

    public static final String CategoriesCreate = "CREATE TABLE " + TABLE_CATEGORIES + "(" +
            ID + " INTEGER NOT NULL PRIMARY KEY, " +
            COLUMN_CATEGORY_NAME + " VARCHAR(255), " +
            COLUMN_CATEGORY_IMAGE + " VARCHAR(255), " +
            COLUMN_CATEGORY_LEVEL + " INTEGER, " +
            COLUMN_CATEGORY_HAS_LEVELS + " INTEGER)";

    public static final String MenuCreate = "CREATE TABLE " + TABLE_MENU + "(" +
            ID + " INTEGER NOT NULL PRIMARY KEY, " +
            COLUMN_MENU_NAME + " VARCHAR(255), " +
            COLUMN_MENU_CATEGORY + " VARCHAR(255), " +
            COLUMN_MENU_IMAGE + " VARCHAR(255), " +
            COLUMN_MENU_DESCRIPTION + " VARCHAR(255), " +
            COLUMN_MENU_REQ + " VARCHAR(255), " +
            COLUMN_MENU_INNER_CATEGORY + " VARCHAR(255), " +
            COLUMN_MENU_USE_INNER + " INTEGER, " +
            COLUMN_ORDER_BY_OPTIONS + " INTEGER)";

    public static final String CustomizationsCreate = "CREATE TABLE " + TABLE_CUSTOMIZATIONS + "(" +
            ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_CUSTOMIZATIONS_TYPE + " VARCHAR(255), " +
            COLUMN_CUSTOMIZATIONS_OPTIONS + " VARCHAR(255), " +
            COLUMN_CUSTOMIZATIONS_ITEM + " VARCHAR(255), " +
            COLUMN_CUSTOMIZATIONS_TITLE + " VARCHAR(255), " +
            COLUMN_ORDER_BY_OPTIONS + " INTEGER, " +
            COLUMN_CUSTOMIZATIONS_IS_REQUIRED + " INTEGER)";

    public static final String DownloadedCreate = "CREATE TABLE " + TABLE_DOWNLOADED + "(" +
            ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_LAST_ACCESSED + " VARCHAR(255), " +
            COLUMN_URL + " VARCHAR(255))";

    public static final String CartCreate = "CREATE TABLE " + TABLE_CART + "(" +
            ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_CART_NAME + " VARCHAR(255), " +
            COLUMN_CART_CUSTOMIZATIONS + " BLOB," +
            COLUMN_CART_COUNT + " INTEGER, " +
            COLUMN_CART_OTHER + " INTEGER)";

    public static final String PreviousOrderCreate = "CREATE TABLE " + TABLE_PREVIOUS_ORDERS + "(" +
            ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_PREVIOUS_ORDERS_ORDER + " MEDIUMTEXT, " +
            COLUMN_PREVIOUS_ORDERS_TIME + " VARCHAR(255))";

    public static final String AcctCreate = "CREATE TABLE " + TABLE_ACCT + "(" +
            ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_ACCT_USERNAME + " VARCHAR(45), " +
            COLUMN_ACCT_NAME + " VARCHAR(45), " +
            COLUMN_ACCT_IS_BAKER + " INTEGER default 0, " +
            COLUMN_ACCT_UNIQUE_ID + " INTEGER UNIQUE)";

    public static final String OrdersCreate = "CREATE TABLE " + TABLE_ORDERS + "(" +
            ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_ORDERS_ORDER_PLACED + " TEXT, " +
            COLUMN_ORDERS_TIME_PLACED + " VARCHAR(45), " +
            COLUMN_ORDERS_NEEDS_VERIFICATION + " INTEGER)";

    public static final String PeopleCreate = "CREATE TABLE " + TABLE_PEOPLE + "(" +
            ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_PEOPLE_NAME + " VARCHAR(45), " +
            COLUMN_PEOPLE_PAST_MESSAGES + " LONGTEXT, " +
            COLUMN_PEOPLE_UNIQUE_ID + " TEXT UNIQUE)";

    SQLiteDatabase database;

    public DBHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        database = getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(RecentCreate);
        db.execSQL(CategoriesCreate);
        db.execSQL(MenuCreate);
        db.execSQL(CustomizationsCreate);
        db.execSQL(DownloadedCreate);
        db.execSQL(CartCreate);
        db.execSQL(PreviousOrderCreate);
        db.execSQL(AcctCreate);
        db.execSQL(OrdersCreate);
        db.execSQL(PeopleCreate);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECENT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MENU);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CUSTOMIZATIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DOWNLOADED);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CART);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PREVIOUS_ORDERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACCT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PEOPLE);
        onCreate(db);
    }

    public List<HashMap<String, String>> executeOne(String Query) {
        return cursorToHashMap(database.rawQuery(Query, null));
    }

    public static ArrayList<HashMap<String, String>> cursorToHashMap(
            Cursor cursor) {

        if (cursor != null) {
            int cursorCount = cursor.getCount();
            int columnCount;
            ArrayList<HashMap<String, String>> cursorData = new ArrayList<HashMap<String, String>>();
            HashMap<String, String> rowHashMap;
            for (int i = 0; i < cursorCount; i++) {
                cursor.moveToPosition(i);
                rowHashMap = new HashMap<String, String>();
                columnCount = cursor.getColumnCount();
                for (int j = 0; j < columnCount; j++) {
                    rowHashMap.put(cursor.getColumnName(j),
                            cursor.getString(j));
                }
                cursorData.add(rowHashMap);
            }
            cursor.close();

            return cursorData;
        } else {
            return null;
        }
    }

    public ArrayList<Cursor> execute(String Query){
        //get writable database
        //get writable database
        SQLiteDatabase sqlDB = this.getWritableDatabase();
        String[] columns = new String[] { "message" };
        //an array list of cursor to save two cursors one has results from the query
        //other cursor stores error message if any errors are triggered
        ArrayList<Cursor> alc = new ArrayList<Cursor>(2);
        MatrixCursor Cursor2= new MatrixCursor(columns);
        alc.add(null);
        alc.add(null);

        try{
            String maxQuery = Query ;
            //execute the query results will be save in Cursor c
            Cursor c = sqlDB.rawQuery(maxQuery, null);

            //add value to cursor2
            Cursor2.addRow(new Object[] { "Success" });

            alc.set(1,Cursor2);
            if (null != c && c.getCount() > 0) {

                alc.set(0,c);
                c.moveToFirst();

                return alc ;
            }
            return alc;
        } catch(SQLException sqlEx){
            Log.d("printing exception", sqlEx.getMessage());
            //if any exceptions are triggered save the error message to cursor an return the arraylist
            Cursor2.addRow(new Object[] { ""+sqlEx.getMessage() });
            alc.set(1,Cursor2);
            return alc;
        } catch(Exception ex){
            Log.d("printing exception", ex.getMessage());

            //if any exceptions are triggered save the error message to cursor an return the arraylist
            Cursor2.addRow(new Object[] { ""+ex.getMessage() });
            alc.set(1,Cursor2);
            return alc;
        }
    }

}