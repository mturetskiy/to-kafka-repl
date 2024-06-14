package mt.exp;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import net.ttddyy.dsproxy.ExecutionInfo;
import net.ttddyy.dsproxy.QueryInfo;
import net.ttddyy.dsproxy.proxy.JdbcProxyFactory;
import net.ttddyy.dsproxy.proxy.ParameterSetOperation;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;
import net.ttddyy.dsproxy.transform.ParameterReplacer;
import net.ttddyy.dsproxy.transform.QueryTransformer;
import net.ttddyy.dsproxy.transform.TransformInfo;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Slf4j
public class DbInterceptor {
    public static void main(String[] args) {
        DbInterceptor dbInterceptor = new DbInterceptor();
        dbInterceptor.run();
    }

    void run() {
        log.info("Started...");

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl("jdbc:postgresql://localhost:5432/postgres");
        hikariConfig.setUsername("postgres");
        hikariConfig.setPassword("pwd123");

        HikariDataSource hikariDataSource = new InterceptableDataSource(hikariConfig);


        DataSource dataSource = ProxyDataSourceBuilder
                .create(hikariDataSource)
                .queryTransformer(new QueryTransformer() {
                    @Override
                    public String transformQuery(TransformInfo transformInfo) {
                        return transformInfo.getQuery();
                    }
                })
                .beforeQuery(new ProxyDataSourceBuilder.SingleQueryExecution() {
                    @Override
                    public void execute(ExecutionInfo execInfo, List<QueryInfo> queryInfoList) {
                        queryInfoList.forEach(queryInfo -> log.info("Executing: {}, params: {}", queryInfo.getQuery(), queryInfo.getParametersList()));

                        Iterator<QueryInfo> iterator = queryInfoList.iterator();
                        while (iterator.hasNext()) {
                            QueryInfo next = iterator.next();
                            String query = next.getQuery();
                            if (query.contains("insert")) {
                                List<List<ParameterSetOperation>> parametersList = next.getParametersList();
                                List<ParameterSetOperation> parameterSetOperations = parametersList.get(0);
                                ParameterSetOperation parameterSetOperation = parameterSetOperations.get(0);
                                Object arg = parameterSetOperation.getArgs()[1];
                                if ((int) arg % 2 == 0) {
                                    log.info("Detected even arg.");
//                                    iterator.remove();
                                }
                            }
                        }

                    }
                })
                .parameterTransformer((replacer, transformInfo) -> {
                    if (transformInfo.getQuery().contains("insert")) {
                       log.info("insert detected. params: {}", replacer.getModifiedParameters());
                    }
                })
                .build();


        NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        List<ImmutablePair<Integer, String>> data = jdbcTemplate.query("select * from tschm.data", (rs, rowNum) -> new ImmutablePair<>(rs.getInt("id"), rs.getString("name")));
        log.info("Loaded from db: ");
        data.forEach(p -> log.info(p.toString()));

        log.info("Updating ...");

        int count = 2;
        int lastId = data.get(data.size() - 1).getLeft();
        for (int id = lastId + 1; id <= lastId + count; id++) {

            jdbcTemplate.update("insert into tschm.data(id, name) values (:id, :name)",
                    Map.of(
                            "id", id,
                            "name", "generated_" + id
                    ));
        }

        log.info("Read after modification:");
        data = jdbcTemplate.query("select * from tschm.data", (rs, rowNum) -> new ImmutablePair<>(rs.getInt("id"), rs.getString("name")));
        data.forEach(p -> log.info(p.toString()));


        log.info("Done.");
    }
}
