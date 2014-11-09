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
	private final List<Move> blackMoves = new ArrayList<Move>(), whiteMoves = new ArrayList<Move>();
	private boolean doneStable = false;

	public Model() {
		set(3, 3, WHITE);
		set(4, 3, BLACK);
		set(3, 4, BLACK);
		set(4, 4, WHITE);
		/*
		set(0, 0, BLACK);
		
		set(6, 0, BLACK);
		set(7, 0, BLACK);
		set(6, 1, WHITE);
		set(7, 1, BLACK);
		
		set(0, 6, WHITE);
		set(1, 6, WHITE);
		set(0, 7, WHITE);
		set(1, 7, WHITE);
		
		set(6, 6, BLACK);
		set(6, 7, WHITE);
		set(7, 6, WHITE);
		set(7, 7, BLACK);
		*/
	}

	public Model (Model other) {
		for (int n = 0; n < 8; n++)
			board[n] = other.board[n].clone();
		// don't copy moves
	}
	
	public Model clone() {
		return new Model(this);
	}
	
	/**
	 * Make the given move
	 */
	public void move(Move move) {
		if (!move(move.black, move.x, move.y))
			throw new RuntimeException();
	}
	
	/**
	 * Make a speculative move, return true if move succeeded.
	 */
	public boolean move(boolean black, int x, int y) {
		if (get(x,y) != 0) {
			Main.out.println("occupied");
			return false;
		}
		List<Move> moves = getMoves(black);
		Move move = Move.getMove(moves, x, y);
		if (move == null) {
			Main.out.println("illegal");
			return false;
		}
		set(move);
		return true;
	}
	
	/**
	 * Set discs as per given move
	 */
	private void set(Move move) {
		int sx = move.x, sy = move.y;
		for (byte[] line : move.lines) {
			int ex = line[0], ey = line[1], dx = line[2], dy = line[3];
			for (int x = sx, y = sy; !(x == ex && y == ey); x += dx, y += dy)
				set(x, y, move.black);
		}
	}
	
	/**
	 * Return true if given player must pass
	 */
	public boolean pass(boolean black) {
		return getMobility(black) == 0;
	}
	
	/**
	 * Get a list of legal moves for the given colour, may return empty list,
	 * never null
	 */
	public List<Move> getMoves(boolean black) {
		List<Move> moves = black ? blackMoves : whiteMoves;
		if (moves.size() == 0) {
			for (int x = 0; x < 8; x++) {
				for (int y = 0; y < 8; y++) {
					if (get(x,y) != 0)
						continue;
					Move move = getMove(black, x, y);
					if (move != null)
						moves.add(move);
				}
			}
		}
		return moves;
	}
	
	public int getMobility(boolean black) {
		return getMoves(black).size();
	}

	/**
	 * Create a Move to the specified square. Returns null if the move would not
	 * capture any pieces.
	 */
	private Move getMove(boolean black, int sx, int sy) {
		List<byte[]> lines = null;
		
		for (int n = 0; n < dirs.length; n++) {
			byte[] line = getLine(black, sx, sy, dirs[n][0], dirs[n][1]);
			if (line != null) {
				if (lines == null)
					lines = new ArrayList<byte[]>(8);
				lines.add(line);
			}
		}
		
		return lines != null ? new Move(black, sx, sy, lines) : null;
	}
	
	/**
	 * Return line specification of possible move as byte[4] { x, y, dx, dy }, or null
	 */
	private byte[] getLine(boolean black, int sx, int sy, int dx, int dy) {
		byte src = black ? BLACK : WHITE;
		byte tgt = black ? WHITE : BLACK;
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
		blackMoves.clear();
		whiteMoves.clear();
		doneStable = false;
	}

	private void set(int x, int y, boolean black) {
		set(x, y, black ? BLACK : WHITE);
	}
	
	public Player getWinner(Player black, Player white) {
		int[] c = getCount();
		if (c[BLACK] > c[WHITE])
			return black;
		else if (c[WHITE] > c[BLACK])
			return white;
		else
			return null;
	}
	
	/**
	 * Get square population count as int[] { free, BLACK, WHITE }
	 */
	public int[] getCount() {
		int[] c = new int[3];
		for (int x = 0; x < 8; x++)
			for (int y = 0; y < 8; y++)
				c[get(x,y)]++;
		return c;
	}
	
	/**
	 * Return true if a line is stable in the given direction
	 */
	private boolean isLineStable(int sx, int sy, int dx, int dy) {
		byte startSq = get(sx, sy);
		for (int x = sx + dx, y = sy + dy; x >= 0 && x < 8 && y >= 0 && y < 8; x += dx, y += dy) {
			/*
			boolean bl = board[y][x] != s;
			boolean ins = !stable[y][x];
			Main.out.printf("Line from %d,%d dir %d,%d at %d,%d: %s\n",
					sx, sy, dx, dy, x, y, bl ? "blocked" : ins ? "unstable" : "ok");
					*/
			int sq = board[y][x];
			if (startSq != (sq & DISC) || (sq & STABLE) == 0)
				return false;
		}
		return true;
	}
	
	private boolean isSquareStable(int x, int y) {
		if (get(x, y) == 0)
			return false;
		
		int s = 0, t = 0;
		for (int n = 0; n < 8 && s < 4; n++) {
			if (isLineStable(x, y, dirs[n][0], dirs[n][1])) {
				s++;
				if (t == n)
					t++;
				
			} else {
				// TODO give up if can never find enough
				s = 0;
			}
		}
		//Main.out.println("disc " + toString(x, y) + " stable: " + (s >= 4 || s + t >= 4));
		return s >= 4 || s + t >= 4;
	}
	
	/**
	 * Return true if given disc is stable
	 */
	public boolean isStable(int sx, int sy) {
		if ((board[sy][sx] & STABLE) != 0)
			return true;
		
		if (!doneStable) {
			boolean changed;
			int p = 1;
			do {
				// FIXME edge line full -> stable
				
				//Main.out.println("pass " + p++);
				changed = false;
				for (int y = 0; y < 8; y++) {
					for (int x = 0; x < 8; x++) {
						if ((board[y][x] & STABLE) == 0) {
							if (isSquareStable(x, y)) {
								board[y][x] |= STABLE;
								changed = true;
							}
						}
					}
				}
			} while (changed);
			doneStable = true;
		}
		
		return (board[sy][sx] & STABLE) != 0;
	}
	
	public String getCountString() {
		int[] c = getCount();
		return String.format("free: %d  black: %d  white: %d", c[0], c[BLACK], c[WHITE]);
	}
	
	/**
	 * Return string representation of board
	 * TODO highlight last move
	 */
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
		sb.append(getCountString());
		return sb.toString();
	}
	
	/**
	 * Get string representing square
	 */
	private String toString(int x, int y, boolean black) {
		byte square = get(x, y);
		boolean stable = isStable(x, y);
		boolean legal = Move.getMove(getMoves(black), x, y) != null;
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
	public final boolean black;
	/**
	 * Starting point of move, i.e. where the disc is placed
	 */
	public final byte x, y;
	/**
	 * List of captured lines as end and delta of line as byte[] { x, y, dx, dy }.
	 */
	public final List<byte[]> lines;
	Move(boolean black, int x, int y, List<byte[]> lines) {
		this.black = black;
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
	public String toString() {
		return String.format("%c%d: %d", (char) ('a' + x), y + 1, getAdvantage());
	}
}
