package com.jutong.live.jni;

import android.os.Handler;

public class PusherNative {

	private Handler mHandler;

	public void setPusherHandler(Handler handler) {
		mHandler = handler;
	}

	public void postMessage(int what) {
		mHandler.sendEmptyMessage(what);
	}

	public native void prepare();

	public native void setVideoOptions(int width, int height, int bitrate,
			int fps);

	public native void setAudioOptions(int sampleRate, int channel);

	public native void fireVideo(byte[] buffer);

	public native void fireAudio(byte[] buffer, int len);

	public native boolean startPusher(String url);

	public native void stopPusher();

	public native void release();
}
