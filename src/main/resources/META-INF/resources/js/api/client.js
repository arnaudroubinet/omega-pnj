const API_BASE = window.location.origin;

export class ApiError extends Error {
    constructor(message, status, data) {
        super(message);
        this.name = 'ApiError';
        this.status = status;
        this.data = data;
    }
}

async function apiCall(endpoint, options = {}) {
    const url = `${API_BASE}${endpoint}`;

    const config = {
        headers: {
            'Content-Type': 'application/json',
            ...options.headers
        },
        ...options
    };

    if (options.body && typeof options.body === 'object') {
        config.body = JSON.stringify(options.body);
    }

    try {
        const response = await fetch(url, config);

        if (!response.ok) {
            let errorData;
            try {
                errorData = await response.json();
            } catch {
                errorData = await response.text();
            }

            const errorMessage = errorData?.error || errorData?.message || `HTTP ${response.status}: ${response.statusText}`;
            throw new ApiError(errorMessage, response.status, errorData);
        }

        if (response.status === 204) {
            return null;
        }

        return await response.json();
    } catch (error) {
        if (error instanceof ApiError) {
            throw error;
        }

        throw new ApiError(
            'Impossible de contacter le serveur. Vérifiez votre connexion.',
            0,
            { originalError: error.message }
        );
    }
}

export async function GET(endpoint) {
    return apiCall(endpoint, { method: 'GET' });
}

export async function POST(endpoint, body) {
    return apiCall(endpoint, { method: 'POST', body });
}

export async function DELETE(endpoint) {
    return apiCall(endpoint, { method: 'DELETE' });
}
