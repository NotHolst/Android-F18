const server = require('http').createServer();
const io = require('socket.io')(server);
const db = require('./database');
const sha1 = require('sha1');


io.on('connection', (client) => {

    client.on('register', (data) => {
        let username = data.username;
        let password = data.password;
        let nickname = data.nickname;

        if (username == undefined || password == undefined) {
            client.send('registrationError', {
                message: 'Please supply username and password'
            })
        }

        if (nickname == undefined) {
            nickname = username;
        }

        if (db.createUser(username, sha1(password), nickname) != -1) {
            client.send('registrationSuccess')
        } else {
            client.send('registrationError', {
                message: 'Username already taken.'
            })
        }

    });

    client.on('authenticate', (data) => {
        let username = data.username;
        let password = data.password;

        if (username && password) {
            const USER = db.auth(username, sha1(password));

            if (!USER) {
                client.send({
                    status: 'error',
                    message: 'Username or password incorrect.'
                });
                return;
            }

            //we're authenticated, add event listeners.

            client.on('createRoom', (data) => {
                let otherUserID = data.otherUserID;
                if (!otherUserID) {
                    return;
                }
                db.createRoom(USER.ID, data.otherUserID);

            });

            client.on('addToRoom', (data) => {
                let userID = data.userID;
                let roomID = data.roomID;
                if (userID && roomID) {
                    db.addUserToRoom(userID, roomID);
                }
            });

            client.on('leaveRoom', (data) => {
                let roomID = data.roomID;
                if (!roomID) {
                    return;
                }
                db.leaveRoom(USER.ID, roomID);
                client.send('roomLeft', { roomID: roomID });
                io.to(roomID).emit('userLeft', USER);
            });

            client.on('sendMessage', (data) => {
                let message = data.message;
                let roomID = data.roomID;
                if(message && roomID){
                    let msg = db.createMessage(USER.ID, roomID, message);
                    io.to(roomID).emit('message', msg)
                }

            });

            client.on('joinRoom', (data) => {
                let roomID = data.roomID;
                if(roomID && db.userIsInRoom(USER.ID, roomID)){
                    client.join(roomID);
                }

            });

            client.on('disconnect', function () {
                // Do something, maybe notify other users that this user is now offline.
            });

            client.send('authenticationSuccess');
        }
    });

});

server.listen(3000);