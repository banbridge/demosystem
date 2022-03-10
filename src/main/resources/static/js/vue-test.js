'use strict';


let stompURL = null;
let stompClient = null;
let userID = null;
let lockReconnect = false;
let wsCreateHandler = null;

let map = null;
let viewer = null;
let models;
let graphicLayer = null;
let center_point = [119.75, 26.38, 1000];
let start_point1 = [120, 29, 1000];

let id2property;

let startTime;
let endTime;

let perCellWidth = 0.05;
let visitPointIndex = 0;

let pathClusterArray = null;
let nodeClusterList = null;

let clock = null;

let nodeColors = [Cesium.Color.DARKGREY,
    Cesium.Color.CRIMSON,
    Cesium.Color.CORNFLOWERBLUE];

function initAllData() {
    //pathClusterArray = new Map();
    nodeClusterList = null;
    id2property = new Map();
    models = new Map();
}

createWebSocket("cesium");

Date.prototype.format = function (fmt) {
    let o = {
        "M+": this.getMonth() + 1, //月份
        "d+": this.getDate(), //日
        "h+": this.getHours() % 12 === 0 ? 12 : this.getHours() % 12, //小时
        "H+": this.getHours(), //小时
        "m+": this.getMinutes(), //分
        "s+": this.getSeconds(), //秒
        "q+": Math.floor((this.getMonth() + 3) / 3), //季度
        "S": this.getMilliseconds() //毫秒
    };
    let week = {
        "0": "\u65e5",
        "1": "\u4e00",
        "2": "\u4e8c",
        "3": "\u4e09",
        "4": "\u56db",
        "5": "\u4e94",
        "6": "\u516d"
    };
    if (/(y+)/.test(fmt)) {
        fmt = fmt.replace(RegExp.$1, (this.getFullYear() + "").substr(4 - RegExp.$1.length));
    }
    if (/(E+)/.test(fmt)) {
        fmt = fmt.replace(RegExp.$1, ((RegExp.$1.length > 1) ? (RegExp.$1.length > 2 ? "\u661f\u671f" : "\u5468") : "") + week[this.getDay() + ""]);
    }
    for (let k in o) {
        if (new RegExp("(" + k + ")").test(fmt)) {
            fmt = fmt.replace(RegExp.$1, (RegExp.$1.length == 1) ? (o[k]) : (("00" + o[k]).substr(("" + o[k]).length)));
        }
    }
    return fmt;
};

mapInit();

//将路由信息显示
function addInfoShow(str, color = '') {
    let str_append = '<p style="color: ' + color + '">' + str + '<p>'
    $("#id_show_route_info")
        .prepend(str_append);
}


// 3D可视化参数初始化
function mapInit() {
    Cesium.Ion.defaultAccessToken =
        'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJqdGkiOiIzZDYxZDFlNS1kZWQ4LTQ5YTgtOGU1OS1mOGEwMzQ5ZWQxYTAiLCJpZCI6NTU2NTIsImlhdCI6MTYyMDgwNzEzN30.gfK2CAcCL_rqe3u4IWm6aOTa08U39DO8c7MK5ASJtyc';
    viewer = new Cesium.Viewer('container', {
        animation: true, // [ Bool, 是否显示动画控件 ]
        shouldAnimate: false, // [ Bool, 是否开启动画 ]
        timeline: false, // [ Bool, 是否显示时间线控件 ]
        requestRenderMode: true, // [ Bool, 启用请求渲染模式 ]
        sceneModePicker: false, // [ Bool, 是否显示场景切换控件 ]
        // terrainProvider: Cesium.createWorldTerrain({
        //     requestVertexNormals: true
        // })
    });
    // 隐藏Cesium自身的logo
    viewer._cesiumWidget._creditContainer.style.display = "none";
    //viewer.scene.globe.enableLighting = true;

    // 清除默认的第一个影像 bing地图影像
    viewer.imageryLayers.remove(viewer.imageryLayers.get(0))
    // mapbox影像
    let img = viewer.imageryLayers.addImageryProvider(
        new Cesium.MapboxStyleImageryProvider({
            styleId: 'satellite-v9',
            accessToken: 'pk.eyJ1IjoiYmFuYnJpZGdlIiwiYSI6ImNrbm9jbWZwODEyeWkyd3FqeWlrMjBpNDkifQ.IKOlC_ndL5W8Lpa_XAVmJA'
        })
    );

    map = new mars3d.Map(viewer, {
        scene: {
            center: {
                x: center_point[0],
                y: center_point[1],
                z: center_point[2] + 100000,
                heading: 120.6,
                pitch: -55.3,
                roll: 0.6
            },
            showSkyBox: false,
            showSkyAtmosphere: false, //关闭球周边的白色轮廓 map.scene.skyAtmosphere = false
            fog: false,
            fxaa: false,
            globe: {
                showGroundAtmosphere: false, //关闭大气（球表面白蒙蒙的效果）
                depthTestAgainstTerrain: false,
                baseColor: '#546a53',
            },
            cameraController: {
                zoomFactor: 3.0,
                minimumZoomDistance: 1,
                maximumZoomDistance: 50000000,
                enableRotate: true,
                enableZoom: true,
            },
        },
        control: {
            homeButton: true,
            baseLayerPicker: true,
            sceneModePicker: true,
            sceneMode: 3, // [ Number,初始场景模式 1 2D模式 2 2D循环模式 3 3D模式  Cesium.SceneMode ]
            fullscreenElement: document.body, // [ Object, 全屏时渲染的HTML元素 ]

            vrButton: false,
            fullscreenButton: true,
            navigationHelpButton: true,
            animation: true,
            shouldAnimate: false,
            timeline: false,
            infoBox: false,
            geocoder: false,
            geocoderConfig: {
                key: [
                    "ae29a37307840c7ae4a785ac905927e0"
                ]
            },
            defaultContextMenu: true,
            mouseDownView: false,
            compass: {
                top: "10px",
                right: "5px"
            },
            distanceLegend: {
                left: "100px",
                bottom: "2px"
            },
            locationBar: {
                fps: true,
                crs: "CGCS2000_GK_Zone_3",
                crsDecimal: 0,
                template: "<div>经度:{lng}</div> <div>纬度:{lat}</div> <div>海拔：{alt}米</div> <div>层级：{level}</div><div>方向：{heading}度</div> <div>俯仰角：{pitch}度</div><div>视高：{cameraHeight}米</div>"
            }
        },

    });
    graphicLayer = new mars3d.layer.GraphicLayer();
    map.addLayer(graphicLayer);
    map.clock.shouldAnimate = false;

}


