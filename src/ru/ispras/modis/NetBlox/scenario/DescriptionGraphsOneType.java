package ru.ispras.modis.NetBlox.scenario;

import java.util.Collection;

import ru.ispras.modis.NetBlox.utils.MiningJobBase;
import ru.ispras.modis.NetBlox.utils.MiningJobBase.JobBase;


/**
 * Contains the description of graphs of one type from the scenario.
 * 
 * @author ilya
 */
public abstract class DescriptionGraphsOneType extends AlgorithmDescription {
	protected boolean directed = false;
	protected boolean weighted = false;

	protected RangeOfValues<Integer> numbersOfNodes = null;

	private String referenceDataRelativeFileName = "";
	private RangeOfValues<String> externalDataForMiningFiles = null;
	private RangeOfValues<String> externallyProvidedForCharacterizationFiles = null;
	private String attributesFileName = null;

	private MiningJobBase.JobBase externalDataForMiningType = JobBase.NODES_GROUPS_SET;
	private MiningJobBase.JobBase externalDataForCharacterizationType = JobBase.NODES_GROUPS_SET;


	public void setDirectedWeighted(boolean directed, boolean weighted)	{
		this.directed = directed;
		this.weighted = weighted;
	}

	public void setNumberOfNodes(RangeOfValues<Integer> n)	{
		numbersOfNodes = n;
	}

	public void setReferenceCommunitiesRelativeFileName(String name)	{
		referenceDataRelativeFileName = name;
	}

	public void setExternalForMiningFiles(RangeOfValues<String> names, MiningJobBase.JobBase dataType)	{
		externalDataForMiningFiles = names;
		externalDataForMiningType = dataType;
	}
	public void setExternalForCharacterizationFiles(RangeOfValues<String> names, MiningJobBase.JobBase dataType)	{
		externallyProvidedForCharacterizationFiles = names;
		externalDataForCharacterizationType = dataType;
	}

	public void setAttributesFileName(String name)	{
		attributesFileName = name;
	}


	protected void setSpecifiedFilenames(GraphParametersSet parametersSet)	{
		parametersSet.setFilenames(referenceDataRelativeFileName, attributesFileName,
				externalDataForMiningFiles, externalDataForMiningType,
				externallyProvidedForCharacterizationFiles, externalDataForCharacterizationType);
	}


	@Override
	public Collection<RangeOfValues<?>> getAllVariations()	{
		Collection<RangeOfValues<?>> variations = super.getAllVariations();

		addNonNullVariation(numbersOfNodes, variations);
		addNonNullVariation(externalDataForMiningFiles, variations);
		addNonNullVariation(externallyProvidedForCharacterizationFiles, variations);

		return variations;
	}
}
