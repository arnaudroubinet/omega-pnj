import { getState, setUserCharacterName } from '../state.js';
import { handleInteraction } from '../api/npc-handler-api.js';
import { getChatHistory, saveChatMessage, clearChatHistory } from '../utils/storage.js';
import { formatTime, showToast, confirmDialog } from './ui-helpers.js';
import { validateInteraction } from '../utils/validators.js';

let currentNpcId = null;
let currentActionType = 'PARLER';

export function render(container, npcId = null) {
    const { npcs, user } = getState();

    if (npcs.length === 0) {
        container.innerHTML = `
            <div class="empty-state">
                <div class="empty-state-icon">🎭</div>
                <h3>Aucun PNJ disponible</h3>
                <p>Créez d'abord un PNJ pour interagir avec lui</p>
                <button class="btn btn-primary" onclick="window.location.hash = '/npc/new'">
                    Créer un PNJ
                </button>
            </div>
        `;
        return;
    }

    currentNpcId = npcId || npcs[0].npcId;
    const selectedNpc = npcs.find(n => n.npcId === currentNpcId);

    const npcOptions = npcs.map(npc => `
        <option value="${npc.npcId}" ${npc.npcId === currentNpcId ? 'selected' : ''}>
            ${escapeHtml(npc.name)}
        </option>
    `).join('');

    container.innerHTML = `
        <div class="section-header">
            <h2 class="section-title">Interactions</h2>
            <button class="btn btn-secondary" onclick="window.location.hash = '/'">
                Retour
            </button>
        </div>

        <div class="chat-container">
            <div class="chat-header">
                <h3>Discussion avec ${escapeHtml(selectedNpc.name)}</h3>
                <div class="chat-settings">
                    <div class="chat-settings-row">
                        <div class="form-group">
                            <label class="form-label" for="npc-select">PNJ</label>
                            <select id="npc-select" class="form-select">
                                ${npcOptions}
                            </select>
                        </div>
                        <div class="form-group">
                            <label class="form-label" for="character-name">Votre nom de personnage</label>
                            <input
                                type="text"
                                id="character-name"
                                class="form-input"
                                placeholder="Nom du personnage"
                                value="${escapeHtml(user.characterName)}"
                            >
                        </div>
                    </div>
                    <div class="chat-settings-row">
                        <div class="form-group">
                            <label class="form-label">Type d'interaction</label>
                            <div class="interaction-type-toggle">
                                <div class="toggle-option">
                                    <input type="radio" id="type-parler" name="action-type" value="PARLER" checked>
                                    <label for="type-parler">Conversation</label>
                                </div>
                                <div class="toggle-option">
                                    <input type="radio" id="type-action" name="action-type" value="ACTION">
                                    <label for="type-action">Action</label>
                                </div>
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="form-label" for="context-input">Contexte (optionnel)</label>
                            <input
                                type="text"
                                id="context-input"
                                class="form-input"
                                placeholder="Dans la boutique, la nuit..."
                            >
                        </div>
                    </div>
                </div>
            </div>

            <div id="chat-messages" class="chat-messages">
            </div>

            <div class="chat-input">
                <div class="chat-input-row">
                    <textarea
                        id="message-input"
                        class="form-textarea"
                        placeholder="Tapez votre message..."
                    ></textarea>
                    <button id="send-button" class="btn btn-primary">
                        Envoyer
                    </button>
                </div>
                <button id="clear-history" class="btn btn-danger btn-sm" style="margin-top: 8px;">
                    Effacer l'historique
                </button>
            </div>
        </div>
    `;

    setupChatHandlers();
    loadChatHistory();
}

function setupChatHandlers() {
    const npcSelect = document.getElementById('npc-select');
    const characterNameInput = document.getElementById('character-name');
    const sendButton = document.getElementById('send-button');
    const messageInput = document.getElementById('message-input');
    const clearButton = document.getElementById('clear-history');
    const typeRadios = document.querySelectorAll('input[name="action-type"]');

    npcSelect?.addEventListener('change', (e) => {
        currentNpcId = e.target.value;
        loadChatHistory();
    });

    characterNameInput?.addEventListener('change', (e) => {
        setUserCharacterName(e.target.value);
        loadChatHistory();
    });

    typeRadios.forEach(radio => {
        radio.addEventListener('change', (e) => {
            currentActionType = e.target.value;
            const placeholder = currentActionType === 'PARLER'
                ? 'Tapez votre message...'
                : 'Décrivez l\'action...';
            messageInput.placeholder = placeholder;
        });
    });

    sendButton?.addEventListener('click', handleSendMessage);

    messageInput?.addEventListener('keydown', (e) => {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            handleSendMessage();
        }
    });

    clearButton?.addEventListener('click', handleClearHistory);
}

