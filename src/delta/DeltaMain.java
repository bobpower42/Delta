package delta;

import java.io.File;
import java.io.FilenameFilter;
//import java.util.ArrayList;
import java.util.ArrayList;

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
	static String userDir;
	public World2D world;
	Viewport vp, vp2;
	AudioContext ac;
	Gain master;
	Plug out;
	long frameTimer;
	int frameCounter = 0;
	float fps = 0;
	PFont font;
	float frameRate = 60f;
	int counter = 0;
	boolean tether = false;

	public static void main(String[] args) {
		String[] a = { "MAIN" };
		PApplet.runSketch(a, new DeltaMain());
	}

	static boolean is64bit() {
		return System.getProperty("sun.arch.data.model").equals("64");
	}

	public void settings() {
		userDir = System.getProperty("user.dir");
		p = new PlayerInput[4];
		for (int i = 0; i < 4; i++) {
			p[i] = new PlayerInput(this, i + 1);
		}
		fullScreen(P2D); // openGl
		PJOGL.setIcon(userDir + "\\data\\icon.png");
		smooth(1); // turn off anti-aliasing
	}

	public void setup() {
		surface.setTitle("delta 1.0");

		font = loadFont("Avant-GardeBoldT.-48.vlw");
		textFont(font, 24);
		textAlign(LEFT, TOP);
		ac = new AudioContext();
		out = new Plug(ac);
		master = new Gain(ac, 1, 3.0f);
		master.addInput(out);
		ac.out.addInput(master);
		ac.start();
		noCursor();
		frameRate(frameRate);
		world = new World2D(this, frameRate, out);
		world.setScale(60f);
		packFolder = System.getProperty("user.dir") + "\\packs";
		File folder = new File(packFolder);

		File[] files = folder.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String filename) {
				return filename.endsWith(".xml");
			}
		});
		for (int i = 0; i < files.length; i++) {
			println(files[i].getName());
		}
		// XML pack=loadXML("C:/Users/Bob/4PG/Delta/packs/WhiteMap1.xml");
		XML pack = loadXML(files[0].getAbsolutePath());
		String name = files[0].getName();
		println(name);
		//world.loadfromXML(pack, packFolder, name, "001_rails");
		 world.loadfromXML(pack, packFolder, name, "011_mag");
		// world.loadfromXML(pack, packFolder, name, "010_first");
		world.font = font;
		world.addAllGhosts();
		player = new Player(world, 0);
		player.attachInput(p[0]);
		// player.attachAi(new
		// File(userDir+"\\data\\ai\\85_feedForward_256_64_16.eg"));
		// player.setTraining();
		player2 = new Player(world, 1);
		player2.attachInput(p[1]);
		// player2.attachAi(new
		// File(userDir+"\\data\\ai\\85_feedForward_256_64_16.eg"));
		// Player player3 = new Player(world, p[2], 2);
		// Player player4 = new Player(world, p[3], 3);
		if (tether) {
			vp = new Viewport(this, world, 0, 0, width, height);
			// vp = new Viewport(this, world, 0, 0, width, height / 2);
			vp.loadShader(loadShader("vcr.glsl"));
			ArrayList<Player> test = new ArrayList<Player>();
			test.add(player);
			test.add(player2);
			world.tether(test);
			vp.attachTarget(test);

		} else {
			vp = new Viewport(this, world, 0, 0, width, height);
			vp.loadShader(loadShader("vcr.glsl"));
			// vp2 = new Viewport(this, world, 0, height / 2, width, height /
			// 2);
			// vp2.loadShader(loadShader("vcr.glsl"));
			vp.attachTarget(player);
			vp.attachTarget(player2);
			vp.setTrackMode(1);
		}
		// player3.createShip();
		// player4.createShip();

		// test.add(player3);
		// test.add(player4);

		// test = new ArrayList<Player>();
		// test.add(player3);
		// test.add(player4);
		// world.tether(test);
		world.prepare();

		// player3.connectAudio(ac, out);
		// player4.connectAudio(ac, out);

		// vp2 = new Viewport(this, world, 0, height / 2, width, height / 2);
		// vp2.loadShader(loadShader("vcr.glsl"));

		// vp2.attachTarget(player2);
		frameTimer = System.nanoTime();
	}

	public void draw() {
		if (state == splashState) {
			background(0);
			counter++;
			if (counter == 50) {
				state = menuState;
			}

		} else if (state == menuState) {
			state = gameState;

		} else if (state == gameState) {
			if (!pause) {

				world.step();
				vp.setFade(1f);
				if (vp2 != null) {
					vp2.setFade(1f);
				}

			} else {
				vp.setFade(0.2f);
				if (vp2 != null) {
					vp2.setFade(0.2f);
				}
			}
			vp.update();
			image(vp.pg, vp.pos.x, vp.pos.y);
			if (!tether) {
				// vp2.update();
				// image(vp2.pg, vp2.pos.x, vp2.pos.y);
			}

			if (frameCounter >= 10) {
				frameCounter = 0;
				fps = 10000000 / (float) (System.nanoTime() - frameTimer);
				fps *= 1000;
				frameTimer = System.nanoTime();

			} else {
				frameCounter++;
			}
			fill(0);
			// text("FPS: " + fps, 10, 10);

		}
	}

	public void pause() {
		if (state == gameState) {

			pause = !pause;
			vp.setPause(pause);
		}
	}

	public boolean isPaused() {
		return pause;
	}

	public void exit() {
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
