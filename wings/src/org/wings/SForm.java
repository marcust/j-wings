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

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.net.URL;
import java.util.*;
import java.util.logging.*;

import javax.swing.event.EventListenerList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.wings.plaf.*;
import org.wings.io.Device;

/*
 * Ein Form Container. In HTML ist die einzige Moeglichkeit
 * Benutzerinteraktion zu haben durch Forms gegeben. Alle
 * HTML Komponenten, die auf Benutzerinteraktion angewiesen sind
 * muessen in einen Form Container eingebettet sein.
 */

/**
 * TODO: documentation
 *
 * @author Dominik Bartenstein
 * @author <a href="mailto:haaf@mercatis.de">Armin Haaf</a>
 * @version $Revision$
 */
public class SForm
    extends SContainer
    implements RequestListener
{
    private final static Logger logger = Logger.getLogger("org.wings");

    /**
     * @see #getCGClassID
     */
    private static final String cgClassID = "FormCG";

    /**
     * Use method POST for submission of the data.
     */
    private boolean postMethod = true;

    /**
     * EncondingType for submission of the data.
     */
    private String encType = null;

    /**
     * URL to which data
     * should be sent to
     */
    private URL action = null;

    /**
     * TODO: documentation
     */
    protected final EventListenerList listenerList = new EventListenerList();

    /**
     * TODO: documentation
     */
    protected String actionCommand = null;

    /*
     * In dieser Map werden die Eventqueues der verschiedenen
     * Threads verwaltet. Das ist deshlab noetig, weil ein Thread sonst
     * die EventQueue eines anderen leeren (feuern) koennte, obwohl noch
     * kein konsistener Zustand der {@link SComponent} erreicht ist.
     * <P>
     * Beispiel: Ein Thread haelt ein Button und eine Liste. In der
     * Liste wird ein Element selektiert, der Button wird gedrueckt. Im
     * Extremfall werden diese Get Aktionen vom Dispatcher bearbeitet
     * und eventuell zuerst die Aktion des Buttons und dann erst die
     * Selektion. Der Anwendungsprogrammierer reagiert auf den Button
     * Event und liest den Zustand der Liste aus. Sind alle Get Aktionen
     * bearbeitet worden, kein Problem. Wird aber der Event des Buttons
     * von einem anderen Thread, der zufaellig gerade seine Events
     * feuert, gefeuert, kann es sein dass die Listen Selektion noch
     * nicht bearbeitet ist und der Zustand der Liste nicht mit dem der
     * Liste auf HTML Seite uebereinstimmt. Das darf nicht
     * passieren. Also koennen Events nur vom erzeugenden Thread
     * gefeuert werden.
     */
    private static final HashMap threads = new HashMap();

    /**
     * TODO: documentation
     *
     * @param action
     */
    public SForm(URL action) {
        setAction(action);
    }

    /**
     * TODO: documentation
     *
     */
    public SForm() {
    }

    /**
     * TODO: documentation
     *
     * @param layout
     */
    public SForm(SLayoutManager layout) {
        super(layout);
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
    public String getActionCommand() {
        return actionCommand;
    }

    /**
     * Add a listener for Form events. A Form event is always triggered, when
     * a form has been submitted. Usually, this happens, whenever a submit
     * button is pressed or some other mechanism triggered the posting of the
     * form. Other mechanisms are
     * <ul>
     * <li> Java Script submit() event</li>
     * <li> If a form contains a single text input, then many browsers
     *      submit the form, if the user presses RETURN in that field. In that
     *      case, the submit button will <em>not</em> receive any event but
     *      only the form.
     * <li> The {@link SFileChooser} will trigger a form event, if the file 
     *      size exceeded the allowed size. In that case, even if the submit
     *      button has been pressed, no submit-button event will be triggered.
     *      (For details, see {@link SFileChooser}).
     * </ul>
     *
     * @param listener
     */
    public void addActionListener(ActionListener listener) {
        listenerList.add(ActionListener.class, listener);
    }

    /**
     * Remove a form action listener, that has been added in
     * {@link #addActionListener(ActionListener)}
     *
     * @param listener
     */
    public void removeActionListener(ActionListener listener) {
        listenerList.remove(ActionListener.class, listener);
    }

    /**
     * Fire a ActionEvent at each registered listener.
     */
    protected void fireActionPerformed() {
        ActionEvent e = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, actionCommand);
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

    public final static void addArmedComponent(RequestListener component) {
        Thread thread = Thread.currentThread();

        List armedComponents = (List)threads.get(thread);
        if (armedComponents == null) {
            armedComponents = new ArrayList(2);
            threads.put(thread, armedComponents);
        }
        armedComponents.add(component);
    }

    /*
     * Die Sache muss natuerlich Thread Save sein, d.h. es duerfen nur
     * die Events gefeuert werden, die auch aus dem feuernden Thread
     * stammen (eben dem Dispatcher Thread). Sichergestellt wird das
     * dadurch das beim abfeuern der Event in eine Queue (ArrayList)
     * gestellt wird, die zu dem feuernden Event gehoert. Diese Queues
     * der verschiedenen Threads werden in einer Map verwaltet.
     * Beim feuern wird dann die Queue, die dem aktuellen Thread
     * entspricht gefeuert und aus der Map entfernt.
     * <P> <EM>VORSICHT:</EM><BR>
     * Die Map verwaltet natuerlich die Threads und haelt Zeiger
     * auf diese. Im Normalfall werden diese Zeiger durch das feuern
     * geloescht, aber falls nicht, werden die Threads nicht
     * aufgeraeumt!!!
     */
    /**
     * TODO: documentation
     */
    public static void fireEvents() {
        Thread thread = Thread.currentThread();

        List armedComponents = (List)threads.remove(thread);
        if (armedComponents != null) {
            RequestListener component;
            Iterator iterator = armedComponents.iterator();
            while (iterator.hasNext()) {
                component = (RequestListener)iterator.next();
                component.fireIntermediateEvents();
            }
            iterator = armedComponents.iterator();
            while (iterator.hasNext()) {
                component = (RequestListener)iterator.next();
                component.fireFinalEvents();
            }
        }
    }


    /**
     * Set, whether this form is to be transmitted via <code>POST</code> (true)
     * or <code>GET</code> (false). The default, and this is what you 
     * usually want, is <code>POST</code>.
     *
     * @param postMethod
     */
    public void setPostMethod(boolean postMethod) {
        this.postMethod = postMethod;
    }

    /**
     * Returns, whether this form is transmitted via <code>POST</code> (true)
     * or <code>GET</code> (false).
     *
     * @return
     */
    public boolean isPostMethod() {
        return postMethod;
    }

    /**
     * Set the encoding of this form. This actually is an HTML interna
     * that bubbles up here. By default, the encoding type of any HTML-form
     * is <code>application/x-www-form-urlencoded</code>, and as such, needn't
     * be explicitly set with this setter. However, if you've included a
     * file upload element (as represented by {@link SFileChooser}) in your
     * form, this must be set to <code>multipart/form-data</code>, since only
     * then, files are transmitted correctly. In 'normal' forms without
     * file upload, it is not necessary to set it to
     * <code>multipart/form-data</code>; actually it enlarges the data to
     * be transmitted, so you probably don't want to do this, then.
     *
     * @param type the encoding type; one of <code>multipart/form-data</code>,
     *             <code>application/x-www-form-urlencoded</code>.
     */
    public void setEncodingType(String type) {
        encType = type;
    }

    /**
     * Get the current encoding type, as set with 
     * {@link #setEncodingType(String)}
     *
     * @return string containting the encoding type. This is something like
     *         <code>multipart/form-data</code>, 
     *         <code>application/x-www-form-urlencoded</code> .. or 'null'
     *         by default.
     */
    public String getEncodingType() {
        return encType;
    }

    /**
     * TODO: documentation
     *
     * @param action
     */
    public void setAction(URL action) {
        this.action = action;
    }

    /**
     * TODO: documentation
     *
     * @return
     */
    public URL getAction() {
        return action;
    }

    /**
     * TODO: documentation
     *
     * @return
     */
    public RequestURL getRequestURL() {
        RequestURL addr = super.getRequestURL();
        if ( getAction()!=null )
            addr.addParameter(getAction().toString());

        return addr;
    }

    public void processRequest(String name, String[] values) {
        SForm.addArmedComponent(this);
    }

    public void fireIntermediateEvents() {}

    public void fireFinalEvents() {
        fireActionPerformed();
    }

    public SComponent addComponent(SComponent c, Object constraint, int index){
        if (c instanceof SForm)
            logger.warning("WARNING: attempt to nest forms; won't work.");
        return super.addComponent(c, constraint, index);
    }

    public String getCGClassID() {
        return cgClassID;
    }

    public void setCG(FormCG cg) {
        super.setCG(cg);
    }
}

/*
 * Local variables:
 * c-basic-offset: 4
 * indent-tabs-mode: nil
 * compile-command: "ant -emacs -find build.xml"
 * End:
 */
