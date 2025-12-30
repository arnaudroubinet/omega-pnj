import { POST } from './client.js';

export async function handleInteraction(interactionData) {
    return await POST('/npc/handle', interactionData);
}
