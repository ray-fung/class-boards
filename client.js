
const
    io = require("socket.io"),
    ioClient = io.connect("http://attu6.cs.washington.edu:12346")

ioClient.on("seq-num", (msg) => console.info(msg));

function setup_connection() {
    var server_socket = new io.Socket();
    socket.connect('https://127.0.0.1:12345');

    // Add a connect listener
    socket.on('connect',function() {
        console.log('Client has connected to the server!');
    });
    // Add a connect listener
    socket.on('message',function(data) {
        console.log('Received a message from the server!',data);
    });
    // Add a disconnect listener
    socket.on('disconnect',function() {
        console.log('The client has disconnected!');
    });
  
    // Sends a message to the server via sockets
    function sendMessageToServer(message) {
        socket.send(message);
    };
}

function setup() {
    var course_button = document.getElementById("course_button")
    var name_button = document.getElementById("name_button")
    var send_button = document.getElementById("send_button")

    course_button.addEventListener("click", test);
    name_button.addEventListener("click", choose_dept)
}

function test() {
    document.getElementById("demo").innerHTML = "Testing!";
}

function choose_dept() {

}

function send_message() {

}
