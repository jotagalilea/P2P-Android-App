package com.example.samue.login;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
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
	private ListView dl_listView;
	private DownloadListAdapter listAdapter;
	private Timer timer;
	private boolean serviceBound = false;
	private ServiceConnection serviceConnection = new ServiceConnection(){
		@Override
		public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
			DownloadService.DownloadBinder binder = (DownloadService.DownloadBinder) iBinder;
			downloadService = binder.getService();
			serviceBound = true;
			prepareActivity();
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
		setContentView(R.layout.activity_download_manager);
		Toolbar toolbar = findViewById(R.id.resources_toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setTitle("Descargas");

		Intent intent = getIntent();
		Intent serviceIntent = intent.getParcelableExtra("downloadServiceIntent");
		bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
	}


	/**
	 * Prepara la actividad para su uso.
	 *
	 * Obtiene la lista de descargas del servicio, construye el adaptador, crea los listener, y prepara
	 * el timer que actualizará la interfaz.
	 */
	private void prepareActivity(){
		if (serviceBound) {
			al_downloads = downloadService.getDownloads();
			listAdapter = new DownloadListAdapter(getApplicationContext(), al_downloads);
			dl_listView = findViewById(R.id.downloads_list);
			dl_listView.setAdapter(listAdapter);
			dl_listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
					final Download dl = al_downloads.get(i);
					final Dialog dialog = new Dialog(DownloadManagerActivity.this);
					dialog.setContentView(R.layout.dialog_canceldownload);
					dialog.show();

					Button cancelButton = dialog.findViewById(R.id.button_cancel);
					cancelButton.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View view) {
							dialog.dismiss();
							try {
								JSONObject json = new JSONObject();
								json.put("type", "RA");
								json.put(Utils.NAME, dl.getFileName());
								json.put(Utils.CANCEL_DL, true);
								if (dl.isRunning()) {
									downloadService.stopDownload(dl.getPath(), dl.getFileName());
									dl.setStopped();
									//dl.deleteFile();
								}

								Profile.downloaderClient.transmit(dl.getFriend(), json);
								//Profile.pnRTCClient.closeConnection(dl.getFriend());
								al_downloads.remove(dl);
							}
							catch (JSONException e){
								e.printStackTrace();
							}
						}
					});

					Button openButton = dialog.findViewById(R.id.button_open_file);
					openButton.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View view) {
							if (dl.isFinished()) {
								File f = new File(dl.getPath());
								Utils.openFile(dl.getFileName(), f, DownloadManagerActivity.this);
								dialog.dismiss();
							}
							else{
								DownloadManagerActivity.this.runOnUiThread(new Runnable() {
									@Override
									public void run() {
									Toast.makeText(DownloadManagerActivity.this, "No se puede abrir porque no ha terminado",
											Toast.LENGTH_LONG).show();
									}
								});
								dialog.dismiss();
							}
						}
					});
				}
			});
			timer = new Timer();
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					updateGUI();
				}
			}, 0, 1000);
		}
	}


	/**
	 * Va notificando al adapter que se actualice con el estado de las descargas.
	 */
	private void updateGUI(){
		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				listAdapter.notifyDataSetChanged();
			}
		});
	}


	@Override
	public void onDestroy(){
		unbindService(serviceConnection);
		super.onDestroy();
	}

}
