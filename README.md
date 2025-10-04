# ARSW-Laboratorio5: Blueprints

[![Java](https://img.shields.io/badge/Java-17%2B-blue.svg)](https://www.oracle.com/java/)
[![Maven](https://img.shields.io/badge/Maven-Build-brightgreen.svg)](https://maven.apache.org/)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-brightgreen.svg)


## Descripción General

En este ejercicio se va a construír el componente BlueprintsRESTAPI, el cual permita gestionar los planos arquitectónicos de una prestigiosa compañia de diseño. La idea de este API es ofrecer un medio estandarizado e 'independiente de la plataforma' para que las herramientas que se desarrollen a futuro para la compañía puedan gestionar los planos de forma centralizada. 

Este proyecto es una aplicación full-stack desarrollada con **Spring Boot**, **Java 17+**, **Maven**, y un frontend web moderno con **HTML, CSS y JavaScript (jQuery)**. Permite registrar, consultar, filtrar y visualizar planos, así como crear nuevos autores y planos desde la interfaz web.

[https://github.com/ARSW-ECI-archive/SpringBoot_REST_API_Blueprints_Part2](https://github.com/ARSW-ECI-archive/SpringBoot_REST_API_Blueprints_Part2)

---

## Tecnologías y dependencias

- **Backend:** Spring Boot 2.7.18, Java 17+, Maven
- **Frontend:** HTML5, CSS3, Bootstrap, jQuery
- **Pruebas:** JUnit 4
- **Persistencia:** En memoria (sin base de datos)

---

## Estructura del Proyecto

```
├── src/
│   ├── main/
│   │   ├── java/edu/eci/arsw/blueprints/
│   │   │   ├── controllers/           # Controladores REST
│   │   │   ├── model/                 # Entidades: Blueprint, Point
│   │   │   ├── persistence/           # Interfaces y persistencia en memoria
│   │   │   ├── services/              # Servicios y filtros
│   │   └── resources/
│   │       ├── public/                # Frontend web (index.html, js, css)
│   │       └── applicationContext.xml # Configuración Spring
│   └── test/
│       └── java/edu/eci/arsw/blueprints/test/persistence/impl/
│           ├── InMemoryPersistenceTest.java
│           └── BlueprintFilterTest.java
├── pom.xml
├── README.md
├── img/                              # Diagramas y capturas
└── model.uml
```

---

## Instalación y Ejecución

### Requisitos
- Java 17 o superior
- Maven

### Backend (Spring Boot)
1. Instala dependencias y compila:

	```bash
	mvn clean package
	```
2. Ejecuta el backend:

	```bash
	mvn spring-boot:run
	```

### Frontend (Web)

Al ejecutar el backend el front estará disponible en `http://localhost:8080`.

---

## Funcionalidades

- **Registrar planos:** Desde la web, puedes crear nuevos planos y autores.
- **Consultar planos:** Buscar planos por autor.
- **Visualizar planos:** Ver los puntos del plano en un canvas interactivo.
- **Listar autores:** Ver todos los autores registrados y sus planos.
- **Filtrar planos:** El backend aplica filtros antes de retornar los datos.
- **Persistencia en memoria:** Los datos se almacenan temporalmente en memoria, sin base de datos.

---

## API REST para gestión de planos

El backend implementa una API REST completa para la gestión de planos arquitectónicos, cumpliendo con los siguientes puntos:

- **Controlador REST único:** Todos los recursos `/blueprints`, `/blueprints/{author}`, `/blueprints/{author}/{bpname}` y sus verbos (GET, POST, PUT) están en el mismo bean (`BlueprintsAPIController`).
- **Inyección de dependencias:** El controlador inyecta el servicio, y este a su vez la persistencia y el filtro, usando `@Autowired` y `@Service`.
- **Persistencia thread-safe:** El bean `InMemoryBlueprintPersistence` usa `ConcurrentHashMap` y operaciones atómicas (`putIfAbsent`).
- **Filtros seguros:** Los filtros no modifican los objetos originales, evitando efectos colaterales entre hilos.
- **Códigos HTTP correctos:** Los métodos REST retornan 202 (ACCEPTED) para éxito, 404 para no encontrado, 201 para creación y 403 para errores de persistencia.


### Análisis de concurrencia

Este API atiende múltiples peticiones concurrentes. A continuación, se documentan las posibles condiciones de carrera detectadas y la solución aplicada.

1) Posibles condiciones de carrera

- Lectura mientras se actualiza: una petición GET puede leer un `Blueprint` al mismo tiempo que otra petición PUT lo actualiza. Si el objeto se muta in-place (por ejemplo, modificando la lista de puntos), se corre el riesgo de observar estados intermedios o lanzar excepciones como `ConcurrentModificationException`.
- Inserción duplicada: múltiples POST con el mismo autor/nombre pueden intentar registrar el mismo plano simultáneamente.

2) Regiones críticas

- Persistencia en memoria (mapa de `Tuple(author,name) -> Blueprint`): la inserción y la actualización de un plano específico.
- Objeto `Blueprint`: si se muta luego de ser publicado en el mapa compartido, todos los lectores pueden observar dicha mutación.

3) Solución aplicada

- Estructura thread-safe: se usa `ConcurrentHashMap` para la colección principal.
- Operaciones atómicas para crear: `saveBlueprint` utiliza `putIfAbsent`, lo que hace la inserción condicional atómica y evita condiciones de carrera en creación.
- Operaciones atómicas para actualizar: `updateBlueprint` deja de mutar el objeto existente y ahora reemplaza la entrada mediante `computeIfPresent` con una NUEVA instancia de `Blueprint` construida a partir de los puntos recibidos. Esto asegura que la actualización sea atómica y evita estados intermedios visibles para otros hilos.
- Evitar mutación compartida: los servicios/filtros crean copias para filtrar (no modifican el objeto original). Así, los lectores no ven cambios parciales.

4) Alternativas consideradas y por qué no se usaron

- Bloques `synchronized` amplios: sincronizar toda la persistencia degradaría el desempeño bajo carga. Al usar primitivas atómicas del `ConcurrentHashMap` y objetos reemplazados por nuevas instancias, se logra seguridad sin contención global.
- Locks por clave: agregan complejidad innecesaria dado que el mapa ya ofrece operaciones atómicas que cubren este caso de uso.

5) Resultado

