import sys
import os
import json
from music21 import converter, midi, dynamics, tempo, stream
import xml.etree.ElementTree as xmlTree

def apply_dynamics_to_notes(score):
    for part in score.parts:
        for element in part.flat.notesAndRests:
            if element.isNote or element.isChord:
                dynamic_context = element.getContextByClass(dynamics.Dynamic)
                if dynamic_context:
                    velocity = int(dynamic_context.volumeScalar * 127)
                    element.volume.velocity = velocity if element.isNote else min(note.volume.velocity for note in element.notes)

def print_note_details(score):
    for part in score.parts:
        for element in part.flat.notesAndRests:
            if element.isNote:
                print(f"Note: {element.pitch}, Velocity: {element.volume.velocity}, Realized Volume: {element.volume.getRealized()}")
            elif element.isChord:
                for note in element.notes:
                    print(f"Chord Note: {note.pitch}, Velocity: {note.volume.velocity}, Realized Volume: {note.volume.getRealized()}")

def print_part_details(score):
    for part in score.parts:
        print(f"Part: {part.id}")
        for element in part.flat.notesAndRests:
            if element.isNote:
                print(f"Note: {element.pitch}, Duration: {element.quarterLength}, Velocity: {element.volume.velocity}")
            elif element.isChord:
                for note in element.notes:
                    print(f"Chord Note: {note.pitch}, Duration: {note.quarterLength}, Velocity: {note.volume.velocity}")

def mxl_to_midi(mxl_file, midi_file, custom_tempos=None):
    score = converter.parse(mxl_file)
    score = confirm_mxl_tempo(os.path.splitext(mxl_file)[0], score)

    apply_dynamics_to_notes(score)

    if custom_tempos:
        apply_custom_tempos(score, custom_tempos)

    mf = midi.translate.music21ObjectToMidiFile(score)
    mf.open(midi_file, 'wb')
    mf.write()
    mf.close()

def apply_custom_tempos(score, custom_tempos):
    try:
        tempo_marks = json.loads(custom_tempos)
    except json.JSONDecodeError as e:
        print(f"Error parsing custom tempos JSON: {e}")
        sys.exit(1)

    metronome_marks = {mark['measure']: tempo.MetronomeMark(number=mark['tempo'])
                       for mark in tempo_marks if mark.get('measure') is not None and mark.get('tempo') is not None}

    for part in score.parts:
        for measure_number, metronome_mark in metronome_marks.items():
            measure = part.measure(measure_number)
            if measure and not measure.getElementsByClass(tempo.MetronomeMark):
                measure.insert(0, metronome_mark)
                print(f"Inserted custom tempo {metronome_mark.number} at measure {measure_number} in part {part.id}")

def get_tempo_from_xml(xml_file):
    tempo_saved = []
    tree = xmlTree.parse(xml_file)
    root = tree.getroot()

    for measure in root.findall('.//measure'):
        measure_number = int(measure.get('number'))
        for direction in measure.findall('direction'):
            words_element = direction.find('.//words')
            if words_element is not None and words_element.text:
                words_text = words_element.text.strip()
                if '=' in words_text:
                    try:
                        tempo_value = int(words_text.split('=')[1].strip())
                        tempo_saved.append((measure_number, tempo_value))
                    except ValueError:
                        continue
                else:
                    tempo_value = convert_tempo_term_to_bpm(words_text)
                    if tempo_value:
                        tempo_saved.append((measure_number, tempo_value))
    return tempo_saved

def convert_tempo_term_to_bpm(term):
    tempo_terms_to_bpm = {
        24: ["larghissimo", "very, very slow", "very very slow", "extrêmement lent", "sehr breit"],
        35: ["grave", "very slow", "grave", "schwer", "solenne"],
        53: ["largo", "broadly", "large", "largement", "breit"],
        60: ["lento", "slowly", "lent", "langsam"],
        63: ["larghetto", "rather broadly", "assez large", "etwas breit", "zart"],
        71: ["adagio", "slow and stately", "à l'aise", "gemächlich"],
        94: ["andante", "at a walking pace", "allant", "gehend"],
        114: ["moderato", "moderately", "modéré", "mäßig"],
        120: ["allegretto", "moderately fast", "assez vite", "ein wenig schnell"],
        140: ["allegro", "fast, quickly, and bright", "vif", "schnell", "allegro moderato"],
        172: ["vivace", "lively and fast", "vif", "lebhaft"],
        188: ["presto", "very, very fast", "très rapide", "sehr schnell"],
    }

    term = term.lower()
    for bpm, terms in tempo_terms_to_bpm.items():
        if term in terms:
            return bpm
    return None

def confirm_mxl_tempo(base_path, score):
    tempo_saved = get_tempo_from_xml(base_path + ".xml")
    clear_initial_tempos(score)

    for measure_number, tempo_value in tempo_saved:
        metronome_mark = tempo.MetronomeMark(number=tempo_value)
        for part in score.parts:
            measure = part.measure(measure_number)
            if measure is not None:
                measure.insert(0, metronome_mark)

    return score

def clear_initial_tempos(score):
    for part in score.parts:
        for measure_number in (0, 1):
            measure = part.measure(measure_number)
            if measure:
                for element in measure.getElementsByClass(tempo.MetronomeMark):
                    measure.remove(element)

def list_tempos(score):
    tempo_list = []
    for part in score.parts:
        for measure in part.getElementsByClass('Measure'):
            for t in measure.getElementsByClass(tempo.MetronomeMark):
                tempo_list.append((part.id, measure.number, t.number))
    return tempo_list

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
