package delta;

import java.util.ArrayList;

import org.jbox2d.collision.AABB;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.common.Transform;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.contacts.Contact;
import org.jbox2d.dynamics.joints.RevoluteJoint;
import beads.AudioContext;
import beads.Plug;
import processing.core.PApplet;
import processing.core.PGraphics;

public class Player extends Container {
	World2D world;
	PlayerInput input;
	Fixture ship_fixture;
	Body ship, proxy;
	boolean finished;
	float maxVelocity;
	int collisionCount;
	public Ghost recorder;
	boolean recordGhost;
	LocRot locRot;

	RayCastClosestCallback rocketCallback;
	float r = 14;
	ArrayList<Contact> boost;
	ArrayList<Contact> kill;
	ArrayList<Contact> hit;

	ArrayList<Viewport> view;

	public float killFactor = 1.5f;
	float heal = 0.01f;
	float powerMax = 1.5f;
	public int index;
	public int totalContacts = 1;
	private Vec2 vel, oldVel;
	private int vibCounter = 0;
	private boolean vib = false;
	float oldmag = 0;
	Vec2 loc, oldLoc, magForce;
	float curA, oldCurA;
	AudioContext ac;
	Plug out;
	PlayerSound sound;
	FilterQueryCallback magQuery;
	ArrayList<Player> tethered;
	ArrayList<Tether> tethers;
	RevoluteJoint tetherJoint;

	Player(World2D _world, PlayerInput _input, int _index) {
		index = _index;
		v = new Vec2[1];
		v[0] = new Vec2(0, 0);
		boost = new ArrayList<Contact>();
		kill = new ArrayList<Contact>();
		hit = new ArrayList<Contact>();
		view = new ArrayList<Viewport>();
		tethered=new ArrayList<Player>();
		tethers=new ArrayList<Tether>();
		world = _world;
		input = _input;
		type = "player";
		world.addPlayer(this);
		vel = new Vec2(0, 0);
		oldVel = new Vec2(0, 0);
		oldLoc = new Vec2(0, 0);
		magForce = new Vec2(0, 0);
		oldCurA = 0;
		finished = false;
		maxVelocity=0;
		collisionCount=0;
	}

	public void createShip() {
		BodyDef bd = new BodyDef();
		bd.type = BodyType.DYNAMIC;
		bd.position.set(world.coordPixelsToWorld(world.spawn.v[0].x, world.spawn.v[0].y));
		bd.isBullet();

		BodyDef bdp = new BodyDef();
		bdp.type = BodyType.KINEMATIC;
		bdp.position.set(world.coordPixelsToWorld(world.spawn.v[0].x, world.spawn.v[0].y));

		CircleShape cs = new CircleShape();
		cs.m_radius = world.scalarPixelsToWorld(r);

		FixtureDef fd = new FixtureDef();
		fd.shape = cs;
		fd.density = 1.0f;
		fd.friction = 0.4f;
		fd.restitution = 0.02f;
		fd.filter.categoryBits = 1 << (index + 3);
		fd.filter.maskBits = 0x0001;
		fd.setUserData(this);

		// Attach fixture to body
		ship = world.world.createBody(bd);
		ship.createFixture(fd);
		ship_fixture = ship.getFixtureList();
		CollisionFilter cf = new CollisionFilter(ship_fixture);
		FixtureDef sensor = new FixtureDef();
		sensor.shape = cs;
		sensor.filter.maskBits = 0xFFFD;
		sensor.isSensor = true;
		sensor.setUserData(cf);
		ship.createFixture(sensor);

		proxy = world.particles.createBody(bdp);
		proxy.createFixture(fd);

		rocketCallback = new RayCastClosestCallback();
	}
	
	public PlayerInput getInput(){
		return input;	
	}
	
	public void setRecorderData(String _file, String _map){
		recorder=new Ghost(world,index,_file,_map);
	}

	void attachViewport(Viewport _view) {
		view.add(_view);
	}

	void tether(ArrayList<Player> _tethered) {
		for (Player tp : _tethered) {
			if (!(tp == this)) {
				tethered.add(tp);
			}
		}
	}

	void connectAudio(AudioContext _ac, Plug _out) {
		ac = _ac;
		out = _out;
		sound = new PlayerSound(ac, out);
	}

	public void addBoostContact(Contact _contact) {
		boost.add(_contact);
	}

	public void addKillContact(Contact _contact) {
		collisionCount++;
		kill.add(_contact);
	}

