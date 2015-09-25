package uk.ac.open.kmi.discou.server;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.WebAppContext;

import uk.ac.open.kmi.discou.rest.Application;

public class StandaloneServer {
	private static class Cli {

		private String[] args = null;
		private Options options = new Options();

		public Cli(String[] args) {
			this.args = args;
			options.addOption("h", "help", false, "Show this help.");
			options.addOption("i", "index", true, "Set the index location.");
			options.addOption("n", "interface", true, "The folder where the html/js alfa interface is located.");
			options.addOption("a", "annotator", true, "Set the url of the spotlight annotator. Defaults to http://spotlight.dbpedia.org/rest/annotate.");
			options.addOption("p", "port", true,
					"Set the port the server will listen to (defaults to 8080).");
		}

		/**
		 * Prints help.
		 */
		private void help() {
			String syntax = "java [java-opts] -jar [jarfile] ";
			new HelpFormatter().printHelp(syntax, options);
			System.exit(0);
		}

		/**
		 * Parses command line arguments and acts upon them.
		 */
		public void parse() {
			CommandLineParser parser = new BasicParser();
			CommandLine cmd = null;
			try {
				cmd = parser.parse(options, args);
				if (cmd.hasOption('h'))
					help();
				if (!cmd.hasOption('i')) {
					// Throw error
					E.println("Parameter 'i' is mandatory. Use -h for details.");
					System.exit(1);
				} else {
					String i = cmd.getOptionValue('i');
					File f = new File(i);
					// Check if i is an existing directory
					if (!f.exists()) {
						E.println("Directory " + i + " does not exists.");
						System.exit(2);
					} else if (!f.isDirectory()) {
						E.println("Directory " + i + " does not exists.");
						System.exit(2);
					} else if (!f.canRead()) {
						E.println("Cannot read " + i);
						System.exit(3);
					} else if (!f.canWrite()) {
						E.println("Cannot write " + i);
						System.exit(4);
					}
					index = i;
				}
				if (cmd.hasOption('a')) {
					spotlight = cmd.getOptionValue('a');
				}
				if (cmd.hasOption('n')) {
					String n = cmd.getOptionValue('n');
					File f = new File(n);
					// Check if i is an existing directory
					if (!f.exists()) {
						E.println("Directory " + n + " does not exists.");
						System.exit(2);
					} else if (!f.isDirectory()) {
						E.println("Directory " + n + " does not exists.");
						System.exit(2);
					} else if (!f.canRead()) {
						E.println("Cannot read " + n);
						System.exit(3);
					}
					interfac = n;
				}
				if (cmd.hasOption('p')) {
					port = Integer.parseInt(cmd.getOptionValue('p'));
					if (port < 0 && port > 65535) {
						O.println("Invalid port number " + port
								+ ". Must be in the range [0,65535].");
						System.exit(100);
					}
				}
			} catch (ParseException e) {
				E.println("Failed to parse comand line properties");
				e.printStackTrace();
				help();
			}
		}

	}

	private static PrintStream O = System.out;
	private static PrintStream E = System.err;
	private static int port = 8080;
	private static String index = null;
	private static String spotlight = null;
	private static String interfac = null;

	public static void main(String[] args) {
		System.out.println("#1: welcome to discou");
		new Cli(args).parse();
		Server server = new Server();
		ServerConnector connector = new ServerConnector(server);

		connector.setIdleTimeout(1000 * 60 * 60);
		connector.setSoLingerTime(-1);
		connector.setPort(port);
		server.setConnectors(new Connector[] { connector });
		System.out.println("#2: discou is starting on port " + port);
		ContextHandlerCollection contexts = new ContextHandlerCollection();

		// Discou Rest services
		WebAppContext root = new WebAppContext();
		root.setContextPath("/discou-services");
		String webxmlLocation = StandaloneServer.class
				.getResource("/WEB-INF/web.xml").toString();
		root.setDescriptor(webxmlLocation);
		root.setResourceBase("/discou-services");
		root.setInitParameter(Application.INDEX_HOME, index);
		root.setInitParameter(Application.SPOTLIGHT, spotlight);
		root.setInitParameter(Application.ALLOWED_PROXY_TARGET, "");
		root.setParentLoaderPriority(true);
		contexts.addHandler(root);

		if (interfac != null) {
			try {
				System.out.println("Placing /ui " + interfac);
				ContextHandler context = new ContextHandler();
				context.setContextPath("/ui");
				ResourceHandler staticResourceHandler = new ResourceHandler();
		        staticResourceHandler.setBaseResource(Resource.newResource(interfac));
		        staticResourceHandler.setDirectoriesListed(true);
				context.setHandler(staticResourceHandler);
				contexts.addHandler(context);
			} catch (IOException e) {
				System.err.println("Cannot load " + interfac);
				e.printStackTrace();
			}
		}
		server.setHandler(contexts);
		System.out.println("#3: Index at " + root.getInitParameter(Application.INDEX_HOME));

		try {
			server.start();
			System.out.println("#4: enjoy");
			server.join();
			System.out.println("#5: stopping server");
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(100);
		}
		System.out.println("#6: thank you");
	}
}
