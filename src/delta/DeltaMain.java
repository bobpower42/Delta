package delta;

import java.io.File;
import java.io.FilenameFilter;

import beads.AudioContext;
import beads.Gain;
import beads.Plug;
import beads.Reverb;
import processing.core.PApplet;
import processing.core.PFont;
import processing.data.XML;
import processing.opengl.PShader;


public class DeltaMain extends PApplet {
	//player colours
	public static int[] cl = { -34048, -16740353, -65413, -8716033 };	
	private PlayerInput[] p;
	Player player, player2;
	private int splashState = 0;
	private int menuState = 1;
	private int gameState = 2;
	private int state = 0;
	public int frame = 0;
	private boolean pause = false;
	private String packFolder;
	public World2D world;
	Viewport vp, vp2;
	AudioContext ac;
	Gain master;
	Plug out;	
	long frameTimer;
	int frameCounter = 0;
	float fps = 0;
	PFont font;
	float frameRate=60f;

	public static void main(String[] args) {
		String[] a = { "MAIN" };
		PApplet.runSketch(a, new DeltaMain());
	}

	static boolean is64bit() {
		return System.getProperty("sun.arch.data.model").equals("64");
	}

	public void settings() {
		p = new PlayerInput[4];
		for (int i = 0; i < 4; i++) {
			p[i] = new PlayerInput(this, i + 1);
		}
		fullScreen(P2D);	//openGl		
		smooth(0);	 //turn off anti-aliasing, can do our own with glsl filter	
	}

	public void setup() {
		font=loadFont("Avant-GardeBoldT.-48.vlw");
		textFont(font,24);
		textAlign(LEFT,TOP);
		ac = new AudioContext();
		out = new Plug(ac);
		master = new Gain(ac, 1, 2.5f);
		master.addInput(out);
		ac.out.addInput(master);
		ac.start();
		noCursor();
		frameRate(frameRate);
		world = new World2D(frameRate, out);
		world.setScale(60f);
		packFolder = System.getProperty("user.dir") + "\\packs";
		File folder = new File(packFolder);

		File[] files = folder.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String filename) {
				return filename.endsWith(".xml");
			}
		});
		for (int i = 0; i < files.length; i++) {
			println(files[i].toString());
		}
		// XML pack=loadXML("C:/Users/Bob/4PG/Delta/packs/WhiteMap1.xml");
		XML pack = loadXML(files[0].getAbsolutePath());

		world.loadfromXML(pack, "001_rails");
		player = new Player(world, p[0], 0);
		player2 = new Player(world, p[1], 1);
		player.createShip();
		player2.createShip();
		//player.tether(player2);
		player.connectAudio(ac, out);
		player2.connectAudio(ac, out);
		vp = new Viewport(this, world, 0, 0, width, height/2);
		vp.loadShader(loadShader("vcr.glsl"));
		vp2 = new Viewport(this, world, 0, height / 2, width, height / 2);
		vp2.loadShader(loadShader("vcr.glsl"));
		vp.attachTarget(player);
		//vp.attachTarget(player2);
		
		vp2.attachTarget(player2);
		frameTimer = System.nanoTime();
	}

	public void draw() {
		if (state == splashState) {
			//long startFrame=System.nanoTime();
			world.step();
			//long gameStep=System.nanoTime()-startFrame;			
			vp.update();
			vp2.update();			
			image(vp.pg, vp.pos.x, vp.pos.y);
			image(vp2.pg, vp2.pos.x, vp2.pos.y);
			//long viewportDraw=System.nanoTime()-startFrame-gameStep;
			//println("Game: "+gameStep+" Draw: "+viewportDraw+" G/D Ratio: "+(float)gameStep/(float)viewportDraw);
			if (frameCounter >= 10) {
				frameCounter = 0;
				fps = 10000000 / (float) (System.nanoTime() - frameTimer);
				fps *= 1000;
				frameTimer = System.nanoTime();
				
			} else {
				frameCounter++;
			}
			//fill(0);
			//text("FPS: " + fps, 10,10);

		} else if (state == menuState) {

		} else if (state == gameState) {
			if (!pause) {
			} else {

			}
		}
	}

	public void pause() {
		if (state == gameState) {
			pause = !pause;
		}
	}

	public void exit() {		
		for (PlayerInput pi : p) {
			pi.release();
		}
		ac.stop();
		surface.stopThread();
		
		ac = null;		
		System.out.println("stopping");
		super.exit();
		System.exit(0);
	}

}
