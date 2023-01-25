package tipsystem.utils;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class StringFormat {
	
	public static String convertToIntNumberFormat(String number ) {
		NumberFormat numberFormat = NumberFormat.getInstance();
		
		double n = Double.valueOf(number);
		
		return String.format("%s", numberFormat.format((int)n));
	}
	
	public static String convertToNumberFormat(String number ) {
		if ("".equals(number)) number = "0";
		
		double n = Double.valueOf(number);
		
		return convertToNumberFormat(n);
	}
	
	public static String convertToNumberFormat(Double number ) {
		NumberFormat numberFormat = NumberFormat.getInstance();
		
		return String.format("%s", numberFormat.format(number));
	}
	
	//수숫점 반올리 예 100.56 -> 101 로표현
	public static String convertToRoundFormat(String number ) {
		if (number.equals("")) number = "0";
		
		double n = Double.valueOf(number);
		
		return convertToRoundFormat(n);
	}
	
	public static String convertToRoundFormat(Double number){
		long round_number = Math.round(number);
		NumberFormat numberFormat = NumberFormat.getInstance();
		
		return String.format("%s", numberFormat.format(round_number));		
	}	
	
	
	public static String convertT00NumberFormat(String number ) {
		if (number.equals("")) number = "0";
		
		double n = Double.valueOf(number);

		String pattern = "###,###,###.##";
		DecimalFormat df = new DecimalFormat(pattern);
		
		//System.out.println(df.format(c));

		return df.format(n);
	}

	public static String convertTNumberFormat(String number ) {
		if (number.equals("")) number = "0";

		double n = Double.valueOf(number);

		String pattern = "###,###,###";
		DecimalFormat df = new DecimalFormat(pattern);

		return df.format(n);
	}

}

