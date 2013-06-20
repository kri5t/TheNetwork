package networkPackage;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.commons.math3.stat.descriptive.moment.Skewness;
import org.encog.ConsoleStatusReportable;
import org.encog.Encog;
import org.encog.engine.network.activation.ActivationTANH;
import org.encog.mathutil.error.ErrorCalculation;
import org.encog.ml.data.MLData;
import org.encog.ml.data.MLDataSet;
import org.encog.ml.data.basic.BasicMLDataSet;
import org.encog.ml.train.MLTrain;
import org.encog.ml.train.strategy.HybridStrategy;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.layers.BasicLayer;
import org.encog.neural.networks.training.propagation.Propagation;
import org.encog.neural.networks.training.propagation.back.Backpropagation;
import org.encog.neural.networks.training.propagation.resilient.ResilientPropagation;
import org.encog.neural.networks.training.propagation.scg.ScaledConjugateGradient;
import org.encog.neural.pattern.FeedForwardPattern;
import org.encog.neural.pattern.JordanPattern;
import org.encog.neural.prune.PruneIncremental;
import org.encog.util.arrayutil.NormalizationAction;
import org.encog.util.arrayutil.NormalizedField;

import au.com.bytecode.opencsv.CSVReader;

public class TheNetwork1 {
	private MyCSVWriter myCsvWriter;
	private MyCSVWriter myCsvWriterOnSelf;
	private int numberOfInputs = 0;
	private int numberOfFirstLayerNeurons = 0;
	private int numberOfSecondLayerNeurons = 0;
	private CSVReader csvReader;
	private CSVReader csvReaderTest;
	private boolean limitNumberOfEntries = true;
	private boolean useTestSet = true;
	private int numberOfDataEntries = 1500;
	private int numberOfTestEntries = 24;
	private int numberOfEpochs = 2000;
	private double[][] inputArray;
	private double[][] outputArray;
	private double[][] testInputArray;
	private double[][] testOutputArray;
	private boolean mute = false;
	private boolean useHiddenLayer = false;
	private HashMap<Integer,Integer> ranges = null;
	private double sumOfValuesSeenInNetwork = 0.0;
	private double timesSeenSumOfValuesInNetwork = 0.0;
	private boolean firstRunCompleted = false;
	private static Stack<Double> priceStack;
	private static Stack<Double> decayStack;
	private static Stack<Double> skewnessStack;
	private int extraVariables = 0;
	private int curveBehaviourPlace = 0;
	private int historicalVolatilityPlace = 0;
	private int turningPointsPlace = 0;
	private int skewnessPlace = 0;
	private int averagePlace = 0;
	
	//Parameters set from here:
	private String inputFile = "YEAR_2012_DA_EXCEL_FOR_DA_PRICE_FORECAST_29-04-2013_ZeroToOne.csv";
	private static double max = 632.0;
	private static double min = 61.0;
	public int curveStackSize = 6;
	private static int priceStackSize = 2;
	public int skewnessStackSize = 12;
	public static double decay = 0.1;
	public boolean usePrediction = true;
	public boolean useLastKnown = true;
	private BasicNetwork theNetwork = null;
	public boolean trainOnce = false;
	public boolean useLastXdays = false; 
	public boolean useNewMethod = false;
	public boolean useCurveBehaviour = false;
	public boolean useHistoricalVolatility = false;
	public boolean useTurningpoints = false;
	public boolean useSkewness = false;
	public boolean useAverage = false;
	
	public TheNetwork1(int numberOfDataEntries, int numberOfTestEntries, int numberOfEpochs
						, boolean useTest, boolean limitNumberOfEntries, boolean mute
						, int numberOfNeurons, boolean useSecondLayer, int numberOfSecondLayerNeurons){
		System.out.println("The network: ");
		this.numberOfDataEntries = numberOfDataEntries;
		this.numberOfTestEntries = numberOfTestEntries;
		this.numberOfEpochs = numberOfEpochs;
		this.useTestSet = useTest;
		this.limitNumberOfEntries = limitNumberOfEntries;
		this.mute = mute;
		this.numberOfFirstLayerNeurons = numberOfNeurons;
		this.useHiddenLayer = useSecondLayer;
		this.numberOfSecondLayerNeurons = numberOfSecondLayerNeurons;
		this.myCsvWriter = new MyCSVWriter();
		this.myCsvWriterOnSelf = new MyCSVWriter();
		priceStack = new Stack<Double>();
		decayStack = new Stack<Double>();
		skewnessStack = new Stack<Double>();
	}
	
	public void setValues(int numberOfDataEntries, int numberOfTestEntries, int numberOfEpochs
			, boolean useTest, boolean limitNumberOfEntries, boolean mute, int numberOfNeurons
			, boolean useSecondLayer, int numberOfSecondLayerNeurons, HashMap<Integer,Integer> myMap){
		if(!mute){ System.out.println("DataEntries: " + numberOfDataEntries + ", TestEntries: " + numberOfTestEntries + ", Epochs: " + numberOfEpochs
				+ ", UseTestData: " + useTest + ", LimitEntries: " + limitNumberOfEntries + ", Firstlayer#: " + numberOfNeurons + 
				", SecondLayer: " + useSecondLayer + ", SecondLayer#: " + numberOfSecondLayerNeurons);}
		if(!firstRunCompleted){
			if(theNetwork != null){
				numberOfNeurons = theNetwork.getLayerNeuronCount(1);
				numberOfSecondLayerNeurons = theNetwork.getLayerNeuronCount(2);
			}
			myCsvWriterOnSelf.writeLineToFile(new String[]{"DataEntries: " + numberOfDataEntries + ", TestEntries: " + numberOfTestEntries + ", Epochs: " + numberOfEpochs
					+ ", UseTestData: " + useTest + ", LimitEntries: " + limitNumberOfEntries + ", Firstlayer#: " + numberOfNeurons + 
					", SecondLayer: " + useSecondLayer + ", SecondLayer#: " + numberOfSecondLayerNeurons+ ", Predict: " + usePrediction +
					", Curvebehaviour: " + useCurveBehaviour + ", Stacksize: " + curveStackSize + ", useLastXDays: " + useLastXdays + ", Max: " + getMax() +
					", Min: " + getMin() + ", Curve behaviour: " + useCurveBehaviour + ", Historical: " + useHistoricalVolatility + ", useAverage: " + useAverage +
					", priceStack" + getPriceStackSize() + ", Skewness: " + useSkewness + ", SkewStack: " + skewnessStackSize, ", useNewMethod: " + useNewMethod +
					", decay: " + decay});
			if(myMap != null){
				for(Map.Entry<Integer, Integer> pairs : myMap.entrySet()){
					myCsvWriterOnSelf.writeLineToFile(new String []{"Range: " + pairs.getKey() + " to " + pairs.getValue()});
				}
			}
			myCsvWriterOnSelf.writeLineToFile(new String[]{"Actual", "Ideal"});
		}
		this.numberOfDataEntries = numberOfDataEntries;
		this.numberOfTestEntries = numberOfTestEntries;
		this.numberOfEpochs = numberOfEpochs;
		this.useTestSet = useTest;
		this.limitNumberOfEntries = limitNumberOfEntries;
		this.mute = mute;
		this.numberOfFirstLayerNeurons = numberOfNeurons;
		this.useHiddenLayer = useSecondLayer;
		this.numberOfSecondLayerNeurons = numberOfSecondLayerNeurons;
		this.ranges = myMap;
	}
	
