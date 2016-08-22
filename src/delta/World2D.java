package delta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.jbox2d.callbacks.ContactImpulse;
import org.jbox2d.callbacks.ContactListener;
import org.jbox2d.collision.Manifold;
import org.jbox2d.collision.WorldManifold;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Transform;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.Filter;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.contacts.Contact;
import org.jbox2d.dynamics.joints.RevoluteJointDef;
import beads.UGen;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PFont;
import processing.core.PGraphics;
import processing.data.XML;

public class World2D {
	public World world, particles;
	public static int[] cl = { -34048, -16740353, -65413, -8716033 };
	private float scale;
	float tetherLength, tetherWidth;
	Level map;
	Layer game;
	Map<String, Object> obj = new HashMap<String, Object>();
	Container spawn;
	Container score;
	ArrayList<Instance> kinematics;
	ArrayList<Player> players;
	ArrayList<Contact> contacts;
	ArrayList<Particle> parts;
	ArrayList<Particle> partsRemove;
	ArrayList<Particle> partsAdd;
	ArrayList<Player> doFinish;
	ArrayList<Player> doFinishLater;
	ArrayList<Tether> tethers;
	ArrayList<Tether> doDestroyTethers;
	long lastFrameTimer;
	public float frameRate;
	UGen out;
	public static int MAXPARTICLES = 400;
	public static int FRAMES;
	public boolean playerCollisions = true;
	public static int interFrame = 0;
	public static int interFrames = 5;
	public Vec2 spawnPoint;
	public PFont font;

	World2D(float _frameRate, UGen _out) {
		FRAMES = -1;
		out = _out;
		frameRate = _frameRate;
		tetherLength = 80f;
		tetherWidth = 8f;
		world = new World(new Vec2(0, -9.8f));
		particles = new World(new Vec2(0, -9.8f));
		players = new ArrayList<Player>();
		doFinish = new ArrayList<Player>();
		doFinishLater = new ArrayList<Player>();
		contacts = new ArrayList<Contact>();
		parts = new ArrayList<Particle>();
		partsRemove = new ArrayList<Particle>();
		partsAdd = new ArrayList<Particle>();
		kinematics = new ArrayList<Instance>();
		tethers = new ArrayList<Tether>();
		doDestroyTethers = new ArrayList<Tether>();
		lastFrameTimer = System.nanoTime();
		world.setContactListener(new ContactListener() {

			public void beginContact(Contact contact) {
				addContact(contact);

			}

			public void endContact(Contact contact) {
				remContact(contact);
			}

			@Override
			public void postSolve(Contact contact, ContactImpulse impulse) {
				float totalImpulse = impulse.normalImpulses[0];
				// float totalTangent = impulse.tangentImpulses[0];

				Fixture fA = contact.getFixtureA();
				Fixture fB = contact.getFixtureB();
				Container cA = (Container) fA.getUserData();
				Container cB = (Container) fB.getUserData();
				Container cP = null, cO = null;
				if (cA.type.equals("player")) {
					cP = cA;
					cO = cB;
				} else if (cB.type.equals("player")) {
					cP = cB;
					cO = cA;
				}
				if (cP != null) {
					Player tp = (Player) cP;
					if (cO.data.equals("solid") || cO.data.equals("player")) {
						tp.sound.addSolidImpulse(totalImpulse / 2f);
					} else if (cO.data.equals("bounce")) {
						tp.sound.addSolidImpulse(totalImpulse / 3f);
						tp.sound.addBounceImpulse(totalImpulse);
						WorldManifold worldManifold = new WorldManifold();
						contact.getWorldManifold(worldManifold);
						Vec2 worldPoint = worldManifold.points[0];
						generateBounceParticles(worldPoint, totalImpulse);
					} else if (cO.data.equals("kill")) {
						tp.sound.addSolidImpulse(totalImpulse / 3f);
						tp.sound.addKillImpulse(totalImpulse);
					} else if (cO.data.equals("finish")) {
						tp.sound.finishImpulse(totalImpulse);
					}
				}
			}

			@Override
			public void preSolve(Contact arg0, Manifold arg1) {

			}
		});

	}

	public float getTime() {
		return (FRAMES + ((interFrame + 1) / (float) interFrames)) / 60f;
	}

