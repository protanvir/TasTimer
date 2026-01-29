const net = require('net');
const http = require('http');

const TCP_PORT = 9854;
const HTTP_PORT = 3000;
const HEARTBEAT_INTERVAL = 1000;
// const READ_INTERVAL = 2500; // Disabled random generation

const tcpClients = new Set();
const tagsHistory = [];

// --- Helper Functions ---

function getFormattedTCPTimestamp(dateObj) {
    const now = dateObj || new Date();
    const YYYY = now.getFullYear();
    const MM = String(now.getMonth() + 1).padStart(2, '0');
    const DD = String(now.getDate()).padStart(2, '0');
    const hh = String(now.getHours()).padStart(2, '0');
    const mm = String(now.getMinutes()).padStart(2, '0');
    const ss = String(now.getSeconds()).padStart(2, '0');
    const ccc = String(now.getMilliseconds()).padStart(3, '0');
    return `${YYYY}${MM}${DD}${hh}${mm}${ss}${ccc}`;
}

// --- TCP Server ---

const tcpServer = net.createServer((socket) => {
    console.log(`TCP Client connected: ${socket.remoteAddress}:${socket.remotePort}`);
    tcpClients.add(socket);

    const heartbeatTimer = setInterval(() => {
        if (!socket.destroyed) {
            socket.write('*');
        }
    }, HEARTBEAT_INTERVAL);

    socket.on('end', () => {
        console.log('TCP Client disconnected');
        tcpClients.delete(socket);
        clearInterval(heartbeatTimer);
    });

    socket.on('error', (err) => {
        console.error('TCP Socket error:', err);
        tcpClients.delete(socket);
        clearInterval(heartbeatTimer);
    });
});

tcpServer.listen(TCP_PORT, () => {
    console.log(`TCP Mock Server listening on port ${TCP_PORT}`);
});

// --- HTTP Server ---

const httpServer = http.createServer((req, res) => {
    res.setHeader('Access-Control-Allow-Origin', '*');
    res.setHeader('Access-Control-Allow-Methods', 'POST, GET, OPTIONS');

    if (req.method === 'POST' && req.url === '/api/tags') {
        let body = '';
        req.on('data', chunk => body += chunk.toString());
        req.on('end', () => {
            console.log(`[${new Date().toISOString()}] Received Tag Data via HTTP:`, body);
            try {
                const tagData = JSON.parse(body);

                // 1. Store in history (UI)
                if (!tagData.timestamp) tagData.timestamp = new Date().toISOString();
                tagsHistory.unshift(tagData);
                if (tagsHistory.length > 50) tagsHistory.pop();

                // 2. Broadcast to TCP Clients
                const chipId = tagData.epc || 'UNKNOWN';
                const readerId = tagData.reader || 'Reader1';
                const dateObj = new Date(tagData.timestamp);
                const tcpTimestamp = getFormattedTCPTimestamp(isNaN(dateObj) ? new Date() : dateObj);

                // Format: "C,YYYYMMDDhhmmssccc,@"
                // Note: User spec says separator=",". 
                // Ex: "C,YYYYMMDDhhmmssccc,@" -> "CHIP,TIME,READER"
                const message = `${chipId},${tcpTimestamp},${readerId}\r`;

                for (const client of tcpClients) {
                    if (!client.destroyed) {
                        client.write(message);
                    }
                }
                console.log(`Forwarded to ${tcpClients.size} TCP clients: ${message.trim()}`);

                res.writeHead(200, { 'Content-Type': 'application/json' });
                res.end(JSON.stringify({ status: 'success' }));
            } catch (e) {
                console.error("Error processing JSON:", e);
                res.writeHead(400);
                res.end(JSON.stringify({ error: e.message }));
            }
        });
    } else if (req.method === 'GET') {
        // Serve Dashboard
        res.writeHead(200, { 'Content-Type': 'text/html' });
        res.write(`
            <!DOCTYPE html>
            <html>
            <head>
                <title>RFID Tag Monitor</title>
                <meta http-equiv="refresh" content="2">
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
                <p>TCP Server Port: ${TCP_PORT} | HTTP Server Port: ${HTTP_PORT}</p>
                <p>Send POST to /api/tags to simulate reads.</p>
                <table>
                    <thead><tr><th>Time</th><th>EPC</th><th>Reader</th></tr></thead>
                    <tbody>
        `);
        tagsHistory.forEach(tag => {
            res.write(`<tr><td>${tag.timestamp}</td><td>${tag.epc}</td><td>${tag.reader}</td></tr>`);
        });
        res.write(`</tbody></table></body></html>`);
        res.end();
    } else {
        res.writeHead(404);
        res.end();
    }
});

httpServer.listen(HTTP_PORT, () => {
    console.log(`HTTP Mock Server listening on port ${HTTP_PORT}`);
});
