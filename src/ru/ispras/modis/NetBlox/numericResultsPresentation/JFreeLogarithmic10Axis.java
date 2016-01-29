package ru.ispras.modis.NetBlox.numericResultsPresentation;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.axis.Tick;
import org.jfree.data.Range;
import org.jfree.ui.RectangleEdge;

import ru.ispras.modis.NetBlox.JFreeChartUtils;

/**
 * A modification of JFreeChart <code>LogarithmicAxis</code> class that allows
 * to use <i>10^n</i> format for x-axis (horizontal axis) labels.
 * 
 * @author ilya
 */
public class JFreeLogarithmic10Axis extends LogarithmicAxis {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8819674642415952858L;

	public JFreeLogarithmic10Axis(String label) {
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
	 */
	@Override
	protected List<Tick> refreshTicksHorizontal(Graphics2D g2, Rectangle2D dataArea, RectangleEdge edge) {
		List<Tick> ticks = new ArrayList<Tick>();
		Range range = getRange();

		//get lower bound value:
		double lowerBoundVal = range.getLowerBound();
			//if small log values and lower bound value too small
			// then set to a small value (don't allow <= 0):
		if (this.smallLogFlag && lowerBoundVal < SMALL_LOG_VALUE) {
			lowerBoundVal = SMALL_LOG_VALUE;
		}

		//get upper bound value
		double upperBoundVal = range.getUpperBound();

		//get log10 version of lower bound and round to integer:
		int iBeginingCount = (int) Math.rint(switchedLog10(lowerBoundVal));
		//get log10 version of upper bound and round to integer:
		int iEndCount = (int) Math.rint(switchedLog10(upperBoundVal));

		if (iBeginingCount == iEndCount  &&  iBeginingCount > 0  &&
				Math.pow(10, iBeginingCount) > lowerBoundVal) {
			//only 1 power of 10 value, it's > 0 and its resulting
			// tick value will be larger than lower bound of data
			--iBeginingCount;       //decrement to generate more ticks
		}

		double currentTickValue;
		String tickLabel;
		boolean zeroTickFlag = false;
		for (int i = iBeginingCount; i <= iEndCount; i++) {
			//for each power of 10 value; create ten ticks
			for (int j = 0; j < 10; ++j) {
				//for each tick to be displayed

				if (zeroTickFlag) {   //if did zero tick last iter then
					--j;              //decrement to do 1.0 tick now
				}     //calculate power-of-ten value for tick:
				currentTickValue = (i >= 0)	?
						Math.pow(10, i) + (Math.pow(10, i) * j) :
							-(Math.pow(10, -i) - (Math.pow(10, -i - 1) * j));
				if (!zeroTickFlag) {  // did not do zero tick last iteration
					if (Math.abs(currentTickValue - 1.0) < 0.0001  &&
							lowerBoundVal <= 0.0 && upperBoundVal >= 0.0) {
						//tick value is 1.0 and 0.0 is within data range
						currentTickValue = 0.0;     //set tick value to zero
						zeroTickFlag = true;        //indicate zero tick
					}
				}
				else {     //did zero tick last iteration
					zeroTickFlag = false;         //clear flag
				}

				//create tick label string:
				tickLabel = (j==0) ? ("10^{"+Math.abs(i)+"}") : "";

				if (currentTickValue > upperBoundVal) {
					return ticks;   // if past highest data value then exit method.
				}

				if (currentTickValue >= lowerBoundVal - SMALL_LOG_VALUE) {	//tick value not below lowest data value
					Tick tick = JFreeChartUtils.makeTick(currentTickValue, tickLabel, edge, isVerticalTickLabels());
					ticks.add(tick);
				}
			}
		}

		return ticks;
	}
}
