package org.red;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.red.factorio.Clock;
import org.red.factorio.Combinator;
import org.red.music.Note;

public class Generate {
	
	public static final long VERSION_NUMBER = 64425361411l;
	
	/**
	 * Takes a large list of notes and attempts to compress them into
	 * a reasonable amount of combinators.
	 * @param notes A list of notes
	 * @return The list of combinators
	 */
	public static List<Combinator> combinators(List<Note> notes) {
		List<Combinator> combinators = new ArrayList<>();
		
		//try to place all notes in a combinator
		placingNotes: 
		for(int n = 0; n < notes.size(); n++) {
			Note note = notes.get(n);
			
			//search for combinator of matching tick
			for(int c = 0; c < combinators.size(); c++) {
				Combinator combinator = combinators.get(c);
				if(combinator.tick == note.getGameTick()) {
					if(!combinator.setNote(note)) {
						
						//found a combinator with a note from the same track!
						//Try to place it in the next combinator.
						note.increaseGameTickOffset();
						n--;
					}
					continue placingNotes;
				}
			}
			Combinator newCombinator = new Combinator(note.getGameTick());
			newCombinator.setNote(note);
			combinators.add(newCombinator);
		}
		return combinators;
	}
	
	/**
	 * Generates the single clock to power it all.
	 * @param rom The entities array.
	 * @param combinators List of combinators
	 */
	public static void clock(JSONArray rom, List<Combinator> combinators) {
		rom.put(Clock.generateFirstConstantComb());
		rom.put(Clock.generateFirstDeciderComb(combinators.get(combinators.size() - 1).tick));
	}

	/**
	 * Generates JSON that contains an X and Y position
	 * @param x X cord
	 * @param y Y cord
	 * @return JSONObject
	 */
	public static JSONObject position(double x, double y) {
		return new JSONObject().put("x", x).put("y", y);
	}

	/**
	 * Generates JSON that contains a blueprint
	 * @param rom The ROM (entities) within the blueprint.
	 * @param file The file that was used to generate this blueprint
	 * @return JSONObject
	 */
	public static JSONObject blueprint(JSONArray rom, File file) {
		return new JSONObject()
		.put("blueprint", new JSONObject()
			.put("icons", new JSONArray()
				.put(new JSONObject()
					.put("signal", new JSONObject()
						.put("type", "item")
						.put("name", "programmable-speaker")
					).put("index", 1)
				)
			).put("entities", rom)
			.put("item", "blueprint")
			.put("label", (file.getName() + " music box").trim())
			.put("version", VERSION_NUMBER)
		);
	}
}
