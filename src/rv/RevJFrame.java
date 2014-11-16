package rv;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import rv.Model.Count;

public class RevJFrame extends JFrame {
	
	public static RevJFrame instance;
	
	private static final String TITLE = "Rev";
	private static final List<Class<? extends Player>> pcs = new ArrayList<>();
	
	public static void main (String[] args) {
		pcs.add(SwingPlayer.class);
		pcs.add(RandomPlayer.class);
		pcs.add(AdvantagePlayer.class);
		pcs.add(ValuePlayer.class);
		pcs.add(AntiMobilityPlayer.class);
		pcs.add(AdvantagePlayer2.class);
		pcs.add(ValuePlayer2.class);
		instance = new RevJFrame();
		instance.show();
	}
	
	private final RevJPanel revPanel = new RevJPanel(this);
	private final JComboBox<PlayerItem> blackCombo;
	private final JComboBox<PlayerItem> whiteCombo;
	
	private volatile List<Move> moves;
	private volatile Move move;
	
	public RevJFrame () {
		super(TITLE);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		Vector<PlayerItem> players = new Vector<>();
		for (Class<? extends Player> c : pcs) {
			players.add(new PlayerItem(c));
		}
		
		blackCombo = new JComboBox<>(players);
		whiteCombo = new JComboBox<>(players);
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
		final PlayerItem bp = (PlayerItem) blackCombo.getSelectedItem();
		final PlayerItem wp = (PlayerItem) whiteCombo.getSelectedItem();
		final Model model = new Model();
		revPanel.setModel(model);
		repaint();
		Thread t = new Thread() {
			@Override
			public void run () {
				try {
					Main.play(model, bp.c.newInstance(), wp.c.newInstance());
					repaint();
					Count c = model.getCount();
					String ws = c.black > c.white ? "Black" : c.black == c.white ? "Draw" : "White";
					JOptionPane.showMessageDialog(RevJFrame.this, "Black: " + c.black + " White: " + c.white + " Winner: " + ws);
					
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

class SwingPlayer extends Player {
	
	@Override
	public Move getMove (Model model) {
		return RevJFrame.instance.nextMove(model.getMoves());
	}
	
	@Override
	public boolean isReal () {
		return true;
	}
	
}

class PlayerItem {
	public final Class<? extends Player> c;
	
	public PlayerItem (Class<? extends Player> c) {
		this.c = c;
	}
	
	@Override
	public String toString () {
		return c.getSimpleName();
	}
}

class RevJPanel extends JPanel {
	private final RevJFrame f;
	private Model model;
	
	public RevJPanel (RevJFrame f) {
		super(new GridBagLayout());
		initComponents();
		setMinimumSize(new Dimension(480, 480));
		setPreferredSize(getMinimumSize());
		this.f = f;
	}

	private void initComponents () {
		JPanel xp = new JPanel(new GridLayout(1, 8));
		for (int x = 0; x < 8; x++) {
			JLabel l = new JLabel("" + (char) ('a' + x));
			l.setHorizontalAlignment(SwingConstants.CENTER);
			l.setVerticalAlignment(SwingConstants.CENTER);
			l.setBorder(BorderFactory.createRaisedBevelBorder());
			xp.add(l);
		}
		
		JPanel yp = new JPanel(new GridLayout(8, 1));
		for (int y = 0; y < 8; y++) {
			JLabel l = new JLabel(" " + (y + 1) + " ");
			l.setHorizontalAlignment(SwingConstants.CENTER);
			l.setVerticalAlignment(SwingConstants.CENTER);
			l.setBorder(BorderFactory.createRaisedBevelBorder());
			yp.add(l);
		}
		
		JPanel bp = new JPanel(new GridLayout(8, 8));
		for (int y = 0; y < 8; y++) {
			for (int x = 0; x < 8; x++) {
				final RevJComp c = new RevJComp(this, x, y);
				c.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						f.clicked(c.x, c.y);
					}
				});
				bp.add(c);
			}
		}
		
		{
			GridBagConstraints c = new GridBagConstraints();
			c.gridx = 1;
			c.gridy = 0;
			c.fill = GridBagConstraints.BOTH;
			add(xp, c);
		}
		{
			GridBagConstraints c = new GridBagConstraints();
			c.gridx = 0;
			c.gridy = 1;
			c.fill = GridBagConstraints.BOTH;
			add(yp, c);
		}
		{
			GridBagConstraints c = new GridBagConstraints();
			c.gridx = 1;
			c.gridy = 1;
			c.weightx = 1;
			c.weighty = 1;
			c.fill = GridBagConstraints.BOTH;
			add(bp, c);
		}
	}
	
	public Model getModel () {
		return model;
	}
	
	public void setModel (Model model) {
		this.model = model;
	}
}

class RevJComp extends JComponent {
	public final int x;
	public final int y;
	private final RevJPanel panel;
	
	public RevJComp (RevJPanel panel, int x, int y) {
		this.panel = panel;
		this.x = x;
		this.y = y;
		setBorder(BorderFactory.createEtchedBorder());
	}
	
	@Override
	protected void paintComponent (final Graphics g) {
		final Model model = panel.getModel();
		if (model != null) {
			final Graphics2D g2 = (Graphics2D) g;
			final byte state = model.get(x, y);
			final Insets i = getBorder().getBorderInsets(this);
			final int w = getWidth() - i.right - i.left;
			final int xi = i.left;
			final int yi = i.top;
			final int h = getHeight() - i.top - i.bottom;
			final int w116 = w / 16;
			final int h116 = h / 16;
			final int w18 = w / 8;
			final int h18 = h / 8;
			final int w38 = (w * 3) / 8;
			final int w48 = (w * 4) / 8;
			final int w716 = (w * 7) / 16;
			final int h716 = (h * 7) / 16;
			
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			
			if ((state & Model.DISC) != 0) {
				Color c = (state & Model.BLACK) != 0 ? Color.black : Color.white;
				g.setColor(c);
				g.fillOval(w116 + xi, h116 + yi, w - w18, h - h18);
			}
			
			Move m = Move.getMove(model.getMoves(), x, y);
			if (m != null) {
				g.setColor(model.blackMove() ? Color.black : Color.white);
				g.fillOval(w716 + xi, h716 + yi, w18, h18);
			}
		}
	}
}