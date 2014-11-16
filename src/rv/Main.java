package rv;

/*
 * Reversi
 */

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Reversi main loop
 * TODO really needs a gui
 */
public class Main {
	
	public static void main(final String[] args) throws Exception {
		
		//play(new Model(), HumanPlayer.class, AntiMobilityPlayer.class);

		// advantage  is 1.94 better than random
		// value      is 2.53 better than random
		// mobility   is 2.56 better than random
		// stability  is 2.99 better than random
		// antimob    is 3.00 better than random
		// advantage2 is 3.29 better than random
		// value2     is 4.29 better than random
		
		// value is 2x better than advantage
		// value is 1.25x better than mobility
		// antimob is equal to value
		// antimob is 2-6x better than advantage
		// antimob is 2-11x better than mobility
		
		// TODO thread this...
		// TODO allow all players, display ranking
		test (ValuePlayer2.class, AdvantagePlayer2.class);
	}
	
	private static void test (final Class<? extends Player> p1c, final Class<? extends Player> p2c) throws Exception {
		final float[] p1w = new float[1];
		final float[] p2w = new float[1];
		final float[] d = new float[1];
		ExecutorService e = Executors.newFixedThreadPool(8);
		final CountDownLatch l = new CountDownLatch(8);
		for (int n = 0; n < 8; n++) {
			e.execute(new Runnable() {
				@Override
				public void run () {
					try {
						Player p1 = p1c.newInstance();
						Player p2 = p2c.newInstance();
						autoplay(p1, p1w, p2, p2w, d);
						autoplay(p2, p2w, p1, p1w, d);
						System.out.println("x");
						l.countDown();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		}
		l.await();
		e.shutdown();
		System.out.println(p1c.getSimpleName() + " = " + p1w[0] + ", " + (p1w[0] / (p2w[0] + d[0])));
		System.out.println(p2c.getSimpleName() + " = " + p2w[0] + ", " + (p2w[0] / (p1w[0] + d[0])));
		System.out.println("draw = " + d[0]);
	}

	private static void autoplay(Player black, float[] bw, Player white, float[] ww, float[] d) {
		int games = 10000;
		for (int n = 0; n < games; n++) {
			Model model = new Model();
			autoplay(model, black, bw, white, ww, d);
		}
	}

	private static void autoplay(Model m, Player black, float[] bw, Player white, float[] ww, float[] d) {
		while (true) {
			Player player = m.blackMove() ? black : white;
			if (m.isPass()) {
				m.pass();
				if (m.isPass()) {
					int bd = m.getBlackDiscs();
					int wd = m.getWhiteDiscs();
					if (bd > wd) {
						synchronized (bw) {
							bw[0]++;
						}
					} else if (wd > bd) {
						synchronized (ww) {
							ww[0]++;
						}
					} else {
						synchronized (d) {
							d[0]++;
						}
					}
					return;
				}
			} else {
				m.move(player.getMove(m));
			}
		}
	}
	
	public static void play(Model model, Player black, Player white) {
		System.out.println("Black: " + black);
		System.out.println("White: " + white);
		
		while (true) {
			Player player = model.blackMove() ? black : white;
			
			if (model.isPass()) {
				System.out.println(player + " must pass");
				model.pass();
				// player must pass
				if (model.isPass()) {
					System.out.println(player + " must pass");
					// game over
					System.out.println();
					System.out.println(model);
					System.out.println();
					return;
				}
				
			} else if (player.isReal()) {
				// human player, show model state
				System.out.println();
				System.out.println(model);
				Move move = player.getMove(model);
				model.move(move);

			} else {
				// computer player
				System.out.println();
				System.out.println(model);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				Move move = player.getMove(model);
				System.out.println(player + " moves to " + move);
				model.move(move);
			}
			
			System.out.println();
		}
	}
	
}
