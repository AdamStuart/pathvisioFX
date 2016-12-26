package diagrams.pViz.app;

import java.util.List;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import model.bio.Gene;

public class GeneListRecord {

	StringProperty id = new SimpleStringProperty();
	StringProperty type = new SimpleStringProperty();
	StringProperty name = new SimpleStringProperty();
	BooleanProperty visible = new SimpleBooleanProperty();
	BooleanProperty editable = new SimpleBooleanProperty();
	DoubleProperty score = new SimpleDoubleProperty(0);
	DoubleProperty size = new SimpleDoubleProperty(0);
	StringProperty header1 = new SimpleStringProperty();
	StringProperty header2 = new SimpleStringProperty();
	StringProperty comments = new SimpleStringProperty();
	StringProperty history = new SimpleStringProperty();
	StringProperty species = new SimpleStringProperty();
	StringProperty tissue = new SimpleStringProperty();

	public StringProperty  idProperty()  { return id;}
	public String getId()  { return id.get();}
	public void setId(String s)  { id.set(s);}

	public StringProperty  typeProperty()  { return type;}
	public String getType()  { return  type.get();		}
	public void setType(String s)  { type.set(s);}

	public StringProperty  nameProperty()  { return name;}
	public String getName()  { return name.get();}
	public void setName(String s)  { name.set(s);}

	public BooleanProperty  visibleProperty()  { return visible;}
	public Boolean getVisible()  { return visible.get();}
	public void setVisible(Boolean s)  { visible.set(s);}
	
	public BooleanProperty  editableProperty()  { return editable;}
	public Boolean getEditable()  { return editable.get();}
	public void setEditable(Boolean s)  { editable.set(s);}
	
	public DoubleProperty  scoreProperty()  { return score;}
	public Double getScore()  { return score.get();}
	public void setScore(Double s)  { score.set(s);}

	public StringProperty  header1Property()  { return header1;}
	public String getHeader1()  { return header1.get();}
	public void setHeader1(String s)  { header1.set(s);}

	public StringProperty  header2Property()  { return header2;}
	public String getHeader2()  { return header2.get();}
	public void setHeader2(String s)  { header2.set(s);}

	public StringProperty  speciesProperty()  { return species;}
	public String getSpecies()  { return species.get();}
	public void setSpecies(String s)  { species.set(s);}

	private List<Gene> geneList;
	public void setGeneList(List<Gene> g) {  geneList = g; 	}
	public List<Gene>  getGeneList() {	return geneList; }
}
