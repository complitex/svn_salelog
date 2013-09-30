package ru.complitex.salelog.web.edit;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;
import org.complitex.dictionary.entity.FilterWrapper;
import org.complitex.template.web.security.SecurityRole;
import org.complitex.template.web.template.FormTemplatePage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.complitex.salelog.entity.CallGirl;
import ru.complitex.salelog.entity.Product;
import ru.complitex.salelog.service.CallGirlBean;
import ru.complitex.salelog.web.list.CallGirlList;

import javax.ejb.EJB;
import java.text.MessageFormat;
import java.util.List;

/**
 * @author Pavel Sknar
 */
@AuthorizeInstantiation(SecurityRole.AUTHORIZED)
public class CallGirlEdit extends FormTemplatePage {

    @EJB
    private CallGirlBean callGirlBean;

    private static final Logger log = LoggerFactory.getLogger(CallGirlEdit.class);

    private CallGirl callGirl;

    public CallGirlEdit() {
        init();
    }

    public CallGirlEdit(PageParameters parameters) {
        StringValue callGirlId = parameters.get("callGirlId");
        if (callGirlId != null && !callGirlId.isNull()) {
            callGirl = callGirlBean.getCallGirl(callGirlId.toLong());
            if (callGirl == null) {
                throw new RuntimeException("CallGirl by id='" + callGirlId + "' not found");
            }
        }
        init();
    }

    private void init() {

        if (callGirl == null) {
            callGirl = new CallGirl();
        }

        IModel<String> labelModel = new ResourceModel("label");

        add(new Label("title", labelModel));
        add(new Label("label", labelModel));

        final FeedbackPanel messages = new FeedbackPanel("messages");
        messages.setOutputMarkupId(true);
        add(messages);

        final Form form = new Form("form");
        add(form);

        //eirc account field
        form.add(new TextField<>("code", new PropertyModel<String>(callGirl, "code")).setRequired(true));

        // FIO fields
        form.add(new TextField<>("lastName",   new PropertyModel<String>(callGirl.getPerson(), "lastName")).setRequired(true));
        form.add(new TextField<>("firstName",  new PropertyModel<String>(callGirl.getPerson(), "firstName")).setRequired(true));
        form.add(new TextField<>("middleName", new PropertyModel<String>(callGirl.getPerson(), "middleName")));
        // save button
        Button save = new Button("save") {

            @Override
            public void onSubmit() {

                List<CallGirl> callGirls = callGirlBean.getCallGirls(FilterWrapper.of(new CallGirl(callGirl.getCode())));
                if (callGirls.size() > 0) {
                    if (callGirl.getId() == null ||
                            !callGirls.get(0).getId().equals(callGirl.getId())) {
                        form.error(MessageFormat.format(getString("error_duplicate_code"), callGirl.getCode()));
                        return;
                    }
                }

                callGirlBean.save(callGirl);

                getSession().info(getString("saved"));

                setResponsePage(CallGirlList.class);
            }
        };
        form.add(save);

        // cancel button
        Link<String> cancel = new Link<String>("cancel") {

            @Override
            public void onClick() {
                setResponsePage(CallGirlList.class);
            }
        };
        form.add(cancel);
    }

}
