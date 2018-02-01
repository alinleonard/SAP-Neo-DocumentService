package documentservice;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisNameConstraintViolationException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;

import com.sap.ecm.api.RepositoryOptions;
import com.sap.ecm.api.RepositoryOptions.Visibility;
import com.sap.ecm.api.EcmService;
import javax.naming.InitialContext;


/**
 * Servlet implementation class DocumentServiceServlet
 */
@WebServlet("/")
public class DocumentServiceServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public DocumentServiceServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.getWriter().append("Served at: ").append(request.getContextPath());
		
		displayCommandsToHTML(response);
		listenForParameters(request, response);
	}
	
	private void displayCommandsToHTML(HttpServletResponse res) throws IOException {
		StringBuilder content = new StringBuilder();
		
		content.append("<h3>Commands</h3>");
		content.append("<div><b>display?=true</b></div>");
		content.append("<q>Display the structure of the current repository.</q>");
		content.append("<div><b>&dContent=fileName</b></div>");
		content.append("<q>Display the content of the current file.</q>");
		content.append("<div><b>&nFile=fileName&content=content</b></div>");
		content.append("<q>Creates new file with the filename + content provided.</q>");
		content.append("<div><b>&nFolder=folderName</b></div>");
		content.append("<q>Creates new folder with the provided name.</q>");
		content.append("<div><b>&filder=file/content</b></div>");
		content.append("<q>Filter in the repository the files or content with the search criteria</q>");
		content.append("<div style='margin-top:10px'></div>");
		
		res.getWriter().println(content.toString());
	}
	
	private void listenForParameters(HttpServletRequest req,HttpServletResponse res) throws IOException {
		String queryPath = req.getQueryString();
		
		res.getWriter().printf("(GET) Query string: %s", queryPath);
		
		if(req.getParameter("display") != null) {
			// do something
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
