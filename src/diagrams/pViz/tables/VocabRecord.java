 package diagrams.pViz.tables;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import javafx.beans.property.SimpleStringProperty;

public class VocabRecord {

	private SimpleStringProperty term = new SimpleStringProperty();
	public SimpleStringProperty termProperty() { return term;  }
	public String getTerm()	{ return term.get();  }
	public void setTerm(String a)	{  term.set(a);  }

	private SimpleStringProperty id = new SimpleStringProperty();
	public SimpleStringProperty idProperty() { return id;  }
	public String getId()	{ return id.get();  }
	public void setId(String a)	{  id.set(a);  }

	private SimpleStringProperty ontology = new SimpleStringProperty();
	public SimpleStringProperty ontologyProperty() { return ontology;  }
	public String getOntology()	{ return ontology.get();  }
	public void setOntology(String a)	{  ontology.set(a);  }


	public VocabRecord(org.w3c.dom.Node elem) {
		for (int j=0; j<elem.getChildNodes().getLength(); j++)
		{
			org.w3c.dom.Node grandchild = elem.getChildNodes().item(j);
			if (grandchild == null) continue;
			org.w3c.dom.Node kid =  grandchild.getFirstChild();
			if (kid == null) continue;
			
			String subname = grandchild.getNodeName();
			if ("#text".equals(subname)) continue;
			if ("bp:ID".equals(subname))			id.set(kid.getNodeValue());
			else if ("bp:TERM".equals(subname))		term.set(kid.getTextContent());
			else if ("bp:Ontology".equals(subname))	ontology.set(kid.getTextContent());
		}
	}
	
	public String toString() { return getTerm() + " [" + getId() + ": " + getOntology() + "]"; }
}