	public void loadfromXML(XML xml, String name) {
		XML oxml = xml.getChild("objects");
		XML[] children = oxml.getChildren("object");
		for (XML child : children) {
			Container thisObj = new Object(this, child);
			addObject(thisObj);
		}
		XML mxml = xml.getChild("maps");
		children = mxml.getChildren("map");
		int i = 0;
		boolean found = false;

		do {
			if (children[i].getString("data").equals(name))
				found = true;
			else
				i++;
		} while (i < children.length && !found);
		if (found) {
			map = new Level(this, children[i]);
			game = map.getLayer(5);
			buildMap();
		}
	}

	void buildMap() {
		for (Container c : game.shapes) {
			if (c.type.equals("poly")) {
				Poly pc = (Poly) c;
				FixtureDef fd = pc.getFixtureDef(false);

				BodyDef bd = new BodyDef();
				bd.type = BodyType.STATIC;
				bd.position.set(coordPixelsToWorld(c.v[0]));
				Body b = world.createBody(bd);
				b.createFixture(fd);
				Body bp = particles.createBody(bd);
				bp.createFixture(fd);
			} else if (c.type.equals("circle")) {
				Circle cc = (Circle) c;
				FixtureDef fd = cc.getFixtureDef(false);
				BodyDef bd = new BodyDef();
				bd.type = BodyType.STATIC;
				bd.position.set(coordPixelsToWorld(c.v[0]));
				Body b = world.createBody(bd);
				b.createFixture(fd);
				Body bp = particles.createBody(bd);
				bp.createFixture(fd);

			} else if (c.type.equals("marker")) {
				if (c.data.equals("start")) {
					spawn = c;
				}
				if (c.data.equals("score")) {
					score = c;
				}
			} else if (c.type.equals("instance")) {
				Instance ic = (Instance) c;
				ic.build();
				kinematics.add(ic);
			}
		}
	}

	public void addPlayer(Player p) {
		players.add(p);
	}

	public void addTether(Tether t) {
		tethers.add(t);
	}

