import * as d3 from "https://cdn.jsdelivr.net/npm/d3@7/+esm";

let info = {};

function init() {
    const modal = document.getElementById('info');
    const drag = new Draggabilly(modal);

    info.window = modal;
    info.title = document.getElementById('info-title');
    info.user = document.getElementById('info-user');
    info.day = document.getElementById('info-day');
    info.url = document.getElementById('info-url');
    info.entry = document.getElementById('info-entry');

    fetch('/data.json')
        .then(res => res.json())
        .then(data => drawDiagram(data));
}

function drawDiagram(data) {
    // Specify the dimensions of the chart.
    const width = 900;
    const height = 900;

    // Specify the color scale.
    const color = d3.scaleOrdinal(d3.schemeCategory10);

    // The force simulation mutates links and nodes, so create a copy
    // so that re-evaluating this cell produces the same result.
    const links = data.links.map(d => ({...d}));
    const nodes = data.nodes.map(d => ({...d}));

    // Create a simulation with several forces.
    const simulation = d3.forceSimulation(nodes)
        .force("link", d3.forceLink(links).id(d => d["entry-url"]))
        .force("charge", d3.forceManyBody())
        .force("x", d3.forceX())
        .force("y", d3.forceY());

    // Create the SVG container.
    const svg = d3.create("svg")
        .attr("width", width)
        .attr("height", height)
        .attr("viewBox", [-width / 2, -height / 2, width, height])
        .attr("style", "max-width: 100%; height: auto;");

    // Add a line for each link, and a circle for each node.
    const link = svg.append("g")
        .attr("stroke", "#999")
        .attr("stroke-opacity", 0.6)
        .selectAll("line")
        .data(links)
        .join("line")
        // .attr("stroke-width", d => Math.sqrt(d.value))
    ;

    const node = svg.append("g")
        .attr("stroke", "#fff")
        .attr("stroke-width", 1.5)
        .selectAll("circle")
        .data(nodes)
        .join("circle")
        .on("click", event => {
            d3.select("circle.selected").classed("selected", false);
            const target = d3.select(event.target).classed("selected", true);
            const d = target.datum();

            info.window.classList.remove('hidden');
            info.title.innerText = d.title;
            info.user.innerText = d.user;
            info.day.innerText = d.day;
            info.url.href = d.url;
            info.entry.href = d["entry-url"];
        })
        .attr("r", 5)
        .attr("fill", d => color(d.day));

    // TODO make sure this hack gets removed once I get more familiar with d3
    // node.append("text")
    //     .text(d => JSON.stringify([d.title, d.user, d.day, d.url, d.entry]));

    // Add a drag behavior.
    node.call(d3.drag()
        .on("start", dragstarted)
        .on("drag", dragged)
        .on("end", dragended));

    // Set the position attributes of links and nodes each time the simulation ticks.
    simulation.on("tick", () => {
        link
            .attr("x1", d => d.source.x)
            .attr("y1", d => d.source.y)
            .attr("x2", d => d.target.x)
            .attr("y2", d => d.target.y);

        node
            .attr("cx", d => d.x)
            .attr("cy", d => d.y);
    });

    // Reheat the simulation when drag starts, and fix the subject position.
    function dragstarted(event) {
        if (!event.active) simulation.alphaTarget(0.3).restart();
        event.subject.fx = event.subject.x;
        event.subject.fy = event.subject.y;
    }

    // Update the subject (dragged node) position during drag.
    function dragged(event) {
        event.subject.fx = event.x;
        event.subject.fy = event.y;
    }

    // Restore the target alpha so the simulation cools after dragging ends.
    // Unfix the subject position now that it’s no longer being dragged.
    function dragended(event) {
        if (!event.active) simulation.alphaTarget(0);
        event.subject.fx = null;
        event.subject.fy = null;
    }

    // When this cell is re-run, stop the previous simulation. (This doesn’t
    // really matter since the target alpha is zero and the simulation will
    // stop naturally, but it’s a good practice.)
    // invalidation.then(() => simulation.stop());

    document.getElementById('svg-wrapper').appendChild(svg.node());
}

init();
