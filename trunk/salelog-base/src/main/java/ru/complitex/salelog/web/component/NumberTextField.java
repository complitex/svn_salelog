package ru.complitex.salelog.web.component;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.value.IValueMap;

/**
 * @author Pavel Sknar
 */
public class NumberTextField<N extends Number & Comparable<N>> extends org.apache.wicket.markup.html.form.NumberTextField<N> {
    public NumberTextField(String id) {
        super(id);
    }

    public NumberTextField(String id, IModel<N> model) {
        super(id, model);
    }

    public NumberTextField(String id, IModel<N> model, Class<N> type) {
        super(id, model, type);
    }

    @Override
    protected void onComponentTag(ComponentTag tag) {
        super.onComponentTag(tag);
        IValueMap attributes = tag.getAttributes();
        attributes.put("step", "any");
    }
}
