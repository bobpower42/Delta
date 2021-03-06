package delta;

import java.util.ArrayList;

import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.FixtureDef;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.data.XML;

public abstract class Container {
	public String type = "";
	ArrayList<Container> shapes;
	Vec2[] v;
	public float d = 0;
	int fill;
	public String data = "";
	boolean closed = false;
	Container parent;
	int nv;
	World2D world;
	boolean check = true;

	Container() {
		//
	}

	public int getFill() {
		return fill;
	}

	public void setData(String _data) {
		data = _data;
	}

	public String getData() {
		return data;
	}

	public String getType() {
		return type;
	}

	public Container getFromXML(XML _xml) {
		Container out = null;
		String t = _xml.getString("type");
		if (t.equals("poly")) {
			out = new Poly(world, _xml);
		} else if (t.equals("circle")) {
			out = new Circle(world, _xml);
		} else if (t.equals("marker")) {
			out = new Marker(world, _xml);
		} else if (t.equals("instance")) {
			out = new Instance(world, _xml);
		} else if (t.equals("layer")) {
			out = new Layer(world, _xml);
		} else if (t.equals("map")) {
			out = new Level(world, _xml);
		} else if (t.equals("object")) {
			out = new Object(world, _xml);
		}
		return out;
	}

	public ArrayList<Container> getShapes() {
		return shapes;
	}

	boolean onScreen(Vec2 tl, Vec2 br) {
		return true;
	}

	public void draw(PGraphics pG, Vec2 p1, Vec2 p2, Viewport vp) {

	}

	public void noCheck() {
		check = false;
	}
}

class Poly extends Container {
	Vec2 BBtl, BBbr;
	int region,nextRegion;

	Poly(World2D _world, XML xml) {
		world = _world;
		type = "poly";
		nv = 0;
		region=-1;
		try {
			if (xml.hasAttribute("data"))
				data = xml.getString("data");
			if (xml.hasAttribute("cl"))
				fill = xml.getInt("cl");
			if (xml.hasAttribute("closed"))
				closed = true;
			XML[] verts = xml.getChildren("vert");
			v = new Vec2[verts.length];
			for (XML vert : verts) {
				v[nv] = new Vec2(vert.getFloat("x"), vert.getFloat("y"));
				nv++;
			}
			getBoundingBox();
		} catch (Exception ex) {
			// println("Parse Error Load Poly");
			// println(ex);
		}
		// System.out.println("Poly: "+data);
	}

	void getBoundingBox() {
		BBtl = new Vec2(v[0].x, v[0].y);
		BBbr = new Vec2(v[0].x, v[0].y);
		for (int i = 1; i < v.length; i++) {
			if (v[i].x < BBtl.x)
				BBtl.x = v[i].x;
			if (v[i].x > BBbr.x)
				BBbr.x = v[i].x;
			if (v[i].y < BBtl.y)
				BBtl.y = v[i].y;
			if (v[i].y > BBbr.y)
				BBbr.y = v[i].y;
		}
	}

	boolean onScreen(Vec2 tl, Vec2 br) {
		if (BBtl.x < br.x && BBtl.y < br.y && BBbr.x > tl.x && BBbr.y > tl.y)
			return true;
		return false;
	}

	public void draw(PGraphics pG, Vec2 p1, Vec2 p2, Viewport vp) {
		if (!check || onScreen(p1, p2)) {
			if(region==-1){
			pG.fill(fill);
			pG.noStroke();
			}else{
				pG.noFill();
				pG.strokeWeight(0.5f);
				pG.stroke(0);
			}
			pG.beginShape();
			for (int i = 0; i < v.length; i++) {
				pG.vertex(v[i].x, v[i].y);
			}
			if (!closed) {
				pG.endShape();
			} else {
				pG.endShape(PConstants.CLOSE);
			}

		}

	}

