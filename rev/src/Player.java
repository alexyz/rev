/*
 * Reversi
 */

import java.io.*;
import java.util.*;
import java.util.regex.*;

/**
 * An abstract game player, either human or computer.
 * All players have a fixed model and colour.
 */
public abstract class Player {
	public static final Filter value = new ValueFilter();
	public static final Filter advantage = new AdvantageFilter();
	public static final Filter mobility = new MobilityFilter();
	public static final Filter antiMobility = new AntiMobilityFilter();
	public static final Filter stability = new StabilityFilter();
	protected Model model;
	protected boolean black;
	public void init(Model model, boolean black) {
		this.model = model;
		this.black = black;
	}
	/**
	 * Return given players move as int[] { x, y }
	 */
	public abstract Move getMove();
	/**
	 * Get moves from model
	 */
	public final List<Move> getMoves() {
		return model.getMoves(black);
	}
	/**
	 * Return name of class
	 */
	public String toString() {
		return getClass().getSimpleName();
	}
	/**
	 * Returns true for human players
	 */
	public boolean isReal() {
		return false;
	}
	/**
	 * Return the highest value moves in the list
	 */
	public List<Move> filter(Filter filter, List<Move> moves) {
		if (moves.size() == 1)
			return moves;
		List<Move> ret = new ArrayList<Move>();
		int vMax = Integer.MIN_VALUE;
		for (int n = 0; n < moves.size(); n++) {
			Move move = moves.get(n);
			int v = filter.valueOf(model, move);
			if (v > vMax) {
				vMax = v;
				ret.clear();
				ret.add(move);
			} else if (v == vMax) {
				ret.add(move);
			}
		}
		//Main.out.println("Filter moves (v=" + vMax + "): " + ret);
		return ret;
	}
	public static Move random(List<Move> moves) {
		if (moves.size() == 1)
			return moves.get(0);
		int n = (int) (Math.random() * moves.size());
		return moves.get(n);
	}
}

/**
 * Computer player that makes completely random moves
 */
class RandomPlayer extends Player {
	public Move getMove() {
		return random(getMoves());
	}
}

/**
 * Computer player that makes the move to capture the most high value squares
 */
class ValuePlayer extends Player {
	public Move getMove() {
		return random(filter(value, getMoves()));
	}
}

/**
 * Computer player that plays for numerical advantage
 */
class AdvantagePlayer extends Player {
	public Move getMove() {
		return random(filter(value, filter(advantage, getMoves())));
	}
}

/**
 * Computer player that plays for mobility
 */
class MobilityPlayer extends Player {
	public Move getMove() {
		return random(filter(value, filter(mobility, getMoves())));
	}
}

/**
 * Computer player that plays for mobility
 */
class AntiMobilityPlayer extends Player {
	public Move getMove() {
		return random(filter(value, filter(antiMobility, getMoves())));
	}
}

class StabilityPlayer extends Player {
	public Move getMove() {
		return random(filter(value, filter(antiMobility, filter(stability, getMoves()))));
	}
}

/**
 * Interactive human player
 */
class HumanPlayer extends Player {
	private static final Pattern linePat = Pattern.compile(" *([a-h]) *([1-8]) *");
	public boolean isReal() {
		return true;
	}
	public Move getMove() {
		List<Move> moves = getMoves();
		Main.out.println(moves);
		while (true) {
			try {
				Main.out.print(black ? "black> " : "white> ");
				// TODO allow model manipulation commands
				String line = Main.in.readLine();
				if (line == null || (line = line.toLowerCase().trim()).equals("exit")) {
					Main.out.println("exiting");
					System.exit(0);
					return null;
					
				} else if (line.equals("debug")) {
					Filter.debug = !Filter.debug;
					Main.out.println("Debug filters: " + Filter.debug);
					
				} else if (line.equals("back")) {
					Main.out.println("live with it");
					
				} else {
					Matcher m = linePat.matcher(line);
					if (m.matches()) {
						int x = m.group(1).charAt(0) - 'a';
						int y = m.group(2).charAt(0) - '1';
						Move move = Move.getMove(moves, x, y);
						if (move != null)
							return move;
						Main.out.println("not legal");
						
					} else {
						Main.out.println("invalid command");
					}
				}
				
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
