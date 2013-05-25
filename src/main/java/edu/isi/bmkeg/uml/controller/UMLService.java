package edu.isi.bmkeg.uml.controller;

import java.util.List;

import org.w3c.dom.Document;

//import edu.isi.bmkeg.discuss.model.Comment;
import edu.isi.bmkeg.uml.model.UMLmodel;

public interface UMLService {

	public List<UMLmodel> getModels() throws Exception;

	public Boolean addUpdateModel(UMLmodel model) throws Exception;

	public Boolean deleteModel(Long modelId) throws Exception;

	public UMLmodel loadSkeletalModel(UMLmodel queryModel) throws Exception;

	public Document loadXMLModel(UMLmodel queryModel) throws Exception;

	public Boolean addNewModelFromFile(String modelName, String modelUrl,
			byte[] data, String type) throws Exception;

	public Boolean addNewModelFromConnection(String name, String url,
			String login, String password, String type) throws Exception;

	/*public List<Comment> loadComments(Long umlItemId) throws Exception;

	public Boolean addNewComment(String id, String owner,
			String comment, String timestamp) throws Exception;

	public Boolean deleteComment(Comment c) throws Exception;*/
	
}
