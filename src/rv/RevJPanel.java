package rv;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

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