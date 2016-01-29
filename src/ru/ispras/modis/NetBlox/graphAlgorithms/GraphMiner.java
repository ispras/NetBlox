package ru.ispras.modis.NetBlox.graphAlgorithms;


//TODO Turn this into an interface? And extract interfaces from children?
public abstract class GraphMiner {
	protected void throwUnimplementedException()	{
		throw new UnsupportedOperationException("The method hasn't been implemented in plug-in.");
	}
}
