package ru.ispras.modis.NetBlox.numericResultsPresentation;

import java.awt.Shape;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.plot.DefaultDrawingSupplier;

/**
 * The drawing supplier that gives out only one shape: small vertical rectangle.
 * 
 * @author ilya
 */
public class JFreeVerticalRectangleDrawingSupplier extends DefaultDrawingSupplier {
	private static final long serialVersionUID = 1L;

	private final Shape verticalRectangle;

	public JFreeVerticalRectangleDrawingSupplier(double pointHeight)	{
		super();
		verticalRectangle = new Rectangle2D.Double(-1, -pointHeight/2, 2, pointHeight);
	}

	@Override
    public Shape getNextShape() {
		return verticalRectangle;
	}
}
