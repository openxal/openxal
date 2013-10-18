package xal.extension.widgets.swing.wheelswitch.util;

import java.util.Locale;


public class PrintfFormat {
    
    
    private Locale locale;
    
    private String format;
    
    public PrintfFormat(String format) throws IllegalArgumentException {
        this(Locale.getDefault(), format);
    }
    
    public PrintfFormat(Locale locale, String format) throws IllegalArgumentException {
        this.locale = locale;
        this.format = format;
    }
    
    public String sprintf(Object... args) {
        return String.format(locale, format, args);
    }
}
