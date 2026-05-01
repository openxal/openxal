package xal.app.lossviewer.preferences;

import java.awt.*;
import java.util.*;

public class ObjectConverter {
	
	public static String convertObjectToString(Object newValue) {
		if (newValue == null)
			return null;
		try {
			ObjectConstructor c =constructors.get(newValue.getClass().getName());
			return c.convertToString(newValue);
		}
		catch (Exception ex) {
			return newValue.toString();
		}
	}
	public static Object createObjectFromString(String s) {
		Map<String,String> parameters = new HashMap<String,String>();
		
		if (!s.startsWith("@")) {
			return s;
		}
		//get class name
		int indexOfBracket = s.indexOf('[');
		String className = s.substring(1, indexOfBracket);
		String classInits = s.substring(indexOfBracket + 1, s.length() - 1);
		StringTokenizer st = new StringTokenizer(classInits, ";");
		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			int sep = token.indexOf('=');
			if (sep < 1) {
				continue;
			}
			String key = token.substring(0, sep).trim();
			String value = token.substring(sep + 1, token.length()).trim();
			if (key.equals("") || value.equals("")) {
				continue;
			}
			parameters.put(key, value);
		}
		
		
		
		return constructObject(className, parameters);
	}
	
	private static Map<String,ObjectConstructor> constructors = new HashMap<String,ObjectConstructor>();
	
	private static Object constructObject(String className, Map<String, String> parameters) {
		try {
			return constructors.get(className).createObject(parameters);
		}
		catch (Exception ex) {
			System.out.println(ex.getMessage());
			return null;
		}
		
	}
	
	
	
	private interface ObjectConstructor {
		public Object createObject(Map<String,String> parameters) throws Exception;
		public String convertToString(Object v) throws Exception;
	}
	
	
	
	static {
		
		
		constructors.put("java.awt.Rectangle", new ObjectConstructor(){
				
				public String convertToString(Object v) {
					Rectangle r = (Rectangle)v;
					String result = "@" + r.getClass().getName() + "[" +
						"width=" + r.width + ";height=" + r.height +
						";x=" + r.x + ";y=" + r.y + "]";
					return result;
				}
				
				public Object createObject(Map<String, String> parameters) {
					int width=0, height=0,x=0,y=0;
					for (String key : parameters.keySet()) {
						if (key.equals("width")) {
							width = Integer.parseInt(parameters.get(key));
						}
						else if (key.equals("height")) {
							height = Integer.parseInt(parameters.get(key));
						}
						else if (key.equals("x")) {
							x = Integer.parseInt(parameters.get(key));
						}
						else if (key.equals("y")) {
							y = Integer.parseInt(parameters.get(key));
						}
						
					}
					return new Rectangle(x, y, width, height);
					
				}
				
				
			});
	}
	
}
