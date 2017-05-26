package org.red.factorio;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONObject;
import org.red.Generate;
import org.red.music.Note;

public class Combinator {
	
	public static final int COMBINATOR_SIZE = 15;
	public final int tick;
	private final Map<String,Integer> notes = new HashMap<>();
	private int id = 0;
	
	public Combinator(int tick) {
		this.tick = tick;
	}
	
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append("ID " + id + " At tick " + tick + " ");
		String delem = "(";
		for(Entry<String, Integer> i : notes.entrySet()) {
			str.append(delem + i.getKey() + " " + i.getValue());
			delem = ":";
		}
		str.append(")");
		return str.toString();
	}
	
	public boolean setNote(Note note) {
		
		//check if we can insert another note
		if(notes.size() >= COMBINATOR_SIZE) return false;
		
		//check if the track we are inserting already exists
		Integer currentNote = notes.get(note.trackName);
		if(currentNote != null) {
			
			//return false if different, true if same
			return currentNote.intValue() == note.key;
		}
		
		notes.put(note.trackName, note.key);
		return true;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public int getDeciderId() {
		return this.id * 2 + 2;
	}
	
	public int getConstantId() {
		return this.id * 2 + 1;
	}

	/**
	 * Generates JSON for a single note
	 * @param pos zero based position of the note
	 * @param note note value
	 * @return JSONObject
	 */
	private JSONObject generateNote(String track, int note, int index) {
		return new JSONObject()
		.put("signal", new JSONObject()
			.put("type", "virtual")
			.put("name", "signal-" + track)
		).put("count", note)
		.put("index", index);
	}
	
	/**
	 * Generates JSON for all notes in the combinator
	 * @return JSONObject
	 */
	private JSONObject generateNoteData() {
		JSONArray values = new JSONArray();
		int index = 0;
		for(Entry<String, Integer> entry: notes.entrySet()) {
			values.put(generateNote(entry.getKey(), entry.getValue(), ++index));
		}
		return new JSONObject().put("filters", values);
	}
	
	/**
	 * Generates JSON for a decider combinator logic to output when
	 * the T signal is the same as this object's tick value.
	 * @return JSONObject
	 */
	private JSONObject generateDeciderData() {
		return new JSONObject()
		.put("decider_conditions", new JSONObject()
			.put("first_signal", new JSONObject()
				.put("type", "virtual")
				.put("name", "signal-T")
				
			//have to add one so the first note plays!
			).put("constant", this.tick + 1) 
			.put("comparator", "=")
			.put("output_signal", new JSONObject()
				.put("type", "virtual")
				.put("name","signal-everything")
			).put("copy_count_from_input", true)
		);
	}

	/**
	 * Generates JSON for a constant combinator connection.
	 * ALWAYS ASSUME THAT THE DECIDER COMBINATOR IS THE NEXT ID!
	 * @param id Current id of the constant combinator. 
	 * @return JSONObject
	 */
	private JSONObject generateConstantCombCon() {
		int subID = getConstantId();
		return new JSONObject()
		.put("1", new JSONObject()
			.put("green", new JSONArray()
				.put(Wire.to(subID + 1, 1))
			)
		);
	} 
	
	/**
	 * Generates JSON for a decider combinators connection logic.
	 * ALWAYS ASSUME THAT THE CONSTANT COMBINATOR IS ID - 1!
	 * ALWAYS ASSUME THAT THE DECIDER COMBINATOR IS ID - 2!
	 * @param id the current id of the decider combinator.
	 * @return JSONObject
	 */
	private JSONObject generateDeciderCombCon(Line pos) {
		int subID = getDeciderId();
		
		//create wire array
		JSONArray red1 = new JSONArray();
		JSONArray green1 = new JSONArray();
		JSONArray green2 = new JSONArray();
		
		//always make a connection to the previous Decider Combinator
		red1.put(Wire.to(subID - 2, 1));
		
		//if we are last, we don't want to connect to the next Decider Combinator
		if(pos != Line.LAST)
			red1.put(Wire.to(subID + 2, 1));
		
		//always connect to previous Constant Combinator
		green1.put(Wire.toCC(subID - 1));
		
		//if we are first, we don't want to connect to previous Decider Combinator 
		//(that's the clock!)
		if(pos != Line.FIRST)
			green2.put(Wire.to(subID - 2, 2));
		
		//if we are last, we don't want to connect to the next Decider Combinator
		if(pos != Line.LAST)
			green2.put(Wire.to(subID + 2, 2));
		
		return new JSONObject()
		.put("1", new JSONObject()
			.put("red", red1)
			.put("green", green1)
		).put("2", new JSONObject()
			.put("green", green2)
		);
	}
	
	/**
	 * Generates JSON data for the constant combinator
	 * @param count Id of the current object.
	 * @return JSONObject
	 */
	public JSONObject generateConstantComb(int x, int y) {
		return new JSONObject()
		.put("entity_number", getConstantId())
		.put("name", "constant-combinator")
		.put("position", Generate.position(x * 3,y))
		.put("direction", 2)
		.put("control_behavior", generateNoteData())
		.put("connections", generateConstantCombCon());
	}

	/**
	 * Generates JSON data for the decider combinator
	 * @param id Id of the current object.
	 * @return JSONObject
	 */
	public JSONObject generateDeciderComb(int x, int y, Line pos) {
		return new JSONObject()
		.put("entity_number", getDeciderId())
		.put("name", "decider-combinator")
		.put("position", Generate.position((double)x * 3.0 + 1.5, y))
		.put("direction", 2)
		.put("control_behavior", generateDeciderData())
		.put("connections", generateDeciderCombCon(pos));
	}
}
