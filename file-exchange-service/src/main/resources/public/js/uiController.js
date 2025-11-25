const UI = (function(Utils) {
    const elements = {
        uploadArea: document.getElementById('upload-area'),
        fileInput: document.getElementById('file-input'),
        filenameDisplay: document.getElementById('filename-display'),
        progressContainer: document.getElementById('progress-container'),
        progressFill: document.getElementById('progress-fill'),
        progressText: document.getElementById('progress-text'),
        speedText: document.getElementById('speed-text'),
        result: document.getElementById('result'),
        linkInput: document.getElementById('link-input'),
        copyBtn: document.getElementById('copy-btn'),
        newUploadBtn: document.getElementById('new-upload-btn'),
        errorDiv: document.getElementById('error'),
        errorMessage: document.getElementById('error-message'),
        themeToggle: document.getElementById('theme-toggle'),
    };

    function reset() {
        // ... (логика resetUI из исходника) ...
        elements.progressContainer.style.display = 'none';
        elements.result.style.display = 'none';
        elements.errorDiv.style.display = 'none';
        elements.filenameDisplay.textContent = '';
        elements.fileInput.value = '';
        elements.progressFill.style.width = '0%';
        elements.progressText.textContent = '0%';
        elements.speedText.textContent = '';
        elements.uploadArea.style.display = 'block';
    }

    // --- Публичные методы для UI ---

    function showProgress() {
        reset();
        elements.uploadArea.style.display = 'none';
        elements.progressContainer.style.display = 'block';
    }

    function updateProgress(percent, speed) {
        elements.progressFill.style.width = percent + '%';
        elements.progressText.textContent = percent + '%';
        elements.speedText.textContent = Utils.formatSpeed(speed);
    }

    function showResult(link, onNewUploadClick) {
        elements.progressContainer.style.display = 'none';
        elements.result.style.display = 'block';
        elements.linkInput.value = link;

        // Копирование (использование современного API)
        elements.copyBtn.onclick = async () => {
            try {
                await navigator.clipboard.writeText(link);
                elements.copyBtn.textContent = 'Скопировано!';
                elements.copyBtn.classList.add('copied');
                setTimeout(() => {
                    elements.copyBtn.textContent = 'Скопировать';
                    elements.copyBtn.classList.remove('copied');
                }, 2000);
            } catch {
                // Фоллбэк: лучше использовать модальное окно с инструкцией
                alert("Не удалось автоматически скопировать ссылку. Пожалуйста, скопируйте её вручную.");
            }
        };

        elements.newUploadBtn.onclick = onNewUploadClick;
    }

    function showError(msg, onRetryClick) {
        elements.progressContainer.style.display = 'none';
        elements.errorDiv.style.display = 'block';
        elements.errorMessage.textContent = msg;
        document.getElementById('retry-btn').onclick = onRetryClick;
    }

    function updateFilename(file) {
        elements.filenameDisplay.textContent = `${file.name} (${Utils.formatBytes(file.size)})`;
    }

    // --- Логика темы ---
    function initTheme() {
        const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
        if (prefersDark) document.body.classList.add('dark-theme');
        elements.themeToggle.textContent = document.body.classList.contains('dark-theme') ? '☀️' : '🌙';

        elements.themeToggle.addEventListener('click', () => {
            document.body.classList.toggle('dark-theme');
            elements.themeToggle.textContent = document.body.classList.contains('dark-theme') ? '☀️' : '🌙';
        });
    }

    return {
        elements: elements, // для доступа из App
        reset: reset,
        showProgress: showProgress,
        updateProgress: updateProgress,
        showResult: showResult,
        showError: showError,
        updateFilename: updateFilename,
        initTheme: initTheme
    };
})(Utils);