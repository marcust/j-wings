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

import java.awt.Insets;
import java.io.IOException;

import org.wings.io.Device;

/**
 * This is the interface for Borders.
 *
 * @author <a href="mailto:haaf@mercatis.de">Armin Haaf</a>
 * @version $Revision$
 */
public interface SBorder {

    /**
     * Sets the insets of this border. Insets describe the amount
     * of space 'around' the bordered component.
     *
     * @param insets
     * @see #getInsets()
     */
    void setInsets(Insets insets);

    /**
     * Returns the insets of this border.
     *
     * @return Insets specification of the border.
     * @see #setInsets()
     */
    Insets getInsets();

    /**
     * Get the color of the border.
     *
     * @return color
     */
    java.awt.Color getColor();

    /**
     * Get the color of this border.
     *
     * @param color the color
     */
    void setColor(java.awt.Color color);

    /**
     * writes the border prefix
     *
     * @exception IOException
     * @param d output device
     */
    void writePrefix(Device d) throws IOException;

    /**
     * writes the border postfix to the given device
     *
     * @exception IOException
     * @param d output device
     */
    void writePostfix(Device d) throws IOException;

    /**
     * writes the border attributes for span element.
     *
     * @exception IOException
     * @param d output device
     */
    void writeSpanAttributes(Device d) throws IOException;

    /**
     * Returns the name of the CGFactory class that generates the
     * look and feel for this border.
     *
     * @return content of private static final cgClassID attribute
     * @see SBorder#getCGClassID
     * @see org.wings.plaf.CGDefaults#getCG
     */
    String getCGClassID();

    /**
     * Notification from the CGFactory that the L&F has changed.
     *
     * @see SComponent#updateCG
     */
    void updateCG();

    /**
     * Get the thickness in pixel for this border.
     *
     * @return thickness
     * @see #setThickness(int)
     */
    public int getThickness();

    /**
     * Set the thickness in pixel for this border.
     *
     * @param thickness
     * @see #getThickness()
     */
    public void setThickness(int thickness);
}

/*
 * Local variables:
 * c-basic-offset: 4
 * indent-tabs-mode: nil
 * End:
 */
