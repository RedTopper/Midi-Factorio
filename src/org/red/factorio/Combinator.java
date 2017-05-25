package org.red.factorio;

import org.json.JSONArray;
import org.json.JSONObject;
import org.red.Position;
import org.red.music.Note;

public class Combinator {
	public final int tick;
	private final int[] notes = new int[15];
	private int id = 0;
	
	public Combinator(int tick) {
		this.tick = tick;
	}
	
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append("ID " + id + " At tick " + tick + " ");
		String delem = "(";
		for(int i : notes) {
			str.append(delem + i);
			delem = ":";
		}
		str.append(")");
		return str.toString();
	}
	
	public boolean setNote(Note note) throws Exception {
		if(note.track > 14) throw new Exception("Too many tracks in song!");
		if(notes[note.track] > 0 && notes[note.track] != note.key) return false;
		notes[note.track] = note.key;
		return true;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	private int getDeciderId() {
		return this.id * 2 + 2;
	}
	
	private int getConstantId() {
		return this.id * 2 + 1;
	}

	/**
	 * Generates JSON for a single note
	 * @param pos zero based position of the note
	 * @param note note value
	 * @return JSONObject
	 */
	private JSONObject generateNote(int pos, int note) {
		return new JSONObject()
		.put("signal", new JSONObject()
			.put("type", "virtual")
			.put("name", "signal-" + (char)(pos + 'A'))
		).put("count", note)
		.put("index", pos + 1);
	}
	
	/**
	 * Generates JSON for all notes in the combinator
	 * @return JSONObject
	 */
	private JSONObject generateNoteData() {
		JSONArray notes = new JSONArray();
		for(int i = 0; i < this.notes.length; i++) {
			if(this.notes[i] > 0) notes.put(generateNote(i, this.notes[i]));
		}
		return new JSONObject().put("filters", notes);
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
			).put("constant", this.tick)
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
	private JSONObject generateDeciderCombCon(Position pos) {
		int subID = getDeciderId();
		
		//create wire array
		JSONArray red1 = new JSONArray();
		JSONArray green1 = new JSONArray();
		JSONArray green2 = new JSONArray();
		
		//always make a connection to the previous Decider Combinator
		red1.put(Wire.from(subID - 2));
		
		//if we are last, we don't want to connect to the next Decider Combinator
		if(pos != Position.LAST)
			red1.put(Wire.to(subID + 2, 1));
		
		//always connect to previous Constant Combinator
		green1.put(Wire.from(subID - 1));
		
		//if we are first, we don't want to connect to previous Decider Combinator 
		//(that's the clock!)
		if(pos != Position.FIRST)
			green2.put(Wire.from(subID - 2));
		
		//if we are last, we don't want to connect to the next Decider Combinator
		if(pos != Position.LAST)
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
	public JSONObject generateConstantComb(int y) {
		return new JSONObject()
		.put("entity_number", getConstantId())
		.put("name", "constant-combinator")
		.put("position", Logic.generatePosition(0,y))
		.put("direction", 2)
		.put("control_behavior", generateNoteData())
		.put("connections", generateConstantCombCon());
	}

	/**
	 * Generates JSON data for the decider combinator
	 * @param id Id of the current object.
	 * @return JSONObject
	 */
	public JSONObject generateDeciderComb(int y, Position pos) {
		return new JSONObject()
		.put("entity_number", getDeciderId())
		.put("name", "decider-combinator")
		.put("position", Logic.generatePosition(1.5,y))
		.put("direction", 2)
		.put("control_behavior", generateDeciderData())
		.put("connections", generateDeciderCombCon(pos));
	}
}
