package ru.ispras.modis.NetBlox.numericResultsPresentation;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Paint;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.jfree.chart.plot.DefaultDrawingSupplier;
import org.jfree.chart.plot.DrawingSupplier;

public class JFreeTextures {
	private Paint[] colouredPatternPaints;

	public JFreeTextures()	{
		colouredPatternPaints = makeColouredPatternPaints();
	}

	private Paint[] makeColouredPatternPaints()	{
		List<Paint> colouredPatternPaints = new LinkedList<Paint>();

		colouredPatternPaints.add(new Color(0.9f, 0.9f, 0.9f, 0.5f));

		Color transparentColour = new Color(1.0f, 1.0f, 1.0f, 0.0f);	//Last position is for zero alpha - absolute transparency.
		for (Color baseColour : getBaseColours())	{
			List<Paint> oneColourTextures = getColouredTextures(baseColour, transparentColour);
			colouredPatternPaints.addAll(oneColourTextures);
		}

		Paint[] result = new Paint[colouredPatternPaints.size()];
		int i = 0;
		for (Paint paint : colouredPatternPaints)	{
			result[i] = paint;
			i++;
		}
		return result;
	}

	private List<Color> getBaseColours()	{
		List<Color> colours = new ArrayList<Color>(12);

		colours.add(Color.BLACK);
		colours.add(Color.RED);
		colours.add(Color.BLUE);
		colours.add(Color.GREEN);
		colours.add(Color.DARK_GRAY);
		colours.add(Color.MAGENTA);
		colours.add(Color.CYAN);
		colours.add(Color.YELLOW);
		colours.add(Color.GRAY);
		colours.add(Color.ORANGE);
		colours.add(Color.PINK);
		colours.add(Color.LIGHT_GRAY);

		return colours;
	}

	private List<Paint> getColouredTextures(Color baseColour, Color transparentColour)	{
		List<Paint> colouredTextures = new LinkedList<Paint>();

		//colouredTextures.add(baseColour);

		GradientPaint gpVertical = new GradientPaint(5, 5, baseColour, 10, 5, transparentColour, true);
		GradientPaint gpUpSlant = new GradientPaint(5, 5, baseColour, 10, 10, transparentColour, true);
		GradientPaint gpHorizontal = new GradientPaint(5, 5, baseColour, 5, 10, transparentColour, true);
		GradientPaint gpDownSlant = new GradientPaint(5, 10, baseColour, 10, 5, transparentColour, true);
		colouredTextures.add(gpVertical);
		//colouredTextures.add(Color.LIGHT_GRAY);
		colouredTextures.add(gpUpSlant);
		colouredTextures.add(gpHorizontal);
		colouredTextures.add(gpDownSlant);

		//It's also possible to try change places of base and transparent colours.

		return colouredTextures;
	}


	public DrawingSupplier getDrawingSupplier()	{
		DefaultDrawingSupplier drawingSupplier = new DefaultDrawingSupplier(colouredPatternPaints,
				DefaultDrawingSupplier.DEFAULT_OUTLINE_PAINT_SEQUENCE,
				DefaultDrawingSupplier.DEFAULT_STROKE_SEQUENCE,
				DefaultDrawingSupplier.DEFAULT_OUTLINE_STROKE_SEQUENCE,
				DefaultDrawingSupplier.DEFAULT_SHAPE_SEQUENCE);

		return drawingSupplier;
	}
}
