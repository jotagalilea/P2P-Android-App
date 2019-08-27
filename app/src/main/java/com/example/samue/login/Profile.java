package com.example.samue.login;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.ConnectivityManager;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubException;
import com.tom_roush.pdfbox.multipdf.Splitter;
import com.tom_roush.pdfbox.pdmodel.PDDocument;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.VideoRendererGui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import me.kevingleason.pnwebrtc.PnPeer;
import me.kevingleason.pnwebrtc.PnRTCClient;
import me.kevingleason.pnwebrtc.PnRTCListener;
import util.Constants;



public class Profile extends AppCompatActivity {
	Dialog mdialog;
	Dialog mdialogGroup;
	FloatingActionButton fab;
	FloatingActionButton groups;
	EditText name;
	EditText nameGroup;
	Button bf;
	ListView friends_list;
	FriendsAdapter adapter;
	ArrayList<Friends> al_friends;
	ArrayList<Friends> al_blocked_users;
	private String selectedFolder = null;
	static DatabaseHelper mDatabaseHelper;
	static final int BLOCKED_USERS_REQUEST = 4;
	static final int SEE_SHARED_FOLDERS_REQUEST = 5;
	// Nombre de las carpetas, lista de archivos de cada una.
	private HashMap<String,ArrayList<String>> sharedFolders;
	// Nombre de las carpetas, lista de amigos que tienen acceso a cada una.
	private HashMap<String,ArrayList<String>> foldersAccess;
	ArchivesDatabase mArchivesDatabase;
	private String userRecursos;

	public static final String LOCAL_MEDIA_STREAM_ID = "localStreamPN";
	static PnRTCClient pnRTCClient;
	private Pubnub mPubNub;
	public String username;
	private FileSender activeFileSender;
	private SendersManager sendersManager;
	boolean sendingFile;
	ProgressDialog pd;
	private DownloadService downloadService;
	private Intent dl_intent;
	private boolean serviceBound = false;
	private boolean mobileDataBlocked;
	private ServiceConnection serviceConnection = new ServiceConnection(){
		@Override
		public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
			DownloadService.DownloadBinder binder = (DownloadService.DownloadBinder) iBinder;
			downloadService = binder.getService();
			serviceBound = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			serviceBound = false;
			Log.e("ERROR EN DESCARGA", "SERVICIO DESCONECTADO INESPERADAMENTE");
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profile);
		sendingFile = false;
		mobileDataBlocked = false;
		this.username = getIntent().getExtras().getString("user");
		al_blocked_users = new ArrayList<>();
		mDatabaseHelper = new DatabaseHelper(this);
		loadBlockedUsersList();
		loadSharedFolders();
		loadFoldersAccess();
		mArchivesDatabase = new ArchivesDatabase(this);
		friends_list = (ListView) findViewById(R.id.friends_list);
		sendersManager = SendersManager.getSingleton();

		populateListView();

		friends_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				final String connectTo = al_friends.get(position).getNombre();
				mdialog = new Dialog(Profile.this);
				mdialog.setContentView(R.layout.dialog_friend_options);
				mdialog.show();

				Button deleteButton = mdialog.findViewById(R.id.deleteButton);
				Button seeFilesButton = mdialog.findViewById(R.id.seefriendfilesButton);
				Button seeFriendSFButton = mdialog.findViewById(R.id.seefriendSFButton);

