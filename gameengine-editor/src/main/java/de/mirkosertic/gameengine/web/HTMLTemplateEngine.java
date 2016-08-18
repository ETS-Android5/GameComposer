package de.mirkosertic.gameengine.web;

import org.teavm.jso.dom.html.HTMLDocument;
import org.teavm.jso.dom.html.HTMLElement;

import java.util.Map;

public class HTMLTemplateEngine {

    private final HTML5Document document;

    public HTMLTemplateEngine(HTML5Document aDocument) {
        document = aDocument;
    }

    public <T extends HTMLElement> T createNewComponent(String aComponentID) {
        return (T) document.createElement(aComponentID);
    }

    public HTMLElement renderToElement(String aTemplateID, Map<String, Object> aData) {
        HTMLElement theTemplate = (HTMLElement) document.getElementById(aTemplateID);
        HTMLElement theImportedNode = document.importNode(theTemplate, true);

        String theContent = document.toHTML(theImportedNode);
        for (Map.Entry<String, Object> theEntry : aData.entrySet()) {
            if (theEntry.getValue() != null) {
                theContent = theContent.replace("{{"+theEntry.getKey()+"}}", theEntry.getValue().toString());
            } else {
                theContent = theContent.replace("{{"+theEntry.getKey()+"}}", "");
            }
        }

        DOMParser theParser = DOMParser.create();
        HTMLDocument theResult = theParser.parseFromString(theContent, "text/html");
        return (HTMLElement) theResult.getBody().getFirstChild();
    }
}
