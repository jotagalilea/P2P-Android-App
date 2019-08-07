package com.example.samue.login;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
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

import me.kevingleason.pnwebrtc.PnPeer;
import me.kevingleason.pnwebrtc.PnRTCClient;
import me.kevingleason.pnwebrtc.PnRTCListener;
import util.Constants;



public class Profile extends AppCompatActivity {
	Dialog mdialog;
	FloatingActionButton fab;
	EditText name;
	Button bf;
	ListView friends_list;
	FriendsAdapter adapter;
	ArrayList<Friends> al_friends;
	ArrayList<Friends> al_blocked_users;
	static DatabaseHelper mDatabaseHelper;
	static final int BLOCKED_USERS_REQUEST = 4;
	static final int SEE_SHARED_FOLDERS_REQUEST = 5;
	//Nombre de las carpetas, lista de archivos de cada una.
	private HashMap<String,ArrayList<String>> sharedFolders;
	// ¡¡OJO!! antes era: Nombre de los amigos, lista de carpetas a las que tiene acceso cada uno.
	// Es mejor que sea:  Nombre de las carpetas, lista de amigos que tienen acceso a cada una.
	private HashMap<String,ArrayList<String>> foldersAccess;
	ArchivesDatabase mArchivesDatabase;
	private String userRecursos;

	public static final String LOCAL_MEDIA_STREAM_ID = "localStreamPN";
	static PnRTCClient pnRTCClient;
	private Pubnub mPubNub;
	public String username;
	private FileSender fileSender;
	boolean sendingFile;
	ProgressDialog pd;
	private DownloadService downloadService;
	private Intent dl_intent;
	private boolean serviceBound = false;
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
		this.username = getIntent().getExtras().getString("user");
		al_blocked_users = new ArrayList<>();
		mDatabaseHelper = new DatabaseHelper(this);
		loadBlockedUsersList();
		loadSharedFolders();
		loadFoldersAccess();
		//blockedUsersHelper = new BlockedUsersHelper(this);
		mArchivesDatabase = new ArchivesDatabase(this);
		friends_list = (ListView) findViewById(R.id.friends_list);

		populateListView();

		friends_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				final String connectTo = al_friends.get(position).getNombre();
				mdialog = new Dialog(Profile.this);
				mdialog.setContentView(R.layout.dialog_confirmsharedarchive);
				mdialog.show();

				TextView tv = (TextView) mdialog.findViewById(R.id.confirm_archive_tv);
				tv.setText("¿Qué quieres hacer?");

				Button yes = (Button) mdialog.findViewById(R.id.confirm_archive_yes);
				yes.setText("Borrar");
				Button no = (Button) mdialog.findViewById(R.id.confirm_archive_no);
				no.setText("Ver archivos");

