let colorls = new Array("#9c073d", "#095ea0", "#0d930d");
let types = new Array("主干节点", "支干节点", "边缘节点");

const tooltip = new G6.Tooltip({
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


function findShortPath() {
    const selectedNodes = graph.findAllByState('node', 'selected');
    if (selectedNodes.length !== 2) {
        alert('Please select TWO nodes!\n\r请选择有且两个节点！');
        return;
    }

    clearStates();
    const {findShortestPath} = G6.Algorithm;
    const {path} = findShortestPath(graph, selectedNodes[0].getID(), selectedNodes[1].getID());
    const pathNodeMap = {};
    path.forEach(id => {
        const pathNode = graph.findById(id);
        pathNode.toFront();
        graph.setItemState(pathNode, 'highlight', true);
        pathNodeMap[id] = true;
    });
    graph.getEdges().forEach(edge => {
        const edgeModel = edge.getModel();
        const source = edgeModel.source;
        const target = edgeModel.target;
        const sourceInPathIdx = path.indexOf(source);
        const targetInPathIdx = path.indexOf(target);
        if (sourceInPathIdx === -1 || targetInPathIdx === -1) return;
        if (Math.abs(sourceInPathIdx - targetInPathIdx) === 1) {
            graph.setItemState(edge, 'highlight', true);
        } else {
            graph.setItemState(edge, 'inactive', true);
        }
    });
    graph.getNodes().forEach(node => {
        if (!pathNodeMap[node.getID()]) {
            graph.setItemState(node, 'inactive', true);
        }
    });
}

const clearStates = () => {
    graph.getNodes().forEach(node => {
        graph.clearItemStates(node);
    });
    graph.getEdges().forEach(edge => {
        graph.clearItemStates(edge);
    });
}

function getData(net, id_canvas) {
    let container = document.getElementById(id_canvas);
    let con_width = container.clientWidth;
    let con_height = container.clientHeight;
    let nodes = new Array();
    let edges = new Array();

    net.nodeList.forEach(function (node) {
        nodes.push({
            id: node.id + "",
            label: node.ip,
            class: node.type + "",
            x: node.x * con_width / 100,
            y: node.y * con_height / 100,
            cap: node.capacity
        })
    })

    nodes.forEach(function (node) {
        if (!node.style) {
            node.style = {};
        }
        switch (node.class) {
            case "0":
                node.size = 51;
                node.style.fill = colorls[0]
                node.style.stroke = '#686f77'
                node.style.lineWidth = 1
                break
            case "1":
                node.size = 38;
                node.style.fill = colorls[1]
                node.style.stroke = '#686f77'
                node.style.lineWidth = 1
                break;
            case "2":
                node.size = 25;
                node.style.fill = colorls[2]
                node.style.stroke = '#686f77'
                node.style.lineWidth = 1
                break;
        }
    });

    net.edgeList.forEach(function (edge) {
        edges.push({
            source: edge.from + "",
            target: edge.to + "",
        })
    });

    edges.forEach(function (edge) {
        if (!edge.style) {
            edge.style = {};
        }
        edge.style.lineWidth = 2;
        edge.style.stroke = "#929fba";

    })

    const initData = {
        nodes: nodes,
        edges: edges,
    }
    return initData;
}

function drawNet(net, id_canvas) {
    var container = document.getElementById(id_canvas);
    var con_width = container.clientWidth;
    var con_height = container.clientHeight;

    data1 = getData(net, id_canvas);
    graph = new G6.Graph({
        container: id_canvas,
        width: con_width,
        height: con_height,

        fitView: true,
        layout: {
            type: 'random',
            gatherDiscrete: true,
            descreteGravity: 200,
            maxIteration: 2000,
        },
        modes: {
            default: ['drag-node', 'click-select',],
            edit: []
        },
        plugins: [tooltip],
        defaultNode: {
            labelCfg: {
                autoRotate: true,
                position: 'bottom'
            },
            style: {
                fill: '#000104',

            }
        },
        defaultEdge: {
            type: 'circle-running',

        },
        nodeStateStyles: {
            hover: {
                fill: 'lightsteelblue'
            },
            click: {
                stroke: '#201c1c',
                lineWidth: 3
            }
        },
        edgeStateStyles: {
            click: {
                stroke: 'steelblue'
            }
        }
    })

    graph.data(data1);
    graph.render();

    graph.on('node:mouseenter', (e) => {
        const nodeItem = e.item;
        graph.setItemState(nodeItem, 'hover', true);
    });

    graph.on('node:mouseleave', (e) => {
        const nodeItem = e.item;
        graph.setItemState(nodeItem, 'hover', false);
    });

    // 点击节点
    graph.on('node:click', (e) => {
        // 先将所有当前是 click 状态的节点置为非 click 状态
        const clickNodes = graph.findAllByState('node', 'click');
        clickNodes.forEach((cn) => {
            graph.setItemState(cn, 'click', false);
        });
        const nodeItem = e.item; // 获取被点击的节点元素对象
        graph.setItemState(nodeItem, 'click', true); // 设置当前节点的 click 状态为 true
    });

    // 点击边
    graph.on('edge:click', (e) => {
        // 先将所有当前是 click 状态的边置为非 click 状态
        const clickEdges = graph.findAllByState('edge', 'click');
        clickEdges.forEach((ce) => {
            graph.setItemState(ce, 'click', false);
        });
        const edgeItem = e.item; // 获取被点击的边元素对象
        graph.setItemState(edgeItem, 'click', true); // 设置当前边的 click 状态为 true
    });


    graph.on('canvas:click', e => {
        clearStates();
    });

}


G6.registerEdge(
    'circle-running',
    {
        afterDraw(cfg, group) {
            // 获得当前边的第一个图形，这里是边本身的 path
            const shape = group.get('children')[0];
            // 边 path 的起点位置
            const startPoint = shape.getPoint(0);

            // 添加红色 circle 图形
            const circle = group.addShape('circle', {
                attrs: {
                    x: startPoint.x,
                    y: startPoint.y,
                    fill: 'red',
                    r: 3,
                },
                // must be assigned in G6 3.3 and later versions. it can be any value you want
                name: 'circle-shape',
            });

            // 对红色圆点添加动画
            circle.animate(
                (ratio) => {
                    // 每一帧的操作，入参 ratio：这一帧的比例值（Number）。返回值：这一帧需要变化的参数集（Object）。
                    // 根据比例值，获得在边 path 上对应比例的位置。
                    const tmpPoint = shape.getPoint(ratio);
                    // 返回需要变化的参数集，这里返回了位置 x 和 y
                    return {
                        x: tmpPoint.x,
                        y: tmpPoint.y,
                    };
                },
                {
                    repeat: true, // 动画重复
                    duration: 3000,
                },
            ); // 一次动画的时间长度
        },
    },
    'cubic',
); // 该自定义边继承内置三阶贝塞尔曲线 cubic
