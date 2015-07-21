import java.util.ArrayList;
import java.util.Random;

public class RandomSubgraphGenerator 
{
	private int numNodes;
	private double edgeDensity;
	private int numEdges;
	
	private final boolean directed;
	private final boolean weighted;
	
	private int supergraphSize;
	private double[][] superAdjMatrix;
	
	//adjacency matrix of this matrix
	private double[][] adjMatrix;
	
	private int[] subToSuperMap;
	
	private Random generator;
	
	//creates subgraph using a random graph generator's s parameters
	//attempts to get subgraph edge density relative to supergraph's edge
	//density as close to input edge density as possible
	public RandomSubgraphGenerator(RandomGraphGenerator someGraph, 
								   int numNodes, double edgeDensity)
	{
		if ((numNodes > 0) && (numNodes <= someGraph.getNumNodes()) && 
			(edgeDensity >= 0) && (edgeDensity <= 1))
		{
			this.numNodes = numNodes;
			this.edgeDensity = edgeDensity;
		}
		else
		{
			this.numNodes = 0;
			this.edgeDensity = 0;
			
			supergraphSize = 0;
			superAdjMatrix = new double[1][1];
		}
		
		directed = someGraph.areEdgesDirected();
		weighted = someGraph.areEdgesWeighted();
		
		supergraphSize = someGraph.getNumNodes();
		superAdjMatrix = new double[supergraphSize][supergraphSize];
		
		double[][] curSupergraph = someGraph.getAdjMatrix();
		
		//create local copy of supergraph in case supergraph changed
		for (int i = 0; i < supergraphSize; i++)
		{
			for (int j = 0; j < supergraphSize; j++)
			{
				superAdjMatrix[i][j] = curSupergraph[i][j];
			}
		}
		
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
	
	//generates subgraph from supergraph by randomly selecting numNodes number
	//of nodes from the supergraph, and choosing whether to include the induced
	//subgraph's edges with probabilty edgeDensity (i.e. overall edge density is
	//the product of the subgraph's density and the supergraph's density, so that
	//the induced subgraph with all its edges would have edgeDensity 1.0
	public void generateNewRandomSubgraph()
	{
		adjMatrix = new double[numNodes][numNodes];
		numEdges = 0;
		
		findRandomSetOfSupergraphNodes();
		int[] superToSubMap = new int[supergraphSize];
		
		for (int i = 0; i < supergraphSize; i++)
		{
			superToSubMap[i] = -1;
		}		
		
		for (int i = 0; i < numNodes; i++)
		{
			superToSubMap[subToSuperMap[i]] = i;
		}
		
		if (directed)
		{
			for (int i = 0; i < supergraphSize; i++)
			{
				for (int j = 0; j < supergraphSize; j++)
				{
					if ((superAdjMatrix[i][j] != 0) &&
						((superToSubMap[i] >= 0) && (superToSubMap[j] >= 0)) &&
						(generator.nextDouble() <= edgeDensity))
					{
						adjMatrix[superToSubMap[i]][superToSubMap[j]] = superAdjMatrix[i][j];
						numEdges++;
					}
				}
			}
		}
		else
		{
			for (int i = 0; i < supergraphSize; i++)
			{
				for (int j = 0; j < i; j++)
				{
					if ((superAdjMatrix[i][j] != 0) &&
						((superToSubMap[i] >= 0) && (superToSubMap[j] >= 0)) &&
						(generator.nextDouble() <= edgeDensity))
					{
						adjMatrix[superToSubMap[i]][superToSubMap[j]] = superAdjMatrix[i][j];
						adjMatrix[superToSubMap[j]][superToSubMap[i]] = superAdjMatrix[j][i];
						
						numEdges++;
					}
				}
			}
		}
	}
	
	private void findRandomSetOfSupergraphNodes()
	{
		ArrayList<Integer> remainingNodes = new ArrayList<Integer>();
		ArrayList<Integer> selectedNodes = new ArrayList<Integer>();
		
		for (int superNode = 0; superNode < supergraphSize; superNode++)
		{
			remainingNodes.add(new Integer(superNode));
		}
		
		int numNodesSelected = 0;
		int nodesLeft = supergraphSize;
		
		subToSuperMap = new int[numNodes];
		
		while (numNodesSelected < numNodes)
		{
			selectedNodes.add(remainingNodes.remove(generator.nextInt(nodesLeft)));
			subToSuperMap[numNodesSelected] = selectedNodes.get(numNodesSelected);
			
			numNodesSelected++;
			nodesLeft--;
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
	
	public int[] getSubToSuperMap()
	{
		return subToSuperMap;
	}
	
	public int getNumEdges()
	{
		return numEdges;
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
