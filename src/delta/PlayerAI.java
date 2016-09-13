package delta;

import java.io.File;

import org.encog.ml.data.basic.BasicMLData;
import org.encog.ml.data.basic.BasicMLDataSet;
import org.encog.neural.networks.BasicNetwork;
import org.encog.util.simple.EncogUtility;
import org.jbox2d.common.Vec2;

import processing.core.PApplet;

public class PlayerAI {
	Player player;
	World2D world;
	float sensorRadius;
	float sensorSpokes;
	BasicMLData inputData;
	BasicMLData lastInputData;
	BasicMLData idealData;
	BasicMLDataSet trainingSet;
	BasicNetwork network;
	RayCastAISensor sensorRay;
	RayCastRegionCallback regionRay;
	Vec2[] spoke;
	int region;
	int nextRegion;
	boolean training;
	int index = 0;
	double xVal, yVal, aBut, xBut;

	PlayerAI(World2D _world, Player _player) {
		world = _world;
		player = _player;
		sensorRadius = world.scalarPixelsToWorld(1000);
		sensorSpokes = 24;
		spoke = new Vec2[(int) sensorSpokes];
		for (int i = 0; i < sensorSpokes; i++) {
			spoke[i] = new Vec2(sensorRadius * PApplet.sin(i * PApplet.TWO_PI / sensorSpokes),
					sensorRadius * PApplet.cos(i * PApplet.TWO_PI / sensorSpokes));
		}
		training = false;
		xVal = 0;
		yVal = 0;
		aBut = 0;
		xBut = 0;
	}

	void setTraining(boolean _training) {
		training = _training;
		if (training) {
			trainingSet = new BasicMLDataSet();
		}
	}

	public void gather() {
		index = 0;
		inputData = new BasicMLData(83);
		int nextRegion = player.nextRegion;
		Vec2 loc = player.worldLoc;
		for (int i = 0; i < sensorSpokes; i++) {
			Vec2 eRay = loc.add(spoke[i]);
			sensorRay = new RayCastAISensor();
			regionRay = new RayCastRegionCallback(nextRegion);
			world.world.raycast(sensorRay, loc, eRay);
			world.regions.raycast(regionRay, loc, eRay);
			if (sensorRay.m_hit) {
				setInput(sensorRay.A);
				setInput(sensorRay.B);
			} else {
				setInput(0);
				setInput(0);
			}
			if (regionRay.m_hit) {
				setInput(regionRay.m_fraction);
			} else {
				setInput(0);
			}
		}

		setInput(xVal);
		setInput(yVal);
		setInput(aBut);
		setInput(xBut);
		setInput(player.vel.x / 100f);
		setInput(player.vel.y / 100f);
		setInput(Math.sin(player.rot));
		setInput(Math.cos(player.rot));
		if (player.magInRange) {
			setInput(player.magDist);
			setInput(player.magDiff.x / player.worldMagRadius);
			setInput(player.magDiff.y / player.worldMagRadius);
		} else {
			setInput(0);
			setInput(0);
			setInput(0);
		}		
		if (training) {
			if (lastInputData != null) {
				double[] ideal = player.getIdealOutput();
				idealData = new BasicMLData(ideal);
				xVal = ideal[0];
				yVal = ideal[1];
				aBut = ideal[2];
				xBut = ideal[3];
				trainingSet.add(lastInputData, idealData);
				//System.out.println("record count: " + trainingSet.getRecordCount());
			}
			lastInputData = inputData;
		}
	}

	private void setInput(float _in) {
		double out = _in;
		setInput(out);
	}

	private void setInput(Double _in) {
		inputData.add(index, _in);		
		index++;
	}

	public boolean saveTrainingFile(String file) {
		if (training) {
			try {
				
				EncogUtility.saveEGB(new File(file+"_"+trainingSet.size()+".egb"), trainingSet);
				return true;
			} catch (Exception ex) {
				System.out.println(ex);
			}
		}

		return false;
	}

}
