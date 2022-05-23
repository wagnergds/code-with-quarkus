package org.withus;

import java.util.Date;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.Tuple;

@Data
@NoArgsConstructor
public class Employee {
    Long codeEmployee;
    String nameEmployee;
    Date birthDate;
    Date admissionDate;
    Long codeDepartment;


    
    public Employee(Long codeEmployee, String nameEmployee){
        this.nameEmployee = nameEmployee;
        this.codeEmployee = codeEmployee;
    }

    public static Multi<Employee> findAll(PgPool client) {
        return client
                .query("SELECT codeEmployee, nameEmployee FROM Employee ORDER BY nameEmployee DESC")
                .execute()
                .onItem()
                .transformToMulti(set -> Multi.createFrom().iterable(set))
                .onItem()
                .transform(Employee::from);
      }
    
      public static Uni<Employee> findById(PgPool client, Long id) {
        return client
                .preparedQuery("SELECT codeEmployee, nameEmployee FROM Employee WHERE codeEmployee = $1")
                .execute(Tuple.of(id))
                .onItem()
                .transform(m -> m.iterator().hasNext() ? from(m.iterator().next()) : null);
      }
    
      public static Uni<Long> save(PgPool client, String nameEmployee) {
        return client
                .preparedQuery("INSERT INTO Employee (nameEmployee) VALUES ($1) RETURNING codeEmployee")
                .execute(Tuple.of(nameEmployee))
                .onItem()
                .transform(m -> m.iterator().next().getLong("codeEmployee"));
      }
    
      public static Uni<Boolean> delete(PgPool client, Long codeEmployee) {
        return client
                .preparedQuery("DELETE FROM Employee WHERE codeEmployee = $1")
                .execute(Tuple.of(codeEmployee))
                .onItem()
                .transform(m -> m.rowCount() == 1);
      }
    
      private static Employee from(Row row) {
        return new Employee(row.getLong("codeEmployee"), row.getString("nameEmployee"));
      }
}
