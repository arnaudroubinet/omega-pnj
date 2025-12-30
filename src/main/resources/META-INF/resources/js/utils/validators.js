export function validateNpcConfig(data) {
    const errors = {};

    if (!data.npcId || data.npcId.trim() === '') {
        errors.npcId = 'L\'identifiant du PNJ est requis';
    } else if (data.npcId.length > 100) {
        errors.npcId = 'L\'identifiant ne doit pas dépasser 100 caractères';
    } else if (!/^[a-zA-Z0-9_-]+$/.test(data.npcId)) {
        errors.npcId = 'L\'identifiant ne peut contenir que des lettres, chiffres, tirets et underscores';
    }

    if (!data.name || data.name.trim() === '') {
        errors.name = 'Le nom du PNJ est requis';
    } else if (data.name.length > 200) {
        errors.name = 'Le nom ne doit pas dépasser 200 caractères';
    }

    if (data.backstory && data.backstory.length > 2000) {
        errors.backstory = 'L\'histoire ne doit pas dépasser 2000 caractères';
    }

    if (data.personality && data.personality.length > 1000) {
        errors.personality = 'La personnalité ne doit pas dépasser 1000 caractères';
    }

    if (data.occupation && data.occupation.length > 500) {
        errors.occupation = 'L\'occupation ne doit pas dépasser 500 caractères';
    }

    if (data.goals && data.goals.length > 500) {
        errors.goals = 'Les objectifs ne doivent pas dépasser 500 caractères';
    }

    if (data.fears && data.fears.length > 500) {
        errors.fears = 'Les peurs ne doivent pas dépasser 500 caractères';
    }

    return {
        isValid: Object.keys(errors).length === 0,
        errors
    };
}

export function validateInteraction(data) {
    const errors = {};

    if (!data.npcId || data.npcId.trim() === '') {
        errors.npcId = 'Le PNJ est requis';
    }

    if (!data.characterName || data.characterName.trim() === '') {
        errors.characterName = 'Le nom du personnage est requis';
    }

    if (!data.actionType) {
        errors.actionType = 'Le type d\'action est requis';
    }

    if (!data.message || data.message.trim() === '') {
        errors.message = 'Le message est requis';
    }

    return {
        isValid: Object.keys(errors).length === 0,
        errors
    };
}
