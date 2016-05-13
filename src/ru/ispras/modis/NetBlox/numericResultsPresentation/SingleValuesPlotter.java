package ru.ispras.modis.NetBlox.numericResultsPresentation;

import java.awt.BasicStroke;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.DrawingSupplier;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.statistics.BoxAndWhiskerCalculator;
import org.jfree.data.statistics.BoxAndWhiskerItem;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import ru.ispras.modis.NetBlox.JFreeChartUtils;
import ru.ispras.modis.NetBlox.configuration.LanguagesConfiguration;
import ru.ispras.modis.NetBlox.dataManagement.GraphOnDriveHandler;
import ru.ispras.modis.NetBlox.dataStructures.NumericCharacteristic;
import ru.ispras.modis.NetBlox.dataStructures.internalMechs.CoordinateVector;
import ru.ispras.modis.NetBlox.dataStructures.internalMechs.MultiDimensionalArray;
import ru.ispras.modis.NetBlox.dataStructures.internalMechs.SingleTypeBigChart;
import ru.ispras.modis.NetBlox.exceptions.ResultsPresentationException;
import ru.ispras.modis.NetBlox.scenario.DescriptionDataArrangement.PlotStyle;
import ru.ispras.modis.NetBlox.scenario.DescriptionDataArrangement.StatisticsAggregation;
import ru.ispras.modis.NetBlox.scenario.RangeOfValues;
import ru.ispras.modis.NetBlox.scenario.ScenarioTask;

/**
 * Class for drawing plots for single value measures (like NMI, average F1-score, diameter, etc.).
 *
 * @author ilya
 */
public class SingleValuesPlotter extends JFreeChartPlotter {
	class XYDatasetPair	{
		JFreeBoxAndWhiskerXYDataset boxAndWhiskersDataset;
		XYSeriesCollection seriesDataset;

		public XYDatasetPair(JFreeBoxAndWhiskerXYDataset boxAndWhiskersDataset, XYSeriesCollection seriesDataset)	{
			this.boxAndWhiskersDataset = boxAndWhiskersDataset;
			this.seriesDataset = seriesDataset;
		}
	}

	private static final int MARKERS_AUGMENTATION_COEFFICIENT = 2;

	private static final String KEY_GRAPHS = "graphs";

	private PlotStyle currentPlotStyle;


	public SingleValuesPlotter(ScenarioTask scenarioTask) {
		super(scenarioTask);
	}


	public void drawPlot(SingleTypeBigChart plotData) throws ResultsPresentationException	{
		showGraphsData = plotData.toShowGraphsData();
		currentPlotStyle = plotData.getPlotStyle();

		JFreeChart chart = null;
		if (currentPlotStyle == PlotStyle.BAR)	{
			CategoryDataset categoryDataset = prepareCategoryDataset(plotData);
			chart = makeChart(plotData, categoryDataset);
		}
		else	{
			if (plotData.doAveraging())	{
				XYDatasetPair boxWhiskerAndSeriesPair = prepareDoubleDataset(plotData);
				chart = makeChart(plotData, boxWhiskerAndSeriesPair);
			}
			else	{
				XYSeriesCollection seriesCollectionForPlotting = prepareSeriesCollection(plotData);
				chart = makeChart(plotData, seriesCollectionForPlotting);
			}
		}

		JFreeChartUtils.exportToPNG(makePNGPlotFilePathname(plotData), chart, plotData.getPlotWidth(), plotData.getPlotHeight());

		currentPlotStyle = null;
		showGraphsData = null;
	}


	private XYSeriesCollection prepareSeriesCollection(SingleTypeBigChart plotData) throws ResultsPresentationException	{
		XYSeriesCollection seriesCollection = new XYSeriesCollection();

		for (MultiDimensionalArray lineData : plotData)	{
			processAlongXAxis(lineData, plotData.getStatisticsAggregationType(), seriesCollection);
		}

		return seriesCollection;
	}

