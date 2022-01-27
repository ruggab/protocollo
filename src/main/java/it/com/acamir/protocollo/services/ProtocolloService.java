package it.com.acamir.protocollo.services;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.krysalis.barcode4j.HumanReadablePlacement;
import org.krysalis.barcode4j.impl.code128.Code128Bean;
import org.krysalis.barcode4j.output.bitmap.BitmapCanvasProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;

import it.com.acamir.protocollo.util.PropertiesUtil;

@Service
public class ProtocolloService {

	private static final Logger log = LoggerFactory.getLogger(ProtocolloService.class);

	@Scheduled(fixedRateString = "${cronExpression}")
	public void creaImmagineAssociaPdf() {
		try {
			log.info("*********Start BATCH *************");
			
			// Lettura files da dir path
			List<String> listFile = getListFileOfDir(PropertiesUtil.getPathDaProtocollare());
			if (listFile.size() > 2) {
				throw new Exception("Attenzione lella directory devono essere presenti solo 2 file, un file .RAW ed un file .PDF");
			}
			//
			String pathFileRaw = "", nomefileRaw = "", nomefileImgBarcode = "", nomefileImgTesto = "";
			String pathFilePdf = "", nomefilePdf = "";
			int cont = 0;
			for (String nomefile : listFile) {
				log.info("trovato il file: " + nomefile);
				if (nomefile.contains(".raw")) {
					pathFileRaw = PropertiesUtil.getPathDaProtocollare() + nomefile;
					nomefileRaw = nomefile;
					cont = cont + 1;
				}
				if (nomefile.contains(".pdf")) {
					pathFilePdf = PropertiesUtil.getPathDaProtocollare() + nomefile;
					nomefilePdf = nomefile;
					cont = cont + 1;
				}
			}
			if (cont == 0) {
				throw new Exception("Nessun file da protocollare");
			} else if (cont != 2) {
				throw new Exception("Attenzione lella directory devono essere presenti 2 file, un file .RAW ed un file .PDF");
			}

			log.info("*********Start creazione immagine barcode e testo *************");
			/*
			 * Because font metrics is based on a graphics context, we need to create a small, temporary image so we can
			 * ascertain the width and height of the final image
			 */
			int width = 320;
			int height = 200;

			BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2d = img.createGraphics();
			Font font = new Font("Arial", Font.PLAIN, 20);

			g2d.setFont(font);
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

			log.info("Carico il file " + pathFileRaw);
			File fileRaw = new File(pathFileRaw);

			BufferedReader br = null;
			int nextLinePosition = 100;
			int fontSize = 20;
			try {
				br = new BufferedReader(new InputStreamReader(new FileInputStream(fileRaw), "ISO-8859-1"));
				//
				String barcode;
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
							nomefileImgBarcode = PropertiesUtil.getPathDaRimuovere() + nomefileRaw + "_barcode.png";
							log.info("Creo immagine barcode: " + nomefileImgBarcode);
							generateEAN13BarcodeImage(testo, nomefileImgBarcode);
						}
						nextLinePosition = nextLinePosition + fontSize;
					}
					contLine++;
				}
				br.close();
			} catch (FileNotFoundException ex) {
				throw ex;
			} catch (IOException ex) {
				throw ex;
			} catch (Exception ex) {
				throw ex;
			}

			g2d.dispose();

			try {
				nomefileImgTesto = PropertiesUtil.getPathDaRimuovere() + nomefileRaw + "_testo.png";
				log.info("Creo immagine testo: " + nomefileImgTesto);
				ImageIO.write(img, "png", new File(nomefileImgTesto));
			} catch (IOException ex) {
				throw ex;
			}
			try {
				BufferedImage imgTesto = ImageIO.read(new File(nomefileImgTesto));
				BufferedImage imgBr = ImageIO.read(new File(nomefileImgBarcode));
				BufferedImage joinedImg = joinBufferedImage(imgTesto, imgBr);
				log.info("Creo immagine unica: " + PropertiesUtil.getPathDaRimuovere() + "joined.png");
				ImageIO.write(joinedImg, "png", new File(PropertiesUtil.getPathDaRimuovere() + "joined.png"));
			} catch (Exception ex) {
				throw ex;
			}

			try {
				BufferedImage myPicture = ImageIO.read(new File(PropertiesUtil.getPathDaRimuovere() + "joined.png"));
				Graphics2D g = (Graphics2D) myPicture.getGraphics();
				// g.setStroke(new BasicStroke(3));
				g.setColor(Color.LIGHT_GRAY);
				g.drawRect(0, 70, myPicture.getWidth() - 1, myPicture.getHeight() - 71);
				log.info("Creo immagine unica con bordo: " + PropertiesUtil.getPathDaRimuovere() + "joined_border.png");
				ImageIO.write(myPicture, "png", new File(PropertiesUtil.getPathDaRimuovere() + "joined_border.png"));
			} catch (Exception ex) {
				throw ex;
			}

			try {
				// Getting path of current working directory to create the pdf file in the same directory of the running
				// java program

				String pdfPathProtocollato = PropertiesUtil.getPathProtocollati() + nomefilePdf + "_image.pdf";
				log.info("Associo Immagine con bordo a pdf" + pdfPathProtocollato);
				// Modify PDF located at "source" and save to "target"
				PdfDocument pdfDocument = new PdfDocument(new PdfReader(pathFilePdf), new PdfWriter(pdfPathProtocollato));
				// Document to add layout elements: paragraphs, images etc
				Document document = new Document(pdfDocument);

				// Load image from disk
				ImageData imageData = ImageDataFactory.create(PropertiesUtil.getPathDaRimuovere() + "joined_border.png");
				// Create layout image object and provide parameters. Page number = 1

				String position = PropertiesUtil.getPosition();
				int x, y = 0;
				switch (position) {
				case "1":
					x = 5;
					y = 750;
					break;
				case "2":
					x = 200;
					y = 750;
					break;
				case "3":
					x = 405;
					y = 750;
					break;
				case "4":
					x = 405;
					y = 405;
					break;
				case "5":
					x = 405;
					y = 5;
					break;
				case "6":
					x = 200;
					y = 5;
					break;
				case "7":
					x = 5;
					y = 5;
					break;
				case "8":
					x = 5;
					y = 405;
					break;
				default:
					x = 405;
					y = 750;
					break;
				}
				Image image = new Image(imageData).scaleAbsolute(160, 100).setFixedPosition(1, x, y);
				// This adds the image to the page
				document.add(image);
				// Don't forget to close the document.
				// When you use Document, you should close it rather than PdfDocument instance
				document.close();
				// Closing the document
				log.info("Image aggiunta con successo!");

				// Sposto file raw
				Path fileIn = Paths.get(pathFileRaw);
				Path fileOut = Paths.get(PropertiesUtil.getPathDaRimuovere() + nomefileRaw);
				Files.copy(fileIn, fileOut);
				Files.delete(fileIn);
				log.info("Spostato file raw");
				// Sposto file pdf
				log.info("Sposto file pdf");
				fileIn = Paths.get(pathFilePdf);
				fileOut = Paths.get(PropertiesUtil.getPathDaRimuovere() + nomefilePdf);
				Files.copy(fileIn, fileOut);
				Files.delete(fileIn);
				log.info("Spostato file pdf");

			} catch (Exception e) {
				throw e;
			}

		} catch (Exception e) {
			log.error(e.toString());
		}
		try {
			log.info("Ripulisco la cartella temporanea");
			//Ripulisce tutta la dir daRimuovere
			List<String> listFileDarimuovere = getListFileOfDir(PropertiesUtil.getPathDaRimuovere());
			for (String nomeFileDarimuovere : listFileDarimuovere) {
				Files.delete(Paths.get(PropertiesUtil.getPathDaRimuovere() + nomeFileDarimuovere));
			}
		} catch (Exception e) {
			log.error(e.toString());
		}
		

		
		log.info("*********FINE*************");
	}

	private static BufferedImage generateEAN13BarcodeImage(String barcodeText, String nomeFileJpg) {
		try {

			OutputStream fout = new FileOutputStream(nomeFileJpg);
			// BarcodeUtil util = BarcodeUtil.getInstance();
			// BarcodeGenerator gen;
			try {
				// Create the barcode bean
				Code128Bean bean = new Code128Bean();
				int dpi = 200;
				// Configure the barcode generator
				// bean.setModuleWidth(UnitConv.in2mm(1.1f / dpi)); //makes the narrow bar, width exactly one pixel
				bean.doQuietZone(true);
				bean.setBarHeight(4);
				// bean.setVerticalQuietZone(3);
				// bean.setQuietZone(0);
				bean.setMsgPosition(HumanReadablePlacement.HRP_NONE);
				BitmapCanvasProvider canvas = new BitmapCanvasProvider(fout, "image/jpeg", dpi, BufferedImage.TYPE_BYTE_BINARY, false, 0);
				bean.generateBarcode(canvas, barcodeText);
				canvas.finish();
			} catch (IOException e) {
				throw e;
			}
			return null;

		} catch (Exception e) {
			// TODO: handle exception
		}
		return null;
	}

	private static BufferedImage joinBufferedImage(BufferedImage img1, BufferedImage img2) {

		// do some calculate first
		int offset = 5;
		int wid = Math.max(img1.getWidth(), img2.getWidth()) + offset;
		int height = img1.getHeight() + img2.getHeight() + offset + 10;
		// create a new buffer and draw two image into the new image
		BufferedImage newImage = new BufferedImage(wid, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D gra = newImage.createGraphics();
		Color oldColor = gra.getColor();
		// fill background
		gra.setPaint(Color.WHITE);
		gra.fillRect(0, 0, wid, height);
		// draw image
		gra.setColor(oldColor);
		//
		gra.drawImage(img1, null, 0, 0);
		gra.drawImage(img2, null, 45, img1.getHeight() + offset);
		gra.dispose();
		return newImage;

	}

	private List<String> getListFileOfDir(String dir) throws IOException {
		File folder = new File(dir);
		File[] listOfFiles = folder.listFiles();
		List<String> listFile = new ArrayList<String>();
		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				listFile.add(listOfFiles[i].getName());
			}
		}
		return listFile;
	}

}