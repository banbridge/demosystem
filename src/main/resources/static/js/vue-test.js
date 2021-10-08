'use strict';


let stompURL = null;
let stompClient = null;
let userID = null;
let lockReconnect = false;
let wsCreateHandler = null;

let map = null;
let viewer = null;
let models = [];
let graphicLayer = null;
let center_point = [121, 31, 10000];
let start_point1 = [120, 29, 10000];

let path_array1 = [];
let path_array2 = [];

let id2property = [];

let startTime;
let endTime;

path_array1.push(center_point);
path_array2.push(start_point1);
let perCellWidth = 0.05;
let visitPointIndex = 0;

let nodedata;

let clock = null;

let p;
for (let i = 1; i < 200; i++) {
    p = path_array1[i - 1];
    let p_point = [p[0] + 0.05, p[1], p[2] + 10];
    path_array1.push(p_point);
    p = path_array2[i - 1];
    p_point = [p[0] + 0.05, p[1], p[2] + 10];
    path_array2.push(p_point);
}

//createWebSocket("cesium");

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
                y: center_point[1],
                x: center_point[0],
                z: center_point[2] + 1000,
                heading: 13.6,
                pitch: -55.3,
                roll: 0.1
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
    //map.clock.shouldAnimate = false;
    testOther();
}


function testOther() {
    //showFeijiDemo();
    testPoint();
    //testPointLine();
}


