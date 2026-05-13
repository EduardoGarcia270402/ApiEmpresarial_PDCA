package com.empresa.tasks.sqa.engine;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

public class McCallReportParser {

    // Lee cobertura desde jacoco.xml
    public double parseCoverageFromJacoco(String jacocoXmlPath) {
        try {
            File file = new File(jacocoXmlPath);
            if (!file.exists()) return 0.0;

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(file);

            NodeList counters = doc.getElementsByTagName("counter");
            long missed = 0, covered = 0;

            for (int i = 0; i < counters.getLength(); i++) {
                var node = counters.item(i);
                String type = node.getAttributes().getNamedItem("type").getNodeValue();
                if ("INSTRUCTION".equals(type)) {
                    missed = Long.parseLong(
                        node.getAttributes().getNamedItem("missed").getNodeValue()
                    );
                    covered = Long.parseLong(
                        node.getAttributes().getNamedItem("covered").getNodeValue()
                    );
                    break;
                }
            }

            if (missed + covered == 0) return 0.0;
            return (double) covered / (missed + covered) * 100.0;

        } catch (Exception e) {
            return 0.0;
        }
    }

    // Cuenta bugs desde spotbugsXml.xml
    public int parseBugsFromSpotBugs(String spotbugsXmlPath) {
        try {
            File file = new File(spotbugsXmlPath);
            if (!file.exists()) return 0;

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(file);

            NodeList bugs = doc.getElementsByTagName("BugInstance");
            return bugs.getLength();

        } catch (Exception e) {
            return 0;
        }
    }
}