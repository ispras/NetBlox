package ru.ispras.modis.NetBlox.numericResultsPresentation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.jfree.data.xy.XYSeries;

import ru.ispras.modis.NetBlox.configuration.LanguagesConfiguration;
import ru.ispras.modis.NetBlox.dataStructures.NumericCharacteristic;
import ru.ispras.modis.NetBlox.dataStructures.internalMechs.SingleTypeBigChart;
import ru.ispras.modis.NetBlox.exceptions.ResultsPresentationException;
import ru.ispras.modis.NetBlox.scenario.DescriptionDataArrangement.StatisticsAggregation;
import ru.ispras.modis.NetBlox.scenario.ScenarioTask;

public class ListsOfValuesPerSetOfGroupsOfNodesPlotter extends JFreeChartPlotter {
	private static final String KEY_RANK_K = "rank_k";
	private static final String KEY_CUMULATIVE_AVERAGE_VALUE = "cumulativeAverageValue";

	public ListsOfValuesPerSetOfGroupsOfNodesPlotter(ScenarioTask scenarioTask) {
		super(scenarioTask);
	}


	@Override
	protected XYSeries getXYSeries(NumericCharacteristic measureValues, String seriesLabel, StatisticsAggregation aggregationType) throws ResultsPresentationException	{
		XYSeries series = null;

		switch (aggregationType)	{
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
		Y_AXIS_LABEL = addCoefficientToLabel(Y_AXIS_LABEL, plotData);
	}


	private XYSeries getCumulativeAverageXYSeries(NumericCharacteristic measureValues, String seriesLabel) throws ResultsPresentationException	{
		JFreeXYSeries series = new JFreeXYSeries(seriesLabel, currentPlotData.getAxesScale());

		if (measureValues == null)	{	//#4689. Plot 'absence of results'.
			return series;
		}

		List<Double> localValues = new ArrayList<Double>(measureValues.getValues());
		Collections.sort(localValues, new ReverseDoubleComparator());	//Sort the list in descending order.

		int communityRankNumber = 0;
		double cumulativeValue = 0.0;

		for (Double value : localValues)	{
			if (value.equals(Double.NaN) || value.equals(Double.NEGATIVE_INFINITY) || value.equals(Double.POSITIVE_INFINITY))	{
				continue;
			}
			cumulativeValue += value;
			communityRankNumber++;

			series.addCorrect((Integer)communityRankNumber, (Double)(cumulativeValue / communityRankNumber));
		}

		return series;
	}

	private XYSeries getNonAggregatedValuesSeries(NumericCharacteristic measureValues, String seriesLabel) throws ResultsPresentationException	{
		JFreeXYSeries series = new JFreeXYSeries(seriesLabel, currentPlotData.getAxesScale());

		if (measureValues == null)	{	//#4689. Plot 'absence of results'.
			return series;
		}

		List<Double> values = new ArrayList<Double>(measureValues.getValues());
		Collections.sort(values, new ReverseDoubleComparator());	//Sort the list in descending order.

		int orderNumber = 0;
		for (Double value : values)	{
			if (value.equals(Double.NaN) || value.equals(Double.NEGATIVE_INFINITY) || value.equals(Double.POSITIVE_INFINITY))	{
				continue;
			}

			orderNumber++;
			series.addCorrect((Integer)orderNumber, value);
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
