<<<<<<< HEAD
# TVMaze API

API middleware desarrollada en Java y Spring Boot para consultar TVMaze, almacenar shows en caché y asociar comentarios con calificaciones.

## Estado

Paso 3 completado: búsqueda y consulta individual de shows mediante TVMaze.

El caché y los comentarios se incorporarán de forma incremental en los siguientes pasos de la prueba técnica.

## Requisitos

- JDK 21 o posterior.
- No se requiere una instalación global de Maven; el repositorio incluye Maven Wrapper.

## Verificación

En Windows:

```powershell
.\mvnw.cmd verify
```

En Linux o macOS:

```shell
./mvnw verify
```

## Búsqueda de shows

Inicia la aplicación:

```powershell
.\mvnw.cmd spring-boot:run
```

Consulta shows por nombre:

```http
GET /api/v1/shows/search?search_query=girls
```

Ejemplo con `curl`:

```shell
curl "http://localhost:8080/api/v1/shows/search?search_query=girls"
```

La respuesta contiene exclusivamente `id`, `name`, `channel`, `summary` y `genres`. El canal se obtiene de `network.name` y, cuando no existe, de `webChannel.name`.

Los datos de los shows son proporcionados por [TVMaze](https://www.tvmaze.com/api) bajo su licencia CC BY-SA.

## Detalle de un show

Consulta un show por su ID de TVMaze:

```http
GET /api/v1/shows/1
```

Ejemplo con `curl`:

```shell
curl "http://localhost:8080/api/v1/shows/1"
```

La respuesta conserva el objeto completo entregado por TVMaze. Un ID inexistente devuelve `404 Not Found` y un ID que no sea positivo devuelve `400 Bad Request`.
