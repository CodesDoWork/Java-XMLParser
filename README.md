# XMLParser
A project to parse XML to any Object or any Object to XML in Java!
***

## Usage

### 1. Create a new SAXHandler and parse a xml String

    String xml = "<team>...</team>"
    SAXHandler<Team> handler = new SAXHandler<>(Team.class);
    Team team = SAX.parse(xml, handler);

### 2. Register all necessary ValueParsers or use Annotations

If you come into a need of using extra parsers, you can use the following:
    
    handler.registerParser(Bar.class, new BarParser());

or the annotation equivalent:

    public class Foo {
        
        @XmlParser(BarParser.class)
        Bar bar:
    }
    
### 3. Object to XML

    new XMLWriter().toXML(anyObject)
