package com.jutong.live;

import android.hardware.Camera;
import android.media.AudioRecord;
import android.view.SurfaceHolder;

import com.jutong.live.jni.PusherNative;
import com.jutong.live.param.AudioParam;
import com.jutong.live.param.VideoParam;
import com.jutong.live.pusher.AudioPusher;
import com.jutong.live.pusher.VideoPusher;

public class LivePusher {

	private final static String TAG = "LivePusher";
	private Camera mCamera;
	private AudioRecord audioRecord;
	private VideoParam videoParam;
	private AudioParam audioParam;
	private VideoPusher videoPusher;
	private PusherNative mNative;
	private AudioPusher audioPusher;
	private LiveStateChangeListener mListener;

	static {
		System.loadLibrary("myjni");
	}

	public LivePusher(int width, int height, int bitrate, int fps,
			int sampleRate, int channel, int cameraId) {
		videoParam = new VideoParam(width, height, bitrate, cameraId);
		audioParam = new AudioParam(sampleRate, channel);
		mNative = new PusherNative();
	}

	public void prepare(SurfaceHolder surfaceHolder) {
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		videoPusher = new VideoPusher(surfaceHolder, videoParam, mNative);
		audioPusher = new AudioPusher(audioParam, mNative);
		videoPusher.setLiveStateChangeListener(mListener);
		audioPusher.setLiveStateChangeListener(mListener);
	}

	public void startPusher(String url) {
		videoPusher.startPusher();
		audioPusher.startPusher();
		mNative.startPusher(url);
	}

	public void stopPusher() {
		videoPusher.stopPusher();
		audioPusher.stopPusher();
		mNative.stopPusher();
	}

	public void switchCamera() {
		videoPusher.switchCamera();
	}

	public void relase() {
		stopPusher();
		videoPusher.setLiveStateChangeListener(null);
		audioPusher.setLiveStateChangeListener(null);
		mNative.setLiveStateChangeListener(null);
		videoPusher.release();
		audioPusher.release();
		mNative.release();
	}

	public void setLiveStateChangeListener(LiveStateChangeListener listener) {
		mListener = listener;
		mNative.setLiveStateChangeListener(listener);
		if (null != videoPusher) {
			videoPusher.setLiveStateChangeListener(listener);
		}
		if (null != audioPusher) {
			audioPusher.setLiveStateChangeListener(listener);
		}

	}

}
