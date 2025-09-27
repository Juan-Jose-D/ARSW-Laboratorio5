var api = apiclient;

var BlueprintApp = (function () {
    var blueprints = [];
    var authorName = "";

    var setAuthorName = function (newAuthorName) {
        authorName = newAuthorName;
        document.getElementById("selectedAuthor").innerText = authorName;
    };

    var updateTotalPoints = function () {
        var totalPoints = blueprints.reduce(function (acc, blueprint) {
            return acc + blueprint.points.length;
        }, 0);
        $("#totalPoints").text(totalPoints);
    };

    var renderTable = function (blueprintList) {
        var tableBody = blueprintList.map(function (blueprint) {
            return `
                <tr>
                    <td>${blueprint.name}</td>
                    <td>${blueprint.numberOfPoints}</td>
                    <td>
                        <button class="btn btn-info" onclick="BlueprintApp.drawBlueprint('${authorName}', '${blueprint.name}')">Open</button>
                    </td>
                </tr>
            `;
        }).join("");
        $("#blueprintsTable tbody").html(tableBody);
    };


    var updateBlueprintsByAuthor = function (author) {
        api.getBlueprintsByAuthor(author, function (data) {
            // Almacenamos los planos obtenidos en la variable privada blueprints
            blueprints = data;

            // Transformar los planos a una lista de objetos con nombre y número de puntos
            var transformedBlueprints = blueprints.map(function (blueprint) {
                return {
                    name: blueprint.name,
                    numberOfPoints: blueprint.points.length
                };
            });

            renderTable(transformedBlueprints);

            var totalPoints = blueprints.reduce(function (acc, blueprint) {
                return acc + blueprint.points.length;
            }, 0);

            // Actualizar el campo de total de puntos en el DOM usando jQuery
            $("#totalPoints").text(totalPoints);
        });
    };
    var drawBlueprint = function (author, blueprintName) {
        api.getBlueprintsByNameAndAuthor(author, blueprintName, function (blueprint) {
            // Limpiar el canvas
            var canvas = document.getElementById("canvas");
            var ctx = canvas.getContext("2d");
            ctx.clearRect(0, 0, canvas.width, canvas.height);

            // Dibujar los puntos y líneas en el canvas
            if (blueprint.points.length > 0) {
                ctx.beginPath();
                ctx.moveTo(blueprint.points[0].x, blueprint.points[0].y);
                // Dibujar líneas entre puntos
                for (var i = 1; i < blueprint.points.length; i++) {
                    ctx.lineTo(blueprint.points[i].x, blueprint.points[i].y);
                }
                ctx.stroke();

                // Dibujar círculos en cada punto
                ctx.fillStyle = '#007bff';
                for (var i = 0; i < blueprint.points.length; i++) {
                    ctx.beginPath();
                    ctx.arc(blueprint.points[i].x, blueprint.points[i].y, 6, 0, 2 * Math.PI);
                    ctx.fill();
                }
            }

            // Actualizar el nombre del blueprint en el DOM
            $("#name-blueprint").text(`Current blueprint: ${blueprint.name}`);
        });
    };

    return {
        setAuthorName: setAuthorName,
        updateBlueprintsByAuthor: updateBlueprintsByAuthor,
        drawBlueprint: drawBlueprint
    };
})();


$("#getBlueprintsBtn").on("click", function () {
    var authorInput = $("#authorInput").val();
    if (authorInput) {
        BlueprintApp.setAuthorName(authorInput);
        BlueprintApp.updateBlueprintsByAuthor(authorInput);
    } else {
        alert("Por favor ingrese un nombre de autor.");
    }
});

// Crear blueprint
$("#createBlueprintBtn").on("click", function () {
    var author = $("#newAuthor").val();
    var name = $("#newBlueprintName").val();
    var pointsStr = $("#newPoints").val();
    if (!author || !name || !pointsStr) {
        alert("Por favor ingrese todos los campos para crear el blueprint.");
        return;
    }
    // Parsear puntos: formato "10,20;30,40"
    var points = pointsStr.split(";").map(function (pair) {
        var coords = pair.split(",");
        return { x: parseInt(coords[0]), y: parseInt(coords[1]) };
    });
    var blueprint = { author: author, name: name, points: points };
    api.createBlueprint(blueprint, function () {
        alert("Blueprint creado exitosamente.");
        $("#newAuthor").val("");
        $("#newBlueprintName").val("");
        $("#newPoints").val("");
    });
});

// Listar autores
$("#listAuthorsBtn").on("click", function () {
    api.getAuthors(function (authors) {
        var html = "";
        authors.forEach(function (author) {
            html += `<li>${author}</li>`;
        });
        $("#authorsList").html(html);
    });
});