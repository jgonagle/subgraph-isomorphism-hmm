import net.sourceforge.jswarm_pso.FitnessFunction;

public class HmmFitnessFunction extends FitnessFunction 
{	
	private SiManager siManager;
	
	private int supergraphSize;
	private double averageObservation;
	private double[] subHmmSeqProbs;
	private double[] superHmmSeqProbs;
	
	public HmmFitnessFunction(SiManager siManager)
	{
		this.siManager = siManager;
		
		supergraphSize = siManager.getSupergraphSize();
		averageObservation = 1 / supergraphSize;
		
		subHmmSeqProbs = new double[SiManager.numSubObservationSequences];
		superHmmSeqProbs = new double[SiManager.numSubObservationSequences];
		
		//want to maximize heuristic to testing observations
		setMaximize(true);	   
	}
	
	public void updateSubHmmSeqProbs()
	{
		subHmmSeqProbs = siManager.getSubHmmSeqProbs();
	}
	
	//returns the distance from the known subgraph's sequence observation
	//probabilites to the current HMM for the supergraph's sequence observation
	//probabilities
	//superHmmParams is adjacency matrix followed by observation matrix
	public double evaluate(double[] superHmmParams)
	{	
		double[][][] superHmmParamMatrices = getParamMatrices(supergraphSize, superHmmParams);
		
		double[][] superTransitionMatrix = superHmmParamMatrices[0];
		double[][] superObservationMatrix = superHmmParamMatrices[1];
		
		superHmmSeqProbs = siManager.getSuperTestingSeqProbs(superTransitionMatrix,
															 superObservationMatrix);
		
		double[] likelihoodSkew;
		double likelihoodDistance;
		
		likelihoodSkew = getAverageLikelihoodSkews(siManager.getOptimizedSuperObservationProbs());
		likelihoodDistance = getLikelihoodDistance();
		
		double fitness = Math.pow(likelihoodSkew[0], (0 - likelihoodDistance));
		fitness = Math.exp(likelihoodSkew[0]) / likelihoodDistance;
		
		return fitness;
	}
	
	//calculate Euclidean distance between two hmm params
	private double getLikelihoodDistance()
	{
		if (subHmmSeqProbs.length == superHmmSeqProbs.length)
		{
			double sum = 0;
			
			for (int i = 0; i < subHmmSeqProbs.length; i++)
			{
				sum += Math.pow(((superHmmSeqProbs[i] - subHmmSeqProbs[i]) / subHmmSeqProbs[i]), 2);
			}
			
			return Math.sqrt(sum);
		}
		else
		{
			return Double.NEGATIVE_INFINITY;
		}
	}
	
	//calculate skew for observation distribution between two hmm params
	private double[] getAverageLikelihoodSkews(double[][] optimizedSuperObservationProbs)
	{
		double nodeSkewNumerator, nodeSkewDenominator,
				spreadSkewNumerator, spreadSkewDenominator;
		
		//horizontal skew
		double avgNodeSkew = 0;
		//vertical skew
		double avgSpreadSkew = 0;
		
		for (int i = 0; i <  supergraphSize; i++)
		{
			nodeSkewNumerator = 0;
			nodeSkewDenominator = 0;

			spreadSkewNumerator = 0;
			spreadSkewDenominator = 0;
			
			for (int j = 0; j < supergraphSize; j++)
			{
				nodeSkewNumerator += Math.pow((optimizedSuperObservationProbs[i][j] - 
												  averageObservation),
												 3);
				spreadSkewNumerator +=  Math.pow((optimizedSuperObservationProbs[j][i] - 
						  							averageObservation),
						  							3);
		
				nodeSkewDenominator += Math.pow((optimizedSuperObservationProbs[i][j] - 
						  						    averageObservation),
						  						   2);
				spreadSkewDenominator += Math.pow((optimizedSuperObservationProbs[j][i] - 
						   							  averageObservation),
						   						     2);
			}
			
			nodeSkewNumerator /= supergraphSize;
			spreadSkewNumerator /= supergraphSize;
			
			nodeSkewDenominator /= supergraphSize;
			spreadSkewDenominator /= supergraphSize;
			
			avgNodeSkew += (nodeSkewNumerator / Math.pow(nodeSkewDenominator, 1.5));
			avgSpreadSkew += (spreadSkewNumerator / Math.pow(spreadSkewDenominator, 1.5));
		}
		
		avgNodeSkew /= supergraphSize;
		avgSpreadSkew /= supergraphSize;
		
		return (new double[] {avgNodeSkew, avgSpreadSkew});
	}
	
	public static double[][][] getParamMatrices(int size, double[] params)
	{
		double[][] transitionMatrix = new double[size][size];
		double[][] observationMatrix = new double[size][size];
	
		for (int i = 0; i < size; i++)
		{
			for (int j = 0; j < size; j++)
			{
				transitionMatrix[i][j] = params[j + (i * size)];
				observationMatrix[i][j] = params[(size * size) +
				                                 j + (i * size)];
			}
		}
		
		double[][][] result = new double[2][size][size];
		
		result[0] = transitionMatrix;
		result[1] = observationMatrix;
		
		return result;
	}
}