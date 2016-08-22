package delta;

import java.util.ArrayList;

import processing.core.PGraphics;

public class Scoreboard {
	PGraphics board;
	boolean common;
	World2D world;
	ArrayList<ScoreEntry> scores;

	Scoreboard(World2D _world) {
		world = _world;
		scores = new ArrayList<ScoreEntry>();
	}
}

class ScoreEntry {
	ArrayList<Player> player;
	float time;
	int numPlayers;

	ScoreEntry(Player _player, float _time) {
		player = new ArrayList<Player>();
		player.add(_player);
		numPlayers = 1;
		time = _time;
	}

	ScoreEntry(ArrayList<Player> _players, float _time) {
		player = new ArrayList<Player>();
		player.addAll(_players);
		numPlayers = player.size();
		time = _time;
	}
}
