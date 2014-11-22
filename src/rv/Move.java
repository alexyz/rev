package rv;

import java.util.List;

/**
 * Represents a possible move (assuming x, y is free)
 */
class Move {
	/**
	 * Search list for move
	 */
	static Move getMove(List<Move> moves, int x, int y) {
		for (Move move : moves)
			if (move.x == x && move.y == y)
				return move;
		return null;
	}
	/**
	 * Starting point of move, i.e. where the disc is placed
	 */
	public final byte x, y;
	/**
	 * List of captured lines as end and delta of line as byte[] { x, y, dx, dy }.
	 */
	public final List<byte[]> lines;
	public Move(int x, int y, List<byte[]> lines) {
		this.x = (byte) x;
		this.y = (byte) y;
		this.lines = lines;
	}
	/**
	 * Return list of captured squares as byte[] { x1, y1, ..., xn, yn }
	 */
	public byte[] getCaptured() {
		byte[] ret = new byte[getAdvantage() * 2];
		int p = 0;
		// actual move
		ret[p++] = x;
		ret[p++] = y;
		
		for (byte[] line : lines) {
			int ex = line[0], ey = line[1], dx = line[2], dy = line[3];
			// for start plus delta to end
			for (int cx = x + dx, cy = y + dy; !(cx == ex && cy == ey); cx += dx, cy += dy) {
				ret[p++] = (byte) cx;
				ret[p++] = (byte) cy;
			}
		}
		return ret;
	}
	/**
	 * Return the number of discs this move will win
	 */
	public int getAdvantage () {
		int win = 1;
		for (byte[] line : lines)
			win += Math.max(Math.abs(x - line[0]), Math.abs(y - line[1])) - 1;
		return win;
	}
	@Override
	public String toString() {
		return String.format("%c%d(%d)", (char) ('a' + x), y + 1, getAdvantage());
	}
}