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

package org.wings.plaf.xhtml.css1;

import java.io.IOException;

import org.wings.*; import org.wings.border.*;
import org.wings.io.*;
import org.wings.plaf.*;
import org.wings.plaf.xhtml.*;
import org.wings.style.Style;

public final class TabbedPaneCG
    extends org.wings.plaf.xhtml.TabbedPaneCG
{
    SIcon firstIcon;
    SIcon normalIcon;
    SIcon selectedIcon;
    SIcon lastIcon;

    SIcon transIcon;

    public void installCG(SComponent component) {
        super.installCG(component);
        CGManager cg = component.getSession().getCGManager();
        firstIcon    = cg.getIcon("TabbedPaneCG.firstIcon");
        normalIcon   = cg.getIcon("TabbedPaneCG.normalIcon");
        selectedIcon = cg.getIcon("TabbedPaneCG.selectedIcon");
        lastIcon     = cg.getIcon("TabbedPaneCG.lastIcon");
        transIcon = LookAndFeel.makeIcon(getClass().getClassLoader(),
                                         "/org/wings/icons/transdot.gif");
    }


    // ignore tab placement for now .. always TOP
    public void write(Device d, SComponent c)
        throws IOException
    {
        SBorder border = c.getBorder();
        STabbedPane pane = (STabbedPane)c;

        int maxTabsPerLine = pane.getMaxTabsPerLine();

        SContainer buttons;
        SContainer contents;
        if (SBorderLayout.CENTER.equals(pane.getConstraintAt(0))) {
            buttons = (SContainer)pane.getComponentAt(1);
            contents = (SContainer)pane.getComponentAt(0);
        }
        else {
            buttons = (SContainer)pane.getComponentAt(0);
            contents = (SContainer)pane.getComponentAt(1);
        }

        /* for browsers, that do not support the border styles, create a line
         * at top, that starts at position 18 (the first 17 pixels are used up by
         * the rise of the left tab. This gives the correct illusion at least for the
         * first line of tabs.
         */
        // this is actually only really for NS 4.x. Looks very ugly for Mozilla.
        // (dunno for IE).
        /*
        d.append("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"100%\">")
            .append("<tr><td>")
            .append("<img src=\"").append(transAdr).append("\" width=\"17\" height=\"1\" />")
            .append("</td><td width=\"100%\" bgcolor=\"#000000\">") // '100%' hack needed for NS4.x
            .append("<img src=\"").append(transAdr).append("\" width=\"1\" height=\"1\" />")
            .append("</td></tr></table>\n");
        */
        boolean newLine = true;
        boolean selected = false;

        for (int i=0; i < buttons.getComponentCount(); i++) {
            d.append("<img src=\"")
                .append(newLine 
                        ? firstIcon.getURL() 
                        : (selected) ? selectedIcon.getURL() : normalIcon.getURL())
                .append("\" />");
            newLine = false;
            SRadioButton button = (SRadioButton)buttons.getComponentAt(i);
            String text = button.getText();
            if (text != null && !text.endsWith("&nbsp;"))
                button.setText(text + "&nbsp;");

            selected = (i == pane.getSelectedIndex());
            if (selected)
                button.setStyle(pane.getSelectionStyle());
            else
                button.setStyle(pane.getStyleAt(pane.getSelectedIndex()));

            button.write(d);

            if ( maxTabsPerLine > 0 && ((i+1) % maxTabsPerLine == 0) ) {
                d.append("<img src=\"").append(lastIcon.getURL()).append("\" />");
                d.append("<br />");
                newLine = true;
            }
        }
        if (!newLine) {
            // closed tab if not already written..
            d.append("<img src=\"").append(lastIcon.getURL()).append("\" />");
        }

        d.append("<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"1\"><tr><td>");

        contents.write(d);

        d.append("</td></tr></table>");
    }

    protected void writePrefix(Device d, SContainer c)
        throws IOException
    {
        Utils.writeDivWithStyleAttributePrefix(d, c.getStyle());
    }

    protected void writePostfix(Device d, SContainer c)
        throws IOException
    {
        Utils.writeDivWithStyleAttributePostfix(d, c.getStyle());
    }
}

/*
 * Local variables:
 * c-basic-offset: 4
 * indent-tabs-mode: nil
 * compile-command: "ant -emacs -find build.xml"
 * End:
 */
