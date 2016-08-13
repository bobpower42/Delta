package delta;

import org.jbox2d.common.Vec2;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;
import processing.opengl.PShader;

public class Viewport {
	PApplet pA;
	
	PGraphics pg;
	Vec2 pos, dim, cam, half, track, damp, f_damp;
	World2D world;
	Container target;	

	Viewport(PApplet _pA, World2D _world, int _x, int _y, int _w, int _h) {
		pA = _pA;
		world = _world;
		pos = new Vec2(_x, _y);
		dim = new Vec2(_w, _h);
		half = new Vec2(_w / 2, _h / 2);
		pg = pA.createGraphics(_w, _h);
		
		cam = new Vec2(0, 0);
		damp = new Vec2(0, 0);
		f_damp = new Vec2(0, 0);
	}

	void update() {
		if (target != null) {
			if(target.isTarget){
			track = new Vec2(target.v[0]);
			}
			track.x=-track.x+dim.x/2f;
			track.y=-track.y+dim.y/2f;
			
			//System.out.println(track.x);
			damp.x += (track.x - damp.x) / 15f;
			damp.y += (track.y - damp.y) / 15f;
			
			f_damp.x = track.x + (track.x - damp.x);
			f_damp.y = track.y + (track.y - damp.y);
			
			cam.x += (f_damp.x - cam.x) / 10f;
			cam.y += (f_damp.y - cam.y) / 10f;
			
			//cam.x=f_damp.x;
			//cam.y=f_damp.y;
		}
		pg.beginDraw();
		world.map.draw(pg, cam, dim);
		pg.endDraw();
		

	}

	public void attachTarget(Container _target) {
		target = _target;
		target.isTarget=true;

	}	

}
