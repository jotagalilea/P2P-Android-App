package com.example.samue.login;

import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Iterator;

public class UsersSharedWith_Activity extends AppCompatActivity {

	private DatabaseHelper helper;
	private String folderName;
	private ArrayList<String> usersWithAccess;
	private ArrayList<Friends> al_friends;
	private ArrayList<String> usersSelected;
	private SelectFriends_Adapter adapter;
	private ListView listView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_users_shared_with);
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		usersSelected = new ArrayList<>();
		helper = Profile.mDatabaseHelper;
		Intent intent = getIntent();
		// TODO: Si no funciona, usar getSerializableExtra().
		usersWithAccess = intent.getStringArrayListExtra("users");
		folderName = intent.getStringExtra("folderName");
		al_friends = (ArrayList<Friends>) intent.getSerializableExtra("friends");

		listView = findViewById(R.id.friends_list);
		adapter = new SelectFriends_Adapter(UsersSharedWith_Activity.this, usersWithAccess);
		listView.setAdapter(adapter);

		// Con el botón se podrán añadir nuevos amigos a la carpeta.
		final FloatingActionButton addFriendsFAB = findViewById(R.id.addFriendsFAB);
		addFriendsFAB.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				final ArrayList<String> remainingFriends = getRemainingFriends();
				if (remainingFriends != null) {
					final Dialog dg = new Dialog(UsersSharedWith_Activity.this);
					dg.setContentView(R.layout.dialog_addfriendssharedfolder);
					dg.show();

					final SelectFriends_Adapter adap = new SelectFriends_Adapter(UsersSharedWith_Activity.this, remainingFriends);
					ListView dg_list = dg.findViewById(R.id.friends_list);
					dg_list.setAdapter(adap);

					Button addSelection = dg.findViewById(R.id.button_add);
					addSelection.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View view) {
							//TODO: tomar los seleccionados y añadirlos a usersWithAccess y a la BD.
							boolean selected[] = adap.getSelected();
							ArrayList<String> usersSel = new ArrayList<>(selected.length);
							for (int i=0; i<selected.length; i++){
								if (selected[i]) {
									usersSel.add(remainingFriends.get(i));
									usersWithAccess.add(remainingFriends.get(i));
								}
							}
							//TODO: ADAPTAR el ArrayList para que sea del tipo esperado por add...folder().
							helper.addFriends2Folder(usersSel, folderName);
						}
					});
				}
				else Toast.makeText(getApplicationContext(), "No quedan amigos por añadir a esta carpeta", Toast.LENGTH_LONG).show();
			}
		});

		// Con este botón se podrá eliminar el acceso de los amigos seleccionados a la carpeta.
		FloatingActionButton removeFriendsFAB = findViewById(R.id.removeFriendsFAB);
		removeFriendsFAB.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				// TODO: Faltaría meter un diálogo de confirmación.
				boolean[] selected = adapter.getSelected();
				for (int i = 0; i<selected.length; i++) {
					if (selected[i])
						usersSelected.add(usersWithAccess.get(i));
				}
				boolean success = helper.removeFriendsFromFolder(folderName, usersSelected);
				onBackPressed();
			}
		});
	}


	/**
	 * Obtiene una lista con los amigos que aún no hayan sido añadidos a la carpeta.
	 * @return
	 */
	private ArrayList<String> getRemainingFriends(){
		int size = al_friends.size() - usersWithAccess.size();
		if (size == 0)
			return null;

		ArrayList<String> result = new ArrayList<>(size);
		Iterator<Friends> it = al_friends.iterator();
		while (it.hasNext()){
			String f = it.next().getNombre();
			if (!usersWithAccess.contains(f))
				result.add(f);
		}
		return result;
	}



	@Override
	public void onBackPressed(){
		super.onBackPressed();
		// TODO:
	}

}
