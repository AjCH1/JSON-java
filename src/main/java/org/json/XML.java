package org.json;

/*
Public Domain.
*/

import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;


/**
 * This provides static methods to convert an XML text into a JSONObject, and to
 * covert a JSONObject into an XML text.
 *
 * @author JSON.org
 * @version 2016-08-10
 */
@SuppressWarnings("boxing")
public class XML {

    /** The Character '&amp;'. */
    public static final Character AMP = '&';

    /** The Character '''. */
    public static final Character APOS = '\'';

    /** The Character '!'. */
    public static final Character BANG = '!';

    /** The Character '='. */
    public static final Character EQ = '=';

    /** The Character <pre>{@code '>'. }</pre>*/
    public static final Character GT = '>';

    /** The Character '&lt;'. */
    public static final Character LT = '<';

    /** The Character '?'. */
    public static final Character QUEST = '?';

    /** The Character '"'. */
    public static final Character QUOT = '"';

    /** The Character '/'. */
    public static final Character SLASH = '/';

    /**
     * Null attribute name
     */
    public static final String NULL_ATTR = "xsi:nil";

    public static final String TYPE_ATTR = "xsi:type";

    /**
     * Creates an iterator for navigating Code Points in a string instead of
     * characters. Once Java7 support is dropped, this can be replaced with
     * <code>
     * string.codePoints()
     * </code>
     * which is available in Java8 and above.
     *
     * @see <a href=
     *      "http://stackoverflow.com/a/21791059/6030888">http://stackoverflow.com/a/21791059/6030888</a>
     */
    private static Iterable<Integer> codePointIterator(final String string) {
        return new Iterable<Integer>() {
            @Override
            public Iterator<Integer> iterator() {
                return new Iterator<Integer>() {
                    private int nextIndex = 0;
                    private int length = string.length();

                    @Override
                    public boolean hasNext() {
                        return this.nextIndex < this.length;
                    }

                    @Override
                    public Integer next() {
                        int result = string.codePointAt(this.nextIndex);
                        this.nextIndex += Character.charCount(result);
                        return result;
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
    }

    /**
     * Replace special characters with XML escapes:
     *
     * <pre>{@code 
     * &amp; (ampersand) is replaced by &amp;amp;
     * &lt; (less than) is replaced by &amp;lt;
     * &gt; (greater than) is replaced by &amp;gt;
     * &quot; (double quote) is replaced by &amp;quot;
     * &apos; (single quote / apostrophe) is replaced by &amp;apos;
     * }</pre>
     *
     * @param string
     *            The string to be escaped.
     * @return The escaped string.
     */
    public static String escape(String string) {
        StringBuilder sb = new StringBuilder(string.length());
        for (final int cp : codePointIterator(string)) {
            switch (cp) {
            case '&':
                sb.append("&amp;");
                break;
            case '<':
                sb.append("&lt;");
                break;
            case '>':
                sb.append("&gt;");
                break;
            case '"':
                sb.append("&quot;");
                break;
            case '\'':
                sb.append("&apos;");
                break;
            default:
                if (mustEscape(cp)) {
                    sb.append("&#x");
                    sb.append(Integer.toHexString(cp));
                    sb.append(';');
                } else {
                    sb.appendCodePoint(cp);
                }
            }
        }
        return sb.toString();
    }

    /**
     * @param cp code point to test
     * @return true if the code point is not valid for an XML
     */
    private static boolean mustEscape(int cp) {
        /* Valid range from https://www.w3.org/TR/REC-xml/#charsets
         *
         * #x9 | #xA | #xD | [#x20-#xD7FF] | [#xE000-#xFFFD] | [#x10000-#x10FFFF]
         *
         * any Unicode character, excluding the surrogate blocks, FFFE, and FFFF.
         */
        // isISOControl is true when (cp >= 0 && cp <= 0x1F) || (cp >= 0x7F && cp <= 0x9F)
        // all ISO control characters are out of range except tabs and new lines
        return (Character.isISOControl(cp)
                && cp != 0x9
                && cp != 0xA
                && cp != 0xD
            ) || !(
                // valid the range of acceptable characters that aren't control
                (cp >= 0x20 && cp <= 0xD7FF)
                || (cp >= 0xE000 && cp <= 0xFFFD)
                || (cp >= 0x10000 && cp <= 0x10FFFF)
            )
        ;
    }

    /**
     * Removes XML escapes from the string.
     *
     * @param string
     *            string to remove escapes from
     * @return string with converted entities
     */
    public static String unescape(String string) {
        StringBuilder sb = new StringBuilder(string.length());
        for (int i = 0, length = string.length(); i < length; i++) {
            char c = string.charAt(i);
            if (c == '&') {
                final int semic = string.indexOf(';', i);
                if (semic > i) {
                    final String entity = string.substring(i + 1, semic);
                    sb.append(XMLTokener.unescapeEntity(entity));
                    // skip past the entity we just parsed.
                    i += entity.length() + 1;
                } else {
                    // this shouldn't happen in most cases since the parser
                    // errors on unclosed entries.
                    sb.append(c);
                }
            } else {
                // not part of an entity
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Throw an exception if the string contains whitespace. Whitespace is not
     * allowed in tagNames and attributes.
     *
     * @param string
     *            A string.
     * @throws JSONException Thrown if the string contains whitespace or is empty.
     */
    public static void noSpace(String string) throws JSONException {
        int i, length = string.length();
        if (length == 0) {
            throw new JSONException("Empty string.");
        }
        for (i = 0; i < length; i += 1) {
            if (Character.isWhitespace(string.charAt(i))) {
                throw new JSONException("'" + string
                        + "' contains a space character.");
            }
        }
    }

    /**
     * Scan the content following the named tag, attaching it to the context.
     *
     * @param x
     *            The XMLTokener containing the source string.
     * @param context
     *            The JSONObject that will include the new material.
     * @param name
     *            The tag name.
     * @return true if the close tag is processed.
     * @throws JSONException
     */
    private static boolean parse(XMLTokener x, JSONObject context, String name, XMLParserConfiguration config)
            throws JSONException {
        char c;
        int i;
        JSONObject jsonObject = null;
        String string;
        String tagName;
        Object token;
        XMLXsiTypeConverter<?> xmlXsiTypeConverter;

        // Test for and skip past these forms:
        // <!-- ... -->
        // <! ... >
        // <![ ... ]]>
        // <? ... ?>
        // Report errors for these forms:
        // <>
        // <=
        // <<

        token = x.nextToken();
        // <!

        if (token == BANG) {
            c = x.next();
            if (c == '-') {
                if (x.next() == '-') {
                    x.skipPast("-->");
                    return false;
                }
                x.back();
            } else if (c == '[') {
                token = x.nextToken();
                
                if ("CDATA".equals(token)) {
                    if (x.next() == '[') {
                        string = x.nextCDATA();
                        if (string.length() > 0) {
                            context.accumulate(config.getcDataTagName(), string);
                        }
                        return false;
                    }
                }
                throw x.syntaxError("Expected 'CDATA['");
            }
            i = 1;
            do {
                token = x.nextMeta();
                
                if (token == null) {
                    throw x.syntaxError("Missing '>' after '<!'.");
                } else if (token == LT) {
                    i += 1;
                } else if (token == GT) {
                    i -= 1;
                }
            } while (i > 0);
            return false;
        } else if (token == QUEST) {
            // <?
            x.skipPast("?>");
            return false;
        } else if (token == SLASH) {
            // Close tag </
            token = x.nextToken();
            
            //System.out.println("END TOKEN: " + token + " NAME: " + name);
          
            if (name == null) {
                throw x.syntaxError("Mismatched close tag " + token);
            }
            if (!token.equals(name)) {
                throw x.syntaxError("Mismatched " + name + " and " + token);
            }
            if (x.nextToken() != GT) {
                throw x.syntaxError("Misshaped close tag");
            }

            return true;

        } else if (token instanceof Character) {
            throw x.syntaxError("Misshaped tag");

            // Open tag <
            

        } else {
            tagName = (String) token;

            token = null;
            jsonObject = new JSONObject();
            boolean nilAttributeFound = false;
            xmlXsiTypeConverter = null;

            for (;;) {
                if (token == null) {
                    token = x.nextToken();
                }
                // attribute = value
                if (token instanceof String) {
     
                    string = (String) token;
                    token = x.nextToken();
                    

                    if (token == EQ) {
                        token = x.nextToken();
                        if (!(token instanceof String)) {
                            throw x.syntaxError("Missing value");
                        }

                        if (config.isConvertNilAttributeToNull()
                                && NULL_ATTR.equals(string)
                                && Boolean.parseBoolean((String) token)) {
                            nilAttributeFound = true;
                        } else if(config.getXsiTypeMap() != null && !config.getXsiTypeMap().isEmpty()
                                && TYPE_ATTR.equals(string)) {
                            xmlXsiTypeConverter = config.getXsiTypeMap().get(token);
                        } else if (!nilAttributeFound) {
                            jsonObject.accumulate(string,
                                    config.isKeepStrings()
                                            ? ((String) token)
                                            : stringToValue((String) token));
                        }
                        token = null;
                    } else {
                        jsonObject.accumulate(string, "");
                    }


                } else if (token == SLASH) {
                    // Empty tag <.../>
                    if (x.nextToken() != GT) {
                        throw x.syntaxError("Misshaped tag");
                    }
                    if (config.getForceList().contains(tagName)) {
                        // Force the value to be an array
                        if (nilAttributeFound) {
                            context.append(tagName, JSONObject.NULL);
                        } else if (jsonObject.length() > 0) {
                            context.append(tagName, jsonObject);
                        } else {
                            context.put(tagName, new JSONArray());
                        }
                    } else {
                        if (nilAttributeFound) {
                            context.accumulate(tagName, JSONObject.NULL);
                        } else if (jsonObject.length() > 0) {
                            context.accumulate(tagName, jsonObject);
                        } else {
                            context.accumulate(tagName, "");
                        }
                    }
                    return false;

                } else if (token == GT) {
                    // Content, between <...> and </...>
                    for (;;) {
                        token = x.nextContent();

                        if (token == null) {
                            if (tagName != null) {
                                throw x.syntaxError("Unclosed tag " + tagName);
                            }
                            return false;
                        } else if (token instanceof String) {

                            //System.out.println("TAG: " + tagName + ", STRING CONTENT: " + token);

                            string = (String) token;
                            if (string.length() > 0) {
                                if(xmlXsiTypeConverter != null) {
                                    jsonObject.accumulate(config.getcDataTagName(),
                                            stringToValue(string, xmlXsiTypeConverter));
                                } else {
                                    jsonObject.accumulate(config.getcDataTagName(),
                                            config.isKeepStrings() ? string : stringToValue(string));
                                }
                            }

                        } else if (token == LT) {

                          //System.out.println("TAG: " + tagName + ", NESTED CONTENT: " + token);

                
                            // Nested element
                            if (parse(x, jsonObject, tagName, config)) {
                                if (config.getForceList().contains(tagName)) {
                                    // Force the value to be an array
                                    if (jsonObject.length() == 0) {
                                        context.put(tagName, new JSONArray());
                                    } else if (jsonObject.length() == 1
                                            && jsonObject.opt(config.getcDataTagName()) != null) {
                                        context.append(tagName, jsonObject.opt(config.getcDataTagName()));
                                    } else {
                                        context.append(tagName, jsonObject);
                                    }
                                } else {

                                    if (jsonObject.length() == 0) {
                                        context.accumulate(tagName, "");
                                    } else if (jsonObject.length() == 1
                                            && jsonObject.opt(config.getcDataTagName()) != null) {
                                        context.accumulate(tagName, jsonObject.opt(config.getcDataTagName()));
                                    } else {
                                        context.accumulate(tagName, jsonObject);
                                    }
                                }
                                
                                return false;
                            }
                        }
                    }
                } else {
                    throw x.syntaxError("Misshaped tag");
                }
            }
        }
    }


    /**
     * This method tries to convert the given string value to the target object
     * @param string String to convert
     * @param typeConverter value converter to convert string to integer, boolean e.t.c
     * @return JSON value of this string or the string
     */
    public static Object stringToValue(String string, XMLXsiTypeConverter<?> typeConverter) {
        if(typeConverter != null) {
            return typeConverter.convert(string);
        }
        return stringToValue(string);
    }

    /**
     * This method is the same as {@link JSONObject#stringToValue(String)}.
     *
     * @param string String to convert
     * @return JSON value of this string or the string
     */
    // To maintain compatibility with the Android API, this method is a direct copy of
    // the one in JSONObject. Changes made here should be reflected there.
    // This method should not make calls out of the XML object.
    public static Object stringToValue(String string) {
        if ("".equals(string)) {
            return string;
        }

        // check JSON key words true/false/null
        if ("true".equalsIgnoreCase(string)) {
            return Boolean.TRUE;
        }
        if ("false".equalsIgnoreCase(string)) {
            return Boolean.FALSE;
        }
        if ("null".equalsIgnoreCase(string)) {
            return JSONObject.NULL;
        }

        /*
         * If it might be a number, try converting it. If a number cannot be
         * produced, then the value will just be a string.
         */

        char initial = string.charAt(0);
        if ((initial >= '0' && initial <= '9') || initial == '-') {
            try {
                return stringToNumber(string);
            } catch (Exception ignore) {
            }
        }
        return string;
    }
    
    /**
     * direct copy of {@link JSONObject#stringToNumber(String)} to maintain Android support.
     */
    private static Number stringToNumber(final String val) throws NumberFormatException {
        char initial = val.charAt(0);
        if ((initial >= '0' && initial <= '9') || initial == '-') {
            // decimal representation
            if (isDecimalNotation(val)) {
                // Use a BigDecimal all the time so we keep the original
                // representation. BigDecimal doesn't support -0.0, ensure we
                // keep that by forcing a decimal.
                try {
                    BigDecimal bd = new BigDecimal(val);
                    if(initial == '-' && BigDecimal.ZERO.compareTo(bd)==0) {
                        return Double.valueOf(-0.0);
                    }
                    return bd;
                } catch (NumberFormatException retryAsDouble) {
                    // this is to support "Hex Floats" like this: 0x1.0P-1074
                    try {
                        Double d = Double.valueOf(val);
                        if(d.isNaN() || d.isInfinite()) {
                            throw new NumberFormatException("val ["+val+"] is not a valid number.");
                        }
                        return d;
                    } catch (NumberFormatException ignore) {
                        throw new NumberFormatException("val ["+val+"] is not a valid number.");
                    }
                }
            }
            // block items like 00 01 etc. Java number parsers treat these as Octal.
            if(initial == '0' && val.length() > 1) {
                char at1 = val.charAt(1);
                if(at1 >= '0' && at1 <= '9') {
                    throw new NumberFormatException("val ["+val+"] is not a valid number.");
                }
            } else if (initial == '-' && val.length() > 2) {
                char at1 = val.charAt(1);
                char at2 = val.charAt(2);
                if(at1 == '0' && at2 >= '0' && at2 <= '9') {
                    throw new NumberFormatException("val ["+val+"] is not a valid number.");
                }
            }
            // integer representation.
            // This will narrow any values to the smallest reasonable Object representation
            // (Integer, Long, or BigInteger)
            
            // BigInteger down conversion: We use a similar bitLength compare as
            // BigInteger#intValueExact uses. Increases GC, but objects hold
            // only what they need. i.e. Less runtime overhead if the value is
            // long lived.
            BigInteger bi = new BigInteger(val);
            if(bi.bitLength() <= 31){
                return Integer.valueOf(bi.intValue());
            }
            if(bi.bitLength() <= 63){
                return Long.valueOf(bi.longValue());
            }
            return bi;
        }
        throw new NumberFormatException("val ["+val+"] is not a valid number.");
    }
    
    /**
     * direct copy of {@link JSONObject#isDecimalNotation(String)} to maintain Android support.
     */
    private static boolean isDecimalNotation(final String val) {
        return val.indexOf('.') > -1 || val.indexOf('e') > -1
                || val.indexOf('E') > -1 || "-0".equals(val);
    }


    /**
     * Convert a well-formed (but not necessarily valid) XML string into a
     * JSONObject. Some information may be lost in this transformation because
     * JSON is a data format and XML is a document format. XML uses elements,
     * attributes, and content text, while JSON uses unordered collections of
     * name/value pairs and arrays of values. JSON does not does not like to
     * distinguish between elements and attributes. Sequences of similar
     * elements are represented as JSONArrays. Content text may be placed in a
     * "content" member. Comments, prologs, DTDs, and <pre>{@code 
     * &lt;[ [ ]]>}</pre>
     * are ignored.
     *
     * @param string
     *            The source string.
     * @return A JSONObject containing the structured data from the XML string.
     * @throws JSONException Thrown if there is an errors while parsing the string
     */
    public static JSONObject toJSONObject(String string) throws JSONException {
        return toJSONObject(string, XMLParserConfiguration.ORIGINAL);
    }

    

    /**
     * Convert a well-formed (but not necessarily valid) XML into a
     * JSONObject. Some information may be lost in this transformation because
     * JSON is a data format and XML is a document format. XML uses elements,
     * attributes, and content text, while JSON uses unordered collections of
     * name/value pairs and arrays of values. JSON does not does not like to
     * distinguish between elements and attributes. Sequences of similar
     * elements are represented as JSONArrays. Content text may be placed in a
     * "content" member. Comments, prologs, DTDs, and <pre>{@code 
     * &lt;[ [ ]]>}</pre>
     * are ignored.
     *
     * @param reader The XML source reader.
     * @return A JSONObject containing the structured data from the XML string.
     * @throws JSONException Thrown if there is an errors while parsing the string
     */
    public static JSONObject toJSONObject(Reader reader) throws JSONException {
        return toJSONObject(reader, XMLParserConfiguration.ORIGINAL);
    }

    /**
     * Convert a well-formed (but not necessarily valid) XML into a
     * JSONObject. Some information may be lost in this transformation because
     * JSON is a data format and XML is a document format. XML uses elements,
     * attributes, and content text, while JSON uses unordered collections of
     * name/value pairs and arrays of values. JSON does not does not like to
     * distinguish between elements and attributes. Sequences of similar
     * elements are represented as JSONArrays. Content text may be placed in a
     * "content" member. Comments, prologs, DTDs, and <pre>{@code
     * &lt;[ [ ]]>}</pre>
     * are ignored.
     *
     * All values are converted as strings, for 1, 01, 29.0 will not be coerced to
     * numbers but will instead be the exact value as seen in the XML document.
     *
     * @param reader The XML source reader.
     * @param keepStrings If true, then values will not be coerced into boolean
     *  or numeric values and will instead be left as strings
     * @return A JSONObject containing the structured data from the XML string.
     * @throws JSONException Thrown if there is an errors while parsing the string
     */
    public static JSONObject toJSONObject(Reader reader, boolean keepStrings) throws JSONException {
        if(keepStrings) {
            return toJSONObject(reader, XMLParserConfiguration.KEEP_STRINGS);
        }
        return toJSONObject(reader, XMLParserConfiguration.ORIGINAL);
    }

    /**
     * Convert a well-formed (but not necessarily valid) XML into a
     * JSONObject. Some information may be lost in this transformation because
     * JSON is a data format and XML is a document format. XML uses elements,
     * attributes, and content text, while JSON uses unordered collections of
     * name/value pairs and arrays of values. JSON does not does not like to
     * distinguish between elements and attributes. Sequences of similar
     * elements are represented as JSONArrays. Content text may be placed in a
     * "content" member. Comments, prologs, DTDs, and <pre>{@code
     * &lt;[ [ ]]>}</pre>
     * are ignored.
     *
     * All values are converted as strings, for 1, 01, 29.0 will not be coerced to
     * numbers but will instead be the exact value as seen in the XML document.
     *
     * @param reader The XML source reader.
     * @param config Configuration options for the parser
     * @return A JSONObject containing the structured data from the XML string.
     * @throws JSONException Thrown if there is an errors while parsing the string
     */
    public static JSONObject toJSONObject(Reader reader, XMLParserConfiguration config) throws JSONException {
        JSONObject jo = new JSONObject();
        XMLTokener x = new XMLTokener(reader);
        
        while (x.more()) {
            x.skipPast("<");
            if(x.more()) {
                parse(x, jo, null, config);
            }
        }
        return jo;
    }

    /**
     * Convert a well-formed (but not necessarily valid) XML string into a
     * JSONObject. Some information may be lost in this transformation because
     * JSON is a data format and XML is a document format. XML uses elements,
     * attributes, and content text, while JSON uses unordered collections of
     * name/value pairs and arrays of values. JSON does not does not like to
     * distinguish between elements and attributes. Sequences of similar
     * elements are represented as JSONArrays. Content text may be placed in a
     * "content" member. Comments, prologs, DTDs, and <pre>{@code 
     * &lt;[ [ ]]>}</pre>
     * are ignored.
     *
     * All values are converted as strings, for 1, 01, 29.0 will not be coerced to
     * numbers but will instead be the exact value as seen in the XML document.
     *
     * @param string
     *            The source string.
     * @param keepStrings If true, then values will not be coerced into boolean
     *  or numeric values and will instead be left as strings
     * @return A JSONObject containing the structured data from the XML string.
     * @throws JSONException Thrown if there is an errors while parsing the string
     */
    public static JSONObject toJSONObject(String string, boolean keepStrings) throws JSONException {
        return toJSONObject(new StringReader(string), keepStrings);
    }

    /**
     * Convert a well-formed (but not necessarily valid) XML string into a
     * JSONObject. Some information may be lost in this transformation because
     * JSON is a data format and XML is a document format. XML uses elements,
     * attributes, and content text, while JSON uses unordered collections of
     * name/value pairs and arrays of values. JSON does not does not like to
     * distinguish between elements and attributes. Sequences of similar
     * elements are represented as JSONArrays. Content text may be placed in a
     * "content" member. Comments, prologs, DTDs, and <pre>{@code 
     * &lt;[ [ ]]>}</pre>
     * are ignored.
     *
     * All values are converted as strings, for 1, 01, 29.0 will not be coerced to
     * numbers but will instead be the exact value as seen in the XML document.
     *
     * @param string
     *            The source string.
     * @param config Configuration options for the parser.
     * @return A JSONObject containing the structured data from the XML string.
     * @throws JSONException Thrown if there is an errors while parsing the string
     */
    public static JSONObject toJSONObject(String string, XMLParserConfiguration config) throws JSONException {
        return toJSONObject(new StringReader(string), config);
    }

    /**
     * Convert a JSONObject into a well-formed, element-normal XML string.
     *
     * @param object
     *            A JSONObject.
     * @return A string.
     * @throws JSONException Thrown if there is an error parsing the string
     */
    public static String toString(Object object) throws JSONException {
        return toString(object, null, XMLParserConfiguration.ORIGINAL);
    }

    /**
     * Convert a JSONObject into a well-formed, element-normal XML string.
     *
     * @param object
     *            A JSONObject.
     * @param tagName
     *            The optional name of the enclosing tag.
     * @return A string.
     * @throws JSONException Thrown if there is an error parsing the string
     */
    public static String toString(final Object object, final String tagName) {
        return toString(object, tagName, XMLParserConfiguration.ORIGINAL);
    }

    /**
     * Convert a JSONObject into a well-formed, element-normal XML string.
     *
     * @param object
     *            A JSONObject.
     * @param tagName
     *            The optional name of the enclosing tag.
     * @param config
     *            Configuration that can control output to XML.
     * @return A string.
     * @throws JSONException Thrown if there is an error parsing the string
     */
    public static String toString(final Object object, final String tagName, final XMLParserConfiguration config)
            throws JSONException {
        return toString(object, tagName, config, 0, 0);
    }

    /**
     * Convert a JSONObject into a well-formed, element-normal XML string,
     * either pretty print or single-lined depending on indent factor.
     *
     * @param object
     *            A JSONObject.
     * @param tagName
     *            The optional name of the enclosing tag.
     * @param config
     *            Configuration that can control output to XML.
     * @param indentFactor
     *            The number of spaces to add to each level of indentation.
     * @param indent
     *            The current ident level in spaces.
     * @return
     * @throws JSONException
     */
    private static String toString(final Object object, final String tagName, final XMLParserConfiguration config, int indentFactor, int indent)
            throws JSONException {
        StringBuilder sb = new StringBuilder();
        JSONArray ja;
        JSONObject jo;
        String string;

        if (object instanceof JSONObject) {

            // Emit <tagName>
            if (tagName != null) {
                sb.append(indent(indent));
                sb.append('<');
                sb.append(tagName);
                sb.append('>');
                if(indentFactor > 0){
                    sb.append("\n");
                    indent += indentFactor;
                }
            }

            // Loop thru the keys.
            // don't use the new entrySet accessor to maintain Android Support
            jo = (JSONObject) object;
            for (final String key : jo.keySet()) {
                Object value = jo.opt(key);
                if (value == null) {
                    value = "";
                } else if (value.getClass().isArray()) {
                    value = new JSONArray(value);
                }

                // Emit content in body
                if (key.equals(config.getcDataTagName())) {
                    if (value instanceof JSONArray) {
                        ja = (JSONArray) value;
                        int jaLength = ja.length();
                        // don't use the new iterator API to maintain support for Android
						for (int i = 0; i < jaLength; i++) {
                            if (i > 0) {
                                sb.append('\n');
                            }
                            Object val = ja.opt(i);
                            sb.append(escape(val.toString()));
                        }
                    } else {
                        sb.append(escape(value.toString()));
                    }

                    // Emit an array of similar keys

                } else if (value instanceof JSONArray) {
                    ja = (JSONArray) value;
                    int jaLength = ja.length();
                    // don't use the new iterator API to maintain support for Android
					for (int i = 0; i < jaLength; i++) {
                        Object val = ja.opt(i);
                        if (val instanceof JSONArray) {
                            sb.append('<');
                            sb.append(key);
                            sb.append('>');
                            sb.append(toString(val, null, config, indentFactor, indent));
                            sb.append("</");
                            sb.append(key);
                            sb.append('>');
                        } else {
                            sb.append(toString(val, key, config, indentFactor, indent));
                        }
                    }
                } else if ("".equals(value)) {
                    sb.append(indent(indent));
                    sb.append('<');
                    sb.append(key);
                    sb.append("/>");
                    if(indentFactor > 0){
                        sb.append("\n");
                    }

                    // Emit a new tag <k>

                } else {
                    sb.append(toString(value, key, config, indentFactor, indent));
                }
            }
            if (tagName != null) {

                // Emit the </tagName> close tag
                sb.append(indent(indent - indentFactor));
                sb.append("</");
                sb.append(tagName);
                sb.append('>');
                if(indentFactor > 0){
                    sb.append("\n");
                }
            }
            return sb.toString();

        }

        if (object != null && (object instanceof JSONArray ||  object.getClass().isArray())) {
            if(object.getClass().isArray()) {
                ja = new JSONArray(object);
            } else {
                ja = (JSONArray) object;
            }
            int jaLength = ja.length();
            // don't use the new iterator API to maintain support for Android
			for (int i = 0; i < jaLength; i++) {
                Object val = ja.opt(i);
                // XML does not have good support for arrays. If an array
                // appears in a place where XML is lacking, synthesize an
                // <array> element.
                sb.append(toString(val, tagName == null ? "array" : tagName, config, indentFactor, indent));
            }
            return sb.toString();
        }


        string = (object == null) ? "null" : escape(object.toString());

        if(tagName == null){
            return indent(indent) + "\"" + string + "\"" + ((indentFactor > 0) ? "\n" : "");
        } else if(string.length() == 0){
            return indent(indent) + "<" + tagName + "/>" + ((indentFactor > 0) ? "\n" : "");
        } else {
            return indent(indent) + "<" + tagName
                    + ">" + string + "</" + tagName + ">" + ((indentFactor > 0) ? "\n" : "");
        }
    }

    /**
     * Convert a JSONObject into a well-formed, pretty printed element-normal XML string.
     *
     * @param object
     *            A JSONObject.
     * @param indentFactor
     *            The number of spaces to add to each level of indentation.
     * @return A string.
     * @throws JSONException Thrown if there is an error parsing the string
     */
    public static String toString(Object object, int indentFactor){
        return toString(object, null, XMLParserConfiguration.ORIGINAL, indentFactor);
    }

    /**
     * Convert a JSONObject into a well-formed, pretty printed element-normal XML string.
     *
     * @param object
     *            A JSONObject.
     * @param tagName
     *            The optional name of the enclosing tag.
     * @param indentFactor
     *            The number of spaces to add to each level of indentation.
     * @return A string.
     * @throws JSONException Thrown if there is an error parsing the string
     */
    public static String toString(final Object object, final String tagName, int indentFactor) {
        return toString(object, tagName, XMLParserConfiguration.ORIGINAL, indentFactor);
    }

    /**
     * Convert a JSONObject into a well-formed, pretty printed element-normal XML string.
     *
     * @param object
     *            A JSONObject.
     * @param tagName
     *            The optional name of the enclosing tag.
     * @param config
     *            Configuration that can control output to XML.
     * @param indentFactor
     *            The number of spaces to add to each level of indentation.
     * @return A string.
     * @throws JSONException Thrown if there is an error parsing the string
     */
    public static String toString(final Object object, final String tagName, final XMLParserConfiguration config, int indentFactor)
            throws JSONException {
        return toString(object, tagName, config, indentFactor, 0);
    }

    /**
     * Return a String consisting of a number of space characters specified by indent
     *
     * @param indent
     *          The number of spaces to be appended to the String.
     * @return
     */
    private static final String indent(int indent) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < indent; i++) {
            sb.append(' ');
        }
        return sb.toString();
    }


    /**------ Milestone 2 Overloaded Parse Function 1 ----------**/
    private static boolean parse(XMLTokener x, JSONObject context, String name, XMLParserConfiguration config, 
    Stack<String> pathStack, Map<String, Integer> pathMap, Set<String> pathSet, String lastKey, String nestedKey, Boolean isNested, Boolean canParse, Boolean isArray
    , JSONObject contextCopy)
            throws JSONException {
        char c;
        int i;
        JSONObject jsonObject = null;
        String string;
        String tagName;
        Object token;
        XMLXsiTypeConverter<?> xmlXsiTypeConverter;

        // Test for and skip past these forms:
        // <!-- ... -->
        // <! ... >
        // <![ ... ]]>
        // <? ... ?>
        // Report errors for these forms:
        // <>
        // <=
        // <<

        token = x.nextToken();
        // <!

        if (token == BANG) {
            c = x.next();
            if (c == '-') {
                if (x.next() == '-') {
                    x.skipPast("-->");
                    return false;
                }
                x.back();
            } else if (c == '[') {
                token = x.nextToken();
                
                if ("CDATA".equals(token)) {
                    if (x.next() == '[') {
                        string = x.nextCDATA();
                        if (string.length() > 0) {
                            context.accumulate(config.getcDataTagName(), string);
                        }
                        return false;
                    }
                }
                throw x.syntaxError("Expected 'CDATA['");
            }
            i = 1;
            do {
                token = x.nextMeta();
                
                if (token == null) {
                    throw x.syntaxError("Missing '>' after '<!'.");
                } else if (token == LT) {
                    i += 1;
                } else if (token == GT) {
                    i -= 1;
                }
            } while (i > 0);
            return false;
        } else if (token == QUEST) {
            // <?
            x.skipPast("?>");
            return false;
        } else if (token == SLASH) {
            // Close tag </
            token = x.nextToken();

            //System.out.println("CLOSING TOKEN: " + token + " last Key: " + lastKey + ", NESTED KEY: " + nestedKey + ", NESTED KEY COUNT: " + pathMap.get(nestedKey));
 
            if (name == null) {
                //CHANGE
                //case 1: check if tag exists in the path using the Map (since it is likely to have been deleted)
                if(pathMap.containsKey(token))
                  return true; //if return true, then we intend to end it early

                throw x.syntaxError("Mismatched close tag " + token);
            }
            if (!token.equals(name)) {
                throw x.syntaxError("Mismatched " + name + " and " + token);
            }
            if (x.nextToken() != GT) {
                throw x.syntaxError("Misshaped close tag");
            }

     

            return true;

        } else if (token instanceof Character) {
            throw x.syntaxError("Misshaped tag");

            // Open tag <
            

        } else {
            tagName = (String) token;

            //System.out.println("TAGNAME: " + tagName);

            //find tag that is after the lastKey and quit, helpful after an array of unknown length (lastKey = -1 in pathMap)
            //check if the stack is 0, nestedKey is in the path (but before the lastKey)
            if(pathStack.size() == 0 && !tagName.equals(lastKey) && !nestedKey.equals(lastKey) && pathMap.containsKey(nestedKey)
            && pathMap.get(lastKey) == -1){
              System.out.println("SKIP PAST Possible tag: " + tagName);
              pathMap.put(lastKey, -2); //EXIT EARLY CONDITION
              return true;
            }
            //remove the current path from stack
            else if(pathStack.size() >=  1 &&  token.equals(pathStack.peek()) ){
              if(isNested){
                if(pathMap.containsKey(nestedKey))
                  pathStack.pop();
              }
              else{
                pathStack.pop();
              }
              
              //System.out.println("POP FROM STACK: " + token + ", TAG COUNT: " + pathMap.get(token) +", STACK SIZE: " + pathStack.size());
            }

            token = null;
            jsonObject = new JSONObject();
            boolean nilAttributeFound = false;
            xmlXsiTypeConverter = null;

            for (;;) {
                if (token == null) {
                    token = x.nextToken();
                }
                // attribute = value
                if (token instanceof String) {
     
                    string = (String) token;
                    token = x.nextToken();
                    

                    if (token == EQ) {
                        token = x.nextToken();
                        if (!(token instanceof String)) {
                            throw x.syntaxError("Missing value");
                        }

                        //System.out.println("EQUALS TOKEN: " + token + ", TAG NAME: " + tagName + ", NESTED: " + string);

                        if(pathStack.size() == 1){
                          if(string.equals(lastKey)){
                          if(isNested){
                            if(pathMap.containsKey(nestedKey))
                              pathStack.pop();
                          }

                            contextCopy.put(string, token);

                            pathMap.put(lastKey, -2); //exit
                            return true;
                          }
                        }
                        

                        if (config.isConvertNilAttributeToNull()
                                && NULL_ATTR.equals(string)
                                && Boolean.parseBoolean((String) token)) {
                            nilAttributeFound = true;
                        } else if(config.getXsiTypeMap() != null && !config.getXsiTypeMap().isEmpty()
                                && TYPE_ATTR.equals(string)) {
                            xmlXsiTypeConverter = config.getXsiTypeMap().get(token);
                        } else if (!nilAttributeFound) {

                            jsonObject.accumulate(string,
                                    config.isKeepStrings()
                                            ? ((String) token)
                                            : stringToValue((String) token));
                        }

 
                        token = null;
                    } else {
                        jsonObject.accumulate(string, "");
                    }


                } else if (token == SLASH) {
                    // Empty tag <.../>
                    if (x.nextToken() != GT) {
                        throw x.syntaxError("Misshaped tag");
                    }
                    if (config.getForceList().contains(tagName)) {
                        // Force the value to be an array
                        if (nilAttributeFound) {
                            context.append(tagName, JSONObject.NULL);
                        } else if (jsonObject.length() > 0) {
                            context.append(tagName, jsonObject);
                        } else {
                            context.put(tagName, new JSONArray());
                        }
                    } else {
                        if (nilAttributeFound) {
                            context.accumulate(tagName, JSONObject.NULL);
                        } else if (jsonObject.length() > 0) {
                            context.accumulate(tagName, jsonObject);
                        } else {
                            context.accumulate(tagName, "");
                        }
                    }
                    return false;

                } 
                else if (token == GT) {
                    // Content, between <...> and </...>
                    for (;;) {
                        token = x.nextContent();

                        if (token == null) {
                            if (tagName != null) {
                                throw x.syntaxError("Unclosed tag " + tagName);
                            }
                            return false;
                        } else if (token instanceof String) {
       
                          //System.out.println("TAG: " + tagName + ", STACK CONTENT: " + token + ", LAST KEY: " + lastKey + ", NESTED: " + isNested + ", NESTED: " + nestedKey);
                          
                      
                          
                          if(pathStack.size() == 0 && tagName.equals(lastKey) && isNested == false){
                            //System.out.println("HEYA10");
                            contextCopy.put(tagName, token);

                            if(pathMap.get(lastKey) == -1){ //just a string
                              pathMap.put(lastKey, pathMap.get(lastKey) - 1);
                            }
                            return true;
                          }
                          else{
                            isArray = false;
                          }
                          
                          /* 
                          System.out.println(jsonObject);
                          System.out.println(jsonObject.keySet());
                          */

                            string = (String) token;
                            if (string.length() > 0) {
                                if(xmlXsiTypeConverter != null) {
                                    jsonObject.accumulate(config.getcDataTagName(),
                                            stringToValue(string, xmlXsiTypeConverter));
                                } else {
                                    jsonObject.accumulate(config.getcDataTagName(),
                                            config.isKeepStrings() ? string : stringToValue(string));
                                     
                                }
                            }

                        } else if (token == LT) {

                          
                          // Nested element
                          if (parse(x, jsonObject, tagName, XMLParserConfiguration.ORIGINAL, pathStack , pathMap, pathSet , lastKey, tagName, true, canParse, isArray, contextCopy)) {
                            
                            //START OF CHANGE -----
                            //System.out.println("STACK SIZE: " + pathStack.size() + ", TAGNAME: " + tagName + ", CURRENT STACK: " + pathStack.size() + ", COUNT: " + pathMap.get(lastKey) + "NESTED KEY: " + nestedKey);
                            

                            //exit early
                            if(pathStack.size() == 0 && !tagName.equals(lastKey) && !nestedKey.equals(lastKey) && pathMap.containsKey(nestedKey)
                            && pathMap.get(lastKey) == -1){
                              //System.out.println("SKIP PAST Possible tag: " + tagName);
                              pathMap.put(lastKey, -2); //EXIT EARLY CONDITION
                              return true;
                            }

                            //QUITING EARLY OUT OF PARSE CASE
                            if(pathMap.get(lastKey) == -2){
                              return true; 
                            }
                            //System.out.println(jsonObject);
                            //System.out.println(jsonObject.keySet());

                            //if array index specified at the very end of the key path
                            if(pathStack.size() == 0 && tagName.equals(lastKey) && pathMap.get(lastKey) >= 1){
                              //System.out.println("HEYA");

                              //if pathStack.size() is 0, then at index
                              if(jsonObject.keySet().contains("content"))
                                contextCopy.put(tagName, jsonObject.remove("content"));           
                              else
                                contextCopy.put(tagName, jsonObject);

                              pathMap.put(lastKey, -2); //early exit case
                    
                            }
                            
                            //if no array index specified at the end of the key path
                            if(pathStack.size() == 0 && tagName.equals(lastKey) && pathMap.get(lastKey) == -1){

                              if(jsonObject.keySet().contains("content") && pathStack.size() == 0){
                                //System.out.println("HEYA2");

                                //System.out.println(pathMap.get(nestedKey) + " " + isArray);

                                JSONObject temp = new JSONObject();
                                temp.put(tagName, jsonObject.remove("content"));
                                
                                if(pathMap.containsKey(nestedKey) && pathMap.get(nestedKey) == -1){
                                  contextCopy.accumulate( tagName, temp.remove(tagName));
                                }
                                else if (pathMap.containsKey(nestedKey) && pathMap.get(nestedKey) >= 1){
                                  contextCopy.put( tagName, temp.remove(tagName));
                                  pathMap.put(lastKey, -2); //exit
                                }
                                       
                              }
                              else if(isArray && pathStack.size() == 0){
                                //System.out.println("HEYA3");
                                //System.out.println(pathMap.get(nestedKey));

                                if(pathMap.get(nestedKey) == null || pathMap.get(nestedKey) >= -1)
                                  contextCopy.accumulate(tagName, jsonObject);
                              }        
                            }

                            //End of change

                            if (config.getForceList().contains(tagName)) {
                                  // Force the value to be an array
                                  if (jsonObject.length() == 0) {
                                      context.put(tagName, new JSONArray());
                                  } else if (jsonObject.length() == 1
                                          && jsonObject.opt(config.getcDataTagName()) != null) {
                                      context.append(tagName, jsonObject.opt(config.getcDataTagName()));
                                  } else {
                                      context.append(tagName, jsonObject);
                                  }
                              } else {

                              //END OF CHANGE ----------------------------

                                  /* 
                                  //CHANGE
                                  //if not in path...
                                  if(!pathMap.containsKey(tagName) && !canParse){
                                    return false;
                                  }
                                  */
                                  if (jsonObject.length() == 0) {
                                      context.accumulate(tagName, "");
                                  } else if (jsonObject.length() == 1
                                          && jsonObject.opt(config.getcDataTagName()) != null) {
                                      context.accumulate(tagName, jsonObject.opt(config.getcDataTagName()));
                                  } else {
                                      context.accumulate(tagName, jsonObject);
                                  }
                              }
                              
                              return false;
                            }
                        }
                    }
                } else {
                    throw x.syntaxError("Misshaped tag");
                }
            }
        }
    }
    
    /**---- Milestone 2 Overloaded Parse Function 2 ----*/
    private static boolean parse(XMLTokener x, JSONObject context, String name, XMLParserConfiguration config,
    Stack<String> pathStack, Map<String, Integer> pathMap, String lastKey, String nestedKey, JSONObject replacement) throws JSONException {
        char c;
        int i;
        JSONObject jsonObject = null;
        String string;
        String tagName;
        Object token;
        XMLXsiTypeConverter<?> xmlXsiTypeConverter;

        // Test for and skip past these forms:
        // <!-- ... -->
        // <! ... >
        // <![ ... ]]>
        // <? ... ?>
        // Report errors for these forms:
        // <>
        // <=
        // <<

        token = x.nextToken();
        // <!

        if (token == BANG) {
            c = x.next();
            if (c == '-') {
                if (x.next() == '-') {
                    x.skipPast("-->");
                    return false;
                }
                x.back();
            } else if (c == '[') {
                token = x.nextToken();
                
                if ("CDATA".equals(token)) {
                    if (x.next() == '[') {
                        string = x.nextCDATA();
                        if (string.length() > 0) {
                            context.accumulate(config.getcDataTagName(), string);
                        }
                        return false;
                    }
                }
                throw x.syntaxError("Expected 'CDATA['");
            }
            i = 1;
            do {
                token = x.nextMeta();
                
                if (token == null) {
                    throw x.syntaxError("Missing '>' after '<!'.");
                } else if (token == LT) {
                    i += 1;
                } else if (token == GT) {
                    i -= 1;
                }
            } while (i > 0);
            return false;
        } else if (token == QUEST) {
            // <?
            x.skipPast("?>");
            return false;
        } else if (token == SLASH) {
            // Close tag </
            token = x.nextToken();
            
            //System.out.println("END TOKEN: " + token + " NAME: " + name);

          
            if (name == null) {
                throw x.syntaxError("Mismatched close tag " + token);
            }
            if (!token.equals(name)) {
                throw x.syntaxError("Mismatched " + name + " and " + token);
            }
            if (x.nextToken() != GT) {
                throw x.syntaxError("Misshaped close tag");
            }

            return true;

        } else if (token instanceof Character) {
            throw x.syntaxError("Misshaped tag");

            // Open tag <
            

        } else {
            tagName = (String) token;

            //System.out.println("TAGNAME: " + tagName);

            if(pathStack.size() >=  1 &&  tagName.equals(pathStack.peek()) ){
              pathStack.pop();
              //System.out.println("POP FROM STACK REPLACEMENT: " + tagName + ", TAG COUNT: " + pathMap.get(token) +", STACK SIZE: " + pathStack.size());
            }

            token = null;
            jsonObject = new JSONObject();
            boolean nilAttributeFound = false;
            xmlXsiTypeConverter = null;

            for (;;) {
                if (token == null) {
                    token = x.nextToken();
                }
                // attribute = value
                if (token instanceof String) {
     
                    string = (String) token;
                    token = x.nextToken();
                    

                    if (token == EQ) {
                        token = x.nextToken();
                        if (!(token instanceof String)) {
                            throw x.syntaxError("Missing value");
                        }

                        if (config.isConvertNilAttributeToNull()
                                && NULL_ATTR.equals(string)
                                && Boolean.parseBoolean((String) token)) {
                            nilAttributeFound = true;
                        } else if(config.getXsiTypeMap() != null && !config.getXsiTypeMap().isEmpty()
                                && TYPE_ATTR.equals(string)) {
                            xmlXsiTypeConverter = config.getXsiTypeMap().get(token);
                        } else if (!nilAttributeFound) {

                          
                          if(pathStack.size() == 1 && string.equals(lastKey)){
                            //System.out.println("EQUALS: " + string);
                            pathStack.pop();
                            pathMap.put(string, -2);
                            jsonObject.accumulate(string, replacement);
                          }
                          else{
                            //System.out.println(string + " " + jsonObject);
                              jsonObject.accumulate(string,
                                      config.isKeepStrings()
                                              ? ((String) token)
                                              : stringToValue((String) token));
                          }
                         
                        }
                        token = null;
                    } else {
                        jsonObject.accumulate(string, "");
                    }


                } else if (token == SLASH) {
                    // Empty tag <.../>
                    if (x.nextToken() != GT) {
                        throw x.syntaxError("Misshaped tag");
                    }
                    if (config.getForceList().contains(tagName)) {
                        // Force the value to be an array
                        if (nilAttributeFound) {
                            context.append(tagName, JSONObject.NULL);
                        } else if (jsonObject.length() > 0) {
                            context.append(tagName, jsonObject);
                        } else {
                            context.put(tagName, new JSONArray());
                        }
                    } else {
                        if (nilAttributeFound) {
                            context.accumulate(tagName, JSONObject.NULL);
                        } else if (jsonObject.length() > 0) {
                            context.accumulate(tagName, jsonObject);
                        } else {
                            context.accumulate(tagName, "");
                        }
                    }
                    return false;

                } else if (token == GT) {
                    // Content, between <...> and </...>
                    for (;;) {
                        token = x.nextContent();

                        if (token == null) {
                            if (tagName != null) {
                                throw x.syntaxError("Unclosed tag " + tagName);
                            }
                            return false;
                        } else if (token instanceof String) {

                            //System.out.println("TAG: " + tagName + ", STRING CONTENT: " + token);

                            string = (String) token;
                            if (string.length() > 0) {
                                if(xmlXsiTypeConverter != null) {
                                    jsonObject.accumulate(config.getcDataTagName(),
                                            stringToValue(string, xmlXsiTypeConverter));
                                } else {
                                    jsonObject.accumulate(config.getcDataTagName(),
                                            config.isKeepStrings() ? string : stringToValue(string));
                                }
                            }

                        } else if (token == LT) {


                            // Nested element
                            if (parse(x, jsonObject, tagName, config, pathStack, pathMap, lastKey, tagName, replacement)) {
                                //CHANGE
                                //System.out.println("STACK SIZE: " + pathStack.size() + ", TAGNAME: " + tagName + ", CURRENT STACK: " + pathStack.size() + ", COUNT: " + pathMap.get(tagName) + ", NESTED KEY : " + nestedKey);

                                if (config.getForceList().contains(tagName)) {
                                    // Force the value to be an array
                                    if (jsonObject.length() == 0) {
                                        context.put(tagName, new JSONArray());
                                    } else if (jsonObject.length() == 1
                                            && jsonObject.opt(config.getcDataTagName()) != null) {
                                        context.append(tagName, jsonObject.opt(config.getcDataTagName()));
                                    } else {
                                        context.append(tagName, jsonObject);
                                    }
                                } else {

                                    if (jsonObject.length() == 0) {
                                        context.accumulate(tagName, "");
                                    } else if (jsonObject.length() == 1
                                            && jsonObject.opt(config.getcDataTagName()) != null) {

                                              /* 
                                              //find path error
                                              if(pathStack.size() == 0 && tagName.equals(lastKey)){
                                                if(nestedKey != "" && !pathMap.containsKey(nestedKey)){
                                                  throw new JSONException("Path is not realized");
                                                }
                                              }
                                        */
                                        
                                              //String based 
                                              if(pathStack.size()  == 0 && tagName.equals(lastKey) && pathMap.get(tagName) != -1 ) {
                                                //System.out.println("HERE2");
                                                //System.out.println(tagName + " " + jsonObject + ", KEY COUNT: " + pathMap.get(tagName));
                                            
                                                if(pathMap.get(tagName) == 1 || pathMap.get(tagName) == 0){ //is an array
                                                  //System.out.println("replace this02");
                                                 
                                                  context.accumulate(lastKey, replacement);    
                                                  pathMap.put(lastKey, -2); //don't replace the rest of array indexes afterwards
                                                }
                                                else if(pathMap.get(tagName) == -2){ //get the original values instead of replacement
                                                  //System.out.println("Not the right index to replace for string");

                                                  context.accumulate(tagName, jsonObject.opt(config.getcDataTagName()));
                                                }
                                                else if(pathMap.get(tagName) == -3){
                                                  //System.out.println("PATH KEY COUNT FOR " + tagName + ": " + pathMap.containsKey(nestedKey));

                                                  if(!tagName.equals(lastKey) && pathMap.get(lastKey) == -3){

                                                    //System.out.println("PATH1");
                                                    context.accumulate(tagName, jsonObject.opt(config.getcDataTagName()));
                                                  }
                                                  else if(tagName.equals(lastKey) && !tagName.equals(nestedKey)){
                                                    //System.out.println("PATH2");


                                                    context.accumulate(tagName, jsonObject.opt(config.getcDataTagName()));
                                                  }
                                                  
                                                }

                                                //END OF REPLACE
                                                
                                              }
                                              else{
                                                if(tagName.equals(lastKey) && pathMap.get(tagName) > 0)
                                                  pathMap.put(tagName, pathMap.get(tagName) - 1);

                                      
                                                if(pathStack.size() == 0 && tagName.equals(lastKey)){
                                                                                            
                                                  //if(pathMap.containsKey(nestedKey)) System.out.println("NESTED KEY: " + pathMap.get(nestedKey));
                                                  //System.out.println("TAGNAME CONTEXT for " + tagName + ":: " + nestedKey);

                                                  if(!pathMap.containsKey(nestedKey) || pathMap.get(nestedKey) != -1){
                                                      pathMap.put(lastKey, -2);
                                                  }

                                                  context.put(lastKey, replacement); 
                                                
                                                } 
                                                else{
                                                  context.accumulate(tagName, jsonObject.opt(config.getcDataTagName()));
                                                }
  
                                              }

                                      
                                    } else {
                                      
                                    
                                      if(pathStack.size()  == 0 && tagName.equals(lastKey)) {
                                        //System.out.println("HERE1" + ", STACK: " +pathStack.size());
                                        //System.out.println(tagName + " " + jsonObject);

                                        if(pathMap.containsKey(tagName))
                                          //System.out.println("HERE1 Replace: " + pathMap.get(tagName));
                       
                                        //replace with the replacement JSONObject
                                        if(pathMap.get(tagName) == -1){ //Not an array
                                          //System.out.println("replace this");

                                          context.accumulate(lastKey, replacement);
                                          pathMap.put(lastKey, -3);

                                        }
                                        else if(pathMap.get(tagName) == 1 || pathMap.get(tagName) == 0){ //is an array
                                          //System.out.println("replace this01");

                                          context.accumulate(lastKey, replacement);
                                          pathMap.put(lastKey, -2);
                                        }
                                        else if(pathMap.get(lastKey) == -2){
                                          //System.out.println("replace none");
                                          context.accumulate(tagName, jsonObject);
                                        }

                                  
                                      }
                                      else{
                                        if(tagName.equals(lastKey) && pathMap.get(tagName) > 0)
                                          pathMap.put(tagName, pathMap.get(tagName) - 1);
                                        else if(!tagName.equals(lastKey) && pathMap.get(lastKey) == -3){
                                          
                                          //System.out.println("After -3: " + tagName);
                                          //pathMap.put(lastKey, -2);
                                        }
                                                                                  
                                        context.accumulate(tagName, jsonObject);
                                      }
                                    }
                                }
                                
                                return false;
                            }
                        }
                    }
                } else {
                    throw x.syntaxError("Misshaped tag");
                }
            }
        }
    }

    //------------------------------
    //Milestone 2 Overloaded Functions

    //modify reader string
    public static JSONObject toJSONObject(Reader reader, JSONPointer path) throws JSONException {
      checkPathForSyntaxErrors(path);

      JSONObject jo = new JSONObject();
      XMLTokener x = new XMLTokener(reader);
      Map<String, Integer> map = new HashMap<String,Integer>();
      Stack<String> stack = new Stack<String>(); //keep track of the path
      Set<String> set = new HashSet<String>();
      String lastKey = getLastKey(path); //reference to the very last path 
      Boolean isNested = false;
      String nestedKey = "";

      getPathCountMap(map, path);
      getPathStack(stack, path);

      JSONObject copy = new JSONObject();

      while (x.more()) {
          x.skipPast("<");
          if(x.more()) {
              //System.out.println("KEY: " + map.get(lastKey));
              
               if(map.get(lastKey) == -2){ //when the lastKey in the path is array-indexed
                break;
              }
                Boolean parsed = parse(x, jo, null, XMLParserConfiguration.ORIGINAL, stack, map, set, lastKey, nestedKey, isNested, false, true, copy);
                //System.out.println(parsed);

                if(parsed){
                  break;
                }       
              }
      }

  
      if(stack.size() > 0){
        throw new JSONException("Path is not realized");
      }

      return copy;
    }

    private static String getLastKey(JSONPointer path){
      String[] pathWay = path.toString().split("/");
      if(pathWay.length > 1){
        if(pathWay[pathWay.length - 1].matches("^(0|[1-9][0-9]*)$"))
          return pathWay[pathWay.length - 2];
        else return pathWay[pathWay.length - 1];
      }
      return "";
    }

    private static void getPathStack(Stack<String> stack, JSONPointer path){
      String[] pathWay = path.toString().split("/");

      for(int i = pathWay.length - 1; i > 0; i--){
        //check if it is a number or a string
        //if get number n, get the stack topmost element and repeat it n + 1 times

        if(pathWay[i].matches("^(0|[1-9][0-9]*)$")) {    
          //count array
       
          int num = Integer.parseInt(pathWay[i]);
          for(int x = 0; x < num; x++){
            stack.push(pathWay[i-1]);
            //System.out.println("Pushed: " + pathWay[i - 1]);
          }
          
          i = i - 1;
        }
        stack.push(pathWay[i]);
        //System.out.println("STRING: " + pathWay[i]);     
      }


    }

    private static void getPathCountMap(Map<String, Integer> map, JSONPointer path){
      String[] pathWay = path.toString().split("/");

      for(int i = 1; i < pathWay.length; i++){
          if(pathWay[i].matches("^(0|[1-9][0-9]*)$")) {
            //if there is a stack that pushes according to array index, 
            //then set count to 1

            int count = Integer.parseInt(pathWay[i]) + 1;

            //System.out.println(pathWay[i-1] + " - " +count);
            //int count = 1;
            map.put(pathWay[i - 1],  count); //JSONArray
          }
          else{
            map.put(pathWay[i], -1); //not an JSONArray
          }
      }

      //Add a map for the closed tag of the last key
      if(pathWay.length > 1){
        map.put("</" + getLastKey(path), 1);
      }
            
    }

    public static JSONObject toJSONObject(Reader reader, JSONPointer path, JSONObject replacement){
      checkPathForSyntaxErrors(path);

      
      JSONObject jo = new JSONObject();
      JSONObject copy = new JSONObject();

      Map<String, Integer> map = new HashMap<String,Integer>();
      Stack<String> stack = new Stack<String>(); //keep track of the path
      String keyPathString = path.toString();
      String[] pathWay = keyPathString.split("/");
      List<String> arr = new ArrayList<String>(Arrays.asList(pathWay));
      String lastKey = getLastKey(path); //reference to the very last path 
      String nestedKey = ""; //reference to the very last path 

      arr.remove(0);

      getPathCountMap(map, path);
      getPathStack(stack, path);

      //Approach 1
      //parse entire reader file as a JSONObject then insert the JSONObject replacement?
      //then use helper function to get to path, traverse JSONObject and replace

      //Approach 2
      //Convert JSONObject replacement to a XML String?
      //Have 1 string when reach the path, 2nd string (replacement)
      //Skip to the closing tag of path required and concatenate all 3 strings together

      XMLTokener x = new XMLTokener(reader);
      
      while (x.more()) {
          x.skipPast("<");
          if(x.more()) {
              parse(x, jo, null, XMLParserConfiguration.ORIGINAL, stack, map, lastKey, nestedKey, replacement);
          }
      }

      if(stack.size() > 0){
        throw new JSONException("Path is not realized");
      }

      return jo;
    }

    private static void checkPathForSyntaxErrors(JSONPointer path){
      String keyPathString = path.toString();
      String[] pathWay = keyPathString.split("/");

      if(path.toString().length() == 0){
        throw new IllegalArgumentException("Path is empty");
      }

      //doesn't start with a "/"
      else if(keyPathString.charAt(0) != '/'){
        throw new IllegalArgumentException("Path needs to start with '/");
      }

      //ends with a "/"
      else if(keyPathString.charAt(keyPathString.length() - 1) == '/'){
        throw new IllegalArgumentException("Path shouldn't end with '/");

        
      }

      //has more than one "/" in a row

      //2 numbers in a row(?)
      else{
        for(int i = 0; i < pathWay.length; i++){
          
          if(pathWay[i].matches("^-(0|[1-9][0-9]*)$")) {
            throw new IllegalArgumentException(pathWay[i] + " index is negative");
          }
          else if(pathWay[i].matches("^-?[0-9]+([.,][0-9]+)?$") && pathWay[i].indexOf(".") != -1){
            if(Double.parseDouble(pathWay[i]) < 0)
              throw new IllegalArgumentException(pathWay[i] + " index is negative");
            else
              throw new IllegalArgumentException(pathWay[i] + " is not an array index");
          }
          
          else if(i < pathWay.length - 1){
            if(pathWay[i].matches("^-?[0-9]+([.,][0-9]+)?$")
            && pathWay[i + 1].matches("^-?[0-9]+([.,][0-9]+)?$")) {
              throw new IllegalArgumentException("There is more than 1 digit in a row");
            }
          }
        }
 
      }
    }

    //------------------------MILESTONE 2 FUNCTIONS ABOVE-------------------------------

    //------------------------MILESTONE 3 FUNCTIONS BELOW-------------------------------

  
    interface KeyTransformerInterface{
      String call(String token);
    }

    //Function that can be passed in to reverse a key
    public static class reverseString implements KeyTransformerInterface{
      @Override
      public String call(String token) {
        StringBuilder reverseStr = new StringBuilder(token).reverse();
        return reverseStr.toString();
      }
    } 

        //Function that modifies nothing
        public static class doNothing implements KeyTransformerInterface{
          @Override
          public String call(String token) {
            return token;
          }
        } 


      //Function that can be passed in to add swe262_ suffix to a key
      public static class addSWE262_String implements KeyTransformerInterface{
        @Override
        public String call(String token) {
          return "swe262_"+ token;
        }
      } 

    //Overloaded Milestone 3 function transforms the key during the parsing
    private static boolean parse(XMLTokener x, JSONObject context, String name, XMLParserConfiguration config, KeyTransformerInterface keyTransformer)
            throws JSONException {
        char c;
        int i;
        JSONObject jsonObject = null;
        String string;
        String tagName;
        Object token;
        XMLXsiTypeConverter<?> xmlXsiTypeConverter;

        // Test for and skip past these forms:
        // <!-- ... -->
        // <! ... >
        // <![ ... ]]>
        // <? ... ?>
        // Report errors for these forms:
        // <>
        // <=
        // <<

        token = x.nextToken();
        // <!

        if (token == BANG) {
            c = x.next();
            if (c == '-') {
                if (x.next() == '-') {
                    x.skipPast("-->");
                    return false;
                }
                x.back();
            } else if (c == '[') {
                token = x.nextToken();
                
                if ("CDATA".equals(token)) {
                    if (x.next() == '[') {
                        string = x.nextCDATA();
                        if (string.length() > 0) {
                            context.accumulate(config.getcDataTagName(), string);
                        }
                        return false;
                    }
                }
                throw x.syntaxError("Expected 'CDATA['");
            }
            i = 1;
            do {
                token = x.nextMeta();
                
                if (token == null) {
                    throw x.syntaxError("Missing '>' after '<!'.");
                } else if (token == LT) {
                    i += 1;
                } else if (token == GT) {
                    i -= 1;
                }
            } while (i > 0);
            return false;
        } else if (token == QUEST) {
            // <?
            x.skipPast("?>");
            return false;
        } else if (token == SLASH) {
            // Close tag </
            token = x.nextToken();
            
            //System.out.println("END TOKEN: " + token + " NAME: " + name);
          
            if (name == null) {
                throw x.syntaxError("Mismatched close tag " + token);
            }
            if (!token.equals(name)) {
                throw x.syntaxError("Mismatched " + name + " and " + token);
            }
            if (x.nextToken() != GT) {
                throw x.syntaxError("Misshaped close tag");
            }

            return true;

        } else if (token instanceof Character) {
            throw x.syntaxError("Misshaped tag");

            // Open tag <
            

        } else {
            tagName = (String) token;


            //System.out.println("M3 Tagname: " + tagName);

            token = null;
            jsonObject = new JSONObject();
            boolean nilAttributeFound = false;
            xmlXsiTypeConverter = null;

            for (;;) {
                if (token == null) {
                    token = x.nextToken();
                }
                // attribute = value
                if (token instanceof String) {
     
                    string = (String) token;
                    token = x.nextToken();
                    

                    if (token == EQ) {
                        token = x.nextToken();
                        if (!(token instanceof String)) {
                            throw x.syntaxError("Missing value");
                        }

                        if (config.isConvertNilAttributeToNull()
                                && NULL_ATTR.equals(string)
                                && Boolean.parseBoolean((String) token)) {
                            nilAttributeFound = true;
                        } else if(config.getXsiTypeMap() != null && !config.getXsiTypeMap().isEmpty()
                                && TYPE_ATTR.equals(string)) {
                            xmlXsiTypeConverter = config.getXsiTypeMap().get(token);
                        } else if (!nilAttributeFound) {

                            //Milestone 3: call function on strng
                            string = keyTransformer.call(string);

                            jsonObject.accumulate(string,
                                    config.isKeepStrings()
                                            ? ((String) token)
                                            : stringToValue((String) token));
                        }
                        token = null;
                    } else {
                        jsonObject.accumulate(string, "");
                    }


                } else if (token == SLASH) {
                    // Empty tag <.../>
                    if (x.nextToken() != GT) {
                        throw x.syntaxError("Misshaped tag");
                    }
                    if (config.getForceList().contains(tagName)) {
                        // Force the value to be an array
                        if (nilAttributeFound) {
                            context.append(tagName, JSONObject.NULL);
                        } else if (jsonObject.length() > 0) {
                            context.append(tagName, jsonObject);
                        } else {
                            context.put(tagName, new JSONArray());
                        }
                    } else {
                        if (nilAttributeFound) {
                            context.accumulate(tagName, JSONObject.NULL);
                        } else if (jsonObject.length() > 0) {
                            context.accumulate(tagName, jsonObject);
                        } else {
                            context.accumulate(tagName, "");
                        }
                    }
                    return false;

                } else if (token == GT) {
                    // Content, between <...> and </...>
                    for (;;) {
                        token = x.nextContent();

                        if (token == null) {
                            if (tagName != null) {
                                throw x.syntaxError("Unclosed tag " + tagName);
                            }
                            return false;
                        } else if (token instanceof String) {

                            //System.out.println("TAG: " + tagName + ", STRING CONTENT: " + token);

                            string = (String) token;
                            if (string.length() > 0) {
                                if(xmlXsiTypeConverter != null) {
                                    jsonObject.accumulate(config.getcDataTagName(),
                                            stringToValue(string, xmlXsiTypeConverter));
                                } else {
                                    jsonObject.accumulate(config.getcDataTagName(),
                                            config.isKeepStrings() ? string : stringToValue(string));
                                }
                            }

                        } else if (token == LT) {

                          //System.out.println("TAG: " + tagName + ", NESTED CONTENT: " + token);

                
                            // Nested element
                            if (parse(x, jsonObject, tagName, config, keyTransformer)) {

                                //Milestone 3: call a function passed in by keyTransformer that returns a
                                //changed tagName
                                tagName = keyTransformer.call(tagName);

                                if (config.getForceList().contains(tagName)) {
                                    // Force the value to be an array
                                    if (jsonObject.length() == 0) {
                                        context.put(tagName, new JSONArray());
                                    } else if (jsonObject.length() == 1
                                            && jsonObject.opt(config.getcDataTagName()) != null) {
                                        context.append(tagName, jsonObject.opt(config.getcDataTagName()));
                                    } else {
                                        context.append(tagName, jsonObject);
                                    }
                                } else {

                                    if (jsonObject.length() == 0) {
                                        context.accumulate(tagName, "");
                                    } else if (jsonObject.length() == 1
                                            && jsonObject.opt(config.getcDataTagName()) != null) {
                                        context.accumulate(tagName, jsonObject.opt(config.getcDataTagName()));
                                    } else {
                                        context.accumulate(tagName, jsonObject);
                                    }
                                }
                                
                                return false;
                            }
                        }
                    }
                } else {
                    throw x.syntaxError("Misshaped tag");
                }
            }
        }
    }

    public static JSONObject toJSONObject(Reader reader, KeyTransformerInterface keyTransformer){
      JSONObject jo = new JSONObject();
      XMLTokener x = new XMLTokener(reader);
        
        while (x.more()) {
            x.skipPast("<");
            if(x.more()) {
                parse(x, jo, null, XMLParserConfiguration.ORIGINAL, keyTransformer);
            }
        }
  

      return jo;
    }
    
}
