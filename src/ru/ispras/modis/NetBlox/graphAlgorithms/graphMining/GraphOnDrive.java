package ru.ispras.modis.NetBlox.graphAlgorithms.graphMining;

import ru.ispras.modis.NetBlox.scenario.GraphParametersSet;

/**
 * A container for transferring the graphs that reside on a drive to plug-ins.
 * 
 * @author ilya
 */
public class GraphOnDrive {
	private String absoluteGraphPath;
	private boolean directed;
	private boolean weighted;

	public GraphOnDrive(String absoluteGraphPath, GraphParametersSet graphParameters)	{
		this.absoluteGraphPath = absoluteGraphPath;
		directed = graphParameters.isDirected();
		weighted = graphParameters.isWeighted();
	}


	public String getGraphFilePathString()	{
		return absoluteGraphPath;
	}

	public boolean isDirected()	{
		return directed;
	}
	public boolean isWeighted()	{
		return weighted;
	}
}
