/*
 * $Id$
 * Copyright 2000,2005 wingS development team.
 *
 * This file is part of wingS (http://www.j-wings.org).
 *
 * wingS is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * Please see COPYING for the complete licence.
 */
package org.wings.table;

import org.wings.SComponent;
import org.wings.STable;

import javax.swing.*;

/**
 * @author <a href="mailto:holger.engels@mercatis.de">Holger Engels</a>
 * @version $Revision$
 */
public interface STableCellEditor
        extends CellEditor {
    SComponent getTableCellEditorComponent(STable table,
                                           Object value,
                                           boolean isSelected,
                                           int row,
                                           int column);
}