				no.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						mdialog.dismiss();
						userRecursos = connectTo;
						publish(connectTo, "VAR");
					}
				});

				yes.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						mdialog.dismiss();
						mDatabaseHelper.removeData(connectTo, mDatabaseHelper.FRIENDS_TABLE_NAME);
						populateListView();
						Toast.makeText(getApplicationContext(), "Friend "+ connectTo + " removed", Toast.LENGTH_LONG).show();
					}
				});
			}
		});

		Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
		setSupportActionBar(myToolbar);
		getSupportActionBar().setTitle("Bienvenido/a, " + getIntent().getExtras().getString("user"));

		comprobarPermisos();

		fab = (FloatingActionButton) findViewById(R.id.addFriendsFAB);

		fab.setOnClickListener(new View.OnClickListener() { //TODO debe subir al fichero interno el path del archivo que elije
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Profile.this, ArchiveExplorer.class);
				intent.putExtra("friendsList", al_friends);
				startActivityForResult(intent, 1);
			}
		});
		initPubNub();

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

					if(connectionType.equals("VAR")){ //buscamos que tipo de mensaje debemos enviar
						VAR(connectTo);
					}else if(connectionType.equals("FR")){
						FR(connectTo);
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
			case BLOCKED_USERS_REQUEST: //Caso para cuando se vuelve de ver los usuarios bloqueados. Hay que recargar la lista.
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
							Toast.makeText(Profile.this, "now you can share archives :)", Toast.LENGTH_SHORT).show();
						}
					});
				}else{
					Profile.this.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							Toast.makeText(Profile.this, "cannot access to archives", Toast.LENGTH_SHORT).show();
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
				// User chose the "Settings" item, show the app settings UI...
				//Toast.makeText(getBaseContext(), "Settings clicked", Toast.LENGTH_LONG).show();
				final ArrayList<String> al = getArchivesList();
				Intent intent = new Intent(Profile.this, Recursos.class);
				intent.putExtra("lista", al);
				intent.putExtra("listener", false);
				startActivityForResult(intent, 3);
				return true;

			case R.id.action_add_friend:
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
							title.setText("El usuario está bloqueado y se desbloqueará si continúas.\n¿Continuar?");

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
			msg.put("type", "RA");
			msg.put("sendTo", this.username);
			msg.put(Utils.NAME, name);
			msg.put(Utils.REQ_PREVIEW, isPreview);

			this.pnRTCClient.transmit(sendTo, msg);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	private void handleRA(JSONObject jsonMsg){
		try{
			boolean cancel_dl;
			try{
				cancel_dl = jsonMsg.getBoolean(Utils.CANCEL_DL);
			} catch (JSONException e){
				cancel_dl = false;
			}

			if (!cancel_dl){
				final String userFR = jsonMsg.getString("sendTo");
				// Si el usuario está bloqueado se desecha la petición silenciosamente.
				if (!listContains(userFR, al_blocked_users)) {
					String archive = jsonMsg.getString(Utils.NAME);
					String sendTo = jsonMsg.getString("sendTo");
					Cursor c = this.mArchivesDatabase.getData(archive);

					c.moveToNext();
					String path = c.getString(1);
					c.close();

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
						// La cantidad de datos que se van a enviar dependerá del tipo de archivo:
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


					//TODO: Revisar:
					/*
					 * Antes de comenzar el bucle habría que mandar al amigo el mensaje de nueva descarga
					 * con los datos necesarios y hasta que no reciba respuesta no entra en el while.
					 * Si este dispositivo ya está enviando un archivo (hilo de envío ocupado) entonces hay que
					 * decirle al amigo que no puede descargar el archivo de aquí. Entonces el amigo debe intentar
					 * otra descarga que tenga en espera.
					 */
					//pnRTCClient.transmit(sendTo, msg);

					// Voy a enviar 8 KB de datos en cada mensaje, codificado aumentará.
					//TODO: Lo que sigue debería estar a la espera de que el amigo dé la señal en un hilo nuevo.

					if (!sendingFile) {
						sendingFile = true;
						fileSender = new FileSender();
						fileSender.setName("fileSender");
						fileSender.setVariables(previewSize, msg, sendTo, file, fis, isPreview);
						fileSender.start();
					}
					else{
						//TODO: Falta que al recibir nueva petición de descarga se compruebe que no se esté mandando ya un fichero
						//      en cuyo caso debería quedarse en una cola de espera. Estaría bien un mecanismo de seguridad
						//      que impida el envío contínuo de ficheros.
					}
				}
				else{
					fileSender.interrupt();
					sendingFile = false;
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}




	private File prepareCutDocument(String path, String extension){
		File f;
		final String preview = "_preview";
		switch (extension){
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
	 * Crea una imagen de calidad reducida para la previsualización.
	 * @param path Ruta de la imagen.
	 * @param ext Extensión de la imagen.
	 * @return Imagen de menor calidad.
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


	private void handleSA(JSONObject jsonMsg){
		this.downloadService.handleMsg(jsonMsg);
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
			// Si el usuario está bloqueado se desecha la petición silenciosamente.
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
	 * Hilo para el envío de 1 archivo.
	 */
	private class FileSender extends Thread {
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
			byte[] bFile = new byte[8192];
			int bytesRead;
			int totalBytesRead = 0;
			String s;
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
			} catch (Exception e){
				e.printStackTrace();
			}
		}


		public void setVariables(long p, JSONObject j, String s, File f, FileInputStream fiss, boolean prev){
			previewLength = p;
			msg = j;
			sendTo2 = s;
			file2 = f;
			fis = fiss;
			isPreview = prev;
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
				}

			} catch (JSONException e){
				e.printStackTrace();
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

	// Este cargaba como clave en el HashMap el nombre del amigo.
	/*private void loadFoldersAccess(){
		Cursor c = mDatabaseHelper.getData(DatabaseHelper.FOLDER_ACCESS_TABLE);
		foldersAccess.clear();
		String lastFriend = null;
		ArrayList<String> al_folders = null;

		while (c.moveToNext()){
			String friend = c.getString(0);
			String folder = c.getString(1);
			//Si el usuario es distinto al anterior o es el primero se crea una nueva entrada:
			if (!friend.equalsIgnoreCase(lastFriend)) {
				al_folders = new ArrayList<>(4);
				foldersAccess.put(friend, al_folders);
			}
			al_folders.add(folder);
			lastFriend = friend;
		}
	}*/

	@Override
	protected void onDestroy(){
		downloadService.stop();
		super.onDestroy();
	}

}
