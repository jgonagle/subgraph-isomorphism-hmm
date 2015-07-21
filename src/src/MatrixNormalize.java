
public class MatrixNormalize 
{
	public static double maxConvergenceError = .0001;	
	
	//if normalizeRows is true, rows are normalizer, otherwise columns are normalized
	public static double[][] normalizeMatrix(double[][] someMatrix, boolean normalizeRows, double gain)
	{
		double normalizer;
		int independentDim, dependentDim;
		
		if (normalizeRows)
		{
			independentDim = someMatrix.length;
			dependentDim = someMatrix[0].length;
		}
		else
		{
			independentDim = someMatrix[0].length;
			dependentDim = someMatrix.length;
		}
		
		double[][] normalizedMatrix = new double[someMatrix.length][someMatrix[0].length];
		
		for (int i = 0; i < independentDim; i++)
		{
			normalizer = 0;
			
			for (int j = 0; j < dependentDim; j++)
			{
				if (normalizeRows)
				{
					normalizer += someMatrix[i][j];
				}
				else
				{
					normalizer += someMatrix[j][i];
				}
			}
			
			for (int j = 0; j < dependentDim; j++)
			{
				if (normalizeRows)
				{
					normalizedMatrix[i][j] = someMatrix[i][j] * (gain / normalizer);
				}
				else
				{
					normalizedMatrix[j][i] = someMatrix[j][i] * (gain / normalizer);
				}
			}
		}
		
		return normalizedMatrix;
	}
	
	//assume gain equals 1
	public static double[][] normalizeMatrix(double[][] someMatrix, boolean normalizeRows)
	{
		return normalizeMatrix(someMatrix, normalizeRows, 1.0);
	}
	
	//ensure that the sum of each row and column equals 1
	public static double[][] makeMatrixBistochastic(double[][] someMatrix) 
	{
		boolean steady = false;
		boolean normalizeRows = true;
		
		double[][] normalizedMatrix;
		
		//keep alternately normalizing rows and matrix 
		//until all entries are "steady", i.e. change is
		//less than maxError
		while(!steady)
		{
			steady = true;
			
			normalizedMatrix = normalizeMatrix(someMatrix, normalizeRows);
			
			for (int i = 0; i < someMatrix.length; i++)
			{
				for (int j = 0; j < someMatrix[0].length; j++)
				{
					if (Math.abs(normalizedMatrix[i][j] -
								 someMatrix[i][j]) > maxConvergenceError)
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
			
			someMatrix = normalizedMatrix;
			normalizeRows = (!normalizeRows);
		}
		
		return someMatrix;
	}
}
