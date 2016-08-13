package delta;

import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;

import processing.core.PConstants;
import processing.core.PGraphics;

public abstract class Particle {
	public boolean dead;
	public float life, decay;
	public String type;
	Body body;
	World2D world;

	Particle() {

	}

	public boolean update() {
		return false;
	}

	public void draw(PGraphics pG, Vec2 p1, Vec2 p2) {

	}

}

class SmokeParticle extends Particle {
	SmokeParticle(World2D _world, Vec2 pos, Vec2 vel, float life_, float decay_) {
		world = _world;
		life = life_;
		decay = decay_;
		BodyDef bd = new BodyDef();
		bd.type = BodyType.DYNAMIC;
		bd.position.set(pos);
		body = world.particles.createBody(bd);
		CircleShape cs = new CircleShape();
		cs.m_radius = 0.01f;
		FixtureDef fd = new FixtureDef();
		fd.shape = cs;
		fd.density = 1.0f;
		fd.friction = 0.0f;
		fd.restitution = 0.9f;
		fd.setUserData(0);
		// Attach fixture to body
		body.createFixture(fd);
		body.setLinearDamping(2.0f);
		body.setLinearVelocity(vel);
		body.setGravityScale(0.0f);
		dead = false;
	}

	public boolean update() {
		life -= decay;
		if (life < 0) {
			dead = true;
		}
		return dead;
	}

	public void draw(PGraphics pG, Vec2 p1, Vec2 p2) {

		Vec2 pp = world.getBodyPixelCoord(body);
		pG.rectMode(PConstants.CORNERS);
		pG.fill(80);
		pG.noStroke();
		float sz = (life / 20f);
		pG.rect(pp.x - sz, pp.y - sz, pp.x + sz, pp.y + sz);

	}

}

class SparkParticle extends Particle {
	SparkParticle(World2D _world, Vec2 pos, Vec2 vel, float life_, float decay_) {
		world = _world;
		life = life_;
		decay = decay_;
		BodyDef bd = new BodyDef();
		bd.type = BodyType.DYNAMIC;
		bd.position.set(pos);
		body = world.particles.createBody(bd);
		CircleShape cs = new CircleShape();
		cs.m_radius = 0.01f;
		FixtureDef fd = new FixtureDef();
		fd.shape = cs;
		fd.density = 1.0f;
		fd.friction = 0.0f;
		fd.restitution = 0.9f;
		fd.setUserData(0);
		// Attach fixture to body
		body.createFixture(fd);
		body.setLinearDamping(1.0f);
		body.setLinearVelocity(vel);
		body.setGravityScale(2.0f);
		dead = false;
	}

	public boolean update() {
		life -= decay;
		if (life < 0) {
			dead = true;
		}
		return dead;
	}

	public void draw(PGraphics pG, Vec2 p1, Vec2 p2) {
		Vec2 pp = world.getBodyPixelCoord(body);
		pG.rectMode(PConstants.CORNERS);
		pG.fill(255);
		pG.noStroke();
		float sz = (life / 20f);
		pG.rect(pp.x - sz, pp.y - sz, pp.x + sz, pp.y + sz);

	}

}

class BounceParticle extends Particle {
	BounceParticle(World2D _world, Vec2 pos, Vec2 vel, float life_, float decay_) {
		world = _world;
		life = life_;
		decay = decay_;
		BodyDef bd = new BodyDef();
		bd.type = BodyType.DYNAMIC;
		bd.position.set(pos);
		body = world.particles.createBody(bd);
		CircleShape cs = new CircleShape();
		cs.m_radius = 0.01f;
		FixtureDef fd = new FixtureDef();
		fd.shape = cs;
		fd.density = 1.0f;
		fd.friction = 0.0f;
		fd.restitution = 1.0f;
		fd.setUserData(0);
		// Attach fixture to body
		body.createFixture(fd);
		body.setLinearDamping(0.1f);
		body.setLinearVelocity(vel);
		body.setGravityScale(0.1f);
		dead = false;
	}

	public boolean update() {
		life -= decay;
		if (life < 0) {
			dead = true;
		}
		return dead;
	}

	public void draw(PGraphics pG, Vec2 p1, Vec2 p2) {
		Vec2 pp = world.getBodyPixelCoord(body);
		pG.rectMode(PConstants.CORNERS);
		pG.fill(255, 255, 0);
		pG.noStroke();
		float sz = (life / 20f);
		pG.rect(pp.x - sz, pp.y - sz, pp.x + sz, pp.y + sz);

	}

}

