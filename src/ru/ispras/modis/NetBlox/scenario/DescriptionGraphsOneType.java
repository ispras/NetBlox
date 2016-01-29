package ru.ispras.modis.NetBlox.scenario;

import java.util.Collection;


/**
 * Contains the description of graphs of one type from the scenario.
 * 
 * @author ilya
 */
public abstract class DescriptionGraphsOneType extends AlgorithmDescription {
	protected boolean directed = false;
	protected boolean weighted = false;

	protected RangeOfValues<Integer> numbersOfNodes = null;

	protected String referenceCommunitiesRelativeFileName = "";
	protected RangeOfValues<String> externalSetsForMiningFiles = null;
	protected RangeOfValues<String> externalSetsForCharacterizationFiles = null;
	protected String attributesFileName = null;


	public void setDirectedWeighted(boolean directed, boolean weighted)	{
		this.directed = directed;
		this.weighted = weighted;
	}

	public void setNumberOfNodes(RangeOfValues<Integer> n)	{
		numbersOfNodes = n;
	}

	public void setReferenceCommunitiesRelativeFileName(String name)	{
		referenceCommunitiesRelativeFileName = name;
	}

	public void setExternalForMiningFiles(RangeOfValues<String> names)	{
		externalSetsForMiningFiles = names;
	}
	public void setExternalCoversFiles(RangeOfValues<String> names)	{
		externalSetsForCharacterizationFiles = names;
	}

	public void setAttributesFileName(String name)	{
		attributesFileName = name;
	}


	@Override
	public Collection<RangeOfValues<?>> getAllVariations()	{
		Collection<RangeOfValues<?>> variations = super.getAllVariations();

		addNonNullVariation(numbersOfNodes, variations);
		addNonNullVariation(externalSetsForMiningFiles, variations);
		addNonNullVariation(externalSetsForCharacterizationFiles, variations);

		return variations;
	}
}
