package ru.ispras.modis.NetBlox.scenario;

import java.awt.Color;


public class GraphVisualisationDescription {
	public static enum Method	{
		MATRIX, CLUSTERS_GRAPH, FORCE_DIRECTED
	}
	public static enum FinalPresentationType	{
		NO, ONE_CANVAS, MULTIPLE_CANVAS
	}

	private boolean visualiseGraph;
	private FinalPresentationType visualiseSubstructures;

	private Method visualisationMethod = Method.FORCE_DIRECTED;

	private int minimalNumberOfNodesInOverlapToVisualiseIt = 25;
	private float nodesSizeCorrectionCoefficient = 0.2f;
	private Float repulsionCoefficient = null;
	private Float gravityCoefficient = null;
	private Integer normalisedEdgeWeightInfluence = null;

	private Color backgroundColour = Color.BLACK;

	private String exportFilename;


	public GraphVisualisationDescription(boolean visualiseGraph, FinalPresentationType visualiseSubstructures)	{
		this.visualiseGraph = visualiseGraph;
		this.visualiseSubstructures = visualiseSubstructures;
	}


	public void setVisualisationMethod(Method method)	{
		this.visualisationMethod = method;
	}

	public void setExportFilename(String exportFilename)	{
		this.exportFilename = exportFilename;
	}

	public void setMinimalNumberOfNodesInOverlapToVisualiseIt(int n)	{
		minimalNumberOfNodesInOverlapToVisualiseIt = n;
	}

	public void setNodesSizeCorrectionCoefficient(float coefficient)	{
		nodesSizeCorrectionCoefficient = coefficient;
	}

	public void setRepulsionCoefficient(float coefficient)	{
		repulsionCoefficient = coefficient;
	}
	public void setGravityCoefficient(float coefficient)	{
		gravityCoefficient = coefficient;
	}
	public void setNormalisedEdgeWeightInfluence(int coefficient)	{
		normalisedEdgeWeightInfluence = coefficient;
	}


	public boolean visualiseGraph()	{
		return visualiseGraph;
	}

	public FinalPresentationType getSubstructuresFinalPresentationType()	{
		return visualiseSubstructures;
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

	public Float getRepulsionCoefficient()	{
		return repulsionCoefficient;
	}
	public Float getGravityCoefficient()	{
		return gravityCoefficient;
	}
	public Integer getNormalisedEdgeWeightInfluence()	{
		return normalisedEdgeWeightInfluence;
	}

	public void setBackgroundColour(Color color)	{
		backgroundColour = color;
	}
	public Color getBackgroundColour()	{
		return backgroundColour;
	}
}
