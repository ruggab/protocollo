package it.com.acamir.protocollo.services;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Locale;

import javax.imageio.ImageIO;
import javax.swing.text.AttributeSet.FontAttribute;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.krysalis.barcode4j.BarcodeGenerator;
import org.krysalis.barcode4j.BarcodeUtil;
import org.krysalis.barcode4j.HumanReadablePlacement;
import org.krysalis.barcode4j.impl.code128.Code128Bean;
import org.krysalis.barcode4j.impl.upcean.EAN13Bean;
import org.krysalis.barcode4j.output.bitmap.BitmapCanvasProvider;
import org.krysalis.barcode4j.tools.UnitConv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.itextpdf.barcodes.BarcodeEAN;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.io.source.ByteArrayOutputStream;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;

@Service
public class ProtocolloService {

	private static final Logger log = LoggerFactory.getLogger(ProtocolloService.class);

	public void creaImmagineDaFile() {
		log.info("*********job start crea imm barcode e testo *************");

		/*
		 * Because font metrics is based on a graphics context, we need to create a small, temporary image so we can
		 * ascertain the width and height of the final image
		 */
		int width = 320;
		int height = 200;

		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = img.createGraphics();
		Font font = new Font("Arial", Font.PLAIN, 20);

		//
		// AffineTransform affineTransform = new AffineTransform();
		// affineTransform.rotate(Math.toRadians(90), 0, 0);
		// Font rotatedFont = font.deriveFont(affineTransform);

		g2d.setFont(font);
		// FontMetrics fm = g2d.getFontMetrics();

		//img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		g2d = img.createGraphics();
		g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
		g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
		g2d.setFont(font);
		g2d.setColor(Color.BLACK);

		File file = new File("C://APPO//protocollo//042.raw");

		BufferedReader br = null;
		int nextLinePosition = 100;
		int fontSize = 20;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(file),"ISO-8859-1"));
			
			String barcode = "";
			String line;
			int x = 10;
			int contLine = 0;
			while ((line = br.readLine()) != null) {
				if (contLine > 1 && contLine < 9) {

					if (contLine != 8) {
						String testo = line.substring(line.indexOf("N,\"") + 3, line.length() - 1);
						barcode = line.substring(line.indexOf("N,\"") + 3, line.length() - 1);
						g2d.drawString(testo, x, nextLinePosition);
					} else {
						String testo = line.substring(line.indexOf("N,\"") + 3, line.length() - 1);
						generateEAN13BarcodeImage(testo, "C://APPO//protocollo//generati//042_barcode.png");
					}
					nextLinePosition = nextLinePosition + fontSize;
					// x = x - fontSize;
				}
				contLine++;
			}
			br.close();
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		g2d.dispose();
		try {
			ImageIO.write(img, "png", new File("C://APPO//protocollo//generati//042.png"));
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		log.info("*********job stop crea imm barcode e testo *************");
	}

	public void associaImmagineTestoApdf() {
		log.info("*********associaImmagineApdf start*************");
		try {
			// Getting path of current working directory
			// to create the pdf file in the same directory of
			// the running java program
			String pdfPath = "C://APPO//protocollo//042.pdf";
			String pdfPath2 = "C://APPO//protocollo//generati//042_image.pdf";
			// Modify PDF located at "source" and save to "target"
			PdfDocument pdfDocument = new PdfDocument(new PdfReader(pdfPath), new PdfWriter(pdfPath2));
			// Document to add layout elements: paragraphs, images etc
			Document document = new Document(pdfDocument);

			// Load image from disk
			ImageData imageData = ImageDataFactory.create("C://APPO//protocollo//generati//" + "042.png");
			// Create layout image object and provide parameters. Page number = 1
			Image image = new Image(imageData).scaleAbsolute(160,100).setFixedPosition(1, 350, 750);
			// This adds the image to the page
			document.add(image);
			// Don't forget to close the document.
			// When you use Document, you should close it rather than PdfDocument instance
			document.close();

			// Closing the document
			System.out.println("Image added successfully and PDF file created!");

			log.info("*********associaImmagineApdf fine*************");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void associaBarcodeApdf() {
		log.info("*********associaImmagineApdf start*************");
		try {
			// Getting path of current working directory
			// to create the pdf file in the same directory of
			// the running java program
			String pdfPath = "C://APPO//protocollo//generati//042_image.pdf";
			String pdfPath2 = "C://APPO//protocollo//generati//042_barcode.pdf";
			// Modify PDF located at "source" and save to "target"
			PdfDocument pdfDocument = new PdfDocument(new PdfReader(pdfPath), new PdfWriter(pdfPath2));
			// Document to add layout elements: paragraphs, images etc
			Document document = new Document(pdfDocument);

			// Load image from disk
			ImageData imageData = ImageDataFactory.create("C://APPO//protocollo//generati//" + "042_barcode.png");
			// Create layout image object and provide parameters. Page number = 1
			Image image = new Image(imageData).scaleAbsolute(200,25).setFixedPosition(1, 340, 720);
			// This adds the image to the page
			document.add(image);
			// Don't forget to close the document.
			// When you use Document, you should close it rather than PdfDocument instance
			document.close();

			// Closing the document
			System.out.println("Image added successfully and PDF file created!");

			log.info("*********associaImmagineApdf fine*************");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static BufferedImage generateEAN13BarcodeImage(String barcodeText, String nomeFileJpg) {

		try {
//			BarcodeUtil util = BarcodeUtil.getInstance();
//			BarcodeGenerator gen = util.createBarcodeGenerator(buildCfg("ean-128"));
//
//			OutputStream fout = new FileOutputStream(nomeFileJpg);
//			int resolution = 200;
//			BitmapCanvasProvider canvas = new BitmapCanvasProvider(fout, "image/jpeg", resolution, BufferedImage.TYPE_BYTE_BINARY, false, 0);
//
//			gen.generateBarcode(canvas, barcodeText);
//			canvas.finish();
			//return null;
			
			 OutputStream fout = new FileOutputStream(nomeFileJpg);
			 //BarcodeUtil util = BarcodeUtil.getInstance();
		     //BarcodeGenerator gen;
		     try {
		         //Create the barcode bean
		         Code128Bean bean = new Code128Bean();
		         int dpi = 200;
		         //Configure the barcode generator
		        // bean.setModuleWidth(UnitConv.in2mm(1.1f / dpi)); //makes the narrow bar, width exactly one pixel
		         bean.doQuietZone(true);
		         bean.setBarHeight(3);
		         //bean.setVerticalQuietZone(3);
		        // bean.setQuietZone(0);
		         bean.setMsgPosition(HumanReadablePlacement.HRP_NONE);
		         BitmapCanvasProvider canvas = new BitmapCanvasProvider(fout, "image/jpeg", dpi, BufferedImage.TYPE_BYTE_BINARY, false, 0);
		         bean.generateBarcode(canvas, barcodeText);
		         canvas.finish();
		     } catch (IOException  e) {
		         throw e;
		     }
		     return null;	
			
		} catch (Exception e) {
			// TODO: handle exception
		}
		return null;
	}

	private static Configuration buildCfg(String type) {
		DefaultConfiguration cfg = new DefaultConfiguration("barcode");
		// Bar code type
		DefaultConfiguration child = new DefaultConfiguration(type);
		cfg.addChild(child);

		// Human readable text position
		DefaultConfiguration attr = new DefaultConfiguration("human-readable");
		DefaultConfiguration subAttr = new DefaultConfiguration("placement");
		subAttr.setValue("bottom");
		attr.addChild(subAttr);

		child.addChild(attr);
		return cfg;
	}

}