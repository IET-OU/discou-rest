package uk.ac.open.kmi.discou.rest;

import java.io.IOException;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.open.kmi.discou.spotlight.SpotlightAnnotation;
import uk.ac.open.kmi.discou.spotlight.SpotlightClient;
import uk.ac.open.kmi.discou.spotlight.SpotlightResponse;

@Path("entities")
@Produces("application/json")
public class EntitiesResource extends AbstractResource {

	private static final Logger log = LoggerFactory.getLogger(EntitiesResource.class);

	@GET
	public Response get(@QueryParam("text") String text) {
		return perform(text);
	}

	@POST
	@Consumes("application/x-www-form-urlencoded")
	public Response perform(@FormParam("text") String text) {
		if (text == null || text.equals("")) {
			return Response.status(Status.BAD_REQUEST).entity("Missing mandatory parameter: text").build();
		}
		SpotlightClient service = getSpotlight();
		double confidence = 0.2;
		int support = 20;
		SpotlightResponse re;
		try {
			re = service.perform(text, confidence, support);
		} catch (IOException e) {
			log.error("Spotlight Service error: {}", e.getMessage());
			return Response.status(Status.BAD_GATEWAY).build();
		}
		List<SpotlightAnnotation> annotations = SpotlightClient.toList(re.getXml());
		JsonObject o = new JsonObject();
		JsonArray data = new JsonArray();
		for (SpotlightAnnotation s : annotations) {
			// We skip non web resources ...
			if (s.getUri().startsWith("http")) {
				JsonObject r = new JsonObject();
				r.put("uri", s.getUri());
				r.put("score", Long.toString(Math.round(s.getSimilarity() * 100)));
				data.add(r);
			}
		}
		o.put("data", data);
		return Response.ok().header("Content-type", "application/json").entity(o.toString()).build();

	}
}
