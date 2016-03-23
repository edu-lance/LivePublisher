package com.jutong.live.pusher;

import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.Surface;
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
	private byte[] raw;
	private Activity mActivity;
	private int screen;
	private final static int SCREEN_PORTRAIT = 0;
	private final static int SCREEN_LANDSCAPE_LEFT = 90;
	private final static int SCREEN_LANDSCAPE_RIGHT = 270;

	public VideoPusher(Activity activity, SurfaceHolder surfaceHolder,
			VideoParam param, PusherNative pusherNative) {
		super(pusherNative);
		mActivity = activity;
		mParam = param;
		mHolder = surfaceHolder;
		surfaceHolder.addCallback(this);
	}

	@Override
	public void startPusher() {
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
		mActivity = null;
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
			Camera.Parameters parameters = mCamera.getParameters();
			parameters.setPreviewFormat(ImageFormat.NV21);
			setPreviewSize(parameters);
			setPreviewFpsRange(parameters);
			setPreviewOrientation(parameters);
			mCamera.setParameters(parameters);
			buffer = new byte[mParam.getWidth() * mParam.getHeight() * 3 / 2];
			raw = new byte[mParam.getWidth() * mParam.getHeight() * 3 / 2];
			mCamera.addCallbackBuffer(buffer);
			mCamera.setPreviewCallbackWithBuffer(this);
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

	private void setPreviewSize(Camera.Parameters parameters) {
		List<Integer> supportedPreviewFormats = parameters
				.getSupportedPreviewFormats();
		for (Integer integer : supportedPreviewFormats) {
			System.out.println("支持:" + integer);
		}
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
	}

	private void setPreviewFpsRange(Camera.Parameters parameters) {
		int range[] = new int[2];
		parameters.getPreviewFpsRange(range);
		Log.d(TAG, "预览帧率 fps:" + range[0] + " - " + range[1]);
	}

	private void setPreviewOrientation(Camera.Parameters parameters) {
		CameraInfo info = new CameraInfo();
		Camera.getCameraInfo(mParam.getCameraId(), info);
		int rotation = mActivity.getWindowManager().getDefaultDisplay()
				.getRotation();
		screen = 0;
		switch (rotation) {
		case Surface.ROTATION_0:
			screen = SCREEN_PORTRAIT;
			mNative.setVideoOptions(mParam.getHeight(), mParam.getWidth(),
					mParam.getBitrate(), mParam.getFps());
			break;
		case Surface.ROTATION_90: // 横屏 左边是头部(home键在右边)
			screen = SCREEN_LANDSCAPE_LEFT;
			mNative.setVideoOptions(mParam.getWidth(), mParam.getHeight(),
					mParam.getBitrate(), mParam.getFps());
			break;
		case Surface.ROTATION_180:
			screen = 180;
			break;
		case Surface.ROTATION_270:// 横屏 头部在右边
			screen = SCREEN_LANDSCAPE_RIGHT;
			mNative.setVideoOptions(mParam.getWidth(), mParam.getHeight(),
					mParam.getBitrate(), mParam.getFps());
			break;
		}
		int result;
		if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
			result = (info.orientation + screen) % 360;
			result = (360 - result) % 360; // compensate the mirror
		} else { // back-facing
			result = (info.orientation - screen + 360) % 360;
		}
		mCamera.setDisplayOrientation(result);

		// // 如果是竖屏 设置预览旋转90度，并且由于回调帧数据也需要旋转所以宽高需要交换
		// if (mContext.getResources().getConfiguration().orientation ==
		// Configuration.ORIENTATION_PORTRAIT) {
		// mNative.setVideoOptions(mParam.getHeight(), mParam.getWidth(),
		// mParam.getBitrate(), mParam.getFps());
		// parameters.set("orientation", "portrait");
		// mCamera.setDisplayOrientation(90);
		// } else {
		// mNative.setVideoOptions(mParam.getWidth(), mParam.getHeight(),
		// mParam.getBitrate(), mParam.getFps());
		// parameters.set("orientation", "landscape");
		// mCamera.setDisplayOrientation(0);
		// }
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
			switch (screen) {
			case SCREEN_PORTRAIT:
				portraitData2Raw(data);
				break;
			case SCREEN_LANDSCAPE_LEFT:
				raw = data;
				break;
			case SCREEN_LANDSCAPE_RIGHT:
				landscapeData2Raw(data);
				break;
			}
			mNative.fireVideo(raw);
		}
		camera.addCallbackBuffer(buffer);
	}

	private void landscapeData2Raw(byte[] data) {
		int width = mParam.getWidth(), height = mParam.getHeight();
		int y_len = width * height;
		int k = 0;
		// y数据倒叙插入raw中
		for (int i = y_len - 1; i > -1; i--) {
			raw[k] = data[i];
			k++;
		}
		// System.arraycopy(data, y_len, raw, y_len, uv_len);
		// v1 u1 v2 u2
		// v3 u3 v4 u4
		// 需要转换为:
		// v4 u4 v3 u3
		// v2 u2 v1 u1
		int maxpos = data.length - 1;
		int uv_len = y_len >> 2; // 4:1:1
		for (int i = 0; i < uv_len; i++) {
			int pos = i << 1;
			raw[y_len + i * 2] = data[maxpos - pos - 1];
			raw[y_len + i * 2 + 1] = data[maxpos - pos];
		}
	}

	private void portraitData2Raw(byte[] data) {
		// if (mContext.getResources().getConfiguration().orientation !=
		// Configuration.ORIENTATION_PORTRAIT) {
		// raw = data;
		// return;
		// }
		int width = mParam.getWidth(), height = mParam.getHeight();
		int y_len = width * height;
		int uvHeight = height >> 1; // uv数据高为y数据高的一半
		int k = 0;
		if (mParam.getCameraId() == CameraInfo.CAMERA_FACING_BACK) {
			for (int j = 0; j < width; j++) {
				for (int i = height - 1; i >= 0; i--) {
					raw[k++] = data[width * i + j];
				}
			}
			for (int j = 0; j < width; j += 2) {
				for (int i = uvHeight - 1; i >= 0; i--) {
					raw[k++] = data[y_len + width * i + j];
					raw[k++] = data[y_len + width * i + j + 1];
				}
			}
		} else {
			for (int i = 0; i < width; i++) {
				int nPos = width - 1;
				for (int j = 0; j < height; j++) {
					raw[k] = data[nPos - i];
					k++;
					nPos += width;
				}
			}
			for (int i = 0; i < width; i += 2) {
				int nPos = y_len + width - 1;
				for (int j = 0; j < uvHeight; j++) {
					raw[k] = data[nPos - i - 1];
					raw[k + 1] = data[nPos - i];
					k += 2;
					nPos += width;
				}
			}
		}
	}

}
