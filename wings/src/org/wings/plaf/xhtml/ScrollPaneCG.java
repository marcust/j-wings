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

package org.wings.plaf.xhtml;

import java.awt.*;
import java.io.IOException;

import org.wings.*;
import org.wings.io.*;
import org.wings.plaf.*;

public class ScrollPaneCG
    extends org.wings.plaf.AbstractCG
    implements org.wings.plaf.ScrollPaneCG
{
    public void installCG(SComponent component) {
        SScrollPane scrollPane = (SScrollPane)component;


        SScrollBar horizontalScroller = new SScrollBar(SConstants.HORIZONTAL);
        scrollPane.addComponent( horizontalScroller );

        SScrollBar verticalScroller = new SScrollBar(SConstants.VERTICAL);
        scrollPane.addComponent( verticalScroller );

        horizontalScroller.addAdjustmentListener(scrollPane.getAdjustmentListener());
        verticalScroller.addAdjustmentListener(scrollPane.getAdjustmentListener());
    }

    public void uninstallCG(SComponent component) {
        SScrollPane scrollPane = (SScrollPane)component;
        SScrollBar scrollbar;
        scrollbar = (SScrollBar)scrollPane.removeComponentAt(2);
        scrollbar.removeAdjustmentListener(scrollPane.getAdjustmentListener());
        scrollbar = (SScrollBar)scrollPane.removeComponentAt(1);
        scrollbar.removeAdjustmentListener(scrollPane.getAdjustmentListener());
    }

    public void write(Device d, SComponent c)
        throws IOException
    {
        SScrollPane scrollPane = (SScrollPane)c;
        updateScrollBars(scrollPane);
		
        writePrefix(d, scrollPane);
        ((SComponent) scrollPane.getScrollable()).write( d );
        writePostfix(d, scrollPane);
    }

    protected void updateScrollBars(SScrollPane scrollPane) {
        SScrollBar horizontalScroller = (SScrollBar)scrollPane.getComponentAt(1);
        SScrollBar verticalScroller = (SScrollBar)scrollPane.getComponentAt(2);
        Scrollable scrollable = scrollPane.getScrollable();
        
        horizontalScroller.setBlockIncrement( scrollPane.getHorizontalExtent() - 1 );
        verticalScroller.setBlockIncrement( scrollPane.getVerticalExtent() - 1 );

        Dimension dim = scrollable.getScrollableViewportSize();
        if (dim.width != horizontalScroller.getMaximum()) {
            int maximum = dim.width;
            int extent = scrollPane.getHorizontalExtent();
            if (maximum < extent)
                extent = maximum;
            int value = horizontalScroller.getValue();
            int maxValue = maximum - extent + 1;
            if (value > maxValue)
                value = maxValue;

            horizontalScroller.setValues(value, extent, 0, maximum);
            horizontalScroller.setVisible(maximum > extent);
        }
        if (dim.height != verticalScroller.getMaximum()) {
            int maximum = dim.height;
            int extent = scrollPane.getVerticalExtent();
            if (maximum < extent)
                extent = maximum;
            int value = verticalScroller.getValue();
            int maxValue = maximum - extent + 1;
            if (value > maxValue)
                value = maxValue;

            verticalScroller.setValues(value, extent, 0, maximum);
            verticalScroller.setVisible(maximum > extent);
        }
    }

    protected void writePrefix(Device d, SContainer container)
        throws IOException
    {
        SScrollBar horizontalScroller = (SScrollBar)container.getComponentAt(1);
        SScrollBar verticalScroller = (SScrollBar)container.getComponentAt(2);
        SBorder border = container.getBorder();

    	int cspan = ( horizontalScroller.isVisible() )? ((ScrollBarCG) horizontalScroller.getCG()).SCROLLBAR_STEPS + 2 : 1 ;
    	int rspan = ( verticalScroller.isVisible() )? ((ScrollBarCG) verticalScroller.getCG()).SCROLLBAR_STEPS + 2 : 1 ;
        
		d.append("<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\"");
        if ( border != null )
         {
			d.append("style=\"");
            border.writeSpanAttributes( d );
            d.append("\"");
         }
        d.append("><tr><td ");
        if ( cspan > 1 )
         {
        	d.append( "colspan=\"" );
        	d.append( cspan );
            d.append( "\"" );
		 }
		if ( rspan > 1 )
         {
        	d.append( " rowspan=\"" );
        	d.append( rspan );
            d.append( "\"" );
		 }
		d.append(">");
    }

    protected void writePostfix(Device d, SContainer container)
        throws IOException
    {
        SScrollBar horizontalScroller = (SScrollBar)container.getComponentAt(1);
        SScrollBar verticalScroller = (SScrollBar)container.getComponentAt(2);

		d.append( "</td>" );
        verticalScroller.write( d );
        d.append( "</tr>" );
		horizontalScroller.write( d );
        d.append("</tr></table>");
        
    }
}

/*
 * Local variables:
 * c-basic-offset: 4
 * indent-tabs-mode: nil
 * End:
 */
