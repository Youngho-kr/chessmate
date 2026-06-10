async function authFetch(url, options = {}) {
    const accessToken = localStorage.getItem('accessToken');

    options.headers = {
        ...options.headers,
        'Authorization': 'Bearer ' + accessToken
    };

    const response = await fetch(url, options);

    if (response.status === 401) {
        const refreshToken = localStorage.getItem('refreshToken');

        const reissueResponse = await fetch('/api/auth/reissue', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ refreshToken })
        });

        if (reissueResponse.ok) {
            const data = await reissueResponse.json();
            localStorage.setItem('accessToken', data.accessToken);
            localStorage.setItem('refreshToken', data.refreshToken);

            options.headers['Authorization'] = 'Bearer ' + data.accessToken;
            return fetch(url, options);
        } else {
            localStorage.removeItem('accessToken');
            localStorage.removeItem('refreshToken');
            // window.location.href = '/';
        }
    }

    return response;
}

async function logout() {
    await authFetch('/api/auth/logout', { method: 'POST' })
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    window.location.href = '/';
}