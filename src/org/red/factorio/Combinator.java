package org.red.factorio;

import org.red.music.Note;

public class Combinator {
	public final int tick;
	private final int[] notes = new int[15];
	
	public Combinator(int tick) {
		this.tick = tick;
	}
	
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append("At tick " + tick + " ");
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
		if(notes[note.track] > 0) return false;
		notes[note.track] = note.key;
		return true;
	}
}
