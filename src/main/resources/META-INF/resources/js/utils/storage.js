const CHAT_HISTORY_KEY = 'chat_history';
const MAX_MESSAGES_PER_CHAT = 100;

export function getChatHistory(npcId, characterName) {
    const key = `${npcId}:${characterName}`;
    const allHistory = JSON.parse(localStorage.getItem(CHAT_HISTORY_KEY) || '{}');
    return allHistory[key] || [];
}

export function saveChatMessage(npcId, characterName, message) {
    const key = `${npcId}:${characterName}`;
    const allHistory = JSON.parse(localStorage.getItem(CHAT_HISTORY_KEY) || '{}');

    if (!allHistory[key]) {
        allHistory[key] = [];
    }

    allHistory[key].push(message);

    if (allHistory[key].length > MAX_MESSAGES_PER_CHAT) {
        allHistory[key] = allHistory[key].slice(-MAX_MESSAGES_PER_CHAT);
    }

    localStorage.setItem(CHAT_HISTORY_KEY, JSON.stringify(allHistory));
}

export function clearChatHistory(npcId, characterName) {
    const key = `${npcId}:${characterName}`;
    const allHistory = JSON.parse(localStorage.getItem(CHAT_HISTORY_KEY) || '{}');
    delete allHistory[key];
    localStorage.setItem(CHAT_HISTORY_KEY, JSON.stringify(allHistory));
}

export function clearAllChatHistory() {
    localStorage.removeItem(CHAT_HISTORY_KEY);
}
