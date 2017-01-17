package diagrams.pViz.dialogs;

import java.net.URL;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;

public class LegendDialog extends Dialog {
	public LegendDialog()
	{
		super();
		setTitle("Legend");
		setHeaderText("Add a legend to the diagram");
	    DialogPane dialogPane = getDialogPane();
		// Set the icon (must be included in the project).
		URL res =  getClass().getResource("legend.png");		//TODO
		if (res != null)
			setGraphic(new ImageView(res.toString()));

		// Set the button types.
		ButtonType addButtonType = new ButtonType("Add Legend", ButtonData.OK_DONE);
		dialogPane.getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20, 150, 10, 10));
		
		TextField titleFld = new TextField();
		titleFld.setPromptText("Legend");
		titleFld.setText("Legend");
		TextField commentFld = new TextField();
		commentFld.setPromptText("Comment");
		commentFld.setMinWidth(260);

		grid.add(new Label("Title:"), 0, 0);		grid.add(titleFld, 1, 0);
		grid.add(new Label("Comment:"), 0, 1);		grid.add(commentFld, 1, 1);

//		Node addButton = dialogPane.lookupButton(addButtonType);
//		addButton.setDisable(true);

		CheckBox nodes = new CheckBox("Include Nodes"); 	nodes.setSelected(true);
		CheckBox edges = new CheckBox("Include Edges");	 	edges.setSelected(true);
		CheckBox refs = new CheckBox("Include References");  refs.setSelected(true);
		// Do some validation (using the Java 8 lambda syntax).

		grid.add(nodes, 0, 3);	
		grid.add(edges, 0, 4);	
		grid.add(refs, 0, 5);	
//	
//		Button fonts = new Button("Set Fonts");
//		Button colors = new Button("Set Colors");
//		Button save = new Button("Save");
//		Button export = new Button("Export");
//		
//		grid.add(fonts, 0, 6);	
//		grid.add(colors, 1, 6);	
//		grid.add(save, 2, 6);	
//		grid.add(export, 3, 6);	
	
//		
//		username.textProperty().addListener((observable, oldValue, newValue) -> {
//			loginButton.setDisable(newValue.trim().isEmpty());
//		});

		dialogPane.setContent(grid);

		// Request focus on the findFld field by default.
		Platform.runLater(() -> titleFld.requestFocus());

		// Convert the result to a username-password-pair when the login button
		// is clicked.
		String TAB = "\t";
		setResultConverter(dialogButton -> {
			if (dialogButton == addButtonType)
				return titleFld.getText() + TAB + commentFld.getText() 
					+ TAB + (refs.isSelected() ? "TRUE" : "FALSE")
					+ TAB + (nodes.isSelected() ? "TRUE" : "FALSE")
					+ TAB + (edges.isSelected() ? "TRUE" : "FALSE");

//			ResultSet out = new ResultSet<>();
			return null;
		});

	}

	
}
