package ru.ispras.modis.NetBlox.numericResultsPresentation;

import java.awt.BasicStroke;
import java.awt.Color;
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
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.DrawingSupplier;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.category.CategoryDataset;
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
import ru.ispras.modis.NetBlox.scenario.DescriptionDataArrangement.BoxAndWhiskersStyle;
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
	private static final String KEY_SOME_CASES = "inSomeCases";


	public SingleValuesPlotter(ScenarioTask scenarioTask) {
		super(scenarioTask);
	}


	public void drawPlot(SingleTypeBigChart plotData) throws ResultsPresentationException	{
		currentPlotData = plotData;

		JFreeChart chart = null;
		if (currentPlotData.getPlotStyle() == PlotStyle.BAR)	{
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

		currentPlotData = null;
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
			if (!lineData.hasDataToBeAveraged())	{	//Then deal with the lines that do not require averaging.
				processAlongXAxis(lineData, plotData.getStatisticsAggregationType(), seriesCollection);
			}
		}

		return datasetPair;
	}


	@Override
	protected void processForRange(int dimension, RangeOfValues<?> dimensionCoordinatesValues, MultiDimensionalArray lineData,
			StatisticsAggregation aggregationType, Object resultContainer) throws ResultsPresentationException	{
		NumericCharacteristic.Type valuesType = lineData.getContainedValuesType();
		if (valuesType == null  ||  valuesType == NumericCharacteristic.Type.SINGLE_VALUE)	{
			processForRangeSingleValueMeasures(dimension, dimensionCoordinatesValues, lineData, resultContainer);
		}
		else if (valuesType == NumericCharacteristic.Type.FUNCTION)	{
			processForRangeFunctionValueMeasures(dimension, dimensionCoordinatesValues, lineData, resultContainer);
		}
	}

	private void processForRangeSingleValueMeasures(int dimension, RangeOfValues<?> dimensionCoordinatesValues, MultiDimensionalArray lineData,
			Object resultContainer) throws ResultsPresentationException	{
		String seriesLabel = makeSeriesLabel(lineData, dimension, dimensionCoordinatesValues);
		JFreeXYSeries series = new JFreeXYSeries(seriesLabel, currentPlotData.getAxesScale());
		boolean isDataToBeAveraged = lineData.hasDataToBeAveraged();

		CoordinateVector<Object> fixedCoordinates = new CoordinateVector<Object>(dimension);
		for (Object coordinateValue : dimensionCoordinatesValues)	{
			fixedCoordinates.set(dimension, coordinateValue);

			if (currentPlotData.getPlotStyle() == PlotStyle.BAR)	{
				MultiDimensionalArray.DataCell dataCell = lineData.getDataCell(fixedCoordinates);
				NumericCharacteristic carriedCharacteristic = (dataCell==null) ? null : dataCell.getCarriedValue();
				Float value = (carriedCharacteristic==null)?null:carriedCharacteristic.getValue();

				((JFreeCategoryDataset)resultContainer).addCorrectValue(value, seriesLabel, (Comparable<?>)coordinateValue);
			}
			else if (isDataToBeAveraged)	{	//There's what to average.
				XYDatasetPair datasetPair = (XYDatasetPair)resultContainer;

				Collection<MultiDimensionalArray.DataCell> toBeAveraged = lineData.getMultipleValues(fixedCoordinates);
				BoxAndWhiskerItem boxAndWhiskerItem = makeBoxAndWhiskerItem(toBeAveraged);

				datasetPair.boxAndWhiskersDataset.add(seriesLabel, (Number) coordinateValue, tuneBoxItem(boxAndWhiskerItem));
				series.addCorrect((Number) coordinateValue, boxAndWhiskerItem.getMean());
			}
			else	{	//There's nothing to be averaged, we're dealing with more or less singles.
				MultiDimensionalArray.DataCell dataCell = lineData.getDataCell(fixedCoordinates);
				NumericCharacteristic characteristic = (dataCell==null) ? null : dataCell.getCarriedValue();
				series.addCorrect((Number) coordinateValue, (characteristic==null)?null:characteristic.getValue());
			}
		}

		if (currentPlotData.getPlotStyle() == PlotStyle.BAR)	{
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

			MultiDimensionalArray.DataCell initialDataCell = null;
			Collection<MultiDimensionalArray.DataCell> toBeAveraged = null;
			NumericCharacteristic carriedCharacteristic = null;
			if (lineData.hasDataToBeAveraged())	{
				toBeAveraged = lineData.getMultipleValues(fixedCoordinates);
				initialDataCell = toBeAveraged.iterator().next();
			}
			else	{
				initialDataCell = lineData.getDataCell(fixedCoordinates);	//#4761: data cell can be NULL
				carriedCharacteristic = (initialDataCell==null) ? null : initialDataCell.getCarriedValue();
			}

			String seriesLabel = makeSeriesLabel(lineData, dimension, coordinateValue, initialDataCell);
			JFreeXYSeries series = new JFreeXYSeries(seriesLabel, currentPlotData.getAxesScale());

			if (currentPlotData.getPlotStyle() == PlotStyle.BAR)	{
				if (carriedCharacteristic != null)	{	//#4689. Can be NULL when the results are absent.
					Map<Double, Double> function = carriedCharacteristic.getFunction();
					for (Map.Entry<Double, Double> functionEntry : function.entrySet())	{
						((JFreeCategoryDataset)resultContainer).addCorrectValue(functionEntry.getValue(), seriesLabel, functionEntry.getKey());
					}
				}
				else	{
					((JFreeCategoryDataset)resultContainer).addValue(null, seriesLabel, "");	//#4689. Plot 'absence of data'.
				}
			}
			else if (toBeAveraged != null)	{	//There's what to average.
				makeBoxAndWhiskerFunction(toBeAveraged, ((XYDatasetPair)resultContainer).boxAndWhiskersDataset, seriesLabel, series);
				((XYDatasetPair)resultContainer).seriesDataset.addSeries(series);
			}
			else	{	//There's nothing to be averaged, we're dealing with simple functions.
				if (carriedCharacteristic != null)	{	//#4689. Can be NULL when the results are absent.
					Map<Double, Double> function = carriedCharacteristic.getFunction();
					for (Map.Entry<Double, Double> functionEntry : function.entrySet())	{
						series.addCorrect(functionEntry.getKey(), functionEntry.getValue());
					}
				}
				((XYSeriesCollection)resultContainer).addSeries(series);
			}
		}
	}

	private String makeSeriesLabel(MultiDimensionalArray lineData, int dimension, RangeOfValues<?> dimensionCoordinatesValues)
			throws ResultsPresentationException	{
		StringBuilder builder = new StringBuilder(lineData.getLabel());

		CoordinateVector<Object> fixedCoordinates = new CoordinateVector<Object>(dimension);
		for (Object coordinateValue : dimensionCoordinatesValues)	{	// #4689. Check whether it is necessary to add 'no data' mark to the series label.
			fixedCoordinates.set(dimension, coordinateValue);
			MultiDimensionalArray.DataCell dataCell = lineData.getDataCell(fixedCoordinates);
			if (dataCell == null)	{	// #4761. Tell the user about plug-ins that fell down.
				builder.append(" [").append(LanguagesConfiguration.getNetBloxLabel(KEY_PLUGIN_FELL_DOWN)).append(" ").
					append(LanguagesConfiguration.getNetBloxLabel(KEY_SOME_CASES)).append("]");
				break;
			}
			NumericCharacteristic characteristic = dataCell.getCarriedValue();
			if (characteristic == null)	{
				builder.append(" [").append(LanguagesConfiguration.getNetBloxLabel(KEY_NO_DATA)).append(" ").
					append(LanguagesConfiguration.getNetBloxLabel(KEY_SOME_CASES)).append("]");
				break;
			}
		}

		if (currentPlotData.toShowGraphsData())	{
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
		if (currentPlotData.getPlotStyle() == PlotStyle.BAR)	{
			JFreeCategoryDataset dataset = (JFreeCategoryDataset)resultContainer;
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
			JFreeCategoryDataset dataset) throws ResultsPresentationException	{
		MultiDimensionalArray.DataCell dataCell = lineData.getDataCell(fixedXCoordinates);

		String seriesLabel = makeSeriesLabel(lineData, MultiDimensionalArray.FIRST_DIMENSION, xValue, dataCell);
		String stringForXValue = (xValue==null) ? "" : xValue.toString();

		NumericCharacteristic carriedCharacteristic = (dataCell==null) ? null : dataCell.getCarriedValue();
		if (carriedCharacteristic != null)	{
			switch (carriedCharacteristic.getType())	{
			case SINGLE_VALUE:
				Float value = carriedCharacteristic.getValue();
				dataset.addCorrectValue(value, seriesLabel, stringForXValue);
				break;
			case FUNCTION:
				Map<Double, Double> function = carriedCharacteristic.getFunction();
				for (Map.Entry<Double, Double> functionEntry : function.entrySet())	{
					Double argument = functionEntry.getKey();
					if (argument.equals(Math.rint(argument)))	{
						dataset.addCorrectValue(functionEntry.getValue(), seriesLabel, argument.intValue());
					}
					else	{
						dataset.addCorrectValue(functionEntry.getValue(), seriesLabel, argument);
					}
				}
				break;
			}
		}
		else	{
			dataset.addCorrectValue(null, seriesLabel, stringForXValue);
		}
	}

	private void processForFixedXValueWithAveraging(Object xValue, CoordinateVector<Object> fixedXCoordinates,
			MultiDimensionalArray lineData, XYDatasetPair datasetPair) throws ResultsPresentationException	{
		Collection<MultiDimensionalArray.DataCell> toBeAveraged = lineData.getMultipleValues(fixedXCoordinates);
		MultiDimensionalArray.DataCell firstDataCell = toBeAveraged.iterator().next();

		String seriesLabel = makeSeriesLabel(lineData, MultiDimensionalArray.FIRST_DIMENSION, xValue, firstDataCell);
		JFreeXYSeries series = new JFreeXYSeries(seriesLabel, currentPlotData.getAxesScale());

		NumericCharacteristic firstCellCharacteristic = (firstDataCell==null) ? null : firstDataCell.getCarriedValue();
		if (firstCellCharacteristic != null)	{
			switch (firstCellCharacteristic.getType())	{
			case SINGLE_VALUE:
				BoxAndWhiskerItem boxAndWhiskerItem = makeBoxAndWhiskerItem(toBeAveraged);
				Number x = (xValue==null) ? 0 : (Number)xValue;
				series.addCorrect(x, boxAndWhiskerItem.getMean());
				datasetPair.boxAndWhiskersDataset.add(seriesLabel, x, tuneBoxItem(boxAndWhiskerItem));
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
		JFreeXYSeries series = new JFreeXYSeries(seriesLabel, currentPlotData.getAxesScale());

		if (dataCell != null)	{
			NumericCharacteristic measureContainer = dataCell.getCarriedValue();
			if (measureContainer != null)	{
				switch (measureContainer.getType())	{
				case SINGLE_VALUE:
					Number x = (xValue==null) ? 0 : (Number)xValue;
					series.addCorrect(x, measureContainer.getValue());
					break;
				case FUNCTION:
					Map<Double, Double> function = measureContainer.getFunction();
					for (Map.Entry<Double, Double> functionEntry : function.entrySet())	{
						series.addCorrect(functionEntry.getKey(), functionEntry.getValue());
					}
					break;
				}
			}
		}

		resultCollection.addSeries(series);
	}


	private BoxAndWhiskerItem makeBoxAndWhiskerItem(Collection<MultiDimensionalArray.DataCell> data)	{
		ArrayList<Float> values = new ArrayList<Float>(data.size());
		for (MultiDimensionalArray.DataCell dataCell : data)	{
			NumericCharacteristic carriedCharacteristic = (dataCell==null) ? null : dataCell.getCarriedValue();
			if (carriedCharacteristic != null)	{
				values.add(carriedCharacteristic.getValue());
			}
			else	{
				values.clear();
				values.add(null);
				break;
			}
		}
		BoxAndWhiskerItem item = BoxAndWhiskerCalculator.calculateBoxAndWhiskerStatistics(values);
		return item;
	}

	private void makeBoxAndWhiskerFunction(Collection<MultiDimensionalArray.DataCell> data, JFreeBoxAndWhiskerXYDataset boxAndWhiskersDataset,
			String seriesLabel, JFreeXYSeries series) throws ResultsPresentationException	{
		Map<Double, List<Double>> listsOfFunctionValuesForArguments = new HashMap<Double, List<Double>>();
		for (MultiDimensionalArray.DataCell dataCell : data)	{
			NumericCharacteristic carriedCharacteristic = (dataCell==null) ? null : dataCell.getCarriedValue();
			if (carriedCharacteristic == null)	{	//#4689, #4761. This situation must be impossible, but still let's make a check.
				for (List<Double> valuesForAnArgument : listsOfFunctionValuesForArguments.values())	{
					valuesForAnArgument.clear();
				}
				break;
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
			BoxAndWhiskerItem noBoxBWItem = tuneBoxItem(boxWhiskerItem);

			series.addCorrect(argument, boxWhiskerItem.getMean());
			boxAndWhiskersDataset.add(seriesLabel, argument, noBoxBWItem);
		}
	}

	private BoxAndWhiskerItem tuneBoxItem(BoxAndWhiskerItem item)	{
		if (currentPlotData.getBoxAndWhiskersStyle() == BoxAndWhiskersStyle.NO_BOX)	{
			return new BoxAndWhiskerItem(null /*item.getMean()*/, item.getMedian(), item.getMedian(), item.getMedian(),
					item.getMinRegularValue(), item.getMaxRegularValue(), item.getMinOutlier(), item.getMaxOutlier(), item.getOutliers());
		}

		Number minimum = (item.getMinOutlier()!=null) ? item.getMinOutlier() : item.getMinRegularValue();
		Number maximum = (item.getMaxOutlier()!=null) ? item.getMaxOutlier() : item.getMaxRegularValue();
		List<Number> outliers = item.getOutliers();
		if (outliers != null  &&  !outliers.isEmpty())	{
			for (Number outlier : outliers)	{
				minimum = (minimum.doubleValue()<outlier.doubleValue()) ? minimum : outlier;
				maximum = (maximum.doubleValue()>outlier.doubleValue()) ? maximum : outlier;
			}
		}
		return new BoxAndWhiskerItem(item.getMean(), item.getMedian(), item.getQ1(), item.getQ3(),
				minimum, maximum, minimum, maximum, item.getOutliers());
	}


	private JFreeChart makeChart(SingleTypeBigChart plotData, CategoryDataset categoryDataset)	{
		defineAxesLabels(plotData);

		String plotTitle = null;	//plotData.getMeasureName()
		JFreeChart chart = ChartFactory.createBarChart(plotTitle, X_AXIS_LABEL, Y_AXIS_LABEL, categoryDataset,
				PlotOrientation.VERTICAL, plotData.showLegend(), false, false);

		tweakBarPlot(chart.getCategoryPlot(), plotData);

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
		//plot.setDataset(1, plot.getDataset());
		//plot.setRenderer(1, plot.getRenderer());
		plot.setDataset(1/*0*/, boxWhiskerAndSeriesPair.seriesDataset);
		plot.setRenderer(1/*0*/, seriesCollectionRenderer);

		//Make excessive legend data invisible.
		XYItemRenderer boxWhiskerRenderer = plot.getRenderer(/*1*/);
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
			if (currentPlotData.getBoxAndWhiskersStyle() == BoxAndWhiskersStyle.NO_BOX)	{
				Shape shape = augmentShapeSize(drawingSupplier.getNextShape());
				seriesCollectionRenderer.setSeriesShape(seriesIndex, shape);
			}

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


	private void tweakBarPlot(CategoryPlot plot, SingleTypeBigChart plotData)	{
		switch (plotData.getAxesScale())	{
		case Y_LOG10:
		case XY_LOG10:
			ValueAxis yAxis = new JFreeLogarithmic10Axis(Y_AXIS_LABEL);
			yAxis.setMinorTickMarksVisible(true);

			BarRenderer barRenderer = (BarRenderer) plot.getRenderer();
			barRenderer.setBase(1.0);

			plot.setRangeAxis(yAxis);
			break;
		}

		plot.setBackgroundPaint(Color.white);
		plot.setDomainGridlinePaint(Color.gray);
		plot.setRangeGridlinePaint(Color.gray);
	}

	@Override
	protected void defineAxesLabels(SingleTypeBigChart plotData) {
		X_AXIS_LABEL = plotData.get1stDimensionName();
		Y_AXIS_LABEL = addCoefficientToLabel(plotData.getMeasureValuesName(), plotData);
	}

	@Override
	protected XYSeries getXYSeries(NumericCharacteristic measureValues, String seriesLabel, StatisticsAggregation aggregationType) {
		throw new UnsupportedOperationException();
	}
}
