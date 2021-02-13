const path = window.location.host;
let wsUrl = null;
let wsObj = null;
let userID = null;
let lockReconnect = false;
let wsCreateHandler = null;

function createWebSocket(userSessionID) {
    userID = userSessionID;
    wsUrl = "ws://" + host + '/websocket';
    try {
        wsObj = new WebSocket(wsUrl);
        initWsEventHandle();

    } catch (e) {
        writeToInfo("连接失败，开始重连");
        reconnect();
    }
}

function initWsEventHandle() {
    try {
        wsObj.onopen = function (evt) {
            onWsOpen(evt);
        };

        wsObj.onmessage = function (evt) {
            onWsMessage(evt);
        };

        wsObj.onclose = function (evt) {
            writeToInfo("连接已经关闭，开始重连");
            onWsClose(evt);
            reconnect();
        };

        wsObj.onerror = function (evt) {
            writeToInfo("连接出错，开始重连");
            onWsError(evt);
            reconnect();
        };
    } catch (e) {
        writeToInfo("事件绑定失败，开始重连");
        reconnect();
    }
}

function onWsOpen(evt) {

}

function onWsMessage(evt) {
    writeToInfo(evt.data);
}

function onWsError(evt) {

}

function onWsClose(evt) {

}

function writeToInfo(message) {
    $("#debug_info").text($("#debug_info").text() + "\n" + message);
}

function reconnect() {
    if (lockReconnect) {
        return;
    }
    let n = 10;
    writeToInfo(n + "秒后重连");
    lockReconnect = true;
    wsCreateHandler && clearTimeout(wsCreateHandler);
    wsCreateHandler = setTimeout(function () {
        writeToInfo("重连....." + wsUrl);
        createWebSocket();
        lockReconnect = false;
    }, n * 1000);
}

