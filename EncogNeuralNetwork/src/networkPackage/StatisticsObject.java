package networkPackage;

import java.util.Comparator;

public class StatisticsObject{
	private String fileName;
	private double mae;
	private double mpe;
	private double maeLOW;
	private double mpeLOW;
	private double maeMIDDLE;
	private double mpeMIDDLE;
	private double maeHIGH;
	private double mpeHIGH;
	private double belowTenPercent;
	private double belowTenPercentLOW;
	private double belowTenPercentMIDDLE;
	private double belowTenPercentHIGH;
	private double time;
	
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public double getMpe() {
		return mpe;
	}
	public void setMpe(double mpe) {
		this.mpe = mpe;
	}
	public double getMaeLow() {
		return maeLOW;
	}
	public void setMaeLow(double maeLow) {
		this.maeLOW = maeLow;
	}
	public double getMpeLow() {
		return mpeLOW;
	}
	public void setMpeLow(double mpeLow) {
		this.mpeLOW = mpeLow;
	}
	public double getMae() {
		return mae;
	}
	public void setMae(double mae) {
		this.mae = mae;
	}
	public double getMaeMIDDLE() {
		return maeMIDDLE;
	}
	public void setMaeMIDDLE(double maeMIDDLE) {
		this.maeMIDDLE = maeMIDDLE;
	}
	public double getMpeMIDDLE() {
		return mpeMIDDLE;
	}
	public void setMpeMIDDLE(double mpeMIDDLE) {
		this.mpeMIDDLE = mpeMIDDLE;
	}
	public double getMaeHIGH() {
		return maeHIGH;
	}
	public void setMaeHIGH(double maeHIGH) {
		this.maeHIGH = maeHIGH;
	}
	public double getMpeHIGH() {
		return mpeHIGH;
	}
	public void setMpeHIGH(double mpeHIGH) {
		this.mpeHIGH = mpeHIGH;
	}
	public double getBelowTenPercent() {
		return belowTenPercent;
	}
	public void setBelowTenPercent(double belowTenPercent) {
		this.belowTenPercent = belowTenPercent;
	}
	public double getBelowTenPercentLOW() {
		return belowTenPercentLOW;
	}
	public void setBelowTenPercentLOW(double belowTenPercentLOW) {
		this.belowTenPercentLOW = belowTenPercentLOW;
	}
	public double getBelowTenPercentMIDDLE() {
		return belowTenPercentMIDDLE;
	}
	public void setBelowTenPercentMIDDLE(double belowTenPercentMIDDLE) {
		this.belowTenPercentMIDDLE = belowTenPercentMIDDLE;
	}
	public double getBelowTenPercentHIGH() {
		return belowTenPercentHIGH;
	}
	public void setBelowTenPercentHIGH(double belowTenPercentHIGH) {
		this.belowTenPercentHIGH = belowTenPercentHIGH;
	}
	public double getTime() {
		return time;
	}
	public void setTime(double time) {
		this.time = time;
	}
}

class MaeComparator implements Comparator<StatisticsObject> {
	public int compare(StatisticsObject o1, StatisticsObject o2) {
		if(o1.getMae() > o2.getMae()) return 1;
		else if(o1.getMae() < o2.getMae()) return -1;
		else return 0;
	}
}

class MpeComparator implements Comparator<StatisticsObject> {
	public int compare(StatisticsObject o1, StatisticsObject o2) {
		if(o1.getMpe() > o2.getMpe()) return 1;
		else if(o1.getMpe() < o2.getMpe()) return -1;
		else return 0;
	}
}

class MaeComparatorHigh implements Comparator<StatisticsObject> {
	public int compare(StatisticsObject o1, StatisticsObject o2) {
		if(o1.getMaeHIGH() > o2.getMaeHIGH()) return 1;
		else if(o1.getMaeHIGH() < o2.getMaeHIGH()) return -1;
		else return 0;
	}
}


class MpeComparatorHigh implements Comparator<StatisticsObject> {
	public int compare(StatisticsObject o1, StatisticsObject o2) {
		if(o1.getMpeHIGH() > o2.getMpeHIGH()) return 1;
		else if(o1.getMpeHIGH() < o2.getMpeHIGH()) return -1;
		else return 0;
	}
}

class MaeComparatorLow implements Comparator<StatisticsObject> {
	public int compare(StatisticsObject o1, StatisticsObject o2) {
		if(o1.getMaeLow() > o2.getMaeLow()) return 1;
		else if(o1.getMaeLow() < o2.getMaeLow()) return -1;
		else return 0;
	}
}


class MpeComparatorLow implements Comparator<StatisticsObject> {
	public int compare(StatisticsObject o1, StatisticsObject o2) {
		if(o1.getMpeLow() > o2.getMpeLow()) return 1;
		else if(o1.getMpeLow() < o2.getMpeLow()) return -1;
		else return 0;
	}
}

class MaeComparatorMiddle implements Comparator<StatisticsObject> {
	public int compare(StatisticsObject o1, StatisticsObject o2) {
		if(o1.getMaeMIDDLE() > o2.getMaeMIDDLE()) return 1;
		else if(o1.getMaeMIDDLE() < o2.getMaeMIDDLE()) return -1;
		else return 0;
	}
}


class MpeComparatorMiddle implements Comparator<StatisticsObject> {
	public int compare(StatisticsObject o1, StatisticsObject o2) {
		if(o1.getMpeLow() > o2.getMpeLow()) return 1;
		else if(o1.getMpeLow() < o2.getMpeLow()) return -1;
		else return 0;
	}
}
