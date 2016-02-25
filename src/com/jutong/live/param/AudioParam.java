package com.jutong.live.param;

public class AudioParam {
	private int sampleRate = 44100;
	private int channel = 1;

	public AudioParam(int sampleRate, int channel) {
	}

	public int getSampleRate() {
		return sampleRate;
	}

	public void setSampleRate(int sampleRate) {
		this.sampleRate = sampleRate;
	}

	public int getChannel() {
		return channel;
	}

	public void setChannel(int channel) {
		this.channel = channel;
	}
}
