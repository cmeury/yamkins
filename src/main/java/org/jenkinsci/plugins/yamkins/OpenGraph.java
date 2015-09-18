package org.jenkinsci.plugins.yamkins;

/**
 * enum representing the queryparmeters for the messages.json resource in the yammer rest api <br>
 * <a href="https://developer.yammer.com/restapi/#rest-messages>POST Yammer Message</a>
 * @author Bernd Kiefer <b.kiefer@raion.de>
 */
public enum OpenGraph {

    /** The title of your object as it should appear within the graph */
    TITLE("og_title"),

    /**  The canonical URL of the OG object that will be used as its permanent ID in the graph */
    URL("og_url"),

    /** A thumbnail image URL which represents your object in the graph */
    IMAGE("og_image"),

    /** A one to two sentence description of your object.*/
    DESCRIPTION("og_description"),

    /** An identifier to relate objects from a common domain, e.g., “Yammer Blog”. */
    SITE_NAME("og_site_name"),

    /** Structured metadata about this object that can be used by clients for custom rendering. */
    META("og_meta");


    private final String name;

    OpenGraph(String aName) {
        name = aName;
    }

    @Override
    public String toString() { return name; }


}
