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
	public List<Move> filter(final Model model, final BoardFilter filter) {
		final List<Move> moves = model.getMoves();
		if (moves.size() <= 1) {
			System.out.println("board filter: forced move " + moves);
			return moves;
		}
		
		int retMax = Integer.MIN_VALUE;
		final List<Move> ret = new ArrayList<>();
		
		// for every move
		for (Move move : moves) {
			Model model2 = model.clone();
			model2.move(move);
			int value;
			
			if (model2.isPass()) {
				model2.pass();
				value = filter.valueOf(model2);
				
			} else {
				// find least worst move
				List<Move> oppmoves = model2.getMoves();
				int vMin = Integer.MAX_VALUE;
				for (Move oppmove : oppmoves) {
					Model m3 = model2.clone();
					m3.move(oppmove);
					int v = filter.valueOf(m3);
					if (v < vMin) {
						vMin = v;
					}
				}
				value = vMin;
			}
			
			System.out.println("value of move " + move + " is " + value);
			
			if (value > retMax) {
				retMax = value;
				ret.clear();
			}
			if (value >= retMax) {
				ret.add(move);
			}
		}
		
		System.out.println("move is " + ret);
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

class BoardPlayer extends Player {
	private final BoardFilter f;
	public BoardPlayer(BoardFilter f) {
		this.f = f;
	}
	@Override
	public Move getMove (Model model) {
		return random(filter(model, f));
	}
	@Override
	public String toString() {
		return f.getClass().getSimpleName();
	}
}
