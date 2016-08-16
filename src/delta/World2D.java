package delta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.jbox2d.callbacks.ContactImpulse;
import org.jbox2d.callbacks.ContactListener;
import org.jbox2d.collision.Manifold;
import org.jbox2d.collision.WorldManifold;
import org.jbox2d.common.Transform;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.contacts.Contact;

import beads.UGen;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.data.XML;

public class World2D {
	public World world, particles;
	public static int[] cl = { -34048, -16740353, -65413, -8716033 };
	private float scale;
	Level map;
	Layer game;
	Map<String, Container> obj = new HashMap<String, Container>();
	Container spawn;
	ArrayList<Player> players;
	ArrayList<Contact> contacts;
	ArrayList<Particle> parts;
	ArrayList<Particle> partsRemove;
	ArrayList<Particle> partsAdd;
	ArrayList<Player> doFinish;
	// HashMap<Contact, CollisionSound> collisionSound;
	long lastFrameTimer;
	float frameRate;
	UGen out;
	public static int MAXPARTICLES = 200;

	World2D(float _frameRate, UGen _out) {
		// collisionSound=new HashMap<Contact, CollisionSound>();
		out = _out;
		frameRate = _frameRate;
		world = new World(new Vec2(0, -9.8f));
		particles = new World(new Vec2(0, -9.8f));
		players = new ArrayList<Player>();
		doFinish = new ArrayList<Player>();
		contacts = new ArrayList<Contact>();
		parts = new ArrayList<Particle>();
		partsRemove = new ArrayList<Particle>();
		partsAdd = new ArrayList<Particle>();
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
			} else if (c.type.equals("instance")) {
				// todo
			}
		}
	}

	public void addPlayer(Player p) {
		players.add(p);
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
				int t = 3 + (int) (relativeSpeed / 3f);

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

				for (int i = 0; i < 4; i++) {
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
					for (int i = 0; i < 4; i++) {

						Vec2 loc = new Vec2(worldPoint.x - vel.x / 50f, worldPoint.y + vel.y / 50f);
						if (parts.size() < MAXPARTICLES) {
							parts.add(new SparkParticle(this, loc,
									new Vec2(relativeSpeed * ((float) Math.random() * 4f - 2),
											relativeSpeed * ((float) Math.random() * 4f - 2)),
									100f, 2 + (float) Math.random() * 5f));
						}
					}
				}
			}
		}
	}

	void generateBounceParticles(Vec2 p, float ti) {
		int tp = (int) (4 * ti);
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
		for (Player p : doFinish) {
			p.finish();
		}
		doFinish.clear();
		generateSparks();

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
		// draw players not added to viewport
		for (Player p : players) {
			if (!vp.target.contains(p)) {
				p.draw(pG, p1, p2);
			}
		}
		// draw viewport players
		for (Player p : vp.target) {
			p.draw(pG, p1, p2);
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
			} else if (cO.data.equals("finish")) {
				if(!p.finished){
				doFinish.add(p);
				}
			} else {
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
			} else if (cO.data.equals("kill")) {
				p.remKillContact(_contact);
			} else {
				p.remHitContact(_contact);
			}
		}
	}

	private void addObject(Container _obj) {
		String od = _obj.getData();
		obj.put(od, _obj);
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
		update();
		// float
		// timeStep=(float)((System.nanoTime()-lastFrameTimer)/1000000000f);
		float timeStep = 1 / 50f;
		// System.out.println(timeStep);
		// if(timeStep>0.05)timeStep=0.05f;

		lastFrameTimer = System.nanoTime();
		this.step(timeStep, 5, 5);
		// world.clearForces();
	}

	private void step(float timeStep, int velocityIterations, int positionIterations) {
		try {
			world.step(timeStep, velocityIterations, positionIterations);
			particles.step(timeStep, velocityIterations, positionIterations);
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
