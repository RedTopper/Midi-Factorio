package org.red.io;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

import org.red.music.Note;
import org.red.music.Tempo;

public class Midi {
    public static final int NOTE_ON = 0x90;
    public static final int NOTE_OFF = 0x80;
    public static final String[] NOTE_NAMES = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};

    public static List<Note> parse(String file) throws Exception {
    	List<Tempo> tempo = new ArrayList<>(); 
    	List<Note> notes = new ArrayList<>();
        Sequence sequence = MidiSystem.getSequence(new File(file));
    	long ticksPerQN = sequence.getResolution();
        System.out.println("Sequence resolution: " + ticksPerQN);
        System.out.println();

        //each track will represent a different speaker
        int trackNumber = 0;
        for (Track track :  sequence.getTracks()) {
            System.out.println("Track " + trackNumber + ": size = " + track.size());
            System.out.println();
            
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
                    note = new Note(noteName,key,octave,velocity, trackNumber);
                    
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
                
                if(note != null) {
                	note.setTime(seconds, currentTick, deltaTick);
                	System.out.println(note);
                	notes.add(note);
                }
            }
            trackNumber++;
            System.out.println();
        }
        return notes;
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