	public void setNewFile(String newFile){
		inputFile = newFile;
	}
	
	public void countExtraVariables(){
		int extras = 0;
		if(useCurveBehaviour) extras ++;
		if(useHistoricalVolatility) extras ++;
		if(useTurningpoints) extras ++;
		if(useSkewness) extras ++;
		if(useAverage) extras ++;
		
		extraVariables = extras;
	}
	
	public void initPlaces(){
		List<String> list = new ArrayList<String>();
		if(useCurveBehaviour) list.add("curve");
		if(useHistoricalVolatility) list.add("hist");
		if(useTurningpoints) list.add("turning");
		if(useSkewness) list.add("skew");
		if(useAverage) list.add("average");
		for(int i = 0; i < list.size(); i++){
			if(list.get(i) == "curve") curveBehaviourPlace = i+1;
			if(list.get(i) == "hist") historicalVolatilityPlace = i+1;
			if(list.get(i) == "turning") turningPointsPlace = i+1;
			if(list.get(i) == "skew") skewnessPlace = i+1;
			if(list.get(i) == "average") averagePlace = i+1;
		}
	}
	
	public void run(){
		countExtraVariables();
		initPlaces();
		if(ranges != null) createDataSetSpecificMonths(ranges);
		else createDataSet();
		// create a neural network, without using a factory
		BasicNetwork network;
		if(theNetwork == null) network = createNormalNetwork();
		else network = theNetwork;
		// create training data
		MLDataSet trainingSet = new BasicMLDataSet(inputArray, outputArray);
		if(useTestSet) {
			createVerificationDataSet();
			testingSet = new BasicMLDataSet(testInputArray, testOutputArray);
		}
//		System.out.println("Set size: " + trainingSet.size());
		
		// train the neural network
		Propagation train = null;
		
		if(useNewMethod){
			train = new Backpropagation(network, trainingSet, 0.00001, 0.05);
	        final MLTrain trainAlt = new ScaledConjugateGradient(network, trainingSet);
	        train.addStrategy(new HybridStrategy(trainAlt));
        }
		else {
			train = new ResilientPropagation(network, trainingSet);
		}
		
		
//		EarlyStoppingStrategy strategy = new EarlyStoppingStrategy(testingSet, trainingSet);
//		
//		train.addStrategy(strategy);
//		final MultiPropagation train = new MultiPropagation(network, trainingSet);
//		final Train train = new ManhattanPropagation(network, trainingSet,0.0001);
//		final LevenbergMarquardtTraining train = new LevenbergMarquardtTraining(network, trainingSet);
		train.setThreadCount(8);
		if(!trainOnce || (theNetwork != null && trainOnce)){
			int epoch = 1;
			do {
				train.iteration();
				if(epoch % 1000 == 0 && !mute) System.out.println("Epoch #" + epoch + " Error:" + train.getError());
				epoch++;
	//		} while(train.getError() > 0.005);
			} while(epoch < numberOfEpochs);
	
			train.finishTraining();
		}
		
		if(trainOnce && theNetwork != null){
			theNetwork = network;
		}
		
		// test the neural network
		int numberOfSets;
		boolean doneOnTrainingSet;
		boolean onTestSet;
		Stack<Double> stack = new Stack<Double>();
		if(trainOnce){
			numberOfSets = 1;
			doneOnTrainingSet = true;
			onTestSet = true;
		}
		else{
			numberOfSets = 0;
			doneOnTrainingSet = false;
			onTestSet = false;
		}
		priceStack = new Stack<Double>();
		skewnessStack = new Stack<Double>();
		while(numberOfSets < 2){
			if(!mute)System.out.println("Neural Network Results:");
			double largestError = 0;
			ArrayList<Double> errors = new ArrayList<Double>();
			String toPrint = "";
			int o = 0;
			ErrorCalculation errorCalc = new ErrorCalculation();
			
			if(useTestSet && doneOnTrainingSet){ 
				trainingSet = testingSet;
				onTestSet = true;
			}
			if(!mute)System.out.println("Set size: " + trainingSet.size());
			for(int j = 0; j < trainingSet.size(); j++ ) {
				o++;
				MLData output = network.compute(trainingSet.get(j).getInput());
//				if(j<20) System.out.println(trainingSet.get(j).toString());
				if(onTestSet && usePrediction && j < trainingSet.size()-1){
					int offset = 1 + extraVariables;
					int lastSeenIndex = trainingSet.getInputSize()-offset;
					if(useLastXdays){
						setLastXdays(offset, trainingSet, j, output.getData(0));
					}else{
						trainingSet.get(j+1).getInput().setData(lastSeenIndex, output.getData(0));
					}
				}
				if(onTestSet && usePrediction && useCurveBehaviour && j < trainingSet.size()-1){
					
					if(stack.size()>=curveStackSize) {
						stack.remove(0);
					}
					stack.push(output.getData(0));
					double curveBehaviour = analyzeCurveIntelligentV3(stack);
					// j has to be less than the size of the testingSet. To not get index out of bounds.
					if(j < trainingSet.size()){
						trainingSet.get(j+1).getInput().setData(trainingSet.getInputSize()-1-extraVariables+curveBehaviourPlace, curveBehaviour);
					}
				}
				if(onTestSet && usePrediction && useHistoricalVolatility && j < trainingSet.size()-1){
					trainingSet.get(j+1).getInput().setData(trainingSet.getInputSize()-1-extraVariables+historicalVolatilityPlace, historicalVolatilityEWMA());
				}
				if(onTestSet && usePrediction && useTurningpoints && j < trainingSet.size()-1){
					trainingSet.get(j+1).getInput().setData(trainingSet.getInputSize()-1-extraVariables+turningPointsPlace, turningPoints());
				}
				if(onTestSet && usePrediction && useSkewness && j < trainingSet.size()-1){
					trainingSet.get(j+1).getInput().setData(trainingSet.getInputSize()-1-extraVariables+skewnessPlace, calculateSkewness());
				}
				
				if(onTestSet && usePrediction && useAverage && j < trainingSet.size()-1){
					trainingSet.get(j+1).getInput().setData(trainingSet.getInputSize()-1-extraVariables+averagePlace, averageOverLastPrices());
				}
				
				if(onTestSet && (useTurningpoints||useHistoricalVolatility)){
					priceStack.push(output.getData(0));
					if(priceStack.size() > getPriceStackSize()) priceStack.remove(0);
				}
				if(onTestSet && useSkewness){
					skewnessStack.push(output.getData(0));
					if(skewnessStack.size() > skewnessStackSize) skewnessStack.remove(0);
				}
				
				
				if(!onTestSet && trainingSet.size()-curveStackSize <= j && j < trainingSet.size() && useCurveBehaviour){
					stack.push(trainingSet.get(j).getIdeal().getData(0));
				}
				if(!onTestSet && trainingSet.size()-getPriceStackSize() <= j && j < trainingSet.size() && (useHistoricalVolatility||useTurningpoints||useAverage)){
					priceStack.push(trainingSet.get(j).getIdeal().getData(0));
				}
				if(!onTestSet && trainingSet.size()-skewnessStackSize <= j && j < trainingSet.size() && useSkewness){
					skewnessStack.push(trainingSet.get(j).getIdeal().getData(0));
				}
				
				if(!mute && 0 < j && j < 24){
					System.out.println(trainingSet.get(j).getInput() + "  " + trainingSet.get(j).getIdeal());
				}
				double ideal = deNormalizeMinusOneToOne(trainingSet.get(j).getIdeal().getData(0));
				double actual = deNormalizeMinusOneToOne(output.getData(0));
//				double ideal = denormalizePrice(trainingSet.get(j).getIdeal().getData(0));
//				double actual = denormalizePrice(output.getData(0));
				double percentAbreviation = Math.round(Math.abs(ideal - actual)/ideal*100);
				
				if(!doneOnTrainingSet && !firstRunCompleted){
					myCsvWriterOnSelf.writeLineToFile(new String[]{""+actual, ""+ideal});
				}
				if(firstRunCompleted && onTestSet){
					myCsvWriter.writeLineToFile(new String[]{""+actual, ""+ideal});
				}
				
//				if(!mute) System.out.println("actual=" + actual + ",ideal=" + ideal + ", error=" + percentAbreviation + "%");
				
				errors.add(percentAbreviation);
				
				if(percentAbreviation > largestError)
				{
					largestError = percentAbreviation;
					toPrint = ("actual=" + actual + ",ideal=" + ideal +
							", error=" + percentAbreviation + "%" + " lineNumber = " + o);
				}
				errorCalc.updateError(output.getData(0), trainingSet.get(j).getIdeal().getData(0));
			}
			double totalErrors = 0.0;		
			for(Double error : errors){
				 totalErrors += error;
			}
			
//			System.out.println("Total errors: " + totalErrors);
//			System.out.println("Largest error: " + largestError);
			if(doneOnTrainingSet){
				sumOfValuesSeenInNetwork += (totalErrors / (errors.size()));
				timesSeenSumOfValuesInNetwork++;
			}
			if(!mute){
				System.out.println(toPrint);
				System.out.println("Average error margin: " + (totalErrors / (errors.size())));
				
				System.out.println("MSE: " + errorCalc.calculate());
				System.out.println("ESS: " + errorCalc.calculateESS());
				System.out.println("RMS: " + errorCalc.calculateRMS());
				System.out.println(" ");
			}
			doneOnTrainingSet = true;
			firstRunCompleted = true;
			numberOfSets++;
		}
		
	}
	
