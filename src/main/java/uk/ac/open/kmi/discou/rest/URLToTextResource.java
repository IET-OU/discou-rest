package uk.ac.open.kmi.discou.rest;

import java.io.IOException;
import java.net.URL;

import javax.servlet.ServletException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import au.id.jericho.lib.html.Source;

@Path("totext")
public class URLToTextResource extends AbstractResource {
	@GET
	public Response doGet() throws ServletException, IOException {
		String uri = request.getParameter("uri");

		if (uri == null) {
			return Response.status(Status.BAD_REQUEST).entity("Missing mandatory parameter: uri").build();
		}
		Source source = new Source(new URL(uri));
		String renderedText = source.getRenderer().toString();
		try {
			return Response.ok().header("Content-type", "text/plain").entity(renderedText).build();
		} catch (Exception e) {
			return Response.status(Status.NOT_FOUND).build();
		} finally {

		}
	}
}
