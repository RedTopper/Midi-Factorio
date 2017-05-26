package org.red;

import java.util.List;

import org.json.JSONArray;
import org.red.factorio.Combinator;
import org.red.factorio.Line;
import org.red.factorio.Speaker;
import org.red.factorio.Substation;
import org.red.music.Sound;

public class Position {
	
	public static final int SUBSTATION_X = 6;
	public static final int SUBSTATION_Y = 18;
	public static final int SUBSTATION_OFFSET = 2;
	
	public static int combinators(JSONArray rom, int maxHeight, List<Combinator> combinators) {
		
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
			} else if(y < 0) {
				y = 0;
				x++;
				down = true;
			}
			
			System.out.println(combinator);
		}
		return x;
	}

	public static void speakers(JSONArray rom, int id, List<Sound> tracks) {
		for(int i = 0; i < tracks.size(); i++) {
			rom.put(Speaker.generateSpeaker(i, ++id, tracks.get(i)));
			rom.put(Speaker.generateLamp(i, ++id, tracks.get(i)));
		}
	}

	public static int substations(JSONArray rom, int maxWidth, int maxHeight, int id) {
		for(int x = 0; x <= (int)((double)maxWidth / (double)SUBSTATION_X + 0.5); x++) {
			for(int y = 0; y <= (int)((double)maxHeight / (double)SUBSTATION_Y + 0.5); y++) {
				rom.put(Substation.generate(++id, 
					(x * SUBSTATION_X) + SUBSTATION_OFFSET, (y * SUBSTATION_Y) + SUBSTATION_OFFSET));
			}
		}
		return id;
	}
}
