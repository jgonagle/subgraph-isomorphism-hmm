import net.sourceforge.jswarm_pso.FitnessFunction;
import net.sourceforge.jswarm_pso.Particle;
import net.sourceforge.jswarm_pso.Swarm;

public class HmmSwarmRepulsive extends Swarm
{
	private static boolean printParticleInfo = false;
	
	private int supergraphSize;
	
	/** Other particle increment */
	double otherParticleIncrement;
	/** Random increment */
	double randomIncrement;
	
	public HmmSwarmRepulsive(int supergraphSize,
							  int numberOfParticles, 
							  Particle sampleParticle, 
							  FitnessFunction fitnessFunction)
	{
		super(numberOfParticles, sampleParticle, fitnessFunction);
		
		setParticleUpdate(new HmmRepulsiveUpdate(sampleParticle));
		
		this.supergraphSize = supergraphSize;
	}
	
	public void init()
	{
		super.init();
		
		if (printParticleInfo)
		{
			printParticlesInfo();
		}
	}

	public void setOtherParticleIncrement(double otherParticleIncrement) 
	{
		this.otherParticleIncrement = otherParticleIncrement;
	}

	public double getOtherParticleIncrement() 
	{
		return otherParticleIncrement;
	}
	
	public void printParticlesInfo()
	{
		for (int i = 0; i < getNumberOfParticles(); i++)
		{
			double fitness = getParticle(i).getFitness();
			double[] hmmParams = getParticle(i).getPosition();
			double[][][] hmmParamMatrices = HmmFitnessFunction.getParamMatrices(supergraphSize, 
																				 hmmParams);
			
			double[][] hmmTransitionProbs = hmmParamMatrices[0];
			double[][] hmmObservationProbs = hmmParamMatrices[1];
			
			System.out.println("\nFitness for Particle " + i + ": " + fitness);
			
			System.out.println("\nParticle " + i + "'s Supergraph Transition Matrix");
			SiManager.print2DArray(hmmTransitionProbs);
			
			System.out.println("\nParticle " + i + "'s Supergraph Observation Matrix");
			SiManager.print2DArray(hmmObservationProbs);
		}
	}
}
