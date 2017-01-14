package diagrams.pViz.gpml;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.TableColumn;
import model.bio.BiopaxRecord;
import model.bio.TableRecord;

public class ReferenceListRecord extends TableRecord<BiopaxRecord> {

	private SimpleStringProperty db = new SimpleStringProperty();
	public StringProperty  dbProperty()  { return db;}
	public String getDb()  { return db.get();}
	public void setDb(String s)  { db.set(s);}	
	
	public ReferenceListRecord(String inName)
	{		
		super(inName);
		setName(inName);	
	}
	List<BiopaxRecord> referenceList = new ArrayList<BiopaxRecord>();
	public void addReferences(List<BiopaxRecord> list) {		referenceList.addAll(list);	}
	public List<BiopaxRecord> getReferences() 			{		return referenceList;	}

}
