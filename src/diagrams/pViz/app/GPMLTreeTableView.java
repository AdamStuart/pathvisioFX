package diagrams.pViz.app;

import java.util.List;
import java.util.Map;

import diagrams.pViz.gpml.Anchor;
import diagrams.pViz.model.DataNode;
import diagrams.pViz.model.DataNodeGroup;
import diagrams.pViz.model.DataNodeState;
import diagrams.pViz.model.Interaction;
import diagrams.pViz.model.Model;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.ComboBoxTreeTableCell;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import model.bio.XRefable;
import model.bio.XRefableSection;

public class GPMLTreeTableView  {
	
	TreeTableView<XRefable> treeView;
	Controller controller;
	Model model;
	boolean verbose = false;
	public GPMLTreeTableView(TreeTableView<XRefable> theView, Controller c)
	{
		treeView = theView;
		controller = c;
		model = c.getModel();
	}
	TreeItem<XRefable> root = new TreeItem<XRefable> ();
	TreeItem<XRefable> nodes = new TreeItem<XRefable> ();
	TreeItem<XRefable> shapes = new TreeItem<XRefable> ();
	TreeItem<XRefable> labels = new TreeItem<XRefable> ();
	TreeItem<XRefable> groups = new TreeItem<XRefable> ();
	Label size, select;
	
	public void setup(Label inSize, Label inSelect)
	{
		size = inSize;
		select = inSelect;
		if (treeView != null)
		{
			root = new TreeItem<XRefable> ();
			treeView.setRoot(root);
			nodes.setValue(new XRefableSection("Nodes"));
			shapes.setValue(new XRefableSection("Shapes"));
			labels.setValue(new XRefableSection("Labels"));
			groups.setValue(new XRefableSection("Groups"));
//			root.getChildren().addAll(nodes, shapes, labels, groups);
			root.setExpanded(true);
			model.getNodes().stream().forEach((node) -> {
				nodes.getChildren().add(new TreeItem<XRefable>(node));
		        });
		       
			treeView.setStyle(Controller.CSS_Gray2);		
	        TreeTableColumn<XRefable, String> nodeCol = new TreeTableColumn<>("Node");
	        TreeTableColumn<XRefable, String> xrefdbCol = new TreeTableColumn<>("Database");
	        TreeTableColumn<XRefable, String> xrefidCol = new TreeTableColumn<>("ID");
	        TreeTableColumn<XRefable, String> graphidCol = new TreeTableColumn<>("Graph Id");
		
	        nodeCol.setPrefWidth(150);
	        xrefdbCol.setPrefWidth(80);
	        xrefidCol.setPrefWidth(125);
	        graphidCol.setPrefWidth(65);
	     
	        nodeCol.setCellValueFactory(
	                (TreeTableColumn.CellDataFeatures<XRefable, String> param) -> 
	        		{
	        			XRefable g = param.getValue().getValue();
	        			String name = g == null ? "All Nodes" : g.getName();
	        	        return  new ReadOnlyStringWrapper(name);
	      		} );
	        graphidCol.setCellValueFactory(
	                (TreeTableColumn.CellDataFeatures<XRefable, String> param) -> 
	                {
	                	XRefable g = param.getValue().getValue();
	        			String s = g == null ? "" : g.getGraphId();
	        	        return  new ReadOnlyStringWrapper(s);
	                 }
	                );
	        xrefdbCol.setCellValueFactory(
	                (TreeTableColumn.CellDataFeatures<XRefable, String> param) -> 
	                {
	                	XRefable g = param.getValue().getValue();
	        			String s = g == null ? "" : g.getDatabase();
	        	        return  new ReadOnlyStringWrapper(s);
	                 }
	                );

	        ObservableList<String> dbList = FXCollections.observableArrayList();
	        dbList.addAll("Ensembl","Entrez","HGNC","HMDB","HCA","WikiPathways","GoProcess","GoFunction","CHEBI","Funk&Wagnells");
	        
	        xrefdbCol.setCellFactory(ComboBoxTreeTableCell.forTreeTableColumn(dbList));
	        xrefdbCol.setOnEditCommit(new EventHandler<TreeTableColumn.CellEditEvent<XRefable,String>>() {
				@Override public void handle(TreeTableColumn.CellEditEvent<XRefable, String> event) {
					TreeItem<XRefable> cur = treeView.getTreeItem(event.getTreeTablePosition().getRow());
					cur.getValue().setDatabase(event.getNewValue());
					cur.getValue().put("Database",event.getNewValue());
					}  
	        	}  );

	        xrefidCol.setCellValueFactory(
	                (TreeTableColumn.CellDataFeatures<XRefable, String> param) -> 
	                {
	                	XRefable g = param.getValue().getValue();
	          			String s = g == null ? "" : g.getDbid();
	          	        return  new ReadOnlyStringWrapper(s);
	                   }
	               );
	        xrefidCol.setCellFactory(TextFieldTreeTableCell.forTreeTableColumn());
	        xrefidCol.setOnEditCommit(new EventHandler<TreeTableColumn.CellEditEvent<XRefable,String>>() {
				@Override public void handle(TreeTableColumn.CellEditEvent<XRefable, String> event) {
					TreeItem<XRefable> cur = treeView.getTreeItem(event.getTreeTablePosition().getRow());
					cur.getValue().setDbid(event.getNewValue());
					cur.getValue().put("ID",event.getNewValue());
				}  
	        	}  );
	        treeView.setEditable(true);
			//-------
//			nodeTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
	        treeView.getColumns().clear();		// throw away the definition from the fxml !
	        treeView.getColumns().setAll(nodeCol, xrefdbCol, xrefidCol, graphidCol);
//	        treeView.setRowFactory((a) -> {
//			       return new DraggableTreeTableRow<XRefable>(treeView, Controller.GENE_MIME_TYPE, this, controller.getGeneSetRecord());
//				});
	//	
	        treeView.setShowRoot(false);
	        treeView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
	        treeView.getSelectionModel().setCellSelectionEnabled(true);
			// bind two labels:  select and size to the number selected and number of rows, e.g.: (32 / 2092)
//			select.setAlignment(Pos.CENTER_RIGHT);
//			select.setText("0");
//			size.setText("0");
	        treeView.getSelectionModel().getSelectedIndices().addListener(new ListChangeListener<Integer>()
			{
				@Override public void onChanged(Change<? extends Integer> change) 
				{  
					String txt = "" + treeView.getSelectionModel().getSelectedIndices().size();
					if (select != null) select.setText(txt); 
					String siz = "/ " + treeView.getExpandedItemCount();  
					if (size != null) size.setText(siz); 	
					for (int idx : treeView.getSelectionModel().getSelectedIndices())
					{
						TreeItem<XRefable> cur = treeView.getTreeItem(idx);
						if (cur != null)
						{
							String id = cur.getValue().getGraphId();
							DataNode datanode = model.findDataNode(id);
							if (datanode != null)
							{
								System.out.println(datanode.getName());
//								pasteboard.getSelectionMgr().select(datanode.getStack(),true);

							}
						}
					}
				}
			}
		);
			
//			nodeTable.getRoot().getChildren().addListener(new ListChangeListener<Gene>()
//			{
//				@Override public void onChanged(Change<? extends Gene> change) { size.setText("/ " + nodeTable.getItems().size()); }
//			});
	        treeView.setOnKeyPressed( new EventHandler<KeyEvent>()
			{
				@Override
				public void handle(final KeyEvent keyEvent) {
//					if (keyEvent.getCode().equals(KeyCode.DELETE) || keyEvent.getCode().equals(KeyCode.BACK_SPACE)) {
//						List<Integer> ids = treeView.getSelectionModel().getSelectedIndices();
//						int sz = ids.size();
//						for (int i = sz - 1; i >= 0; i--)
//							treeView.getRoot().getChildren().remove(i);
//						treeView.getSelectionModel().clearSelection();
//					}
				}
			});
//			search.setTooltip(new Tooltip(tooltip));
//			searchBox.setTooltip(new Tooltip(tooltip));
		}
	}
	