	public void tether(ArrayList<Player> tP) {
		int num = tP.size();
		if (num > 1) {
			Player[] pList = new Player[num];
			for (int i = 0; i < num; i++) {
				pList[i] = tP.get(i); // use array instead (just easier)
				pList[i].tether(tP); // pass tether list to each player in
										// tether list
			}
			// these values are used to get the position of ships and tethers
			float ang = PConstants.TWO_PI / (float) num;
			float rad = tetherLength / (2.0f * PApplet.sin(PConstants.PI / (float) num)); // radius
																							// of
																							// circle
			float start_ang = 0;
			if (num == 3)
				start_ang = ang / 4f;
			if (num == 4)
				start_ang = ang / 2f;

			Vec2[] pos = new Vec2[num];
			for (int i = 0; i < num; i++) {
				pos[i] = new Vec2(spawn.v[0].x + rad * PApplet.cos(start_ang + i * ang),
						spawn.v[0].y + rad * PApplet.sin(start_ang + i * ang));
			}
			// move ships
			for (int i = 0; i < num; i++) {
				pList[i].ship.setTransform(coordPixelsToWorld(pos[i]), 0);
			}
			float tL = scalarPixelsToWorld(tetherLength);
			float tW = scalarPixelsToWorld(tetherWidth);

			for (int i = 0; i < num; i++) {
				if (!(num == 2 && i == 1)) { // no need to close loop if only
												// two players
					int j = (i + 1) % num;
					Vec2 tpos = new Vec2((pos[i].x + pos[j].x) / 2f, (pos[i].y + pos[j].y) / 2f);
					float tang = PApplet.atan2(pos[j].y - pos[i].y, pos[j].x - pos[i].x);
					BodyDef bd = new BodyDef();
					bd.type = BodyType.DYNAMIC;
					FixtureDef fd = new FixtureDef();
					fd.density = 0.01f;
					Tether tc = new Tether(this, tetherLength, tetherWidth);
					fd.setUserData((Container) tc);
					fd.filter.maskBits = 0x0001; // turn off collisions with
													// tether and players
					fd.filter.categoryBits = 0x0002;
					PolygonShape tShape = new PolygonShape();
					tShape.setAsBox(tL / 2f, tW / 2f);
					fd.setShape(tShape);
					Body tether = world.createBody(bd);
					tether.createFixture(fd);
					tc.attachBody(tether);
					pList[i].tethers.add(tc);
					tether.setTransform(coordPixelsToWorld(tpos), -tang);
					RevoluteJointDef revoluteJointDefA = new RevoluteJointDef();
					revoluteJointDefA.bodyA = pList[i].ship;
					revoluteJointDefA.bodyB = tether;
					revoluteJointDefA.collideConnected = false;
					revoluteJointDefA.localAnchorA.set(0, 0);
					revoluteJointDefA.localAnchorB.set(-tL / 2f, 0);
					world.createJoint(revoluteJointDefA);
					RevoluteJointDef revoluteJointDefB = new RevoluteJointDef();
					revoluteJointDefB.bodyA = pList[j].ship;
					revoluteJointDefB.bodyB = tether;
					revoluteJointDefB.collideConnected = false;
					revoluteJointDefB.localAnchorA.set(0, 0);
					revoluteJointDefB.localAnchorB.set(tL / 2f, 0);
					world.createJoint(revoluteJointDefB);
					addTether((Tether) tc);
				}
			}
			if (num == 4) { // cross braces if x4
				for (int i = 0; i < 2; i++) {
					int j = (i + 2) % num;
					Vec2 tpos = new Vec2((pos[i].x + pos[j].x) / 2f, (pos[i].y + pos[j].y) / 2f);
					float tang = PApplet.atan2(pos[j].y - pos[i].y, pos[j].x - pos[i].x);
					float tlen = PApplet.dist(pos[i].x, pos[i].y, pos[j].x, pos[j].y);
					BodyDef bd = new BodyDef();
					bd.type = BodyType.DYNAMIC;
					FixtureDef fd = new FixtureDef();
					fd.density = 0.01f;
					Tether tc = new Tether(this, tlen, tetherWidth);
					fd.setUserData((Container) tc);
					fd.filter.maskBits = 0x0001; // turn off collisions with
													// tether and players
					fd.filter.categoryBits = 0x0002;
					PolygonShape tShape = new PolygonShape();
					tShape.setAsBox(scalarPixelsToWorld(tlen) / 2f, tW / 2f);
					fd.setShape(tShape);
					Body tether = world.createBody(bd);
					tether.createFixture(fd);
					tc.attachBody(tether);
					pList[i].tethers.add(tc);
					tether.setTransform(coordPixelsToWorld(tpos), -tang);
					RevoluteJointDef revoluteJointDefA = new RevoluteJointDef();
					revoluteJointDefA.bodyA = pList[i].ship;
					revoluteJointDefA.bodyB = tether;
					revoluteJointDefA.collideConnected = false;
					revoluteJointDefA.localAnchorA.set(0, 0);
					revoluteJointDefA.localAnchorB.set(-scalarPixelsToWorld(tlen) / 2f, 0);
					world.createJoint(revoluteJointDefA);
					RevoluteJointDef revoluteJointDefB = new RevoluteJointDef();
					revoluteJointDefB.bodyA = pList[j].ship;
					revoluteJointDefB.bodyB = tether;
					revoluteJointDefB.collideConnected = false;
					revoluteJointDefB.localAnchorA.set(0, 0);
					revoluteJointDefB.localAnchorB.set(scalarPixelsToWorld(tlen) / 2f, 0);
					world.createJoint(revoluteJointDefB);
					addTether((Tether) tc);
				}
			}
		}

	}

