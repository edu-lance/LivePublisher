package com.jutong.live.pusher;

import com.jutong.live.jni.PusherNative;

public abstract class Pusher {

	protected boolean mPusherRuning;
	protected PusherNative mNative;

	public Pusher(PusherNative pusherNative) {
		mNative = pusherNative;
	}

	public abstract void startPusher();

	public abstract void stopPusher();

	public abstract void release();
}
