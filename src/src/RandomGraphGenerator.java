import java.util.Random;

public class RandomGraphGenerator 
{
	private int numNodes;
	private double edgeDensity;
	private int numEdges;
	
	private final boolean directed;
	private final boolean weighted;
	
	private double[][] adjMatrix;
	
	private Random generator;
	
	//creates random graph with numNodes nodes, edge density of edgeDensity,
	//directed edges if directed is true, and with uniform random edge weights
	//between 0.0 and 1.0
	//no self edges (from node to itself)
	public RandomGraphGenerator(int numNodes, double edgeDensity, 
								boolean directed, boolean weighted)
	{
		if ((numNodes > 0) && (edgeDensity >= 0) && (edgeDensity <= 1))
		{
			this.numNodes = numNodes;
			this.edgeDensity = edgeDensity;
			this.directed = directed;
			this.weighted = weighted;
		}
		else
		{
			this.numNodes = 0;
			this.edgeDensity = 0;
			this.directed = false;
			this.weighted = false;
		}
		
		numEdges = 0;		
		generator = new Random();
	}
	
	public void setNumNodes(int numNodes)
	{
		if (numNodes > 0)
		{
			this.numNodes = numNodes;
		}
	}
	
	public void setEdgeDensity(double edgeDensity)
	{
		if ((edgeDensity >= 0) && (edgeDensity <= 1))
		{
			this.edgeDensity = edgeDensity;
		}
	}
	
	public void generateNewRandomGraph()
	{
		adjMatrix = new double[numNodes][numNodes];
		
		numEdges = 0;
		
		if (directed)
		{
			for (int firstNode = 0; firstNode < numNodes; firstNode++)
			{
				for (int secondNode = 0; secondNode < numNodes; secondNode++)
				{
					if (firstNode != secondNode)
					{
						if (generator.nextDouble() <= edgeDensity)
						{
							if (weighted)
							{
								adjMatrix[firstNode][secondNode] = generator.nextDouble();
							}
							else
							{
								adjMatrix[firstNode][secondNode] = 1;
							}
							
							numEdges++;
						}
					}
				}
			}
		}
		else
		{
			for (int firstNode = 0; firstNode < numNodes; firstNode++)
			{
				for (int secondNode = 0; secondNode < firstNode; secondNode++)
				{
					if (generator.nextDouble() <= edgeDensity)
					{
						if (weighted)
						{
							adjMatrix[firstNode][secondNode] = generator.nextDouble();
							adjMatrix[secondNode][firstNode] = adjMatrix[firstNode][secondNode];
						}
						else
						{

							adjMatrix[firstNode][secondNode] = 1;
							adjMatrix[secondNode][firstNode] = 1;
						}
						
						numEdges++;
					}
				}
			}
		}
	}
	
	public int getNumNodes()
	{
		return numNodes;
	}
	
	public double getEdgeDensity()
	{
		return edgeDensity;
	}
	
	public boolean areEdgesDirected()
	{
		return directed;
	}
	
	public boolean areEdgesWeighted()
	{
		return weighted;
	}

	public double[][] getAdjMatrix() 
	{
		return adjMatrix;
	}
	
	public double getActualEdgeDensity()
	{
		double completeNumEdges;
		
		if (directed)
		{
			completeNumEdges = numNodes * (numNodes - 1);
		}
		else
		{
			completeNumEdges = (numNodes * (numNodes - 1)) / 2;
		}
		
		return (numEdges / completeNumEdges);
	}
}
