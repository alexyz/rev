package rv;

public abstract class BoardFilter {
	public final int valueOf (Model model) {
		if (model.isWin()) {
			return Integer.MAX_VALUE;
		} else if (model.isLose()) {
			return Integer.MIN_VALUE;
		} else {
			return valueOfImpl(model);
		}
	}
	protected abstract int valueOfImpl (Model model);
}

class AdvantageBoardFilter extends BoardFilter {
	@Override
	public int valueOfImpl(Model model) {
		return model.getDiscs() - model.getOppDiscs();
	}
}

class MobilityBoardFilter extends BoardFilter {
	@Override
	public int valueOfImpl(Model model) {
		return model.getMobility();
	}
}

class ZeroBoardFilter extends BoardFilter {
	@Override
	public int valueOfImpl(Model model) {
		return 0;
	}
}