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
	Vec2 pos, dim, cam, half, track, damp, f_damp, separation, separation_smooth;
	World2D world;
	ArrayList<Player> target;
	PShader filter;
	boolean hasShader = false;

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
		target = new ArrayList<Player>();
		track = new Vec2(0, 0);
		pg.beginDraw();
		// pg.noSmooth();

		pg.endDraw();
	}

	void update() {
		// get average of target players
		float count = 0;
		float xTotal = 0;
		float yTotal = 0;
		for (Player p : target) {
			if (!p.finished) {
				count++;
				xTotal += p.v[0].x;
				yTotal += p.v[0].y;
			}
		}
		if (count > 0) {
			track.set(xTotal / count, yTotal / count);
			// offset to top left corner

		} else {
			track.set(world.score.v[0]);
		}
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

		pg.beginDraw();
		world.map.draw(pg, cam, dim, this);
		if (hasShader) {
			separation_smooth.x += (separation.x - separation_smooth.x) / 5f;
			separation_smooth.y += (separation.y - separation_smooth.y) / 5f;
			filter.set("magX", separation_smooth.x + 0.8f);
			filter.set("magY", separation_smooth.y);
			pg.filter(filter);
			pg.endDraw();
			separation.x *= 0.86f;
			separation.y *= 0.86f;
		} else {
			pg.endDraw();
		}

	}

	public void attachTarget(Player _target) {
		target.add(_target);
		_target.attachViewport(this);
	}

	public void attachTarget(ArrayList<Player> _list) {
		for (Player player : _list) {
			target.add(player);
			player.attachViewport(this);
		}
	}

	public void loadShader(PShader _glsl) {
		filter = _glsl;
		hasShader = true;
		filter.set("scanlinesNum", dim.y * 1.5f);
	}

	public void setColorHit(Vec2 _sep) {
		separation = _sep;
	}

}
