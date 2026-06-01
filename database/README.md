# Database

This folder contains the SQL Server schema required to run Portfolio Tracker.

## Files

- `schema.sql`: creates the application tables, relationships, unique constraints, and validations.

## Setup

1. Create a database in SQL Server, for example `PortfolioTracker`.
2. Open `schema.sql` in SQL Server Management Studio, Azure Data Studio, or another SQL Server client.
3. Select the database you created.
4. Run the script.
5. Configure the application environment variables:

```powershell
[Environment]::SetEnvironmentVariable("DB_HOST", "localhost", "User")
[Environment]::SetEnvironmentVariable("DB_PORT", "1433", "User")
[Environment]::SetEnvironmentVariable("DB_NAME", "PortfolioTracker", "User")
[Environment]::SetEnvironmentVariable("DB_USER", "your_user", "User")
[Environment]::SetEnvironmentVariable("DB_PASSWORD", "your_password", "User")
```

No required seed data is needed. Users, portfolios, assets, transactions, and favorites are created from the application while it is being used.
