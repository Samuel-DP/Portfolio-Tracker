# Portfolio Tracker

Aplicacion de escritorio desarrollada en JavaFX para gestionar carteras de inversion, registrar operaciones y consultar informacion de mercado de acciones y criptomonedas.

El proyecto esta pensado como una herramienta personal para centralizar portfolios, transacciones, favoritos y precios de mercado, usando una base de datos SQL Server y APIs externas.

## Caracteristicas

- Registro e inicio de sesion de usuarios.
- Almacenamiento de credenciales con hash mediante BCrypt.
- Gestion de portfolios por usuario.
- Registro de transacciones de compra, venta y transferencia.
- Consulta de acciones mediante Finnhub.
- Consulta de criptomonedas mediante CoinGecko.
- Vista de favoritos para seguir activos seleccionados.
- Persistencia de datos en SQL Server.
- Separacion por capas: controladores JavaFX, modelos y DAOs.
- Configuracion sensible mediante variables de entorno.

## Tecnologias

- Java 23
- JavaFX
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
+-- Imagenes/          # Recursos graficos de la aplicacion
+-- Modelo/            # Entidades, servicios y configuracion
+-- Vista/             # Archivos FXML y hojas de estilo CSS
+-- portfoliotracker/  # Clase principal de arranque
```

La clase principal es:

```text
portfoliotracker.PortfolioTracker
```

## Configuracion

La aplicacion no guarda credenciales ni claves API directamente en el codigo. Para ejecutarla correctamente hay que definir estas variables de entorno:

| Variable | Descripcion |
| --- | --- |
| `DB_HOST` | Host o IP del servidor SQL Server |
| `DB_PORT` | Puerto de SQL Server |
| `DB_NAME` | Nombre de la base de datos |
| `DB_USER` | Usuario de la base de datos |
| `DB_PASSWORD` | Password de la base de datos |
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

Despues de crear o modificar variables de entorno, cierra y vuelve a abrir NetBeans o la terminal desde la que ejecutes la aplicacion.

## Base de datos

El proyecto usa SQL Server mediante JDBC. La aplicacion espera una base de datos con tablas para usuarios, portfolios, activos, transacciones, favoritos y cotizaciones/cache de mercado.

Tablas principales usadas por el codigo:

- `USUARIOS`
- `PORTFOLIOS`
- `ACTIVOS`
- `TRANSACCIONES`
- `FAVORITOS`

> Nota: si clonas este proyecto desde cero, necesitaras crear la base de datos y las tablas antes de ejecutar la aplicacion.

## Ejecucion

### Desde NetBeans

1. Clona el repositorio.
2. Abre el proyecto en NetBeans.
3. Configura las librerias necesarias:
   - JavaFX
   - Microsoft JDBC Driver for SQL Server
   - org.json
   - jBCrypt
4. Crea las variables de entorno indicadas en la seccion de configuracion.
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

Se usa para obtener informacion de acciones, perfiles de empresas y cotizaciones.

La clave se lee desde:

```java
System.getenv("FINNHUB_KEY")
```

### CoinGecko

Se usa para obtener informacion de mercado de criptomonedas. Actualmente no requiere clave API en esta configuracion.

## Seguridad

- La clave de Finnhub se carga desde variable de entorno.
- Las credenciales de base de datos se cargan desde variables de entorno.
- Las passwords de usuario se almacenan con hash BCrypt.
- No deben subirse claves API, passwords, archivos `nbproject/private/`, builds generados ni archivos `.class` al repositorio.

## Estado del proyecto

Proyecto en desarrollo. Funcionalidades principales implementadas:

- Autenticacion de usuarios.
- Gestion de carteras.
- Gestion de transacciones.
- Consulta de acciones y criptomonedas.
- Favoritos.
- Persistencia en SQL Server.

Mejoras previstas:

- Script SQL de creacion de base de datos.
- Limpieza del repositorio para excluir archivos generados.
- Tests unitarios para servicios y DAOs.
- Capturas de pantalla de la interfaz.
- Empaquetado mas sencillo para distribucion.

## Autor

Desarrollado por Samuel.
