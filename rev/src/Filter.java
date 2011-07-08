/*
 * Reversi
 */

/**
 * Evaluate a move
 */
abstract class Filter {
	public static boolean debug = false;
	public abstract int valueOf(Model model, Move move);
}

/**
 * Value move by number of captured pieces
 */
class AdvantageFilter extends Filter {
	public int valueOf(Model model, Move move) {
		return move.getAdvantage();
	}
}

/**
 * Value move by mobility afterwards
 */
class MobilityFilter extends Filter {
	public int valueOf(Model model, Move move) {
		Model modelClone = model.clone();
		modelClone.move(move);
		// TODO speculate opponent moves?
		// esp by value?
		return modelClone.getMobility(move.black);
	}
}

/**
 * Value move by opponent mobility afterwards
 */
class AntiMobilityFilter extends Filter {
	public int valueOf(Model model, Move move) {
		Model modelClone = model.clone();
		modelClone.move(move);
		int v = -1 * modelClone.getMobility(!move.black);
		if (debug)
			Main.out.println("  AntiMobility of " + move + " is " + v);
		return v;
	}
}

/**
 * Value by number of stable discs captured
 */
class StabilityFilter extends Filter {
	public int valueOf(Model model, Move move) {
		byte[] cap = move.getCaptured();
		int v = 0;
		for (int m = 0; m < cap.length; m += 2)
			v += model.isStable(cap[m], cap[m+1]) ? 1 : 0;
		if (debug)
			Main.out.println("  Stability of " + move + " is " + v);
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
	private final static int[][] values = { 
		{ 7, 1, 4, 2 }, 
		{ 1, 0, 5, 3 },
		{ 4, 5, 6, 6 }, 
		{ 2, 3, 6, 7 }
	};
	/**
	 * Return value of given square
	 */
	private static int valueOf(int x, int y) {
		if (x >= 4)
			x = 7 - x;
		if (y >= 4)
			y = 7 - y;
		return values[y][x];
	}
	public int valueOf(Model model, Move move) {
		// FIXME value depends on how crowded the board is
		// and in the endgame, advantage needs to be considered
		byte[] cap = move.getCaptured();
		int v = 0;
		for (int m = 0; m < cap.length; m += 2)
			v += valueOf(cap[m], cap[m+1]);
		if (debug)
			Main.out.println("  Value of " + move + " is " + v);
		return v;
	}
}
