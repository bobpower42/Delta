package delta;
import org.jbox2d.callbacks.RayCastCallback;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Fixture;

public class RayCastAISensor implements RayCastCallback {
	public boolean m_hit;
	public float m_fraction;
	public Vec2 m_point;
	public double A,B;
	

	RayCastAISensor() {
		m_hit = false;
		m_fraction = 1f;
		A=0;
		B=0;
	}

	public float reportFixture(Fixture fixture, Vec2 point, Vec2 normal, float fraction) {
		Container c=(Container) fixture.getUserData();
		//System.out.println(c.type);
		if (!c.type.equals("player") && !c.type.equals("tether") && !c.type.equals("sensor")) {
			m_hit = true;			
			m_fraction = fraction;
			m_point=point;
			double d=1-fraction;
			if(c.data.equals("boost")){
				A=d;
				B=d;
			}else if(c.data.equals("kill")){
				A=-d;
				B=-d;
			}else if(c.data.equals("bounce")){
				A=-d;
				B=d;
			}else{
				A=d;
				B=-d;
			}
			return fraction;
		}
		return -1f;
	}
}

