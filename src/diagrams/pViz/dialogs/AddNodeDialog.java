package diagrams.pViz.dialogs;

import java.io.IOException;

import diagrams.pViz.model.Model;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;

public class AddNodeDialog extends Dialog {

	AddNodeController controller;
	public AddNodeController getController() { return controller; }
	
	public AddNodeDialog(Model model)
	{
		super();
		setTitle("AddNodeDialog");
		setHeaderText("Add new nodes to the canvas with identifiers");
	    DialogPane dialogPane = getDialogPane();
		// Set the icon (must be included in the project).
	    FXMLLoader loader = new FXMLLoader(getClass().getResource("AddNode.fxml"));
	    Parent root;
		try {
			root = (Parent)loader.load();
		    controller = (AddNodeController)loader.getController();
		    controller.setup(this, model); 
			dialogPane.setContent(root);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
}
