import sys
import os
from music21 import converter, midi, dynamics, volume, tempo, stream
import xml.etree.ElementTree as xmlTree

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
    score = converter.parse(mxl_file)  # parse the MXL file using music21

    # Check the type of score
    print(f"Type of score after parsing: {type(score)}")

    score = confirmMxlTempo(os.path.splitext(mxl_file)[0], score)
    print("Tempo confirmed")

    apply_dynamics_to_notes(score)

    # Create a new MIDI file object from the score
    mf = midi.translate.music21ObjectToMidiFile(score)
    mf.open(midi_file, 'wb')
    mf.write()
    mf.close()

def GetTempoFromXML(xml_file):
    print("Getting tempo...")
    tempoSaved = []

    # Load and parse the XML file
    tree = xmlTree.parse(xml_file)
    root = tree.getroot()

    # Iterate through each 'measure' element
    for measure in root.findall('.//measure'):
        measureNumber = measure.get('number')

        # Iterate through each 'direction' element in the measure
        for direction in measure.findall('direction'):
            wordsElement = direction.find('.//words')
            if wordsElement is not None:
                wordsText = wordsElement.text
                if wordsText and ('J =' in wordsText or 'J. =' in wordsText):
                    # Extract the value after 'J =' or 'J. ='
                    try:
                        tempoValue = int(wordsText.split('=')[1].strip())
                        print(f"Measure Number: {measureNumber}, J Value: {tempoValue}")
                        tempoSaved.append((int(measureNumber), tempoValue))
                    except Exception as e:
                        print(f"Error parsing J value in measure {measureNumber}: {e}")

    return tempoSaved

def confirmMxlTempo(base_path, score):
    tempoSaved = GetTempoFromXML(base_path + ".xml")

    # Clear initial tempos in measure 1
    clearInitialTempos(score)

    # Iterate through the list of tuples
    for measure_number, tempo_value in tempoSaved:
        # Create a MetronomeMark object with the specified tempo
        print(f"Creating MetronomeMark for measure {measure_number} with tempo {tempo_value}")
        metronome_mark = tempo.MetronomeMark(number=tempo_value)

        # Iterate through all parts
        for part in score.parts:
            print(f"Processing part {part.id} for measure {measure_number}")
            try:
                # Get the specified measure
                measure = part.measure(measure_number)
                print(f"Got measure {measure_number} in part {part.id}: {measure}")

                if measure is not None:
                    # Insert the MetronomeMark at the beginning of the measure
                    print(f"Inserting MetronomeMark at measure {measure_number} in part {part.id}")
                    measure.insert(0, metronome_mark)
                    print(f"Inserted MetronomeMark at measure {measure_number} in part {part.id}")
                else:
                    print(f"Measure {measure_number} not found in part {part.id}")
            except Exception as e:
                print(f"An error occurred in part {part.id}, measure {measure_number}: {e}")

    print("end of insert")
    list_tempos(score)
    return score

def clearInitialTempos(score):
    for part in score.parts:
        # Remove initial tempos in measure 1
        measure_1 = part.measure(1)
        if measure_1 is not None:
            for element in measure_1.getElementsByClass(tempo.MetronomeMark):
                measure_1.remove(element)

def listTempos(score):
    tempoList = []
    for part in score.parts:
        print(f"Part: {part.id}")
        for measure in part.getElementsByClass('Measure'):
            tempos = measure.getElementsByClass(tempo.MetronomeMark)
            if tempos:
                for t in tempos:
                    tempoList.append((part.id, measure.number, t.number))
                    print(f"Measure {measure.number}: Tempo {t.number}")
            else:
                print(f"Measure {measure.number}: No tempo marking")
    return tempoList

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
