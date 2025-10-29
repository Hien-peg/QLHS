# Database initialization — db/init.sql

This folder contains the SQL file used to create the demo database and seed sample data for the QLHS application.

Files
- `init.sql` — full CREATE DATABASE, tables, views, triggers and sample data. Running this will create database `QLHS_New`.

How to load (safe, manual)

1. Backup any existing database if you have one named `QLHS_New`.
2. From PowerShell (will prompt for password):

```powershell
mysql -u root -p < db\init.sql
```

3. Or, open mysql client then SOURCE the file (Windows absolute path):

```powershell
mysql -u root -p
# at mysql> prompt:
SOURCE D:/huyngyuen/nam_5/Java/JavaCode/QLHS/db/init.sql;
```

Configuration — DatabaseConnection

The application reads DB settings in this order:
- System properties: `-Ddb.name=...`, `-Ddb.user=...`, `-Ddb.pass=...`
- Environment variables: `QLHS_DB`, `QLHS_USER`, `QLHS_PASS`
- Defaults: db name `QLHS_New`, user `root`, password (from env or system property)

So to run the app against a different DB name or credentials, start Java with e.g.:

```powershell
# set via system properties
java -Ddb.name=MyDB -Ddb.user=myuser -Ddb.pass=mypass -cp build\classes;lib\mysql-connector-j-8.0.33.jar com.sgu.qlhs.ui.MainDashboard
```

Notes & next steps
- The SQL script will create tables that match the current DAO/DTO expectations (MonHoc includes `GhiChu`, Diem columns and generated columns are present).
- I updated `ThongKeLopSucChuaDialog` to load data from BUS/DAO instead of hardcoded sample rows.
- If you prefer non-destructive migration scripts instead of full `init.sql`, tell me and I'll produce ALTER/INSERT-only scripts.
