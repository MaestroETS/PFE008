import { useState } from 'react';

interface UseMaestroClientResult {
  loading: boolean;
  error: string | null;
  convert: (formData: FormData) => Promise<void>;
}

const useMaestroClient = (): UseMaestroClientResult => {
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  const apiUrl = process.env.BACKEND_URL || 'http://localhost:8080'; // Default value for the backend URL

  const checkBackendHealth = async (): Promise<boolean> => {
    try {
      const healthResponse = await fetch(`${apiUrl}/health`, { method: 'GET' });
      if (!healthResponse.ok) {
        throw new Error('Backend is not available');
      }
      return true;
    } catch (err) {
      setError('Backend is not available. Please try again later.');
      return false;
    }
  };

  const convert = async (formData: FormData): Promise<void> => {
    setLoading(true);
    setError(null);

    // Check if backend is alive before proceeding
    const isBackendAlive = await checkBackendHealth();
    if (!isBackendAlive) {
      setLoading(false);
      return;
    }

    try {
      const response = await fetch(`${apiUrl}/convert`, {
        method: 'POST',
        mode: 'cors',
        body: formData,
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || 'An error occurred');
      }

      const blob = await response.blob();

      const fileDownload = require('js-file-download');
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
