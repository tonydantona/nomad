package com.tonydantona.nomad;

import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.StringReader;

/**
 * Created by rti1ajd on 1/20/2017.
 */

public class XMLDestinationParser {

    private static final String TAG = "XMLMessageParser";
    private Destination mDestination;

    public Destination parseDestination(String message) throws XmlPullParserException {
        mDestination = new Destination();
        parseDestinationXML(message);
        return mDestination;
    }
    
    private  void parseDestinationXML(String msg) throws XmlPullParserException {
        // get XML resource parser
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser routeXML = factory.newPullParser();
        routeXML.setInput(new StringReader(msg));

        // set initial parse event and element node reference
        int routeEventType = XMLParsingUtilities.GetNextParseEvent(routeXML);
        String routeElementNode = null;

        while(routeEventType != XmlPullParser.END_DOCUMENT) {
            if (routeEventType == XmlPullParser.START_DOCUMENT) {
                // start of routes XML
                Log.d(TAG, "Start XML");
            } else if (routeEventType == XmlPullParser.START_TAG) {
                // opens a node
                routeElementNode = routeXML.getName();
            } else if (routeEventType == XmlPullParser.END_TAG) {
                // closes a node -- do nothing
            } else if (routeEventType == XmlPullParser.TEXT && (routeElementNode != null && routeXML.getText().length() != 0)) {
                // process contents of node
                switch (routeElementNode) {
                    case "Consignee":
                        Log.d(TAG, routeElementNode + ": " + routeXML.getText());
                        mDestination.setConsignee(routeXML.getText());
                        break;
                    case "NapLat":
                        Log.d(TAG, routeElementNode + ": " + routeXML.getText());
                        double napLat = Double.parseDouble(routeXML.getText());
                        mDestination.setNapLat(napLat);
                        break;
                    case "NapLong":
                        Log.d(TAG, routeElementNode + ": " + routeXML.getText());
                        double napLon = Double.parseDouble(routeXML.getText());
                        mDestination.setNapLon(napLon);
                        break;
                    case "HIN":
                        Log.d(TAG, routeElementNode + ": " + routeXML.getText());
                        mDestination.setHIN(routeXML.getText());
                        break;
                    // unused tag
                    default:
                        Log.e(TAG, routeElementNode + ": Unrecognized node");
                        break;
                }
            }

            routeEventType = XMLParsingUtilities.GetNextParseEvent(routeXML);
        }
    }


}
