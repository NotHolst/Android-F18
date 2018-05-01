var server = require('http').createServer();
var io = require('socket.io')(server);

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