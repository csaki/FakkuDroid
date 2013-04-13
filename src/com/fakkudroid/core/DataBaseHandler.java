package com.fakkudroid.core;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.fakkudroid.bean.DoujinBean;
import com.fakkudroid.bean.URLBean;
import com.fakkudroid.bean.UserBean;
import com.fakkudroid.util.Util;

public class DataBaseHandler extends SQLiteOpenHelper {

	// All Static variables
	// Database Version
	private static final int DATABASE_VERSION = 2;

	// Database Name
	private static final String DATABASE_NAME = "doujinsDB";

	// Settings table name
	private static final String TABLE_SETTINGS = "settings";
	private static final String TABLE_DOUJIN = "doujin";

	// Commun Table Columns names
	private static final String KEY_ID = "id";

	// Doujin Table Columns names
	private static final String KEY_URL = "url";
	private static final String KEY_TITLE = "title";
	private static final String KEY_DESCRIPTION = "description";
	private static final String KEY_ARTIST = "artist";
	private static final String KEY_TAGS = "tags";
	private static final String KEY_SERIE = "serie";
	private static final String KEY_QTY_PAGES = "qty_pages";

	// Settings Table Columns names
	private static final String KEY_USER = "user";
	private static final String KEY_PASSWORD = "password";
	private static final String KEY_CHECKED = "checked";
	private static final String KEY_MESSAGE_HELP = "message_help";
	private static final String KEY_DATE_MESSAGE_HELP = "date_message_help";

