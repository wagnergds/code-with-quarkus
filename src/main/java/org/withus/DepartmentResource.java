package org.withus;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.net.URI;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;

@Path("deparment")
public class DepartmentResource {

    @Inject
    PgPool client;

    @PostConstruct
    void config (){
        System.out.println("Config");
        initdb();
    }

    @GET
    public Multi<Department> getAll() {
        return Department.findAll(client);
    }

    @GET
    @Path("{codeDepartment}")
    public Uni<Response> get(@PathParam("codeDepartment") Long id) {
        return Department.findById(client, id)
                .onItem()
                .transform(department -> department != null ? Response.ok(department) : Response.status(Response.Status.NOT_FOUND))
                .onItem()
                .transform(Response.ResponseBuilder::build);
    }

    @POST
    public Uni<Response> create(Department department) {
        return Department.save(client, department.getNameDepartment())
                .onItem()
                .transform(id -> URI.create("/department/" + id))
                .onItem()
                .transform(uri -> Response.created(uri).build());
    }

    @DELETE
    @Path("{codeDepartment}")
    public Uni<Response> delete(@PathParam("codeDepartment") Long codeDepartment) {
        return Department.delete(client, codeDepartment)
                .onItem()
                .transform(deleted -> deleted ? Response.Status.NO_CONTENT : Response.Status.NOT_FOUND)
                .onItem()
                .transform(status -> Response.status(status).build());
    }
    
    private void initdb() {
        client.query("DROP TABLE IF EXISTS Department").execute()
                .flatMap(m-> client.query("CREATE TABLE Department (codeDepartment SERIAL PRIMARY KEY, " +
                        "nameDepartment TEXT NOT NULL)").execute())
                .flatMap(m -> client.query("INSERT INTO Department (nameDepartment) VALUES('Dept do Joao')").execute())
                .flatMap(m -> client.query("INSERT INTO Department (nameDepartment) VALUES('Dept do Jose')").execute())
                .await()
                .indefinitely();
    }
}
