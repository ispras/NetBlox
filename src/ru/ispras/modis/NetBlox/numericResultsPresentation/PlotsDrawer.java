package ru.ispras.modis.NetBlox.numericResultsPresentation;

import java.util.List;

import ru.ispras.modis.NetBlox.dataStructures.NumericCharacteristic;
import ru.ispras.modis.NetBlox.dataStructures.internalMechs.ArrangedData;
import ru.ispras.modis.NetBlox.dataStructures.internalMechs.SingleTypeBigChart;
import ru.ispras.modis.NetBlox.exceptions.ResultsPresentationException;
import ru.ispras.modis.NetBlox.scenario.ScenarioTask;

public class PlotsDrawer {

	public static void drawPlots(ScenarioTask task, ArrangedData arrangedData)	{
		List<SingleTypeBigChart> differentPlots = arrangedData.getArrangedForPlots();

		SingleValuesPlotter singleValuesPlotter = new SingleValuesPlotter(task);
		ListsOfValuesPerSetOfGroupsOfNodesPlotter listsOfValuesPlotter = new ListsOfValuesPerSetOfGroupsOfNodesPlotter(task);
		DistributionPlotter distributionPlotter = new DistributionPlotter(task);

		for (SingleTypeBigChart plotData : differentPlots)	{
			NumericCharacteristic.Type individualValueType = plotData.getIndividualValueType();
			try	{
				if (individualValueType == null)	{
					String errorMessage = "WARNING:\tThere're no computation results for any of lines of "+plotData.getChartName();
					System.out.println(errorMessage);
					singleValuesPlotter.drawPlot(plotData, individualValueType);
					continue;
				}

				switch (individualValueType)	{
				case SINGLE_VALUE:
					singleValuesPlotter.drawPlot(plotData, individualValueType);
					break;
				case LIST_OF_VALUES:
					listsOfValuesPlotter.plotValuesDistributedOverCommunities(plotData);
					break;
				case DISTRIBUTION:
					distributionPlotter.plotValuesDistributedOverCommunities(plotData);
					break;
				case FUNCTION:
					singleValuesPlotter.drawPlot(plotData, individualValueType);
					break;
				}
			} catch (ResultsPresentationException rpe)	{
				System.out.println("ERROR:\tNo plot can be drawn for "+plotData.getChartName()+":");
				rpe.printStackTrace();
			}
		}
	}
}
