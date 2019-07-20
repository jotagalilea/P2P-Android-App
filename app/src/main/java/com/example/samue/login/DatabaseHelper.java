package com.example.samue.login;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v4.util.Pair;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;


public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHelper";
    private static final String DB_NAME = "P2PDB";

    // Tabla amigos
    static final String FRIENDS_TABLE_NAME = "friends";
    private static final String FRIENDS_COL1 = "id";
    private static final String FRIENDS_COL2 = "name";

    // Tabla ususarios bloqueados
	static final String BLOCKED_TABLE_NAME = "blocked_users";
	private static final String BLOCKED_COL1 = "id";
	private static final String BLOCKED_COL2 = "name";

	// Tabla de carpetas compartidas con los archivos que contienen.
    static final String SHARED_FOLDERS_TABLE = "shared_folders";
	private static final String SHARED_FOLDERS_COL1 = "folder";
	private static final String SHARED_FOLDERS_COL2 = "files";

	//Tabla con las carpetas compartidas a las que tiene acceso cada usuario.
	static final String FOLDER_ACCESS_TABLE = "folder_access";
	//private static final String FOLDER_ACCESS_COL1 = "userid";
	//private static final String FOLDER_ACCESS_COL2 = "folder";
	private static final String FOLDER_ACCESS_COL1 = "folder";
	private static final String FOLDER_ACCESS_COL2 = "userid";


	/*
	 * Colección de todos los nombres de las tablas de la base de datos. La finalidad de esta
	 * estructura es asegurar complejidad constante cuando se quiera consultar si una tabla existe.
	 */
	//TODO: Actualizar esta colección en el constructor si se añaden nuevas tablas a la aplicación.
	private static final HashSet<String> TABLE_NAMES = new HashSet<>(4);


    public DatabaseHelper(Context context){
        super(context, DB_NAME, null, 1);
        //context.deleteDatabase(DB_NAME); //para borrar la base de datos si hace falta
        TABLE_NAMES.add(FRIENDS_TABLE_NAME);
        TABLE_NAMES.add(BLOCKED_TABLE_NAME);
        TABLE_NAMES.add(FOLDER_ACCESS_TABLE);
        TABLE_NAMES.add(SHARED_FOLDERS_TABLE);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable1 = "CREATE TABLE " +FRIENDS_TABLE_NAME+ "(" +FRIENDS_COL1+ " INTEGER PRIMARY KEY AUTOINCREMENT, " +FRIENDS_COL2+ " TEXT);";
		String createTable2 = "CREATE TABLE " +BLOCKED_TABLE_NAME+ "(" +BLOCKED_COL1+ " INTEGER PRIMARY KEY AUTOINCREMENT, " +BLOCKED_COL2+ " TEXT);";
		String createTable3 = "CREATE TABLE " +SHARED_FOLDERS_TABLE+ "(" +SHARED_FOLDERS_COL1+ " TEXT PRIMARY KEY, " +SHARED_FOLDERS_COL2+ " TEXT);";
		/*String createTable4 = "CREATE TABLE " +FOLDER_ACCESS_TABLE+ "(" +FOLDER_ACCESS_COL1+ " INTEGER, " +FOLDER_ACCESS_COL2+ " TEXT, " +
				"FOREIGN KEY (" +FOLDER_ACCESS_COL1+ ") REFERENCES " +FRIENDS_TABLE_NAME+" (" +FRIENDS_COL1+ ")," +
				"FOREIGN KEY (" +FOLDER_ACCESS_COL2+ ") REFERENCES " +SHARED_FOLDERS_TABLE+ " (" +SHARED_FOLDERS_COL1+ "));";
		*/
		String createTable4 = "CREATE TABLE " +FOLDER_ACCESS_TABLE+ "(" +FOLDER_ACCESS_COL1+ " TEXT, " +FOLDER_ACCESS_COL2+ " INTEGER, " +
				"FOREIGN KEY (" +FOLDER_ACCESS_COL1+ ") REFERENCES " +SHARED_FOLDERS_TABLE+ " (" +SHARED_FOLDERS_COL1+ ")," +
				"FOREIGN KEY (" +FOLDER_ACCESS_COL2+ ") REFERENCES " +FRIENDS_TABLE_NAME+" (" +FRIENDS_COL1+ "));";
		db.execSQL(createTable1);
        db.execSQL(createTable2);
        db.execSQL(createTable3);
        db.execSQL(createTable4);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String dropTable1 = "DROP TABLE IF EXISTS " + FRIENDS_TABLE_NAME;
		String dropTable2 = "DROP TABLE IF EXISTS " + BLOCKED_TABLE_NAME;
		String dropTable3 = "DROP TABLE IF EXISTS " + SHARED_FOLDERS_TABLE;
		String dropTable4 = "DROP TABLE IF EXISTS " + FOLDER_ACCESS_TABLE;
		db.execSQL(dropTable1);
        db.execSQL(dropTable2);
        db.execSQL(dropTable3);
        db.execSQL(dropTable4);
        onCreate(db);
    }


	/**
	 * Método para añadir filas nuevas a las tablas.
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
				contentValues.put(BLOCKED_COL2, item);
				break;
		}
		Log.d(TAG, "addData: Adding " + item + " to " + table);
		long result = db.insert(table, null, contentValues);
		if (result == -1)
			return false;
		else
			return true;
	}

	public boolean addSharedFolder(Pair<String,String> row){
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues contentValues = new ContentValues();
		contentValues.put(SHARED_FOLDERS_COL1, row.first);
		contentValues.put(SHARED_FOLDERS_COL2, row.second);
		Log.d(TAG, "addData: Adding " + row + " to " + SHARED_FOLDERS_TABLE);
		long result = db.insert(SHARED_FOLDERS_TABLE, null, contentValues);
		if (result == -1)
			return false;
		else
			return true;
	}

	/*public boolean addFriends2Folder(ArrayList<Pair<Integer,String>> rows){
		SQLiteDatabase db = this.getWritableDatabase();
		long result = 0;
		ContentValues contentValues = new ContentValues();
		Iterator it = rows.iterator();
		while (it.hasNext() && (result!=-1)){
			contentValues.clear();
			Pair<Integer,String> row = (Pair<Integer,String>) it.next();
			contentValues.put(FOLDER_ACCESS_COL1, row.first);
			contentValues.put(FOLDER_ACCESS_COL2, row.second);
			result = db.insert(FOLDER_ACCESS_TABLE, null, contentValues);
		}
		Log.d(TAG, "addData: Adding " + rows + " to " + FOLDER_ACCESS_TABLE);
		if (result == -1)
			return false;
		else
			return true;
	}*/

	/**
	 * Añade amigos a la lista de acceso de una carpeta.
	 * @param rows
	 * @return
	 */
	public boolean addFriends2Folder(ArrayList<Pair<String,Integer>> rows){
		SQLiteDatabase db = this.getWritableDatabase();
		long result = 0;
		ContentValues contentValues = new ContentValues();
		Iterator it = rows.iterator();
		while (it.hasNext() && (result!=-1)){
			contentValues.clear();
			Pair<Integer,String> row = (Pair<Integer,String>) it.next();
			contentValues.put(FOLDER_ACCESS_COL1, row.first);
			contentValues.put(FOLDER_ACCESS_COL2, row.second);
			result = db.insert(FOLDER_ACCESS_TABLE, null, contentValues);
		}
		Log.d(TAG, "addData: Adding to " + FOLDER_ACCESS_TABLE + " rows:\n" + rows);
		if (result == -1)
			return false;
		else
			return true;
	}


	//TODO: Falta método para eliminar de la tabla de acceso a carpetas.



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
