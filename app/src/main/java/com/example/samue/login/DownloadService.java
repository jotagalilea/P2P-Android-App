package com.example.samue.login;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by jotagalilea on 12/03/2019.
 *
 * Servicio que almacena el estado de las descargas durante su ejecución.
 */
public class DownloadService extends Service implements Serializable {

	private ArrayList<Download> al_downloads;
	private final IBinder binder = new DownloadBinder();


	public class DownloadBinder extends Binder {
		DownloadService getService(){
			return DownloadService.this;
		}
	}

	//TODO: Falta crear hilos de descarga y actualización de estado.
	//public DownloadService(){}

	@Override
	public void onCreate(){
		al_downloads = new ArrayList<>();
	}


	@Override
	public int onStartCommand(Intent intent, int flags, int startId){
		Toast.makeText(this, "Servicio de descargas iniciado", Toast.LENGTH_SHORT).show();
		return START_REDELIVER_INTENT;
	}


	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}


	//TODO: Que se añada al ArrayList y se lance un hilo para ella.
	//TODO: Sería bueno poner límite de hilos. Las nuevas descargas que se queden en cola hasta que acabe una.
	public void addDownload(){

	}


	public ArrayList<Download> getDownloads(){
		return this.al_downloads;
	}


	public void stop(){
		//TODO: No sé si haría falta desvincular todos los clientes.
		this.stopSelf();
	}


}
