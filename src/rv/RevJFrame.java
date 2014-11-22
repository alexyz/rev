package rv;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Vector;

import javax.swing.*;

public class RevJFrame extends JFrame {
	
	private static final String TITLE = "Rev";
	
	public static void main (String[] args) {
		RevJFrame f = new RevJFrame();
		f.show();
	}
	
	private final RevJPanel revPanel = new RevJPanel(this);
	private final JComboBox<Player> blackCombo;
	private final JComboBox<Player> whiteCombo;
	
	private volatile List<Move> moves;
	private volatile Move move;
	
	public RevJFrame () {
		super(TITLE);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		Vector<Player> ps = new Vector<>();
		ps.add(new SwingPlayer(this));
		ps.add(new BoardPlayer(new AdvantageBoardFilter()));
		ps.add(new BoardPlayer(new MobilityBoardFilter()));
		ps.add(new RandomPlayer());
		ps.add(new AdvantagePlayer());
		ps.add(new ValuePlayer());
		ps.add(new AntiMobilityPlayer());
		ps.add(new AdvantagePlayer2());
		ps.add(new ValuePlayer2());
		
		blackCombo = new JComboBox<>(ps);
		whiteCombo = new JComboBox<>(ps);
		whiteCombo.setSelectedIndex(1);
		
		JButton startButton = new JButton("Start");
		
		startButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed (ActionEvent e) {
				start();
			}
		});
		
		JPanel northPanel = new JPanel();
		northPanel.add(new JLabel("Black"));
		northPanel.add(blackCombo);
		northPanel.add(new JLabel("White"));
		northPanel.add(whiteCombo);
		northPanel.add(startButton);
		
		JPanel p = new JPanel(new BorderLayout());
		p.add(northPanel, BorderLayout.NORTH);
		p.add(revPanel, BorderLayout.CENTER);
		p.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		setContentPane(p);
		pack();
	}
	
	private void start () {
		final Player bp = (Player) blackCombo.getSelectedItem();
		final Player wp = (Player) whiteCombo.getSelectedItem();
		final Model model = new Model();
		revPanel.setModel(model);
		repaint();
		Thread t = new Thread() {
			@Override
			public void run () {
				try {
					Main.play(model, bp, wp);
					repaint();
					int bd = model.getBlackDiscs();
					int wd = model.getWhiteDiscs();
					String ws = bd > wd ? "Black" : bd == wd ? "Draw" : "White";
					JOptionPane.showMessageDialog(RevJFrame.this, "Black: " + bd + " White: " + wd + " Winner: " + ws);
					
				} catch (Exception e1) {
					JOptionPane.showMessageDialog(RevJFrame.this, e1.toString());
				}
			}
		};
		t.setDaemon(true);
		t.setPriority(Thread.MIN_PRIORITY);
		t.start();
	}
	
	public void clicked (int x, int y) {
		System.out.println("clicked " + x + ", " + y);
		synchronized (this) {
			if (moves != null) {
				for (Move m : moves) {
					if (m.x == x && m.y == y) {
						move = m;
						System.out.println("notify");
						notifyAll();
						break;
					}
				}
			} else {
				System.out.println("no moves");
			}
		}
	}
	
	public Move nextMove (List<Move> list) {
		System.out.println("next move " + list);
		synchronized (this) {
			moves = list;
			move = null;
			setTitle(TITLE + " [your move]");
			repaint();
			while (move == null) {
				System.out.println("wait");
				try {
					wait();
				} catch (InterruptedException e) {
					System.out.println("next move: " + e);
				}
			}
			setTitle(TITLE);
			repaint();
			return move;
		}
	}
}