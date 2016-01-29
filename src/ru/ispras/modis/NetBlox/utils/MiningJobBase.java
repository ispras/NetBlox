package ru.ispras.modis.NetBlox.utils;

/**
 * The algorithm introduces the possible job bases for graph mining and characteristics computation plug-ins.
 * 
 * @author ilya
 */
public class MiningJobBase {
	public enum JobBase	{
		GRAPH, MULTIPLE_GRAPHS, NODES_GROUPS_SET, MULTIPLE_SETS_OF_GROUPS_OF_NODES, NUMERIC_CHARACTERISTIC
	}

	private JobBase jobBase;

	public MiningJobBase(JobBase jobBase)	{
		this.jobBase = jobBase;
	}

	public JobBase getJobBase()	{
		return jobBase;
	}
}
