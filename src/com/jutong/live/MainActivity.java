package com.jutong.live;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PictureCallback;
import android.media.FaceDetector;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.example.myrtmp.R;

public class MainActivity extends FragmentActivity implements OnClickListener,
		Callback, LiveStateChangeListener {

	private Button button01;
	private SurfaceView mSurfaceView;
	private SurfaceHolder mSurfaceHolder;
	private boolean isStart;
	private LivePusher livePusher;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		button01 = (Button) findViewById(R.id.button_first);
		button01.setOnClickListener(this);
		findViewById(R.id.button_take).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				livePusher.switchCamera();
			}
		});
		mSurfaceView = (SurfaceView) this.findViewById(R.id.surface);
		mSurfaceHolder = mSurfaceView.getHolder();
		mSurfaceHolder.addCallback(this);
		livePusher = new LivePusher(480, 320, 480000, 25, 44100, 1,
				CameraInfo.CAMERA_FACING_BACK);
		livePusher.setLiveStateChangeListener(this);
		livePusher.prepare(mSurfaceHolder);

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		livePusher.relase();
	}

	@Override
	public void onClick(View v) {
		if (isStart) {
			button01.setText("推流");
			isStart = false;
			livePusher.stopPusher();
		} else {
			button01.setText("停止");
			isStart = true;
			livePusher.startPusher("rtmp://xxxxx/xxxx/xxxxx");

		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		System.out.println("MAIN: CREATE");
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		System.out.println("MAIN: CHANGE");
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		System.out.println("MAIN: DESTORY");
	}

	@Override
	public void onStateChange(int code) {
		System.out.println("state:" + code);
	}
}
