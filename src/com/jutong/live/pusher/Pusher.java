package com.jutong.live.pusher;

import com.jutong.live.LiveStateChangeListener;
import com.jutong.live.jni.PusherNative;

public abstract class Pusher {

	protected boolean mPusherRuning;
	protected PusherNative mNative;
	protected LiveStateChangeListener mListener;

	public Pusher(PusherNative pusherNative) {
		mNative = pusherNative;
	}

	public void setLiveStateChangeListener(LiveStateChangeListener listener) {
		mListener = listener;
	}

	public abstract void startPusher();

	public abstract void stopPusher();

	public abstract void release();
}
