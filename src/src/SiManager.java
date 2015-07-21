import java.util.ArrayList;

import Jama.Matrix;
import be.ac.ulg.montefiore.run.jahmm.*;
import be.ac.ulg.montefiore.run.jahmm.toolbox.*;

public class SiManager 
{
	public static int numSubObservationSequences = 50;
	private static double subSequenceLengthExp = 2;
	private static int numBWIterations = 7;
	private static int doublePrintPrecision = 3;
	private static SiBwScaledLearner trainer = new SiBwScaledLearner();
	
	static
	{
		trainer.setNbIterations(numBWIterations);
	}
	
	private final int subgraphSize;
	private final int supergraphSize;
	//private int trainingCount;
	
	private double[][] subAdjMatrix;
	private double[][] superAdjMatrix;
	
	public Hmm<ObservationInteger> subHmm;
	public Hmm<ObservationInteger> optimizedSuperHmm;
	
	//for subgraph HMM params
	private double[] subInitialProbs;										//distribution of subgraph starting states
	private double[][] subTransitionProbs;									//subgraph edge transition probabilities
	private ArrayList<Opdf<ObservationInteger>> subObservationProbs;		//subgraph state observation probabilities

	private ArrayList<ArrayList<ObservationInteger>> subTrainingObservationSequences;
	private ArrayList<ArrayList<ObservationInteger>> subTestingObservationSequences;
	private double[] subHmmSeqProbs;
	
	private RandomGraphGenerator superGenerator;
	private RandomSubgraphGenerator subGenerator;
	
	private boolean directed;
	private boolean weighted;
	
	//creates random supergraph and subgraph of supergraph
	//according to input parameters
	//both graphs are of the same type ((un)directed and (un)weighted)
	public SiManager(int numSubNodes, double subEdgeDensity, 
					  int numSuperNodes, double superEdgeDensity,
					  boolean directed, boolean weighted)
	{		
		this.directed = directed;
		this.weighted = weighted;
		
		if ((numSuperNodes > 0) && (numSubNodes > 0) && (numSubNodes <= numSuperNodes))
		{
			subEdgeDensity = Math.min(Math.max(subEdgeDensity, 0), 1.0);	
			superEdgeDensity = Math.min(Math.max(superEdgeDensity, 0), 1.0);		

			superGenerator = new RandomGraphGenerator(numSuperNodes, superEdgeDensity, directed, weighted);
			superGenerator.generateNewRandomGraph();
			
			subGenerator = new RandomSubgraphGenerator(superGenerator, numSubNodes, subEdgeDensity);
			subGenerator.generateNewRandomSubgraph();
			
			subAdjMatrix = subGenerator.getAdjMatrix();
			superAdjMatrix = superGenerator.getAdjMatrix();
			
			subgraphSize = numSubNodes;
			supergraphSize = numSuperNodes;
		}
		else
		{
			//dummy conditions
			subAdjMatrix = new double[1][1];
			superAdjMatrix = new double[1][1];
			subgraphSize = 1;
			supergraphSize = 1;
		}
		
		subHmmSeqProbs = new double[numSubObservationSequences];
	}
	
	//inputs need to be square matrices
	//assumed directed and weighted
	public SiManager(double[][] superAdjMatrix,  double[][] subAdjMatrix)
	{
		this.subAdjMatrix = subAdjMatrix;
		this.superAdjMatrix = superAdjMatrix;

		this.subgraphSize = subAdjMatrix.length;
		this.supergraphSize = superAdjMatrix.length;
		
		subHmmSeqProbs = new double[numSubObservationSequences];
	}
	
	public void initializeNewSubgraph()
	{
		subGenerator.generateNewRandomSubgraph();
		subAdjMatrix = subGenerator.getAdjMatrix();
		
		initialize();
	}
	
	public void initializeNewSupergraph()
	{
		superGenerator.generateNewRandomGraph();
		
		subGenerator = new RandomSubgraphGenerator(superGenerator, subgraphSize,
												   subGenerator.getEdgeDensity());
		subGenerator.generateNewRandomSubgraph();
		
		superAdjMatrix = superGenerator.getAdjMatrix();
		subAdjMatrix = subGenerator.getAdjMatrix();
		
		initialize();
	}
	
	public void initialize()
	{
		findSubParams();
		
		subHmm = new Hmm<ObservationInteger>(subInitialProbs, subTransitionProbs, subObservationProbs);
	}
	
