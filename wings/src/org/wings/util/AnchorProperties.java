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
 */
package org.wings.util;

import org.wings.SimpleURL;

/**
 * This class contains the properties for the AnchorStack.
 */
public final class AnchorProperties {
    private final SimpleURL url;
    private final String    target;
    
    AnchorProperties(SimpleURL url, String target) {
	this.url = url;
	this.target = target;
    }
    
    public SimpleURL getURL() { return url; }
    public String    getTarget() { return target; }
}

/*
 * Local variables:
 * c-basic-offset: 4
 * indent-tabs-mode: nil
 * compile-command: "ant -emacs -find build.xml"
 * End:
 */