import { getState } from '../state.js';
import { deleteNpc } from '../api/npc-config-api.js';
import { truncateText } from './ui-helpers.js';
import { showToast, confirmDialog } from './ui-helpers.js';

export function render(container) {
    const { npcs } = getState();

    if (npcs.length === 0) {
        container.innerHTML = `
            <div class="section-header">
                <h2 class="section-title">Mes PNJ</h2>
                <button class="btn btn-primary" onclick="window.location.hash = '/npc/new'">
                    Créer un PNJ
                </button>
            </div>
            <div class="empty-state">
                <div class="empty-state-icon">🎭</div>
                <h3>Aucun PNJ configuré</h3>
                <p>Créez votre premier PNJ pour commencer</p>
            </div>
        `;
        return;
    }

    const cardsHtml = npcs.map(npc => `
        <div class="npc-card">
            <div class="npc-card-header">
                <h3 class="npc-card-title">${escapeHtml(npc.name)}</h3>
                <div class="npc-card-subtitle">${escapeHtml(npc.occupation || 'Aucune occupation')}</div>
            </div>
            <div class="npc-card-body">
                <p class="npc-card-text">
                    <strong>Personnalité:</strong> ${escapeHtml(truncateText(npc.personality || 'Non définie', 80))}
                </p>
            </div>
            <div class="npc-card-actions">
                <button class="btn btn-primary btn-sm" data-action="view" data-npc-id="${npc.npcId}">
                    Voir
                </button>
                <button class="btn btn-secondary btn-sm" data-action="edit" data-npc-id="${npc.npcId}">
                    Éditer
                </button>
                <button class="btn btn-secondary btn-sm" data-action="chat" data-npc-id="${npc.npcId}">
                    Discuter
                </button>
                <button class="btn btn-danger btn-sm" data-action="delete" data-npc-id="${npc.npcId}">
                    Supprimer
                </button>
            </div>
        </div>
    `).join('');

    container.innerHTML = `
        <div class="section-header">
            <h2 class="section-title">Mes PNJ</h2>
            <button class="btn btn-primary" onclick="window.location.hash = '/npc/new'">
                Créer un PNJ
            </button>
        </div>
        <div class="npc-grid">
            ${cardsHtml}
        </div>
    `;

    container.querySelectorAll('[data-action]').forEach(button => {
        button.addEventListener('click', handleAction);
    });
}

async function handleAction(e) {
    const action = e.target.dataset.action;
    const npcId = e.target.dataset.npcId;

    switch (action) {
        case 'view':
            window.location.hash = `/npc/${npcId}`;
            break;
        case 'edit':
            window.location.hash = `/npc/${npcId}/edit`;
            break;
        case 'chat':
            window.location.hash = `/interact/${npcId}`;
            break;
        case 'delete':
            await handleDelete(npcId);
            break;
    }
}

async function handleDelete(npcId) {
    const confirmed = await confirmDialog(
        'Êtes-vous sûr de vouloir supprimer ce PNJ ? Cette action est irréversible et supprimera également toutes les interactions associées.',
        'Confirmer la suppression'
    );

    if (confirmed) {
        try {
            await deleteNpc(npcId);
            showToast('PNJ supprimé avec succès', 'success');
            window.dispatchEvent(new CustomEvent('npcs-changed'));
        } catch (error) {
            showToast(error.message || 'Erreur lors de la suppression du PNJ', 'error');
        }
    }
}

function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}
