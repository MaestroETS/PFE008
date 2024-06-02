import MusicSheetUploadForm from "./pages/MusicSheetUploadForm";
import i18n from "./i18n/i18n";
import { I18nextProvider } from "react-i18next";

function App() {
  return (
    <I18nextProvider i18n={i18n}>
      <div className="App">
        <MusicSheetUploadForm />
      </div>
    </I18nextProvider>
  );
}

export default App;
