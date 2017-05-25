package org.red.factorio;

import org.json.JSONObject;

public class Position {
	
	/**
	 * Generates JSON that contains an X and Y position
	 * @param x X cord
	 * @param y Y cord
	 * @return JSONObject
	 */
	public static JSONObject generatePosition(double x, double y) {
		return new JSONObject().put("x", x).put("y", y);
	}
}
