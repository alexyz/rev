package rv;

/*
 * Reversi
 */

import java.util.*;

/**
 * Reversi board and rules model
 * 
 * TODO move move history, mobility history, stability history, population history, goto move
 */
public class Model {

	public static final byte BLACK = 1, WHITE = 2, DISC = 3, STABLE = 4;
	/**
	 * Direction deltas as { north dx, north dy, northeast dx, northeast dy, ... }
	 */
	private static final byte[][] dirs = {{0, -1}, {1, -1}, {1, 0}, {1, 1}, {0, 1}, {-1, 1}, {-1, 0}, {-1, -1}};
	
	/**
	 * The board. Bit 0 = black, bit 1 = white, bit 2 = stable.
	 */
	private final byte[][] board = new byte[8][8];
	private final List<Move> moves = new ArrayList<>();
	
	private boolean blackMove;;

	public Model() {
		set(3, 3, WHITE);
		set(4, 3, BLACK);
		set(3, 4, BLACK);
		set(4, 4, WHITE);
		blackMove = true;
		updateMoves();
	}

	public Model (Model other) {
		for (int n = 0; n < board.length; n++) {
			board[n] = other.board[n].clone();
			moves.addAll(other.moves);
			blackMove = other.blackMove;
		}
	}
	
	@Override
	public Model clone() {
		return new Model(this);
	}
	
	/**
	 * Make the given move
	 */
	public void move(Move move) {
		if (!moves.contains(move)) {
			throw new RuntimeException();
		}
		int sx = move.x, sy = move.y;
		for (byte[] line : move.lines) {
			int ex = line[0], ey = line[1], dx = line[2], dy = line[3];
			for (int x = sx, y = sy; !(x == ex && y == ey); x += dx, y += dy) {
				set(x, y, blackMove);
			}
		}
		blackMove = !blackMove;
		updateMoves();
	}
	
	/**
	 * Return true if given player must pass
	 */
	public boolean isPass() {
		return moves.size() == 0;
	}
	
	public void pass () {
		if (moves.size() > 0) {
			throw new RuntimeException();
		}
		blackMove = !blackMove;
		updateMoves();
	}
	
	/**
	 * Get a list of legal moves for the given colour, may return empty list,
	 * never null
	 */
	public List<Move> getMoves() {
		return moves;
	}
	
	private void updateMoves() {
		moves.clear();
		for (int x = 0; x < 8; x++) {
			for (int y = 0; y < 8; y++) {
				if (get(x,y) != 0)
					continue;
				Move move = getMove(x, y);
				if (move != null) {
					moves.add(move);
				}
			}
		}
	}
	
	public int getMobility() {
		return moves.size();
	}

	/**
	 * Create a Move to the specified square. Returns null if the move would not
	 * capture any pieces.
	 */
	private Move getMove(int sx, int sy) {
		List<byte[]> lines = null;
		
		for (int n = 0; n < dirs.length; n++) {
			byte[] line = getLine(sx, sy, dirs[n][0], dirs[n][1]);
			if (line != null) {
				if (lines == null) {
					lines = new ArrayList<byte[]>();
				}
				lines.add(line);
			}
		}
		
		return lines != null ? new Move(sx, sy, lines) : null;
	}
	
	/**
	 * Return line specification of possible move as byte[4] { x, y, dx, dy }, or null
	 */
	private byte[] getLine(int sx, int sy, int dx, int dy) {
		byte src = blackMove ? BLACK : WHITE;
		byte tgt = blackMove ? WHITE : BLACK;
		boolean tgtFound = false;
		for (int x = sx + dx, y = sy + dy; x >= 0 && x < 8 && y >= 0 && y < 8; x += dx, y += dy) {
			byte s = get(x, y);
			if (tgtFound && s == src) {
				return new byte[] { (byte) x, (byte) y, (byte) dx, (byte) dy };
			} else if (s == tgt) {
				tgtFound = true;
			} else {
				return null;
			}
		}
		return null;
	}
	
	public byte get(int x, int y) {
		return (byte) (board[y][x] & DISC);
	}
	
	private void set(int x, int y, byte sq) {
		board[y][x] = sq;
		moves.clear();
	}

	private void set(int x, int y, boolean black) {
		set(x, y, black ? BLACK : WHITE);
	}
	
	public boolean blackMove () {
		return blackMove;
	}
	
	public boolean isStable (int x, int y) {
		return (get(x, y) & STABLE) != 0;
	}
	
	/**
	 * Get square population count as int[] { free, BLACK, WHITE }
	 */
	public Count getCount () {
		int b = 0, w = 0, f = 0;
		for (int x = 0; x < 8; x++) {
			for (int y = 0; y < 8; y++) {
				byte s = get(x, y);
				if ((s & BLACK) != 0) {
					b++;
				} else if ((s & WHITE) != 0) {
					w++;
				} else {
					f++;
				}
			}
		}
		return new Count(b, w, f);
	}
	
	public static class Count {
		public final int black;
		public final int white;
		public final int free;
		public Count (int black, int white, int free) {
			this.black = black;
			this.white = white;
			this.free = free;
		}
		@Override
		public String toString () {
			return "Count[black=" + black + " white=" + white + " free=" + free + "]";
		}
	}
	
	/**
	 * Return string representation of board
	 * TODO highlight last move
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("   a b c d e f g h \n");
		sb.append("  +----------------+\n");
		for (int y = 0; y < 8; y++) {
			sb.append(y + 1).append(" |");
			for (int x = 0; x < 8; x++) {
				sb.append(toString(x, y, true)).append(" ");
			}
			sb.append("| ").append(y + 1).append("\n");
		}
		sb.append("  +----------------+\n");
		sb.append("   a b c d e f g h \n");
		sb.append(getCount());
		return sb.toString();
	}
	
	/**
	 * Get string representing square
	 */
	private String toString(int x, int y, boolean black) {
		byte square = get(x, y);
		boolean stable = false;
		boolean legal = Move.getMove(moves, x, y) != null;
		if (square == WHITE)
			return stable ? "O" : "o";
		if (square == BLACK)
			return stable ? "X" : "x";
		if (legal)
			return "~";
		return ".";
	}
	
	/**
	 * Get string representing move
	 */
	public static String toString(int x, int y) {
		return String.format("%c%d", (char) ('a' + x), y + 1);
	}


}

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
		return String.format("%c%d: %d", (char) ('a' + x), y + 1, getAdvantage());
	}
}
