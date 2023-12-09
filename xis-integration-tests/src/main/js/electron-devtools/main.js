const { app, BrowserWindow } = require('electron')

app.on('ready', () => {
	const win = new BrowserWindow({ width: 1024, height: 768 });
	win.loadURL('devtools://devtools/bundled/js_app.html?ws=127.0.0.1:4242/xis');
})
