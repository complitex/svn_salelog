package ru.complitex.salelog.util;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * @author Pavel Sknar
 */
public class XmlUtil {
    private static final ThreadLocal<XStream> xStreamThreadLocal = new ThreadLocal<>();

    public static XStream getXStream(){
        XStream xStream = xStreamThreadLocal.get();

        if (xStream == null){
            xStream = new XStream(new DomDriver());
            xStreamThreadLocal.set(xStream);
        }

        return xStream;
    }
}
