package ru.ispras.modis.NetBlox.scenario;


//TODO Implement preliminary (supplementary) algorithm description.
public abstract class DescriptionPreliminaryAlgorithm extends AlgorithmDescription implements Comparable<DescriptionPreliminaryAlgorithm> {
	//private AlgorithmType.Supplementary algorithmType;

	/*public DescriptionPreliminaryAlgorithm(AlgorithmType.Supplementary algorithmType)	{
		//super(AlgorithmType.Group.SUPPLEMENTARY);

		this.algorithmType = algorithmType;
	}

	public AlgorithmType.Supplementary getType()	{
		return algorithmType;
	}


	@Override
	public int compareTo(DescriptionPreliminaryAlgorithm o) {
		int result = 0;

		if (this.algorithmType.ordinal() < o.algorithmType.ordinal())	{
			result = -1;
		}
		else if (this.algorithmType.ordinal() > o.algorithmType.ordinal())	{
			result = 1;
		}

		return result;
	}*/
}