	private XYDatasetPair prepareDoubleDataset(SingleTypeBigChart plotData) throws ResultsPresentationException	{
		JFreeBoxAndWhiskerXYDataset boxAndWhiskersDataset = new JFreeBoxAndWhiskerXYDataset();
		XYSeriesCollection seriesCollection = new XYSeriesCollection();
		XYDatasetPair datasetPair = new XYDatasetPair(boxAndWhiskersDataset, seriesCollection);

		for (MultiDimensionalArray lineData : plotData)	{
			if (lineData.hasDataToBeAveraged())	{	//First deal with all lines that require averaging in their description.
				processAlongXAxis(lineData, plotData.getStatisticsAggregationType(), datasetPair);
			}
		}
		for (MultiDimensionalArray lineData : plotData)	{
			if (!lineData.hasDataToBeAveraged())	{	//Then deal with the lines that don not require averaging.
				processAlongXAxis(lineData, plotData.getStatisticsAggregationType(), seriesCollection);
			}
		}

		return datasetPair;
	}


	@Override
	protected void processForRange(int dimension, RangeOfValues<?> dimensionCoordinatesValues, MultiDimensionalArray lineData,
			StatisticsAggregation aggregationType, Object resultContainer) throws ResultsPresentationException	{
		switch (lineData.getContainedValuesType())	{
		case SINGLE_VALUE:
			processForRangeSingleValueMeasures(dimension, dimensionCoordinatesValues, lineData, resultContainer);
			break;

		case FUNCTION:
			processForRangeFunctionValueMeasures(dimension, dimensionCoordinatesValues, lineData, resultContainer);
			break;
		}
	}

	private void processForRangeSingleValueMeasures(int dimension, RangeOfValues<?> dimensionCoordinatesValues, MultiDimensionalArray lineData,
			Object resultContainer) throws ResultsPresentationException	{
		String seriesLabel = makeSeriesLabel(lineData);
		XYSeries series = new XYSeries(seriesLabel);
		boolean isDataToBeAveraged = lineData.hasDataToBeAveraged();

		CoordinateVector<Object> fixedCoordinates = new CoordinateVector<Object>(dimension);
		for (Object coordinateValue : dimensionCoordinatesValues)	{
			fixedCoordinates.set(dimension, coordinateValue);

			if (currentPlotStyle == PlotStyle.BAR)	{
				MultiDimensionalArray.DataCell dataCell = lineData.getDataCell(fixedCoordinates);
				NumericCharacteristic carriedCharacteristic = dataCell.getCarriedValue();
				Float value = (carriedCharacteristic==null)?null:carriedCharacteristic.getValue();

				((DefaultCategoryDataset)resultContainer).addValue(value, seriesLabel, (Comparable<?>)coordinateValue);
			}
			else if (isDataToBeAveraged)	{	//There's what to average.
				XYDatasetPair datasetPair = (XYDatasetPair)resultContainer;

				Collection<MultiDimensionalArray.DataCell> toBeAveraged = lineData.getMultipleValues(fixedCoordinates);
				BoxAndWhiskerItem boxAndWhiskerItem = makeBoxAndWhiskerItem(toBeAveraged);

				datasetPair.boxAndWhiskersDataset.add(seriesLabel, (Number) coordinateValue, makeNoBoxItem(boxAndWhiskerItem));
				series.add((Number) coordinateValue, boxAndWhiskerItem.getMean());
			}
			else	{	//There's nothing to be averaged, we're dealing with more or less singles.
				MultiDimensionalArray.DataCell dataCell = lineData.getDataCell(fixedCoordinates);
				NumericCharacteristic characteristic = dataCell.getCarriedValue();
				series.add((Number) coordinateValue, (characteristic==null)?null:characteristic.getValue());
			}
		}

		if (currentPlotStyle == PlotStyle.BAR)	{
			return;
		}
		if (isDataToBeAveraged)	{
			((XYDatasetPair)resultContainer).seriesDataset.addSeries(series);
		}
		else	{
			((XYSeriesCollection)resultContainer).addSeries(series);
		}
	}

