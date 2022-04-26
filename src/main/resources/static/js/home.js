let colorls = ["#f1ddce", "#d9e7d1", "#0eaae6"];
let types = ["A类子网节点", "B类子网节点", "C类子网节点"];
let cluster_colors = ['lightgreen', 'lightblue', 'rgba(117,132,161,0.19)'];
let clusters = [];
let cluster_members = null;
const tooltip = new G6.Tooltip({
    offsetX: 70,
    offsetY: 40,
    getContent(e) {
        const outDiv = document.createElement('span');
        outDiv.innerHTML = `
        <li>ip: ${e.item.getModel().label}</li>
        <li >节点抗毁值：${e.item.getModel().invulnerability}</li>
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
    let startID = selectedNodes[0].getID();
    let endID = selectedNodes[1].getID()
    clearStates();
    //const {path} = findShortestPath(graph, selectedNodes[0].getID(), selectedNodes[1].getID());
    //let { length, path, allPath } = findShortestPath(graph, selectedNodes[0].getID(), selectedNodes[1].getID());
    $.ajax({
        url: "/getShortPath",
        type: "GET",
        dataType: "JSON",
        data: {
            start: startID,
            end: endID
        },
        error: function () {
            alert("获取失败");
        },
        success(path) {
            //path = path.map(String);
            //console.log(path);
            const pathNodeMap = {};
            path.forEach(p => {
                //console.log(p)
                //p = p.split(',');
                p.forEach(id => {
                    id = id + "";
                    const pathNode = graph.findById(id);
                    //pathNode.toFront();
                    graph.setItemState(pathNode, 'highlight', true);
                    pathNodeMap[id] = true;
                });
            })
            graph.getEdges().forEach(edge => {
                const edgeModel = edge.getModel();
                const source = edgeModel.source;
                const target = edgeModel.target;
                let sourceInPathIdx = -1;
                let targetInPathIdx = -1;
                let cnt = 0;
                //console.log(path.length)
                for (let i = 0; i < path.length; i++) {
                    path[i] = path[i].map(String)
                    //console.log(path[i])
                    for (let j = 0; j < path[i].length; j++) {

                        if ((path[i][j] === source && path[i][j + 1] === target) || (path[i][j + 1] === source && path[i][j] === target)) {
                            sourceInPathIdx = i;
                            targetInPathIdx = i + 1;
                            cnt = i;
                            break;
                        }
                    }

                }
                if (sourceInPathIdx === -1 || targetInPathIdx === -1) return;

                if (Math.abs(sourceInPathIdx - targetInPathIdx) === 1) {
                    graph.setItemState(edge, (cnt === path.length - 1) ? 'click' : 'highlight', true);
                } else {
                    graph.setItemState(edge, 'inactive', true);
                }
            });
            graph.getNodes().forEach(node => {
                if (!pathNodeMap[node.getID()]) {
                    graph.setItemState(node, 'inactive', true);
                }
                if (node.getID() === startID || node.getID() === endID) {
                    graph.clearItemStates(node);
                }
            });
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

function updateNet(net, id_convas) {
    let new_data = getData(net, id_convas);
    graph.read(new_data);
    //graph.changeData(new_data);
    // graph.changeData()
    //drawHulls();

}

function drawNet(net, id_canvas) {
    const container = document.getElementById(id_canvas);
    const con_width = container.clientWidth;
    const con_height = container.clientHeight;

    if (typeof window !== 'undefined')
        window.onresize = () => {
            if (!graph || graph.get('destroyed')) return;
            if (!container || !container.scrollWidth || !container.scrollHeight) return;
            graph.changeSize(container.scrollWidth, container.scrollHeight - 20);
        };

    graph = new G6.Graph({
        container: id_canvas,
        width: con_width,
        height: con_height,
        linkCenter: true,
        fitView: true,
        layout: {
            preventOverlap: true,
        },
        animate: true,
        modes: {
            default: ['drag-node', 'click-select', 'lasso-select', "drag-combo"],
            edit: []
        },
        plugins: [tooltip],
        defaultNode: {
            size: 15,
            style: {
                stroke: '#686f77',
                lineWidth: 1,
            },
            labelCfg: {
                style: {
                    fontSize: 10,
                },
            },
        },
        defaultEdge: {
            type: 'line',
            style: {
                lineWidth: 0.5,
                stroke: "rgba(35,74,109,0.18)",
            },
        },

        nodeStateStyles: {
            hover: {
                fill: 'lightsteelblue'
            },
            click: {
                stroke: '#201c1c',
                lineWidth: 2
            },
            highlight: {
                fill: 'lightsteelblue'
            },
            inactive: {
                fill: 'rgb(117,120,124, 0.12)'
            }
        },
        edgeStateStyles: {
            click: {
                stroke: 'steelblue',
                lineWidth: 1
            },
            highlight: {
                stroke: '#e24759',
                lineWidth: 1
            },
            inactive: {
                stroke: 'rgba(103,103,98,0.1)',
            }
        }
    })
    let data1 = getData(net, id_canvas);
    //console.log(data1);
    graph.read(data1);

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

    // graph.on('afterlayout', drawHulls());
    // graph.on('aftergraphrefresh', drawHulls());

    graph.on('canvas:click', e => {
        clearStates();
    });

}

function drawHulls() {
    //console.log(graph.getShortestPathMatrix())
    //alert("开始画轮廓包裹");
    cluster_members = Object.values(cluster_members)
    graph.removeHulls();
    for (let i = 0; i < cluster_members.length; i++) {
        //console.log(cluster_members[i])
        let mems = [];
        for (let j = 0; j < cluster_members[i].length; j++) {
            mems.push(cluster_members[i][j] + "");
        }
        //console.log(mems);
        const hull1 = graph.createHull({
            id: 'hull' + i,
            type: 'round-convex',
            members: mems,
            padding: 10,
            style: {
                fill: cluster_colors[i % cluster_colors.length],
                stroke: 'green',
            },
        });
        clusters.push(hull1);
    }

    clusters.forEach(cluster => {
        cluster.updateData(cluster.members)
    })


}

function getData(net, id_canvas) {
    let container = document.getElementById(id_canvas);
    let con_width = container.clientWidth;
    let con_height = container.clientHeight;
    let nodes = [];
    let edges = [];
    $("#value_of_net").text(net.netValue.toFixed(3));
    $("#id_init_cluster_id").text(net.nodeList[net.clusterId].ip);
    cluster_members = net.clusters;
    //console.log(net.nodeList)
    //console.log(con_width)
    //console.log(con_height)
    let len = 0;
    net.cluster.forEach((network) => {
        network.nodeList.forEach(function (node) {
            nodes.push({
                id: node.id + "",
                label: node.ip,
                class: node.type + "",
                x: node.x / 100 * con_width,
                y: node.y / 100 * con_height,
                cap: node.capacity,
                invulnerability: node.invulnerability,
                cluster: node.cluster
            })
            node.edges.forEach((to) => {
                edges.push({
                    source: node.id + "",
                    target: to + "",
                })
            })
        })
    })

    $("#num_node").val(nodes.length + "");
    //console.log(nodes);

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

    return {
        nodes: nodes,
        edges: edges,
        // combos: combos,
    };
}

const lineDash = [4, 2, 1, 2];

G6.registerEdge(
    'line-dash',
    {
        afterDraw(cfg, group) {
            // get the first shape in the group, it is the edge's path here=
            const shape = group.get('children')[0];
            let index = 0;
            // Define the animation
            shape.animate(
                () => {
                    index++;
                    if (index > 9) {
                        index = 0;
                    }
                    const res = {
                        lineDash,
                        lineDashOffset: -index,
                    };
                    // returns the modified configurations here, lineDash and lineDashOffset here
                    return res;
                },
                {
                    repeat: true, // whether executes the animation repeatly
                    duration: 3000, // the duration for executing once
                },
            );
        },
    },
    'line', // extend the built-in edge 'cubic'
);