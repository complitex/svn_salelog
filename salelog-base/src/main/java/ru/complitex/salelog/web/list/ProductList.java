package ru.complitex.salelog.web.list;

import com.google.common.collect.ImmutableList;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.convert.IConverter;
import org.complitex.dictionary.converter.BigDecimalConverter;
import org.complitex.dictionary.entity.FilterWrapper;
import org.complitex.dictionary.web.component.datatable.DataProvider;
import org.complitex.dictionary.web.component.paging.PagingNavigator;
import org.complitex.dictionary.web.component.scroll.ScrollBookmarkablePageLink;
import org.complitex.template.web.component.toolbar.AddItemButton;
import org.complitex.template.web.component.toolbar.ToolbarButton;
import org.complitex.template.web.template.TemplatePage;
import ru.complitex.salelog.entity.Product;
import ru.complitex.salelog.service.ProductBean;
import ru.complitex.salelog.web.component.NumberTextField;
import ru.complitex.salelog.web.edit.ProductEdit;
import ru.complitex.salelog.web.security.SecurityRole;

import javax.ejb.EJB;
import java.math.BigDecimal;
import java.util.List;

import static org.complitex.dictionary.util.PageUtil.newSorting;

/**
 * @author Pavel Sknar
 */
@AuthorizeInstantiation(SecurityRole.PRODUCT_VIEW)
public class ProductList extends TemplatePage {

    private static final BigDecimalConverter converter = new BigDecimalConverter(2);

    @EJB
    private ProductBean productBean;

    private IModel<Product> filterModel = new CompoundPropertyModel<>(new Product());

    public ProductList() {
        init();
    }

    private void init() {
        IModel<String> labelModel = new ResourceModel("label");

        add(new Label("title", labelModel));
        add(new Label("label", labelModel));

        final FeedbackPanel messages = new FeedbackPanel("messages");
        messages.setOutputMarkupId(true);
        add(messages);

        final WebMarkupContainer container = new WebMarkupContainer("container");
        container.setOutputMarkupPlaceholderTag(true);
        container.setVisible(true);
        add(container);

        //Form
        final Form<Product> filterForm = new Form<>("filterForm", filterModel);
        container.add(filterForm);

        //Data Provider
        final DataProvider<Product> dataProvider = new DataProvider<Product>() {

            @Override
            protected Iterable<? extends Product> getData(int first, int count) {
                FilterWrapper<Product> filterWrapper = FilterWrapper.of(filterModel.getObject(), first, count);
                filterWrapper.setAscending(getSort().isAscending());
                filterWrapper.setSortProperty(getSort().getProperty());
                filterWrapper.setLike(true);

                return productBean.getProducts(filterWrapper);
            }

            @Override
            protected int getSize() {
                FilterWrapper<Product> filterWrapper = FilterWrapper.of(new Product());
                return productBean.count(filterWrapper);
            }
        };
        dataProvider.setSort("code", SortOrder.ASCENDING);

        //Data View
        DataView<Product> dataView = new DataView<Product>("data", dataProvider, 1) {

            @Override
            protected void populateItem(Item<Product> item) {
                final Product product = item.getModelObject();

                item.add(new Label("code", product.getCode()));
                item.add(new Label("name", product.getName()));
                item.add(new Label("price", converter.convertToString(product.getPrice(), getLocale())));

                ScrollBookmarkablePageLink<WebPage> detailsLink = new ScrollBookmarkablePageLink<>("detailsLink",
                        getEditPage(), getEditPageParams(product.getId()),
                        String.valueOf(product.getId()));
                detailsLink.add(new Label("editMessage", new AbstractReadOnlyModel<String>() {

                    @Override
                    public String getObject() {
                        return getString("edit");
                    }
                }));
                item.add(detailsLink);
            }
        };
        filterForm.add(dataView);

        //Sorting
        filterForm.add(newSorting("header.", dataProvider, dataView, filterForm, true,
                "code", "name", "price"));

        //Filters
        filterForm.add(new TextField<>("code"));

        filterForm.add(new TextField<>("name"));

        filterForm.add(new NumberTextField<BigDecimal>("price") {
            @SuppressWarnings("unchecked")
            @Override
            public <C> IConverter<C> getConverter(Class<C> type) {
                return (IConverter<C>)converter;
            }
        }.setMinimum(BigDecimal.ZERO).setMaximum(new BigDecimal(Integer.MAX_VALUE)));

        //Reset Action
        AjaxLink reset = new AjaxLink("reset") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                filterForm.clearInput();
                filterModel.setObject(new Product());

                target.add(container);
            }
        };
        filterForm.add(reset);

        //Submit Action
        AjaxButton submit = new AjaxButton("submit", filterForm) {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {

                target.add(container);
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
            }
        };
        filterForm.add(submit);

        //Navigator
        container.add(new PagingNavigator("navigator", dataView, getPreferencesPage(), container));

    }

    @Override
    protected List<? extends ToolbarButton> getToolbarButtons(String id) {
        return ImmutableList.of(new AddItemButton(id) {

            @Override
            protected void onClick() {
                this.getPage().setResponsePage(getEditPage(), getEditPageParams(null));
            }
        });
    }

    private Class<? extends Page> getEditPage() {
        return ProductEdit.class;
    }

    private PageParameters getEditPageParams(Long id) {
        PageParameters parameters = new PageParameters();
        if (id != null) {
            parameters.add("productId", id);
        }
        return parameters;
    }
}

