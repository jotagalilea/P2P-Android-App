package com.example.samue.login;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Base64;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by jotagalilea on 12/03/2019.
 *
 * Servicio que almacena el estado de las descargas durante su ejecución.
 */
public class DownloadService extends Service{

	private static DownloadService thisService = null;
	private ArrayList<Download> al_downloads;
	private HashMap<String, Download> hm_downloads;
	private final IBinder binder = new DownloadBinder();
	private DownloadThread thread;
	private JSONObject jsonMsg;
	private boolean newMsgReceived = false;



	//TODO: Falta crear hilos de descarga y actualización de estado.
	//public DownloadService(){}

	@Override
	public void onCreate(){
		al_downloads = new ArrayList<>();
		thisService = this;
		thread = new DownloadThread();
	}


	/*public static DownloadService getService(){
		return thisService;
	}
	*/// Hacerlo con el Binder.


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
		synchronized (jsonMsg){
			jsonMsg = json;
			newMsgReceived = true;
			jsonMsg.notify();
		}
	}



	public void addDownload(Download d){
		al_downloads.add(d);
		hm_downloads.put(d.getFileName(), d);
	}


	public ArrayList<Download> getDownloads(){
		return al_downloads;
	}


	public void stop(){
		//TODO: No sé si haría falta desvincular todos los clientes.
		this.stopSelf();
	}


	private void notificate(String notification){
		final String notice = notification;
		Toast.makeText(getApplicationContext(), notice, Toast.LENGTH_SHORT).show();
	}





	/////////////////////////////////// Clases extra ///////////////////////////////////

	private class DownloadThread extends Thread{
		private int step, storedLastSecond, size, inicio, total, bps, seconds;
		private String archivoCompartido;
		private Timer timer;
		private Download dl;

		@Override
		public void run(){
			try{
				timer = new Timer();
				timer.schedule(new TimerTask() {
					@Override
					public void run() {
						updateDownload(dl);
					}
				}, 1000, 1000);

				while (true) {
					// El servicio se pausa si no se recibe el json.
					synchronized (jsonMsg) {
						while (!newMsgReceived)
							jsonMsg.wait();
						newMsgReceived = false;

						String aux = "";
						//int size, inicio;
						size = jsonMsg.getInt("size");
						this.step = size / 100;
						inicio = jsonMsg.getInt("inicio");
						String name = jsonMsg.getString("name");
						boolean split = jsonMsg.getBoolean("split");
						boolean lastPiece = jsonMsg.getBoolean("lastPiece");
						String archive = jsonMsg.getString("archive");

						//TODO: COMPROBAR SI AL INICIARSE UNA DESCARGA inicio VALE 0.
						if (inicio == 0) {
							dl = new Download(name, size);
							addDownload(dl);
						} else
							//TODO: Quizá no necesito el HashMap...
							dl = hm_downloads.get(name);

						if (split) {
							aux = this.archivoCompartido;
							this.archivoCompartido = aux + archive;
							if (!lastPiece) {
								if (inicio != 0) {
									if (inicio > this.total)
										this.total += this.step;
								} else
									this.total = this.step;
							} else {
								byte[] bFile = Base64.decode(this.archivoCompartido, Base64.URL_SAFE);
								guardarArchivo(bFile, name);
							}
						} else {
							byte[] bFile = Base64.decode(archive, Base64.URL_SAFE);
							guardarArchivo(bFile, name);
						}
					}
				}

			} catch(Exception e){
				e.printStackTrace();
			}

		}


		private void updateDownload(Download dl){
			if (dl != null){
				byte prog = (byte) ((storedLastSecond/inicio) * 100);
				dl.updateProgress(prog);
				bps = inicio - storedLastSecond;
				dl.updateSpeed(bps);
				++seconds;
				dl.updateETA(seconds);
				storedLastSecond += inicio;
			}
		}



		private void guardarArchivo(byte[] bFile, String name){
			String path = Environment.getExternalStorageDirectory().getPath() + "/DownloadService";
			File file = new File(path, "P2PArchiveSharing");

			if(!file.isDirectory()){
				file.mkdirs();
			}

			path += "/P2PArchiveSharing/" + name;

			try{
				FileOutputStream fos = new FileOutputStream(path);
				BufferedOutputStream bos = new BufferedOutputStream(fos);

				bos.write(bFile);

				bos.close();
				fos.close();

				this.archivoCompartido = "";
				notificate("Archive " + name + " saved in Downloads");

			}catch(Exception e){
				e.printStackTrace();
			}

		}
	}



	public class DownloadBinder extends Binder {
		DownloadService getService(){
			return DownloadService.this;
		}
	}


}
