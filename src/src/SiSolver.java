public class SiSolver 
{
	public static void main (String[] args)
	{
		int subgraphSize = 10;
		int supergraphSize = 20;
		double subEdgeDensity = .2;
		double superEdgeDensity = .8;
		boolean directed = false;
		boolean weighted = false;
		int numParticles = ((int) Math.pow(subgraphSize,  2));
		int numIterationsPerSwarm = 10;
		
		SiManager siManager = new SiManager(subgraphSize, subEdgeDensity,
											supergraphSize, superEdgeDensity,
											directed, weighted);
		siManager.initialize();
		siManager.printInitializationInfo();
		
		HmmSwarmOptimizer swarmOptimizer = new HmmSwarmOptimizer(numParticles, 
																 numIterationsPerSwarm, 
																 siManager);
		
		long startTime = System.currentTimeMillis();
		//optimization stage
		swarmOptimizer.swarm();
		long endTime = System.currentTimeMillis();;
		
		swarmOptimizer.printCurrentBestOfSwarm();
		System.out.println("\nOptimization Took "+ ((endTime - startTime) / 1000) + " s");
	}
}

