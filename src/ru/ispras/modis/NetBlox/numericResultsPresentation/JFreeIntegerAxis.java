package ru.ispras.modis.NetBlox.numericResultsPresentation;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.text.NumberFormat;
import java.util.List;

import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTick;
import org.jfree.chart.axis.Tick;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.TextAnchor;

public class JFreeIntegerAxis extends NumberAxis {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5184720669243069423L;


	public JFreeIntegerAxis(String label)	{
		super(label);
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
	  *
	  */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	protected List refreshTicksVertical(Graphics2D g2, Rectangle2D dataArea, RectangleEdge edge) {
		List result = new java.util.ArrayList();
		result.clear();

		Font tickLabelFont = getTickLabelFont();
		g2.setFont(tickLabelFont);
		if (isAutoTickUnitSelection()) {
			selectAutoTickUnit(g2, dataArea, edge);
		}

		//double size = getTickUnit().getSize();
		int size = 1;
		if (getTickUnit().getSize() > 1)	{
			size = (int)Math.floor(getTickUnit().getSize());
		}
		int count = calculateVisibleTickCount();
		//double lowestTickValue = calculateLowestVisibleTickValue();
		int lowestTickValue = (int)Math.floor(calculateLowestVisibleTickValue());

		while (lowestTickValue+size*count < getRange().getUpperBound())	{
			count++;
		}

		if (count <= ValueAxis.MAXIMUM_TICK_COUNT) {
			for (int i = 0; i < count; i++) {
				//double currentTickValue = lowestTickValue + (i * size);
				Integer currentTickValue = lowestTickValue + (i * size);
				String tickLabel;
				NumberFormat formatter = getNumberFormatOverride();
				if (formatter != null) {
					tickLabel = formatter.format(currentTickValue);
				}
				else {
					tickLabel = currentTickValue.toString();	//getTickUnit().valueToString(currentTickValue);
				}

				TextAnchor anchor = null;
				TextAnchor rotationAnchor = null;
				double angle = 0.0;
				if (isVerticalTickLabels()) {
					if (edge == RectangleEdge.LEFT) { 
						anchor = TextAnchor.BOTTOM_CENTER;
						rotationAnchor = TextAnchor.BOTTOM_CENTER;
						angle = -Math.PI / 2.0;
					}
					else {
						anchor = TextAnchor.BOTTOM_CENTER;
						rotationAnchor = TextAnchor.BOTTOM_CENTER;
						angle = Math.PI / 2.0;
					}
				}
				else {
					if (edge == RectangleEdge.LEFT) {
						anchor = TextAnchor.CENTER_RIGHT;
						rotationAnchor = TextAnchor.CENTER_RIGHT;
					}
					else {
						anchor = TextAnchor.CENTER_LEFT;
						rotationAnchor = TextAnchor.CENTER_LEFT;
					}
				}

				Tick tick = new NumberTick(currentTickValue,
						tickLabel, anchor, rotationAnchor, angle);
				result.add(tick);
			}
		}

		return result;
	}
}
