/*
 * Copyright (c) 2004 Your Corporation. All Rights Reserved.
 */
package org.wings;

import org.wings.plaf.PopupMenuCG;

import java.util.ArrayList;
import java.util.List;

/**
 * @author hengels
 * @version $Revision$
 */
public class SPopupMenu
    extends SComponent
{
    protected final List menuItems = new ArrayList();

    /**
     * Add a menu item to this menu.
     */
    public void add(SMenuItem menuItem) {
        menuItems.add(menuItem);
        menuItem.setParentMenu(this);
    }

    /**
     * Add a menu item to this menu.
     */
    public void add(SComponent menuItem) {
        menuItems.add(menuItem);
        menuItem.setParentFrame(getParentFrame());
    }

    public void setParentFrame(SFrame f) {
        super.setParentFrame(f);
        for ( int i=0; i<menuItems.size(); i++ )
            ((SComponent)menuItems.get(i)).setParentFrame(f);
    }

    /**
     * Add a menu item to this menu.
     */
    public void add(String menuitem) {
        this.add(new SMenuItem(menuitem));
    }

    public SComponent getMenuComponent(int pos) {
        return (SComponent)menuItems.get(pos);
    }

    /**
     * Return the number of items on the menu, including separators.
     */
    public int getMenuComponentCount() {
        return menuItems.size();
    }

    /**
     * Remove all {@link SMenuItem} from this menu.
     */
    public void removeAll() {
        while ( menuItems.size()>0 ) {
            remove(0);
        }
    }

    /**
     * Removes the menu item at specified index from the menu.
     */
    public void remove(int pos) {
        remove(getMenuComponent(pos));
    }

    /**
     * removes a specific menu item component.
     */
    public void remove(SComponent comp) {
        menuItems.remove(comp);
        comp.setParentFrame(null);
    }

    public void setCG(PopupMenuCG cg) {
        super.setCG(cg);
    }
}