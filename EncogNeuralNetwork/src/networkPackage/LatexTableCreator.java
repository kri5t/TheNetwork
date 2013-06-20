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

    public static void main(String[] args) {

        File directory = new File("newPredictions/NewestPredictions/");

        File[] files = directory.listFiles();

        TreeMap<Double,String> sortedMapOfMAEs = new TreeMap<Double,String>();
        for(File f : files) {
            if(f.getName().contains("PREDICT") && !f.getName().contains("Trim"))createLatexTable(f,sortedMapOfMAEs);
        }

        int rank = 0;
        for(Map.Entry<Double,String> entry : sortedMapOfMAEs.entrySet()) {
            rank++;
            double mae = entry.getKey();
            String line = entry.getValue();
            line += mae + " & \\#" + rank + " \\\\";
            System.out.println(line);
        }
    }
    
    public static void createLatexTable(File file, TreeMap<Double,String> sortedMapOfMAEs) {
        try {
            CSVReader csvreader = new CSVReader(new FileReader(file),',');
            String totalString = "";
            String [] rows = {"Price", "Consump", "windSpeed", "temperatureRow", "timeOfDay", "weekdays", "monthOfYear", "seasonOfYear", "MATRIX"};
            for(String s : rows) {
                if(file.getName().toLowerCase().contains(s.toLowerCase())) {
                    totalString+=" x &";
                } else {
                    totalString+= " &";
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
            sortedMapOfMAEs.put(averageMae,totalString);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }
}