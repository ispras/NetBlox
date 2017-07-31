package ru.ispras.modis.NetBlox.graphVisualisation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ru.ispras.modis.NetBlox.dataManagement.GraphOnDriveHandler;
import ru.ispras.modis.NetBlox.dataManagement.StorageScanner;
import ru.ispras.modis.NetBlox.dataStructures.IGraph;
import ru.ispras.modis.NetBlox.dataStructures.IGroupOfNodes;
import ru.ispras.modis.NetBlox.dataStructures.IPackOfGraphStructures;
import ru.ispras.modis.NetBlox.dataStructures.ISetOfGroupsOfNodes;
import ru.ispras.modis.NetBlox.dataStructures.PackOfGraphs;
import ru.ispras.modis.NetBlox.dataStructures.internalMechs.ExtendedMiningParameters;
import ru.ispras.modis.NetBlox.exceptions.SourceGraphException;
import ru.ispras.modis.NetBlox.exceptions.StorageException;
import ru.ispras.modis.NetBlox.exceptions.VisualisationException;
import ru.ispras.modis.NetBlox.utils.MiningJobBase;

public class MinedDataDealer {
	public static IPackOfGraphStructures<?> getMined(MiningJobBase.JobBase minedDataType, GraphOnDriveHandler initialGraphHandler,
			ExtendedMiningParameters miningParameters, Integer timeSlice) throws VisualisationException	{
		try {
			IPackOfGraphStructures<?> result = null;

			switch (minedDataType)	{
			case NODES_GROUPS_SET:
				ISetOfGroupsOfNodes setOfGroupsOfNodes = StorageScanner.getMinedGroupsOfNodes(initialGraphHandler, miningParameters, timeSlice);
				result = setOfGroupsOfNodes;
				break;
			case GRAPH:
				IGraph minedGraph = StorageScanner.getMinedGraphStructure(initialGraphHandler, miningParameters, timeSlice);
				if (minedGraph != null)	{
					PackOfGraphs packOfStuctures = new PackOfGraphs();
					packOfStuctures.add(minedGraph);
					result = packOfStuctures;
				}
				break;
			case MULTIPLE_GRAPHS:
				List<IGraph> subgraphs = StorageScanner.getMinedMultipleGraphStructures(IGraph.class, initialGraphHandler, miningParameters, timeSlice);
				if (subgraphs!=null && !subgraphs.isEmpty())	{
					result = new PackOfGraphs(subgraphs);
				}
				break;
			}

			return result;
		} catch (StorageException | SourceGraphException e) {
			throw new VisualisationException(e);
		}
	}

	public static List<?> getOrderedMined(MiningJobBase.JobBase minedDataType, GraphOnDriveHandler initialGraphHandler,
			ExtendedMiningParameters miningParameters) throws VisualisationException	{
		try {
			List<?> result = null;
	
			switch (minedDataType)	{
			case NODES_GROUPS_SET:
				ISetOfGroupsOfNodes setOfGroupsOfNodes = StorageScanner.getMinedGroupsOfNodes(initialGraphHandler, miningParameters);
				result = sortGroupsBySizeInDescendingOrder(setOfGroupsOfNodes);
				break;
			case GRAPH:
				IGraph minedGraph = StorageScanner.getMinedGraphStructure(initialGraphHandler, miningParameters);
				result = new ArrayList<IGraph>(1);
				((ArrayList<IGraph>)result).add(minedGraph);
				break;
			case MULTIPLE_GRAPHS:
				List<IGraph> graphs = StorageScanner.getMinedMultipleGraphStructures(IGraph.class, initialGraphHandler, miningParameters, null);
				if (graphs != null  &&  !graphs.isEmpty())	{
					result = graphs;
				}
				break;
			}
	
			return result;
		} catch (StorageException | SourceGraphException e) {
			throw new VisualisationException(e);
		}
	}


	public static List<IGroupOfNodes> sortGroupsBySizeInDescendingOrder(ISetOfGroupsOfNodes groupsOfNodes)	{
		if (groupsOfNodes == null)	{
			return null;
		}

		List<IGroupOfNodes> sortedGroups = new ArrayList<IGroupOfNodes>(groupsOfNodes.size());
		for (IGroupOfNodes group : groupsOfNodes)	{
			sortedGroups.add(group);
		}
		Collections.sort(sortedGroups, new DescendingCommunitiesSizesComparator());

		return sortedGroups;
	}
	private static class DescendingCommunitiesSizesComparator implements Comparator<IGroupOfNodes>	{
		@Override
		public int compare(IGroupOfNodes o1, IGroupOfNodes o2) {
			if (o1.size() > o2.size())	{
				return -1;
			}
			else if (o1.size() < o2.size())	{
				return 1;
			}
			return 0;
		}
	}
}
