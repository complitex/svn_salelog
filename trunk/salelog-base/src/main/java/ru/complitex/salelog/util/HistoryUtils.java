package ru.complitex.salelog.util;

import com.google.common.base.Joiner;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;

/**
 * @author Pavel Sknar
 */
public abstract class HistoryUtils {

    public static <T> Map<T, Set<String>> getChangedFields(List<T> objects) throws Exception {

        Map<T, Set<String>> result = Maps.newHashMap();

        T oldObject = null;
        T newObject = null;

        Map<String, String> oldFields = null;
        Map<String, String> newFields = null;

        for (T object : objects) {
            if (newObject == null) {
                newObject = object;
                newFields = getFields(newObject);
                continue;
            } else if (oldObject != null) {
                newObject = oldObject;
                newFields = oldFields;
            }
            oldObject = object;
            oldFields = getFields(oldObject);
            MapDifference<String, String> diffMap = Maps.difference(
                    oldFields,
                    newFields);

            result.put(oldObject, diffMap.entriesDiffering().keySet());
        }

        return result;
    }

    private static <T> Map<String, String> getFields(T object) throws Exception {

        String xmlObject = XmlUtil.getXStream().toXML(object);

        if (StringUtils.isEmpty(xmlObject)) {
            return Collections.emptyMap();
        }

        Map<String, String> fields = Maps.newHashMap();

        DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

        InputStream is = new ByteArrayInputStream(xmlObject.getBytes("UTF-8"));

        org.w3c.dom.Document doc = documentBuilder.parse(is);

        NodeList propertyNodeList = doc.getDocumentElement().getChildNodes();

        for (int i = 0; i < propertyNodeList.getLength(); i++) {
            Node node = propertyNodeList.item(i);

            if (node instanceof Element) {
                Element element = (Element) node;

                NodeList subNodeList = element.getChildNodes();

                int subLength = subNodeList.getLength();

                if (subLength > 2) {
                    for (int k = 0; k < subLength; ++k){
                        Node subNode = subNodeList.item(k);

                        if (subNode instanceof Element) {
                            Element subElement = (Element) subNode;

                            String id = ":";

                            NodeList idNodeList = subElement.getElementsByTagName("id");
                            if (idNodeList.getLength() > 0){
                                id = ":" + idNodeList.item(0).getTextContent().trim() + ":";
                            }

                            fields.put(element.getTagName() + id  + getLastPacketName(subElement.getTagName()),
                                    getString(subElement));
                        }
                    }
                } else {
                    fields.put(element.getTagName(), "<code>" + element.getTextContent().trim() + "</code>");
                }
            }
        }

        return fields;
    }

    private static String getString(Node node){
        NodeList childNodeList = node.getChildNodes();

        int childLength = childNodeList.getLength();

        if (childLength > 2){
            List<String> list = new ArrayList<>();

            for (int i = 0; i < childLength; ++i){
                Node childNode = childNodeList.item(i);

                if (childNode instanceof Element){
                    Element childElement = (Element) childNode;

                    list.add(getLastPacketName(childElement.getTagName()) + ": <code>" +
                            childElement.getTextContent().trim() + "</code>" );
                }
            }

            return Joiner.on(", ").join(list);
        }

        return "<code>" + node.getTextContent().trim() + "</code>";
    }

    private static String getLastPacketName(String s){
        if (s == null){
            return "";
        }

        return s.substring(s.lastIndexOf('.') + 1);
    }
}
