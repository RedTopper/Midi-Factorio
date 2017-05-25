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
import org.red.io.Midi;
import org.red.music.Note;

public class Main {
	
	public static final String VERSION = "0";
	
	public static void main(String[] args) throws Exception {
		List<Note> notes = Midi.parse("C:\\Users\\AJ\\Desktop\\factorio.mid");
		List<Combinator> combinators = new ArrayList<>();
		
		//try to place all notes in a combinator
		placingNotes: for(int n = 0; n < notes.size(); n++) {
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
		
		//Sort the combinators based on tick
		Collections.sort(combinators, new Comparator<Combinator>() {
			public int compare(Combinator o1, Combinator o2) {
				return o1.tick - o2.tick;
			}
		});
		
		//the main ROM
		JSONArray rom = new JSONArray();
		
		//build the main clock
		rom.put(Clock.generateFirstConstantComb());
		rom.put(Clock.generateFirstDeciderComb(1000));
		
		//build the combinators and print
		for(int i = 0; i < 10; i++){
			Combinator combinator = combinators.get(i);
			combinator.setId(i + 1);
			rom.put(combinator.generateConstantComb(i));
			if(i == 0)
				rom.put(combinator.generateDeciderComb(i, Position.FIRST));
			else if(i == combinators.size() - 1)
				rom.put(combinator.generateDeciderComb(i, Position.LAST));
			else 
				rom.put(combinator.generateDeciderComb(i, Position.MIDDLE));
			System.out.println(combinator);
		}
		
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
			.put("version", 64425361411l)
		);
		
		System.out.println();
		System.out.println(blueprint);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DeflaterOutputStream dos = new DeflaterOutputStream(baos);
        dos.write(blueprint.toString().getBytes());
        dos.flush();
        dos.close();
        System.out.println(VERSION + new String(Base64.getEncoder().encode(baos.toByteArray())));
		
		System.out.println();
		System.out.printf("Your song has %d notes and %d combinators!\n", notes.size(), combinators.size());
	}
}
