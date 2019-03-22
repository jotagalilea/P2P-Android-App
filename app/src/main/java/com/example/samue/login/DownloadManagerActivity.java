package com.example.samue.login;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ListView;

import java.util.ArrayList;


/**
 * Created by jotagalilea on 12/03/2019.
 *
 * Actividad que agrupa las descargas.
 */

public class DownloadManagerActivity extends AppCompatActivity {

	private ArrayList<Download> al_downloads;
	private DownloadService downloadService;
	private boolean serviceBound;
	private ListView dl_listView;
	private DownloadListAdapter listAdapter;
	private ServiceConnection dl_serviceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
			DownloadService.DownloadBinder binder = (DownloadService.DownloadBinder) iBinder;
			downloadService = binder.getService();
			serviceBound = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			Log.e("ERROR EN DESCARGA", "SERVICIO DESCONECTADO INESPERADAMENTE");
			serviceBound = false;
		}
	};



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_download_manager);
		Toolbar toolbar = findViewById(R.id.download_toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setTitle("Descargas");

		//TODO: Si se guardan las descargas al cerrar la app, debería obtenerlas aquí para pasarlas al adapter.
		//TODO: Falta pasar las descargas al intent desde Profile, y mandar el intent.
		Intent downloads_intent = getIntent();
		downloadService = (DownloadService) downloads_intent.getSerializableExtra("downloadService");
		al_downloads = downloadService.getDownloads();
		listAdapter = new DownloadListAdapter(getApplicationContext(), al_downloads);
		dl_listView = findViewById(R.id.downloads_list);
		dl_listView.setAdapter(listAdapter);

		bindDownloads();

	}

	/**
	 * Método que enlaza cada una de las descargas (servicios) con cada uno de sus respectivos
	 * elementos de la listView.
	 */
	private void bindDownloads(){


	}

}