	private void generateSparks() {
		for (Contact c : contacts) {
			Fixture fA = c.getFixtureA();
			Fixture fB = c.getFixtureB();
			Container cA = (Container) fA.getUserData();
			Container cB = (Container) fB.getUserData();
			Body bA = fA.getBody();
			Body bB = fB.getBody();
			Container cP = null;
			Container cO = null;
			if (cA.type.equals("player")) {
				cP = cA;
				cO = cB;
			} else if (cB.type.equals("player")) {
				cP = cB;
				cO = cA;
			}
			if (cP != null) {
				if (!cO.data.equals("sensor")) {
					// get the contact point in world coordinates
					WorldManifold worldManifold = new WorldManifold();
					c.getWorldManifold(worldManifold);
					Vec2 worldPoint = worldManifold.points[0];

					// find the relative speed of the fixtures at that point
					Vec2 velA = bA.getLinearVelocityFromWorldPoint(worldPoint);
					Vec2 velB = bB.getLinearVelocityFromWorldPoint(worldPoint);
					Vec2 vel = new Vec2(velA.x + velB.x, velA.y + velB.y);
					float relativeSpeed = (velA.sub(velB)).length();
					float totalFriction = fA.getFriction() * fB.getFriction();

					if (cO.data.equals("boost")) {
						if (cP != null) {
							Player tp = (Player) cP;
							tp.sound.addBoostFriction(relativeSpeed);
						}
						Vec2 loc = new Vec2(worldPoint.x - vel.x / 400f, worldPoint.y + vel.y / 400f);
						// System.out.println(relativeSpeed);
						int t = 1 + (int) (relativeSpeed / 9f);

						for (int i = 0; i < t; i++) {
							if (parts.size() < MAXPARTICLES) {
								parts.add(new BoostParticle(this, loc,
										new Vec2(relativeSpeed * ((float) Math.random() * 2f - 1),
												relativeSpeed * ((float) Math.random() * 2f - 1)),
										100 + (float) Math.random() * 100f, 10 + (float) Math.random() * 10f));

							}
						}

					} else if (cO.data.equals("kill")) {
						Vec2 loc = new Vec2(worldPoint.x - vel.x / 1000f, worldPoint.y + vel.y / 1000f);

						for (int i = 0; i < 2; i++) {
							if (parts.size() < MAXPARTICLES) {
								parts.add(new KillParticle(this, loc,
										new Vec2(25f * ((float) Math.random() * 2f - 1),
												25f * ((float) Math.random() * 2f - 1)),
										300f, 15 + (float) Math.random() * 15f));
							}
						}

					} else {
						float intensity = relativeSpeed * totalFriction;
						if (cP != null) {
							Player tp = (Player) cP;
							tp.sound.addSolidFriction(intensity);
						}
						if (intensity > 3) {
							// for (int i = 0; i < 2; i++) {

							Vec2 loc = new Vec2(worldPoint.x - vel.x / 50f, worldPoint.y + vel.y / 50f);
							if (parts.size() < MAXPARTICLES) {
								parts.add(new SparkParticle(this, loc,
										new Vec2(relativeSpeed * ((float) Math.random() * 4f - 2),
												relativeSpeed * ((float) Math.random() * 4f - 2)),
										100f, 2 + (float) Math.random() * 5f));
							}
							// }
						}
					}
				}
			}
		}
	}

	void generateBounceParticles(Vec2 p, float ti) {
		int tp = (int) (3 * ti);
		for (int i = 0; i < tp; i++) {
			if (parts.size() < MAXPARTICLES) {
				parts.add(new BounceParticle(this, p,
						new Vec2(ti * PApplet.cos(PApplet.TWO_PI * i / (float) tp),
								ti * PApplet.sin(PApplet.TWO_PI * i / (float) tp)),
						100f + (float) Math.random() * 100f, 5f + (float) Math.random() * 5f));
			}
		}
	}

	public void update() {
		for (Instance k : kinematics) {
			k.update(FRAMES);
		}
		for (Player p : doFinish) {
			p.finish();
		}
		doFinish.clear();
		if (doFinishLater.size() > 0) {
			doFinish.addAll(doFinishLater);
			doFinishLater.clear();
		}

		for (Tether t : doDestroyTethers) {
			t.finished=true;
			tethers.remove(t);
			world.destroyBody(t.body);
			t.body.setActive(false);
		}
		doDestroyTethers.clear();	

		for (Particle p : parts) {
			if (p.update()) {
				partsRemove.add(p);
			}
		}
		for (Particle p : partsRemove) {
			particles.destroyBody(p.body);
			parts.remove(p);
		}
		partsRemove.clear();
		for (Particle p : partsAdd) {
			particles.destroyBody(p.body);
			parts.add(p);
		}
		partsAdd.clear();
		for (Player p : players) {
			p.update();
		}

	}

	public void draw(PGraphics pG, Vec2 p1, Vec2 p2, Viewport vp) {
		// draw particles
		for (Particle p : parts) {
			p.draw(pG, p1, p2);
		}
		// draw tethers
		for (Tether t : tethers) {
			t.draw(pG, p1, p2, vp);
		}
		// draw players not added to viewport
		for (Player p : players) {
			if (!vp.target.contains(p)) {
				p.draw(pG, p1, p2, vp);
			}
		}
		// draw viewport players
		for (Player p : vp.target) {
			p.draw(pG, p1, p2, vp);
		}
	}

