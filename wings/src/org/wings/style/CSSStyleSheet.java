package org.wings.style;

import java.awt.Color;
import java.awt.Font;
import java.io.*;
import java.net.*;
import java.util.*;

import org.wings.*;

public class CSSStyleSheet
    implements StyleSheet
{
    private static Map lengthMapping = new HashMap();
    static {
	lengthMapping.put("pt", new Float(1f));
	lengthMapping.put("px", new Float(1.3f));
	lengthMapping.put("mm", new Float(2.83464f));
	lengthMapping.put("cm", new Float(28.3464f));
	lengthMapping.put("pc", new Float(12f));
	lengthMapping.put("in", new Float(72f));
    }

    private Map map = new HashMap();

    public void putStyle(Style style) {
        map.put(style.getName(), style);
	style.setSheet(this);
    }

    public Style getStyle(String name) {
        return (Style)map.get(name);
    }

    public Style removeStyle(String name) {
        Style style = (Style)map.remove(name);
	style.setSheet(null);
	return style;
    }

    public Set styles() {
	return new HashSet(map.values());
    }

    public void write(OutputStream out)
	throws IOException
    {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, "8859_1"));

	Iterator iterator = map.entrySet().iterator();
	while (iterator.hasNext()) {
	    Map.Entry entry = (Map.Entry)iterator.next();
	    writer.write((String)entry.getValue());
	    writer.write(" { ");
	    writer.write(entry.getValue().toString());
	    writer.write(" }");
	    writer.newLine();
	}
    }

    public void read(InputStream is)
	throws IOException
    {
	Reader r = new BufferedReader(new InputStreamReader(is));
	CssParser parser = new CssParser();
	parser.parse(null, r, false, false);
    }

    public void importStyleSheet(URL url) {
	try {
	    InputStream is = url.openStream();
	    Reader r = new BufferedReader(new InputStreamReader(is));
	    CssParser parser = new CssParser();
	    parser.parse(url, r, false, true);
	    r.close();
	    is.close();
	}
	catch (Throwable e) {
	    // on error we simply have no styles... the html
	    // will look mighty wrong but still function.
	}
    }

    public InputStream getInputStream() throws IOException { return null; };
    public boolean isFinal() { return false; }

    /**
     * Fetches the font to use for the given set of attributes.
     */
    public static SFont getFont(AttributeSet a) {
	int size = getFontSize(a);

	/*
	 * If the vertical alignment is set to either superscirpt or
	 * subscript we reduce the font size by 2 points.
	 */
	String vAlign = (String)a.getAttribute("vertical-align");

	if (vAlign != null) {
	    if ((vAlign.indexOf("sup") >= 0) ||
		(vAlign.indexOf("sub") >= 0)) {
		size -= 2;
	    }
	}

	String family = (String)a.getAttribute("font-family");
	if (family == null)
	    family = "sans-serif";

	int style = Font.PLAIN;
	String weight = (String)a.getAttribute("font-weight");
	if (weight == null)
	    ;
	else if (weight.equals("bold")) {
	    style |= Font.BOLD;
	}
	else if (weight.equals("normal"))
	    ;
	else {
	    try {
		int w = Integer.parseInt(weight);
		if (w > 400)
		    style |= Font.BOLD;
	    } catch (NumberFormatException nfe) {}
	}

	String styleValue = (String)a.getAttribute("font-style");
	if ((styleValue != null) && (styleValue.indexOf("italic") >= 0))
	    style |= Font.ITALIC;

	return new SFont(family, style, size);
    }

    static int sizeMap[] = { 8, 10, 12, 14, 18, 24, 36 };

    public static int getFontSize(AttributeSet attr) {
	String value = (String)attr.getAttribute("font-size");
	if (value == null)
	    return 12;
	try {
	    if (value.equals("xx-small")) {
		return 8;
	    } else if (value.equals("x-small")) {
		return 10;
	    } else if (value.equals("small")) {
		return 12;
	    } else if (value.equals("medium")) {
		return 14;
	    } else if (value.equals("large")) {
		return 18;
	    } else if (value.equals("x-large")) {
		return 24;
	    } else if (value.equals("xx-large")) {
		return 36;
	    } else {
		return new Float(getLength(value)).intValue();
	    }
	} catch (NumberFormatException nfe) {
	    // the default
	    return 12;
	}
    }

    public static float getLength(String value) {
	int length = value.length();
	if (length >= 2) {
	    String units = value.substring(length - 2, length);
	    Float scale = (Float)lengthMapping.get(units);

	    if (scale != null) {
		try {
		    return Float.valueOf(value.substring(0, length - 2)).floatValue() *
			scale.floatValue();
		}
		catch (NumberFormatException nfe) {}
	    }
	    else {
		// treat like points.
		try {
		    return Float.valueOf(value).floatValue();
		} catch (NumberFormatException nfe) {}
	    }
	}
	else if (length > 0) {
	    // treat like points.
	    try {
		return Float.valueOf(value).floatValue();
	    } catch (NumberFormatException nfe) {}
	}
	return 12.0f;
    }

    /**
     * Takes a set of attributes and turn it into a foreground color
     * specification. This might be used to specify things
     * like brighter, more hue, etc.
     *
     * @param a the set of attributes
     * @return the color
     */
    public static Color getForeground(AttributeSet a) {
	Color c = getColor(a, "color");
	if (c == null) {
	    return Color.black;
	}
	return c;
    }

    /**
     * Takes a set of attributes and turn it into a background color
     * specification.  This might be used to specify things
     * like brighter, more hue, etc.
     *
     * @param attr the set of attributes
     * @return the color
     */
    public static  Color getBackground(AttributeSet a) {
	return getColor(a, "background-color");
    }

    static Color getColor(AttributeSet a, String key) {
	String cv = (String)a.getAttribute(key);
	if (cv != null) {
	    return stringToColor(cv);
	}
	return null;
    }

    /**
     * Converts a type Color to a hex string
     * in the format "#RRGGBB"
     */
    static String colorToHex(Color color) {
	String colorstr = new String("#");

	// Red
	String str = Integer.toHexString(color.getRed());
	if (str.length() > 2)
	    str = str.substring(0, 2);
	else if (str.length() < 2)
	    colorstr += "0" + str;
	else
	    colorstr += str;

	// Green
	str = Integer.toHexString(color.getGreen());
	if (str.length() > 2)
	    str = str.substring(0, 2);
	else if (str.length() < 2)
	    colorstr += "0" + str;
	else
	    colorstr += str;

	// Blue
	str = Integer.toHexString(color.getBlue());
	if (str.length() > 2)
	    str = str.substring(0, 2);
	else if (str.length() < 2)
	    colorstr += "0" + str;
	else
	    colorstr += str;

	return colorstr;
    }

    /**
     * Convert a "#FFFFFF" hex string to a Color.
     * If the color specification is bad, an attempt
     * will be made to fix it up.
     */
    static final Color hexToColor(String value) {
	String digits;
	int n = value.length();
	if (value.startsWith("#")) {
	    digits = value.substring(1, Math.min(value.length(), 7));
	} else {
	    digits = value;
	}
	String hstr = "0x" + digits;
	Color c;
	try {
	    c = Color.decode(hstr);
	} catch (NumberFormatException nfe) {
	    c = null;
	}
	return c;
    }

    /**
     * Convert a color string such as "RED" or "#NNNNNN" or "rgb(r, g, b)"
     * to a Color.
     */
    static Color stringToColor(String str) {
	Color color = null;

	if (str.length() == 0)
	    color = Color.black;
	else if (str.startsWith("rgb(")) {
	    color = parseRGB(str);
	}
	else if (str.charAt(0) == '#')
	    color = hexToColor(str);
	else if (str.equalsIgnoreCase("Black"))
	    color = hexToColor("#000000");
	else if(str.equalsIgnoreCase("Silver"))
	    color = hexToColor("#C0C0C0");
	else if(str.equalsIgnoreCase("Gray"))
	    color = hexToColor("#808080");
	else if(str.equalsIgnoreCase("White"))
	    color = hexToColor("#FFFFFF");
	else if(str.equalsIgnoreCase("Maroon"))
	    color = hexToColor("#800000");
	else if(str.equalsIgnoreCase("Red"))
	    color = hexToColor("#FF0000");
	else if(str.equalsIgnoreCase("Purple"))
	    color = hexToColor("#800080");
	else if(str.equalsIgnoreCase("Fuchsia"))
	    color = hexToColor("#FF00FF");
	else if(str.equalsIgnoreCase("Green"))
	    color = hexToColor("#008000");
	else if(str.equalsIgnoreCase("Lime"))
	    color = hexToColor("#00FF00");
	else if(str.equalsIgnoreCase("Olive"))
	    color = hexToColor("#808000");
	else if(str.equalsIgnoreCase("Yellow"))
	    color = hexToColor("#FFFF00");
	else if(str.equalsIgnoreCase("Navy"))
	    color = hexToColor("#000080");
	else if(str.equalsIgnoreCase("Blue"))
	    color = hexToColor("#0000FF");
	else if(str.equalsIgnoreCase("Teal"))
	    color = hexToColor("#008080");
	else if(str.equalsIgnoreCase("Aqua"))
	    color = hexToColor("#00FFFF");
	else
	    color = hexToColor(str); // sometimes get specified without leading #
	return color;
    }

    /**
     * Parses a String in the format <code>rgb(r, g, b)</code> where
     * each of the Color components is either an integer, or a floating number
     * with a % after indicating a percentage value of 255. Values are
     * constrained to fit with 0-255. The resulting Color is returned.
     */
    private static Color parseRGB(String string) {
        // Find the next numeric char
        int[] index = new int[1];

        index[0] = 4;
        int red = getColorComponent(string, index);
        int green = getColorComponent(string, index);
        int blue = getColorComponent(string, index);

        return new Color(red, green, blue);
    }

    /**
     * Returns the next integer value from <code>string</code> starting
     * at <code>index[0]</code>. The value can either can an integer, or
     * a percentage (floating number ending with %), in which case it is
     * multiplied by 255.
     */
    private static int getColorComponent(String string, int[] index) {
        int length = string.length();
        char aChar;

        // Skip non-decimal chars
        while(index[0] < length && (aChar = string.charAt(index[0])) != '-' &&
              !Character.isDigit(aChar) && aChar != '.') {
            index[0]++;
        }

        int start = index[0];

        if (start < length && string.charAt(index[0]) == '-') {
            index[0]++;
        }
        while(index[0] < length &&
	      Character.isDigit(string.charAt(index[0]))) {
            index[0]++;
        }
        if (index[0] < length && string.charAt(index[0]) == '.') {
            // Decimal value
            index[0]++;
            while(index[0] < length &&
                  Character.isDigit(string.charAt(index[0]))) {
                index[0]++;
            }
        }
        if (start != index[0]) {
            try {
                float value = Float.parseFloat(string.substring
                                               (start, index[0]));

                if (index[0] < length && string.charAt(index[0]) == '%') {
                    index[0]++;
                    value = value * 255f / 100f;
                }
                return Math.min(255, Math.max(0, (int)value));
            } catch (NumberFormatException nfe) {
                // Treat as 0
            }
        }
        return 0;
    }

    public static URL getURL(URL base, String cssString) {
	if (cssString == null) {
	    return null;
	}
	if (cssString.startsWith("url(") &&
	    cssString.endsWith(")")) {
	    cssString = cssString.substring(4, cssString.length() - 1);
	}
	// Absolute first
	try {
	    URL url = new URL(cssString);
	    if (url != null) {
		return url;
	    }
	} catch (MalformedURLException mue) {
	}
	// Then relative
	if (base != null) {
	    // Relative URL, try from base
	    try {
		URL url = new URL(base, cssString);
		return url;
	    }
	    catch (MalformedURLException muee) {
	    }
	}
	return null;
    }

    public static AttributeSet getAttributes(SFont font) {
	AttributeSet attributes = new SimpleAttributeSet();
	if (font == null)
	    return attributes;
	attributes.putAttribute("font-family", font.getFace());

	int style = Font.PLAIN;
	if ((font.getStyle() & Font.ITALIC) > 0)
	    attributes.putAttribute("font-style", "italic");

	if ((font.getStyle() & Font.BOLD) > 0)
	    attributes.putAttribute("font-weight", "bold");

	attributes.putAttribute("font-size", font.getSize() + "pt");
	return attributes;
    }

    public static AttributeSet getAttributes(Color color, String key) {
	AttributeSet attributes = new SimpleAttributeSet();
	if (color != null)
	    attributes.putAttribute(key, colorToHex(color));
	return attributes;
    }

    class CssParser
	implements CSSParser.CSSParserCallback
    {
	/**
	 * Parses the passed in CSS declaration into an AttributeSet.
	 */
	public AttributeSet parseDeclaration(String string) {
	    try {
		return parseDeclaration(new StringReader(string));
	    } catch (IOException ioe) {}
	    return null;
	}

	/**
	 * Parses the passed in CSS declaration into an AttributeSet.
	 */
	public AttributeSet parseDeclaration(Reader r) throws IOException {
	    parse(base, r, true, false);
	    return new SimpleAttributeSet(declaration);
	}

	/**
	 * Parse the given CSS stream
	 */
	public void parse(URL base, Reader r, boolean parseDeclaration, boolean isLink)
	    throws IOException
	{
	    this.base = base;
	    this.isLink = isLink;
	    this.parsingDeclaration = parseDeclaration;
	    declaration.clear();
	    selectorTokens.clear();
	    selectors.clear();
	    propertyName = null;
	    parser.parse(r, this, parseDeclaration);
	}

	//
	// CSSParserCallback methods, public to implement the interface.
	//

	/**
	 * Invoked when a valid @import is encountered, will call
	 * <code>importStyleSheet</code> if a 
	 * <code>MalformedURLException</code> is not thrown in creating
	 * the URL.
	 */
	public void handleImport(String importString) {
	    URL url = CSSStyleSheet.getURL(base, importString);
	    if (url != null) {
		importStyleSheet(url);
	    }
	}

	/**
	 * A selector has been encountered.
	 */
	public void handleSelector(String selector) {
	    selector = selector.toLowerCase();

	    int length = selector.length();

	    if (selector.endsWith(",")) {
		if (length > 1) {
		    selector = selector.substring(0, length - 1);
		    selectorTokens.add(selector);
		}
		addSelector();
	    }
	    else if (length > 0) {
		selectorTokens.add(selector);
	    }
	}

	/**
	 * Invoked when the start of a rule is encountered.
	 */
	public void startRule() {
	    if (selectorTokens.size() > 0) {
		addSelector();
	    }
	    propertyName = null;
	}

	/**
	 * Invoked when a property name is encountered.
	 */
	public void handleProperty(String property) {
	    propertyName = property;
	}

	/**
	 * Invoked when a property value is encountered.
	 */
	public void handleValue(String value) {
	    if (propertyName != null) {
		declaration.putAttribute(propertyName, value);
	    }
	    propertyName = null;
	}

	/**
	 * Invoked when the end of a rule is encountered.
	 */
	public void endRule() {
	    int n = selectors.size();
	    for (int i = 0; i < n; i++) {
		String[] selector = (String[])selectors.get(i);
		if (selector.length > 0) {
		    CSSStyleSheet.this.putStyle(new Style(selector[i], declaration));
		}
	    }
	    declaration.clear();
	    selectors.clear();
	}

	private void addSelector() {
	    String[] selector = new String[selectorTokens.size()];
	    selector = (String[])selectorTokens.toArray(selector);
	    selectors.add(selector);
	    selectorTokens.clear();
	}

	List selectors = new LinkedList();
	List selectorTokens = new LinkedList();
	/** Name of the current property. */
	String propertyName;
	AttributeSet declaration = new SimpleAttributeSet();
	/** True if parsing a declaration, that is the Reader will not
	 * contain a selector. */
	boolean parsingDeclaration;
	/** True if the attributes are coming from a linked/imported style. */
	boolean isLink;
	/** Where the CSS stylesheet lives. */
	URL base;
	CSSParser parser = new CSSParser();
    }
}