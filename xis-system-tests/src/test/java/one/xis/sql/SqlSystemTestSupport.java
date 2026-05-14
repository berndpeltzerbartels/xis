package one.xis.sql;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

abstract class SqlSystemTestSupport {
    private PersonRepository repository;

    abstract DataSource dataSource();

    abstract void createSchema() throws SQLException;

    @BeforeEach
    void setUp() throws SQLException {
        createSchema();
        repository = SQLRepositoryProxyFactory.<PersonRepository>standalone(dataSource()).createProxy(PersonRepository.class);
    }

    @Test
    void findsByPrimaryKey() {
        var person = repository.findById(1L).orElseThrow();

        assertThat(person.firstName).isEqualTo("Ada");
    }

    @Test
    void findsAllRows() {
        List<Person> people = repository.findAll();

        assertThat(people).extracting(person -> person.firstName).containsExactly("Ada", "Grace");
    }

    @Test
    void countsRows() {
        assertThat(repository.count()).isEqualTo(2);
    }

    @Test
    void selectsWithNamedParameters() {
        assertThat(repository.nameById(1L)).isEqualTo("Ada");
    }

    @Test
    void insertsWithNamedParameters() {
        int changed = repository.insert(3L, "Katherine", "Inserted note");

        assertThat(changed).isEqualTo(1);
        assertThat(repository.nameById(3L)).isEqualTo("Katherine");
        assertThat(repository.findById(3L).orElseThrow().notes).isEqualTo("Inserted note");
    }

    @Test
    void updatesWithNamedParameters() {
        boolean changed = repository.rename(1L, "Augusta");

        assertThat(changed).isTrue();
        assertThat(repository.nameById(1L)).isEqualTo("Augusta");
    }

    @Test
    void savesEntity() {
        Person person = new Person();
        person.id = 3L;
        person.firstName = "Katherine";
        person.notes = "new notes";

        Person returned = repository.save(person);

        assertThat(returned).isSameAs(person);
        assertThat(repository.findById(3L).orElseThrow().firstName).isEqualTo("Katherine");
    }

    @Test
    void deletesEntity() {
        Person person = repository.findById(1L).orElseThrow();

        assertThat(repository.delete(person)).isTrue();

        assertThat(repository.findById(1L)).isEmpty();
    }

    @Test
    void deletesById() {
        assertThat(repository.deleteById(1L)).isTrue();

        assertThat(repository.findById(1L)).isEmpty();
    }

    @Test
    void deletesWithAnnotatedSql() {
        assertThat(repository.deleteNamed(1L)).isEqualTo(1);

        assertThat(repository.findById(1L)).isEmpty();
    }

    @Test
    void callsFunction() {
        assertThat(repository.doubleValue(7)).isEqualTo(14);
    }

    @Test
    void callsStoredProcedureWithOutParameter() {
        assertThat(repository.addFive(7)).isEqualTo(12);
    }

    @Test
    void mapsClobToStringContent() {
        assertThat(repository.findById(1L).orElseThrow().notes).isEqualTo("First note from clob");
    }

    @Repository
    interface PersonRepository extends CrudRepository<Person, Long> {
        @Select("select first_name from people where id = {id}")
        String nameById(@Param("id") long id);

        @Insert("insert into people (id, first_name, notes) values ({id}, {firstName}, {notes})")
        int insert(@Param("id") long id, @Param("firstName") String firstName, @Param("notes") String notes);

        @Update("update people set first_name = {firstName} where id = {id}")
        boolean rename(@Param("id") long id, @Param("firstName") String firstName);

        @Delete("delete from people where id = {id}")
        int deleteNamed(@Param("id") long id);

        @Function("double_value")
        int doubleValue(int value);

        @StoredProcedure(value = "add_five", out = "result")
        int addFive(@Param("value") int value);
    }

    @Entity("people")
    static class Person {
        long id;
        String firstName;
        String notes;
    }
}
