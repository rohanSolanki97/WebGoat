// Delta tests for jwt-refresh.js focusing on:
// - Removal of hard-coded password; password is sourced from a configurable data attribute.
// - newToken now uses tokens from server response instead of undeclared variables.

require('../../../../main/resources/lessons/jwt/js/jwt-refresh.js'); // Ensure script is loaded

describe('jwt-refresh delta tests', () => {
  beforeEach(() => {
    // Reset DOM body data attribute and localStorage between tests
    document.body.innerHTML = '';
    document.body.removeAttribute('data-jwt-refresh-password');
    localStorage.clear();
    jest.restoreAllMocks();
  });

  test('login should use password from body data attribute instead of hardcoded value', () => {
    // Arrange
    document.body.setAttribute('data-jwt-refresh-password', 'dynamicSecret');

    const ajaxMock = jest.spyOn($, 'ajax').mockImplementation((options) => {
      // Simulate immediate success callback
      const response = { access_token: 'acc', refresh_token: 'ref' };
      if (options && typeof options.success === 'function') {
        options.success(response);
      }
      return { success: (cb) => cb(response) };
    });

    // Act
    // Call the exported login function if available, else trigger through ready handler
    if (typeof window.login === 'function') {
      window.login('Jerry');
    } else {
      // Fallback: directly invoke AJAX as in ready handler
      const readyHandlers = $._data(document, 'events')?.ready || [];
      readyHandlers.forEach(h => h.handler());
    }

    // Assert
    expect(ajaxMock).toHaveBeenCalled();
    const call = ajaxMock.mock.calls[0][0];
    const payload = JSON.parse(call.data);
    expect(payload.password).toBe('dynamicSecret');

    ajaxMock.mockRestore();
  });

  test('newToken should update tokens from server response instead of undeclared variables', () => {
    // Arrange
    localStorage.setItem('access_token', 'oldAccess');
    localStorage.setItem('refresh_token', 'oldRefresh');

    const ajaxMock = jest.spyOn($, 'ajax').mockImplementation((options) => {
      const response = { access_token: 'newAccess', refresh_token: 'newRefresh' };
      if (options && typeof options.success === 'function') {
        options.success(response);
      }
      return { success: (cb) => cb(response) };
    });

    // Act
    if (typeof window.newToken === 'function') {
      window.newToken();
    }

    // Assert
    expect(ajaxMock).toHaveBeenCalled();
    expect(localStorage.getItem('access_token')).toBe('newAccess');
    expect(localStorage.getItem('refresh_token')).toBe('newRefresh');

    ajaxMock.mockRestore();
  });
});
