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

package org.wings.plaf.xhtml;

import java.io.IOException;

import org.wings.*; import org.wings.border.*;
import org.wings.io.*;
import org.wings.plaf.*;
import org.wings.util.*;

public class MenuItemCG
    extends org.wings.plaf.AbstractComponentCG
    implements org.wings.plaf.MenuItemCG, SConstants
{
    private final static String propertyPrefix = "MenuItem";

    protected String getPropertyPrefix() {
        return propertyPrefix;
    }

    public void write(Device d, SComponent c)
        throws IOException
    {
        SButton button = (SButton)c;
        SBorder border = button.getBorder();

        Utils.writeBorderPrefix(d, border);

        if (!button.getShowAsFormComponent() || !button.isEnabled())
            writeAnchorButton(d, button);
        else
            writeFormButton(d, button);

        Utils.writeBorderPostfix(d, border);
    }

    protected void writeAnchorButton(Device d, SButton button)
        throws IOException
    {
        SIcon icon = button.getIcon();
        String text = button.getText();
        int horizontalTextPosition = button.getHorizontalTextPosition();
        int verticalTextPosition = button.getVerticalTextPosition();

        if (icon == null )
            writeAnchorText(d, button);
        else if (text == null)
            writeAnchorIcon(d, button);
        else {
            // Hauptsache, es funktioniert !!!
            if (verticalTextPosition == TOP && horizontalTextPosition == LEFT) {
                d.print("<table><tr><td valign=\"top\">");
                writeAnchorText(d, button);
                d.print("</td><td>");
                writeAnchorIcon(d, button);
                d.print("</td></tr></table>\n");
            } else if (verticalTextPosition == CENTER && horizontalTextPosition == LEFT) {
                d.print("<table><tr><td>");
                writeAnchorText(d, button);
                d.print("</td><td>");
                writeAnchorIcon(d, button);
                d.print("</td></tr></table>\n");
            } else if (verticalTextPosition == BOTTOM && horizontalTextPosition == LEFT) {
                d.print("<table><tr><td valign=\"bottom\">");
                writeAnchorText(d, button);
                d.print("</td><td>");
                writeAnchorIcon(d, button);
                d.print("</td></tr></table>\n");
            } else if (verticalTextPosition == TOP && horizontalTextPosition == CENTER) {
                d.print("<table><tr><td>");
                writeAnchorText(d, button);
                d.print("</td></tr><tr><td>");
                writeAnchorIcon(d, button);
                d.print("</td></tr></table>\n");
            } else if (verticalTextPosition == CENTER && horizontalTextPosition == CENTER) {
                d.print("<table><tr><td>");
                writeAnchorText(d, button);
                d.print("</td><td>");
                writeAnchorIcon(d, button);
                d.print("</td></tr></table>\n");
            } else if (verticalTextPosition == BOTTOM && horizontalTextPosition == CENTER) {
                d.print("<table><tr><td>");
                writeAnchorIcon(d, button);
                d.print("</td></tr><tr><td>");
                writeAnchorText(d, button);
                d.print("</td></tr></table>\n");
            } else if (verticalTextPosition == TOP && horizontalTextPosition == RIGHT) {
                writeAnchorIcon(d, button, "top");
                writeAnchorText(d, button);
            } else if (verticalTextPosition == CENTER && horizontalTextPosition == RIGHT) {
                writeAnchorIcon(d, button, "middle");
                writeAnchorText(d, button);
            } else if (verticalTextPosition == BOTTOM && horizontalTextPosition == RIGHT) {
                writeAnchorIcon(d, button, "bottom");
                writeAnchorText(d, button);
            } else {
                writeAnchorText(d, button);
                writeAnchorIcon(d, button, "middle");
            }
        }
    }

    protected void writeAnchorText(Device d, SButton button)
        throws IOException
    {
        String text = button.getText();
        if (text != null && text.trim().length() > 0) {
            writeAnchorPrefix(d, button);
            writeAnchorBody(d, button);
            writeAnchorPostfix(d, button);
        }
    }

    protected void writeAnchorIcon(Device d, SButton button)
        throws IOException
    {
        writeAnchorIcon(d, button, null);
    }

    protected void writeAnchorIcon(Device d, SButton button, String align)
        throws IOException
    {
        SIcon icon = button.isEnabled() ? button.getIcon() :
            button.getDisabledIcon();

        if (icon != null) {
            writeAnchorPrefix(d, button);
            d.print("<img src=\"").print(icon.getURL()).print("\"");
            if (align != null)
                d.print(" align=\"").print(align).print("\"");

            
            if ( icon.getIconWidth() > 0)
                d.print(" width=\"").print(icon.getIconWidth()).print("\"");

            if ( icon.getIconHeight() > 0)
                d.print(" height=\"").print(icon.getIconHeight()).print("\"");

            d.print(" border=\"0\"");


            String text = button.getText();
            String tooltip = button.getToolTipText();


            if (tooltip != null) {
                d.print(" alt=\"").print(tooltip).print("\"");
            } else if (text != null) {
                d.print(" alt=\"").print(text).print("\"");
            }

            d.print(" />");

            writeAnchorPostfix(d, button);
        }
    }

    protected void writeAnchorAddress(Device d, SButton button) 
    throws IOException {
        RequestURL addr = button.getRequestURL();
        addr.addParameter(button.getNamePrefix(),
                          button.getUnifiedId() + SConstants.UID_DIVIDER);
        addr.write(d);
    }

    protected void writeAnchorPrefix(Device d, SButton button)
        throws IOException
    {
        String tooltip = button.getToolTipText();

        if (button.isEnabled()) {
            d.print("<a href=\"");
            writeAnchorAddress(d, button);
            d.print("\"");

            if (button.getRealTarget() != null)
                d.print(" target=\"").print(button.getRealTarget()).print("\"");

            if (tooltip != null)
                d.print(" title=\"").print(tooltip).print("\"");

            d.print(">");
        }
    }

    protected void writeAnchorBody(Device d, SButton button)
        throws IOException
    {
        String text = button.getText();
        boolean noBreak = button.isNoBreak();

        if (text == null)
            text = "";
        d.print((noBreak) ? StringUtil.replace(text, " ", "&nbsp;") : text);
    }

    protected void writeAnchorPostfix(Device d, SButton button)
        throws IOException
    {
        if (button.isEnabled()) {
            d.print("</a>");
        }
    }

    protected void writeFormButton(Device d, SButton button)
        throws IOException
    {
        SIcon icon = button.getIcon();
        String text = button.getText();
        int horizontalTextPosition = button.getHorizontalTextPosition();
        int verticalTextPosition = button.getVerticalTextPosition();

        if (icon == null )
            writeFormText(d, button);
        else if (text == null)
            writeFormIcon(d, button);
        else {
            // Hauptsache, es funktioniert !!!
            if (verticalTextPosition == TOP && horizontalTextPosition == LEFT) {
                d.print("<table><tr><td valign=\"top\">");
                writeFormText(d, button);
                d.print("</td><td>");
                writeFormIcon(d, button);
                d.print("</td></tr></table>\n");
            } else if (verticalTextPosition == CENTER && horizontalTextPosition == LEFT) {
                d.print("<table><tr><td>");
                writeFormText(d, button);
                d.print("</td><td>");
                writeFormIcon(d, button);
                d.print("</td></tr></table>\n");
            } else if (verticalTextPosition == BOTTOM && horizontalTextPosition == LEFT) {
                d.print("<table><tr><td valign=\"bottom\">");
                writeFormText(d, button);
                d.print("</td><td>");
                writeFormIcon(d, button);
                d.print("</td></tr></table>\n");
            } else if (verticalTextPosition == TOP && horizontalTextPosition == CENTER) {
                d.print("<table><tr><td>");
                writeFormText(d, button);
                d.print("</td></tr><tr><td>");
                writeFormIcon(d, button);
                d.print("</td></tr></table>\n");
            } else if (verticalTextPosition == CENTER && horizontalTextPosition == CENTER) {
                d.print("<table><tr><td>");
                writeFormText(d, button);
                d.print("</td><td>");
                writeFormIcon(d, button);
                d.print("</td></tr></table>\n");
            } else if (verticalTextPosition == BOTTOM && horizontalTextPosition == CENTER) {
                d.print("<table><tr><td>");
                writeFormIcon(d, button);
                d.print("</td></tr><tr><td>");
                writeFormText(d, button);
                d.print("</td></tr></table>\n");
            } else if (verticalTextPosition == TOP && horizontalTextPosition == RIGHT) {
                d.print("<table><tr><td valign=\"top\">");
                writeFormIcon(d, button);
                d.print("</td><td align=\"right\">");
                writeFormText(d, button);
                d.print("</td></tr></table>\n");
            } else if (verticalTextPosition == CENTER && horizontalTextPosition == RIGHT) {
                d.print("<table><tr><td>");
                writeFormIcon(d, button);
                d.print("</td><td align=\"right\">");
                writeFormText(d, button);
                d.print("</td></tr></table>\n");
            } else if (verticalTextPosition == BOTTOM && horizontalTextPosition == RIGHT) {
                d.print("<table><tr><td valign=\"bottom\">");
                writeFormIcon(d, button);
                d.print("</td></tr><tr><td align=\"right\">");
                writeFormText(d, button);
                d.print("</td></tr></table>\n");
            } else {
                d.print("<table><tr><td>");
                writeFormIcon(d, button);
                d.print("</td></tr><tr><td>");
                writeFormText(d, button);
                d.print("</td></tr></table>\n");
            }
        }
    }

    protected void writeFormText(Device d, SButton button)
        throws IOException
    {
        String text = button.getText();
        if (text != null && text.trim().length() > 0) {
            writeFormPrefix(d, button);
            writeFormBody(d, button);
            writeFormPostfix(d, button);
        }
    }

    protected void writeFormIcon(Device d, SButton button)
        throws IOException
    {
        SIcon icon = button.isEnabled() ? button.getIcon() :
            button.getDisabledIcon();

        if (icon != null) {
            d.print("<input type=\"image\"");
            d.print(" src=\"").print(icon.getURL()).print("\"");
            
            if ( icon.getIconWidth() > 0)
                d.print(" width=\"").print(icon.getIconWidth()).print("\"");

            if ( icon.getIconHeight() > 0)
                d.print(" height=\"").print(icon.getIconHeight()).print("\"");

            d.print(" border=\"0\"");

            String text = button.getText();
            String tooltip = button.getToolTipText();

            d.print(" name=\"").print(button.getNamePrefix()).print("\"");
            d.print(" value=\"").print(text).print("\"");

            if (tooltip != null) {
                d.print(" alt=\"").print(tooltip).print("\"");
                d.print(" title=\"").print(tooltip).print("\"");
            } else if (text != null) {
                d.print(" alt=\"").print(text).print("\"");
                d.print(" title=\"").print(text).print("\"");
            }
            d.print(" />");
        }
    }

    protected void writeFormPrefix(Device d, SButton button)
        throws IOException
    {
        d.print("<input type=\"submit\"");
    }

    protected void writeFormBody(Device d, SButton button)
        throws IOException
    {
        String text = button.getText();

        if (button.isEnabled())
            d.print(" name=\"").
                print(button.getNamePrefix()).print("\"");
        if (text != null)
            d.print(" value=\"").
                print(text).
                print("\"");
    }

    protected void writeFormPostfix(Device d, SButton button)
        throws IOException
    {
        d.print(" />\n");
    }
}

/*
 * Local variables:
 * c-basic-offset: 4
 * indent-tabs-mode: nil
 * compile-command: "ant -emacs -find build.xml"
 * End:
 */