	public DataBaseHandler(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String CREATE_TABLE_SETTINGS = "CREATE TABLE " + TABLE_SETTINGS + "("
				+ KEY_ID + " TEXT PRIMARY KEY," + KEY_USER + " TEXT,"
				+ KEY_MESSAGE_HELP + " INTEGER" + "," + KEY_DATE_MESSAGE_HELP
				+ " INTEGER" + "," + KEY_PASSWORD + " TEXT" + "," + KEY_CHECKED
				+ " INTEGER" + ")";

		String CREATE_TABLE_DOUJIN = "CREATE TABLE " + TABLE_DOUJIN + "("
				+ KEY_ID + " TEXT PRIMARY KEY," + KEY_URL + " TEXT,"
				+ KEY_TITLE + " TEXT" + "," + KEY_ARTIST + " TEXT" + ","
				+ KEY_TAGS + " TEXT" + "," + KEY_DESCRIPTION + " TEXT" + ","
				+ KEY_QTY_PAGES + " INTEGER" + "," + KEY_SERIE + " TEXT" + ")";

		db.execSQL(CREATE_TABLE_SETTINGS);
		db.execSQL(CREATE_TABLE_DOUJIN);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Drop older table if existed
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_SETTINGS);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_DOUJIN);

		// Create tables again
		onCreate(db);

	}

	public UserBean addSetting() {
		UserBean bean = new UserBean();
		bean.setUser("");
		bean.setPassword("");
		bean.setChecked(false);

		Calendar c = Calendar.getInstance();

		int random = (int) (Math.random() * 7 + 3);
		c.add(Calendar.DATE, random);
		Date dateMessageHelp = c.getTime();

		bean.setDateShowMessageHelp(dateMessageHelp);
		bean.setMessageHelp(false);

		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();

		values.put(KEY_USER, bean.getUser());
		values.put(KEY_PASSWORD, bean.getPassword());
		values.put(KEY_CHECKED, bean.isChecked() ? 1 : 0);
		values.put(KEY_MESSAGE_HELP, bean.isMessageHelp() ? 1 : 0);
		values.put(KEY_DATE_MESSAGE_HELP, bean.getDateShowMessageHelp()
				.getTime());

		// Inserting Row
		db.insert(TABLE_SETTINGS, null, values);
		db.close(); // Closing database connection

		return bean;
	}

	public DoujinBean addDoujin(DoujinBean bean) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();

		values.put(KEY_ID, bean.getId());
		values.put(KEY_URL, bean.getUrl());
		values.put(KEY_ARTIST, bean.getArtist().getDescription() + "|"
				+ bean.getArtist().getUrl());
		values.put(KEY_SERIE, bean.getSerie().getDescription() + "|"
				+ bean.getSerie().getUrl());
		values.put(KEY_TAGS, bean.getTagsWithURL());
		values.put(KEY_DESCRIPTION, bean.getDescription());
		values.put(KEY_QTY_PAGES, bean.getQtyPages());
		values.put(KEY_TITLE, bean.getTitle());

		// Inserting Row
		db.insert(TABLE_DOUJIN, null, values);
		db.close(); // Closing database connection

		return bean;
	}

	public UserBean getSetting() {
		UserBean result = null;
		// Select All Query
		String selectQuery = "SELECT " + KEY_ID + "," + KEY_USER + ","
				+ KEY_PASSWORD + "," + KEY_CHECKED + "," + KEY_MESSAGE_HELP
				+ "," + KEY_DATE_MESSAGE_HELP + " FROM " + TABLE_SETTINGS;

		Log.i(this.getClass().toString(), selectQuery);

		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		// looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			result = new UserBean();
			result.setUser(cursor.getString(1));
			result.setPassword(cursor.getString(2));
			result.setChecked(cursor.getInt(3) == 1);
			result.setMessageHelp(cursor.getInt(4) == 1);
			result.setDateShowMessageHelp(new Date(cursor.getLong(5)));
		}

		db.close();
		// return contact list
		return result;
	}
	
	public DoujinBean getDoujinBean(String id) {
		DoujinBean result = null;
		// Select All Query
		String selectQuery = "SELECT " + KEY_ID + "," + KEY_TITLE + ","
				+ KEY_DESCRIPTION + "," + KEY_ARTIST + "," + KEY_TAGS + ","
				+ KEY_SERIE + "," + KEY_QTY_PAGES + "," + KEY_URL + " FROM " + TABLE_DOUJIN
				+ " WHERE " + KEY_ID + "='" + id + "'";

		Log.i(this.getClass().toString(), selectQuery);

		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		// looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			result = new DoujinBean();
			result.setTitle(cursor.getString(1));
			result.setDescription(cursor.getString(2));
			result.setArtist(Util.castURLBean(cursor.getString(3)));
			List<URLBean> lstTags = new ArrayList<URLBean>();
			String tags = cursor.getString(4);
			String[] tags_list = tags.split(",");
			for (String str : tags_list) {
				lstTags.add(Util.castURLBean(str));
			}
			result.setLstTags(lstTags);
			result.setSerie(Util.castURLBean(cursor.getString(5)));
			result.setQtyPages(cursor.getInt(6));
			result.setUrl(cursor.getString(7));
		}

		db.close();
		// return contact list
		return result;
	}

	public LinkedList<DoujinBean> getDoujinList() {
		LinkedList<DoujinBean> result = new LinkedList<DoujinBean>();
		// Select All Query
		String selectQuery = "SELECT " + KEY_ID + "," + KEY_TITLE + ","
				+ KEY_DESCRIPTION + "," + KEY_ARTIST + "," + KEY_TAGS + ","
				+ KEY_SERIE + "," + KEY_QTY_PAGES + "," + KEY_URL + " FROM " + TABLE_DOUJIN;

		Log.i(this.getClass().toString(), selectQuery);

		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		// looping through all rows and adding to list
		if (cursor.moveToFirst())
			do{
				DoujinBean bean = new DoujinBean();
				bean.setTitle(cursor.getString(1));
				bean.setDescription(cursor.getString(2));
				bean.setArtist(Util.castURLBean(cursor.getString(3)));
				List<URLBean> lstTags = new ArrayList<URLBean>();
				String tags = cursor.getString(4);
				String[] tags_list = tags.split(",");
				for (String str : tags_list) {
					lstTags.add(Util.castURLBean(str));
				}
				bean.setLstTags(lstTags);
				bean.setSerie(Util.castURLBean(cursor.getString(5)));
				bean.setQtyPages(cursor.getInt(6));
				bean.setUrl(cursor.getString(7));
				result.add(bean);
			}while (cursor.moveToNext());

		db.close();
		// return contact list
		return result;
	}

	public void updateSetting(UserBean bean) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();

		values.put(KEY_USER, bean.getUser());
		values.put(KEY_PASSWORD, bean.getPassword());
		values.put(KEY_CHECKED, bean.isChecked() ? 1 : 0);
		values.put(KEY_MESSAGE_HELP, bean.isMessageHelp() ? 1 : 0);
		values.put(KEY_DATE_MESSAGE_HELP, bean.getDateShowMessageHelp()
				.getTime());

		// Inserting Row
		db.update(TABLE_SETTINGS, values, "1=1", new String[] {});
		db.close(); // Closing database connection
	}

	public void deleteDoujin(String id) {
		SQLiteDatabase db = this.getWritableDatabase();

		// Inserting Row
		db.delete(TABLE_DOUJIN, KEY_ID + "=?", new String[] { id });
		db.close(); // Closing database connection
	}
}
