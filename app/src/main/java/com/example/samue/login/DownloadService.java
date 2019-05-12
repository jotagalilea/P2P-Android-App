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

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

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
	private Boolean newMsgReceived = FALSE;
	private final Object monitor = new Object();



	//TODO: Falta crear hilos de descarga y actualización de estado.
	//public DownloadService(){}

	@Override
	public void onCreate(){
		al_downloads = new ArrayList<>();
		thisService = this;
		//jsonMsg = new JSONObject();
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
		synchronized (monitor){
			jsonMsg = json;
			newMsgReceived = TRUE;
			monitor.notify();
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
		//TODO: No sé si haría falta desvincular todos los clientes.
		this.stopSelf();
	}


	private void notificate(String notification){
		final String notice = notification;
		Toast.makeText(getApplicationContext(), notice, Toast.LENGTH_LONG).show();
	}





	/////////////////////////////////// Clases extra ///////////////////////////////////

	private class DownloadThread extends Thread{
		private int step, storedLastSecond, size, start, total, bps, seconds;
		//private String archivoCompartido;
		// ¡¡OJO!! ESTO NO SOPORTA DESCARGAS SIMULTÁNEAS:
		private StringBuilder codedData = new StringBuilder();
		private String name;
		private boolean nameAcquired = false;
		private FileOutputStream fos;
		//private StringBuilder decodedData = new StringBuilder();
		////////////////////////////////////////////////
		private Timer timer;
		private Download dl;

		@Override
		public void run(){
			try{
				// NO VALE PARA DESCARGAS PARALELAS:
				boolean lastPiece;
				StringBuilder archive = new StringBuilder();
				String path = Environment.getExternalStorageDirectory().getPath() + "/DownloadService";
				File file = new File(path, "P2PArchiveSharing");
				if(!file.isDirectory())
					file.mkdirs();
				path += "/P2PArchiveSharing/";

				timer = new Timer();
				timer.schedule(new TimerTask() {
					@Override
					public void run() {
						updateDownload(dl);
					}
				}, 1000, 1000);


				while (true) {
					// El servicio se pausa si no se recibe el json.
					synchronized (monitor) {
						while (!newMsgReceived)
							monitor.wait();
						newMsgReceived = FALSE;

						if (!nameAcquired) {
							name = jsonMsg.getString(Utils.NAME);
							path += name;
							fos = new FileOutputStream(path);
							nameAcquired = true;
						}
						//int size, start;
						size = jsonMsg.getInt(Utils.SIZE);
						step = size / 100;
						start = jsonMsg.getInt(Utils.START);
						//boolean split = jsonMsg.getBoolean("split");
						lastPiece = jsonMsg.getBoolean(Utils.LAST_PIECE);
						//TODO: Ver si esto es más rápido y consume menos memoria:
						archive.replace(0, archive.length(), jsonMsg.getString(Utils.ARCHIVE));

						if (start == 0) {
							dl = new Download(name, size);
							addDownload(dl);
						} /*else
							//TODO: Quizá no necesito el HashMap...
							dl = hm_downloads.get(name);
							*/

						codedData.replace(0, codedData.length(), jsonMsg.getString(Utils.ARCHIVE));
						//decodedData.append(Base64.decode(codedData.toString(), Base64.URL_SAFE));
						fos.write(Base64.decode(codedData.toString(), Base64.URL_SAFE));

						if (!lastPiece) {
							//TODO: ¡¡OJO!! Esto NO SOPORTA descargas SIMULTÁNEAS.
							if (start != 0) {
								if (start > total)
									total += step;
							} else
								total = step;
						} else {
							nameAcquired = false;
							fos.close();
						}
					}
				}

			} catch(Exception e){
				e.printStackTrace();
			}

		}


		private void updateDownload(Download dl){
			if (dl != null){
				byte prog;
				if (start != 0)
					prog = (byte) ((storedLastSecond/start) * 100);
				else
					prog = 1;
				dl.updateProgress(prog);
				bps = start - storedLastSecond;
				dl.updateSpeed(bps);
				++seconds;
				dl.updateETA(seconds);
				storedLastSecond += start;
			}
		}



		//TODO: El guardado tiene que ser sobre la marcha de la descarga, así no.
		/*private void guardarArchivo(byte[] bFile, String name){
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
				notificate("Archive " + name + " saved in DownloadService");

			}catch(Exception e){
				e.printStackTrace();
			}

		}
		*/
	}



	public class DownloadBinder extends Binder {
		DownloadService getService(){
			return DownloadService.this;
		}
	}


}
