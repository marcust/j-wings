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
import java.awt.Font;
import java.beans.*;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.logging.*;

import org.wings.io.Device;
import org.wings.io.StringBufferDevice;
import org.wings.plaf.*;
import org.wings.event.*;
import org.wings.plaf.ComponentCG;
import org.wings.session.Session;
import org.wings.session.SessionManager;
import org.wings.style.*;
import org.wings.externalizer.ExternalizeManager;
import org.wings.util.*;
import org.wings.script.ScriptListener;
import org.wings.border.SBorder;

/**
 * The basic component implementation for all components in this package.
 *
 * @author <a href="mailto:haaf@mercatis.de">Armin Haaf</a>
 * @version $Revision$
 */
public abstract class SComponent
    implements SConstants, Cloneable, Serializable, Renderable
{
    private final static Logger logger = Logger.getLogger("org.wings");

    /* */
    private transient String unifiedId = null;

    /** @see #getCGClassID */
    private static final String cgClassID = "ComponentCG";

    /** the session */
    private transient Session session = null;

    /**
     * The code generation delegate, which is responsible for
     * the visual representation of this component.
     */
    protected transient ComponentCG cg;

    /** Vertical alignment */
    protected int verticalAlignment = NO_ALIGN;

    /** Horizontal alignment */
    protected int horizontalAlignment = NO_ALIGN;

    /** The style class */
    protected Style style;

    /** The attributes */
    protected AttributeSet attributes = new SimpleAttributeSet();

    /** Visibility. */
    protected boolean visible = true;

    /** Enabled / disabled. */
    protected boolean enabled = true;

    /** The container, this component resides in. */
    protected SContainer parent = null;

    /** The frame, this component resides in. */
    protected SFrame parentFrame = null;

    /** The name of the component. */
    protected String name = null;

    /** The border for the component. */
    protected SBorder border = null;

    /** The tooltip for this component. */
    protected String tooltip = null;

    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    /** Preferred size of component in pixel. */
    protected SDimension preferredSize = null;

    protected ArrayList componentListeners;

    protected Map scriptListeners = new HashMap();

    /**
     * Default constructor.cript
     * The method updateCG is called to get a cg delegate installed.
     */
    public SComponent() {
        updateCG();
    }

    /**
     * Return the border of this component.
     * @return the border
     */
    public final SBorder getBorder() {
        return border;
    }

    /**
     * Set the border to be drawn around this component.
     * @param b the new border
     */
    public final void setBorder(SBorder b) {
        border = b;
    }

    /**
     * Return the parent container.
     * @return the container this component resides in
     */
    public final SContainer getParent() {
        return parent;
    }

    /**
     * Sets the parent container. Also gets the parent frame from the parent.
     *
     * @param p the container
     */
    public void setParent(SContainer p) {
        parent = p;
        if ( p!=null )
            setParentFrame(p.getParentFrame());
        else
            setParentFrame(null);
    }

    /**
     * Sets the parent frame.
     *
     * @param f the frame
     */
    protected void setParentFrame(SFrame f) {
        if ( f!=parentFrame ) {
            unregister();
            parentFrame = f;
            register();
        }
    }

    public RequestURL getRequestURL() {
        SFrame p = getParentFrame();
        if (p == null)
            throw new IllegalStateException("no parent frame");

        return p.getRequestURL();
    }

    /**
     * Set the preferred size of the receiving component in pixel.
     * It is not guaranteed that the component accepts this property because of
     * missing implementations in the component cg or html properties.
     * If <i>width</i> or <i>height</i> is zero, it is ignored and the browser
     * defines the size.
     * @see org.wings.SComponent#getPreferredSize
     * @see org.wings.SComponent#getPreferredPercentageSize
     */
    public void setPreferredSize(SDimension preferredSize) {
        this.preferredSize = preferredSize;
    }

    /**
     * Get the preferred size of this component.
     * @see SComponent#setPreferredSize
     * @see org.wings.SComponent#setPreferredPercentageSize
     */
    public final SDimension getPreferredSize() {
        return preferredSize;
    }


    /**
     * Adds the specified component listener to receive component events from
     * this component.
     * If l is null, no exception is thrown and no action is performed.
     * @param    l   the component listener.
     * @see      org.wings.event.SComponentEvent
     * @see      org.wings.event.SComponentListener
     * @see      org.wings.SComponent#removeComponentListener
     */
    public synchronized void addComponentListener(SComponentListener l) {
        if (l == null) {
            return;
        }
        if ( componentListeners == null ) componentListeners = new ArrayList();
        componentListeners.add( l );
    }

    /**
     * Removes the specified component listener so that it no longer
     * receives component events from this component. This method performs
     * no function, nor does it throw an exception, if the listener
     * specified by the argument was not previously added to this component.
     * If l is null, no exception is thrown and no action is performed.
     * @param    l   the component listener.
     * @see      org.wings.event.SComponentEvent
     * @see      org.wings.event.SComponentListener
     * @see      org.wings.SComponent#addComponentListener
     */
    public synchronized void removeComponentListener(SComponentListener l) {
        if (l == null) {
            return;
        }
        if ( componentListeners == null ) return;

        int index = componentListeners.indexOf( l );
        if ( index == -1 ) return;
        componentListeners.remove( index );
        return;
    }

    /**
     * Reports a component change.
     * @param aEvent report this event to all listeners
     * @see org.wings.event.SComponentListener
     */
    protected void fireComponentChangeEvent( SComponentEvent aEvent )
    {
        if ( componentListeners == null ) return;
        for ( ListIterator it = componentListeners.listIterator(); it.hasNext(); )
            processComponentEvent( (SComponentListener) it.next(), aEvent );
    }

    /**
     * Processes component events occurring on this component by
     * dispatching them to any registered
     * <code>SComponentListener</code> objects.
     * <p>
     * This method is not called unless component events are
     * enabled for this component. Component events are enabled
     * when one of the following occurs:
     * <p><ul>
     * <li>A <code>SComponentListener</code> object is registered
     * via <code>addComponentListener</code>.
     * </ul>
     * @param       e the component event.
     * @see         org.wings.event.SComponentEvent
     * @see         org.wings.event.SComponentListener
     * @see         org.wings.SComponent#addComponentListener
     */
    protected void processComponentEvent(SComponentListener listener, SComponentEvent e)
    {
        int id = e.getID();
        switch(id) {
        case SComponentEvent.COMPONENT_RESIZED:
            listener.componentResized(e);
            break;
        case SComponentEvent.COMPONENT_MOVED:
            listener.componentMoved(e);
            break;
        case SComponentEvent.COMPONENT_SHOWN:
            listener.componentShown(e);
            break;
        case SComponentEvent.COMPONENT_HIDDEN:
            listener.componentHidden(e);
            break;
        }
    }

    /**
     * Adds the specified component listener to receive component events from
     * this component.
     * If l is null, no exception is thrown and no action is performed.
     * @param    l   the component listener.
     * @see      org.wings.event.SComponentEvent
     * @see      org.wings.event.SComponentListener
     * @see      org.wings.SComponent#removeComponentListener
     */
    public synchronized void addScriptListener(ScriptListener l) {
        if (l == null)
            return;

        scriptListeners.put(l.getEvent(), l);
        if (l.getScript() != null)
            reload(ReloadManager.RELOAD_SCRIPT);
    }

    /**
     * Removes the specified component listener so that it no longer
     * receives component events from this component. This method performs
     * no function, nor does it throw an exception, if the listener
     * specified by the argument was not previously added to this component.
     * If l is null, no exception is thrown and no action is performed.
     * @param    l   the component listener.
     * @see      org.wings.event.SComponentEvent
     * @see      org.wings.event.SComponentListener
     * @see      org.wings.SComponent#addComponentListener
     */
    public synchronized void removeScriptListener(ScriptListener l) {
        if (l == null)
            return;

        if (scriptListeners.remove(l.getEvent()) != null && l.getScript() != null)
            reload(ReloadManager.RELOAD_SCRIPT);
    }

    public Collection getScriptListeners() { return scriptListeners.values(); }

    /**
     * Return a jvm wide unique id.
     * @return an id
     */
    public final String getUnifiedId() {
        if (unifiedId == null)
            unifiedId = getSession().createUniqueId();
        return unifiedId;
    }

    /**
     * Return the session this component belongs to.
     *
     * @return the session
     */
    public Session getSession() {
        if (session == null) {
            session = SessionManager.getSession();
        }

        return session;
    }

    /**
     * Return the dispatcher.
     *
     * @return the dispatcher
     */
    public SRequestDispatcher getDispatcher() {
        if ( getParentFrame()==null )
            return null;
        else
            return getParentFrame().getDispatcher();
    }

    /**
     * Set the locale.
     *
     * @param l the new locale
     */
    public void setLocale(Locale l) {
        getSession().setLocale(l);
    }

    /**
     * Return the local.
     *
     * @return the locale
     */
    public Locale getLocale() {
        return getSession().getLocale();
    }

    /*
     * If a subclass implements the {@link RequestListener} interface,
     * it will be unregistered at the associated dispatcher.
     */
    private void unregister() {
        if (getDispatcher() != null && this instanceof RequestListener) {
            getDispatcher().unregister((RequestListener)this);
        }
    }

    /*
     * If a subclass implements the {@link RequestListener} interface,
     * it will be registered at the associated dispatcher.
     */
    private void register() {
        if (getDispatcher() != null && this instanceof RequestListener) {
            getDispatcher().register((RequestListener)this);
        }
    }

    /**
     * Watch components beeing garbage collected.
    protected void finalize() {
        System.out.println("finalize " + getClass().getName());
    }
     */

    /**
     * Set the class of the laf-provided style.
     * @param style the new value for style
     */
    public void setStyle(Style style) {
        this.style = style;
    }
    /**
     * @return the current style
     */
    public Style getStyle() { return style; }

    /**
     * Set a attribute.
     * @param name the attribute name
     * @param value the attribute value
     */
    public void setAttribute(String name, String value) {
        boolean changed = attributes.isDefined(name);
        attributes.putAttribute(name, value);

        if (changed)
            reload(ReloadManager.RELOAD_STYLE);
    }

    /**
     * return the value of an attribute.
     * @param name the attribute name
     */
    public String getAttribute(String name) {
        return attributes.getAttribute(name);
    }

    /**
     * remove an attribute
     * @param name the attribute name
     */
    public String removeAttribute(String name) {
        if ( attributes.isDefined(name) ) {
            String value = attributes.removeAttribute(name);

            reload(ReloadManager.RELOAD_STYLE);

            return value;
        }

        return null;
    }


    /**
     * Set the attributes.
     * @param attributes the attributes
     */
    public void setAttributes(AttributeSet attributes) {
        if (attributes == null)
            throw new IllegalArgumentException("null not allowed");

        if (!this.attributes.equals(attributes)) {
            this.attributes = attributes;
            reload(ReloadManager.RELOAD_STYLE);
        }
    }

    /**
     * @return the current attributes
     */
    public AttributeSet getAttributes() {
        return attributes;
    }

    /**
     * Set the background color.
     * @param c the new background color
     */
    public void setBackground(Color color) {
        setAttribute(Style.BACKGROUND_COLOR, CSSStyleSheet.getAttribute(color));
    }

    /**
     * Return the background color.
     * @return the background color
     */
    public Color getBackground() {
        return CSSStyleSheet.getBackground(attributes);
    }

    /**
     * Set the foreground color.
     * @param c the new foreground color
     */
    public void setForeground(Color color) {
        setAttribute(Style.COLOR, CSSStyleSheet.getAttribute(color));
    }

    /**
     * Return the foreground color.
     * @return the foreground color
     */
    public Color getForeground() {
        return CSSStyleSheet.getForeground(attributes);
    }

    /**
     * Set the font.
     * @param f the new font
     */
    public void setFont(Font f) {
        if (f == null) {
            setFont((SFont)null);
            return;
        }

        SFont font = new SFont(f.getName(), f.getStyle(), f.getSize());
        setFont(font);
    }

    /**
     * Set the font.
     * @param f the new font
     */
    public void setFont(SFont font) {
        boolean changed = attributes.putAttributes(CSSStyleSheet.getAttributes(font));
        if (changed)
            reload(ReloadManager.RELOAD_STYLE);
    }

    /**
     * Return the font.
     * @return f the font
     */
    public SFont getFont() {
        return CSSStyleSheet.getFont(attributes);
    }

    /**
     * Set the visibility.
     * @param v wether this component wil show or not
     */
    public void setVisible(boolean v) {
        boolean old = visible;
        visible = v;
        if (old != visible) {
            reload(ReloadManager.RELOAD_CODE);
            SComponentEvent evt = new SComponentEvent(this, v ?
                                                      SComponentEvent.COMPONENT_SHOWN :
                                                      SComponentEvent.COMPONENT_HIDDEN);
            fireComponentChangeEvent(evt);
        }
    }

    /**
     * Return the visibility.
     *
     * @return wether the component will show
     * @deprecated use isVisible instead
     */
    public boolean getVisible() {
        return visible;
    }

    /**
     * Return the visibility.
     *
     * @return wether the component will show
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * Set wether this component should be enabled.
     *
     * @param v true if the component is enabled, false otherwise
     */
    public void setEnabled(boolean e) {
        boolean old = enabled;
        enabled = e;
        if (old != enabled)
            reload(ReloadManager.RELOAD_CODE);
    }

    /**
     * Return true if this component is enabled.
     *
     * @return true if component is enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Return the name of this component.
     *
     * @return the name of this component
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name of this component.
     *
     * @param n the new name for this component
     */
    public void setName(String n) {
        name = n;
    }


    /**
     * Mark the component as subject to reload.
     * The component will be registered with the ReloadManager.
     */
    public final void reload(int aspect) {
        getSession().getReloadManager().reload(this, aspect);
    }

    /**
     * Let the delegate write the component's code to the device.
     *
     * @param s the Device to write into
     * @throws IOException Thrown when the connection to the client gets broken,
     *         for example when the user stops loading
     */
    public void write(Device s) throws IOException {
        try {
            if (visible)
                cg.write(s, this);
        }
        catch (Throwable t) {
            System.err.println(t.getMessage());
            t.printStackTrace(System.err);
            logger.log(Level.SEVERE, "exception during code generation", t);
        }
    }

    /**
     * renders the component into a string.
     */
    public String toString() {
        Device d = new StringBufferDevice();
        try {
            write(d);
        }
        catch (IOException e) {}

        return d.toString();
    }


    /**
     * Generic implementation for generating a string that represents the components
     * configuration.
     * @return a string containing all properties
     */
    public String paramString() {
        StringBuffer buffer = new StringBuffer(getClass().getName());
        buffer.append("[");

        try {
            BeanInfo info = Introspector.getBeanInfo(getClass());
            PropertyDescriptor[] descriptors = info.getPropertyDescriptors();

            boolean first=true;
            for (int i=0; i < descriptors.length; i++) {
                try {
                    Method getter = descriptors[i].getReadMethod();
                    if (getter == null)
                        continue;
                    Object value = getter.invoke(this, null);
                    if (first)
                        first = false;
                    else
                        buffer.append(",");
                    buffer.append(descriptors[i].getName() + "=" + value);
                }
                catch (Exception e) {}
            }
        }
        catch (Exception e) {}

        buffer.append("]");
        return buffer.toString();
    }

    /**
     * Return a unique name prefix.
     *
     * @return a unique name prefix
     */
    public String getNamePrefix() {
        if (getParentFrame() != null)
            return getParentFrame().getEventEpoch() + SConstants.UID_DIVIDER + getUnifiedId();
        return getUnifiedId();
    }

    /**
     * Return the parent frame.
     *
     * @return the parent frame
     */
    public SFrame getParentFrame() {
        return parentFrame;
    }

    /**
     * Return true, if this component is contained in a form.
     *
     * @return true, if this component resides in a form, false otherwise
     */
    public final boolean getResidesInForm() {
        SComponent parent = getParent();

        boolean actuallyDoes = false;
        while (parent != null && !(actuallyDoes = (parent instanceof SForm)))
            parent = parent.getParent();

        return actuallyDoes;
    }

    /**
     * Set the tooltip text.
     *
     * @param t the new tooltip text
     */
    public void setToolTipText(String t) {
        tooltip = t;
    }

    /**
     * Return the tooltip text.
     *
     * @return the tooltip text
     */
    public String getToolTipText() { return tooltip; }

    /**
     * Clone this component.
     *
     * @return a clone of this component
     */
    public Object clone() {
        try {
            return super.clone();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Return the value of the horizontal alignment property.
     * @return the horizontal alignment
     * @see SConstants
     */
    public final int getHorizontalAlignment() {
        return horizontalAlignment;
    }

    /**
     * Set the horizontal alignment.
     * @param the new value for the horizontal alignment
     * @see SConstants
     */
    public final void setHorizontalAlignment(int alignment) {
        horizontalAlignment = alignment;
    }

    /**
     * Set the vertical alignment.
     * @param the new value for the vertical alignment
     * @see SConstants
     */
    public final void setVerticalAlignment(int alignment) {
        verticalAlignment = alignment;
    }

    /**
     * Return the value of the vertical alignment property.
     * @return the vertical alignment
     * @see SConstants
     */
    public final int getVerticalAlignment() {
        return verticalAlignment;
    }

    private Map clientProperties;

    /**
     * @return a small Hashtable
     * @see #putClientProperty
     * @see #getClientProperty
     */
    private Map getClientProperties() {
        if (clientProperties == null) {
            clientProperties = new HashMap(2);
        }
        return clientProperties;
    }


    /**
     * Returns the value of the property with the specified key.  Only
     * properties added with <code>putClientProperty</code> will return
     * a non-null value.
     *
     * @return the value of this property or null
     * @see #putClientProperty
     */
    public final Object getClientProperty(Object key) {
        if(clientProperties == null) {
            return null;
        }
        else {
            return getClientProperties().get(key);
        }
    }

    /**
     * Add an arbitrary key/value "client property" to this component.
     * <p>
     * The <code>get/putClientProperty<code> methods provide access to
     * a small per-instance hashtable. Callers can use get/putClientProperty
     * to annotate components that were created by another module, e.g. a
     * layout manager might store per child constraints this way.  For example:
     * <pre>
     * componentA.putClientProperty("to the left of", componentB);
     * </pre>
     * <p>
     * If value is null this method will remove the property.
     * Changes to client properties are reported with PropertyChange
     * events.  The name of the property (for the sake of PropertyChange
     * events) is <code>key.toString()</code>.
     * <p>
     * The clientProperty dictionary is not intended to support large
     * scale extensions to SComponent nor should be it considered an
     * alternative to subclassing when designing a new component.
     *
     * @see #getClientProperty
     * @see #addPropertyChangeListener
     */
    public final void putClientProperty(Object key, Object value) {
        Object oldValue = getClientProperties().get(key);

        if (value != null) {
            getClientProperties().put(key, value);
        } else {
            getClientProperties().remove(key);
        }

        firePropertyChange(key.toString(), oldValue, value);
    }

    /**
     * Add a PropertyChangeListener to the current list of listeners.
     *
     * @param l some class implementing the PropertyChangeListener interface
     */
    public void addPropertyChangeListener(PropertyChangeListener l) {
	propertyChangeSupport.addPropertyChangeListener(l);
    }

    /**
     * Remove a PropertyChangeListener from the current list of listeners.
     *
     * @param l the listener to be removed
     */
    public void removePropertyChangeListener(PropertyChangeListener l) {
	propertyChangeSupport.removePropertyChangeListener(l);
    }

    /**
     * Notify all listeners that a property change has occured.
     * @param propertyName the name of the property
     * @param oldValue the old value
     * @param newValue the new value
     */
    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }

    /**
     * Notify all listeners that a property change has occured.
     * @param propertyName the name of the property
     * @param oldValue the old value
     * @param newValue the new value
     */
    protected void firePropertyChange(String propertyName, int oldValue, int newValue) {
        propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }

    /**
     * Notify all listeners that a property change has occured.
     * @param propertyName the name of the property
     * @param oldValue the old value
     * @param newValue the new value
     */
    protected void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {
        propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }

    /**
     * Set the look and feel delegate for this component.
     * SComponent subclasses generally override this method
     * to narrow the argument type, e.g. in STextField:
     * <pre>
     * public void setCG(TextFieldCG newCG) {
     *     super.setCG(newCG);
     * }
     * </pre>
     *
     * @see #updateCG
     * @see org.wings.plaf.CGManager#getLookAndFeel
     * @see org.wings.plaf.CGManager#getCG
     * @beaninfo
     *        bound: true
     *  description: The component's look and feel delegate
     */
    protected void setCG(ComponentCG newCG) {
        /* We do not check that the CG instance is different
         * before allowing the switch in order to enable the
         * same CG instance *with different default settings*
         * to be installed.
         */
        if (cg != null) {
            cg.uninstallCG(this);
        }
        ComponentCG oldCG = cg;
        cg = newCG;
        if (cg != null) {
            cg.installCG(this);
        }
        firePropertyChange("CG", oldCG, newCG);

        if ((cg == null && oldCG != null) ||
            (cg != null && !cg.equals(oldCG)))
            reload(ReloadManager.RELOAD_ALL);
    }

    /**
     * Return the look and feel delegate.
     *
     * @return the componet's cg
     */
    public ComponentCG getCG() {
        return cg;
    }

    /**
     * Notification from the CGFactory that the L&F has changed.
     *
     * @see SComponent#updateCG
     */
    public void updateCG() {
        if (getSession() == null) {
            logger.warning("no session yet.");
        }
        else if (getSession().getCGManager() == null) {
            logger.warning("no CGManager");
        }
        else {
            setCG((ComponentCG)getSession().getCGManager().getCG(this));
        }
        if (border != null)
            border.updateCG();
    }

    /**
     * Returns the name of the CGFactory class that generates the
     * look and feel for this component.
     *
     * @return content of private static final cgClassID attribute
     * @see SComponent#getCGClassID
     * @see org.wings.plaf.CGDefaults#getCG
     */
    public String getCGClassID() {
        return cgClassID;
    }

    /**
     * Invite a ComponentVisitor.
     * Invokes visit on the ComponentVisitor.
     * @param visitor the visitor to be invited 
     */
    public void invite(ComponentVisitor visitor)
        throws Exception
    {
        visitor.visit(this);
    }
}

/*
 * Local variables:
 * c-basic-offset: 4
 * indent-tabs-mode: nil
 * compile-command: "ant -emacs -find build.xml"
 * End:
 */
