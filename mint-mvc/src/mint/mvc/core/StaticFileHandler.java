package mint.mvc.core;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Handle static file request.
 * 
 * @author Michael Liao (askxuefeng@gmail.com)
 */
class StaticFileHandler {

    private static final String MIME_OCTET_STREAM = "application/octet-stream";
    private static final int MAX_BUFFER_SIZE = 4096;

    private final ServletContext servletContext;

    private long expires = 0;
    private String maxAge = "";

    /**
     * @param config
     * @throws ServletException
     */
    public StaticFileHandler(ServletConfig config) throws ServletException {
        this.servletContext = config.getServletContext();
        String expiresValue = config.getInitParameter("expires");
        if (expiresValue!=null) {
            int n = Integer.parseInt(expiresValue);
            if (n>0) {
                this.expires = n * 1000L;
                this.maxAge = "max-age=" + n;
            } else if (n<0) {
                this.expires = (-1);
            }
        }
    }

    /**
     * 响应静态文件请求
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    public void handle(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String url = request.getRequestURI();
        String path = request.getContextPath();
        url = url.substring(path.length());
        if (url.toUpperCase().startsWith("/WEB-INF/")) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        
        /*
         * @TODO
         * 自定义目录的访问控制
         */
        
        int n = url.indexOf('?');
        if (n!=(-1)){
            url = url.substring(0, n);
        }
        
        n = url.indexOf('#');
        if (n!=(-1)){
            url = url.substring(0, n);
        }
        
        File f = new File(servletContext.getRealPath(url));
        if (! f.isFile()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        
        long ifModifiedSince = request.getDateHeader("If-Modified-Since");
        long lastModified = f.lastModified();
        if (ifModifiedSince!=(-1) && ifModifiedSince>=lastModified) {
            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            return;
        }
        response.setDateHeader("Last-Modified", lastModified);
        response.setContentLength((int)f.length());
        
        // set cache:
        if (expires<0) {
            response.setHeader("Cache-Control", "no-cache");
        } else if (expires>0) {
            response.setHeader("Cache-Control", maxAge);
            response.setDateHeader("Expires", System.currentTimeMillis() + expires);
        }
        
        String mime = servletContext.getMimeType(f.getName());
        response.setContentType((mime==null) ? MIME_OCTET_STREAM : mime);
        sendFile(f, response.getOutputStream());
    }

    /**
     * @param file
     * @param output
     * @throws IOException
     */
    void sendFile(File file, OutputStream output) throws IOException {
        InputStream input = null;
        try {
            input = new BufferedInputStream(new FileInputStream(file));
            byte[] buffer = new byte[MAX_BUFFER_SIZE];
            while(true) {
                int n = input.read(buffer);
                if (n==(-1))
                    break;
                output.write(buffer, 0, n);
            }
            output.flush();
        } finally {
            if (input!=null) {
                try {
                    input.close();
                } catch (IOException e) {}
            }
        }
    }
}
