package delta;

import org.jbox2d.callbacks.RayCastCallback;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Fixture;

public class RayCastClosestCallback implements RayCastCallback {
	public boolean m_hit;
	public Vec2 m_point;
	public float m_fraction;

	RayCastClosestCallback() {
		m_hit = false;
		m_fraction = 1f;
	}

	public float reportFixture(Fixture fixture, Vec2 point, Vec2 normal, float fraction) {
		Container c=(Container) fixture.getUserData();
		if (!c.type.equals("player") && !c.type.equals("tether")) {
			m_hit = true;
			m_point = point;
			m_fraction = fraction;
			return fraction;
		}
		return -1f;
	}

}
