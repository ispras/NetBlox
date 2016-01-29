package ru.ispras.modis.NetBlox.dataManagement;

import java.io.File;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;

import ru.ispras.modis.NetBlox.configuration.SystemConfiguration;
import ru.ispras.modis.NetBlox.dataStructures.Graph;
import ru.ispras.modis.NetBlox.dataStructures.ISetOfGroupsOfNodes;
import ru.ispras.modis.NetBlox.dataStructures.SetOfGroupsOfNodes;
import ru.ispras.modis.NetBlox.exceptions.SourceGraphException;
import ru.ispras.modis.NetBlox.scenario.GraphParametersSet;
import ru.ispras.modis.NetBlox.scenario.UncategorisedGraphParametersSet;
import ru.ispras.modis.NetBlox.utils.Pair;

/**
 * The class that handles graphs located on the disk drive that are described by their <code>GraphParametersSet</code>.
 * 
 * @author ilya
 */
public class GraphOnDriveHandler {
	private static final String GRAPH_FILES_ROOT = SystemConfiguration.getInstance().getGraphFilesRoot();

	private GraphParametersSet parameters;
	private String absoluteGraphDirectoryPath;
	private String absoluteGraphPath;
	private String absoluteReferenceCoverPath;

	private Graph graph = null;


	public GraphOnDriveHandler(GraphParametersSet parameters)	{
		this.parameters = parameters;

		String relativePathOnDisk = generateRelativeGraphDirectoryPath(parameters);

		absoluteGraphDirectoryPath = Paths.get(relativePathOnDisk).isAbsolute() ? relativePathOnDisk :
				GRAPH_FILES_ROOT + SystemConfiguration.FILES_SEPARATOR + relativePathOnDisk;
		absoluteGraphPath = absoluteGraphDirectoryPath + SystemConfiguration.FILES_SEPARATOR + parameters.getGraphFileName();
		absoluteReferenceCoverPath = absoluteGraphDirectoryPath + SystemConfiguration.FILES_SEPARATOR + parameters.getReferenceCoverFilename();
	}

	private String generateRelativeGraphDirectoryPath(GraphParametersSet parameters)	{
		if (parameters instanceof UncategorisedGraphParametersSet)	{
			return ((UncategorisedGraphParametersSet) parameters).getDirectoryPathname();
		}

		StringBuilder pathBuilder = generateGraphTypeRelativeDirectory(parameters);

		List<List<Pair<String, String>>> groupedParametersValues = parameters.getSpecifiedParametersAsGroupsOfPairsOfUniqueKeysAndValues();
		Integer graphGenerationNumber = parameters.getGenerationNumber();
		if (groupedParametersValues == null  &&  graphGenerationNumber == null)	{
			return pathBuilder.toString();
		}

		for (List<Pair<String, String>> groupOfParameters : groupedParametersValues)	{
			boolean is1st = true;

			for (Pair<String, String> keyValuePair : groupOfParameters)	{
				if (is1st)	{
					is1st = false;
				}
				else	{
					pathBuilder.append("_");
				}
				pathBuilder.append(keyValuePair.getKey()).append(keyValuePair.getValue());
			}

			pathBuilder.append(SystemConfiguration.FILES_SEPARATOR);
		}

		if (graphGenerationNumber != null)	{
			pathBuilder.append("gen").append(graphGenerationNumber).append(SystemConfiguration.FILES_SEPARATOR);
		}

		return pathBuilder.toString();
	}

	private StringBuilder generateGraphTypeRelativeDirectory(GraphParametersSet parameters)	{
		StringBuilder pathBuilder = new StringBuilder(parameters.getGraphTypeName());

		if (parameters.isDirected() && parameters.isWeighted())	{
			pathBuilder.append("_weighted_directed");
		}
		else if (parameters.isDirected())	{
			pathBuilder.append("_directed");
		}
		else if (parameters.isWeighted())	{
			pathBuilder.append("_weighted");
		}

		pathBuilder.append(SystemConfiguration.FILES_SEPARATOR);
		return pathBuilder;
	}


	public GraphParametersSet getGraphParameters()	{
		return parameters;
	}

	public String getAbsoluteGraphDirectoryPathString()	{
		return absoluteGraphDirectoryPath;
	}

	public String getAbsoluteGraphPathString()	{
		return absoluteGraphPath;
	}

	public String getAbsoluteReferenceCoverPathString()	{
		return absoluteReferenceCoverPath;
	}


	public String getAbsolutePathPossiblyWithGraphDirectory(String possiblyRelativePath)	{
		File file = new File(possiblyRelativePath);
		if (file.isAbsolute())	{
			return possiblyRelativePath;
		}

		return  getAbsoluteGraphDirectoryPathString() + SystemConfiguration.FILES_SEPARATOR + possiblyRelativePath;
	}


	public Graph getGraph() throws SourceGraphException	{
		if (graph != null)	{
			return graph;
		}

		if (!doesGraphExistOnDisk())	{
			throw new SourceGraphException("The source graph with path '"+absoluteGraphPath+"' does not exist.");
		}

		graph = new Graph(absoluteGraphPath, parameters.isDirected(), parameters.isWeighted());
		return graph;
	}

	public ISetOfGroupsOfNodes getReference() throws SourceGraphException	{
		return new SetOfGroupsOfNodes(getAbsoluteReferenceCoverPathString(), getGraph());
	}


	public boolean doesGraphExistOnDisk()	{
		File file = new File(absoluteGraphPath);
		return file.exists();
	}



	public static class PositionInRowComparator implements Comparator<GraphOnDriveHandler>	{
		@Override
		public int compare(GraphOnDriveHandler arg0, GraphOnDriveHandler arg1) {
			return arg0.parameters.getNumberAmongOtherGraphParametersSets() - arg1.parameters.getNumberAmongOtherGraphParametersSets();
		}
	}
}
