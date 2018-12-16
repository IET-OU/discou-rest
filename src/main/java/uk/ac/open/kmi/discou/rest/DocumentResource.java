package uk.ac.open.kmi.discou.rest;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.jena.atlas.json.JsonObject;

import uk.ac.open.kmi.discou.DiscouReader;
import uk.ac.open.kmi.discou.DiscouResource;
import uk.ac.open.kmi.discou.ResourceDoesNotExistException;

@Path("document")
public class DocumentResource extends AbstractResource {

	@GET
	public Response doGet() throws ServletException, IOException {
		String uri = request.getParameter("uri");

		if (uri == null) {
			return Response.status(Status.BAD_REQUEST).entity("Missing mandatory parameter: uri").build();
		}
		DiscouReader reader = getReader();
		reader.open();
		try {
			DiscouResource ires = reader.getFromURI(uri);
			JsonObject entity = new JsonObject();
			entity.put("uri", ires.getUri());
			entity.put("title", ires.getTitle());
			entity.put("description", ires.getDescription());
			entity.put("content", ires.getContent());
			JsonObject o = new JsonObject();
			o.put("entity", entity);
			return Response.ok().header("Content-type", "application/json").entity(o.toString()).build();
		} catch (ResourceDoesNotExistException e) {
			return Response.status(Status.NOT_FOUND).build();
		} finally{
			reader.close();
		}
	}
}