Con las medidas anteriores:
- No hay ventanas de tiempo donde un lector observe un plano en mitad de una actualización.
- Se evita la duplicidad en altas concurrentes.
- No se bloquea el mapa completo, manteniendo buena escalabilidad.

Notas de mejora futura: Llevar el modelo `Blueprint` hacia inmutabilidad estricta (campos finales y listas inmodificables) eliminaría completamente la posibilidad de mutaciones compartidas incluso por error. Actualmente los servicios y filtros ya trabajan con copias para no mutar los originales, y la persistencia actualiza por reemplazo atómico.




### Endpoints principales

- `GET /blueprints` — Lista todos los planos (filtrados)
- `GET /blueprints/{author}` — Lista planos por autor
- `GET /blueprints/{author}/{bpname}` — Consulta un plano específico
- `POST /blueprints` — Crea un nuevo plano
- `PUT /blueprints/{author}/{bpname}` — Actualiza un plano existente





## Uso de la Interfaz Web

1. **Listar autores:** Haz clic en "List all authors" para ver los autores disponibles.
2. **Consultar planos:** Ingresa el nombre de un autor y haz clic en "Get blueprints" para ver sus planos.
3. **Visualizar plano:** Haz clic en "Open" en la tabla para ver el plano en el canvas.
4. **Crear plano:** Completa el formulario y haz clic en "Create Blueprint". El formato de puntos es `x1,y1;x2,y2;...`.


---


## Filtros de Planos

El backend aplica uno de dos filtros configurables:
- **Redundancia:** Elimina puntos consecutivos repetidos.
- **Submuestreo:** Elimina 1 de cada 2 puntos.

### ¿Cómo alternar el filtro?
En el archivo `BlueprintsServices.java`, la inyección del filtro se realiza con:
```java
@Autowired
public BlueprintsServices(BlueprintsPersistence bpp,
	@Qualifier("redundancyFilter") BlueprintFilter blueprintFilter) {
	this.bpp = bpp;
	this.blueprintFilter = blueprintFilter;
}
```
Para usar el filtro de submuestreo, cambia el qualifier a:
```java
@Qualifier("subsamplingFilter")
```
Guarda el archivo y reinicia el back para aplicar el cambio.


---

## Pruebas

Ejecuta las pruebas unitarias con:
```bash
mvn test
```
Incluyen pruebas de persistencia y de los filtros.


---

## Criterios de evaluación cumplidos

- Diseño desacoplado y uso de inyección de dependencias con Spring.
- Todos los recursos REST en un solo bean.
- Códigos HTTP correctos en todas las respuestas.
- Persistencia y operaciones thread-safe.
- Análisis de concurrencia documentado y aplicado.
- Funcionalidad completa de la API REST según el enunciado.

---


## Créditos y Autor

**Autor:** Juan José Díaz ([github](https://github.com/Juan-Jose-D))

**Institución:** Escuela Colombiana de Ingeniería Julio Garavito