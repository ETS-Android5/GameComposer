package de.mirkosertic.gameengine.dragome;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.dragome.services.ServiceLocator;

public class DragomeLogger {

    public static void info(String aMessage) {
        Document theDocument = ServiceLocator.getInstance().getDomHandler().getDocument();
        Element theLogger = ServiceLocator.getInstance().getDomHandler().getElementBySelector("#logger");

        Element theDiv = theDocument.createElement("div");
        theDiv.appendChild(theDocument.createTextNode(aMessage));
        theLogger.appendChild(theDiv);
    }

    public static void error(String aMessage) {
        Document theDocument = ServiceLocator.getInstance().getDomHandler().getDocument();
        Element theLogger = ServiceLocator.getInstance().getDomHandler().getElementBySelector("#logger");

        Element theDiv = theDocument.createElement("div");
        theDiv.setAttribute("style","color: red;");
        theDiv.appendChild(theDocument.createTextNode(aMessage));
        theLogger.appendChild(theDiv);
    }
}