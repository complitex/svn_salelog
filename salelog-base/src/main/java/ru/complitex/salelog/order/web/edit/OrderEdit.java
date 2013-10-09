package ru.complitex.salelog.order.web.edit;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.extensions.ajax.markup.html.autocomplete.AbstractAutoCompleteTextRenderer;
import org.apache.wicket.extensions.ajax.markup.html.autocomplete.AutoCompleteSettings;
import org.apache.wicket.extensions.ajax.markup.html.autocomplete.AutoCompleteTextField;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.*;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.convert.IConverter;
import org.apache.wicket.util.convert.converter.IntegerConverter;
import org.apache.wicket.util.string.StringValue;
import org.apache.wicket.util.string.Strings;
import org.complitex.address.strategy.region.RegionStrategy;
import org.complitex.dictionary.converter.BigDecimalConverter;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.entity.FilterWrapper;
import org.complitex.dictionary.entity.Person;
import org.complitex.dictionary.entity.example.DomainObjectExample;
import org.complitex.dictionary.web.component.datatable.DataProvider;
import org.complitex.template.web.security.SecurityRole;
import org.complitex.template.web.template.FormTemplatePage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.complitex.salelog.entity.CallGirl;
import ru.complitex.salelog.entity.Product;
import ru.complitex.salelog.order.entity.Order;
import ru.complitex.salelog.order.entity.OrderStatus;
import ru.complitex.salelog.order.entity.ProductSale;
import ru.complitex.salelog.order.service.OrderBean;
import ru.complitex.salelog.order.web.list.OrderList;
import ru.complitex.salelog.service.CallGirlBean;
import ru.complitex.salelog.service.ProductBean;
import ru.complitex.salelog.util.HistoryUtils;
import ru.complitex.salelog.web.component.LabelHistory;
import ru.complitex.salelog.web.component.NumberTextField;

import javax.ejb.EJB;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Pavel Sknar
 */
@AuthorizeInstantiation(SecurityRole.AUTHORIZED)
public class OrderEdit extends FormTemplatePage {

    private static final Logger log = LoggerFactory.getLogger(OrderEdit.class);

    private static final BigDecimalConverter BIG_DECIMAL_CONVERTER = new BigDecimalConverter(2);

    private static final IntegerConverter INTEGER_CONVERTER = new IntegerConverter();

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    @EJB
    private OrderBean orderBean;

    @EJB
    private CallGirlBean callGirlBean;

    @EJB
    private ProductBean productBean;

    @EJB
    private RegionStrategy regionStrategy;

    private Order order;

    private List<Order> history = Lists.newArrayList();

    public OrderEdit() {
        init();
    }

    public OrderEdit(PageParameters parameters) {
        StringValue orderId = parameters.get("orderId");
        if (orderId != null && !orderId.isNull()) {
            history = orderBean.getOrder(orderId.toLong());
            if (history.size() <= 0) {
                throw new RuntimeException("Order by id='" + orderId + "' not found");
            }
            order = history.remove(0);
        }
        init();
    }

