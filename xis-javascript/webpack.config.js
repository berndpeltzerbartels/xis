const path = require('path');

module.exports = {
  mode: 'production',
  entry: './src/main/resources/xis-prod.js',
  output: {
    path: path.resolve(__dirname, 'build/resources/main'),
    filename: 'bundle.min.js',
    library: 'XIS', // optional
    libraryTarget: 'umd' // optional: macht global verwendbar
  }
};