package diagrams.pViz.view;

import java.io.File;

import diagrams.pViz.app.Tool;
import diagrams.pViz.model.DataNode;
import javafx.beans.binding.Bindings;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.scene.web.WebView;
import model.AttributeMap;
import model.dao.CSVTableData;
import util.FileUtil;
import util.MacUtil;

public class ControlFactory {
	
	VNode parent;
	DataNode dataNode;
	
	ControlFactory(VNode stack)
	{
		parent = stack;
		dataNode = parent.modelNode();
	}
	// **-------------------------------------------------------------------------------
	public void addNewNode(Tool type, DataNode model)
	{
		switch (type)
		{
			case Browser:	makeBrowser();		break;
			case Text:		makeTextArea();		break;
			case Table:		makeTableView();	break;
			case Image:		makeImageView();	break;	
			case SVGPath:	makeSVGPath();		break;
			default:							break;
		}
	}
	// **-------------------------------------------------------------------------------
	private void makeBrowser()
	{
		AttributeMap attrMap = dataNode;
		attrMap.put("ShapeType","Browser");
		String url = attrMap.get("url");
		if (url == null) 
		{
			String filepath = attrMap.get("file");		// f.getAbsolutePath()
			url = MacUtil.urlFromPlist(filepath);
		}
		if (url == null) return ;
		WebView webView = new WebView();
		webView.setZoom(0.4);
		webView.getEngine().load(url);
		parent.getChildren().add(webView);
		addTitleBar(url);
	}
	
	// **-------------------------------------------------------------------------------
	private void makeImageView()
	{
		dataNode.put("ShapeType","Image");
		String filepath = dataNode.get("file");		// f.getAbsolutePath()
		if (filepath == null) return;
		Image img = new Image("file:" + filepath);
		if (img.isError())
			System.out.println("makeImageView error");
		ImageView imgView = new ImageView(img);
		if (dataNode.get("GraphId") == null) 
			dataNode.put("GraphId", parent.gensym("I"));
		
		imgView.prefWidth(200); 	imgView.prefHeight(200);
		imgView.setFitWidth(200); 	imgView.setFitHeight(200);
		dataNode.put("name", filepath);
	    imgView.setMouseTransparent(true);
	    imgView.fitWidthProperty().bind(Bindings.subtract(parent.widthProperty(), 20));
	    imgView.fitHeightProperty().bind(Bindings.subtract(parent.heightProperty(), 40));
	    imgView.setTranslateY(-10);
	    parent.readGeometry(dataNode, imgView);
		parent.getChildren().add(new VBox(addTitleBar(filepath), imgView));
	}
	
	// **-------------------------------------------------------------------------------
	private void makeSVGPath() {
		AttributeMap attrMap = dataNode;
		attrMap.put("ShapeType","SVGPath");

		String path = attrMap.get("file");
		if (path != null)
		{
			String s = FileUtil.readFileIntoString(path);
			if (s != null)
			{
				SVGPath svg = new SVGPath();
				int idx1 = s.indexOf("<g>");
				int idx2 = s.indexOf("</g>") + 4;
				if (idx1 >0 && idx2 > idx1)
					s = s.substring(idx1, idx2);
				svg.setContent(s);
				parent.readGeometry(attrMap, svg);
				parent.getChildren().add(new VBox(addTitleBar(path), svg));
			}
		}
	}
	// **-------------------------------------------------------------------------------
	private void makeTableView()
	{
		AttributeMap attrMap = dataNode;
		attrMap.put("ShapeType","Table");
		TableView<ObservableList<StringProperty>> table = new TableView<ObservableList<StringProperty>>();
		if (attrMap.get("GraphId") == null)
			attrMap.put("GraphId", parent.gensym("T"));
		String filename = attrMap.get("file");
		CSVTableData data = CSVTableData.readCSVfile(filename);	
		attrMap.put("name", filename);
		if (data == null) return;
		parent.readGeometry(attrMap, table);
		parent.getChildren().addAll(new VBox(addTitleBar(filename), table));
	}
	// **-------------------------------------------------------------------------------
	private void makeTextArea()
	{
		AttributeMap attrMap = dataNode;
		attrMap.put("ShapeType","Text");
		String text = attrMap.get("text");
		if (text == null)
		{
			String name = attrMap.get("file");
			if (name == null)
				text = "Boilerplate";
				else
				{
					StringBuilder buffer = new StringBuilder();
					attrMap.put("name", name);
					FileUtil.readFile(new File(name), buffer);
					text = buffer.toString();
				}
			attrMap.put("text", text);
		}
		TextArea textArea = new TextArea(text);
	    textArea.setPrefColumnCount(60);
	    textArea.setPrefRowCount(20);
	    parent.readGeometry(attrMap, textArea);
	    parent.getChildren().add(textArea);
	}

	// **-------------------------------------------------------------------------------
	private HBox addTitleBar(String title)
	{		
		HBox titleBar = new HBox();
	    titleBar.setMaxHeight(25);
	    Label idLabel = new Label(parent.getGraphId());
	    idLabel.setMinWidth(50);
	    StackPane.setAlignment(titleBar, Pos.TOP_CENTER);
	    Label titleLabel = new Label(title);
		titleBar.getChildren().addAll(idLabel, titleLabel);
	    titleBar.setMouseTransparent(true);
	    return titleBar;
	}

}
