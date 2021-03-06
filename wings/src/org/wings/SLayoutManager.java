/*
 * $Id$
 * (c) Copyright 2000 wingS development team.
 *
 * This file is part of wingS (http://j-wings.org).
 *
 * wingS is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * Please see COPYING for the complete licence.
 */

package org.wings;

import java.io.*;

import org.wings.plaf.*;
import org.wings.io.Device;

/**
 * The interface for the layout managers.
 *
 * @author <a href="mailto:haaf@mercatis.de">Armin Haaf</a>
 * @version $Revision$
 */
public interface SLayoutManager extends Serializable, Renderable
{
    /**
     * Adds a component to the layout manager
     * @param c The new component
     * @param constraint A (sometimes optional) constraint object
     */
    void addComponent(SComponent c, Object constraint, int index);

    /**
     * Removes a component from the layout manager
     * @param c The new component
     */
    void removeComponent(SComponent c);

    /**
     * Sets the corresponding container
     * @param c The container
     */
    void setContainer(SContainer c);

    /**
     * Returns the corresponding container
     * @return The container
     */
    SContainer getContainer();

    /**
     * Writes the layouted container to the given device.
     * @param s The output device
     */
    void write(Device s) throws IOException;

    /**
     * Returns the name of the CGFactory class that generates the
     * look and feel for this component.
     *
     * @return content of private static final cgClassID attribute
     * @see SLayoutManager#getCGClassID
     * @see org.wings.plaf.CGDefaults#getCG
     */
    String getCGClassID();

    /**
     * Notification from the CGFactory that the L&F has changed.
     *
     * @see SLayoutManager#updateCG
     */
    void updateCG();
}

/*
 * Local variables:
 * c-basic-offset: 4
 * indent-tabs-mode: nil
 * compile-command: "ant -emacs -find build.xml"
 * End:
 */
