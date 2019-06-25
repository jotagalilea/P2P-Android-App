package com.example.samue.login;

import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.util.Pair;
import android.util.Base64;
import android.webkit.MimeTypeMap;
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
	// Útil para el paso de los JSON entrantes al hilo que corresponda y controlar qué descargas están activas.
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
		private Timer timer;


		@Override
		public void run(){
			try{
				timer = new Timer();
				timer.schedule(new TimerTask() {
					@Override
					public void run() {
						//TODO: Probar esto para cuando llega una tercera descarga y termina una activa.
						//TODO: Quizá sea mejor hacerlo con un avisador para cuando termine la activa.
						if ((threadsRunning<MAX_DL_THREADS) && (al_downloads.size()>1)){
							boolean dl_found = false;
							Download d = null;
							Iterator<Download> it = al_downloads.iterator();
							while (it.hasNext() && !dl_found){
								d = it.next();
								dl_found = !d.isRunning();
							}
							// Si se encuentra se lanza.
							if (dl_found) {
								dl = d;
								startDownload();
							}
						}
					}
				}, 10010, 10010);

				while (true) {
					// El servicio se pausa si no se recibe el json.
					synchronized (serviceMonitor) {
						while (!newMsgReceived)
							serviceMonitor.wait();
						//TODO: Revisar este comentario:
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
							String friendName = jsonMsg.getString(Utils.FRIEND_NAME);
							fileLength = jsonMsg.getLong(Utils.FILE_LENGTH);
							dl = new Download(name, fileLength, friendName);
							addDownload(dl);
						}

						// Si hay hilos disponibles...
						if (threadsRunning < MAX_DL_THREADS){
							// Si es nueva descarga se lanza.
							if (newDownload)
								startDownload();

							// Si no es nueva descarga el json es de la única descarga activa y hay que notificar a su monitor.
							else
								notifyAndSetJson();
						}
						// Si no hay hilos disponibles, entonces se trata seguro de un json para alguna de
						// las descargas activas y hay que pasárselo y notificárselo.
						else
							notifyAndSetJson();


						newDownload = false;
						newMsgReceived = false;
					}
				}

			} catch(Exception e){
				e.printStackTrace();
			}
		}


		/**
		 * Método que notifica a una descarga que han llegado datos para ella. Para ello se obtiene
		 * el monitor del hilo y el hilo con ayuda del hashMap hm_downloads donde están almacenados
		 * y se le pasa el json. Por último se llama a notify() para que continúe.
		 */
		private void notifyAndSetJson(){
			Pair<Object, DownloadThread> dl_pair = hm_downloads.get(name);
			Object dl_monitor = dl_pair.first;
			DownloadThread th = dl_pair.second;

			synchronized (dl_monitor) {
				//Puede que meter el json así en el hilo no sea necesario.
				th.setJSON(jsonMsg);
				dl_monitor.notify();
			}
		}


		private void startDownload(){
			Object monitor = new Object();
			dl.setRunning();
			DownloadThread dl_th = new DownloadThread(monitor, dl);
			dl_th.setJSON(jsonMsg);
			Pair<Object, DownloadThread> pair = new Pair<>(monitor, dl_th);
			hm_downloads.put(dl.getFileName(), pair);
			++threadsRunning;
			dl_th.start();
			// TODO: Falta avisar aquí al amigo para que comience la transferencia.
			// Quizá no hace falta y ese trabajo ya lo hace pubnub.
			/*try{
				JSONObject signal = new JSONObject();
				signal.put(Utils.BEGIN, true);
				Profile.pnRTCClient.transmit(dl.getFriend(), signal);
			}
			catch (JSONException e){
				dl_th.interrupt();
				--threadsRunning;
				hm_downloads.remove(dl.getFileName());
				dl.setStopped();
			}*/
		}



		/**
		 * Clase dedicada a almacenar el hilo en el que se ejecuta una descarga.
		 */
		private class DownloadThread extends Thread{
			private JSONObject json;
			private Timer dl_timer;
			private int storedLastSecond, bps, bytesWritten;
			private StringBuilder codedData = new StringBuilder();
			private byte[] decodedData;
			private FileOutputStream fos;
			private Download dl;
			private final Object dl_monitor;
			private boolean lastPiece, newJson;


			public DownloadThread(Object m, Download d){
				dl_monitor = m;
				dl = d;
				lastPiece = false;
				newJson = true;
			}

			@Override
			public void run(){
				try{
					String path = Environment.getExternalStorageDirectory().getPath() + "/P2PArchiveSharing/";
					File file = new File(path);
					if(!file.isDirectory())
						file.mkdirs();
					name = jsonMsg.getString(Utils.NAME);
					path += name;
					fos = new FileOutputStream(path);
					file = new File(path);
					bytesWritten = 0;

					dl_timer = new Timer();
					dl_timer.schedule(new TimerTask() {
						@Override
						public void run() {
							updateDownload();
						}
					}, 1000, 1000);

					while (!lastPiece){
						synchronized (dl_monitor){
							while (!newJson)
								dl_monitor.wait();

							codedData.replace(0, codedData.length(), json.getString(Utils.DATA));
							decodedData = Base64.decode(codedData.toString(), Base64.URL_SAFE);
							fos.write(decodedData);
							bytesWritten += decodedData.length;
							storedLastSecond += decodedData.length;

							lastPiece = json.getBoolean(Utils.LAST_PIECE);
							if (lastPiece) {
								fos.close();
								dl_timer.cancel();
								hm_downloads.remove(dl.getFileName());
								--threadsRunning;
							}
							newJson = false;
						}
					}
					// Si se pidió una previsualización se abre el archivo:
					boolean isPreview = jsonMsg.getBoolean(Utils.PREVIEW_SENT);
					if (isPreview){
						MimeTypeMap myMime = MimeTypeMap.getSingleton();
						Intent newIntent = new Intent(Intent.ACTION_VIEW);
						String mimeType = myMime.getMimeTypeFromExtension(name.substring(name.lastIndexOf('.')+1));
						newIntent.setDataAndType(Uri.fromFile(file), mimeType);
						newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						try {
							getApplicationContext().startActivity(newIntent);
						} catch (ActivityNotFoundException e) {
							//TODO: Cambiar mensaje.
							Toast.makeText(getApplicationContext(), "No handler for this type of file.", Toast.LENGTH_LONG).show();
						}
					}
					String completed = "Descargado " + name;
					Toast.makeText(getApplicationContext(), completed, Toast.LENGTH_LONG).show();
				} catch(Exception e){
					e.printStackTrace();
				}
			}


			/**
			 * Actualiza los atributos de la descarga en este hilo en cada llamada.
			 */
			private void updateDownload(){
				if (dl != null){
					int prog = (int) ((bytesWritten * 100L) / dl.getSize());
					dl.updateProgress(prog);
					bps = storedLastSecond;
					dl.updateSpeed(bps);
					dl.updateETA(bps);
					storedLastSecond = 0;
				}
			}


			/**
			 * Actualiza el mensaje JSON y señaliza que lo ha recibido.
			 * @param j
			 */
			public void setJSON(JSONObject j){
				json = j;
				newJson = true;
			}
		}
	}



	public class DownloadBinder extends Binder {
		DownloadService getService(){
			return DownloadService.this;
		}
	}


}
