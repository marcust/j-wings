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

import java.awt.Color;
import java.beans.*;
import java.io.IOException;
import java.net.URL;
import java.util.*;

import org.wings.*;
import org.wings.io.Device;
import org.wings.io.StringBufferDevice;
import org.wings.io.DeviceBuffer;
import org.wings.plaf.*;
import org.wings.style.StyleSheet;
import org.wings.session.Session;
import org.wings.session.SessionManager;
import org.wings.util.*;

/*
 * The frame is the root component in every component hierarchie.
 * A SessionServlet requires an instance of SFrame to render the page.
 * SFrame consists of some header informaton (metas, headers, style sheet)
 * and a stack of components. The bottommost component of the stack is always
 * the contentPane. When dialogs are to be shown, they are stacked on top of
 * it.
 *
 * @author <a href="mailto:engels@mercatis.de">Holger Engels</a>,
 *         <a href="mailto:haaf@mercatis.de">Armin Haaf</a>
 * @version $Revision$
 */
public class SFrame
    extends SContainer
    implements PropertyChangeListener
{
    /**
     * @see #getCGClassID
     */
    private static final String cgClassID = "FrameCG";

    private static final boolean DEBUG = true;

    /**
     *  The Title of the Frame.
     */
    protected String title;

    /**
     * The container for the contentPane.
     */
    protected final SContainer contentPane = new SContainer();

    protected String baseTarget = null;

    /**
     * A List containing meta tags for the html header.
     */
    protected ArrayList metas;

    /**
     * A List containing additional tags for the html header.
     */
    protected ArrayList headers;

    /**
     * A List containing links for the html header.
     */
    protected ArrayList links;


    private Color textColor = null;
    private Color linkColor = null;
    private Color vLinkColor = null;
    private Color aLinkColor = null;

    private SIcon backgroundImage = null;

    /**
     * TODO: documentation
     */
    protected boolean resizable = true;

    /** the style sheet used in certain look and feels. */
    protected StyleSheet styleSheet;  // IMPORTANT: initialization with null causes errors; what errors ?

    /**
     * TODO: documentation
     */
    protected String statusLine = null;

    /**
     * TODO: documentation
     */
    private transient SRequestDispatcher dispatcher = null;

    private Session session;
    private RequestURL requestURL = new RequestURL();
    private String targetResource;

    private HashMap dynamicResources;

    /**
     * TODO: documentation
     *
     */
    public SFrame() {
        super.setLayout(new SStackLayout());
        super.addComponent(getContentPane(), null);
        getSession().addPropertyChangeListener("lookAndFeel", this);
        getSession().addPropertyChangeListener("request.url", this);
    }

    /**
     * TODO: documentation
     *
     * @param title
     */
    public SFrame(String title) {
        this();
        setTitle(title);
    }

    /**
     * TODO: documentation
     *
     */
    public void addDynamicResource(DynamicResource d) {
        if (dynamicResources == null)
            dynamicResources = new HashMap();
        dynamicResources.put(d.getClass(), d);
    }

    /**
     * TODO: documentation
     *
     */
    public DynamicResource getDynamicResource(Class c) {
        if (dynamicResources == null)
            dynamicResources = new HashMap();
        return (DynamicResource)dynamicResources.get(c);
    }


    /**
     * TODO: documentation
     *
     * @param b
     */
    public void setResizable(boolean b) {
        resizable = b;
    }

    /**
     * TODO: documentation
     *
     * @return
     */
    public boolean isResizable() {
        return resizable;
    }

    /**
     * Set the background image.
     */
    public void setBackgroundImage(SIcon icon) {
        backgroundImage = icon;
    }

    /**
     * TODO: documentation
     *
     * @return
     */
    public SIcon getBackgroundImage() {
        return backgroundImage;
    }

    /**
     * Use getContentPane().addComponent(c) instead.
     */
    public SComponent addComponent(SComponent c, Object constraint) {
        throw new IllegalArgumentException("use getContentPane().addComponent()");
    }

    /**
     * Use getContentPane().removeComponent(c) instead.
     */
    public boolean removeComponent(SComponent c) {
        throw new IllegalArgumentException("use getContentPane().removeComponent()");
    }

    /**
     * TODO: documentation
     *
     * @return
     */
    public SRequestDispatcher getDispatcher() {
        if (dispatcher == null)
            dispatcher = getSession().getDispatcher();
        return dispatcher;
    }

    /**
     * TODO: documentation
     *
     * @return
     */
    public SFrame getParentFrame() {
        return this;
    }

    /**
     * TODO: documentation
     *
     * @return
     */
    public Session getSession() {
        if (session == null)
            session = SessionManager.getSession();
        return session;
    }

    /**
     * TODO: documentation
     *
     * @return
     */
    public String getEventEpoch() {
        return getDynamicResource(DynamicCodeResource.class).getEpoch();
    }

    /**
     * TODO: documentation
     */
    public final void pushDialog(SDialog dialog) {
        super.addComponent(dialog, null);
        int count = getComponentCount();
        System.err.println("pushDialog: " + count);
        dialog.setFrame(this);
        reload(RELOAD_CODE);
    }

    /**
     * TODO: documentation
     */
    public final SDialog popDialog() {
        int count = getComponentCount();
        if (count <= 1)
            throw new IllegalStateException("there's no dialog left!");

        SDialog dialog = (SDialog)getComponent(count - 1);
        super.removeComponent(dialog);
        dialog.setFrame((SFrame)null);
        System.err.println("popDialog: " + count);

        reload(RELOAD_CODE);
        return dialog;
    }

    /**
     * TODO: documentation
     */
    public SContainer getContentPane() {
        return contentPane;
    }

    /**
     * Set server address.
     */
    public final void setRequestURL(RequestURL requestURL) {
        this.requestURL = requestURL;
    }

    /**
     * TODO: documentation
     */
    public final RequestURL getRequestURL() {
        RequestURL result = (RequestURL)requestURL.clone();
        // maybe null
        result.setResource(targetResource);
        return result;
    }

    /**
     * Set the target resource
     */
    public void setTargetResource(String targetResource) {
        this.targetResource = targetResource;
    }

    /**
     * TODO: documentation
     *
     * @return
     */
    public String getTargetResource() {
        return targetResource;
    }

    /**
     * Set the base target
     */
    public void setBaseTarget(String baseTarget) {
        this.baseTarget = baseTarget;
    }

    /**
     * TODO: documentation
     *
     * @return
     */
    public String getBaseTarget() {
        return baseTarget;
    }

    /*
     * Add meta tags in the form of three attributes.
     * For example:
     * <PRE>name="keywords" lang="de" content="Ferien, Griechenland, Sonnenschein"<PRE>
     *
     * @param m
     */
    public void addMeta(String m) {
        metas().add(m);
    }

    /**
     * TODO: documentation
     *
     */
    public void clearMetas() {
        metas().clear();
    }

    /**
     * TODO: documentation
     *
     * @return
     */
    public List metas() {
        if (metas == null)
            metas = new ArrayList(2);
        return metas;
    }

    public void addHeader(String m) {
	headers().add(m);
    }

    public void clearHeaders() {
	headers().clear();
    }
    
    public List headers() {
        if (headers == null)
            headers = new ArrayList(2);
	return headers;
    }

    public void addLink(SLink link) {
	links().add(link);
    }

    public void clearLinks() {
	links().clear();
    }
    
    public List links() {
        if (links == null)
            links = new ArrayList(2);
	return links;
    }

    /**
     * TODO: documentation
     *
     * @param t
     */
    public void setTitle(String title) {
        this.title = title;
    }
    /**
     * TODO: documentation
     *
     * @return
     */
    public String getTitle() { return title; }

    /**
     * TODO: documentation
     *
     * @param c
     */
    public void setTextColor(Color c) {
        textColor = c;
    }

    /**
     * TODO: documentation
     *
     * @return
     */
    public Color getTextColor() {
        return textColor;
    }

    /**
     * TODO: documentation
     *
     * @param c
     */
    public void setLinkColor(Color c) {
        linkColor = c;
    }

    /**
     * TODO: documentation
     *
     * @return
     */
    public Color getLinkColor() {
        return linkColor;
    }

    /**
     * TODO: documentation
     *
     * @param c
     */
    public void setVLinkColor(Color c) {
        vLinkColor = c;
    }

    /**
     * TODO: documentation
     *
     * @return
     */
    public Color getVLinkColor() {
        return vLinkColor;
    }

    /**
     * TODO: documentation
     *
     * @param c
     */
    public void setALinkColor(Color c) {
        aLinkColor = c;
    }

    /**
     * TODO: documentation
     *
     * @return
     */
    public Color getALinkColor() {
        return aLinkColor;
    }

    /**
     * TODO: documentation
     *
     * @param s
     */
    public void setStyleSheet(StyleSheet s) {
        styleSheet = s;
    }

    /**
     * TODO: documentation
     *
     * @return
     */
    public StyleSheet getStyleSheet() {
        return styleSheet;
    }

    /**
     * TODO: documentation
     *
     * @param s
     */
    public void setStatusLine(String s) {
        statusLine = s;
    }

    /**
     * TODO: documentation
     *
     * @deprecated don't use
     * @return
     */
    public String getStatusLine() {
        return statusLine;
    }

    /**
     * TODO: documentation
     *
     * @return
     */
    public String show() {
        StringBufferDevice erg = new StringBufferDevice();
        try {
            write(erg);
        }
        catch (IOException e) {
        }
        return erg.toString();
    }


    public void propertyChange(PropertyChangeEvent pe) {
        if ("lookAndFeel".equals(pe.getPropertyName())) {
            updateComponentTreeCG(getContentPane());
            System.err.println("lookAndFeel Change: " + pe.getPropertyName());
        }
        if ("request.url".equals(pe.getPropertyName())) {
            setRequestURL((RequestURL)pe.getNewValue());
        }
        else
            System.err.println("propertyChange: " + pe.getPropertyName());
    }

    private void updateComponentTreeCG(SComponent c) {
        if (c instanceof SComponent) {
            ((SComponent)c).updateCG();
        }
        if (c instanceof SContainer) {
            SComponent[] children = ((SContainer)c).getComponents();
            for(int i = 0; i < children.length; i++) {
                updateComponentTreeCG(children[i]);
            }
        }
        updateCG();
    }

    public String getCGClassID() {
        return cgClassID;
    }

    private class SStackLayout extends SAbstractLayoutManager
    {
        private SContainer container = null;

        public SStackLayout() {}

        public void updateCG() {}
        public void addComponent(SComponent c, Object constraint) {}
        public void removeComponent(SComponent c) {}

        public SComponent getComponentAt(int i) {
            return (SComponent)container.getComponent(i);
        }

        public void setContainer(SContainer c) {
            container = c;
        }

        /**
         * Allways write code for the topmost component.
         *
         * @param s
         * @throws IOException
         */
        public void write(Device s)
            throws IOException
        {
            int topmost = container.getComponentCount() - 1;
            SComponent comp = (SComponent)container.getComponent(topmost);
            comp.write(s);
        }
    }

    public void setCG(FrameCG cg) {
        super.setCG(cg);
    }

    public void invite(ComponentVisitor visitor) {
        visitor.visit(this);
        getContentPane().invite(visitor);
    }

}

/*
 * Local variables:
 * c-basic-offset: 4
 * indent-tabs-mode: nil
 * compile-command: "ant -emacs -find build.xml"
 * End:
 */
