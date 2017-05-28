package org.red;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.zip.DeflaterOutputStream;

import org.json.JSONArray;
import org.json.JSONObject;
import org.red.factorio.Combinator;
import org.red.io.Data;
import org.red.io.Midi;
import org.red.music.Note;
import org.red.music.Sound;

public class Main {
	
	public static final String VERSION = "0";
	
	public static void main(String[] args) throws Exception {
		
		//Open scanner
        Scanner reader = new Scanner(System.in);
		
		//file?
        System.err.println("What midi file do you want to parse?");
        System.err.print("[file] > ");
        File file = new File(reader.nextLine());
        
        //speakers?
        System.err.println("Also include speakers?");
        System.err.println("[y/n] (Y)");
        boolean speakers = !reader.nextLine().trim().equals("n");
        
        //parse the song
		Data data = Midi.parse(reader, file);
		
		List<Note> notes = data.notes;
		List<Sound> tracks = data.tracks;
		
		//create combinators
		List<Combinator> combinators = Generate.combinators(notes);

		//Sort the combinators based on tick
		Collections.sort(combinators, new Comparator<Combinator>() {
			public int compare(Combinator o1, Combinator o2) {
				return o1.tick - o2.tick;
			}
		});
		
		//create ROM
		JSONArray rom = new JSONArray();
		
		//build the main clock
		Generate.clock(rom, combinators);
		
		//position array of giant combinators
		int maxHeight = (int) Math.sqrt(combinators.size()) * 2;
		int maxWidth = Position.combinators(rom, maxHeight, combinators);
		
		//position substation within ROM
		int id = Position.substations(rom, maxWidth, maxHeight, combinators.get(combinators.size() - 1).getDeciderId());
		
		//place speakers
		
		if(speakers) Position.speakers(rom, id, tracks);
		
		//build the blueprint
		JSONObject blueprint = Generate.blueprint(rom, file);
		
		//print raw blueprint
		System.out.println();
		System.out.println("Blueprint: ");
		System.out.println("(hidden)");
		//System.out.println(blueprint);
		
		//compress blueprint
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DeflaterOutputStream dos = new DeflaterOutputStream(baos);
        dos.write(blueprint.toString().getBytes());
        dos.flush();
        dos.close();
        System.out.println();
        System.out.println("Blueprint String: ");
        System.out.println(VERSION + new String(Base64.getEncoder().encode(baos.toByteArray())));
		
        //print some neat statistics
		System.out.println();
		System.out.println("Stats: ");
		System.out.printf("Your song has %d notes, %d combinators, and %d tracks!\n", notes.size(), combinators.size(), tracks.size());
		
		reader.close();
	}
}
