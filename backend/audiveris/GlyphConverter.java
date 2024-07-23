import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Predicate;

import org.audiveris.omr.run.Run;
import org.audiveris.omr.sheet.Picture;
import org.w3c.dom.Document;

import javax.imageio.ImageIO;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.audiveris.omr.run.Orientation;
import org.audiveris.omr.run.RunTable;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class GlyphConverter {

    /**
     * Convert HOMUS dataset glyphs to xml format
     * to be used by the audiveris training tool.
     * 
     * To get this to work:
     * - You need to put this file in src/main of the audiveris code.
     * - It is also required to download the HOMUS Dataset.
     * - Finally, you need to change the glyphPath, xmlPath and formattedPath to the correct paths for your machine.
     * 
     * Author: Charlie Poncsak
     * Version: 2024.07.23
     * 
     * @param args
     */
    public static void main (final String[] args) throws XMLStreamException, JAXBException, IOException, ParserConfigurationException, SAXException {
        // Path to the HOMUS dataset
        String glyphPath = "path\\to\\Homus-Dataset";
        System.out.println("PNG dataset path: " + glyphPath);

        // Intermediary xml files will be saved here, but can be deleted after the script is done
        String xmlPath = "temp\\path\\to\\runtables";
        System.out.println("xml output path: " + xmlPath);

        // Extracting RunTable info from png and saving to xml
        extractRunTables(glyphPath, xmlPath);

        // Output path here
        String formattedPath = "output\\path";
        System.out.println("\n==========================================\n");
        System.out.println("formatted dataset path: " + formattedPath);

        // Formatting data to be read by the training tool
        formatRunTables(xmlPath, formattedPath);
    }

    /**
     * Extract run tables from the glyphs in the specified path
     *
     * @param glyphPath the path to the glyphs
     * @param xmlPath   the path to the output xml files
     * @throws XMLStreamException if an error occurs during the extraction
     * @throws JAXBException      if an error occurs during the extraction
     * @throws IOException        if an error occurs during the extraction
     */
    private static void extractRunTables(String glyphPath, String xmlPath) throws XMLStreamException, JAXBException, IOException {
        // Turning all glyphs in glyph path into binary image ---------------------------------
        for(File subDir : new File(glyphPath).listFiles()) {
            if(subDir.isDirectory()) {
                System.out.println("----------------------------------");
                System.out.println("Processing subdirectory: " + subDir.getName());

                // Create a subdirectory in the xml path
                new File(Paths.get(xmlPath, subDir.getName()).toString()).mkdirs();

                // for all files in the subdirectory
                for (File file : subDir.listFiles()) {
                    if (file.isFile()) {
                        System.out.println("Processing file: " + file.getName());
                        BufferedImage in = ImageIO.read(file);

                        BufferedImage binaryImg = new BufferedImage(
                                in.getWidth(), in.getHeight(), BufferedImage.TYPE_BYTE_GRAY);

                        Graphics2D g = binaryImg.createGraphics();
                        g.drawImage(in, 0, 0, null);
                        g.dispose();

                        System.out.println("Binary image done");

                        // Generate run table ----------------------------------------------
                        RunTable runTable = Picture.tableOf(binaryImg);
                        System.out.println("Runtable done: " + runTable.getTotalRunCount() + " \n");
                        runTable.marshal(Paths.get(xmlPath, subDir.getName(), file.getName().replace(".png", ".xml")));
                    }
                }
            }
        }
    }

    /**
     * Format the run tables in the specified xml path
     *
     * @param xmlPath       the path to the xml files
     * @param formattedPath the path to the formatted xml files
     */
    private static void formatRunTables(String xmlPath, String formattedPath) throws ParserConfigurationException, IOException, SAXException {
        int id = 0;
        // For every subdirectory in the xml path
        for(File subDir : new File(xmlPath).listFiles()) {
            if(subDir.isDirectory()) {
                System.out.println("----------------------------------");
                System.out.println("Processing subdirectory: " + subDir.getName());

                // Create a subdirectory in the formatted path
                new File(Paths.get(formattedPath, subDir.getName()).toString()).mkdirs();

                // Write data to xml file ------------------------------------------
                DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                
                // root elements
                Document doc = docBuilder.newDocument();
                Element rootElement = doc.createElement("samples");
                rootElement.setAttribute("sheet-name", "HOMUS");
                doc.appendChild(rootElement);
                
                // for all files in the subdirectory
                for (File file : subDir.listFiles()) {
                    if (file.isFile()) {
                        System.out.println("Processing file: " + file.getName());
                        
                        // get runtable element of the xml file
                        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                        DocumentBuilder db = dbf.newDocumentBuilder();
                        Document fileDoc = db.parse(new File(file.getAbsolutePath()));
                        fileDoc.getDocumentElement().normalize();
                        NodeList list = fileDoc.getElementsByTagName("run-table");
                        
                        if(list.getLength() != 1) {
                            throw new RuntimeException("Invalid run-table element count: " + list.getLength() + " in file: " + file.getName());
                        }

                        // add runtable element to the root element
                        Element sample = doc.createElement("sample");
                        sample.setAttribute("shape", subDir.getName());
                        sample.setAttribute("interline", "29");
                        sample.setAttribute("pitch", "1");
                        sample.setAttribute("left", "750");
                        sample.setAttribute("top", "1000");
                        sample.setAttribute("id", String.valueOf(id++));
                        rootElement.appendChild(sample);
                        sample.appendChild(doc.importNode(list.item(0), true));
                    }
                }

                // write dom document to a file
                try (FileOutputStream output =
                             new FileOutputStream(String.valueOf(Paths.get(formattedPath, subDir.getName(), "samples.xml")))) {
                    writeXml(doc, output);
                } catch (IOException | TransformerException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Write the DOM document to the specified output stream
     *
     * @param doc    the DOM document
     * @param output the output stream
     * @throws TransformerException if an error occurs during the transformation
     */
    private static void writeXml(Document doc, OutputStream output) throws TransformerException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(output);

        transformer.transform(source, result);
    }
}