    private void init() {

        if (order == null) {
            order = new Order();
            order.setCallGirl(new CallGirl());
            order.setStatus(OrderStatus.EMPTY);
            order.setCustomer(new Person());
        }

        IModel<String> labelModel = new ResourceModel("label");

        add(new Label("title", labelModel));
        add(new Label("label", labelModel));

        final FeedbackPanel messages = new FeedbackPanel("messages");
        messages.setOutputMarkupId(true);
        add(messages);

        final Form form = new Form("form");
        add(form);

        // call girl`s code
        final AutoCompleteTextField<CallGirl> callGirlField = new AutoCompleteTextField<CallGirl>("callGirl",
                new IModel<CallGirl>() {
                    @Override
                    public CallGirl getObject() {
                        return order.getCallGirl();
                    }

                    @Override
                    public void setObject(CallGirl callGirl) {
                        order.setCallGirl(callGirl);
                    }

                    @Override
                    public void detach() {

                    }
                }
                , CallGirl.class
                , new AbstractAutoCompleteTextRenderer<CallGirl>() {
                    @Override
                    protected String getTextValue(CallGirl object) {
                        return object.getCode();
                    }
                }
                , new AutoCompleteSettings()
        ) {
            @Override
            protected Iterator<CallGirl> getChoices(String input)
            {
                if (Strings.isEmpty(input)) {
                    List<CallGirl> emptyList = Collections.emptyList();
                    return emptyList.iterator();
                }

                FilterWrapper<CallGirl> filter = FilterWrapper.of(new CallGirl(input));
                filter.setLike(true);
                filter.setCount(10);
                List<CallGirl> choices = callGirlBean.getCallGirls(filter);

                return choices.iterator();
            }

            @Override
            public <C> IConverter<C> getConverter(Class<C> type) {
                return new IConverter<C>() {
                    @SuppressWarnings("unchecked")
                    @Override
                    public C convertToObject(String value, Locale locale) {
                        FilterWrapper<CallGirl> filter = FilterWrapper.of(new CallGirl(value));
                        filter.setLike(false);
                        filter.setCount(1);
                        List<CallGirl> callGirls = callGirlBean.getCallGirls(filter);
                        return callGirls.size() > 0? (C)callGirls.get(0) : null;
                    }

                    @Override
                    public String convertToString(C value, Locale locale) {
                        return value != null? ((CallGirl)value).getCode(): "";
                    }
                };
            }
        };
        callGirlField.setRequired(true);
        form.add(callGirlField);

        // customer FIO
        form.add(new TextField<>("lastName",   new PropertyModel<String>(order.getCustomer(), "lastName")).setRequired(true));
        form.add(new TextField<>("firstName",  new PropertyModel<String>(order.getCustomer(), "firstName")).setRequired(true));
        form.add(new TextField<>("middleName", new PropertyModel<String>(order.getCustomer(), "middleName")));

        // phones
        form.add(new TextField<>("phones", new PropertyModel<String>(order, "phones")).setRequired(true));

        // address
        form.add(new TextField<>("address", new PropertyModel<String>(order, "address")).setRequired(true));

        // region
        form.add(new DropDownChoice<>("region",
                new IModel<DomainObject>() {
                    @Override
                    public DomainObject getObject() {
                        return order.getRegionId() != null? regionStrategy.findById(order.getRegionId(), false) : null;
                    }

                    @Override
                    public void setObject(DomainObject region) {
                        order.setRegionId(region.getId());
                    }

                    @Override
                    public void detach() {

                    }
                },
                regionStrategy.find(new DomainObjectExample()),
                new IChoiceRenderer<DomainObject>() {
                    @Override
                    public Object getDisplayValue(DomainObject region) {
                        return regionStrategy.displayDomainObject(region, getLocale());
                    }

                    @Override
                    public String getIdValue(DomainObject region, int i) {
                        return region !=null? region.getId().toString() : "-1";
                    }
                }
        ).setRequired(true));

        // comment
        form.add(new TextField<>("comment", new PropertyModel<String>(order, "comment")));

        form.add(new DropDownChoice<>("status",
                new IModel<OrderStatus>() {
                    @Override
                    public OrderStatus getObject() {
                        return order.getStatus();
                    }

                    @Override
                    public void setObject(OrderStatus status) {
                        order.setStatus(status);
                    }

                    @Override
                    public void detach() {

                    }
                },
                Arrays.asList(OrderStatus.values()),
                new IChoiceRenderer<OrderStatus>() {
                    @Override
                    public Object getDisplayValue(OrderStatus status) {
                        return status.getLabel(getLocale());
                    }

                    @Override
                    public String getIdValue(OrderStatus status, int i) {
                        return status.getId().toString();
                    }
                }
        ).setRequired(true));

        final WebMarkupContainer container = new WebMarkupContainer("container");
        container.setOutputMarkupPlaceholderTag(true);
        container.setVisible(true);
        form.add(container);

        //Data Provider
        final DataProvider<ProductSale> dataProvider = new DataProvider<ProductSale>() {

            @Override
            protected Iterable<? extends ProductSale> getData(int first, int count) {

                return order.getProductSales();
            }

            @Override
            protected int getSize() {
                return order.getProductSales().size();
            }
        };

        //Data View
        DataView<ProductSale> dataView = new DataView<ProductSale>("data", dataProvider) {

            @Override
            protected void populateItem(Item<ProductSale> item) {
                final ProductSale sale = item.getModelObject();

                item.add(new Label("productCode", sale.getProduct().getCode()));
                item.add(new Label("price", BIG_DECIMAL_CONVERTER.convertToString(sale.getPrice(), getLocale())));
                item.add(new Label("count", Integer.toString(sale.getCount())));
                item.add(new Label("totalCost", BIG_DECIMAL_CONVERTER.convertToString(sale.getTotalCost(), getLocale())));

                AjaxLink deleteLink = new AjaxLink("deleteProductSaleLink") {
                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        order.getProductSales().remove(sale);
                        target.add(container);
                    }
                };
                deleteLink.add(new Label("deleteProductSaleMessage", new AbstractReadOnlyModel<String>() {

                    @Override
                    public String getObject() {
                        return getString("delete");
                    }
                }));
                item.add(deleteLink);
            }
        };
        container.add(dataView);

