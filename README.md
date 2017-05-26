# Midi-Factorio

Converts a MIDI file into a factorio blueprint. The program is controled from the command line.

### About:

This particular MIDI to Factorio converter parses MIDIs based on tracks. That means each instrument gets its own speaker, rather than each note being simutaniously played. 

Therefore: 

|                              |    1 instrument   |    3 instruments    |
|------------------------------|-------------------|---------------------|
| **playing 1 note**           | uses 1 combinator | uses 1 combinator   |
| **playing 3 notes together** | uses 3 combinators| uses 3 combinators  |

etc...

This means you can configure the sound and volume of each instrument rather than the sound and volume of arbitrary notes. However, it can mean the generated blueprints are bigger.

In order to achieve the best compression, try splitting cords amung multiple instruments. In MuseScore, this would mean having a piano play a chord on 3 staves rathern than one. You can use ```edit > tools > explode``` to achieve this quickly.

### How to use this tool:

1. Download the jar
1. Run the file from the command line
1. Follow the directions in the terminal
1. The program will output the blueprint to be imported into factorio

### Automation tips:

You can pipe the settings you use into this program. Instead of typing into standard input, have the computer do it for you.

### Broken MIDIs:

Some MIDIs just can't be parsed. If you ever get an infinite loop of "Tempo set to xxxx", it's likely that MIDI does not work. You could try a few things to resolve this:

 * Import it into a MIDI editing program and re-export it
 * Find another, similar MIDI online
 * Edit the file with MuseScore or similar notation software, and re-export that
