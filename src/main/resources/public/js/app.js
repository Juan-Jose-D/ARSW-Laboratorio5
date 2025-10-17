var api = apiclient;


var BlueprintApp = (function () {
    let blueprints = [];
    let authorName = "";
    let currentBlueprint = null; // {author, name, points}
    let isNewBlueprint = false;

    // Modular event setup for canvas
    function setupCanvasEvents() {
        const canvas = document.getElementById("canvas");
        if (!canvas) return;
        canvas.onpointerdown = function (event) {
            if (!currentBlueprint) return; // Only if blueprint is open
            const rect = canvas.getBoundingClientRect();
            const x = Math.round(event.clientX - rect.left);
            const y = Math.round(event.clientY - rect.top);
            currentBlueprint.points.push({ x, y });
            repaintCanvas(currentBlueprint.points);
        };
    }

    function repaintCanvas(points) {
        const canvas = document.getElementById("canvas");
        const ctx = canvas.getContext("2d");
        ctx.clearRect(0, 0, canvas.width, canvas.height);
        if (points.length > 0) {
            ctx.beginPath();
            ctx.moveTo(points[0].x, points[0].y);
            points.slice(1).forEach(pt => ctx.lineTo(pt.x, pt.y));
            ctx.stroke();
            ctx.fillStyle = '#007bff';
            points.forEach(pt => {
                ctx.beginPath();
                ctx.arc(pt.x, pt.y, 6, 0, 2 * Math.PI);
                ctx.fill();
            });
        }
    }

    function setAuthorName(newAuthorName) {
        authorName = newAuthorName;
        document.getElementById("selectedAuthor").innerText = authorName;
    }

    function updateTotalPoints() {
        const totalPoints = blueprints.map(bp => bp.points.length).reduce((a, b) => a + b, 0);
        $("#totalPoints").text(totalPoints);
    }

    function renderTable(blueprintList) {
        const tableBody = blueprintList.map(bp => `
            <tr>
                <td>${bp.name}</td>
                <td>${bp.numberOfPoints}</td>
                <td>
                    <button class="btn btn-info" onclick="BlueprintApp.drawBlueprint('${authorName}', '${bp.name}')">Open</button>
                    <button class="btn btn-danger" onclick="BlueprintApp.deleteBlueprint('${authorName}', '${bp.name}')">Delete</button>
                </td>
            </tr>
        `).join("");
        $("#blueprintsTable tbody").html(tableBody);
    }

    function updateBlueprintsByAuthor(author) {
        return new Promise((resolve, reject) => {
            api.getBlueprintsByAuthor(author, function (data) {
                blueprints = data || [];
                const transformed = blueprints.map(bp => ({ name: bp.name, numberOfPoints: bp.points.length }));
                renderTable(transformed);
                updateTotalPoints();
                resolve();
            });
        });
    }

    function drawBlueprint(author, blueprintName) {
        api.getBlueprintsByNameAndAuthor(author, blueprintName, function (bp) {
            currentBlueprint = { author: bp.author, name: bp.name, points: bp.points.slice() };
            isNewBlueprint = false;
            repaintCanvas(currentBlueprint.points);
            $("#name-blueprint").text(`Current blueprint: ${bp.name}`);
        });
    }

    function saveOrUpdateBlueprint() {
        if (!currentBlueprint) return Promise.reject("No blueprint selected");
        const blueprint = {
            author: currentBlueprint.author,
            name: currentBlueprint.name,
            points: currentBlueprint.points
        };
        let opPromise;
        if (isNewBlueprint) {
            opPromise = new Promise((resolve, reject) => {
                api.createBlueprint(blueprint, resolve);
            });
        } else {
            opPromise = new Promise((resolve, reject) => {
                api.updateBlueprint(blueprint, resolve);
            });
        }
        return opPromise
            .then(() => updateBlueprintsByAuthor(authorName))
            .then(() => updateTotalPoints());
    }

    function createNewBlueprintFlow() {
        // Limpiar canvas y blueprint actual
        repaintCanvas([]);
        const author = prompt("Nombre del autor para el nuevo blueprint:");
        if (!author) return;
        const name = prompt("Nombre del nuevo blueprint:");
        if (!name) return;
        currentBlueprint = { author: author, name: name, points: [] };
        authorName = author;
        isNewBlueprint = true;
        $("#selectedAuthor").text(author);
        $("#name-blueprint").text(`Current blueprint: ${name}`);
        // Actualizar autores en la lista tras crear
        api.getAuthors(function (authors) {
            const html = authors.map(a => `<li>${a}</li>`).join("");
            $("#authorsList").html(html);
        });
        // Mostrar mensaje instructivo
        alert("Ya puedes crear tu blueprint en el canvas, no olvides guardarlo!");
    }

    function deleteBlueprint(author, name) {
        return new Promise((resolve, reject) => {
            api.deleteBlueprint(author, name, resolve);
        })
        .then(() => {
            repaintCanvas([]);
            currentBlueprint = null;
            return updateBlueprintsByAuthor(authorName);
        })
        .then(() => updateTotalPoints());
    }

    // Expose for buttons
    return {
        setAuthorName,
        updateBlueprintsByAuthor,
        drawBlueprint,
        saveOrUpdateBlueprint,
        createNewBlueprintFlow,
        deleteBlueprint,
        setupCanvasEvents
    };
})();



$(document).ready(function () {
    BlueprintApp.setupCanvasEvents();

    $("#getBlueprintsBtn").on("click", function () {
        const authorInput = $("#authorInput").val();
        if (authorInput) {
            BlueprintApp.setAuthorName(authorInput);
            BlueprintApp.updateBlueprintsByAuthor(authorInput);
        } else {
            alert("Por favor ingrese un nombre de autor.");
        }
    });

    $("#createBlueprintBtn").on("click", function () {
        BlueprintApp.createNewBlueprintFlow();
    });

    $("#saveUpdateBtn").on("click", function () {
        BlueprintApp.saveOrUpdateBlueprint()
            .then(() => alert("Plano guardado/actualizado correctamente."))
            .catch(err => alert("Error: " + err));
    });

    $("#listAuthorsBtn").on("click", function () {
        api.getAuthors(function (authors) {
            const html = authors.map(author => `<li>${author}</li>`).join("");
            $("#authorsList").html(html);
        });
    });
});