package delta;

import beads.AudioContext;
import beads.BiquadFilter;
import beads.Buffer;
import beads.Clip;
import beads.Gain;
import beads.Glide;
import beads.Mult;
import beads.OnePoleFilter;
import beads.Plug;
import beads.TapIn;
import beads.TapOut;
import beads.WavePlayer;

public class PlayerSound {
	AudioContext ac;

	// thrusters
	WavePlayer wp;
	BiquadFilter bqpre, bqpost, hppre;
	Clip cl;
	Glide toggle, power, vol, kill;
	Plug out;
	float rocketGain = 0.1f;
	float rocketPreFilterBase = 300;
	float rocketPostFilterBase = 100;
	float rocketPostFilterMod = 600;
	float rocketClipLimit = 0.08f;

	// solid hit
	WavePlayer solid_hit;
	MetalNoise solid_hit_metal;
	Glide solid_hit_impulse;
	OnePoleFilter solid_hit_smooth;
	Clip solid_hit_clip;
	Gain solid_hit_gain;
	BiquadFilter solid_hit_LP;

	// solid friction
	MetalNoise solid_friction;
	Glide solid_speed, solid_gain;
	BiquadFilter solid_friction_LP;
	Gain solid_friction_gain;

	// bounce hit
	WavePlayer bounce_hit;
	Glide bounce_hit_impulse;
	Glide bounce_hit_frequency;
	OnePoleFilter bounce_hit_smooth;
	Clip bounce_hit_clip;
	Gain bounce_hit_gain;
	BiquadFilter bounce_hit_LP;

	PlayerSound(AudioContext _ac, Plug _out) {
		ac = _ac;
		out = _out;
		// thrusters
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

		// metal hit
		solid_hit_impulse = new Glide(ac, 0, 200);
		solid_hit_smooth = new OnePoleFilter(ac, 500);
		solid_hit_smooth.addInput(solid_hit_impulse);
		solid_hit_clip = new Clip(ac);
		solid_hit_clip.addInput(solid_hit_smooth);
		solid_hit_gain = new Gain(ac, 1, solid_hit_clip);
		solid_hit = new WavePlayer(ac, 100f, Buffer.SINE);
		solid_hit_metal = new MetalNoise(ac, 0.8f);
		solid_hit_metal.setFrequency(50f);
		solid_hit_LP = new BiquadFilter(ac, BiquadFilter.LP, 300, 1);
		//Mult solid_hit_envFilter=new Mult(ac,1,200f);
		//solid_hit_envFilter.addInput(solid_hit_smooth);
		//solid_hit_LP.setFrequency(solid_hit_envFilter);
		solid_hit_LP.addInput(solid_hit_metal);
		solid_hit_gain.addInput(solid_hit_LP);
		solid_hit_gain.addInput(solid_hit);
		out.addInput(solid_hit_gain);

		// bounce hit
		bounce_hit_impulse = new Glide(ac, 0, 300);
		bounce_hit_frequency = new Glide(ac, 0, 500);
		bounce_hit_smooth = new OnePoleFilter(ac, 500);
		bounce_hit_smooth.addInput(bounce_hit_impulse);
		bounce_hit_clip = new Clip(ac);
		bounce_hit_clip.addInput(bounce_hit_smooth);
		bounce_hit_gain = new Gain(ac, 1, bounce_hit_clip);
		bounce_hit = new WavePlayer(ac, 54f, Buffer.SQUARE);
		bounce_hit.setFrequency(bounce_hit_frequency);
		bounce_hit_LP=new BiquadFilter(ac,BiquadFilter.LP,700,1);
		
		bounce_hit_gain.addInput(bounce_hit);
		bounce_hit_LP.addInput(bounce_hit_gain);
		TapIn ti=new TapIn(ac,100f);
		ti.addInput(bounce_hit_LP);
		TapOut to=new TapOut(ac,ti,100f);
		Gain feedback=new Gain(ac,1,0.4f);
		feedback.addInput(to);
		bounce_hit_LP.addInput(feedback);
		out.addInput(bounce_hit_LP);
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
		if (val < 0)
			val = 0;
		if (val > 1)
			val = 1;
		vol.setValue(1 - (1 - val) * (1 - val));
		kill.setValue(50 + ((1 - val) * 800f));
	}

	public void addMetalImpulse(float _impulse) {
		float imp = 0;
		if (_impulse > 0.1) {
			imp = _impulse / 50f;
			imp += solid_hit_impulse.getValue();
			solid_hit_impulse.setValueImmediately(imp);
			// System.out.println("imp: "+imp);
		}
		solid_hit_impulse.setValue(0);
	}
	public void addBounceImpulse(float _impulse) {
		float imp = 0;
		if (_impulse > 0.1) {
			imp = _impulse / 50f;
			bounce_hit_impulse.setValueImmediately(imp);
			bounce_hit_frequency.setValueImmediately(0);
			bounce_hit_frequency.setValue(_impulse*100);
			// System.out.println("imp: "+imp);
		}
		bounce_hit_impulse.setValue(0);
	}

}
