package edu.isi.bmkeg.uml.utils;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.isi.bmkeg.uml.model.UMLattribute;

public class UMLDataConverters {

	public static String convertToString(UMLattribute att, Object data)
			throws Exception {
		if (data == null) {
			return null;
		}

		if (data.getClass().getName().endsWith(".Vector")
				|| data.getClass().getName().endsWith(".SparseObjectMatrix1D")
				|| data.getClass().getName().endsWith(".DenseObjectMatrix1D")) {
			return null;
		}

		String type = att.getType().getBaseName();

		String stringObj = null;

		try {
			if (type.equals("int") ) {

				stringObj = ((Integer) data).toString();

			} else if (type.equals("long") || type.equals("serial")) {

				stringObj = ((Long) data).toString();

			} else if (type.equals("float")) {

				stringObj = ((Float) data).toString();

			} else if (type.equals("double")) {

				stringObj = ((Double) data).toString();

			} else if (type.equals("boolean")) {

				/**
				 * @todo MySQL does support full 'BOOLEAN' type at this moment.
				 * 
				 *       Current solution : A value of zero is considered false;
				 *       Non-zero values are considered true. This needs to be
				 *       updated when the 'BOOLEAN' type handling is introduced
				 *       in the future.
				 */
				// stringObj = ((Boolean) data).toString();
				if (((Boolean) data).booleanValue()) {
					stringObj = "1";
				} else {
					stringObj = "0";
				}

			} else if (type.equals("short")) {

				stringObj = ((Short) data).toString();

			} else if (type.equals("char")) {

				stringObj = new String(((String) data).substring(0, 1));

			} else if (type.equals("url") || type.equals("longString")) {

				stringObj = data.toString();

			} else if (type.equals("shortString") ) {

				stringObj = data.toString();
				if( stringObj.length() >25) 
					stringObj = stringObj.substring(0,25);

			} else if (type.equals("String") ) {

				stringObj = data.toString();
				if( stringObj.length() > 255) 
					stringObj = stringObj.substring(0,255);

			} else if (type.equals("blob")) {

				stringObj = "BLOB: " + data.toString();

			} else if (type.equals("image")) {

				stringObj = "IMAGE: " + data.toString();

			} else if (type.equals("date") || type.equals("year")
					|| type.equals("time") || type.equals("timestamp")) {

				SimpleDateFormat df = null;
				ParsePosition pos = null;
				Date data_date = null;

				if (type.equals("timestamp")) {
					df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				} else if (type.equals("time")) {
					df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				} else if (type.equals("date")) {
					df = new SimpleDateFormat("yyyy-MM-dd");
				} else if (type.equals("year")) {
					df = new SimpleDateFormat("yyyy");
				}

				stringObj = df.format(data);

			} else {

				throw new Exception("Data type " + type + " not supported");

			}

		} catch (Exception e) {

			throw new Exception("Errors converting data of type " + type);

		}

		return (stringObj);

	}

	/**
	 * Trims string to the length of the attribute type
	 * 
	 * @param value
	 *            String The value that the attribute will be set to
	 * 
	 * @throws Exception
	 * @return String
	 */
	public static String trimString(UMLattribute att, String value)
			throws Exception {

		String t = att.getType().getBaseName();

		if (!t.startsWith("string(") || value == null)
			return value;

		if (value.length() == 0)
			return value;

		Pattern p = Pattern.compile("string\\((\\d+)\\)");
		Matcher m = p.matcher(t);
		if (m.find()) {
			String lll = m.group(1);
			Integer ll = new Integer(lll);
			int l = ll.intValue();
			if (l < value.length()) {
				value = value.substring(0, l);
				System.out.println("        Attempting to set value of "
						+ att.getBaseName() + " truncated to " + value);
			}
		}

		return value;

	}

