/*
 * $Id$
 * (c) Copyright 2000 wingS development team.
 *
 * This file is part of wingS (http://j-wings.org).
 *
 * wingS is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * Please see COPYING for the complete licence.
 */

package org.wings.plaf.xhtml;

import java.io.IOException;

import javax.swing.Icon;

import org.wings.*;
import org.wings.border.*;
import org.wings.io.*;
import org.wings.plaf.*;

public class DefaultBorderCG
    implements org.wings.plaf.BorderCG
{
    public void writePrefix(Device d, SBorder b)
	throws IOException
    {
/*	d.print("<table><tr><td cellpadding=\"")
	    .print(b.getInsets().left)
	    .print("\">"); */
    }
    
    public void writePostfix(Device d, SBorder b)
	throws IOException
    {
/*	d.print("</td></tr></table>"); */
    }
    
    public String getSpanAttributes(SBorder border) {
      StringBuffer sb = new StringBuffer();
      sb.append("border: 1px black solid;" );
      return sb.toString();

    }
    
    public void writeSpanAttributes( Device d, SBorder border )
        throws IOException
    {
      d.print(getSpanAttributes(border));
    }
}

/*
 * Local variables:
 * c-basic-offset: 4
 * indent-tabs-mode: nil
 * compile-command: "ant -emacs -find build.xml"
 * End:
 */
