# TVMaze API

API middleware desarrollada en Java y Spring Boot para consultar TVMaze, almacenar shows en caché y asociar comentarios con calificaciones.

## Estado

Paso 4 completado: búsqueda, consulta individual y persistencia base con MongoDB.

El uso del caché dentro del endpoint y los comentarios se incorporarán de forma incremental en los siguientes pasos de la prueba técnica.

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

## MongoDB

La aplicación utiliza la variable de entorno `MONGODB_URI`. Si no está definida, utiliza por defecto una base local:

```text
mongodb://localhost:27017/tvmaze
```

### Desarrollo local con Docker

Inicia MongoDB:

```shell
docker compose up -d mongodb
```

Después inicia la aplicación normalmente. Para detener MongoDB sin eliminar sus datos:

```shell
docker compose down
```

### MongoDB Atlas

1. Crea un cluster gratuito.
2. Crea un usuario exclusivo para la prueba con acceso `readWrite` únicamente a la base `tvmaze`.
3. Agrega `0.0.0.0/0` en Network Access solamente porque la prueba solicita acceso sin restricción de IP.
4. Copia la URI de conexión y define la variable antes de iniciar la aplicación:

```powershell
$env:MONGODB_URI='mongodb+srv://USUARIO:CONTRASENA@CLUSTER/tvmaze?retryWrites=true&w=majority'
.\mvnw.cmd spring-boot:run
```

Si el usuario o la contraseña contienen caracteres especiales, deben codificarse para una URI. Nunca guardes la URI real en `application.yml`, `.env.example` o Git. El acceso global de Atlas debe retirarse al terminar la evaluación.

La colección preparada para el siguiente paso es `shows_cache`. Cada documento utilizará el ID de TVMaze como `_id`, conservará el show completo y registrará `cached_at`.
