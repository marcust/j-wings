package ldap;

import java.util.*;
import java.io.*;
import javax.naming.*;
import javax.naming.directory.*;
import java.awt.Color; 

import java.util.logging.*;

import org.wings.*;
import org.wings.event.*;
import org.wings.session.*;

import ldap.editors.*;

public class AttributesEditor
    extends SPanel
{
  private final static Logger logger = Logger.getLogger("ldap");
  static Properties order;

  private String [] orderString = {"cn","sn","uid","uidnumber","gidnumber","homedirectory","userpassword","jpegphoto","telephonenumber","mail","physicaldeliveryofficename","street","postalcode","l","st","team","seit","taetigkeitsbereiche","werdegang","zusatzausbildungen","descriptor","gecos","loginshell"};
  
  private List rows = new LinkedList();
  private List oClasses = new LinkedList();
  
    private Comparator comparator = new RowComparator();
  
    Map mayAttributeDefinitions;
    Map mustAttributeDefinitions;

    boolean messages = false;

    public AttributesEditor() {
	setLayout(new SGridLayout(3));

	SessionManager.getSession().addRequestListener(new SRequestListener() {
		public void processRequest(SRequestEvent e) {
		    if (SRequestEvent.DELIVER_DONE == e.getType())
			clearMessages();
		}
	    });
    }


    public void clearClassDefinitions() {
	rows.clear();
        oClasses.clear();
    }

    public void addClassDefinition(Attributes classDefinition)
	throws NamingException
    {
	removeAll();
        
        String objectClass = (String)(classDefinition.get("name").get(0));
        int index = objectClass.indexOf(":");
        objectClass = objectClass.substring(index+1);
        oClasses.add(objectClass);
        
               
	mayAttributeDefinitions = LDAP.getAttributeDefinitions(getSchema(), classDefinition, LDAP.MAY);
	Iterator it = mayAttributeDefinitions.entrySet().iterator();
	while (it.hasNext()) {
	    Map.Entry entry = (Map.Entry)it.next();
	    Attributes attributes = (Attributes)entry.getValue();
	    Row row = new Row(attributes, LDAP.MAY);
	    rows.add(row);
	}

	mustAttributeDefinitions = LDAP.getAttributeDefinitions(getSchema(), classDefinition, LDAP.MUST);
	it = mustAttributeDefinitions.entrySet().iterator();
	while (it.hasNext()) {
	    Map.Entry entry = (Map.Entry)it.next();
	    Attributes attributes = (Attributes)entry.getValue();
	    Row row = new Row(attributes, LDAP.MUST);
	    rows.add(row);
	}
        
        if ((oClasses.contains("pgdPerson")) && (oClasses.contains("posixAccount")))
          sortByDefinedOrder(rows);
        else  
          Collections.sort(rows, comparator);
        
        
	it = rows.iterator();
	while (it.hasNext()) {

	    Row row = (Row)it.next();
	    add(row.label);
	    add(row.component);
	    add(row.message);
	}
    }

  private void sortByDefinedOrder(List rows)
  {
    for (int i = 0;i<orderString.length;i++)
      {
        boolean found = false;
        int j = 0;
        while(!found && j<rows.size()) {
          String id = ((Row)rows.get(j)).id.toLowerCase();
          if (id.equals(orderString[i])) {
            Row r1 = (Row)rows.get(i);
            rows.set(i,rows.get(j));
            rows.set(j,r1);
            found = true;
          }
          j++;
        }
      }
  }

    public void setData(Attributes attributes)
	throws NamingException
    {
	Iterator it = rows.iterator();
	while (it.hasNext()) {
	    Row row = (Row)it.next();
	    Attribute attribute = attributes.get(row.id);
	    if (attribute != null)
              row.editor.setValue(row.component, attribute);
	    else
		row.editor.setValue(row.component, null);
	}

	clearMessages();
    }

    public Attributes getData()
	throws NamingException
    {
	Attributes attributes = new BasicAttributes();
	Iterator it = rows.iterator();
	while (it.hasNext()) {
          Row row = (Row)it.next();
          try {
            Attribute attribute = row.editor.getValue(row.component, row.id);
            if (attribute != null)
              attributes.put(attribute);
            
            if (row.maymust == LDAP.MUST && attribute.size() == 0)
              addMessage(row.id, "required");
          }

          catch (NamingException e) {
            addMessage(row.id, e.getMessage());
            //row.message.setText( e.getMessage());
            return null;
          }
	}
	return attributes;
    }

    public void addMessage(String id, String message) {
     	if (id == null)
	    throw new IllegalArgumentException("id must not be null");

	Iterator it = rows.iterator();
	while (it.hasNext()) {
	    Row row = (Row)it.next();
            if (id.equals(row.id)) {
              System.out.println(row.id);
              row.message.setText(message);
		messages = true;
		return;
	    }

	}
    }

    public void clearMessages() {
	if (messages) {
	    Iterator it = rows.iterator();
	    while (it.hasNext()) {
		Row row = (Row)it.next();
		row.message.setText(null);
	    }
	    messages = false;
	}
    }

    static class Row
    {
	public String id;
	public SLabel label;
	public Editor editor;
	public SLabel message;
	public SComponent component;
	public int maymust;

	public Row(Attributes attributes, int maymust)
	    throws NamingException
	{
	    this.id = (String)attributes.get("NAME").get(0);
	    this.label = new SLabel(this.id);
	    this.editor = EditorFactory.getEditor(attributes);
	    this.message = new SLabel("");
	    this.component = editor.createComponent(attributes);
	    this.maymust = maymust;
            
	    if (maymust == LDAP.MUST)
		label.setAttribute("font-weight", "bold");

	    this.message.setAttribute("color", "red");
	}
    }
  
  private DirContext schema = null;
  
  protected DirContext getSchema()
    throws NamingException
    {
      if (schema == null) {
        Session session = getSession();
	    DirContext context = new InitialDirContext(new Hashtable(getSession().getProperties()));
	    schema = context.getSchema("");
        }
      return schema;
    }
  
  class RowComparator
    implements Comparator
  {
    public int compare(Object o1, Object o2) {
      Row r1 = (Row)o1;
	    Row r2 = (Row)o2;

	    if (r1.maymust == LDAP.MUST && r2.maymust == LDAP.MAY)
		return -1;

	    if (r1.maymust == LDAP.MAY && r2.maymust == LDAP.MUST)
		return 1;

	    return (r1 == null) ? -1 : r1.id.compareTo(r2.id);
	}
	public boolean equals(Object obj) {
	    return obj instanceof RowComparator;
	}
    }
}


