package uk.ac.open.kmi.discou.rest;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriInfo;

import uk.ac.open.kmi.discou.DiscouIndexer;
import uk.ac.open.kmi.discou.DiscouReader;
import uk.ac.open.kmi.discou.spotlight.SpotlightClient;

class AbstractResource {

	@Context
	protected HttpHeaders requestHeaders;

	@Context
	protected UriInfo requestUri;

	@Context
	protected ServletContext context;

	@Context
	protected HttpServletRequest request;

	protected synchronized DiscouIndexer getWriter() {
		return (DiscouIndexer) context.getAttribute(Application.WRITER);
	}

	protected DiscouReader getReader() throws IOException {
		return (DiscouReader) new DiscouReader(new File((String) context.getAttribute(Application.INDEX_HOME)));
	}

	protected SpotlightClient getSpotlight() {
		return new SpotlightClient((String) context.getAttribute(Application.SPOTLIGHT));
	}
}
