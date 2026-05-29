# Base de datos

Esta carpeta contiene el esquema SQL Server necesario para ejecutar Portfolio Tracker.

## Archivos

- `schema.sql`: crea las tablas de la aplicación, relaciones, restricciones únicas y validaciones.

## Preparación

1. Crea una base de datos en SQL Server, por ejemplo `PortfolioTracker`.
2. Abre `schema.sql` en SQL Server Management Studio, Azure Data Studio u otro cliente SQL Server.
3. Selecciona la base de datos creada.
4. Ejecuta el script.
5. Configura las variables de entorno de la aplicación:

```powershell
[Environment]::SetEnvironmentVariable("DB_HOST", "localhost", "User")
[Environment]::SetEnvironmentVariable("DB_PORT", "1433", "User")
[Environment]::SetEnvironmentVariable("DB_NAME", "PortfolioTracker", "User")
[Environment]::SetEnvironmentVariable("DB_USER", "tu_usuario", "User")
[Environment]::SetEnvironmentVariable("DB_PASSWORD", "tu_password", "User")
```

No se necesitan seeds obligatorios. Los usuarios, portfolios, activos, transacciones y favoritos se crean desde la aplicación mientras se usa.
