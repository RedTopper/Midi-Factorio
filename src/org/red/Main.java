package org.red;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.zip.DeflaterOutputStream;

import org.json.JSONArray;
import org.json.JSONObject;
import org.red.factorio.Clock;
import org.red.factorio.Combinator;
import org.red.factorio.Substation;
import org.red.io.Midi;
import org.red.music.Note;

public class Main {
	
	public static final String VERSION = "0";
	public static final long VERSION_NUMBER = 64425361411l;
	public static final int SUBSTATION_X = 6;
	public static final int SUBSTATION_Y = 18;
	public static final int SUBSTATION_OFFSET = 2;
	
	public static void main(String[] args) throws Exception {
		
		//parse the song
		List<Note> notes = Midi.parse("C:\\Users\\AJ\\Desktop\\Factorio.mid");
		
		//create combinators
		List<Combinator> combinators = createCombinators(notes);

		//Sort the combinators based on tick
		Collections.sort(combinators, new Comparator<Combinator>() {
			public int compare(Combinator o1, Combinator o2) {
				return o1.tick - o2.tick;
			}
		});
		
		//create ROM
		JSONArray rom = new JSONArray();
		
		//build the main clock
		rom.put(Clock.generateFirstConstantComb());
		rom.put(Clock.generateFirstDeciderComb(combinators.get(combinators.size() - 1).tick));
		
		//position array of giant combinators
		int maxHeight = (int) Math.sqrt(combinators.size()) * 2;
		int maxWidth = positionCombinators(rom, maxHeight, combinators);
		
		//position substation within ROM
		int id = positionSubstations(rom, maxHeight, maxWidth, combinators.get(combinators.size() - 1).getDeciderId());
		
		
		
		//build the blueprint
		JSONObject blueprint = new JSONObject()
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
			.put("label", "SONG NAME GOES HERE PLS REMEMB")
			.put("version", VERSION_NUMBER)
		);
		
		//print raw blueprint
		System.out.println();
		System.out.println(blueprint);
		
		//compress blueprint
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DeflaterOutputStream dos = new DeflaterOutputStream(baos);
        dos.write(blueprint.toString().getBytes());
        dos.flush();
        dos.close();
        System.out.println(VERSION + new String(Base64.getEncoder().encode(baos.toByteArray())));
		
		System.out.println();
		System.out.printf("Your song has %d notes and %d combinators!\n", notes.size(), combinators.size());
	}
	
	
	public static List<Combinator> createCombinators(List<Note> notes) {
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
	
	public static int positionCombinators(JSONArray rom, int maxHeight, List<Combinator> combinators) {
		
		//positioning variables
		boolean down = true;
		int x = 0; 
		int y = 0;
		
		//build the combinators and print
		for(int i = 0; i < combinators.size(); i++){
			Combinator combinator = combinators.get(i);
			combinator.setId(i + 1);
			
			//put the constant combinator
			rom.put(combinator.generateConstantComb(x, y));
			
			//put the decider combinator
			if(i == 0)
				rom.put(combinator.generateDeciderComb(x, y, Line.FIRST));
			else if(i == combinators.size() - 1)
				rom.put(combinator.generateDeciderComb(x, y, Line.LAST));
			else 
				rom.put(combinator.generateDeciderComb(x, y, Line.MIDDLE));
			
			//check if a substation goes here
			if(y % SUBSTATION_Y == SUBSTATION_OFFSET && x % SUBSTATION_X == SUBSTATION_OFFSET) {
				if(down) {
					y = y + 2;
				} else {
					y = y - 2;
				}
			}
			
			//increase x
			if(down) {
				y++;
			} else {
				y--;
			}
			
			//check bounds
			if(y >= maxHeight) {
				y = maxHeight - 1;
				x++;
				down = false;
			}
			if(y < 0) {
				y = 0;
				x++;
				down = true;
			}
			
			System.out.println(combinator);
		}
		return y;
	}

	private static int positionSubstations(JSONArray rom, int maxHeight, int maxWidth, int id) {
		for(int x = 0; x < (maxWidth / SUBSTATION_X) + 1; x++) {
			for(int y = 0; y < (maxHeight / SUBSTATION_Y) + 1; y++) {
				rom.put(Substation.generate(++id, 
					(x * SUBSTATION_X) + SUBSTATION_OFFSET, (y * SUBSTATION_Y) + SUBSTATION_OFFSET));
			}
		}
		return id;
	}
}
