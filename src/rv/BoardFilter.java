package rv;

public abstract class BoardFilter {
	public static final BoardFilter mobilityBoard = new MobilityBoardFilter();
	public static final BoardFilter advantageBoard = new AdvantageBoardFilter();
	public abstract int valueOf (Model model);
	
}
