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
	private Integer groupID;
	private Collection<IGraph.INode> nodes;

	public GroupOfNodes(Integer groupID)	{
		this.groupID = groupID;
		nodes = new LinkedList<IGraph.INode>();
	}

	public void add(IGraph.INode node)	{
		nodes.add(node);
	}


	public boolean contains(IGraph.INode node)	{
		return nodes.contains(node);
	}


	public Integer getID()	{
		return groupID;
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
		StringBuilder clusterStringBuilder = new StringBuilder("group_").append((groupID==null)?"":groupID).append("[");
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
		if (groupID!=null)	{
			return groupID;
		}
		return nodes.hashCode();
	}

	@Override
	public boolean equals(Object obj)	{
		if (!(obj instanceof GroupOfNodes))	{
			return super.equals(obj);
		}

		GroupOfNodes other = (GroupOfNodes)obj;
		if (groupID!=null && other.groupID!=null)	{
			return groupID.equals(other.groupID);
		}
		else	{
			return nodes.equals(other.nodes);
		}
	}
}
