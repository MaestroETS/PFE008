import sys
import os
from music21 import converter, midi, dynamics, volume

def apply_dynamics_to_notes(score):
    for part in score.parts:
        for element in part.flatten().notesAndRests:
            if element.isNote or element.isChord:
                element.volume.velocity = element.volume.velocity or 64

            dynamic_context = element.getContextByClass(dynamics.Dynamic)
            if dynamic_context is not None:
                volume_scalar = dynamic_context.volumeScalar
                velocity = int(volume_scalar * 127)
                if element.isNote:
                    element.volume.velocity = velocity
                elif element.isChord:
                    for note in element.notes:
                        note.volume.velocity = velocity

def print_note_details(score):
    for part in score.parts:
        for element in part.flatten().notesAndRests:
            if element.isNote:
                realized_volume = element.volume.getRealized()
                print(f"Note: {element.pitch}, Velocity: {element.volume.velocity}, Realized Volume: {realized_volume}")
            elif element.isChord:
                for note in element.notes:
                    realized_volume = note.volume.getRealized()
                    print(f"Chord Note: {note.pitch}, Velocity: {note.volume.velocity}, Realized Volume: {realized_volume}")

def print_part_details(score):
    for part in score.parts:
        print(f"Part: {part.id}")
        for element in part.flatten().notesAndRests:
            if element.isNote:
                print(f"Note: {element.pitch}, Duration: {element.quarterLength}, Velocity: {element.volume.velocity}")
            elif element.isChord:
                for note in element.notes:
                    print(f"Chord Note: {note.pitch}, Duration: {note.quarterLength}, Velocity: {note.volume.velocity}")

def mxl_to_midi(mxl_file, midi_file):
    score = converter.parse(mxl_file) #parse the MXL file using music21

    # print("Initial Part Details:")
    # print_part_details(score)

    apply_dynamics_to_notes(score)

    # print("Note Details after Applying Dynamics:")
    # print_note_details(score)

    #create a new MIDI file object from the score
    mf = midi.translate.music21ObjectToMidiFile(score)
    mf.open(midi_file, 'wb')
    mf.write()
    mf.close()

def main():
    if len(sys.argv) != 2:
        print("Usage: python MxlToMidi.py <path_to_mxl_file>")
        sys.exit(1)

    input_file = sys.argv[1]

    if not input_file.endswith('.mxl'):
        print("Error: The input file must be in '.mxl' format.")
        sys.exit(1)

    output_file = os.path.splitext(input_file)[0] + '.mid'

    try:
        mxl_to_midi(input_file, output_file)
        print(f"Successfully converted '{input_file}' to '{output_file}'")
    except Exception as e:
        print(f"An error occurred: {e}")
        sys.exit(1)

if __name__ == "__main__":
    main()
