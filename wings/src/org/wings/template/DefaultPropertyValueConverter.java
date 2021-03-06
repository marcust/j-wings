/*
  $Id$
  (c) Copyright 2002 mercatis information systems GmbH

  Part of e-lib 
 
  This file contains unpublished, proprietary trade secret information of
  mercatis information systems GmbH. Use, transcription, duplication and
  modification are strictly prohibited without prior written consent of
  mercatis information systems GmbH.
  See http://www.mercatis.de
*/

package org.wings.template;

import java.awt.Color;
import java.io.InputStream;
import java.util.StringTokenizer;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;
import org.wings.ClasspathResource;
import org.wings.Resource;
import org.wings.SDimension;
import org.wings.SFont;
import org.wings.SIcon;
import org.wings.SURLIcon;
import org.wings.plaf.LookAndFeel;
import org.wings.style.AttributeSet;
import org.wings.style.CSSStyleSheet;
import org.wings.style.SimpleAttributeSet;
import org.wings.style.StyleSheet;
import org.wings.template.PropertyValueConverter;
import org.wings.plaf.ComponentCG;


/**
 * <!--
 * DefaultPropertyValueConverter.java
 * Created: Tue Aug  6 17:30:51 2002
 * -->
 *
 *
 *
 * <p><b>(c)2002 <a href="http://www.mercatis.de">mercatis information systems GmbH</a></b></p>
 *
 * @author <a href="mailto:armin.haaf@mercatis.de">Armin Haaf</a>
 * @version $Revision$
 */
public class DefaultPropertyValueConverter  implements PropertyValueConverter  {

    public static final DefaultPropertyValueConverter INSTANCE =
	new DefaultPropertyValueConverter();

    private final static Log logger = LogFactory.getLog("org.wings.template");
    
    /**
     * 
     */
    public DefaultPropertyValueConverter() {
	
    }
    // implementation of org.wings.template.PropertyValueConverter interface

    /**
     * Describe <code>convertPropertyValue</code> method here.
     *
     * @param value an <code>Object</code> value
     * @param targetClass a <code>Class</code> value
     * @return <description>
     * @exception UnsupportedOperationException if an error occurs
     * @exception java.lang.UnsupportedOperationException <description>
     */
    public Object convertPropertyValue(String value, Class targetClass) 
	throws UnsupportedOperationException
    {
	if ( value==null || "null".equals(value) ) {
	    return null;
	} // end of if ()
	

	if ( targetClass==String.class ) {
	    return value;
	} // end of if ()

	if ( targetClass==Boolean.TYPE || targetClass==Boolean.class ) {
	    return Boolean.valueOf(value);
	}

	if ( targetClass==Integer.TYPE || targetClass==Integer.class ) {
	    return Integer.valueOf(value);
	}
	if ( targetClass==Long.TYPE || targetClass==Long.class ) {
	    return Long.valueOf(value);
	}
	if ( targetClass==Short.TYPE || targetClass==Short.class ) {
	    return Short.valueOf(value);
	}
	if ( targetClass==Byte.TYPE || targetClass==Byte.class ) {
	    return Byte.valueOf(value);
	}
	if ( targetClass==Float.TYPE || targetClass==Float.class ) {
	    return Float.valueOf(value);
	}
	if ( targetClass==Double.TYPE || targetClass==Double.class ) {
	    return Double.valueOf(value);
	}
	if ( targetClass==Character.TYPE || targetClass==Character.class ) {
	    return new Character(value.charAt(0));
	}

	if ( targetClass==StringBuffer.class ) {
	    return new StringBuffer(value);
	} // end of if ()

	if ( SIcon.class.isAssignableFrom(targetClass) ) {
	    return LookAndFeel.makeIcon(getClass().getClassLoader(), value);
	}

	if ( targetClass==Color.class ) {
	    return LookAndFeel.makeColor(value);
	}

	if ( targetClass==SDimension.class ) {
	    return LookAndFeel.makeDimension(value);
	}

	if ( targetClass==SFont.class ) {
	    return TemplateUtil.parseFont(value);
	}

	if ( Resource.class.isAssignableFrom(targetClass) ) {
	    return new ClasspathResource(value);
	}

	if ( AttributeSet.class.isAssignableFrom(targetClass) ) {
	    return LookAndFeel.makeAttributeSet(value);
	}

	if ( StyleSheet.class.isAssignableFrom(targetClass) ) {
	    return LookAndFeel.makeStyleSheet(getClass().getClassLoader(), value);
	}

	if ( ComponentCG.class.isAssignableFrom(targetClass) ) {
	    return LookAndFeel.makeCG(getClass().getClassLoader(), value);
	}

	throw new UnsupportedOperationException("cannot create object of type " + 
						targetClass.getName());
    }
    

 
}// DefaultPropertyValueConverter

/*
   $Log$
   Revision 1.4  2004/10/08 08:43:27  blueshift
   BATCH UPDATE
   - Switched logging to commons logging
   - Correct handling of default button
   - Implemented outdated request feature
   - Implemented an 'approximation' back button
   - More usage of FORM Submit via updated javascript
   - Modified javscript to distinguish jsSubmit from submits by enter-key
   - JavaDoc

   Revision 1.3  2002/11/19 15:36:40  ahaaf
   o use LookAndFeel methods

   Revision 1.2  2002/11/01 14:17:50  ahaaf
   o add support for more types

   Revision 1.1  2002/08/06 16:45:55  ahaaf
   add DefaultPropertyManager using reflection and bean shell scripting support

*/