	// **-------------------------------------------------------------------------------
	public void updateTreeTable() 
	{	
		System.out.println("GPMLTreeTableView.updateTreeTable");
		root.getChildren().clear();
		if (model.getDataNodeMap().size() > 0) { nodes.getChildren().clear();   root.getChildren().add(nodes);  }
		if (model.getShapes().size() > 0)  
		{ 
			shapes.getChildren().clear(); root.getChildren().add(shapes); }
		if (model.getLabels().size() > 0) 
		{ 
			labels.getChildren().clear(); root.getChildren().add(labels);}
		if (model.getGroups().size() > 0)  
		{ 
			groups.getChildren().clear(); root.getChildren().add(groups);}
//		root.getChildren().addAll(nodes, shapes, labels, groups);
		ObservableList<TreeItem<XRefable>> kids =  nodes.getChildren();
		kids.clear();

		model.getNodes().stream().forEach((node) -> {
//			if (node instanceof Anchor) return;
			addBranch(node);
        });
		
		model.getEdges().stream().forEach((interaction) -> {
			String origin = interaction.get("sourceid");
			interaction.setNameFromState();
//			System.out.println("interaction " + interaction.get("GraphId") + " starts at " + origin);

			TreeItem<XRefable> origItem = findDeep(nodes,origin);
			if (origItem == null)  // source of interaction is an anchor
			{
				DataNode node = model.findDataNode(origin);
				System.err.println("origItem  == null");
//				if (node instanceof Anchor)
//					System.err.println("Anchor");
//				else  
					System.err.println("Not Anchor");
//				dumpDeep(nodes, "");
			}
			else addBranch(interaction, origItem);
        });
		
		model.getStates().stream().forEach((state) -> {
			String origin = state.get("GraphRef");
			state.setName(state.get("TextLabel"));
			TreeItem<XRefable> origItem = findDeep(nodes,origin);
			if (origItem == null)  reportError(state);
			else	addBranch(state, origItem);
        });
		
		model.getEdges().stream().forEach((edge) -> {
			
			List<Anchor> anchors = edge.getAnchors();
			if (anchors == null) return;
			for (Anchor a : anchors)
			{
				String interactionId = a.getInteractionId();
				TreeItem<XRefable> interactionRow = findDeep(nodes,interactionId);
				if (interactionRow == null)
					System.out.println("Error");
				else
					addBranch(a, interactionRow);
			}
        });
		model.getGroups().stream().forEach((group) -> {
			addBranch(group);
        });
		
		System.out.println("======================\n\n\n");
		dumpDeep(nodes, "", 0);
//		model.resetEdgeTable();
	}

