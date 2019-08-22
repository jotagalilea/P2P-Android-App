package com.example.samue.login;

/**
 * Created by jotagalilea on 20/08/2019.
 *
 * Hilo útil para administrar una cola de envíos a amigos.
 */

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import com.example.samue.login.Profile.FileSender;



public class SendersManager extends Thread {
	private static SendersManager singleton;
	/* Colección de hilos de envío de ficheros, identificables por el nombre de los ficheros.
	 * Es conveniente implementarlo como HashMap para acceder o eliminar rápidamente un sender
	 * mediante el nombre del fichero. Funciona como una cola cuando no se cancela ninguna
	 * descarga desde el dispositivo remoto.
	 */
	private HashMap<String,FileSender> sendersQueue;
	private final Object monitor;
	// Avisador de nueva subida.
	private boolean newUpload;
	// Tamaño máximo de la "cola".
	private final int QUEUE_MAX_SIZE = 4;

	private SendersManager(){
		super();
		sendersQueue = new HashMap<>(QUEUE_MAX_SIZE);
		monitor = new Object();
		newUpload = false;
	}

	public static SendersManager getSingleton() {
		if (singleton == null)
			singleton = new SendersManager();
		return singleton;
	}

	@Override
	public void run() {
		while (true){
			synchronized (monitor){
				try{
					while (!newUpload)
						monitor.wait();

					if (!sendersQueue.isEmpty()){
						Iterator it = sendersQueue.entrySet().iterator();
						Map.Entry data = (Map.Entry) it.next();
						FileSender fs = (FileSender) data.getValue();
						// TODO: ¿Falta avisar al amigo y esperar su respuesta?
						fs.start();
					}
					newUpload = false;
				}
				catch (InterruptedException e){
					e.printStackTrace();
				}
			}
		}
	}

	public boolean addSender(String fileName, FileSender fs){
		boolean added = sendersQueue.size() < QUEUE_MAX_SIZE;
		if (added)
			sendersQueue.put(fileName, fs);
		return added;
	}

	public void removeSender(String filename){
		sendersQueue.remove(filename);
	}

	public FileSender getSender(String fileName){
		return sendersQueue.get(fileName);
	}

	public boolean isQueueEmpty(){
		return sendersQueue.isEmpty();
	}

	public synchronized void notifyFinishedUpload(){
		newUpload = true;
		monitor.notify();
	}

	public boolean queueFull(){
		return sendersQueue.size() == QUEUE_MAX_SIZE;
	}
}
