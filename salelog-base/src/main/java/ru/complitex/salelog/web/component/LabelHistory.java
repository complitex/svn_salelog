package ru.complitex.salelog.web.component;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.util.value.IValueMap;

import java.util.Set;

/**
 * @author Pavel Sknar
 */
public class LabelHistory extends Label {

    Set<String> changedFields;

    public LabelHistory(String id, String label, Set<String> changedFields) {
        super(id, label);
        this.changedFields = changedFields;
    }

    @Override
    protected void onComponentTag(ComponentTag tag) {
        super.onComponentTag(tag);
        if (changedFields != null && changedFields.contains(getId())) {
            IValueMap attributes = tag.getAttributes();
            attributes.put("style", "color: #f00;");
        }
    }
}
