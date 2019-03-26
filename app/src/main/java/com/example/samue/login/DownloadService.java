package com.example.samue.login;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Base64;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by jotagalilea on 12/03/2019.
 *
 * Servicio que almacena el estado de las descargas durante su ejecución.
 */
public class DownloadService extends Service implements Serializable {

	private static DownloadService thisService = null;
	private ArrayList<Download> al_downloads;
	private final IBinder binder = new DownloadBinder();
	private DownloadThread thread;
	private JSONObject jsonMsg;
	private boolean newMsgReceived = false;


	class DownloadThread extends Thread{
		private int step, total;
		private String archivoCompartido;

		@Override
		public void run(){
			try{
				while () {
					// El servicio se pausa si no se recibe el json.
					synchronized (jsonMsg) {
						while (!newMsgReceived)
							jsonMsg.wait();

						newMsgReceived = false;
						String aux = "";
						int size, inicio;
						size = jsonMsg.getInt("size");
						this.step = size / 100;
						inicio = jsonMsg.getInt("inicio");
						String name = jsonMsg.getString("name");
						boolean split = jsonMsg.getBoolean("split");
						boolean lastPiece = jsonMsg.getBoolean("lastPiece");
						String archive = jsonMsg.getString("archive");

						if (split) {
							aux = this.archivoCompartido;
							this.archivoCompartido = aux + archive;
							if (!lastPiece) {
								if (inicio != 0) {
									if (inicio > this.total) {
										Profile.this.runOnUiThread(new Runnable() {
											@Override
											public void run() {
												pd.incrementProgressBy(1);
											}
										});
										this.total += this.step;
									}
								} else {
									this.total = this.step;
								}
							} else {
								byte[] bFile = Base64.decode(this.archivoCompartido, Base64.URL_SAFE);
								Profile.this.runOnUiThread(new Runnable() {
									@Override
									public void run() {
										pd.dismiss();
									}
								});
								guardarArchivo(bFile, name);
							}
						} else {
							byte[] bFile = Base64.decode(archive, Base64.URL_SAFE);
							Profile.this.runOnUiThread(new Runnable() {
								@Override
								public void run() {
									pd.dismiss();
								}
							});
							guardarArchivo(bFile, name);
						}
					}
				}

			} catch(Exception e){
				e.printStackTrace();
			}

		}
	}






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
		thisService = this;
		thread = new DownloadThread();
	}


	public static DownloadService getThisService(){
		return thisService;
	}


	@Override
	public int onStartCommand(Intent intent, int flags, int startId){
		Toast.makeText(this, "Servicio de descargas iniciado", Toast.LENGTH_SHORT).show();
		thread.start();
		return START_REDELIVER_INTENT;
	}


	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}


	public void handleMsg(final JSONObject json){
		jsonMsg = json;
		newMsgReceived = true;
		jsonMsg.notify();
	}


	//TODO: Que se añada al ArrayList y se lance un hilo para ella.
	//TODO: Sería bueno poner límite de hilos. Las nuevas descargas que se queden en cola hasta que acabe una.
	public void addDownload(Download d){
		al_downloads.add(d);

	}


	public ArrayList<Download> getDownloads(){
		return this.al_downloads;
	}


	public void stop(){
		//TODO: No sé si haría falta desvincular todos los clientes.
		this.stopSelf();
	}


}
