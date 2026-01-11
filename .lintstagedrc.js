const { execSync } = require('child_process');
const path = require('path');

module.exports = {
  'frontend/**/*.{js,jsx,ts,tsx,vue,css,scss,json,md}': (files) => 
    `prettier --write ${files.join(' ')}`,
  
  'api/**/*.java': () => {
    try {
      console.log('Running Spotless...');
      execSync('mvnw.cmd spotless:apply', { 
        cwd: path.join(__dirname, 'api'),
        stdio: 'inherit' 
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