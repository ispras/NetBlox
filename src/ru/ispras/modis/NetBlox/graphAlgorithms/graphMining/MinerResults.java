package ru.ispras.modis.NetBlox.graphAlgorithms.graphMining;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;

import ru.ispras.modis.NetBlox.dataStructures.IGraph;
import ru.ispras.modis.NetBlox.dataStructures.ISetOfGroupsOfNodes;
import ru.ispras.modis.NetBlox.dataStructures.NumericCharacteristic;
import ru.ispras.modis.NetBlox.exceptions.PluginException;
import ru.ispras.modis.NetBlox.scenario.GraphMiningParametersSet;
import ru.ispras.modis.NetBlox.utils.ResultsFromPlugins;

/**
 * A container (its abstract parent) for returning mined data from plug-ins to NetBlox.
 * 
 * @author ilya
 */
public abstract class MinerResults extends ResultsFromPlugins {
	public enum MinedResultType	{
		GRAPH_EDGES,		//edges of a single graph, representing its structure
		SETS_OF_GRAPH_EDGES,//sets of edges representing the structure of several graphs (or several paths in one graph)
		NODES_GROUPS,		//groups of nodes (like communities)
		CHARACTERISTIC,		//computed numeric characteristics
		MULTIRESULT			//MULTIRESULT - for multiple results of work of an algorithm launch, as for SLPA
	}

	private MinedResultType resultType;
	private GraphMiningParametersSet parametersOfAlgorithmThatHadMinedThisResult;
	protected Integer currentTimeSlice = null;	//The time slice for which the results are put into this container (if time slices are considered).

	private List<MinerResults> containerForMultipleResults = null;


	public MinerResults(ResultsProvisionFormat format, MinedResultType resultType, GraphMiningParametersSet miningParameters) {
		super(format);

		this.resultType = resultType;
		parametersOfAlgorithmThatHadMinedThisResult = miningParameters;
	}

	public MinerResults(GraphMiningParametersSet miningParameters, List<MinerResults> multipleResult) {
		this(null, MinedResultType.MULTIRESULT, miningParameters);
		this.containerForMultipleResults = multipleResult;
	}


	public MinedResultType getResultType()	{
		return resultType;
	}


	public String getMinedGraphStructureFilePathString()	{
		tellAboutUnimplementedMethod(ResultsProvisionFormat.FILE_PATH_STRING);
		return null;
	}
	public String getNodesGroupsFilePathString()	{
		tellAboutUnimplementedMethod(ResultsProvisionFormat.FILE_PATH_STRING);
		return null;
	}
	public Collection<String> getMultipleGraphStructuresFilePathStrings()	{
		tellAboutUnimplementedMethod(ResultsProvisionFormat.FILE_PATH_STRING);
		return null;
	}

	public IGraph getMinedGraphStructure()	{
		tellAboutUnimplementedMethod(ResultsProvisionFormat.INTERNAL);
		return null;
	}
	public ISetOfGroupsOfNodes getNodesGroups()	{
		tellAboutUnimplementedMethod(ResultsProvisionFormat.INTERNAL);
		return null;
	}
	public Collection<IGraph> getMultipleGraphStructures()	{
		tellAboutUnimplementedMethod(ResultsProvisionFormat.INTERNAL);
		return null;
	}

	public List<String> getMinedGraphStrings()	{
		tellAboutUnimplementedMethod(ResultsProvisionFormat.LIST_OF_STRINGS);
		return null;
	}
	public List<String> getNodesGroupsStrings()	{
		tellAboutUnimplementedMethod(ResultsProvisionFormat.LIST_OF_STRINGS);
		return null;
	}
	public Collection<List<String>> getStringsForMultipleGraphs()	{
		tellAboutUnimplementedMethod(ResultsProvisionFormat.LIST_OF_STRINGS);
		return null;
	}

	public InputStream getMinedGraphStream()	{
		tellAboutUnimplementedMethod(ResultsProvisionFormat.STREAM);
		return null;
	}
	public InputStream getNodesGroupsStream()	{
		tellAboutUnimplementedMethod(ResultsProvisionFormat.STREAM);
		return null;
	}
	public Collection<InputStream> getMultipleGraphsStreams()	{
		tellAboutUnimplementedMethod(ResultsProvisionFormat.STREAM);
		return null;
	}

	public NumericCharacteristic getCharacteristic()	{
		tellAboutUnimplementedMethod(ResultsProvisionFormat.INTERNAL);
		return null;
	}

	public List<MinerResults> getMultipleResults()	{
		if (resultType != MinedResultType.MULTIRESULT)	{
			throw new PluginException("Requiring multiple results when a single result of type "+resultType+" is provided.");
		}

		if (containerForMultipleResults == null)	{
			throw new PluginException("Multiple results of graph mining process haven't been implemented correctly in plug-in for "+
					parametersOfAlgorithmThatHadMinedThisResult.getAlgorithmName());
		}

		return containerForMultipleResults;
	}


	public GraphMiningParametersSet getParametersOfAlgorithm()	{
		return parametersOfAlgorithmThatHadMinedThisResult;
	}

	/**
	 * @return time slice for this result when they are considered. <code>null</code> if time slices do not matter (for this result).
	 */
	public Integer getTimeSlice()	{
		return currentTimeSlice;
	}
}
