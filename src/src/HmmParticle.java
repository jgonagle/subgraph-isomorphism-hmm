import net.sourceforge.jswarm_pso.Particle;

public class HmmParticle extends Particle 
{
	private static int supergraphSize;
	
	public HmmParticle()
	{
		//create particle with parameter for each entry
		//in adjacency matrix and observation matrix
		super((2 * supergraphSize * supergraphSize));
	}
	
	public void init(double maxPosition[], double minPosition[], 
					  double maxVelocity[], double minVelocity[]) 
	{
		super.init(maxPosition, minPosition, maxVelocity, minVelocity);

		double[][][] superHmmParamMatrices = HmmFitnessFunction.getParamMatrices(supergraphSize, getPosition());
		
		double[][] superTransitionMatrix = superHmmParamMatrices[0];
		double[][] superObservationMatrix = superHmmParamMatrices[1];
		
		superTransitionMatrix = MatrixNormalize.normalizeMatrix(superTransitionMatrix, true);
		superObservationMatrix = MatrixNormalize.makeMatrixBistochastic(superObservationMatrix);
		
		double[] constrainedInitPosition = new double[(2 * supergraphSize * supergraphSize)];
		
		for (int i = 0; i < supergraphSize; i++)
		{
			for (int j = 0; j < supergraphSize; j++)
			{
				constrainedInitPosition[j + (i * supergraphSize)] = superTransitionMatrix[i][j];
				constrainedInitPosition[(supergraphSize * supergraphSize) + 
				                        j + (i * supergraphSize)] = superObservationMatrix[i][j];
			}
		}
		
		setPosition(constrainedInitPosition);
	}
	
	public static void setSuperGraphSize(int numSuperNodes)
	{
		supergraphSize = numSuperNodes;
	}
}