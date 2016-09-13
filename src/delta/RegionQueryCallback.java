package delta;

import java.util.ArrayList;

import org.jbox2d.callbacks.QueryCallback;
import org.jbox2d.dynamics.Fixture;

public class RegionQueryCallback implements QueryCallback{
	ArrayList<Fixture> found=new ArrayList<Fixture>();

	@Override
	public boolean reportFixture(Fixture fixture) {
		found.add(fixture);
		return true;
	}

}
