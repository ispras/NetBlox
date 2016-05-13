package ru.ispras.modis.NetBlox.numericResultsPresentation;

import java.io.IOException;
import java.nio.file.Paths;

import ru.ispras.modis.NetBlox.configuration.SystemConfiguration;
import ru.ispras.modis.NetBlox.dataManagement.StorageWriter;
import ru.ispras.modis.NetBlox.dataStructures.internalMechs.CoordinateVector;
import ru.ispras.modis.NetBlox.dataStructures.internalMechs.MultiDimensionalArray;
import ru.ispras.modis.NetBlox.dataStructures.internalMechs.SingleTypeBigChart;
import ru.ispras.modis.NetBlox.exceptions.ResultsPresentationException;
import ru.ispras.modis.NetBlox.scenario.DescriptionDataArrangement.StatisticsAggregation;
import ru.ispras.modis.NetBlox.scenario.RangeOfValues;
import ru.ispras.modis.NetBlox.scenario.ScenarioTask;

public abstract class Plotter {
	protected static final SystemConfiguration configuration;
	private static final String OUTPUT_DIRECTORY;

	static	{
		configuration = SystemConfiguration.getInstance();

		OUTPUT_DIRECTORY = configuration.getGraphFilesRoot() + SystemConfiguration.FILES_SEPARATOR + "resultPlots" + SystemConfiguration.FILES_SEPARATOR;
		try {
			StorageWriter.makeSureDirectoryExists(Paths.get(OUTPUT_DIRECTORY));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	protected ScenarioTask scenarioTask;

	private boolean isXAxisSpecified;


	public Plotter(ScenarioTask scenarioTask)	{
		this.scenarioTask = scenarioTask;
	}


	protected static String makePDFPlotFilePathname(SingleTypeBigChart plotData) {
		StringBuilder plotFilePathBuilder = new StringBuilder(OUTPUT_DIRECTORY).
				append(plotData.getChartName()).append(".pdf");
	
		return plotFilePathBuilder.toString();
	}

	protected static String makePNGPlotFilePathname(SingleTypeBigChart plotData) {
		StringBuilder plotFilePathBuilder = new StringBuilder(OUTPUT_DIRECTORY).
				append(plotData.getChartName()).append(".png");
	
		return plotFilePathBuilder.toString();
	}


	/**
	 * Goes along all values of X coordinate (for what has been specified as X axis in graph description) and
	 * assembles the corresponding plot values for a single line description (<code>lineData</code>) into the
	 * <code>resultContainer</code>.
	 * @param lineData
	 * @param aggregationType
	 * @param resultContainer
	 * @throws ResultsPresentationException 
	 */
	protected void processAlongXAxis(MultiDimensionalArray lineData, StatisticsAggregation aggregationType, Object resultContainer) throws ResultsPresentationException	{
		int dimension = MultiDimensionalArray.FIRST_DIMENSION;
		String xAxisVariationId = lineData.getVariationIdForDimension(dimension);

		isXAxisSpecified = !xAxisVariationId.equals(RangeOfValues.NO_RANGE_ID);
		if (isXAxisSpecified)	{
			RangeOfValues<?> xRange = scenarioTask.getVariationValues(xAxisVariationId);
			processForRange(dimension, xRange, lineData, aggregationType, resultContainer);
		}
		else	{
			processValuesForFixedXValue(null, MultiDimensionalArray.DEFAULT_SINGLE_COORDINATE_VALUE, lineData, aggregationType, resultContainer);
		}
	}

	/**
	 * The range exists, now make all the operations to put characteristic/measure values along it into the container for plot drawing.
	 */
	protected void processForRange(int dimension, RangeOfValues<?> dimensionCoordinatesValues, MultiDimensionalArray lineData,
			StatisticsAggregation aggregationType, Object resultContainer) throws ResultsPresentationException	{
		CoordinateVector<Object> fixedCoordinates = new CoordinateVector<Object>(dimension);
		for (Object coordinateValue : dimensionCoordinatesValues)	{
			fixedCoordinates.set(dimension, coordinateValue);
			processValuesForFixedXValue(coordinateValue, fixedCoordinates, lineData, aggregationType, resultContainer);
		}
	}

	/**
	 * Prepares plot values from <code>lineData</code> line for one X axis coordinate value and puts them into the
	 * <code>resultContainer</code>.
	 * @param xValue
	 * @param fixedXCoordinates
	 * @param lineData
	 * @param aggregationType
	 * @param resultContainer
	 * @throws ResultsPresentationException 
	 */
	protected abstract void processValuesForFixedXValue(Object xValue, CoordinateVector<Object> fixedXCoordinates,
			MultiDimensionalArray lineData, StatisticsAggregation aggregationType, Object resultContainer) throws ResultsPresentationException;

	protected boolean isXAxisSpecified()	{
		return isXAxisSpecified;
	}
}