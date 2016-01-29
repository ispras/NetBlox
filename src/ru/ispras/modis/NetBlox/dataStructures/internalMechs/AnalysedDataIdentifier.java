package ru.ispras.modis.NetBlox.dataStructures.internalMechs;


public class AnalysedDataIdentifier {
	public enum Type	{	MINED, EXTERNAL	}

	private Type type;
	private ExtendedMiningParameters miningParameters;
	private String externalFilePathString;


	public AnalysedDataIdentifier(ExtendedMiningParameters miningParameters)	{
		type = Type.MINED;
		this.miningParameters = miningParameters;
	}

	public AnalysedDataIdentifier(String externalFilePathString)	{
		type = Type.EXTERNAL;
		this.externalFilePathString = externalFilePathString;
	}


	public Type type()	{
		return type;
	}

	public ExtendedMiningParameters getMiningParameters()	{
		if (type != Type.MINED)	{
			//TODO Mismatching identifier call, throw an exception.
		}
		return miningParameters;
	}

	public String getExternalFilepathAsInScenario()	{
		if (type != Type.EXTERNAL)	{
			//TODO Mismatching identifier call, throw an exception.
		}
		return externalFilePathString;
	}
}
