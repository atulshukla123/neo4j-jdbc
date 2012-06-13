package org.neo4j.jdbc;

import org.neo4j.jdbc.ext.DbVisualizerConnection;
import org.neo4j.jdbc.ext.IntelliJConnection;
import org.neo4j.jdbc.ext.LibreOfficeConnection;
import org.restlet.Client;

import java.sql.SQLException;
import java.util.Properties;

/**
 * @author mh
 * @since 12.06.12
 */
public enum Connections {
    OpenOffice() {
        protected boolean matches(Properties sysProps) {
            return sysProps.containsKey("org.openoffice.native");
        }

        protected Neo4jConnection doCreate(Driver driver, String url, Client client, Properties p) throws SQLException {
            return new LibreOfficeConnection(driver, url, client, p);
        }
    }, IntelliJ() {
        @Override
        protected boolean matches(Properties sysProps) {
            return sysProps.getProperty("user.dir").contains("IntelliJ");
        }

        @Override
        protected Neo4jConnection doCreate(Driver driver, String url, Client client, Properties p) throws SQLException {
            return new IntelliJConnection(driver, url, client, p);
        }
    }, DbVisualizer() {
        @Override
        protected boolean matches(Properties sysProps) {
            return sysProps.containsKey(DB_VIS);
        }

        @Override
        protected Neo4jConnection doCreate(Driver driver, String url, Client client, Properties p) throws SQLException {
            return new DbVisualizerConnection(driver, url, client, p);
        }
    }, Default() {
        @Override
        protected boolean matches(Properties sysProps) {
            return true;
        }

        @Override
        protected Neo4jConnection doCreate(Driver driver, String url, Client client, Properties p) throws SQLException {
            return new Neo4jConnection(driver, url, client, p);
        }
    };

    public static final String DB_VIS = "dbvis.ScriptsTreeShowDetails";
    protected abstract boolean matches(Properties sysProps);

    public static Neo4jConnection create(Driver driver, String url, Client client, Properties p) throws SQLException {
        final Properties sysProps = System.getProperties();
        for (Connections connections : values()) {
            if (connections.matches(sysProps)) {
                final Neo4jConnection con = connections.doCreate(driver, url, client, p);
                return debug(con,hasDebug(p));
            }
        }
        throw new SQLException("Couldn't create connection for "+url+" properties "+p);
    }

    public static boolean hasDebug(Properties properties) {
        return "true".equalsIgnoreCase(properties.getProperty("debug", "false"));
    }

    @SuppressWarnings("unchecked")
    public static <T> T debug(T obj, boolean debug) {
        if (debug) {
            Class type = obj.getClass().getInterfaces()[0];
            return (T) CallProxy.proxy(type, obj);
        } else
            return obj;
    }

    protected abstract Neo4jConnection doCreate(Driver driver, String url, Client client, Properties p) throws SQLException;
}
