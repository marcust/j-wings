package ldap.editors;

import java.io.*;
import java.util.*;
import java.util.logging.*;
import javax.naming.*;
import javax.naming.directory.*;

import org.wings.*;
import org.wings.session.*;

public class EditorFactory
{
    private final static Logger logger = Logger.getLogger("ldap.editors");

    static final Map editorsByName = new HashMap();
    static final Map editorsBySyntax = new HashMap();
    static Properties names;
    static Properties syntaxes;
    static Editor singleLineTextEditor = new SingleLineTextEditor();

    public static Editor getEditor(Attributes attributes)
	throws NamingException
    {
	String name = (String)attributes.get("NAME").get();
	Editor editor = getEditorByName(name);
        if (editor != null)
          return editor;

	Attribute syntaxAttribute = attributes.get("SYNTAX");
	if (syntaxAttribute != null) {
	    String syntax = (String)syntaxAttribute.get();
	    editor = getEditorBySyntax(syntax);
	    if (editor != null)
		return editor;
	}

	// fallback
	logger.log(Level.INFO, "no editor for: " + attributes);
	return singleLineTextEditor;
    }

    public static Editor getEditorBySyntax(String syntax) {
	Editor editor = (Editor)editorsBySyntax.get(syntax);
	if (editor != null)
	    return editor;

	if (syntaxes == null) {
	    syntaxes = new Properties();
	    try {
		InputStream in = EditorFactory.class.getClassLoader().getResourceAsStream("ldap/editors/syntax.properties");
		syntaxes.load(in);
	    }
	    catch (Exception e) {
		logger.log(Level.WARNING, "name.properties", e);
	    }
	}

	String className = syntaxes.getProperty(syntax);
	if (className == null)
	    return null;

	try {
	    Class clazz = Class.forName(className);
	    editor = (Editor)clazz.newInstance();
	    editorsBySyntax.put(syntax, editor);
	    return editor;
	}
	catch (Exception e) {
            logger.log(Level.WARNING, "class: " + className, e);
	}

	return null;
    }

    public static Editor getEditorByName(String name) {
	Editor editor = (Editor)editorsByName.get(name);
	if (editor != null)
	    return editor;

	if (names == null) {
	    names = new Properties();
	    try {
		InputStream in = EditorFactory.class.getClassLoader().getResourceAsStream("ldap/editors/name.properties");
		names.load(in);
	    }
	    catch (Exception e) {
		logger.log(Level.WARNING, "name.properties", e);
	    }
	}

	String className = names.getProperty(name);
	if (className == null)
	    return null;

	try {
	    Class clazz = Class.forName(className);
	    editor = (Editor)clazz.newInstance();
	    editorsByName.put(name, editor);
	    return editor;
	}
	catch (Exception e) {
            logger.log(Level.WARNING, "class: " + className, e);
	}

	return null;
    }
}