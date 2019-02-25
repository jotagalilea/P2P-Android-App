package com.example.samue.login;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Julio on 21/02/2019.
 *
 * **************CLASE DESECHADA DE MOMENTO*****************
 * Será borrada en el siguiente commit.
 */
public class BlockedUsersHelper extends SQLiteOpenHelper {
	private static final String TAG = "BlockedUsersHelper";
	private static final String DB_NAME = "BUDB";

	static final String BLOCKED_TABLE_NAME = "blocked_users";
	private static final String USERS_COL1 = "id";
	private static final String USERS_COL2 = "name";


	public BlockedUsersHelper(Context context){
		super(context, DB_NAME, null, 1);
		//context.deleteDatabase(DB_NAME); //para borrar la base de datos si hace falta
	}
	@Override
	public void onCreate(SQLiteDatabase db) {
		String createTable = "CREATE TABLE " + BLOCKED_TABLE_NAME + "(ID INTEGER PRIMARY KEY AUTOINCREMENT, " + USERS_COL2 + " TEXT);";
		db.execSQL(createTable);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		String dropTable = "DROP TABLE IF EXISTS " + BLOCKED_TABLE_NAME;
		db.execSQL(dropTable);
		onCreate(db);
	}


	public boolean removeData(String name){
		SQLiteDatabase db = this.getWritableDatabase();
		String[] args = new String[]{name};

		int result = db.delete(BLOCKED_TABLE_NAME, "name=?", args);

		if (result == -1) {
			return false;
		} else {
			return true;
		}
	}


	public boolean addData(String item){
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues contentValues = new ContentValues();

		contentValues.put(USERS_COL2, item);

		Log.d(TAG, "addData: Adding " + item + " to " + BLOCKED_TABLE_NAME);

		long result = db.insert(BLOCKED_TABLE_NAME, null, contentValues);

		if (result == -1) {
			return false;
		} else {
			return true;
		}
	}


	public Cursor getData(){
		SQLiteDatabase db = this.getWritableDatabase();
		String q = "SELECT * FROM " + BLOCKED_TABLE_NAME;
		Cursor data = db.rawQuery(q, null);
		return data;
	}


}
