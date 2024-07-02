package searchengine.integration.tools;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Table;
import org.springframework.data.repository.CrudRepository;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.HashMap;

public class DatabaseWorker {

    public static <T, S> void saveToDb(T entity, CrudRepository<T, S> repository, EntityManager manager) {
        repository.save(entity);
        manager.flush();
    }

    public static <T, S> void saveAndDetach(T entity, CrudRepository<T, S> repository, EntityManager manager) {
        repository.save(entity);
        manager.flush();
        manager.detach(entity);
    }

    public static <T> T get(Class<T> entity, NamedParameterJdbcTemplate jdbc) {
        final String tableName = entity.getAnnotation(Table.class).name();
        final String query = String.format("select * from %s order by id desc limit 1", tableName);
        return jdbc.queryForObject(query, new HashMap<>(),
                new BeanPropertyRowMapper<>(entity, false));
    }

    public static Integer count(String tableName, NamedParameterJdbcTemplate jdbc) {
        final String query = String.format("select count(*) from %s", tableName);
        return jdbc.getJdbcTemplate().queryForObject(query, Integer.class);
    }
}
