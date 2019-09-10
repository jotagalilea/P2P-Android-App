package com.example.samue.login;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ArchiveExplorerGroup extends AppCompatActivity {
    private Dialog mdialog;
    private ArrayList listaNombresArchivos;
    private List listaRutasArchivos;
    private ArrayAdapter adaptador;
    private String directorioRaiz;
    private TextView carpetaActual;
    private String currentFolder;
    private ListView listaItems;
    private FloatingActionButton fab;

    private File[]listaArchivos;
    private String username;
    private String namegroup;
    private String filesstring;
    private String ownersstring;
    private DatabaseHelper helperGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_archive_explorer_group);

        carpetaActual = findViewById(R.id.rutaActual);
        listaItems = findViewById(R.id.lista_items);

        Bundle extras = getIntent().getExtras();
        username = extras.getString("username");
        namegroup = extras.getString("namegroup");
        filesstring=extras.getString("listfiles");
        ownersstring=extras.getString("listowners");

        directorioRaiz = Environment.getExternalStorageDirectory().getPath();

        verArchivosDirectorio(directorioRaiz);

        // Compartir un archivo:
        listaItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                File archivo = new File((String)listaRutasArchivos.get(position));

                // Si es un archivo se muestra un Toast con su nombre y si es un directorio
                // se cargan los archivos que contiene en el listView
                if (archivo.isFile()) {
                    final String name = archivo.getName();
                    final String path = archivo.getPath();

                    mdialog = new Dialog(ArchiveExplorerGroup.this);
                    mdialog.setContentView(R.layout.dialog_confirmsharedarchive);
                    mdialog.show();

                    TextView tv = mdialog.findViewById(R.id.confirm_archive_tv);
                    tv.setText("¿Quieres compartir " + archivo.getName() + " con tus amigos?");

                    Button yes = mdialog.findViewById(R.id.confirm_archive_yes);
                    Button no = mdialog.findViewById(R.id.confirm_archive_no);

                    no.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mdialog.dismiss();
                        }
                    });

                    yes.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            boolean add;

                            add = addfilegroupBBDD(namegroup, name, username);
                            if (add) {
                                Toast.makeText(getApplicationContext(), "File " + name + " has been add", Toast.LENGTH_SHORT).show();

                            }
                            else {
                                Toast.makeText(getApplicationContext(), "Ha ocurrido un error", Toast.LENGTH_SHORT).show();
                            }
                            finish();

                        }
                    });

                } else {
                    // Si es un directorio mostramos todos los archivos que contiene
                    verArchivosDirectorio((String)listaRutasArchivos.get(position));
                }
            }
        });

        // Compartir carpeta:
        fab = findViewById(R.id.fab_folder_share);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mdialog = new Dialog(ArchiveExplorerGroup.this);
                mdialog.setContentView(R.layout.dialog_share_folder);
                mdialog.show();

                Button yes = mdialog.findViewById(R.id.share_folder_yes);
                Button no = mdialog.findViewById(R.id.share_folder_no);

                no.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mdialog.dismiss();
                    }
                });
                yes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final ProgressDialog progressDialog = new ProgressDialog(ArchiveExplorerGroup.this);
                        progressDialog.setIndeterminate(true);
                        progressDialog.setMessage("Compartiendo carpeta");
                        progressDialog.show();

                        new android.os.Handler().postDelayed(
                                new Runnable() {
                                    public void run() {
                                        // Abrir diálogo para añadir amigos:
                                        final Dialog addFriendsDl = new Dialog(ArchiveExplorerGroup.this);
                                        addFriendsDl.setContentView(R.layout.dialog_addfriendssharedfolder);

                                        // Obtener lista de amigos para elegir:
                                        Intent intent = getIntent();
                                        final ArrayList<Friends> friendsList = (ArrayList<Friends>) intent.getSerializableExtra("friendsList");
                                        final ArrayList<String> friendsNames = Utils.getFriendsArrayListAsStrings(friendsList);
                                        ListView selectionList = addFriendsDl.findViewById(R.id.select_friends_list);
                                        final SelectFriends_Adapter fAdapter = new SelectFriends_Adapter(ArchiveExplorerGroup.this, friendsNames);
                                        selectionList.setAdapter(fAdapter);
                                        progressDialog.dismiss();
                                        addFriendsDl.show();

                                        Button addSelected = addFriendsDl.findViewById(R.id.button_add_selected);
                                        addSelected.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                // Si hay alguno seleccionado se procede:
                                                if (fAdapter.getCountSelected() > 0) {
                                                    // Se añade la carpeta con los nombres de los archivos que contiene a la BD, excluyendo '../':
                                                    ArrayList<String> sublist = new ArrayList<String>(listaNombresArchivos.subList(1,listaNombresArchivos.size()));
                                                    Profile.mDatabaseHelper.addSharedFolder(currentFolder, Utils.joinStrings(",",sublist));
                                                    // Se añaden los amigos seleccionados a la tabla de acceso:
                                                    boolean selected[] = fAdapter.getSelected();
                                                    ArrayList<String> friendsSelected = new ArrayList<>();
                                                    for (int i = 0; i < friendsList.size(); i++) {
                                                        if (selected[i])
                                                            friendsSelected.add(friendsNames.get(i));
                                                    }
                                                    Profile.mDatabaseHelper.addFriends2Folder(friendsSelected, currentFolder);
                                                    Intent result = new Intent();
                                                    result.putExtra("folder_sharing", true);
                                                    setResult(RESULT_OK, result);
                                                    progressDialog.dismiss();
                                                    addFriendsDl.dismiss();
                                                    finish();
                                                }
                                                // Si no hay ninguno seleccionado...
                                                else{
                                                    Toast.makeText(ArchiveExplorerGroup.this, "ERROR: Ningún amigo seleccionado", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }
                                }, 2000);
                        mdialog.dismiss();
                    }
                });
            }
        });
    }


    private void verArchivosDirectorio(String rutaDirectorio) {
        carpetaActual.setText("Estas en: " + rutaDirectorio);
        currentFolder = rutaDirectorio;
        listaNombresArchivos = new ArrayList();
        listaRutasArchivos = new ArrayList();
        File directorioActual = new File(rutaDirectorio);
        if(!directorioActual.exists()){
            return;
        }
        listaArchivos = directorioActual.listFiles();

        int x = 0;

        if (listaArchivos == null) {
            Toast.makeText(ArchiveExplorerGroup.this, "No se puede acceder",Toast.LENGTH_LONG).show(); return;
        }

        // Si no es nuestro directorio raiz creamos un elemento que nos
        // permita volver al directorio padre del directorio actual
        if (!rutaDirectorio.equals(directorioRaiz)) {
            listaNombresArchivos.add("../");
            listaRutasArchivos.add(directorioActual.getParent());
            x = 1;
        }

        // Almacenamos las rutas de todos los archivos y carpetas del directorio
        for (File archivo : listaArchivos) {
            listaRutasArchivos.add(archivo.getPath());
        }

        // Ordenamos la lista de archivos para que se muestren en orden alfabetico
        Collections.sort(listaRutasArchivos, String.CASE_INSENSITIVE_ORDER);


        // Recorremos la lista de archivos ordenada para crear la lista de los nombres
        // de los archivos que mostraremos en el listView
        for (int i = x; i < listaRutasArchivos.size(); i++){
            File archivo = new File((String)listaRutasArchivos.get(i));
            if (archivo.isFile()) {
                listaNombresArchivos.add(archivo.getName());
            } else {
                listaNombresArchivos.add("/" + archivo.getName());
            }
        }

        // Si no hay ningun archivo en el directorio lo indicamos
        if (listaArchivos.length < 1) {
            listaNombresArchivos.add("No hay ningun archivo");
            listaRutasArchivos.add(rutaDirectorio);
        }


        // Creamos el adaptador y le asignamos la lista de los nombres de los
        // archivos y el layout para los elementos de la lista
        adaptador = new AEArrayAdapter(this, android.R.layout.simple_list_item_1, listaNombresArchivos);
        listaItems.setAdapter(adaptador);
    }
    //añadir el archivo seleccionado a la BBDD
    private boolean addfilegroupBBDD(String nameGrouptmp, String namefileadd, String owner) {
        String filesnew = filesstring + ", " + namefileadd;
        String ownersnew = ownersstring+ ", " + owner;

        boolean inserted = helperGroup.addFileGroup(nameGrouptmp, filesnew, ownersnew,helperGroup.GROUPS_TABLE_NAME);
        if (inserted)
            return inserted;
        else
            return false;
    }

    @Override
    public void onBackPressed(){
        if (!currentFolder.equalsIgnoreCase(directorioRaiz)) {
            verArchivosDirectorio(new File(currentFolder).getParent());
        }
        else
            super.onBackPressed();
    }
}