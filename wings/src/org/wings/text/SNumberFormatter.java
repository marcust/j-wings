/*
 * SNumberFormatter.java
 *
 * Created on 9. September 2003, 12:15
 */

package org.wings.text;

import java.text.NumberFormat;
import java.util.Vector;
import org.wings.script.*;
import java.io.*;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import org.wings.SFormattedTextField;


/**
 * The Formatter can only be configured before the parent Frame is installed,
 * because until now it isn't possible to add JavaScript-Functions correctly 
 * after that.  
 *
 * @author  theresia & erik
 */
public class SNumberFormatter extends SAbstractFormatter{
    
    private NumberFormat numberFormat;
    private double maxVal = Double.MAX_VALUE;
    private double minVal = Double.MIN_VALUE;
    
    private boolean alert = false;

    private JavaScriptListener javaScriptListener;

    /*
     * Creates a new instance of SNumberFormatter.
     * SNumberFormater supports NumberFormat.parseIntegerOnly,
     * NumberFormat.maxIntegerDigits, NumberFormat.minIntegerDigits, NumberFormat.maxFractionDigits, 
     * NumberFormat.minIntegerDigits. 
     * Additional it's possible to set a maximal or a minimal valid value.
     *
     */
    
    public SNumberFormatter() {
    }
    
    /*
     * Creates a new instance of SNumberFormatter.
     * NumberFormater supports NumberFormat.parseIntegerOnly,
     * maxIntegerDigits, minIntegerDigits, maxFractionDigits, minIntegerDigits.
     * Additional it's possible to set a maximal valid value or a minimal valid value.
     *
     * @param NumberFormat nf the NumberFormat for the SFormattedTextField
     */
    public SNumberFormatter(NumberFormat nf){
        numberFormat = nf;
    }
    
    public NumberFormat getNumberFormat(){
        return numberFormat;
    }
    
    /* 
     * sets the NumberFormat. Doesn't work after the Frame is installed, because
     * until now it's  not possible to add a JavaScriptFunction correctly after that.
     * @param NumberFormat nf the NumberFormat for the SFormattedTextField.
     * 
     */
    public void setNumberFormat(NumberFormat nf){
        numberFormat = nf;
        updateFormatter();
    }
   
    public String valueToString ( Object value ) {
        return numberFormat.format( value );
    }
    
    /** Specified by stringToValue in class AbstractFormatter
     * @return Object representation of text
     * @param text text - String to convert
     */    
    public Object stringToValue(String text){
        Object obj = null;
        try {
            obj = numberFormat.parseObject( text );
        } catch ( Exception e ) {
            e.printStackTrace();
        }
        return obj;
    }
    
    /* 
     * sets the maximal possible value. Doesn't work after the Frame is installed, because
     * until now it's  not possible to add a JavaScriptFunction correctly after that.
     * @param double maxVal the maximal possible value for the SFormattedTextField.
     * 
     */
    public void setMaxVal(double maxVal){
        this.maxVal = maxVal;
        updateFormatter();
    }
    
    public double getMaxVal(){
        return maxVal;
    }
    
     /* 
     * sets the minimal possible value. Doesn't work after the Frame is installed, because
     * until now it's  not possible to add a JavaScriptFunction correctly after that.
     * @param double minVal the minimal possible value for the SFormattedTextField.
     * 
     */
    public void setMinVal(double minVal){
        this.minVal = minVal;
        updateFormatter();
    }
    
    public double getMinVal(){
        return minVal;
    }
    
    private char getDecimalSeparator () {       
        DecimalFormatSymbols dfs = new DecimalFormatSymbols();
        if ( numberFormat instanceof DecimalFormat ) {
            dfs = ((DecimalFormat)numberFormat).getDecimalFormatSymbols();
        }
        return dfs.getDecimalSeparator();
    }
    
    private char getGroupingSeparator() {
        DecimalFormatSymbols dfs = new DecimalFormatSymbols();
        if ( numberFormat instanceof DecimalFormat ) {
            dfs = ((DecimalFormat)numberFormat).getDecimalFormatSymbols();
        }
        return dfs.getGroupingSeparator(); 
    }
    
    /*
     * Generates the JavaScript-function based on numberFormat, MinVal and MaxVal
     * @param String name Name of the function.
     * @param boolean b true if an ActionListener is added to the SFormattedTextField
     **/
    public JavaScriptListener generateJavaScript(SFormattedTextField field, boolean b){
        
        field.addScriptListener( SCRIPT_NUMBERFORMATTER );
        
        field.removeScriptListener( SCRIPT_SAVEOLD );
        field.addScriptListener( SCRIPT_SAVEOLD );
        
        if(b){
            String funct = "if (numberFormatter(" + numberFormat.getMinimumIntegerDigits() + "," + numberFormat.getMaximumIntegerDigits() + "," + numberFormat.getMinimumFractionDigits() + "," + numberFormat.getMaximumFractionDigits() + "," + minVal + "," + maxVal + "," + numberFormat.isGroupingUsed() + "," + numberFormat.isParseIntegerOnly() + ",'"+this.getDecimalSeparator()+"','"+this.getGroupingSeparator()+"',this)) commandlessSubmitForm(event);";
            javaScriptListener = new JavaScriptListener("onchange", funct );
        }
        else{
            javaScriptListener = new JavaScriptListener("onchange", "numberFormatter(" + numberFormat.getMinimumIntegerDigits() + "," + numberFormat.getMaximumIntegerDigits() + "," + numberFormat.getMinimumFractionDigits() + "," + numberFormat.getMaximumFractionDigits() + "," + minVal + "," + maxVal + "," + numberFormat.isGroupingUsed() + "," + numberFormat.isParseIntegerOnly() + ",'"+this.getDecimalSeparator()+"','"+this.getGroupingSeparator()+"',this)");
        }
        return javaScriptListener;
    }

    public void setAlert(boolean alert){
        this.alert = alert;
    }
    
    public boolean isAlert(){
        return alert;
    }
    
    private static String functStr = "saveOld(this)";
    
    private static final JavaScriptListener SCRIPT_NUMBERFORMATTER   = new JavaScriptListener("", "", loadScript());

    private static final JavaScriptListener SCRIPT_SAVEOLD           = new JavaScriptListener("onfocus", functStr);

    private static String loadScript() {
        InputStream in = null;
        BufferedReader reader = null;
        try {    
            in = SNumberFormatter.class.getClassLoader().getResourceAsStream("org/wings/text/SNumberFormatter.js");
            reader = new BufferedReader(new InputStreamReader(in));
            StringBuffer buffer = new StringBuffer();
            String line;
            while ((line = reader.readLine()) != null)
                buffer.append(line).append("\n");

            return buffer.toString();
        }
        catch (Exception e) {
            e.printStackTrace();
            return "";
        } finally {
          try { in.close(); } catch (Exception ign) {}
          try { reader.close(); } catch (Exception ign) {}
        }
    }
}