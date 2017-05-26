package org.red.factorio;

import org.json.JSONArray;
import org.json.JSONObject;
import org.red.Generate;
import org.red.music.Sound;

public class Speaker {
	
	public static final int FIRST_NOTE_DECIDER = 4;
	public static final double PERCUSSION_VOL = 0.6;
	public static final double PIANO_VOL = 1;

	public static JSONObject generateSpeaker(int index, int id, Sound sound) {
		return new JSONObject()
		.put("entity_number", id)
		.put("name", "programmable-speaker")
		.put("position", Generate.position(index + 4, -2))
		.put("control_behavior", new JSONObject()
			.put("circuit_condition", generateCircutCondition(index, sound))
			.put("circuit_parameters", generateSpeakerData(sound)))
		.put("connections", generateSpeakerConnections(index, id))
		.put("parameters", new JSONObject()
			.put("playback_globally", true)
			.put("playback_volume", (sound.percussion ? PERCUSSION_VOL : PIANO_VOL))
			.put("allow_polyphony", true)
		);
	}

	public static JSONObject generateLamp(int index, int id, Sound sound) {
		return new JSONObject()
		.put("entity_number", id)
		.put("name", "small-lamp")
		.put("position", Generate.position(index + 4, -3))
		.put("control_behavior", new JSONObject()
			.put("circuit_condition", generateCircutCondition(index, sound)))
		.put("connections", generateSpeakerConnections(1, id));
	}

	private static JSONObject generateSpeakerConnections(int index, int id) {
		JSONObject wire;
		if(index == 0)
			wire = Wire.to(FIRST_NOTE_DECIDER, 2);
		else
			wire = Wire.toCC(id - 1);
		
		return new JSONObject()
		.put("1", new JSONObject()
			.put("green", new JSONArray()
				.put(wire)
			)
		);
	}

	private static JSONObject generateSpeakerData(Sound sound) {
		return new JSONObject()
		.put("signal_value_is_pitch", true)
		.put("instrument_id", (sound.percussion ? 1 : 2))
		.put("note_id", 0);
	}

	private static JSONObject generateCircutCondition(int index, Sound sound) {
		return new JSONObject()
		.put("first_signal", new JSONObject()
			.put("type", "virtual")
			.put("name", "signal-" + sound.name))
		.put("constant", 0)
		.put("comparator", ">");
	}
}
