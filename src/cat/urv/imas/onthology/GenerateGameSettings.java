/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.onthology;

import static cat.urv.imas.onthology.InitialGameSettings.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Random;
import java.util.ArrayList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import cat.urv.imas.agent.DiggerAgent;
/**
 * Helper for updating the game settings. To do so, just update the content of
 * the <code>defineSettings()</code> method.
 */
public class GenerateGameSettings {

    private static final int STEPS = 600;
    private static final String FILENAME = "game.settings";
    private static final String FILENAME_2 = "game.settings.runtime";


    /*
     * ********************* JUST SET YOUR SETTINGS ****************************
     */
    /**
     * Override the default settings to what you need.
     *
     * @param settings GameSettings instance.
     */
    public static void defineSettings(InitialGameSettings settings) {
        //add here whatever settings.set* to define your new settings.
        try {
            // We start reading the file
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document doc = documentBuilder.parse(FILENAME);
            doc.getDocumentElement().normalize();
            // Now we can read the tags
            String seed = doc.getElementsByTagName("seed").item(0).getTextContent();
            settings.setSeed(Integer.parseInt(seed));
            String newMetalProb = doc.getElementsByTagName("newMetalProbability").item(0).getTextContent();
            settings.setNewMetalProbability(Integer.parseInt(newMetalProb));
            String maxNumberFields = doc.getElementsByTagName("maxNumberFieldsWithNewMetal").item(0).getTextContent();
            settings.setMaxNumberFieldsWithNewMetal(Integer.parseInt(maxNumberFields));
            String maxAmountMetal = doc.getElementsByTagName("maxAmountOfNewMetal").item(0).getTextContent();
            settings.setMaxAmountOfNewMetal(Integer.parseInt(maxAmountMetal));
            String diggersCapacity = doc.getElementsByTagName("diggersCapacity").item(0).getTextContent();
            settings.setDiggersCapacity(Integer.parseInt(diggersCapacity));
            
            NodeList prices = doc.getElementsByTagName("manufacturingCenterPrice");
            int[] manufacturingPrices = new int[prices.getLength()];
            for(int i = 0; i < prices.getLength(); i++) {
                manufacturingPrices[i] = Integer.parseInt(prices.item(i).getTextContent());
            }
            settings.setManufacturingCenterPrice(manufacturingPrices);
            
            NodeList metalTypes = doc.getElementsByTagName("manufacturingCenterMetalType");
            MetalType[] manufacturingMetalTypes = new MetalType[metalTypes.getLength()];
            for(int i = 0; i < metalTypes.getLength(); i++) {
                String metal = metalTypes.item(i).getTextContent();
                if(metal.equals("GOLD"))
                    manufacturingMetalTypes[i] = MetalType.GOLD;
                else
                    manufacturingMetalTypes[i] = MetalType.SILVER;
            }
            settings.setManufacturingCenterMetalType(manufacturingMetalTypes);
            
            String simulationSteps = doc.getElementsByTagName("simulationSteps").item(0).getTextContent();
            settings.setSimulationSteps(Integer.parseInt(simulationSteps));
            String title = doc.getElementsByTagName("title").item(0).getTextContent();
            settings.setTitle(title);
            String initialElements = doc.getElementsByTagName("numberInitialElements").item(0).getTextContent();
            settings.setNumberInitialElements(Integer.parseInt(initialElements));
            String initialVisibleElements = doc.getElementsByTagName("numberVisibleInitialElements").item(0).getTextContent();
            settings.setNumberVisibleInitialElements(Integer.parseInt(initialVisibleElements));
                        
            NodeList nList = doc.getElementsByTagName("initialMap");
                                    
            ArrayList <ArrayList <Integer>> amap = new ArrayList<ArrayList<Integer>>();
            ArrayList <Integer> line = new ArrayList<Integer>();
            
            for(int i=0; i < nList.getLength(); i++)
            {
                Node nNode = nList.item(i);
                
                //System.out.println("\n Current element: " + nNode.getNodeName());
                
                if(nNode.getNodeType() == Node.ELEMENT_NODE)
                {
                    Element eElement = (Element) nNode;
                    line = new ArrayList<Integer>();
                    
                    for(int j=0; j < eElement.getElementsByTagName("item").getLength(); j++)
                    {
                        line.add(Integer.parseInt(eElement.getElementsByTagName("item").item(j).getTextContent()));
                        //System.out.println(eElement.getElementsByTagName("item").getLength());
                        //System.out.println("Item " +i+" "+j+": " + eElement.getElementsByTagName("item").item(j).getTextContent());                        
                    }
                    
                    amap.add(line);
                }
            }
            /*
            int [][] map = new int[amap.size()][line.size()];
            
            for(int i=0; i<amap.size();i++)
            {
                for(int j=0; j<line.size();j++)
                {
                    map[i][j] = amap.get(i).get(j);
                    
                    //System.out.println(map[i][j]);
                }                
            }
            
            settings.setInitialMap(map);*/
                        
            // settings for first date
            int[][] firstMap
                = {
                    {F, F, F, MCC, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F},
                    {F, P, P, P, P, P, P, P, P, P, P, DC, P, P, P, P, P, P, P, F},
                    {F, P, PC, P, P, P, P, DC, P, P, P, P, P, P, P, P, P, P, DC, F},
                    {F, P, P, F, F, F, F, F, F, P, P, F, F, F, F, F, F, F, F, F},
                    {F, P, P, F, F, F, F, F, MCC, P, P, F, F, F, F, F, F, F, F, F},
                    {F, PC, P, F, F, P, P, P, P, P, P, F, F, P, P, P, P, P, P, F},
                    {F, P, P, F, F, P, P, P, P, P, P, F, F, P, P, P, P, P, P, F},
                    {F, P, P, F, F, P, P, F, F, P, P, F, F, P, P, F, F, P, P, F},
                    {F, P, P, F, F, P, DC, F, F, P, P, F, F, P, P, F, F, P, P, F},
                    {F, P, P, F, F, P, P, F, F, P, P, F, F, P, P, F, F, P, P, F},
                    {F, P, P, F, F, P, P, F, F, P, P, F, F, P, P, F, F, P, P, F},
                    {F, P, DC, F, F, P, P, F, F, P, PC, F, F, P, P, F, F, P, P, F},
                    {F, P, P, F, F, P, P, F, F, P, P, F, F, P, P, MCC, F, P, P, F},
                    {F, P, P, F, F, P, P, F, F, P, P, F, F, P, P, F, F, DC, P, F},
                    {F, P, P, F, F, P, P, F, F, P, P, F, F, P, P, F, F, DC, P, F},
                    {F, P, P, F, F, P, P, F, F, P, P, F, F, P, P, F, F, P, P, F},
                    {F, P, P, MCC, F, P, P, F, F, P, P, F, F, P, P, F, F, P, P, F},
                    {F, P, PC, P, DC, P, P, F, F, P, P, P, P, P, P, F, F, P, P, F},
                    {F, P, P, P, P, P, P, F, F, P, P, P, P, P, P, F, F, DC, P, F},
                    {F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F},
                };
            //settings.setInitialMap(firstMap);
        } catch(IOException | ParserConfigurationException | DOMException | SAXException e) {
        }
    }

