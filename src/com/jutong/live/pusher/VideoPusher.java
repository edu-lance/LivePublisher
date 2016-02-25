package com.jutong.live.pusher;

import java.util.List;

import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;

import com.jutong.live.LiveStateChangeListener;
import com.jutong.live.jni.PusherNative;
import com.jutong.live.param.VideoParam;

public class VideoPusher extends Pusher implements Callback, PreviewCallback {

	private final static String TAG = "VideoPusher";
	private boolean mPreviewRunning;
	private Camera mCamera;
	private SurfaceHolder mHolder;
	private VideoParam mParam;

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

	@SuppressWarnings("deprecation")
	private void startPreview() {
		if (mPreviewRunning) {
			return;
		}
		try {
			mCamera = Camera.open(mParam.getCameraId());
		} catch (Throwable e) {
			e.printStackTrace();
		}
		Camera.Parameters parameters = mCamera.getParameters();
		parameters.setPreviewFormat(ImageFormat.NV21);
		List<Size> supportedPreviewSizes = parameters
				.getSupportedPreviewSizes();
		boolean isSupportSize = false;
		for (Size size : supportedPreviewSizes) {
			if (mParam.getWidth() == size.width
					&& mParam.getHeight() == size.height) {
				isSupportSize = true;
				Log.d(TAG, "支持预设预览分辨率");
				break;
			}
		}
		if (isSupportSize) {
			parameters.setPreviewSize(mParam.getWidth(), mParam.getHeight());
		} else {
			Size previewSize = parameters.getPreviewSize();
			mParam.setHeight(previewSize.height);
			mParam.setWidth(previewSize.width);
			Log.d(TAG, "修改分辨率 width:" + previewSize.width + " height:"
					+ previewSize.height);
		}

		List<Integer> supportedPreviewFrameRates = parameters
				.getSupportedPreviewFrameRates();
		boolean isSupportFps = supportedPreviewFrameRates.contains(mParam
				.getFps());
		if (isSupportFps) {
			Log.d(TAG, "支持预设预览帧率");
			parameters.setPreviewFrameRate(mParam.getFps());
		} else {
			Log.d(TAG, "修改预览帧率 fps:" + parameters.getPreviewFrameRate());
			mParam.setFps(parameters.getPreviewFrameRate());
		}
		mCamera.setParameters(parameters);
		mCamera.setPreviewCallback(this);
		try {
			mCamera.setPreviewDisplay(mHolder);
			mPreviewRunning = true;
			mCamera.startPreview();
			mNative.postMessage(LiveStateChangeListener.PREPARE_COMPLETE);
		} catch (Exception ex) {
			ex.printStackTrace();
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
	}

}
