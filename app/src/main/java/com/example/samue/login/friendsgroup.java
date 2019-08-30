package com.example.samue.login;

import android.content.Intent;
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
import java.util.ArrayList;
import java.util.List;


public class friendsgroup extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RecyclerView.Adapter rvadapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<Friends> friends;
    private ArrayList<Friends> friendsSelected;
    public SparseBooleanArray selectedItems;
    private ArrayList files;
    private String nameGroup;
    private Groups newGroup;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friendsgroup);
        Toolbar toolbar = findViewById(R.id.group_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Selecciona los amigos");

        Bundle extras = getIntent().getExtras();
        nameGroup = extras.getString("nameGroup");


        //ArrayList<Friends> friends= new ArrayList<>();
        //llamar a base de datos u cargar amigos de verdad, no lista estatica
        friends=listadeamigos();
        friendsSelected=new ArrayList<>();


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


                newGroup= new Groups(nameGroup,R.drawable.icongroup,friendsSelected);
                //Falta la implemenntacion de guardar los datos en la BBDD
               // ArrayList<Friends> marcados = RVadapter.;
                String contenidoMarcados = "Marcados: ";
               // for (Friends os : marcados){
               //     contenidoMarcados += os.getTexto() + ", ";
               // }
                Toast.makeText(getApplicationContext(), "Group "+ nameGroup + " has been created", Toast.LENGTH_SHORT).show();
                //Toast.makeText(getApplicationContext(), "Seleccionados: "+ nameGroup , Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
    // ...
    public ArrayList<Friends> listadeamigos(){
        ArrayList<Friends> listFriends= new ArrayList<>();
        listFriends.add(new Friends("Alex", R.drawable.astronaura));
        listFriends.add(new Friends("Alba", R.drawable.cohete));
        listFriends.add(new Friends("Rupert", R.drawable.astronaura));
        return listFriends;
    }





}

