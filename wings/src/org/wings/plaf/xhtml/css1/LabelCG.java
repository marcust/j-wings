/*
 * $Id$
 * (c) Copyright 2000 wingS development team.
 *
 * This file is part of wingS (http://wings.mercatis.de).
 *
 * wingS is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * Please see COPYING for the complete licence.
 */

package org.wings.plaf.xhtml.css1;

import java.io.IOException;

import org.wings.*;
import org.wings.io.*;
import org.wings.plaf.*;
import org.wings.plaf.xhtml.*;

public final class LabelCG
    extends org.wings.plaf.xhtml.LabelCG
{
    protected void writeText(Device d, SLabel label)
        throws IOException
    {
        String text = label.getText();
        if (text != null && text.trim().length() > 0) {
            boolean noBreak = label.isNoBreak();

            d.append("<span");
            Utils.writeStyleAttribute(d, label.getStyle());
            d.append(">");

            if (noBreak)
                d.append("<nobr>");
            d.append(text);
            if (noBreak)
                d.append("</nobr>");

            d.append("</span>");
        }
    }

    protected void writeTextPrefix(Device d, SLabel label)
        throws IOException
    {
        d.append("<span");
        Utils.writeStyleAttribute(d, "anchor", label.getStyle());
        d.append(">");
    }
}

/*
 * Local variables:
 * c-basic-offset: 4
 * indent-tabs-mode: nil
 * End:
 */
