package documentservice;

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
import javax.naming.NamingException;

public class DocumentServiceAdapter {
	private static final Boolean CREATE_REPOSITORY_IF_NOT_EXIST = false;

	private static Session cmisSession = null;

	public static void createFolder(HttpServletResponse response, String folderPath) throws IOException {

		Session session = getCmisSession(response);

		if (session == null) {
			response.getWriter().println("ECM not found, the session is null");
			return;
		}
		
		response.getWriter().printf("<h3>Create of %s folder path structure</h3>",  folderPath);
		
		// access the root folder of the repository
		Folder root = getFolderByName(response, folderPath);
		
		int beginIndex = folderPath.lastIndexOf("/");
		String folderName = folderPath.substring(beginIndex, folderPath.length());

		// create a new folder
		Map<String, String> newFolderProps = new HashMap<String, String>();
		newFolderProps.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");
		newFolderProps.put(PropertyIds.NAME, folderName);
		try {
			root.createFolder(newFolderProps);
			response.getWriter().printf("<p>Folder: %s created with succcess!</p>", folderName);
		} catch (CmisNameConstraintViolationException e) {
			// Folder exists already, nothing to do
			response.getWriter().printf(
					"<p>Folder: %s exists or something else happened!</p><p style='color:red'>%s</p>", folderName,
					e.getMessage());
		}
	}

	public static void createDocument(HttpServletResponse response, String documentName, String documentContent) throws IOException {

		Session session = getCmisSession(response);

		if (session == null) {
			response.getWriter().println("ECM not found, the session is null");
			return;
		}

		// access the root folder of the repository
		Folder root = session.getRootFolder();

		// create a new file in the root folder
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");
		properties.put(PropertyIds.NAME, "HelloWorld.txt");
		byte[] helloContent = documentContent.getBytes("UTF-8");
		InputStream stream = new ByteArrayInputStream(helloContent);
		ContentStream contentStream = session.getObjectFactory().createContentStream(documentName, helloContent.length,
				"text/plain; charset=UTF-8", stream);
		try {
			root.createDocument(properties, contentStream, VersioningState.NONE);
		} catch (CmisNameConstraintViolationException e) {
			// Document exists already, nothing to do
			response.getWriter().printf(
					"<p>Document %s exists or something else happened!</p><p style='color:red'>%s</p>", documentName,
					e.getMessage());
		}
		
	}
	
	public static void displayFolderStructureOfRoot(HttpServletResponse res) throws IOException {
		
		res.getWriter().println("<h3 style='color:blue'>displayFolderStructureOfRoot</h3>");

		Session session = getCmisSession(res);

		if (session == null) {
			res.getWriter().println("ECM not found, the session is null");
			return;
		}
		
		res.getWriter().println("<h3>Display of root folder structure</h3>");

		// access the root folder of the repository
		Folder folder = session.getRootFolder();
		
		if(folder == null) {
			return;
		}
		// Display the root folder's children objects
		ItemIterable<CmisObject> children = folder.getChildren();
		res.getWriter().println(
				"The root folder of the repository with id " + folder.getId() + " contains the following objects:<ul>");
		for (CmisObject o : children) {
			res.getWriter().print("<li>" + o.getName());
			if (o instanceof Folder) {
				res.getWriter().println(" createdBy: " + o.getCreatedBy() + "</li>");
			} else {
				Document doc = (Document) o;
				res.getWriter().println(" createdBy: " + o.getCreatedBy() + " filesize: "
						+ doc.getContentStreamLength() + " bytes" + "</li>");
			}
		}
		res.getWriter().println("</ul>");
		
	}
	
	public static void displayFolderStructure(HttpServletResponse res, String folderPath) throws IOException {

		Session session = getCmisSession(res);

		if (session == null) {
			res.getWriter().println("ECM not found, the session is null");
			return;
		}
		
		res.getWriter().printf("<h3>Display of %s folder path structure</h3>",  folderPath);

		// access the root folder of the repository
		Folder folder = getFolderByName(res, folderPath);
		
		if(folder == null) {
			return;
		}
		// Display the root folder's children objects
		ItemIterable<CmisObject> children = folder.getChildren();
		res.getWriter().println(
				"The root folder of the repository with id " + folder.getId() + " contains the following objects:<ul>");
		for (CmisObject o : children) {
			res.getWriter().print("<li>" + o.getName());
			if (o instanceof Folder) {
				res.getWriter().println(" createdBy: " + o.getCreatedBy() + "</li>");
			} else {
				Document doc = (Document) o;
				res.getWriter().println(" createdBy: " + o.getCreatedBy() + " filesize: "
						+ doc.getContentStreamLength() + " bytes" + "</li>");
			}
		}
		res.getWriter().println("</ul>");
		
	}
	
	/**
	 * 
	 * @param res
	 * The HttpServletResponse so we can interact with the frontend
	 * @param folderPath
	 * The folder path + the folder name eg: /folderName
	 * @return
	 * returns null of there was no folder found
	 * @throws IOException
	 */
	private static Folder getFolderByName(HttpServletResponse res, String folderPath) throws IOException {
		
		Session session = getCmisSession(res);
		
		Folder folder = null;

		if (session == null) {
			res.getWriter().println("ECM not found, the session is null");
			return folder;
		}
		
		try {
			session.getObjectByPath(folderPath);
			folder = (Folder) session.getObjectByPath(folderPath);
		} catch (CmisObjectNotFoundException e) {
			// TODO: handle exception
			res.getWriter().printf("Folder path %s was not found", folderPath);
		}
		
		return folder;
	}

	private static Session getCmisSession(HttpServletResponse response) throws IOException {
		if (response == null)
			return cmisSession = null;
		if (cmisSession == null) {
			
			response.getWriter().println("<h3 style='color:blue'>getCmisSessione</h3>");
			
			try {
				javax.naming.InitialContext context = new javax.naming.InitialContext();
				String lookupName = "java:comp/env/" + "EcmService";
				EcmService ecmSvc = (EcmService) context.lookup(lookupName);
				try {
					cmisSession = ecmSvc.connect(Config.UNIQUE_NAME, Config.UNIQUE_KEY);
				} catch (CmisObjectNotFoundException e) {
					response.getWriter().println("<div style='color:red'>Repository does not exist</div>");
					// repository does not exist
					if (CREATE_REPOSITORY_IF_NOT_EXIST) {
						createRepository(ecmSvc);
						cmisSession = ecmSvc.connect(Config.UNIQUE_NAME, Config.UNIQUE_KEY);
					} else {
						response.getWriter().println(e.getMessage());
					}
				}
			} catch (NamingException e) {
				response.getWriter().println("<div style='color:red'>There was an error in retrieving the CMIS Session</div>");
				// TODO: handle exception
				response.getWriter().println(e.getMessage());
			}
		}

		return cmisSession;
	}
	

	private static void createRepository(EcmService ecmSvc) {
		RepositoryOptions options = new RepositoryOptions();
		options.setUniqueName(Config.UNIQUE_NAME);
		options.setRepositoryKey(Config.UNIQUE_KEY);
		options.setVisibility(Visibility.PROTECTED);
		ecmSvc.createRepository(options);
	}
}
