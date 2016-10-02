package delta;

import java.io.File;
import java.io.FilenameFilter;
//import java.util.ArrayList;
import java.util.ArrayList;
import java.util.HashMap;

import org.encog.Encog;

import beads.AudioContext;
import beads.Gain;
import beads.Plug;
import processing.core.PApplet;
import processing.core.PFont;
import processing.data.XML;
import processing.opengl.PJOGL;

public class DeltaMain extends PApplet {
	// player colours
	public static int[] cl = { -34048, -16740353, -65413, -8716033 };
	private static PlayerInput[] p;
	Player player, player2;
	private int splashState = 0;
	private int menuState = 1;
	private int gameState = 2;
	private int state = 0;
	public int frame = 0;
	public boolean pause = false;
	private String packFolder;
	XML packData;
	String[] packs;
	static String userDir;
	World2D world;
	ArrayList<Player> players;
	Viewport[] views = new Viewport[4];
	int nov = 1;
	HashMap<String, MenuPage> menus = new HashMap<String, MenuPage>();
	MenuPage page;
	int nop = 1;
	int mode = 0;
	int type = 0;
	String pack, map;

	int actionTimer = 0;
	int att = 18;

	
	AudioContext ac;
	Gain master;
	Plug out;
	long frameTimer;
	int frameCounter = 0;
	float fps = 0;
	public PFont font;
	float frameRate = 60f;
	int counter = 0;
	static private int aa = 1;
	
	boolean running=true;

	public static void main(String[] args) {
		for (String arg : args) {
			if (arg.equalsIgnoreCase("aa0"))
				aa = 0;
			if (arg.equalsIgnoreCase("aa1"))
				aa = 1;
			if (arg.equalsIgnoreCase("aa2"))
				aa = 2;
			if (arg.equalsIgnoreCase("aa4"))
				aa = 4;
			if (arg.equalsIgnoreCase("aa8"))
				aa = 8;

		}
		p = new PlayerInput[4];
		String[] a = { "MAIN" };
		PApplet.runSketch(a, new DeltaMain());

	}

	static boolean is64bit() {
		return System.getProperty("sun.arch.data.model").equals("64");
	}

	public void settings() {
		for (int i = 0; i < 4; i++) {
			p[i] = new PlayerInput(this, i + 1);
		}
		userDir = System.getProperty("user.dir");
		fullScreen(P2D); // openGl
		PJOGL.setIcon(userDir + "\\data\\icon.png");
		smooth(4);

	}

