package ldap;

import javax.naming.*;
import javax.naming.directory.*;
import javax.swing.tree.*;
import java.util.*;

public class LdapTreeNode
    implements MutableTreeNode 
{
    private static String[] RETURNING_ATTRIBUTES = new String [] { "objectclass" };
    private static String SEARCH_FILTER = "(objectclass=*)";

    private String dn;

    private DirContext context;
    private LdapTreeNode parent;
    private ArrayList children = null;

    public LdapTreeNode(DirContext context, LdapTreeNode parent, String dn) {
	this.context = context;
	this.dn = dn;
	this.parent = parent;
    }

    public LdapTreeNode(String dn) {
       	this.dn = dn;
    }

    public TreeNode getChildAt(int childIndex) {
	if (children == null)
	    getChildren();

	return (TreeNode)children.get(childIndex);
    }
    
    public int getChildCount() {
	if (children == null)
	    getChildren();

	return children.size();
    }

    public void setParent(MutableTreeNode node) {
        this.parent = parent;
    }
    public TreeNode getParent() {
	return parent;
    }

    public void removeFromParent() {
        this.parent = null;
    }

    public void add(MutableTreeNode child) {
	child.setParent(this);
	children.add(child);
    }

    public void insert(MutableTreeNode child, int index) {
	child.setParent(this);
	children.add(index, child);
    }

    public void remove(MutableTreeNode child) {
	children.remove(child);
    }

    public void remove(int index) {
	children.remove(index);
    }

    public void setUserObject(Object object) {
        dn = (String)object;
    }

    public int getIndex(TreeNode node) {
	if (children == null)
	    getChildren();

	return children.indexOf(node);
    }

    public boolean getAllowsChildren() {
	return true;
    }

    public boolean isLeaf() {
	if (children == null)
	    getChildren();

        return children.isEmpty();
    }

    public Enumeration children() {
	if (children == null)
            getChildren();

	return Collections.enumeration(children);
    }

    private void getChildren() {
        try {
            children = new ArrayList();

            SearchControls constraints = new SearchControls();
            constraints.setSearchScope(SearchControls.ONELEVEL_SCOPE);
            constraints.setReturningAttributes(RETURNING_ATTRIBUTES);

            NamingEnumeration results = context.search(getDN(), SEARCH_FILTER, constraints);
            while (results != null && results.hasMoreElements()) {
                SearchResult sr  = (SearchResult)results.next(); 
                children.add(new LdapTreeNode(context, this, sr.getName()));
            }
        }
        catch (NamingException e) {
            System.err.println(e);
        }
    }

    public String getDN() {
        if (getParent() == null || !(getParent() instanceof LdapTreeNode))
            return dn;
        else {
            String actDN = dn + "," + ((LdapTreeNode)getParent()).getDN();
            if (actDN.endsWith(",")) {
                int index = actDN.lastIndexOf(",");
                actDN = actDN.substring(0,index);
            }
            return actDN;
        }
    }
    
    public String toString() {
        return dn;
    }

    public static void main(String[] args) {
        String server = args[0];
        String bindDN = args[1];
        String passwd = args[2];
        String rootDN = args[3];

	Hashtable env = new Hashtable();
	env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
	env.put(Context.PROVIDER_URL, "ldap://" + server);
	env.put(Context.SECURITY_PRINCIPAL, bindDN);
	env.put(Context.SECURITY_CREDENTIALS, passwd);

	try {
	    DirContext context = new InitialDirContext(env);
            LdapTreeNode node = new LdapTreeNode(context, null, rootDN);

            DefaultMutableTreeNode root = new DefaultMutableTreeNode();
            root.add(node);

            Enumeration enum = root.breadthFirstEnumeration();
        }
	catch(NamingException e) {
            System.err.println(e.getMessage());
            e.printStackTrace(System.err);
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
