package delta;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;


import org.jbox2d.common.Vec2;
import processing.core.PGraphics;

public class Ghost extends Container {
	World2D world;
	int index;
	int lastFrame = 0;
	boolean finished;
	float time;
	String file;
	String map;
	String name;
	GhostBlock next;
	int block = 3600;
	float r = 12f;
	float fade=0;
	LocRot[] locRot;

	Ghost(World2D _world, int _index, String _file, String _map) {
		file = _file;
		world = _world;
		type = "ghost";
		index = _index;
		map = _map;
		finished = false;
		name = "";
		locRot = new LocRot[block];
	}

	Ghost(World2D _world, byte[] bytes) {
		world = _world;
		type = "ghost";
		getFromBytes(bytes);
	}

	public void add(int _frame, LocRot lr) {
		if (_frame < 0)
			_frame = 0;
		if (!finished || _frame <= lastFrame) {
			if (_frame < block) {
				locRot[_frame] = new LocRot(lr.loc, lr.rot);
			} else {
				_frame -= block;
				if (next == null) {
					next = new GhostBlock(block);
				}
				next.add(_frame, lr);
			}
		}
	}

	public LocRot get(int _frame) {
		if (_frame < 0)
			_frame = 0;
		if ((!finished || _frame <= lastFrame)) {
			if (_frame < block) {
				return locRot[_frame];
			} else {
				_frame -= block;
				if (next != null) {
					return next.get(_frame);
				}
			}
		}
		return null;
	}

	public void draw(PGraphics pG, int _frame) {
		// System.out.println("draw Ghost at frame "+_frame+" of "+lastFrame);
		
		if (_frame < 0)
			_frame = 0;
		if (_frame > lastFrame-1) {
			_frame = lastFrame-1;
			fade += 0.2;
		}else{
			fade=0;
		}
		if (4 - fade > 0) {
			LocRot lr = get(_frame);
			// System.out.println("x: "+lr.loc.x+" y: "+lr.loc.y);
			if (lr != null) {
				if (lr.hasValue) {
					pG.pushMatrix();
					pG.translate(lr.loc.x, lr.loc.y);
					pG.rotate(lr.rot);
					pG.strokeWeight(4-fade);
					pG.stroke(255);
					pG.noFill();
					pG.arc(0, 0, 2 * r, 2 * r, 0, 3.14f);
					pG.stroke(World2D.cl[index]);
					pG.arc(0, 0, 2 * r, 2 * r, -3.14f, 0);
					pG.popMatrix();
				}

			}
		}
	}

	public void finish(int _frame, float _time) {
		time = _time;
		finished = true;
		lastFrame = _frame;
	}

	public byte[] getBytes() {
		if (finished) {
			ByteArrayOutputStream bas = new ByteArrayOutputStream();
			DataOutputStream ds = new DataOutputStream(bas);
			try {
				ds.writeChars(file + ">");
				ds.writeChars(map + ">");
				ds.writeChars(name + ">");
				ds.writeInt(index);
				ds.writeFloat(time);
				ds.writeInt(lastFrame);
				for (int i = 0; i < lastFrame; i++) {
					LocRot lr = get(i);
					ds.writeFloat(lr.loc.x);
					ds.writeFloat(lr.loc.y);
					ds.writeFloat(lr.rot);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			byte[] out = bas.toByteArray();
			long number = (long) Math.floor(Math.random() * 900000000L) + 10000000L;			
			world.pA.saveBytes(map+"_"+String.valueOf(number)+"test.ghost", out);
			return bas.toByteArray();
		}
		return null;
	}

	public void getFromBytes(byte[] buffer) {
		ByteArrayInputStream bas = new ByteArrayInputStream(buffer);
		DataInputStream ds = new DataInputStream(bas);		
		try {
			file = "";
			boolean end = false;
			do {
				char c = ds.readChar();
				if (c == '>') {
					end = true;
				} else {
					file += c;
				}
			} while (!end);
			map = "";
			end = false;
			do {
				char c = ds.readChar();
				if (c == '>') {
					end = true;
				} else {
					map += c;
				}
			} while (!end);
			name = "";
			end = false;
			do {
				char c = ds.readChar();
				if (c == '>') {
					end = true;
				} else {
					name += c;
				}
			} while (!end);
			index = ds.readInt();
			time = ds.readFloat();
			lastFrame = ds.readInt();
			block = lastFrame+1;
			locRot = new LocRot[block];
			for (int i = 0; i < lastFrame; i++) {
				float x = ds.readFloat();
				float y = ds.readFloat();
				float r = ds.readFloat();
				add(i, new LocRot(new Vec2(x, y), r));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finished = true;
		System.out.println(":" + file + ":" + map + ":" + name + ":" + index + ":" + time + ":" + lastFrame);
	}

}

class GhostBlock {
	GhostBlock next;
	int block;

	LocRot[] locRot;

	GhostBlock(int _block) {
		block = _block;
		locRot = new LocRot[block];
	}

	public void add(int _frame, LocRot lr) {
		if (_frame < 0)
			_frame = 0;
		if (_frame < block) {
			locRot[_frame] = new LocRot(lr.loc, lr.rot);
		} else {
			_frame -= block;
			if (next == null) {
				next = new GhostBlock(block);
			}
			next.add(_frame, lr);
		}
	}

	public LocRot get(int _frame) {
		if (_frame < 0)
			_frame = 0;
		if (_frame < block) {
			return locRot[_frame];
		} else {
			_frame -= block;
			if (next != null) {
				return next.get(_frame);
			}
		}
		return null;
	}
}

class LocRot {
	float rot = 0;
	Vec2 loc = new Vec2(0, 0);
	boolean hasValue = false;

	LocRot(Vec2 _loc, float _rot) {
		loc.set(_loc);
		rot = _rot;
		hasValue = true;
	}
}
