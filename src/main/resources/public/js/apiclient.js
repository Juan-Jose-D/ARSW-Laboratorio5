var apiclient = (function () {
    var apiUrl = 'http://localhost:8080/blueprints';


    return {
        getBlueprintsByAuthor: function (authname, callback) {
            $.get(apiUrl + "/" + authname, function (data) {
                callback(data);
            }).fail(function (error) {
                console.error("Error al obtener los planos: ", error);
            });
        },

        getBlueprintsByNameAndAuthor: function (authname, bpname, callback) {
            $.get(apiUrl + "/" + authname + "/" + bpname, function (data) {
                callback(data);
            }).fail(function (error) {
                console.error("Error al obtener el plano: ", error);
            });
        },

        createBlueprint: function (blueprint, callback) {
            $.ajax({
                url: apiUrl,
                type: "POST",
                data: JSON.stringify(blueprint),
                contentType: "application/json",
                success: function (data) {
                    callback(data);
                },
                error: function (error) {
                    console.error("Error al crear el plano: ", error);
                }
            });
        },

        getAuthors: function (callback) {
            $.get(apiUrl.replace("/blueprints", "/blueprints/authors"), function (data) {
                callback(data);
            }).fail(function (error) {
                console.error("Error al obtener los autores: ", error);
            });
        }
    };
})();