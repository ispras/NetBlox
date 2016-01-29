package ru.ispras.modis.NetBlox.dataStructures;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import ru.ispras.modis.NetBlox.dataStructures.IGraph.INode;

/**
 * An inner representation for a group of graph nodes.
 * 
 * @author ilya
 */
public class GroupOfNodes implements IGroupOfNodes {
	private Collection<IGraph.INode> nodes;

	public GroupOfNodes()	{
		nodes = new LinkedList<IGraph.INode>();
	}

	public void add(IGraph.INode node)	{
		nodes.add(node);
	}


	public boolean contains(IGraph.INode node)	{
		return nodes.contains(node);
	}


	@Override
	public int size()	{
		return nodes.size();
	}

	@Override
	public Iterator<INode> iterator() {
		return nodes.iterator();
	}


	@Override
	public String toString()	{
		StringBuilder clusterStringBuilder = new StringBuilder("group_[");
		boolean firstNode = true;
		for (IGraph.INode node : nodes)	{
			if (firstNode)	{	firstNode = false;	}
			else	{	clusterStringBuilder.append(',');	}
			clusterStringBuilder.append(node.getId());
		}
		clusterStringBuilder.append(']');

		return clusterStringBuilder.toString();
	}

	@Override
	public int hashCode()	{
		return nodes.hashCode();
	}
}
