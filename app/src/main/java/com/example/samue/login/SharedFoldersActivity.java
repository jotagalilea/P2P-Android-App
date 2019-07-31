package com.example.samue.login;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SharedFoldersActivity extends AppCompatActivity {

	//TODO Implementar adaptador.
	//private FoldersListAdapter adapter;
	private SimpleAdapter adapter;
	private ListView listView;
	private HashMap<String,ArrayList<String>> sharedFolders;
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
		foldersAccess = (HashMap<String,ArrayList<String>>) intent.getSerializableExtra("folderAccess");
		al_friends = (ArrayList<Friends>) intent.getSerializableExtra("friends");

		loadFoldersNamesAndPrepareAdapter();
		//adapter = new FoldersListAdapter(this, android.R.layout.simple_list_item_1, foldersNames);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
				final String folder_name = foldersNames.get(i);
				Dialog dialog = new Dialog(SharedFoldersActivity.this);
				dialog.setContentView(R.layout.dialog_sf_options);
				dialog.show();

				Button seeFiles = dialog.findViewById(R.id.see_files_button);
				seeFiles.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						// TODO: Probar:
						// Se abre un diálogo con los archivos que hay en la carpeta seleccionada.
						Dialog dialog = new Dialog(SharedFoldersActivity.this);
						dialog.setContentView(R.layout.dialog_see_files);
						AEArrayAdapter filesAdapter = new AEArrayAdapter(SharedFoldersActivity.this,
								android.R.layout.simple_list_item_1, sharedFolders.get(folder_name));
						ListView files_list = dialog.findViewById(R.id.files_list);
						files_list.setAdapter(filesAdapter);
						dialog.show();
					}
				});

				Button seeUsers = dialog.findViewById(R.id.see_users_button);
				seeUsers.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						// Se abre la actividad que permite ver, añadir o eliminar algún usuario
						// de la lista de acceso a la carpeta seleccionada.
						//TODO: ¿devolver resultado?
						Intent intent = new Intent(SharedFoldersActivity.this, UsersSharedWith_Activity.class);
						intent.putExtra("folderName", folder_name);
						intent.putExtra("users", foldersAccess.get(folder_name));
						intent.putExtra("friends", al_friends);
						startActivity(intent);
					}
				});
			}
		});
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
	 * Carga los datos de las carpetas compartidas en el adapter.
	 */
	private void loadFoldersNamesAndPrepareAdapter(){
		foldersNames = new ArrayList<>(sharedFolders.size());
		ArrayList<HashMap<String,String>> list = new ArrayList<>();

		Iterator it = sharedFolders.entrySet().iterator();
		while (it.hasNext()) {
			String item = (String) it.next();
			foldersNames.add(item);
			HashMap<String,String> map = new HashMap<>(2);
			//Obtiene el número de usuarios con acceso:
			int people = foldersAccess.get(item).size();
			map.put("name", item);
			map.put("users", Integer.toString(people));
			//Añade a la lista el nombre de la carpeta y el número de personas con acceso.
			list.add(map);
		}

		adapter = new SimpleAdapter(this, list, android.R.layout.simple_list_item_2,
				new String[]{"name","users"}, new int[]{android.R.id.text1, android.R.id.text2});

		listView = findViewById(R.id.shared_folders_list);
		listView.setAdapter(adapter);
	}
}
