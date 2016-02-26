package com.jutong.live.pusher;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import com.jutong.live.jni.PusherNative;
import com.jutong.live.param.AudioParam;

public class AudioPusher extends Pusher {
	private AudioParam mParam;
	private int minBufferSize;
	private AudioRecord audioRecord;

	public AudioPusher(AudioParam param, PusherNative pusherNative) {
		super(pusherNative);
		mParam = param;
		minBufferSize = AudioRecord.getMinBufferSize(mParam.getSampleRate(),
				mParam.getChannel() == 1 ? AudioFormat.CHANNEL_IN_MONO
						: AudioFormat.CHANNEL_IN_STEREO,
				AudioFormat.ENCODING_PCM_16BIT);
		audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
				mParam.getSampleRate(), AudioFormat.CHANNEL_IN_MONO,
				AudioFormat.ENCODING_PCM_16BIT, minBufferSize);
	}

	@Override
	public void startPusher() {
		if (null == audioRecord) {
			return;
		}
		mNative.setAudioOptions(mParam.getSampleRate(), mParam.getChannel());
		mPusherRuning = true;
		if (audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_STOPPED) {
			try {
				audioRecord.startRecording();
				new Thread(new AudioRecordTask()).start();
			} catch (Throwable th) {
				th.printStackTrace();
				if (null != mListener) {
					mListener.onErrorPusher(-101);
				}
			}
		}
	}

	@Override
	public void stopPusher() {
		if (null == audioRecord) {
			return;
		}
		mPusherRuning = false;
		if (audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING)
			audioRecord.stop();
	}

	@Override
	public void release() {
		if (null == audioRecord) {
			return;
		}
		mPusherRuning = false;
		if (audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_STOPPED)
			audioRecord.release();
	}

	class AudioRecordTask implements Runnable {

		@Override
		public void run() {
			while (mPusherRuning) {
				byte[] buffer = new byte[2048];
				int len = audioRecord.read(buffer, 0, buffer.length);
				if (0 < len) {
					mNative.fireAudio(buffer, len);
				}
			}
		}
	}

}
