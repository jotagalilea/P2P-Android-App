package com.example.samue.login;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by jotagalilea on 12/03/2019.
 *
 * Actividad que agrupa las descargas.
 */

public class DownloadManagerActivity extends AppCompatActivity {

	private ArrayList<Download> al_downloads;
	private DownloadService downloadService;
	private boolean serviceBound = false;
	private ListView dl_listView;
	private DownloadListAdapter listAdapter;
	private Timer timer;
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

		Intent intent = getIntent();
		Intent serviceIntent = intent.getParcelableExtra("downloadServiceIntent");
		//TODO: Revisar si estos parámetros son correctos:
		bindService(serviceIntent, dl_serviceConnection, Context.BIND_AUTO_CREATE);

		al_downloads = downloadService.getDownloads();
		listAdapter = new DownloadListAdapter(getApplicationContext(), al_downloads);
		dl_listView = findViewById(R.id.downloads_list);
		dl_listView.setAdapter(listAdapter);

		timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				updateGUI();
			}
		}, 0, 1000);
	}



	// Va actualizando las descargas y notificando al adapter.
	private void updateGUI(){
		/*Iterator<Download> it = al_downloads.iterator();
		while (it.hasNext()) {
			try {
				Download d = it.next();
				 //findViewById(R.id.dl_row);
			}
			catch (ConcurrentModificationException e) {
				e.printStackTrace();
				it = al_downloads.iterator();
			}
		}
		*/
		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				listAdapter.notifyDataSetChanged();
			}
		});
	}


}
