'use strict';

let map = null;
let viewer = null;
let models = [];
let point_array = [];
let primitiveCircleArray = [];
let primitive1;
let primitive2;
let clock1;
let clock2;
let graphicLayer = null;

Date.prototype.format = function (fmt) {
    let o = {
        "M+": this.getMonth() + 1, //月份
        "d+": this.getDate(), //日
        "h+": this.getHours() % 12 == 0 ? 12 : this.getHours() % 12, //小时
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
                y: 31.588498,
                x: 120.714274,
                z: 10492.53,
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
    testPointLine();
}


function testPoint() {

    map.addLayer(graphicLayer);
    let num_plain = 30;

    let center_point = [120.714274, 31.588498, 1492.53];
    let w = 1;
    let l = 1;
    for (let i = 0; i < num_plain; i++) {

        let r = i / 10,
            c = i % 10;
        let p = [];
        p[0] = center_point[0] + w / 10 * r;
        p[1] = center_point[1] + l / 10 * c;
        p[2] = center_point[2] + Math.random() * 1000;
        point_array.push(p);

        let m = new mars3d.graphic.ModelEntity({
            name: '飞机' + i,
            position: p,
            style: {
                url: 'icons/Cesium_Air.glb',
                scale: 20,
                minimumPixelSize: 80,
                scaleByDistance: new Cesium.NearFarScalar(10000, 2.0, 500000, 0.1),
                color: '#0d27e8',
                label: {
                    text: '193.168.0.' + i,
                    font_size: 10,
                    color: '#fff',
                    pixelOffsetY: -10,
                    scaleByDistance: new Cesium.NearFarScalar(10000, 1.0, 500000, 0.5),
                }
            }
        });
        models.push(m);
        graphicLayer.addGraphic(m);
        m.addDynamicPosition(p, 0);
        m.addDynamicPosition(p, 0);
        graphicLayer.addGraphic(m);
    }


    let ellipse = new mars3d.graphic.CircleEntity({
        position: models[3].position,
        style: {
            radius: 20500.0,
            material: mars3d.MaterialUtil.createMaterialProperty(mars3d.MaterialType.CircleWave, {
                color: 'rgba(255,0,0,0.58)',
                count: 1, //单个圆圈
                speed: 5,
            }),
        },
    })
    graphicLayer.addGraphic(ellipse);

    initTree(models);
    /**
     for (let i = 0; i < 50; i++) {
        let line = new mars3d.graphic.PolylineEntity({
            positions: [
                point_array[i],
                point_array[i + 1],
            ],
            style: {
                width: 3,
                material: mars3d.MaterialUtil.createMaterialProperty(mars3d.MaterialType.LineFlow, {
                    color: 'rgba(5,37,245,0.57)',
                    image: 'icons/lineClr.png',
                    speed: 10,
                }),
            },
        })
        graphicLayer.addGraphic(line);
    }*/

    for (let i = 0; i < 50; i++) {
        let line = new mars3d.graphic.PolylineEntity({
            positions: [
                point_array[i],
                point_array[i + 1],
            ],
            style: {
                width: 2,
                material: mars3d.MaterialUtil.createMaterialProperty(mars3d.MaterialType.PolylineOutline, {
                    color: Cesium.Color.ORANGE,
                }),
            },
        })
        graphicLayer.addGraphic(line);
    }

    let line1 = new mars3d.graphic.PolylineEntity({
        id: 'line1',
        positions: [
            point_array[4],
            point_array[3],
        ],
        style: {
            width: 5,
            material: mars3d.MaterialUtil.createMaterialProperty(mars3d.MaterialType.LineFlow, {
                color: 'rgba(40,232,11,0.61)',
                image: 'icons/lineClr.png',
                speed: 10,
            }),
        },
    })
    graphicLayer.addGraphic(line1);

    $('#btnStart').click(() => {
        clock1 = setInterval(() => {
            for (let i = 0; i < num_plain; i++) {
                point_array[i][1] = point_array[i][1] + 0.1;
                models[i].addDynamicPosition(point_array[i], 1);
            }
            showPosition(models[0], point_array[0]);
            //console.log(models[4])
            let p_s = [mars3d.LatLngPoint.fromCartesian(models[4]), mars3d.LatLngPoint.fromCartesian(models[3])];
            //line1.positions = [models[4].position, models[3].position];
        }, 1000);

    });
    $('#btnStop').click(() => {
        map.clock.shouldAnimate = false;
        //alert('停止');
        clearInterval(clock1);
    });

    $('#btnViewLine').click(() => {
        let line_ = graphicLayer.getGraphicById('line1');
        line_.positions = [models[0].points];
    })

}


function testPointLine() {

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
    entity.flyTo({
        radius: 10000,
    })
    map.trackedEntity = entity._entity;
}