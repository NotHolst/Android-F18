const db = require('sqlite-sync');


var facade = {

    createUser(username, password, nickname) {
        let res = db.run(
            "INSERT INTO Users (Username, Password, Nickname, Status) VALUES (?,?,?,'Offline')",
            [username, password, nickname]
        )

        return res.error ? -1 : res;

    },

    /**
     * Authenticates and returns the user on success
     * @param {string} username 
     * @param {string} password 
     */
    auth(username, password) {
        let res = db.run("SELECT * FROM Users WHERE Username = ? AND Password = ?", [username, password]);
        return res.length == 1 ? res[0] : undefined;
    },

    createRoom(userID, otherUserID) {
        let roomID = db.run("INSERT INTO Rooms (ID) VALUES (null)");
        db.run("INSERT INTO RoomMemberships (UserID, RoomID) VALUES (?,?)", [userID, roomID]);
        db.run("INSERT INTO RoomMemberships (UserID, RoomID) VALUES (?,?)", [otherUserID, roomID]);
        return roomID;
    },

    addUserToRoom(userID, roomID) {
        let res = db.run("INSERT INTO RoomMemberships (UserID, RoomID) VALUES (?,?)", [userID, roomID]);
    },

    leaveRoom(userID, roomID) {
        let res = db.run("DELETE FROM RoomMemberships WHERE UserID = ? AND RoomID = ?", [userID, roomID]);
    },

    /**
     * Creates and returns the message on success
     */
    createMessage(userID, roomID, content) {
        let res = db.run(
            "INSERT INTO Messages (Sender, RoomID, Timestamp, Content) VALUES ("+userID+","+roomID+","+Date.now()+",'"+content+"')"
        );
        return res > 0 ? {
            Sender: userID,
            RoomID: roomID,
            Timestamp: Date.now(), // yes, this will be slightly different.
            Content: content
        } : undefined;
    },

    userExists(username) {
        let res = db.run("SELECT * FROM Users WHERE Username = ?", [username]);
        return res.length == 1;
    },

    user(userId) {
        let res = db.run("SELECT * FROM Users WHERE ID = ?", [userId]);
        return res.length == 1 ? res[0] : undefined;
    },

    userByUsername(username){
        let res = db.run("SELECT * FROM Users WHERE Username = ?", [username])
        return res.length == 1 ? res[0] : undefined;
    },

    addFriend(userOne, userTwo) {
        let res = db.run("INSERT INTO Friendships (UserOne, UserTwo) VALUES (?,?)",
            [userOne, userTwo]);
    },

    getFriends(userId) {
        let res = db.run(`
        SELECT u.ID, u.Username, u.Nickname, u.Status FROM Friendships f 
        INNER JOIN Users u on f.UserTwo = u.ID
        WHERE UserOne = `+ userId + ` 
        UNION
        SELECT u.ID, u.Username, u.Nickname, u.Status FROM Friendships f 
        INNER JOIN Users u on f.UserOne = u.ID
        WHERE UserTwo = ` + userId);
        return res.length > 0 ? res[0].values : undefined;
    },

    getRooms(userId) {
        let res = db.run(`
        SELECT m.RoomID, group_concat(u.Nickname, ', ') as Title, 
        (select Content from Messages where RoomID = m.RoomID ORDER BY Timestamp desc limit 1) as lastMessage
        FROM RoomMemberships m
        INNER JOIN Users u on m.UserID = u.ID
        WHERE RoomID in (Select RoomID from RoomMemberships WHERE UserID = ?)
        GROUP BY RoomID
        ` [userId]);
        return res;
    },
    userIsInRoom(userID, roomID){
        let res = db.run("SELECT * FROM RoomMemberships WHERE UserID = ? AND RoomID = ?", [userID, roomID])
        return res.length == 1;
    }
}

db.connect('database/database.db')
module.exports = facade;