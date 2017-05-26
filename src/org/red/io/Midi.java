package org.red.io;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

import org.red.music.Note;
import org.red.music.Sound;
import org.red.music.Tempo;

public class Midi {
    public static final int NOTE_ON = 0x90;
    public static final int NOTE_OFF = 0x80;
    public static final String[] NOTE_NAMES = 
    	{"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};
    
    public static final String[] TRACK_NAMES = 
    	{"0","1","2","3","4","5","6","7","8","9","A","B","C","D","E","F","G",
    	"H","I","J","K","L","M","N","O","P","Q","R","S","U","V","W","X","Y","Z"};
    
    public static final String[] PERCUSSION = 
    	{"1) Kick 1",
    	 "2) Kick 2",
    	 "3) Snare 1",
    	 "4) Snare 2",
    	 "5) Snare 3",
    	 "6) Hi-Hat 1",
    	 "7) Hi-Hat 2",
    	 "8) Fx",
    	 "9) High Q",
    	 "10) Percussion 1",
    	 "11) Percussion 2",
    	 "12) Crash",
    	 "13) Reverse Cymbal",
    	 "14) Clap",
    	 "15) Shaker",
    	 "16) Cowbell",
    	 "17) Triangle"};
    
	public static final int BASE_OFFSET = -40;

    public static Data parse(Scanner reader, File file) throws Exception {
    	List<Tempo> tempo = new ArrayList<>();
    	List<Note> notes = new ArrayList<>();
    	List<Sound> tracks = new ArrayList<>();
        Sequence sequence = MidiSystem.getSequence(file);
    	long ticksPerQN = sequence.getResolution();
    	
    	//verify if tracks can fit!
    	if(sequence.getTracks().length > TRACK_NAMES.length) throw new Exception("Too many tracks in song!");
    	
    	//show sequencing resolution
        System.out.println("Sequence resolution: " + ticksPerQN);
        System.out.println();

        //each track will represent a different speaker
        int trackNumber = 0;
        for (Track track :  sequence.getTracks()) {
        	
        	//offsets
        	int offset = BASE_OFFSET;
        	boolean percussion = false; 
        	
            System.out.println("Track " + trackNumber + ": size = " + track.size());
            System.out.println();
            
            //get offset and staff information from user
            System.err.println("Is track " + trackNumber + " percussion?");
            System.err.print("[y/n] (N) > ");
            if(reader.nextLine().equals("y")) percussion = true;
            if(percussion) {
            	System.err.println();
            	for(String percussive : PERCUSSION) System.err.println(percussive);
            	System.err.println("Which percussion instrument?");
            	System.err.println("[1-" + PERCUSSION.length + "] > ");
            	offset = 0;
            	while(offset == 0) {
            		offset = getInt(reader, 1, PERCUSSION.length);
            	}
            } else {
            	System.err.println();
            	System.err.println("How much should this instrument be offset?");
            	System.err.println("(12 will shift the instrument up 1 octave)");
            	System.err.println("[-100 - 100] (0) > ");
            	int shift = -101;
            	while(shift == -101) {
            		shift = getInt(reader, -100, 100);
            	}
            	offset += shift;
            }
            
            //add track
            tracks.add(new Sound(TRACK_NAMES[trackNumber], percussion));
            
            //reset time
            long lastTick = 0;
            long currentUSPerQN = 0;
            double seconds = 0;
            
            //tempo events are only given once, but we parse via track, not note.
            int tempoChange = 0;
            
            //each note must be converted to the correct tick
            for (int i=0; i < track.size(); i++) { 
                Tempo time = null;
                MidiEvent event = track.get(i);
                MidiMessage message = event.getMessage();
                Note note = null;
                long currentTick = event.getTick();
                
                //parse note
                if (message instanceof ShortMessage) {
                    ShortMessage sm = (ShortMessage) message;
                    
                    //all we care about is notes turning on, as factorio cannot set a duration of a note.
                    if (sm.getCommand() != NOTE_ON) continue;
                    
                    //a velocity of zero is synonymous to note off, so ignore it as well.
                    int velocity = sm.getData2();
                    if(velocity == 0) continue;
                    
                    //get note info
                    int key = sm.getData1();
                    int octave = (key / 12)-1;
                    int noteVal = key % 12;
                    String noteName = NOTE_NAMES[noteVal];
                    
                    //actually set offsets
                    if(percussion) {
                    	key = offset;
                    } else {
                    	key += offset;
                    }
                    
                    note = new Note(noteName,TRACK_NAMES[trackNumber],key,octave,velocity);
                    
                //parse tempo change
                } else if(message instanceof MetaMessage){
                    MetaMessage mm = (MetaMessage) message;
                    
                    //only care about tempo changes
                    if(mm.getMessage()[1] != 0x51) continue;
                    time = new Tempo(currentTick, getUSPerQN(mm.getMessage()));
                    tempo.add(time);
                    
                //wasn't tempo or note
                } else continue;
                
                //if we are not on the first pass (where tempo changes are present
                //update tempo instead of note
                if(trackNumber > 0 && tempoChange < tempo.size() && tempo.get(tempoChange).tick <= currentTick) {
                	time = tempo.get(tempoChange);
                	currentTick = time.tick;
                	i--; //re-run the same note
                	tempoChange++; //go to next tempo
                	message = null;
                }
                
                //update timing information
                //if we got this far, we either received a tempo or a beat;
                long deltaTick = currentTick - lastTick;
                double deltaSecond = 0;
                if(currentUSPerQN != 0 && deltaTick != 0) {
                	double TickPerUS = (double)ticksPerQN / (double)currentUSPerQN;
                	deltaSecond = (double)deltaTick / (double)TickPerUS / 1000000d;
                }
                lastTick = currentTick;
                seconds += deltaSecond;
                
                //change current tempo AFTER we calculate deltas
                if(time != null) {
                	System.out.printf("Tempo set to %d at second %.2f!\n", time.tempo, seconds);
                	currentUSPerQN = time.tempo;
                	continue;
                }
                
                //add the note
                if(note != null) {
                	note.setTime(seconds, currentTick, deltaTick);
                	System.out.println(note);
                	notes.add(note);
                }
            }
            trackNumber++;
            System.out.println();
        }
        reader.close();
        return new Data(notes, tracks);
    }
    
    private static int getInt(Scanner reader, int min, int max) {
		try {
			String in = reader.nextLine();
			int number = 0;
			if(!in.trim().equals("")) number = Integer.parseInt(in);
			if(number >= min && number <= max) return number;
			System.err.println("Must be a number between 1 and " + max + "!");
			return min - 1;
		} catch (NumberFormatException e) {
			System.err.println("Must be a number!");
			return min - 1;
		}
	}

	public static int getUSPerQN(byte[] message) {
    	byte[] bytes = new byte[4];
    	
    	//as per the MIDI specification
    	bytes[0] = 0;
    	bytes[1] = message[3];
    	bytes[2] = message[4];
    	bytes[3] = message[5];
    	return ByteBuffer.wrap(bytes).getInt();
    }
}