	public FixtureDef getFixtureDef(Boolean relative) {
		PolygonShape sd = new PolygonShape();
		if (relative) {
			sd.set(world.coordPixelsToWorld(v), v.length);
		} else {
			Vec2[] t = new Vec2[v.length];
			t[0] = new Vec2(0, 0);
			// System.out.println(v.length);
			for (int i = 1; i < v.length; i++) {
				t[i] = new Vec2(v[i].x - v[0].x, v[i].y - v[0].y);
			}
			sd.set(world.coordPixelsToWorld(t), v.length);
		}
		FixtureDef fd = new FixtureDef();
		fd.shape = sd;
		fd.density = 1;
		fd.setUserData(this);
		if (data.equals("solid")) {
			fd.friction = 1.8f;
			fd.restitution = 0.2f;
		} else if (data.equals("boost")) {
			fd.friction = 0.05f;
			fd.restitution = 0.1f;
		} else if (data.equals("bounce")) {
			fd.friction = 0.9f;
			fd.restitution = 2.0f;
		} else if (data.equals("kill")) {
			fd.friction = 0.9f;
			fd.restitution = 0.8f;
		} else if (data.equals("mag")) {
			fd.friction = 0.1f;
			fd.restitution = 0.1f;
		} else if (data.equals("end")) {
			fd.isSensor();
		}
		return fd;
	}

}

class Circle extends Container {
	int region,nextRegion;

	Circle(World2D _world, XML xml) {
		world = _world;
		type = "circle";
		v = new Vec2[2];
		nv = 0;
		try {
			if (xml.hasAttribute("data"))
				data = xml.getString("data");
			if (xml.hasAttribute("cl"))
				fill = xml.getInt("cl");
			XML[] verts = xml.getChildren("vert");
			for (XML vert : verts) {
				v[nv] = new Vec2(vert.getFloat("x"), vert.getFloat("y"));
				nv++;
			}
			d = (float) Math.sqrt(Math.pow(v[0].x - v[1].x, 2) + Math.pow(v[0].y - v[1].y, 2));
			closed = true;
		} catch (Exception ex) {
			// println("Parse Error Load Circle");
			// println(ex);
		}
		// System.out.println("Circle: "+data);
	}

	boolean onScreen(Vec2 tl, Vec2 br) {
		if (v[0].x >= tl.x - d && v[0].y > tl.y - d && v[0].x < br.x + d && v[0].y < br.y + d)
			return true;
		return false;
	}

	public void draw(PGraphics pG, Vec2 p1, Vec2 p2, Viewport vp) {
		if (!check || onScreen(p1, p2)) {
			pG.fill(fill);
			pG.noStroke();
			pG.ellipse(v[0].x, v[0].y, d * 2, d * 2);
		}

	}

	public FixtureDef getFixtureDef(Boolean relative) {
		CircleShape sd = new CircleShape();
		if (relative) {
			sd.m_p.set(world.coordPixelsToWorld(v[0]));
		}
		sd.m_radius = world.scalarPixelsToWorld(d);
		FixtureDef fd = new FixtureDef();
		fd.shape = sd;
		fd.density = 1;
		fd.setUserData(this);
		if (data.equals("solid")) {
			fd.friction = 1.8f;
			fd.restitution = 0.2f;
		} else if (data.equals("boost")) {
			fd.friction = 0.05f;
			fd.restitution = 0.1f;
		} else if (data.equals("bounce")) {
			fd.friction = 0.9f;
			fd.restitution = 2.0f;
		} else if (data.equals("kill")) {
			fd.friction = 0.9f;
			fd.restitution = 0.8f;
		} else if (data.equals("mag")) {
			fd.friction = 0.1f;
			fd.restitution = 0.1f;
		} else if (data.equals("end")) {
			fd.isSensor();
		}
		return fd;
	}
}

