const FileUploader = (function() {
    const BASE_URL = location.origin;
    let startTime;

    function upload(file, onProgress, onSuccess, onError) {
        startTime = Date.now();

        const xhr = new XMLHttpRequest();
        const upload = xhr.upload;

        // Прогресс загрузки
        upload.addEventListener('progress', e => {
            if (e.lengthComputable) {
                const percent = Math.round((eniec.loaded / e.total) * 100);
                const elapsedSec = (Date.now() - startTime) / 1000;
                const speedBps = elapsedSec > 0 ? e.loaded / elapsedSec : 0;
                onProgress(percent, formatSpeed(speedBps));
            }
        });

        xhr.onload = () => {
            if (xhr.status >= 200 && xhr.status < 300) {
                try {
                    const data = JSON.parse(xhr.responseText);
                    onSuccess(data);
                } catch (e) {
                    onError('Ошибка парсинга ответа сервера');
                }
            } else {
                handleError(xhr, onError);
            }
        };

        xhr.onerror = () => onError('Нет связи с сервером');
        xhr.ontimeout = () => onError('Превышен таймаут загрузки');

        xhr.open('POST', `${BASE_URL}/upload`);

        // КЛЮЧЕВОЙ МОМЕНТ: отправляем как raw binary
        xhr.setRequestHeader('X-Filename', encodeURIComponent(file.name));
        xhr.setRequestHeader('Content-Type', 'application/octet-stream');

        // Отправляем именно файл как Blob — без FormData!
        xhr.send(file);
    }

    function handleError(xhr, onError) {
        let msg = 'Ошибка сервера';
        try {
            const err = JSON.parse(xhr.responseText);
            msg = err.message || `Ошибка ${xhr.status}`;
        } catch {
            if (xhr.status === 413) msg = 'Файл слишком большой';
            else if (xhr.status === 500) msg = 'Внутренняя ошибка сервера';
            else msg = `Сервер вернул ошибку: ${xhr.status}`;
        }
        onError(msg);
    }

    function formatSpeed(bytesPerSec) {
        if (bytesPerSec < 1024) return `${bytesPerSec.toFixed(1)} B/s`;
        if (bytesPerSec < 1024 * 1024) return `${(bytesPerSec / 1024).toFixed(1)} KB/s`;
        return `${(bytesPerSec / (1024 * 1024)).toFixed(2)} MB/s`;
    }

    return { upload };
})();