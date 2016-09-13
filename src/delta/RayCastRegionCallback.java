package delta;

import org.jbox2d.callbacks.RayCastCallback;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Fixture;

public class RayCastRegionCallback implements RayCastCallback {
	int region;
	public boolean m_hit;
	public float m_fraction;

	RayCastRegionCallback(int _region){
		region=_region;
		m_hit=false;
		m_fraction = 1f;
	}
	@Override
	public float reportFixture(Fixture fixture, Vec2 point, Vec2 normal, float fraction) {
		Container c=(Container)fixture.getUserData();
		if(c.type.equals("poly")){
			Poly pc=(Poly)c;
			if(pc.region==region){
				m_hit=true;
				m_fraction = fraction;
				return fraction;
			}			
		}else if(c.type.equals("circle")){
			Circle cc=(Circle)c;
			if(cc.region==region){
				m_hit=true;
				m_fraction = fraction;
				return fraction;
			}			
		}
		
		return -1f;
	}
}