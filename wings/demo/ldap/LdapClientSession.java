/*
 * $Id$
 * (c) Copyright 2000 wingS development team.
 *
 * This file is part of the wingS demo (http://wings.mercatis.de).
 *
 * The wingS demo is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * Please see COPYING for the complete licence.
 */

package ldap;

import java.awt.Color;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.naming.*;
import javax.naming.directory.*;


import javax.servlet.*;
import javax.servlet.http.*;

import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeSelectionEvent;


import org.wings.*;
import org.wings.io.Device;
import org.wings.io.ServletDevice;
import org.wings.servlet.*;
import org.wings.session.*;
import org.wings.util.*;


public class LdapClientSession 
    extends SessionServlet
    implements SConstants, TreeSelectionListener, ActionListener
{
    private final static String NOT_CONNECTED = "not connected";

    private LdapWorker worker = null;
    private TreeModel treeModel; 
    private String dn;
    private LdapTreeNode node;

    private STabbedPane tabbedPane;
    private SForm settingsForm;
    private SPanel mainPanel;
    private STree tree;

    private SDesktopPane cards;
    private SPanel editorPanel;
    private SForm attrPanel;
    private SButton commitButton;
    private AddObjectsPanel addPanel;


    private Hashtable attrHTable;
    private Hashtable attrHLTable;
    private SPanel oPanel = new SPanel();

    private Hashtable textHashtable = new Hashtable();
    private Hashtable componentTable = new Hashtable();


    public LdapClientSession(Session session) {
        super(session);
        System.out.println("I`m starting now");  
    }

    public void postInit(ServletConfig config) {
        initGUI();
    }

    void initGUI() {
	tabbedPane = new STabbedPane();
	getFrame().getContentPane().setLayout(new SFlowLayout());
	getFrame().getContentPane().add(tabbedPane);

	settingsForm = new SForm(new SGridLayout(2));
	tabbedPane.add(settingsForm, "Connection Settings");

	SLabel descServer = new SLabel("sever:port");
	final STextField server = new STextField("");
	server.setColumns(30);
	server.setText(((PropertyService)getSession()).getProperty("ldap.server.host"));
	settingsForm.add(descServer);
	settingsForm.add(server);

	SLabel descBaseDN = new SLabel("base DN");
	final STextField baseDN = new STextField();
	baseDN.setColumns(30);
	baseDN.setText(((PropertyService)getSession()).getProperty("ldap.server.basedn"));
	settingsForm.add(descBaseDN);
	settingsForm.add(baseDN);

	SLabel descBindDN = new SLabel("bind DN");
	final STextField bindDN= new STextField("cn=admin,dC=tiscon,dc=de");
	bindDN.setColumns(30);
	settingsForm.add(descBindDN);
	settingsForm.add(bindDN);

	SLabel descBindDNPassword = new SLabel("password");
	final SPasswordField bindDNPassword= new SPasswordField();
	bindDNPassword.setColumns(30);
	settingsForm.add(descBindDNPassword);
	settingsForm.add(bindDNPassword);

	final SButton connectButton = new SButton("connect");
	final SButton disconnectButton = new SButton("disconnect");
	disconnectButton.setVisible(false);
	settingsForm.add(connectButton);
	settingsForm.add(disconnectButton);

	mainPanel = new SPanel(new SGridLayout(2));
	tabbedPane.add(mainPanel, "Browser");

	createTreeModel(null);
	createTree();
	mainPanel.add(tree);

	//editorPanel = new SPanel();
	//mainPanel.add(editorPanel);

	attrPanel = new SForm();
	attrPanel.setLayout(new SGridLayout(2));
	//editorPanel.add(attrPanel);


	mainPanel.add(attrPanel);

	commitButton = new SButton("Commit");
	commitButton.addActionListener(this);
	//attrPanel.add(commitButton);

	addPanel = new AddObjectsPanel();
	tabbedPane.add(addPanel, "Add new Object");

	connectButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    worker = new LdapWorker(server.getText(),
					    baseDN.getText(),
					    bindDN.getText(),
					    bindDNPassword.getText());

		    boolean success = worker.getSuccess();
		    if (success) {
			connectButton.setVisible(false);
			disconnectButton.setVisible(true);
			tabbedPane.setSelectedIndex(1);
		    }
		    else {
			connectButton.setVisible(true);
			disconnectButton.setVisible(false);
		    }

		    if (success) {
			setLdapWorker(worker);
			createTreeModel(worker);
			tree.setModel(treeModel);
			addPanel.setWorker(worker);
			addPanel.setTree(tree);
		    }
		}
	    });

	disconnectButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    createTreeModel(null);
		    tree.setModel(treeModel);
		    attrPanel.removeAll();

		    bindDNPassword.setText("");

		    disconnectButton.setVisible(false);
		    connectButton.setVisible(true);
		}
	    });
    }
    


    private LdapWorker getLdapWorker() {
	return worker;
    }

    private void setLdapWorker(LdapWorker c) {
	this.worker = c;
    }

    private void createTreeModel(final LdapWorker c) {
	TreeNode root = null;
	if (c != null)
	    root = new LdapTreeNode(c, null, c.getBaseDN());
	else
	    root = new DefaultMutableTreeNode(NOT_CONNECTED);

	treeModel = new DefaultTreeModel(root);
    }

    private void createTree() {
	tree = new STree(treeModel);	
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.addTreeSelectionListener(this);
    }

    public void actionPerformed(ActionEvent evt) {
	String newValue = null;
	String oldValue = null;
	String oValue;

	System.out.println(">> commit <<");
	
	LdapWorker worker = getLdapWorker();
	BasicAttributes attributes = new BasicAttributes();
	Enumeration enumer = componentTable.keys();
	while (enumer != null && enumer.hasMoreElements()) {
	    SLabel key = (SLabel)enumer.nextElement();
	    System.out.println(key.getText());

	    oldValue = (String)(textHashtable.get(key));
	    System.out.println("fuer " + key + " old value " +oldValue);
	    
	    newValue = ((STextField)componentTable.get(key)).getText();
	    
	    System.out.println("new value " +newValue);
	    System.out.println("old value " +oldValue);
	    
	    if (!oldValue.equals(newValue)) {
		BasicAttribute attr = new BasicAttribute((String)key.getText());
		StringTokenizer st = new StringTokenizer(newValue,",");
		String atV;
		boolean b = (st !=null && st.hasMoreTokens());
		if (b) 
		    while (st !=null && st.hasMoreTokens()) {
			attr.add(st.nextToken());
		}
		else attr.add(newValue);
		attributes.put(attr);
	    }
	}
	if (attributes.size() > 0) 
	    worker.modifyAttributes(dn,attributes);
    }
    
    
    private void setDN(String dn) {
	this.dn = dn;
    }

    public String getDN() {
	return dn;
    }

    public LdapTreeNode getNode() {
	return node;
    }

    private void setNode(LdapTreeNode node) {
	this.node =node;
    }


    public void valueChanged(TreeSelectionEvent e) {
	TreeNode node = (TreeNode)tree.getLastSelectedPathComponent();
	if (node == null || !(node instanceof LdapTreeNode)) {
	    System.out.println("nothing selected");
	    return;
	}
	setDN(((LdapTreeNode)node).getDN());
	setNode((LdapTreeNode)node);

	componentTable.clear();
	textHashtable.clear();
	attrHTable = new Hashtable();
	attrHLTable = new Hashtable();

	attrPanel.removeAll();

	LdapWorker worker = getLdapWorker();
	BasicAttributes attributes = (BasicAttributes)worker.getDNAttributes(getDN());

	try {
	    NamingEnumeration en = attributes.getAll();
	    while (en!=null && en.hasMoreElements()) {
		BasicAttribute attr = (BasicAttribute)en.nextElement();
		String label = attr.getID();
		String values = "";
	    NamingEnumeration aValues = attr.getAll();
	    while (aValues!=null && aValues.hasMore()) {
		Object i = aValues.next();
		System.out.println("label " + label + "class" + i.getClass().getName());
		if (i.getClass().getName() == "java.lang.String") {
		    if(!values.equals("")) {
			values = values + "," + i;
		    }
		    else {
			values = (String)i;
		    }
		    SLabel attrLabel = new SLabel(label);
		    STextField attrField = new STextField((String)values);
		    componentTable.put(attrLabel,attrField);
		    textHashtable.put(attrLabel,values);
		}
		if (i.getClass().getName() == "[B") {
		    byte hallo [] = (byte [])i;
		    System.out.println(hallo[3]);
		    if (label.equals("jpegPhoto")) { 
			SLabel attrLabel = new SLabel(label);
			SLabel photo = new SLabel(new ImageIcon((byte [])i));
			componentTable.put(attrLabel,photo);
		    }
		    if (label.equals("userPassword")) { 
			SLabel attrLabel = new SLabel(label);
			STextField attrField = new STextField(i.toString());
			componentTable.put(attrLabel,attrField);
			textHashtable.put(attrLabel,i.toString());
		    }
		    System.out.println("binary");
		}
	    }
	    //SLabel attrLabel = new SLabel(label);
	    //STextField attrField = new STextField((String)values);
	    //componentTable.put(attrLabel,attrField);
	    //textHashtable.put(attrLabel,values);
	    //System.out.println("cT*" + label +"*");
	    //System.out.println(values);

	    
	    
	    }
	    Vector objectAttributes = worker.getObjectAttributes(getDN());
	    Enumeration enum = componentTable.keys();
	    while (enum != null && enum.hasMoreElements()) {
		String keyString = (String)((SLabel)enum.nextElement()).getText();
		if (objectAttributes.contains((String)keyString)) 
		    objectAttributes.remove((String)keyString);
	    }
	
	    Enumeration er  = objectAttributes.elements();
	    while (er!=null && er.hasMoreElements()) {
		Object ob = er.nextElement();
		SLabel sLabel = new SLabel((String)ob);
		STextField tf = new STextField("");
		componentTable.put(sLabel,tf);
		textHashtable.put(sLabel,tf.getText());
	    }
	    
	}
	catch (NamingException ex) {
	    System.out.println(ex);
	}

	Enumeration compEnum;
	compEnum = componentTable.keys();
	while (compEnum!=null && compEnum.hasMoreElements()) {
	    Object key = compEnum.nextElement();
		attrPanel.add((SLabel)key);
		attrPanel.add((SComponent)componentTable.get(key));
	}
	attrPanel.add(commitButton);
    }


    /**
     * Servletinfo
     */
    public String getServletInfo() {
        return "LdapClient $Revision$";
    }
}