function testPoint() {
    startTime = viewer.clock.currentTime;
    let totalSeconds = 60 * 60;
    endTime = Cesium.JulianDate.addSeconds(startTime, totalSeconds, new Cesium.JulianDate());
    viewer.clock.stopTime = endTime.clone();
    viewer.clock.shouldAnimate = false;

    const cluster1HeadPositionProperty = new Cesium.SampledPositionProperty();
    const cluster2HeadPositionProperty = new Cesium.SampledPositionProperty();

    for (let i = 0; i < path_array1.length; i++) {
        let time = Cesium.JulianDate.addSeconds(startTime, i, new Cesium.JulianDate());
        let timePosition1_ = Cesium.Cartesian3.fromDegrees(
            path_array1[i][0],
            path_array1[i][1],
            path_array1[i][2],
        );
        let timePosition2_ = Cesium.Cartesian3.fromDegrees(
            path_array2[i][0],
            path_array2[i][1],
            path_array2[i][2],
        );

        cluster1HeadPositionProperty.addSample(time, timePosition1_);
        cluster2HeadPositionProperty.addSample(time, timePosition2_);
        viewer.entities.add({
            description: `Location: (${path_array1[i][0]}, ${path_array1[i][1]}, ${path_array1[i][2]})`,
            position: timePosition1_,
            point: {
                pixelSize: 10,
                color: Cesium.Color.DARKORANGE,
                material: Cesium.Color.DODGERBLUE.withAlpha(0.7),
            }
        });

        // viewer.entities.add({
        //     description: `Location: (${path_array2[i][0]}, ${path_array2[i][1]}, ${path_array2[i][2]})`,
        //     position: timePosition2_,
        //     point: {
        //         pixelSize: 10,
        //         color: Cesium.Color.DARKORANGE,
        //         material: Cesium.Color.DODGERBLUE.withAlpha(0.7),
        //     }
        // });
    }

    let pathLineFuture = {
        resolution: 1,
        material: new Cesium.PolylineGlowMaterialProperty({
            glowPower: 0.1,
            color: Cesium.Color.YELLOW,
        }),
        width: 10,
    };

    let ch1 = viewer.entities.add({
        id: 'p1',
        availability: new Cesium.TimeIntervalCollection([new Cesium.TimeInterval({
            start: startTime,
            stop: endTime
        })]),
        position: cluster1HeadPositionProperty,
        orientation: new Cesium.VelocityOrientationProperty(cluster1HeadPositionProperty),
        model: {
            uri: "icons/Cesium_Air.glb",
            minimumPixelSize: 64,
        },
        label: {
            text: '簇首1',
            font: '10pt monospace',
            style: Cesium.LabelStyle.FILL_AND_OUTLINE,
            outlineWidth: 2,
            verticalOrigin: Cesium.VerticalOrigin.BOTTOM,
            pixelOffset: new Cesium.Cartesian2(0, -9)
        },
        path: pathLineFuture,
    });

    let ch2 = viewer.entities.add({
        id: 'p2',
        availability: new Cesium.TimeIntervalCollection([new Cesium.TimeInterval({
            start: startTime,
            stop: endTime
        })]),
        position: cluster2HeadPositionProperty,
        orientation: new Cesium.VelocityOrientationProperty(cluster2HeadPositionProperty),
        // point: {
        //     pixelSize : 12,
        //     color : Cesium.Color.ANTIQUEWHITE,
        //     scaleByDistance: new Cesium.NearFarScalar(1.5e2, 2.0, 1.5e7, 0.5),
        // },
        // label : {
        //     text : '簇首2',
        //     font : '10pt monospace',
        //     style: Cesium.LabelStyle.FILL_AND_OUTLINE,
        //     outlineWidth : 2,
        //     verticalOrigin : Cesium.VerticalOrigin.BOTTOM,
        //     pixelOffset : new Cesium.Cartesian2(0, -9)
        // },
        path: pathLineFuture,
    });
    ch1.position.setInterpolationOptions({
        interpolationDegree: 2,
        interpolationAlgorithm: Cesium.HermitePolynomialApproximation,
    });
    ch2.position.setInterpolationOptions({
        interpolationDegree: 2,
        interpolationAlgorithm: Cesium.HermitePolynomialApproximation,
    });

    models.push(ch1);
    models.push(ch2);


    initTree(models, 'id');

    $('#btnStart').click(() => {

        map.clock.shouldAnimate = true;
        if (clock == null) {

            clock = setInterval(() => {
                visitPointIndex++;
                console.log(id2property);
                if (visitPointIndex < path_array1.length) {
                    nodedata.nodeList.forEach((node) => {
                        let nodePosition = Cesium.Cartesian3.fromDegrees(
                            path_array1[visitPointIndex][0] + (node.x - 50) / 100 * perCellWidth,
                            path_array1[visitPointIndex][1] + (node.y - 50) / 100 * perCellWidth,
                            path_array1[visitPointIndex][2] + Math.random() * 100,
                        );
                        let timr = Cesium.JulianDate.addSeconds(startTime, visitPointIndex, new Cesium.JulianDate());
                        id2property[node.id].addSample(timr, nodePosition);
                        console.log(timr)
                    });
                } else {
                    console.log('stop', visitPointIndex)
                    clearInterval(clock);
                }
            }, 1000);
        }
    });

    $('#btnStop').click(() => {
        map.clock.shouldAnimate = false;
        clearInterval(clock);
        clock = null;
    });

    $('#btnViewLine').click(() => {

    });

    $('#btnCancelTrack').click(() => {
        viewer.trackedEntity = null;
    })

}


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
    let pnode1 = {
        id: -1,
        name: '群1',
        type: 'group',
        open: false,
        checked: true,
        icon: 'icons/folder1.png',
    }
    let pnode2 = {
        id: -2,
        name: '群2',
        type: 'group',
        open: false,
        checked: true,
        icon: 'icons/folder1.png',
    }
    zNodes.push(pnode1)
    zNodes.push(pnode2)

    for (let i = 0, len = arr.length; i < len; i++) {
        let item = arr[i]
        let name = item[nameColum] || '未命名'
        let node = {
            id: i,
            pId: i % 2 === 0 ? pnode1.id : pnode2.id,
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


function getDataFromServer() {

    axios.get('/getNetInfo').then((res) => {
        addNetToCesium(res.data);
    }).catch((error) => {
        console.log('ERROR', error);
    })

}

function addNetToCesium(data) {
    nodedata = data;
    console.log(data);
    data.nodeList.forEach((node) => {
        let nodePositionProperty = new Cesium.SampledPositionProperty();
        let nodePosition = Cesium.Cartesian3.fromDegrees(
            path_array1[visitPointIndex][0] + (node.x - 50) / 100 * perCellWidth,
            path_array1[visitPointIndex][1] + (node.y - 50) / 100 * perCellWidth,
            path_array1[visitPointIndex][2] + Math.random() * 100,
        );
        nodePositionProperty.addSample(startTime, nodePosition);

        let time = Cesium.JulianDate.addSeconds(startTime, 1, new Cesium.JulianDate());
        nodePosition = Cesium.Cartesian3.fromDegrees(
            path_array1[visitPointIndex + 1][0] + (node.x - 50) / 100 * perCellWidth,
            path_array1[visitPointIndex + 1][1] + (node.y - 50) / 100 * perCellWidth,
            path_array1[visitPointIndex + 1][2] + Math.random() * 100,
        );
        nodePositionProperty.addSample(time, nodePosition);
        id2property[node.id] = nodePositionProperty;
        let entity = viewer.entities.add({
            id: node.id,
            availability: new Cesium.TimeIntervalCollection([new Cesium.TimeInterval({
                start: startTime,
                stop: endTime
            })]),
            position: nodePositionProperty,
            point: {
                pixelSize: 12,
                color: Cesium.Color.INDIGO,
                scaleByDistance: new Cesium.NearFarScalar(1.5e2, 2.0, 1.5e7, 0.5),
            },
            label: {
                text: node.ip,
                font: '10pt monospace',
                style: Cesium.LabelStyle.FILL_AND_OUTLINE,
                outlineWidth: 2,
                verticalOrigin: Cesium.VerticalOrigin.BOTTOM,
                pixelOffset: new Cesium.Cartesian2(0, -19)
            },
            path: {
                resolution: 1,
                material: new Cesium.PolylineGlowMaterialProperty({
                    glowPower: 0.1,
                    color: Cesium.Color.BEIGE,
                }),
                width: 10,
            },
        });
        models.push(entity);
    });
    initTree(models, 'id');
    visitPointIndex++;
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
    writeToInfo(message);
}

function onWsError(evt) {

}

function onWsClose(evt) {

}

function writeToInfo(message) {
    //$("#greetings").append("<tr><td>" + message + "</td></tr>");
    console.log(message);
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

