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

import java.util.Enumeration;
import java.util.ArrayList;

import javax.swing.tree.*;
import javax.swing.event.*;

import org.wings.plaf.*;
import org.wings.io.Device;

/**
 * TODO: documentation
 *
 * @author <a href="mailto:haaf@mercatis.de">Armin Haaf</a>
 * @version $Revision$
 */
public class STree
    extends SComponent
    implements SGetListener, TreeSelectionListener
{
    private static final String cgClassID = "TreeCG";

    /**
     * TODO: documentation
     */
    protected TreeModel model = null;

    /**
     * TODO: documentation
     */
    transient protected TreeModelListener treeModelListener;

    /**
     * TODO: documentation
     */
    protected STreeCellRenderer renderer;

    /**
     * TODO: documentation
     */
    protected TreeSelectionModel selectionModel;

    private ArrayList selectionListener = null;
    private ArrayList expansionListener = null;

    /**
     * TODO: documentation
     */
    protected int nodeIndentDepth = 12;

    /**
     * TODO: documentation
     */
    protected AbstractLayoutCache treeState = new VariableHeightLayoutCache();

    /**
     * TODO: documentation
     *
     * @param model
     */
    public STree(TreeModel model) {
        super();
        setModel(model);
        setRootVisible(true);
        setSelectionModel(new DefaultTreeSelectionModel());
    }


    /**
     * TODO: documentation
     *
     * @param tsl
     */
    public void addTreeSelectionListener(TreeSelectionListener tsl) {
        if ( selectionListener == null )
            selectionListener = new ArrayList(2);
        selectionListener.add(tsl);
    }

    /**
     * TODO: documentation
     *
     * @param tsl
     */
    public void removeTreeSelectionListener(TreeSelectionListener tsl) {
        if ( selectionListener == null )
            return;
        selectionListener.remove(tsl);
    }


    /**
     * Notify all listeners that have registered interest for
     * notification on this event type.  The event instance
     * is lazily created using the parameters passed into
     * the fire method.
     *
     * @param e the TreeSelectionEvent generated by the TreeSelectionModel
     *          when a node is selected or deselected
     * @see EventListenerList
     */
    public void valueChanged(TreeSelectionEvent e) {
        //    System.out.println("VALUE CHANGED " + e);

        if ( selectionListener == null )
            return;

        if ( selectionListener != null ) {
            for (int i=selectionListener.size()-1; i>=0; i-- ) {
                ((TreeSelectionListener)selectionListener.get(i)).valueChanged(e);
            }
        }
    }


    /**
     * TODO: documentation
     *
     * @param tel
     */
    public void addTreeExpansionListener(TreeExpansionListener tel) {
        if ( expansionListener == null )
            expansionListener = new ArrayList(2);
        expansionListener.add(tel);
    }

    /**
     * TODO: documentation
     *
     * @param tel
     */
    public void removeTreeExpansionListener(TreeExpansionListener tel) {
        if ( expansionListener == null )
            return;
        expansionListener.remove(tel);
    }


    /**
     * Notify all listeners that have registered interest for
     * notification on this event type.  The event instance
     * is lazily created using the parameters passed into
     * the fire method.
     *
     * @param path the TreePath indicating the node that was expanded
     * @see EventListenerList
     */
    public void fireTreeExpanded(TreePath path) {
        // System.out.println("Tree Expanded " + path);

        if ( expansionListener == null )
            return;

        TreeExpansionEvent e = null;
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for ( int i=expansionListener.size()-1; i>=0; i-- ) {
            // Lazily create the event:
            if ( e == null )
                e = new TreeExpansionEvent(this, path);
            ((TreeExpansionListener)expansionListener.get(i)).treeExpanded(e);
        }
    }

    /**
     * Notify all listeners that have registered interest for
     * notification on this event type.  The event instance
     * is lazily created using the parameters passed into
     * the fire method.
     *
     * @param path the TreePath indicating the node that was collapsed
     * @see EventListenerList
     */
    public void fireTreeCollapsed(TreePath path) {
        if ( expansionListener == null )
            return;
        TreeExpansionEvent e = null;
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for ( int i=expansionListener.size()-1; i>=0 ;i-- ) {
            if ( e == null )
                e = new TreeExpansionEvent(this, path);
            ((TreeExpansionListener)expansionListener.get(i)).treeCollapsed(e);
        }
    }


    /**
     * TODO: documentation
     *
     * @param rootVisible
     */
    public void setRootVisible(boolean rootVisible) {
        treeState.setRootVisible(rootVisible);
    }

    /**
     * TODO: documentation
     *
     * @return
     */
    public boolean isRootVisible() {
        return treeState.isRootVisible();
    }


    /**
     * TODO: documentation
     *
     * @param m
     */
    public void setModel(TreeModel m) {
        if ( model != null && treeModelListener != null )
            model.removeTreeModelListener(treeModelListener);
        model = m;
        treeState.setModel(m);

        if ( model != null ) {
            if ( treeModelListener == null )
                treeModelListener = createTreeModelListener();

            if ( treeModelListener != null )
                model.addTreeModelListener(treeModelListener);

            // Mark the root as expanded, if it isn't a leaf.
            if ( !model.isLeaf(model.getRoot()) )
                treeState.setExpandedState(new TreePath(model.getRoot()), true);
        }
    }

    /**
     * TODO: documentation
     *
     * @return
     */
    public TreeModel getModel() {
        return model;
    }

    /**
     * TODO: documentation
     *
     * @return
     */
    public int getRowCount() {
        return treeState.getRowCount();
    }

    /**
     * TODO: documentation
     *
     * @param row
     * @return
     */
    public TreePath getPathForRow(int row) {
        return treeState.getPathForRow(row);
    }

    /**
     * Sets the tree's selection model. When a null value is specified
     * an empty electionModel is used, which does not allow selections.
     *
     * @param selectionModel the TreeSelectionModel to use, or null to
     *        disable selections
     * @see TreeSelectionModel
     * @beaninfo
     *        bound: true
     *  description: The tree's selection model.
     */
    public void setSelectionModel(TreeSelectionModel selectionModel) {
        if ( this.selectionModel != null )
            this.selectionModel.removeTreeSelectionListener(this);

        if ( selectionModel != null )
            selectionModel.addTreeSelectionListener(this);

        if ( selectionModel == null )
            this.selectionModel = EmptySelectionModel.sharedInstance();
        else
            this.selectionModel = selectionModel;
    }

    /**
     * Returns the model for selections. This should always return a
     * non-null value. If you don't want to allow anything to be selected
     * set the selection model to null, which forces an empty
     * selection model to be used.
     *
     * @param the TreeSelectionModel in use
     * @see #setSelectionModel
     */
    public TreeSelectionModel getSelectionModel() {
        return selectionModel;
    }

    /**
     * Returns JTreePath instances representing the path between index0
     * and index1 (including index1).
     *
     * @param index0  an int specifying a display row, where 0 is the
     *                first row in the display
     * @param index0  an int specifying a second display row
     * @return an array of TreePath objects, one for each node between
     *         index0 and index1, inclusive
     */
    protected TreePath[] getPathBetweenRows(int index0, int index1) {
        int newMinIndex = Math.min(index0, index1);
        int newMaxIndex = Math.max(index0, index1);

        TreePath[] selection = new TreePath[newMaxIndex - newMinIndex + 1];

        for ( int i=newMinIndex; i<=newMaxIndex; i++ ) {
            selection[i - newMinIndex] = getPathForRow(i);
        }

        return selection;
    }

    /**
     * Selects the node identified by the specified path.  If any
     * component of the path is hidden (under a collapsed node), it is
     * exposed (made viewable).
     *
     * @param path the TreePath specifying the node to select
     */
    public void setSelectionPath(TreePath path) {
        getSelectionModel().setSelectionPath(path);
    }

    /**
     * Selects the nodes identified by the specified array of paths.
     * If any component in any of the paths is hidden (under a collapsed
     * node), it is exposed (made viewable).
     *
     * @param paths an array of TreePath objects that specifies the nodes
     *        to select
     */
    public void setSelectionPaths(TreePath[] paths) {
        getSelectionModel().setSelectionPaths(paths);
    }

    /**
     * Selects the node at the specified row in the display.
     *
     * @param row  the row to select, where 0 is the first row in
     *             the display
     */
    public void setSelectionRow(int row) {
        int[] rows = { row };
        setSelectionRows(rows);
    }

    /**
     * Selects the nodes corresponding to each of the specified rows
     * in the display.
     *
     * @param rows  an array of ints specifying the rows to select,
     *              where 0 indicates the first row in the display
     */
    public void setSelectionRows(int[] rows) {
        if ( rows == null )
            return;

        TreePath paths[] = new TreePath[rows.length];
        for ( int i=0; i<rows.length; i++ ) {
            paths[i] = getPathForRow(i);
        }

        setSelectionPaths(paths);
    }

    /**
     * Adds the node identified by the specified TreePath to the current
     * selection. If any component of the path isn't visible, it is
     * made visible.
     *
     * @param path the TreePath to add
     */
    public void addSelectionPath(TreePath path) {
        getSelectionModel().addSelectionPath(path);
    }

    /**
     * Adds each path in the array of paths to the current selection.  If
     * any component of any of the paths isn't visible, it is
     * made visible.
     *
     * @param paths an array of TreePath objects that specifies the nodes
     *              to add
     */
    public void addSelectionPaths(TreePath[] paths) {
        getSelectionModel().addSelectionPaths(paths);
    }

    /**
     * Adds the path at the specified row to the current selection.
     *
     * @param row  an int specifying the row of the node to add,
     *             where 0 is the first row in the display
     */
    public void addSelectionRow(int row) {
        int[] rows = { row };
        addSelectionRows(rows);
    }

    /**
     * Adds the paths at each of the specified rows to the current selection.
     *
     * @param rows  an array of ints specifying the rows to add,
     *              where 0 indicates the first row in the display
     */
    public void addSelectionRows(int[] rows) {
        if ( rows != null ) {
            int numRows = rows.length;
            TreePath[] paths = new TreePath[numRows];

            for ( int counter = 0; counter < numRows; counter++ )
                paths[counter] = getPathForRow(rows[counter]);
            addSelectionPaths(paths);
        }
    }

    /**
     * Returns the last path component in the first node of the current
     * selection.
     *
     * @return the last Object in the first selected node's TreePath,
     *         or null if nothing is selected
     * @see TreePath#getLastPathComponent
     */
    public Object getLastSelectedPathComponent() {
        Object obj = null;
        TreePath selPath = getSelectionModel().getSelectionPath();
        if ( selPath != null ) {
            obj = selPath.getLastPathComponent();
        }
        return obj;
    }

    /**
     * Returns the path to the first selected node.
     *
     * @return the TreePath for the first selected node, or null if
     *         nothing is currently selected
     */
    public TreePath getSelectionPath() {
        return getSelectionModel().getSelectionPath();
    }

    /**
     * Returns the paths of all selected values.
     *
     * @return an array of TreePath objects indicating the selected
     *         nodes, or null if nothing is currently selected.
     */
    public TreePath[] getSelectionPaths() {
        return getSelectionModel().getSelectionPaths();
    }

    /**
     * Returns all of the currently selected rows.
     *
     * @return an array of ints that identifies all currently selected rows
     *         where 0 is the first row in the display
     */
    public int[] getSelectionRows() {
        return getSelectionModel().getSelectionRows();
    }

    /**
     * Returns the number of nodes selected.
     *
     * @return the number of nodes selected
     */
    public int getSelectionCount() {
        return selectionModel.getSelectionCount();
    }

    /**
     * Gets the first selected row.
     *
     * @return an int designating the first selected row, where 0 is the
     *         first row in the display
     */
    public int getMinSelectionRow() {
        return getSelectionModel().getMinSelectionRow();
    }

    /**
     * Gets the last selected row.
     *
     * @return an int designating the last selected row, where 0 is the
     *         first row in the display
     */
    public int getMaxSelectionRow() {
        return getSelectionModel().getMaxSelectionRow();
    }

    /**
     * Returns the row index of the last node added to the selection.
     *
     * @return an int giving the row index of the last node added to the
     *         selection, where 0 is the first row in the display
     */
    public int getLeadSelectionRow() {
        return getSelectionModel().getLeadSelectionRow();
    }

    /**
     * Returns the path of the last node added to the selection.
     *
     * @return the TreePath of the last node added to the selection.
     */
    public TreePath getLeadSelectionPath() {
        return getSelectionModel().getLeadSelectionPath();
    }

    /**
     * Returns true if the item identified by the path is currently selected.
     *
     * @param path a TreePath identifying a node
     * @return true if the node is selected
     */
    public boolean isPathSelected(TreePath path) {
        return getSelectionModel().isPathSelected(path);
    }

    /**
     * Returns true if the node identitifed by row is selected.
     *
     * @param row  an int specifying a display row, where 0 is the first
     *             row in the display
     * @return true if the node is selected
     */
    public boolean isRowSelected(int row) {
        return getSelectionModel().isRowSelected(row);
    }

    /**
     * Removes the nodes between index0 and index1, inclusive, from the
     * selection.
     *
     * @param index0  an int specifying a display row, where 0 is the
     *                first row in the display
     * @param index0  an int specifying a second display row
     */
    public void removeSelectionInterval(int index0, int index1) {
        TreePath[] paths = getPathBetweenRows(index0, index1);
        this.getSelectionModel().removeSelectionPaths(paths);
    }

    /**
     * Removes the node identified by the specified path from the current
     * selection.
     *
     * @param path  the TreePath identifying a node
     */
    public void removeSelectionPath(TreePath path) {
        getSelectionModel().removeSelectionPath(path);
    }

    /**
     * Removes the nodes identified by the specified paths from the
     * current selection.
     *
     * @param paths an array of TreePath objects that specifies the nodes
     *              to remove
     */
    public void removeSelectionPaths(TreePath[] paths) {
        getSelectionModel().removeSelectionPaths(paths);
    }

    /**
     * Removes the path at the index <code>row</code> from the current
     * selection.
     *
     * @param path  the TreePath identifying the node to remove
     */
    public void removeSelectionRow(int row) {
        int[] rows = { row };
        removeSelectionRows(rows);
    }

    public void removeSelectionRows(int[] rows) {
        TreePath[] paths = new TreePath[rows.length];
        for ( int i = 0; i<rows.length; i++ )
            paths[i] = getPathForRow(rows[i]);
        removeSelectionPaths(paths);
    }


    /**
     * TODO: documentation
     *
     * @return
     */
    public int getMaximumExpandedDepth() {
        int max = 0;
        for ( int i=0; i<getRowCount(); i++ )
            max = Math.max(max, getPathForRow(i).getPathCount());
        return max;
    }

    /**
     * TODO: documentation
     *
     * @param node
     * @return
     */
    protected TreePath getTreePath(TreeNode node) {
        ArrayList v = new ArrayList(2);

        TreeNode n = node;
        while ( n!=null ) {
            v.add(n);
            n = n.getParent();
        }

        Object path[] = new Object[v.size()];
        v.toArray(path);

        return new TreePath(path);
    }

    /**
     * TODO: documentation
     *
     * @param hash
     * @return
     */
    protected int getRow(String hash) {
        try {
            int hashCode = Integer.parseInt(hash);

            for ( int i=0; i<getRowCount(); i++ ) {
                TreePath p = getPathForRow(i);
                // System.out.println("path " + p);
                Object node = p.getLastPathComponent();
                if ( node.hashCode() == hashCode )
                    return i;
            }
        }
        catch (Exception e) {
        }

        return -1;
    }

    /**
     * TODO: documentation
     *
     * @param p
     */
    public void expandRow(TreePath p) {
        treeState.setExpandedState(p, true);
        fireTreeExpanded(p);
    }

    /**
     * TODO: documentation
     *
     * @param row
     */
    public void expandRow(int row) {
        expandRow(getPathForRow(row));
    }

    /**
     * TODO: documentation
     *
     * @param p
     */
    public void collapseRow(TreePath p) {
        treeState.setExpandedState(p, false);
        fireTreeCollapsed(p);
    }

    /**
     * TODO: documentation
     *
     * @param row
     */
    public void collapseRow(int row) {
        collapseRow(getPathForRow(row));
    }

    /**
     * TODO: documentation
     *
     * @param path
     * @return
     */
    public boolean isVisible(TreePath path) {
        if ( path != null ) {
            TreePath parentPath = path.getParentPath();

            if ( parentPath != null )
                return isExpanded(parentPath);

            // Root.
            return true;
        }

        return false;
    }

    /**
     * TODO: documentation
     *
     * @param path
     * @return
     */
    public boolean isExpanded(TreePath path) {
        return treeState.isExpanded(path);
    }

    /**
     * TODO: documentation
     *
     * @param path
     */
    protected void togglePathSelection(TreePath path) {
        if ( path != null ) {
            if ( isPathSelected(path) ) {
                removeSelectionPath(path);
            }
            else {
                addSelectionPath(path);
            }
        }
    }

    /**
     * TODO: documentation
     *
     * @param path
     */
    protected void togglePathExpansion(TreePath path) {
        if ( path != null ) {
            if ( treeState.isExpanded(path) ) {
                collapseRow(path);
            }
            else {
                expandRow(path);
            }
        }
    }


    public void getPerformed(String action, String value) {
        boolean handle = value.charAt(0) == 'h';
        value = value.substring(1);
        TreePath path = getPathForRow(getRow(value));
        if ( path != null )
            if ( ((TreeNode)path.getLastPathComponent()).isLeaf() || !handle ) {
                togglePathSelection(path);
            }
            else {
                togglePathExpansion(path);
            }
    }

    /**
     * TODO: documentation
     *
     * @param depth
     */
    public void setNodeIndentDepth(int depth) {
        nodeIndentDepth = depth;
    }

    /**
     * TODO: documentation
     *
     * @return
     */
    public int getNodeIndentDepth() {
        return nodeIndentDepth;
    }

    /**
     * TODO: documentation
     *
     * @param x
     */
    public void setCellRenderer(STreeCellRenderer x) {
        renderer = x;
    }

    /**
     * TODO: documentation
     *
     * @return
     */
    public STreeCellRenderer getCellRenderer() {
        return renderer;
    }

    /**
     * Creates an instance of TreeModelHandler.
     */
    protected TreeModelListener createTreeModelListener() {
        return new TreeModelHandler();
    }


    /**
     * Listens to the model and updates the expandedState accordingly
     * when nodes are removed, or changed.
     */
    protected class TreeModelHandler implements TreeModelListener
    {
        /**
         * TODO: documentation
         *
         * @param e
         */
        public void treeNodesChanged(TreeModelEvent e) {
            if ( e == null )
                return;
            treeState.treeNodesChanged(e);
        }

        /**
         * TODO: documentation
         *
         * @param e
         */
        public void treeNodesInserted(TreeModelEvent e) {
            if ( e == null )
                return;
            treeState.treeNodesInserted(e);
        }

        /**
         * TODO: documentation
         *
         * @param e
         */
        public void treeStructureChanged(TreeModelEvent e) {
            if ( e == null )
                return;
            treeState.treeStructureChanged(e);
        }

        /**
         * TODO: documentation
         *
         * @param e
         */
        public void treeNodesRemoved(TreeModelEvent e) {
            if ( e == null )
                return;
            treeState.treeNodesRemoved(e);
        }
    }

    /**
     * EmptySelectionModel is a TreeSelectionModel that does not allow
     * anything to be selected.
     */
    protected static class EmptySelectionModel extends DefaultTreeSelectionModel
    {
        /**
         * Unique shared instance.
         */
        protected static final EmptySelectionModel sharedInstance =
            new EmptySelectionModel();

        /**
         * Returns a shared instance of an empty selection model
         *
         * @return
         */
        static public EmptySelectionModel sharedInstance() {
            return sharedInstance;
        }

        /** A null implementation that selects nothing */
        public void setSelectionPaths(TreePath[] pPaths) {
        }

        /** A null implementation that adds nothing */
        public void addSelectionPaths(TreePath[] paths) {
        }

        /** A null implementation that removes nothing */
        public void removeSelectionPaths(TreePath[] paths) {
        }
    }

    /**
     * TODO: documentation
     */
    protected class NodeState {
        boolean expanded;
    }


    public void setParent(SContainer p) {
        super.setParent(p);
        if ( getCellRendererPane() != null )
            getCellRendererPane().setParent(p);
    }

    protected void setParentFrame(SFrame f) {
        super.setParentFrame(f);
        if ( getCellRendererPane() != null )
            getCellRendererPane().setParentFrame(f);
    }


    // do not initalize with null!
    private SCellRendererPane cellRendererPane;

    /**
     * TODO: documentation
     *
     * @param c
     */
    public void setCellRendererPane(SCellRendererPane c) {
        cellRendererPane=c;
        cellRendererPane.setParent(getParent());
    }

    /**
     * TODO: documentation
     *
     * @return
     */
    public SCellRendererPane getCellRendererPane() {
        return cellRendererPane;
    }

    /**
     * TODO: documentation
     *
     * @return
     */
    public void removeCellRendererPane() {
        cellRendererPane.setParent(null);
        cellRendererPane = null;
    }


    /**
     * Returns the name of the CGFactory class that generates the
     * look and feel for this component.
     *
     * @return "TreeCG"
     * @see SComponent#getCGClassID
     * @see CGDefaults#getCG
     */
    public String getCGClassID() {
        return cgClassID;
    }
}

/*
 * Local variables:
 * c-basic-offset: 4
 * indent-tabs-mode: nil
 * End:
 */
