'use strict';


let stompURL = null;
let stompClient = null;
let userID = null;
let lockReconnect = false;
let wsCreateHandler = null;

let map = null;
let viewer = null;

let graphicLayer = null;
let center_point = [119.75, 26.38, 1000];
let start_point1 = [120, 29, 1000];

//节点id转化为随时间的离散点
let id2property;
//节点id到cesium model的映射
let models;
//所有的节点id，随机得到一个节点时用到
let ids;
//id转化为后端传来的node信息
let id2Node;
//所有的分簇信息
let netsList;
//群的配置信息
let settingConfig;


let startTime;
let endTime;

let pathClusterArray = null;


let clock = null;


let nodeColors = [Cesium.Color.fromCssColorString("#ffffff"),
    Cesium.Color.fromCssColorString("#e60515"),
    Cesium.Color.fromCssColorString("rgba(28,89,202,0.64)"),
    Cesium.Color.fromCssColorString("rgb(99,221,93,0.64)"),
    Cesium.Color.fromCssColorString("rgb(226,213,53,0.64)"),
    Cesium.Color.fromCssColorString("rgba(191,106,22,0.57)"),
    Cesium.Color.fromCssColorString("rgb(8,100,105)")]

function initAllData() {
    //pathClusterArray = new Map();
    viewer.entities.removeAll();
    id2Node = new Map();
    id2property = new Map();
    models = new Map();
    ids = [];
    netsList = null;
    initSetting();
}
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
createWebSocket("cesium");
mapInit();
getDataFromServer();


//将路由信息显示
function addInfoShow(str, color = '') {
    let str_append = '<p style="color: ' + color + '">' + str + '</p>'
    $("#id_show_route_info")
        .prepend(str_append);
}

