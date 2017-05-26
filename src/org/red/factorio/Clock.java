package org.red.factorio;

import org.json.JSONArray;
import org.json.JSONObject;
import org.red.Generate;

public class Clock {
	
	public static final int COOLDOWN_TIME = 180;
	public static final int CCID = 1;
	public static final int DCID = 2;
	
	private static JSONObject generateTimer() {
		return new JSONObject()
		.put("filters", new JSONArray()
			.put(new JSONObject()
				.put("signal", new JSONObject()
					.put("type", "virtual")
					.put("name", "signal-T")
				).put("count", 1)
				.put("index", 1)
			)
		).put("is_on", false);
	}
	
	private static JSONObject generateDeciderData(int max) {
		return new JSONObject()
		.put("decider_conditions", new JSONObject()
			.put("first_signal", new JSONObject()
				.put("type", "virtual")
				.put("name", "signal-T")
			).put("constant", max + COOLDOWN_TIME)
			.put("comparator", "<")
			.put("output_signal", new JSONObject()
				.put("type", "virtual")
				.put("name", "signal-T")
			).put("copy_count_from_input", true)
		);
	}
	
	private static JSONObject generateConstantCombCon() {
		return new JSONObject()
		.put("1", new JSONObject()
			.put("red", new JSONArray()
					
				//connect to Decider Combinator
				.put(Wire.to(DCID, 1))
			)
		);
	}

	private static JSONObject generateDeciderCombCon() {
		return new JSONObject()
		.put("1", new JSONObject()
			.put("red", new JSONArray()
					
				//connection to self
				.put(Wire.to(DCID, 2)
						
				//connect to next Decider Combinator
				).put(Wire.to(DCID + 2, 1)
				
				//connect to Constant Combinator
				).put(Wire.toCC(CCID))
			)
		)
		.put("2", new JSONObject()
			.put("red", new JSONArray()
					
				//connect to self
				.put(Wire.to(DCID, 1))
			)
		);
	}
	
	public static JSONObject generateFirstConstantComb() {
		return new JSONObject()
		.put("entity_number", 1)
		.put("name", "constant-combinator")
		.put("position", Generate.position(0,-2))
		.put("direction", 2)
		.put("control_behavior", generateTimer())
		.put("connections", generateConstantCombCon());
		
	}

	public static JSONObject generateFirstDeciderComb(int max) {
		return new JSONObject()
		.put("entity_number", 2)
		.put("name", "decider-combinator")
		.put("position", Generate.position(1.5,-2))
		.put("direction", 2)
		.put("control_behavior", generateDeciderData(max))
		.put("connections", generateDeciderCombCon());
	}
}
