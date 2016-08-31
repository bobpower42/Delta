package delta;

import java.io.File;
import java.util.ArrayList;
//import java.util.HashMap;
import java.util.HashMap;

import processing.core.PApplet;
import processing.data.XML;

public class LeaderBoard {
	String packFolder;
	String packName;
	String mapName;
	String type;
	ArrayList<Entry> board;
	HashMap<Integer, Entry> sorted;
	World2D world;

	File folder;
	PApplet pA;

	LeaderBoard(PApplet _pA, World2D _world, String _pF, String _pN, String _mN) {
		pA = _pA;
		world = _world;
		packFolder = _pF;
		packName = _pN;
		mapName = _mN;
		// type=_type;
		board = new ArrayList<Entry>();
		sorted = new HashMap<Integer, Entry>();
		load();
	}

	public void load() {
		folder = new File(packFolder + "\\" + packName + "\\" + mapName);
		if (!folder.exists()) {
			if (!folder.mkdir()) {
				// error
			}
		} else {
			File file = new File(folder.getAbsolutePath() + "\\leaderboard.xml");
			if (file.exists()) {
				try {
					XML xml = pA.loadXML(file.getAbsolutePath());
					XML[] entry = xml.getChildren("entry");
					for (XML e : entry) {
						Entry te = new Entry(e);
						board.add(te);
					}
					sort();
				} catch (Exception ex) {
					//
				}
			}
		}
	}

	public void sort() {
		sorted = new HashMap<Integer, Entry>();
		ArrayList<Entry> delAfter = new ArrayList<Entry>();
		for (Entry te : board) {
			if (te.place < 11) {
				sorted.put(te.place, te);
			} else {
				delAfter.add(te);
			}
		}
		for (Entry te : delAfter) {
			File del = new File(folder.getAbsolutePath() + "\\" + te.ghostFile + ".ghost");
			if (del.exists()) {
				del.delete();
			}
			board.remove(te);
		}
		delAfter.clear();
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
			if (min == 11)
				min = board.size() + 1;
			if (min < 11)
				return min;

			return 0;
		}
		return 1;
	}

	public void place(Ghost gh) {
		float time = gh.time;
		int place = check(time);
		System.out.println("place: "+place+" time: "+time);
		String name = gh.name;
		File output;
		String ghost;
		do {
			Long num = (long) (Math.floor(Math.random() * 900000000L) + 10000000L);
			ghost = gh.map + "_" + String.valueOf(num);
			output = new File(folder.getAbsolutePath() + "\\" + ghost + ".ghost");
		} while (output.exists());
		gh.getBytes();
		pA.saveBytes(output.getAbsolutePath(), gh.getBytes());
		for (Entry te : board) {
			if (te.place >= place)
				te.place++;
		}
		board.add(new Entry(place, time, name, ghost));
		sort();
		XML out = new XML("leaderboard");
		for (int i = 1; i < 11; i++) {
			if (sorted.containsKey(i)) {
				Entry te = sorted.get(i);
				out.addChild(te.getXML());
			}
			pA.saveXML(out, folder.getPath() + "\\leaderboard.xml");
			load();
		}
	}

	Ghost getGhost(int _place) {
		if (sorted.containsKey(_place)) {
			Entry te = sorted.get(_place);
			File gf = new File(folder.getAbsolutePath() + "\\" + te.ghostFile + ".ghost");
			if (gf.exists()) {
				try {
					return new Ghost(world, pA.loadBytes(gf.getAbsolutePath()));
				} catch (Exception ex) {
				}
			}
		}
		return null;
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
