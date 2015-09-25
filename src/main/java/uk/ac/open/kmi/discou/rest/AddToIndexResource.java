package uk.ac.open.kmi.discou.rest;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import uk.ac.open.kmi.discou.DiscouIndexer;

@Path("index")
@Produces("application/json")
public class AddToIndexResource extends AbstractResource {

	@POST
	@Consumes("application/x-www-form-urlencoded")
	public Response doPost(@FormParam("uri") String uri, @FormParam("entities") String entities)
			throws ServletException, IOException {
		return doGet(uri, entities);
	}

	@GET
	public Response doGet(@QueryParam("uri") String uri, @QueryParam("entities") String entities) throws ServletException, IOException {
		DiscouIndexer writer = getWriter();
		writer.open();
		writer.putRaw(uri, "", "", entities);
		writer.close();
		return Response.ok().header("Content-type", "application/json").entity("{\"status\": \"done\"}").build();
	}
}
