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

import org.wings.plaf.*;
import org.wings.io.Device;

/**
 * TODO: documentation
 *
 * @author Dominik Bartenstein
 * @version $Revision$
 */
public class STextField
    extends STextComponent
{
    private static final String cgClassID = "TextFieldCG";

    /** maximum columns shown */
    protected int columns = 12;

    /** maximum columns allowed */
    protected int maxColumns = 50;


    /**
     * TODO: documentation
     *
     */
    public STextField() {
    }

    /**
     * TODO: documentation
     *
     * @param text
     */
    public STextField(String text) {
        super(text);
    }


    /**
     * TODO: documentation
     *
     * @param c
     */
    public void setColumns(int c) {
        int oldColumns = columns;
        columns = c;
        if (columns != oldColumns)
            reload(RELOAD_STATE);
    }

    /**
     * TODO: documentation
     *
     * @return
     */
    public int getColumns() {
        return columns;
    }


    /**
     * TODO: documentation
     *
     * @param mc
     */
    public void setMaxColumns(int mc) {
        int oldMaxColumns = maxColumns;
        maxColumns = mc;
        if (maxColumns != oldMaxColumns)
            reload(RELOAD_STATE);
    }

    /**
     * TODO: documentation
     *
     * @return
     */
    public int getMaxColumns() {
        return maxColumns;
    }

    public String getCGClassID() {
        return cgClassID;
    }

    public void setCG(TextFieldCG cg) {
        super.setCG(cg);
    }
}

/*
 * Local variables:
 * c-basic-offset: 4
 * indent-tabs-mode: nil
 * End:
 */
