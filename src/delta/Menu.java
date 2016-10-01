package delta;

import java.util.ArrayList;

class MenuPage{
	ArrayList<MenuItem> items=new ArrayList<MenuItem>();
	String name;
	String title;
	String back="";
	int selected=0;
	public MenuPage(String _name, String _title){
		name=_name;
		title=_title;
		items=new ArrayList<MenuItem>();
	}	
	public void addItem(String _text, String _action){
		MenuItem newItem=new MenuItem(_text,_action);
		items.add(newItem);
		
	}
	public void setBack(String _back){
		back=_back;
	}
	public String getBack(){
		return back;
	}
	public void up(){
		if(selected>0){
			selected--;
		}
	}
	public void down(){
		if(selected<items.size()-1){
			selected++;
		}
	}
	public String getSelectedAction(){
		return items.get(selected).getAction();
	}

}

class MenuItem{
	private String text;
	private String action;
	
	MenuItem(String _text, String _action){
		text=_text;
		action=_action;
	}
	public String getText(){
		return text;
	}
	public String getAction(){
		return action;
	}
}

