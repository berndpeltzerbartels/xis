package one.xis.sql;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

abstract class SqlSystemTestSupport {
    private PersonRepository repository;

    abstract DataSource dataSource();

    abstract void createSchema() throws SQLException;

    boolean supportsStoredProcedureOutParameters() {
        return true;
    }

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
    void insertsGeneratedPrimaryKeyAndWritesItBack() {
        GeneratedPerson person = new GeneratedPerson();
        person.firstName = "Katherine";

        GeneratedPerson returned = repository.insertGenerated(person);

        assertThat(returned).isSameAs(person);
        assertThat(person.id).isPositive();
        assertThat(repository.findGeneratedById(person.id).firstName).isEqualTo("Katherine");
    }

    @Test
    void savesGeneratedPrimaryKeyAndWritesItBack() {
        GeneratedPerson person = new GeneratedPerson();
        person.firstName = "Katherine";

        GeneratedPerson returned = repository.saveGenerated(person);

        assertThat(returned).isSameAs(person);
        assertThat(person.id).isPositive();
        assertThat(repository.findGeneratedById(person.id).firstName).isEqualTo("Katherine");
    }

    @Test
    void insertsGeneratedPartOfCompositePrimaryKeyAndWritesItBack() {
        GeneratedMembership membership = new GeneratedMembership();
        membership.tenantId = "tenant-a";
        membership.label = "Admin";

        GeneratedMembership returned = repository.insertGeneratedMembership(membership);

        assertThat(returned).isSameAs(membership);
        assertThat(membership.id).isPositive();
        assertThat(repository.findGeneratedMembershipById(membership.tenantId, membership.id).label).isEqualTo("Admin");
    }

    @Test
    void savesGeneratedPartOfCompositePrimaryKeyAndWritesItBack() {
        GeneratedMembership membership = new GeneratedMembership();
        membership.tenantId = "tenant-a";
        membership.label = "Admin";

        GeneratedMembership returned = repository.saveGeneratedMembership(membership);

        assertThat(returned).isSameAs(membership);
        assertThat(membership.id).isPositive();
        assertThat(repository.findGeneratedMembershipById(membership.tenantId, membership.id).label).isEqualTo("Admin");
    }

    @Test
    void selectsRecordEntity() {
        PersonRecord person = repository.findRecordById(1L);

        assertThat(person.id()).isEqualTo(1L);
        assertThat(person.firstName()).isEqualTo("Ada");
        assertThat(person.notes()).isEqualTo("First note from clob");
    }

    @Test
    void insertsRecordEntityWhenPrimaryKeyIsProvided() {
        var person = new PersonRecord(3L, "Katherine", "Inserted record");

        PersonRecord returned = repository.insertRecord(person);

        assertThat(returned).isSameAs(person);
        assertThat(repository.findRecordById(3L)).isEqualTo(person);
    }

    @Test
    void savesRecordEntityWhenPrimaryKeyIsProvided() {
        var person = new PersonRecord(3L, "Katherine", "Saved record");

        PersonRecord returned = repository.saveRecord(person);

        assertThat(returned).isSameAs(person);
        assertThat(repository.findRecordById(3L)).isEqualTo(person);
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
        org.junit.jupiter.api.Assumptions.assumeTrue(supportsStoredProcedureOutParameters());

        assertThat(repository.addFive(7)).isEqualTo(12);
    }

    @Test
    void mapsClobToStringContent() {
        assertThat(repository.findById(1L).orElseThrow().notes).isEqualTo("First note from clob");
    }

    @Test
    void mapsDatabaseDateTypesToJavaDateTypes() {
        DateRow row = repository.findDateRow(1L);

        assertThat(row.localDate).isEqualTo(LocalDate.of(2026, 5, 18));
        assertThat(row.localTime).isEqualTo(LocalTime.of(10, 15, 30));
        assertThat(row.localDateTime).isEqualTo(LocalDateTime.of(2026, 5, 18, 10, 15, 30));
        assertThat(row.instantValue).isEqualTo(Instant.parse("2026-05-18T10:15:30Z"));
        assertThat(row.utilDate.toInstant()).isEqualTo(Instant.parse("2026-05-18T10:15:30Z"));
        assertThat(row.sqlDate.toLocalDate()).isEqualTo(LocalDate.of(2026, 5, 18));
        assertThat(row.sqlTime.toLocalTime()).isEqualTo(LocalTime.of(10, 15, 30));
        assertThat(row.sqlTimestamp.toInstant()).isEqualTo(Instant.parse("2026-05-18T10:15:30Z"));
    }

    void insertDateRows() throws SQLException {
        try (var connection = dataSource().getConnection();
             var insert = connection.prepareStatement("""
                     insert into date_rows (
                         id,
                         local_date,
                         local_time,
                         local_date_time,
                         instant_value,
                         util_date,
                         sql_date,
                         sql_time,
                         sql_timestamp
                     ) values (?, ?, ?, ?, ?, ?, ?, ?, ?)
                     """)) {
            Instant instant = Instant.parse("2026-05-18T10:15:30Z");
            insert.setLong(1, 1L);
            insert.setDate(2, java.sql.Date.valueOf(LocalDate.of(2026, 5, 18)));
            insert.setTime(3, java.sql.Time.valueOf(LocalTime.of(10, 15, 30)));
            insert.setTimestamp(4, Timestamp.valueOf(LocalDateTime.of(2026, 5, 18, 10, 15, 30)));
            insert.setTimestamp(5, Timestamp.from(instant));
            insert.setTimestamp(6, Timestamp.from(instant));
            insert.setDate(7, java.sql.Date.valueOf(LocalDate.of(2026, 5, 18)));
            insert.setTime(8, java.sql.Time.valueOf(LocalTime.of(10, 15, 30)));
            insert.setTimestamp(9, Timestamp.from(instant));
            insert.executeUpdate();
        }
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

        @Select("select * from date_rows where id = {id}")
        DateRow findDateRow(@Param("id") long id);

        @Insert
        GeneratedPerson insertGenerated(GeneratedPerson person);

        @Save
        GeneratedPerson saveGenerated(GeneratedPerson person);

        @Select("select * from generated_people where id = {id}")
        GeneratedPerson findGeneratedById(@Param("id") long id);

        @Insert
        GeneratedMembership insertGeneratedMembership(GeneratedMembership membership);

        @Save
        GeneratedMembership saveGeneratedMembership(GeneratedMembership membership);

        @Select("select * from generated_memberships where tenant_id = {tenantId} and id = {id}")
        GeneratedMembership findGeneratedMembershipById(@Param("tenantId") String tenantId, @Param("id") long id);

        @Select("select * from people where id = {id}")
        PersonRecord findRecordById(@Param("id") long id);

        @Insert
        PersonRecord insertRecord(PersonRecord person);

        @Save
        PersonRecord saveRecord(PersonRecord person);
    }

    @Entity("people")
    static class Person {
        long id;
        String firstName;
        String notes;
    }

    @Entity("people")
    record PersonRecord(long id, String firstName, String notes) {
    }

    @Entity("generated_people")
    static class GeneratedPerson {
        long id;
        String firstName;
    }

    @Entity("generated_memberships")
    static class GeneratedMembership {
        String tenantId;
        long id;
        String label;
    }

    static class DateRow {
        long id;
        LocalDate localDate;
        LocalTime localTime;
        LocalDateTime localDateTime;
        Instant instantValue;
        java.util.Date utilDate;
        java.sql.Date sqlDate;
        java.sql.Time sqlTime;
        Timestamp sqlTimestamp;
    }
}