	public void addHitContact(Contact _contact) {
		collisionCount++;
		hit.add(_contact);
	}

	public void remBoostContact(Contact _contact) {
		boost.remove(_contact);
	}

	public void remKillContact(Contact _contact) {
		kill.remove(_contact);
	}

	public void remHitContact(Contact _contact) {
		hit.remove(_contact);
	}

	public void update() {
		if (!finished) {
			// compare the change in velocity since previous frame
			vel = ship.getLinearVelocity();
			if(vel.length()>maxVelocity)maxVelocity=vel.length();
			vel.sub(oldVel);
			oldVel = ship.getLinearVelocity();
			// compare the magnitude of that change against that magnitude in
			// the previous frame
			float mag = vel.length();
			float md = PApplet.abs(mag - oldmag);
			oldmag = mag;
			// if over a threshold of 1
			if (md > 3) {
				// clamp to 10
				if (md > 10)
					md = 10;
				for (Viewport vp : view) {
					vp.setColorHit(vel.mul(2f));
				}
				// only send message if not already vibrating (don't overload)
				if (!vib) {
					input.vibrateLeft(md / 10f);
				}
				// reset counter
				vibCounter = 0;
				vib = true;
			}
			// if vibrating count down a few frames and then stop
			if (vib) {
				vibCounter++;
				if (vibCounter > 7) {
					input.vibrateLeft(0);
					vib = false;
				}
			}

			Transform t = ship.getTransform();
			// this value is read by the viewport for tracking
			v[0] = world.coordWorldToPixels(ship.getPosition());
			// move the proxy to match (for particle collision)
			proxy.setTransform(t.p, t.q.getAngle());

			// get ship velocity and nearest surface behind ship
			Vec2 ov = ship.getLinearVelocity();
			Vec2 sRay = ship.getWorldPoint(new Vec2(0, -world.scalarPixelsToWorld(r)));
			Vec2 eRay = ship.getWorldPoint(new Vec2(0, -world.scalarPixelsToWorld(r + 150)));
			world.world.raycast(rocketCallback, sRay, eRay);

			// get difference between ship rotaion and thumbstick position
			float curA = ship.getAngle() * 0.15915494309189533576888376337251f;
			float padA = -input.ang / 360f;
			curA -= PApplet.floor(curA);
			while (curA < 0) {
				curA = 1 + curA;
			}
			padA -= PApplet.floor(padA);
			while (padA < 0) {
				padA = 1 + padA;
			}
			curA *= PApplet.TWO_PI;
			padA *= PApplet.TWO_PI;

			float ang = padA - curA;
			if (Math.abs(ang) > PApplet.PI) {
				if (ang < 0) {
					ang += PApplet.TWO_PI;
				} else {
					ang += -PApplet.TWO_PI;
				}
			}
			// give ship angular velocity to move it to the right angle. the
			// larger the multiplier the faster it gets there (max=frame rate)
			ship.setAngularVelocity(ang * 15);

			// if not in contact with hi-voltage rail heal engines
			if (kill.size() == 0) {
				if (killFactor < powerMax)
					killFactor += heal;
				else
					killFactor = powerMax;
			} else {
				// kill engines
				killFactor = 0;
			}

			// calculate power
			float pwr = 0;
			if (input.butA) {
				sound.toggle(1);
				if (boost.size() == 0) {
					if (rocketCallback.m_hit) {
						pwr = 0.1f + ((1 - rocketCallback.m_fraction) * (1 - rocketCallback.m_fraction)) / 2f;
						rocketCallback.m_hit = false;
					} else {
						pwr = 0.1f;
					}
				} else {
					// boost rail
					pwr = 0.9f;
				}
			} else {
				sound.toggle(0);
			}
			pwr *= killFactor;
			sound.setPower(pwr);
			sound.setVol((float) killFactor / powerMax);
			// Generate smoke particles
			loc = ship.getWorldCenter();
			if (pwr > 0) {
				for (int i = 0; i < (int) ((killFactor / powerMax) * 2f); i++) {
					float rand = -0.1f + 0.2f * (float) Math.random();
					float randInter = ((float) Math.random()) * 2.0f;
					Vec2 thisLoc = new Vec2(oldLoc.x + (loc.x - oldLoc.x) * randInter,
							oldLoc.y + (loc.y - oldLoc.y) * randInter);
					float wr = world.scalarPixelsToWorld(r);
					Vec2 thisPartLoc = new Vec2(thisLoc.x + wr * PApplet.cos(curA + rand - PApplet.HALF_PI),
							thisLoc.y + wr * PApplet.sin(curA + rand - PApplet.HALF_PI));
					if (world.parts.size() < World2D.MAXPARTICLES) {
						world.parts.add(new SmokeParticle(world, thisPartLoc,
								new Vec2(-(killFactor * 3) * PApplet.cos(-2 * rand + curA + PApplet.HALF_PI),
										-(killFactor * 3) * PApplet.sin(-2 * rand + curA + PApplet.HALF_PI)),
								50 + killFactor * 60f * (float) Math.random(), 2 + 3f * (float) Math.random()));
					}
				}
			}

			// mag
			if (input.butX) {
				float radius = 500f;
				float maxVel = 3f;
				float worldRad = world.scalarPixelsToWorld(radius);
				AABB bounds = new AABB(new Vec2(loc.x - worldRad, loc.y - worldRad),
						new Vec2(loc.x + worldRad, loc.y + worldRad));
				magQuery = new FilterQueryCallback();
				magQuery.setFilter("mag");
				world.world.queryAABB(magQuery, bounds);
				float minRad = worldRad;
				boolean found = false;
				Body thisMag = null;
				for (Fixture f : magQuery.found) {
					Body b = f.getBody();
					Vec2 diff = b.getWorldCenter().sub(loc);
					if (diff.length() < minRad) {
						minRad = diff.length();
						found = true;
						thisMag = b;
					}

				}
				if (found) {
					Vec2 diff = thisMag.getWorldCenter().sub(loc);
					float force = diff.length();
					force = (worldRad - force) / worldRad;
					force *= maxVel;
					diff.normalize();
					magForce.set(diff.x * force, diff.y * force);
				}

			} else {
				magForce.set(0, 0);
			}

			oldLoc.x = loc.x;
			oldLoc.y = loc.y;
			oldCurA = curA;
			// calculate velociy
			float mx = ov.x + (float) (pwr * Math.cos(curA + PApplet.HALF_PI)) + magForce.x;
			float my = ov.y + (float) (pwr * Math.sin(curA + PApplet.HALF_PI)) + magForce.y;
			ship.setLinearVelocity(new Vec2(mx, my));
			ship.setLinearDamping(0.4f);
			locRot=getLocRot();
		}
	}
	public void recordFrame(int _frame){
		recorder.add(_frame, locRot);
	}

