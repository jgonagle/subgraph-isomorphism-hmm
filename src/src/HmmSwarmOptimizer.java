public class HmmSwarmOptimizer 
{
    private static double particleIncrement = .5;
    private static double globalIncrement = .5;
    private static double otherParticleIncrement = .5;
	
    private SiManager siManager;
    private int supergraphSize;
    
	private HmmSwarmRepulsive swarm;
	private HmmFitnessFunction superHmmFitnessFunction;
	private int numIterationsPerSwarm;
	
	public HmmSwarmOptimizer(int numParticles, int numIterationsPerSwarm,
							  SiManager siManager)
	{
		this.numIterationsPerSwarm = numIterationsPerSwarm;
		this.siManager = siManager;
		
		double[][] superAdjMatrix = siManager.getSuperAdjMatrix();
		supergraphSize = siManager.getSupergraphSize();
		
		superHmmFitnessFunction = new HmmFitnessFunction(siManager);
		HmmParticle.setSuperGraphSize(supergraphSize);
		
		swarm = new HmmSwarmRepulsive(supergraphSize,
									  numParticles, 
									  new HmmParticle(), 
									  superHmmFitnessFunction);
		
		double[] maxPosition = new double[(2 * supergraphSize * supergraphSize)];		
		
		for (int i = 0; i < supergraphSize; i++)
		{
			for (int j = 0; j < supergraphSize; j++)
			{
				if (superAdjMatrix[i][j] > 0)
				{
					maxPosition[j + (i * supergraphSize)] = 1;
				}
				else
				{
					maxPosition[j + (i * supergraphSize)] = 0;
				}
				
				maxPosition[(supergraphSize * supergraphSize) + 
				            j + (i * supergraphSize)] = 1;
			}
		}
		
		swarm.setMaxPosition(maxPosition);
		swarm.setMinPosition(0);
		swarm.setMaxMinVelocity(Math.sqrt(1 / numIterationsPerSwarm));

		swarm.setParticleIncrement(particleIncrement);
		swarm.setGlobalIncrement(globalIncrement);
		swarm.setOtherParticleIncrement(otherParticleIncrement);
		swarm.setNeighborhoodIncrement(0);
	}
	
	public void swarm()
	{
		System.out.println("");
		
		for( int i = 0; i < numIterationsPerSwarm; i++ ) 
		{
			siManager.findNewSubObservationSequences();
			superHmmFitnessFunction.updateSubHmmSeqProbs();
			
			swarm.evolve();
			
			System.out.println("Calculating Swarm " + (i + 1) + " of " + 
							   numIterationsPerSwarm +  
							   " (" + swarm.getBestFitness() + ")");
		}
	}

	public void printCurrentBestOfSwarm()
	{
		double bestFitness = swarm.getBestFitness();
		double[] bestHmmParams = swarm.getBestPosition();
		double[][][] bestHmmParamMatrices = HmmFitnessFunction.getParamMatrices(supergraphSize, 
																				 bestHmmParams);
		
		siManager.trainSuperHmm(bestHmmParamMatrices[0], bestHmmParamMatrices[1]);
		
		double[][] bestHmmTransitionProbs = siManager.getOptimizedSuperTransitionProbs();
		double[][] bestHmmObservationProbs = siManager.getOptimizedSuperObservationProbs();
		
		System.out.println("\nBest Fitness Found: " + bestFitness);
		
		System.out.println("\nOptimized Supergraph Transition Matrix");
		SiManager.print2DArray(bestHmmTransitionProbs);
		
		System.out.println("\nOptimized Supergraph Observation Matrix");
		SiManager.print2DArray(bestHmmObservationProbs);
	}
}
