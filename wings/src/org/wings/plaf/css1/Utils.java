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
package org.wings.plaf.css1;

import java.io.IOException;
import java.util.*;

import org.wings.*; 
import org.wings.border.*;
import org.wings.script.*;
import org.wings.style.*;
import org.wings.io.Device;

/**
 * Utils.java
 *
 * @author <a href="mailto:mreinsch@to.com">Michael Reinsch</a>
 * @version $Revision$
 */
public final class Utils
{
    

    private Utils() {}

    /**
     * Renders a container
     */
    static void renderContainer(Device d, SContainer c)
        throws IOException
    {
        SLayoutManager layout = c.getLayout();

        if (layout != null) {
            layout.write(d);
        }
        else {
            for (int i=0; i < c.getComponentCount(); i++)
                c.getComponentAt(i).write(d);
        }
    }

    static void writeEvents(Device d, SComponent c)
        throws IOException
    {
        ScriptListener[] listeners = c.getScriptListeners();
        if (  listeners.length>0 ) {
            Map eventScripts = new HashMap();
            for ( int i=0; i<listeners.length; i++ ) {
                ScriptListener script = listeners[i];
                String eventScriptCode = script.getCode();
                String event = script.getEvent();
                if (eventScripts.containsKey(event)) {
                    String savedEventScriptCode = (String) eventScripts.get(event);
                    eventScriptCode = savedEventScriptCode
                        + (savedEventScriptCode.trim().endsWith(";") ? "" : ";")
                        + eventScriptCode;
                }
                eventScripts.put(event,  eventScriptCode);
            } // end of for ()
        
            Iterator it = eventScripts.keySet().iterator();
            while (it.hasNext()) {
                String event = (String) it.next();
                String code = (String) eventScripts.get(event);
                d.print(" ");
                d.print(event);
                d.print("=\"");
                d.print(code);
                d.print("\"");
            }
        } // end of if ()
    }

    static String style(SComponent component) {
        if (component.getAttributes().size() > 0)
            return (DynamicStyleSheetResource.NORMAL_ATTR_PREFIX 
                    + component.getComponentId());
        else if (component.getStyle() != null)
            return component.getStyle();
        return null;
    }

    static String selectionStyle(SComponent component) {
        SSelectionComponent sel = (SSelectionComponent)component;
        if (sel.getSelectionAttributes().size() > 0)
            return (DynamicStyleSheetResource.SELECT_ATTR_PREFIX 
                    + component.getComponentId());
        else if (sel.getSelectionStyle() != null)
            return sel.getSelectionStyle();
        return null;
    }

    final static byte[] ALIGN_CENTER = " align=\"center\"".getBytes();
    final static byte[] ALIGN_LEFT = " align=\"left\"".getBytes();
    final static byte[] ALIGN_RIGHT = " align=\"right\"".getBytes();
    final static byte[] ALIGN_JUSTIFY = " align=\"justify\"".getBytes();
    final static byte[] VALIGN_TOP = " valign=\"top\"".getBytes();
    final static byte[] VALIGN_BOTTOM = " valign=\"bottom\"".getBytes();
    final static byte[] VALIGN_BASELINE = " valign=\"baseline\"".getBytes();

    static void printTableCellAlignment(Device d, SComponent c) 
        throws IOException {
        switch (c.getHorizontalAlignment()) {
        case SConstants.NO_ALIGN:
        case SConstants.LEFT:
            break;
        case SConstants.CENTER:
            d.write(ALIGN_CENTER);
            break;
        case SConstants.RIGHT:
            d.write(ALIGN_RIGHT);
            break;
        case SConstants.JUSTIFY:
            d.write(ALIGN_JUSTIFY);
            break;
        }
    
        switch (c.getVerticalAlignment()) {
        case SConstants.NO_ALIGN:
        case SConstants.CENTER:
            break;
        case SConstants.TOP:
            d.write(VALIGN_TOP);
            break;
        case SConstants.BOTTOM:
            d.write(VALIGN_BOTTOM);
            break;
        case SConstants.BASELINE:
            d.write(VALIGN_BASELINE);
            break;
        }
    }

}

/*
 * Local variables:
 * c-basic-offset: 4
 * indent-tabs-mode: nil
 * compile-command: "ant -emacs -find build.xml"
 * End:
 */
