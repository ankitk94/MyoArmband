
package thalmiclabs.myoarmband;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler extends SQLiteOpenHelper {

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "passpose";

    private static final String TABLE_MAPPING = "mapping";

    private static final String TABLE_PASSPOSE = "passpose";

    private static final String KEY_ID = "id";
    private static final String KEY_POSE = "pose";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_MAPPING_TABLE = "CREATE TABLE " + TABLE_MAPPING + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_POSE + " INTEGER"
                + ")";
        db.execSQL(CREATE_MAPPING_TABLE);

        String CREATE_PASSPOSE_TABLE = "CREATE TABLE " + TABLE_PASSPOSE + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_POSE + " INTEGER"
                + ")";
        db.execSQL(CREATE_PASSPOSE_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MAPPING);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PASSPOSE);

        // Create tables again
        onCreate(db);
    }

    void addPoseMapping(PassposeMapping passposeMapping) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_POSE, passposeMapping.getPose());

        // Inserting Row
        db.insert(TABLE_MAPPING, null, values);
        db.close(); // Closing database connection
    }

    PassposeMapping getPassposeMapping(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_MAPPING, new String[]{KEY_ID,
                        KEY_POSE}, KEY_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        PassposeMapping passposeMapping = new PassposeMapping(Integer.parseInt(cursor.getString(0)),
                cursor.getString(1));
        // return contact
        return passposeMapping;
    }

    public List<PassposeMapping> getAllPassposeMappings() {
        List<PassposeMapping> PassposeMappingList = new ArrayList<PassposeMapping>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_MAPPING;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                PassposeMapping contact = new PassposeMapping();
                contact.setID(Integer.parseInt(cursor.getString(0)));
                contact.setPose(cursor.getString(1));
                PassposeMappingList.add(contact);
            } while (cursor.moveToNext());
        }

        // return contact list
        return PassposeMappingList;
    }

    void addPasspose(Passpose passpose) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_POSE, passpose.getPose());

        // Inserting Row
        db.insert(TABLE_PASSPOSE, null, values);
        db.close(); // Closing database connection
    }

    public ArrayList<Integer> getAllPasspose() {
        ArrayList<Integer> PassposeList = new ArrayList<Integer>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_PASSPOSE;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                PassposeList.add(Integer.parseInt(cursor.getString(1)));
            } while (cursor.moveToNext());
        }

        // return contact list
        return PassposeList;
    }



}