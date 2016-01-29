package ru.ispras.modis.NetBlox.scenario.performanceStats;

import ru.ispras.modis.NetBlox.scenario.MeasureParametersSet;

public class PerformanceStatisticParameters extends MeasureParametersSet {
	public enum PerformanceStatType	{EXEC_TIME}

	public PerformanceStatisticParameters() {
		super(PerformanceStatType.EXEC_TIME.toString(), null);
	}

}