// 取消追踪节点
function clickCancelTrack() {
    viewer.trackedEntity = null;
}


// 模拟节点下一个运行位置，并开始运行
function clickStartDemo() {
    axios.get("/test/startNodeRun")
        .then((res) => {
            console.log(res);
        })
        .catch((error) => {
            console.log('ERROR', error);
        })
    if (clock == null) {
        clock = setInterval(() => {
            visitPointIndex++;
            let lenOfCluster1 = nodeClusterList[0].nodeList.length - 1;
            let start = Math.round(Math.random() * lenOfCluster1);
            let end = Math.round(Math.random() * lenOfCluster1);
            if (start != end) {
                sendRequestPath(start, end);
            }

        }, 1000);
    }
    map.clock.shouldAnimate = true;

}


// 得到两个节点之间的第一条路径
function sendRequestPath(start, end) {
    $.ajax({
        url: "/test/getShortPath",
        type: "GET",
        dataType: "JSON",
        data: {
            start: start,
            end: end
        },
        error: function (error) {
            addInfoShow("路径获取失败");
        },
        success(path) {
            //path = path.map(String);
            //console.log(path);
            let s1 = (start + '>' + end + ": ")
            if (path.length > 1) {
                let str = '';

                str += s1;
                for (let i = 0; i < path.length; i++) {
                    if (i > 0) {
                        str += '->';
                    }
                    str += path[i];
                }
                str += ("  Hop:" + (path.length - 1));
                //console.log(str);
                addInfoShow(str);
            } else {
                addInfoShow(s1 + "无路径", 'red')
            }
            showCountTransmitTable();

        }
    });
}

//停止运行
function clickStopDemo() {
    map.clock.shouldAnimate = false;
    clearInterval(clock);
    clock = null;
    axios.get("/test/endNodeRun")
        .then((res) => {
            console.log(res);
        })
        .catch((error) => {
            console.log('ERROR', error);
        })
}

// 随机破坏一个节点
function randomDestroyNode() {
    let c_id = Math.round(Math.random() * nodeClusterList.length);
    let lenOfCluster1 = nodeClusterList[0].nodeList.length - 1;
    let n_id = Math.round(Math.random() * lenOfCluster1);
    let index = getIdFromC_N(c_id, n_id);
    // 为给定 ID 的 user 创建请求
    axios.get('/test/destroyNode', {
        params: {
            c_id: c_id,
            n_id: n_id
        }
    }).then((res) => {
        addInfoShow("删除节点" + index, "red");
        console.log(res);
    }).catch((error) => {
        console.log('ERROR', error);
        addInfoShow("删除节点" + index + '失败', "orange");
    })

}

