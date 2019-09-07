package com.example.samue.login;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *  Created by jotagalilea on 17/07/2019.
 *
 *  Actividad que muestra las carpetas que se han compartido. Permite ver los amigos con acceso,
 *  los archivos que contienen, y borrarlas si se eliminan todos los usuarios de alguna.
 */
public class SharedFoldersActivity extends AppCompatActivity {

	private SimpleAdapter adapter;
	private ListView listView;
	//Nombre de las carpetas, lista de archivos de cada una.
	private HashMap<String,ArrayList<String>> sharedFolders;
	// Nombre de las carpetas, lista de amigos que tienen acceso a cada una.
	private HashMap<String,ArrayList<String>> foldersAccess;
	private ArrayList<Friends> al_friends;
	// foldersNames es útil cuando se selecciona una carpeta de la lista para tomar su nombre y obtener
	// la lista de archivos de sharedFolders o bien la lista de usuarios con acceso de foldersAccess.
	private ArrayList<String> foldersNames;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_shared_folders);
		Intent intent = getIntent();
		sharedFolders = (HashMap<String,ArrayList<String>>) intent.getSerializableExtra("sharedFolders");
		foldersAccess = (HashMap<String,ArrayList<String>>) intent.getSerializableExtra("foldersAccess");
		al_friends = (ArrayList<Friends>) intent.getSerializableExtra("friends");

		Toolbar myToolbar = findViewById(R.id.sf_toolbar);
		setSupportActionBar(myToolbar);
		getSupportActionBar().setTitle("Carpetas compartidas");

		loadFoldersNamesAndPrepareAdapter();

		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
				final String folder_name = foldersNames.get(i);
				final Dialog dialog = new Dialog(SharedFoldersActivity.this);
				dialog.setContentView(R.layout.dialog_sf_options);
				dialog.show();

				Button seeFiles = dialog.findViewById(R.id.see_files_button);
				seeFiles.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						// Se abre un diálogo con los archivos que hay en la carpeta seleccionada.
						Dialog dialog2 = new Dialog(SharedFoldersActivity.this);
						dialog2.setContentView(R.layout.dialog_see_files);
						TextView title = dialog2.findViewById(R.id.folder_name);
						title.setText(folder_name.substring(folder_name.lastIndexOf('/')+1));
						AEArrayAdapter filesAdapter = new AEArrayAdapter(SharedFoldersActivity.this,
								android.R.layout.simple_list_item_1, sharedFolders.get(folder_name));
						ListView files_list = dialog2.findViewById(R.id.files_list);
						files_list.setAdapter(filesAdapter);
						dialog2.show();
						dialog.dismiss();
					}
				});

				Button seeUsers = dialog.findViewById(R.id.see_users_button);
				seeUsers.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						/*
						 * Se abre la actividad que permite ver, añadir y eliminar algún usuario
						 * de la lista de acceso a la carpeta seleccionada. Si se borran todos
						 * entonces se elimina la carpeta de la aplicación.
						 */
						Intent intent = new Intent(SharedFoldersActivity.this, UsersSharedWithActivity.class);
						intent.putExtra("folderName", folder_name);
						intent.putExtra("users", foldersAccess.get(folder_name));
						intent.putExtra("friends", al_friends);
						dialog.dismiss();
						startActivityForResult(intent, 1);
					}
				});
			}
		});
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		// Recargar el adapter con los cambios hechos:
		boolean someRemovedOrAdded = data.getBooleanExtra("someRemovedOrAdded", false);
		if (someRemovedOrAdded)
			reloadSharedFoldersData();
	}



	/**
	 * Al presionar el botón atrás se deben enviar los arrayList a Profile para la gestión de
	 * peticiones de acceso a las carpetas compartidas.
	 */
	@Override
	public void onBackPressed(){
		Intent result = new Intent();
		result.putExtra("foldersArray", sharedFolders);
		result.putExtra("accessArray", foldersAccess);
		setResult(Activity.RESULT_OK, result);
		super.onBackPressed();
	}


	/**
	 * Accede a la BD para obtener las carpetas compartidas y los usuarios con acceso a dichas carpetas.
	 * Si ha habido algún cambio en los usuarios que tienen acceso a una carpeta compartida o se ha
	 * añadido, modificado, o eliminado alguna carpeta compartida se ha de llamar a este método.
	 */
	private void reloadSharedFoldersData(){
		sharedFolders = Profile.mDatabaseHelper.getSharedFolders();
		foldersAccess = Profile.mDatabaseHelper.getFoldersAccess();
		loadFoldersNamesAndPrepareAdapter();
	}


	/**
	 * Carga los datos de las carpetas en el adapter.
	 */
	private void loadFoldersNamesAndPrepareAdapter(){
		foldersNames = new ArrayList<>(sharedFolders.size());
		ArrayList<HashMap<String,String>> list = new ArrayList<>();

		Iterator it = sharedFolders.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry item = (Map.Entry) it.next();
			String folder_name = (String) item.getKey();
			foldersNames.add(folder_name);
			HashMap<String,String> map = new HashMap<>(2);
			//Obtiene el número de usuarios con acceso:
			int people = 0;
			ArrayList ar_people = foldersAccess.get(folder_name);
			if (ar_people != null)
				people = foldersAccess.get(folder_name).size();

			map.put("name", folder_name);
			map.put("users", "Amigos con acceso: " + people);
			//Añade a la lista el nombre de la carpeta y el número de personas con acceso.
			list.add(map);
		}

		adapter = new SimpleAdapter(this, list, android.R.layout.simple_list_item_2,
				new String[]{"name","users"}, new int[]{android.R.id.text1, android.R.id.text2});

		listView = findViewById(R.id.shared_folders_list);
		listView.setAdapter(adapter);
	}
}
