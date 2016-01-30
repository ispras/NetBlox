package ru.ispras.modis.NetBlox.numericResultsPresentation;

import java.awt.Color;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.DrawingSupplier;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYSeries;

import ru.ispras.modis.NetBlox.JFreeChartUtils;
import ru.ispras.modis.NetBlox.configuration.LanguagesConfiguration;
import ru.ispras.modis.NetBlox.dataStructures.NumericCharacteristic;
import ru.ispras.modis.NetBlox.dataStructures.internalMechs.CoordinateVector;
import ru.ispras.modis.NetBlox.dataStructures.internalMechs.MultiDimensionalArray;
import ru.ispras.modis.NetBlox.dataStructures.internalMechs.SingleTypeBigChart;
import ru.ispras.modis.NetBlox.exceptions.ResultsPresentationException;
import ru.ispras.modis.NetBlox.scenario.DescriptionDataArrangement.PlotStyle;
import ru.ispras.modis.NetBlox.scenario.DescriptionDataArrangement.StatisticsAggregation;
import ru.ispras.modis.NetBlox.scenario.ScenarioTask;

public class DistributionPlotter extends JFreeChartPlotter {
	private PlotStyle currentPlotStyle;


	public DistributionPlotter(ScenarioTask scenarioTask) {
		super(scenarioTask);
	}


	@Override
	public void plotValuesDistributedOverCommunities(SingleTypeBigChart plotData) throws ResultsPresentationException	{
		currentPlotStyle = plotData.getPlotStyle();
		if (currentPlotStyle == PlotStyle.BAR)	{
			showGraphsData = plotData.toShowGraphsData();

			CategoryDataset dataset = prepareDatasetForPlot(plotData);

			String plotTitle = null;	//plotData.getMeasureName()
			JFreeChart chart = ChartFactory.createBarChart(plotTitle, X_AXIS_LABEL, Y_AXIS_LABEL, dataset,
					PlotOrientation.VERTICAL, plotData.showLegend(), false, false);
			tweakPlot(chart.getCategoryPlot());
			addSubtitles(chart, plotData);

			JFreeChartUtils.exportToPNG(makePNGPlotFilePathname(plotData), chart, plotData.getPlotWidth(), plotData.getPlotHeight());

			showGraphsData = null;
		}
		else	{
			super.plotValuesDistributedOverCommunities(plotData);
		}
		currentPlotStyle = null;
	}


	private CategoryDataset prepareDatasetForPlot(SingleTypeBigChart plotData) throws ResultsPresentationException	{
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();

		for (MultiDimensionalArray lineData : plotData)	{
			processAlongXAxis(lineData, plotData.getStatisticsAggregationType(), dataset);
		}

		return dataset;
	}

	@Override
	protected void processValuesForFixedXValue(Object xValue, CoordinateVector<Object> fixedXCoordinates,
			MultiDimensionalArray lineData, StatisticsAggregation aggregationType, Object resultContainer) throws ResultsPresentationException	{
		if (currentPlotStyle == PlotStyle.BAR)	{
			DefaultCategoryDataset dataset = (DefaultCategoryDataset)resultContainer;	//XXX Check?
	
			MultiDimensionalArray.DataCell dataCell = lineData.getDataCell(fixedXCoordinates);
	
			String seriesLabel = makeSeriesLabel(lineData, MultiDimensionalArray.FIRST_DIMENSION, xValue, dataCell);
			switch (aggregationType)	{
			/*case BIG_INTERVALS:	//XXX Need to make somehow unified intervals for BIG_INTERVALS.
				putDistributionWithBiggerIntervalsToDataset(dataset, value, seriesLabel);
				break;*/
			default:
				putDistributionToCategoryDataset(dataset, dataCell.getCarriedValue(), seriesLabel);
			}
		}
		else	{
			super.processValuesForFixedXValue(xValue, fixedXCoordinates, lineData, aggregationType, resultContainer);
		}
	}

	private void putDistributionToCategoryDataset(DefaultCategoryDataset dataset, NumericCharacteristic measureValues, String seriesLabel)	{
		NumericCharacteristic.Distribution distribution = measureValues.getDistribution();

		for (Number value : distribution.getValues())	{
			Integer numberOfOccurences = distribution.getNumberOfOccurences(value);
			if (value.equals(0))	{
				value = value.doubleValue() + Double.MIN_VALUE;
			}

			dataset.addValue(numberOfOccurences, seriesLabel, value.toString());
		}
	}

	/*private void putDistributionWithBiggerIntervalsToDataset(DefaultCategoryDataset dataset, CharacteristicOrMeasure measureValues, String seriesLabel)	{
		CharacteristicOrMeasure.Distribution distribution = measureValues.getDistribution();

		int numberOfIntervals = (int) Math.floor(Math.sqrt(distribution.getTotalNumberOfOccurences()));

		Set<Number> values = distribution.getValues();
		List<Double> doubleValues = new ArrayList<Double>(values.size());
		for (Number number : values)	{
			doubleValues.add(number.doubleValue());
		}

		double minValue = Collections.min(doubleValues);
		double maxValue = Collections.max(doubleValues)*1.00001;;
		double intervalLength = (maxValue - minValue) / numberOfIntervals;

		LinkedList<Number> localList = new LinkedList<Number>(values);
		double intervalMinValue = minValue;
		for (int i=1 ; i<=numberOfIntervals ; i++)	{
			int aggregatedNumberOfOccurences = 0;

			double intervalMaxValue = intervalMinValue + intervalLength;

			ListIterator<Number> localValuesIterator = localList.listIterator();
			while (localValuesIterator.hasNext())	{
				Number candidate = localValuesIterator.next();
				if (intervalMinValue <= candidate.doubleValue()  &&  candidate.doubleValue() <= intervalMaxValue)	{
					aggregatedNumberOfOccurences += distribution.getNumberOfOccurences(candidate);

					localValuesIterator.remove();
				}
			}

			String categoryLabel = "["+(int)intervalMinValue+";"+(int)intervalMaxValue+"]";
			dataset.addValue(aggregatedNumberOfOccurences, seriesLabel, categoryLabel);

			intervalMinValue = intervalMaxValue;
		}
	}*/


	@Override
	protected XYSeries getXYSeries(NumericCharacteristic measureValues, String seriesLabel, StatisticsAggregation aggregationType)	{
		return getDistributionXYSeries(measureValues, seriesLabel);
	}


	private void tweakPlot(CategoryPlot plot)	{
		//TODO Tweak axes.

		plot.setBackgroundPaint(Color.white);
		plot.setDomainGridlinePaint(Color.gray);
		plot.setRangeGridlinePaint(Color.gray);

		//Use textures for paint.
		DrawingSupplier drawingSupplier = texturesAdapter.getDrawingSupplier();
		plot.setDrawingSupplier(drawingSupplier);

		if (plot.getRenderer() instanceof BarRenderer)	{
			BarRenderer barRenderer = (BarRenderer) plot.getRenderer();
			barRenderer.setBarPainter( new StandardBarPainter() );
			barRenderer.setGradientPaintTransformer(null);

			barRenderer.setBaseOutlinePaint(Color.black); // set bar outline
			barRenderer.setDrawBarOutline(true);
		}

		tweakLegend(plot);
	}


	@Override
	protected void defineAxesLabels(SingleTypeBigChart plotData)	{
		X_AXIS_LABEL = plotData.getMeasureValuesName();
		Y_AXIS_LABEL = LanguagesConfiguration.getNetBloxLabel(KEY_NUMBER_OF_OCCURENCES);
	}
}
