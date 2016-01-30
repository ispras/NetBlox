package ru.ispras.modis.NetBlox;

import java.awt.GradientPaint;
import java.awt.Shape;
import java.io.File;
import java.io.IOException;

import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberTick;
import org.jfree.chart.axis.Tick;
import org.jfree.ui.GradientPaintTransformer;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.TextAnchor;

/**
 * Some useful utilities for JFreeChart plots.
 * 
 * @author ilya
 */
public class JFreeChartUtils {
	private static final int DEFAULT_WIDTH = 800;
	private static final int DEFAULT_HEIGHT = 600;


	public static void exportToPNG(String exportFileName, JFreeChart chart)	{
		exportToPNG(exportFileName, chart, DEFAULT_WIDTH, DEFAULT_HEIGHT);
	}

	public static void exportToPNG(String exportFileName, JFreeChart chart, int width, int height)	{
		File pngFile = new File(exportFileName);
		try {
			ChartUtilities.saveChartAsPNG(pngFile, chart, width, height);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}



	public static Tick makeTick(Number currentTickValue, String tickLabel, RectangleEdge edge, boolean isVerticalTickLabels)	{
		TextAnchor anchor;
        TextAnchor rotationAnchor;
        double angle = 0.0;
        if (isVerticalTickLabels) {
            anchor = TextAnchor.CENTER_RIGHT;
            rotationAnchor = TextAnchor.CENTER_RIGHT;
            if (edge == RectangleEdge.TOP) {
                angle = Math.PI / 2.0;
            }
            else {
                angle = -Math.PI / 2.0;
            }
        }
        else {
            if (edge == RectangleEdge.TOP) {
                anchor = TextAnchor.BOTTOM_CENTER;
                rotationAnchor = TextAnchor.BOTTOM_CENTER;
            }
            else {
                anchor = TextAnchor.TOP_CENTER;
                rotationAnchor = TextAnchor.TOP_CENTER;
            }
        }

        Tick tick = new NumberTick(currentTickValue, tickLabel, anchor, rotationAnchor, angle);
        return tick;
	}



	public static class UntransformingGradientPaintTransformer implements GradientPaintTransformer	{
		@Override
		public GradientPaint transform(GradientPaint gradientPaint, Shape shape) {
			return gradientPaint;
		}
	}
}
