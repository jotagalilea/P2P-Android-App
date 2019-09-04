package com.example.samue.login;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;


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
	private static final String FOLDER_ACCESS_COL1 = "folder";
	private static final String FOLDER_ACCESS_COL2 = "userid";


	//Tabla con los grupos a las que tiene acceso cada usuario.
	static final String GROUPS_TABLE_NAME = "groups";
	private static final String GROUPS_COL1 = "name_group";
	private static final String GROUPS_COL2 = "friends";
	private static final String GROUPS_COL3 = "files";
	private static final String GROUPS_COL4 = "owners";
	private static final String GROUPS_COL5 = "admin";

	/*
	 * Colección de todos los nombres de las tablas de la base de datos. La finalidad de esta
	 * estructura es asegurar complejidad constante cuando se quiera consultar si una tabla existe.
	 */
	//TODO: Actualizar esta colección en el constructor si se añaden nuevas tablas a la aplicación.
	private static final HashSet<String> TABLE_NAMES = new HashSet<>(5);


    public DatabaseHelper(Context context){
        super(context, DB_NAME, null, 1);
        // context.deleteDatabase(DB_NAME); //para borrar la base de datos si hace falta
        TABLE_NAMES.add(FRIENDS_TABLE_NAME);
        TABLE_NAMES.add(BLOCKED_TABLE_NAME);
        TABLE_NAMES.add(FOLDER_ACCESS_TABLE);
        TABLE_NAMES.add(SHARED_FOLDERS_TABLE);
		TABLE_NAMES.add(GROUPS_TABLE_NAME);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable1 = "CREATE TABLE " +FRIENDS_TABLE_NAME+ "(" +FRIENDS_COL1+ " INTEGER PRIMARY KEY AUTOINCREMENT, " +FRIENDS_COL2+ " TEXT);";
		String createTable2 = "CREATE TABLE " +BLOCKED_TABLE_NAME+ "(" +BLOCKED_COL1+ " INTEGER PRIMARY KEY AUTOINCREMENT, " +BLOCKED_COL2+ " TEXT);";
		String createTable3 = "CREATE TABLE " +SHARED_FOLDERS_TABLE+ "(" +SHARED_FOLDERS_COL1+ " TEXT PRIMARY KEY, " +SHARED_FOLDERS_COL2+ " TEXT);";
		String createTable4 = "CREATE TABLE " +FOLDER_ACCESS_TABLE+ "(" +FOLDER_ACCESS_COL1+ " TEXT, " +FOLDER_ACCESS_COL2+ " INTEGER, " +
				"FOREIGN KEY (" +FOLDER_ACCESS_COL1+ ") REFERENCES " +SHARED_FOLDERS_TABLE+ " (" +SHARED_FOLDERS_COL1+ ")," +
				"FOREIGN KEY (" +FOLDER_ACCESS_COL2+ ") REFERENCES " +FRIENDS_TABLE_NAME+" (" +FRIENDS_COL1+ "));";
		String createTable5 = "CREATE TABLE " +GROUPS_TABLE_NAME+ "(" +GROUPS_COL1+ " TEXT PRIMARY KEY, " +GROUPS_COL2+ " TEXT, "+
				GROUPS_COL3+ " TEXT, " +GROUPS_COL4+ " TEXT, " +GROUPS_COL5+ " TEXT);";
		db.execSQL(createTable1);
        db.execSQL(createTable2);
        db.execSQL(createTable3);
        db.execSQL(createTable4);
		db.execSQL(createTable5);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String dropTable1 = "DROP TABLE IF EXISTS " + FRIENDS_TABLE_NAME;
		String dropTable2 = "DROP TABLE IF EXISTS " + BLOCKED_TABLE_NAME;
		String dropTable3 = "DROP TABLE IF EXISTS " + SHARED_FOLDERS_TABLE;
		String dropTable4 = "DROP TABLE IF EXISTS " + FOLDER_ACCESS_TABLE;
		String dropTable5 = "DROP TABLE IF EXISTS " + GROUPS_TABLE_NAME;

		db.execSQL(dropTable1);
        db.execSQL(dropTable2);
        db.execSQL(dropTable3);
        db.execSQL(dropTable4);
		db.execSQL(dropTable5);
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

	/**
	 * Añade una carpeta compartida a la base de datos junto con la lista de ficheros que contiene.
	 * @param folder Nombre de la carpeta
	 * @param files Lista de ficheros en un String.
	 * @return true si ha tenido éxito, false en caso contrario.
	 */
	public boolean addSharedFolder(String folder, String files){
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues contentValues = new ContentValues();
		contentValues.put(SHARED_FOLDERS_COL1, folder);
		contentValues.put(SHARED_FOLDERS_COL2, files);
		Log.d(TAG, "addData: Adding " + folder + " to " + SHARED_FOLDERS_TABLE);
		long result = db.insert(SHARED_FOLDERS_TABLE, null, contentValues);
		if (result == -1)
			return false;
		else
			return true;
	}


	/**
	 * Añade amigos a la lista de acceso de una carpeta.
	 * @param friendsNames Lista de nombres de los amigos a añadir.
	 * @param folder Nombre de la carpeta compartida.
	 * @return true si la inserción ha tenido éxito. false en otro caso.
	 */
	public boolean addFriends2Folder(ArrayList<String> friendsNames, String folder){
		SQLiteDatabase db = this.getWritableDatabase();
		// Primero obtengo los id de los usuarios que se van a añadir:
		String queryIDs = getUsersIDsQuery(friendsNames);
		Cursor ids = db.rawQuery(queryIDs, null);

		// Después, con un cursor a los ids voy insertando en la tabla de acceso:
		ContentValues contentValues = new ContentValues();
		long result = 0;
		while (ids.moveToNext() && (result != -1)){
			contentValues.clear();
			contentValues.put(FOLDER_ACCESS_COL1, folder);
			contentValues.put(FOLDER_ACCESS_COL2, ids.getInt(0));
			result = db.insert(FOLDER_ACCESS_TABLE, null, contentValues);
		}
		Log.d(TAG, "addData: Adding to " + FOLDER_ACCESS_TABLE + " rows:\n" + friendsNames);
		if (result == -1)
			return false;
		else
			return true;
	}



	/**
	 * Devuelve el String para la posterior consulta de los id de una lista de nombres de amigos pasada como parámetro.
	 * @param names lista de nombres de amigos de los cuales se obtendrán los id.
	 * @return Consulta SELECT en formato String.
	 */
	private String getUsersIDsQuery(ArrayList<String> names){
		StringBuilder queryIDs = new StringBuilder("SELECT " + FRIENDS_COL1 + " FROM " + FRIENDS_TABLE_NAME + " WHERE " + FRIENDS_COL2 + " IN (");
		for (int i=0; i<names.size(); i++) {
			queryIDs.append("'");
			queryIDs.append(names.get(i));
			queryIDs.append("'");
			if (i < (names.size()-1))
				queryIDs.append(",");
		}
		queryIDs.append(')');
		return queryIDs.toString();
	}



	/**
	 * Borra amigos de la tabla de acceso a una carpeta.
	 * @param folder
	 * @param users
	 * @return true si ha tenido éxito, false en caso contrario.
	 */
	public boolean removeFriendsFromFolder(String folder, ArrayList<String> users){
		SQLiteDatabase db = this.getWritableDatabase();
		// Primero obtengo los id de los usuarios a eliminar:
		String queryIDs = getUsersIDsQuery(users);

		// monto subconsulta:
		StringBuilder where = new StringBuilder();
		where.append("\"");
		where.append(folder);
		where.append("\"");
		where.append(" = ");
		where.append(FOLDER_ACCESS_COL1);
		where.append(" AND ");
		where.append(FOLDER_ACCESS_COL2);
		where.append(" IN (");
		where.append(queryIDs);
		where.append(')');

		// y finalmente el borrado:
		int result = db.delete(FOLDER_ACCESS_TABLE, where.toString(), null);

		if (result == -1)
			return false;
		else
			return true;
	}


	/**
	 * Borra una carpeta compartida.
	 * @param folder Nombre de la carpeta.
	 * @return true si ha tenido éxito, false en caso contrario.
	 */
	public boolean removeSharedFolder(String folder){
		SQLiteDatabase db = this.getWritableDatabase();
		String[] args = new String[]{folder};

		int result = db.delete(SHARED_FOLDERS_TABLE, "folder=?", args);

		if (result == -1)
			return false;
		else
			return true;
	}


	/**
	 * Método para eliminar datos de las tablas FRIENDS_TABLE_NAME o bien BLOCKED_TABLE_NAME.
	 * @param name String con el nombre del eliminado.
	 * @param table Tabla seleccionada.
	 * @return true si ha tenido éxito, false en caso contrario.
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
	 * @param table Tabla seleccionada.
	 * @return
	 */
    public Cursor getData(String table){
        SQLiteDatabase db = this.getWritableDatabase();
        String q = "SELECT * FROM " + table;
        Cursor data = db.rawQuery(q, null);
        return data;
    }


	/**
	 * Obtiene el nombre de un usuario dado su id.
	 * @param id Identificador.
	 * @return Nombre del usuario cuyo id coincide.
	 */
	public String getUserName(int id){
		SQLiteDatabase db = this.getWritableDatabase();
		String q = "SELECT "+ FRIENDS_COL2 +" FROM " + FRIENDS_TABLE_NAME + " WHERE " + FRIENDS_COL1 + " = " + id;
		Cursor data = db.rawQuery(q, null);
		data.moveToNext();
		return data.getString(0);
	}


	/**
	 * Obtiene los datos de las carpetas compartidas en forma de hashMap.
	 * @return Carpetas compartidas.
	 */
	public HashMap<String,ArrayList<String>> getSharedFolders(){
		HashMap<String, ArrayList<String>> result = new HashMap<>();
		SQLiteDatabase db = this.getWritableDatabase();
		String q = "SELECT * FROM " + SHARED_FOLDERS_TABLE;
		Cursor data = db.rawQuery(q, null);

		while (data.moveToNext()){
			String folder = data.getString(0);
			String files = data.getString(1);
			ArrayList<String> al_files = new ArrayList<>(Arrays.asList(files.split(",")));
			result.put(folder, al_files);
		}
		return result;
	}


	/**
	 * Obtiene los datos de la tabla de acceso a las carpetas compartidas.
	 * @return Nombres de carpetas y usuarios con acceso.
	 */
	public HashMap<String,ArrayList<String>> getFoldersAccess(){
		HashMap<String, ArrayList<String>> result = new HashMap<>();
		SQLiteDatabase db = this.getWritableDatabase();
		String q = "SELECT * FROM " + FOLDER_ACCESS_TABLE;
		Cursor data = db.rawQuery(q, null);

		String lastFolder = null;
		ArrayList<String> al_friends = null;

		while (data.moveToNext()){
			String folder = data.getString(0);
			int friendID = data.getInt(1);
			//Si la carpeta es distinta a la anterior o es la primera se crea una nueva entrada:
			if (!folder.equalsIgnoreCase(lastFolder)) {
				al_friends = new ArrayList<>(4);
				result.put(folder, al_friends);
			}
			String friend = getUserName(friendID);
			al_friends.add(friend);
			lastFolder = folder;
		}
		return result;
	}
	/**
	 * Borra un amigo. Si este tenía acceso a alguna carpeta compartida se elimina de ellas.
	 * @param name
	 * @return
	 */
	public boolean deleteFriend(String name){
		SQLiteDatabase db = getWritableDatabase();
		Cursor user = db.query(FRIENDS_TABLE_NAME, new String[]{FRIENDS_COL1}, FRIENDS_COL2+"=?", new String[]{name},
				null, null, null);
		user.moveToNext();
		String where = FOLDER_ACCESS_COL2+'='+user.getInt(0);
		int result = db.delete(FOLDER_ACCESS_TABLE, where, null);

		if (result == -1)
			return false;
		else{
			removeData(name, FRIENDS_TABLE_NAME);
			return true;
		}
	}

	//Funciones relacionadas con los GRUPOS

	/**
	 * Añade un nuevo grupo a la base de datos junto con la lista de amigos que lo forman.
	 * @param  name nombre del grupo
	 * @param friends Lista de amigos del grupo en un string.
	 * @return true si ha tenido éxito, false en caso contrario.
	 */
	public boolean addGroup(String name, String friends, String administrator){
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues contentValues = new ContentValues();
		contentValues.put(GROUPS_COL1, name);
		contentValues.put(GROUPS_COL2, friends);
		//contentValues.put(GROUPS_COL3, "null");
		//contentValues.put(GROUPS_COL4, "null");
		contentValues.put(GROUPS_COL5,administrator);
		Log.d(TAG, "addData: Adding " + name + " to " + GROUPS_TABLE_NAME);
		long result = db.insert(GROUPS_TABLE_NAME, null, contentValues);
		if (result == -1)
			return false;
		else
			return true;
	}

	/**
	 * Añade ficheros a la lista de archivos compartidos de un grupo.
	 * @param friendsNames Lista de nombres de los amigos a añadir.
	 * @param nameGroup Nombre de la carpeta compartida.
	 * @return true si la inserción ha tenido éxito. false en otro caso.
	 */
	public boolean addFiles2Group(ArrayList<String> friendsNames, String nameGroup){
		SQLiteDatabase db = this.getWritableDatabase();
		// Primero obtengo los id de los usuarios que se van a añadir:
		String queryIDs = getUsersIDsQuery(friendsNames);
		Cursor ids = db.rawQuery(queryIDs, null);

		// Después, con un cursor a los ids voy insertando en la tabla de acceso:
		ContentValues contentValues = new ContentValues();
		long result = 0;
		while (ids.moveToNext() && (result != -1)){
			contentValues.clear();
			contentValues.put(FOLDER_ACCESS_COL1, nameGroup);
			contentValues.put(FOLDER_ACCESS_COL2, ids.getInt(0));
			result = db.insert(FOLDER_ACCESS_TABLE, null, contentValues);
		}
		Log.d(TAG, "addData: Adding to " + FOLDER_ACCESS_TABLE + " rows:\n" + friendsNames);
		if (result == -1)
			return false;
		else
			return true;
	}
	/**
	 * Añade amigos al grupo.
	 * @param friendsNames Lista de nombres de los amigos a añadir.
	 * @param nameGroup Nombre del grupo al que se van a añadir
	 * @return true si la inserción ha tenido éxito. false en otro caso.
	 */
	public boolean addFriends2Group(ArrayList<String> friendsNames, String nameGroup){
		SQLiteDatabase db = this.getWritableDatabase();
		// Primero obtengo los id de los usuarios que se van a añadir:
		String queryIDs = getUsersIDsQuery(friendsNames);
		Cursor ids = db.rawQuery(queryIDs, null);

		// Después, con un cursor a los ids voy insertando en la tabla de acceso:
		ContentValues contentValues = new ContentValues();
		long result = 0;
		while (ids.moveToNext() && (result != -1)){
			contentValues.clear();
			contentValues.put(GROUPS_COL1, nameGroup);
			contentValues.put(GROUPS_COL2, ids.getInt(0));
			result = db.insert(GROUPS_TABLE_NAME, null, contentValues);
		}
		Log.d(TAG, "addData: Adding to " + GROUPS_TABLE_NAME + " rows:\n" + friendsNames);
		if (result == -1)
			return false;
		else
			return true;
	}


	/**
	 * Borra amigos de la lista de acceso a un grupo.
	 * @param nameGroup
	 * @param friends
	 * @return true si ha tenido éxito, false en caso contrario.
	 */
	public boolean removeFriendsFromGroup(String nameGroup, ArrayList<String> friends){
		SQLiteDatabase db = this.getWritableDatabase();
		// Primero obtengo los id de los usuarios a eliminar:
		String queryIDs = getUsersIDsQuery(friends);

		// monto subconsulta:
		StringBuilder where = new StringBuilder();
		where.append("\"");
		where.append(nameGroup);
		where.append("\"");
		where.append(" = ");
		where.append(FOLDER_ACCESS_COL1);
		where.append(" AND ");
		where.append(FOLDER_ACCESS_COL2);
		where.append(" IN (");
		where.append(queryIDs);
		where.append(')');

		// y finalmente el borrado:
		int result = db.delete(GROUPS_TABLE_NAME, where.toString(), null);

		if (result == -1)
			return false;
		else
			return true;
	}


	/**
	 * Borra un grupo.
	 * @param nameGroup Nombre del grupo.
	 * @return true si ha tenido éxito, false en caso contrario.
	 */
	public boolean removeGroup(String nameGroup){
		SQLiteDatabase db = this.getWritableDatabase();
		String[] args = new String[]{nameGroup};

		int result = db.delete(GROUPS_TABLE_NAME, "name_group=?", args);

		if (result == -1)
			return false;
		else
			return true;
	}

}