	public void addContact(Contact _contact) {
		contacts.add(_contact);

		Fixture fA = _contact.getFixtureA();
		Fixture fB = _contact.getFixtureB();
		Container cA = (Container) fA.getUserData();
		Container cB = (Container) fB.getUserData();
		Container cP = null;
		Container cO = null;
		if (cA.type.equals("player")) {
			cP = cA;
			cO = cB;
		} else if (cB.type.equals("player")) {
			cP = cB;
			cO = cA;
		}
		if (cP != null) {
			Player p = (Player) cP;
			if (cO.data.equals("boost")) {
				p.addBoostContact(_contact);
			} else if (cO.data.equals("kill")) {
				p.addKillContact(_contact);
				p.killFactor = 0;
			} else if (cO.data.equals("finish")) {
				if (!p.finished) {
					doFinish.add(p);
				}
			} else if (!cO.data.equals("sensor")) {
				p.addHitContact(_contact);
			}
		}

	}

	public void remContact(Contact _contact) {
		contacts.remove(_contact);

		Fixture fA = _contact.getFixtureA();
		Fixture fB = _contact.getFixtureB();
		Container cA = (Container) fA.getUserData();
		Container cB = (Container) fB.getUserData();
		Container cP = null;
		Container cO = null;
		if (cA.type.equals("player")) {
			cP = cA;
			cO = cB;
		} else if (cB.type.equals("player")) {
			cP = cB;
			cO = cA;
		}
		if (cP != null) {
			Player p = (Player) cP;
			if (cO.data.equals("boost")) {
				p.remBoostContact(_contact);
				p.sound.endBoost();
			} else if (cO.data.equals("kill")) {
				p.remKillContact(_contact);
			} else if (cO.data.equals("sensor")) {
				if (playerCollisions) {
					CollisionFilter cf = (CollisionFilter) cO;
					Filter f = cf.fixture.getFilterData();
					Filter pf = p.ship_fixture.getFilterData();
					f.maskBits |= pf.categoryBits;
					cf.fixture.setFilterData(f);
				}
			} else {
				p.remHitContact(_contact);
			}
		}
	}

	private void addObject(Container _obj) {
		String od = _obj.getData();
		obj.put(od, (Object) _obj);
	}

	public void setScale(float scale_) {
		scale = scale_;
	}

	public void setGravity(Vec2 gravity_) {
		world.setGravity(gravity_);
	}

	public Vec2 coordWorldToPixels(Vec2 pos) {
		return (coordWorldToPixels(pos.x, pos.y));
	}

	public Vec2 coordWorldToPixels(float x, float y) {
		return (new Vec2(x * scale, -y * scale));
	}

	public Vec2 coordPixelsToWorld(Vec2 pos) {
		return (coordPixelsToWorld(pos.x, pos.y));
	}

	public Vec2[] coordPixelsToWorld(Vec2[] pos) {
		Vec2[] out = new Vec2[pos.length];
		for (int i = 0; i < pos.length; i++) {
			out[i] = coordPixelsToWorld(pos[i].x, pos[i].y);
		}
		return out;
	}

	public Vec2 coordPixelsToWorld(float x, float y) {
		return (new Vec2(x / scale, -y / scale));
	}

	public float scalarPixelsToWorld(float in) {
		return (in / scale);
	}

	public float scalarWorldToPixel(float in) {
		return (in * scale);
	}

	public Vec2 getBodyPixelCoord(Body b) {
		if (b != null) {
			Transform xf = b.getTransform();

			return coordWorldToPixels(xf.p);
		}
		return null;
	}

	public void step() {
		FRAMES++;
		update();
		float timeStep = 1 / frameRate;
		lastFrameTimer = System.nanoTime();
		this.step(timeStep, 5, 5);
	}

	private void step(float timeStep, int velocityIterations, int positionIterations) {
		try {
			timeStep /= (float) interFrames;
			for (interFrame = 0; interFrame < interFrames; interFrame++) {
				generateSparks();
				world.step(timeStep, velocityIterations, positionIterations);
				particles.step(timeStep, velocityIterations, positionIterations);
			}
			world.clearForces();
			particles.clearForces();
		} catch (Exception ex) {
			System.out.println(ex);
		}
	}

	public int getForeground() {
		Level m = (Level) map;
		return m.fg;
	}

	public int getBackground() {
		Level m = (Level) map;
		return m.bg;
	}
}
