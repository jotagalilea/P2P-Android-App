package com.example.samue.login;

/**
 * Created by jotagalilea on 21/03/2019.
 *
 * Almacenamiento del estado de una descarga.
 */
public class Download {
	private final String fileName;
	private final String sizeString;
	private final long size;
	private int progress;
	private StringBuilder speed;
	private StringBuilder estimatedTime;
	private final String B = " B";
	private final String KB = " KB";
	private final String MB = " MB";
	private final String BPS = " B/s";
	private final String KBPS = " KB/s";
	private final String MBPS = " MB/s";
	private final int KILO = 1024;
	private final int MEGA = 1024*1024;


	public Download(String name, long size){
		this.fileName = name;
		this.size = size;
		this.sizeString = getSizeString(size);
		this.progress = 0;
		this.speed = new StringBuilder();
		this.estimatedTime = new StringBuilder();
	}


	// De momento no es necesario.
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Download d = (Download) o;
		return fileName.equalsIgnoreCase(d.getFileName()) &&
				(size == d.getSize());
	}



	public String getFileName() {
		return fileName;
	}

	public String getSizeString() {
		return sizeString;
	}

	public long getSize() {
		return size;
	}

	public int getProgress() {
		return progress;
	}

	public String getSpeed() {
		return speed.toString();
	}

	public String getEstimatedTime() {
		return estimatedTime.toString();
	}

	public void updateProgress(int p){
		if (p>=0 && p<=100)
			progress = p;
	}

	public void updateSpeed(int bytesPerSecond){
		if (bytesPerSecond >= MEGA){
			bytesPerSecond /= MEGA;
			speed.replace(0, speed.length(), MBPS);
		}
		else if (bytesPerSecond >= KILO){
			bytesPerSecond /= KILO;
			speed.replace(0, speed.length(), KBPS);
		}
		else{
			speed.replace(0, speed.length(), BPS);
		}
		speed.insert(0, bytesPerSecond);
	}


	public void updateETA(int bps){
		if (bps != 0) {
			long aproxDataLeft = size - ((size * progress) / 100);
			int secondsLeft = (int) (aproxDataLeft / bps);
			estimatedTime.replace(0, estimatedTime.length(),
					String.format("%02d:%02d:%02d", secondsLeft / 3600, secondsLeft / 60, secondsLeft % 60));
		}
	}


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
}
