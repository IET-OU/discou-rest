package uk.ac.open.kmi.discou.rest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("describe")
public class DescribeResource extends AbstractResource {
	private static final Logger log = LoggerFactory.getLogger(DescribeResource.class);

	@GET
	public Response perform() {
		String uri = request.getParameter("uri");
		String endpoint = request.getParameter("endpoint");

		// if not empty
		if (uri == null || uri.trim().equals("")) {
			throw new RuntimeException("Wrong argument : uri");
		}

		if (endpoint == null) {
			endpoint = "http://data.open.ac.uk/query";
		}

		String query = "select distinct ?property ?value where {<" + uri + "> ?property ?value ." +
				"FILTER(?property = <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ||" +
				"?property = <http://www.w3.org/2000/01/rdf-schema#comment> ||" +
				"?property = <http://xmlns.com/foaf/0.1/page> ||" +
				"?property = <http://purl.org/dc/terms/description> ||" +
				"?property = <http://data.open.ac.uk/ontology/relatesToCourse> ||" +
				"?property = <http://purl.org/dc/terms/title> ||" +
				"?property = <http://www.w3.org/2000/01/rdf-schema#label> ||" +
				"?property = <http://www.w3.org/TR/2010/WD-mediaont-10-20100608/locator> )" +
				"}";

		try {
			query = URLEncoder.encode(query, "utf-8");
			URL url = new URL(endpoint + "?query=" + query);
			final HttpURLConnection cnx = (HttpURLConnection) url.openConnection();
			cnx.setAllowUserInteraction(false);
			cnx.setDoOutput(true);
			cnx.setDoInput(true);
			cnx.setInstanceFollowRedirects(true);
			cnx.setUseCaches(true);
			cnx.setRequestProperty("Accept",
					"application/sparql-results+xml");
			log.debug("X-Endpoint-Was: {}", endpoint);
			log.debug("X-Response-Code-Was: {}", String.valueOf(cnx.getResponseCode()));
			StreamingOutput stream = new StreamingOutput() {

				@Override
				public void write(OutputStream out) throws IOException, WebApplicationException {
					InputStream in = cnx.getInputStream();
					byte[] buffer = new byte[1024]; // Adjust if you want
					int bytesRead;
					while ((bytesRead = in.read(buffer)) != -1) {
						out.write(buffer, 0, bytesRead);
					}
					in.close();
					out.close();
				}
			};
			// return XML content-type
			return Response.ok().header("Content-type", "application/xml").entity(stream).build();

		} catch (IOException e) {
			log.error("", e);
			return Response.serverError().build();
		}
	}
}
