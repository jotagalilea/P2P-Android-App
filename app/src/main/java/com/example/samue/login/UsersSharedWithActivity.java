package com.example.samue.login;

import android.app.Dialog;
import android.content.Intent;
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

/**
 * Created by jotagalilea on 20/07/2019.
 *
 * Actividad que permite gestionar (ver, añadir, y eliminar) los usuarios que tienen acceso a una carpeta compartida.
 */
public class UsersSharedWithActivity extends AppCompatActivity {

	private DatabaseHelper helper;
	private String folderName;
	private ArrayList<String> usersWithAccess;
	private ArrayList<Friends> al_friends;
	private ArrayList<String> usersSelected;
	private SelectFriends_Adapter adapter;
	private ListView listView;
	private boolean noFriendsLeft;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_users_shared_with);
		Toolbar toolbar = findViewById(R.id.my_toolbar2);
		setSupportActionBar(toolbar);
		usersSelected = new ArrayList<>();
		helper = Profile.mDatabaseHelper;
		Intent intent = getIntent();
		usersWithAccess = intent.getStringArrayListExtra("users");
		folderName = intent.getStringExtra("folderName");
		al_friends = (ArrayList<Friends>) intent.getSerializableExtra("friends");

		listView = findViewById(R.id.friendswaccess_list);
		if ((usersWithAccess!=null) && (usersWithAccess.size()>0)) {
			adapter = new SelectFriends_Adapter(UsersSharedWithActivity.this, usersWithAccess);
			noFriendsLeft = false;
		}
		else {
			ArrayList<String> empty_al = new ArrayList<>();
			empty_al.add("No quedan amigos con acceso");
			adapter = new SelectFriends_Adapter(UsersSharedWithActivity.this, empty_al);
			noFriendsLeft = true;
		}
		listView.setAdapter(adapter);

		Intent nothingChanged = new Intent();
		nothingChanged.putExtra("someRemovedOrAdded", false);
		setResult(RESULT_OK, nothingChanged);

		// Con el botón se podrán añadir nuevos amigos a la carpeta.
		final FloatingActionButton addFriendsFAB = findViewById(R.id.addFriendsFAB);
		addFriendsFAB.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				final ArrayList<String> remainingFriends = getRemainingFriends();
				if (remainingFriends != null) {
					final Dialog dg = new Dialog(UsersSharedWithActivity.this);
					dg.setContentView(R.layout.dialog_addfriendssharedfolder);
					dg.show();

					final SelectFriends_Adapter adap = new SelectFriends_Adapter(UsersSharedWithActivity.this, remainingFriends);
					ListView dg_list = dg.findViewById(R.id.select_friends_list);
					dg_list.setAdapter(adap);

					Button addSelection = dg.findViewById(R.id.button_add_selected);
					addSelection.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View view) {
							boolean selected[] = adap.getSelected();
							ArrayList<String> usersSel = new ArrayList<>(selected.length);
							for (int i=0; i<selected.length; i++){
								if (selected[i]) {
									usersSel.add(remainingFriends.get(i));
									if (usersWithAccess==null)
										usersWithAccess = new ArrayList<>();
									usersWithAccess.add(remainingFriends.get(i));
								}
							}
							helper.addFriends2Folder(usersSel, folderName);
							Intent addedINT = new Intent();
							addedINT.putExtra("someRemovedOrAdded", true);
							setResult(RESULT_OK, addedINT);
							dg.dismiss();
							onBackPressed();
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
				// Si no quedan usuarios con acceso y se pulsa el botón entonces se borra la carpeta:
				if (noFriendsLeft) {
					helper.removeSharedFolder(folderName);
					Intent removedINT = new Intent();
					removedINT.putExtra("someRemovedOrAdded", true);
					setResult(RESULT_OK, removedINT);
					onBackPressed();
				}
				else if (adapter.getCountSelected() > 0) {
					boolean[] selected = adapter.getSelected();
					for (int i = 0; i < selected.length; i++) {
						if (selected[i])
							usersSelected.add(usersWithAccess.get(i));
					}
					boolean success = helper.removeFriendsFromFolder(folderName, usersSelected);
					boolean allRemoved = usersSelected.size() == adapter.getCountSelected();
					if (success && allRemoved)
						// La próxima vez que se pulse el botón de la papelera se borrará la carpeta
						// si no se añaden nuevos usuarios a ella.
						noFriendsLeft = true;

					Intent removedINT = new Intent();
					removedINT.putExtra("someRemovedOrAdded", true);
					setResult(RESULT_OK, removedINT);
					onBackPressed();
				}
				else Toast.makeText(UsersSharedWithActivity.this, "Error: Ningún amigo seleccionado", Toast.LENGTH_SHORT).show();
			}
		});
	}


	/**
	 * Obtiene una lista con los amigos que aún no hayan sido añadidos a la carpeta.
	 * @return lista de los "restantes".
	 */
	private ArrayList<String> getRemainingFriends(){
		int size;
		if (usersWithAccess!=null)
			size = al_friends.size() - usersWithAccess.size();
		else
			size = al_friends.size();
		if (size == 0)
			return null;

		ArrayList<String> result = new ArrayList<>(size);
		Iterator<Friends> it = al_friends.iterator();
		while (it.hasNext()){
			String f = it.next().getNombre();
			if (usersWithAccess==null || !usersWithAccess.contains(f))
				result.add(f);
		}
		return result;
	}

}
