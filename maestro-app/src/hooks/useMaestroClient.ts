import { useState } from 'react';

interface UseMaestroClientResult {
  loading: boolean;
  error: string | null;
  convert: (formData: FormData) => Promise<void>;
}

const useMaestroClient = (): UseMaestroClientResult => {
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  const convert = async (formData: FormData): Promise<void> => {
    setLoading(true);
    setError(null);

    try {
      const response = await fetch('http://localhost:8080/convert', {
        method: 'POST',
        mode: 'cors',
        body: formData,
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || 'An error occurred');
      }
      const blob = await response.blob();

      var fileDownload = require('js-file-download');
      fileDownload(blob, `${formData.get("midiFileName")?.toString()}.mid` ?? "myFile.mid");

    } catch (err) {
      setError((err as Error).message);
    } finally {
      setLoading(false);
    }
  };

  return { loading, error, convert };
};

export default useMaestroClient;
