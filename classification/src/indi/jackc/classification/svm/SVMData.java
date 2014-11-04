package indi.jackc.classification.svm;

public class SVMData {

	private double[][] x;
	private int[] y;

	public double[][] getX() {
		return x;
	}

	public void setX(double[][] x) {
		this.x = x;
	}

	public int[] getY() {
		return y;
	}

	public void setY(int[] y) {
		this.y = y;
	}

	public SVMData(double[][] x, int y[]) {
		this.x = x;
		this.y = y;
	}
}