        final IModel<Product> productModel = new Model<>();
        final IModel<Integer> countModel = new Model<>(1);

        final AutoCompleteTextField<Product> productField = new AutoCompleteTextField<Product>("product",
                productModel
                , Product.class
                , new AbstractAutoCompleteTextRenderer<Product>() {
            @Override
            protected String getTextValue(Product object) {
                return object.getCode();
            }
        }
                , new AutoCompleteSettings()
        ) {
            @Override
            protected Iterator<Product> getChoices(String input)
            {
                if (Strings.isEmpty(input)) {
                    List<Product> emptyList = Collections.emptyList();
                    return emptyList.iterator();
                }

                FilterWrapper<Product> filter = FilterWrapper.of(new Product(input));
                filter.setLike(true);
                filter.setCount(10);
                List<Product> choices = productBean.getProducts(filter);
                for (ProductSale sale : order.getProductSales()) {
                    choices.remove(sale.getProduct());
                }

                return choices.iterator();
            }

            @Override
            public <C> IConverter<C> getConverter(Class<C> type) {
                return new IConverter<C>() {
                    @SuppressWarnings("unchecked")
                    @Override
                    public C convertToObject(String value, Locale locale) {
                        FilterWrapper<Product> filter = FilterWrapper.of(new Product(value));
                        filter.setLike(false);
                        filter.setCount(1);
                        List<Product> products = productBean.getProducts(filter);
                        return products.size() > 0? (C)products.get(0) : null;
                    }

                    @Override
                    public String convertToString(C value, Locale locale) {
                        return value != null? ((Product)value).getCode(): "";
                    }
                };
            }
        };
        productField.setRequired(true);
        container.add(productField);

        final NumberTextField<Integer> countField = new NumberTextField<Integer>("count", countModel, Integer.class) {
            @SuppressWarnings("unchecked")
            @Override
            public <C> IConverter<C> getConverter(Class<C> type) {
                return (IConverter<C>) INTEGER_CONVERTER;
            }

        };
        countField.setMinimum(1);
        countField.setMaximum(Integer.MAX_VALUE);
        countField.setRequired(true);
        container.add(countField);
        
