package ru.ispras.modis.NetBlox.numericResultsPresentation;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.DrawingSupplier;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.title.Title;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import ru.ispras.modis.NetBlox.JFreeChartUtils;
import ru.ispras.modis.NetBlox.configuration.LanguagesConfiguration;
import ru.ispras.modis.NetBlox.dataManagement.GraphOnDriveHandler;
import ru.ispras.modis.NetBlox.dataStructures.Graph;
import ru.ispras.modis.NetBlox.dataStructures.NumericCharacteristic;
import ru.ispras.modis.NetBlox.dataStructures.internalMechs.CoordinateVector;
import ru.ispras.modis.NetBlox.dataStructures.internalMechs.MultiDimensionalArray;
import ru.ispras.modis.NetBlox.dataStructures.internalMechs.SingleTypeBigChart;
import ru.ispras.modis.NetBlox.exceptions.ResultsPresentationException;
import ru.ispras.modis.NetBlox.exceptions.SourceGraphException;
import ru.ispras.modis.NetBlox.scenario.DescriptionDataArrangement.StatisticsAggregation;
import ru.ispras.modis.NetBlox.scenario.ScenarioTask;

/**
 * This plotter uses JFreeChart library to draw plots.
 * 
 * @author ilya
 */
public abstract class JFreeChartPlotter extends Plotter {
	//private static final float PLOT_FOREGROUND_ALPHA = 0.5f;//0.1f

	protected static final String KEY_NUMBER_OF_OCCURENCES = "numberOfOcurences";
	private static final String KEY_GRAPH = "graph";
	private static final String KEY_AVERAGE_DEGREE = "averageDegree";
	private static final String KEY_DISTRIBUTION = "distribution";
	private static final String KEY_AVERAGE = "average";
	private static final String KEY_STANDARD_DEVIATION = "standardDeviation";
	private static final String KEY_MEDIAN = "median";
	private static final String KEY_SAMPLE_SIZE = "sampleSize";

	protected String X_AXIS_LABEL;
	protected String Y_AXIS_LABEL;

	protected static final int LINES_THICKENING_COEFFICIENT = 2;

	protected JFreeTextures texturesAdapter;

	protected Boolean showGraphsData = null;	//keep it null when done with plotting


	public JFreeChartPlotter(ScenarioTask scenarioTask)	{
		super(scenarioTask);

		texturesAdapter = new JFreeTextures();
	}


	public void plotValuesDistributedOverCommunities(SingleTypeBigChart plotData) throws ResultsPresentationException	{
		showGraphsData = plotData.toShowGraphsData();

		XYSeriesCollection seriesCollectionForPlotting = prepareSeriesCollection(plotData);

		JFreeChart chart = makeChart(plotData, seriesCollectionForPlotting);

		JFreeChartUtils.exportToPNG(makePNGPlotFilePathname(plotData), chart, plotData.getPlotWidth(), plotData.getPlotHeight());

		showGraphsData = null;
	}


	private XYSeriesCollection prepareSeriesCollection(SingleTypeBigChart plotData) throws ResultsPresentationException	{
		List<XYSeries> allXYSeries = new LinkedList<XYSeries>();
		for (MultiDimensionalArray lineData : plotData)	{
			List<XYSeries> singleLineSeriesData = makeSeriesCollectionOutOfLine(lineData, plotData.getStatisticsAggregationType());
			allXYSeries.addAll(singleLineSeriesData);	//For now we just put them all in one big series collection.
		}

		XYSeriesCollection seriesCollectionForPlotting = new XYSeriesCollection();
		for (XYSeries series : allXYSeries)	{
			seriesCollectionForPlotting.addSeries(series);
		}

		seriesCollectionForPlotting.setAutoWidth(true);
		seriesCollectionForPlotting.setIntervalPositionFactor(0);

		return seriesCollectionForPlotting;
	}

	private List<XYSeries> makeSeriesCollectionOutOfLine(MultiDimensionalArray lineData, StatisticsAggregation aggregationType) throws ResultsPresentationException	{
		List<XYSeries> seriesCollection = new LinkedList<XYSeries>();

		processAlongXAxis(lineData, aggregationType, seriesCollection);

		return seriesCollection;
	}


	@Override
	protected void processValuesForFixedXValue(Object xValue, CoordinateVector<Object> fixedXCoordinates,
			MultiDimensionalArray lineData, StatisticsAggregation aggregationType, Object resultContainer) throws ResultsPresentationException	{
		@SuppressWarnings("unchecked")
		List<XYSeries> seriesCollection = (List<XYSeries>)resultContainer;	//XXX Check?

		MultiDimensionalArray.DataCell dataCell = lineData.getDataCell(fixedXCoordinates);

		String seriesLabel = makeSeriesLabel(lineData, MultiDimensionalArray.FIRST_DIMENSION, xValue, dataCell);

		XYSeries series = getXYSeries(dataCell.getCarriedValue(), seriesLabel, aggregationType);
		seriesCollection.add(series);
	}

