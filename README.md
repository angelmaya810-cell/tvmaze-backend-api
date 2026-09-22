# TVMaze API

API REST desarrollada para la prueba técnica. Consulta series en [TVMaze](https://www.tvmaze.com/api), conserva el detalle de cada serie en MongoDB y permite registrar comentarios con una calificación de 0 a 5.

## Funcionalidad

- Busca series por texto y devuelve `id`, nombre, canal, resumen, géneros y comentarios.
- Obtiene el detalle completo de una serie y agrega sus comentarios locales.
- Guarda comentarios y calificaciones en MongoDB.
- Aplica cache-aside al detalle: primero consulta MongoDB y, si no existe, consulta TVMaze y guarda la respuesta.
- Valida entradas y responde errores en formato `application/problem+json`.

## Tecnologías y diseño

- Java 21
- Spring Boot 4.1.1
- Spring Web y Bean Validation
- Spring Data MongoDB
- Maven Wrapper
- JUnit 5, Mockito y MockMvc

La aplicación separa controladores, casos de uso, dominio y adaptadores externos. Los servicios dependen de puertos y no de los detalles de TVMaze o MongoDB:

```text
HTTP -> Controllers -> Application services -> Output ports
                                                |-> TVMaze HTTP adapter
                                                `-> MongoDB adapters
```

Los paquetes principales son `show`, `comment` y `common`. Esta organización facilita probar cada regla de manera aislada y sustituir una integración sin modificar los casos de uso.

## Requisitos

- JDK 21
- Docker Desktop, si se usará MongoDB local; o una cuenta de MongoDB Atlas
- Acceso a `https://api.tvmaze.com`

No es necesario instalar Maven: el repositorio incluye Maven Wrapper.

## Ejecución rápida

### Opción A: MongoDB local con Docker

Desde la raíz del proyecto:

```powershell
docker compose up -d mongodb
.\mvnw.cmd spring-boot:run
```

En Linux o macOS:

```bash
docker compose up -d mongodb
./mvnw spring-boot:run
```

La configuración predeterminada usa `mongodb://localhost:27017/tvmaze`.

### Opción B: MongoDB Atlas

1. En Atlas, crea un usuario con acceso de lectura y escritura a la base `tvmaze`.
2. Autoriza la IP desde la que ejecutarás la aplicación.
3. Copia la cadena de conexión y agrega `/tvmaze` antes de sus parámetros.
4. Define la variable y arranca la aplicación **en la misma terminal**.

PowerShell:

```powershell
$env:MONGODB_URI='mongodb+srv://USUARIO:CONTRASENA@CLUSTER/tvmaze?retryWrites=true&w=majority'
.\mvnw.cmd spring-boot:run
```

Bash:

```bash
export MONGODB_URI='mongodb+srv://USUARIO:CONTRASENA@CLUSTER/tvmaze?retryWrites=true&w=majority'
./mvnw spring-boot:run
```

Si la contraseña contiene caracteres especiales, debe codificarse para poder usarse dentro de una URL. El archivo `.env.example` solo sirve como referencia: Spring Boot no carga un archivo `.env` automáticamente. No guardes una URI real en el repositorio.

Cuando aparezca el mensaje `Started TvMazeApiApplication`, la API estará disponible en `http://localhost:8080`.

## Configuración

| Variable | Valor predeterminado | Uso |
|---|---|---|
| `MONGODB_URI` | `mongodb://localhost:27017/tvmaze` | Conexión a MongoDB |
| `TVMAZE_BASE_URL` | `https://api.tvmaze.com` | URL base del proveedor |
| `TVMAZE_CONNECT_TIMEOUT` | `2s` | Tiempo máximo para abrir la conexión |
| `TVMAZE_READ_TIMEOUT` | `5s` | Tiempo máximo de lectura |
| `TVMAZE_USER_AGENT` | `coppel-tvmaze-api/0.0.1` | Identificación del cliente HTTP |

## API

| Método | Ruta | Resultado correcto |
|---|---|---|
| `GET` | `/api/v1/shows/search?search_query={texto}` | `200 OK` |
| `GET` | `/api/v1/shows/{showId}` | `200 OK` |
| `POST` | `/api/v1/shows/{showId}/comments` | `201 Created` |

### Buscar series

```http
GET /api/v1/shows/search?search_query=under%20the%20dome
```

Ejemplo de respuesta:

```json
[
  {
    "id": 1,
    "name": "Under the Dome",
    "channel": "CBS",
    "summary": "<p>Under the Dome...</p>",
    "genres": ["Drama", "Science-Fiction", "Thriller"],
    "comments": [
      {
        "comment": "Muy buena serie",
        "rating": 5
      }
    ]
  }
]
```

Una búsqueda sin coincidencias devuelve `[]`. `search_query` es obligatorio y no puede estar vacío.

### Obtener el detalle de una serie

```http
GET /api/v1/shows/1
```

La respuesta conserva los campos entregados por TVMaze y agrega `comments`:

```json
{
  "id": 1,
  "name": "Under the Dome",
  "language": "English",
  "genres": ["Drama", "Science-Fiction", "Thriller"],
  "status": "Ended",
  "comments": [
    {
      "comment": "Muy buena serie",
      "rating": 5
    }
  ]
}
```

La primera consulta de un ID obtiene el detalle de TVMaze y lo guarda en `shows_cache`; las siguientes lecturas usan MongoDB. Los comentarios se consultan siempre por separado para que el detalle almacenado no quede desactualizado.

### Crear un comentario

```http
POST /api/v1/shows/1/comments
Content-Type: application/json

{
  "comment": "Muy buena serie",
  "rating": 5
}
```

Respuesta:

```json
{
  "status": "CREATED",
  "commentId": "identificador-generado",
  "showId": 1
}
```

Reglas de validación:

- `showId` debe ser mayor que cero y debe existir en TVMaze.
- `comment` es obligatorio, no acepta solo espacios y admite hasta 1000 caracteres.
- `rating` es un entero obligatorio entre 0 y 5.

## Errores

Los errores controlados usan `application/problem+json`.

| Estado | Caso |
|---|---|
| `400 Bad Request` | Parámetro, ID o cuerpo inválido |
| `404 Not Found` | La serie no existe |
| `502 Bad Gateway` | TVMaze no está disponible o entrega una respuesta inválida |

Ejemplo:

```json
{
  "type": "about:blank",
  "title": "Request validation failed",
  "status": 400,
  "detail": "rating must be between 0 and 5",
  "instance": "/api/v1/shows/1/comments"
}
```

## Persistencia

MongoDB utiliza dos colecciones:

- `shows_cache`: `_id`, objeto `show` y fecha `cached_at`.
- `comments`: `_id`, `show_id`, `comment`, `rating` y `created_at`.

La colección `comments` crea el índice compuesto `show_id_created_at_idx` para localizar y ordenar los comentarios de una serie. La búsqueda agrupa los IDs y realiza una sola consulta de comentarios, evitando una consulta por cada resultado.

## Probar con Postman

1. Inicia la aplicación.
2. Importa [`postman/TVMaze API.postman_collection.json`](postman/TVMaze%20API.postman_collection.json).
3. Revisa las variables de colección: `baseUrl`, `showId` y `searchQuery`.
4. Ejecuta `Search shows` para localizar una serie.
5. Ejecuta `Create comment`.
6. Ejecuta `Get show detail` y después `Search shows` para confirmar que aparece el comentario.
7. En Atlas, opcionalmente verifica los documentos en `Browse Collections`, dentro de la base `tvmaze`.

La colección no contiene usuarios, contraseñas ni cadenas de conexión.

También puedes probar desde PowerShell:

```powershell
Invoke-RestMethod 'http://localhost:8080/api/v1/shows/search?search_query=girls'

Invoke-RestMethod 'http://localhost:8080/api/v1/shows/1/comments' `
  -Method Post `
  -ContentType 'application/json' `
  -Body '{"comment":"Recomendada","rating":5}'

Invoke-RestMethod 'http://localhost:8080/api/v1/shows/1'
```

## Pruebas automatizadas

Windows:

```powershell
.\mvnw.cmd clean verify
```

Linux o macOS:

```bash
./mvnw clean verify
```

La suite contiene 42 pruebas unitarias y de capa web. Las integraciones HTTP y MongoDB están aisladas mediante dobles de prueba, por lo que no es necesario tener TVMaze o MongoDB disponibles para ejecutarla.

```
Autor:
Angel Maya
22 Septiembre 2026
