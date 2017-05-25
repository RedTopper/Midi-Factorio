package org.red.factorio;

import org.json.JSONObject;

public class Substation {
	public static JSONObject generate(int id, int x, int y) {
		return new JSONObject()
		.put("entity_number", id)
		.put("name", "substation")
		.put("position", Position.generatePosition((double)x * 3.0 + 1.5, (double)y + 1.5));
	}
}
