package com.jutong.live.param;

public class VideoParam {
	private int width;
	private int height;
	private int cameraId;
	private int fps;
	private int bitrate;

	public VideoParam(int width, int height, int bitrate,int fps, int cameraId) {
		this.width = width;
		this.height = height;
		this.bitrate = bitrate;
		this.fps = fps;
		this.cameraId = cameraId;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getCameraId() {
		return cameraId;
	}

	public void setCameraId(int cameraId) {
		this.cameraId = cameraId;
	}

	public int getFps() {
		return fps;
	}

	public void setFps(int fps) {
		this.fps = fps;
	}

	public int getBitrate() {
		return bitrate;
	}

	public void setBitrate(int bitrate) {
		this.bitrate = bitrate;
	}
}
