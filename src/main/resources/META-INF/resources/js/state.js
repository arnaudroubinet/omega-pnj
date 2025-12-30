const state = {
    npcs: [],
    currentNpc: null,
    currentView: 'npcs',
    user: {
        characterName: localStorage.getItem('user_character_name') || '',
        context: ''
    },
    chatHistory: {},
    loading: false,
    error: null
};

const listeners = [];

export function getState() {
    return state;
}

export function setState(updates) {
    Object.assign(state, updates);
    notifyListeners();
}

export function subscribe(listener) {
    listeners.push(listener);
    return () => {
        const index = listeners.indexOf(listener);
        if (index > -1) {
            listeners.splice(index, 1);
        }
    };
}

function notifyListeners() {
    listeners.forEach(listener => listener(state));
}

export function setUserCharacterName(name) {
    state.user.characterName = name;
    localStorage.setItem('user_character_name', name);
    notifyListeners();
}

export function setCurrentNpc(npc) {
    state.currentNpc = npc;
    if (npc) {
        localStorage.setItem('last_selected_npc', npc.npcId);
    }
    notifyListeners();
}

export function setNpcs(npcs) {
    state.npcs = npcs;
    notifyListeners();
}

export function addOrUpdateNpc(npc) {
    const index = state.npcs.findIndex(n => n.npcId === npc.npcId);
    if (index >= 0) {
        state.npcs[index] = npc;
    } else {
        state.npcs.push(npc);
    }
    notifyListeners();
}

export function removeNpc(npcId) {
    state.npcs = state.npcs.filter(n => n.npcId !== npcId);
    if (state.currentNpc?.npcId === npcId) {
        state.currentNpc = null;
    }
    notifyListeners();
}
