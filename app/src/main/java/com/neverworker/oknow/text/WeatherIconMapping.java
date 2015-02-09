package com.neverworker.oknow.text;

import java.util.Locale;

public class WeatherIconMapping {

	public static String weatherToIcon(String iconNum) {
		Locale.setDefault(new Locale("lt"));
		switch(iconNum) {
		case "01d":
			return "I";
		case "02d":
			return "\"";
		case "03d":
			return "!";
		case "04d":
			return "!";
		case "09d":
			return "*";
		case "10d":
			return "+";
		case "11d":
			return "F";
		case "13d":
			return "9";
		case "50d":
			return "<";
		case "01n":
			return "N";
		case "02n":
			return "#";
		case "03n":
			return "!";
		case "04n":
			return "!";
		case "09n":
			return "*";
		case "10n":
			return ",";
		case "11n":
			return "F";
		case "13n":
			return "9";
		case "50n":
			return "<";
		}
		
		return "";
	}
}
