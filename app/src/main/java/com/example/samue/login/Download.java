package com.example.samue.login;

/**
 * Created by jotagalilea on 21/03/2019.
 *
 * Almacenamiento del estado de una descarga.
 */
public class Download {
	private final String fileName;
	private final int size;
	private byte progress;
	private String speed;
	private String estimatedTime;
	private final String BPS = " B/s";
	private final String KBPS = " KB/s";
	private final int KILO = 1024;
	private final String MBPS = " MB/s";
	private final int MEGA = 1024*1024;


	public Download(String name, int size){
		this.fileName = name;
		this.size = size;
		this.progress = 0;
		this.speed = "";
		this.estimatedTime = "";
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

	public int getSize() {
		return size;
	}

	public byte getProgress() {
		return progress;
	}

	public String getSpeed() {
		return speed;
	}

	public String getEstimatedTime() {
		return estimatedTime;
	}

	public void updateProgress(byte p){
		if (p>=0 && p<=100)
			progress = p;
	}

	public void updateSpeed(int bytesPerSecond){
		String str;
		if (bytesPerSecond >= MEGA){
			bytesPerSecond /= MEGA;
			str = MBPS;
		}
		else if (bytesPerSecond >= KILO){
			bytesPerSecond /= KILO;
			str = KBPS;
		}
		else{
			str = BPS;
		}
		this.speed = Integer.toString(bytesPerSecond) + str;
	}


	public void updateETA(int seconds){
		this.estimatedTime = String.format("%02d:%02d:%02d", seconds/3600, seconds/60, seconds%60);
	}
}
