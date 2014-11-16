package rv;

import java.util.List;

import rv.Model.Count;

/*
 * Reversi
 */

/**
 * Evaluate a move
 */
abstract class Filter {
	public static final Filter value = new ValueFilter();
	public static final Filter value2 = new LookAheadFilter(new ValueFilter());
	public static final Filter advantage = new AdvantageFilter();
	public static final Filter advantage2 = new LookAheadFilter(new AdvantageFilter());
	public static final Filter antiMobility = new AntiMobilityFilter();
	public static final Filter stability = new StabilityFilter();
	/** return value of move on given model */
	public abstract int valueOf(Model model, Move move);
}

/**
 * Value move by number of captured pieces
 */
class AdvantageFilter extends Filter {
	@Override
	public int valueOf(Model model, Move move) {
		return move.getAdvantage();
	}
}

class AdvantageBoardFilter extends BoardFilter {
	@Override
	public int valueOf(Model model) {
		Count c = model.getCount();
		return model.blackMove() ? c.black : c.white;
	}
}

/**
 * Value move by mobility afterwards
 */
class MobilityBoardFilter extends BoardFilter {
	@Override
	public int valueOf(Model model) {
		return model.getMobility();
	}
}

/**
 * Value move by opponent mobility afterwards
 */
class AntiMobilityFilter extends Filter {
	@Override
	public int valueOf(Model model, Move move) {
		Model modelClone = model.clone();
		modelClone.move(move);
		int v = -1 * modelClone.getMobility();
		return v;
	}
}

/**
 * Value by number of stable discs captured
 */
class StabilityFilter extends Filter {
	@Override
	public int valueOf(Model model, Move move) {
		byte[] cap = move.getCaptured();
		int v = 0;
		for (int m = 0; m < cap.length; m += 2)
			v += model.isStable(cap[m], cap[m+1]) ? 1 : 0;
		return v;
	}
}

/**
 * Value move by location of discs captured
 */
class ValueFilter extends Filter {
	/**
	 * Value of upper left squares
	 */
	protected final static int[][] values = { 
		{ 7, 1, 4, 2 }, 
		{ 1, 0, 5, 3 },
		{ 4, 5, 6, 6 }, 
		{ 2, 3, 6, 7 }
	};
	/**
	 * Return value of given square
	 */
	protected static int valueOf(int x, int y) {
		if (x >= 4)
			x = 7 - x;
		if (y >= 4)
			y = 7 - y;
		return values[y][x];
	}
	@Override
	public int valueOf(Model model, Move move) {
		// value depends on how crowded the board is
		// and in the end game, advantage needs to be considered
		byte[] cap = move.getCaptured();
		int v = 0;
		for (int m = 0; m < cap.length; m += 2)
			v += valueOf(cap[m], cap[m+1]);
		return v;
	}
}

class LookAheadFilter extends Filter {
	private final Filter f;
	public LookAheadFilter (Filter f) {
		this.f = f;
	}
	@Override
	public int valueOf(Model model, Move move) {
		int v = f.valueOf(model, move);
		Model model2 = model.clone();
		model2.move(move);
		List<Move> moves2 = model2.getMoves();
		int ov = 0;
		for (Move move2 : moves2) {
			ov = Math.max(ov, f.valueOf(model, move2));
		}
		return v - ov;
	}
}
