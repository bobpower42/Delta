package delta;

import beads.AudioContext;
import beads.UGen;

public class MetalNoise extends UGen {
	private double phase, phase1, phase2, phase3, phase4, phase5;
	private float freq;
	private float amp;
	private float one_over_sr;
	private UGen frequencyEnvelope;

	public MetalNoise(AudioContext ac, float _amp) {
		super(ac, 1);
		amp = _amp;
		one_over_sr = 1f / context.getSampleRate();
		phase = Math.random();
		phase1 = Math.random();
		phase2 = Math.random();
		phase3 = Math.random();
		phase4 = Math.random();
		phase5 = Math.random();
	}

	public void start() {
		super.start();
		
	}

	@Override
	public void calculateBuffer() {
		
		float[] bo = bufOut[0];
		if (frequencyEnvelope == null) {
			for (int i = 0; i < bufferSize; i++) {
				phase = (((phase + freq * one_over_sr) % 1.0f) + 1.0f) % 1.0f;
				phase1 = (((phase1 + freq * 1.342 * one_over_sr) % 1.0f) + 1.0f) % 1.0f;
				phase2 = (((phase2 + freq * 1.2312 * one_over_sr) % 1.0f) + 1.0f) % 1.0f;
				phase3 = (((phase3 + freq * 1.6532 * one_over_sr) % 1.0f) + 1.0f) % 1.0f;
				phase4 = (((phase4 + freq * 1.9523 * one_over_sr) % 1.0f) + 1.0f) % 1.0f;
				phase5 = (((phase5 + freq * 2.1372 * one_over_sr) % 1.0f) + 1.0f) % 1.0f;
				int total = (phase < 0.5 ? 1 : 0) + (phase1 < 0.5 ? 1 : 0) + (phase2 < 0.5 ? 1 : 0)
						+ (phase3 < 0.5 ? 1 : 0) + (phase4 < 0.5 ? 1 : 0) + (phase5 < 0.5 ? 1 : 0);
				bo[i] = total == 6 | total == 2 | total == 1 ? amp : -amp;
			}
		} else {
			frequencyEnvelope.update();
			for (int i = 0; i < bufferSize; i++) {
				freq = frequencyEnvelope.getValue(0, i);
				phase = (((phase + freq * one_over_sr) % 1.0f) + 1.0f) % 1.0f;
				phase1 = (((phase1 + freq * 1.342 * one_over_sr) % 1.0f) + 1.0f) % 1.0f;
				phase2 = (((phase2 + freq * 1.2312 * one_over_sr) % 1.0f) + 1.0f) % 1.0f;
				phase3 = (((phase3 + freq * 1.6532 * one_over_sr) % 1.0f) + 1.0f) % 1.0f;
				phase4 = (((phase4 + freq * 1.9523 * one_over_sr) % 1.0f) + 1.0f) % 1.0f;
				phase5 = (((phase5 + freq * 2.1372 * one_over_sr) % 1.0f) + 1.0f) % 1.0f;
				int total = (phase < 0.5 ? 1 : 0) + (phase1 < 0.5 ? 1 : 0) + (phase2 < 0.5 ? 1 : 0)
						+ (phase3 < 0.5 ? 1 : 0) + (phase4 < 0.5 ? 1 : 0) + (phase5 < 0.5 ? 1 : 0);
				bo[i] = total == 6 | total == 2 | total == 1 ? amp : -amp;
			}

		}

	}
	public void setFrequency(float _freq){
		freq=_freq;
	}

	public MetalNoise setFrequency(UGen frequencyUGen) {
		if (frequencyUGen != null) {
			this.frequencyEnvelope = frequencyUGen;
		}
		return this;
	}
}
