package uk.ac.open.kmi.discou.rest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.jena.atlas.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.open.kmi.discou.DiscouReader;
import uk.ac.open.kmi.discou.DiscouResource;

@Path("summary")
public class ComparisonSummaryResource extends AbstractResource {

	private static final Logger log = LoggerFactory.getLogger(ComparisonSummaryResource.class);

	@GET
	public Response get(@QueryParam("uri1") String uri1, @QueryParam("uri2") String uri2 ) {
		return perform(uri1, uri2);
	}

	@POST
	public Response perform(@FormParam("uri1") String uri1, @FormParam("uri2") String uri2 ) {
		if(uri1 == null || uri2 == null){
			return Response.status(Status.BAD_REQUEST).entity("Mandatory parameters: uri1, uri2").build();
		}
		String best = "";
		try {
			DiscouReader reader = getReader();
			reader.open();
			DiscouResource r1 = reader.getFromURI(uri1);
			DiscouResource r2 = reader.getFromURI(uri2);
			reader.close();
			List<String> entities1 = new ArrayList<String>();

			entities1.addAll(Arrays.asList(r1.getTitle().split(" ")));
			entities1.addAll(Arrays.asList(r1.getDescription().split(" ")));
			entities1.addAll(Arrays.asList(r1.getContent().split(" ")));

			// Only keep entities from 2 that exists in 1
			List<String> entities2 = new ArrayList<String>();
			entities2.addAll(Arrays.asList(r2.getTitle().split(" ")));
			entities2.addAll(Arrays.asList(r2.getDescription().split(" ")));
			entities2.addAll(Arrays.asList(r2.getContent().split(" ")));
			entities2.retainAll(new HashSet<String>(entities1));
			Collections.sort(entities2);

			// Tell the entity that is more frequent in 2 and is shared with 1
			String last = "";
			int bestN = 0;
			int lastN = 0;
			for (String s : entities2) {
				if (last.equals("")) {
					// first iteration
					last = s;
					best = s;
					lastN++;
					bestN = lastN;
				} else if (!s.equals(last)) {
					// check if last is better then best
					if (lastN > bestN) {
						bestN = lastN;
						best = last;
					}
					last = s;
					lastN = 1;
				} else {
					// s is equal to last
					lastN++;
				}
			}
			JsonObject o = new JsonObject();
			o.put("common", best);
			return Response.ok().header("Content-type", "application/json").entity(o.toString()).build();
		} catch (IOException e) {
			log.error("Cannot write to output: {}", e.getMessage());
			return Response.serverError().build();
		}
	}
}
