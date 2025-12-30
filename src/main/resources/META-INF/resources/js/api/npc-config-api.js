import { GET, POST, DELETE } from './client.js';

export async function getAllNpcs() {
    return await GET('/npc/config');
}

export async function getNpc(npcId) {
    return await GET(`/npc/config/${encodeURIComponent(npcId)}`);
}

export async function createOrUpdateNpc(npcData) {
    return await POST('/npc/config', npcData);
}

export async function deleteNpc(npcId) {
    return await DELETE(`/npc/config/${encodeURIComponent(npcId)}`);
}
