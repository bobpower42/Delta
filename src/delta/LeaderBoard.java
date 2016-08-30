package delta;

import java.io.File;
import java.util.ArrayList;
//import java.util.HashMap;

import processing.core.PApplet;
import processing.data.XML;

public class LeaderBoard {
	String packFolder;
	String packName;
	String mapName;
	String type;
	ArrayList<Entry> board;

	File folder;
	PApplet pA;

	LeaderBoard(PApplet _pA, String _pF, String _pN, String _mN) {
		pA = _pA;
		packFolder = _pF;
		packName = _pN;
		mapName = _mN;
		// type=_type;
		board = new ArrayList<Entry>();
		load();
	}

	public void load() {
		folder = new File(packFolder + "\\" + packName + "\\" + mapName);
		if (!folder.exists()) {
			if (!folder.mkdir()) {
				// error
			}
		} else {
			File file = new File(folder.getPath() + "\\leaderboard.xml");
			if (file.exists()) {
				try {
					XML xml = pA.loadXML(file.getAbsolutePath());
					XML[] entry = xml.getChildren("entry");
					for (XML e : entry) {
						Entry te = new Entry(e);
						board.add(te);
					}
				} catch (Exception ex) {
					//
				}
			}
		}
	}

	public int check(float time) {
		if (board.size() != 0) {
			int min = 11;
			for (int i = 0; i < board.size(); i++) {
				Entry te = board.get(i);
				if (time < te.time && te.place < min) {
					min = te.place;
				}
			}
			if(min==11)min=board.size()+1;
			if (min < 11)
				return min;
			
			return 0;
		}
		return 1;
	}

	public void place(Ghost gh) {
		float time = gh.time;
		int place = check(time);
		String name = gh.name;
		Long num = (long) (Math.floor(Math.random() * 900000000L) + 10000000L);
		String ghost = gh.map + "_" + String.valueOf(num);
		System.out.println(folder.getAbsolutePath());
		gh.getBytes();
		pA.saveBytes(folder.getAbsolutePath() + "\\" + ghost + ".ghost", gh.getBytes());
		for (Entry te : board) {
			if (te.place >= place)
				te.place++;
		}
		board.add(new Entry(place, time, name, ghost));
		XML out = new XML("leaderboard");
		ArrayList<Entry> delAfter=new ArrayList<Entry>();
		for (Entry te : board) {
			if (te.place < 11) {
				out.addChild(te.getXML());
			} else {
				delAfter.add(te);				
			}			
		}
		for(Entry te:delAfter){
			File del = new File(folder.getAbsolutePath() + "\\" + te.ghostFile + ".ghost");
			if (del.exists()) {
				del.delete();
			}
			board.remove(te);			
		}
		delAfter.clear();
		pA.saveXML(out, folder.getPath() +"\\leaderboard.xml");
		load();
	}
}

class Entry {
	int place;
	float time;
	String name;
	String ghostFile;

	Entry() {

	}

	Entry(int _place, float _time, String _name, String _ghost) {
		place = _place;
		time = _time;
		name = _name;
		ghostFile = _ghost;
	}

	Entry(XML _xml) {
		if (_xml.hasAttribute("place")) {
			place = _xml.getInt("place");
		}
		if (_xml.hasAttribute("time")) {
			time = _xml.getFloat("time");
		}
		if (_xml.hasAttribute("name")) {
			name = _xml.getString("name");
		}
		if (_xml.hasAttribute("ghost")) {
			ghostFile = _xml.getString("ghost");
		}
	}

	public XML getXML() {
		XML out = new XML("entry");
		out.setInt("place", place);
		out.setFloat("time", time);
		out.setString("name", name);
		out.setString("ghost", ghostFile);
		return out;
	}
}
