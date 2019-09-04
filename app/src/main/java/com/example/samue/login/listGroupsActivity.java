package com.example.samue.login;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class listGroupsActivity extends AppCompatActivity {

    private GroupsAdapter adapter;
    private ListView listView;
    private ArrayList<Groups> listGroups;


    Dialog mdialogCreate;
    Dialog mdialogDelete;
    EditText nameGroup;
    Button bf;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_groups);

        ArrayList<Friends> listFriends= new ArrayList<>();
        listFriends.add(new Friends("Alex", R.drawable.astronaura));
        listFriends.add(new Friends("Alba", R.drawable.cohete));
        listFriends.add(new Friends("Rupert", R.drawable.astronaura));
        listGroups= new ArrayList<Groups>();
        listGroups.add(new Groups("grupo1",R.drawable.group,listFriends));
        listGroups.add(new Groups("grupo2",R.drawable.group,listFriends));
        adapter = new GroupsAdapter(this, listGroups);
        listView = findViewById(R.id.groups_list);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                final Dialog dialog = new Dialog(listGroupsActivity.this);
                dialog.setContentView(R.layout.dialog_group);
                dialog.show();

                //Boton ver archivos del dialogo del grupo seleccionado
                Button seeFiles = dialog.findViewById(R.id.files_button);
                seeFiles.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        /*
                         * Se abre la actividad que permite ver, añadir y eliminar algún amigo
                         * del grupo seleccionado.
                         */
                        Intent intent = new Intent(listGroupsActivity.this, FriendsGroupActivity.class);
                        //intent.putExtra("folderName", folder_name);
                        //intent.putExtra("users", foldersAccess.get(folder_name));
                        //intent.putExtra("friends", al_friends);
                        dialog.dismiss();
                        startActivityForResult(intent, 1);
                    }
                });

                //Boton ver amigos del dialogo del grupo seleccionado
                Button seeUsers = dialog.findViewById(R.id.friends_button);
                seeUsers.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        /*
                         * Se abre la actividad que permite ver, añadir y eliminar algún amigo
                         * de la lista de acceso a la carpeta seleccionada. Si se borran todos
                         * entonces se elimina la carpeta de la aplicación.
                         */
                        Intent intent = new Intent(listGroupsActivity.this, FilesGroupActivity.class);
                        //intent.putExtra("folderName", folder_name);
                        //intent.putExtra("users", foldersAccess.get(folder_name));
                        //intent.putExtra("friends", al_friends);
                        dialog.dismiss();
                        startActivityForResult(intent, 1);
                    }
                });
            }
        });
        FloatingActionButton createGroup = findViewById(R.id.createGroup);
        createGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mdialogCreate = new Dialog(listGroupsActivity.this);
                mdialogCreate.setContentView(R.layout.dialog_newgroup);
                mdialogCreate.show();
                nameGroup = (EditText) mdialogCreate.findViewById(R.id.nameGroup);
                bf = (Button) mdialogCreate.findViewById(R.id.button_addFriends);

                bf.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        mdialogCreate.dismiss();
                        Intent myIntent = new Intent(listGroupsActivity.this, friendsgroup.class);
                        myIntent.putExtra("nameGroup", nameGroup.getText().toString());
                        startActivityForResult(myIntent, 3);
                        finish();
                    }

                });
            }
        });

    }

    @Override
    public void onBackPressed() {
        finish();


    }





}
