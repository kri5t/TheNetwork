package networkPackage;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import au.com.bytecode.opencsv.CSVReader;

public class AveragePredictPrice implements Runnable{
	private String fileName = "";
	private List<StatisticsObject> listOfObjects;
	private StatisticsObject statisticsObject;
	private static CSVReader csvReader;
	
	public AveragePredictPrice(String fileName, List<StatisticsObject> listOfObjects){
		this.fileName = fileName;
		this.listOfObjects = listOfObjects;
		statisticsObject = new StatisticsObject();
		statisticsObject.setFileName(fileName);
	}
	
	public void run(){
		try{
			CSVReader csvReader = new CSVReader(new FileReader(fileName));
			String [] nextLine;
			ArrayList<double[]> values = new ArrayList<double[]>();
			boolean header = false;
			int i = 0;
			DescriptiveStatistics stats = new DescriptiveStatistics();
			while ((nextLine = csvReader.readNext()) != null && i < 5000) {
				if(!header) {
		    		header = true;
		    		continue;
		    	}
				//i++;
				double[] value = new double[]{Double.parseDouble(nextLine[0]), Double.parseDouble(nextLine[1])};
				values.add(value);
				stats.addValue(Double.parseDouble(nextLine[0]));
			}
			
		    ArrayList<double[]> below = new ArrayList<double[]>();
		    ArrayList<double[]> middle = new ArrayList<double[]>();
		    ArrayList<double[]> higher = new ArrayList<double[]>();
		    for(double[] entry : values){
		    	if(entry[0] < stats.getPercentile(30)){
		    		below.add(entry);
		    	}
		    	else if(entry[0] > stats.getPercentile(70)){
		    		higher.add(entry);
		    	}
		    	else{
		    		middle.add(entry);
		    	}
		    }
		    System.out.println("Finished");
			//System.out.println(fileName);
			doCalculationsOnDoubleArray(values, fileName, "ALL");
		    doCalculationsOnDoubleArray(below, fileName, "LOW");
		    doCalculationsOnDoubleArray(middle, fileName, "MIDDLE");
		    doCalculationsOnDoubleArray(higher, fileName, "HIGH");
		    listOfObjects.add(statisticsObject);
		    csvReader.close();
		}
		catch(Exception e){
			
		}
	}
	
	public void doCalculationsOnDoubleArray(ArrayList<double[]> values, String fileName, String WhatSet){
		double mpe = 0.0;
		double mae = 0.0;
		double numberOfRows = values.size();
		int belowTenPercent = 0;
	    for (double[] value : values) {
	    	
	    	Double foreseen = value[0];
	    	Double actual = value[1];
	    	
	    	//MPE
	    	double mpeError = Math.abs(actual - foreseen)/actual;
	    	if(mpeError*100 < 10) belowTenPercent ++;
	    	mpe += mpeError; 
	    	//MAE
	    	mae += Math.abs(foreseen-actual);
	    	//MSE
//	    	mse += Math.pow((foreseen-actual),2);		    
	    }
	    
	    double actualMpe = (mpe/numberOfRows)*100;
	    double actualMae = mae/numberOfRows;
	    
	    if(WhatSet =="ALL") {
	    	statisticsObject.setBelowTenPercent(belowTenPercent);
	    	statisticsObject.setMae(actualMae);
	    	statisticsObject.setMpe(actualMpe);
	    }
	    if(WhatSet =="LOW") {
	    	statisticsObject.setBelowTenPercentLOW(belowTenPercent);
	    	statisticsObject.setMaeLow(actualMae);
	    	statisticsObject.setMpeLow(actualMpe);
	    }
	    if(WhatSet =="MIDDLE") {
	    	statisticsObject.setBelowTenPercentMIDDLE(belowTenPercent);
	    	statisticsObject.setMaeMIDDLE(actualMae);
	    	statisticsObject.setMpeMIDDLE(actualMpe);
	    }
	    if(WhatSet =="HIGH") {
	    	statisticsObject.setBelowTenPercentHIGH(belowTenPercent);
	    	statisticsObject.setMaeHIGH(actualMae);
	    	statisticsObject.setMpeHIGH(actualMpe);
	    }
	}
	
