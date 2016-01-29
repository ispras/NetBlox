package ru.ispras.modis.NetBlox.scenario;

public class GraphVisualisationDescription {
	public static enum Method	{
		MATRIX, CLUSTERS_GRAPH, FORCE_DIRECTED//, CIRCULAR
	}

	private boolean visualiseGraph;
	private boolean visualiseGroupsOfNodes;

	private Method visualisationMethod = Method.FORCE_DIRECTED;

	private int minimalNumberOfNodesInOverlapToVisualiseIt = 25;
	private float nodesSizeCorrectionCoefficient = 0.2f;

	private String exportFilename;


	public GraphVisualisationDescription(boolean visualiseGraph, boolean visualiseGroupsOfNodes)	{
		this.visualiseGraph = visualiseGraph;
		this.visualiseGroupsOfNodes = visualiseGroupsOfNodes;
		//TODO Check they aren't both false at once.
	}


	public void setVisualisationMethod(Method method)	{
		this.visualisationMethod = method;
	}

	public void setMinimalNumberOfNodesInOverlapToVisualiseIt(int n)	{
		minimalNumberOfNodesInOverlapToVisualiseIt = n;
	}

	public void setNodesSizeCorrectionCoefficient(float coefficient)	{
		nodesSizeCorrectionCoefficient = coefficient;
	}

	public void setExportFilename(String exportFilename)	{
		this.exportFilename = exportFilename;
	}


	public boolean visualiseGraph()	{
		return visualiseGraph;
	}

	public boolean visualiseGroupsOfNodes()	{
		return visualiseGroupsOfNodes;
	}

	public Method getMethod()	{
		return visualisationMethod;
	}

	public String getExportFilename()	{
		return exportFilename;
	}

	public int getMinimalNumberOfNodesInOverlapToVisualiseIt()	{
		return minimalNumberOfNodesInOverlapToVisualiseIt;
	}

	public float getNodesSizeCorrectionCoefficient()	{
		return nodesSizeCorrectionCoefficient;
	}
}
