package ru.ispras.modis.NetBlox.scenario;


/**
 * For the uncategorised and uploaded graphs. The path to a (root) directory with such graph must be specified explicitly,
 * as well as the name of the file that contains the graph.
 * 
 * @author ilya
 */
public abstract class UploadedGraphDataParametersSet extends GraphParametersSet {
	private String graphDirectoryPathname;
	private String graphFileName;

	//The flag tells the system if the graph file location is fixed by user (for example when an external graph is taken)
	//or is it partly defined by the system (for example when a graph is extracted from a provided dataset or another graph).
	private boolean isPathToRequiredGraphExternallyFixed = false;


	/**
	 * Constructor for uncategorised/uploaded graph data parameters set.
	 * @param graphTypeName	- can be null	TODO ?
	 * @param graphDescriptionID
	 * @param directed
	 * @param weighted
	 * @param directoryPathname - the path to the directory with the graph file, can be either relative or absolute.
	 * @param graphFileName - the name of the file with graph (relative).
	 * @param referenceCommunitiesRelativeFileName
	 * @param externalSetsForMiningFilenames
	 * @param externalSetsForMeasuresFilenames
	 * @param attributesFileName - the name of the file with attributes (is applicable only for some graphs).
	 * @param generation - this is the number of generation for the case when the graph is generated several times.
	 */
	public UploadedGraphDataParametersSet(String graphTypeName, String graphDescriptionID, boolean directed, boolean weighted,
			String directoryPathname, String graphFileName, String referenceCommunitiesRelativeFileName,
			RangeOfValues<String> externalSetsForMiningFilenames, RangeOfValues<String> externalSetsForMeasuresFilenames,
			String attributesFileName, ValueFromRange<Integer> generation) {
		super(graphTypeName, graphDescriptionID, directed, weighted, null, referenceCommunitiesRelativeFileName,
				externalSetsForMiningFilenames, externalSetsForMeasuresFilenames, attributesFileName, generation);

		this.graphDirectoryPathname = directoryPathname;
		this.graphFileName = graphFileName;
	}


	/**
	 * The path can be either relative or absolute.
	 * @return the path to the directory with the graph file.
	 */
	public String getDirectoryPathname()	{
		return graphDirectoryPathname;
	}

	/**
	 * The name of the file with graph (relative).
	 */
	@Override
	public String getGraphFileName()	{
		return graphFileName;
	}


	/**
	 * Sets the flag that tells the system if the graph file location is fixed by user (for example when an external graph is taken)
	 * or is it partly defined by the system (for example when a graph is extracted from a provided dataset or another graph).
	 * @param flag	- boolean flag
	 */
	public void setPathToRequiredGraphExternallyFixed(boolean flag)	{
		isPathToRequiredGraphExternallyFixed = flag;
	}
	public boolean isPathToRequiredGraphExternallyFixed()	{
		return isPathToRequiredGraphExternallyFixed;
	}


	@Override
	public String toString()	{
		StringBuilder builder = new StringBuilder("[Unparametrised: [directory: ").
				append(graphDirectoryPathname).append("] [file name: ").
				append(graphFileName).append("]]");

		return builder.toString();
	}
}
