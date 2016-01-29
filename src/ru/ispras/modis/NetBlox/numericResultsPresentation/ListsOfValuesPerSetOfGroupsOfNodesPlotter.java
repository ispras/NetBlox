package ru.ispras.modis.NetBlox.numericResultsPresentation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.jfree.data.xy.XYSeries;

import ru.ispras.modis.NetBlox.configuration.LanguagesConfiguration;
import ru.ispras.modis.NetBlox.dataStructures.NumericCharacteristic;
import ru.ispras.modis.NetBlox.dataStructures.internalMechs.SingleTypeBigChart;
import ru.ispras.modis.NetBlox.scenario.DescriptionDataArrangement.StatisticsAggregation;
import ru.ispras.modis.NetBlox.scenario.ScenarioTask;

public class ListsOfValuesPerSetOfGroupsOfNodesPlotter extends JFreeChartPlotter {
	private static final String KEY_RANK_K = "rank_k";
	private static final String KEY_CUMULATIVE_AVERAGE_VALUE = "cumulativeAverageValue";

	public ListsOfValuesPerSetOfGroupsOfNodesPlotter(ScenarioTask scenarioTask) {
		super(scenarioTask);
	}


	@Override
	protected XYSeries getXYSeries(NumericCharacteristic measureValues, String seriesLabel, StatisticsAggregation aggregationType)	{
		XYSeries series = null;

		switch (aggregationType)	{
		case BIG_INTERVALS:	//nothing special for now
		case DISTRIBUTION:
			series = getDistributionXYSeries(measureValues, seriesLabel);
			break;
		case CUMULATIVE_AVERAGE:
			series = getCumulativeAverageXYSeries(measureValues, seriesLabel);
			break;
		case NONE:
			series = getNonAggregatedValuesSeries(measureValues, seriesLabel);
			break;
		}

		return series;
	}

	@Override
	protected void defineAxesLabels(SingleTypeBigChart plotData)	{
		switch (plotData.getStatisticsAggregationType())	{
		case BIG_INTERVALS:	//nothing special for now
		case DISTRIBUTION:
			X_AXIS_LABEL = plotData.getMeasureValuesName();
			Y_AXIS_LABEL = LanguagesConfiguration.getNetBloxLabel(KEY_NUMBER_OF_OCCURENCES);
			break;
		case CUMULATIVE_AVERAGE:
			X_AXIS_LABEL = LanguagesConfiguration.getNetBloxLabel(KEY_RANK_K);
			Y_AXIS_LABEL = LanguagesConfiguration.getNetBloxLabel(KEY_CUMULATIVE_AVERAGE_VALUE);
			break;
		case NONE:
			X_AXIS_LABEL = LanguagesConfiguration.getNetBloxLabel(KEY_RANK_K);
			Y_AXIS_LABEL = plotData.getMeasureValuesName();
			break;
		}
	}


	private XYSeries getCumulativeAverageXYSeries(NumericCharacteristic measureValues, String seriesLabel)	{
		XYSeries series = new XYSeries(seriesLabel);

		List<Double> localValues = new ArrayList<Double>(measureValues.getValues());
		Collections.sort(localValues, new ReverseDoubleComparator());	//Sort the list in descending order.

		int communityRankNumber = 0;
		double cumulativeValue = 0.0;

		for (Double value : localValues)	{
			cumulativeValue += value;
			communityRankNumber++;

			series.add(communityRankNumber, cumulativeValue / communityRankNumber);
		}

		return series;
	}

	private XYSeries getNonAggregatedValuesSeries(NumericCharacteristic measureValues, String seriesLabel)	{
		XYSeries series = new XYSeries(seriesLabel);

		List<Double> values = new ArrayList<Double>(measureValues.getValues());
		Collections.sort(values, new ReverseDoubleComparator());	//Sort the list in descending order.

		int orderNumber = 0;
		for (Double value : values)	{
			orderNumber++;

			series.add(orderNumber, value);
		}

		return series;
	}



	private class ReverseDoubleComparator implements Comparator<Double>	{
		@Override
		public int compare(Double arg0, Double arg1) {
			return -arg0.compareTo(arg1);
		}
	}
}
