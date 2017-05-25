package red;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import factorio.Combinator;
import io.Midi;
import music.Note;

public class Main {
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
		
		Collections.sort(combinators, new Comparator<Combinator>() {
			public int compare(Combinator o1, Combinator o2) {
				return o1.tick - o2.tick;
			}
		});
		for(Combinator combinator : combinators) System.out.println(combinator);
		
		System.out.println();
		System.out.printf("Your song has %d notes and %d combinators!\n", notes.size(), combinators.size());
	}
}
