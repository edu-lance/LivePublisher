package com.jutong.live;

public interface LiveStateChangeListener {

	// 针对视频 ，准备完成
	public void onErrorPusher(int code);

	// 开始推流
	public void onStartPusher();

	// 停止推流
	public void onStopPusher();
}
