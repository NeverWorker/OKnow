package com.neverworker.oknow.common;

import java.util.regex.*;

import com.google.gson.*;

public class CsvToJson {
	private int keySize;
	private String[] keys;
	private Pattern pattern;
	
	public void init(String format) {
		keySize = format.length() - format.replace(",", "").length() + 1;
		keys = format.split(",");
		String patternStr = " *(.*?)";
		for (int i = 1; i < keySize; i++) {
			patternStr +=" *, *(.*?)";
		}
		patternStr += " *\n";
		pattern = Pattern.compile(patternStr);
	}
	
	public JsonArray parse(boolean hasTitleRow, String linedata) {
		JsonArray list = new JsonArray();
		Matcher matcher = pattern.matcher(linedata);
		
		// discard first row
		if (hasTitleRow)
			matcher.find();
		while (matcher.find()) {
			JsonObject item = new JsonObject();
			for (int i = 0; i < keys.length; i++) {
				if (!keys[i].equals(""))
					item.addProperty(keys[i], matcher.group(i+1));
			}
			list.add(item);
		}
		
		return list;
	}
	
	
}