class Marker extends Container {
	Marker(World2D _world, XML xml) {
		world = _world;
		type = "marker";
		v = new Vec2[1];
		nv = 0;
		try {
			if (xml.hasAttribute("data"))
				data = xml.getString("data");
			XML[] verts = xml.getChildren("vert");
			for (XML vert : verts) {
				v[nv] = new Vec2(vert.getFloat("x"), vert.getFloat("y"));
				nv++;
			}
		} catch (Exception ex) {
			// println("Parse Error Load Marker");
			// println(ex);
		}
		// System.out.println("Marker: "+data);
	}

	boolean onScreen(Vec2 tl, Vec2 br) {
		// return false;
		if (data.equals("start")) {
			return false;
		} else if (data.equals("tl")) {
			if (v[0].x >= tl.x || v[0].y >= tl.y) {
				return true;
			}
			return false;
		} else if (data.equals("br")) {
			if (v[0].x <= br.x || v[0].y <= br.y) {
				return true;
			}
			return false;
		} else if (v[0].x + 10 >= tl.x && v[0].y + 10 > tl.y && v[0].x - 10 < br.x && v[0].y - 10 < br.y)
			return true;
		return false;
	}

	public void draw(PGraphics pG, Vec2 p1, Vec2 p2, Viewport vp) {
		if (onScreen(p1, p2)) {
			if (data != null) {
				pG.fill(world.getForeground());
				pG.noStroke();
				pG.rectMode(PConstants.CORNERS);
				if (data.equals("tl")) {
					if (v[0].x > p1.x) {
						pG.rect(p1.x, p1.y, v[0].x, p2.y);
					}
					if (v[0].y > p1.y) {
						pG.rect(v[0].x, p1.y, p2.x, v[0].y);
					}
				} else if (data.equals("br")) {
					if (v[0].x < p2.x) {
						pG.rect(v[0].x, p1.y, p2.x, p2.y);
					}
					if (v[0].y < p2.y) {
						pG.rect(p1.x, v[0].y, v[0].x, p2.y);

					}
				} else if (data.equals("score")) {
					//pG.fill(180);
					//pG.textFont(world.font,48);
					//pG.text(PApplet.nfc(world.getTime(),2), v[0].x, v[0].y);

				}
			}
		}
	}
}

class Instance extends Container {
	Object obj;
	Body body, proxy;
	Vec2 BBtl, BBbr;

	int rT, aT, abT, bT, baT, rTO, abTO, abTotal;
	float rot, rotStep;
	Vec2 locScreen, ABLocStepScreen, BALocStepScreen;
	Vec2 ABLocStepWorld, BALocStepWorld, LocStepWorld;

	Instance(World2D _world, XML xml) {

		world = _world;
		type = "instance";
		nv = 0;
		rT = 0;
		rTO = 0;
		aT = 0;
		abT = 0;
		bT = 0;
		baT = 0;
		abTO = 0;
		abTotal = 0;
		LocStepWorld = new Vec2(0, 0);

		try {
			if (xml.hasAttribute("data")) {
				data = xml.getString("data");
				obj = world.obj.get(data);
			}
			if (xml.hasAttribute("rt"))
				rT = xml.getInt("rt");
			if (xml.hasAttribute("rto"))
				rTO = xml.getInt("rto");
			if (xml.hasAttribute("at"))
				aT = xml.getInt("at");
			if (xml.hasAttribute("abt"))
				abT = xml.getInt("abt");
			if (xml.hasAttribute("bt"))
				bT = xml.getInt("bt");
			if (xml.hasAttribute("bat"))
				baT = xml.getInt("bat");
			if (xml.hasAttribute("abto"))
				abTO = xml.getInt("abto");
			abTotal = aT + abT + bT + baT;
			XML[] verts = xml.getChildren("vert");
			v = new Vec2[verts.length];
			for (XML vert : verts) {
				v[nv] = new Vec2(vert.getFloat("x"), vert.getFloat("y"));
				nv++;
			}
			locScreen = new Vec2(v[0]);

			if (rT != 0) {
				rotStep = 6.283185307f / (float) rT;
			} else {
				rotStep = 0;
			}

			// calculate bounding box

			Vec2[] objBB = new Vec2[2];
			BBtl = new Vec2();
			BBbr = new Vec2();
			if (rT != 0) {
				objBB = obj.getBoundingBoxWithRotation();
				rotStep = 6.283185307f / (float) rT;
			} else {
				objBB = obj.getBoundingBox();
				rotStep = 0;
			}
			if (nv == 1) {
				BBtl = objBB[0].add(v[0]);
				BBbr = objBB[1].add(v[0]);
				ABLocStepScreen = new Vec2(0, 0);
				BALocStepScreen = new Vec2(0, 0);
			} else {
				BBtl.set(min(objBB[0].x + v[0].x, objBB[0].x + v[1].x), min(objBB[0].y + v[0].y, objBB[0].y + v[1].y));
				BBbr.set(max(objBB[1].x + v[0].x, objBB[1].x + v[1].x), max(objBB[1].y + v[0].y, objBB[1].y + v[1].y));
				ABLocStepScreen = new Vec2((v[1].x - v[0].x) / (float) abT, (v[1].y - v[0].y) / (float) abT);
				BALocStepScreen = new Vec2((v[0].x - v[1].x) / (float) baT, (v[0].y - v[1].y) / (float) baT);
			}
			ABLocStepWorld = new Vec2(world.coordPixelsToWorld(ABLocStepScreen));
			BALocStepWorld = new Vec2(world.coordPixelsToWorld(BALocStepScreen));

		} catch (Exception ex) {
			System.out.println(ex);
		}

	}

