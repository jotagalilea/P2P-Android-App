package com.example.samue.login;

import android.content.Intent;
import android.database.Cursor;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerViewAccessibilityDelegate;
import android.support.v7.widget.Toolbar;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.security.acl.Group;
import java.util.ArrayList;
import java.util.List;


public class friendsgroup extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RVadapter rvadapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<Friends> friends;
    private ArrayList<Friends> friendsSelected;
    public SparseBooleanArray selectedItems;
    private ArrayList files;
    private String nameGroup;
    private String administrator;
    private Groups newGroup;
    private DatabaseHelper helperGroup;
    public String username;
    static DatabaseHelper groupDatabaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friendsgroup);
        Toolbar toolbar = findViewById(R.id.group_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Selecciona los amigos");
        helperGroup = new DatabaseHelper(this);
        groupDatabaseHelper = new DatabaseHelper(this);


        Bundle extras = getIntent().getExtras();
        nameGroup = extras.getString("nameGroup");
        administrator = extras.getString("username");
        username =extras.getString("username");

        friendslist();
        friendsSelected = new ArrayList<>();

        recyclerView = (RecyclerView) findViewById(R.id.rv_friendsgroup);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // specify an adapter (see also next example)
        rvadapter = new RVadapter(friends);
        recyclerView.setAdapter(rvadapter);

        FloatingActionButton button = findViewById(R.id.createGroup);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean add;
                friendsSelected = rvadapter.obtenerSeleccionados();

                newGroup = new Groups(nameGroup, R.drawable.icongroup, friendsSelected, administrator);
                add = addGroupBBDD(nameGroup, friendsSelected, administrator);
                if (add) {
                    Toast.makeText(getApplicationContext(), "Group " + nameGroup + " has been created", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(friendsgroup.this, listGroupsActivity.class);
                    intent.putExtra("username", username);
                    startActivityForResult(intent, 1);
                }
                else {
                    Toast.makeText(getApplicationContext(), "Ha ocurrido un error", Toast.LENGTH_SHORT).show();
                }
                finish();
            }
        });
    }

    // cargar lista de amigos
    public void friendslist() {
        Cursor data = groupDatabaseHelper.getData(DatabaseHelper.FRIENDS_TABLE_NAME);
        friends = new ArrayList<>();
        while(data.moveToNext()){
            friends.add(new Friends(data.getString(1), R.drawable.ic_launcher_foreground));
        }
    }

    //añadir el grupo creado a la BBDD
    private boolean addGroupBBDD(String name, ArrayList<Friends> listFriends, String administrator) {
        String listFriendStrings = arrayListToString(listFriends);

        boolean inserted = helperGroup.addGroup(name, listFriendStrings, administrator);
        if (inserted)
            return inserted;
        else
            return false;
    }




   // ----------------------a partir de aqui revisar que esto no sobre-------------------

    private void removeGroup(String name) {
        boolean removed = helperGroup.deleteGroup(name, helperGroup.GROUPS_TABLE_NAME);
        if (removed)
            loadGroups();
    }

    private void loadGroups() {
        Cursor c = helperGroup.getData(helperGroup.GROUPS_TABLE_NAME);
        friends.clear();

        while (c.moveToNext())
            friends.add(new Friends(c.getString(1), R.drawable.ic_launcher_foreground));
        rvadapter = new RVadapter(friends);
        recyclerView.setAdapter(rvadapter);
    }

    //usar esta funcion para comprobar que no se crea un grupo igual al que ya pertenezca
    /**
     * Averigua si existe un objeto Groups cuyo nombre coincida con nameGroup.
     *
     * @param nameGroup nombre del grupo que se busca.
     * @param gr        ArrayList en el que se busca.
     * @return Objeto group si existe, o null en caso contrario.
     */
    private Groups customListContains(String nameGroup, ArrayList<Groups> gr) {
        for (Groups g : gr) {
            if (g.getNameGroup().equals(nameGroup)) {
                return g;
            }
        }
        return null;
    }

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

