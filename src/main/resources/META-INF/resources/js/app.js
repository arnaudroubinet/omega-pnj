import { init as initRouter } from './router.js';
import { setNpcs, subscribe } from './state.js';
import { getAllNpcs } from './api/npc-config-api.js';
import { showLoading, hideLoading, showToast } from './components/ui-helpers.js';

async function init() {
    try {
        showLoading();
        await loadNpcs();
        hideLoading();
    } catch (error) {
        hideLoading();
        showToast('Erreur lors du chargement des PNJ: ' + error.message, 'error');
    }

    initRouter();

    window.addEventListener('npcs-changed', async () => {
        try {
            await loadNpcs();
            window.location.reload();
        } catch (error) {
            showToast('Erreur lors du rechargement des PNJ: ' + error.message, 'error');
        }
    });
}

async function loadNpcs() {
    const npcs = await getAllNpcs();
    setNpcs(npcs);
}

if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
} else {
    init();
}
