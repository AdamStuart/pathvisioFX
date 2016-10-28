package diagrams.draw.gpml;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.NamedNodeMap;

public class BiopaxRef {

	String xrefid;
	String id;
	String db;
	String title;
	String source;
	String year;
	List<String> authors = new ArrayList<String>();
		
	public BiopaxRef(org.w3c.dom.Node elem) {
		
		for (int i=0; i<elem.getChildNodes().getLength(); i++)
		{
			org.w3c.dom.Node child = elem.getChildNodes().item(i);
			String name = child.getNodeName();
//			System.out.println(name);
			if ("bp:PublicationXref".equals(name))
			{
				NamedNodeMap attrs = child.getAttributes();
				xrefid = attrs.getNamedItem("rdf:id").getNodeValue();
				for (int j=0; j<child.getChildNodes().getLength(); j++)
				{
					org.w3c.dom.Node grandchild = child.getChildNodes().item(j);
					if (grandchild == null) continue;
					org.w3c.dom.Node kid =  grandchild.getFirstChild();
					if (kid == null) continue;
					
					String subname = grandchild.getNodeName();
					if ("#text".equals(subname)) continue;
					if ("bp:ID".equals(subname))		id = kid.getNodeValue();
					if ("bp:DB".equals(subname))		db = kid.getTextContent();
					if ("bp:TITLE".equals(subname))		title = kid.getTextContent();
					if ("bp:SOURCE".equals(subname))	source = kid.getTextContent();
					if ("bp:YEAR".equals(subname))		year = kid.getTextContent();
					if ("bp:AUTHORS".equals(subname))	authors.add(kid.getTextContent());
				}
			}
		}
	}
	public String toString()
	{
		String firstAuthor =  authors.size() > 0 ? authors.get(0) : "n/a";
		return db + ": " + id + ", " + firstAuthor + ", [" + year + ", " + source + ", " + title + "].";
	}

}
