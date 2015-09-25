package uk.ac.open.kmi.discou.rest;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.open.kmi.discou.DiscouReader;

import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;

@Path("search")
public class SearchResource extends AbstractResource {
	private static final Logger log = LoggerFactory.getLogger(SearchResource.class);

	@GET
	@Produces("application/json")
	public Response get() {
		String uri = request.getParameter("uri");
		if (uri == null) {
			return Response.status(Status.BAD_REQUEST).entity("Missing mandatory parameter: uri").build();
		}
		String nb = request.getParameter("nb");
		if (nb == null) {
			nb = "10";
		}
		int nbi = new Integer(nb);
		Map<String, Float> r;
		try {
			DiscouReader reader = getReader();
			reader.open();
			r = reader.similar(uri, nbi);
			reader.close();
		} catch (IOException e) {
			log.error("", e);
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
		JsonArray a = new JsonArray();
		for (Entry<String, Float> e : r.entrySet()) {
			a.add(new JsonPrimitive(e.getKey()));
		}
		return Response.ok().header("Content-type", "application/json").entity(a.toString()).build();
	}
}
