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

package org.wings;

import java.io.IOException;
import java.util.*;

import javax.swing.SingleSelectionModel;
import javax.swing.DefaultSingleSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.wings.*;
import org.wings.plaf.*;
import org.wings.io.Device;

/**
 * TODO: documentation
 *
 * @author <a href="mailto:engels@mercatis.de">Holger Engels</a>
 * @version $Revision$
 */
public class SDesktopPane
    extends SContainer
    implements SConstants, SFrameModifier
{
    /**
     * @see #getCGClassID
     */
    private static final String cgClassID = "DesktopPaneCG";

    SDesktopLayout layout = new SDesktopLayout();

    /**
     * TODO: documentation
     *
     */
    public SDesktopPane() {
        super();
        super.setLayout(layout);
    }

    /**
     * Sets the parent frame and
     * adds the needed javascript to it.
     *
     * @param f the frame
     */
    protected void setParentFrame(SFrame f) {
        super.setParentFrame(f);
        ClassLoader cl = this.getClass().getClassLoader();
        f.addJavascript(new Javascript(cl, "org/wings/dhtml/dynapi.js"));
        f.addJavascript(new Javascript(cl, "org/wings/dhtml/dynapi/api/browser.js"));
        f.addJavascript(new Javascript(cl, "org/wings/dhtml/dynapi/api/dynlayer.js"));
        f.addJavascript(new Javascript(cl, "org/wings/dhtml/dynapi/api/dyndocument.js"));
        f.addJavascript(new Javascript(cl, "org/wings/dhtml/dynapi/event/listeners.js"));
        f.addJavascript(new Javascript(cl, "org/wings/dhtml/dynapi/event/mouse.js"));
        f.addJavascript(new Javascript(cl, "org/wings/dhtml/dynapi/event/dragevent.js"));
        f.addJavascript(new Javascript(cl, "org/wings/dhtml/dynapi/ext/inline.js"));
        f.addJavascript(new Javascript(cl, "org/wings/dhtml/cookies.js"));
        f.addJavascript(new Javascript(cl, "org/wings/dhtml/windows.js"));
        f.addFrameModifier(this);
    }

    /**
     * TODO: documentation
     *
     * @param l
     */
    public void setLayout(SLayoutManager l) {}

    /**
     * Returns the currently selected component for this desktopPane.
     * Returns null if there is no component on the layout.
     *
     * @return the top most component
     */
    public SInternalFrame getVisibleFrame() {
        return (SInternalFrame)getComponent(getComponentCount()-1);
    }

    /**
     * Adds a <i>component</i> with a tab title defaulting to
     * the name of the component.
     * Cover method for insertTab().
     * @param component The component to be displayed when this tab is clicked.
     */
    public SComponent add(SComponent component) {
        super.addComponent(component, component.getNamePrefix());
        return component;
    }

    /**
     * @param component The internal frame to be added.
     * @param constraints nothing
     */
    public void add(SComponent component, Object constraints) {
        super.addComponent(component, constraints);
    }

    /**
     * Removes the frame at <i>index</i>.
     *
     * @param index the index of the tab to be removed
     */
    public void remove(int index) {
        super.remove(index);
    }

    /**
     * Removes the frame which corresponds to the specified component.
     *
     * @param component the frame to be removed from the desktop
     */
    public void remove(SComponent component) {
        super.remove(component);
    }

    /**
     * Removes all frames from the desktop.
     */
    public void removeAll() {
        super.removeAll();
    }

    /**
     * Sets the position for the specified component.
     * @param c         the Component to set the layer for
     * @param position  an int specifying the position, where
     *                  0 is the topmost position and
     *                  -1 is the bottommost position
     */
    public void setPosition(SComponent c, int position)  {
        getComponentList().remove(c);
        getComponentList().add(position, c);
    }

    /**
     * Returns the index of the specified Component.
     * This is the absolute index, ignoring layers.
     * Index numbers, like position numbers, have the topmost component
     * at index zero. Larger numbers are closer to the bottom.
     *
     * @param c  the Component to check
     * @return an int specifying the component's index
     */
    public int getIndexOf(SComponent c) {
        int i, count;

        count = getComponentCount();
        for(i = 0; i < count; i++) {
            if(c == getComponent(i))
                return i;
        }
        return -1;
    }

    /**
     * Get the position of the component.
     *
     * @param c  the Component to check
     * @return an int giving the component's position, where 0 is the
     *         topmost position and the highest index value = the count
     *         count of components minus 1
     *
     * @see #getComponentCountInLayer
     */
    public int getPosition(SComponent c) {
        return getIndexOf(c);
    }

    private class SDesktopLayout extends SAbstractLayoutManager
    {
        private SContainer container = null;

        public SDesktopLayout() {}

        public void updateCG() {}
        public void addComponent(SComponent c, Object constraint) {}
        public void removeComponent(SComponent c) {}

        public SComponent getComponentAt(int i) {
            return (SComponent)getComponent(i);
        }

        public void setContainer(SContainer c) {
            container = c;
        }

        public void write(Device d)
            throws IOException
        {
            d.append("<table cellpadding=\"0\" cellspacing=\"7\" border=\"0\" width=\"100%\">\n");

            int componentCount = getComponentCount();
            for (int i=0; i<componentCount; i++) {
                SInternalFrame frame = (SInternalFrame)getComponent(i);
                if (!frame.isClosed() && frame.isMaximized()) {
                    d.append("<tr><td>\n");
                    frame.write(d);
                    d.append("</td></tr></table>\n");
                    return;
                }
            }

            for (int i=0; i<componentCount; i++) {
                SInternalFrame frame = (SInternalFrame)getComponent(i);
                if (!frame.isClosed()) {
                    d.append("<tr><td>\n");
                    frame.write(d);
                    d.append("</td></tr>\n");
                }
            }
            d.append("</table>\n");
        }
    }

    /**
      * Write javascript code to initialize frame windows.
      */
    public void writeHead(org.wings.io.Device d, org.wings.SFrame f)
        throws IOException
    {
        StringBuffer strb = new StringBuffer(100);
        int componentCount = getComponentCount();
        strb.append("DynAPI.onLoad=function() { \nvar wm = new WindowManager();\n");
        for (int i=0; i < componentCount; i++) {
            strb.append("new DragWindow(\"wl");
            strb.append(getComponentAt(i).getUnifiedId());
            strb.append("-");
            strb.append(getComponentAt(i).getNamePrefix());
            strb.append("\", wm);\n");
        }
        strb.append("};");
        new Javascript(strb.toString()).write(d, f);
    }


    /**
      * Compare to SDesktopPanes, {@link org.wings.SComponent#getUnifiedId()}
      * is used.
      */
    public boolean equals(java.lang.Object obj) {
        return this.getUnifiedId() == ((SComponent) obj).getUnifiedId();
    }

    /**
     * Returns the name of the CGFactory class that generates the
     * look and feel for this component.
     *
     * @return "DesktopPaneCG"
     * @see SComponent#getCGClassID
     * @see CGDefaults#getCG
     */
    public String getCGClassID() {
        return cgClassID;
    }

    public void setCG(DesktopPaneCG cg) {
        super.setCG(cg);
    }
}

/*
 * Local variables:
 * c-basic-offset: 4
 * indent-tabs-mode: nil
 * End:
 */
