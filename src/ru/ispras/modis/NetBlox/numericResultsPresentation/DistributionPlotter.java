package ru.ispras.modis.NetBlox.numericResultsPresentation;

import java.awt.Color;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.DrawingSupplier;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.CategoryDataset;
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
	public DistributionPlotter(ScenarioTask scenarioTask) {
		super(scenarioTask);
	}


	@Override
	public void plotValuesDistributedOverCommunities(SingleTypeBigChart plotData) throws ResultsPresentationException	{
		if (plotData.getPlotStyle() == PlotStyle.BAR)	{
			currentPlotData = plotData;

			CategoryDataset dataset = prepareCategoryDataset(plotData);

			String plotTitle = null;	//plotData.getMeasureName()
			defineAxesLabels(plotData);

			JFreeChart chart = ChartFactory.createBarChart(plotTitle, X_AXIS_LABEL, Y_AXIS_LABEL, dataset,
					PlotOrientation.VERTICAL, plotData.showLegend(), false, false);
			tweakBarPlot(chart.getCategoryPlot());
			addSubtitles(chart, plotData);

			JFreeChartUtils.exportToPNG(makePNGPlotFilePathname(plotData), chart, plotData.getPlotWidth(), plotData.getPlotHeight());

			currentPlotData = null;
		}
		else	{
			super.plotValuesDistributedOverCommunities(plotData);
		}
	}


	@Override
	protected void processValuesForFixedXValue(Object xValue, CoordinateVector<Object> fixedXCoordinates,
			MultiDimensionalArray lineData, StatisticsAggregation aggregationType, Object resultContainer) throws ResultsPresentationException	{
		if (currentPlotData.getPlotStyle() == PlotStyle.BAR)	{
			JFreeCategoryDataset dataset = (JFreeCategoryDataset)resultContainer;
	
			MultiDimensionalArray.DataCell dataCell = lineData.getDataCell(fixedXCoordinates);
	
			String seriesLabel = makeSeriesLabel(lineData, MultiDimensionalArray.FIRST_DIMENSION, xValue, dataCell);
			switch (aggregationType)	{
			default:
				NumericCharacteristic measureValues = (dataCell==null) ? null : dataCell.getCarriedValue();	//#4761. dataCell==null if a plug-in fell down
				putDistributionToCategoryDataset(dataset, measureValues, seriesLabel);
			}
		}
		else	{
			super.processValuesForFixedXValue(xValue, fixedXCoordinates, lineData, aggregationType, resultContainer);
		}
	}

	private void putDistributionToCategoryDataset(JFreeCategoryDataset dataset, NumericCharacteristic measureValues, String seriesLabel) throws ResultsPresentationException	{
		if (measureValues == null)	{	//#4689. Plot 'absence of results'.
			dataset.addValue(null, seriesLabel, "null");
			return;
		}

		NumericCharacteristic.Distribution distribution = measureValues.getDistribution();
		Float scalingCoefficient = measureValues.getDistributionScalingCoefficient();

		for (Number value : distribution.getValues())	{
			Number numberOfOccurences = (scalingCoefficient==null) ? distribution.getNumberOfOccurences(value) :
				scalingCoefficient * distribution.getNumberOfOccurences(value);

			//dataset.addCorrectValue(numberOfOccurences, seriesLabel, value.toString());
			if (value instanceof Integer)	{
				dataset.addCorrectValue(numberOfOccurences, seriesLabel, value.intValue());
			}
			else if (value instanceof Float)	{
				dataset.addCorrectValue(numberOfOccurences, seriesLabel, value.floatValue());
			}
			else	{
				dataset.addCorrectValue(numberOfOccurences, seriesLabel, value.doubleValue());
			}
		}
	}


	@Override
	protected XYSeries getXYSeries(NumericCharacteristic measureValues, String seriesLabel, StatisticsAggregation aggregationType) throws ResultsPresentationException	{
		return getDistributionXYSeries(measureValues, seriesLabel);
	}


	private void tweakBarPlot(CategoryPlot plot)	{
		BarRenderer barRenderer = (BarRenderer) plot.getRenderer();

		switch (currentPlotData.getAxesScale())	{	//Change range axis.
		case Y_LOG10:
		case XY_LOG10:
			ValueAxis yAxis = new JFreeLogarithmic10Axis(Y_AXIS_LABEL);
			yAxis.setMinorTickMarksVisible(true);

			barRenderer.setBase(1.0);

			plot.setRangeAxis(yAxis);
			break;
		}

		plot.setBackgroundPaint(Color.white);
		plot.setDomainGridlinePaint(Color.gray);
		plot.setRangeGridlinePaint(Color.gray);

		//Use textures for paint.
		DrawingSupplier drawingSupplier = texturesAdapter.getDrawingSupplier();
		plot.setDrawingSupplier(drawingSupplier);

		barRenderer.setBarPainter( new StandardBarPainter() );
		barRenderer.setGradientPaintTransformer(null);

		barRenderer.setBaseOutlinePaint(Color.black); // set bar outline
		barRenderer.setDrawBarOutline(true);

		tweakLegend(plot);
	}


	@Override
	protected void defineAxesLabels(SingleTypeBigChart plotData)	{
		X_AXIS_LABEL = plotData.getMeasureValuesName();
		Y_AXIS_LABEL = addCoefficientToLabel(LanguagesConfiguration.getNetBloxLabel(KEY_NUMBER_OF_OCCURENCES), plotData);
	}
}
