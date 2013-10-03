package ru.complitex.salelog.order.web.edit;

import com.google.common.collect.Lists;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;
import org.complitex.address.strategy.region.RegionStrategy;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.entity.FilterWrapper;
import org.complitex.dictionary.entity.Person;
import org.complitex.dictionary.entity.example.DomainObjectExample;
import org.complitex.dictionary.web.component.DatePicker;
import org.complitex.template.web.security.SecurityRole;
import org.complitex.template.web.template.FormTemplatePage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.complitex.salelog.entity.CallGirl;
import ru.complitex.salelog.order.entity.Order;
import ru.complitex.salelog.order.entity.OrderStatus;
import ru.complitex.salelog.order.service.OrderBean;
import ru.complitex.salelog.order.web.list.OrderList;
import ru.complitex.salelog.service.CallGirlBean;
import ru.complitex.salelog.service.ProductBean;

import javax.ejb.EJB;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author Pavel Sknar
 */
@AuthorizeInstantiation(SecurityRole.AUTHORIZED)
public class OrderEdit extends FormTemplatePage {

    private static final Logger log = LoggerFactory.getLogger(OrderEdit.class);

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
            order = history.get(0);
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
        form.add(new DropDownChoice<>("callGirlCode",
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
                },
                callGirlBean.getCallGirls(null),
                new IChoiceRenderer<CallGirl>() {
                    @Override
                    public Object getDisplayValue(CallGirl callGirl) {
                        return callGirl.getCode();
                    }

                    @Override
                    public String getIdValue(CallGirl callGirl, int i) {
                        return callGirl != null && callGirl.getId() != null? callGirl.getId().toString(): "-1";
                    }
                }
        ).setRequired(true));

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