	public static Object convertToType(UMLattribute att, Date data) throws Exception {
		if (data == null) {
			return null;
		}

		String type = att.getType().getBaseName();
		Object data_obj = null;

		try {
			if (type.equals("date") || type.equals("year")
					|| type.equals("time") || type.equals("timestamp")) {

				if (type.equals("year")) {
					SimpleDateFormat df = new SimpleDateFormat("yyyy");
					ParsePosition pos = new ParsePosition(0);
					Date lowerCheck = df.parse("1901");
					Date upperCheck = df.parse("2155");

					if (data.compareTo(lowerCheck) < 0
							|| data.compareTo(upperCheck) > 0) {
						throw new Exception(
								"Year type must fall in the range 1901-2155");
					}

				}

				data_obj = (Object) data;

			} else {

				throw new Exception("Can only convert Date data into Date type");

			}

		} catch (Exception e) {

			throw new Exception("Errors converting data of type " + type);

		}

		return data_obj;

	}

	public static Object convertToType(UMLattribute att, String data) throws Exception {

		if (data == null) {
			return null;
		} else if (data.length() == 0) {
			return null;
		}

		String type = att.getType().getBaseName();
		Object data_obj = null;

		if (type.equals("int") ) {

			data = data.replaceAll("\\s+", "");
			Integer data_int = Integer.valueOf(data);
			data_obj = (Object) data_int;

		} else if (type.equals("long") || type.equals("serial") ) {

			Long data_long = Long.valueOf(data);
			data_obj = (Object) data_long;

		} else if (type.equals("float")) {

			Float data_float = Float.valueOf(data);
			data_obj = (Object) data_float;

		} else if (type.equals("double")) {

			Double data_double = Double.valueOf(data);
			data_obj = (Object) data_double;

		} else if (type.equals("boolean")) {

			Boolean data_boolean = Boolean.valueOf(data);
			data_obj = (Object) data_boolean;

		} else if (type.equals("short") ) {

			Short data_short = Short.valueOf(data);
			data_obj = (Object) data_short;

		} else if (type.equals("char")) {

			data_obj = (Object) data.substring(0, 0);

		} else if (type.equals("longString") || type.startsWith("url")) {

			data = data.toString();
			data_obj = (Object) data;

		} else if (type.equals("shortString") ) {

			String s = data.toString();
			if( s.length() > 25) 
				s = s.substring(0,25);
			data_obj = (Object) s;

		} else if (type.equals("String") ) {

			String s = data.toString();
			if( s.length() > 255) 
				s = s.substring(0, 255);
			data_obj = (Object) data;

		} else if (type.equals("date") || type.equals("year")
				|| type.equals("time") || type.equals("timestamp")) {

			SimpleDateFormat df = null;
			ParsePosition pos = null;
			Date data_date = null;

			if (type.equals("timestamp")) {
				df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			} else if (type.equals("time")) {
				df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			}
			/**
			 * @todo: must permit users to enter date formats other than
			 *        yyy-MM-dd
			 */
			else if (type.equals("date")) {
				df = new SimpleDateFormat("yyyy-MM-dd");
			} else if (type.equals("year")) {
				df = new SimpleDateFormat("yyyy");
			}

			pos = new ParsePosition(0);
			data_date = df.parse(data, pos);

			if (data_date == null) {

				// 
				// default Date.toString() seems to be 'Wed Jul 24 21:59:31 PDT 2013'
				//  try this as last result
				//
				df = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
				pos = new ParsePosition(0);
				data_date = df.parse(data, pos);
				
				if( data_date == null )
					throw new Exception("String cannot be parsed as date");
			
			}

			data_obj = (Object) data_date;

		} else {

			throw new Exception("Data type " + type + " not supported");

		}

		return data_obj;

	}
	
	public static String getQuote (UMLattribute a)
	  {
	    String type = a.getType().getBaseName();

	    String quote = "";

	    if(type.equals("char") || type.equals("String") || type.equals("url") ||
	       type.startsWith("string(") || type.equals("date") ||
	       type.equals("year") || type.equals("time") ||
	       type.equals("timestamp") || type.equals("longString")
	       || type.equals("shortString")) {

	      quote = "'";

	    }

	    return( quote );

	  }
	

}