	private void reportError(DataNodeState state) {
		// TODO Auto-generated method stub
		
	}

	public void addBranch(XRefable node) {
		String type = node.get("Type");
		System.out.println(type);
		if  ("Label".equals(type))  addBranch(node, labels);
		else if ("Shape".equals(type))  addBranch(node, shapes);
		else if (node instanceof DataNodeGroup)  addBranch(node, groups);
		else if (node instanceof DataNode)  addBranch(node, nodes);
	}


	private void addBranch(XRefable node, TreeItem<XRefable> parent) {
		TreeItem<XRefable> anItem = new TreeItem<XRefable>();
		anItem.setValue(node);
		parent.getChildren().add(anItem);
	}

	public void addBranch(XRefable node, String parentID) {
		TreeItem<XRefable> mom = findDeep(nodes, parentID);
		if (mom == null)  System.err.println("Null parent: " + parentID);
		else
		{
			TreeItem<XRefable> anItem = new TreeItem<XRefable>();
			anItem.setValue(node);
			mom.getChildren().add(anItem);
		}
	}
	// **-------------------------------------------------------------------------------

	public TreeItem<XRefable> findNodeTreeItem(String inId) {		return findNode(inId);	}
	
	public TreeItem<XRefable> findNode(String inId) {
		
		if (inId == null) return null;
		for (TreeItem<XRefable> branch : nodes.getChildren())
		{
			String id = branch.getValue().getGraphId();
			if (inId.equals(id))
				return branch;
		}
		return null;
	}
	private TreeItem<XRefable> findDeep(TreeItem<XRefable> inRoot, String inId) {
		
		if (inId == null) return null;
		if (inId.isEmpty()) return null;
		XRefable val = inRoot.getValue();
		if (inRoot.getValue() == null) return null;
//		System.out.println("searching " + val.getName() + " "+ val.getGraphId() + "  v. " + inId);
		if (inId.equals(val.getGraphId()))
		{
//			System.out.println("HIT");
			return inRoot;
		}
		for (TreeItem<XRefable> branch : inRoot.getChildren())
		{
			XRefable xref =  branch.getValue();
			String id = xref.getGraphId();
			if (id == null)  
				id = xref.get("GraphId");
//			System.out.println("comparing " + inId + " to " + id);
			if (inId.equals(id))
			{
//				System.out.println("HIT");
				return branch;
			}
			for (TreeItem<XRefable> subbranch : branch.getChildren())
			{	
				TreeItem<XRefable> hit = findDeep(subbranch, inId);
				if (hit != null) 
					return hit;
			}
		}
//		System.out.println("no " + inId + " in " + val.getName());
		return null;
	}

	private TreeItem<XRefable> findNodeShallow(DataNode origin) {
		for (TreeItem<XRefable> branch : nodes.getChildren())
			if (branch.getValue().getGraphId().equals(origin.getGraphId()))
				return branch;
		return null;
	}	
	
	//---------------------------------------
	private void dumpDeep(TreeItem<XRefable> inRoot, String prefix, int depth) {
		
		XRefable ref = inRoot.getValue();
		if (ref == null) System.out.println("section head ");
		else	dump(prefix, ref, depth);  
		for (TreeItem<XRefable> branch : inRoot.getChildren())
		{
			XRefable xref =  branch.getValue();
			dump(prefix, xref, depth);
			for (TreeItem<XRefable> subbranch : branch.getChildren())
				dumpDeep(subbranch,">  " + prefix, depth+1);
		}
	}
	private void dump(String prefix, XRefable xref, int depth) 
	{
		String id = xref.getGraphId();
		if (id == null)  id = xref.get("GraphId");
		System.out.println(prefix + xref.getName() + " " + xref.get("Type") + " " + id + " " + depth);
		
	}
}
