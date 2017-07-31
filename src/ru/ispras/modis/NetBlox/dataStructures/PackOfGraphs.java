package ru.ispras.modis.NetBlox.dataStructures;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class PackOfGraphs implements IPackOfGraphStructures<IGraph> {
	private List<IGraph> graphs;


	public PackOfGraphs()	{
		graphs = new LinkedList<IGraph>();
	}

	public PackOfGraphs(List<IGraph> graphs)	{
		this.graphs = graphs;
	}


	public void add(IGraph graphStructure)	{
		graphs.add(graphStructure);
	}


	public List<IGraph> getList()	{
		return Collections.unmodifiableList(graphs);
	}


	public boolean isEmpty()	{
		return graphs.isEmpty();
	}


	@Override
	public Iterator<IGraph> iterator() {
		return graphs.iterator();
	}
}
