package com.parsons.bakery;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class DBHandler extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;

    public static final String DATABASE_NAME = "info";
    public static final String ID = "id";
    public static final String COLUMN_ORDER_BY_OPTIONS = "order_of_options";

    public static final String TABLE_RECENT = "recent";
    public static final String COLUMN_RECENT_NAME = "name";
    public static final String COLUMN_RECENT_VISITS = "visits";
    public static final String COLUMN_LAST_VISIT = "last_visit";

    public static final String TABLE_HOME_IMAGES = "home_images";
    public static final String COLUMN_HOME_IMAGES_PATH = "path";

    public static final String TABLE_CATEGORIES = "categories";
    public static final String COLUMN_CATEGORY_NAME = "name";
    public static final String COLUMN_CATEGORY_IMAGE = "img";
    public static final String COLUMN_CATEGORY_LEVEL = "level";
    public static final String COLUMN_CATEGORY_HAS_LEVELS = "has_levels";
    public static final String COLUMN_CATEGORY_ORDERING = "ordering";

    public static final String TABLE_MENU = "menu";
    public static final String COLUMN_MENU_NAME = "name";
    public static final String COLUMN_MENU_CATEGORY = "category";
    public static final String COLUMN_MENU_IMAGE = "img";
    public static final String COLUMN_MENU_DESCRIPTION = "description";
    public static final String COLUMN_MENU_REQ = "req";
    public static final String COLUMN_MENU_INNER_CATEGORY = "inner_category";
    public static final String COLUMN_MENU_USE_INNER = "use_inner";
    public static final String COLUMN_MENU_PRICE = "price";
    public static final String COLUMN_MENU_DZN_PRICE = "dzn_price";

    public static final String TABLE_CUSTOMIZATIONS = "customizations";
    public static final String COLUMN_CUSTOMIZATIONS_TYPE = "type";
    public static final String COLUMN_CUSTOMIZATIONS_OPTIONS = "options";
    public static final String COLUMN_CUSTOMIZATIONS_ITEM = "item";
    public static final String COLUMN_CUSTOMIZATIONS_TITLE = "title";
    public static final String COLUMN_CUSTOMIZATIONS_IS_REQUIRED = "is_required";

    public static final String TABLE_DOWNLOADED = "downloaded";
    public static final String COLUMN_DOWNLOADED_LAST_ACCESSED = "lastAccessed";
    public static final String COLUMN_DOWNLOADED_URL = "url";

    public static final String TABLE_CART = "cart";
    public static final String COLUMN_CART_NAME = "name";
    public static final String COLUMN_CART_CUSTOMIZATIONS = "customizations";
    public static final String COLUMN_CART_COUNT = "count";
    public static final String COLUMN_CART_OTHER = "other";

    public static final String TABLE_ACCT = "acct";
    public static final String COLUMN_ACCT_FIRSTNAME = "firstname";
    public static final String COLUMN_ACCT_LASTNAME = "lastname";
    public static final String COLUMN_ACCT_USERNAME = "username";
    public static final String COLUMN_ACCT_ACCOUNT_TYPE = "account_type";
    public static final String COLUMN_ACCT_UNIQUE_ID = "unique_id";
    public static final String COLUMN_ACCT_PHONE = "phone";
    public static final String COLUMN_ACCT_EMAIL = "email";

    public static final String TABLE_ORDERS = "orders";
    public static final String COLUMN_ORDERS_ORDER_ID = "order_id";
    public static final String COLUMN_ORDERS_USERID = "user_id";
    public static final String COLUMN_ORDERS_NAME = "name";
    public static final String COLUMN_ORDERS_NUMBER = "number";
    public static final String COLUMN_ORDERS_PRICE = "price";
    public static final String COLUMN_ORDERS_NUMBER_OF_ITEMS = "number_of_items";
    public static final String COLUMN_ORDERS_TIME_PLACED = "time_placed";
    public static final String COLUMN_ORDERS_TIME_PICKUP = "time_pickup";
    public static final String COLUMN_ORDERS_NEEDS_VERIFICATION = "needs_verification";
    public static final String COLUMN_ORDERS_IS_CURRENT = "is_current";

    public static final String TABLE_ORDER_ITEMS = "order_items";
    public static final String COLUMN_ORDER_ITEMS_ORDER_ID = "order_id";
    public static final String COLUMN_ORDER_ITEMS_COUNT = "count";
    public static final String COLUMN_ORDER_ITEMS_CUSTOMIZATIONS = "customizations";
    public static final String COLUMN_ORDER_ITEMS_ITEM = "item";
    public static final String COLUMN_ORDER_ITEMS_TYPE = "type";

    public static final String TABLE_PEOPLE = "people";
    public static final String COLUMN_PEOPLE_NAME ="name";
    public static final String COLUMN_PEOPLE_PAST_MESSAGES = "past_messages";
    public static final String COLUMN_PEOPLE_UNIQUE_ID = "unique_id";

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
            COLUMN_CATEGORY_ORDERING + " INTEGER, " +
            COLUMN_CATEGORY_HAS_LEVELS + " BOOLEAN)";

    public static final String MenuCreate = "CREATE TABLE " + TABLE_MENU + "(" +
            ID + " INTEGER NOT NULL PRIMARY KEY, " +
            COLUMN_MENU_NAME + " VARCHAR(255), " +
            COLUMN_MENU_CATEGORY + " VARCHAR(255), " +
            COLUMN_MENU_IMAGE + " VARCHAR(255), " +
            COLUMN_MENU_DESCRIPTION + " VARCHAR(255), " +
            COLUMN_MENU_REQ + " VARCHAR(255), " +
            COLUMN_MENU_INNER_CATEGORY + " VARCHAR(255), " +
            COLUMN_MENU_USE_INNER + " INTEGER, " +
            COLUMN_MENU_PRICE + " DOUBLE DEFAULT 0.00, " +
            COLUMN_MENU_DZN_PRICE + " DOUBLE DEFAULT 0.00" +
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
            COLUMN_DOWNLOADED_LAST_ACCESSED + " VARCHAR(255), " +
            COLUMN_DOWNLOADED_URL + " VARCHAR(255))";

    public static final String CartCreate = "CREATE TABLE " + TABLE_CART + "(" +
            ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_CART_NAME + " VARCHAR(255), " +
            COLUMN_CART_CUSTOMIZATIONS + " BLOB," +
            COLUMN_CART_COUNT + " INTEGER, " +
            COLUMN_CART_OTHER + " INTEGER)";


    public static final String TableImagesCreate = "CREATE TABLE " + TABLE_HOME_IMAGES + "(" +
            ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_HOME_IMAGES_PATH + " VARCHAR(255))";
    public static final String AcctCreate = "CREATE TABLE " + TABLE_ACCT + "(" +
            ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_ACCT_USERNAME + " VARCHAR(45), " +
            COLUMN_ACCT_FIRSTNAME + " VARCHAR(45), " +
            COLUMN_ACCT_LASTNAME + " VARCHAR(45), " +
            COLUMN_ACCT_ACCOUNT_TYPE + " INTEGER default -1, " +
            COLUMN_ACCT_PHONE + " INTEGER, " +
            COLUMN_ACCT_EMAIL + " VARCHAR(150), " +
            COLUMN_ACCT_UNIQUE_ID + " VARCHAR(255) UNIQUE default 0)";

    public static final String OrdersCreate = "CREATE TABLE " + TABLE_ORDERS + "(" +
            ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_ORDERS_ORDER_ID + " VARCHAR(255), " +
            COLUMN_ORDERS_USERID + " VARCHAR(255), " +
            COLUMN_ORDERS_NAME + " VARCHAR(255), " +
            COLUMN_ORDERS_NUMBER + " INTEGER, " +
            COLUMN_ORDERS_TIME_PICKUP + " VARCHAR(255), " +
            COLUMN_ORDERS_NUMBER_OF_ITEMS + " INTEGER, " +
            COLUMN_ORDERS_TIME_PLACED + " VARCHAR(45), " +
            COLUMN_ORDERS_NEEDS_VERIFICATION + " INTEGER, " +
            COLUMN_ORDERS_PRICE + " DOUBLE DEFAULT 0.00, " +
            COLUMN_ORDERS_IS_CURRENT + " INTEGER)";

    public static final String OrderItemsCreate = "CREATE TABLE " + TABLE_ORDER_ITEMS + "(" +
            ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_ORDER_ITEMS_ORDER_ID + " VARCHAR(255), " +
            COLUMN_ORDER_ITEMS_COUNT + " INTEGER, " +
            COLUMN_ORDER_ITEMS_ITEM + " VARCHAR(255), " +
            COLUMN_ORDER_ITEMS_TYPE + " VARCHAR(255), " +
            COLUMN_ORDER_ITEMS_CUSTOMIZATIONS + " TEXT)";

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
        db.execSQL(AcctCreate);
        db.execSQL("INSERT INTO " + TABLE_ACCT + " DEFAULT VALUES");
        db.execSQL(OrdersCreate);
        db.execSQL(PeopleCreate);
        db.execSQL(OrderItemsCreate);
        db.execSQL(TableImagesCreate);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECENT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MENU);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CUSTOMIZATIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DOWNLOADED);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CART);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACCT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PEOPLE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDER_ITEMS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HOME_IMAGES);
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
            ArrayList<HashMap<String, String>> cursorData = new ArrayList<>();
            HashMap<String, String> rowHashMap;
            for (int i = 0; i < cursorCount; i++) {
                cursor.moveToPosition(i);
                rowHashMap = new HashMap<>();
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
            //execute the query results will be save in Cursor c
            Cursor c = sqlDB.rawQuery(Query, null);

            //add value to cursor2
            Cursor2.addRow(new Object[] { "Success" });

            alc.set(1,Cursor2);
            if (null != c && c.getCount() > 0) {

                alc.set(0,c);
                c.moveToFirst();

                return alc ;
            }
            return alc;
        } catch(Exception ex){
            Log.d("printing exception", Objects.requireNonNull(ex.getMessage()));

            //if any exceptions are triggered save the error message to cursor an return the arraylist
            Cursor2.addRow(new Object[] {ex.getMessage()});
            alc.set(1,Cursor2);
            return alc;
        }
    }

}