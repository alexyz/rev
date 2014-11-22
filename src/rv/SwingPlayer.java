package rv;

class SwingPlayer extends Player {
	
	private final RevJFrame f;

	public SwingPlayer(RevJFrame f) {
		this.f = f;
	}
	
	@Override
	public Move getMove (Model model) {
		return f.nextMove(model.getMoves());
	}
	
	@Override
	public boolean isReal () {
		return true;
	}
	
}