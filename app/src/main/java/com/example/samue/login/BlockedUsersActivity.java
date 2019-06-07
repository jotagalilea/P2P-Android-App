package com.example.samue.login;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import java.util.ArrayList;

/**
 * Created by jotagalilea on 06/02/2019.
 *
 * Actividad que implementa el sistema de bloqueo de usuarios.
 */
public class BlockedUsersActivity extends AppCompatActivity {

	DatabaseHelper helper;
	private Dialog dialog;
	private FriendsAdapter adapter;
	private ListView blocked_users_list;
	private ArrayList<Friends> al_friends;
	private ArrayList<Friends> al_blocked_users;



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		try{
			super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_blocked_users);
			Toolbar toolbar = findViewById(R.id.blocked_toolbar);
			setSupportActionBar(toolbar);
			getSupportActionBar().setTitle("Usuarios bloqueados");

			helper = Profile.mDatabaseHelper;

			Intent intent = getIntent();
			al_friends = (ArrayList<Friends>) intent.getSerializableExtra("amigos");

			blocked_users_list = findViewById(R.id.blocked_users_list);
			// Carga de los usuarios bloqueados:
			al_blocked_users = new ArrayList<>();
			loadBlockedUsers();

			// Si se pulsa en un usuario bloqueado se ofrece la posibilidad de desbloquearlo.
			blocked_users_list.setOnItemClickListener(new AdapterView.OnItemClickListener(){
				@Override
				public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
					final String user = al_blocked_users.get(position).getNombre();
					dialog = new Dialog(BlockedUsersActivity.this);
					dialog.setContentView(R.layout.dialog_remove_blocked_user);
					dialog.show();

					TextView textView = dialog.findViewById(R.id.remove_blocked_title);
					textView.setText("¿Desbloquear a " + user +"?");

					Button yes = dialog.findViewById(R.id.remove_blocked_yes);
					yes.setOnClickListener(new View.OnClickListener(){
						@Override
						public void onClick(View view) {
							removeBlockedUser(user);
							dialog.dismiss();
						}
					});

					Button no = dialog.findViewById(R.id.remove_blocked_no);
					no.setOnClickListener(new View.OnClickListener(){
						@Override
						public void onClick(View view) {
							dialog.dismiss();
						}
					});
				}
			});

			// El botón flotante bloquea un nuevo usuario.
			FloatingActionButton fab = findViewById(R.id.add_blocked_user_button);
			fab.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					dialog = new Dialog(BlockedUsersActivity.this);
					dialog.setContentView(R.layout.dialog_block_user);
					dialog.show();

					Button block = dialog.findViewById(R.id.button_block);
					block.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View view) {
							EditText user_name_text = dialog.findViewById(R.id.blocked_user_name);
							final String userName = user_name_text.getText().toString();

							// Si el usuario ya estaba bloqueado se muestra un aviso.
							if (al_blocked_users.contains(userName)){
								Toast.makeText(getApplicationContext(), userName + " ya estaba bloqueado", Toast.LENGTH_SHORT).show();
							}
							else{
								// Si el usuario que se quiere bloquear es amigo, se pide confirmación.
								final Friends friend = customListContains(userName, al_friends);
								if (friend != null){
									final Dialog confirm_dialog = new Dialog(BlockedUsersActivity.this);
									confirm_dialog.setContentView(R.layout.dialog_confirm_friend_block);
									confirm_dialog.show();

									TextView title = confirm_dialog.findViewById(R.id.block_friend_title);
									title.setText(userName + " es tu amigo.\n¿Deseas bloquearlo?");

									Button yes = confirm_dialog.findViewById(R.id.block_friend_yes);
									yes.setOnClickListener(new View.OnClickListener() {
										// Si se bloquea a un amigo este se borra de la lista de amigos.
										@Override
										public void onClick(View view) {
											addBlockedUser(userName);
											al_friends.remove(friend);
											helper.removeData(userName, helper.FRIENDS_TABLE_NAME);
											Toast.makeText(getApplicationContext(),userName + " bloqueado", Toast.LENGTH_SHORT).show();
											confirm_dialog.dismiss();
										}
									});

									Button no = confirm_dialog.findViewById(R.id.block_friend_no);
									no.setOnClickListener(new View.OnClickListener() {
										@Override
										public void onClick(View view) {
											confirm_dialog.dismiss();
										}
									});
								}

								// Si no es amigo se bloquea directamente.
								else{
									addBlockedUser(userName);
									Toast.makeText(getApplicationContext(),userName + " bloqueado", Toast.LENGTH_SHORT).show();
								}
							}

							dialog.dismiss();
						}
					});
				}
			});
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}


	/**
	 * Al presionar el botón atrás se debe enviar el arrayList de usuarios bloqueados a Profile
	 * para que sepa gestionar una petición de amistad de un usuario potencialmente no deseado.
	 */
	@Override
	public void onBackPressed(){
		Intent result = new Intent();
		result.putExtra("arrayBloqueados", al_blocked_users);
		setResult(Activity.RESULT_OK, result);
		super.onBackPressed();
	}


	/**
	 * Bloqueo de un usuario. Se inserta en la BD y se recarga la IU y el arrayList.
	 * @param name nombre del usuario.
	 */
	private void addBlockedUser(String name){
		boolean inserted = helper.addData(name, helper.BLOCKED_TABLE_NAME);
		if (inserted)
			loadBlockedUsers();
	}


	/**
	 * Desbloqueo de un usuario. Se borra de la BD y se recarga la IU y el arrayList.
	 * @param name nombre del usuario.
	 */
	private void removeBlockedUser(String name){
		boolean removed = helper.removeData(name, helper.BLOCKED_TABLE_NAME);
		if (removed)
			loadBlockedUsers();
	}


	/**
	 * Se recupera los datos de la tabla de bloqueados de la BD y se recarga el arrayList y la IU.
	 */
	private void loadBlockedUsers(){
		Cursor c = helper.getData(helper.BLOCKED_TABLE_NAME);
		al_blocked_users.clear();

		while(c.moveToNext())
			al_blocked_users.add(new Friends(c.getString(1), R.drawable.ic_launcher_foreground));
		adapter = new FriendsAdapter(this, al_blocked_users);
		blocked_users_list.setAdapter(adapter);
	}

	/**
	 * Averigua si existe un objeto Friends cuyo nombre coincida con name.
	 * @param name 	nombre del usuario que se busca.
	 * @param al 	ArrayList en el que se busca.
	 * @return 		Objeto friends si existe, o null en caso contrario.
	 */
	private Friends customListContains(String name, ArrayList<Friends> al){
		for(Friends f : al){
			if(f.getNombre().equals(name)){
				return f;
			}
		}
		return null;
	}

}
