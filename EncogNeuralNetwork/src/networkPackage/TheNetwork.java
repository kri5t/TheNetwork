package networkPackage;

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

public class TheNetwork {
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
	
	//Parameters set from here:
	private String inputFile = "YEAR_2012_DA_EXCEL_FOR_DA_PRICE_FORECAST_29-04-2013_ZeroToOne.csv";
	final static double max = 632.0;
	final static double min = 61.0;
	public int curveStackSize = 6;
	public int priceStackSize = 24;
	public int skewnessStackSize = 24;
	public boolean usePrediction = true;
	public boolean useLastKnown = true;
	private BasicNetwork theNetwork = null;
	public boolean trainOnce = false;
	public boolean useLastXdays = false; 
	public boolean useNewMethod = false;
	public boolean useCurveBehaviour = false;
	public boolean useHistoricalVolatility = true;
	public boolean useTurningpoints = false;
	public boolean useSkewness = false;
	
	public TheNetwork(int numberOfDataEntries, int numberOfTestEntries, int numberOfEpochs
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
		System.out.println("DataEntries: " + numberOfDataEntries + ", TestEntries: " + numberOfTestEntries + ", Epochs: " + numberOfEpochs
				+ ", UseTestData: " + useTest + ", LimitEntries: " + limitNumberOfEntries + ", Firstlayer#: " + numberOfNeurons + 
				", SecondLayer: " + useSecondLayer + ", SecondLayer#: " + numberOfSecondLayerNeurons);
		if(!firstRunCompleted){
			if(theNetwork != null){
				numberOfNeurons = theNetwork.getLayerNeuronCount(1);
				numberOfSecondLayerNeurons = theNetwork.getLayerNeuronCount(2);
			}
			myCsvWriterOnSelf.writeLineToFile(new String[]{"DataEntries: " + numberOfDataEntries + ", TestEntries: " + numberOfTestEntries + ", Epochs: " + numberOfEpochs
					+ ", UseTestData: " + useTest + ", LimitEntries: " + limitNumberOfEntries + ", Firstlayer#: " + numberOfNeurons + 
					", SecondLayer: " + useSecondLayer + ", SecondLayer#: " + numberOfSecondLayerNeurons+ ", Predict: " + usePrediction +
					", Curvebehaviour: " + useCurveBehaviour + ", Stacksize: " + curveStackSize + ", useLastXDays: " + useLastXdays + ", Max: " + max +
					", Min: " + min + ", Curve behaviour: " + useCurveBehaviour + ", Historical: " + useHistoricalVolatility + ", Skewness: " + useSkewness});
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
		
		extraVariables = extras;
	}
	
	public void initPlaces(){
		List<String> list = new ArrayList<String>();
		if(useCurveBehaviour) list.add("curve");
		if(useHistoricalVolatility) list.add("hist");
		if(useTurningpoints) list.add("turning");
		if(useSkewness) list.add("skew");
		for(int i = 0; i < list.size(); i++){
			if(list.get(i) == "curve") curveBehaviourPlace = i+1;
			if(list.get(i) == "hist") historicalVolatilityPlace = i+1;
			if(list.get(i) == "turning") turningPointsPlace = i+1;
			if(list.get(i) == "skew") skewnessPlace = i+1;
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
		else train = new ResilientPropagation(network, trainingSet);
		
		
//		EarlyStoppingStrategy strategy = new EarlyStoppingStrategy(testingSet, trainingSet);
//		
//		train.addStrategy(strategy);
//		final MultiPropagation train = new MultiPropagation(network, trainingSet);
//		final Train train = new ManhattanPropagation(network, trainingSet,0.0001);
//		final LevenbergMarquardtTraining train = new LevenbergMarquardtTraining(network, trainingSet);
		train.setThreadCount(12);
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
			System.out.println("Neural Network Results:");
			double largestError = 0;
			ArrayList<Double> errors = new ArrayList<Double>();
			String toPrint = "";
			int o = 0;
			ErrorCalculation errorCalc = new ErrorCalculation();
			
			if(useTestSet && doneOnTrainingSet){ 
				trainingSet = testingSet;
				onTestSet = true;
			}
			System.out.println("Set size: " + trainingSet.size());
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
				if(onTestSet && usePrediction && useTurningpoints && j < trainingSet.size()-1){
					trainingSet.get(j+1).getInput().setData(trainingSet.getInputSize()-1-extraVariables+skewnessPlace, calculateSkewness());
				}
				if(onTestSet && (useTurningpoints||useHistoricalVolatility)){
					priceStack.remove(0);
					priceStack.push(output.getData(0));
				}
				if(onTestSet && useSkewness){
					skewnessStack.remove(0);
					skewnessStack.push(output.getData(0));
				}
				if(!onTestSet && trainingSet.size()-curveStackSize < j && j < trainingSet.size() && useCurveBehaviour){
					stack.push(trainingSet.get(j).getIdeal().getData(0));
				}
				if(!onTestSet && trainingSet.size()-priceStackSize < j && j < trainingSet.size() && (useHistoricalVolatility||useTurningpoints)){
					priceStack.push(trainingSet.get(j).getIdeal().getData(0));
				}
				if(!onTestSet && trainingSet.size()-skewnessStackSize < j && j < trainingSet.size() && (useHistoricalVolatility||useTurningpoints)){
					skewnessStack.push(trainingSet.get(j).getIdeal().getData(0));
				}
				
				if(0 < j && j < 24){
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
			System.out.println(toPrint);
			System.out.println("Average error margin: " + (totalErrors / (errors.size())));
			if(doneOnTrainingSet){
				sumOfValuesSeenInNetwork += (totalErrors / (errors.size()));
				timesSeenSumOfValuesInNetwork++;
			}
			System.out.println("MSE: " + errorCalc.calculate());
			System.out.println("ESS: " + errorCalc.calculateESS());
			System.out.println("RMS: " + errorCalc.calculateRMS());
			System.out.println(" ");
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
		System.out.println("Use prediction: " + usePrediction);
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
		return (data * (max - min) / 2) + ((max + min) / 2);
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
	    	System.out.println(inputArray.length);
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
		if(numberOfInputs == 0) numberOfInputs = nextLine.length-2+extraVariables;
		double[] input = new double[nextLine.length-1+extraVariables];
		for(int i = 0; i <= numberOfInputs; i++){
    		if(i == nextLine.length -2 && useLastKnown) input[i] = lastKnownInt;
			if(i < nextLine.length-1){
				input[i] = Double.parseDouble(nextLine[i]);	
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
			if(i == numberOfInputs){
				if(priceStack.size() == 24) priceStack.remove(0);
				priceStack.push(Double.parseDouble(nextLine[nextLine.length-1]));
				if(skewnessStack.size() == skewnessStackSize) skewnessStack.remove(0);
				skewnessStack.push(Double.parseDouble(nextLine[nextLine.length-1]));
			}
    	}
		return input;
	}
	
	public void calculateAverageOfValuesSeen(){
		System.out.println("AverageOfAveragesSeen: " + (sumOfValuesSeenInNetwork / timesSeenSumOfValuesInNetwork));
		
		reset();
	}
	
	public void reset(){
		sumOfValuesSeenInNetwork = 0;
		timesSeenSumOfValuesInNetwork = 0;
		firstRunCompleted = false;
		priceStack = new Stack<Double>();
		decayStack = new Stack<Double>();
		skewnessStack = new Stack<Double>();
	}
	
	public static double denormalizePrice(double value) {	        
        NormalizedField norm = new NormalizedField(NormalizationAction.Normalize,
                null,max,min,1,-1);
        return norm.deNormalize(value);
    }
	
	public void reconfigCsvWriter(String outputFileWithoutExtension, String[] headerArray){
		myCsvWriter.closeIt();
		myCsvWriter.setNewOutputFile(outputFileWithoutExtension + 
										"_PREDICT"+System.currentTimeMillis()+".csv", headerArray);
		myCsvWriterOnSelf.closeIt();
		myCsvWriterOnSelf.setNewOutputFile(outputFileWithoutExtension + 
											"_SELF"+System.currentTimeMillis()+".csv", null);
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
		HashMap<Integer,Integer> ranges = new HashMap<Integer,Integer>();
		ranges.put(testSetStart, testSetStop);
		createDataSetSpecificMonths(ranges);
		MLDataSet trainingSet = new BasicMLDataSet(inputArray, outputArray);
		
		FeedForwardPattern pattern = new FeedForwardPattern();
        pattern.setInputNeurons(trainingSet.getInputSize());
        pattern.setOutputNeurons(trainingSet.getIdealSize());
        pattern.setActivationFunction(new ActivationTANH());

        PruneIncremental prune = new PruneIncremental(trainingSet,pattern, 2000, 4, 10,
                new ConsoleStatusReportable());

        prune.addHiddenLayer(5, 50);
        prune.addHiddenLayer(0, 50);

        prune.process();

        theNetwork = prune.getBestNetwork();
        theNetwork.getLayerTotalNeuronCount(0);
        System.out.println("Best network info Architecture: " + theNetwork.getFactoryArchitecture());
        System.out.println("Best network layers: " + theNetwork.getLayerCount());
        System.out.println("Best network info: " + theNetwork.getStructure().toString());
	}
	
	private void setLastXdays(int offset, MLDataSet trainingSet, int j, double predicted){
//		int[] values = {10, 9, 8, 7, 6, 5, 4, 3, 2, 2, 1, 1, 1};
		MLData getInput = trainingSet.get(j+1).getInput();
		MLData setInput = trainingSet.get(j+1).getInput();
		int indexToGet = 11 + offset;
		setInput.setData(indexToGet+1, getInput.getData(indexToGet));
		indexToGet = 10 + offset;
		setInput.setData(indexToGet+1, getInput.getData(indexToGet));
		indexToGet = 9 + offset;
		setInput.setData(indexToGet+1, getInput.getData(indexToGet));
		indexToGet = 8 + offset;
		setInput.setData(indexToGet+1, getInput.getData(indexToGet));
		indexToGet = 7 + offset;
		setInput.setData(indexToGet+1, getInput.getData(indexToGet));
		indexToGet = 6 + offset;
		setInput.setData(indexToGet+1, getInput.getData(indexToGet));
		indexToGet = 5 + offset;
		setInput.setData(indexToGet+1, getInput.getData(indexToGet));
		indexToGet = 2 + offset;
		setInput.setData(indexToGet+2, getInput.getData(indexToGet));
		setInput.setData(indexToGet+1, getInput.getData(indexToGet));
		indexToGet = 0 + offset;
		setInput.setData(indexToGet+2, predicted);
		setInput.setData(indexToGet+1, predicted);
		setInput.setData(indexToGet, predicted);
		System.out.println(setInput.toString());
	}

	public static double historicalVolatilityEWMA() {
        double ewma = 0.0;
        if(priceStack.size()>0) {
            double decay = 0.94;
            double ewmaFromOneDayAgo = 0;
            if(decayStack.size()>0) {
                ewmaFromOneDayAgo = decayStack.get(decayStack.size()-1);
            }
            double productionFromOneDayAgo = priceStack.get(priceStack.size()-1);
            ewma = (decay*ewmaFromOneDayAgo) + ((1-decay)*productionFromOneDayAgo);
            decayStack.push(ewma);
            if(decayStack.size()>24) {
                decayStack.remove(0);
            }
        }
        return ewma;
    }

	public static double calculateSkewness() {
		Skewness skewness = new Skewness();
		double skew = 0.0;
		if(skewnessStack.size()>5) {
			double tempSkew = skewness.evaluate(turnDoubleStackToDoubleArray(skewnessStack),0,skewnessStack.size()-1);
			if(!Double.isNaN(skew)) {
				skew = tempSkew;
			}
		}
		return skew;
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
	
    public void runOneYear(String fileName, int testSetStart, int testSetStop, int epochs, int offSet){
    	String[] headerArray = {"actual", "ideal"};
    	this.reconfigCsvWriter(fileName, headerArray);
		for(int i = 0; i<320; i++){
			HashMap<Integer,Integer> ranges = new HashMap<Integer,Integer>();
			int index = i * 24;
			ranges.put(testSetStart+index+offSet, testSetStop+index+offSet);
			this.setValues(testSetStop+index+offSet, 24, epochs, true, true, mute, 16, true, 23, ranges);
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
    
	public static void main(String[] args){
		
//		numberOfDataEntries, numberOfTestEntries, numberOfEpochs, useTest, limitNumberOfEntries, mute, numberOfNeurons, useSecondLayer, numberOfSecondLayerNeurons
		
//		Normalizer.normalizeFiles("YEAR_2012_DA_EXCEL_FOR_DA_PRICE_FORECAST_29-04-2013_NotNormalized.csv","YEAR_2012_DA_EXCEL_FOR_DA_PRICE_FORECAST_29-04-2013_ZeroToOneNORMALIZED.csv");
		boolean mute = true;
		TheNetwork theNetwork = new TheNetwork(7000, 24, 10000, true, true, mute, 20, false, 0);
		theNetwork.setUsePrediction(true);
		
		
		
		
//		theNetwork.setUseLastKnown(true);
//		for(int i = 7000; i < 7600; i += 100){
//			theNetwork.setValues(i, 24, 10000, true, true, mute, 20, false, 2, null);
//			theNetwork.run();
//		}
		//1. maj - 1. august for begge Œr.
//		theNetwork.reconfigCsvWriter("StandardSet", headerArray);
//		int value = 674;
//		for(int i = 0; i<350; i++){
//			HashMap<Integer,Integer> ranges = new HashMap<Integer,Integer>();
//			int index = i * 24;
//			ranges.put(0+value+index, 7831+value+index);
//			theNetwork.setValues(7831+value+index, 24, 5000, true, true, mute, 16, true, 23, ranges);
//			theNetwork.run();
//		}
//		theNetwork.calculateAverageOfValuesSeen();
//		theNetwork.reset();
		//theNetwork.setNewFile("YEAR_2012_DA_EXCEL_FOR_DA_PRICE_FORECAST_29-04-2013_ZeroToOne_withExtraPrices.csv");
		
		
		
		
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
		//1 Œr. Skift en dag frem.
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
		//1 Œr. Skift en dag frem.
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
}
