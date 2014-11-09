package rv;

/*
 * Reversi
 */

import java.io.*;
import java.util.*;

/**
 * Reversi main loop
 * TODO really needs a gui
 */
public class Main {
	
	public static final BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	public static final PrintStream out = System.out;

	public static void main(final String[] args) throws Exception {
		
		play(new Model(), HumanPlayer.class, AntiMobilityPlayer.class);

		// value is 5x better than random
		// value is 2x better than advantage
		// advantage is 2.5x better than random
		// mobility is 4-5x better than random
		// value is 1.25x better than mobility
		// antimob is 4x better than random
		// antimob is equal to value
		// antimob is 2-6x better than advantage
		// antimob is 2-11x better than mobility
		
		// TODO thread this...
		// TODO allow all players, display ranking
		//autoplay(RandomPlayer.class, ValuePlayer.class);
		//autoplay(ValuePlayer.class, RandomPlayer.class);
	}

	private static void autoplay(Class<? extends Player> blackClass, Class<? extends Player> whiteClass) throws Exception {
		
		int[] s = new int[3];
		int games = 2000;
		
		for (int n = 0; n < games; n++) {
			Model model = new Model();
			Player black = blackClass.newInstance();
			black.init(model, true);
			Player white = whiteClass.newInstance();
			white.init(model, false);
			autoplay(s, model, black, white);
		}
		
		out.printf("%s as black wins: %d and draws: %d\n", blackClass, s[1], s[0]);
		out.printf("%s as white wins: %d and draws: %d\n", whiteClass, s[2], s[0]);
	}

	private static void autoplay(int[] score, Model m, Player black, Player white) {
		boolean blackMove = true;
		while (true) {
			Player player = blackMove ? black : white;
			if (m.pass(blackMove)) {
				if (m.pass(!blackMove)) {
					Player winner = m.getWinner(black, white);
					score[winner == null ? 0 : winner == black ? 1 : 2]++;
					return;
				}
			} else {
				m.move(player.getMove());
			}
			blackMove = !blackMove;
		}
	}
	
	public static byte play(Model model, Class<? extends Player> blackClass, Class<? extends Player> whiteClass) throws Exception {
		Player black = blackClass.newInstance();
		black.init(model, true);
		Player white = whiteClass.newInstance();
		white.init(model, false);
		
		out.println("Black: " + black);
		out.println("White: " + white);
		
		boolean blackMove = true;
		
		while (true) {
			Player player = blackMove ? black : white;
			
			if (model.pass(blackMove)) {
				// player must pass
				if (model.pass(!blackMove)) {
					// game over
					out.println();
					out.println(model);
					out.println();
					Player winner = model.getWinner(black, white);
					if (winner != null)
						out.println("WINNER: " + winner + " as " + (winner == black ? "black" : "white"));
					else
						out.println("DRAW");
					return winner == black ? Model.BLACK : winner == white ? Model.WHITE : 0;
				}
				out.println(player + " must pass");
				
			} else if (player.isReal()) {
				// human player, show model state
				out.println();
				out.println(model);
				Move move = player.getMove();
				model.move(move);

			} else {
				// computer player
				out.println();
				out.println(model);
				Move move = player.getMove();
				out.println(player + " moves to " + move);
				model.move(move);
			}
			
			blackMove = !blackMove;
		}
		
		// TODO some kind of prompt for going backwards, changing players etc
	}
	
	public static <T> List<T> add(List<T> list, T obj) {
		if (obj != null) {
			if (list == null)
				list = new ArrayList<T>();
			list.add(obj);
		}
		return list;
	}
	
}
