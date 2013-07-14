package networkPackage;

public class CSVContainer {
	
	private double consumption;
	private double price;
	
	public CSVContainer(Double consumption, Double price){
		this.setConsumption(consumption);
		this.setPrice(price);
	}

	public double getConsumption() {
		return consumption;
	}

	public void setConsumption(double consumption) {
		this.consumption = consumption;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}
}