	public static void main(String[] args) throws Exception{
		List<StatisticsObject> list = Collections.synchronizedList(new ArrayList<StatisticsObject>());

		System.out.println("--------------------------------------------------------");
		String inputFile = "statisticsObjects.csv";
		boolean reset = false;
		try{
			if(!reset){
				csvReader = new CSVReader(new FileReader(inputFile));
				String [] nextLine;
				
				while ((nextLine = csvReader.readNext()) != null){
					StatisticsObject so = new StatisticsObject();
					so.setFileName(nextLine[0]);
					so.setBelowTenPercent(Double.parseDouble(nextLine[1]));
					so.setMae(Double.parseDouble(nextLine[2]));
					so.setMpe(Double.parseDouble(nextLine[3]));
					so.setBelowTenPercentHIGH(Double.parseDouble(nextLine[4]));
					so.setMaeHIGH(Double.parseDouble(nextLine[5]));
					so.setMpeHIGH(Double.parseDouble(nextLine[6]));
					so.setBelowTenPercentLOW(Double.parseDouble(nextLine[7]));
					so.setMaeLow(Double.parseDouble(nextLine[8]));
					so.setMpeLow(Double.parseDouble(nextLine[9]));
					so.setBelowTenPercentMIDDLE(Double.parseDouble(nextLine[10]));
					so.setMaeMIDDLE(Double.parseDouble(nextLine[11]));
					so.setMpeMIDDLE(Double.parseDouble(nextLine[12]));
					list.add(so);
				}
			}
		}
		catch(Exception e){
			System.out.println(e);
		}
		String folderName = "newPredictions";
		List<String> fileNamesInTheSystem = new ArrayList<String>();
		for(StatisticsObject so: list){
			fileNamesInTheSystem.add(so.getFileName().replace(folderName + "/", ""));
			System.out.println(so.getFileName().replace(folderName + "/", ""));
		}
		
		File folderen = new File(folderName);
		File[] listOfFiles = folderen.listFiles();
		long startTime = Calendar.getInstance().getTimeInMillis();
//		System.out.println(startTime);
		ExecutorService es = Executors.newFixedThreadPool(12);
		int numberOfThreads = list.size();
		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				if(listOfFiles[i].getName().contains("PREDICT") && (!fileNamesInTheSystem.contains(listOfFiles[i].getName()) || reset) ){
					es.execute(new AveragePredictPrice(folderName + "/" + listOfFiles[i].getName(), list));
					numberOfThreads++;
				}
			}
		}
		es.shutdown();
		try {
			es.awaitTermination(1, TimeUnit.HOURS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("Time it took to execute: " + (Calendar.getInstance().getTimeInMillis() - startTime));
		System.out.println("List size: " + list.size());
		System.out.println("Number of threads: " + numberOfThreads);
		if(numberOfThreads - list.size() != 0) throw new Exception("Mismatch i antal trŒde og antal oprettede objekter.");
		System.out.println("BEST MAE: ");
		Collections.sort(list, new MaeComparator());
		for(StatisticsObject so : list){
			if(!Double.isNaN(so.getMae())){
				System.out.println(so.getFileName());
				System.out.println(so.getMae());
			}
		}
		System.out.println("");
		System.out.println("BEST MPE: ");
		Collections.sort(list, new MpeComparator());
		for(StatisticsObject so : list){
			if(!Double.isNaN(so.getMpe())){
				System.out.println(so.getFileName());
				System.out.println(so.getMpe());
			}
		}
		
		
		MyCSVWriter csvWriter = new MyCSVWriter();
		csvWriter.setNewOutputFile(inputFile, null);
		for(StatisticsObject so: list){
			String[] lineToWrite = {so.getFileName(), so.getBelowTenPercent()+"", so.getMae()+"", so.getMpe()+"", so.getBelowTenPercentHIGH()+"",so.getMaeHIGH()+"",so.getMpeHIGH()+"",so.getBelowTenPercentLOW()+"",so.getMaeLow()+"",so.getMpeLow()+"",so.getBelowTenPercentMIDDLE()+"",so.getMaeMIDDLE()+"",so.getMpeMIDDLE()+""};
			csvWriter.writeLineToFile(lineToWrite);
		}
		
		System.out.println("");
		System.out.println("BEST_ALL");
		Collections.sort(list, new MaeComparator());
		System.out.println("MAE file: " + list.get(0).getFileName() + " MAE: "+list.get(0).getMae());
		Collections.sort(list, new MpeComparator());
		System.out.println("MPE file: " + list.get(0).getFileName() + " MPE: "+list.get(0).getMpe());
		System.out.println();
		System.out.println("BEST_LOW");
		Collections.sort(list, new MaeComparatorLow());
		System.out.println("MAE file: " + list.get(0).getFileName() + " MAE: "+list.get(0).getMaeLow());
		Collections.sort(list, new MpeComparatorLow());
		System.out.println("MPE file: " + list.get(0).getFileName() + " MPE: "+list.get(0).getMpeLow());
		System.out.println();
		System.out.println("BEST_MIDDLE");
		Collections.sort(list, new MaeComparatorMiddle());
		System.out.println("MAE file: " + list.get(0).getFileName() + " MAE: "+list.get(0).getMaeMIDDLE());
		Collections.sort(list, new MpeComparatorMiddle());
		System.out.println("MPE file: " + list.get(0).getFileName() + " MPE: "+list.get(0).getMpeMIDDLE());
		System.out.println();
		System.out.println("BEST_HIGH");
		Collections.sort(list, new MaeComparatorHigh());
		System.out.println("MAE file: " + list.get(0).getFileName() + " MAE: "+list.get(0).getMaeHIGH());
		Collections.sort(list, new MpeComparatorHigh());
		System.out.println("MPE file: " + list.get(0).getFileName() + " MPE: "+list.get(0).getMpeHIGH());
//		for(int i = 0; i < 4; i++){
//			String fileName = "";
//			boolean first = true;
//			double timesSeen = 0.0;
//			double MPE = 0.0;
//			double MAE = 0.0;
//			List<StatisticsObject> removeObjects = new ArrayList<StatisticsObject>();
//			for(StatisticsObject so: list){
//				if(first){
//					fileName = so.getFileName().substring(0, so.getFileName().indexOf("_PREDICT"));
//					first = false;
//					MAE += so.getMae();
//					MPE += so.getMpe();
//					timesSeen++;
//					removeObjects.add(so);
//				}
//				else{
//					if(so.getFileName().substring(0, so.getFileName().indexOf("_PREDICT")).equals(fileName)){
//						MAE += so.getMae();
//						MPE += so.getMpe();
//						timesSeen++;
//						removeObjects.add(so);
//					}
//				}
//			}
//			System.out.println(fileName);
//			System.out.println(MAE/timesSeen);
//			System.out.println(MPE/timesSeen);
//			list.removeAll(removeObjects);
//		}
	}
} 
