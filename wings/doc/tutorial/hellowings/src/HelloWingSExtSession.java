
import org.wings.servlet.SessionServlet;
import org.wings.session.Session;
import org.wings.SContainer;
import org.wings.SLabel;
import org.wings.SButton;
import org.wings.STable;

import javax.servlet.ServletConfig;

/**
 * <h1>HelloWingSExtSession.java</h1>
 * <p>A very simple <code>SessionServlet</code>.
 * </p><p>Only prints out "<code>Hello WingS!</code>" and a little more ;-).</p>
 * <p><b>Created 2002, mercatis GmbH</b></p>
 **/
public class HelloWingSExtSession extends SessionServlet {

    /**
     * <p>creates the GUI of the webapplication</p>
     * @param sess the WingS-Session
     **/
    public HelloWingSExtSession ( Session sess ) {
	// call super-constructor
	super ( sess );
	System.out.println ( "creating new HelloWingSSession" );
    }

  /**
   * <p>does anything necessary after the initialization of the servlet has run.</p>
   * @param config the servlet configuration
   **/
    public void postInit(ServletConfig config) {
      initGUI();
    }
  

  /**
   * <p>initializes the GUI</p>
   **/
    private void initGUI () {
	// get the pane to put components in
	SContainer pane = getFrame ( ).getContentPane();

	// create and add a label to the pane
	SLabel label = new SLabel ( "Hello WingS!" );
	pane.add( label );

	// create and add a table to the pane
	final javax.swing.table.DefaultTableModel tm = 
	    new javax.swing.table.DefaultTableModel( 10, 10 );
	for ( int i = 0; i<10; i++ ) 
	    for ( int j = 0; j<10; j++ )
		tm.setValueAt ( (i*j)+"", i, j );
	STable table = new STable ( tm );
	pane.add ( table );

	// create and add a button which is changing table entries randomly
	SButton button = new SButton ( "Change" );
	button.addActionListener ( new java.awt.event.ActionListener () {
		public void actionPerformed ( java.awt.event.ActionEvent ae ) {
		    int row = (int)Math.round(Math.random () * 10000.0) % 10;
		    int col = (int)Math.round(Math.random () * 10000.0) % 10;
		    int val = (int)Math.round(Math.random () * 10000.0);
		    tm.setValueAt ( val+"", row, col );
		}
	    });
	pane.add ( button );

	// note: no frame.show() or frame.setSize(..) needed
    }
    
}// HelloWingSExtSession