//将请求的数据显示出来
function addNetToCesium(data) {
    initAllData();
    nodeClusterList = data;
    console.log(nodeClusterList.length)
    addClusterHeadLine(data.length);
    for (let i = 0; i < data.length; i++) {
        //console.log(data[i]);
        data[i].nodeList.forEach((node) => {
            let nodePositionProperty = new Cesium.SampledPositionProperty();
            let nodePosition = Cesium.Cartesian3.fromDegrees(
                node.longitude,
                node.latitude,
                node.height,
            );
            nodePositionProperty.addSample(startTime, nodePosition);

            let index = getIdFromC_N(i, node.id)
            id2property[index] = nodePositionProperty;
            let entity = viewer.entities.add({
                id: index,
                availability: new Cesium.TimeIntervalCollection([new Cesium.TimeInterval({
                    start: startTime,
                    stop: endTime
                })]),
                position: nodePositionProperty,
                point: {
                    pixelSize: 12,
                    color: nodeColors[node.type + 1],
                    scaleByDistance: new Cesium.NearFarScalar(1.5e2, 2.0, 1.5e7, 0.5),
                },
                label: {
                    text: node.ip,
                    font: '6pt monospace',
                    style: Cesium.LabelStyle.FILL_AND_OUTLINE,
                    outlineWidth: 2,
                    verticalOrigin: Cesium.VerticalOrigin.BOTTOM,
                    pixelOffset: new Cesium.Cartesian2(0, -19)
                },
                path: {
                    resolution: 1,
                    material: new Cesium.PolylineGlowMaterialProperty({
                        glowPower: 0.1,
                        color: Cesium.Color.CORNSILK,
                    }),
                    width: 5,
                },
            });
            models[index] = entity;
            entity.position.setInterpolationOptions({
                interpolationDegree: 2,
                interpolationAlgorithm: Cesium.HermitePolynomialApproximation,
            });
        });
    }

    initTree(models, 'id');


}


// 添加每个簇整体的轨迹线
function addClusterHeadLine(len) {

    startTime = viewer.clock.currentTime;
    let totalSeconds = 60 * 10;
    endTime = Cesium.JulianDate.addSeconds(startTime, totalSeconds, new Cesium.JulianDate());
    viewer.clock.stopTime = endTime.clone();
    viewer.clock.shouldAnimate = false;

    let clusterHeadPositionProperty = [];

    for (let i = 0; i < len; i++) {
        const property1 = new Cesium.SampledPositionProperty();

        for (let j = 0; j < pathClusterArray[i].length; j++) {
            let time = Cesium.JulianDate.addSeconds(startTime, j, new Cesium.JulianDate());
            let p_point = pathClusterArray[i][j];
            let p_point_ = Cesium.Cartesian3.fromDegrees(
                p_point[0],
                p_point[1],
                p_point[2],
            );
            property1.addSample(time, p_point_);
        }
        clusterHeadPositionProperty.push(property1)
    }


    let pathLineFuture = {
        resolution: 1,
        material: new Cesium.PolylineGlowMaterialProperty({
            glowPower: 0.1,
            color: Cesium.Color.BROWN,
        }),
        width: 10,
    };

    for (let i = 0; i < clusterHeadPositionProperty.length; i++) {
        //console.log( clusterHeadPositionProperty[i])
        let ch1 = viewer.entities.add({
            id: 'ch' + i,
            availability: new Cesium.TimeIntervalCollection([new Cesium.TimeInterval({
                start: startTime,
                stop: endTime
            })]),
            position: clusterHeadPositionProperty[i],
            orientation: new Cesium.VelocityOrientationProperty(clusterHeadPositionProperty[i]),
            path: pathLineFuture,
        });
        ch1.position.setInterpolationOptions({
            interpolationDegree: 2,
            interpolationAlgorithm: Cesium.HermitePolynomialApproximation,
        });
        models[i + ""] = ch1;
    }
    //initTree(models, 'id');

}


//根据websocket消息来更新结点信息
function updateNodeInfo(message) {
    let now = viewer.clock.currentTime;
    for (let i = 0; i < message.length; i++) {
        message[i].nodeList.forEach((node) => {
            //console.log(node);
            let index = getIdFromC_N(i, node.id);
            let nodePosition = Cesium.Cartesian3.fromDegrees(
                node.longitude,
                node.latitude,
                node.height,
            );
            let timr = Cesium.JulianDate.addSeconds(now, 1, new Cesium.JulianDate());
            id2property[index].addSample(timr, nodePosition);
            let cesium_id = getIdFromC_N(i, i);
            let entity = viewer.entities.getById(cesium_id);
            //console.log(entity);
            entity.point.color = nodeColors[node.type + 1];
            console.log(nodeColors[node.type + 1]);
        })
    }
}

function getIdFromC_N(i, id) {
    return i + "_" + id;
}