class BoostParticle extends Particle {
	BoostParticle(World2D _world, Vec2 pos, Vec2 vel, float life_, float decay_) {
		world = _world;
		life = life_;
		decay = decay_;
		BodyDef bd = new BodyDef();
		bd.type = BodyType.DYNAMIC;
		bd.position.set(pos);
		body = world.particles.createBody(bd);
		CircleShape cs = new CircleShape();
		cs.m_radius = 0.01f;
		FixtureDef fd = new FixtureDef();
		fd.shape = cs;
		fd.density = 1.0f;
		fd.friction = 0.0f;
		fd.restitution = 0.9f;
		fd.setUserData(0);
		// Attach fixture to body
		body.createFixture(fd);
		body.setLinearDamping(1.0f);
		body.setLinearVelocity(vel);
		body.setGravityScale(0.1f);
		dead = false;
	}

	public boolean update() {
		life -= decay;
		if (life < 0) {
			dead = true;
		}
		return dead;
	}

	public void draw(PGraphics pG, Vec2 p1, Vec2 p2) {
		Vec2 pp = world.getBodyPixelCoord(body);
		pG.rectMode(PConstants.CORNERS);
		pG.fill(0, 255, 0);
		pG.noStroke();
		float sz = (life / 20f);
		pG.rect(pp.x - sz, pp.y - sz, pp.x + sz, pp.y + sz);

	}

}

class KillParticle extends Particle {
	KillParticle(World2D _world, Vec2 pos, Vec2 vel, float life_, float decay_) {
		world = _world;
		life = life_;
		decay = decay_;
		BodyDef bd = new BodyDef();
		bd.type = BodyType.DYNAMIC;
		bd.position.set(pos);
		body = world.particles.createBody(bd);
		CircleShape cs = new CircleShape();
		cs.m_radius = 0.01f;
		FixtureDef fd = new FixtureDef();
		fd.shape = cs;
		fd.density = 0.1f;
		fd.friction = 0.0f;
		fd.restitution = 0.9f;
		fd.setUserData(0);
		// Attach fixture to body
		body.createFixture(fd);
		// body.setLinearDamping(0.0f);
		body.setLinearVelocity(vel);
		body.setGravityScale(0.0f);
		dead = false;
	}

	public boolean update() {
		life -= decay;
		if (life < 0) {
			dead = true;
		}
		return dead;
	}

	public void draw(PGraphics pG, Vec2 p1, Vec2 p2) {
		Vec2 pp = world.getBodyPixelCoord(body);
		pG.rectMode(PConstants.CORNERS);
		pG.fill(255, 0, 0);
		pG.noStroke();
		float sz = (life / 40f);
		pG.rect(pp.x - sz, pp.y - sz, pp.x + sz, pp.y + sz);
	}

}

class FinishParticle extends Particle {
	int cl;

	FinishParticle(World2D _world, Vec2 pos, Vec2 vel, float life_, float decay_, int _cl) {
		cl = _cl;
		world = _world;
		life = life_;
		decay = decay_;
		BodyDef bd = new BodyDef();
		bd.type = BodyType.DYNAMIC;
		bd.position.set(pos);
		body = world.particles.createBody(bd);
		CircleShape cs = new CircleShape();
		cs.m_radius = 0.01f;
		FixtureDef fd = new FixtureDef();
		fd.shape = cs;
		fd.density = 1.0f;
		fd.friction = 0.0f;
		fd.restitution = 0.99f;
		fd.setUserData(0);
		// Attach fixture to body
		body.createFixture(fd);
		body.setLinearDamping(0.1f);
		body.setLinearVelocity(vel);
		body.setGravityScale(0.5f);
		dead = false;
	}

	public boolean update() {
		life -= decay;
		if (life < 0) {
			dead = true;
		}
		return dead;
	}

	public void draw(PGraphics pG, Vec2 p1, Vec2 p2) {
		Vec2 pp = world.getBodyPixelCoord(body);
		pG.rectMode(PConstants.CORNERS);
		pG.fill(cl);
		pG.noStroke();
		float sz = (life / 40f);
		pG.rect(pp.x - sz, pp.y - sz, pp.x + sz, pp.y + sz);

	}

}
