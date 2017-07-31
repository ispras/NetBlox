package ru.ispras.modis.NetBlox.graphAlgorithms.graphProvision;

import java.io.InputStream;
import java.util.List;

import ru.ispras.modis.NetBlox.dataStructures.IGraph;
import ru.ispras.modis.NetBlox.dataStructures.ISetOfGroupsOfNodes;
import ru.ispras.modis.NetBlox.utils.ResultsFromPlugins;

/**
 * A container for transferring the provided graphs from plug-ins to NetBlox.
 * 
 * @author ilya
 */
public abstract class ProvidedGraph extends ResultsFromPlugins {

	public ProvidedGraph(ResultsProvisionFormat format) {
		super(format);
	}


	public String getGraphFilePathString()	{
		tellAboutUnimplementedMethod(ResultsProvisionFormat.FILE_PATH_STRING);
		return null;
	}
	public String getCoverFilePathString()	{
		tellAboutUnimplementedMethod(ResultsProvisionFormat.FILE_PATH_STRING);
		return null;
	}

	public IGraph getGraph()	{
		tellAboutUnimplementedMethod(ResultsProvisionFormat.INTERNAL);
		return null;
	}
	public ISetOfGroupsOfNodes getCover()	{	//XXX Or rather ICover?
		tellAboutUnimplementedMethod(ResultsProvisionFormat.INTERNAL);
		return null;
	}

	public List<String> getGraphStrings()	{
		tellAboutUnimplementedMethod(ResultsProvisionFormat.LIST_OF_STRINGS);
		return null;
	}
	public List<String> getCoverStrings()	{
		tellAboutUnimplementedMethod(ResultsProvisionFormat.LIST_OF_STRINGS);
		return null;
	}

	public InputStream getGraphStream()	{
		tellAboutUnimplementedMethod(ResultsProvisionFormat.STREAM);
		return null;
	}
	public InputStream getCoverStream()	{
		tellAboutUnimplementedMethod(ResultsProvisionFormat.STREAM);
		return null;
	}
}
