package com.example.samue.login;

import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;

public class friendsGroupActivity extends AppCompatActivity {

    private FriendsAdapter adapter;
    private ListView listView;
    private ArrayList<Friends> listFriends;
    private String username;
    private String groupname;
    private String adminGroup;
    static DatabaseHelper friendsGroupDatabaseHelper;

    FloatingActionButton deleteFriend = findViewById(R.id.deleteFriends);
    FloatingActionButton addFriend = findViewById(R.id.addFriends);



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_friends_group);
        Toolbar toolbar = findViewById(R.id.listGroups_toolbar);
        setSupportActionBar(toolbar);
        friendsGroupDatabaseHelper = new DatabaseHelper(this);
        isadmin();
        listFriends= new ArrayList<Friends>();
        loadFriendsList();
        

    }

    private void loadFriendsList() {
        Bundle extras = getIntent().getExtras();
        listFriends = (ArrayList<Friends>) getIntent().getSerializableExtra("friends");
        username = extras.getString("username");
        groupname = extras.getString("nameGroup");
        adminGroup = extras.getString("administrator");

        Cursor c = friendsGroupDatabaseHelper.getData(DatabaseHelper.GROUPS_TABLE_NAME);
        //Log.d("ALEX",c.getString(0));
        if (listFriends != null){listFriends.clear();}
        else {listFriends = new ArrayList<>();}

        while (c.moveToNext()) {
            ArrayList<Friends> friends = stringtoArrayListFriend(c.getString(1));
            listFriends = friends;
        }
        adapter = new FriendsAdapter(this, listFriends);
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

    public void isadmin(){
        if(adminGroup!=username){
            addFriend.setEnabled(false);
            deleteFriend.setEnabled(false);
        }
    }


    @Override
    public void onBackPressed() {
        finish();
    }


}
