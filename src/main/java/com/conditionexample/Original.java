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

public class Original extends AbstractDocumentProvider {

    public Document getDocument(Pack pack, File svgFile, String newImageName) throws ParserConfigurationException, SAXException, IOException {
        boolean isSingleMode = pack.getStampLinks().stream().anyMatch(link -> link.getType() == StampLinkType.SINGLE);
        // лучше использовать дженерик, есть ли возможность это сделать?
        // если так приходит из библиотеки, то давай искать другую библиотеку
        List readCodes = isSingleMode ?
                getStampCodes(pack, StampLinkState.READ) :
                getAggregationCodes(pack, StampLinkState.READ);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(svgFile);

        // строковые литералы лучше вынести в константы
        NodeList header = doc.getElementsByTagName("svg");
        for (int k = 0; k < header.getLength(); k++) {
            Node firstNode = header.item(k);
            Element firstElement = (Element) firstNode;
            firstElement.setAttribute("id", newImageName);
            NodeList firstChildList = firstNode.getChildNodes();
            for (int l = 0; l < firstChildList.getLength(); l++) {

                Node firstNextNode = firstChildList.item(l);
                if (Node.ELEMENT_NODE == firstNextNode.getNodeType()) {
                    // firstElem не используется - удаяем или это баг?!
                    Element firstElem = (Element) firstNextNode;
                }
                // используем нотацию Йоды "title".equals()
                if (firstNextNode.getNodeName().equals("title")) {
                    firstNextNode.setTextContent(newImageName + ".jpg");
                }
            }
        }
        NodeList imageList = doc.getElementsByTagName("image");
        // тк циклы не вложенные, то спокойно можем использовать i=0 как счетчик
        // в этом цикле и цикле выше, не обязательно в каждом цикле новую переменную делать
        for (int m = 0; m < imageList.getLength(); m++) {
            Node image = imageList.item(m);
            Element imageElement = (Element) image;
            imageElement.setAttribute("xlink:href", newImageName + ".jpg");
        }

        NodeList nodeList = doc.getElementsByTagName("g");
        for (int i = 0; i < nodeList.getLength(); i++) {
            Element element = (Element) nodeList.item(i);
            NodeList childList = element.getChildNodes();
            for (int j = 0; j < childList.getLength(); j++) {
                Node nextNode = childList.item(j);
                if (Node.ELEMENT_NODE == nextNode.getNodeType()) {
                    Element node = (Element) nextNode;
                    if (node.getAttribute("class").equals("ROI")) {
                        element.removeAttribute("fill");
                        continue;
                    }
                    node.removeAttribute("fill");
                }
                // для уменьшения вложенности используем хак с проверкой и выходом, вместо кода внутри ифа
                if (nextNode.getNodeName().equals("text")) {
                    String imageCode = nextNode.getTextContent();
                    if (imageCode.endsWith(" PPM")) {
                        element.removeChild(nextNode);
                    } else {
                        ((Element) nextNode).setAttribute("fill", "#00FF00");
                        // нужно объединить эти 2 проверки
                        if (pack.getState() == PackState.DEFECTIVE) {
                            if (!containsCode(readCodes, imageCode, isSingleMode)) {
                                ((Element) nextNode).setAttribute("fill", "red");
                                element.setAttribute("stroke", "red");
                            }
                        }
                    }
                }
            }
        }
        return doc;
    }

}
