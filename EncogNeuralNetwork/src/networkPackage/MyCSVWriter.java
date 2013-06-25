package networkPackage;

import java.io.FileWriter;
import java.io.IOException;

import au.com.bytecode.opencsv.CSVWriter;

public class MyCSVWriter {
	public CSVWriter csvWriter = null;;
	
	public MyCSVWriter(){}
	
	public void writeLineToFile(String[] stringsToWrite){
		csvWriter.writeNext(stringsToWrite);
		try {
			csvWriter.flush();
		} catch (IOException e) {System.out.println(e);}
	}
	
	public void setNewOutputFile(String outputFile, String[] header){
		try {
			if(csvWriter != null) csvWriter.close();
			csvWriter = new CSVWriter(new FileWriter("newPredictions/" + outputFile));
			if(header != null){
				csvWriter.writeNext(header);
				csvWriter.flush();
			}
		} catch (IOException e) {System.out.println(e);}
	}
	
	public void setNewOutputFile(String outputFile, String[] header, boolean forOther){
		try {
			if(csvWriter != null) csvWriter.close();
			csvWriter = new CSVWriter(new FileWriter(outputFile));
			if(header != null){
				csvWriter.writeNext(header);
				csvWriter.flush();
			}
		} catch (IOException e) {System.out.println(e);}
	}
	
	public void closeIt(){
		try {
			if(csvWriter != null){
				csvWriter.flush();
				csvWriter.close();
			}
		} catch (IOException e) {System.out.println(e);}
	}
		
	public static void main(String[] args){
		String[] header = {"Something", "Something"};
		MyCSVWriter writer = new MyCSVWriter();
		writer.setNewOutputFile("test.csv", header);
		String[] strings = {"192.78", "163.84"};
		writer.writeLineToFile(strings);
		writer.closeIt();
	}
}
