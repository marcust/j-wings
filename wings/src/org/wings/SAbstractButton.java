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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.*;
import javax.swing.event.EventListenerList;
import javax.swing.Action;

import org.wings.plaf.*;
import org.wings.io.Device;
import org.wings.style.Style;

/**
 * This is the base class for all components which have a button
 * functionality. This class handles ActionListener notification.
 *
 * @author <a href="mailto:armin.haaf@mercatis.de">Armin Haaf</a>
 * @version $Revision$
 */
public abstract class SAbstractButton
    extends SAbstractIconTextCompound
    implements RequestListener
{
    public static final String SUBMIT_BUTTON  = "submit";
    public static final String RESET_BUTTON   = "reset";
    public static final String IMAGE_BUTTON   = "image";
    public static final String CHECKBOX       = "checkbox";
    public static final String RADIOBUTTON    = "radio";


    /**
     * button type
     * @see #setType
     */
    private String type = SUBMIT_BUTTON;

    /**
     *
     */
    private SButtonGroup buttonGroup;

    /**
     * TODO: documentation
     */
    protected final EventListenerList listenerList = new EventListenerList();

    /**
     * TODO: documentation
     */
    protected String actionCommand;

    /**
     * If this is set to false, the button is always rendered as anchor
     * button. Else the button is rendered as anchor button if it is disabled or if
     * it is not inside a Form.
     */
    private boolean showAsFormComponent = true;

    /**
     * 
     */
    private String eventTarget;

    /**
     * 
     */
    private Action action;

    /**
     * 
     */
    private PropertyChangeListener actionPropertyChangeListener;
    

    /**
     * Create a button with given text.
     * @param text the button text
     */
    public SAbstractButton(String text) {
        setText(text);
    }

    /**
     * @param action
     */
    public SAbstractButton(Action action) {
        setAction(action);
    }

    /**
     * Creates a new Button with the given Text and the given Type.
     *
     * @param text the button text
     * @param type the button type
     * @see #setType
     */
    public SAbstractButton(String text, String type) {
        this(text);
        setType(type);
    }

    /**
     * Creates a new submit button
     */
    public SAbstractButton() {
        this("");
    }

    /**
     * Set display mode (href or form-component).
     * An AbstractButton can appear as HTML-Form-Button or as 
     * HTML-HREF. If button is inside a {@link org.wings.SForm} the default
     * is displaying it as html form button.
     * Setting <i>showAsFormComponent</i> to <i>false</i> will
     * force displaying as href even if button is inside 
     * a form.
     * @param showAsFormComponent if true, display as link, if false as html form component.
     */
    public void setShowAsFormComponent(boolean showAsFormComponent) {
        this.showAsFormComponent = showAsFormComponent;
    }

	/**
      * Test, what display method is set.
      * @see #setShowAsFormComponent(boolean)
      * @return treu, if displayed as link, false when displayed as html form component.
      */
    public boolean getShowAsFormComponent() {
        return showAsFormComponent && getResidesInForm();
    }

    /**
     * TODO: documentation
     *
     * @param ac
     */
    public void setActionCommand(String ac) {
        actionCommand = ac;
    }

    /**
     * TODO: documentation
     *
     * @return
     */
    public final String getActionCommand() {
        return actionCommand;
    }


    /**
     * TODO: documentation
     *
     * @return
     */
    public final SButtonGroup getGroup() {
        return buttonGroup;
    }

    /**
     * Add this button to a button group. This influences the event-prefix
     * this button reports to the request dispatcher: it will change to
     * the button group's prefix.
     * @param g
     */
    protected void setGroup(SButtonGroup g) {
        if ( isDifferent(buttonGroup, g) ) {
            if (getDispatcher()!=null ) {
                getDispatcher().unregister(this);
                buttonGroup = g;
                getDispatcher().register(this);
            } else {
                buttonGroup = g;
            }
            reload(ReloadManager.RELOAD_CODE);
        }
    }

    /**
     *
     */
    public String getNamePrefix() {
        if ( buttonGroup==null )
            return super.getNamePrefix();
        else
            return buttonGroup.getNamePrefix();
    }

    /**
     * TODO: documentation
     *
     * @param listener
     */
    public void addActionListener(ActionListener listener) {
        listenerList.add(ActionListener.class, listener);
    }

    /**
     * TODO: documentation
     *
     * @param listener
     */
    public void removeActionListener(ActionListener listener) {
        listenerList.remove(ActionListener.class, listener);
    }

    /**
     * Fire an ActionEvent at each registered listener.
     */
    protected void fireActionPerformed() {
        fireActionEvent(new ActionEvent(this, ActionEvent.ACTION_PERFORMED,
                                        actionCommand==null ? getText() : actionCommand));
    }

    /**
     * Fire an ActionEvent at each registered listener.
     */
    protected final void fireActionEvent(ActionEvent e) {
        if ( e==null )
            return;

        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i] == ActionListener.class) {
                ((ActionListener)listeners[i+1]).actionPerformed(e);
            }
        }
    }

    /**
     * Sets the button type. Use one of the following types:
     * <UL>
     * <LI> {@link #SUBMIT_BUTTON}
     * <LI> {@link #RESET_BUTTON}
     * <LI> {@link #CHECKBOX}
     * <LI> {@link #RADIOBUTTON}
     * </UL>
     *
     * @param t
     */
    public void setType(String t) {
        if ( isDifferent(type, t) ) {
            type = t;
            reload(ReloadManager.RELOAD_CODE);
        }
    }

    /**
     * TODO: documentation
     *
     * @return
     */
    public final String getType() {
        return type;
    }

    /**
     * TODO: documentation
     */
    public void doClick() {
        setSelected(!isSelected());

        fireActionPerformed();
    }

    /**
     * TODO: documentation
     *
     * @return
     */
    public void setSelected(boolean b) {
        if ( isSelected()!=b ) {
            if ( buttonGroup!=null )
                buttonGroup.setSelected(this, b);

            super.setSelected(b);
        }
    }

    public void processRequest(String action, String[] values) {
        /*
          System.out.print("action " + action);
        for ( int i=0; i<values.length; i++ ) {
            System.out.print(" " + values[i]);
        }
        System.out.println();
        */

        boolean requestSelection = isSelected(); 
        
        int eventCount = 0;

        if ( buttonGroup!=null ) {
            // button group prefix is shared, so maybe more than one value is
            // delivered in a form 
            for ( int i=0; i<values.length; i++ ) {

                // with button group the value has a special encoding...
                // this is because in a form the name of a parameter for
                // buttons in a buttongroup must be the same...
                String value = values[i];

                // illegal format
                if ( value.length() < 3 ) { continue;  }

                // no uid DIVIDER
                // value.charAt(value.length()-2)!=UID_DIVIDER ) { break; }

                // not for me
                if ( !value.startsWith(getUnifiedId()) ) { continue; }

                // last character is indicator, if button should be 
                // selected or not
                switch ( value.charAt(value.length()-1) ) {
                case '1':
                    requestSelection = true;
                    ++eventCount;
                    break;
                case '0':
                    requestSelection = false;
                    ++eventCount;
                    break;
                }
            }
        } 
        else {
            for ( int i=0; i<values.length; i++ ) {
                requestSelection = parseSelectionToggle(values[0]);
                ++eventCount;
            }
        }

        /*
         * Checkboxes in HTML-forms write two form components:
         * one hidden input, containing the deselect-command (value='0'),
         * and one <input type="checkbox".. value="1">.
         * This is, because browsers send the checkbox-variable
         * only if it is set, not if it is not set.
         * So, if we get two events with the same name, then the checkbox
         * actually got selected; if we only got one event (from the hidden
         * input), then it was a deselect-event (select event value = '1',
         * deselect value = '0').
         * This is just in case, the browser sends the both events
         * in the wrong order (select and then deselect).
         */
        if ( eventCount==2 ) {
            requestSelection = true;
        }

        if ( isSelected()!=requestSelection ) {
            if ( buttonGroup!=null ) {
                buttonGroup.setDelayEvents(true);
                setSelected(requestSelection);
                buttonGroup.setDelayEvents(false);
            } else {
                setSelected(requestSelection);
            }
            
            SForm.addArmedComponent(this);
        }
    }

    public void fireIntermediateEvents() {
        if ( buttonGroup!=null ) {
            buttonGroup.fireDelayedIntermediateEvents();
        }
    }

    public void fireFinalEvents() {
        fireActionPerformed();
        if ( buttonGroup!=null ) {
            buttonGroup.fireDelayedFinalEvents();
        }
    }

    /**
     * @deprecated use {@link #getEventTarget}
     */
    public final String getRealTarget() {
        return getEventTarget();
    }

    /**
     * @deprecated use {@link #setEventTarget}
     */
    public final void setRealTarget(String t) {
        setEventTarget(t);
    }

    /**
     *
     */
    public final String getEventTarget() {
        return eventTarget;
    }

    /**
     *
     */
    public void setEventTarget(String target) {
        if ( isDifferent(eventTarget, target) ) {
            eventTarget = target;
            reload(ReloadManager.RELOAD_CODE);
        }
    }

    protected boolean parseSelectionToggle(String toggleParameter) {
        if ( "1".equals(toggleParameter) )
            return true;
        else if ( "0".equals(toggleParameter) )
            return false;

        // don't change...
        return isSelected();
    }

    public String getSelectionToggleParameter() {
        return isSelected() ? getDeselectParameter() : getSelectParameter();
    }

    public String getSelectParameter() {
        String result = "1";
        if ( buttonGroup!=null ) {
            result = getUnifiedId() + UID_DIVIDER + result;
        }
        return result;
    }

    public String getDeselectParameter() {
        String result = "0";
        if ( buttonGroup!=null ) {
            result = getUnifiedId() + UID_DIVIDER + result;
        }
        return result;
    }


    // action implementation

    public void setAction(Action a) {
	Action oldValue = getAction();
	if (action == null || !action.equals(a)) {
	    action = a;
	    if (oldValue != null) {
		removeActionListener(oldValue);
		oldValue.removePropertyChangeListener(actionPropertyChangeListener);
		actionPropertyChangeListener = null;
	    }
	    configurePropertiesFromAction(action);
	    if (action != null) {		
		// Don't add if it is already a listener
		if (!isListener(ActionListener.class, action)) {
		    addActionListener(action);
		}
		// Reverse linkage:
		actionPropertyChangeListener = createActionPropertyChangeListener(action);
		action.addPropertyChangeListener(actionPropertyChangeListener);
	    }
	    firePropertyChange("action", oldValue, action);
	}
    }

    private boolean isListener(Class c, ActionListener a) {
	boolean isListener = false;
	Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i] == c && listeners[i+1] == a) {
                isListener = true;
	    }
	}
	return isListener;
    }

    public Action getAction() {
	return action;
    }

    protected void configurePropertiesFromAction(Action a) {
        // uncomment if compiled against < jdk 1.3
        //	setActionCommand((a != null 
        //                  ? (String)a.getValue(Action.ACTION_COMMAND_KEY) 
        //                  : null));
	setText((a != null ? (String)a.getValue(Action.NAME) : null));
	setIcon((a != null ? (SIcon)a.getValue(Action.SMALL_ICON) : null));
	setEnabled((a != null ? a.isEnabled() : true));
 	setToolTipText((a != null ? (String)a.getValue(Action.SHORT_DESCRIPTION) : null));	
    }

    protected PropertyChangeListener createActionPropertyChangeListener(Action a) {
        return new ButtonActionPropertyChangeListener(this, a);
    }

    private static class ButtonActionPropertyChangeListener
        extends AbstractActionPropertyChangeListener
    {
	ButtonActionPropertyChangeListener(SAbstractButton b, Action a) {
	    super(b, a);
	}

	public void propertyChange(PropertyChangeEvent e) {	    
	    String propertyName = e.getPropertyName();
	    SButton button = (SButton)getTarget();
	    if (button == null) {
		Action action = (Action)e.getSource();
		action.removePropertyChangeListener(this);
            }
            else {
                if (e.getPropertyName().equals(Action.NAME)) {
                    String text = (String)e.getNewValue();
                    button.setText(text);
                }
                else if (e.getPropertyName().equals(Action.SHORT_DESCRIPTION)) {
                    String text = (String)e.getNewValue();
                    button.setToolTipText(text);
                }
                else if (propertyName.equals("enabled")) {
                    Boolean enabled = (Boolean)e.getNewValue();
                    button.setEnabled(enabled.booleanValue());
                }
                else if (e.getPropertyName().equals(Action.SMALL_ICON)) {
                    SIcon icon = (SIcon) e.getNewValue();
                    button.setIcon(icon);
                }
                // uncomment if compiled against jdk < 1.3
                /*                else if (e.getPropertyName().equals(Action.ACTION_COMMAND_KEY)) {
                    String actionCommand = (String)e.getNewValue();
                    button.setActionCommand(actionCommand);
                    }*/
            }
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