	public void findNewSubObservationSequences()
	{
		//trainingCount = 0;
		
		subTrainingObservationSequences = new ArrayList<ArrayList<ObservationInteger>>();
		subTestingObservationSequences = new ArrayList<ArrayList<ObservationInteger>>();
		
		//creates random observation sequences from subgraph hidden markov model
		MarkovGenerator<ObservationInteger> subSequenceFactory = new MarkovGenerator<ObservationInteger>(subHmm);
		
		for (int i = 0; i < numSubObservationSequences; i++)
		{
			subTrainingObservationSequences.add((ArrayList<ObservationInteger>) subSequenceFactory.observationSequence((int) Math.pow(subgraphSize, subSequenceLengthExp)));
			subTestingObservationSequences.add((ArrayList<ObservationInteger>) subSequenceFactory.observationSequence((int) Math.pow(subgraphSize, subSequenceLengthExp)));

			subHmmSeqProbs[i] = subHmm.probability(subTestingObservationSequences.get(i));
		}
	}
	
	public void trainSuperHmm(double[][] superTransitionMatrix,
	 		  				   double[][]  superObservationMatrix)
	{
		//System.out.println("\tTraining Count: " + trainingCount++);
		
		double[] initialProbs = getSteadyStateProbs(superTransitionMatrix);
		
		ArrayList<OpdfInteger> observationProbs = new ArrayList<OpdfInteger>();
		
		for (int i = 0; i < supergraphSize; i++)
		{
			observationProbs.add(new OpdfInteger(superObservationMatrix[i]));
		}
		
		optimizedSuperHmm = new Hmm<ObservationInteger>(initialProbs, 
														superTransitionMatrix, 
														observationProbs);
		
		optimizedSuperHmm = trainer.learn(optimizedSuperHmm, subTrainingObservationSequences);
	}
	
	public double[] getSuperTestingSeqProbs(double[][] superTransitionMatrix,
									 		  double[][] superObservationMatrix)
	{
		trainSuperHmm(superTransitionMatrix, superObservationMatrix);
		
		double[] superHmmSeqProbs = new double[numSubObservationSequences];
		
		for (int i = 0; i < numSubObservationSequences; i++)
		{
			superHmmSeqProbs[i] = optimizedSuperHmm.probability(subTestingObservationSequences.get(i));
		}
		
		return superHmmSeqProbs;
	}
	
	private void findSubParams() 
	{	
		subObservationProbs = new ArrayList<Opdf<ObservationInteger>>();
		subTransitionProbs = new double[subgraphSize][subgraphSize];
		subInitialProbs = new double[subgraphSize];
		
		findSubObservationProbs();
		findSubTransitionProbs();
		findSubInitialProbs();
	}

	private void findSubObservationProbs()
	{
		double[] subStateObservationProbs;
		
		for (int i = 0; i < subgraphSize; i++)
		{
			subStateObservationProbs = new double[subgraphSize];
			subStateObservationProbs[i] = 1;
			
			subObservationProbs.add(new OpdfInteger(subStateObservationProbs));			
		}
	}
	
	private void findSubTransitionProbs()
	{
		int outEdges;
		
		for (int i = 0; i < subgraphSize; i++)
		{
			outEdges = 0;
			
			for (int j = 0; j < subgraphSize; j++)
			{
				if (subAdjMatrix[i][j] != 0)
				{
					outEdges++;
				}
			}
			
			for (int j = 0; j < subgraphSize; j++)
			{
				if (subAdjMatrix[i][j] != 0)
				{
					subTransitionProbs[i][j] = 1.0 / outEdges;
				}
			}
		}
	}
	
	private void findSubInitialProbs()
	{
		subInitialProbs = getSteadyStateProbs(subTransitionProbs);
	}
	
	public int getSupergraphSize()
	{
		return supergraphSize;
	}
	
	public double[][] getSuperAdjMatrix()
	{
		return superAdjMatrix;
	}
	
	public double[] getSubHmmSeqProbs()
	{
		return subHmmSeqProbs;
	}
	
	public double[][] getOptimizedSuperTransitionProbs()
	{
		double[][] optimizedSuperTransitionProbs = new double[supergraphSize][supergraphSize];
		
		for (int i = 0; i < supergraphSize; i++)
		{
			for (int j = 0; j < supergraphSize; j++)
			{
				optimizedSuperTransitionProbs[i][j] = optimizedSuperHmm.getAij(i, j);
			}
		}
		
		return optimizedSuperTransitionProbs;
	}
	
	public double[][] getOptimizedSuperObservationProbs()
	{
		double[][] optimizedSuperObservationProbs = new double[supergraphSize][supergraphSize];
		
		Opdf<ObservationInteger> nodeObservationProbs;
		
		for (int i = 0; i < supergraphSize; i++)
		{
			nodeObservationProbs = optimizedSuperHmm.getOpdf(i);
			
			for (int j = 0; j < supergraphSize; j++)	
			{
				optimizedSuperObservationProbs[i][j] = nodeObservationProbs.probability(new ObservationInteger(j));
			}
		}
		
		return optimizedSuperObservationProbs;
	}
	
