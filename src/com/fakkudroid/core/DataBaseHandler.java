package com.fakkudroid.core;

import com.fakkudroid.bean.SettingBean;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DataBaseHandler extends SQLiteOpenHelper {

	// All Static variables
	// Database Version
	private static final int DATABASE_VERSION = 1;

	// Database Name
	private static final String DATABASE_NAME = "doujinsDB";

	// Contacts table name
	private static final String TABLE_SETTINGS = "settings";

	// Contacts Table Columns names
	private static final String KEY_ID = "id";
	private static final String KEY_USER = "user";
	private static final String KEY_PASSWORD = "password";
	private static final String KEY_CHECKED = "checked";
	private static final String KEY_READING_MODE = "reading_mode";
	private static final String KEY_PIN = "pin";

	public DataBaseHandler(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String CREATE_DOUJIN_SETTINGS = "CREATE TABLE " + TABLE_SETTINGS + "("
				+ KEY_ID + " TEXT PRIMARY KEY," + KEY_USER + " TEXT,"
				+ KEY_PASSWORD + " TEXT" + "," + KEY_PIN + " TEXT" + ","
				+ KEY_CHECKED + " INTEGER" + "," + KEY_READING_MODE
				+ " INTEGER" + ")";

		db.execSQL(CREATE_DOUJIN_SETTINGS);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Drop older table if existed
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_SETTINGS);

		// Create tables again
		onCreate(db);

	}

	public SettingBean addSetting() {
		SettingBean bean = new SettingBean();
		bean.setUser("");
		bean.setPassword("");
		bean.setChecked(false);
		bean.setReading_mode(SettingBean.JAPANESE_MODE);

		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();

		values.put(KEY_USER, bean.getUser());
		values.put(KEY_PASSWORD, bean.getPassword());
		values.put(KEY_CHECKED, bean.isChecked() ? 1 : 0);
		values.put(KEY_READING_MODE, bean.getReading_mode());
		values.put(KEY_PIN, bean.getPin());

		// Inserting Row
		db.insert(TABLE_SETTINGS, null, values);
		db.close(); // Closing database connection

		return bean;
	}

	public SettingBean getSetting() {
		SettingBean result = null;
		// Select All Query
		String selectQuery = "SELECT " + KEY_ID + "," + KEY_USER + ","
				+ KEY_PASSWORD + "," + KEY_CHECKED + "," + KEY_READING_MODE
				+ "," + KEY_PIN + " FROM " + TABLE_SETTINGS;

		Log.i(this.getClass().toString(), selectQuery);

		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		// looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			result = new SettingBean();
			result.setUser(cursor.getString(1));
			result.setPassword(cursor.getString(2));
			result.setChecked(cursor.getInt(3) == 1);
			result.setReading_mode(cursor.getInt(4));
			result.setPin(cursor.getString(5));
		}
		
		db.close();
		// return contact list
		return result;
	}

	public void updateSetting(SettingBean bean) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();

		values.put(KEY_USER, bean.getUser());
		values.put(KEY_PASSWORD, bean.getPassword());
		values.put(KEY_CHECKED, bean.isChecked() ? 1 : 0);
		values.put(KEY_READING_MODE, bean.getReading_mode());
		values.put(KEY_PIN, bean.getPin());

		// Inserting Row
		db.update(TABLE_SETTINGS, values, "1=1", new String[] {});
		db.close(); // Closing database connection
	}
}
