package ru.ispras.modis.NetBlox.dataStructures.internalMechs;

import java.util.ArrayList;
import java.util.List;

import ru.ispras.modis.NetBlox.dataManagement.GraphOnDriveHandler;
import ru.ispras.modis.NetBlox.scenario.GraphMiningParametersSet;
import ru.ispras.modis.NetBlox.scenario.RangeOfValues;
import ru.ispras.modis.NetBlox.scenario.ValueFromRange;

public class ExtendedMiningParameters {
	private GraphMiningParametersSet miningParameters;

	private String absoluteExternalFilename;
	private ValueFromRange<String> relativeExternalSingleFilename;

	private List<String> absoluteExternalFilenames;
	private RangeOfValues<String> relativeExternalFilenames;


	public ExtendedMiningParameters(GraphMiningParametersSet miningParameters, ValueFromRange<String> externalFileRelativePath,
			RangeOfValues<String> externalFilesRelativePaths, GraphOnDriveHandler principalGraphHandler)	{
		this.miningParameters = miningParameters;

		absoluteExternalFilename = (externalFileRelativePath==null) ? null :
			principalGraphHandler.getAbsolutePathPossiblyWithGraphDirectory(externalFileRelativePath.getValue());
		this.relativeExternalSingleFilename = externalFileRelativePath;

		absoluteExternalFilenames = null;
		if (externalFilesRelativePaths != null)	{
			absoluteExternalFilenames = new ArrayList<String>(externalFilesRelativePaths.size());
			for (String relativeFilename : externalFilesRelativePaths)	{
				String absoluteFilename = principalGraphHandler.getAbsolutePathPossiblyWithGraphDirectory(relativeFilename);
				absoluteExternalFilenames.add(absoluteFilename);
			}
		}
		this.relativeExternalFilenames = externalFilesRelativePaths;
	}

	public ExtendedMiningParameters(GraphMiningParametersSet miningParameters, ExtendedMiningParameters extendedParameters)	{
		this.miningParameters = miningParameters;

		this.absoluteExternalFilename = extendedParameters.absoluteExternalFilename;
		this.relativeExternalSingleFilename = extendedParameters.relativeExternalSingleFilename;

		this.absoluteExternalFilenames = extendedParameters.absoluteExternalFilenames;
		this.relativeExternalFilenames = extendedParameters.relativeExternalFilenames;
	}


	public GraphMiningParametersSet getMiningParameters()	{
		return miningParameters;
	}

	public String getAbsoluteExternalFilename()	{
		return absoluteExternalFilename;
	}
	public ValueFromRange<String> getRelativeExternalFilename()	{
		return relativeExternalSingleFilename;
	}

	public List<String> getAbsoluteExternalFilenames()	{
		return absoluteExternalFilenames;
	}
	public RangeOfValues<String> getRelativeExternalFilenames()	{
		return relativeExternalFilenames;
	}
}
