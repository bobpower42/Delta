package delta;
import java.util.ArrayList;

import org.jbox2d.callbacks.*;
import org.jbox2d.dynamics.Fixture;
public class FilterQueryCallback implements QueryCallback {
	ArrayList<Fixture> found=new ArrayList<Fixture>();
	String filter="";
	String type="";
	boolean filterSet=false;
	boolean typeSet=false;

	@Override
	public boolean reportFixture(Fixture fixture) {		
		if(filterSet){
			Container container=(Container) fixture.getUserData();
			if(container.data.equals(filter)){
				if(typeSet){
				    if(fixture.getBody().getType().toString().equals(type)){
				    	found.add(fixture);
				    }
				}else{
					found.add(fixture);
				}
			}
		}else{
			if(typeSet){
			    if(fixture.getBody().getType().toString().equals(type)){
			    	found.add(fixture);
			    }
			}else{
				found.add(fixture);
			}
		}
		return true;
	}
	
	public void setFilter(String _filter){
		filter=_filter;
		filterSet=true;
	}
	public void setType(String _type){
		type=_type;
		typeSet=true;
	}
	public void clearFilter(){
		filter="";
		filterSet=false;
	}
	public void clearType(){
		type="";
		typeSet=false;
	}
	public void clear(){
		found.clear();
	}
}
