const path = window.location.host;
let stompURL = null;
let stompClient = null;
let userID = null;
let lockReconnect = false;
let wsCreateHandler = null;
let chartDom = null;
let option_node = null;
let myChart_node = null;
let selected_node = 0;
let chartDom_net = null;
let myChart_net = null;
let width;
let height;
let colorls = ["#f1ddce", "#d9e7d1", "#0eaae6"];
let types = ["A类子网节点", "B类子网节点", "C类子网节点"];
const lineDash = [4, 2, 1, 2];

let tooltip;

function initAnalyse() {
    tooltip = new G6.Tooltip({
        offsetX: 70,
        offsetY: 40,
        getContent(e) {
            const outDiv = document.createElement('span');
            outDiv.innerHTML = `
        <li>ip: ${e.item.getModel().label}</li>
        <li>负载量: ${e.item.getModel().cap}%</li>
        <li>节点类型: ${types[e.item.getModel().class]}</li>`
            return outDiv
        },
        itemTypes: ['node']
    });
    analyseNode();
    analyseNet()
    createWebSocket("qwe");

}

function createWebSocket(userSessionID) {

    userID = userSessionID;
    stompURL = '/stomp';
    try {
        if (stompClient == null) {
            let socket = new SockJS(stompURL);
            stompClient = Stomp.over(socket);
            stompClient.connect({}, function (info) {
                console.log(info)
                stompClient.subscribe("/all/greeting", function (response) {
                    onWsMessage(response.body);
                });
            });

        }

    } catch (e) {
        writeToInfo("连接失败，开始重连:" + e);
        console.log(e)
        reconnect();
    }
}

window.onunload = function () {
    disconnect()
}

function disconnect() {

    if (stompClient != null) {
        stompClient.disconnect();
    }
    stompClient = null;
    console.log("Disconnect");
}

function sendName() {
    //stompClient.send("/ws/hellostomp", {}, $("#name").val());
}

function onWsOpen(evt) {

}


function onWsMessage(message) {
    message = JSON.parse(message);
    updateNet(message);
    updateNode(message);
    writeToInfo(message);
}

function onWsError(evt) {

}

function onWsClose(evt) {

}

function writeToInfo(message) {
    //$("#greetings").append("<tr><td>" + message + "</td></tr>");
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
        writeToInfo("重连....." + stompURL);
        createWebSocket();
        lockReconnect = false;
    }, n * 1000);
}


function analyseNode() {
    chartDom = document.getElementById("g_id_node");
    myChart_node = echarts.init(chartDom)
    option_node = {
        title: {
            text: '节点分析'
        },
        series: [{
            type: 'gauge',
            progress: {
                show: true,
                width: 18
            },
            axisLine: {
                lineStyle: {
                    width: 18
                }
            },
            axisTick: {
                show: false
            },
            splitLine: {
                length: 15,
                lineStyle: {
                    width: 2,
                    color: '#999'
                }
            },
            axisLabel: {
                distance: 25,
                color: '#999',
                fontSize: 20
            },
            anchor: {
                show: true,
                showAbove: true,
                size: 25,
                itemStyle: {
                    borderWidth: 10
                }
            },
            title: {
                show: false
            },
            detail: {
                valueAnimation: true,
                fontSize: 60,
                offsetCenter: [0, '70%']
            },
            data: [{
                value: 70
            }]
        }]
    }

    myChart_node.setOption(option_node);
}

function analyseNet() {
    chartDom_net = document.getElementById("g_id_net");
    width = chartDom_net.scrollWidth;
    height = chartDom_net.scrollHeight;
    myChart_net = new G6.Graph({
        container: "g_id_net",
        width,
        height,
        linkCenter: true,
        modes: {
            default: [
                'drag-canvas',
                'zoom-canvas',
                'drag-node',
                'click-select',
            ],
            edit: []
        },
        defaultNode: {
            size: 30,
            style: {
                stroke: '#686f77',
                lineWidth: 1,
            }
        },
        defaultEdge: {
            type: 'line-dash',
            style: {
                lineWidth: 1,
                stroke: "rgb(93,93,93)",
            },
        },
        plugins: [tooltip],
        layout: {
            type: 'fruchterman',
            gravity: 5,
            speed: 5,
            preventOverlap: true,
        },

    });

    const data = {
        nodes: [
            {
                id: '0',
                label: '0',
                cluster: 'a',
            },
            {
                id: '1',
                label: '1',
                cluster: 'a',
            },
        ],
        edges: [
            {
                source: '0',
                target: '1',
            },

        ],
    };
    myChart_net.data(data);
    myChart_net.render();

    // 点击节点
    myChart_net.on('node:click', (e) => {
        // 先将所有当前是 click 状态的节点置为非 click 状态
        const clickNodes = myChart_net.findAllByState('node', 'click');
        clickNodes.forEach((cn) => {
            myChart_net.setItemState(cn, 'click', false);
        });
        const nodeItem = e.item; // 获取被点击的节点元素对象
        console.log(nodeItem._cfg.id);
        selected_node = nodeItem._cfg.id;
        myChart_net.setItemState(nodeItem, 'click', true); // 设置当前节点的 click 状态为 true
    });

    myChart_net.on('node:mouseenter', (e) => {
        const nodeItem = e.item;
        myChart_net.setItemState(nodeItem, 'hover', true);
    });

    myChart_net.on('node:mouseleave', (e) => {
        const nodeItem = e.item;
        myChart_net.setItemState(nodeItem, 'hover', false);
    });


}


function updateNet(net) {
    let nodes = new Array();
    let edges = new Array();

    net.nodeList.forEach(function (node) {
        nodes.push({
            id: node.id + "",
            label: node.ip,
            class: node.type + "",
            cluster: node.type + "",
            x: node.x * width / 100,
            y: node.y * height / 100,
            cap: node.capacity
        })
    })

    nodes.forEach(function (node) {
        if (!node.style) {
            node.style = {};
        }
        switch (node.class) {
            case "0":
                node.style.fill = colorls[0]
                break
            case "1":
                node.style.fill = colorls[1]
                break;
            case "2":
                node.style.fill = colorls[2]
                break;
        }
    });

    net.edgeList.forEach(function (edge) {
        edges.push({
            source: edge.from + "",
            target: edge.to + "",
        })
    });

    const initData = {
        nodes: nodes,
        edges: edges,
    }

    myChart_net.changeData(initData);

}


function updateNode(net) {
    option_node.series[0].data[0].value = net.nodeList[selected_node].capacity;
    myChart_node.setOption(option_node);
}

