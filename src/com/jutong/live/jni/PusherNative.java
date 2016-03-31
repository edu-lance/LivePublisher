package com.jutong.live.jni;

import android.util.Log;

import com.jutong.live.LiveStateChangeListener;

public class PusherNative {

	private LiveStateChangeListener mListener;

	public PusherNative() {

	}

	public void setLiveStateChangeListener(LiveStateChangeListener listener) {
		mListener = listener;
	}

	public void onPostNativeError(int code) {
		Log.d("PusherNative", code + "");
		if (null != mListener) {
			mListener.onErrorPusher(code);
		}
	}

	public void onPostNativeState(int state) {
		if (state == 100) {
			mListener.onStartPusher();
		} else if (state == 101) {
			mListener.onStopPusher();
		}
	}

	public native void setVideoOptions(int width, int height, int bitrate,
			int fps);

	public native void setAudioOptions(int sampleRate, int channel);

	public native void fireVideo(byte[] buffer);

	public native void fireAudio(byte[] buffer, int len);

	public native int getInputSamples();

	public native boolean startPusher(String url);

	public native void stopPusher();

	public native void release();
}
