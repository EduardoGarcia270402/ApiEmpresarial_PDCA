import { useEffect, useState } from 'react';

type AlertType = 'error' | 'success';

interface Props {
  type: AlertType;
  message: string;
  onClose?: () => void;
  autoCloseMs?: number;
}

const styles: Record<AlertType, string> = {
  error: 'bg-red-50 border-red-400 text-red-700',
  success: 'bg-green-50 border-green-400 text-green-700',
};

export default function Alert({ type, message, onClose, autoCloseMs = 5000 }: Props) {
  const [visible, setVisible] = useState(true);

  useEffect(() => {
    if (autoCloseMs <= 0) return;
    const timer = setTimeout(() => {
      setVisible(false);
      onClose?.();
    }, autoCloseMs);
    return () => clearTimeout(timer);
  }, [autoCloseMs, onClose]);

  if (!visible) return null;

  return (
    <div
      role="alert"
      className={`flex items-center justify-between rounded-lg border px-4 py-3 text-sm ${styles[type]}`}
    >
      <span>{message}</span>
      {onClose && (
        <button
          onClick={() => {
            setVisible(false);
            onClose();
          }}
          className="ml-4 font-bold opacity-60 hover:opacity-100"
          aria-label="Cerrar alerta"
        >
          ×
        </button>
      )}
    </div>
  );
}
