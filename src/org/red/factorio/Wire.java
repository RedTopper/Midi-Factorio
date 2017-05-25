package org.red.factorio;

import org.json.JSONObject;

public class Wire {
	public static JSONObject to(int entity, int circuit) {
		return new JSONObject()
		.put("entity_id", entity)
		.put("circuit_id", circuit);
	}
	
	public static JSONObject from(int entity) {
		return new JSONObject()
		.put("entity_id", entity);
	}
}
