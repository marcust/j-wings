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

import java.net.*;
import javax.servlet.*;
import javax.servlet.http.*;
 
import org.wings.*;
import org.wings.plaf.*;
import org.wings.servlet.*;
import org.wings.session.*;


 

public class LdapBrowser
    extends WingServlet
{
    public SessionServlet generateSessionServlet(HttpServletRequest req)
        throws Exception
    {
        // create new default session and set plaf
        DefaultSession session = new DefaultSession();
        if (!LookAndFeelFactory.isDeployed("xhtml/css1")) {
            try {
                URL url = servletConfig.getServletContext().getResource("css1.jar");
                LookAndFeelFactory.deploy(url);
            }
            catch (Exception e) {
                System.err.println(e.getMessage());
                e.printStackTrace(System.err);
            }
        }
        session.getCGManager().setLookAndFeel("xhtml/css1");
 
        // return a new wingset session
        return new LdapBrowserSession(session);        
    }

    /** @link dependency 
     * @stereotype instantiate*/
    /*#LdapBrowserSession lnkLdapBrowserSession;*/
}                                                                                                                   

/*
 * Local variables:
 * c-basic-offset: 4
 * indent-tabs-mode: nil
 * compile-command: "ant -emacs -find build.xml"
 * End:
 */
