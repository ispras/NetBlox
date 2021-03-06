package ru.ispras.modis.NetBlox.dataManagement;

import java.io.File;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;

import ru.ispras.modis.NetBlox.configuration.SystemConfiguration;
import ru.ispras.modis.NetBlox.dataStructures.Graph;
import ru.ispras.modis.NetBlox.dataStructures.ISetOfGroupsOfNodes;
import ru.ispras.modis.NetBlox.dataStructures.SetOfGroupsOfNodes;
import ru.ispras.modis.NetBlox.exceptions.SetOfGroupsException;
import ru.ispras.modis.NetBlox.exceptions.SourceGraphException;
import ru.ispras.modis.NetBlox.scenario.GraphParametersSet;
import ru.ispras.modis.NetBlox.scenario.UploadedGraphDataParametersSet;
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
	private String absoluteNodesAttributesPath;

	private Graph graph = null;


	public GraphOnDriveHandler(GraphParametersSet parameters)	{
		this.parameters = parameters;

		String relativePathOnDisk = generateRelativeGraphDirectoryPath(parameters);

		absoluteGraphDirectoryPath = Paths.get(relativePathOnDisk).isAbsolute() ? relativePathOnDisk :
				GRAPH_FILES_ROOT + SystemConfiguration.FILES_SEPARATOR + relativePathOnDisk;
		absoluteGraphPath = absoluteGraphDirectoryPath + SystemConfiguration.FILES_SEPARATOR + parameters.getGraphFileName();
		absoluteReferenceCoverPath = absoluteGraphDirectoryPath + SystemConfiguration.FILES_SEPARATOR + parameters.getReferenceCoverFilename();

		String nodesAttributesFilename = parameters.getNodesAttributesFilename();
		absoluteNodesAttributesPath = Paths.get(nodesAttributesFilename).isAbsolute() ? nodesAttributesFilename :
			absoluteGraphDirectoryPath + SystemConfiguration.FILES_SEPARATOR + nodesAttributesFilename;
	}

	private String generateRelativeGraphDirectoryPath(GraphParametersSet parameters)	{
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

	/**
	 * Both for generated graphs and uploaded graph data (graphs).
	 * @param parameters
	 * @return	the path to the directory with graph data (strictly relative if for generated graph; either relative or absolute for uploaded).
	 */
	public StringBuilder generateGraphTypeRelativeDirectory(GraphParametersSet parameters)	{
		StringBuilder pathBuilder;
		if (parameters instanceof UploadedGraphDataParametersSet)	{
			UploadedGraphDataParametersSet uploadedDataParameters = (UploadedGraphDataParametersSet) parameters;
			pathBuilder = new StringBuilder(uploadedDataParameters.getDirectoryPathname());

			if (!uploadedDataParameters.isPathToRequiredGraphExternallyFixed())	{
				pathBuilder.append(SystemConfiguration.FILES_SEPARATOR);
				if (parameters.isDirected() && parameters.isWeighted())	{
					pathBuilder.append("weighted_directed");
				}
				else if (parameters.isDirected())	{
					pathBuilder.append("directed");
				}
				else if (parameters.isWeighted())	{
					pathBuilder.append("weighted");
				}
				else	{
					pathBuilder.append("basic");
				}
			}
		}
		else	{
			pathBuilder = new StringBuilder(parameters.getGraphTypeName());
			if (parameters.isWeighted())	{
				pathBuilder.append("_weighted");
			}
			if (parameters.isDirected())	{
				pathBuilder.append("_directed");
			}
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

	public String getAbsoluteNodeAttributesPathString()	{
		return absoluteNodesAttributesPath;
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

		if ((new File(absoluteNodesAttributesPath)).exists())	{
			graph = new Graph(absoluteGraphPath, absoluteNodesAttributesPath, parameters.isDirected(), parameters.isWeighted());
		}
		else	{
			graph = new Graph(absoluteGraphPath, parameters.isDirected(), parameters.isWeighted());
		}
		return graph;
	}

	public ISetOfGroupsOfNodes getReference() throws SourceGraphException	{
		try	{
			SetOfGroupsOfNodes setOfGroups = new SetOfGroupsOfNodes(getAbsoluteReferenceCoverPathString(), getGraph());
			return setOfGroups;
		}
		catch (SetOfGroupsException e)	{
			throw new SourceGraphException(e);
		}
	}


	public boolean doesGraphExistOnDisk()	{
		File file = new File(absoluteGraphPath);
		return file.exists();
	}

	public boolean doesReferenceSetOfGroupsOfNodesExist()	{
		File file = new File(absoluteReferenceCoverPath);
		return file.exists();
	}



	public static class PositionInRowComparator implements Comparator<GraphOnDriveHandler>	{
		@Override
		public int compare(GraphOnDriveHandler arg0, GraphOnDriveHandler arg1) {
			return arg0.parameters.getNumberAmongOtherGraphParametersSets() - arg1.parameters.getNumberAmongOtherGraphParametersSets();
		}
	}
}
