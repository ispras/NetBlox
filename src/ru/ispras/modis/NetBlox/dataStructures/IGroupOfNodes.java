package ru.ispras.modis.NetBlox.dataStructures;

/**
 * The interface for internal representations of a group of nodes (community or any other).
 * 
 * @author ilya
 */
public interface IGroupOfNodes extends Iterable<IGraph.INode> {
	public void add(IGraph.INode node);

	public boolean contains(IGraph.INode node);

	/**
	 * @return	group ID. <code>null</null> if none.
	 */
	public Integer getID();

	public int size();

	//public int countCommonNodes(IGroupOfNodes otherGroup);
}
