package ru.ispras.modis.NetBlox.scenario;

import java.util.List;

import ru.ispras.modis.NetBlox.utils.MiningJobBase;
import ru.ispras.modis.NetBlox.utils.MiningJobBase.JobBase;
import ru.ispras.modis.NetBlox.utils.Pair;


/**
 * A fixed set of parameters for a specific graph (basic class for such a set of parameters).
 * 
 * @author ilya
 */
public abstract class GraphParametersSet extends ParametersSet {
	protected static final String GRAPH_FILE_NAME = "network.dat";
	protected static final String NODES_ATTRIBUTES_FILE_NAME = "nodes.attributes.dat";

	private static int numberOfGraphParametersSets = 0;
	private int numberAmongOtherParametersSets;

	private String graphTypeName;
	private String graphDescriptionId;

	private ValueFromRange<Integer> numberOfNodes = null;
	private String referenceCommunitiesRelativeFileName = "";
	private boolean directed = false;
	private boolean weighted = false;

	private MiningJobBase.JobBase externalDataForMiningType = JobBase.NODES_GROUPS_SET;
	private MiningJobBase.JobBase externalDataForCharacterizationType = JobBase.NODES_GROUPS_SET;
	private RangeOfValues<String> externalDataForMiningFilenames = null;
	private RangeOfValues<String> externallyProvidedForCharacterizationFilenames = null;
	private String attributesFileName = null;

	private ValueFromRange<Integer> generationNumber = null;	//For the case when we explore several generations of the same graph type with exactly the same parameters.


	public GraphParametersSet(String graphTypeName, String graphDescriptionID, boolean directed, boolean weighted,
			ValueFromRange<Integer> numberOfNodes, ValueFromRange<Integer> generation)	{
		this.graphTypeName = graphTypeName;
		this.graphDescriptionId = graphDescriptionID;
		this.directed = directed;
		this.weighted = weighted;
		this.numberOfNodes = numberOfNodes;
		this.generationNumber = generation;

		numberOfGraphParametersSets++;
		numberAmongOtherParametersSets = numberOfGraphParametersSets;
	}

	public static void resetNumberOfGraphParametersSets()	{
		numberOfGraphParametersSets = 0;
	}

	public void setFilenames(String referenceRelativeFilename, String attributesFileName,
			RangeOfValues<String> externalDataForMiningFilenames, MiningJobBase.JobBase externalDataForMiningType,
			RangeOfValues<String> externallyProvidedForCharacterizationFilenames, MiningJobBase.JobBase externalDataForCharacterizationType)	{
		if (referenceRelativeFilename != null)	{
			this.referenceCommunitiesRelativeFileName = referenceRelativeFilename;
		}
		this.attributesFileName = attributesFileName;

		if (externalDataForMiningFilenames != null)	{
			this.externalDataForMiningType = externalDataForMiningType;
			this.externalDataForMiningFilenames = externalDataForMiningFilenames;
		}
		if (externallyProvidedForCharacterizationFilenames != null)	{
			this.externalDataForCharacterizationType = externalDataForCharacterizationType;
			this.externallyProvidedForCharacterizationFilenames = externallyProvidedForCharacterizationFilenames;
		}
	}


	public String getGraphTypeName()	{
		return graphTypeName;
	}

	public String getGraphDescriptionId()	{
		return graphDescriptionId;
	}


	public boolean isDirected()	{
		return directed;
	}

	public boolean isWeighted()	{
		return weighted;
	}


	public int getNumberOfNodes()	{
		return numberOfNodes.getValue();
	}

	public String getReferenceCoverFilename()	{
		return referenceCommunitiesRelativeFileName;
	}

	public RangeOfValues<String> getProvidedForMiningExternalDataFilenames()	{
		return externalDataForMiningFilenames;
	}
	public RangeOfValues<String> getProvidedForCharacterizationExternalFilenames()	{
		return externallyProvidedForCharacterizationFilenames;
	}
	public MiningJobBase.JobBase getTypeOfProvidedForMiningExternalData()	{
		return externalDataForMiningType;
	}
	public MiningJobBase.JobBase getTypeOfProvidedForCharacterizationExternalData()	{
		return externalDataForCharacterizationType;
	}


	public Integer getGenerationNumber()	{
		return (generationNumber==null)?null:generationNumber.getValue();
	}


	@Override
	public boolean hasParametersFromSomeRange() {
		return ((numberOfNodes != null)  &&  (!numberOfNodes.getRangeId().equals(RangeOfValues.NO_RANGE_ID)))   ||
				(generationNumber != null)  &&  (!generationNumber.getRangeId().equals(RangeOfValues.NO_RANGE_ID));
	}

	@Override
	public Object getValueForVariationId(String id) {
		Object result = null;
		if ((numberOfNodes != null)  &&  id.equals(numberOfNodes.getRangeId()))	{
			result = numberOfNodes.getValue();
		}
		else if ((generationNumber != null)  &&  id.equals(generationNumber.getRangeId()))	{
			result = getGenerationNumber();
		}

		return result;
	}


	protected List<Pair<String, String>> getCommonGraphParametersInList()	{
		List<Pair<String, String>> result = appendNonNullParameter(null, numberOfNodes, "n");
		return result;
	}


	public String getGraphFileName() {
		return GRAPH_FILE_NAME;
	}

	public String getNodesAttributesFilename()	{
		return NODES_ATTRIBUTES_FILE_NAME;
	}

	public String getShortLabel()	{
		String label = "G_" + numberAmongOtherParametersSets;
		return label;
	}


	public int getNumberAmongOtherGraphParametersSets()	{
		return numberAmongOtherParametersSets;
	}
}
