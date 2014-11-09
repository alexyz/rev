package rv;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Point;
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

public class RevJFrame extends JFrame {
	
	public static RevJFrame instance;
	
	private static final String TITLE = "Rev";
	private static final List<Class<? extends Player>> pcs = new ArrayList<>();
	
	public static void main (String[] args) {
		pcs.add(SwingPlayer.class);
		pcs.add(AdvantagePlayer.class);
		pcs.add(AntiMobilityPlayer.class);
		pcs.add(MobilityPlayer.class);
		pcs.add(RandomPlayer.class);
		pcs.add(StabilityPlayer.class);
		pcs.add(ValuePlayer.class);
		instance = new RevJFrame();
		instance.show();
	}
	
	private final RevJPanel rp = new RevJPanel(this);
	private final JComboBox<PlayerItem> bcombo;
	private final JComboBox<PlayerItem> wcombo;

	private volatile List<Move> moves;
	private volatile Move move;

	public RevJFrame () {
		super(TITLE);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		Vector<PlayerItem> players = new Vector<>();
		for (Class<? extends Player> c : pcs) {
			players.add(new PlayerItem(c));
		}
		
		bcombo = new JComboBox<>(players);
		wcombo = new JComboBox<>(players);
		JButton startButton = new JButton("Start");
		
		startButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed (ActionEvent e) {
				start();
			}
		});
		
		JPanel b = new JPanel();
		b.add(new JLabel("Black"));
		b.add(bcombo);
		b.add(new JLabel("White"));
		b.add(wcombo);
		b.add(startButton);
		
		JPanel p = new JPanel(new BorderLayout());
		p.add(b, BorderLayout.NORTH);
		p.add(rp, BorderLayout.CENTER);
		setContentPane(p);
		pack();
	}
	
	private void start () {
		final PlayerItem bp = (PlayerItem) bcombo.getSelectedItem();
		final PlayerItem wp = (PlayerItem) wcombo.getSelectedItem();
		final Model model = new Model();
		rp.setModel(model);
		repaint();
		Thread t = new Thread() {
			@Override
			public void run() {
				try {
					byte w = Main.play(model, bp.c, wp.c);
					String ws = w == Model.BLACK ? "black" : w == Model.WHITE ? "white" : "draw";
					JOptionPane.showMessageDialog(RevJFrame.this, "Winner: " + ws);
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
			return move;
		}
	}
}

class SwingPlayer extends Player {
	
	@Override
	public Move getMove () {
		return RevJFrame.instance.nextMove(getMoves());
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
	private final RevJComp[][] comps = new RevJComp[8][8];
	private final RevJFrame f;
	private Model model;
	public RevJPanel (RevJFrame f) {
		super(new GridLayout(8, 8));
		this.f = f;
		setMinimumSize(new Dimension(600,600));
		setPreferredSize(getMinimumSize());
		for (int n = 0; n < comps.length; n++) {
			for (int m = 0; m < comps[n].length; m++) {
				RevJComp c = new RevJComp(this, n, m);
				comps[n][m] = c;
				add(c);
			}
		}
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked (MouseEvent e) {
				clicked(e.getPoint());
			}
		});
	}
	private void clicked(Point p) {
		Component c = getComponentAt(p);
		if (c instanceof RevJComp) {
			RevJComp rc = ((RevJComp) c);
			f.clicked(rc.x, rc.y);
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
	protected void paintComponent (Graphics g) {
		final Model model = panel.getModel();
		if (model != null) {
			final byte state = model.get(x, y);
			final int h = getHeight();
			final int w = getWidth();
			if ((state & Model.DISC) != 0) {
				Color c = (state & Model.BLACK) != 0 ? Color.black : Color.white;
				g.setColor(c);
				g.fillOval(2, 2, w-2, h-2);
			}
			String s = "";
			for (boolean m : new boolean[] { true, false }) {
				for (Move move : model.getMoves(m)) {
					if (move.x == x && move.y == y) {
						s = s + (m ? "b" : "w");
						break;
					}
				}
			}
			if (s.length() > 0) {
				g.setColor(Color.blue);
				g.drawString(s, w / 2, h / 2);
			}
		}
	}
}