	public static BasicNetwork createJordanNetwork() {
		// construct an Jordan type network
		JordanPattern pattern = new JordanPattern();
		pattern.setActivationFunction(new ActivationTANH());
		pattern.setInputNeurons(27);
		pattern.addHiddenLayer(2);
		pattern.setOutputNeurons(1);
		return (BasicNetwork)pattern.generate();
	}
	
	public BasicNetwork createNormalNetwork(){
		BasicNetwork network = new BasicNetwork();
		network.addLayer(new BasicLayer(null,true,numberOfInputs));
		if(!mute)System.out.println("Number of input neurons: " + numberOfInputs);
		network.addLayer(new BasicLayer(new ActivationTANH(),true,numberOfFirstLayerNeurons));
		if(useHiddenLayer) network.addLayer(new BasicLayer(new ActivationTANH(),true,numberOfSecondLayerNeurons));
		network.addLayer(new BasicLayer(new ActivationTANH(),false,1));
		network.getStructure().finalizeStructure();
		network.reset();
		return network;
	}
	
	public boolean getUsePrediction(){
		return usePrediction;
	}
	
	public void setUsePrediction(boolean usePrediction){
		if(!mute)System.out.println("Use prediction: " + usePrediction);
		this.usePrediction = usePrediction;
	}
	
	public boolean getUseLastKnown(){
		return useLastKnown;
	}
	
	public void setUseLastKnown(boolean useLastKnown){
		System.out.println("Use last known: " + useLastKnown);
		this.useLastKnown = useLastKnown;
	}
	
	public static double deNormalizeMinusOneToOne(double data){
		return (data * (getMax() - getMin()) / 2) + ((getMax() + getMin()) / 2);
	}
	
//	final static String inputFile = "YEAR_2012_DA_EXCEL_FOR_DA_PRICE_FORECAST_29-04-2013_brian.csv";
	private static MLDataSet testingSet;
	
	public void createDataSet(){
		try {
			Stack<Double> stack = new Stack<Double>();
			csvReader = new CSVReader(new FileReader(inputFile));
			String [] nextLine;
			List<double[]> inputData = new ArrayList<double[]>();
			List<double[]> outputData = new ArrayList<double[]>();
			int j = 0;
			while ((nextLine = csvReader.readNext()) != null && (j < numberOfDataEntries || !limitNumberOfEntries)) {
				double[] input = createInputDataSet(nextLine, stack);
				double output[] = { Double.parseDouble(nextLine[nextLine.length-1])};
				outputData.add(output);
				inputData.add(input);
				j++;
			}
			inputArray = new double[inputData.size()][];
			outputArray = new double[inputData.size()][];
			for(int i = 0; i<inputData.size(); i++){
				inputArray[i] = inputData.get(i);
				outputArray[i] = outputData.get(i);
			}
//		    for(int i = 0; i < inputArray[1].length; i++){
//		    	System.out.println(inputArray[1][i]);
//		    }
		} catch (Exception e) {e.printStackTrace();}
	}
	
	public void createDataSetSpecificMonths(Map<Integer, Integer> ranges){
		try {
			Stack<Double> stack = new Stack<Double>();
			for(int i = 0; i < 8; i++){
				stack.push(0.0);
			}
			String [] nextLine;
			List<double[]> inputData = new ArrayList<double[]>();
			List<double[]> outputData = new ArrayList<double[]>();
			boolean inRange = false;
			csvReader = new CSVReader(new FileReader(inputFile));
			int j = 0;
		    while ((nextLine = csvReader.readNext()) != null) {
		    	j++;
		    	for(Map.Entry<Integer, Integer> pairs : ranges.entrySet()){
		            if((Integer)pairs.getKey() <= j && j < (Integer)pairs.getValue()){ 
		            	inRange = true;
		            	break;
		            }
		            else inRange = false;
		        }
		    	if (!inRange) continue;
		    	double[] input = createInputDataSet(nextLine, stack);
		    	double output[] = { Double.parseDouble(nextLine[nextLine.length-1])};
		        outputData.add(output);
		        inputData.add(input);
		    }
//		    System.out.println("J: " + j);
		    inputArray = new double[inputData.size()][];
		    outputArray = new double[inputData.size()][];
		    for(int i = 0; i<inputData.size(); i++){
		    	inputArray[i] = inputData.get(i);
		    	outputArray[i] = outputData.get(i);
		    }
		    if(!mute){
			    System.out.println("BiggestSkew: " + biggestSkew + " LowestSkew: " + lowestSkew);
		    	System.out.println(inputArray.length);
		    }
		} catch (Exception e) {e.printStackTrace();}
	}
	
