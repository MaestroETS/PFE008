import sys
import os
import json
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

def mxl_to_midi(mxl_file, midi_file, custom_tempos=None):
    score = converter.parse(mxl_file)  # parse the MXL file using music21

    # Apply existing tempo processing
    score = confirmMxlTempo(os.path.splitext(mxl_file)[0], score)
    print("Tempo confirmed from MusicXML.")

    # Apply dynamics and other processes
    apply_dynamics_to_notes(score)

    # Apply custom tempos provided by the user
    if custom_tempos:
        apply_custom_tempos(score, custom_tempos)
        print("Custom tempos applied where no existing tempos found.")

    # Create a new MIDI file object from the score
    mf = midi.translate.music21ObjectToMidiFile(score)
    mf.open(midi_file, 'wb')
    mf.write()
    mf.close()

def apply_custom_tempos(score, custom_tempos):
    # Parse the JSON string into a list of tempo objects
    try:
        tempo_marks = json.loads(custom_tempos)
    except json.JSONDecodeError as e:
        print(f"Error parsing custom tempos JSON: {e}")
        sys.exit(1)
    
    for mark in tempo_marks:
        measure_number = mark.get('measure')
        tempo_value = mark.get('tempo')
        
        # Skip if either measure_number or tempo_value is None
        if measure_number is None or tempo_value is None:
            continue
        
        metronome_mark = tempo.MetronomeMark(number=tempo_value)
        
        for part in score.parts:
            measure = part.measure(measure_number)
            if measure is not None:
                existing_tempos = measure.getElementsByClass(tempo.MetronomeMark)
                if not existing_tempos:
                    measure.insert(0, metronome_mark)
                    print(f"Inserted custom tempo {tempo_value} at measure {measure_number} in part {part.id}")



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
                if wordsText:
                    # Check if 'J =' or 'J. =' is in the wordsText
                    if 'J =' in wordsText or 'J=' in wordsText or 'J. =' in wordsText or 'J.=' in wordsText:
                        try:
                            tempoValue = int(wordsText.split('=')[1].strip())
                            print(f"Measure Number: {measureNumber}, J Value: {tempoValue}")
                            tempoSaved.append((int(measureNumber), tempoValue))
                        except Exception as e:
                            print(f"Error parsing J value in measure {measureNumber}: {e}")
                    else:
                        # Check for tempo word
                        tempoWord = wordsText.split('(')[0].strip() if '(' in wordsText else wordsText.strip()
                        tempoValue = convert_tempo_term_to_bpm(tempoWord)
                        if tempoValue:
                            print(f"Measure Number: {measureNumber}, Word Value: {tempoValue}")
                            tempoSaved.append((int(measureNumber), tempoValue))


    return tempoSaved

def convert_tempo_term_to_bpm(term):

    tempo_terms_to_bpm = {
        24: ["larghissimo", "very, very slow", "very very slow", "extrêmement lent", "sehr breit"],
        35: ["grave", "very slow", "grave", "schwer", "solenne"],
        53: ["largo", "broadly", "large", "largement", "breit"],
        54: ["lentissimo", "slow", "très lent", "sehr langsam", "adagissimo"],
        59: ["adagissimo", "rather slowly", "lentement modéré", "sehr ruhig"],
        60: ["lento", "slowly", "lent", "langsam"],
        63: ["larghetto", "rather broadly", "assez large", "etwas breit", "zart"],
        71: ["adagio", "slow and stately", "à l'aise", "gemächlich"],
        74: ["adagetto", "slower than andante", "assez vite", "ziemlich ruhig"],
        80: ["tranquillo", "tranquil", "tranquille", "ruhig", "adagio"],
        94: ["andante", "at a walking pace", "allant", "gehend"],
        98: ["andantino", "slightly faster than andante", "un peu allant", "etwas gehend"],
        84: ["marcia moderato", "moderately", "in the manner of a march", "modérément", "mäßig"],
        102: ["andante moderato", "between andante and moderato", "très modéré", "mäßig"],
        114: ["moderato", "moderately", "modéré", "mäßig"],
        120: ["allegretto", "moderately fast", "assez vite", "ein wenig schnell"],
        120: ["allegro moderato", "close to but not quite allegro", "allègrement", "vite", "fröhlich", "lustig"],
        140: ["allegro", "fast, quickly, and bright", "vif", "schnell", "allegro moderato"],
        172: ["vivace", "lively and fast", "vif", "lebhaft"],
        176: ["vivacissimo", "very fast and lively", "extrêmement vif", "sehr rasch"],
        168: ["allegrissimo", "very fast", "très vite", "geschwind"],
        188: ["presto", "very, very fast","very very fast", "très rapide", "sehr schnell"],
        200: ["prestissimo", "even faster than presto", "extrêmement rapide", "äußerst schnell"],
    }

    term = term.lower()
    for bpm, terms in tempo_terms_to_bpm.items():
        if term in terms:
            return bpm
    #print(f"Warning: Tempo term '{term}' not found in the conversion table. Keeping current tempo: {current_bpm} BPM.")
    #return current_bpm

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
    listTempos(score)
    return score

def clearInitialTempos(score):
    for part in score.parts:
        # Remove initial tempos in measure 0
        measure_0 = part.measure(0)
        if measure_0 is not None:
            for element in measure_0.getElementsByClass(tempo.MetronomeMark):
                measure_0.remove(element)

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
    if len(sys.argv) < 2:
        print("Usage: python MxlToMidi.py <path_to_mxl_file> [<custom_tempos_json>]")
        sys.exit(1)

    input_file = sys.argv[1]
    custom_tempos = sys.argv[2] if len(sys.argv) > 2 else None

    output_file = os.path.splitext(input_file)[0] + '.mid'

    try:
        mxl_to_midi(input_file, output_file, custom_tempos)
        print(f"Successfully converted '{input_file}' to '{output_file}'")
    except Exception as e:
        print(f"An error occurred: {e}")
        sys.exit(1)

if __name__ == "__main__":
    main()