	private void processForRangeFunctionValueMeasures(int dimension, RangeOfValues<?> dimensionCoordinatesValues, MultiDimensionalArray lineData,
			Object resultContainer) throws ResultsPresentationException	{
		CoordinateVector<Object> fixedCoordinates = new CoordinateVector<Object>(dimension);
		for (Object coordinateValue : dimensionCoordinatesValues)	{
			fixedCoordinates.set(dimension, coordinateValue);
			Collection<MultiDimensionalArray.DataCell> toBeAveraged = lineData.getMultipleValues(fixedCoordinates);

			MultiDimensionalArray.DataCell firstDataCell = toBeAveraged.iterator().next();
			String seriesLabel = makeSeriesLabel(lineData, dimension, coordinateValue, firstDataCell);
			XYSeries series = new XYSeries(seriesLabel);

			if (currentPlotStyle == PlotStyle.BAR)	{
				MultiDimensionalArray.DataCell dataCell = lineData.getDataCell(fixedCoordinates);
				NumericCharacteristic carriedCharacteristic = dataCell.getCarriedValue();
				if (carriedCharacteristic != null)	{
					Map<Double, Double> function = carriedCharacteristic.getFunction();
					for (Map.Entry<Double, Double> functionEntry : function.entrySet())	{
						((DefaultCategoryDataset)resultContainer).addValue(functionEntry.getValue(), seriesLabel, functionEntry.getKey());
					}
				}
				else	{
					((DefaultCategoryDataset)resultContainer).addValue(null, seriesLabel, "");	//#4689. Plot 'absence of data'.
				}
			}
			else if (toBeAveraged.size() > 1)	{	//There's what to average.
				makeBoxAndWhiskerFunction(toBeAveraged, ((XYDatasetPair)resultContainer).boxAndWhiskersDataset, seriesLabel, series);
				((XYDatasetPair)resultContainer).seriesDataset.addSeries(series);
			}
			else	{	//There's nothing to be averaged, we're dealing with simple functions.
				NumericCharacteristic carriedCharacteristic = firstDataCell.getCarriedValue();
				if (carriedCharacteristic != null)	{
					Map<Double, Double> function = carriedCharacteristic.getFunction();
					for (Map.Entry<Double, Double> functionEntry : function.entrySet())	{
						series.add(functionEntry.getKey(), functionEntry.getValue());
					}
				}
				((XYSeriesCollection)resultContainer).addSeries(series);
			}
		}
	}

	private String makeSeriesLabel(MultiDimensionalArray lineData)	{
		StringBuilder builder = new StringBuilder(lineData.getLabel());

		if (showGraphsData)	{
			builder.append("; ").append(LanguagesConfiguration.getNetBloxLabel(KEY_GRAPHS)).append(":");//.append(dataCell.getGraphParameters().getShortLabel());

			Set<GraphOnDriveHandler> participatingGraphsHandlers = lineData.getParticipatingGraphsHandlers();
			for (GraphOnDriveHandler graphHandler : participatingGraphsHandlers)	{
				builder.append(" ").append(graphHandler.getGraphParameters().getShortLabel());
			}
		}

		builder.append(". | ");
		return builder.toString();
	}


	@Override
	protected void processValuesForFixedXValue(Object xValue, CoordinateVector<Object> fixedXCoordinates,
			MultiDimensionalArray lineData, StatisticsAggregation aggregationType, Object resultContainer) throws ResultsPresentationException	{
		if (currentPlotStyle == PlotStyle.BAR)	{
			DefaultCategoryDataset dataset = (DefaultCategoryDataset)resultContainer;
			processForFixedXValue(xValue, fixedXCoordinates, lineData, dataset);
		}
		else	{
			if (lineData.hasDataToBeAveraged())	{
				XYDatasetPair datasetPair = (XYDatasetPair)resultContainer;
				processForFixedXValueWithAveraging(xValue, fixedXCoordinates, lineData, datasetPair);
			}
			else	{
				processForFixedXValue(xValue, fixedXCoordinates, lineData, (XYSeriesCollection)resultContainer);
			}
		}
	}

