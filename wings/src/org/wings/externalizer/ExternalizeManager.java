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

package org.wings.externalizer;

import java.util.*;
import java.util.logging.*;

import javax.servlet.http.HttpServletResponse;

/**
 * TODO: documentation
 *
 * @author <a href="mailto:haaf@mercatis.de">Armin Haaf</a>
 * @version $Revision$
 */
public class ExternalizeManager extends AbstractExternalizeManager
{
    /**
     *
     */
    private static final Externalizer[] DEFAULT_EXTERNALIZERS = {
        new ImageExternalizer(ImageExternalizer.FORMAT_PNG),
        new ImageExternalizer(ImageExternalizer.FORMAT_GIF),
        new ImageIconExternalizer(ImageExternalizer.FORMAT_PNG),
        new ImageIconExternalizer(ImageExternalizer.FORMAT_GIF),
        new StaticResourceExternalizer(),
        new DynamicResourceExternalizer(),
    };

    /**
     * TODO: documentation
     */
    protected final HashMap externalizerByClass = new HashMap();
    
    /**
     * TODO: documentation
     */
    protected final HashMap externalizerByMimeType = new HashMap();
    
    /**
     * TODO: documentation
     */
    protected final Map externalized = Collections.synchronizedMap( new HashMap() );
    
    
    /**
     * TODO: documentation
     *
     */
    public ExternalizeManager(HttpServletResponse response) {
        this(response, true);
    }
    
    /**
     * TODO: documentation
     *
     */
    public ExternalizeManager(HttpServletResponse response,
                              boolean initWithDefaultExternalizers) {
        super(response);
        if ( initWithDefaultExternalizers )
            addDefaultExternalizers();
    }

    /**
     *
     */
    public final void addDefaultExternalizers() {
        for ( int i=0; i<DEFAULT_EXTERNALIZERS.length; i++ ) {
            addExternalizer(DEFAULT_EXTERNALIZERS[i]);
        }
    }

    protected final void storeExternalizedInfo(String identifier, ExternalizedInfo extInfo) {
        if (_wingsLogger.isLoggable(Level.FINER)) {
            _wingsLogger.finer("store identifier " + identifier + " " + extInfo.getObject().getClass());
            _wingsLogger.finer("flags " + extInfo.getFlags());
        }
        externalized.put(identifier, extInfo);
    }

    public final Object getExternalizedObject(String identifier) {
        ExternalizedInfo info = getExternalizedInfo(identifier);

        if ( info!=null )
            return info.getObject();
        
        return null;
    }
    
    public final ExternalizedInfo getExternalizedInfo(String identifier) {
        if ( identifier == null || identifier.length() < 1 )
            return null;

        int pos = identifier.indexOf(org.wings.SConstants.UID_DIVIDER);
        if (pos > -1)
            identifier = identifier.substring(pos + 1);

        pos = identifier.indexOf(".");
        if (pos > -1)
            identifier = identifier.substring(0, pos);

        // SystemExternalizeManager hat negative Identifier
        if ( identifier.charAt(0) == '-' ) {
            return SystemExternalizeManager.getSharedInstance().
                getExternalizedInfo(identifier);
        }

        return (ExternalizedInfo)externalized.get(identifier);
    }

    protected final void removeExternalizedInfo(String identifier) {
        externalized.remove(identifier);
    }


    /**
     * TODO: documentation
     *
     * @return
     */
    public String externalize(Object obj) {
        return externalize(obj, SESSION);
    }

    /**
     * TODO: documentation
     *
     * @return
     */
    public String externalize(Object obj, Set headers) {
        return externalize(obj, headers, SESSION);
    }

    /**
     * TODO: documentation
     *
     * @return
     */
    public String externalize(Object obj, int flags) {
        return externalize(obj, (Set)null, flags);
    }

    /**
     * TODO: documentation
     *
     * @return
     */
    public String externalize(Object obj, Set headers, int flags) {
        if ( obj == null )
            throw new IllegalArgumentException("object must not be null");

        Externalizer externalizer = getExternalizer(obj.getClass());
        if (externalizer == null) {
            _wingsLogger.warning("could not find externalizer for " +
                           obj.getClass().getName());
            return NOT_FOUND_IDENTIFIER;
        }

        return externalize(obj, externalizer, headers, flags);
    }