	public void draw(PGraphics pG, Vec2 p1, Vec2 p2, Viewport vp) {
		/* Debug Bounding box */
		// pG.noFill();
		// pG.stroke(255, 255, 0);
		// pG.rectMode(PConstants.CORNERS);
		// pG.rect(BBtl.x, BBtl.y, BBbr.x, BBbr.y);

		if (onScreen(p1, p2)) {
			if (obj != null) {
				pG.pushMatrix();
				pG.translate(locScreen.x, locScreen.y);
				pG.rotate(rot);
				obj.draw(pG, p1, p2, vp);
				pG.popMatrix();
			}
		}
	}

	private float min(float a, float b) {
		if (a < b)
			return a;
		return b;
	}

	private float max(float a, float b) {
		if (a > b)
			return a;
		return b;
	}

	public void build() {
		BodyDef bd = new BodyDef();
		bd.type = BodyType.KINEMATIC;
		body = world.world.createBody(bd);
		proxy = world.particles.createBody(bd);
		for (Container c : obj.shapes) {
			if (c.type.equals("poly")) {

				Poly pc = (Poly) c;
				FixtureDef fd = pc.getFixtureDef(true);
				body.createFixture(fd);
				proxy.createFixture(fd);
			} else if (c.type.equals("circle")) {
				Circle cc = (Circle) c;
				FixtureDef fd = cc.getFixtureDef(true);
				body.createFixture(fd);
				proxy.createFixture(fd);
			}
		}
		// this sets instance to correct location, rotation, linear velocity and
		// angular velocity
		update(0);
	}

	private void setTransformAndVelocity(Vec2 _loc, float _rot, Vec2 _lin, float _ang) {
		body.setTransform(_loc, -_rot);
		proxy.setTransform(_loc, -_rot);
		body.setAngularVelocity(-_ang * world.frameRate);
		body.setLinearVelocity(new Vec2(_lin.x * world.frameRate, _lin.y * world.frameRate));
		proxy.setAngularVelocity(-_ang * world.frameRate);
		proxy.setLinearVelocity(new Vec2(_lin.x * world.frameRate, _lin.y * world.frameRate));
	}

