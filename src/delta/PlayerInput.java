package delta;

import ch.aplu.xboxcontroller.*;
import processing.core.PApplet;

public class PlayerInput {
	public XboxController xc;
	PApplet parent;
	public boolean connected;
	public boolean active;
	public static int index;
	public float ang;
	public float mag;
	public boolean butA, butB, butX, butY;
	int leftVibrate=0, rightVibrate=0;
	XboxControllerAdapter test;

	PlayerInput(PApplet _pA, int _index) {
		parent = _pA;
		index = _index;
		ang = 0;
		mag = 0;
		xc = new XboxController(
				System.getProperty("sun.arch.data.model").equals("64") ? "xboxcontroller64" : "xboxcontroller", index,
				50, 50);
		active = false;
		poll();
		xc.setLeftThumbDeadZone(30);
		xc.addXboxControllerListener(new XboxControllerAdapter() {
			public void back(boolean state) {
				//System.out.println("back "+state);
				if (state) {
					if (active) {
						active = false;
					}
				}
			}

			public void start(boolean state) {
				//System.out.println("start "+state);
				if (state) {
					if (!active) {
						active = true;
					} else {
						parent.pause();
					}
				}
			}

			public void buttonA(boolean state) {
				butA = state;
			}

			public void buttonB(boolean state) {
				butB = state;
			}

			public void buttonX(boolean state) {
				butX = state;
			}

			public void buttonY(boolean state) {
				butY = state;
			}

			public void leftThumbDirection(double value) {
				//System.out.println("leftDir "+value);
				if (mag != 0) {
					ang = (float) value;
				} else {
					ang = 0;
				}
				//System.out.println("angle: " + ang);
			}

			public void leftThumbMagnitude(double value) {
				//System.out.println("LeftMag "+value);
				if (value > 0.3) {
					mag = (float) value;
				} else {
					mag = 0;
				}
			}			      
		});
	}

	public boolean poll() {
		if (xc.isConnected()) {
			connected = true;
		} else {
			connected = false;
		}
		return connected;
	}
	public void vibrateLeft(float val){
		leftVibrate = (int)(65535 * val * val);
		xc.vibrate(leftVibrate, 0);
	}
	public synchronized void rumbleRight(int val){
		//xc.vibrateRight(val);
		
	}
	public void release(){
		xc.vibrate(0, 0);
		xc.release();
	}

}
