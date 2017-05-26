package org.red.io;

import java.util.List;

import org.red.music.Note;
import org.red.music.Sound;

public class Data {
	public final List<Note> notes;
	public final List<Sound> tracks;
	
	public Data(List<Note> notes, List<Sound> tracks) {
		this.notes = notes;
		this.tracks = tracks;
	}
}