				// Ver archivos compartidos por el amigo seleccionado.
				seeFilesButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						mdialog.dismiss();
						userRecursos = connectTo;
						publish(connectTo, "VAR");
					}
				});

				// Borrar amigo.
				deleteButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						mdialog.dismiss();
						mDatabaseHelper.removeData(connectTo, mDatabaseHelper.FRIENDS_TABLE_NAME);
						populateListView();
						Toast.makeText(getApplicationContext(), "Amigo "+ connectTo + " eliminado", Toast.LENGTH_LONG).show();
					}
				});

				// Ver carpetas compartidas con este dispositivo por el amigo seleccionado.
				seeFriendSFButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						mdialog.dismiss();
						userRecursos = connectTo;
						publish(connectTo, "VSF"); //View Shared Folders
					}
				});
			}
		});

		Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
		setSupportActionBar(myToolbar);
		getSupportActionBar().setTitle("Hola, " + getIntent().getExtras().getString("user"));

		comprobarPermisos();

		// Botón para ir a la activity de grupos.
		groups = (FloatingActionButton) findViewById(R.id.groupsButton);
		groups.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Profile.this, listGroupsActivity.class);
				//intent.putExtra("friendsList", al_friends);
				startActivityForResult(intent, 1);
			}
		});

		fab = (FloatingActionButton) findViewById(R.id.addFriendsFAB);

		// Botón para compartir un archivo o una carpeta.
		fab.setOnClickListener(new View.OnClickListener() { //TODO debe subir al fichero interno el path del archivo que elije
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Profile.this, ArchiveExplorer.class);
				intent.putExtra("friendsList", al_friends);
				startActivityForResult(intent, 1);
			}
		});
		initPubNub();

		// Arranque del servicio de descargas.
		dl_intent = new Intent(this, DownloadService.class);
		startService(dl_intent);
		serviceBound = bindService(dl_intent, serviceConnection, BIND_AUTO_CREATE);
	}



	private void publish(final String connectTo, final String connectionType){
		String userCall = connectTo + Constants.STDBY_SUFFIX;
		JSONObject jsonCall = new JSONObject();
		try {
			jsonCall.put(Constants.JSON_CALL_USER, username);
			mPubNub.publish(userCall, jsonCall, new Callback() {
				@Override
				public void successCallback(String channel, Object message) { //conectamos nosotros al otro
					Log.d("MA-dCall", "SUCCESS: " + message.toString());
					connectPeer(connectTo, true); //conectamos con el peer
					/* Parada necesaria para asegurar que se realiza bien la conexión, ya sea para esperar
					 * el tráfico en caso de estar la red congestionada o bien para esperar la respuesta desde
					 * un dispositivo lento.
					 */
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {}

					if(connectionType.equals("VAR")){ //buscamos que tipo de mensaje debemos enviar
						VAR(connectTo);
					}else if(connectionType.equals("FR")){
						FR(connectTo);
					}else if(connectionType.equals("VSF")){
						VSF(connectTo);
					}
				}
			});
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void comprobarPermisos(){
		if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
			ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
		}
	}

	public void initPubNub(){
		String stdbyChannel = this.username + Constants.STDBY_SUFFIX;
		this.mPubNub = new Pubnub(Constants.PUB_KEY, Constants.SUB_KEY);
		this.mPubNub.setUUID(this.username);
		try {
			this.mPubNub.subscribe(stdbyChannel, new Callback(){ //creamos nuestro canal y nos quedamos en stand-by esperando alguna conexión
				@Override
				public void successCallback(String channel, Object message) { //despierta cuando alguien se conecta a nuestro canal y responde con ACK
					Log.v("MA-success", "MESSAGE: " + message.toString());
					if (!(message instanceof JSONObject)) return; // Ignore if not JSONObject
					JSONObject jsonMsg = (JSONObject) message;
					try {
						if (!jsonMsg.has(Constants.JSON_CALL_USER)) return;
						connectPeer("", false);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		} catch (PubnubException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch(requestCode){
			case 1:
				if(resultCode == Activity.RESULT_OK){
					// Si se ha compartido una carpeta hay que volver a cargar las carpetas y lista de acceso de la BD...
					boolean isFolderSharing = data.getBooleanExtra("folder_sharing", false);
					if (isFolderSharing){
						loadSharedFolders();
						loadFoldersAccess();
					}

					// Si no se ha compartido una carpeta entonces se ha compartido un solo archivo:
					else {
						String name = data.getStringExtra("name");
						String path = data.getStringExtra("path");

						if (!this.mArchivesDatabase.exists(name)) {
							this.mArchivesDatabase.addData(name, path);
							notificate("Se ha compartido el archivo");
						} else {
							notificate("El archivo ya estaba compartido");
						}
					}
				}
				break;
			case 2:
				if(resultCode == Activity.RESULT_OK){
					final String name = data.getStringExtra("name");
					String sendTo = data.getStringExtra("sendTo");
					boolean isPreview = data.getBooleanExtra(Utils.REQ_PREVIEW, false);
					RA(name, sendTo, isPreview);
				}
				break;
			case 3:
				if(resultCode == Activity.RESULT_OK){
					final String name = data.getStringExtra("name");
					if(mArchivesDatabase.removeData(name)){
						Toast.makeText(getApplicationContext(), "Archivo "+ name + " borrado", Toast.LENGTH_LONG).show();
					}
				}
				break;
			case BLOCKED_USERS_REQUEST: // Caso para cuando se vuelve de ver los usuarios bloqueados. Hay que recargar la lista.
				if(resultCode == Activity.RESULT_OK){
					al_blocked_users.clear();
					al_blocked_users = (ArrayList<Friends>) data.getSerializableExtra("arrayBloqueados");
					// Si se ha bloqueado a un amigo hay que recargar el arrayList y el adapter.
					populateListView();
				}
				break;
			case SEE_SHARED_FOLDERS_REQUEST:
				loadSharedFolders();
				loadFoldersAccess();
				break;
			default:
				if(!userRecursos.equals("")){
					cerrarConexion(userRecursos);
					userRecursos = "";
				}
				break;
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		switch(requestCode){
			case 1: {
				if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
					Profile.this.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							Toast.makeText(Profile.this, "Ahora puedes compartir archivos :)", Toast.LENGTH_SHORT).show();
						}
					});
				}else{
					Profile.this.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							Toast.makeText(Profile.this, "No se puede acceder a los archivos", Toast.LENGTH_SHORT).show();
						}
					});
				}
				return;
			}
		}
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.my_toolbar, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.block_upload_mobile:
				mobileDataBlocked = !mobileDataBlocked;
				if (mobileDataBlocked)
					Toast.makeText(getApplicationContext(), "Ya no se comparten archivos con datos móviles", Toast.LENGTH_LONG).show();
				else
					Toast.makeText(getApplicationContext(), "Ahora se comparten archivos con datos móviles", Toast.LENGTH_LONG).show();
				return true;

			case R.id.see_shared_folders:
				Intent sfIntent = new Intent(this, SharedFoldersActivity.class);
				sfIntent.putExtra("friends", al_friends);
				sfIntent.putExtra("sharedFolders", sharedFolders);
				sfIntent.putExtra("foldersAccess", foldersAccess);
				startActivityForResult(sfIntent, SEE_SHARED_FOLDERS_REQUEST);
				return true;

			case R.id.see_downloads:
				Intent dmIntent = new Intent(this, DownloadManagerActivity.class);
				dmIntent.putExtra("downloadServiceIntent", this.dl_intent);
				startActivity(dmIntent);
				return true;

			case R.id.see_blocked_users:
				Intent BUintent = new Intent(this, BlockedUsersActivity.class);
				BUintent.putExtra("amigos", al_friends);
				startActivityForResult(BUintent, BLOCKED_USERS_REQUEST);
				return true;

			case R.id.see_shared_archives:
				final ArrayList<String> al = getArchivesList();
				Intent intent = new Intent(Profile.this, Recursos.class);
				intent.putExtra("lista", al);
				intent.putExtra("listener", false);
				startActivityForResult(intent, 3);
				return true;

			case R.id.add_friend:
				mdialog = new Dialog(Profile.this);
				mdialog.setContentView(R.layout.dialog_newfriend);
				mdialog.show();
				name = mdialog.findViewById(R.id.name);
				bf = mdialog.findViewById(R.id.button_friend);

				bf.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						final String fr = name.getText().toString();

						/* Si el nuevo amigo estaba bloqueado se elimina el bloqueo antes de enviar
						 * la petición de amistad.
						 */
						if (listContains(fr, al_blocked_users)){
							final Dialog removeBlockedDialog = new Dialog(Profile.this);
							removeBlockedDialog.setContentView(R.layout.dialog_remove_blocked_friend);
							removeBlockedDialog.show();

							TextView title = removeBlockedDialog.findViewById(R.id.previous_blocked_friend_title);
							title.setText(fr + " está bloqueado y se desbloqueará si continúas.\n¿Continuar?");

							Button yes = removeBlockedDialog.findViewById(R.id.unlock_yes);
							yes.setOnClickListener(new View.OnClickListener() {
								@Override
								public void onClick(View view) {
									mDatabaseHelper.removeData(fr, mDatabaseHelper.BLOCKED_TABLE_NAME);
									loadBlockedUsersList();
									publish(fr, "FR");
									Toast.makeText(getApplicationContext(), "Petición de amistad enviada", Toast.LENGTH_SHORT).show();
									removeBlockedDialog.dismiss();
								}
							});
							Button no = removeBlockedDialog.findViewById(R.id.unlock_no);
							no.setOnClickListener(new View.OnClickListener() {
								@Override
								public void onClick(View view) {
									removeBlockedDialog.dismiss();
								}
							});
						}
						else if(!listContains(fr, al_friends)) {
							publish(fr, "FR");
							Toast.makeText(getApplicationContext(), "Petición de amistad enviada", Toast.LENGTH_SHORT).show();
						}else{
							Toast.makeText(getApplicationContext(), "Ya eres amigo de " + fr, Toast.LENGTH_SHORT).show();
						}
						mdialog.dismiss();
					}
				});
				return true;
			case R.id.add_group:
                //llevar a la nueva actividad de crear grupo
                mdialogGroup = new Dialog(Profile.this);
                mdialogGroup.setContentView(R.layout.dialog_newgroup);
                mdialogGroup.show();
                nameGroup = (EditText) mdialogGroup.findViewById(R.id.nameGroup);
                bf = (Button) mdialogGroup.findViewById(R.id.button_addFriends);

                bf.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        mdialogGroup.dismiss();
                        Intent myIntent = new Intent(Profile.this, friendsgroup.class);
                        myIntent.putExtra("nameGroup", nameGroup.getText().toString());
                        startActivityForResult(myIntent, 3);
                    }

                });
                return true;
			default:
				// If we got here, the user's action was not recognized.
				// Invoke the superclass to handle it.
				return super.onOptionsItemSelected(item);

		}
	}


	private boolean listContains(String nombre, ArrayList<Friends> al){
		for(Friends f : al){
			if(f.getNombre().equals(nombre)){
				return true;
			}
		}
		return false;
	}

	public void addData(String newEntry){ //llamar cuando aceptemos la peticion de amistad y cuando nos la acepten
		boolean insertData = mDatabaseHelper.addData(newEntry, mDatabaseHelper.FRIENDS_TABLE_NAME);
		if(insertData){
			populateListView();
		}
	}

	private void populateListView(){
		Cursor data = mDatabaseHelper.getData(DatabaseHelper.FRIENDS_TABLE_NAME);
		al_friends = new ArrayList<>();
		while(data.moveToNext()){
			al_friends.add(new Friends(data.getString(1), R.drawable.ic_launcher_foreground));
		}
		adapter = new FriendsAdapter(this, al_friends);
		friends_list.setAdapter(adapter);
	}

	private ArrayList<String> getArchivesList(){
		ArrayList<String> al = new ArrayList<String>();
		Cursor data = mArchivesDatabase.getData();

		while(data.moveToNext()){
			al.add(data.getString(1));
		}
		return al;
	}

	private void connectPeer(String connectTo, boolean call){
		if(this.pnRTCClient == null) {
			PeerConnectionFactory.initializeAndroidGlobals(
					getApplicationContext(),  // Context
					true,  // Audio Enabled
					true,  // Video Enabled
					true,  // Hardware Acceleration Enabled
					VideoRendererGui.getEGLContext()); // Render EGL Context

			PeerConnectionFactory pcFactory = new PeerConnectionFactory();
			this.pnRTCClient = new PnRTCClient(Constants.PUB_KEY, Constants.SUB_KEY, this.username);

			MediaStream mediaStream = pcFactory.createLocalMediaStream(LOCAL_MEDIA_STREAM_ID);

			this.pnRTCClient.attachRTCListener(new myRTCListener());
			this.pnRTCClient.attachLocalMediaStream(mediaStream);

			this.pnRTCClient.listenOn(this.username);
		}

		if(call){
			this.pnRTCClient.connect(connectTo);
		}
	}

	private void notificate(final String notification){
		Profile.this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(getApplicationContext(), notification, Toast.LENGTH_SHORT).show();
			}
		});
	}

	private void cerrarConexion(String userTo){
		this.pnRTCClient.closeConnection(userTo);
	}

	private void RA(String name, String sendTo, boolean isPreview){ //Request Archive
		try{
			JSONObject msg = new JSONObject();
			final String finalName = name;
			msg.put("type", "RA");
			msg.put("sendTo", this.username);
			msg.put(Utils.NAME, name);
			msg.put(Utils.REQ_PREVIEW, isPreview);
			// Útil para la descarga desde una carpeta compartida:
			if (selectedFolder != null){
				msg.put("selectedFolder", selectedFolder);
				selectedFolder = null;
			}

			/*
			 * Si hay hilos de descarga disponibles se lanza.
			 * si no, se añade a la cola y ya conectaré con el emisor.
			 */
			if (downloadService.hasFreeThreads()) {
				Profile.this.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(getApplicationContext(), "Descargando " + finalName, Toast.LENGTH_LONG).show();
					}
				});
				pnRTCClient.transmit(sendTo, msg);
			}
			else{
				downloadService.queueMsg(sendTo, msg);
				Profile.this.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(getApplicationContext(), finalName + " puesto en cola", Toast.LENGTH_LONG).show();
					}
				});
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}



	private void handleSA(JSONObject jsonMsg){
		this.downloadService.handleMsg(jsonMsg);
	}



	private void handleRA(JSONObject jsonMsg){
		try{
			// Primero se comprueba si se está conectado a Internet con datos móviles:
			final ConnectivityManager connMgr = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
			boolean isUsingMobile = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnectedOrConnecting();
			// En caso de que sí y se haya seleccionado que no se usen en la barra superior, se descarta la petición.
			if (!isUsingMobile || !mobileDataBlocked) {
				boolean cancel_dl;
				try {
					cancel_dl = jsonMsg.getBoolean(Utils.CANCEL_DL);
				} catch (JSONException e) {
					cancel_dl = false;
				}
				if (!cancel_dl) {
					final String user = jsonMsg.getString("sendTo");
					// Si el usuario no está bloqueado se procede, en otro caso se desecha la petición silenciosamente.
					if (!listContains(user, al_blocked_users)) {
						String archive = jsonMsg.getString(Utils.NAME);
						String sendTo = jsonMsg.getString("sendTo");
						String folder;
						try {
							folder = jsonMsg.getString("selectedFolder");
						} catch (JSONException e) {
							folder = null;
						}

						String path;
						if (folder != null) {
							path = folder + '/' + archive;
						} else {
							Cursor c = this.mArchivesDatabase.getData(archive);
							c.moveToNext();
							path = c.getString(1);
							c.close();
						}


						JSONObject msg = new JSONObject();
						msg.put(Utils.FRIEND_NAME, this.username);
						msg.put("type", "SA");
						msg.put(Utils.NAME, archive);

						File file;
						final FileInputStream fis;
						long previewSize = 0;
						final boolean isPreview;
						isPreview = jsonMsg.getBoolean(Utils.REQ_PREVIEW);
						if (isPreview) {
							// La cantidad de datos que se van a enviar para una previsualización dependerá del tipo de archivo:
							String extension = archive.substring(archive.lastIndexOf('.') + 1).toLowerCase();
							file = prepareCutDocument(path, extension);
							previewSize = setPreviewSize(extension);
							if (previewSize > 0)
								file = new File(path);
							else
								previewSize = file.length();
							msg.put(Utils.PREVIEW_SENT, true);
						} else {
							file = new File(path);
							msg.put(Utils.PREVIEW_SENT, false);
						}

						fis = new FileInputStream(file);
						int fileLength = (int) file.length();

						msg.put(Utils.FILE_LENGTH, fileLength);
						msg.put(Utils.NEW_DL, true);


						// Si no se está enviando ningún archivo y no hay ningún hilo en cola se lanza el hilo de subida.
						if (!sendingFile && sendersManager.isQueueEmpty()) {
							sendingFile = true;
							activeFileSender = new FileSender();
							activeFileSender.setName("fileSender");
							activeFileSender.setVariables(previewSize, msg, sendTo, file, fis, isPreview);
							activeFileSender.start();
						}
						// Si hay un hilo enviando un fichero y la cola no está llena se pone en cola.
						else if (sendingFile && !sendersManager.queueFull()) {
							FileSender fs = new FileSender();
							fs.setName("fileSenderQueued");
							fs.setVariables(previewSize, msg, sendTo, file, fis, isPreview);
							sendersManager.addSender(archive, fs);
						}
					}
				} else {
					try {
						String uploadFileName = jsonMsg.getString(Utils.NAME);
						// Si la subida cancelada es la activa se para.
						if (uploadFileName.equals(activeFileSender.getFileName()))
							activeFileSender.stopUpload();
							// Si está en cola se elimina.
						else
							sendersManager.removeSender(uploadFileName);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}


	/**
	 * Prepara el fichero que se va a enviar para ser previsualizado en el destino.
	 * @param path Ruta del fichero.
	 * @param extension Extensión o tipo.
	 * @return Fichero de tamaño reducido.
	 */
	private File prepareCutDocument(String path, String extension){
		File f;
		final String preview = "_preview";
		switch (extension){
			// Si es un pdf se crea uno nuevo con 3 páginas.
			case "pdf":
				try {
					f = new File(path);
					PDDocument pdf = PDDocument.load(f);
					Splitter splitter = new Splitter();
					List<PDDocument> pages = splitter.split(pdf);
					Iterator<PDDocument> it = pages.listIterator();

					// Se meten 3 páginas.
					PDDocument pd = new PDDocument();
					PDDocument aux;
					byte i = 0;
					while (it.hasNext() && i<3){
						aux = it.next();
						pd.addPage(aux.getPage(0));
						aux.close();
						++i;
					}

					f = new File(path+preview);
					pd.save(f);
					pd.close();
				}catch (IOException e){
					e.printStackTrace();
					f = new File(path);
				}
				break;
			// Si es una imagen de uno de los tipos soportados se crea otra de calidad reducida.
			case "jpg":
			case "jpeg":
			case "png":
				f = createThumbnail(path, extension);
				break;
			//TODO
			/*case "mp4": break;
			case "avi": break;*/
			// Si no es ninguno de estos tipos de archivo entonces se pone la ruta normal.
			default: f = new File(path); break;
		}
		return f;
	}


	/**
	 * Crea una imagen de calidad reducida para la previsualización haciendo uso de las utilidades
	 * de android para crear miniaturas.
	 * @param path Ruta de la imagen.
	 * @param ext Extensión de la imagen.
	 * @return Archivo de imagen de menor calidad.
	 */
	private File createThumbnail(String path, String ext){
		int width, height;
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, options);
		width = options.outWidth;
		height = options.outHeight;
		Bitmap bmp = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(path), width, height);
		File f = new File(path+"_preview");
		try{
			FileOutputStream fos = new FileOutputStream(f);
			if (ext.equalsIgnoreCase("png"))
				bmp.compress(Bitmap.CompressFormat.PNG, 40, fos);
			else if (ext.equalsIgnoreCase("jpg") || ext.equalsIgnoreCase("jpeg"))
				bmp.compress(Bitmap.CompressFormat.JPEG, 40, fos);
			fos.close();
		} catch (Exception e){
			e.printStackTrace();
			f.delete();
		}
		return f;
	}


	/**
	 * Cuando el usuario remoto quiere previsualizar un archivo hay que enviarle cierta cantidad de
	 * datos, pero depende del tipo de archivo. Esté método determina la cantidad de datos que se
	 * van a enviar en función del tipo del archivo.
	 * @param ext Extensión del archivo.
	 * @return Tamaño máximo de datos para el envío.
	 */
	private int setPreviewSize(String ext){
		int maxSize;
		switch (ext){
			case "txt": maxSize = 1024; break;
			case "mp3": maxSize = 1024*1024; break;
			case "doc": maxSize = 10*1024; break;
			case "docx": maxSize = 10*1024; break;
			case "ppt": maxSize = 100*1024; break;
			case "html": maxSize = 1024; break;
			case "css": maxSize = 1024; break;
			case "xls": maxSize = 10*1024; break;
			case "xlsx": maxSize = 10*1024; break;
			case "csv": maxSize = 1024; break;
			//case "mp4": maxSize = 1024*1024*10; break;
			//case "avi": maxSize = 1024*1024*10; break;
			default: maxSize = 0;
		}
		return maxSize;
	}



	private void FR(String sendTo){ //Friend Request
		try{
			JSONObject msg = new JSONObject();
			msg.put("type", "FR");
			msg.put("sendTo", this.username);

			this.pnRTCClient.transmit(sendTo, msg);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	private void handleFR(JSONObject jsonMsg){
		try{
			final String userFR = jsonMsg.getString("sendTo");
			// Si el usuario está bloqueado se desecha la petición silenciosamente.
			if (!listContains(userFR, al_blocked_users)){
				mdialog = new Dialog(Profile.this);
				mdialog.setContentView(R.layout.dialog_acceptfriend);
				mdialog.show();
				TextView f_name = (TextView) mdialog.findViewById(R.id.accept_friend_tv);
				f_name.setText("¿Quieres aceptar a " + userFR + " como amigo?");

				Button yes = mdialog.findViewById(R.id.accept_friend_yes);
				Button no = mdialog.findViewById(R.id.accept_friend_no);

				no.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						mdialog.dismiss();
						cerrarConexion(userFR);
					}
				});

				yes.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Profile.this.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								addData(userFR);
							}
						});
						FA(userFR);
						mdialog.dismiss();
					}
				});
			}

		}catch(Exception e){
			e.printStackTrace();
		}
	}

	private void FA(String sendTo){
		try{
			JSONObject msg = new JSONObject();
			msg.put("type", "FA");
			msg.put("addme", this.username);

			this.pnRTCClient.transmit(sendTo, msg);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	private void handleFA(JSONObject jsonMsg){
		try{
			String addme = jsonMsg.getString("addme");
			addData(addme);

			cerrarConexion(addme);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	private void VAR(String sendTo){ //envia peticion para ver archivos
		try{
			JSONObject msg = new JSONObject();
			msg.put("type", "VAR"); //tipo de mensaje
			msg.put("sendTo", this.username); //usuario para devolver mensaje con datos
			this.pnRTCClient.transmit(sendTo, msg);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	/**
	 * Método que envía la petición para ver las carpetas compartidas de un amigo.
	 * @param sendTo
	 */
	private void VSF(String sendTo){
		try{
			JSONObject msg = new JSONObject();
			msg.put("type", "VSF");
			msg.put("sendTo", this.username);
			this.pnRTCClient.transmit(sendTo, msg);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	private void handleVAL(JSONObject jsonMsg){
		try{
			boolean blocked = jsonMsg.getBoolean("blocked");
			if (!blocked) {
				ArrayList<String> al = new ArrayList();

				int size = jsonMsg.getInt("size");

				for (int i = 0; i < size; i++) {
					al.add(jsonMsg.getString("item" + i));
				}

				Intent intent = new Intent(Profile.this, Recursos.class);
				intent.putExtra("lista", al);
				intent.putExtra("listener", true);
				intent.putExtra("sendTo", jsonMsg.getString("sendTo"));
				startActivityForResult(intent, 2); //para volver a esta activity, llamar finish() desde la otra.
			}
			else
				Toast.makeText(this, "No puedes ver los archivos", Toast.LENGTH_LONG).show();

		}catch(Exception e){
			e.printStackTrace();
		}
	}

	private void VAL(JSONObject jsonMsg){
		try{
			final String userFR = jsonMsg.getString("sendTo");
			JSONObject msg = new JSONObject();
			msg.put("type", "VAL");
			msg.put("sendTo", this.username);
			// Si el usuario no está bloqueado...
			if (!listContains(userFR, al_blocked_users)) {
				ArrayList<String> al = getArchivesList();
				msg.put("blocked", false);
				msg.put("size", al.size());
				int i = 0;

				for (String item : al) {
					msg.put("item" + i, item);
					i++;
				}
			}
			else{
				msg.put("blocked", true);
			}
			this.pnRTCClient.transmit(jsonMsg.getString("sendTo"), msg);
		}catch(Exception e){
			e.printStackTrace();
		}
	}



	/**
	 * Maneja la petición de ver carpetas compartidas por parte de un amigo.
	 * @param jsonMsg
	 */
	private void handleVSF(JSONObject jsonMsg){
		try{
			final String userFR = jsonMsg.getString("sendTo");
			JSONObject msg = new JSONObject();
			// Si el usuario no está bloqueado...
			if (!listContains(userFR, al_blocked_users)) {

				// Si el amigo tiene acceso a una o más carpetas, se le envían:
				HashMap<String,ArrayList<String>> sf = getFriendAllowedFolders(userFR);
				if (!sf.isEmpty()) {
					msg = new JSONObject(sf);
					msg.put(Utils.FOLDERSHARING_SPECIAL_CHARS + "type", "SF");
					msg.put(Utils.FOLDERSHARING_SPECIAL_CHARS + "SFallowed", true);
					msg.put(Utils.FOLDERSHARING_SPECIAL_CHARS + "blocked", false);
					msg.put(Utils.FOLDERSHARING_SPECIAL_CHARS + "foldersCount", sf.size());
					msg.put(Utils.FOLDERSHARING_SPECIAL_CHARS + "sendTo", this.username);
				}
				//Si no tiene acceso a ninguna carpeta compartida también se le hace saber:
				else {
					msg.put(Utils.FOLDERSHARING_SPECIAL_CHARS + "type", "SF");
					msg.put(Utils.FOLDERSHARING_SPECIAL_CHARS + "SFallowed", false);
					msg.put(Utils.FOLDERSHARING_SPECIAL_CHARS + "blocked", false);
				}
			}
			else{
				msg.put(Utils.FOLDERSHARING_SPECIAL_CHARS + "blocked", true);
			}
			this.pnRTCClient.transmit(jsonMsg.getString("sendTo"), msg);
		}catch(Exception e){
			e.printStackTrace();
		}
	}


	/**
	 * Maneja la respuesta a la petición de ver carpetas compartidas.
	 */
	private void handleSF(JSONObject json){
		try {
			// Primero se comprueba si estoy bloqueado por si acaso.
			boolean blocked = json.getBoolean(Utils.FOLDERSHARING_SPECIAL_CHARS + "blocked");
			if (!blocked) {
				// Después se comprueba si tengo permitido el acceso a alguna carpeta.
				boolean allowed = json.getBoolean(Utils.FOLDERSHARING_SPECIAL_CHARS + "SFallowed");
				if (allowed) {
					int size = json.getInt(Utils.FOLDERSHARING_SPECIAL_CHARS + "foldersCount");
					final String sendTo = json.getString(Utils.FOLDERSHARING_SPECIAL_CHARS + "sendTo");
					final HashMap<String, ArrayList<String>> map = new HashMap<>(size);

					Iterator<String> keysIt = json.keys();
					while (keysIt.hasNext()) {
						String key = keysIt.next();
						// Si no empieza con los caracteres especiales entonces es la info de una carpeta.
						if (!key.startsWith(Utils.FOLDERSHARING_SPECIAL_CHARS)) {
							JSONArray jsonArray = (JSONArray) json.get(key);
							ArrayList<String> filesList = new ArrayList<>(jsonArray.length());
							for (int i=0; i<jsonArray.length(); i++)
								filesList.add(jsonArray.getString(i));
							map.put(key, filesList);
						}
					}

					// Reutilizo el diálogo para ver el contenido de una carpeta compartida propia.
					Profile.this.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							final Dialog dialog = new Dialog(Profile.this);
							dialog.setContentView(R.layout.dialog_see_files);
							TextView title = dialog.findViewById(R.id.folder_name);
							title.setText("Carpetas disponibles");
							final ArrayList<String> foldersArray = new ArrayList<>(map.keySet());
							AEArrayAdapter adapter = new AEArrayAdapter(Profile.this, android.R.layout.simple_list_item_1, foldersArray);
							ListView folders_list = dialog.findViewById(R.id.files_list);
							folders_list.setAdapter(adapter);
							folders_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
								@Override
								public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
									selectedFolder = foldersArray.get(i);
									Intent intent = new Intent(Profile.this, Recursos.class);
									intent.putExtra("isFS", true);
									intent.putStringArrayListExtra("lista", map.get(selectedFolder));
									intent.putExtra("listener", true);
									intent.putExtra("sendTo", sendTo);
									startActivityForResult(intent, 2);
								}
							});
							dialog.show();
						}
					});
				}
				// Si no tengo permitido el acceso se muestra un mensaje:
				else
					Profile.this.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							Toast.makeText(Profile.this, "No puedes ver las carpetas", Toast.LENGTH_SHORT).show();
						}
					});
			}
		}
		catch (JSONException e){
			e.printStackTrace();
		}
	}


	/**
	 * Devuelve las carpetas a las que tiene acceso el usuario que hace la petición.
	 */
	private HashMap<String,ArrayList<String>> getFriendAllowedFolders(String user){
		HashMap<String,ArrayList<String>> result = new HashMap<>();
		Iterator it = foldersAccess.entrySet().iterator();

		while (it.hasNext()){
			Map.Entry item = (Map.Entry) it.next();
			ArrayList users = (ArrayList<String>) item.getValue();
			if (users.contains(user)){
				String folder = (String) item.getKey();
				result.put(folder, sharedFolders.get(folder));
			}
		}

		return result;
	}



	/**
	 * Hilo para el envío de 1 archivo.
	 */
	public class FileSender extends Thread {
		private long previewLength;
		private JSONObject msg;
		private String sendTo2;
		private File file2;
		private FileInputStream fis;
		private boolean isPreview;
		@Override
		public void run() {
			boolean lastPiece = false;
			boolean firstPiece = true;
			// Voy a enviar 16 KB de datos en cada mensaje, codificado aumentará.
			byte[] bFile = new byte[16384];
			int bytesRead;
			int totalBytesRead = 0;
			String s;
			activeFileSender = this;
			try {
				while (!lastPiece) {
					bytesRead = fis.read(bFile);
					totalBytesRead += bytesRead;
					if (isPreview)
						if (previewLength > 0)
							lastPiece = totalBytesRead >= previewLength;
						else
							lastPiece = (bytesRead < bFile.length);
					else
						lastPiece = (bytesRead < bFile.length);

					msg.put(Utils.LAST_PIECE, lastPiece);
					s = Base64.encodeToString(bFile, Base64.URL_SAFE);
					msg.put(Utils.DATA, s);
					pnRTCClient.transmit(sendTo2, msg);
					msg.remove(Utils.DATA);
					msg.remove(Utils.LAST_PIECE);

					if (firstPiece) {
						msg.remove(Utils.FILE_LENGTH);
						msg.remove(Utils.NEW_DL);
						msg.put(Utils.NEW_DL, false);
						firstPiece = false;
					}
				}
				// Si setPreviewSize devolvió 0 entonces sé que ha sido necesario crear un archivo nuevo, y hay que borrarlo.
				if (file2.getName().contains("_preview"))
					file2.delete();

				fis.close();
				pnRTCClient.closeConnection(sendTo2);
				sendingFile = false;
				// Se avisa al manager de que se ha terminado la subida y puede lanzar la siguiente en la cola, si existe:
				sendersManager.notifyFinishedUpload();
			} catch (Exception e){
				e.printStackTrace();
			}
		}


		/**
		 * Método para inicializar las variables del hilo, es obligatorio llamarlo antes de
		 * arrancar el hilo.
		 * @param p Tamaño del fichero en caso de una previsualización, 0 si no lo es.
		 * @param j Mensaje JSON.
		 * @param s Nombre del destinatario.
		 * @param f Fichero.
		 * @param fiss Canal de transmisión de los datos del fichero para su lectura.
		 * @param prev Determina si es un fichero de previsualización o no.
		 */
		public void setVariables(long p, JSONObject j, String s, File f, FileInputStream fiss, boolean prev){
			previewLength = p;
			msg = j;
			sendTo2 = s;
			file2 = f;
			fis = fiss;
			isPreview = prev;
		}

		/**
		 * Devuelve el nombre del fichero que se está descargando.
		 * @return nombre del fichero.
		 */
		public String getFileName(){
			return file2.getName();
		}

		/**
		 * Detiene el envío actual e interrumpe el hilo.
		 */
		public void stopUpload(){
			try{
				fis.close();
				this.interrupt();
			} catch (IOException e){
				e.printStackTrace();
			}
		}
	}



	private class myRTCListener extends PnRTCListener{
		@Override
		public void onPeerConnectionClosed(PnPeer peer) {
			super.onPeerConnectionClosed(peer);
		}

		@Override
		public void onLocalStream(MediaStream localStream) {
			super.onLocalStream(localStream);
		}

		public void onConnected(String userId){
			Log.d("Md-a", "conectado a: " + userId);
		}

		@Override
		public void onMessage(PnPeer peer, Object message) {
			if (!(message instanceof JSONObject)) return; //Ignore if not JSONObject
			final JSONObject jsonMsg = (JSONObject) message;
			try {
				final String type = jsonMsg.getString("type");
				//TODO el manejo de los mensajes estaría bien hacerlos fuera de perfil, ya que no es su objetivo principal
				if(type.equals("VAR")){
					VAL(jsonMsg);
				}else if(type.equals("VAL")){ //se debe manejar en la hebra principal ya que inicia una nueva actividad
					Profile.this.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							handleVAL(jsonMsg);
						}
					});
				}else if(type.equals("FR")){
					Profile.this.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							handleFR(jsonMsg);
						}
					});
				}else if(type.equals("FA")){
					Profile.this.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							handleFA(jsonMsg);
						}
					});
				}else if(type.equals("RA")){
					handleRA(jsonMsg);
				}else if(type.equals("SA")){
					handleSA(jsonMsg);
				}else if(type.equals("VSF")){
					handleVSF(jsonMsg);
				}

			} catch (JSONException e){
				try{
					String type = jsonMsg.getString(Utils.FOLDERSHARING_SPECIAL_CHARS + "type");
					if (type.equals("SF")){
						handleSF(jsonMsg);
					}
					else e.printStackTrace();
				}
				catch (JSONException e1) {}
			}

		}
	}

	/**
	 * Carga de los usuarios bloqueados almacenados en la BD.
	 */
	private void loadBlockedUsersList() {
		Cursor c = mDatabaseHelper.getData(DatabaseHelper.BLOCKED_TABLE_NAME);
		if (al_blocked_users != null)
			al_blocked_users.clear();
		else
			al_blocked_users = new ArrayList<>();
		while (c.moveToNext()){
			Friends f = new Friends(c.getString(1), R.drawable.ic_launcher_foreground);
			al_blocked_users.add(f);
		}
	}

	/**
	 * Carga de las carpetas compartidas.
	 */
	private void loadSharedFolders(){
		Cursor c = mDatabaseHelper.getData(DatabaseHelper.SHARED_FOLDERS_TABLE);
		if (sharedFolders != null )
			sharedFolders.clear();
		else
			sharedFolders = new HashMap<>();
		while (c.moveToNext()){
			String folder = c.getString(0);
			String files = c.getString(1);
			ArrayList<String> al_files = new ArrayList<>(Arrays.asList(files.split(",")));
			sharedFolders.put(folder, al_files);
		}
	}

	/**
	 * Carga de la lista de acceso de los amigos a las carpetas compartidas.
	 */
	private void loadFoldersAccess(){
		Cursor c = mDatabaseHelper.getData(DatabaseHelper.FOLDER_ACCESS_TABLE);
		if (foldersAccess != null)
			foldersAccess.clear();
		else
			foldersAccess = new HashMap<>();
		String lastFolder = null;
		ArrayList<String> al_friends = null;

		while (c.moveToNext()){
			String folder = c.getString(0);
			int friendID = c.getInt(1);
			//Si la carpeta es distinta a la anterior o es la primera se crea una nueva entrada:
			if (!folder.equalsIgnoreCase(lastFolder)) {
				al_friends = new ArrayList<>(4);
				foldersAccess.put(folder, al_friends);
			}
			String friend = mDatabaseHelper.getUserName(friendID);
			al_friends.add(friend);
			lastFolder = folder;
		}
	}



	@Override
	protected void onDestroy(){
		downloadService.stop();
		super.onDestroy();
	}

}
