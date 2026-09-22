# TVMaze API

API middleware desarrollada en Java y Spring Boot para consultar TVMaze, almacenar shows en caché y asociar comentarios con calificaciones.

## Estado

Paso 6 completado: búsqueda, caché de shows y creación de comentarios con calificación.

Los comentarios se incorporarán a las respuestas de búsqueda y detalle en los siguientes pasos de la prueba técnica.

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

La consulta utiliza un caché persistente:

1. Busca el ID en la colección `shows_cache` de MongoDB.
2. Si existe, devuelve el objeto almacenado sin consultar TVMaze.
3. Si no existe, consulta TVMaze, guarda la respuesta completa y la devuelve.

Las solicitudes simultáneas pueden consultar TVMaze más de una vez ante el mismo fallo de caché, pero las escrituras son idempotentes porque el ID del show se utiliza como `_id`.

## Crear comentarios

Registra un comentario y una calificación para un show existente:

```http
POST /api/v1/shows/1/comments
Content-Type: application/json
```

```json
{
  "comment": "Great show",
  "rating": 5
}
```

La respuesta utiliza `201 Created`:

```json
{
  "status": "CREATED",
  "commentId": "68d17b9510b2ac45f2931234",
  "showId": 1
}
```

El comentario es obligatorio, se normalizan los espacios de sus extremos y se permiten hasta 1,000 caracteres. La calificación debe ser un número entero entre 0 y 5. Antes de guardar se comprueba la existencia del show usando el flujo de caché; un show inexistente devuelve `404 Not Found` y no genera comentarios huérfanos.

Los comentarios se almacenan en una colección independiente llamada `comments`, con un índice ascendente por `show_id` y `created_at`. Separarlos de `shows_cache` evita duplicarlos o invalidar el caché cada vez que se agrega uno nuevo.

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

La colección utilizada es `shows_cache`. Cada documento utiliza el ID de TVMaze como `_id`, conserva el show completo y registra `cached_at`.

### Verificación del caché en Atlas

1. Inicia la aplicación con `MONGODB_URI` configurada.
2. Ejecuta `GET http://localhost:8080/api/v1/shows/1` desde Postman.
3. En Atlas, abre Data Explorer y comprueba que exista `tvmaze.shows_cache` con `_id: 1`.
4. Anota el valor de `cached_at` y repite la misma petición.
5. Comprueba que `cached_at` no cambió; esto demuestra que la segunda respuesta salió del caché y no volvió a guardarse.

### Verificación de comentarios en Atlas

1. Ejecuta el `POST /api/v1/shows/1/comments` desde Postman.
2. Comprueba que la respuesta sea `201 Created` y contenga `commentId`.
3. En Data Explorer abre `tvmaze.comments`.
4. Comprueba que el documento tenga `show_id`, `comment`, `rating` y `created_at`.
5. En la pestaña de índices comprueba que exista `show_id_created_at_idx`.
