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
推流器状态回调:
```java
    public void onStateChange(int code){
        switch(code){
          //AudioRecord打开失败
          case LiveStateChangeListener.ERROR_AUDIO_OPENED:
            break;
          //音频编码器打开失败
          case LiveStateChangeListener.ERROR_AUDIO_CODEC_OPENED:
            break;
          //视频编码器打开失败
          case LiveStateChangeListener.ERROR_VIDEO_CODEC_OPENED:
            break;
          //rtmp 初始化失败
          case LiveStateChangeListener.ERROR_RTMP_INIT:
            break;
          //流媒体服务器连接失败
          case LiveStateChangeListener.ERROR_RTMP_SERVER_CONNECT_FAILED:
          case LiveStateChangeListener.ERROR_RTMP_STREAM_CONNECT_FAILED:
            break;
          //服务器连接中断
          case LiveStateChangeListener.ERROR_RTMP_SERVER_SUSPEND:
            break;
          //初始化成功。需要在该状态下 开始推流
          case LiveStateChangeListener.PREPARE_COMPLETE:
            break;
          //开始推流
          case LiveStateChangeListener.PUBLISH_START:
            break;
          //结束推流
          case LiveStateChangeListener.PUBLISH_STOP:
            break;
        }
    }
```
