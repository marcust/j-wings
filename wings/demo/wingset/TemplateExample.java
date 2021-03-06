/*
 * $Id$
 * (c) Copyright 2000 wingS development team.
 *
 * This file is part of the wingS demo (http://j-wings.org).
 *
 * The wingS demo is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * Please see COPYING for the complete licence.
 */

package wingset;

import javax.swing.tree.*;
import org.wings.*;

/**
 * TODO: documentation
 *
 * @author <a href="mailto:hzeller@to.com">Henner Zeller</a>
 * @version $Revision$
 */
public class TemplateExample
    extends WingSetPane
    implements SConstants
{

    protected SComponent createExample() {
        SForm c = new SForm();
        
        try {
            java.net.URL templateURL = 
                getClass().getResource("/wingset/templates/TemplateExample.thtml");
            if( templateURL == null ){
                c.add(new SLabel("Sorry, can't find TemplateExample.thtml. Are you using a JAR-File?"));
                return c;
            }
            // you can of course directly give files here.
            STemplateLayout layout = new STemplateLayout( templateURL );
            c.setLayout( layout );
        }
        catch ( java.io.IOException except ) {
            except.printStackTrace();
        }
        
        //c.add(new STextArea(), "DemoArea");
        c.add(new SLabel ((new java.util.Date()).toString()), "theLabel");
        c.add(new SButton ("Press Me"), "TESTBUTTON");
        c.add(new STextField (), "NAME");
        c.add(new STextField (), "FIRSTNAME");
        SButtonGroup group = new SButtonGroup();
        for ( int i=0; i<3; i++ ) {
            SRadioButton b = new SRadioButton("Radio " + (i+1));
            group.add(b);
            c.add(b, "SELVAL=" + i);
        }

        STree tree = new STree(new DefaultTreeModel(TreeExample.ROOT_NODE));
        c.add(tree, "TREE");
        return c;
    }
}

/*
 * Local variables:
 * c-basic-offset: 4
 * indent-tabs-mode: nil
 * compile-command: "ant -emacs -find build.xml"
 * End:
 */
