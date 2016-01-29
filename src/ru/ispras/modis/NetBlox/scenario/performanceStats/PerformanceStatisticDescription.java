package ru.ispras.modis.NetBlox.scenario.performanceStats;

import org.osgi.framework.Bundle;

import ru.ispras.modis.NetBlox.Activator;
import ru.ispras.modis.NetBlox.scenario.DescriptionMeasure;

public class PerformanceStatisticDescription extends DescriptionMeasure {

	public PerformanceStatisticDescription() {
		super(null);
		setAlgorithmNameInScenario(PerformanceStatisticParameters.PerformanceStatType.EXEC_TIME.toString());
	}


	@Override
	protected Bundle getImplementingPluginBundle() {
		return Activator.getContext().getBundle();
	}

}