// 实时显示某个节点的信息
function showPosition(entity, p) {
    $('#td_time').html(Cesium.JulianDate.toDate(map.clock.currentTime).format('yyyy-MM-dd hh:mm:ss'))
    if (entity.position) {
        let point = mars3d.LatLngPoint.fromCartesian(entity.position);
        $('#td_x').html(point.lng);
        $('#td_y').html(point.lat);
        $('#td_z').html(point.alt);
        $('#td_position').html(point.toString());
    }

}

function initTree(arr, nameColum = 'name') {
    //初始化树
    let setting = {
        check: {
            enable: true,
        },
        data: {
            simpleData: {
                enable: true,
            },
        },
        callback: {
            onCheck: treeOverlays_onCheck,
            onClick: treeOverlays_onClick,
        },
    }

    let zNodes = []

    for (let i = 0; i < nodeClusterList.length; i++) {
        let pnode = {

            id: models[i][nameColum],
            name: models[i][nameColum],
            type: 'group',
            open: false,
            checked: true,
            icon: 'icons/folder1.png',
        }
        zNodes.push(pnode)
    }

    for (let i = nodeClusterList.length, len = arr.length; i < len; i++) {

        let item = arr[i]
        let name = item[nameColum] || '未命名'
        let chId = models[name.split('_')[0]][nameColum];

        let node = {
            id: i,
            pId: chId,
            name: name,
            checked: true,
            icon: 'icons/layer1.png',
        }
        zNodes.push(node);
        //layersObj[i] = item._entity;
    }

    $.fn.zTree.destroy()
    $.fn.zTree.init($('#treeOverlays'), setting, zNodes)
}

function treeOverlays_onCheck(e, treeId) {
    let zTree = $.fn.zTree.getZTreeObj(treeId)

    //获得所有改变check状态的节点
    let changedNodes = zTree.getChangeCheckedNodes()
    for (let i = 0; i < changedNodes.length; i++) {
        let treeNode = changedNodes[i]
        treeNode.checkedOld = treeNode.checked
        let entity = models[treeNode.id];
        if (entity == null) {
            continue
        }
        let show = treeNode.checked
        //console.log(entity);
        //处理图层显示隐藏
        entity.show = show
        if (entity._labelEx) {
            entity._labelEx.show = show
        }
    }
}

function treeOverlays_onClick(event, treeId, treeNode) {
    let entity = models[treeNode.id]
    if (entity == null) {
        return
    }
    viewer.trackedEntity = entity;
    entity.viewFrom = new Cesium.Cartesian3(-2080, -1715, 20000)
}

// 创建websocket与后台交互数据
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

// 显示与隐藏业务统计窗口
function showCountTranmit() {
    $("#id_show_count_transmit").toggle();
}

//更新业务统计数据
function showCountTransmitTable() {
    axios.get("/test/getCountTransmit")
        .then((res) => {

            let data = res.data;
            let ans_str = '';
            //console.log(data);
            let send = data.send;
            let recv = data.recv;
            for (let i = 0; i < send.length; i++) {
                let tr_rate = 0;
                if (send[i] > 0) {
                    tr_rate = recv[i] / send[i];
                }
                ans_str += '<tr>'
                    + '<td>节点' + i + '</td>'
                    + '<td>' + send[i] + '</td>'
                    + '<td>' + recv[i] + '</td>'
                    + '<td>' + (tr_rate * 100).toFixed(2) + '%</td>' + '</tr>';
            }
            $("#id_show_count_transmit_tbody").html(ans_str);
            $("#id_td_total_send").text(data.sendSum);
            $("#id_td_total_recv").text(data.recvSum);
            $("#id_td_total_rate").text((data.recvSum / data.sendSum * 100).toFixed(2));
        })
        .catch((err) => {
            console.log(err);
        })
}

// 从后台得到数据
function getDataFromServer() {

    axios.all([axios.get('/test/getAllNets'), axios.get('/api/getClusterPathList')])
        .then(axios.spread((res1, res2) => {
            pathClusterArray = res2.data;
            addNetToCesium(res1.data);
        })).catch((error) => {
        console.log('ERROR', error);
    })

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
    updateNodeInfo(message);
    //writeToInfo(message);
}

function onWsError(evt) {

}

function onWsClose(evt) {

}

function writeToInfo(message) {
    //$("#greetings").append("<tr><td>" + message + "</td></tr>");
    //console.log(message);
}

function reconnect() {
    if (lockReconnect) {
        return;
    }
    let n = 10;
    writeToInfo(n + "秒后重连");
    lockReconnect = true;
    wsCreateHandler && clearTimeout(wsCreateHandler);
    wsCreateHandler = setTimeout(() => {
        writeToInfo("重连....." + stompURL);
        createWebSocket();
        lockReconnect = false;
    }, n * 1000);
}


function getRndInteger(min, max) {
    return Math.floor(Math.random() * (max - min)) + min;
}