	private void processForFixedXValue(Object xValue, CoordinateVector<Object> fixedXCoordinates, MultiDimensionalArray lineData,
			DefaultCategoryDataset dataset) throws ResultsPresentationException	{
		MultiDimensionalArray.DataCell dataCell = lineData.getDataCell(fixedXCoordinates);

		String seriesLabel = makeSeriesLabel(lineData, MultiDimensionalArray.FIRST_DIMENSION, xValue, dataCell);

		NumericCharacteristic carriedCharacteristic = dataCell.getCarriedValue();
		Float value = (carriedCharacteristic==null)?null:carriedCharacteristic.getValue();

		dataset.addValue(value, seriesLabel, (xValue==null)?"":xValue.toString());
	}

	private void processForFixedXValueWithAveraging(Object xValue, CoordinateVector<Object> fixedXCoordinates,
			MultiDimensionalArray lineData, XYDatasetPair datasetPair) throws ResultsPresentationException	{
		Collection<MultiDimensionalArray.DataCell> toBeAveraged = lineData.getMultipleValues(fixedXCoordinates);
		MultiDimensionalArray.DataCell firstDataCell = toBeAveraged.iterator().next();

		String seriesLabel = makeSeriesLabel(lineData, MultiDimensionalArray.FIRST_DIMENSION, xValue, firstDataCell);
		XYSeries series = new XYSeries(seriesLabel);

		NumericCharacteristic firstCellCharacteristic = firstDataCell.getCarriedValue();
		if (firstCellCharacteristic != null)	{
			switch (firstCellCharacteristic.getType())	{
			case SINGLE_VALUE:
				BoxAndWhiskerItem boxAndWhiskerItem = makeBoxAndWhiskerItem(toBeAveraged);
				Number x = (xValue==null) ? 0 : (Number)xValue;
				series.add(x, boxAndWhiskerItem.getMean());
				datasetPair.boxAndWhiskersDataset.add(seriesLabel, x, makeNoBoxItem(boxAndWhiskerItem));
				break;
			case FUNCTION:
				makeBoxAndWhiskerFunction(toBeAveraged, datasetPair.boxAndWhiskersDataset, seriesLabel, series);
				break;
			}
		}

		datasetPair.seriesDataset.addSeries(series);
	}

	private void processForFixedXValue(Object xValue, CoordinateVector<Object> fixedXCoordinates,
			MultiDimensionalArray lineData, XYSeriesCollection resultCollection) throws ResultsPresentationException	{
		MultiDimensionalArray.DataCell dataCell = lineData.getDataCell(fixedXCoordinates);

		String seriesLabel = makeSeriesLabel(lineData, MultiDimensionalArray.FIRST_DIMENSION, xValue, dataCell);
		XYSeries series = new XYSeries(seriesLabel);

		NumericCharacteristic measureContainer = dataCell.getCarriedValue();
		if (measureContainer != null)	{
			switch (measureContainer.getType())	{
			case SINGLE_VALUE:
				Number x = (xValue==null) ? 0 : (Number)xValue;
				series.add(x, measureContainer.getValue());
				break;
			case FUNCTION:
				Map<Double, Double> function = measureContainer.getFunction();
				for (Map.Entry<Double, Double> functionEntry : function.entrySet())	{
					series.add(functionEntry.getKey(), functionEntry.getValue());
				}
				break;
			}
		}

		resultCollection.addSeries(series);
	}


	private BoxAndWhiskerItem makeBoxAndWhiskerItem(Collection<MultiDimensionalArray.DataCell> data)	{
		ArrayList<Float> values = new ArrayList<Float>(data.size());
		for (MultiDimensionalArray.DataCell dataCell : data)	{
			NumericCharacteristic carriedCharacteristic = dataCell.getCarriedValue();
			Float aValue = (carriedCharacteristic==null)?null:carriedCharacteristic.getValue();
			values.add(aValue);
		}
		BoxAndWhiskerItem item = BoxAndWhiskerCalculator.calculateBoxAndWhiskerStatistics(values);
		//return makeNoBoxItem(item);
		return item;
	}