        container.add(new AjaxButton("addProductSaleButton") {


            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                boolean hasErrors = false;

                countField.validate();
                countField.updateModel();

                productField.validate();
                productField.updateModel();

                if (productModel.getObject() == null) {
                    form.error("Product is required field");
                    hasErrors = true;
                }
                if (countModel.getObject() == null) {
                    form.error("Count product is required field");
                    hasErrors = true;
                }
                if (!hasErrors) {
                    ProductSale sale = new ProductSale();
                    sale.setCount(countModel.getObject());
                    sale.setProduct(productModel.getObject());
                    sale.setPrice(productModel.getObject().getPrice());
                    sale.setTotalCost(sale.getPrice().multiply(new BigDecimal(sale.getCount())));

                    countModel.setObject(1);

                    order.getProductSales().add(sale);
                }
                target.add(container);
            }
        }.setDefaultFormProcessing(false));

        //history
        //Data Provider
        final DataProvider<Order> historyProvider = new DataProvider<Order>() {

            @Override
            protected Iterable<? extends Order> getData(int first, int count) {

                return history;
            }

            @Override
            protected int getSize() {
                return history.size();
            }
        };
        dataProvider.setSort("order_object_id", SortOrder.ASCENDING);

        final Map<Order, Set<String>> changed = Maps.newHashMap();
        try {
            changed.putAll(HistoryUtils.getChangedFields(order, history));
        } catch (Exception e) {
            log.warn("Can get changed fields", e);
        }

        //Data View
        DataView<Order> historyView = new DataView<Order>("history", historyProvider) {

            @Override
            protected void populateItem(Item<Order> item) {
                final Order order = item.getModelObject();
                Set<String> changedFields = changed.get(order);
                DomainObject region = order.getRegionId() != null? regionStrategy.findById(order.getRegionId(), false) : null;

                item.add(new Label("beginDate", order.getBeginDate() != null ? DATE_FORMAT.format(order.getBeginDate()) : ""));
                item.add(new LabelHistory("callGirl", order.getCallGirl() != null? order.getCallGirl().getCode(): "", changedFields));
                item.add(new LabelHistory("customer", order.getCustomer() != null? order.getCustomer().toString(): "", changedFields));
                item.add(new LabelHistory("phones", order.getPhones(), changedFields));
                item.add(new LabelHistory("region", region != null? regionStrategy.displayDomainObject(region, getLocale()): "", changedFields));
                item.add(new LabelHistory("address", order.getAddress(), changedFields));
                item.add(new LabelHistory("comment", order.getComment(), changedFields));
                item.add(new LabelHistory("status", order.getStatus() != null? order.getStatus().getLabel(getLocale()): "", changedFields));


                final Map<ProductSale, Boolean> changed = Maps.newHashMap();
                for (ProductSale sale : order.getProductSales()) {
                    changed.put(sale, !OrderEdit.this.order.getProductSales().contains(sale));
                }
                for (ProductSale sale : OrderEdit.this.order.getProductSales()) {
                    changed.put(sale, !order.getProductSales().contains(sale));
                }

                final DataProvider<ProductSale> dataProvider = new DataProvider<ProductSale>() {

                    @Override
                    protected Iterable<? extends ProductSale> getData(int first, int count) {

                        return order.getProductSales();
                    }

                    @Override
                    protected int getSize() {
                        return order.getProductSales().size();
                    }
                };

                item.add(new DataView<ProductSale>("productCodeView", dataProvider) {

                    @Override
                    protected void populateItem(Item<ProductSale> item) {
                        final ProductSale sale = item.getModelObject();

                        item.add(new LabelHistory("productCode", sale.getProduct().getCode(), changed.get(sale)));
                    }
                });
                item.add(new DataView<ProductSale>("priceView", dataProvider) {

                    @Override
                    protected void populateItem(Item<ProductSale> item) {
                        final ProductSale sale = item.getModelObject();

                        item.add(new LabelHistory("price",
                                BIG_DECIMAL_CONVERTER.convertToString(sale.getPrice(), getLocale()),
                                changed.get(sale)));
                    }
                });
                item.add(new DataView<ProductSale>("countView", dataProvider) {

                    @Override
                    protected void populateItem(Item<ProductSale> item) {
                        final ProductSale sale = item.getModelObject();

                        item.add(new LabelHistory("count", Integer.toString(sale.getCount()), changed.get(sale)));
                    }
                });
                item.add(new DataView<ProductSale>("totalCostView", dataProvider) {

                    @Override
                    protected void populateItem(Item<ProductSale> item) {
                        final ProductSale sale = item.getModelObject();

                        item.add(new LabelHistory("totalCost",
                                BIG_DECIMAL_CONVERTER.convertToString(sale.getTotalCost(),
                                getLocale()), changed.get(sale)));
                    }
                });
            }
        };

        add(historyView);

        // save button
        Button save = new Button("save") {

            @Override
            public void onSubmit() {

                orderBean.save(order);

                getSession().info(getString("saved"));

                setResponsePage(OrderList.class);
            }
        };
        form.add(save);

        // cancel button
        Link<String> cancel = new Link<String>("cancel") {

            @Override
            public void onClick() {
                setResponsePage(OrderList.class);
            }
        };
        form.add(cancel);
    }

}
