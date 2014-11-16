package rv;

import java.util.*;

import static rv.Filter.*;

/**
 * An abstract game player, either human or computer.
 * All players have a fixed model and colour.
 */
public abstract class Player {
	
	/**
	 * Return given players move as int[] { x, y }
	 */
	public abstract Move getMove(Model model);
	/**
	 * Return name of class
	 */
	@Override
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
	public List<Move> filter(Filter filter, Model model, List<Move> moves) {
		if (moves.size() <= 1) {
			return moves;
		}
		List<Move> ret = new ArrayList<>();
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
	/**
	 * Return the highest value moves in the list
	 */
	public List<Move> filter(Model model, BoardFilter filter) {
		List<Move> moves = model.getMoves();
		if (moves.size() == 1)
			return moves;
		List<Move> ret = new ArrayList<>();
		int vMax = Integer.MIN_VALUE;
		for (Move move : moves) {
			Model m2 = model.clone();
			m2.move(move);
			List<Move> moves2 = m2.getMoves();
			if (moves2.size() > 0) {
				for (Move move2 : moves2) {
					Model m3 = m2.clone();
					m3.move(move2);
					int v = filter.valueOf(m3);
					if (v > vMax) {
						vMax = v;
						ret.clear();
						ret.add(move);
					} else if (v == vMax) {
						ret.add(move);
					}
				}
				
			}
		}
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
	@Override
	public Move getMove(Model model) {
		return random(model.getMoves());
	}
}

/**
 * Computer player that makes the move to capture the most high value squares
 */
class ValuePlayer extends Player {
	@Override
	public Move getMove(Model model) {
		return random(filter(value, model, model.getMoves()));
	}
}

/**
 * Computer player that plays for numerical advantage
 */
class AdvantagePlayer extends Player {
	@Override
	public Move getMove(Model model) {
		return random(filter(advantage, model, model.getMoves()));
	}
}

class AntiMobilityPlayer extends Player {
	@Override
	public Move getMove(Model model) {
		return random(filter(antiMobility, model, model.getMoves()));
	}
}

class AdvantagePlayer2 extends Player {
	@Override
	public Move getMove(Model model) {
		return random(filter(advantage2, model, model.getMoves()));
	}
}

class ValuePlayer2 extends Player {
	@Override
	public Move getMove(Model model) {
		return random(filter(value2, model, model.getMoves()));
	}
}

class AdvantageBoardPlayer extends Player {
	@Override
	public Move getMove (Model model) {
		return filter(model, BoardFilter.advantageBoard);
	}
}