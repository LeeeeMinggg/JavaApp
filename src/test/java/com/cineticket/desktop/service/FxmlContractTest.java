package com.cineticket.desktop.service;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilderFactory;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FxmlContractTest {
    @Test
    void allFxmlFilesReferenceExistingControllersFieldsAndHandlers() throws Exception {
        Path viewDirectory = Path.of("src/main/resources/com/cineticket/desktop/view");
        List<String> problems = new ArrayList<>();
        try (var paths = Files.list(viewDirectory)) {
            for (Path path : paths.filter(value -> value.toString().endsWith(".fxml")).sorted().toList()) {
                Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(path.toFile());
                Element root = document.getDocumentElement();
                String controllerName = root.getAttribute("fx:controller");
                if (controllerName.isBlank()) { problems.add(path.getFileName() + ": thiếu fx:controller"); continue; }
                Class<?> controller = Class.forName(controllerName);
                var nodes = document.getElementsByTagName("*");
                for (int index = 0; index < nodes.getLength(); index++) {
                    Element element = (Element) nodes.item(index);
                    String fxId = element.getAttribute("fx:id");
                    if (!fxId.isBlank() && findField(controller, fxId) == null) {
                        problems.add(path.getFileName() + ": không tìm thấy field " + fxId);
                    }
                    NamedNodeMap attributes = element.getAttributes();
                    for (int attributeIndex = 0; attributeIndex < attributes.getLength(); attributeIndex++) {
                        Node attribute = attributes.item(attributeIndex);
                        if (attribute.getNodeValue().startsWith("#")) {
                            String handler = attribute.getNodeValue().substring(1);
                            if (findMethod(controller, handler) == null) {
                                problems.add(path.getFileName() + ": không tìm thấy handler " + handler);
                            }
                        }
                    }
                }
            }
        }
        assertTrue(problems.isEmpty(), String.join(System.lineSeparator(), problems));
    }

    private Field findField(Class<?> type, String name) {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            try { return current.getDeclaredField(name); }
            catch (NoSuchFieldException ignored) { }
        }
        return null;
    }

    private Method findMethod(Class<?> type, String name) {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            for (Method method : current.getDeclaredMethods()) if (method.getName().equals(name)) return method;
        }
        return null;
    }
}
