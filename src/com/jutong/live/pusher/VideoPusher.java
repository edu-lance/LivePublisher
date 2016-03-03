package com.jutong.live.pusher;

import java.util.Iterator;
import java.util.List;

import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;

import com.jutong.live.jni.PusherNative;
import com.jutong.live.param.VideoParam;

public class VideoPusher extends Pusher implements Callback, PreviewCallback {

	private final static String TAG = "VideoPusher";
	private boolean mPreviewRunning;
	private Camera mCamera;
	private SurfaceHolder mHolder;
	private VideoParam mParam;
	private byte[] buffer;

	public VideoPusher(SurfaceHolder surfaceHolder, VideoParam param,
			PusherNative pusherNative) {
		super(pusherNative);
		mParam = param;
		mHolder = surfaceHolder;
		surfaceHolder.addCallback(this);
	}

	@Override
	public void startPusher() {
		mNative.setVideoOptions(mParam.getWidth(), mParam.getHeight(),
				mParam.getBitrate(), mParam.getFps());
		startPreview();
		mPusherRuning = true;
	}

	@Override
	public void stopPusher() {
		mPusherRuning = false;
	}

	@Override
	public void release() {
		mPusherRuning = false;
		stopPreview();
	}

	public void switchCamera() {
		if (mParam.getCameraId() == CameraInfo.CAMERA_FACING_BACK) {
			mParam.setCameraId(CameraInfo.CAMERA_FACING_FRONT);
		} else {
			mParam.setCameraId(CameraInfo.CAMERA_FACING_BACK);
		}
		stopPreview();
		startPreview();
	}

	private void stopPreview() {
		if (mPreviewRunning && mCamera != null) {
			mCamera.setPreviewCallback(null);
			mCamera.stopPreview();
			mCamera.release();
			mCamera = null;
			mPreviewRunning = false;
		}
	}

	private void startPreview() {
		if (mPreviewRunning) {
			return;
		}
		try {
			mCamera = Camera.open(mParam.getCameraId());
			Camera.Parameters parameters = mCamera.getParameters();
			parameters.setPreviewFormat(ImageFormat.NV21);
			List<Size> supportedPreviewSizes = parameters
					.getSupportedPreviewSizes();
			Size size = supportedPreviewSizes.get(0);
			Log.d(TAG, "支持 " + size.width + "x" + size.height);
			int m = Math.abs(size.height * size.width - mParam.getHeight()
					* mParam.getWidth());
			supportedPreviewSizes.remove(0);
			Iterator<Size> iterator = supportedPreviewSizes.iterator();
			while (iterator.hasNext()) {
				Size next = iterator.next();
				Log.d(TAG, "支持 " + next.width + "x" + next.height);
				int n = Math.abs(next.height * next.width - mParam.getHeight()
						* mParam.getWidth());
				if (n < m) {
					m = n;
					size = next;
				}
			}
			mParam.setHeight(size.height);
			mParam.setWidth(size.width);
			parameters.setPreviewSize(mParam.getWidth(), mParam.getHeight());
			Log.d(TAG, "预览分辨率 width:" + size.width + " height:" + size.height);
			int range[] = new int[2];
			parameters.getPreviewFpsRange(range);
			Log.d(TAG, "预览帧率 fps:" + range[0] + " - " + range[1]);
			mCamera.setParameters(parameters);
			buffer = new byte[mParam.getWidth() * mParam.getHeight() * 3 / 2];
			mCamera.addCallbackBuffer(buffer);
			mCamera.setPreviewCallbackWithBuffer(this);
//			mCamera.setPreviewCallback(this);
			mCamera.setPreviewDisplay(mHolder);
			mCamera.startPreview();
			mPreviewRunning = true;
		} catch (Exception ex) {
			ex.printStackTrace();
			if (null != mListener) {
				mListener.onErrorPusher(-100);
			}
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {

	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		mHolder = holder;
		stopPreview();
		startPreview();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		stopPreview();
	}

	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		if (mPusherRuning) {
			mNative.fireVideo(data);
		}
		camera.addCallbackBuffer(buffer);
	}

}
