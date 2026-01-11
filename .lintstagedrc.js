const { execSync } = require('child_process');
const path = require('path');
const os = require('os');

module.exports = {
  'frontend/**/*.{js,jsx,ts,tsx,vue,css,scss,json,md}': (files) => 
    `prettier --write ${files.join(' ')}`,
  
  'api/**/*.java': () => {
    try {
      console.log('Running Spotless...');
      const mvnCmd = os.platform() === 'win32' ? 'mvnw.cmd' : './mvnw';
      execSync(`${mvnCmd} spotless:apply`, { 
        cwd: path.join(__dirname, 'api'),
        stdio: 'inherit',
        shell: true
      });
      execSync('git add .', { stdio: 'inherit' });
      return [];
    } catch (error) {
      throw new Error('Spotless failed');
    }
  },
  
  'api/**/*.sql': (files) => 
    `prettier --write ${files.join(' ')}`,
  
  'api/**/*.{xml,yml,yaml}': (files) => 
    `prettier --write ${files.join(' ')}`
};