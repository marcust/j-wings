package org.wings.plaf.xhtml.old;

import java.io.IOException;

import java.awt.Color;

import org.wings.*;
import org.wings.io.*;
import org.wings.plaf.*;
import org.wings.plaf.xhtml.*;

public final class TextFieldCG
    extends org.wings.plaf.xhtml.TextFieldCG
{
    private StringBuffer buffer = new StringBuffer();
    
    public void write(Device d, SComponent c)
        throws IOException
    {
	STextField textField = (STextField)c;
	SFont font = textField.getFont();
	Color foreground = textField.getForeground();
	
	Utils.writeFontPrefix(d, font, foreground);
	
	d.append("<input type=\"text\"");
	d.append(" size=\"").append(textField.getColumns());
	d.append("\" maxlength=\"").append(textField.getMaxColumns());
	d.append("\"");
	
	if (!textField.isEditable())
	    d.append(" readonly=\"1\"");
	
	String text = textField.getText();
	if (text != null) {
	    int pos=-1, lastpos=0;
	    buffer.setLength(0);
	    while ((pos = text.indexOf("\"", pos+1)) != -1) {
		buffer.append(text.substring(lastpos, pos));
		buffer.append("&quot;");
		lastpos = pos+1;
	    }
	    if (buffer.length() > 0) {
		buffer.append(text.substring(lastpos));
		text = buffer.toString();
	    }
	    
	    d.append(" value=\"").append(text).append("\"");
	}
	
	if (textField.isEnabled()) {
	    d.append(" name=\"");
	    d.append(textField.getNamePrefix());
	    d.append("\"");
	}
	
	d.append(" />");
	
	Utils.writeFontPostfix(d, font, foreground);
    }
}
