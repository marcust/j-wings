package ldap;

import java.awt.Color;
import java.awt.Insets;
import java.awt.event.*;
import java.io.*;
import java.text.*;
import java.util.*;

import javax.naming.*;
import javax.naming.directory.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.swing.*;
import javax.swing.event.*; 
import javax.swing.table.*;

import org.wings.*;
import org.wings.border.*;
import org.wings.io.Device;
import org.wings.io.ServletDevice;
import org.wings.session.*;
import org.wings.util.*;

public class LdapBrowser
    implements SConstants
{
    private SFrame frame;
    private SList namesList;
    private SForm namesForm;
    
    private DirContext context;

    private Vector namesVector;



    private String[] overviewAttributes;
    private String[] overviewLabels;
    private String[] detailviewAttributes;
    private String[] detailviewLabels;
    
    private String [] detailviewTemplateLabels =
    {"cn","sn","uid","mail","labeledURI","telephoneNumber","Geburtsjahr","Taetigkeitsbereiche","Zusatzausbildungen","BeruflicherWerdegang","Seit","Teamzugehoerigkeit","jpegPhoto"}; 

    private String searchLabel;
    private String searchAttribute;

    private String server;
    private String basedn;
    private String binddn;
    private String password;

    private SDesktopPane desktop;
    private SInternalFrame searchFrame;
    private SInternalFrame detailFrame;
    //private SFrame searchFrame;
    //private SFrame detailFrame;

    private STextField searchTextField;
    private STable table;
    private SPanel detailPanel;
    private SForm detailForm;

    private OverviewModel overviewModel;

    private final String SUBMIT = "Suche starten";

    public LdapBrowser() {
        frame = new SFrame("LDAP Browser");

	Session properties = SessionManager.getSession();
	StringTokenizer attributes, labels;
	int i;

	server = (String)properties.getProperty("ldap.server.host");
	basedn = (String)properties.getProperty("ldap.server.basedn");
	binddn = (String)properties.getProperty("ldap.server.binddn");
	password = (String)properties.getProperty("ldap.server.password");

	Hashtable env = new Hashtable();
	env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
	env.put(Context.PROVIDER_URL, "ldap://" + server);
	env.put(Context.SECURITY_PRINCIPAL, binddn);
	env.put(Context.SECURITY_CREDENTIALS, password);

        

	try {
	    context = new InitialDirContext(env);
	}
	catch(NamingException e) {
	    System.err.println(e.getMessage());
	    e.printStackTrace(System.err);
	}
        namesVector = new Vector();
        
	attributes = new StringTokenizer((String)properties.getProperty("overview.attributes"), ",");
	labels = new StringTokenizer((String)properties.getProperty("overview.labels"), ",");
	overviewAttributes = new String[attributes.countTokens()];
	overviewLabels = new String[labels.countTokens()];
	i = 0;
	while (attributes.hasMoreTokens()) {
	    overviewAttributes[i] = attributes.nextToken();
	    overviewLabels[i] = labels.nextToken();
	    i++;
	}

	attributes = new StringTokenizer((String)properties.getProperty("detailview.attributes"), ",");
	labels = new StringTokenizer((String)properties.getProperty("detailview.labels"), ",");
	detailviewAttributes = new String[attributes.countTokens()];
	detailviewLabels = new String[labels.countTokens()];
	i = 0;
	while (attributes.hasMoreTokens()) {
	    detailviewAttributes[i] = attributes.nextToken();
	    detailviewLabels[i] = labels.nextToken();
	    i++;
	}

	this.searchAttribute = (String)properties.getProperty("search.attribute");
	this.searchLabel = (String)properties.getProperty("search.label");

        desktop = new SDesktopPane();
	frame.getContentPane().add(desktop);

	//searchFrame = new SFrame();
	//frame.getContentPane().add(searchFrame);
	searchFrame = new SInternalFrame();
        searchFrame.setClosable(false);
        searchFrame.setTitle("PGD");
	desktop.add(searchFrame);
        
	//detailFrame = new SFrame();
	//frame.getContentPane().add(detailFrame);
	detailFrame = new SInternalFrame();
        detailFrame.setTitle("PGD");
	desktop.add(detailFrame);
        
	SForm searchForm = new SForm(new SFlowLayout());
	searchForm.setBorder(new SBevelBorder());

	SLabel label = new SLabel(searchLabel);
	searchTextField = new STextField();

	SButton submit = new SButton(SUBMIT);
	submit.addActionListener(new SearchAction());
        
        String[] data = {"ones", "two", "three", "four"};

        namesList = new SList(data);
        
        namesList.addListSelectionListener(new ListSelectionListener () { 
                public void valueChanged(ListSelectionEvent e) {
                }
            }); 
        namesList.setVisibleRowCount(3);
        namesList.setSelectionMode(SINGLE_SELECTION);
 
	searchForm.add(label);
	searchForm.add(searchTextField);
	searchForm.add(submit);
	//searchForm.add(namesList);

	searchFrame.getContentPane().add(searchForm, "North");

	overviewModel = new OverviewModel();


       namesForm = new SForm();
        //namesForm.add(namesList);
	table = new STable(overviewModel);
        table.setShowGrid(false);
        table.setShowHorizontalLines(false);
        table.setShowVerticalLines(false);
                
        table.setSelectionMode(SINGLE_SELECTION);
        
        ListSelectionModel rowSM = table.getSelectionModel();
        rowSM.addListSelectionListener(new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e) {
                    ListSelectionModel lsm = (ListSelectionModel)e.getSource();
                    if (lsm.isSelectionEmpty()) {
                    } else {
                        int selectedRow = lsm.getMinSelectionIndex();
                        showDetails(overviewModel.get(selectedRow));
                    }
                }
            });
	AttributesCellRenderer renderer = new AttributesCellRenderer();
	renderer.setSelectableColumns(new int[] { 0 });
	renderer.addCellSelectionListener(new CellSelectionListener() {
		public void cellSelected(CellSelectionEvent e) {
		    System.out.println("select: x=" + e.getXPosition() + ", y=" + e.getYPosition());
		    showDetails(overviewModel.get(e.getYPosition()));
		}
	    });
	table.setDefaultRenderer(Attribute.class, renderer);

	searchFrame.getContentPane().add(table);
        try {
            
            System.err.println(getClass().getResource("ldapbrowser.html"));
            detailPanel = new SPanel(new
                STemplateLayout(getClass().getResource("ldapbrowser.html")));
            detailFrame.getContentPane().add(detailPanel, "Center");
        }
        catch(Exception e) {
            e.printStackTrace(System.err);
        }

	SButton backButton = new SButton("ok");
	backButton.addActionListener(new BackAction());
	detailFrame.getContentPane().add(backButton, "South");

        // the SInternalFrame
	searchFrame.show();
        // the SFrame
        frame.show();
    }

    protected void showDetails(Attributes attributes) {
	System.err.println("show detailFrame");
	//detailPanel.removeAll();

	for (int i=0; i < detailviewAttributes.length; i++) {
            String value = "value";

	    try {
               
		Attribute attribute = attributes.get(detailviewAttributes[i]);
                SLabel detailLabel = new SLabel(detailviewLabels[i]);
                detailLabel.setForeground(Color.blue);
               
		detailPanel.add(detailLabel,detailviewTemplateLabels[i]);
                if (attribute!=null) {
                    if (attribute.get().getClass().getName().equals("[B")) {
                        if (attribute.getID().equals("jpegPhoto")) {
                            SLabel iconL = new SLabel();
                            try {
                                File tmp = File.createTempFile("photo","tmp",null);
                                FileOutputStream fos = new FileOutputStream(tmp);
                                fos.write((byte[])attribute.get());
                                fos.close();
                                iconL.setIcon(new FileImageIcon(tmp));
                            }
                            catch(java.io.FileNotFoundException e) {
                                e.printStackTrace();
                                iconL.setText("photo kann nicht angezeigt werden");
                            }
                            catch(java.io.IOException e) {
                                e.printStackTrace();
                                iconL.setText("photo kann nicht angezeigt werden");
                            }
                           
                            
                                //SImageIcon icon = new SImageIcon(new ImageIcon((byte[])attribute.get()));
                                //detailPanel.add(new SLabel(icon),detailviewTemplateLabels[i]+"_"+ value);
                            detailPanel.add(iconL,detailviewTemplateLabels[i]+"_"+ value);
                        }
                        else if (attribute.getID().equals("userPassword")) {
                            detailPanel.add(new
                                SLabel("-"),detailviewTemplateLabels[i]+"_"+ value);
                        }
                    }
                    else {
                        StringBuffer buffer = new StringBuffer();

                        for (int i2=0; i2 < attribute.size(); i2++) {
                            if (i2 > 0)
                                buffer.append(", ");
                            buffer.append(attribute.get(i2).toString());
                        }
                        detailPanel.add(new
                            SLabel(buffer.toString()),detailviewTemplateLabels[i] + "_" + value);
                    }
                }
                else detailPanel.add(new SLabel("-"),detailviewTemplateLabels[i] + "_" + value);
	    }
	    catch (NullPointerException e) {
                e.printStackTrace();
		detailPanel.add(new SLabel("-"),detailviewTemplateLabels[i] + "_" + value);
	    }
	    catch (NamingException e) {
		detailPanel.add(new SLabel(e.getMessage()),value);
	    }
	}

	detailFrame.setIconified(false);
	detailFrame.setClosed(false);
	searchFrame.setMaximized(false);
    }

    class SearchAction
	extends AbstractAction
    {
	public void actionPerformed(ActionEvent evt) {
            //(&(sn=Geisel)(mail=*))";
            StringBuffer fB = new StringBuffer();
            fB.append("(&");
            fB.append("(" + searchAttribute + "=*" + searchTextField.getText() + "*)");
            fB.append("(objectclass=PGDPerson)");
            fB.append(")");
	    String filter = fB.toString();
            overviewModel.setFilter(filter);
            //da 
            populateList(filter);
	}
    }

    class BackAction
	extends AbstractAction
    {
	public void actionPerformed(ActionEvent evt) {
	    detailFrame.setIconified(true);
	}
    }
    
    public void populateList(String filter) {
        namesVector.clear();
        try {
            SearchControls cons = new SearchControls();
            cons.setSearchScope(SearchControls.SUBTREE_SCOPE);
            
            NamingEnumeration enum = context.search(basedn, filter, cons);
            int i = 0;
            while (enum.hasMore()) {
            i++;
            SearchResult searchResult = (SearchResult)enum.next();
            namesVector.add(searchResult.getAttributes().get(searchAttribute).toString());
                        
        } 
            enum.close();
            //namesList.setListData(namesVector);
            detailPanel.removeAll();
        }
        catch (NamingException e) {
		System.err.println(e.getMessage());
		e.printStackTrace(System.err);
        }
    }
    


    class OverviewModel
	extends AbstractTableModel
    {
	private String filter = null;
	private List data = new LinkedList();
        
	public void setFilter(String filter) {
	    this.filter = filter;
	    populateModel();
	}
        
	protected void populateModel() {
	    try {
		data.clear();
                SearchControls cons = new SearchControls();
                cons.setSearchScope(SearchControls.SUBTREE_SCOPE);
               
		NamingEnumeration enum = context.search(basedn, filter, cons);
                int i = 0;
		while (enum.hasMore()) {
                    i++;
                    SearchResult searchResult = (SearchResult)enum.next();
		    data.add(searchResult.getAttributes());
		}
		enum.close();
                //if (i==0)
                detailPanel.removeAll();
	    }
	    catch (NamingException e) {
		System.err.println(e.getMessage());
		e.printStackTrace(System.err);
	    }
	}
        
	public int getRowCount() {
	    return data.size();
	}

	public int getColumnCount() {
	    return overviewAttributes.length;
	}

	public Class getColumnClass(int columnIndex) {
	    return Attribute.class;
	}

	public Object getValueAt(int row, int column) {
	    Attributes attributes = (Attributes)data.get(row);
            return attributes.get(overviewAttributes[column]);
	}

	public Attributes get(int row) {
	    return (Attributes)data.get(row);
	}

	public String getColumnName(int column) {
	    return overviewLabels[column];
	}
    }

  
    
    public String getServletInfo() {
        return "LdapBrowser $Revision$";
    }
}

/*
 * Local variables:
 * c-basic-offset: 4
 * indent-tabs-mode: nil
 * compile-command: "ant -emacs -find build.xml"
 * End:
 */
