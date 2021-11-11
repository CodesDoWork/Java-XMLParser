package com.codesdowork.sax.writer;

import java.io.StringWriter;

public class PrettyXmlWriter extends StringWriter {

    private int level = 0;

    public void writeTag(String tag, TagType tagType) {
        if(tagType == TagType.Closing) {
            --level;
        }

        if(currentLine().length() == 0) {
            write(indent());
        } else if(tagType == TagType.Opening) {
            append("\n").write(indent());
        }

        write("<");
        if(tagType == TagType.Closing) {
            write("/");
        }

        write(tag);
        if(tagType == TagType.Single) {
            write("/");
        }

        write(">");
        if(tagType == TagType.Closing) {
            write("\n");
        }

        if(tagType == TagType.Opening) {
            ++level;
        }
    }

    public void writeValue(String value) {
        if(value.contains("\n") || value.length() + currentLine().length() > 120) {
            append("\n").append(value.indent(4 * level)).append("\n");
        } else {
            write(value);
        }
    }

    private String indent() {
        return "\t".repeat(level);
    }

    private String currentLine() {
        StringBuffer buffer = getBuffer();
        return buffer.substring(buffer.lastIndexOf("\n") + 1);
    }
}
