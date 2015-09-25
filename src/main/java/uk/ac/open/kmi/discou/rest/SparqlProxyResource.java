package uk.ac.open.kmi.discou.rest;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.StreamingOutput;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("sparql-proxy")
public class SparqlProxyResource extends AbstractResource {
	private static final Logger log = LoggerFactory.getLogger(SparqlProxyResource.class);

	@GET
	@Produces("*/*")
	public Response perform() {
		String endpoint = request.getParameter("endpoint");
		if (endpoint == null || endpoint.trim().equals("")) {
			endpoint = "http://data.open.ac.uk/query";
		}

		// security check (you can't browse the web using our site...)
		String allowedPrefixes = context.getInitParameter(Application.ALLOWED_PROXY_TARGET);
		if (allowedPrefixes == null || allowedPrefixes.trim().equals("")) {
			throw new SecurityException("Please configure allowed proxy targets");
		}

		String[] urlPrefixes = allowedPrefixes.trim().split("\n|\r");
		boolean isAllowed = false;
		for (String prefix : urlPrefixes) {
			prefix = prefix.trim();
			if (endpoint.startsWith(prefix)) {
				isAllowed = true;
				break;
			}
		}

		try {
			if (!isAllowed) {
				return Response.serverError().build();
			}
			int statusCode;

			String methodName;

			String urlString = endpoint;
			String queryString = request.getQueryString();

			urlString += queryString == null ? "" : "?" + queryString;
			URL url = new URL(urlString);

			final HttpURLConnection con = (HttpURLConnection) url.openConnection();

			methodName = request.getMethod();
			con.setRequestMethod(methodName);
			con.setDoOutput(true);
			con.setDoInput(true);
			// con.setFollowRedirects(false);
			con.setUseCaches(true);

			for (Enumeration<?> e = request.getHeaderNames(); e.hasMoreElements();) {
				String headerName = e.nextElement().toString();
				con.setRequestProperty(headerName, request.getHeader(headerName));
			}
			con.connect();

			if (methodName.equals("POST")) {
				BufferedInputStream clientToProxyBuf = new BufferedInputStream(request.getInputStream());
				BufferedOutputStream proxyToWebBuf = new BufferedOutputStream(con.getOutputStream());
				int oneByte;
				while ((oneByte = clientToProxyBuf.read()) != -1)
					proxyToWebBuf.write(oneByte);

				proxyToWebBuf.flush();
				proxyToWebBuf.close();
				clientToProxyBuf.close();
			}
			statusCode = con.getResponseCode();
			// Proxy response
			ResponseBuilder response = Response.status(statusCode);

			// Proxy headers
			for (Iterator<?> i = con.getHeaderFields().entrySet().iterator(); i.hasNext();) {
				@SuppressWarnings("unchecked")
				Map.Entry<?, List<?>> mapEntry = (Map.Entry<?, List<?>>) i.next();
				if (mapEntry.getKey() != null)
					response.header(mapEntry.getKey().toString(), ((List<?>) mapEntry.getValue()).get(0).toString());
			}

			/**
			 * Stream to output
			 */
			StreamingOutput stream = new StreamingOutput() {
				@Override
				public void write(OutputStream os) throws IOException, WebApplicationException {
					BufferedInputStream webToProxyBuf = new BufferedInputStream(con.getInputStream());
					BufferedOutputStream proxyToClientBuf = new BufferedOutputStream(os);
					int oneByte;
					while ((oneByte = webToProxyBuf.read()) != -1)
						proxyToClientBuf.write(oneByte);

					proxyToClientBuf.flush();
					proxyToClientBuf.close();

					webToProxyBuf.close();
					con.disconnect();

				}
			};

			return response.entity(stream).build();

		} catch (Exception e) {
			log.error("", e);
			throw new RuntimeException(e);
		} finally {
		}
	}
}
