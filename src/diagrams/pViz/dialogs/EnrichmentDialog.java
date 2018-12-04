package diagrams.pViz.dialogs;

import java.io.IOException;

import diagrams.pViz.model.Model;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;

public class EnrichmentDialog extends Dialog {

	EnrichmentController controller;
	public EnrichmentController getController() { return controller; }
	
	public EnrichmentDialog(Model model)
	{
		super();
		setTitle("Enrichment");
		setHeaderText("Add new columns to the table based on ID mapping");
	    DialogPane dialogPane = getDialogPane();
		// Set the icon (must be included in the project).
	    FXMLLoader loader = new FXMLLoader(getClass().getResource("enrichment.fxml"));
	    Parent root;
		try {
			root = (Parent)loader.load();
		    controller = (EnrichmentController)loader.getController();
		    controller.setup(this, model); 
			dialogPane.setContent(root);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
}
