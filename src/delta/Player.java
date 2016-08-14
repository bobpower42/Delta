package delta;

import java.util.ArrayList;

import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.common.Transform;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.contacts.Contact;

import beads.AudioContext;
import beads.Plug;
import processing.core.PApplet;
import processing.core.PGraphics;


public class Player extends Container {
	World2D world;
	PlayerInput input;
	Body ship, proxy;
	RayCastClosestCallback rocketCallback;
	float r = 14;
	ArrayList<Contact> boost;
	ArrayList<Contact> kill;
	ArrayList<Contact> hit;
	float killFactor = 1.5f;
	float heal = 0.015f;
	float killFactorMax = 1.8f;
	public int index;
	public int totalContacts = 1;
	private Vec2 vel, oldVel;
	private int vibCounter = 0;
	private boolean vib = false;
	float oldmag = 0;
	Vec2 loc, oldLoc;
	float curA, oldCurA;
	AudioContext ac;
	Plug out;
	PlayerSound sound;
	

	Player(World2D _world, PlayerInput _input, int _index) {
		index = _index;
		v = new Vec2[1];
		v[0] = new Vec2(0, 0);
		boost = new ArrayList<Contact>();
		kill = new ArrayList<Contact>();
		hit = new ArrayList<Contact>();
		world = _world;
		input = _input;
		type = "player";
		world.addPlayer(this);
		vel = new Vec2(0, 0);
		oldVel = new Vec2(0, 0);
		oldLoc = new Vec2(0, 0);
		oldCurA = 0;
	}

	public void createShip() {
		BodyDef bd = new BodyDef();
		bd.type = BodyType.DYNAMIC;
		//Vec2 sp = world.coordPixelsToWorld(world.spawn.v[0].x, world.spawn.v[0].y);
		// System.out.println("Sx: " + sp.x + " Sy: " + sp.y);
		bd.position.set(world.coordPixelsToWorld(world.spawn.v[0].x, world.spawn.v[0].y));
		bd.isBullet();

		BodyDef bdp = new BodyDef();
		bdp.type = BodyType.KINEMATIC;
		bdp.position.set(world.coordPixelsToWorld(world.spawn.v[0].x, world.spawn.v[0].y));

		CircleShape cs = new CircleShape();
		cs.m_radius = world.scalarPixelsToWorld(r);

		FixtureDef fd = new FixtureDef();
		fd.shape = cs;
		fd.density = 1;
		fd.friction = 0.5f;
		fd.restitution = 0.02f;
		fd.setUserData(this);

		// Attach fixture to body
		ship = world.world.createBody(bd);
		ship.createFixture(fd);
		// ship.setAngularVelocity(10.1f);

		proxy = world.particles.createBody(bdp);
		proxy.createFixture(fd);

		rocketCallback = new RayCastClosestCallback();
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
		kill.add(_contact);
	}

	public void addHitContact(Contact _contact) {
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
		if (ship != null) {
			// compare the change in velocity since previous frame
			vel = ship.getLinearVelocity();
			vel.sub(oldVel);
			oldVel = ship.getLinearVelocity();
			// compare the magnitude of that change against that magnitude in
			// the previous frame
			float mag = vel.length();
			float md = PApplet.abs(mag - oldmag);
			oldmag = mag;
			// if over a threshold of 1
			if (md > 1) {
				// clamp to 10
				if (md > 10)
					md = 10;
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
				if (vibCounter > 5) {
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
			// larger the multiplier the faster it gets there
			ship.setAngularVelocity(ang * 15);

			// if not in contact with hi-voltage rail heal engines
			if (kill.size() == 0) {
				if (killFactor < killFactorMax)
					killFactor += heal;
				else
					killFactor = killFactorMax;
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
			sound.setVol((float) killFactor / killFactorMax);
			// Generate smoke particles
			loc = ship.getWorldCenter();
			if (pwr > 0) {
				for (int i = 0; i < (int) ((killFactor / killFactorMax) * 2f); i++) {
					float rand = -0.1f + 0.2f * (float) Math.random();
					float randInter = ((float) Math.random()) * 2.0f;
					Vec2 thisLoc = new Vec2(oldLoc.x + (loc.x - oldLoc.x) * randInter,
							oldLoc.y + (loc.y - oldLoc.y) * randInter);
					float wr = world.scalarPixelsToWorld(r);
					Vec2 thisPartLoc = new Vec2(thisLoc.x + wr * PApplet.cos(curA + rand - PApplet.HALF_PI),
							thisLoc.y + wr * PApplet.sin(curA + rand - PApplet.HALF_PI));
					world.parts.add(new SmokeParticle(world, thisPartLoc,
							new Vec2(-(killFactor * 3) * PApplet.cos(-2 * rand + curA + PApplet.HALF_PI),
									-(killFactor * 3) * PApplet.sin(-2 * rand + curA + PApplet.HALF_PI)),
							50 + killFactor * 60f * (float) Math.random(), 2 + 3f * (float) Math.random()));
				}
			}
			oldLoc.x = loc.x;
			oldLoc.y = loc.y;
			oldCurA = curA;
			// calculate velociy
			float mx = ov.x + (float) (pwr * Math.cos(curA + PApplet.HALF_PI));
			float my = ov.y + (float) (pwr * Math.sin(curA + PApplet.HALF_PI));
			ship.setLinearVelocity(new Vec2(mx, my));
			ship.setLinearDamping(0.4f);
		}
	}

	public void draw(PGraphics pG, Vec2 p1, Vec2 p2) {
		if (ship != null) {
			Vec2 pos = world.getBodyPixelCoord(ship);
			float ang = ship.getAngle();
			// System.out.println("px: "+pos.x+" py: "+pos.y+" ang: "+ang);
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

	public void finish() {
		if (ship != null) {
			input.vibrateLeft(0);
			boost.clear();
			kill.clear();
			hit.clear();
			sound.toggle(0);	
			Vec2 loc=ship.getPosition();
			world.world.destroyBody(ship);
			world.particles.destroyBody(proxy);
			for(int i=0;i<40;i++){				
				world.parts.add(new FinishParticle(world,loc,new Vec2(15f*(float)Math.random(),30f*(float)Math.random()),150+(int)(Math.random()*50),5+(int)(Math.random()*5),World2D.cl[index]));
				world.parts.add(new FinishParticle(world,loc,new Vec2(15f*(float)Math.random(),30f*(float)Math.random()),150+(int)(Math.random()*50),5+(int)(Math.random()*5),-1));
			}
			ship = null;
			proxy = null;
		}
	}

}