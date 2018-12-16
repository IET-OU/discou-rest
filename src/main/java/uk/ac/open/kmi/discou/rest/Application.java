package uk.ac.open.kmi.discou.rest;

import java.io.File;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.open.kmi.discou.DiscouIndexer;
import uk.ac.open.kmi.discou.DiscouReader;

public class Application extends ResourceConfig implements ServletContextListener {

	private final static Logger log = LoggerFactory.getLogger(Application.class);
	public static final String INDEX_HOME = "index-home";
	public static final String WRITER = "discou-writer";
	public static final String READER = "discou-reader";
	public static final String SPOTLIGHT = "spotlight";
	public static final String DBPEDIA_SPOTLIGHT = "http://spotlight.dbpedia.org/rest/annotate";
	public static final String ALLOWED_PROXY_TARGET = "allowed-proxies";

	public Application() {
		packages("uk.ac.open.kmi.discou.rest");
		System.out.println("application initialized");
	}

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		// TODO Auto-generated method stub
		try {
			ServletContext context = sce.getServletContext();
			String i = context.getInitParameter(Application.INDEX_HOME);
			String spotlight = DBPEDIA_SPOTLIGHT;
			if (context.getInitParameter(Application.SPOTLIGHT) != null) {
				spotlight = context.getInitParameter(Application.SPOTLIGHT);
			}
			log.info("Index location: {}", i);
			File index = new File(i);
			sce.getServletContext().setAttribute(INDEX_HOME, i);
			sce.getServletContext().setAttribute(WRITER, new DiscouIndexer(index));
			sce.getServletContext().setAttribute(SPOTLIGHT, spotlight);
			log.info("context initialized");
		} catch (Exception e) {
			log.error("FATAL: cannot initialize context", e);
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		log.info("context destroyed");
	}

}
