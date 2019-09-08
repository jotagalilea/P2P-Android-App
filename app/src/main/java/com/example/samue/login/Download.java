package com.example.samue.login;

import java.io.File;

/**
 * Created by jotagalilea on 21/03/2019.
 *
 * Almacenamiento del estado de una descarga.
 */
public class Download {
	private final String friend;
	private final String fileName;
	private final String path;
	private final String sizeString;
	private final long size;
	private int progress;
	private StringBuilder speed;
	private StringBuilder estimatedTime;
	private boolean running;
	private boolean finished;
	private final String B = " B";
	private final String KB = " KB";
	private final String MB = " MB";
	private final String BPS = " B/s";
	private final String KBPS = " KB/s";
	private final String MBPS = " MB/s";
	private int KILO = 1024;
	private int MEGA = 1024*1024;


	public Download(String name, String p, long size, String f){
		this.friend = f;
		this.fileName = name;
		this.path = p;
		this.size = size;
		this.sizeString = getSizeString(size);
		this.progress = 0;
		this.speed = new StringBuilder();
		this.estimatedTime = new StringBuilder();
		this.running = false;
		this.finished = false;
	}


	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Download d = (Download) o;
		return fileName.equalsIgnoreCase(d.getFileName()) &&
				(size == d.getSize()) && (running == d.isRunning());
	}


	/**
	 * Obtiene amigo vinculado a la descarga.
	 * @return
	 */
	public String getFriend(){
		return friend;
	}

	/**
	 * Fichero que se descarga.
	 * @return
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * Ruta del fichero.
	 * @return
	 */
	public String getPath() {
		return path;
	}

	/**
	 * Tamaño del fichero como String.
	 * @return
	 */
	public String getSizeString() {
		return sizeString;
	}

	/**
	 * Tamaño del fichero.
	 * @return
	 */
	public long getSize() {
		return size;
	}

	/**
	 * Progreso porcentual de la descarga.
	 * @return
	 */
	public int getProgress() {
		return progress;
	}

	/**
	 * Velocidad actual.
	 * @return
	 */
	public String getSpeed() {
		return speed.toString();
	}

	/**
	 * Tiempo estimado actual.
	 * @return
	 */
	public String getEstimatedTime() {
		return estimatedTime.toString();
	}

	/**
	 * Obtiene si la descarga está activa.
	 * @return
	 */
	public boolean isRunning(){
		return running;
	}

	/**
	 * Señaliza la descarga como activa.
	 */
	public void setRunning(){
		if (!running)
			running = true;
	}

	/**
	 * Señaliza la descarga como parada.
	 */
	public void setStopped(){
		if (running)
			running = false;
	}

	/**
	 * Obtiene si está terminada.
	 * @return
	 */
	public boolean isFinished(){
		return finished;
	}

	/**
	 * Actualiza el progreso.
	 * @param p
	 */
	public void updateProgress(int p){
		if (p>=0 && p<100)
			progress = p;
		else if (p>=100) {
			progress = 100;
			finished = true;
		}
	}

	/**
	 * Actualiza la velocidad en bytes por segundo y la convierte a una escala adecuada.
	 * @param bytesPerSecond
	 */
	public void updateSpeed(int bytesPerSecond){
		float f = 0F;
		if (progress == 100)
			speed.replace(0, speed.length(), BPS);
		else {
			if (bytesPerSecond >= MEGA) {
				f = (float) bytesPerSecond / MEGA;
				speed.replace(0, speed.length(), MBPS);
			} else if (bytesPerSecond >= KILO) {
				f = (float) bytesPerSecond / KILO;
				speed.replace(0, speed.length(), KBPS);
			} else {
				speed.replace(0, speed.length(), BPS);
			}
		}
		speed.insert(0, String.format("%.1f", f));
	}

	/**
	 * Actualiza el tiempo estimado.
	 * @param bps bytes por segundo.
	 */
	public void updateETA(int bps){
		if (progress == 100)
			estimatedTime.replace(0, estimatedTime.length(), String.format("%02d:%02d:%02d",0,0,0));
		else {
			if (bps != 0) {
				long aproxDataLeft = size - ((size * progress) / 100);
				int secondsLeft = (int) (aproxDataLeft / bps);
				estimatedTime.replace(0, estimatedTime.length(),
						String.format("%02d:%02d:%02d", secondsLeft / 3600, secondsLeft / 60, secondsLeft % 60));
			}
		}
	}

	/**
	 * Obtiene el tamaño del fichero como String.
	 * @param s tamaño.
	 * @return tamaño en String.
	 */
	private String getSizeString(long s){
		StringBuilder aux;
		if (s >= MEGA){
			s /= MEGA;
			aux = new StringBuilder(MB);
		}
		else if (s >= KILO){
			s /= KILO;
			aux = new StringBuilder(KB);
		}
		else{
			aux = new StringBuilder(B);
		}
		aux.insert(0, s);

		return aux.toString();
	}

	/**
	 * Borra el fichero de una descarga. Útil cuando la descarga se cancela.
	 */
	public void deleteFile(){
		File f = new File(path);
		f.delete();
	}

}
