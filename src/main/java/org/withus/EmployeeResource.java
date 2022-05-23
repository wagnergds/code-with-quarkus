package org.withus;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.net.URI;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;

@Path("employee")
public class EmployeeResource {
    
    @Inject
    PgPool client;

    @PostConstruct
    void config (){
        System.out.println("Config");
        initdb();
    }

    @GET
    public Multi<Employee> getAll() {
        return Employee.findAll(client);
    }

    @GET
    @Path("{codeEmployee}")
    public Uni<Response> get(@PathParam("codeEmployee") Long id) {
        return Employee.findById(client, id)
                .onItem()
                .transform(employee -> employee != null ? Response.ok(employee) : Response.status(Response.Status.NOT_FOUND))
                .onItem()
                .transform(Response.ResponseBuilder::build);
    }

    @POST
    public Uni<Response> create(Employee employee) {
        return Employee.save(client, employee.getNameEmployee())
                .onItem()
                .transform(id -> URI.create("/employee/" + id))
                .onItem()
                .transform(uri -> Response.created(uri).build());
    }

    @DELETE
    @Path("{codeEmployee}")
    public Uni<Response> delete(@PathParam("codeEmployee") Long codeEmployee) {
        return Employee.delete(client, codeEmployee)
                .onItem()
                .transform(deleted -> deleted ? Response.Status.NO_CONTENT : Response.Status.NOT_FOUND)
                .onItem()
                .transform(status -> Response.status(status).build());
    }
    
    private void initdb() {
        client.query("DROP TABLE IF EXISTS Employee").execute()
                .flatMap(m-> client.query("CREATE TABLE Employee (codeEmployee SERIAL PRIMARY KEY, " +
                        "nameEmployee TEXT NOT NULL)").execute())
                .flatMap(m -> client.query("INSERT INTO Employee (nameEmployee) VALUES('Joao')").execute())
                .flatMap(m -> client.query("INSERT INTO Employee (nameEmployee) VALUES('Jose')").execute())
                .await()
                .indefinitely();
    }
}