	private double lastKnownInt = 0;
	public void createVerificationDataSet(){
		try {
			csvReaderTest = new CSVReader(new FileReader(inputFile));
			Stack<Double> stack = new Stack<Double>();
			String [] nextLine;
			List<double[]> inputData = new ArrayList<double[]>();
			List<double[]> outputData = new ArrayList<double[]>();
			int j = 0;
		    while ((nextLine = csvReaderTest.readNext()) != null && (j < numberOfDataEntries + numberOfTestEntries || !useTestSet)) {
		    	j++;
		    	if(j == numberOfDataEntries) lastKnownInt = Double.parseDouble(nextLine[nextLine.length-2]);
		    	if(j <= numberOfDataEntries) continue;
		    	double[] input = createInputDataSet(nextLine, stack);
		    	
		    	double output[] = { Double.parseDouble(nextLine[nextLine.length-1])};
		        outputData.add(output);
		        inputData.add(input);
		    }
		    testInputArray = new double[inputData.size()][];
		    testOutputArray = new double[inputData.size()][];
		    for(int i = 0; i<inputData.size(); i++){
		    	testInputArray[i] = inputData.get(i);
		    	testOutputArray[i] = outputData.get(i);
		    }
		} catch (Exception e) {e.printStackTrace();}
	}
	
	public double[] createInputDataSet(String[] nextLine, Stack<Double> stack){
		if(numberOfInputs == 0) numberOfInputs = nextLine.length-1+extraVariables;
		double[] input = new double[nextLine.length-1+extraVariables];
		//System.out.println();
		for(int i = 0; i <= numberOfInputs; i++){
    		if(i == nextLine.length -2 && useLastKnown) input[i] = lastKnownInt;
			if(i < nextLine.length-1){
				input[i] = Double.parseDouble(nextLine[i]);	
				//System.out.println("NextLine: " + nextLine[i] + "double: " + Double.parseDouble(nextLine[i]));
			}
			if(useHistoricalVolatility && i == nextLine.length-2+historicalVolatilityPlace){
	    		input[i] = historicalVolatilityEWMA();
			}
			if(useCurveBehaviour && i == nextLine.length-2+curveBehaviourPlace){
				input[i] = analyzeCurveIntelligentV3(stack);
				if(stack.size()>=curveStackSize) stack.remove(0);
				stack.push(Double.parseDouble(nextLine[nextLine.length-1]));
				
			}
			if(useTurningpoints && i == nextLine.length-2+turningPointsPlace){
				input[i] = turningPoints();
			}
			if(useSkewness && i == nextLine.length-2+skewnessPlace){
				input[i] = calculateSkewness();
			}
			if(useAverage && i == nextLine.length-2+averagePlace){
				input[i] = averageOverLastPrices(); 
			}
			
			if(i == numberOfInputs){
				if(priceStack.size() > getPriceStackSize()) priceStack.remove(0);
				priceStack.push(Double.parseDouble(nextLine[nextLine.length-1]));
				if(skewnessStack.size() > skewnessStackSize) skewnessStack.remove(0);
				skewnessStack.push(Double.parseDouble(nextLine[nextLine.length-1]));
			}
    	}
//		String s  = "nextLine: ";
//		for(String d : nextLine) s += d + ",";
//		System.out.println(s);
//		
//		String o  = "Input   : ";
//		for(double d : input) o += d + ",";
//		System.out.println(o + inputFile);
		
		return input;
	}
	
	public void calculateAverageOfValuesSeen(){
		System.out.println("AverageOfAveragesSeen: " + (sumOfValuesSeenInNetwork / timesSeenSumOfValuesInNetwork));
		
		reset();
	}
	
	public void reset(){
		numberOfInputs = 0;
		sumOfValuesSeenInNetwork = 0;
		timesSeenSumOfValuesInNetwork = 0;
		firstRunCompleted = false;
		priceStack = new Stack<Double>();
		decayStack = new Stack<Double>();
		skewnessStack = new Stack<Double>();
	}
	
	public static double denormalizePrice(double value) {	        
        NormalizedField norm = new NormalizedField(NormalizationAction.Normalize,
                null,getMax(),getMin(),1,-1);
        return norm.deNormalize(value);
    }
	
	public void reconfigCsvWriter(String outputFileWithoutExtension, String[] headerArray){
		myCsvWriter.closeIt();
		String extension = System.currentTimeMillis() + "";
		myCsvWriter.setNewOutputFile(outputFileWithoutExtension + 
										"_PREDICT"+extension+".csv", headerArray);
		myCsvWriterOnSelf.closeIt();
		myCsvWriterOnSelf.setNewOutputFile(outputFileWithoutExtension + 
											"_SELF"+extension+".csv", null);
	}

	public static double analyzeCurveIntelligentV3(Stack<Double> stack) {
		double behaviourValue = 0;
		for(int i = stack.size()-1; i>0;i=i-1) {
			double valToCompare1 = stack.get(i);
			double valToCompare2 = stack.get(i-1);
			double comparison = valToCompare1-valToCompare2;
			behaviourValue+=comparison;
		}
		return behaviourValue;
	}
	
	public void checkNetwork(int testSetStart, int testSetStop){
		theNetwork = null;
		HashMap<Integer,Integer> ranges = new HashMap<Integer,Integer>();
		ranges.put(testSetStart, testSetStop);
		createDataSetSpecificMonths(ranges);
		MLDataSet trainingSet = new BasicMLDataSet(inputArray, outputArray);
		
		FeedForwardPattern pattern = new FeedForwardPattern();
        pattern.setInputNeurons(trainingSet.getInputSize());
        pattern.setOutputNeurons(trainingSet.getIdealSize());
        pattern.setActivationFunction(new ActivationTANH());

        PruneIncremental prune = new PruneIncremental(trainingSet,pattern, 200, 4, 10,
                new ConsoleStatusReportable());

        prune.addHiddenLayer(5, 30);
        prune.addHiddenLayer(0, 30);

        prune.process();

        theNetwork = prune.getBestNetwork();
        theNetwork.getLayerTotalNeuronCount(0);
        System.out.println("Best network info Architecture: " + theNetwork.getFactoryArchitecture());
        System.out.println("Best network layers: " + theNetwork.getLayerCount());
        System.out.println("Best network info: " + theNetwork.getStructure().toString());
	}
	
	private void setLastXdays(int offset, MLDataSet trainingSet, int j, double predicted){
//		int[] values = {10, 9, 8, 7, 6, 5, 4, 3, 2, 2, 1, 1, 1};
		MLData getInput = trainingSet.get(j).getInput();
		MLData setInput = trainingSet.get(j+1).getInput();
		int size = getInput.size()-1;
		int indexToGet = size - 11 - offset;
		setInput.setData(indexToGet-1, getInput.getData(indexToGet));
		indexToGet = size - 10 - offset;
		setInput.setData(indexToGet-1, getInput.getData(indexToGet));
		indexToGet = size - 9 - offset;
		setInput.setData(indexToGet-1, getInput.getData(indexToGet));
		indexToGet = size - 8 - offset;
		setInput.setData(indexToGet-1, getInput.getData(indexToGet));
		indexToGet = size - 7 - offset;
		setInput.setData(indexToGet-1, getInput.getData(indexToGet));
		indexToGet = size - 6 - offset;
		setInput.setData(indexToGet-1, getInput.getData(indexToGet));
		indexToGet = size - 5 - offset;
		setInput.setData(indexToGet-1, getInput.getData(indexToGet));
		indexToGet = size - 2 - offset;
		setInput.setData(indexToGet-2, getInput.getData(indexToGet));
		setInput.setData(indexToGet-1, getInput.getData(indexToGet));
		indexToGet = size - 0 - offset;
		setInput.setData(indexToGet-2, predicted);
		setInput.setData(indexToGet-1, predicted);
		//setInput.setData(indexToGet, predicted);
	}

