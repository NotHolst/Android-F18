var server = require('http').createServer();
var io = require('socket.io')(server);
var sqlite3 = require('sqlite3').verbose();
var db = new sqlite3.Database('database.db', (err) => {
    if(err != null) {
        console.log("Opening database failed!");
    }
}); 

io.on('connection', (client) => {

    client.on('joinRoom', (data) => {
        client.join(data.roomId);
    });

    client.on('sendMessage', (data) => {
        // Data should probably contain roomId and message
    });

    client.on('disconnect', function () {
        // Do something, maybe notify other users that this user is now offline.
    });
});

server.listen(3000);