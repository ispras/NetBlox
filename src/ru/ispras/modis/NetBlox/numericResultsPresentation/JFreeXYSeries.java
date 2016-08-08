package ru.ispras.modis.NetBlox.numericResultsPresentation;

import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.data.xy.XYSeries;

import ru.ispras.modis.NetBlox.exceptions.ResultsPresentationException;
import ru.ispras.modis.NetBlox.scenario.DescriptionDataArrangement.AxesScale;

public class JFreeXYSeries extends XYSeries {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private AxesScale axesScale;


	public JFreeXYSeries(Comparable<?> key, AxesScale axesScale)	{
		super(key);
		this.axesScale = axesScale;
	}


	public void addCorrect(Number x, Number y) throws ResultsPresentationException	{
		switch (axesScale)	{
		case XY_LOG10:
		case Y_LOG10:
			if (y == null)	{
				break;
			}
			if (y.doubleValue() < -LogarithmicAxis.SMALL_LOG_VALUE)	{
				throw new ResultsPresentationException("Negative numbers cannot be put to logarithmic scale.");
			}
			else if (y.doubleValue() < LogarithmicAxis.SMALL_LOG_VALUE)	{
				y = LogarithmicAxis.SMALL_LOG_VALUE;
			}
			break;
		}

		super.add(x, y);
	}
}
