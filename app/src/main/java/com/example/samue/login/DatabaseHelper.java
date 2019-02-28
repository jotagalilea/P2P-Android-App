package com.example.samue.login;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.HashSet;


public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHelper";
    private static final String DB_NAME = "P2PDB";

    static final String FRIENDS_TABLE_NAME = "friends";
    private static final String FRIENDS_COL1 = "id";
    private static final String FRIENDS_COL2 = "name";

	static final String BLOCKED_TABLE_NAME = "blocked_users";
	private static final String USERS_COL1 = "id";
	private static final String USERS_COL2 = "name";


	/*
	 * Colección de todos los nombres de las tablas de la base de datos. La finalidad de esta
	 * estructura es asegurar complejidad constante cuando se quiera consultar si una tabla existe.
	 */
	//TODO: Actualizar esta colección en el constructor si se añaden nuevas tablas a la aplicación.
	private static final HashSet<String> TABLE_NAMES = new HashSet<>(2);


    public DatabaseHelper(Context context){
        super(context, DB_NAME, null, 1);
        //context.deleteDatabase(DB_NAME); //para borrar la base de datos si hace falta
        TABLE_NAMES.add(FRIENDS_TABLE_NAME);
        TABLE_NAMES.add(BLOCKED_TABLE_NAME);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable1 = "CREATE TABLE " + FRIENDS_TABLE_NAME + "(ID INTEGER PRIMARY KEY AUTOINCREMENT, " + FRIENDS_COL2 + " TEXT);";
		String createTable2 = "CREATE TABLE " + BLOCKED_TABLE_NAME + "(ID INTEGER PRIMARY KEY AUTOINCREMENT, " + USERS_COL2 + " TEXT);";
		db.execSQL(createTable1);
        db.execSQL(createTable2);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String dropTable1 = "DROP TABLE IF EXISTS " + FRIENDS_TABLE_NAME;
		String dropTable2 = "DROP TABLE IF EXISTS " + BLOCKED_TABLE_NAME;
		db.execSQL(dropTable1);
        db.execSQL(dropTable2);
        onCreate(db);
    }


	/**
	 * Método para añadir datos en la tablas FRIENDS_TABLE_NAME o bien BLOCKED_TABLE_NAME.
	 *
	 * @param item String con el nombre del añadido.
	 * @param table Tabla seleccionada.
	 * @return
	 */
	public boolean addData(String item, String table){
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues contentValues = new ContentValues();

		switch (table){
			case FRIENDS_TABLE_NAME:
				contentValues.put(FRIENDS_COL2, item);
				break;
			case BLOCKED_TABLE_NAME:
				contentValues.put(USERS_COL2, item);
				break;
		}

		Log.d(TAG, "addData: Adding " + item + " to " + table);

		long result = db.insert(table, null, contentValues);

		if (result == -1)
			return false;
		else
			return true;
	}

	/**
	 * Método para eliminar datos de las tablas FRIENDS_TABLE_NAME o bien BLOCKED_TABLE_NAME.
	 *
	 * @param name String con el nombre del eliminado.
	 * @param table Tabla seleccionada.
	 * @return true si tiene éxito.
	 */
    public boolean removeData(String name, String table){
		SQLiteDatabase db = this.getWritableDatabase();
		String[] args = new String[]{name};

		int result = db.delete(table, "name=?", args);

		if (result == -1)
			return false;
		else
			return true;
    }


	/**
	 * Método para obtener un cursor a los datos de la tabla indicada.
	 *
	 * @param table Tabla seleccionada.
	 * @return
	 */
    public Cursor getData(String table){
        SQLiteDatabase db = this.getWritableDatabase();
        String q = "SELECT * FROM " + table;
        Cursor data = db.rawQuery(q, null);
        return data;
    }




}
