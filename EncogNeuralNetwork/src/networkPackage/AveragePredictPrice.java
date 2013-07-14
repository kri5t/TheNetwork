package networkPackage;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
			DescriptiveStatistics stats = new DescriptiveStatistics();
			while ((nextLine = csvReader.readNext()) != null) {
				if(!header) {
		    		header = true;
		    		continue;
		    	}
				//i++;
				double[] value = new double[]{Double.parseDouble(nextLine[0]), Double.parseDouble(nextLine[1])};
				values.add(value);
				if(Double.parseDouble(nextLine[0]) < 2000.0) stats.addValue(Double.parseDouble(nextLine[0]));
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
	    	if(!Double.isNaN(value[0])){
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
	
	public static void findBestMPE(List<StatisticsObject> list){
		System.out.println("");
		System.out.println("BEST MPE: ");
		Collections.sort(list, new MpeComparator());
		for(StatisticsObject so : list){
			if(!Double.isNaN(so.getMpe())){
				System.out.println(so.getFileName());
				System.out.println(so.getMpe());
			}
		}
	}
	
	public static void findBestMAE(List<StatisticsObject> list){
		System.out.println("BEST MAE: ");
		Collections.sort(list, new MaeComparator());
		double first = 0;
		for(StatisticsObject so : list){
			if(!Double.isNaN(so.getMae())){
				if(first == 0){
					first = so.getMae();
				}
				System.out.println(so.getFileName().replace("newPredictions/", ""));
				System.out.println(roundIt(so.getMae(),2));
				System.out.println(roundIt((so.getMae()-first)/first*100,2));
			}
		}
	}
	
	public static void findHighMiddleLow(List<StatisticsObject> list){
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
	}
	
	public static void writeStatisticObjectsToFile(List<StatisticsObject> list, String inputFile){
		MyCSVWriter csvWriter = new MyCSVWriter();
		System.out.println("INPUTFILE: " + inputFile);
		csvWriter.setNewOutputFile(inputFile, null, false);
		for(StatisticsObject so: list){
			String[] lineToWrite = {so.getFileName(), so.getBelowTenPercent()+"", so.getMae()+"", so.getMpe()+"", so.getBelowTenPercentHIGH()+"",so.getMaeHIGH()+"",so.getMpeHIGH()+"",so.getBelowTenPercentLOW()+"",so.getMaeLow()+"",so.getMpeLow()+"",so.getBelowTenPercentMIDDLE()+"",so.getMaeMIDDLE()+"",so.getMpeMIDDLE()+""};
			csvWriter.writeLineToFile(lineToWrite);
		}
	}
	
	public static void findAverageOfTenRuns(List<StatisticsObject> list){
		String[] filesList = {"newPredictions/TEN__MIXEDPrice_Consump_windSpeed_temperatureRow_timeOfDay_weekdaysMATRIX_monthOfYearMATRIX",
				  "newPredictions/TEN__MIXEDPrice_Consump_windSpeed_temperatureRow_timeOfDayMATRIX_weekdays",
				  "newPredictions/TEN__MIXEDPrice_Consump_windSpeed_temperatureRow_timeOfDayMATRIX_weekdays_monthOfYearMATRIX",
				  "newPredictions/TEN__MIXEDPrice_Consump_windSpeed_timeOfDayMATRIX_weekdays_monthOfYearMATRIX",
				  "newPredictions/TEN__MATRIX_Price_Consump_windSpeed_temperatureRow_timeOfDay_weekdays_seasonOfYear",
				  "newPredictions/TEN__MIXEDPrice_Consump_windSpeed_temperatureRow_timeOfDayMATRIX_weekdays_seasonOfYearMATRIX",
				  "newPredictions/TEN__MIXEDPrice_Consump_windSpeed_temperatureRow_timeOfDayMATRIX_monthOfYearMATRIX",
				  "newPredictions/TEN__MIXEDPrice_Consump_windSpeed_temperatureRow_timeOfDayMATRIX_seasonOfYearMATRIX",
				  "newPredictions/TEN__MIXEDPrice_Consump_windSpeed_temperatureRow_timeOfDay_weekdays_seasonOfYearMATRIX",
				  "newPredictions/TEN__MATRIX_Price_Consump_windSpeed_timeOfDay_weekdays_monthOfYear"};
		for(String s : filesList){
			for(StatisticsObject so : list){
			//	System.out.println(so.getFileName());
			//	System.out.println(s);
				String fileName = so.getFileName().replace("5PTrim_", "");
				fileName = fileName.replace("5PTrim", "");
				fileName = fileName.substring(0,fileName.indexOf("_PREDICT"));
				if(fileName.equals(s)){
					System.out.println(s);
					System.out.println(roundIt(so.getMae(), 2));
				}
			}
		}
		String[] cases = {
				"X1_Curve_",
				"X1_1Historical_Curve_",
				"X1_1Historical_Skew_",
				"X1_1Historical_Curve_Skew_",
				"X1_1Historical_",
				"X1_Skew_",
				"X1_Curve_Skew_"
		};
		for(String fileName : filesList){
			fileName = fileName.replace("newPredictions/TEN__", "");
			HashMap<String, String> myMap = new HashMap<String,String>();
			for(String aCase : cases){
				for(StatisticsObject so : list){
					if((so.getFileName().contains(fileName+"_PREDICT") && so.getFileName().contains(aCase)) || so.getFileName().contains(fileName+".csv") && so.getFileName().contains(aCase)){
						//if(fileName.contains())myMap.put(aCase, so.getMae()+"");
					}
				}
			}
			System.out.println(fileName);
			for(String aCase : cases){
				for(Map.Entry<String, String> entry : myMap.entrySet()){
					if(aCase.equals(entry.getKey())) System.out.print(entry.getValue() + " ");
				}
			}
			System.out.println();
		}
	}
	
	public static void main(String[] args) throws Exception{
		List<StatisticsObject> list = Collections.synchronizedList(new ArrayList<StatisticsObject>());
//		File folderen1 = new File("newPredictions");
//		File[] listOfFiles1 = folderen1.listFiles();
//		for(File f : listOfFiles1){
//			if(!f.isDirectory() && !f.getName().contains(".DS_Store")){
//				File two = new File(f.getName().replace("NEWQuarterTrain", ""));
//				boolean lol = f.renameTo(two);
//				if(!lol){
//					System.out.println("Failed");
//				}
//			}
//		}
//		System.exit(0);
		System.out.println("--------------------------------------------------------");
		String inputFile = "statisticsObjects.csv";
		boolean reset = true;
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
		String folderName = "newPredictions/1HourAhead";
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
				if(listOfFiles[i].getName().contains("PREDICT") && (!fileNamesInTheSystem.contains(listOfFiles[i].getName()) || reset) && !listOfFiles[i].getName().contains("Trim") && !listOfFiles[i].getName().contains("TEN")){
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
		//if(numberOfThreads - list.size() != 0) throw new Exception("Mismatch i antal trŒde og antal oprettede objekter.");
		findBestMAE(list);

		findBestMPE(list);
		
		writeStatisticObjectsToFile(list, inputFile);
		
		findHighMiddleLow(list);
		
		List<StatisticsObject> listOfObjectsToRemove = new ArrayList<StatisticsObject>();
		for(StatisticsObject so : list){
			if(so.getFileName().contains("Trim")) listOfObjectsToRemove.add(so);
		}
		list.removeAll(listOfObjectsToRemove);
		for(int i = 0; i < 11; i++){
			String fileName = "";
			boolean first = true;
			double timesSeen = 0.0;
//			double MPE = 0.0;
			double MAE = 0.0;
			List<StatisticsObject> removeObjects = new ArrayList<StatisticsObject>();
			for(StatisticsObject so: list){
				if(first){
					fileName = so.getFileName().substring(0, so.getFileName().indexOf("_PREDICT"));
					first = false;
					MAE += so.getMae();
//					MPE += so.getMpe();
					timesSeen++;
					removeObjects.add(so);
				}
				else{
					if(so.getFileName().substring(0, so.getFileName().indexOf("_PREDICT")).equals(fileName)){
						MAE += so.getMae();
//						MPE += so.getMpe();
						timesSeen++;
						removeObjects.add(so);
					}
				}
			}
			System.out.println(fileName);
			System.out.println(MAE/timesSeen);
//			System.out.println("AVERAGE MAE: " + MAE/timesSeen);
//			System.out.println("AVERAGE MPE: " + MPE/timesSeen);
			list.removeAll(removeObjects);
		}
		
		findAverageOfTenRuns(list);
		
	}
	
	public static double roundIt(double value, int places){
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
	}
} 
