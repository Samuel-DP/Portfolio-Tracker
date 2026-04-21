package Dao;

import Modelo.ActivoPortfolioResumen;
import Modelo.ConfigDB;
import Modelo.PortfolioItem;
import Modelo.PortfolioService;
import Modelo.PrecioActivoService;
import Modelo.Transaccion;
import Modelo.vGlobales;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class TransaccionesDAO {

    private static final Set<String> TICKERS_STOCKS = new HashSet<>(Arrays.asList(
            "AAPL", "MSFT", "AMZN", "GOOGL", "TSLA", "NVDA", "RACE", "BLK", "V", "MA", "MSTR", "ASML", "ASTS"
    ));

    public static List<Transaccion> obtenerTransaccionesPorPortfolio(int portfolioId) {
        List<Transaccion> transacciones = new ArrayList<>();
        String sql = "SELECT T.ID, T.TIPO_TRANSACCION, T.TIPO_TRANSFERENCIA, T.CANTIDAD, T.PRECIO_UNITARIO, T.IMPORTE_TOTAL, T.FECHA_TRANSACCION, T.NOTAS, "
                + "A.NOMBRE, A.SIMBOLO "
                + "FROM TRANSACCIONES T "
                + "INNER JOIN ACTIVOS A ON A.ID = T.ACTIVO_ID "
                + "WHERE T.PORTFOLIO_ID = ? "
                + "ORDER BY T.FECHA_TRANSACCION DESC, T.ID DESC";

        try (Connection conn = ConexionDB.getConexion(vGlobales.getCadena(), ConfigDB.getUser(), ConfigDB.getPassword())) {
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, portfolioId);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        transacciones.add(mapearTransaccion(rs));
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return transacciones;
    }

    public static Transaccion insertarTransaccion(int portfolioId, Transaccion transaccion) {
        String sql = "INSERT INTO TRANSACCIONES (PORTFOLIO_ID, ACTIVO_ID, TIPO_TRANSACCION, TIPO_TRANSFERENCIA, CANTIDAD, PRECIO_UNITARIO, IMPORTE_TOTAL, FECHA_TRANSACCION, NOTAS) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConexionDB.getConexion(vGlobales.getCadena(), ConfigDB.getUser(), ConfigDB.getPassword())) {
            Integer activoId = obtenerOCrearActivoId(conn, transaccion.getActivo());
            if (activoId == null) {
                return null;
            }

            String tipoTransaccion = mapearTipoTransaccionDB(transaccion.getTipo());
            String tipoTransferencia = mapearTipoTransferenciaDB(transaccion.getTipo());

            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, portfolioId);
                ps.setInt(2, activoId);
                ps.setString(3, tipoTransaccion);
                ps.setString(4, tipoTransferencia);
                ps.setDouble(5, transaccion.getUnidades());
                ps.setDouble(6, transaccion.getPrecioPorMoneda());
                ps.setDouble(7, transaccion.getImporte());
                ps.setTimestamp(8, Timestamp.valueOf(transaccion.getFecha()));
                ps.setString(9, transaccion.getNotas());

                int filas = ps.executeUpdate();
                if (filas == 0) {
                    return null;
                }

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        int idGenerado = keys.getInt(1);
                        return new Transaccion(
                                idGenerado,
                                transaccion.getTipo(),
                                transaccion.getFecha(),
                                transaccion.getActivo(),
                                transaccion.getUnidades(),
                                transaccion.getPrecioPorMoneda(),
                                transaccion.getImporte(),
                                transaccion.getNotas()
                        );
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static boolean eliminarTransaccion(int usuarioId, int transaccionId) {
        String sql = "DELETE T "
                + "FROM TRANSACCIONES T "
                + "INNER JOIN PORTFOLIOS P ON P.ID = T.PORTFOLIO_ID "
                + "WHERE T.ID = ? AND P.USUARIO_ID = ?";

        try (Connection conn = ConexionDB.getConexion(vGlobales.getCadena(), ConfigDB.getUser(), ConfigDB.getPassword())) {
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, transaccionId);
                ps.setInt(2, usuarioId);
                return ps.executeUpdate() > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static Transaccion actualizarTransaccion(int usuarioId, int transaccionId, Transaccion transaccion) {
        String sql = "UPDATE T "
                + "SET T.ACTIVO_ID = ?, "
                + "T.TIPO_TRANSACCION = ?, "
                + "T.TIPO_TRANSFERENCIA = ?, "
                + "T.CANTIDAD = ?, "
                + "T.PRECIO_UNITARIO = ?, "
                + "T.IMPORTE_TOTAL = ?, "
                + "T.FECHA_TRANSACCION = ?, "
                + "T.NOTAS = ? "
                + "FROM TRANSACCIONES T "
                + "INNER JOIN PORTFOLIOS P ON P.ID = T.PORTFOLIO_ID "
                + "WHERE T.ID = ? AND P.USUARIO_ID = ?";

        try (Connection conn = ConexionDB.getConexion(vGlobales.getCadena(), ConfigDB.getUser(), ConfigDB.getPassword())) {
            Integer activoId = obtenerOCrearActivoId(conn, transaccion.getActivo());
            if (activoId == null) {
                return null;
            }

            String tipoTransaccion = mapearTipoTransaccionDB(transaccion.getTipo());
            String tipoTransferencia = mapearTipoTransferenciaDB(transaccion.getTipo());

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, activoId);
                ps.setString(2, tipoTransaccion);
                ps.setString(3, tipoTransferencia);
                ps.setDouble(4, transaccion.getUnidades());
                ps.setDouble(5, transaccion.getPrecioPorMoneda());
                ps.setDouble(6, transaccion.getImporte());
                ps.setTimestamp(7, Timestamp.valueOf(transaccion.getFecha()));
                ps.setString(8, transaccion.getNotas());
                ps.setInt(9, transaccionId);
                ps.setInt(10, usuarioId);

                int filas = ps.executeUpdate();
                if (filas > 0) {
                    return new Transaccion(
                            transaccionId,
                            transaccion.getTipo(),
                            transaccion.getFecha(),
                            transaccion.getActivo(),
                            transaccion.getUnidades(),
                            transaccion.getPrecioPorMoneda(),
                            transaccion.getImporte(),
                            transaccion.getNotas()
                    );
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static Transaccion mapearTransaccion(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("FECHA_TRANSACCION");
        LocalDateTime fecha = ts != null ? ts.toLocalDateTime() : LocalDateTime.now();
        String simbolo = rs.getString("SIMBOLO");
        String nombre = rs.getString("NOMBRE");
        String activo = (nombre != null && !nombre.isBlank())
                ? (nombre + " " + simbolo)
                : simbolo;

        return new Transaccion(
                rs.getInt("ID"),
                mapearTipoTransaccionUI(rs.getString("TIPO_TRANSACCION"), rs.getString("TIPO_TRANSFERENCIA")),
                fecha,
                activo,
                rs.getDouble("CANTIDAD"),
                rs.getDouble("PRECIO_UNITARIO"),
                rs.getDouble("IMPORTE_TOTAL"),
                rs.getString("NOTAS")
        );
    }

    private static String mapearTipoTransaccionDB(String tipoUI) {
        if ("COMPRA".equalsIgnoreCase(tipoUI)) {
            return "BUY";
        }

        if ("VENTA".equalsIgnoreCase(tipoUI)) {
            return "SELL";
        }

        return "TRANSFER";
    }

    private static String mapearTipoTransferenciaDB(String tipoUI) {
        if ("Transferencia entrante".equalsIgnoreCase(tipoUI)) {
            return "IN";
        }

        if ("Transferencia saliente".equalsIgnoreCase(tipoUI)) {
            return "OUT";
        }

        return null;
    }

    private static String mapearTipoTransaccionUI(String tipo, String transferencia) {
        if ("BUY".equalsIgnoreCase(tipo)) {
            return "COMPRA";
        }

        if ("SELL".equalsIgnoreCase(tipo)) {
            return "VENTA";
        }

        if ("IN".equalsIgnoreCase(transferencia)) {
            return "Transferencia entrante";
        }

        return "Transferencia saliente";
    }

    private static Integer obtenerOCrearActivoId(Connection conn, String activoDescripcion) throws SQLException {
        String simbolo = extraerSimbolo(activoDescripcion);
        Integer activoId = obtenerActivoIdPorSimbolo(conn, simbolo);

        if (activoId != null) {
            return activoId;
        }

        return crearActivo(conn, activoDescripcion, simbolo);
    }

    private static Integer obtenerActivoIdPorSimbolo(Connection conn, String simbolo) throws SQLException {
        String sql = "SELECT TOP 1 ID FROM ACTIVOS WHERE SIMBOLO = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, simbolo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ID");
                }
            }
        }

        return null;
    }

    private static Integer crearActivo(Connection conn, String descripcion, String simbolo) throws SQLException {
        String sql = "INSERT INTO ACTIVOS (TIPO_ACTIVO, SIMBOLO, NOMBRE, API_ID, FUENTE) VALUES (?, ?, ?, ?, ?)";
        String tipoActivo = TICKERS_STOCKS.contains(simbolo.toUpperCase()) ? "STOCK" : "CRYPTO";
        String nombre = extraerNombre(descripcion, simbolo);
        String fuente = "STOCK".equals(tipoActivo) ? "FINNHUB" : "COINGECKO";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, tipoActivo);
            ps.setString(2, simbolo);
            ps.setString(3, nombre);
            ps.setString(4, simbolo);
            ps.setString(5, fuente);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        return obtenerActivoIdPorSimbolo(conn, simbolo);
    }

    private static String extraerSimbolo(String descripcion) {
        if (descripcion == null || descripcion.isBlank()) {
            return "";
        }

        String valor = descripcion.trim();
        int ultimoEspacio = valor.lastIndexOf(' ');
        if (ultimoEspacio < 0 || ultimoEspacio == valor.length() - 1) {
            return valor.toUpperCase();
        }

        return valor.substring(ultimoEspacio + 1).trim().toUpperCase();
    }

    private static String extraerNombre(String descripcion, String simbolo) {
        if (descripcion == null || descripcion.isBlank()) {
            return simbolo;
        }

        String valor = descripcion.trim();
        if (valor.equalsIgnoreCase(simbolo)) {
            return simbolo;
        }

        int ultimoEspacio = valor.lastIndexOf(' ');
        if (ultimoEspacio <= 0) {
            return valor;
        }

        return valor.substring(0, ultimoEspacio).trim();
    }

    public static List<ActivoPortfolioResumen> obtenerResumenActivosPortfolioActual() {
        Integer portfolioId = obtenerPortfolioActualId();
        if (portfolioId == null) {
            return new ArrayList<>();
        }

        List<TransaccionActivoRow> transacciones = obtenerTransaccionesParaResumen(portfolioId);
        Map<String, PosicionActivo> posiciones = construirPosicionesPorActivo(transacciones);
        List<ActivoPortfolioResumen> resumen = new ArrayList<>();

        for (PosicionActivo posicion : posiciones.values()) {
            double unidadesTotales = posicion.getUnidadesTotales();
            if (unidadesTotales <= 0) {
                continue;
            }

            PrecioActivoService.CotizacionActivo cotizacion = PrecioActivoService.obtenerCotizacion(posicion.descripcionActivo);
            double precioActual = cotizacion.getPrecioActual();
            double dineroEntranteTransferencias = posicion.unidadesTransferenciaEntrante * precioActual;
            double inversion = posicion.costeAcumuladoTrading + dineroEntranteTransferencias;
            double valorActualTrading = posicion.unidadesTrading * precioActual;
            double gananciaTrading = valorActualTrading - posicion.costeAcumuladoTrading;
            double gananciaPerdida = posicion.unidadesTrading > 0
                    ? gananciaTrading
                    : dineroEntranteTransferencias;
            double precioPromedio = posicion.unidadesTrading > 0
                    ? posicion.costeAcumuladoTrading / posicion.unidadesTrading
                    : 0;
            Double porcentVariacion = precioPromedio > 0
                    ? ((precioActual - precioPromedio) / precioPromedio) * 100
                    : null;

            resumen.add(new ActivoPortfolioResumen(
                    posicion.descripcionActivo,
                    formatearMoneda(precioActual),
                    formatearPorcentaje(cotizacion.getCambioPorcentual24h()),
                    formatearMoneda(inversion),
                    formatearCantidad(unidadesTotales),
                    formatearMoneda(precioPromedio),
                    formatearMonedaConSigno(gananciaPerdida),
                    formatearPorcentajeOMarcador(porcentVariacion)
            ));
        }

        return resumen;
    }

    private static List<TransaccionActivoRow> obtenerTransaccionesParaResumen(int portfolioId) {
        List<TransaccionActivoRow> rows = new ArrayList<>();
        String sql = "SELECT T.TIPO_TRANSACCION, T.TIPO_TRANSFERENCIA, T.CANTIDAD, T.PRECIO_UNITARIO, T.FECHA_TRANSACCION, A.NOMBRE, A.SIMBOLO "
                + "FROM TRANSACCIONES T "
                + "INNER JOIN ACTIVOS A ON A.ID = T.ACTIVO_ID "
                + "WHERE T.PORTFOLIO_ID = ? "
                + "ORDER BY T.FECHA_TRANSACCION ASC, T.ID ASC";

        try (Connection conn = ConexionDB.getConexion(vGlobales.getCadena(), ConfigDB.getUser(), ConfigDB.getPassword()); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, portfolioId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String simbolo = rs.getString("SIMBOLO");
                    String nombre = rs.getString("NOMBRE");
                    String descripcionActivo = (nombre != null && !nombre.isBlank())
                            ? (nombre + " " + simbolo)
                            : simbolo;
                    Timestamp fecha = rs.getTimestamp("FECHA_TRANSACCION");

                    rows.add(new TransaccionActivoRow(
                            descripcionActivo,
                            mapearTipoTransaccionUI(rs.getString("TIPO_TRANSACCION"), rs.getString("TIPO_TRANSFERENCIA")),
                            rs.getDouble("CANTIDAD"),
                            rs.getDouble("PRECIO_UNITARIO"),
                            fecha == null ? 0L : fecha.getTime()
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        rows.sort(Comparator.comparingLong(r -> r.fechaMillis));
        return rows;
    }

    private static Map<String, PosicionActivo> construirPosicionesPorActivo(List<TransaccionActivoRow> transacciones) {
        Map<String, PosicionActivo> posiciones = new LinkedHashMap<>();

        for (TransaccionActivoRow t : transacciones) {
            PosicionActivo posicion = posiciones.computeIfAbsent(t.descripcionActivo, PosicionActivo::new);

            if ("COMPRA".equalsIgnoreCase(t.tipo)) {
                posicion.unidadesTrading += t.cantidad;
                posicion.costeAcumuladoTrading += (t.cantidad * t.precioUnitario);
            } else if ("VENTA".equalsIgnoreCase(t.tipo)) {
                if (posicion.getUnidadesTotales() <= 0) {
                    continue;
                }

                posicion.aplicarSalida(t.cantidad);
            } else if ("Transferencia entrante".equalsIgnoreCase(t.tipo)) {
                posicion.unidadesTransferenciaEntrante += t.cantidad;
            } else if ("Transferencia saliente".equalsIgnoreCase(t.tipo)) {
                posicion.aplicarSalida(t.cantidad);
            }

        }

        return posiciones;
    }

    private static String formatearMoneda(double valor) {
        return String.format(Locale.US, "$%,.2f", valor);
    }

    private static String formatearMonedaConSigno(double valor) {
        return String.format(Locale.US, "%s$%,.2f", valor >= 0 ? "+" : "-", Math.abs(valor));
    }

    private static String formatearCantidad(double cantidad) {
        return String.format(Locale.US, "%,.6f", cantidad).replaceAll("0+$", "").replaceAll("\\.$", "");
    }

    private static String formatearPorcentaje(double porcentaje) {
        return String.format(Locale.US, "%+.2f%%", porcentaje);
    }

    private static String formatearPorcentajeOMarcador(Double porcentaje) {
        return porcentaje == null ? "--%" : formatearPorcentaje(porcentaje);
    }

    private static class TransaccionActivoRow {

        private final String descripcionActivo;
        private final String tipo;
        private final double cantidad;
        private final double precioUnitario;
        private final long fechaMillis;

        private TransaccionActivoRow(String descripcionActivo, String tipo, double cantidad, double precioUnitario, long fechaMillis) {
            this.descripcionActivo = descripcionActivo;
            this.tipo = tipo;
            this.cantidad = cantidad;
            this.precioUnitario = precioUnitario;
            this.fechaMillis = fechaMillis;
        }
    }

    private static class PosicionActivo {

        private final String descripcionActivo;
        private double unidadesTrading;
        private double costeAcumuladoTrading;
        private double unidadesTransferenciaEntrante;

        private PosicionActivo(String descripcionActivo) {
            this.descripcionActivo = descripcionActivo;
        }

        private double getUnidadesTotales() {
            return unidadesTrading + unidadesTransferenciaEntrante;
        }

        private void aplicarSalida(double cantidadSalida) {
            if (cantidadSalida <= 0) {
                return;
            }

            double restante = cantidadSalida;

            double salidaTrading = Math.min(restante, unidadesTrading);
            if (salidaTrading > 0) {
                double precioPromedioTrading = unidadesTrading > 0 ? costeAcumuladoTrading / unidadesTrading : 0;
                costeAcumuladoTrading -= (salidaTrading * precioPromedioTrading);
                unidadesTrading -= salidaTrading;
                restante -= salidaTrading;
            }

            if (restante > 0) {
                double salidaTransferencia = Math.min(restante, unidadesTransferenciaEntrante);
                unidadesTransferenciaEntrante -= salidaTransferencia;
            }

            if (unidadesTrading < 1e-8) {
                unidadesTrading = 0;
                costeAcumuladoTrading = 0;
            }

            if (unidadesTransferenciaEntrante < 1e-8) {
                unidadesTransferenciaEntrante = 0;
            }
        }

    }

    public static Integer obtenerPortfolioActualId() {
        List<PortfolioItem> portfolios = PortfolioService.getPortfolios();

        if (portfolios.isEmpty() && vGlobales.getUsuarioIdActual() != null) {
            PortfolioService.loadForCurrentUser();
            portfolios = PortfolioService.getPortfolios();
        }

        for (PortfolioItem portfolio : portfolios) {
            if (portfolio.isEsDefault()) {
                return portfolio.getId();
            }
        }

        if (!portfolios.isEmpty()) {
            return portfolios.get(0).getId();
        }

        return null;
    }
}