	protected String makeSeriesLabel(MultiDimensionalArray lineData, int dimension, Object xValue, MultiDimensionalArray.DataCell dataCell)	{
		StringBuilder builder = new StringBuilder(lineData.getLabel());

		if (isXAxisSpecified())	{
			String dimensionLabel = lineData.getDimensionLabel(dimension);
			builder.append(", ").append(dimensionLabel).append("=").append(xValue);
		}

		if (showGraphsData)	{
			builder.append("; ").append(LanguagesConfiguration.getNetBloxLabel(KEY_GRAPH)).append(": ").
				append(dataCell.getGraphParameters().getShortLabel());
		}

		if (lineData.getContainedValuesType() == NumericCharacteristic.Type.DISTRIBUTION)	{
			NumericCharacteristic value = dataCell.getCarriedValue();
			builder.append("; ").append(LanguagesConfiguration.getNetBloxLabel(KEY_DISTRIBUTION)).
				append(": ").append(LanguagesConfiguration.getNetBloxLabel(KEY_AVERAGE)).append("=").append(value.getAverage()).
				append(",\n").append(LanguagesConfiguration.getNetBloxLabel(KEY_STANDARD_DEVIATION)).append("=").append(value.getStandardDeviation()).
				append(",").append(LanguagesConfiguration.getNetBloxLabel(KEY_MEDIAN)).append("=").append(value.getMedian()).
				append(", ").append(LanguagesConfiguration.getNetBloxLabel(KEY_SAMPLE_SIZE)).append("=").append(value.getSampleSize());
		}

		builder.append(". | ");
		return builder.toString();
	}

	protected abstract XYSeries getXYSeries(NumericCharacteristic measureValues, String seriesLabel, StatisticsAggregation aggregationType);

	protected XYSeries getDistributionXYSeries(NumericCharacteristic measureValues, String seriesLabel)	{
		XYSeries series = new XYSeries(seriesLabel);

		NumericCharacteristic.Distribution distribution = measureValues.getDistribution();
		for (Number value : distribution.getValues())	{
			Integer numberOfOccurences = distribution.getNumberOfOccurences(value);
			if (value.equals(0))	{
				value = value.doubleValue() + Double.MIN_VALUE;
			}
			series.add(value, numberOfOccurences);
		}

		return series;
	}


	protected JFreeChart makeChart(SingleTypeBigChart plotData, XYSeriesCollection seriesCollectionForPlotting)	{
		defineAxesLabels(plotData);

		JFreeChart chart = makeBasicChart(plotData, seriesCollectionForPlotting);

		tweakPlot(chart.getXYPlot(), plotData);

		addSubtitles(chart, plotData);

		return chart;
	}

	protected abstract void defineAxesLabels(SingleTypeBigChart plotData);

	private JFreeChart makeBasicChart(SingleTypeBigChart plotData, XYSeriesCollection seriesCollectionForPlotting)	{
		JFreeChart chart = null;
		String plotTitle = null;	//plotData.getMeasureName()
		switch (plotData.getPlotStyle())	{
		case LINE:
			chart = ChartFactory.createXYLineChart(plotTitle, X_AXIS_LABEL, Y_AXIS_LABEL, seriesCollectionForPlotting,
					PlotOrientation.VERTICAL, plotData.showLegend(), false, false);
			makeWiderLines(chart);
			break;
		case BAR:
			chart = ChartFactory.createXYBarChart(plotTitle, X_AXIS_LABEL, false, Y_AXIS_LABEL, seriesCollectionForPlotting,
					PlotOrientation.VERTICAL, plotData.showLegend(), false, false);
			break;
		case STEP:
			chart = ChartFactory.createXYStepChart(plotTitle, X_AXIS_LABEL, Y_AXIS_LABEL, seriesCollectionForPlotting,
					PlotOrientation.VERTICAL, plotData.showLegend(), false, false);
			makeWiderLines(chart);
			break;
		case SCATTER:
			chart = ChartFactory.createScatterPlot(plotTitle, X_AXIS_LABEL, Y_AXIS_LABEL, seriesCollectionForPlotting,
					PlotOrientation.VERTICAL, plotData.showLegend(), false, false);
			break;
		case HISTOGRAM:
			chart = ChartFactory.createHistogram(plotTitle, X_AXIS_LABEL, Y_AXIS_LABEL, seriesCollectionForPlotting,
					PlotOrientation.VERTICAL, plotData.showLegend(), false, false);
			break;
		}

		return chart;
	}