function loadChatHistory() {
    const { user } = getState();
    if (!currentNpcId || !user.characterName) {
        return;
    }

    const history = getChatHistory(currentNpcId, user.characterName);
    const messagesContainer = document.getElementById('chat-messages');

    if (history.length === 0) {
        messagesContainer.innerHTML = `
            <div class="empty-state">
                <p>Pas encore de messages. Commencez la conversation!</p>
            </div>
        `;
        return;
    }

    messagesContainer.innerHTML = history.map(msg => {
        if (msg.type === 'user') {
            return `
                <div class="chat-message user">
                    <div class="message-bubble">${escapeHtml(msg.message)}</div>
                    <div class="message-meta">${formatTime(msg.timestamp)}</div>
                </div>
            `;
        } else {
            return `
                <div class="chat-message npc">
                    ${msg.thinking ? `
                        <details class="thinking-details">
                            <summary class="thinking-summary">💭 Voir la pensée</summary>
                            <div class="thinking-content">${escapeHtml(msg.thinking)}</div>
                        </details>
                    ` : ''}
                    <div class="message-bubble">${escapeHtml(msg.message)}</div>
                    <div class="message-meta">
                        ${formatTime(msg.timestamp)}
                        ${msg.emotionalState ? `<span class="emotion-badge">${escapeHtml(msg.emotionalState)}</span>` : ''}
                        ${msg.relationshipChange ? `<span>${msg.relationshipChange > 0 ? '+' : ''}${msg.relationshipChange}</span>` : ''}
                    </div>
                </div>
            `;
        }
    }).join('');

    scrollToBottom();
}

function scrollToBottom() {
    const messagesContainer = document.getElementById('chat-messages');
    if (messagesContainer) {
        messagesContainer.scrollTop = messagesContainer.scrollHeight;
    }
}

async function handleSendMessage() {
    const { user } = getState();
    const messageInput = document.getElementById('message-input');
    const sendButton = document.getElementById('send-button');
    const contextInput = document.getElementById('context-input');

    const message = messageInput?.value.trim();
    const characterName = user.characterName;
    const context = contextInput?.value.trim();

    if (!characterName) {
        showToast('Veuillez entrer votre nom de personnage', 'error');
        document.getElementById('character-name')?.focus();
        return;
    }

    if (!message) {
        showToast('Veuillez entrer un message', 'error');
        return;
    }

    const interactionData = {
        npcId: currentNpcId,
        characterName,
        actionType: currentActionType,
        message,
        context: context || null
    };

    const validation = validateInteraction(interactionData);
    if (!validation.isValid) {
        showToast('Veuillez remplir tous les champs requis', 'error');
        return;
    }

    sendButton.disabled = true;
    sendButton.textContent = 'Envoi...';

    saveChatMessage(currentNpcId, characterName, {
        type: 'user',
        message,
        timestamp: new Date().toISOString()
    });

    loadChatHistory();
    messageInput.value = '';

    try {
        const response = await handleInteraction(interactionData);

        saveChatMessage(currentNpcId, characterName, {
            type: 'npc',
            message: response.npcResponse,
            thinking: response.thinking,  // Chain of Thought
            emotionalState: response.emotionalState,
            relationshipChange: response.relationshipChange,
            timestamp: response.timestamp
        });

        loadChatHistory();
    } catch (error) {
        showToast(error.message || 'Erreur lors de l\'envoi du message', 'error');
    } finally {
        sendButton.disabled = false;
        sendButton.textContent = 'Envoyer';
        messageInput.focus();
    }
}

async function handleClearHistory() {
    const { user } = getState();

    const confirmed = await confirmDialog(
        'Êtes-vous sûr de vouloir effacer l\'historique de cette conversation ?',
        'Effacer l\'historique'
    );

    if (confirmed) {
        clearChatHistory(currentNpcId, user.characterName);
        loadChatHistory();
        showToast('Historique effacé', 'success');
    }
}

function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}
