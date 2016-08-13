package delta;

import beads.AudioContext;
import beads.BiquadFilter;
import beads.Buffer;
import beads.Clip;
import beads.Gain;
import beads.Glide;
import beads.Mult;
import beads.Plug;
import beads.WavePlayer;

public class RocketSynth {
	AudioContext ac;
	WavePlayer wp;
	BiquadFilter bqpre, bqpost, hppre;
	Clip cl;
	Glide toggle, power, vol, kill;
	Plug out;
	float rocketGain = 0.3f;
	float rocketPreFilterBase = 300;
	float rocketPostFilterBase = 100;
	float rocketPostFilterMod = 1200;
	float rocketClipLimit = 0.08f;

	RocketSynth(AudioContext _ac, Plug _out) {
		ac = _ac;
		out = _out;
		wp = new WavePlayer(ac, 0.1f, Buffer.NOISE);
		wp.setPhase((float) Math.random());
		toggle = new Glide(ac, 500);
		vol = new Glide(ac, 1000);
		toggle.setValueImmediately(0);
		vol.setValueImmediately(0);
		power = new Glide(ac, 500);
		kill = new Glide(ac, 500);
		kill.setValue(100);
		hppre = new BiquadFilter(ac, BiquadFilter.HP, kill, 1);
		bqpre = new BiquadFilter(ac, BiquadFilter.LP, rocketPreFilterBase, 1);
		bqpost = new BiquadFilter(ac, BiquadFilter.LP, power, 1);

		Mult invert = new Mult(ac, 1, -1);
		invert.addInput(toggle);
		cl = new Clip(ac);
		cl.setMaximum(toggle);
		cl.setMinimum(invert);
		Gain inGain = new Gain(ac, 1, vol);
		
		inGain.addInput(wp);
		hppre.addInput(inGain);
		bqpre.addInput(hppre);
		cl.addInput(bqpre);
		bqpost.addInput(cl);
		Gain gain = new Gain(ac, 1, rocketGain);
		gain.addInput(bqpost);
		out.addInput(gain);
		power.setValue(200f);
	}

	void setPower(float _power) {
		power.setValue(rocketPostFilterBase + (_power * rocketPostFilterMod));

	}

	void toggle(int val) {
		if (val < 0)
			val = 0;
		if (val > 1)
			val = 1;
		toggle.setValue(val * rocketClipLimit);
	}

	void setVol(float val) {
		if(val<0)val=0;
		if(val>1)val=1;
		vol.setValue(1 - (1 - val) * (1 - val));
		kill.setValue(50+((1-val) * 800f));
	}

}
