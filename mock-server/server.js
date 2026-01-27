const http = require('http');

const PORT = 3000;
const tags = [];

const server = http.createServer((req, res) => {
    // Enable CORS for browser access if needed
    res.setHeader('Access-Control-Allow-Origin', '*');
    res.setHeader('Access-Control-Allow-Methods', 'POST, GET, OPTIONS');

    if (req.method === 'POST' && req.url === '/api/tags') {
        let body = '';

        req.on('data', chunk => {
            body += chunk.toString();
        });

        req.on('end', () => {
            console.log(`[${new Date().toISOString()}] Received Tag Data:`);
            console.log(body);
            try {
                const tagData = JSON.parse(body);
                tags.unshift(tagData); // Add to beginning of array
                // Keep only last 50 tags
                if (tags.length > 50) tags.pop();
            } catch (e) {
                console.error("Error parsing JSON:", e);
            }
            res.writeHead(200, { 'Content-Type': 'application/json' });
            res.end(JSON.stringify({ status: 'success' }));
        });
    } else if (req.method === 'GET') {
        // Serve a simple HTML page for any GET request
        res.writeHead(200, { 'Content-Type': 'text/html' });
        res.write(`
            <!DOCTYPE html>
            <html>
            <head>
                <title>RFID Tag Monitor</title>
                <meta http-equiv="refresh" content="2"> <!-- Auto-refresh every 2 seconds -->
                <style>
                    body { font-family: sans-serif; padding: 20px; }
                    table { border-collapse: collapse; width: 100%; }
                    th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
                    th { background-color: #f2f2f2; }
                    tr:nth-child(even) { background-color: #f9f9f9; }
                </style>
            </head>
            <body>
                <h1>Live RFID Tag Reads</h1>
                <p>Auto-refreshing every 2 seconds...</p>
                <table>
                    <thead>
                        <tr>
                            <th>Time</th>
                            <th>EPC</th>
                            <th>Reader</th>
                        </tr>
                    </thead>
                    <tbody>
        `);

        tags.forEach(tag => {
            res.write(`
                <tr>
                    <td>${tag.timestamp || new Date().toISOString()}</td>
                    <td>${tag.epc}</td>
                    <td>${tag.reader}</td>
                </tr>
            `);
        });

        res.write(`
                    </tbody>
                </table>
            </body>
            </html>
        `);
        res.end();
    } else {
        res.writeHead(404);
        res.end();
    }
});

server.listen(PORT, () => {
    console.log(`Mock REST API Server running at http://localhost:${PORT}/`);
    console.log('You can now view tag reads in your browser.');
});
