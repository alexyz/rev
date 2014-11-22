package rv;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.JComponent;

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