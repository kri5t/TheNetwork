package networkPackage;

import au.com.bytecode.opencsv.CSVReader;

import java.io.File;
import java.io.FileReader;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Brian
 * Date: 15-06-13
 * Time: 17:25
 * To change this template use File | Settings | File Templates.
 */
public class LatexTableCreator {
	public static int rank = 0;
	public static String directoryName = "newPredictions";
    public static void main(String[] args) {

        File directory = new File(directoryName);

        File[] files = directory.listFiles();
        
        String[] filesList = {
        		"newPredictions/TEN__MATRIX_Price_Consump_windSpeed_temperatureRow_timeOfDay_weekdays_seasonOfYear",
        		"newPredictions/TEN__MIXEDPrice_Consump_windSpeed_temperatureRow_timeOfDayMATRIX_weekdays_seasonOfYearMATRIX",
        		"newPredictions/TEN__MIXEDPrice_Consump_windSpeed_temperatureRow_timeOfDayMATRIX_monthOfYearMATRIX",
        		"newPredictions/TEN__MIXEDPrice_Consump_windSpeed_temperatureRow_timeOfDay_weekdaysMATRIX_monthOfYearMATRIX",
				"newPredictions/TEN__MIXEDPrice_Consump_windSpeed_temperatureRow_timeOfDayMATRIX_weekdays",
				"newPredictions/TEN__MIXEDPrice_Consump_windSpeed_temperatureRow_timeOfDayMATRIX_weekdays_monthOfYearMATRIX",
				"newPredictions/TEN__MIXEDPrice_Consump_windSpeed_timeOfDayMATRIX_weekdays_monthOfYearMATRIX",
				"newPredictions/TEN__MIXEDPrice_Consump_windSpeed_temperatureRow_timeOfDayMATRIX_seasonOfYearMATRIX",
				"newPredictions/TEN__MIXEDPrice_Consump_windSpeed_temperatureRow_timeOfDay_weekdays_seasonOfYearMATRIX",
				"newPredictions/TEN__MATRIX_Price_Consump_windSpeed_timeOfDay_weekdays_monthOfYear"
				};
        
        TreeMap<Double,String> sortedMapOfMAEs = new TreeMap<Double,String>();
//        for(String fileToFind : filesList){
        	rank = 0;
//        	String f2f = fileToFind.replace("newPredictions/TEN__", "") + "_PREDICT";
//        	fileToFind = fileToFind.replace("newPredictions/TEN__", "") + ".csv";
//        	System.out.println(f2f);
	        for(File f : files) {
	            if(!f.getName().contains(".DS_Store") && f.getName().contains("PREDICT") && !f.getName().contains("SELF") && !f.getName().contains("weekdaystimeOfDayMATRIX") /*&& (f.getName().contains(fileToFind) || f.getName().contains(f2f))*/)
	            {
	            	createLatexTableForStatistics(f,sortedMapOfMAEs);
//		        	InputStream inStream = null;
//		        	OutputStream outStream = null;
//		        	try{
//		        	    inStream = new FileInputStream(f);
//		        	    File direct = new File(directory.getAbsolutePath() +"/moveTo/"+fileToFind+"/");
//		        	    direct.mkdir();
//		        	    outStream = new FileOutputStream(direct.getAbsolutePath() +"/"+ f.getName());
//		     
//		        	    byte[] buffer = new byte[1024];
//		     
//		        	    int length;
//		        	    //copy the file content in bytes 
//		        	    while ((length = inStream.read(buffer)) > 0){
//		     
//		        	    	outStream.write(buffer, 0, length);
//		     
//		        	    }
//		     
//		        	    inStream.close();
//		        	    outStream.close();
//		     
//		        	    //delete the original file
//		        	    f.delete();
//		     
//		     
//		        	}catch(Exception e){
//		        	    e.printStackTrace();
//		        	}
	            }
	        }
	        //System.out.println(fileToFind.replace(".csv", ""));
	        Double firstMAE = 0.0;
	        for(Map.Entry<Double,String> entry : sortedMapOfMAEs.entrySet()) {
	            rank++;
	            Double mae = entry.getKey();
	            String percentage = "";
	            if(firstMAE == 0.0){
	            	firstMAE = mae;
	            	percentage = "-";
	            }else{
	            	percentage = round((mae-firstMAE)/firstMAE*100, 2) + "\\%";
	            }
	            String line = entry.getValue();
	            
	            line += " " + doubleToStringWithFixedDecimals(mae) + " & " + percentage + " \\\\ \\hline";
	            
	            System.out.println(rank + " & " + line);
	        }
	        System.out.println();
	        sortedMapOfMAEs = new TreeMap<Double,String>();
        }
//    }
    
