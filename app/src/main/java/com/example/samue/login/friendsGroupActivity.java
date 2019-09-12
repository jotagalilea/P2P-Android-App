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
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class friendsGroupActivity extends AppCompatActivity {

    private FriendsAdapter adapter;
    private ListView listView;
    private ArrayList<Friends> listFriends;
    private ArrayList<Friends> newFriends;
    private String username;
    private String groupname;
    private String adminGroup;
    static DatabaseHelper friendsGroupDatabaseHelper;

    FloatingActionButton deleteFriend;
    FloatingActionButton addFriend;
    String nameFriend;
    String friendsupdate;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_friends_group);
        Toolbar toolbar = findViewById(R.id.listfriendsgroup_toolbar);
        setSupportActionBar(toolbar);
        friendsGroupDatabaseHelper = new DatabaseHelper(this);
        //deleteFriend = findViewById(R.id.deleteFriends);
        addFriend = findViewById(R.id.addFriends);
        isadmin();
        listFriends= new ArrayList<Friends>();
        newFriends = new ArrayList<>();
        loadFriendsList();


        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                nameFriend = listFriends.get(position).getNombre();

                if (!username.equals(nameFriend)) {
                    final Dialog deletedialog = new Dialog(friendsGroupActivity.this);
                    deletedialog.setContentView(R.layout.dialog_deletefriendgroup);
                    deletedialog.show();


                    Button yes = deletedialog.findViewById(R.id.delete_friend_yes);
                    yes.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {
                            //removeGroup(nameGroup);
                            listFriends.remove(listFriends.get(position));
                            friendsupdate = arrayListToString(listFriends);
                            friendsGroupDatabaseHelper.deleteFriendToGroup(groupname, friendsupdate, friendsGroupDatabaseHelper.GROUPS_TABLE_NAME);
                            Toast.makeText(getApplicationContext(), nameFriend + " se ha eliminado", Toast.LENGTH_SHORT).show();
                            deletedialog.dismiss();
                            reloadlistview(listFriends);

                        }
                    });

                    Button no = deletedialog.findViewById(R.id.delete_friend_no);
                    no.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            deletedialog.dismiss();
                        }
                    });
                }
                return true; //esto hay que ver que poner
            }
        });
        FloatingActionButton addfriendgroup = findViewById(R.id.addFriends);
        addfriendgroup.setOnClickListener(new View.OnClickListener() {
                                              @Override
                                              public void onClick(View view) {
                                                  Intent myIntent = new Intent(friendsGroupActivity.this, friendsgroup.class);
                                                  myIntent.putExtra("nameGroup", groupname);
                                                  myIntent.putExtra("username",username);
                                                  myIntent.putExtra("valor",2); //valor=1, crear grupo, valor=2, añadir amigos nuevos
                                                  myIntent.putExtra("friendsold",arrayListToString(listFriends));
                                                  startActivityForResult(myIntent, 1);
                                              }
                                          }

        );


    }

    private void loadFriendsList() {
        String friendstmp;
        Bundle extras = getIntent().getExtras();
        username = extras.getString("username");
        groupname = extras.getString("nameGroup");
        adminGroup = extras.getString("administrator");
        friendstmp=extras.getString("friends");
        listFriends=stringtoArrayListFriend(friendstmp);

        adapter = new FriendsAdapter(this, listFriends);
        listView = findViewById(R.id.listfriendgroups);
        listView.setAdapter(adapter);

    }
    private void reloadlistview(ArrayList<Friends> friendsreload){
        adapter = new FriendsAdapter(this, friendsreload);
        listView = findViewById(R.id.listfriendgroups);
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

    //pasar de un array lists de amigos a un string
    private String arrayListToString(ArrayList<Friends> listfriend) {
        String myString =null;

        for (int i = 0; i<listfriend.size();i++){
            if (myString==null){
                myString=listfriend.get(i).getNombre();
                if (i < (listfriend.size() - 1)){myString = myString + ",";}
            }else {
                myString = myString + listfriend.get(i).getNombre();
                if (i < (listfriend.size() - 1)) {
                    myString = myString + ",";
                }
            }
        }
        return myString;
    }

    public void isadmin(){
        if(adminGroup!=username){
            addFriend.setEnabled(false);
            deleteFriend.setEnabled(false);
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        switch(requestCode){
            case 1:
                if(resultCode == Activity.RESULT_OK){
                    ArrayList<Friends> newListFriends = (ArrayList<Friends>) data.getSerializableExtra("friends");
                    for (Friends f: newListFriends)
                        if (!listFriends.contains(f)) {
                            listFriends.add(f);
                            newFriends.add(f);
                        }
                    adapter = new FriendsAdapter(this, this.listFriends);
                    listView = findViewById(R.id.listfriendgroups);
                    listView.setAdapter(adapter);
                    break;
                }

        }
    }


    @Override
    public void onBackPressed() {
        Intent result = new Intent();
        /* TODO
         * En realidad habría que coger los amigos nuevos, meterlos en la lista
         * del grupo listFriends y pasar listFriends cen el intent.
         */
        result.putExtra("newFriends", newFriends);
        result.putExtra("newgroup", this.groupname);
        setResult(Activity.RESULT_OK, result);
        //TODO: si no funciona llamar a finish()
        super.onBackPressed();
    }


}
