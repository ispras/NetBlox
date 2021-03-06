package ru.ispras.modis.NetBlox.graphVisualisation.visualisers;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import ru.ispras.modis.NetBlox.JFreeChartUtils;
import ru.ispras.modis.NetBlox.configuration.LanguagesConfiguration;
import ru.ispras.modis.NetBlox.dataManagement.GraphOnDriveHandler;
import ru.ispras.modis.NetBlox.dataManagement.StorageWriter;
import ru.ispras.modis.NetBlox.dataStructures.IGraph;
import ru.ispras.modis.NetBlox.dataStructures.IGroupOfNodes;
import ru.ispras.modis.NetBlox.dataStructures.IPackOfGraphStructures;
import ru.ispras.modis.NetBlox.dataStructures.ISetOfGroupsOfNodes;
import ru.ispras.modis.NetBlox.dataStructures.internalMechs.ExtendedMiningParameters;
import ru.ispras.modis.NetBlox.exceptions.VisualisationException;
import ru.ispras.modis.NetBlox.numericResultsPresentation.JFreeVerticalRectangleDrawingSupplier;
import ru.ispras.modis.NetBlox.scenario.GraphParametersSet;
import ru.ispras.modis.NetBlox.scenario.GraphVisualisationDescription;
import ru.ispras.modis.NetBlox.utils.MiningJobBase;

/**
 * Represents overlapping communities as matrix, showing the belonging of nodes
 * to pairs of communities at once (along vertical and horizontal axis).
 * 
 * @author ilya
 */
public class MatrixGraphVisualiser extends GraphVisualiser {
	public class GroupsOfNodesOnAxisPositionData	{
		private int startPosition;
		private int lengthOnAxis;
		private int lengthOfPrevious;

		public GroupsOfNodesOnAxisPositionData(int startPosition, int lengthOnAxis, int lengthOfPrevious)	{
			this.startPosition = startPosition;
			this.lengthOnAxis = lengthOnAxis;
			this.lengthOfPrevious = lengthOfPrevious;
		}

		public int getStartPosition()	{
			return startPosition;
		}

		public int getLengthOnAxis()	{
			return lengthOnAxis;
		}

		public int getPreviousCommunityLength()	{
			return lengthOfPrevious;
		}
	}



	private static final String KEY_NODES_IN_CLUSTERS = "nodesInClusters";
	private static final String KEY_CLUSTERS = "clusters";

	private static final boolean SHOW_LEGEND = false;
	private static final String TITLE = null;	//"Matrix visualisation of overlapping clusters";
	private String xAxisLabel;
	private String yAxisLabel;

	private List<GroupsOfNodesOnAxisPositionData> communitiesPositionsData;

	private static final int DRAWING_AREA_WIDTH = 1200;
	private static final int DRAWING_AREA_HEIGHT = 900;
	private static final int APPROXIMATE_LABELS_AREA_HEIGHT = 150;


	public MatrixGraphVisualiser(GraphVisualisationDescription visualisationDescription, MiningJobBase.JobBase minedDataType) {
		super(visualisationDescription, minedDataType);
	}


	@Override
	public void visualise(IGraph graph, GraphParametersSet initialGraphParameters, ExtendedMiningParameters miningParameters, Integer timeSlice) {
		//FUTURE_WORK Make a chart of connections between nodes? Auto-generated method stub.
		System.out.println("WARNING: Visualising a graph without of groups of nodes is not supproted in matrix visualisation mode.");
	}

	@Override
	public void visualise(IGraph graph, IPackOfGraphStructures<?> packOfGraphStructures, GraphParametersSet initialGraphParameters,
			ExtendedMiningParameters miningParameters, Integer timeSlice) throws VisualisationException {
		if (minedDataType != MiningJobBase.JobBase.NODES_GROUPS_SET)	{
			System.out.println("WARNING: Visualising graph substructure is not supproted in matrix visualisation mode.");
			return;
		}

		XYSeriesCollection seriesWithMatrixData = prepareSeriesCollection((ISetOfGroupsOfNodes) packOfGraphStructures);

		JFreeChart chart = makeChart(seriesWithMatrixData);

		export(chart, initialGraphParameters, miningParameters, timeSlice);
	}

	@Override
	public void visualiseGroupsOverGraph(IGraph graph, GraphOnDriveHandler initialGraphHandler, ExtendedMiningParameters extendedMiningParameters)
			throws VisualisationException {
		callVisualisationForTimeSlices(graph, initialGraphHandler, extendedMiningParameters);
	}


