package ru.complitex.salelog.web.edit;

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.NumberTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.convert.IConverter;
import org.apache.wicket.util.string.StringValue;
import org.complitex.dictionary.converter.BigDecimalConverter;
import org.complitex.template.web.security.SecurityRole;
import org.complitex.template.web.template.FormTemplatePage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.complitex.salelog.entity.Product;
import ru.complitex.salelog.service.ProductBean;
import ru.complitex.salelog.web.list.ProductList;

import javax.ejb.EJB;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

/**
 * @author Pavel Sknar
 */
@AuthorizeInstantiation(SecurityRole.AUTHORIZED)
public class ProductEdit extends FormTemplatePage {
    private static final BigDecimalConverter converter = new BigDecimalConverter(2);

    @EJB
    private ProductBean productBean;

    private static final Logger log = LoggerFactory.getLogger(ProductEdit.class);

    private Product product;

    public ProductEdit() {
        init();
    }

    public ProductEdit(PageParameters parameters) {
        StringValue productId = parameters.get("productId");
        if (productId != null && !productId.isNull()) {
            product = productBean.getProduct(productId.toLong());
            if (product == null) {
                throw new RuntimeException("Product by id='" + productId + "' not found");
            }
        }
        init();
    }

    private void init() {

        if (product == null) {
            product = new Product();
        }

        IModel<Product> formModel = new CompoundPropertyModel<>(product);

        IModel<String> labelModel = new ResourceModel("label");

        add(new Label("title", labelModel));
        add(new Label("label", labelModel));

        final FeedbackPanel messages = new FeedbackPanel("messages");
        messages.setOutputMarkupId(true);
        add(messages);

        Form<Product> form = new Form<>("form", formModel);
        add(form);

        //eirc account field
        form.add(new TextField<>("code").setRequired(true));
        form.add(new TextField<>("name").setRequired(true));
        form.add(new NumberTextField<BigDecimal>("price") {
            @SuppressWarnings("unchecked")
            @Override
            public <C> IConverter<C> getConverter(Class<C> type) {
                return (IConverter<C>) converter;
            }

        }.setMinimum(BigDecimal.ZERO).setMaximum(new BigDecimal(Double.MAX_VALUE)).setRequired(true));

        // save button
        Button save = new Button("save") {

            @Override
            public void onSubmit() {

                productBean.save(product);

                getSession().info(getString("saved"));

                setResponsePage(ProductList.class);
            }
        };
        form.add(save);

        // cancel button
        Link<String> cancel = new Link<String>("cancel") {

            @Override
            public void onClick() {
                setResponsePage(ProductList.class);
            }
        };
        form.add(cancel);
    }

}
