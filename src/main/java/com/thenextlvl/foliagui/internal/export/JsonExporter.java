package com.thenextlvl.foliagui.internal.export;

import com.thenextlvl.foliagui.api.GUI;
import com.thenextlvl.foliagui.api.export.GUIExporter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * JSON 格式导出器
 *
 * @author TheNextLvl
 */
public class JsonExporter implements GUIExporter {

    @Override
    public @NotNull String getFormat() {
        return "JSON";
    }

    @Override
    public @NotNull String getFileExtension() {
        return "json";
    }

    @Override
    public @NotNull String getContentType() {
        return "application/json";
    }

    @Override
    public @NotNull String export(@NotNull GUI gui) {
        StringWriter writer = new StringWriter();
        try {
            export(gui, writer);
        } catch (IOException e) {
            // StringWriter 不会抛出 IOException
        }
        return writer.toString();
    }

    @Override
    public void export(@NotNull GUI gui, @NotNull Writer writer) throws IOException {
        Map<String, Object> config = buildConfigMap(gui);
        writeJson(config, writer);
    }

    private Map<String, Object> buildConfigMap(@NotNull GUI gui) {
        Map<String, Object> config = new LinkedHashMap<>();
        config.put("id", gui.getId());

        if (gui.getInventory() != null) {
            config.put("size", gui.getInventory().getSize());
        }

        return config;
    }

    private void writeJson(Map<String, Object> map, Writer writer) throws IOException {
        writer.write("{\n");
        boolean first = true;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) {
                writer.write(",\n");
            }
            first = false;

            writer.write("  \"");
            writer.write(escapeJson(entry.getKey()));
            writer.write("\": ");

            Object value = entry.getValue();
            if (value instanceof Map) {
                writeJsonMap((Map<String, Object>) value, writer, 2);
            } else if (value instanceof Iterable) {
                writeJsonArray((Iterable<?>) value, writer, 2);
            } else if (value instanceof String) {
                writer.write("\"");
                writer.write(escapeJson(value.toString()));
                writer.write("\"");
            } else if (value instanceof Number || value instanceof Boolean) {
                writer.write(value.toString());
            } else {
                writer.write("null");
            }
        }
        writer.write("\n}");
    }

    private void writeJsonMap(Map<String, Object> map, Writer writer, int indent) throws IOException {
        writer.write("{\n");
        boolean first = true;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) {
                writer.write(",\n");
            }
            first = false;

            writeIndent(writer, indent);
            writer.write("\"");
            writer.write(escapeJson(entry.getKey()));
            writer.write("\": ");

            Object value = entry.getValue();
            if (value instanceof Map) {
                writeJsonMap((Map<String, Object>) value, writer, indent + 2);
            } else if (value instanceof Iterable) {
                writeJsonArray((Iterable<?>) value, writer, indent + 2);
            } else if (value instanceof String) {
                writer.write("\"");
                writer.write(escapeJson(value.toString()));
                writer.write("\"");
            } else if (value instanceof Number || value instanceof Boolean) {
                writer.write(value.toString());
            } else {
                writer.write("null");
            }
        }
        writer.write("\n");
        writeIndent(writer, indent - 2);
        writer.write("}");
    }

    private void writeJsonArray(Iterable<?> iterable, Writer writer, int indent) throws IOException {
        writer.write("[\n");
        boolean first = true;
        for (Object item : iterable) {
            if (!first) {
                writer.write(",\n");
            }
            first = false;

            writeIndent(writer, indent);
            if (item instanceof Map) {
                writeJsonMap((Map<String, Object>) item, writer, indent + 2);
            } else if (item instanceof String) {
                writer.write("\"");
                writer.write(escapeJson(item.toString()));
                writer.write("\"");
            } else if (item instanceof Number || item instanceof Boolean) {
                writer.write(item.toString());
            } else {
                writer.write("null");
            }
        }
        writer.write("\n");
        writeIndent(writer, indent - 2);
        writer.write("]");
    }

    private void writeIndent(Writer writer, int indent) throws IOException {
        for (int i = 0; i < indent; i++) {
            writer.write(' ');
        }
    }

    private String escapeJson(String str) {
        return str
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t");
    }
}