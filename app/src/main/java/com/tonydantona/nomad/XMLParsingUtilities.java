package com.tonydantona.nomad;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

// utility function for XML resource (res/xml) parsing
public class XMLParsingUtilities {

    // returns type of the next XML parsing event
    public static int GetNextParseEvent(XmlPullParser xmlResourceParser) {
        try {
            return xmlResourceParser.next();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }
}