	public void setup() {
		surface.setTitle("delta 1.0");
		font = loadFont("Avant-GardeBoldT.-48.vlw");
		textFont(font, 24);
		textAlign(LEFT, TOP);
		ac = new AudioContext();
		println("default buffer: " + ac.getBufferSize());
		out = new Plug(ac);
		master = new Gain(ac, 1, 3.0f);
		master.addInput(out);
		ac.out.addInput(master);
		ac.start();
		noCursor();

		frameRate(frameRate);

		packFolder = userDir + "\\packs";
		try {
			File folder = new File(packFolder);
			File[] files = folder.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String filename) {
					return filename.endsWith(".xml");
				}
			});
			packs = new String[files.length];
			int i = 0;
			for (File f : files) {
				packs[i] = f.getName().split("\\.")[0];
				i++;
			}
		} catch (Exception ex) {
			System.out.println("packs failed to load");
		}
		MenuPage tm = new MenuPage("players", "select number of players");
		tm.addItem("single player", "nop=1.packs");
		tm.addItem("2 player", "nop=2.types");
		tm.addItem("3 player", "nop=3.types");
		tm.addItem("4 player", "nop=4.types");
		menus.put("players", tm);
		page = tm;
		MenuPage tm2 = new MenuPage("packs", "select pack");
		for (String p : packs) {
			tm2.addItem(p, "pack=" + p + ".maps");
		}
		menus.put("packs", tm2);
		MenuPage tm3 = new MenuPage("modes", "select game mode");
		tm3.addItem("split screen", "mode=0.packs");
		tm3.addItem("elimination", "mode=1.packs");
		menus.put("modes", tm3);
		//n0oLoop();
		//thread("frameClock");

	}

	public void mapMenu() {
		packData = loadXML(packFolder + "\\" + pack + ".xml");
		XML mapData = packData.getChild("maps");
		XML[] maps = mapData.getChildren("map");
		MenuPage tm = new MenuPage("maps", "select map");
		for (XML m : maps) {
			String mapName = m.getString("data");
			tm.addItem(mapName, "map=" + mapName + ".go");
		}
		menus.put("maps", tm);

	}

	public void typesMenu() {
		MenuPage tm = new MenuPage("types", "select game type");
		if (nop == 1) {
			tm.addItem("race", "type=0.packs");
		} else if (nop == 2 || nop == 3) {
			tm.addItem("race", "type=0.modes");
			tm.addItem("tethered", "type=1.packs");
		} else if (nop == 4) {
			tm.addItem("race", "type=0.modes");
			tm.addItem("tethered", "type=1.packs");
			tm.addItem("2v2 tethered", "type=2.modes");
		}
		menus.put("types", tm);
	}

	public void drawMenuPage() {
		textAlign(0, 3);
		fill(255);
		textFont(font, 36);
		text(page.title, 400, 400);
		for (int i = 0; i < page.items.size(); i++) {
			if (i == page.selected) {
				textFont(font, 48);
			} else {
				textFont(font, 36);
			}
			text(page.items.get(i).getText(), 400, 500 + 50 * i);
		}
	}

	public void draw() {
		//frameRate(60f);
		if (frameCounter >= 10) {
			frameCounter = 0;
			fps = 10000000 / (float) (System.nanoTime() - frameTimer);
			fps *= 1000;
			frameTimer = System.nanoTime();

		} else {
			frameCounter++;
		}
		if (state == splashState) {
			background(0);
			state = menuState;

		} else if (state == menuState) {
			if (actionTimer > 0)
				actionTimer--;
			background(0);
			if (actionTimer == 0) {
				for (PlayerInput tpi : p) {
					if (tpi.connected) {
						if (tpi.butA) {
							select();
							actionTimer = att;
						} else if (tpi.butB) {
							back();
							actionTimer = att;
						} else if (tpi.mag > 0.5f) {
							System.out.println(tpi.ang);
							if (tpi.ang > 90 && tpi.ang < 270) {
								page.down();
								actionTimer = att;
							} else {
								page.up();
								actionTimer = att;
							}
						}

					}
				}
			}
			drawMenuPage();

		} else if (state == gameState) {
			// background(0);
			

			world.step();

			for (int i = 0; i < nov; i++) {
				views[i].setFade(1f);
				views[i].update();
				image(views[i].pg, views[i].pos.x, views[i].pos.y);
			}

			fill(255);
			text("FPS: " + fps, 10, 10);

		}
	}

	public void select() {
		String[] action = page.getSelectedAction().split("\\.");
		System.out.println(page.getSelectedAction());
		System.out.println(action[0]);
		System.out.println(action.length);
		String[] assign = action[0].split("\\=");
		int start = 0;
		if (assign.length == 2) {
			start = 1;
			String var = assign[0];
			String val = assign[1];
			if (var.equals("nop")) {
				nop = Integer.valueOf(val);
			} else if (var.equals("mode")) {
				mode = Integer.valueOf(val);
			} else if (var.equals("type")) {
				type = Integer.valueOf(val);
			} else if (var.equals("pack")) {
				pack = val;
			} else if (var.equals("map")) {
				map = val;
			}
		}
		for (int i = start; i < action.length; i++) {
			String act = action[i];
			if (act.equals("maps")) {
				mapMenu();
			} else if (act.equals("types")) {
				typesMenu();
			}

			if (act.equals("maps") || act.equals("types") || act.equals("packs") || act.equals("modes")) {
				String thisPage = page.name;
				page = menus.get(act);
				page.setBack(thisPage);
			} else if (act.equals("go")) {
				startGame();
			}
		}

	}

	public void back() {
		if (!page.back.equals("")) {
			page = menus.get(page.back);
		}
	}

	public void startGame() {
		world = new World2D(this, frameRate, out);
		world.loadfromXML(packData, packFolder, pack, map);
		world.font = font;
		Level thisMap = world.map;
		String split = thisMap.split;

		if (nop == 1) {
			world.addAllGhosts();
		}
		players = new ArrayList<Player>();
		for (int i = 0; i < nop; i++) {
			Player pl = new Player(world, i);
			pl.attachInput(p[i]);
			pl.createShip();
			players.add(pl);
		}
		views = new Viewport[4];
		if (mode == 1 || nop == 1 || type == 1) {
			Viewport vp = new Viewport(this, world, 0, 0, width, height);
			vp.attachTarget(players);
			vp.setTrackMode(mode);
			if (type == 1) {
				world.tether(players);
			}
			views[0] = vp;
			nov = 1;
		} else {
			if (nop == 2) {
				if (split.charAt(0) == 'H') {
					views[0] = new Viewport(this, world, 0, 0, width, height / 2);
					views[1] = new Viewport(this, world, 0, height / 2, width, height / 2);
					views[0].attachTarget(players.get(0));
					views[1].attachTarget(players.get(1));
				} else {
					views[0] = new Viewport(this, world, 0, 0, width / 2, height);
					views[1] = new Viewport(this, world, width / 2, 0, width / 2, height);
					views[0].attachTarget(players.get(0));
					views[1].attachTarget(players.get(1));
				}
				nov = 2;
			} else if (nop == 3) {
				if (split.charAt(1) == 'H') {
					views[0] = new Viewport(this, world, 0, 0, width, height / 3);
					views[1] = new Viewport(this, world, 0, height / 3, width, height / 3);
					views[2] = new Viewport(this, world, 0, 2 * height / 3, width, height / 3);
					views[0].attachTarget(players.get(0));
					views[1].attachTarget(players.get(1));
					views[2].attachTarget(players.get(2));
				} else {
					views[0] = new Viewport(this, world, 0, 0, width / 3, height);
					views[1] = new Viewport(this, world, width / 3, 0, width / 3, height);
					views[2] = new Viewport(this, world, 2 * width / 3, 0, width / 3, height);
					views[0].attachTarget(players.get(0));
					views[1].attachTarget(players.get(1));
					views[2].attachTarget(players.get(2));
				}
				nov = 3;
			} else if (nop == 4) {
				if (type == 0) {
					if (split.charAt(2) == 'H') {
						views[0] = new Viewport(this, world, 0, 0, width, height / 4);
						views[1] = new Viewport(this, world, 0, height / 4, width, height / 4);
						views[2] = new Viewport(this, world, 0, 2 * height / 4, width, height / 4);
						views[3] = new Viewport(this, world, 0, 3 * height / 4, width, height / 4);
						views[0].attachTarget(players.get(0));
						views[1].attachTarget(players.get(1));
						views[2].attachTarget(players.get(2));
						views[3].attachTarget(players.get(3));
					} else if (split.charAt(2) == 'V') {
						views[0] = new Viewport(this, world, 0, 0, width / 4, height);
						views[1] = new Viewport(this, world, width / 4, 0, width / 4, height);
						views[2] = new Viewport(this, world, 2 * width / 4, 0, width / 4, height);
						views[3] = new Viewport(this, world, 3 * width / 4, 0, width / 4, height);
						views[0].attachTarget(players.get(0));
						views[1].attachTarget(players.get(1));
						views[2].attachTarget(players.get(2));
						views[3].attachTarget(players.get(3));
					} else {
						views[0] = new Viewport(this, world, 0, 0, width / 2, height / 2);
						views[1] = new Viewport(this, world, width / 2, 0, width / 2, height / 2);
						views[2] = new Viewport(this, world, 0, height / 2, width / 2, height / 2);
						views[3] = new Viewport(this, world, width / 2, height / 2, width / 2, height / 2);
						views[0].attachTarget(players.get(0));
						views[1].attachTarget(players.get(1));
						views[2].attachTarget(players.get(2));
						views[3].attachTarget(players.get(3));
					}
					nov = 4;
				} else {
					ArrayList<Player> gp1 = new ArrayList<Player>();
					gp1.add(players.get(0));
					gp1.add(players.get(1));
					world.tether(gp1);
					ArrayList<Player> gp2 = new ArrayList<Player>();
					gp2.add(players.get(2));
					gp2.add(players.get(3));
					world.tether(gp2);
					if (split.charAt(0) == 'H') {
						views[0] = new Viewport(this, world, 0, 0, width, height / 2);
						views[1] = new Viewport(this, world, 0, height / 2, width, height / 2);
						views[0].attachTarget(gp1);
						views[1].attachTarget(gp2);
					} else {
						views[0] = new Viewport(this, world, 0, 0, width / 2, height);
						views[1] = new Viewport(this, world, width / 2, 0, width / 2, height);
						views[0].attachTarget(gp1);
						views[1].attachTarget(gp2);
					}
					nov = 2;
				}
			}

		}		
		for (Viewport vp : views) {
			try {
				vp.loadShader(loadShader("vcr.glsl"));
			} catch (Exception ex) {
				//
			}

		}
		world.prepare();
		state = gameState;
	}

	public void pause() {
		if (state == gameState) {

			pause = !pause;
			//vp.setPause(pause);
		}
	}

	public boolean isPaused() {
		return pause;
	}
	
	public void frameClock(){
		long lastTime = System.nanoTime();
		double nsPerTick = 1000000000D/60.0;
		//long lastTimer = System.currentTimeMillis();
		double delta = 0;
		
		while (running){
			long now = System.nanoTime();
			delta += (now - lastTime) / nsPerTick;
			lastTime = now;			
			while(delta >= 1)
			{				
				redraw();
				delta -= 1;				
			}	
			
			
		}
		
	}

	public void exit() {
		running=false;
		for (PlayerInput pi : p) {
			pi.release();
		}
		ac.stop();
		surface.stopThread();
		ac = null;
		Encog.getInstance().shutdown();
		System.out.println("stopping");
		super.exit();
		System.exit(0);
	}
}
