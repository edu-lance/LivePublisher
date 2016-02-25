package com.jutong.live;

public interface LiveStateChangeListener {

	// -101 audiorecord开始失败
	// -201 音频编码器打开失败
	// -202 视频编码器打开失败
	// -301 rtmp初始化失败
	// -302 流媒体服务器连接失败
	// -303 流媒体服务器 流 连接失败
	// -304 流媒体服务器 中断
	// 101 音频初始化成功
	// 102 视频初始化成功
	// 103 准备完成 以视频为主 视频预览成功则回调准备完成
	// 104 开始推流成功
	// 105 停止推流
	public static final int ERROR_AUDIO_OPENED = -101;
	// public static final int ERROR_VIDEO_OPENED = -102;
	public static final int ERROR_AUDIO_CODEC_OPENED = -201;
	public static final int ERROR_VIDEO_CODEC_OPENED = -202;
	public static final int ERROR_RTMP_INIT = -301;
	public static final int ERROR_RTMP_SERVER_CONNECT_FAILED = -302;
	public static final int ERROR_RTMP_STREAM_CONNECT_FAILED = -303;
	public static final int ERROR_RTMP_SERVER_SUSPEND = -304;
	// public static final int AUDIO_INIT_SUCCESS = 101;
//	public static final int VIDEO_INIT_SUCCESS = 102;
	public static final int PREPARE_COMPLETE = 103;
	public static final int PUBLISH_START = 104;
	public static final int PUBLISH_STOP = 105;

	public void onStateChange(int code);

}
