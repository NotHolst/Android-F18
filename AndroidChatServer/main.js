const app = require('express')();
const bodyParser = require('body-parser')
const server = require('http').Server(app);
const io = require('socket.io')(server);
const db = require('./database');
const sha1 = require('sha1');
const jwt = require('jsonwebtoken');

const JWT_SECRET = 'androidchatapplication';

app.use(bodyParser.urlencoded({
    extended: true
}));
app.use(bodyParser.json());

app.post('/authenticate', function (req, res) {
    let username = req.body.username;
    let password = sha1(req.body.password);

    let user = db.auth(username, password);
    if (user != undefined) {
        let token = jwt.sign(user, JWT_SECRET);
        return res.send({
            status: 'success',
            token: token
        });
    }
    res.send({
        status: 'error',
        message: 'Incorrect credentials.'
    })

});

app.post('/register', (req, res) => {

    let username = req.body.username;
    let password = req.body.password;
    let nickname = req.body.nickname;

    if (username == undefined || password == undefined) {
        client.emit('registrationError', {
            message: 'Please supply username and password'
        })
    }

    if (nickname == undefined) {
        nickname = username;
    }

    let userID = db.createUser(username, sha1(password), nickname);
    if (userID != -1) {
        let token = jwt.sign(db.user(userID), JWT_SECRET);
        res.send({
            status: 'success',
            token: token
        })
    } else {
        res.send({
            status: 'error',
            message: 'Username already taken.'
        })
    }

});

var connections = []

io.on('connection', (client) => {
    console.log("Client connected.");

    client.on('authenticate', (data) => {
        console.log("client attempting auth")
        let user;
        try {
            user = jwt.verify(data.token, JWT_SECRET);
        } catch (err) {
            client.emit('invalidToken');
            return;
        }
        console.log(user.Username + " authenticated with token: " + data.token);
        connections.push({ userID: user.ID, socket: client });
    })

    client.on('createRoom', (data) => {
        let user;
        try {
            user = jwt.verify(data.token, JWT_SECRET);
        } catch (err) {
            client.emit('invalidToken');
            return;
        }

        let otherUserID = data.otherUserID;
        if (!otherUserID) {
            return;
        }
        let roomID = db.createRoom(user.ID, data.otherUserID);
        let responseData = { roomID: roomID };
        connections.find(x => x.userID = user.ID).socket.emit('joinedRoom', data);
        connections.find(x => x.userID = data.otherUserID).socket.emit('joinedRoom', data);
    });

    client.on('addToRoom', (data) => {
        let user;
        try {
            user = jwt.verify(data.token, JWT_SECRET);
        } catch (err) {
            client.emit('invalidToken');
            return;
        }

        let userID = data.userID;
        let roomID = data.roomID;
        if (userID && roomID) {
            db.addUserToRoom(userID, roomID);
        }
    });

    client.on('leaveRoom', (data) => {
        let user;
        try {
            user = jwt.verify(data.token, JWT_SECRET);
        } catch (err) {
            client.emit('invalidToken');
            return;
        }

        let roomID = data.roomID;
        if (!roomID) {
            return;
        }
        db.leaveRoom(user.ID, roomID);
        client.emit('roomLeft', { roomID: roomID });
        io.to(roomID).emit('userLeft', user);
    });

    client.on('sendMessage', (data) => {
        let user;
        try {
            user = jwt.verify(data.token, JWT_SECRET);
        } catch (err) {
            client.emit('invalidToken');
            return;
        }

        let message = data.message;
        let roomID = data.roomID;
        if (message && roomID) {
            let msg = db.createMessage(user.ID, roomID, message);
            io.to(roomID).emit('message', msg)
        }

    });

    client.on('addFriend', (data) => {
        let user;
        try {
            user = jwt.verify(data.token, JWT_SECRET);
        } catch (err) {
            client.emit('invalidToken');
            return;
        }

        let friendID = data.friendID;
        db.addFriend(user.ID, friendID);

    });

    client.on('getFriends', (data) => {
        let user;

        try {
            user = jwt.verify(data.token, JWT_SECRET);
        } catch (err) {
            client.emit('invalidToken');
            return;
        }

        client.emit("getFriendsResponse", db.getFriends(user.ID));
        console.log(user.ID, db.getFriends(user.ID));
    });

    client.on('getRooms', (data) => {
        let user;

        try {
            user = jwt.verify(data.token, JWT_SECRET);
        } catch (err) {
            client.emit('invalidToken');
            return;
        }

        client.emit("getFriendsResponse", db.getRooms(user.ID));
    });

    client.on('joinRoom', (data) => {
        let user;
        try {
            user = jwt.verify(data.token, JWT_SECRET);
        } catch (err) {
            client.emit('invalidToken');
            return;
        }

        let roomID = data.roomID;
        if (roomID && db.userIsInRoom(user.ID, roomID)) {
            client.join(roomID);
        }

    });

    client.on('disconnect', function () {
        // Do something, maybe notify other users that this user is now offline.
    });

});

server.listen(3000);