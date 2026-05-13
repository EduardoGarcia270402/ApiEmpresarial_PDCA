package com.empresa.tasks.sqa;

import com.empresa.tasks.sqa.engine.McCallReportParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class McCallReportParserTest {

    private McCallReportParser parser;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        parser = new McCallReportParser();
    }

    @Test
    void deberiaParsearCoberturaDeJacocoXml() throws Exception {
        // Crear jacoco.xml falso con 80 covered, 20 missed = 80%
        String jacocoXml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <report name="tasks">
                <counter type="INSTRUCTION" missed="20" covered="80"/>
                <counter type="LINE" missed="5" covered="20"/>
            </report>
            """;

        File jacocoFile = tempDir.resolve("jacoco.xml").toFile();
        try (FileWriter fw = new FileWriter(jacocoFile)) {
            fw.write(jacocoXml);
        }

        double coverage = parser.parseCoverageFromJacoco(jacocoFile.getAbsolutePath());
        assertEquals(80.0, coverage, 0.01);
    }

    @Test
    void deberiaRetornarCeroSiArchivoJacocoNoExiste() {
        double coverage = parser.parseCoverageFromJacoco("ruta/que/no/existe.xml");
        assertEquals(0.0, coverage);
    }

    @Test
    void deberiaParsearBugsDeSpotBugsXml() throws Exception {
        // Crear spotbugsXml.xml falso con 3 bugs
        String spotbugsXml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <BugCollection>
                <BugInstance type="NP_NULL" priority="1" category="CORRECTNESS"/>
                <BugInstance type="DM_STRING" priority="2" category="PERFORMANCE"/>
                <BugInstance type="RCN_NULL" priority="1" category="CORRECTNESS"/>
            </BugCollection>
            """;

        File spotbugsFile = tempDir.resolve("spotbugsXml.xml").toFile();
        try (FileWriter fw = new FileWriter(spotbugsFile)) {
            fw.write(spotbugsXml);
        }

        int bugs = parser.parseBugsFromSpotBugs(spotbugsFile.getAbsolutePath());
        assertEquals(3, bugs);
    }

    @Test
    void deberiaRetornarCeroSiArchivoSpotBugsNoExiste() {
        int bugs = parser.parseBugsFromSpotBugs("ruta/que/no/existe.xml");
        assertEquals(0, bugs);
    }

    @Test
    void deberiaRetornarCeroSiNoHayInstrucciones() throws Exception {
        String jacocoXml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <report name="tasks">
                <counter type="LINE" missed="0" covered="0"/>
            </report>
            """;

        File jacocoFile = tempDir.resolve("jacoco_empty.xml").toFile();
        try (FileWriter fw = new FileWriter(jacocoFile)) {
            fw.write(jacocoXml);
        }

        double coverage = parser.parseCoverageFromJacoco(jacocoFile.getAbsolutePath());
        assertEquals(0.0, coverage);
    }
}