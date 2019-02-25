package com.example.samue.login;

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
 * Created by Julio on 06/02/2019.
 */
public class BlockedUsersActivity extends AppCompatActivity {

	//private BlockedUsersHelper helper;
	DatabaseHelper helper;
	private Dialog dialog;
	FriendsAdapter adapter;
	ListView blocked_users_list;
	ArrayList<Friends> al_blocked_users;



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		try{
			super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_blocked_users);
			Toolbar toolbar = findViewById(R.id.my_toolbar);
			setSupportActionBar(toolbar);

			//Intent intent = getIntent();
			//databaseHelper = (DatabaseHelper) intent.getSerializableExtra("DBHelper");
			//helper = new BlockedUsersHelper(this);
			helper = Profile.mDatabaseHelper;

			blocked_users_list = findViewById(R.id.blocked_users_list);
			// Carga de los usuarios bloqueados:
			al_blocked_users = new ArrayList<>();
			loadBlockedUsers();

			blocked_users_list.setOnItemClickListener(new AdapterView.OnItemClickListener(){
				@Override
				public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
					final String user = al_blocked_users.get(position).getNombre();
					dialog = new Dialog(BlockedUsersActivity.this);
					dialog.setContentView(R.layout.dialog_remove_blocked_user);
					//TODO: ¿No debería ir el show() al final?
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
							String userName = user_name_text.getText().toString();

							if (al_blocked_users.contains(userName)){
								Toast.makeText(getApplicationContext(), userName + " ya estaba bloqueado", Toast.LENGTH_SHORT).show();
							}
							else{
								addBlockedUser(userName);
								Toast.makeText(getApplicationContext(),userName + " bloqueado", Toast.LENGTH_SHORT).show();
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

	@Override
	protected void onPause(){
		super.onPause();
		/*
		 *
		 */
	}



	private void addBlockedUser(String name){
		boolean inserted = helper.addData(name, helper.BLOCKED_TABLE_NAME);
		if (inserted)
			loadBlockedUsers();
	}


	private void removeBlockedUser(String name){
		boolean removed = helper.removeData(name, helper.BLOCKED_TABLE_NAME);
		if (removed)
			loadBlockedUsers();
	}


	private void loadBlockedUsers(){
		Cursor c = helper.getData(helper.BLOCKED_TABLE_NAME);
		/*
		 * Lo suyo sería renombrar la clase Friends por User y añadirle un booleano que sirva
		 * para identificar si el objeto es un amigo o un usuario bloqueado.
		 */
		al_blocked_users.clear();

		while(c.moveToNext())
			al_blocked_users.add(new Friends(c.getString(1), R.drawable.ic_launcher_foreground));
		adapter = new FriendsAdapter(this, al_blocked_users);
		blocked_users_list.setAdapter(adapter);
	}

}
