package org.eclipse.jetty.demo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;

public class Issue1864
{
    public static void main(String[] args) throws Exception
    {
        Server server = new Server(8090);
        HandlerList handlers = new HandlerList();
        server.setHandler(handlers);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        context.setBaseResource(findBaseResource());
        handlers.addHandler(context);
        context.addServlet(new ServletHolder(new HelloWorld()), "/upload");
        context.addServlet(new ServletHolder(new DefaultServlet()), "/");
        handlers.addHandler(new DefaultHandler()); // to show error messages better
        server.start();
        System.out.println("Server is started: " + server.getURI());
        server.join();
    }

    private static Resource findBaseResource() throws IOException, URISyntaxException
    {
        // Look for jar file location first
        ProtectionDomain pd = Issue1864.class.getProtectionDomain();
        if (pd != null)
        {
            CodeSource cd = pd.getCodeSource();
            if (cd != null)
            {
                URL location = cd.getLocation();
                String locationURL = location.toExternalForm();
                if (locationURL.endsWith(".jar"))
                {
                    return Resource.newResource("jar:" + locationURL + "!/webapps/");
                }
            }
        }

        // not run from jar file, perhaps this is IDE, look in source tree instead.
        File path = new File("src/main/resources/webapps");
        if (!path.exists())
        {
            throw new FileNotFoundException("Unable to find webapps resource or directory");
        }
        return Resource.newResource(path.getAbsoluteFile());
    }

    public static class HelloWorld extends HttpServlet
    {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp)
                throws ServletException, IOException
        {
            doPost(req, resp);
        }

        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp)
                throws ServletException, IOException
        {
            // Uncomment this line to make bad browser implementations happy.
            // IO.copy(req.getInputStream(), IO.getNullStream());
            resp.sendError(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE, "File: " + req.getContentLength() / 1024 / 1024 + " MB. Refused the file.");
        }
    }
}