	public static double historicalVolatilityEWMA() {
        double ewma = 0.0;
        if(priceStack.size()>0) {
            double ewmaFromOneDayAgo = 0;
            if(decayStack.size()>0) {
                ewmaFromOneDayAgo = decayStack.get(decayStack.size()-1);
            }
            double productionFromOneDayAgo = priceStack.get(priceStack.size()-1);
            ewma = (decay*ewmaFromOneDayAgo) + ((1-decay)*productionFromOneDayAgo);
            decayStack.push(ewma);
            if(decayStack.size()>getPriceStackSize()) {
                decayStack.remove(0);
            }
        }
        return ewma;
    }
	
	public double averageOverLastPrices(){
		double prices = 0.0;
		int pricesSeen = 0;
		for(double price : priceStack){
			if(price != 0.0){
				prices += price;
				pricesSeen++;
			}
		}
		if(pricesSeen != 0) return prices / pricesSeen;
		else return 0.0;
	}
	
	private static double biggestSkew = 0;
	private static double lowestSkew = 0;
	
	public static double calculateSkewness() {
		Skewness skewness = new Skewness();
		double skew = 0.0;
		if(skewnessStack.size()>5) {
			double tempSkew = skewness.evaluate(turnDoubleStackToDoubleArray(skewnessStack));
			if(!Double.isNaN(skew)) {
				skew = normalizePrice(tempSkew, 3.3, -3.3);
			}
		}
		if(biggestSkew == 0 || biggestSkew < skew){
			biggestSkew = skew;
		}
		if(lowestSkew == 0 || lowestSkew > skew){
			lowestSkew = skew;
		}
		
		return skew;
	}

    public static double normalizePrice(double value, double max, double min) {
        NormalizedField norm = new NormalizedField(NormalizationAction.Normalize,
                null,max,min,1,-1);
        return norm.normalize(value);
    }
	
	private static double[] turnDoubleStackToDoubleArray(Stack<Double> doubles) {
		double[] target = new double[doubles.size()];
		for (int i = 0; i < target.length; i++) {
			target[i] = doubles.get(i); // java 1.5+ style (outboxing)
		}
		return target;
	}

    public static double turningPoints() {
        double turningPoints = 0;
        if(priceStack.size()>=3) {
            for(int i = 1;i<priceStack.size()-1;i++) {
                double x_current = priceStack.get(i);
                double x_plus_1 = priceStack.get(i+1);
                double x_minus_1 = priceStack.get(i-1);
                double turningPoint = (x_plus_1-x_current)*(x_current-x_minus_1);
                if(turningPoint<0) {
                    turningPoints++;
                }
            }
        }
        return normalizeTurningPoints(turningPoints);
    }
    
    private static double normalizeTurningPoints(double turningPoints) {
        int max = 24;
        int min = 0;
        NormalizedField norm = new NormalizedField(NormalizationAction.Normalize,
                null,max,min,1,-1);
        return norm.normalize(turningPoints);
    }
	
    public void runOneYear(String fileName, int testSetStart, int testSetStop, int epochs, int offSet, int hoursToPredictAhead, int divider){
    	String[] headerArray = {"actual", "ideal"};
    	this.reconfigCsvWriter(fileName, headerArray);
		for(int i = 0; i<(7680/divider/hoursToPredictAhead); i++){ //
			HashMap<Integer,Integer> ranges = new HashMap<Integer,Integer>();
			int index = i * hoursToPredictAhead;
			ranges.put(testSetStart+index+offSet, testSetStop+index+offSet);
			this.setValues(testSetStop+index+offSet, hoursToPredictAhead, epochs, true, true, mute, 6, true, 14, ranges);
			this.run();
		}
		this.calculateAverageOfValuesSeen();
		this.reset();
    }
    
    public void setMethods(boolean useHistorical,boolean useCurve,boolean useSkew){
    	this.useHistoricalVolatility = useHistorical;
    	this.useCurveBehaviour = useCurve;
    	this.useSkewness = useSkew;
    	
    }
    
    public static void removeUnwantedItems(File[] listOfFiles){
    	List<String> tenBestFilesList = new ArrayList<String>();
    	for(File file : listOfFiles){
			if(file.getName().contains("1PTrim")){
				String fileName = file.getName().substring(file.getName().indexOf("_") +1, file.getName().length());
				fileName = fileName.substring(0, fileName.indexOf(".csv"));
				if(fileName.substring(fileName.length() -1).equals("_")) fileName = fileName.substring(0, fileName.length()-1);
				tenBestFilesList.add(fileName);
			}
		}
		
		boolean weAreGood = false;
		for(File f : listOfFiles){
			if(f.getName().contains("00")){
				String[] fileName = f.getName().split("00");
				for(String name : tenBestFilesList){
					if((fileName[1].equals(name))){
						weAreGood = true;
					}
				}
				if(!weAreGood) {f.delete();}
				weAreGood = false;
			}
		}
    }
    