	public void draw(PGraphics pG, Vec2 p1, Vec2 p2, Viewport vp) {
		if (!finished) {
			Vec2 pos = world.getBodyPixelCoord(ship);
			float ang = ship.getAngle();
			pG.pushMatrix();
			pG.translate(pos.x, pos.y);
			pG.rotate(-ang);
			pG.noStroke();
			pG.fill(255);
			pG.arc(0, 0, 2 * r, 2 * r, 0, 3.14f);
			pG.fill(World2D.cl[index]);
			pG.arc(0, 0, 2 * r, 2 * r, -3.14f, 0);
			pG.popMatrix();
		}
	}
	public LocRot getLocRot(){
		Vec2 loc = world.getBodyPixelCoord(ship);
		float rot = -ship.getAngle();
		return new LocRot(loc,rot);		
	}
	
	public void setFinishTime(int _frames, float _time){
		recorder.finish(_frames, _time);
		for(Player p:tethered){
			p.recorder.finish(_frames, _time);
		}
	}

	public void finish() {
		if (!finished) {
			input.vibrateLeft(0);
			boost.clear();
			kill.clear();
			hit.clear();
			sound.toggle(0);
			Vec2 loc = ship.getPosition();
			world.world.destroyBody(ship);
			ship.setActive(false);
			world.particles.destroyBody(proxy);
			proxy.setActive(false);
			for (int i = 0; i < 40; i++) {
				if (world.parts.size() < World2D.MAXPARTICLES) {
					world.parts.add(new FinishParticle(world, loc,
							new Vec2(15f * (float) Math.random(), 30f * (float) Math.random()),
							150 + (int) (Math.random() * 50), 5 + (int) (Math.random() * 5), World2D.cl[index]));
					world.parts.add(new FinishParticle(world, loc,
							new Vec2(15f * (float) Math.random(), 30f * (float) Math.random()),
							150 + (int) (Math.random() * 50), 5 + (int) (Math.random() * 5), -1));
				}
			}
			finished = true;			
			for(Tether t:tethers){
				world.tethersRemove.add(t);				
			}			
		}
	}

}
