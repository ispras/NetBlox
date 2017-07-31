package ru.ispras.modis.NetBlox.dataStructures;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ru.ispras.modis.NetBlox.exceptions.SetOfGroupsException;
import ru.ispras.modis.NetBlox.exceptions.SourceGraphException;

/**
 * An internal representation for a set of groups of graph nodes.
 * 
 * @author ilya
 */
public class SetOfGroupsOfNodes implements ISetOfGroupsOfNodes {
	private static final String WHITESPACE_CHARACTER_REGEX = "\\s";
	public static final String IDS_PRESENCE_MARK = "ids";

	private final Collection<IGroupOfNodes> setOfGroups;

	public SetOfGroupsOfNodes(Collection<IGroupOfNodes> setOfGroups)	{
		this.setOfGroups = setOfGroups;
	}

	public SetOfGroupsOfNodes(String pathToFileWithGroups, IGraph graph) throws SourceGraphException, SetOfGroupsException	{
		setOfGroups = parseCommunitiesFile(pathToFileWithGroups, graph);
	}


	private Collection<IGroupOfNodes> parseCommunitiesFile(String pathToFileWithGroups, IGraph graph) throws SourceGraphException, SetOfGroupsException	{
		Collection<IGroupOfNodes> collectionOfGroups = new LinkedList<IGroupOfNodes>();

		Path path = Paths.get(pathToFileWithGroups);
		if (!Files.exists(path))	{
			throw new SetOfGroupsException("File with set of groups does not exist: "+pathToFileWithGroups);
		}

		try {
			List<String> groupsOfNodesInLines = Files.readAllLines(Paths.get(pathToFileWithGroups), Charset.defaultCharset());
			if (groupsOfNodesInLines==null || groupsOfNodesInLines.isEmpty())	{
				return collectionOfGroups;	//Shouldn't there be an exception?
			}

			boolean withIDs = false;
			Iterator<String> linesIterator = groupsOfNodesInLines.iterator();
			String line = linesIterator.next();
			if (line.trim().equalsIgnoreCase(IDS_PRESENCE_MARK))	{
				withIDs = true;
			}
			else	{
				IGroupOfNodes groupOfNodes = parseGroupNodes(line, graph, false);
				if (groupOfNodes != null)	{
					collectionOfGroups.add(groupOfNodes);
				}
			}

			while (linesIterator.hasNext())	{
				line = linesIterator.next();
				IGroupOfNodes groupOfNodes = parseGroupNodes(line, graph, withIDs);
				if (groupOfNodes == null)	{
					break;
				}
				collectionOfGroups.add(groupOfNodes);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return collectionOfGroups;
	}

	private IGroupOfNodes parseGroupNodes(String groupInLine, IGraph graph, boolean withIDs) throws SourceGraphException	{
		List<String> nodesIdsStrings = Arrays.asList(groupInLine.split(WHITESPACE_CHARACTER_REGEX));
		if (nodesIdsStrings==null || nodesIdsStrings.isEmpty())	{
			return null;
		}
		Iterator<String> iterator = nodesIdsStrings.iterator();

		IGroupOfNodes group = null;
		if (withIDs)	{
			Integer groupId = Integer.parseInt(iterator.next());
			group = new GroupOfNodes(groupId);
		}
		else	{
			group = new GroupOfNodes(null);
		}

		while (iterator.hasNext())	{
			Integer id = Integer.parseInt(iterator.next());
			IGraph.INode node = graph.getNode(id);
			if (node == null)	{
				String errorMessage = "Node with id "+id+" is present in a group of nodes but is absent from the graph.";
				throw new SourceGraphException(errorMessage);
				//System.out.println(errorMessage);
			}
			else	{	//The option (instead of plain adding the node) is necessary in case no exception is thrown above, but just a message is printed.
				group.add(node);
			}
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