	public static void main(String[] args){
		
//		numberOfDataEntries, numberOfTestEntries, numberOfEpochs, useTest, limitNumberOfEntries, mute, numberOfNeurons, useSecondLayer, numberOfSecondLayerNeurons
		
//		Normalizer.normalizeFiles("YEAR_2012_DA_EXCEL_FOR_DA_PRICE_FORECAST_29-04-2013_NotNormalized.csv","YEAR_2012_DA_EXCEL_FOR_DA_PRICE_FORECAST_29-04-2013_ZeroToOneNORMALIZED.csv");
		boolean mute = true;
		TheNetwork1 network = new TheNetwork1(7000, 24, 10000, true, true, mute, 20, false, 0);
		network.setUsePrediction(true);
		
//		theNetwork.useLastXdays = true;
//		int start = 0;
		//theNetwork.checkNetwork(start, 8756);
		//historical -- curve -- skew
//		theNetwork.setNewFile("YEAR_2012_DA_EXCEL_FOR_DA_PRICE_FORECAST_29-04-2013_ZeroToOne_withLastTenPrices.csv");		
//		theNetwork.setMethods(false, false, false);
//		theNetwork.runOneYear("2pTrim_1YearTrain_LastTenDays_Historical", start, 8756, 3000, 0);
		
		
		
		int start = 0;
//		theNetwork.setNewFile("YEAR_2012_DA_EXCEL_FOR_DA_PRICE_FORECAST_29-04-2013_ZeroToOne.csv");	
//		theNetwork.checkNetwork(start, 8756);
//		theNetwork.setMethods(false, false, false);
//		theNetwork.runOneYear("2pTrim_1YearTrain_StandardSet_Standard", start, 8756, 3000, 672);
//		theNetwork.setMethods(true, false, false);
//		theNetwork.runOneYear("2pTrim_1YearTrain_StandardSet_Historical", start, 8756, 3000, 672);
//		theNetwork.setMethods(true, true, false);
//		theNetwork.runOneYear("2pTrim_1YearTrain_StandardSet_Historical_Curve", start, 8756, 3000, 672);
//		theNetwork.setMethods(true, false, true);
//		theNetwork.runOneYear("2pTrim_1YearTrain_StandardSet_Historical_Skew", start, 8756, 3000, 672);
//		theNetwork.setMethods(false, true, false);
//		theNetwork.runOneYear("2pTrim_1YearTrain_StandardSet_Curve", start, 8756, 3000, 672);
//		theNetwork.setMethods(false, false, true);
//		theNetwork.runOneYear("2pTrim_1YearTrain_StandardSet_Skew", start, 8756, 3000, 672);
		
//		network.setNewFile("YEAR_2012_DA_EXCEL_FOR_DA_PRICE_FORECAST_29-04-2013_ZeroToOne_withPaperPrices.csv");		
//		network.checkNetwork(0, 8756);
//		network.setMethods(false, false, false);
//		network.runOneYear("2pTrim_1YearTrain_PaperSetup_Standard", start, 8756, 3000, 0);
//		network.setMethods(true, false, false);
//		network.runOneYear("2pTrim_1YearTrain_PaperSetup_Historical", start, 8756, 3000, 0);
//		network.setMethods(true, true, false);
//		network.runOneYear("2pTrim_1YearTrain_PaperSetup_Historical_Curve", start, 8756, 3000, 0);
//		network.setMethods(true, false, true);
//		network.runOneYear("2pTrim_1YearTrain_PaperSetup_Historical_Skew", start, 8756, 3000, 0);
//		network.setMethods(false, true, false);
//		network.runOneYear("2pTrim_1YearTrain_PaperSetup_Curve", start, 8756, 3000, 0);
//		network.setMethods(false, false, true);
//		network.runOneYear("2pTrim_1YearTrain_PaperSetup_Skew", start, 8756, 3000, 0);
//		
//		start = 4378;
//		network.setNewFile("YEAR_2012_DA_EXCEL_FOR_DA_PRICE_FORECAST_29-04-2013_ZeroToOne.csv");	
//		network.checkNetwork(start, 8756);
//		network.setMethods(false, false, false);
//		network.runOneYear("2pTrim_HalfYearTrain_StandardSet_Standard", start, 8756, 3000, 672);
//		network.setMethods(true, false, false);
//		network.runOneYear("2pTrim_HalfYearTrain_StandardSet_Historical", start, 8756, 3000, 672);
//		network.setMethods(true, true, false);
//		network.runOneYear("2pTrim_HalfYearTrain_StandardSet_Historical_Curve", start, 8756, 3000, 672);
//		network.setMethods(true, false, true);
//		network.runOneYear("2pTrim_HalfYearTrain_StandardSet_Historical_Skew", start, 8756, 3000, 672);
//		network.setMethods(false, true, false);
//		network.runOneYear("2pTrim_HalfYearTrain_StandardSet_Curve", start, 8756, 3000, 672);
//		network.setMethods(false, false, true);
//		network.runOneYear("2pTrim_HalfYearTrain_StandardSet_Skew", start, 8756, 3000, 672);
////		
//		network.setNewFile("YEAR_2012_DA_EXCEL_FOR_DA_PRICE_FORECAST_29-04-2013_ZeroToOne_withPaperPrices.csv");		
//		network.checkNetwork(start, 8756);
//		network.setMethods(false, false, false);
//		network.runOneYear("2pTrim_HalfYearTrain_PaperSetup_Standard", start, 8756, 3000, 0);
//		network.setMethods(true, false, false);
//		network.runOneYear("2pTrim_HalfYearTrain_PaperSetup_Historical", start, 8756, 3000, 0);
//		network.setMethods(true, true, false);
//		network.runOneYear("2pTrim_HalfYearTrain_PaperSetup_Historical_Curve", start, 8756, 3000, 0);
//		network.setMethods(true, false, true);
//		network.runOneYear("2pTrim_HalfYearTrain_PaperSetup_Historical_Skew", start, 8756, 3000, 0);
//		network.setMethods(false, true, false);
//		network.runOneYear("2pTrim_HalfYearTrain_PaperSetup_Curve", start, 8756, 3000, 0);
//		network.setMethods(false, false, true);
//		network.runOneYear("2pTrim_HalfYearTrain_PaperSetup_Skew", start, 8756, 3000, 0);
//		
		start = 0;
//		network.setNewFile("YEAR_2012_DA_EXCEL_FOR_DA_PRICE_FORECAST_29-04-2013_ZeroToOne.csv");	
//		network.checkNetwork(start, 8756);
//		network.setMethods(false, false, false);
//		network.runOneYear("2pTrim_QuarterYearTrain_StandardSet_Historical", start, 8756, 3000, 672);
//		network.setMethods(true, false, false);
//		network.runOneYear("2pTrim_QuarterYearTrain_StandardSet_Historical", start, 8756, 3000, 672);
//		network.setMethods(true, true, false);
//		network.runOneYear("2pTrim_QuarterYearTrain_StandardSet_Historical_Curve", start, 8756, 3000, 672);
//		network.setMethods(true, false, true);
//		network.runOneYear("2pTrim_QuarterYearTrain_StandardSet_Historical_Skew", start, 8756, 3000, 672);
//		network.setMethods(false, true, false);
//		network.runOneYear("2pTrim_QuarterYearTrain_StandardSet_Curve", start, 8756, 3000, 672);
//		network.setMethods(false, false, true);
//		network.runOneYear("2pTrim_QuarterYearTrain_StandardSet_Skew", start, 8756, 3000, 672);
//		
		
		network.setNewFile("YEAR_2012_DA_EXCEL_FOR_DA_PRICE_FORECAST_29-04-2013_ZeroToOne_withPaperPrices.csv");		
			
//		network.checkNetwork(start, 8756);
//		network.setMethods(true, false, false);
//		network.runOneYear("2pTrim_QuarterYearTrain_PaperSetup_Standard", start, 8756, 3000, 0);
//		network.setMethods(true, false, false);
//		network.runOneYear("24daysAverage_24hist_2pTrim_QuarterYearTrain_PaperSetup_Historical", start, 8756, 3000, 0);
//		network.setMethods(true, true, false);
//		network.runOneYear("2pTrim_QuarterYearTrain_PaperSetup_Historical_Curve", start, 8756, 3000, 0);
//		network.setMethods(true, false, true);
//		network.runOneYear("24daysAverage_24hist_12skew_2pTrim_QuarterYearTrain_PaperSetup_Historical_Skew", start, 8756, 3000, 0);
//		network.setMethods(false, true, false);
//		network.runOneYear("2pTrim_QuarterYearTrain_PaperSetup_Curve", start, 8756, 3000, 0);
//		network.setMethods(false, false, true);
//		network.runOneYear("2pTrim_QuarterYearTrain_PaperSetup_Skew", start, 8756, 3000, 0);
		
//		network.useNewMethod = false;
//		network.useAverage = false;
		
//		network.checkNetwork(start, 8756);
//		network.skewnessStackSize = 24;
//		network.setMethods(false, false, true);
//		network.runOneYear("Normalized24skew_2pTrim_QuarterYearTrain_PaperSetup_Skew", start, 8756, 3000, 0);
//		
//		network.skewnessStackSize = 48;
//		network.setMethods(false, false, true);
//		network.runOneYear("Normalized48skew_2pTrim_QuarterYearTrain_PaperSetup_Skew", start, 8756, 3000, 0);
//		
//		network.skewnessStackSize = 24;
//		network.priceStackSize = 24;
//		network.setMethods(true, false, false);
//		network.runOneYear("Historical_2pTrim_QuarterYearTrain_PaperSetup_Skew", start, 8756, 3000, 0);
		
		
//		network.runOneYear("1HOURAHEAD_24Historical_200epochs_2pTrim_QuarterYearTrain_PaperSetup", start, 8756, 200, 0, 1, 1);
//		network.runOneYear("3HOURAHEAD_24Historical_200epochs_2pTrim_QuarterYearTrain_PaperSetup", start, 8756, 200, 0, 2, 1);
//		network.runOneYear("3HOURAHEAD_24Historical_200epochs_2pTrim_QuarterYearTrain_PaperSetup", start, 8756, 200, 0, 3, 1);
//		network.runOneYear("4HOURAHEAD_24Historical_200epochs_2pTrim_QuarterYearTrain_PaperSetup", start, 8756, 200, 0, 4, 1);
//		network.runOneYear("5HOURAHEAD_24Historical_200epochs_2pTrim_QuarterYearTrain_PaperSetup", start, 8756, 200, 0, 5, 1);
//		network.runOneYear("6HOURAHEAD_24Historical_200epochs_2pTrim_QuarterYearTrain_PaperSetup", start, 8756, 200, 0, 6, 1);
//		network.runOneYear("7HOURAHEAD_24Historical_200epochs_2pTrim_QuarterYearTrain_PaperSetup", start, 8756, 200, 0, 7, 1);
//		network.setNewFile("YEAR_2012_DA_EXCEL_FOR_DA_PRICE_FORECAST_29-04-2013_ZeroToOne_withPaperPrices_1PTrim_Weekday.csv");	
//		for(double j = 0; j < 10; j += 1){
//			//network.checkNetwork(start, 8756);
//			network.decay = 0.5;
//			network.runOneYear(j+"ATTEMT24HOURAHEAD_decay0.5_1Historical_200epochs_2pTrim_QuarterYearTrain_PaperSetup_WithWeekdays_1PTrim_Weekday", start, 8756, 200, 0, 24, 1);
//		}
		File folderen = new File("runFilesFolder");
		File[] listOfFiles = folderen.listFiles();
		//TheNetwork1.removeUnwantedItems(listOfFiles);
		
			for (File file : listOfFiles) {
				start = 6567;
				if (file.isFile() && !file.getName().equals(".DS_Store")) {
					network.setMax(1561);
					network.setMin(1);
					if(file.getName().contains("1PTrim")){
						network.setMax(631.0);
						network.setMin(62.0);
					}
					if(file.getName().contains("2PTrim")){
						network.setMax(558.0);
						network.setMin(74.0);
					}
					if(file.getName().contains("3PTrim")){
						network.setMax(524.0);
						network.setMin(86.0);
					}
					if(file.getName().contains("4PTrim")){
						network.setMax(510.0);
						network.setMin(108.0);
					}
					if(file.getName().contains("5PTrim")){
						network.setMax(502.0);
						network.setMin(126.0);
					}
					
					
					System.out.println(file.getName());
					network.setNewFile("runFilesFolder/" + file.getName());
					network.setMethods(false, false, false);
					network.runOneYear("TEN_NEWQuarterTrain_" + file.getName().replace(".csv", "").replace("runFilesFolder/", "newPredictions/").replace("00", ""), start, 8756, 200, 0, 24, 1);
				}
			
		}
		System.exit(0);
		start = 6567;
		String file ="";
		network.setPriceStackSize(1);
		for(int i = 2; i <= 2; i++){
			String dataSet = "";
			if(i == 1) {
				start = 4378;
				dataSet = "halfYear";
			}
			if(i == 2) {
				start = 0;
				dataSet = "fullYear";
			}
			for(int j = 2; j<=4; j++){
				int offset = 0;
				if(j ==1){
					network.setNewFile("YEAR_2012_DA_EXCEL_FOR_DA_PRICE_FORECAST_29-04-2013_ZeroToOne_withPaperPrices_1PTrim_Weekday.csv");
					file = "Paper_1PTrim_weekday";
					network.setMax(632);
					network.setMin(61);
				}
				else if(j == 2){
					network.setNewFile("YEAR_2012_DA_EXCEL_FOR_DA_PRICE_FORECAST_29-04-2013_ZeroToOne_withPaperPrices.csv");
					file = "Paper";
				}
				else if(j == 3){
					network.setNewFile("YEAR_2012_DA_EXCEL_FOR_DA_PRICE_FORECAST_29-04-2013_ZeroToOne.csv");
					file = "Standard";
					offset = 672;
				}
				else{
					network.setNewFile("YEAR_2012_DA_EXCEL_FOR_DA_PRICE_FORECAST_29-04-2013_ZeroToOne_withPaperPrices_5PTrim_Weekday.csv");
					file = "Paper_5PTrim_weekday";
					network.setMax(502);
					network.setMin(126);
				}
				int epochs = 200;
	//			network.checkNetwork(start, 8756);
				network.setMethods(false, false, false);
				network.runOneYear("NEW"+dataSet+"Train_" + file, start, 8756, epochs, offset, 24, 1);
				network.setMethods(true, false, false);
				network.runOneYear("NEW"+dataSet+"Train_1Historical_" + file, start, 8756, epochs, offset, 24, 1);
				network.setMethods(true, true, false);
				network.runOneYear("NEW"+dataSet+"Train_1Historical_Curve_" + file, start, 8756, epochs, offset, 24, 1);
				network.setMethods(true, false, true);
				network.runOneYear("NEW"+dataSet+"Train_1Historical_Skew_" + file, start, 8756, epochs, offset, 24, 1);
				network.setMethods(false, true, false);
				network.runOneYear("NEW"+dataSet+"Train_Curve_" + file, start, 8756, epochs, offset, 24, 1);
				network.setMethods(false, false, true);
				network.runOneYear("NEW"+dataSet+"Train_Skew_" + file, start, 8756, epochs, offset, 24, 1);
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		
		
//		network.setNewFile("YEAR_2012_DA_EXCEL_FOR_DA_PRICE_FORECAST_29-04-2013_ZeroToOne_withPaperPrices_5pTrim.csv");
		
		//network.checkNetwork(start, 8756);
//		network.setMax(501);
//		network.setMin(127);
//		network.skewnessStackSize = 24;
//		network.setMethods(false, false, true);
//		network.runOneYear("Normalized24skew_5pTrim_QuarterYearTrain_PaperSetup_Skew", start, 8756, 3000, 0);
		
//		network.priceStackSize = 24;
//		network.setMethods(true, false, false);
//		network.runOneYear("24Historical_5pTrim_QuarterYearTrain_PaperSetup_Skew", start, 8756, 3000, 0);
		
//		network.priceStackSize = 96;
//		network.setMethods(true, false, false);
//		network.runOneYear("96Historical_5pTrim_QuarterYearTrain_PaperSetup", start, 8756, 3000, 0);
//		
//		network.priceStackSize = 12;
//		network.setMethods(true, false, false);
//		network.runOneYear("12Historical_5pTrim_QuarterYearTrain_PaperSetup_Skew", start, 8756, 3000, 0);
		
		
//		network.setNewFile("YEAR_2012_DA_EXCEL_FOR_DA_PRICE_FORECAST_29-04-2013_ZeroToOne_withPaperPrices_NOTrim.csv");
//		network.checkNetwork(start, 8756);
//		network.setMax(1561);
//		network.setMin(1);
//		network.priceStackSize = 96;
//		network.setMethods(true, false, false);
//		network.runOneYear("96Historical_5pTrim_QuarterYearTrain_PaperSetup", start, 8756, 3000, 0);
//		
//		network.priceStackSize = 24;
//		network.setMethods(true, false, false);
//		network.runOneYear("96Historical_5pTrim_QuarterYearTrain_PaperSetup", start, 8756, 3000, 0);
//		
//		
//		network.priceStackSize = 12;
//		network.setMethods(true, false, false);
//		network.runOneYear("96Historical_5pTrim_QuarterYearTrain_PaperSetup", start, 8756, 3000, 0);
//		
//		
		
//		network.skewnessStackSize = 6;
//		network.setMethods(false, false, true);
//		network.runOneYear("12_hours_Normalized6skew_2pTrim_1YearTrain_PaperSetup_Skew", start, 8756, 3000, 0);
//		network.setMethods(true, true, false);
//		network.runOneYear("2pTrim_QuarterYearTrain_PaperSetup_Historical_Curve", start, 8756, 3000, 0);
//		network.useNewMethod = true;
//		network.useAverage = true;
//		network.setMethods(false, false, true);
//		network.runOneYear("newMethod_24daysAverage_12skew_2pTrim_QuarterYearTrain_PaperSetup_Historical_Skew", start, 8756, 3000, 0);
		
//		theNetwork.useLastXdays = true;
//		start = 0;
//		//historical -- curve -- skew
//		theNetwork.setNewFile("YEAR_2012_DA_EXCEL_FOR_DA_PRICE_FORECAST_29-04-2013_ZeroToOne_withLastTenPrices.csv");		
//		theNetwork.checkNetwork(0, 8756);
//		theNetwork.setMethods(true, false, false);
//		theNetwork.runOneYear("2pTrim_1YearTrain_LastTenDays_Historical", start, 8756, 3000, 0);
//		theNetwork.setMethods(true, true, false);
//		theNetwork.runOneYear("2pTrim_1YearTrain_LastTenDays_Historical_Curve", start, 8756, 3000, 0);
//		theNetwork.setMethods(true, false, true);
//		theNetwork.runOneYear("2pTrim_1YearTrain_LastTenDays_Historical_Skew", start, 8756, 3000, 0);
//		theNetwork.setMethods(false, true, false);
//		theNetwork.runOneYear("2pTrim_1YearTrain_LastTenDays_Curve", start, 8756, 3000, 0);
//		theNetwork.setMethods(false, false, true);
//		theNetwork.runOneYear("2pTrim_1YearTrain_LastTenDays_Skew", start, 8756, 3000, 0);
//		
//		start = 4378;
//		//historical -- curve -- skew	
//		theNetwork.useLastXdays = true;
//		theNetwork.checkNetwork(start, 8756);
//		theNetwork.setMethods(false, false, false);
//		theNetwork.runOneYear("2pTrim_HalfYearTrain_LastTenDays_Standard", start, 8756, 3000, 0);
//		theNetwork.setMethods(true, false, false);
//		theNetwork.runOneYear("2pTrim_HalfYearTrain_LastTenDays_Historical", start, 8756, 3000, 0);
//		theNetwork.setMethods(true, true, false);
//		theNetwork.runOneYear("2pTrim_HalfYearTrain_LastTenDays_Historical_Curve", start, 8756, 3000, 0);
//		theNetwork.setMethods(true, false, true);
//		theNetwork.runOneYear("2pTrim_HalfYearTrain_LastTenDays_Historical_Skew", start, 8756, 3000, 0);
//		theNetwork.setMethods(false, true, false);
//		theNetwork.runOneYear("2pTrim_HalfYearTrain_LastTenDays_Curve", start, 8756, 3000, 0);
//		theNetwork.setMethods(false, false, true);
//		theNetwork.runOneYear("2pTrim_HalfYearTrain_LastTenDays_Skew", start, 8756, 3000, 0);
//		
//		start = 6567;
//		theNetwork.checkNetwork(start, 8756);
//		theNetwork.setMethods(true, false, false);
//		theNetwork.runOneYear("2pTrim_QuarterYearTrain_LastTenDays_Historical", start, 8756, 3000, 0);
//		theNetwork.setMethods(true, true, false);
//		theNetwork.runOneYear("2pTrim_QuarterYearTrain_LastTenDays_Historical_Curve", start, 8756, 3000, 0);
//		theNetwork.setMethods(true, false, true);
//		theNetwork.runOneYear("2pTrim_QuarterYearTrain_LastTenDays_Historical_Skew", start, 8756, 3000, 0);
//		theNetwork.setMethods(false, true, false);
//		theNetwork.runOneYear("2pTrim_QuarterYearTrain_LastTenDays_Curve", start, 8756, 3000, 0);
//		theNetwork.setMethods(false, false, true);
//		theNetwork.runOneYear("2pTrim_QuarterYearTrain_LastTenDays_Skew", start, 8756, 3000, 0);
//		
		
//		value = 0;
//		theNetwork.setNewFile("YEAR_2012_DA_EXCEL_FOR_DA_PRICE_FORECAST_29-04-2013_ZeroToOne_withExtraPrices.csv");
//		theNetwork.reconfigCsvWriter("PricesSet", headerArray);
//		for(int i = 0; i<350; i++){
//			HashMap<Integer,Integer> ranges = new HashMap<Integer,Integer>();
//			int index = i * 24;
//			ranges.put(0+value+index, 7831+value+index);
//			theNetwork.setValues(7831+value+index, 24, 5000, true, true, mute, 25, true, 10, ranges);
//			theNetwork.run();
//		}
//		theNetwork.calculateAverageOfValuesSeen();
//		theNetwork.reset();
		//1 �r. Skift en dag frem.
//		theNetwork.reconfigCsvWriter("Days100", headerArray);
//		for(int i = 0; i<10; i++){
//			HashMap<Integer, Integer> ranges = new HashMap<Integer, Integer>();
//			int index = i * 24;
//			ranges.put(0+index, 7831+index);
//			theNetwork.setValues(7831+index, 24, 5000, true, true, mute, 20, true, 5, ranges);
//			theNetwork.run();
//		}
//		theNetwork.calculateAverageOfValuesSeen();
//		theNetwork.reset();
		//1 �r. Skift en dag frem.
//		for(int j = 2; j<=8; j += 2){
//			theNetwork.reconfigCsvWriter("20neurons" + j, headerArray);
//			//		for(int i = 0; i<329; i++){
//			for(int i = 0; i<329; i++){
//				HashMap<Integer, Integer> ranges = new HashMap<Integer, Integer>();
//				int index = i * 24;
//				ranges.put(0+index, 7831+index);
//				theNetwork.setValues(7831+index, 24, 4000, true, true, mute, 20, true, j, ranges);
//				theNetwork.run();
//			}
//			theNetwork.calculateAverageOfValuesSeen();
//		}
		Encog.getInstance().shutdown();
	}

	public static double getMax() {
		return max;
	}

	public void setMax(double max) {
		TheNetwork1.max = max;
	}

	public static double getMin() {
		return min;
	}

	public void setMin(double min) {
		TheNetwork1.min = min;
	}

	public static int getPriceStackSize() {
		return priceStackSize;
	}

	public void setPriceStackSize(int priceStackSize) {
		TheNetwork1.priceStackSize = priceStackSize;
	}
}
