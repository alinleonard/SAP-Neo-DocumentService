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
	private static final String UNIQUE_NAME = "";
	private static final String UNIQUE_KEY = "";
	private static final Boolean CREATE_REPOSITORY_IF_NOT_EXIST = false;

	private static Session cmisSession = null;

	public static void createFolder(HttpServletResponse response, String folderName) throws IOException {

		Session session = getCmisSession(response);

		if (session == null) {
			response.getWriter().println("ECM not found, the session is null");
			return;
		}
		// access the root folder of the repository
		Folder root = session.getRootFolder();

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
		ContentStream contentStream = session.getObjectFactory().createContentStream(documentName,
				helloContent.length, "text/plain; charset=UTF-8", stream);
		try {
			root.createDocument(properties, contentStream, VersioningState.NONE);
		} catch (CmisNameConstraintViolationException e) {
			// Document exists already, nothing to do
			response.getWriter().printf(
					"<p>Document %s exists or something else happened!</p><p style='color:red'>%s</p>", documentName,
					e.getMessage());
		}
	}

	private static Session getCmisSession(HttpServletResponse response) throws IOException {
		if (response == null)
			return cmisSession = null;
		if (cmisSession == null) {
			try {
				InitialContext context = new InitialContext();
				String lookupName = "java:comp/env/" + "EcmService";
				EcmService ecmSvc = (EcmService) context.lookup(lookupName);
				try {
					cmisSession = ecmSvc.connect(UNIQUE_NAME, UNIQUE_KEY);
				} catch (CmisObjectNotFoundException e) {
					// repository does not exist
					if (CREATE_REPOSITORY_IF_NOT_EXIST) {
						createRepository(ecmSvc);
						cmisSession = ecmSvc.connect(UNIQUE_NAME, UNIQUE_KEY);
					} else {
						response.getWriter().println(e.getMessage());
					}
				}
			} catch (NamingException e) {
				// TODO: handle exception
				response.getWriter().println(e.getMessage());
			}
		}

		return cmisSession;
	}

	private static void createRepository(EcmService ecmSvc) {
		RepositoryOptions options = new RepositoryOptions();
		options.setUniqueName(UNIQUE_NAME);
		options.setRepositoryKey(UNIQUE_KEY);
		options.setVisibility(Visibility.PROTECTED);
		ecmSvc.createRepository(options);
	}
}