	public void update(int frame) {
		// sync world values to screen values on frame=0, once per rotation and
		// at each change of AB state
		// otherwise just calculate screen location and rotation

		boolean sync = frame == 0;
		if (rT != 0) {
			int loop = (rTO + frame) % rT;
			rot = loop * rotStep;
			if (loop == 0)
				sync = true;
		} else {
			rot = 0;
		}
		if ((abT != 0 || baT != 0) && nv > 1) {
			int loop = (abTO + frame) % abTotal;
			if (loop == 0)
				sync = true;
			if (loop < aT) {
				locScreen.set(v[0].x, v[0].y);
				LocStepWorld.set(new Vec2(0, 0));
			} else {
				loop -= aT;
				if (loop == 0)
					sync = true;
				if (loop < abT) {
					float progress = loop / (float) abT;
					locScreen.set(v[0].x + (v[1].x - v[0].x) * progress, v[0].y + (v[1].y - v[0].y) * progress);
					LocStepWorld.set(ABLocStepWorld);
				} else {
					loop -= abT;
					if (loop == 0)
						sync = true;
					if (loop < bT) {
						locScreen.set(v[1].x, v[1].y);
						LocStepWorld.set(new Vec2(0, 0));
					} else {
						loop -= bT;
						if (loop == 0)
							sync = true;
						if (loop < baT) {
							float progress = loop / (float) baT;
							locScreen.set(v[1].x + (v[0].x - v[1].x) * progress, v[1].y + (v[0].y - v[1].y) * progress);
							LocStepWorld.set(BALocStepWorld);
						}
					}
				}
			}
		} else {
			locScreen.set(v[0].x, v[0].y);
			LocStepWorld.set(new Vec2(0, 0));
		}
		if (sync) {
			setTransformAndVelocity(world.coordPixelsToWorld(locScreen), rot, LocStepWorld, rotStep);
		}
	}

	boolean onScreen(Vec2 tl, Vec2 br) {
		if (BBtl.x < br.x && BBtl.y < br.y && BBbr.x > tl.x && BBbr.y > tl.y)
			return true;
		return false;
	}

}

class Object extends Container {
	Object(World2D _world, XML xml) {
		world = _world;
		shapes = new ArrayList<Container>();
		type = "object";
		try {
			if (xml.hasAttribute("data"))
				data = xml.getString("data");
			if (xml.hasChildren()) {
				XML[] children = xml.getChildren("container");
				for (XML child : children) {
					Container c = getFromXML(child);
					c.noCheck();
					addShape(c);
				}
			}
		} catch (Exception ex) {
			// println("Parse Error Object");
			// println(ex);
		}
		// System.out.println("Object: "+data);
	}

	void addShape(Container _shape) {
		shapes.add(_shape);
	}

	public void draw(PGraphics pG, Vec2 p1, Vec2 p2, Viewport vp) {
		for (Container s : shapes) {
			s.draw(pG, p1, p2, vp);
		}
	}

	public Vec2[] getBoundingBox() {
		Vec2[] out = new Vec2[2];
		out[0] = new Vec2(0, 0);
		out[1] = new Vec2(0, 0);
		for (Container s : shapes) {
			for (Vec2 vert : s.v) {
				if (vert.x < out[0].x)
					out[0].x = vert.x;
				if (vert.y < out[0].y)
					out[0].y = vert.y;
				if (vert.x > out[1].x)
					out[1].x = vert.x;
				if (vert.y > out[1].y)
					out[1].y = vert.y;
			}
		}
		return out;
	}

	public Vec2[] getBoundingBoxWithRotation() {
		Vec2[] out = new Vec2[2];
		out[0] = new Vec2(0, 0);
		out[1] = new Vec2(0, 0);
		float d = 0;
		for (Container s : shapes) {
			for (Vec2 vert : s.v) {
				float td = vert.length();
				if (td > d)
					d = td;
			}
		}
		out[0].set(-d, -d);
		out[1].set(d, d);
		return out;
	}
}

class Layer extends Container {
	ArrayList<Container> shapes;
	float fade = 0;
	float paralax = 0;
	int index;

	boolean disable = false;

