import * as NpcList from './components/npc-list.js';
import * as NpcForm from './components/npc-form.js';
import * as NpcDetail from './components/npc-detail.js';
import * as ChatInterface from './components/chat-interface.js';

const routes = {
    '/': {
        component: NpcList,
        navLink: 'npcs'
    },
    '/npc/new': {
        component: NpcForm,
        navLink: 'npcs',
        params: []
    },
    '/npc/:npcId': {
        component: NpcDetail,
        navLink: 'npcs',
        params: ['npcId']
    },
    '/npc/:npcId/edit': {
        component: NpcForm,
        navLink: 'npcs',
        params: ['npcId']
    },
    '/interact': {
        component: ChatInterface,
        navLink: 'interact',
        params: []
    },
    '/interact/:npcId': {
        component: ChatInterface,
        navLink: 'interact',
        params: ['npcId']
    }
};

let currentRoute = null;

export function init() {
    window.addEventListener('hashchange', handleRouteChange);
    handleRouteChange();
}

function handleRouteChange() {
    const hash = window.location.hash.slice(1) || '/';
    const { route, params } = matchRoute(hash);

    if (!route) {
        window.location.hash = '/';
        return;
    }

    currentRoute = route;
    updateActiveNav(route.navLink);
    renderRoute(route, params);
}

function matchRoute(path) {
    for (const [pattern, route] of Object.entries(routes)) {
        const regex = new RegExp('^' + pattern.replace(/:[^\s/]+/g, '([^\\s/]+)') + '$');
        const match = path.match(regex);

        if (match) {
            const params = {};
            if (route.params && route.params.length > 0) {
                route.params.forEach((param, index) => {
                    params[param] = match[index + 1];
                });
            }
            return { route, params };
        }
    }

    return { route: null, params: {} };
}

function updateActiveNav(activeLink) {
    document.querySelectorAll('.nav-link').forEach(link => {
        link.classList.remove('active');
        if (link.dataset.route === activeLink) {
            link.classList.add('active');
        }
    });
}

async function renderRoute(route, params) {
    const container = document.getElementById('main-content');
    if (!container) return;

    try {
        if (route.params && route.params.length > 0) {
            await route.component.render(container, ...Object.values(params));
        } else {
            await route.component.render(container);
        }
    } catch (error) {
        console.error('Error rendering route:', error);
        container.innerHTML = `
            <div class="empty-state">
                <h3>Erreur</h3>
                <p>Une erreur est survenue lors du chargement de la page.</p>
                <button class="btn btn-primary" onclick="window.location.hash = '/'">
                    Retour à l'accueil
                </button>
            </div>
        `;
    }
}

export function navigate(path) {
    window.location.hash = path;
}
