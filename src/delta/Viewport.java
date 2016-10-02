package delta;

import java.util.ArrayList;

import org.jbox2d.common.Vec2;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.opengl.PShader;

public class Viewport {
	PApplet pA;
	PGraphics pg;
	Vec2 pos, dim, cam, half, track, damp, f_damp, separation, separation_smooth, mm_track;
	World2D world;
	ArrayList<PlayerInput> inputs;
	ArrayList<Player> target;
	PShader filter;
	boolean hasShader = false;
	float fade = 0;
	float fadeTarget = 1;
	boolean paused = false;
	public int trackMode = 0;

	Viewport(PApplet _pA, World2D _world, int _x, int _y, int _w, int _h) {
		pA = _pA;
		world = _world;
		pos = new Vec2(_x, _y);
		dim = new Vec2(_w, _h);
		half = new Vec2(_w / 2, _h / 2);
		separation = new Vec2(0, 0);
		separation_smooth = new Vec2(0, 0);
		pg = pA.createGraphics(_w, _h, PConstants.P2D);
		cam = new Vec2(0, 0);
		damp = new Vec2(0, 0);
		f_damp = new Vec2(0, 0);
		mm_track=new Vec2(0,0);
		target = new ArrayList<Player>();
		inputs = new ArrayList<PlayerInput>();
		track = new Vec2(0, 0);
		fade = 0;
		trackMode = 0;
	}

	public void setTrackMode(int _mode) {
		if (_mode == 0) {
			trackMode = 0;
		} else {
			trackMode = 1;
		}
	}

	void update() {
		// get average of target players
		float count = 0;
		float xTotal = 0;
		float yTotal = 0;
		if (trackMode == 1) {// micro machine style. Only track the leaders
			float highestRegion = -1;
			for (Player p : target) {
				if (!p.finished) {
					if (p.regionSmooth > highestRegion)
						highestRegion = p.regionSmooth;
				}
			}
			for (Player p : target) {
				if (!p.finished) {
					if (p.regionSmooth > highestRegion-2) {
						float factor=p.regionSmooth - (highestRegion-2);
						count+=factor;
						xTotal += p.v[0].x*factor;
						yTotal += p.v[0].y*factor;
					}
				}
			}

		} else { //track the average of all players attached to viewport
			for (Player p : target) {
				if (!p.finished) {
					count++;
					xTotal += p.v[0].x;
					yTotal += p.v[0].y;
				}
			}
		}
		if (count > 0) {			
			track.set(xTotal / count, yTotal / count);			
			track.x = -track.x + half.x;
			track.y = -track.y + half.y;
			// camera tracking. 3 steps to give it a more natural look
			// damp track (follow target)
			damp.x += (track.x - damp.x) / 15f;
			damp.y += (track.y - damp.y) / 15f;
		
			// flip damp track over target (track ahead)
			f_damp.x = track.x + (track.x - damp.x);
			f_damp.y = track.y + (track.y - damp.y);
			// damp track again (smooth out movement)
			cam.x += (f_damp.x - cam.x) / 10f;
			cam.y += (f_damp.y - cam.y) / 10f;
			

		} else {
			track.set(world.score.v[0]);
			track.x = -track.x + half.x;
			track.y = -track.y + half.y;
			cam.x += (f_damp.x - cam.x) / 20f;
			cam.y += (f_damp.y - cam.y) / 20f;

		}

		pg.beginDraw();
		world.map.draw(pg, cam, dim, this);
		if (hasShader) {
			fade += (fadeTarget - fade) / 8f;
			separation_smooth.x += (separation.x - separation_smooth.x) / 5f;
			separation_smooth.y += (separation.y - separation_smooth.y) / 5f;
			filter.set("magX", separation_smooth.x + 0.8f);
			filter.set("magY", separation_smooth.y);
			filter.set("fade", fade);
			pg.filter(filter);

			separation.x *= 0.86f;
			separation.y *= 0.86f;
		}
		if (paused) {
			pg.textFont(world.font, 44);
			pg.textAlign(PConstants.CENTER, PConstants.CENTER);
			pg.fill(255);
			pg.text("paused", dim.x / 2f, dim.y / 2f);
		}

		pg.endDraw();

	}

	public void setFade(float _f) {
		fadeTarget = _f;
	}

	public void setPause(boolean isPaused) {
		paused = isPaused;
		if (paused) {
			setFade(0.2f);
		} else {
			setFade(1f);
		}
	}

	public void attachTarget(Player _target) {
		target.add(_target);
		_target.attachViewport(this);
		inputs.add(_target.getInput());
	}

	public void attachTarget(ArrayList<Player> _list) {
		for (Player player : _list) {
			attachTarget(player);
		}
	}

	public void loadShader(PShader _glsl) {
		filter = _glsl;
		hasShader = true;		
	}

	public void setColorHit(Vec2 _sep) {
		separation = _sep;
	}

}
