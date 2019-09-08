package com.example.samue.login;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import com.example.samue.login.Profile.FileSender;


/**
 * Created by jotagalilea on 20/08/2019.
 *
 * Hilo útil para administrar una cola de envíos a amigos.
 */
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
					// Si la cola no está vacía se lanza un nuevo envío.
					if (!sendersQueue.isEmpty()){
						Iterator it = sendersQueue.entrySet().iterator();
						Map.Entry data = (Map.Entry) it.next();
						FileSender fs = (FileSender) data.getValue();
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

	/**
	 * Añade un hilo de envío creado previamente a la cola.
	 * @param fileName Nombre del fichero.
	 * @param fs Hilo de envío.
	 * @return true si la cola no está llena, false en otro caso.
	 */
	public boolean addSender(String fileName, FileSender fs){
		boolean added = sendersQueue.size() < QUEUE_MAX_SIZE;
		if (added)
			sendersQueue.put(fileName, fs);
		return added;
	}

	/**
	 * Elimina un envío encolado.
	 * @param filename Nombre del fichero que iba a ser enviado.
	 */
	public void removeSender(String filename){
		sendersQueue.remove(filename);
	}

	/**
	 * Comprueba si la cola está vacía.
	 * @return true si la cola está vacía, false en otro caso.
	 */
	public boolean isQueueEmpty(){
		return sendersQueue.isEmpty();
	}

	/**
	 * Notifica al monitor del manager que hay un nuevo envío.
	 */
	public void notifyFinishedUpload(){
		synchronized (monitor){
			newUpload = true;
			monitor.notify();
		}
	}

	/**
	 * Comprueba si la cola está llena.
	 * @return true si la cola está llena, false en otro caso.
	 */
	public boolean queueFull(){
		return sendersQueue.size() >= QUEUE_MAX_SIZE;
	}

	/**
	 * Comprueba si la cola tiene un hilo de envío cuyo nombre de fichero es el parámetro.
	 * @param name Nombre del fichero
	 * @return true si está, false en otro caso.
	 */
	public boolean hasSender(String name){
		return sendersQueue.containsKey(name);
	}
}
