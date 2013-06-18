package networkPackage;

import java.io.File;
import java.util.List;

import org.encog.app.analyst.AnalystFileFormat;
import org.encog.app.analyst.EncogAnalyst;
import org.encog.app.analyst.csv.normalize.AnalystNormalizeCSV;
import org.encog.app.analyst.script.normalize.AnalystField;
import org.encog.app.analyst.wizard.AnalystWizard;
import org.encog.util.arrayutil.NormalizationAction;
import org.encog.util.csv.CSVFormat;

public class Normalizer {
	
	public static void normalizeFiles(String inputFileString, String outputFileString) {
        File sourceFile = new File(inputFileString);
        File targetFile = new File(outputFileString);

        EncogAnalyst analyst = new EncogAnalyst();
        AnalystWizard wizard = new AnalystWizard(analyst);

        // wizard.set
        wizard.wizard(sourceFile, true, AnalystFileFormat.DECPNT_COMMA);

        dumpFieldInfo(analyst);


        final AnalystNormalizeCSV norm = new AnalystNormalizeCSV();
        norm.analyze(sourceFile, true, CSVFormat.DECIMAL_POINT, analyst);
        norm.setProduceOutputHeaders(true);
        norm.normalize(targetFile);


//        alterCSVFileForPrice(outputFileString,normalizedFileCombinedMatrixAttempt);
    }
	
    public static void dumpFieldInfo(EncogAnalyst analyst) {
        System.out.println("Fields found in file:");
        List<AnalystField> fields = analyst.getScript().getNormalize()
                .getNormalizedFields();

//        fields.get(3).setAction(NormalizationAction.PassThrough);
        //  fields.get(3).setActualHigh(41);
        //  fields.get(3).setActualLow(0);
//        fields.get(0).setAction(NormalizationAction.PassThrough);
        
        for(int i = 2; i<= 25; i++){
        	fields.get(i).setAction(NormalizationAction.PassThrough);
        }
        //   fields.get(1).setActualHigh(24);
        //  fields.get(1).setActualLow(1);
//        for (AnalystField field : fields) {
//
//            StringBuilder line = new StringBuilder();
//            line.append(field.getName());
//            line.append(",action=");
//            line.append(field.getAction());
//            line.append(",min=");
//            line.append(field.getActualLow());
//            line.append(",max=");
//            line.append(field.getActualHigh());
//            System.out.println(line.toString());
//        }
    }
}