	private XYSeriesCollection prepareSeriesCollection(ISetOfGroupsOfNodes setOfGroupsOfNodes)	{
		communitiesPositionsData = prepareCommunitiesBordersOnMatrixRow(setOfGroupsOfNodes);

		XYSeriesCollection seriesPerCommunityCollection = new XYSeriesCollection();

		int communityNumber = 0;
		for (IGroupOfNodes groupOfNodes : setOfGroupsOfNodes)	{
			communityNumber++;
			XYSeries series = allocateNodesFromACommunity(groupOfNodes, communityNumber, setOfGroupsOfNodes, communitiesPositionsData);
			seriesPerCommunityCollection.addSeries(series);
		}

		return seriesPerCommunityCollection;
	}

	private List<GroupsOfNodesOnAxisPositionData> prepareCommunitiesBordersOnMatrixRow(ISetOfGroupsOfNodes setOfGroupsOfNodes)	{
		List<GroupsOfNodesOnAxisPositionData> startPositionsForCommunities = new ArrayList<GroupsOfNodesOnAxisPositionData>(setOfGroupsOfNodes.size());

		int position = 0;
		int lengthOfPreviousCommunity = 0;
		for (IGroupOfNodes groupOfNodes : setOfGroupsOfNodes)	{
			startPositionsForCommunities.add(
					new GroupsOfNodesOnAxisPositionData(position, groupOfNodes.size(), lengthOfPreviousCommunity));

			lengthOfPreviousCommunity = groupOfNodes.size();
			position += lengthOfPreviousCommunity;
		}

		return startPositionsForCommunities;
	}

	private XYSeries allocateNodesFromACommunity(IGroupOfNodes groupOfNodes, int communityWithNodesNumber,
			ISetOfGroupsOfNodes setOfGroupsOfNodes, List<GroupsOfNodesOnAxisPositionData> startPositionsForCommunities)	{
		XYSeries series = new XYSeries("C"+communityWithNodesNumber);

		Iterator<GroupsOfNodesOnAxisPositionData> startPositionsIterator = startPositionsForCommunities.iterator();
		for (IGroupOfNodes carryingCommunity : setOfGroupsOfNodes)	{
			int communityStartPosition = startPositionsIterator.next().getStartPosition();
			int numberAllocatedNodes = 0;

			for (IGraph.INode node : groupOfNodes)	{
				if (carryingCommunity.contains(node))	{
					int absolutePosition = communityStartPosition + numberAllocatedNodes;

					series.add(absolutePosition, communityWithNodesNumber);

					numberAllocatedNodes++;
				}
			}
		}

		return series;
	}


	private JFreeChart makeChart(XYSeriesCollection seriesWithMatrixData)	{
		xAxisLabel = LanguagesConfiguration.getNetBloxLabel(KEY_NODES_IN_CLUSTERS);
		yAxisLabel = LanguagesConfiguration.getNetBloxLabel(KEY_CLUSTERS);
		JFreeChart chart = ChartFactory.createScatterPlot(TITLE, xAxisLabel, yAxisLabel, seriesWithMatrixData,
				PlotOrientation.VERTICAL, SHOW_LEGEND, false, false);

		XYPlot plot = chart.getXYPlot();

		double pointHeight = (DRAWING_AREA_HEIGHT - APPROXIMATE_LABELS_AREA_HEIGHT)  /  seriesWithMatrixData.getSeriesCount();
		plot.setDrawingSupplier(new JFreeVerticalRectangleDrawingSupplier(pointHeight));

		markCommunitiesOnXAxis(plot);

		return chart;
	}

	private void markCommunitiesOnXAxis(XYPlot plot)	{
		NumberAxis xAxis = new JFreeSectionsBoundariesAxis(xAxisLabel, communitiesPositionsData);

		plot.setDomainAxis(xAxis);
	}


	private void export(JFreeChart chart, GraphParametersSet initialGraphParameters, ExtendedMiningParameters miningParameters, Integer timeSlice)
			throws VisualisationException	{
		String exportFilename = makePNGExportFilePathname(initialGraphParameters, miningParameters, timeSlice);

		try {
			StorageWriter.makeSureDirectoryExists(Paths.get(exportFilename).getParent());
		} catch (IOException e) {
			throw new VisualisationException(e);
		}

		JFreeChartUtils.exportToPNG(exportFilename, chart, DRAWING_AREA_WIDTH, DRAWING_AREA_HEIGHT);
	}
}
