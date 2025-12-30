let toastId = 0;

export function showToast(message, type = 'info') {
    const container = document.getElementById('toast-container');
    if (!container) return;

    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.innerHTML = `
        <div>${message}</div>
    `;

    container.appendChild(toast);

    setTimeout(() => {
        toast.style.animation = 'slideOut 0.3s ease-out';
        setTimeout(() => toast.remove(), 300);
    }, 3000);
}

export function showLoading() {
    const spinner = document.getElementById('loading-spinner');
    if (spinner) {
        spinner.style.display = 'flex';
    }
}

export function hideLoading() {
    const spinner = document.getElementById('loading-spinner');
    if (spinner) {
        spinner.style.display = 'none';
    }
}

export function showModal(title, content, actions = []) {
    const container = document.getElementById('modal-container');
    if (!container) return;

    const actionsHtml = actions.map(action => `
        <button class="btn ${action.className || 'btn-secondary'}" data-action="${action.id}">
            ${action.label}
        </button>
    `).join('');

    container.innerHTML = `
        <div class="modal">
            <div class="modal-header">
                <h2 class="modal-title">${title}</h2>
                <button class="modal-close" data-action="close">&times;</button>
            </div>
            <div class="modal-body">
                ${content}
            </div>
            <div class="modal-footer">
                ${actionsHtml}
            </div>
        </div>
    `;

    container.classList.add('active');

    return new Promise((resolve) => {
        container.addEventListener('click', (e) => {
            if (e.target === container || e.target.dataset.action === 'close') {
                hideModal();
                resolve(null);
            }

            const action = actions.find(a => a.id === e.target.dataset.action);
            if (action) {
                if (action.handler) {
                    action.handler();
                }
                hideModal();
                resolve(action.id);
            }
        });
    });
}

export function hideModal() {
    const container = document.getElementById('modal-container');
    if (container) {
        container.classList.remove('active');
        container.innerHTML = '';
    }
}

export async function confirmDialog(message, title = 'Confirmation') {
    const result = await showModal(
        title,
        `<p>${message}</p>`,
        [
            { id: 'cancel', label: 'Annuler', className: 'btn-secondary' },
            { id: 'confirm', label: 'Confirmer', className: 'btn-danger' }
        ]
    );
    return result === 'confirm';
}

export function formatDate(dateString) {
    if (!dateString) return '';
    const date = new Date(dateString);
    return new Intl.DateTimeFormat('fr-FR', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    }).format(date);
}

export function formatTime(dateString) {
    if (!dateString) return '';
    const date = new Date(dateString);
    return new Intl.DateTimeFormat('fr-FR', {
        hour: '2-digit',
        minute: '2-digit'
    }).format(date);
}

export function truncateText(text, maxLength) {
    if (!text || text.length <= maxLength) return text;
    return text.substring(0, maxLength) + '...';
}

export function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}
