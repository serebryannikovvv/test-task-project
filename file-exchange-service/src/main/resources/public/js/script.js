(function(UI, Uploader) {
    let currentFile = null;

    function handleFile(file) {
        currentFile = file;
        UI.updateFilename(file);
        uploadCurrentFile();
    }

    function uploadCurrentFile() {
        if (!currentFile) return;

        UI.showProgress();

        Uploader.upload(
            currentFile,
            // onProgress: обновляем UI
            (percent, speed) => UI.updateProgress(percent, speed),

            // onSuccess: показываем результат
            (data) => UI.showResult(
                data.downloadLink || `${location.origin}/file/${data.fileId}`,
                resetApp
            ),

            // onError: показываем ошибку
            (msg) => UI.showError(msg, resetApp) // Перезапуск при ошибке - это просто reset
        );
    }

    function resetApp() {
        currentFile = null;
        UI.reset();
    }

    function initEventListeners() {
        // Drag & Drop
        UI.elements.uploadArea.addEventListener('click', () => UI.elements.fileInput.click());
        UI.elements.uploadArea.addEventListener('dragover', e => { e.preventDefault(); UI.elements.uploadArea.classList.add('dragover'); });
        UI.elements.uploadArea.addEventListener('dragleave', () => UI.elements.uploadArea.classList.remove('dragover'));

        UI.elements.uploadArea.addEventListener('drop', e => {
            e.preventDefault();
            UI.elements.uploadArea.classList.remove('dragover');
            if (e.dataTransfer.files[0]) handleFile(e.dataTransfer.files[0]);
        });

        // Выбор файла через кнопку
        UI.elements.fileInput.addEventListener('change', e => e.target.files[0] && handleFile(e.target.files[0]));

        // Инициализация UI
        UI.reset();
        UI.initTheme();
    }

    // Запускаем приложение
    document.addEventListener('DOMContentLoaded', initEventListeners);

})(UI, FileUploader);