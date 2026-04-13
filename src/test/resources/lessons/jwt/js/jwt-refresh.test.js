const fs = require('fs');
const path = require('path');

describe('jwt-refresh.js delta behavior - hardcoded password removal', () => {
  const filePath = path.resolve(
    __dirname,
    '../../../../main/resources/lessons/jwt/js/jwt-refresh.js'
  );

  test('file should not contain the original hardcoded password literal', () => {
    const contents = fs.readFileSync(filePath, 'utf8');

    // Assert the known hardcoded password value is no longer present
    expect(contents).not.toContain('bm5nhSkxCXZkKRy4');
  });

  test('login should use configuration-driven demoPasswordProvider', () => {
    const contents = fs.readFileSync(filePath, 'utf8');

    // The updated implementation should use webgoatJwtConfig.demoPasswordProvider()
    expect(contents).toMatch(/webgoatJwtConfig\.demoPasswordProvider\s*\(/);
  });
});
