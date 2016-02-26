# LivePublisher
Android RTMP推流程序。

使用Camera+AudioRecord获取原始音视频数据,进行x264+faac对进行实时编码,然后通过librtmp上传至流媒体服务器。  
待加入MediaCodec编码。  

创建推流器:
```java  
    //分辨率:480x320 fps:25 码率:480k 音频采样率:44.1k 声道数:1 默认摄像头:后置
    //如果设备不支持这些参数会自动对参数进行调整，满足设备的支持。
    LivePusher livePusher = new LivePusher(480, 320, 480000, 25, 44100, 1,CameraInfo.CAMERA_FACING_BACK); 
	livePusher.setLiveStateChangeListener(this);  
	livePusher.prepare(mSurfaceHolder);
```
开始推流
```java
    livePusher.startPusher("rtmp://ip:port/hub/stream");
```
结束推流
```java
    livePusher.stopPusher();
```
释放
```java
    livePusher.release();
```
设置状态监听
```java
	public interface LiveStateChangeListener {
	// 错误信息 非主线程调用
	public void onErrorPusher(int code);
	// 开始推流 非主线程调用
	public void onStartPusher();
	// 停止推流 非主线程调用
	public void onStopPusher();
}
```
获取错误信息:
```java
   	switch (code) {
			case -100:
				Toast.makeText(MainActivity.this, "视频预览开始失败", 0).show();
				livePusher.stopPusher();
				break;
			case -101:
				Toast.makeText(MainActivity.this, "音频录制失败", 0).show();
				livePusher.stopPusher();
				break;
			case -102:
				Toast.makeText(MainActivity.this, "音频编码器配置失败", 0).show();
				livePusher.stopPusher();
				break;
			case -103:
				Toast.makeText(MainActivity.this, "视频频编码器配置失败", 0).show();
				livePusher.stopPusher();
				break;
			case -104:
				Toast.makeText(MainActivity.this, "流媒体服务器/网络等问题", 0).show();
				livePusher.stopPusher();
				break;
			}
	}
```
