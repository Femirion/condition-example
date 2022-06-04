package com.conditionexample;

import com.conditionexample.stub.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.List;

import static com.conditionexample.Constants.*;


public class DocumentProvider extends AbstractDocumentProvider {

    public Document getDocument(Pack pack, File svgFile, String newImageName)
            throws ParserConfigurationException, SAXException, IOException {

        boolean isSingleMode = pack.getStampLinks().stream()
                .anyMatch(link -> link.getType() == StampLinkType.SINGLE);

        List readCodes = isSingleMode ?
                getStampCodes(pack, StampLinkState.READ) :
                getAggregationCodes(pack, StampLinkState.READ);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(svgFile);

        NodeList header = doc.getElementsByTagName(SVG_TAG_NAME);
        processSvgTag(header, newImageName);

        NodeList imageList = doc.getElementsByTagName(IMAGE_TAG_NAME);
        processImageTag(imageList, newImageName);

        NodeList nodeList = doc.getElementsByTagName(G_TAG_NAME);
        processTagG(nodeList, pack, readCodes, isSingleMode);

        return doc;
    }

    private void processSvgTag(NodeList header, String newImageName) {
        for (int i = 0; i < header.getLength(); i++) {
            Node firstNode = header.item(i);
            Element firstElement = (Element) firstNode;
            firstElement.setAttribute(ID, newImageName);
            NodeList firstChildList = firstNode.getChildNodes();
            for (int l = 0; l < firstChildList.getLength(); l++) {
                Node firstNextNode = firstChildList.item(l);
                if (TITLE.equals(firstNextNode.getNodeName())) {
                    firstNextNode.setTextContent(createNewImageName(newImageName));
                }
            }
        }
    }

    private void processImageTag(NodeList imageList, String newImageName) {
        for (int i = 0; i < imageList.getLength(); i++) {
            Node image = imageList.item(i);
            Element imageElement = (Element) image;
            imageElement.setAttribute("xlink:href", createNewImageName(newImageName));
        }
    }

    private void processTagG(NodeList nodeList, Pack pack, List readCodes, boolean isSingleMode) {
        for (int i = 0; i < nodeList.getLength(); i++) {
            Element element = (Element) nodeList.item(i);
            NodeList childList = element.getChildNodes();
            processChildrenList(childList, element, pack, readCodes, isSingleMode);
        }
    }

    private void processChildrenList(NodeList childList, Element element, Pack pack, List readCodes, boolean isSingleMode) {
        for (int j = 0; j < childList.getLength(); j++) {
            Node nextNode = childList.item(j);
            if (Node.ELEMENT_NODE == nextNode.getNodeType()) {
                Element node = (Element) nextNode;
                if (ROI.equals(node.getAttribute(CLASS))) {
                    element.removeAttribute(FILL);
                    continue;
                }
                node.removeAttribute(FILL);
            }

            if (isNotText(nextNode)) {
                continue;
            }

            String imageCode = nextNode.getTextContent();
            if (isEndsWith(PPM_SUFFIX)) {
                element.removeChild(nextNode);
                continue;
            }

            ((Element) nextNode).setAttribute(FILL, GOOD);
            if (pack.getState() == PackState.DEFECTIVE && !containsCode(readCodes, imageCode, isSingleMode)) {
                ((Element) nextNode).setAttribute(FILL, DEFECTIVE);
                element.setAttribute(STROKE, DEFECTIVE);
            }
        }

    }

    private boolean isNotText(Node nextNode) {
        return !TEXT.equals(nextNode.getNodeName());
    }

    private boolean isEndsWith(String imageCode) {
        return imageCode.endsWith(PPM_SUFFIX);
    }


    private String createNewImageName(String newImageName) {
        return String.format(NEW_IMAGE_NAME, newImageName);
    }

}
