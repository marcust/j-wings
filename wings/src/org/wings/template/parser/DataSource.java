/*
 * $Id$
 * (c) Copyright 2001 wingS development team.
 *
 * This file is part of wingS (http://wings.mercatis.de).
 *
 * wingS is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * Please see COPYING for the complete licence.
 *
 * Another version of this file by the same author has been in the ApacheJSSI
 * Servlet; there with Apache License.
 */
package org.wings.template.parser;

import java.io.IOException;
import java.io.InputStream;

/**
 * A DataSource for a Template. This encapsulates what is the notion
 * of a template from the parsers point of view: some named entity, that
 * might change over time and thus has a modification time; an input stream
 * to access the contents.
 * 
 * So <CODE>DataSource</CODE> is a general source where templates can come
 * from. You can think of Files, URLs, Database-BLOBS ..
 *
 * @version $Revision$ $Date$
 * @author <A href="mailto:zeller@think.de">Henner Zeller</A>
 */
public interface DataSource {
    /**
     * Returns a canonical name of this DataSource.
     * <p>
     * The name returned here is used to look up the parsing
     * result of the internal caching, so it should differ
     * for all different Sources :-)
     * May return <code>null</code> if this Source is supposed
     * to be parsed each time. The canonical name would be something
     * like a filename or an URL.
     */
    String getCanonicalName ();
    
    /**
     * Returns the time the content of this DataSource
     * was last modified. 
     * <p>
     * The return value is used to decide whether to reparse a
     * Source or not. Reparsing is done if the value returned
     * here <em>differs</em> from the value returned at the last processing 
     * time. This may not return a 'real' time, it needs just 
     * to be comparable to itself; so some sort of version counter
     * would be perfect as well.
     *
     * @return long a modification time
     */
    long lastModified();
    
    /**
     * Gets an InputStream of this DataSource. 
     * <p>
     * <em>Note</em> 
     * that this method may be called twice if the page has to
     * be parsed first. So you probably have to implement a
     * buffer if your underlying source is transient ..
     */
    InputStream  getInputStream() throws IOException;
}

/*
 * Local variables:
 * c-basic-offset: 4
 * indent-tabs-mode: nil
 * compile-command: "ant -emacs -find build.xml"
 * End:
 */


