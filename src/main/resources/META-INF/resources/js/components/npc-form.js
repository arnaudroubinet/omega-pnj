import { createOrUpdateNpc, getNpc } from '../api/npc-config-api.js';
import { validateNpcConfig } from '../utils/validators.js';
import { showToast, showLoading, hideLoading } from './ui-helpers.js';

export async function render(container, npcId = null) {
    let npc = null;
    let isEdit = false;

    if (npcId) {
        isEdit = true;
        try {
            showLoading();
            npc = await getNpc(npcId);
            hideLoading();
        } catch (error) {
            hideLoading();
            showToast(error.message || 'Erreur lors du chargement du PNJ', 'error');
            window.location.hash = '/';
            return;
        }
    }

    container.innerHTML = `
        <div class="section-header">
            <h2 class="section-title">${isEdit ? 'Éditer' : 'Créer'} un PNJ</h2>
            <button class="btn btn-secondary" onclick="window.location.hash = '/'">
                Retour
            </button>
        </div>

        <form id="npc-form" class="detail-grid">
            <div class="detail-item">
                <div class="form-group">
                    <label class="form-label form-label-required" for="npcId">Identifiant</label>
                    <input
                        type="text"
                        id="npcId"
                        class="form-input"
                        placeholder="merchant_bob"
                        value="${npc?.npcId || ''}"
                        ${isEdit ? 'readonly' : ''}
                        required
                    >
                    <span class="form-help">Lettres, chiffres, tirets et underscores uniquement. Max 100 caractères.</span>
                    <span class="form-error" id="error-npcId"></span>
                </div>

                <div class="form-group">
                    <label class="form-label form-label-required" for="name">Nom</label>
                    <input
                        type="text"
                        id="name"
                        class="form-input"
                        placeholder="Bob le Marchand"
                        value="${npc?.name || ''}"
                        required
                    >
                    <span class="form-error" id="error-name"></span>
                    <div class="char-count"><span id="count-name">0</span>/200</div>
                </div>

                <div class="form-group">
                    <label class="form-label" for="occupation">Occupation</label>
                    <input
                        type="text"
                        id="occupation"
                        class="form-input"
                        placeholder="Marchand d'armes"
                        value="${npc?.occupation || ''}"
                    >
                    <span class="form-error" id="error-occupation"></span>
                    <div class="char-count"><span id="count-occupation">0</span>/500</div>
                </div>
            </div>

            <div class="detail-item">
                <div class="form-group">
                    <label class="form-label" for="backstory">Histoire</label>
                    <textarea
                        id="backstory"
                        class="form-textarea"
                        rows="5"
                        placeholder="Ancien aventurier devenu marchand..."
                    >${npc?.backstory || ''}</textarea>
                    <span class="form-error" id="error-backstory"></span>
                    <div class="char-count"><span id="count-backstory">0</span>/2000</div>
                </div>

                <div class="form-group">
                    <label class="form-label" for="personality">Personnalité</label>
                    <textarea
                        id="personality"
                        class="form-textarea"
                        rows="4"
                        placeholder="Jovial mais méfiant avec les inconnus..."
                    >${npc?.personality || ''}</textarea>
                    <span class="form-error" id="error-personality"></span>
                    <div class="char-count"><span id="count-personality">0</span>/1000</div>
                </div>

                <div class="form-group">
                    <label class="form-label" for="goals">Objectifs</label>
                    <textarea
                        id="goals"
                        class="form-textarea"
                        rows="3"
                        placeholder="Devenir le marchand le plus riche de la région..."
                    >${npc?.goals || ''}</textarea>
                    <span class="form-error" id="error-goals"></span>
                    <div class="char-count"><span id="count-goals">0</span>/500</div>
                </div>

                <div class="form-group">
                    <label class="form-label" for="fears">Peurs</label>
                    <textarea
                        id="fears"
                        class="form-textarea"
                        rows="3"
                        placeholder="Perdre sa boutique dans un incendie..."
                    >${npc?.fears || ''}</textarea>
                    <span class="form-error" id="error-fears"></span>
                    <div class="char-count"><span id="count-fears">0</span>/500</div>
                </div>
            </div>

            <div class="detail-item">
                <button type="submit" class="btn btn-primary btn-lg">
                    ${isEdit ? 'Mettre à jour' : 'Créer'} le PNJ
                </button>
            </div>
        </form>
    `;

    setupFormHandlers();
    updateCharCounts();
}

function setupFormHandlers() {
    const form = document.getElementById('npc-form');
    const fields = ['npcId', 'name', 'backstory', 'personality', 'occupation', 'goals', 'fears'];

    fields.forEach(fieldId => {
        const field = document.getElementById(fieldId);
        if (field) {
            field.addEventListener('input', () => {
                updateCharCount(fieldId);
                clearFieldError(fieldId);
            });
        }
    });

    form.addEventListener('submit', handleSubmit);
}

function updateCharCounts() {
    ['name', 'backstory', 'personality', 'occupation', 'goals', 'fears'].forEach(updateCharCount);
}

function updateCharCount(fieldId) {
    const field = document.getElementById(fieldId);
    const counter = document.getElementById(`count-${fieldId}`);
    if (field && counter) {
        const length = field.value.length;
        counter.textContent = length;

        const maxLengths = {
            name: 200,
            backstory: 2000,
            personality: 1000,
            occupation: 500,
            goals: 500,
            fears: 500
        };

        const parent = counter.parentElement;
        if (length > maxLengths[fieldId] * 0.9) {
            parent.classList.add('warning');
        } else {
            parent.classList.remove('warning');
        }
    }
}

function clearFieldError(fieldId) {
    const field = document.getElementById(fieldId);
    const errorSpan = document.getElementById(`error-${fieldId}`);

    if (field) {
        field.classList.remove('error');
    }
    if (errorSpan) {
        errorSpan.textContent = '';
    }
}

function showFieldErrors(errors) {
    Object.keys(errors).forEach(fieldId => {
        const field = document.getElementById(fieldId);
        const errorSpan = document.getElementById(`error-${fieldId}`);

        if (field) {
            field.classList.add('error');
        }
        if (errorSpan) {
            errorSpan.textContent = errors[fieldId];
        }
    });
}

async function handleSubmit(e) {
    e.preventDefault();

    const formData = {
        npcId: document.getElementById('npcId').value.trim(),
        name: document.getElementById('name').value.trim(),
        backstory: document.getElementById('backstory').value.trim(),
        personality: document.getElementById('personality').value.trim(),
        occupation: document.getElementById('occupation').value.trim(),
        goals: document.getElementById('goals').value.trim(),
        fears: document.getElementById('fears').value.trim()
    };

    const validation = validateNpcConfig(formData);
    if (!validation.isValid) {
        showFieldErrors(validation.errors);
        showToast('Veuillez corriger les erreurs dans le formulaire', 'error');
        return;
    }

    const submitButton = e.target.querySelector('button[type="submit"]');
    const originalText = submitButton.textContent;
    submitButton.disabled = true;
    submitButton.textContent = 'Enregistrement...';

    try {
        await createOrUpdateNpc(formData);
        showToast('PNJ enregistré avec succès', 'success');
        window.dispatchEvent(new CustomEvent('npcs-changed'));
        window.location.hash = '/';
    } catch (error) {
        showToast(error.message || 'Erreur lors de l\'enregistrement du PNJ', 'error');
        submitButton.disabled = false;
        submitButton.textContent = originalText;
    }
}
