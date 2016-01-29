package ru.ispras.modis.NetBlox.dataStructures;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ru.ispras.modis.NetBlox.exceptions.SourceGraphException;

/**
 * An internal representation for a set of groups of graph nodes.
 * 
 * @author ilya
 */
public class SetOfGroupsOfNodes implements ISetOfGroupsOfNodes {
	private static final String WHITESPACE_CHARACTER_REGEX = "\\s";

	private final Collection<IGroupOfNodes> setOfGroups;
	//XXX IGraph?

	public SetOfGroupsOfNodes(Collection<IGroupOfNodes> setOfGroups)	{
		this.setOfGroups = setOfGroups;
	}

	public SetOfGroupsOfNodes(String pathToFileWithGroups, IGraph graph) throws SourceGraphException	{
		setOfGroups = parseCommunitiesFile(pathToFileWithGroups, graph);
	}


	private Collection<IGroupOfNodes> parseCommunitiesFile(String pathToFileWithGroups, IGraph graph) throws SourceGraphException	{
		Collection<IGroupOfNodes> collectionOfGroups = new LinkedList<IGroupOfNodes>();

		try {
			List<String> groupsOfNodesInLines = Files.readAllLines(Paths.get(pathToFileWithGroups), Charset.defaultCharset());
			for (String groupInLine : groupsOfNodesInLines)	{
				IGroupOfNodes groupOfNodes = parseGroupNodes(groupInLine, graph);
				collectionOfGroups.add(groupOfNodes);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return collectionOfGroups;
	}

	private IGroupOfNodes parseGroupNodes(String groupInLine, IGraph graph) throws SourceGraphException	{
		IGroupOfNodes group = new GroupOfNodes();

		String[] nodesIdsStrings = groupInLine.split(WHITESPACE_CHARACTER_REGEX);
		for (String nodeIdString : nodesIdsStrings)	{
			Integer id = Integer.parseInt(nodeIdString);
			IGraph.INode node = graph.getNode(id);
			if (node == null)	{
				String errorMessage = "Node with id "+id+" is present in a group of nodes but is absent from the graph.";
				throw new SourceGraphException(errorMessage);
			}
			group.add(node);
		}

		return group;
	}


	@Override
	public int size()	{
		return setOfGroups.size();
	}

	@Override
	public Iterator<IGroupOfNodes> iterator() {
		return setOfGroups.iterator();
	}
}