    public static String doubleToStringWithFixedDecimals(Double mae){
    	mae = round(mae, 2);
    	String decimalFixedString = "";
        String[] splitDouble = mae.toString().split("\\.");
        if(splitDouble[1].length() == 1) decimalFixedString = mae.toString() + "0";
        else return mae.toString();
        return decimalFixedString;
    }
    
    public static void createLatexTable(File file, TreeMap<Double,String> sortedMapOfMAEs) {
        try {
            CSVReader csvreader = new CSVReader(new FileReader(file),',');
            String totalString = "";
            String [] rows = {"Price", "Consump", "windSpeed", "temperatureRow", "timeOfDay", "weekdays", "monthOfYear", "seasonOfYear"};
            List<String> matrixStrings = new ArrayList<String>();
            matrixStrings.add("timeOfDay"); matrixStrings.add("weekdays"); matrixStrings.add("monthOfYear"); matrixStrings.add("seasonOfYear");
            boolean allAreMatrix = false;
            if(file.getName().indexOf("_Price") != -1 && file.getName().substring(0, file.getName().indexOf("_Price")).equals("NEWQuarterTrain_MATRIX")) allAreMatrix = true;
            for(String s : rows) {
                if(file.getName().toLowerCase().contains(s.toLowerCase())) {
                	if((file.getName().toLowerCase().contains(s.toLowerCase() + "matrix") 
                			|| allAreMatrix) && matrixStrings.contains(s)) totalString+=" \\x\\m  &";
                	else totalString+=" \\x    &";
                } else {
                    totalString+= "       &";
                }
            }

            double totalMae = 0;
            double totalMpe = 0;
            String [] nextLine;
            int numberOfRows = 0;
            while ((nextLine = csvreader.readNext()) != null) {
                String actual = nextLine[0];
                String ideal = nextLine[1];

                try {
                    double actual_as_double = Double.parseDouble(actual);
                    double ideal_as_double = Double.parseDouble(ideal);

                    double tempMae = Math.abs(actual_as_double-ideal_as_double);
                    totalMae+=tempMae;

                    totalMpe+=(100*tempMae)/ideal_as_double;

                    numberOfRows++;

                } catch (NumberFormatException e) {
                    //e.printStackTrace();
                }
            }
            System.out.println(totalMpe);
            csvreader.close();
            double averageMae = round(totalMae/numberOfRows,2);
            if(sortedMapOfMAEs.containsKey(averageMae)) {
                totalMae+=0.0000000000000000031;
            }
            totalString += openSameFile(file);
            sortedMapOfMAEs.put(averageMae,totalString);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void createLatexTableForStatistics(File file, TreeMap<Double,String> sortedMapOfMAEs) {
        try {
            CSVReader csvreader = new CSVReader(new FileReader(file),',');
            String totalString = "";
            String [] rows = {"Curve", "Skew", "1Historical", "PAPER"};
            for(String s : rows) {
                if(file.getName().toLowerCase().contains(s.toLowerCase())) {
                	totalString+=" \\x    &";
                } else {
                    totalString+= "       &";
                }
            }

            double totalMae = 0;
//            double totalMpe = 0;
            String [] nextLine;
            int numberOfRows = 0;
            boolean skipFirst = false;
            while ((nextLine = csvreader.readNext()) != null) {
            	if(!skipFirst){
            		skipFirst = true;
            		continue;
            	}
                String actual = nextLine[0];
                String ideal = nextLine[1];

                try {
                    double actual_as_double = Double.parseDouble(actual);
                    double ideal_as_double = Double.parseDouble(ideal);

                    double tempMae = Math.abs(actual_as_double-ideal_as_double);
                    totalMae+=tempMae;

//                    totalMpe+=(100*tempMae)/ideal_as_double;

                    numberOfRows++;

                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
//            System.out.println(totalMpe);
            csvreader.close();
            double averageMae = totalMae/numberOfRows;
            if(sortedMapOfMAEs.containsKey(averageMae)) {
                averageMae+=0.0000000000000000031;
            }
//            String lol = "   " + rank + file.getName().substring(0, file.getName().indexOf("MIXED"));
//            if(lol.contains("3X1_1Historical_Curve_Skew_") && file.getName().contains("MIXEDPrice_Consump_windSpeed_temperatureRow_timeOfDay_weekdaysMATRIX_monthOfYearMATRIX")){
//            	System.out.println("ok");
//            }
//            System.out.println(lol);
            sortedMapOfMAEs.put(averageMae,totalString);
        } catch (Exception e) {
            System.out.println(e);
        }
    }
    
    public static void createLatexTableWithName(File file, TreeMap<Double,String> sortedMapOfMAEs){
    	try{
            CSVReader csvreader = new CSVReader(new FileReader(file),',');
            String totalString = file.getName().substring(0, file.getName().indexOf("EPOCHS")) + " & ";
	        double totalMae = 0;
	//        double totalMpe = 0;
	        String [] nextLine;
	        int numberOfRows = 0;
	        boolean skipFirst = false;
	        while ((nextLine = csvreader.readNext()) != null) {
	        	if(!skipFirst){
	        		skipFirst = true;
	        		continue;
	        	}
	            String actual = nextLine[0];
	            String ideal = nextLine[1];
	
	            try {
	                double actual_as_double = Double.parseDouble(actual);
	                double ideal_as_double = Double.parseDouble(ideal);
	
	                double tempMae = Math.abs(actual_as_double-ideal_as_double);
	                totalMae+=tempMae;
	
	//                totalMpe+=(100*tempMae)/ideal_as_double;
	
	                numberOfRows++;
	
	            } catch (NumberFormatException e) {
	                e.printStackTrace();
	            }
	        }
	//        System.out.println(totalMpe);
	        csvreader.close();
	        double averageMae = totalMae/numberOfRows;
	        if(sortedMapOfMAEs.containsKey(averageMae)) {
	            averageMae+=0.0000000000000000031;
	        }
	        totalString += openSameFile(file);
	        sortedMapOfMAEs.put(averageMae,totalString);
	    } catch (Exception e) {
	        System.out.println(e);
	    }
    }
    
    public static String openSameFile(File file){
    	int startIndex = file.getName().indexOf("PREDICT")+7;
    	int endIndex = startIndex + 13;
    	String stamp = file.getName().substring(startIndex, endIndex);
    	System.out.println(stamp);
        File directory = new File(directoryName);
        
        File[] files = directory.listFiles();
        String timeAndNeurons = "";
        for(File f : files){
        	String name = f.getName();
        	if(name.contains(stamp) && name.contains("SELF")){
        		ArrayList<String[]> bufferList = getBufferListFromFile(f);
//        		timeAndNeurons += readTime(bufferList);
        		timeAndNeurons += readNeurons(bufferList);
        	}
        }
        return timeAndNeurons;
    }
    
    public static ArrayList<String[]> getBufferListFromFile(File f){
        ArrayList<String[]> bufferList = new ArrayList<String[]>();
    	try{
    		CSVReader csvreader = new CSVReader(new FileReader(f),',');
    		String[] nextLine;
    		while ((nextLine = csvreader.readNext()) != null) {
    			bufferList.add(nextLine);
    		}
    		csvreader.close();
    	}
    	catch(Exception e){}
    	return bufferList;
    }
    
    public static String readTime(ArrayList<String[]> bufferList){
    	String time = "";
    	time = round(Double.parseDouble(bufferList.get(bufferList.size()-1)[0])/1000, 2) + " &";
    	return time;
    }
    
    public static String readNeurons(ArrayList<String[]> bufferList){
    	String neurons = "";
//    	System.out.println(bufferList.get(0)[0].split(",")[5].split(":")[1]);
//    	System.out.println(bufferList.get(0)[0].split(",")[7].split(":")[1]);
    	neurons += bufferList.get(0)[0].split(",")[5].split(":")[1] + " &";
    	neurons += bufferList.get(0)[0].split(",")[7].split(":")[1] + " &";
    	return neurons;
    }
    
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }
}