    /**
     * TODO: documentation
     *
     * @return
     */
    public String externalize(Object obj, String mimeType)
    {
        return externalize(obj, mimeType, SESSION);
    }

    /**
     * TODO: documentation
     *
     * @return
     */
    public String externalize(Object obj, String mimeType, Set headers) 
    {
        return externalize(obj, mimeType, headers, SESSION);
    }

    /**
     * TODO: documentation
     *
     * @return
     */
    public String externalize(Object obj, String mimeType, int flags)
    {
        return externalize(obj, mimeType, null, flags);
    }

    /**
     * TODO: documentation
     *
     * @return
     */
    public String externalize(Object obj, String mimeType, 
                              Set headers, int flags)
    {
        if ( obj == null )
            throw new IllegalStateException("no externalizer");
        
        if ( mimeType == null )
            return externalize(obj);

        Externalizer externalizer = getExternalizer(mimeType);
        if ( externalizer == null ) {
            _wingsLogger.warning("could not find externalizer for " +
                           obj.getClass().getName());
            return NOT_FOUND_IDENTIFIER;
        }

        return externalize(obj, externalizer, headers, flags);
    }

    /**
     * Adds an externalizer. If an externalizer is already
     * registered for a class or a mime type, it will be replaced.
     */
    public void addExternalizer(Externalizer externalizer) {
        if ( externalizer != null ) {
            Class c[] = externalizer.getSupportedClasses();
            if ( c != null )
                for ( int i=0; i<c.length; i++ ) 
                    if ( c[i]!=null )
                        externalizerByClass.put(c[i], externalizer);

            String mimeTypes[] = externalizer.getSupportedMimeTypes();
            if ( mimeTypes != null )
                for ( int i=0; i<mimeTypes.length; i++ ) 
                    if ( mimeTypes[i]!=null &&
                         mimeTypes[i].trim().length()>0 )
                        externalizerByMimeType.put(mimeTypes[i].trim().toLowerCase(), 
                                              externalizer);
        }
    }
    
    /**
     * Adds an Externalizer
     */
    public void addExternalizer(Externalizer externalizer, String mimeType) {
        if ( externalizer != null && mimeType != null )
	    externalizerByMimeType.put(mimeType, externalizer);
    }

    /**
     * Returns an object externalizer for a class. If one could not be found,
     * it goes down the inheritance tree and looks for an object externalizer
     * for the super classes. If one still could not be found, it goes
     * through the list of interfaces of the class and checks for object
     * externalizers for every interface. If this also doesn't return an
     * object externalizer, null is returned.
     *
     * @return
     */
    public Externalizer getExternalizer(Class c) {
        Externalizer externalizer = null;
        if ( c != null ) {
            externalizer = getSuperclassExternalizer(c);
            if ( externalizer == null )
                externalizer = getInterfaceExternalizer(c);
        }
        return externalizer;
    }

    /**
     *
     */
    private Externalizer getSuperclassExternalizer(Class c) {
        Externalizer externalizer = null;
        if ( c != null ) {
            externalizer = (Externalizer)externalizerByClass.get(c);
            if ( externalizer == null )
                externalizer = getExternalizer(c.getSuperclass());
        }
        return externalizer;
    }

    /**
     *
     */
    private Externalizer getInterfaceExternalizer(Class c) {
        Externalizer externalizer = null;
        Class[] ifList = c.getInterfaces();
        for ( int i = 0; i < ifList.length; i++ ) {
            externalizer = (Externalizer)externalizerByClass.get(ifList[i]);
            if ( externalizer != null )
                break;
        }
        return externalizer;
    }

    /**
     * returns an object externalizer for a mime type
     */
    public Externalizer getExternalizer(String mimeType) {
        Externalizer externalizer = null;
        if (mimeType != null && mimeType.length() > 0) {
            externalizer = (Externalizer)externalizerByMimeType.get(mimeType);
            if (externalizer == null) {
                if ( mimeType.indexOf('/') >=0 )
                    externalizer = 
                        getExternalizer(mimeType.substring(0, mimeType.indexOf('/')));
            }
        }
        return externalizer;
    }
}

/*
 * Local variables:
 * c-basic-offset: 4
 * indent-tabs-mode: nil
 * compile-command: "ant -emacs -find build.xml"
 * End:
 */
