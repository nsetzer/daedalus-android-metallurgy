package com.github.nicksetzer.metallurgy.orm.dsl;

public class StringUtil {


    public static String escape(String input) {
        StringBuilder output = new StringBuilder();

        output.append("\"");
        int offset=0;
        while ( offset<input.length()) {
            int codepoint = input.codePointAt(offset);
            offset += Character.charCount(codepoint);

            if(codepoint == '\n') {
                output.append("\\n");
            } else if(codepoint == '\t') {
                output.append("\\t");
            } else if(codepoint == '\r') {
                output.append("\\r");
            } else if(codepoint == '\\') {
                output.append("\\\\");
            } else if(codepoint == '"') {
                output.append("\\\"");
            } else if(codepoint == '\b') {
                output.append("\\b");
            } else if(codepoint == '\f') {
                output.append("\\f");
            } else if(codepoint > 0xFFFF) {
                // split the code point into UTF16 surrogate pairs
                int u = codepoint - 0x10000;
                int y = (u >> 10) & 0x3FF;
                int x = (u      ) & 0x3FF;
                int W1 = 0xD800 + y;
                int W2 = 0xDC00 + x;
                output.append(String.format("\\u%04x", W1));
                output.append(String.format("\\u%04x", W2));
            } else if(codepoint > 127) {
                output.append(String.format("\\u%04x", codepoint));
            } else {
                output.appendCodePoint(codepoint);
            }
        }
        output.append("\"");

        return output.toString();
    }

    /**
     * parse a quoted string
     * @param input
     * @return
     */
    public static String unescape(String input) {
        StringBuilder sb = new StringBuilder();

        int offset = 0;
        int length = input.length();

        int quote = input.codePointAt(offset);
        offset += Character.charCount(quote); // consume one of ' " `

        while (offset < length) {
            int ch1 = input.codePointAt(offset);
            offset += Character.charCount(ch1); // consume one of ' " `

            if(ch1 == '\\' && offset < length) {

                // consume first after backslash
                int ch2 = input.codePointAt(offset);
                offset += Character.charCount(ch2);

                if(ch2 == '\\' || ch2 == '/' || ch2 == '"' || ch2 == '\'') {
                    sb.appendCodePoint(ch2);
                }
                else if(ch2 == 'n') sb.append('\n');
                else if(ch2 == 'r') sb.append('\r');
                else if(ch2 == 't') sb.append('\t');
                else if(ch2 == 'b') sb.append('\b');
                else if(ch2 == 'f') sb.append('\f');
                else if(ch2 == 'u') {

                    StringBuilder hex = new StringBuilder();

                    // expect 4 digits
                    if (offset+4 > length) {
                        throw new RuntimeException("Not enough unicode digits! ");
                    }
                    for (char x : input.substring(offset, offset + 4).toCharArray()) {
                        if(!Character.isLetterOrDigit(x)) {
                            throw new RuntimeException("Bad character in unicode escape.");
                        }
                        hex.append(Character.toLowerCase(x));
                    }
                    offset+=4; // consume four hex digits.

                    int code = Integer.parseInt(hex.toString(), 16);
                    sb.append((char) code);
                } else {
                    throw new RuntimeException("Illegal escape sequence: \\" + ch2);
                }
            } else if (ch1 == quote) {
                break;
            } else { // it's not a backslash, or it's the last character.
                sb.appendCodePoint(ch1);
            }
        }

        if (offset != length) {
            throw new RuntimeException("Unexpected end of string");
        }

        return sb.toString();
    }
}