	private void makeBoxAndWhiskerFunction(Collection<MultiDimensionalArray.DataCell> data, JFreeBoxAndWhiskerXYDataset boxAndWhiskersDataset,
			String seriesLabel, XYSeries series)	{
		Map<Double, List<Double>> listsOfFunctionValuesForArguments = new HashMap<Double, List<Double>>();
		for (MultiDimensionalArray.DataCell dataCell : data)	{
			NumericCharacteristic carriedCharacteristic = dataCell.getCarriedValue();
			if (carriedCharacteristic == null)	{
				continue;
			}

			Map<Double, Double> function = carriedCharacteristic.getFunction();
			for (Map.Entry<Double, Double> functionEntry : function.entrySet())	{
				Double argument = functionEntry.getKey();
				List<Double> valuesForArgument = listsOfFunctionValuesForArguments.get(argument);
				if (valuesForArgument == null)	{
					valuesForArgument = new ArrayList<Double>(data.size());
				}
				valuesForArgument.add(functionEntry.getValue());
				listsOfFunctionValuesForArguments.put(argument, valuesForArgument);
			}
		}

		for (Map.Entry<Double, List<Double>> valuesForArgumentEntry : listsOfFunctionValuesForArguments.entrySet())	{
			Double argument = valuesForArgumentEntry.getKey();

			BoxAndWhiskerItem boxWhiskerItem = BoxAndWhiskerCalculator.calculateBoxAndWhiskerStatistics(valuesForArgumentEntry.getValue());
			BoxAndWhiskerItem noBoxBWItem = makeNoBoxItem(boxWhiskerItem);

			series.add(argument, boxWhiskerItem.getMean());
			boxAndWhiskersDataset.add(seriesLabel, argument, noBoxBWItem);
		}
	}

	private BoxAndWhiskerItem makeNoBoxItem(BoxAndWhiskerItem item)	{
		return new BoxAndWhiskerItem(null /*item.getMean()*/, item.getMedian(), item.getMedian(), item.getMedian(),
				item.getMinRegularValue(), item.getMaxRegularValue(), item.getMinOutlier(), item.getMaxOutlier(), item.getOutliers());
	}


	private JFreeChart makeChart(SingleTypeBigChart plotData, CategoryDataset categoryDataset)	{
		defineAxesLabels(plotData);

		String plotTitle = null;	//plotData.getMeasureName()
		JFreeChart chart = ChartFactory.createBarChart(plotTitle, X_AXIS_LABEL, Y_AXIS_LABEL, categoryDataset,
				PlotOrientation.VERTICAL, plotData.showLegend(), false, false);

		addSubtitles(chart, plotData);

		return chart;
	}

	private JFreeChart makeChart(SingleTypeBigChart plotData, XYDatasetPair boxWhiskerAndSeriesPair)	{
		defineAxesLabels(plotData);

		JFreeChart chart = makeHybridChart(plotData, boxWhiskerAndSeriesPair);

		tweakPlot(chart.getXYPlot(), plotData);

		addSubtitles(chart, plotData);

		return chart;
	}

