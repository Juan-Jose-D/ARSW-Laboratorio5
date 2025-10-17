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

- **Registrar planos:** Desde la web, puedes crear nuevos planos en el canvas.
- **Consultar planos:** Buscar planos por autor.
- **Visualizar planos:** Ver los puntos del plano en un canvas interactivo.
- **Listar autores:** Ver todos los autores registrados y sus planos.
- **Filtrar planos:** El backend aplica filtros antes de retornar los datos.
- **Persistencia en memoria:** Los datos se almacenan temporalmente en memoria, sin base de datos.
- **Eliminar:** Se puede eliminar los planos existentes.

---
## Capturas de pantalla

![alt text](/img/image.png)

Se pueden ver algunos blueprints ya creados.

![alt text](/img/image2.png)

Y crear y guardar planos nuevos

![alt text](/img/image1.png)

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
- 

### Canvas interactivo y modularidad
- El canvas ahora captura eventos de tipo PointerEvent (mouse y touch), permitiendo agregar puntos al plano de forma intuitiva. La lógica de eventos está modularizada en el frontend.
- Al crear un nuevo blueprint, se solicita el nombre del autor y el nombre del plano. Si el autor no existe, se agrega automáticamente y aparece en la lista de autores.
- Después se puede crear el blueprint mediante puntos(clicks) en el canvas.


### Endpoints principales

- `GET /blueprints` — Lista todos los planos (filtrados)
- `GET /blueprints/{author}` — Lista planos por autor
- `GET /blueprints/{author}/{bpname}` — Consulta un plano específico
- `POST /blueprints` — Crea un nuevo plano
- `PUT /blueprints/{author}/{bpname}` — Actualiza un plano existente
- `DELETE /blueprints/{author}/{bpname}` - Eliminar planos.





## Uso de la Interfaz Web

1. **Listar autores:** Haz clic en "List all authors" para ver los autores disponibles.
2. **Consultar planos:** Ingresa el nombre de un autor y haz clic en "Get blueprints" para ver sus planos.
3. **Visualizar plano:** Haz clic en "Open" en la tabla para ver el plano en el canvas.
4. **Crear plano:** Escribe el nombre del autor y el nombre del blueprint y dibujalo en el canvas, un punto por click. 
5. **Guardar el plano creado:** Guardar el plano creado manualmente mediante el botón Save/Update.


---

## Pruebas

Ejecuta las pruebas unitarias con:
```bash
mvn test
```
Incluyen pruebas de persistencia y de los filtros.


---

## Criterios de evaluación cumplidos

- **Funcional**
  - Carga y dibujo de planos: El frontend carga los planos desde el backend y los dibuja correctamente en el canvas, usando eventos pointer (mouse/touch).
  - Actualización de la lista: Al crear y guardar un nuevo plano (POST/PUT), la lista de planos y autores se actualiza automáticamente, mostrando el nuevo plano y autor si corresponde.
  - Modificación de planos: Puedes abrir un plano, agregarle puntos en el canvas y guardarlo (PUT), actualizando el backend y la lista.
  - Cálculo de puntos: El total de puntos del usuario se calcula y muestra correctamente, usando map/reduce.
- **Diseño**
  - Sin ciclos tradicionales: El cálculo de puntos y la transformación de listas usan exclusivamente operaciones de map/reduce, no for/while.
  - Promesas en actualización y borrado: Los flujos de guardar (PUT/POST) y borrar (DELETE) planos usan promesas para asegurar que el cálculo de puntaje y la actualización de la lista se realicen solo después de que el backend confirme la operación. No hay callbacks anidados, sino encadenamiento de promesas.

---


## Créditos y Autor

**Autor:** Juan José Díaz ([github](https://github.com/Juan-Jose-D))

**Institución:** Escuela Colombiana de Ingeniería Julio Garavito