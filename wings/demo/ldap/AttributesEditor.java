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

    private List rows = new LinkedList();
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
    }

    public void addClassDefinition(Attributes classDefinition)
	throws NamingException
    {
	removeAll();

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

	Collections.sort(rows, comparator);

	it = rows.iterator();
	while (it.hasNext()) {
	    Row row = (Row)it.next();
	    add(row.label);
	    add(row.component);
	    add(row.message);
	}
    }

    public void setSorter(Comparator comparator) {
	this.comparator = comparator;
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
        NamingException exception = null;
	Attributes attributes = new BasicAttributes();
	Iterator it = rows.iterator();
	while (it.hasNext()) {
	    Row row = (Row)it.next();
	    try {
		Attribute attribute = row.editor.getValue(row.component, row.id);
		if (attribute != null)
		    attributes.put(attribute);

		if (row.maymust == LDAP.MUST && attribute.size() == 0) {
		    addMessage(row.id, "required");
                    exception = new InvalidAttributesException(row.id + " is required");
                }
	    }
	    catch (NamingException e) {
		addMessage(row.id, e.getMessage());
                exception = e;
	    }
	}

        if (exception != null)
            throw exception;

	return attributes;
    }

    public void addMessage(String id, String message) {
     	if (id == null)
	    throw new IllegalArgumentException("id must not be null");

	Iterator it = rows.iterator();
	while (it.hasNext()) {
	    Row row = (Row)it.next();
            if (id.equals(row.id)) {
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

/*
 * Local variables:
 * c-basic-offset: 4
 * indent-tabs-mode: nil
 * compile-command: "ant -emacs -find build.xml"
 * End:
 */
