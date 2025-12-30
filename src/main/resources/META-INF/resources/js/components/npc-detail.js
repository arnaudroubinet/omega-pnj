import { getNpc } from '../api/npc-config-api.js';
import { formatDate, showToast, showLoading, hideLoading } from './ui-helpers.js';

export async function render(container, npcId) {
    try {
        showLoading();
        const npc = await getNpc(npcId);
        hideLoading();

        container.innerHTML = `
            <div class="section-header">
                <h2 class="section-title">${escapeHtml(npc.name)}</h2>
                <div style="display: flex; gap: 8px;">
                    <button class="btn btn-secondary" onclick="window.location.hash = '/npc/${npc.npcId}/edit'">
                        Éditer
                    </button>
                    <button class="btn btn-primary" onclick="window.location.hash = '/interact/${npc.npcId}'">
                        Discuter
                    </button>
                    <button class="btn btn-secondary" onclick="window.location.hash = '/'">
                        Retour
                    </button>
                </div>
            </div>

            <div class="detail-grid">
                <div class="detail-item">
                    <div class="detail-label">Identifiant</div>
                    <div class="detail-value">${escapeHtml(npc.npcId)}</div>
                </div>

                ${npc.occupation ? `
                    <div class="detail-item">
                        <div class="detail-label">Occupation</div>
                        <div class="detail-value">${escapeHtml(npc.occupation)}</div>
                    </div>
                ` : ''}

                ${npc.backstory ? `
                    <div class="detail-item">
                        <div class="detail-label">Histoire</div>
                        <div class="detail-value">${escapeHtml(npc.backstory)}</div>
                    </div>
                ` : ''}

                ${npc.personality ? `
                    <div class="detail-item">
                        <div class="detail-label">Personnalité</div>
                        <div class="detail-value">${escapeHtml(npc.personality)}</div>
                    </div>
                ` : ''}

                ${npc.goals ? `
                    <div class="detail-item">
                        <div class="detail-label">Objectifs</div>
                        <div class="detail-value">${escapeHtml(npc.goals)}</div>
                    </div>
                ` : ''}

                ${npc.fears ? `
                    <div class="detail-item">
                        <div class="detail-label">Peurs</div>
                        <div class="detail-value">${escapeHtml(npc.fears)}</div>
                    </div>
                ` : ''}

                <div class="detail-item">
                    <div class="detail-label">Créé le</div>
                    <div class="detail-value">${formatDate(npc.createdAt)}</div>
                </div>

                ${npc.updatedAt && npc.updatedAt !== npc.createdAt ? `
                    <div class="detail-item">
                        <div class="detail-label">Dernière modification</div>
                        <div class="detail-value">${formatDate(npc.updatedAt)}</div>
                    </div>
                ` : ''}
            </div>
        `;
    } catch (error) {
        hideLoading();
        showToast(error.message || 'Erreur lors du chargement du PNJ', 'error');
        window.location.hash = '/';
    }
}

function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}
