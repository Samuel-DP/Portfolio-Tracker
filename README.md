# Portfolio Tracker

Aplicación de escritorio desarrollada en JavaFX para gestionar carteras de inversión, registrar operaciones realizadas en los mercados financieros, obtener estadísticas sobre ellas y analizar si nuestra toma de decisiones resulta rentable en el tiempo.

El proyecto está pensado como una herramienta personal para realizar un seguimiento de nuestras inversiones, centralizando portfolios, transacciones, favoritos y precios de mercado, combinando datos guardados en SQL Server con información obtenida desde APIs externas como Finnhub y CoinGecko.

## Características

- Registro e inicio de sesión de usuarios.
- Almacenamiento de credenciales con hash mediante BCrypt.
- Gestión de portfolios por usuario.
- Registro de transacciones de compra, venta y transferencia en mercados financieros.
- Cálculo de estadísticas a partir de las transacciones realizadas.
- Gráfico lineal para visualizar la evolución del saldo de la cartera.
- Gráfico tipo donut para mostrar la distribución porcentual de los activos en cartera.
- Métricas de portfolio como base de costo, rendimiento, mejor activo y peor activo.
- Tabla resumen de activos mantenidos en cartera, incluyendo estadísticas calculadas por posición.
- Consulta de acciones mediante Finnhub.
- Consulta de criptomonedas mediante CoinGecko.
- Vista de favoritos para seguir activos seleccionados.
- Interfaz diseñada con JavaFX, FXML, CSS y Scene Builder.
- Persistencia de datos en SQL Server.
- Separación por capas: controladores JavaFX, modelos y DAOs.
- Configuración sensible mediante variables de entorno.

## Tecnologías

- Java 23
- JavaFX
- Scene Builder
- FXML y CSS
- SQL Server
- JDBC
- Finnhub API
- CoinGecko API
- org.json
- jBCrypt
- NetBeans / Ant

## Estructura del proyecto

```text
src/
+-- Controlador/       # Controladores JavaFX de las vistas
+-- Dao/               # Acceso a datos y consultas SQL
+-- Imagenes/          # Recursos graficos de la aplicación
+-- Modelo/            # Entidades, servicios y configuración
+-- Vista/             # Archivos FXML y hojas de estilo CSS
+-- portfoliotracker/  # Clase principal de arranque
database/
+-- schema.sql         # Script de creación de tablas para SQL Server
```

La clase principal es:

```text
portfoliotracker.PortfolioTracker
```

## Configuración

La aplicación no guarda credenciales ni claves API directamente en el código. Para ejecutarla correctamente hay que definir estas variables de entorno:

| Variable | Descripción |
| --- | --- |
| `DB_HOST` | Host o IP del servidor SQL Server |
| `DB_PORT` | Puerto de SQL Server |
| `DB_NAME` | Nombre de la base de datos |
| `DB_USER` | Usuario de la base de datos |
| `DB_PASSWORD` | Contraseña de la base de datos |
| `FINNHUB_KEY` | Clave API de Finnhub |

Ejemplo en PowerShell para variables de usuario:

```powershell
[Environment]::SetEnvironmentVariable("DB_HOST", "localhost", "User")
[Environment]::SetEnvironmentVariable("DB_PORT", "1433", "User")
[Environment]::SetEnvironmentVariable("DB_NAME", "PortfolioTracker", "User")
[Environment]::SetEnvironmentVariable("DB_USER", "tu_usuario", "User")
[Environment]::SetEnvironmentVariable("DB_PASSWORD", "tu_password", "User")
[Environment]::SetEnvironmentVariable("FINNHUB_KEY", "tu_api_key", "User")
```

Después de crear o modificar variables de entorno, cierra y vuelve a abrir NetBeans o la terminal desde la que ejecutes la aplicación.

## Base de datos

El proyecto usa SQL Server mediante JDBC. El script de creación de tablas está disponible en:

```text
database/schema.sql
```

Para preparar la base de datos desde cero:

1. Crea una base de datos en SQL Server, por ejemplo `PortfolioTracker`.
2. Ejecuta `database/schema.sql` sobre esa base de datos.
3. Configura las variables de entorno indicadas en la sección de configuración.
4. Inicia la aplicación y registra un usuario desde la interfaz.

Tablas principales usadas por el codigo:

- `USUARIOS`
- `PORTFOLIOS`
- `ACTIVOS`
- `TRANSACCIONES`
- `FAVORITOS`

No se incluyen datos seed obligatorios. Los usuarios, portfolios, activos, transacciones y favoritos se crean desde la propia aplicación. Al iniciar sesión, si el usuario no tiene portfolios, la aplicación crea automáticamente un portfolio por defecto.

> Nota: si clonas este proyecto desde cero, necesitas crear la base de datos y ejecutar `database/schema.sql` antes de usar la aplicación.

## Ejecución

### Desde NetBeans

1. Clona el repositorio.
2. Abre el proyecto en NetBeans.
3. Configura las librerias necesarias:
   - JavaFX
   - Microsoft JDBC Driver for SQL Server
   - org.json
   - jBCrypt
4. Crea las variables de entorno indicadas en la sección de configuración.
5. Ejecuta el proyecto desde NetBeans.

### Desde Ant

Si tienes Ant configurado en el sistema:

```bash
ant clean
ant jar
ant run
```

## APIs externas

### Finnhub

Se usa para obtener información de acciones, perfiles de empresas y cotizaciones.

La clave se lee desde:

```java
System.getenv("FINNHUB_KEY")
```

### CoinGecko

Se usa para obtener información de mercado de criptomonedas. Actualmente no requiere clave API en esta configuración.

## Seguridad

- La clave de Finnhub se carga desde variable de entorno.
- Las credenciales de base de datos se cargan desde variables de entorno.
- Las passwords de usuario se almacenan con hash BCrypt.
- No deben subirse claves API, passwords, archivos `nbproject/private/`, builds generados ni archivos `.class` al repositorio.

## Autor

Desarrollado por Samuel.
