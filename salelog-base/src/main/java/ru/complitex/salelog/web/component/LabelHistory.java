package ru.complitex.salelog.web.component;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.util.value.IValueMap;

import java.util.Set;

/**
 * @author Pavel Sknar
 */
public class LabelHistory extends Label {

    boolean changed;

    public LabelHistory(String id, String label, Set<String> changedFields) {
        super(id, label);
        changed = changedFields != null && changedFields.contains(id);
    }

    public LabelHistory(String id, String label, Boolean changed) {
        super(id, label);
        this.changed = changed != null? changed : false;
    }

    @Override
    protected void onComponentTag(ComponentTag tag) {
        super.onComponentTag(tag);
        if (! changed) {
            IValueMap attributes = tag.getAttributes();
            attributes.put("style", "color: dimgray;");
        }
    }
}