    /*
     * ********************* DO NOT MODIFY BELOW *******************************
     */
    /**
     * Produces a new settings file to be loaded into the game.
     *
     * @param args nothing expected.
     */
    public static final void main(String[] args) {
        InitialGameSettings settings = new InitialGameSettings();

        defineSettings(settings);
        storeSettings(settings);
        testSettings();
    }

    /**
     * Produces an XML file with the whole set of settings from the given
     * GameSettings.
     *
     * @param settings GameSettings to store in a file.
     */
    private static void storeSettings(InitialGameSettings settings) {
        try {

            //create JAXBElement of type GameSettings
            //Pass it the GameSettings object
            JAXBElement<InitialGameSettings> jaxbElement = new JAXBElement(
                    new QName(InitialGameSettings.class.getSimpleName()), InitialGameSettings.class, settings);

            //Create a String writer object which will be
            //used to write jaxbElment XML to string
            StringWriter writer = new StringWriter();

            // create JAXBContext which will be used to update writer
            JAXBContext context = JAXBContext.newInstance(InitialGameSettings.class);

            // marshall or convert jaxbElement containing GameSettings to xml format
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty("jaxb.formatted.output", Boolean.TRUE);
            marshaller.marshal(jaxbElement, writer);

            //print XML string representation of GameSettings
            try {
                PrintWriter out = new PrintWriter(FILENAME_2, "UTF-8");
                out.println(writer.toString());
                out.close();
            } catch (Exception e) {
                System.err.println("Could not create file '" + FILENAME_2 + "'.");
                System.out.println(writer.toString());
            }

        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks that settings file was created and it is readable again.
     */
    private static void testSettings() {
        try {
            GameSettings settings = InitialGameSettings.load(FILENAME);
            if (settings.getSimulationSteps() != STEPS) {
                throw new Exception("Something went wrong, we loaded some different to what we stored on '" + FILENAME + "'.");
            }
            System.out.println("Settings loaded again from '" + FILENAME + "'. Ok!");
        } catch (Exception e) {
            System.err.println("Settings could not be loaded from '" + FILENAME + "'!");
            e.printStackTrace();
        }
    }

}
