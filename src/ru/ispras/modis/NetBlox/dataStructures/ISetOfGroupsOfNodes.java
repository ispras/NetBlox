package ru.ispras.modis.NetBlox.dataStructures;

/**
 * The interface for internal representations of sets of groups of nodes.
 * 
 * @author ilya
 */
public interface ISetOfGroupsOfNodes extends IPackOfGraphStructures<IGroupOfNodes>	{	//Iterable<IGroupOfNodes> {
	public int size();
}
