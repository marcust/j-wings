/*
 * $Id$
 * (c) Copyright 2000 wingS development team.
 *
 * This file is part of wingS (http://wings.mercatis.de).
 *
 * wingS is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * Please see COPYING for the complete licence.
 */

package org.wings.style;

import java.io.*;
import java.util.*;

import org.wings.io.*;

/**
 * TODO: documentation
 *
 * @author <a href="mailto:engels@mercatis.de">Holger Engels</a>
 * @version $Revision$
 */
public final class Style
    extends SimpleAttributeSet
{
    StyleSheet sheet;
    byte[] attr;

    public Style(String name, AttributeSet attributes) {
        super(attributes);
        this.name = name;
    }

    private String name;

    public void setName(String name) {
        this.name = name;
        attr = null;
    }
    public String getName() { return name; }

    public void setSheet(StyleSheet sheet) {
        this.sheet = sheet;
    }
    public StyleSheet getSheet() { return sheet; }

    public void write(Device d)
        throws IOException
    {
        if (attr == null)
            attr = (" class=\"" + name.substring(name.indexOf(".") + 1) + "\"").getBytes();
        d.write(attr);
    }

    public String toString() {
        return name + " { " + super.toString() + "}";
    }
}

/*
 * Local variables:
 * c-basic-offset: 4
 * indent-tabs-mode: nil
 * compile-command: "ant -emacs -find build.xml"
 * End:
 */
