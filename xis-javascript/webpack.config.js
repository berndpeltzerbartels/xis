const path = require('path');

module.exports = {
  mode: 'production',
  entry: './src/main/resources/xis-prod.js',
  output: {
    path: path.resolve(__dirname, 'build/resources/main'),
    filename: 'bundle.min.js',
    library: 'XIS',
    libraryTarget: 'umd'
  },
  devtool: 'source-map',  // Generiert separate .map Datei
  optimization: {
    minimize: true,
    minimizer: [
      new (require('terser-webpack-plugin'))({
        terserOptions: {
          keep_classnames: true,  // Klassennamen beibehalten
          keep_fnames: true,      // Funktionsnamen beibehalten
          mangle: false           // Keine Variable-Umbenennung
        }
      })
    ]
  }
};