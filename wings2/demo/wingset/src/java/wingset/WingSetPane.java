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

package wingset;


import org.wings.*;
import org.wings.session.SessionManager;
import org.wings.event.SComponentAdapter;
import org.wings.event.SComponentEvent;

/**
 * A basic WingSet Pane, which implements some often needed functions.
 *
 * @author <a href="mailto:haaf@mercatis.de">Armin Haaf</a>
 * @version $Revision$
 */
abstract public class WingSetPane
    extends SPanel
    implements SConstants {

    private static final SResourceIcon SOURCE_LABEL_ICON =
        new SResourceIcon("org/wings/icons/File.gif");

    private boolean initialized = false;

    public WingSetPane() {
        setLayout(createResourceTemplate("/templates/ContentLayout.thtml"));

        SAnchor anchor = new SAnchor("/wingset/" +
                                   getClass().getName().substring(getClass().getName().indexOf('.') +1) + ".java");
        anchor.setTarget("sourceWindow");
        anchor.add( new SLabel("view source code", SOURCE_LABEL_ICON));
        add(anchor, "viewSource");

        addComponentListener(new SComponentAdapter() {
                public void componentShown(SComponentEvent e) {
                    if ( !initialized ) {
                        add(createExample(), "content"); // content generated by example
                        initialized = true;
                    }
                }
            });
    }

    /**
     * Override this.
     */
    protected abstract SComponent createExample();
    
    protected static SLayoutManager createResourceTemplate(String name) {
        try {
            return new STemplateLayout(SessionManager.getSession().getServletContext().getRealPath(name));
        }
        catch ( Exception except ) {
            except.printStackTrace();
        }
        return null;
    }
}

/*
 * Local variables:
 * c-basic-offset: 4
 * indent-tabs-mode: nil
 * compile-command: "ant -emacs -find build.xml"
 * End:
 */