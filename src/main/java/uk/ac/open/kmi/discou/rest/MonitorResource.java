package uk.ac.open.kmi.discou.rest;

import java.io.IOException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.apache.jena.atlas.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.open.kmi.discou.DiscouReader;

@Path("monitor")
public class MonitorResource extends AbstractResource {
	private final static Logger log = LoggerFactory.getLogger(MonitorResource.class);

	@Path("stats")
	@GET
	public Response stats() {
		int count;
		try {
			DiscouReader reader = getReader();
			reader.open();
			count = reader.count();
			reader.close();
		} catch (IOException e) {
			log.error("", e);
			return Response.serverError().build();
		}
		JsonObject o = new JsonObject();
		o.put("count", count);
		return Response.ok().header("Content-type", "application/json").entity(o.toString()).build();
	}
}