	public void printInitializationInfo()
	{
		System.out.println("\nGraphs below are " + (directed ? "directed" : "undirected") +
						   			     " and " + (weighted ? "weighted" : "unweighted"));
		
		System.out.println("\nSupergraph" + "\nSize: " + supergraphSize + 
						   "\nEdge Density: " + superGenerator.getActualEdgeDensity());
		System.out.println("Supergraph Adjacency Matrix");
		if (weighted)
		{
			print2DArray(superGenerator.getAdjMatrix());
		}
		else
		{
			print2DArray(superGenerator.getAdjMatrix(), 1, " ");
		}
		
		System.out.println("\nSubgraph" + "\nSize: " + subgraphSize + 
				   		   "\nEdge Density: " + subGenerator.getActualEdgeDensity());
		System.out.println("Subgraph Adjacency Matrix");
		if (weighted)
		{
			print2DArray(subGenerator.getAdjMatrix());
		}
		else
		{
			print2DArray(subGenerator.getAdjMatrix(), 1, " ");
		}
		
		System.out.println("\nSub To Super Map");
		print1DArray(subGenerator.getSubToSuperMap());
	}
	
	public static double[] getSteadyStateProbs(double[][] adjacencyMatrix)
	{
		int graphSize;
		
		if ((graphSize = adjacencyMatrix.length) == adjacencyMatrix[0].length)
		{			
			Matrix steadyTransitionMatrix = Matrix.constructWithCopy(adjacencyMatrix);
			Matrix newSteadyTransitionMatrix = new Matrix(graphSize, graphSize);

			Matrix steadyStateProbs = new Matrix(1, graphSize);
			steadyStateProbs.set(0, 0, 1);							//dummy starting probabilities; can be anything
			
			boolean steady = false;
			
			while(!steady)
			{
				steady = true;
				
				//keep squaring matrix until all entries are "steady", i.e. change
				//less than maxError
				newSteadyTransitionMatrix = steadyTransitionMatrix.times(steadyTransitionMatrix);
				
				for (int i = 0; i < graphSize; i++)
				{
					for (int j = 0; j < graphSize; j++)
					{
						if (Math.abs(newSteadyTransitionMatrix.get(i, j) -
									 steadyTransitionMatrix.get(i, j)) > MatrixNormalize.maxConvergenceError)
						{
							steady = false;
							break;
						}
					}
					
					if (!steady)
					{
						break;
					}
				}
				
				steadyTransitionMatrix = newSteadyTransitionMatrix;
			}
			
			return steadyTransitionMatrix.getArray()[0];
		}
		else
		{
			return null;
		}
	}
	
	//all double print methods assume that array elements are between 0.0 and 1.0, inclusive
	public static void print2DArray(double[][] array, int elementSize, String padString)
	{
		System.out.print("{");
		
		for (int i = 0; i < array.length; i++)
		{			
			for (int j = 0; j < array[0].length; j++)
			{
				System.out.print(doublePad(array[i][j], elementSize, padString));
				
				if (j != (array[i].length - 1))
				{
					System.out.print(" ");
				}
				else if (i != (array.length - 1))
				{
					System.out.print("\n ");
				}
			}
		}
		
		System.out.println("}");
	}
	
	public static void print2DArray(double[][] array)
	{
		print2DArray(array, doublePrintPrecision, "0");
	}
	
	public static void print1DArray(double[] array, int elementSize, String padString)
	{
		System.out.print("{");
		
		for (int i = 0; i < array.length; i++)
		{
			System.out.print(doublePad(array[i], elementSize, padString));
			
			if (i != (array.length - 1))
			{
				System.out.print(" ");
			}
			else
			{
				System.out.println("}");
			}
		}
	}
	
	public static void print1DArray(double[] array)
	{
		print1DArray(array, doublePrintPrecision, "0");
	}
	
	public static void print1DArray(int[] array)
	{
		System.out.print("{");
		
		for (int i = 0; i < array.length; i++)
		{
			System.out.print(array[i]);
			
			if (i != (array.length - 1))
			{
				System.out.print(" ");
			}
		}
		
		System.out.println("}");
	}
	
	private static String doublePad(double someDouble, int length, String padString)
	{
		int multiplier = (int) Math.pow(10, length);
		double shortenedDouble = (((int) (someDouble * multiplier)) * 1.0) / multiplier;
		int padSize = length - (Double.toString(shortenedDouble)).length() + 2;
		
		String result = Double.toString(shortenedDouble);
		String pad = "";
		
		while (padSize-- > 0)
		{
			pad += padString;
		}
		
		if (result.contains("E"))
		{
			return (pad + result);
		}
		else
		{
			return (result + pad);
		}
	}
}