function getColorSpane(str, color = '') {
    return '<span style="color: ' + color + '">' + str + '</span>'
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

    let layer_net = new Cesium.WebMapTileServiceImageryProvider({
        url:
            "https://t0.tianditu.gov.cn/ibo_w/wmts?service=wmts&request=GetTile&version=1.0.0&LAYER=ibo&tileMatrixSet=w&TileMatrix={TileMatrix}&TileRow={TileRow}&TileCol={TileCol}&style=default&format=tiles&tk=8b207a527da69c7a32f636801fa194d4",
        layer: "tiandituImg",
        style: "default",
        format: "image/jpeg",
        tileMatrixSetID: "tiandituImg",
        maximumLevel: 16
    })
    //离线图层
    let layer_lixian = new Cesium.UrlTemplateImageryProvider({
        url: "http://127.0.0.1:8089/{z}/{x}/{y}.png",
    });

    let img2 = viewer.imageryLayers.addImageryProvider(layer_net);


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
                bottom: "100px",
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

function btnSubmitSettings() {
    if (!updateSettingConfig()) {
        return;
    }
    let sett = settingConfig;
    axios.post('/group/updateSetting', sett
    ).then((res) => {
        settingConfig = res.data;
        showSettingConfig(settingConfig, $("#id_setting_net_index").val());
        getDataFromServer();

        alert("修改配置成功")
    }).catch((error) => {
        alert("修改配置失败" + error)
    })
}


// 取消追踪节点
function clickCancelTrack() {
    viewer.trackedEntity = null;
}

// 模拟节点下一个运行位置，并开始运行
function clickStartDemo() {
    axios.get("/group/startNodeRun")
        .then((res) => {
            //getDataFromServer();
            map.clock.shouldAnimate = true;
            console.log(res);
        })
        .catch((error) => {
            console.log('ERROR', error);
        })
    if (clock == null) {
        clock = setInterval(() => {
            let len = ids.length;
            let c_i1 = Math.floor(Math.random() * len);
            let c_i2 = Math.floor(Math.random() * len);

            let start = ids[c_i1];
            let end = ids[c_i2];
            if (start !== end) {
                sendRequestPath(start, end);
            }

        }, 500);
    }


}


//停止运行
function clickStopDemo() {
    map.clock.shouldAnimate = false;
    clearInterval(clock);
    clock = null;
    axios.get("/group/endNodeRun")
        .then((res) => {
            console.log(res);
        })
        .catch((error) => {
            console.log('ERROR', error);
        })
}


// 得到两个节点之间的QOS最好的路径
function sendRequestPath(start, end) {
    $.ajax({
        url: "/group/getShortPath",
        type: "GET",
        dataType: "JSON",
        data: {
            start: start,
            end: end
        },
        error: function (error) {
            addInfoShow(getIPFromID(start) + '>' + getIPFromID(end) + ": 路径获取失败", "blue");
        },
        success(path) {
            //path = path.map(String);
            //console.log(path);
            let s1 = getColorSpane(getIPFromID(start) + '>' + getIPFromID(end) + ": <br/>", 'rgba(10,230,14,0.83)')
            if (path.length > 1) {
                let str = '';
                for (let i = 0; i < path.length; i++) {
                    if (i > 0) {
                        str += '->';
                    }
                    str += getIPFromID(path[i]);
                }
                str += ("  Hop:" + (path.length - 1));
                //console.log(str);
                str = getColorSpane(str, '#c7e7c6');
                addInfoShow(s1 + str);
            } else {
                addInfoShow(s1 + "无路径", 'red')
            }
            showCountTransmitTable();
        }
    });
}

// 随机破坏一个节点
function randomDestroyNode() {
    // 为给定 ID 的 user 创建请求
    axios.get('/group/destroyNode', {
        params: {
            n_id: 1
        }
    }).then((res) => {
        let index = res.data;
        if (index === -1) {
            addInfoShow("随机故障节点失败", "orange");
        } else {
            addInfoShow("故障节点" + index, "red");
        }
        console.log(res);
    }).catch((error) => {
        console.log('ERROR', error);
        addInfoShow("随机故障节点失败", "orange");
    })

}

//将请求的数据显示出来
function addNetToCesium(data) {
    initAllData();
    console.log(data.length)
    addClusterHeadLine(data.length);
    netsList = data;
    for (let i = 0; i < data.length; i++) {
        //console.log(data[i]);
        data[i].nodeList.forEach((node) => {
            //console.log(node.type);
            let nodePositionProperty = new Cesium.SampledPositionProperty();
            let nodePosition = Cesium.Cartesian3.fromDegrees(
                node.longitude,
                node.latitude,
                node.height,
            );
            nodePositionProperty.addSample(startTime, nodePosition);

            let index = node.id;
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
                    color: nodeColors[node.type % 5 + 1],
                    scaleByDistance: new Cesium.NearFarScalar(1.5e2, 2.0, 1.5e7, 1.0),
                },
                label: {
                    text: "节点：" + index,
                    font: '16px Helvetica',
                    style: Cesium.LabelStyle.FILL_AND_OUTLINE,
                    fillColor: Cesium.Color.WHITE,
                    outlineColor: Cesium.Color.BLACK,
                    outlineWidth: 1,
                    verticalOrigin: Cesium.VerticalOrigin.BOTTOM,
                    pixelOffset: new Cesium.Cartesian2(0, -19),
                    scaleByDistance: new Cesium.NearFarScalar(1.5e2, 1.0, 3e6, 0.8),
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
            ids.push(index);
            id2Node[index] = node;
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
    let totalSeconds = 20000;
    endTime = Cesium.JulianDate.addSeconds(startTime, totalSeconds, new Cesium.JulianDate());
    viewer.clock.stopTime = endTime.clone();
    viewer.clock.shouldAnimate = false;

    let clusterHeadPositionProperty = [];
    console.log(len);
    len = pathClusterArray.length;
    for (let i = 0; i < len; i++) {

        const property1 = new Cesium.SampledPositionProperty();
        let len_path = pathClusterArray[i].length;

        for (let j = 0; j < len_path; j++) {
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
        // models["ch"+i] = ch1;
    }
    //initTree(models, 'id');

}


//根据websocket消息来更新结点信息
function updateNodeInfo(message) {
    let now = viewer.clock.currentTime;
    for (let i = 0; i < message.length; i++) {
        message[i].nodeList.forEach((node) => {
            //console.log(node);
            let index = node.id;
            let nodePosition = Cesium.Cartesian3.fromDegrees(
                node.longitude,
                node.latitude,
                node.height,
            );
            let timr = Cesium.JulianDate.addSeconds(now, 1, new Cesium.JulianDate());
            id2property[index].addSample(timr, nodePosition);
            let cesium_id = node.id;
            let entity = viewer.entities.getById(cesium_id);
            //console.log(entity);
            if (node.type === -1) {
                entity._point._color._value = nodeColors[node.type + 1];
            }
            let isShowPath = document.getElementById('id_switch_path');
            entity.path.show = isShowPath.checked;
            //console.log(nodeColors[node.type + 1]);
        })
    }
}

function getIPFromID(id) {
    return id2Node[id].ip;
}


function showSettingConfig(setting, index) {
    $("#id_total_fangzhen").text(setting.numOfBadNode);
    $("#id_setting_spped").val(setting.speed);
    $("#id_setting_endPos").val(setting.endPos);
    $("#id_setting_destroyRate").val(setting.destroyRate);
    $("#id_setting_startPos").val(setting.startPos.slice(index * 3, index * 3 + 3));
    $("#id_setting_flyTime").val(setting.flyTime[index]);
    $("#id_setting_maxHeight").val(setting.maxHeight[index]);
    $("#id_td_end_position").text(setting.endPos);
}

// 显示与隐藏业务统计窗口
function showCountTranmit() {
    $("#id_show_count_transmit").toggle();
}

function showSettingTable() {
    $("#id_setting").toggle();
}

//更新业务统计数据
function showCountTransmitTable() {
    axios.get("/group/getCountTransmit")
        .then((res) => {

            let data = res.data;
            let ans_str = '';

            let sendSum = data.sendSum;
            let recvSum = data.recvSum;
            let sendNode = data.send;
            let recvNode = data.recv;
            let tr_rate = 0;
            //console.log(sendNode);
            for (let id in sendNode) {
                // console.log(id);
                //console.log(sendNode[id]);
                let sendCount = sendNode[id];
                let recvCount = recvNode[id];
                if (recvCount === null) {
                    recvCount = 0;
                }
                tr_rate = 0;
                if (sendCount > 0) {
                    tr_rate = recvCount / sendCount;
                }
                ans_str += '<tr>'
                    + '<td>节点' + getIPFromID(id) + '</td>'
                    + '<td>' + sendCount + '</td>'
                    + '<td>' + recvCount + '</td>'
                    + '<td>' + (tr_rate * 100).toFixed(2) + '%</td>' + '</tr>';
            }

            $("#id_show_count_transmit_tbody").html(ans_str);

            $("#id_td_total_send").text(sendSum[0]);
            $("#id_td_total_recv").text(recvSum[0]);
            let a = 0;
            if (sendSum[0] > 0) {
                a = recvSum[0] / sendSum[0] * 100;
            }
            $("#id_td_total_rate").text((a).toFixed(2));

            $("#id_td_total_send_cunei").text(sendSum[1]);
            $("#id_td_total_recv_cunei").text(recvSum[1]);
            a = 0;
            if (sendSum[1] > 0) {
                a = recvSum[1] / sendSum[1] * 100;
            }
            $("#id_td_total_rate_cunei").text((a).toFixed(2));

            $("#id_td_total_send_cujian").text(sendSum[2]);
            $("#id_td_total_recv_cujian").text(recvSum[2]);
            a = 0;
            if (sendSum[2] > 0) {
                a = recvSum[2] / sendSum[2] * 100;
            }
            $("#id_td_total_rate_cujian").text((a).toFixed(2));
        })
        .catch((err) => {
            console.log(err);
        })
}

// 从后台得到数据
function getDataFromServer() {
    addInfoShow("初始化成功");
    axios.all([axios.get('/group/getAllNets'), axios.get('/group/getClusterPathList')])
        .then(axios.spread((res1, res2) => {
            pathClusterArray = res2.data;
            addNetToCesium(res1.data);
        })).catch((error) => {
        console.log('ERROR', error);
    })
    viewer.zoomTo(viewer.entities);
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

    for (let i = 0; i < netsList.length; i++) {
        let pnode = {
            id: "c" + i,
            name: "簇" + i,
            type: 'group',
            open: false,
            checked: true,
            icon: 'icons/folder1.png',
        }
        zNodes.push(pnode)
    }

    for (let i = 0; i < netsList.length; i++) {
        let nodes = netsList[i].nodeList;
        //console.log(nodes);
        for (let j = 0; j < nodes.length; j++) {
            let id = nodes[j].id;
            let name = nodes[j].ip;
            //let chId = models[name.split('_')[0]][nameColum];

            let node = {
                id: id,
                pId: "c" + i,
                name: name,
                checked: true,
                icon: 'icons/layer1.png',
            }
            zNodes.push(node);
        }


        //layersObj[i] = item._entity;
    }
    //console.log(zNodes);
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
    entity.viewFrom = new Cesium.Cartesian3(0, 0, 80000)
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
    //console.log("---------" + message);
    if (message === "stop") {
        clickStopDemo();
        return;
    }
    if (message.indexOf("init") === 0) {
        let ds = message.split(",");
        $("#id_xuhao_fangzhen").text(ds[1]);
        //getDataFromServer();
        return;
    }
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


function initSetting() {

    axios.get("/group/getSetting")
        .then((res) => {
            settingConfig = res.data;
            showSettingConfig(settingConfig, 0);
            $('#id_setting_net_index').find('option').remove();
            for (let i = 0; i < settingConfig.flyTime.length; i++) {
                $('#id_setting_net_index').append('<option value="' + i + '">' + ("net" + i) + '</option>')
            }
        })
        .catch((error) => {
            alert(error + "请求失败");
        })

}


function updateSettingConfig() {
    let speed = $("#id_setting_spped").val();
    if (speed < 1) {
        alert("运行倍速不正确(1-50)");
        return false;
    }
    let destroyRate = $("#id_setting_destroyRate").val();
    if (destroyRate < 0 || destroyRate > 100) {
        alert("故障比例不正确(0-100),0代表无故障节点，其他数字代表从1开始破坏相应比例节点数量进行重复仿真");
        return false;
    }
    let endPos = splitPos($("#id_setting_endPos").val(), ",，");
    if (!checkPos3(endPos)) {
        alert("结束坐标不正确!!");
        return false;
    }
    let startPos = splitPos($("#id_setting_startPos").val(), ",，");
    if (!checkPos3(startPos)) {
        alert("结束坐标不正确!!");
        return false;
    }
    let maxHeight = $("#id_setting_maxHeight").val();
    if (maxHeight < 1) {
        alert("运行最大高度不正确");
        return false;
    }
    let flyTime = $("#id_setting_flyTime").val();
    if (flyTime < 1) {
        alert("运行时间不正确");
        return false;
    }
    let index = $("#id_setting_net_index").val();
    settingConfig.speed = speed;
    settingConfig.destroyRate = destroyRate;
    settingConfig.endPos = endPos;
    for (let i = 0; i < 3; i++) {
        settingConfig.startPos[index * 3 + i] = startPos[i];
    }
    settingConfig.maxHeight[index] = maxHeight;
    settingConfig.flyTime[index] = flyTime;
    return true;
}

function splitPos(val, s) {
    let ans = [];
    let copy = val.split(s.charAt(0));
    for (let i = 0; i < copy.length; i++) {
        let copy_copy = copy[i].split(s.charAt(1));
        for (let j = 0; j < copy_copy.length; j++) {
            ans.push(copy_copy[j]);
        }
    }
    return ans;
}

function checkPos3(Pos) {
    let re = /^[0-9]+.?[0-9]*$/;
    // console.log(Pos)
    if (Pos.length === 3 || Pos.length === 2) {
        for (let i = 0; i < Pos.length; i++) {
            if (!re.test(Pos[i])) {
                return false;
            }
        }

    } else {
        return false;
    }
    return true;
}

function settingSelecOnChanged(obj) {
    let index = obj.value;
    showSettingConfig(settingConfig, index);
}

