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
	boolean ship_active = true;
	Glide global;
	Gain master;

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
	OnePoleFilter solid_friction_smooth;
	Mult solid_friction_mult;
	BiquadFilter solid_friction_LP;
	BiquadFilter solid_friction_HP;
	Gain solid_friction_gain;

	// bounce hit
	WavePlayer bounce_hit;
	Glide bounce_hit_impulse;
	Glide bounce_hit_frequency;
	OnePoleFilter bounce_hit_smooth;
	Clip bounce_hit_clip;
	Gain bounce_hit_gain;
	BiquadFilter bounce_hit_LP;

	// kill hit
	MetalNoise kill_hit;
	Glide kill_hit_impulse;
	OnePoleFilter kill_hit_smooth;
	Clip kill_hit_clip;
	Gain kill_hit_gain;
	BiquadFilter kill_hit_HP, kill_hit_LP;

	// finish hit (more cow bell)
	WavePlayer finish_hit_1, finish_hit_2;
	Glide finish_hit_impulse;
	OnePoleFilter finish_hit_smooth;
	Clip finish_hit_clip;
	Gain finish_hit_gain;
	BiquadFilter finish_hit_BP;

	// boost friction
	MetalNoise boost_friction;
	Glide boost_speed, boost_gain;
	OnePoleFilter boost_friction_smooth;
	BiquadFilter boost_friction_BP;
	Gain boost_friction_gain;

	PlayerSound(AudioContext _ac, Plug _out) {

		ac = _ac;
		out = _out;
		global = new Glide(ac, 0, 500);
		master = new Gain(ac, 1, global);
		global.setValue(1.0f);
		out.addInput(master);
		
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
		master.addInput(gain);
		power.setValue(200f);

		// solid hit
		solid_hit_impulse = new Glide(ac, 0, 150);
		solid_hit_smooth = new OnePoleFilter(ac, 500);
		solid_hit_smooth.addInput(solid_hit_impulse);
		solid_hit_clip = new Clip(ac);
		solid_hit_clip.addInput(solid_hit_smooth);
		solid_hit_gain = new Gain(ac, 1, solid_hit_clip);
		solid_hit = new WavePlayer(ac, 50f, Buffer.SINE);
		solid_hit_metal = new MetalNoise(ac, 0.8f);
		solid_hit_metal.setFrequency(80f);
		solid_hit_LP = new BiquadFilter(ac, BiquadFilter.LP, 400, 1);
		solid_hit_LP.addInput(solid_hit_metal);
		solid_hit_gain.addInput(solid_hit_LP);
		solid_hit_gain.addInput(solid_hit);
		master.addInput(solid_hit_gain);

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
		bounce_hit_LP = new BiquadFilter(ac, BiquadFilter.LP, 400, 1);
		bounce_hit_gain.addInput(bounce_hit);
		bounce_hit_LP.addInput(bounce_hit_gain);
		master.addInput(bounce_hit_LP);

		// kill hit
		kill_hit_impulse = new Glide(ac, 0, 200);
		kill_hit_smooth = new OnePoleFilter(ac, 500);
		kill_hit_smooth.addInput(kill_hit_impulse);
		kill_hit_clip = new Clip(ac);
		kill_hit_clip.addInput(kill_hit_smooth);
		kill_hit_gain = new Gain(ac, 1, kill_hit_clip);
		kill_hit = new MetalNoise(ac, 0.8f);
		kill_hit.setFrequency(50f);
		kill_hit_LP = new BiquadFilter(ac, BiquadFilter.LP, 4000, 1);
		kill_hit_HP = new BiquadFilter(ac, BiquadFilter.HP, 4000, 1);
		kill_hit_LP.addInput(kill_hit);
		kill_hit_HP.addInput(kill_hit_LP);
		kill_hit_gain.addInput(kill_hit_HP);
		master.addInput(kill_hit_gain);

		// finish hit
		finish_hit_impulse = new Glide(ac, 0, 400);
		finish_hit_smooth = new OnePoleFilter(ac, 500);
		finish_hit_smooth.addInput(finish_hit_impulse);
		finish_hit_clip = new Clip(ac);
		finish_hit_clip.addInput(finish_hit_smooth);
		finish_hit_gain = new Gain(ac, 1, finish_hit_clip);
		finish_hit_1 = new WavePlayer(ac, 540f, Buffer.SQUARE);
		finish_hit_2 = new WavePlayer(ac, 800f, Buffer.SQUARE);
		finish_hit_BP = new BiquadFilter(ac, BiquadFilter.BP_PEAK, 340, 1);
		finish_hit_BP.addInput(finish_hit_1);
		finish_hit_BP.addInput(finish_hit_2);
		finish_hit_gain.addInput(finish_hit_BP);
		master.addInput(finish_hit_gain);

		// solid friction

		solid_speed = new Glide(ac, 0, 1000);
		solid_gain = new Glide(ac, 0, 100);
		solid_friction_smooth = new OnePoleFilter(ac, 500);
		solid_friction_smooth.addInput(solid_gain);
		solid_friction = new MetalNoise(ac, 0.5f);
		solid_friction.setFrequency(solid_speed);
		solid_friction_LP = new BiquadFilter(ac, BiquadFilter.LP, 400, 1);
		solid_friction_HP = new BiquadFilter(ac, BiquadFilter.HP, 1800, 1);
		solid_friction_LP.addInput(solid_friction);
		solid_friction_HP.addInput(solid_friction_LP);
		solid_friction_gain = new Gain(ac, 1, solid_friction_smooth);
		solid_friction_gain.addInput(solid_friction_HP);
		master.addInput(solid_friction_gain);

		// boost friction
		boost_speed = new Glide(ac, 0, 100);
		boost_gain = new Glide(ac, 0, 100);
		boost_friction_smooth = new OnePoleFilter(ac, 500);
		boost_friction_smooth.addInput(boost_gain);
		boost_friction = new MetalNoise(ac, 0.5f);
		boost_friction.setFrequency(boost_speed);
		boost_friction_BP = new BiquadFilter(ac, BiquadFilter.BESSEL_LP, boost_speed, 1);
		boost_friction_BP.addInput(boost_friction);
		boost_friction_gain = new Gain(ac, 1, boost_friction_smooth);
		boost_friction_gain.addInput(boost_friction_BP);
		master.addInput(boost_friction_gain);

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

	public void addSolidImpulse(float _impulse) {
		float imp = 0;
		if (_impulse > 0.1 && ship_active) {
			imp = _impulse / 50f;
			imp += solid_hit_impulse.getValue();
			solid_hit_impulse.setValueImmediately(imp);
			// System.out.println("imp: "+imp);
		}
		solid_hit_impulse.setValue(0);
	}

	public void addSolidFriction(float _friction) {
		float frS = 0;
		float frA = 0;
		if (_friction > 3 && ship_active) {
			frS = _friction * 10f;
			frA = 0.2f + _friction / 20f;
			if (frA > 0.3)
				frA = 0.3f;
			solid_speed.setValueImmediately(frS);
			solid_gain.setValueImmediately(frA);
		}
		solid_speed.setValue(0);
		solid_gain.setValue(0);
	}

	public void addBounceImpulse(float _impulse) {
		float imp = 0;
		if (_impulse > 0.1 && ship_active) {
			imp = _impulse / 20f;
			bounce_hit_impulse.setValueImmediately(imp);
			bounce_hit_frequency.setValueImmediately(0);
			bounce_hit_frequency.setValue(_impulse * 100);
			// System.out.println("imp: "+imp);
		}
		bounce_hit_impulse.setValue(0);
	}

	public void addKillImpulse(float _impulse) {
		float imp = 0;
		if (ship_active) {
			imp = 0.2f + (_impulse / 20f);
			// imp += kill_hit_impulse.getValue();
			kill_hit_impulse.setValueImmediately(imp);
			// System.out.println("imp: "+imp);
		}
		kill_hit_impulse.setValue(0);
	}

	public void finishImpulse(float _impulse) {
		if (ship_active) {
			ship_active = false;
			float imp = 0.1f + (_impulse / 50f);
			finish_hit_impulse.setValueImmediately(imp);
			finish_hit_impulse.setValue(0);
		}
	}

	public void addBoostFriction(float _friction) {
		float frS = 0;
		float frA = 0;
		if (ship_active) {
			frS = _friction * 50f;
			frA = _friction / 100f;
			if (frA > 0.1)
				frA = 0.1f;
			boost_speed.setValue(frS);
			boost_gain.setValueImmediately(frA);
		}
		// boost_speed.setValue(0);
		boost_gain.setValue(0);
	}



}
