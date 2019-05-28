package com.example.samue.login;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.util.Pair;
import android.util.Base64;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by jotagalilea on 12/03/2019.
 *
 * Servicio que almacena el estado de las descargas durante su ejecución.
 */
public class DownloadService extends Service{

	private ArrayList<Download> al_downloads;
	private final IBinder binder = new DownloadBinder();
	private ManagerThread managerThread;
	private JSONObject jsonMsg;
	private boolean newMsgReceived = false;
	private final Object serviceMonitor = new Object();
	private byte threadsRunning;
	private final byte MAX_DL_THREADS = 2;
	// HashMap con clave nombre del fichero y valor el par monitor del hilo y el hilo.
	private HashMap<String, Pair<Object, ManagerThread.DownloadThread>> hm_downloads;



	@Override
	public void onCreate(){
		al_downloads = new ArrayList<>();
		hm_downloads = new HashMap<>(2);
		managerThread = new ManagerThread();
		threadsRunning = 0;
	}


	@Override
	public int onStartCommand(Intent intent, int flags, int startId){
		Toast.makeText(this, "Servicio de descargas iniciado", Toast.LENGTH_LONG).show();
		managerThread.start();
		return START_REDELIVER_INTENT;
	}


	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}


	public void handleMsg(final JSONObject json){
		synchronized (serviceMonitor){
			jsonMsg = json;
			newMsgReceived = true;
			serviceMonitor.notify();
		}
	}



	public void addDownload(Download d){
		al_downloads.add(d);
		//hm_downloads.put(d.getFileName(), d);
	}


	public ArrayList<Download> getDownloads(){
		return al_downloads;
	}


	public void stop(){
		//TODO: No sé si haría falta desvincular todos los clientes. Interrumpir hilos si los hay.
		this.stopSelf();
	}


	/*private void notificate(String notification){
		final String notice = notification;
		Toast.makeText(getApplicationContext(), notice, Toast.LENGTH_LONG).show();
	}*/





	/////////////////////////////////// Clases extra ///////////////////////////////////

	/**
	 * Administrador de los hilos en los que se ejecutan las descargas según los mensajes que se van recibiendo.
	 */
	private class ManagerThread extends Thread{
		private long fileLength;
		private String name;
		private boolean newDownload = false;
		private Download dl;


		@Override
		public void run(){
			try{
				while (true) {
					// El servicio se pausa si no se recibe el json.
					synchronized (serviceMonitor) {
						while (!newMsgReceived)
							serviceMonitor.wait();
						/*
						 * Pasos:
						 * 1. Recibir primer mensaje con NEW_DL solo con el nombre del fichero y el tamaño.
						 * 2. Añadir nueva descarga a la cola.
						 * 		2.1. Si hay hilos libres se arranca uno y se manda un mensaje al amigo para que comience la transferencia.
						 * 	//TODO: Falta guardar la info del amigo de alguna manera, si no no voy a saber de quién es cada archivo.
						 * 		2.2. Si no ambos dispositivos se mantienen con el hilo parado hasta que queden un hilo de descarga libre.
						 * 			 Entonces se avisa al amigo	con START (por ejemplo).
						 * 			 El amigo que manda el archivo no tiene que hacer nada, sólo está esperando a recibir el aviso para
						 * 			 comenzar.
						 * 3. Cuando una descarga finalice comenzar otra en un hilo nuevo.
						 */

						name = jsonMsg.getString(Utils.NAME);
						newDownload = jsonMsg.getBoolean(Utils.NEW_DL);

						// Si es una descarga nueva se añade al ArrayList.
						if (newDownload) {
							fileLength = jsonMsg.getLong(Utils.FILE_LENGTH);
							dl = new Download(name, fileLength);
							addDownload(dl);
						}

						// Si hay hilos disponibles...
						if (threadsRunning < MAX_DL_THREADS){
							if (newDownload)
								startDownload();

							// Si no es nueva descarga el json es de una descarga activa y hay que ver de cuál es y notificar a su monitor.
							else{
								//Creo que falta comprobar el monitor.
								boolean dl_found = false;
								Iterator<Download> it = al_downloads.iterator();
								while (it.hasNext() && !dl_found){
									dl = it.next();
									dl_found = !dl.isRunning();
								}
								// Si se encuentra se lanza.
								if (dl_found)
									startDownload();
							}
						}
						// Si no, se trata de un json para alguna de las descargas activas y hay que pasárselo y notificárselo.
						else{
							Pair<Object, DownloadThread> dl_pair = hm_downloads.get(dl.getFileName());
							Object dl_monitor = dl_pair.first;
							DownloadThread th = dl_pair.second;

							synchronized (dl_monitor){
								dl_monitor.notify();
								//Puede que meter el json en el hilo así no sea necesario.
								//Quizá sea útil th.isInterrupted() o th.isAlive().
								th.setJSON(jsonMsg);
							}
						}

						newDownload = false;
						newMsgReceived = false;
					}
				}

			} catch(Exception e){
				e.printStackTrace();
			}
		}


		private void startDownload(){
			Object monitor = new Object();
			dl.setRunning();
			DownloadThread dl_th = new DownloadThread(monitor, dl);
			Pair<Object, DownloadThread> pair = new Pair<>(monitor, dl_th);
			hm_downloads.put(dl.getFileName(), pair);
			++threadsRunning;
			dl_th.start();
			// TODO: Falta avisar aquí al amigo para que comience la transferencia.
		}



		/**
		 * Clase dedicada a almacenar el hilo en el que se ejecuta una descarga.
		 */
		private class DownloadThread extends Thread{
			private JSONObject json;
			private Timer timer;
			private int storedLastSecond, bps, bytesWritten;
			private StringBuilder codedData = new StringBuilder();
			private byte[] decodedData;
			private FileOutputStream fos;
			private Download dl;
			private final Object dl_monitor;
			private boolean lastPiece;


			public DownloadThread(Object m, Download d){
				dl_monitor = m;
				dl = d;
				lastPiece = false;
			}

			@Override
			public void run(){
				try{
					String path = Environment.getExternalStorageDirectory().getPath();
					File file = new File(path, "P2PArchiveSharing");
					if(!file.isDirectory())
						file.mkdirs();
					name = jsonMsg.getString(Utils.NAME);
					fos = new FileOutputStream(path);

					bytesWritten = 0;

					//
					timer = new Timer();
					timer.schedule(new TimerTask() {
						@Override
						public void run() {
							updateDownload(dl);
						}
					}, 1000, 1000);

					while (!lastPiece){
						synchronized (dl_monitor){
							while (!newMsgReceived)
								dl_monitor.wait();

							codedData.replace(0, codedData.length(), json.getString(Utils.DATA));
							decodedData = Base64.decode(codedData.toString(), Base64.URL_SAFE);
							fos.write(decodedData);
							bytesWritten += decodedData.length;
							storedLastSecond += decodedData.length;

							lastPiece = json.getBoolean(Utils.LAST_PIECE);
							if (lastPiece) {
								fos.close();
								timer.cancel();
								hm_downloads.remove(dl.getFileName());
								--threadsRunning;
							}
						}
					}

				} catch(Exception e){
					e.printStackTrace();
				}
			}


			private void updateDownload(Download dl){
				if (dl != null){
					int prog = (int) ((bytesWritten * 100L) / dl.getSize());
					dl.updateProgress(prog);
					bps = storedLastSecond;
					dl.updateSpeed(bps);
					dl.updateETA(bps);
					storedLastSecond = 0;
				}
			}


			public void setJSON(JSONObject j){
				json = j;
			}
		}
	}



	public class DownloadBinder extends Binder {
		DownloadService getService(){
			return DownloadService.this;
		}
	}


}
