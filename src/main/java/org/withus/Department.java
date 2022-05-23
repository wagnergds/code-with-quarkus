package org.withus;

import lombok.Data;
import lombok.NoArgsConstructor;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.Tuple;

@Data
@NoArgsConstructor
public class Department {
    Long codeDepartment;
    String nameDepartment;
    String addressDeparment;

    public Department(Long codeDepartment, String nameDeparment){
        this.nameDepartment = nameDeparment;
        this.codeDepartment = codeDepartment;
    }

    public static Multi<Department> findAll(PgPool client) {
        return client
                .query("SELECT codeDepartment, nameDepartment FROM Departments ORDER BY nameDepartment DESC")
                .execute()
                .onItem()
                .transformToMulti(set -> Multi.createFrom().iterable(set))
                .onItem()
                .transform(Department::from);
      }
    
      public static Uni<Department> findById(PgPool client, Long codeDepartment) {
        return client
                .preparedQuery("SELECT codeDepartment, nameDepartment FROM Departments WHERE codeDepartment = $1")
                .execute(Tuple.of(codeDepartment))
                .onItem()
                .transform(m -> m.iterator().hasNext() ? from(m.iterator().next()) : null);
      }
    
      public static Uni<Long> save(PgPool client, String nameDepartment) {
        return client
                .preparedQuery("INSERT INTO Departments (nameDepartment) VALUES ($1) RETURNING codeDepartment")
                .execute(Tuple.of(nameDepartment))
                .onItem()
                .transform(m -> m.iterator().next().getLong("codeDepartment"));
      }
    
      public static Uni<Boolean> delete(PgPool client, Long codeDepartment) {
        return client
                .preparedQuery("DELETE FROM Departments WHERE codeDepartment = $1")
                .execute(Tuple.of(codeDepartment))
                .onItem()
                .transform(m -> m.rowCount() == 1);
      }
    
      private static Department from(Row row) {
        return new Department(row.getLong("codeDepartment"), row.getString("nameDepartment"));
      }
   

}
