package com.example.samue.login;

/**
 * Created by jotagalilea on 21/03/2019.
 */
public class Download {
	private final String fileName;
	private final String size;
	private byte progress;
	private String speed;
	private String estimatedTime;


	public Download(String name, String size){
		this.fileName = name;
		this.size = size;
		this.progress = 0;
		this.speed = "";
		this.estimatedTime = "";
	}


	public void runDownload(){

	}



	public String getFileName() {
		return fileName;
	}

	public String getSize() {
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
}
