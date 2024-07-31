import unittest
from music21 import converter, dynamics, note, stream, chord
import importlib.util
import sys
from pathlib import Path
import io
from unittest.mock import patch, MagicMock

main_path = Path(__file__).resolve().parents[4] / 'main'
sys.path.insert(0, str(main_path))
spec = importlib.util.spec_from_file_location("MxlToMidi", main_path / 'MxlToMidi.py')
MxlToMidi = importlib.util.module_from_spec(spec)
sys.modules["MxlToMidi"] = MxlToMidi
spec.loader.exec_module(MxlToMidi)

class TestMxlToMidi(unittest.TestCase):
    
    def setUp(self):
        self.scoreNotes = stream.Score()
        part1 = stream.Part(id='Part1')
        n1 = note.Note("C4")
        n1.volume.velocity = 89
        n2 = note.Note("E4")
        n2.volume.velocity = 127
        part1.append(n1)
        part1.append(n2)
        self.scoreNotes.append(part1)

        self.scoreChords = stream.Score()
        part2 = stream.Part(id='Part2')
        c1 = chord.Chord("C")
        c1.volume.velocity = 88
        c2 = chord.Chord("E")
        c2.volume.velocity = 126
        part2.append(c1)
        part2.append(c2)
        self.scoreChords.append(part2)


    def test_apply_dynamics_to_notes(self):
        MxlToMidi.apply_dynamics_to_notes(self.scoreNotes)
        
        for element in self.scoreNotes.parts[0].flatten().notesAndRests:
            if element.isNote:
                if element.pitch.name == 'C':
                    self.assertEqual(element.volume.velocity, 89)
                elif element.pitch.name == 'E':
                    self.assertEqual(element.volume.velocity, 127)

    def test_print_note_details(self):
        captured_output = io.StringIO()
        sys.stdout = captured_output
        
        MxlToMidi.apply_dynamics_to_notes(self.scoreNotes)
        MxlToMidi.print_note_details(self.scoreNotes)
        
        sys.stdout = sys.__stdout__
        output = captured_output.getvalue()

        self.assertEqual("Note: C4, Velocity: 89, Realized Volume: 0.7007874015748031\nNote: E4, Velocity: 127, Realized Volume: 1.0", output.rstrip())

    def test_print_part_details(self):
        captured_output = io.StringIO()
        sys.stdout = captured_output
        MxlToMidi.apply_dynamics_to_notes(self.scoreNotes)
        MxlToMidi.print_part_details(self.scoreNotes)
        
        sys.stdout = sys.__stdout__
        output = captured_output.getvalue()

        self.assertEqual("Part: Part1\nNote: C4, Duration: 1.0, Velocity: 89\nNote: E4, Duration: 1.0, Velocity: 127", output.rstrip())

    def test_apply_dynamics_to_notes_chords(self):
        MxlToMidi.apply_dynamics_to_notes(self.scoreChords)
        
        for element in self.scoreNotes.parts[0].flatten().notesAndRests:
            if element.isChord:
                if element.pitch.name == 'C':
                    self.assertEqual(element.volume.velocity, 88)
                elif element.pitch.name == 'E':
                    self.assertEqual(element.volume.velocity, 126)

    def test_print_note_details_chords(self):
        captured_output = io.StringIO()
        sys.stdout = captured_output
        
        MxlToMidi.apply_dynamics_to_notes(self.scoreChords)
        MxlToMidi.print_note_details(self.scoreChords)
        
        sys.stdout = sys.__stdout__
        output = captured_output.getvalue()
        print(output)
        self.assertEqual("Chord Note: C, Velocity: None, Realized Volume: 0.7086600000000001\nChord Note: E, Velocity: None, Realized Volume: 0.7086600000000001", output.rstrip())

    def test_print_part_details_chords(self):
        captured_output = io.StringIO()
        sys.stdout = captured_output
        MxlToMidi.apply_dynamics_to_notes(self.scoreChords)
        MxlToMidi.print_part_details(self.scoreChords)
        
        sys.stdout = sys.__stdout__
        output = captured_output.getvalue()
        print(output)
        self.assertEqual("Part: Part2\nChord Note: C, Duration: 1.0, Velocity: None\nChord Note: E, Duration: 1.0, Velocity: None", output.rstrip())

    @patch('music21.midi.MidiFile.open', new_callable=MagicMock)
    @patch('music21.midi.MidiFile.write', new_callable=MagicMock)
    @patch('music21.midi.MidiFile.close', new_callable=MagicMock)
    @patch('music21.converter.parse', new_callable=MagicMock)
    @patch('music21.midi.translate.music21ObjectToMidiFile', new_callable=MagicMock)
    def test_mxl_to_midi(self, mock_translate, mock_parse, mock_open, mock_write, mock_close):
        
        mock_parse.return_value = self.scoreNotes
        mock_midi_file = MagicMock()
        mock_translate.return_value = mock_midi_file

        MxlToMidi.mxl_to_midi('test.mxl', 'test.mid')

        mock_parse.assert_called_once_with('test.mxl')
        mock_translate.assert_called_once_with(self.scoreNotes)
        mock_midi_file.open.assert_called_once_with('test.mid', 'wb')
        mock_midi_file.write.assert_called_once()
        mock_midi_file.close.assert_called_once()


    @patch('music21.midi.MidiFile.open', new_callable=MagicMock)
    @patch('music21.midi.MidiFile.write', new_callable=MagicMock)
    @patch('music21.midi.MidiFile.close', new_callable=MagicMock)
    @patch('music21.converter.parse', new_callable=MagicMock)
    @patch('music21.midi.translate.music21ObjectToMidiFile', new_callable=MagicMock)
    def test_mxl_to_midi_chords(self, mock_translate, mock_parse, mock_open, mock_write, mock_close):
        
        mock_parse.return_value = self.scoreChords
        mock_midi_file = MagicMock()
        mock_translate.return_value = mock_midi_file

        MxlToMidi.mxl_to_midi('test.mxl', 'test.mid')

        mock_parse.assert_called_once_with('test.mxl')
        mock_translate.assert_called_once_with(self.scoreChords)
        mock_midi_file.open.assert_called_once_with('test.mid', 'wb')
        mock_midi_file.write.assert_called_once()
        mock_midi_file.close.assert_called_once()

unittest.main()