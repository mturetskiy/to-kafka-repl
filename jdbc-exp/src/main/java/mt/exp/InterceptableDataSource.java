package mt.exp;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jooq.impl.DSL;

import java.sql.Connection;
import java.sql.SQLException;

public class InterceptableDataSource extends HikariDataSource {
    public InterceptableDataSource(HikariConfig configuration) {
        super(configuration);
    }

    @Override
    public Connection getConnection() throws SQLException {
        Connection connection = super.getConnection();
//        Connection parsingConnection = DSL.using(connection).parsingConnection();
        return new InterceptableConnection(connection);
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return new InterceptableConnection(super.getConnection(username, password));
    }
}
