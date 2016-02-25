package com.jutong.live.pusher;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import com.jutong.live.LiveStateChangeListener;
import com.jutong.live.jni.PusherNative;
import com.jutong.live.param.AudioParam;

public class AudioPusher extends Pusher {
	private AudioParam mParam;

	public AudioPusher(AudioParam param, PusherNative pusherNative) {
		super(pusherNative);
		mParam = param;
	}

	@Override
	public void startPusher() {
		mNative.setAudioOptions(mParam.getSampleRate(), mParam.getChannel());
		mPusherRuning = true;
		new Thread(new AudioRecordTask()).start();
	}

	@Override
	public void stopPusher() {
		mPusherRuning = false;
	}

	@Override
	public void release() {
		mPusherRuning = false;
	}

	class AudioRecordTask implements Runnable {
		private int minBufferSize;

		public AudioRecordTask() {
			minBufferSize = AudioRecord.getMinBufferSize(
					mParam.getSampleRate(),
					mParam.getChannel() == 1 ? AudioFormat.CHANNEL_IN_MONO
							: AudioFormat.CHANNEL_IN_STEREO,
					AudioFormat.ENCODING_PCM_16BIT);
		}

		@Override
		public void run() {
			AudioRecord audioRecord = new AudioRecord(
					MediaRecorder.AudioSource.MIC, mParam.getSampleRate(),
					AudioFormat.CHANNEL_IN_MONO,
					AudioFormat.ENCODING_PCM_16BIT, minBufferSize);
			try {
				audioRecord.startRecording();
			} catch (Exception e) {
				e.printStackTrace();
				// 抛出错误
				audioRecord.release();
				mNative.postMessage(LiveStateChangeListener.ERROR_AUDIO_OPENED);
				return;
			}
			while (mPusherRuning) {
				byte[] buffer = new byte[2048];
				int len = audioRecord.read(buffer, 0, buffer.length);
				if (0 < len) {
					mNative.fireAudio(buffer, len);
				}
			}
			audioRecord.stop();
			audioRecord.release();
		}
	}

}
