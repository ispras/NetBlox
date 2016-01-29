package ru.ispras.modis.NetBlox;

import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberTick;
import org.jfree.chart.axis.Tick;
import org.jfree.ui.GradientPaintTransformer;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.TextAnchor;

import com.itextpdf.awt.DefaultFontMapper;
import com.itextpdf.awt.PdfPrinterGraphics2D;
import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfGState;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * Some useful utilities for JFreeChart plots.
 * 
 * @author ilya
 */
public class JFreeChartUtils {
	private static final int DEFAULT_WIDTH = 800;
	private static final int DEFAULT_HEIGHT = 600;

	private static final float GRAPHIC_FILL_OPACITY = 0.8f;


	/*public static void exportToPDF_OrsonPDF(String exportFileName, JFreeChart chart, int width, int height)	{
		Rectangle2D drawingArea = new Rectangle(0, 0, width, height);

		PDFDocument pdfDocument = new PDFDocument();
		Page page = pdfDocument.createPage(drawingArea);
		PDFGraphics2D pdfGraphics2D = page.getGraphics2D();

		chart.draw(pdfGraphics2D, drawingArea);

		File pdfFile = new File(exportFileName);
		pdfDocument.writeToFile(pdfFile);
	}*/


	public static void exportToPDF_iText(String exportFileName, JFreeChart chart)	{
		exportToPDF_iText(exportFileName, chart, DEFAULT_WIDTH, DEFAULT_HEIGHT);
	}

	public static void exportToPDF_iText(String exportFileName, JFreeChart chart, int width, int height)	{
		PdfWriter writer = null;

		Document document = new Document(new com.itextpdf.text.Rectangle(width, height));

		try {
			writer = PdfWriter.getInstance(document, new FileOutputStream(exportFileName));
			document.open();
			PdfContentByte contentByte = writer.getDirectContent();
			PdfTemplate template = contentByte.createTemplate(width, height);
			//Graphics2D graphics2d = template.createGraphics(width, height, new DefaultFontMapper());
			Graphics2D graphics2d = new PdfPrinterGraphics2D(contentByte, width, height, new DefaultFontMapper(), PrinterJob.getPrinterJob());
			Rectangle2D plottingArea = new Rectangle2D.Double(0, 0, width, height);

			PdfGState pdfGraphicState = new PdfGState();
			pdfGraphicState.setFillOpacity(GRAPHIC_FILL_OPACITY);
			contentByte.setGState(pdfGraphicState);

			chart.draw(graphics2d, plottingArea);

			graphics2d.dispose();
			contentByte.addTemplate(template, 0, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}

		document.close();
	}


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
