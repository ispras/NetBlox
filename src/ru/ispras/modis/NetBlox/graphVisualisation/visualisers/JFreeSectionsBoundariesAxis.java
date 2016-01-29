package ru.ispras.modis.NetBlox.graphVisualisation.visualisers;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.Tick;
import org.jfree.ui.RectangleEdge;

import ru.ispras.modis.NetBlox.JFreeChartUtils;
import ru.ispras.modis.NetBlox.graphVisualisation.visualisers.MatrixGraphVisualiser.GroupsOfNodesOnAxisPositionData;

/**
 * This version of JFreeChart NumberAxis is designed to draw ticks corresponding
 * to the boundaries of sections on the axis.
 * 
 * @author ilya
 */
public class JFreeSectionsBoundariesAxis extends NumberAxis {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6929941085059273563L;

	private static final double MINIMAL_COMMUNITY_LENGTH_SHARE_TO_LABEL = 0.04;

	private List<GroupsOfNodesOnAxisPositionData> communitiesPositionsData;


	public JFreeSectionsBoundariesAxis(String label, List<GroupsOfNodesOnAxisPositionData> communitiesPositionsData)	{
		super(label);

		this.communitiesPositionsData = communitiesPositionsData;
	}


	/**
	 * Calculates the positions of the tick labels for the axis, storing the
	 * results in the tick label list (ready for drawing).
	 *
	 * @param g2  the graphics device.
	 * @param dataArea  the area in which the plot should be drawn.
	 * @param edge  the location of the axis.
	 *
	 * @return A list of ticks.
	 */
	@Override
	protected List<Tick> refreshTicksHorizontal(Graphics2D g2, Rectangle2D dataArea, RectangleEdge edge) {
		List<Tick> ticks = new ArrayList<Tick>();

		int summarisedCommunitiesLength = getCommunitiesSummarisedLength();
		int communityNumber = 0;
		for (GroupsOfNodesOnAxisPositionData communityPositionData : communitiesPositionsData)	{
			communityNumber++;

			String tickLabel = makeTickLabel(communityNumber, communityPositionData, summarisedCommunitiesLength);

			Tick tick = JFreeChartUtils.makeTick(communityPositionData.getStartPosition(), tickLabel, edge, isVerticalTickLabels());
			ticks.add(tick);
		}

		return ticks;
	}

	private String makeTickLabel(int communityNumber, GroupsOfNodesOnAxisPositionData communityPositionData, double summarisedCommunitiesLength)	{
		StringBuilder tickLabelBuilder = new StringBuilder();

		if (communityPositionData.getPreviousCommunityLength() / summarisedCommunitiesLength >= MINIMAL_COMMUNITY_LENGTH_SHARE_TO_LABEL)	{
			tickLabelBuilder.append(communityNumber-1);
		}
		else if (communityNumber < 10)	{
			tickLabelBuilder.append("  ");
		}
		else	{
			tickLabelBuilder.append("    ");
		}

		tickLabelBuilder.append('|');

		if (communityPositionData.getLengthOnAxis() / summarisedCommunitiesLength >= MINIMAL_COMMUNITY_LENGTH_SHARE_TO_LABEL  ||
				communityNumber==communitiesPositionsData.size())	{
			tickLabelBuilder.append(communityNumber);
		}
		else if (communityNumber <= 10)	{
			tickLabelBuilder.append("  ");
		}
		else	{
			tickLabelBuilder.append("    ");
		}

		return tickLabelBuilder.toString();
	}

	private int getCommunitiesSummarisedLength()	{
		GroupsOfNodesOnAxisPositionData lastCommunityData = communitiesPositionsData.get(communitiesPositionsData.size()-1);
		int totalLength = lastCommunityData.getStartPosition() + lastCommunityData.getLengthOnAxis();
		return totalLength;
	}
}
