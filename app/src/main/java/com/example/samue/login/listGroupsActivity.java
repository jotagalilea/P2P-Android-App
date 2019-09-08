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
import android.widget.Toast;

import java.util.ArrayList;

public class listGroupsActivity extends AppCompatActivity {

    private GroupsAdapter adapter;
    private ListView listView;
    private ArrayList<Groups> listGroups;
    private String username;
    static DatabaseHelper groupDatabaseHelper;


    Dialog mdialogCreate;
    EditText nameGroupText;
    String nameGroup;
    Button bf;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_groups);
        Toolbar toolbar = findViewById(R.id.listGroups_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Grupos");
        groupDatabaseHelper = new DatabaseHelper(this);

        ArrayList<Friends> listFriends= new ArrayList<>();
        listGroups= new ArrayList<Groups>();
        Bundle extras = getIntent().getExtras();
        username=extras.getString("username");
        loadGroupList();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                nameGroup = listGroups.get(i).getNameGroup();
                final Groups group = listGroups.get(i);
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
                        Intent intent = new Intent(listGroupsActivity.this, friendsGroupActivity.class);
                        //intent.putExtra("folderName", folder_name);
                        //intent.putExtra("users", foldersAccess.get(folder_name));
                        //intent.putExtra("friends", al_friends);
                        dialog.dismiss();
                        startActivityForResult(intent, 1);
                    }
                });

                //Boton ver amigos del dialogo del grupo seleccionado
                Button seeFriends = dialog.findViewById(R.id.friends_button);
                seeFriends.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        /*
                         * Se abre la actividad que permite ver, añadir y eliminar algún amigo
                         * de la lista de acceso al grupo seleccionado. Si se borran todos
                         * entonces se elimina la carpeta de la aplicación.
                         */
                        dialog.dismiss();
                        Intent intent = new Intent(listGroupsActivity.this, friendsGroupActivity.class);
                        intent.putExtra("nameGroup", group.getNameGroup());
                        intent.putExtra("friends", arrayListToString(group.getListFriends()));
                        intent.putExtra("administrator", group.getAdministrador());
                        intent.putExtra("username",username);
                        startActivityForResult(intent, 1);
                    }
                });
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                nameGroup = listGroups.get(position).getNameGroup();
                final Dialog deletedialog = new Dialog(listGroupsActivity.this);
                deletedialog.setContentView(R.layout.dialog_deletegroup);
                deletedialog.show();


                Button yes = deletedialog.findViewById(R.id.delete_group_yes);
                yes.setOnClickListener(new View.OnClickListener() {
                    // Si se bloquea a un amigo este se borra de la lista de amigos.
                    @Override
                    public void onClick(View view) {
                        //removeGroup(nameGroup);
                        listGroups.remove(listGroups.get(position));
                        groupDatabaseHelper.deleteGroup(nameGroup, groupDatabaseHelper.GROUPS_TABLE_NAME);
                        Toast.makeText(getApplicationContext(),nameGroup + " se ha eliminado", Toast.LENGTH_SHORT).show();
                        deletedialog.dismiss();
                        loadGroupList();
                    }
                });

                Button no = deletedialog.findViewById(R.id.delete_group_no);
                no.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {deletedialog.dismiss();}
                });
                return true; //esto hay que ver que poner
            }
        });



        FloatingActionButton createGroup = findViewById(R.id.createGroup);
        createGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mdialogCreate = new Dialog(listGroupsActivity.this);
                mdialogCreate.setContentView(R.layout.dialog_newgroup);
                mdialogCreate.show();
                nameGroupText = (EditText) mdialogCreate.findViewById(R.id.nameGroup);

                bf = (Button) mdialogCreate.findViewById(R.id.button_addFriends);

                bf.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        mdialogCreate.dismiss();
                        Intent myIntent = new Intent(listGroupsActivity.this, friendsgroup.class);
                        myIntent.putExtra("nameGroup", nameGroupText.getText().toString());
                        myIntent.putExtra("username",username);
                        startActivityForResult(myIntent, 3);
                        finish();
                    }

                });
            }
        });
        FloatingActionButton backFriends = findViewById(R.id.backFriends);
        backFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

    }

    @Override
    public void onBackPressed() {
        finish();
    }
    /**
     * Carga de los grupos que estan almacenados en la BD.
     */
    private void loadGroupList() {
        Cursor c = groupDatabaseHelper.getData(DatabaseHelper.GROUPS_TABLE_NAME);
        //Log.d("ALEX",c.getString(0));
        if (listGroups != null){listGroups.clear();}
        else {listGroups = new ArrayList<>();}

        while (c.moveToNext()) {
            ArrayList<Friends> friends = stringtoArrayListFriend(c.getString(1));
            ArrayList files = stringtoArrayList(c.getString(2));
            ArrayList<Friends> owners = stringtoArrayListFriend(c.getString(3));
            Groups g = new Groups(c.getString(0), R.drawable.icongroup, friends, files, owners, c.getString(4));
            listGroups.add(g);
        }
        adapter = new GroupsAdapter(this, listGroups);
        listView = findViewById(R.id.groups_list);
        listView.setAdapter(adapter);

    }
    private ArrayList<Friends> stringtoArrayListFriend(String friends){
        if (friends == null){return new ArrayList<>();}
        ArrayList<Friends> resultado= new ArrayList<>();
        String[] friendsSeparate = friends.split(",");
        for (int i=0; i<friendsSeparate.length; i++){
            resultado.add(new Friends(friendsSeparate[i],R.drawable.astronaura));
        }
        return resultado;
    }
    private ArrayList stringtoArrayList(String files){
        if (files == null){
            return new ArrayList<>();
        }
        ArrayList resultado= new ArrayList();
        String[] filesSeparate = files.split(",");
        for (int i=0; i<filesSeparate.length; i++){
            resultado.add(filesSeparate[i]);
        }
        return resultado;
    }
    private String ArrayListToString (ArrayList list){
        String resultado =null;
        for (int i=0; i<list.size(); i++){
            resultado=resultado + list.get(i).toString();
        }
        return resultado;
    }
    private String ArrayListFriendToString (ArrayList<Friends> list){
        String resultado ="";
        for (int i=0; i<list.size(); i++){
            resultado=resultado + list.get(i).getNombre();
        }
        return resultado;
    }

    private void removeGroup(String nameGroup){}

    //pasar de un array lists de amigos a un string
    private String arrayListToString(ArrayList<Friends> listfriend) {

        String myString ="";
        for (int i = 0; i<listfriend.size();i++){
            myString = myString + listfriend.get(i).getNombre();
            if (i < (listfriend.size()-1)){
                myString = myString + ",";
            }
        }
        return myString;
    }

}