	Layer(World2D _world, XML xml) {
		world = _world;
		shapes = new ArrayList<Container>();
		type = "layer";
		try {
			if (xml.hasAttribute("data"))
				data = xml.getString("data");
			if (xml.hasAttribute("fade"))
				fade = xml.getFloat("fade");
			if (xml.hasAttribute("paralax"))
				paralax = xml.getFloat("paralax");
			if (xml.hasAttribute("disable"))
				disable = true;
			XML[] children = xml.getChildren("container");
			for (XML child : children) {
				addShape(getFromXML(child));
			}
		} catch (Exception ex) {

		}
		// System.out.println("Layer: "+data);
	}

	void addShape(Container _shape) {
		shapes.add(_shape);
	}

	public float getParalax() {
		return paralax;
	}

	public void draw(PGraphics pG, Vec2 p1, Vec2 p2, Viewport vp) {
		if (!disable && !data.equals("overlay")) {
			Vec2 tr;
			if (paralax == 0) {
				tr = new Vec2(p1.x, p1.y);
			} else {
				tr = new Vec2(vp.half.x + (p1.x - vp.half.x) * (1 - paralax),
						vp.half.y + (p1.y - vp.half.y) * (1 - paralax));
			}

			Vec2 tl = new Vec2((int) -tr.x, (int) -tr.y);
			Vec2 br = new Vec2((int) tl.x + p2.x, (int) tl.y + p2.y);

			pG.pushMatrix();
			pG.translate((int) tr.x, (int) tr.y);
			if (data.equals("game")) {
				world.draw(pG, tr, br, vp);
			}
			for (Container s : shapes) {
				s.draw(pG, tl, br, vp);
			}

			pG.popMatrix();
		}
	}
}

class Level extends Container {
	Container[] layer;
	int bg = -1;
	int fg = -1;
	String split="HHT";

	Level(World2D _world, XML xml) {
		world = _world;
		layer = new Container[7];
		type = "map";
		if (xml.hasAttribute("data"))
			data = xml.getString("data");
		if (xml.hasAttribute("bgcl"))
			bg = xml.getInt("bgcl");
		if (xml.hasAttribute("fgcl"))
			fg = xml.getInt("fgcl");
		if (xml.hasAttribute("split"))
			split = xml.getString("split");
		XML[] child = xml.getChildren("layer");
		if (child.length == 7) {
			for (int i = 0; i < 7; i++) {
				Container newLayer = new Layer(world, child[i]);
				layer[i] = newLayer;
			}
		} else {
			// println("incorrect number of layers");
		}
		// System.out.println("Level: "+data);
	}

	Layer getLayer(int in) {
		return (Layer) layer[in];
	}

	public void draw(PGraphics pG, Vec2 p1, Vec2 p2, Viewport vp) {
		pG.background(bg);
		for (Container l : layer) {
			Layer L = (Layer) l;
			L.draw(pG, p1, p2, vp);
		}
	}
}

class Tether extends Container {
	Body body;
	float length;
	float width;
	boolean finished=false;

	Tether(World2D _world, float _length, float _width) {
		world = _world;
		type = "tether";
		data = "tether";
		length = _length;
		width = _width;
	}

	public void attachBody(Body _body) {
		body = _body;
	}

	public void draw(PGraphics pG, Vec2 p1, Vec2 p2, Viewport vp) {
		if (body != null && !finished) {
			Vec2 loc = world.coordWorldToPixels(body.getWorldCenter());
			float rot = -body.getAngle();
			pG.fill(255);
			pG.noStroke();
			pG.pushMatrix();
			pG.translate(loc.x, loc.y);
			pG.rotate(rot);
			pG.rectMode(PConstants.CORNERS);
			pG.rect(-length / 4f, -width / 2f, length / 4f, width / 2f);
			pG.popMatrix();
		}
	}
}

class CollisionFilter extends Container {
	public Fixture fixture;

	CollisionFilter(Fixture _fixture) {
		data = "sensor";
		type = "sensor";
		fixture = _fixture;
	}
}
