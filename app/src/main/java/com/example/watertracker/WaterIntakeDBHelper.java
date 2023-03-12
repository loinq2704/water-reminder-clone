package com.example.watertracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WaterIntakeDBHelper extends SQLiteOpenHelper {
    // Database version
    private static final int DATABASE_VERSION = 1;
    // Database name
    private static final String DATABASE_NAME = "water_intake_db";
    // Table name
    private static final String TABLE_NAME = "water_intake";
    // Table columns
    private static final String KEY_DATE = "date";
    private static final String KEY_OZ = "oz";

    public WaterIntakeDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                + KEY_DATE + " TEXT PRIMARY KEY,"
                + KEY_OZ + " INTEGER" + ")";
        db.execSQL(CREATE_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        // Create tables again
        onCreate(db);
    }

    // Inserting a new record
    public void addIntakeRecord(IntakeRecord record) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()); // set the date format
        String date = dateFormat.format(new Date()); // get the current date
        values.put(KEY_DATE, date);
        values.put(KEY_OZ, record.getOz());

        // Check if a record with the current date already exists
        Cursor cursor = db.query(
                TABLE_NAME,
                new String[]{KEY_OZ},
                KEY_DATE + " = ?",
                new String[]{date},
                null,
                null,
                null
        );

        if (cursor.moveToFirst()) {
            // A record for today already exists, update the oz value
            int ozIndex = cursor.getColumnIndex(KEY_OZ);
            int currentOz = cursor.getInt(ozIndex);
            int newOz = currentOz + record.getOz();

            ContentValues updateValues = new ContentValues();
            updateValues.put(KEY_OZ, newOz);

            db.update(TABLE_NAME,
                    updateValues,
                    KEY_DATE + " = ?",
                    new String[]{date});
        } else {
            // No record for today exists, insert a new record
            db.insert(TABLE_NAME, null, values);
        }

        cursor.close();
        db.close();
    }

    // Updating a record
    public int updateIntakeRecord(IntakeRecord record) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_OZ, record.getOz());
        // updating row
        return db.update(TABLE_NAME, values, KEY_DATE + " = ?",
                new String[]{record.getDate()});
    }

    // Getting a record by date
    public IntakeRecord getIntakeRecordByDate(String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, new String[]{KEY_DATE, KEY_OZ},
                KEY_DATE + "=?", new String[]{date}, null, null, null, null);
        IntakeRecord record = null;
        if (cursor.moveToFirst()) {
            int dateIndex = cursor.getColumnIndex(KEY_DATE);
            int ozIndex = cursor.getColumnIndex(KEY_OZ);
            if (ozIndex >= 0) {
                record = new IntakeRecord(cursor.getString(dateIndex), cursor.getInt(ozIndex));
            }
        }
        cursor.close();
        db.close();
        return record;
    }

    // Getting all records
    public List<IntakeRecord> getAllIntakeRecords() {
        List<IntakeRecord> intakeRecords = new ArrayList<>();
        String selectQuery = "SELECT  * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                IntakeRecord record = new IntakeRecord();
                record.setDate(cursor.getString(0));
                record.setOz(cursor.getInt(1));
                // Adding record to list
                intakeRecords.add(record);
            } while (cursor.moveToNext());
        }
        // close the cursor
        cursor.close();
        // close the database
        db.close();
        // return the list of records
        return intakeRecords;
    }

    // Deleting a record
    public void deleteIntakeRecord(IntakeRecord record) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, KEY_DATE + " = ?",
                new String[]{record.getDate()});
        db.close();
    }
}