	private JFreeChart makeHybridChart(SingleTypeBigChart plotData, XYDatasetPair boxWhiskerAndSeriesPair)	{
		//Create a box-and-whisker JFreeChart.
		String plotTitle = null;	//plotData.getMeasureName()
		JFreeChart chart = ChartFactory.createBoxAndWhiskerChart(plotTitle, X_AXIS_LABEL, Y_AXIS_LABEL,
				boxWhiskerAndSeriesPair.boxAndWhiskersDataset, plotData.showLegend());

		XYPlot plot = chart.getXYPlot();

		//Change the domain axis to be NumberAxis so that the whole range of values would be available already now.
		ValueAxis xAxis = new NumberAxis(X_AXIS_LABEL);
		plot.setDomainAxis(xAxis);

		//Add to the created chart the data from XYSeriesCollection.
		StandardXYItemRenderer seriesCollectionRenderer = new StandardXYItemRenderer();
		plot.setDataset(1, boxWhiskerAndSeriesPair.seriesDataset);
		plot.setRenderer(1, seriesCollectionRenderer);

		//Make excessive legend data invisible.
		XYItemRenderer boxWhiskerRenderer = plot.getRenderer();
		boxWhiskerRenderer.setBaseSeriesVisibleInLegend(false);

		//Choose new lines width.
		float lineWidth = LINES_THICKENING_COEFFICIENT;
		if (seriesCollectionRenderer.getBaseStroke() instanceof BasicStroke)	{
			BasicStroke basicStroke = (BasicStroke)seriesCollectionRenderer.getBaseStroke();
			lineWidth *= basicStroke.getLineWidth();
		}

		//Make box-and-whisker elements and corresponding lines to be drawn in same colour.
		//Make box-and-whisker elements lines wider.
		DrawingSupplier drawingSupplier = plot.getDrawingSupplier();
		for (int seriesIndex=0 ; seriesIndex<boxWhiskerAndSeriesPair.boxAndWhiskersDataset.getSeriesCount() ; seriesIndex++)	{
			Paint paint = drawingSupplier.getNextPaint();
			boxWhiskerRenderer.setSeriesPaint(seriesIndex, paint);
			seriesCollectionRenderer.setSeriesPaint(seriesIndex, paint);

			boxWhiskerRenderer.setSeriesStroke(seriesIndex, new BasicStroke(lineWidth));
		}

		//Make larger point markers for lines. Make wider lines.
		for (int seriesIndex=0 ; seriesIndex<boxWhiskerAndSeriesPair.seriesDataset.getSeriesCount() ; seriesIndex++)	{
			Shape shape = augmentShapeSize(drawingSupplier.getNextShape());
			seriesCollectionRenderer.setSeriesShape(seriesIndex, shape);

			seriesCollectionRenderer.setSeriesStroke(seriesIndex, new BasicStroke(lineWidth));
		}
		seriesCollectionRenderer.setBaseShapesVisible(true);

		return chart;
	}

	private Shape augmentShapeSize(Shape shape)	{
		double width;
		double height;
		double halfCoefficient = 0.5*MARKERS_AUGMENTATION_COEFFICIENT;

		Shape result = shape;
		if (shape instanceof Rectangle2D.Double)	{
			Rectangle2D.Double rectangle = (Rectangle2D.Double) shape;
			width = rectangle.getWidth();
			height = rectangle.getHeight();
			result = new Rectangle2D.Double(-width*halfCoefficient, -height*halfCoefficient,
					width*MARKERS_AUGMENTATION_COEFFICIENT, height*MARKERS_AUGMENTATION_COEFFICIENT);
		}
		else if (shape instanceof Ellipse2D.Double)	{
			Ellipse2D.Double ellipse = (Ellipse2D.Double) shape;
			width = ellipse.getWidth();
			height = ellipse.getHeight();
			result = new Ellipse2D.Double(-width*halfCoefficient, -height*halfCoefficient,
					width*MARKERS_AUGMENTATION_COEFFICIENT, height*MARKERS_AUGMENTATION_COEFFICIENT);
		}
		else if (shape instanceof Polygon)	{
			Polygon polygon = (Polygon) shape;
			int[] xPoints = augmentArrayValues(polygon.xpoints);
			int[] yPoints = augmentArrayValues(polygon.ypoints);
			result = new Polygon(xPoints, yPoints, polygon.npoints);
		}

		return result;
	}

	private int[] augmentArrayValues(int[] values)	{
		int[] result = new int[values.length];
		for (int i=0 ; i<values.length ; i++)	{
			result[i] = MARKERS_AUGMENTATION_COEFFICIENT*values[i];
		}
		return result;
	}


	@Override
	protected void defineAxesLabels(SingleTypeBigChart plotData) {
		X_AXIS_LABEL = plotData.get1stDimensionName();
		Y_AXIS_LABEL = plotData.getMeasureValuesName();
	}

	@Override
	protected XYSeries getXYSeries(NumericCharacteristic measureValues, String seriesLabel, StatisticsAggregation aggregationType) {
		throw new UnsupportedOperationException();
	}
}