	private void makeWiderLines(JFreeChart chart)	{
		XYPlot plot = chart.getXYPlot();
		XYItemRenderer renderer = plot.getRenderer();
		if (renderer.getBaseStroke() instanceof BasicStroke)	{
			BasicStroke basicStroke = (BasicStroke)renderer.getBaseStroke();
			float lineWidth = basicStroke.getLineWidth() * LINES_THICKENING_COEFFICIENT;
			for (int seriesIndex=0 ; seriesIndex<plot.getSeriesCount() ; seriesIndex++)	{
				renderer.setSeriesStroke(seriesIndex, new BasicStroke(lineWidth));
			}
		}
	}


	protected void tweakPlot(XYPlot plot, SingleTypeBigChart plotData)	{
		changeDomainAxis(plot, plotData);
		changeRangeAxis(plot, plotData);

		plot.setBackgroundPaint(Color.white);
		plot.setDomainGridlinePaint(Color.gray);
		plot.setRangeGridlinePaint(Color.gray);

		switch (plotData.getPlotStyle())	{
		case BAR:
		case HISTOGRAM:
			//plot.setForegroundAlpha(PLOT_FOREGROUND_ALPHA);
			DrawingSupplier drawingSupplier = texturesAdapter.getDrawingSupplier();
			plot.setDrawingSupplier(drawingSupplier);

			XYBarRenderer barRenderer = (XYBarRenderer) plot.getRenderer();
			barRenderer.setBarPainter( new StandardXYBarPainter() );
			barRenderer.setGradientPaintTransformer(null);

			barRenderer.setBaseOutlinePaint(Color.black); // set bar outline
			barRenderer.setDrawBarOutline(true);

			tweakLegend(plot);
			break;
		}
	}

	private void changeDomainAxis(XYPlot plot, SingleTypeBigChart plotData)	{
		ValueAxis xAxis = null;
		switch (plotData.getAxesScale())	{
		case SIMPLE:
			xAxis = new NumberAxis(X_AXIS_LABEL);
			((NumberAxis)xAxis).setAutoRangeIncludesZero(plotData.plotIncludesZero());
			break;
		case X_LOG10:
		case XY_LOG10:
			xAxis = new JFreeLogarithmic10Axis(X_AXIS_LABEL);
			xAxis.setMinorTickMarksVisible(true);
			break;
		}

		plot.setDomainAxis(xAxis);
	}

	private void changeRangeAxis(XYPlot plot, SingleTypeBigChart plotData)	{
		if (!(this instanceof SingleValuesPlotter))	{
			switch (plotData.getStatisticsAggregationType())	{
			case BIG_INTERVALS:	//nothing special for now
			case DISTRIBUTION:
				ValueAxis yAxis = new JFreeIntegerAxis(Y_AXIS_LABEL);
				plot.setRangeAxis(yAxis);
			}
		}

		switch (plotData.getAxesScale())	{
		case Y_LOG10:
		case XY_LOG10:
			ValueAxis yAxis = new JFreeLogarithmic10Axis(Y_AXIS_LABEL);
			yAxis.setMinorTickMarksVisible(true);
			plot.setRangeAxis(yAxis);
			break;
		}
	}

	protected void tweakLegend(Plot plot)	{
		JFreeChartUtils.UntransformingGradientPaintTransformer unTransformer = new JFreeChartUtils.UntransformingGradientPaintTransformer();
		Shape legendBox = new RoundRectangle2D.Float(0,0,20,20,2,2);

		LegendItemCollection legendItems = plot.getLegendItems();
		Iterator<?> legendItemsIterator = legendItems.iterator();
		while (legendItemsIterator.hasNext())	{
			LegendItem legendItem = (LegendItem) legendItemsIterator.next();
			legendItem.setFillPaintTransformer(unTransformer);
			legendItem.setShape(legendBox);
		}

		if (plot instanceof XYPlot)	{
			((XYPlot)plot).setFixedLegendItems(legendItems);
		}
		else if (plot instanceof CategoryPlot)	{
			((CategoryPlot)plot).setFixedLegendItems(legendItems);
		}
	}


	protected void addSubtitles(JFreeChart chart, SingleTypeBigChart plotData)	{
		if (!showGraphsData)	{
			return;
		}

		for (GraphOnDriveHandler fixedGraphHandler : plotData.getParticipatingGraphsHandlers())	{
			try {
				Graph graph = fixedGraphHandler.getGraph();

				int numberOfNodes = graph.size();
				int numberOfEdges = graph.getNumberOfEdges();
				double averageDegree = graph.getAverageDegree();

				StringBuilder subtitleTextBuilder = new StringBuilder(fixedGraphHandler.getGraphParameters().getShortLabel()).
						append("(|N|=").append(numberOfNodes).
						append(", |E|=").append(numberOfEdges).
						append("), ").append(LanguagesConfiguration.getNetBloxLabel(KEY_AVERAGE_DEGREE)).append("=").append(averageDegree);

				Title subtitle = new TextTitle(subtitleTextBuilder.toString());
				chart.addSubtitle(subtitle);
			} catch (SourceGraphException e) {
				e.printStackTrace();
			}
		}
	}
}
