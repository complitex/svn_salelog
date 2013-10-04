package ru.complitex.salelog.order.web.list;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang.StringUtils;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.*;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.complitex.address.strategy.region.RegionStrategy;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.entity.FilterWrapper;
import org.complitex.dictionary.entity.Person;
import org.complitex.dictionary.entity.example.DomainObjectExample;
import org.complitex.dictionary.web.component.DatePicker;
import org.complitex.dictionary.web.component.datatable.DataProvider;
import org.complitex.dictionary.web.component.paging.PagingNavigator;
import org.complitex.dictionary.web.component.scroll.ScrollBookmarkablePageLink;
import org.complitex.template.web.component.toolbar.AddItemButton;
import org.complitex.template.web.component.toolbar.ToolbarButton;
import org.complitex.template.web.security.SecurityRole;
import org.complitex.template.web.template.TemplatePage;
import ru.complitex.salelog.entity.CallGirl;
import ru.complitex.salelog.order.entity.Order;
import ru.complitex.salelog.order.entity.OrderStatus;
import ru.complitex.salelog.order.service.OrderBean;
import ru.complitex.salelog.order.web.edit.OrderEdit;
import ru.complitex.salelog.service.CallGirlBean;
import ru.complitex.salelog.service.ProductBean;

import javax.ejb.EJB;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.complitex.dictionary.util.PageUtil.newSorting;

/**
 * @author Pavel Sknar
 */
@AuthorizeInstantiation(SecurityRole.AUTHORIZED)
public class OrderList extends TemplatePage {

    private static final SimpleDateFormat CREATE_DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    @EJB
    private OrderBean orderBean;

    @EJB
    private CallGirlBean callGirlBean;

    @EJB
    private ProductBean productBean;

    @EJB
    private RegionStrategy regionStrategy;

    public OrderList() {
        init();
    }
    
    private void init() {

        final IModel<Order> filterModel = new CompoundPropertyModel<>(getFilterObject());

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
        final Form<Order> filterForm = new Form<>("filterForm", filterModel);
        container.add(filterForm);

        //Data Provider
        final DataProvider<Order> dataProvider = new DataProvider<Order>() {

            @Override
            protected Iterable<? extends Order> getData(int first, int count) {
                FilterWrapper<Order> filterWrapper = FilterWrapper.of(filterModel.getObject(), first, count);
                filterWrapper.setAscending(getSort().isAscending());
                filterWrapper.setSortProperty(getSort().getProperty());
                filterWrapper.setLike(true);

                return orderBean.getOrders(filterWrapper);
            }

            @Override
            protected int getSize() {
                FilterWrapper<Order> filterWrapper = FilterWrapper.of(new Order());
                return orderBean.count(filterWrapper);
            }
        };
        dataProvider.setSort("order_object_id", SortOrder.ASCENDING);

        //Data View
        DataView<Order> dataView = new DataView<Order>("data", dataProvider, 1) {

            @Override
            protected void populateItem(Item<Order> item) {
                final Order order = item.getModelObject();
                DomainObject region = order.getRegionId() != null? regionStrategy.findById(order.getRegionId(), false) : null;

                item.add(new Label("id", order.getId().toString()));
                item.add(new Label("createDate", order.getCreateDate() != null ? CREATE_DATE_FORMAT.format(order.getCreateDate()) : ""));
                item.add(new Label("callGirlCode", order.getCallGirl() != null? order.getCallGirl().getCode(): ""));
                item.add(new Label("customer", order.getCustomer() != null? order.getCustomer().toString(): ""));
                item.add(new Label("phones", order.getPhones()));
                item.add(new Label("region", region != null? regionStrategy.displayDomainObject(region, getLocale()): ""));
                item.add(new Label("address", order.getAddress()));
                item.add(new Label("comment", order.getComment()));
                item.add(new Label("status", order.getStatus() != null? order.getStatus().getLabel(getLocale()): ""));

                ScrollBookmarkablePageLink<WebPage> detailsLink = new ScrollBookmarkablePageLink<>("detailsLink",
                        getEditPage(), getEditPageParams(order.getId()),
                        String.valueOf(order.getId()));
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
                "order_object_id", "order_create_date", "cg_code", "order_customer", "order_phones", "order_region_id",
                "order_address", "order_comment", "order_status_code"));

        //Filters
        filterForm.add(new DatePicker<Date>("createDate"));

        filterForm.add(new TextField<>("callGirlCode", new IModel<String>() {
            @Override
            public String getObject() {
                return filterModel.getObject().getCallGirl().getCode();
            }

            @Override
            public void setObject(String object) {
                filterModel.getObject().getCallGirl().setCode(object);
            }

            @Override
            public void detach() {

            }
        }));

        filterForm.add(new TextField<>("customer", new PersonModel() {

            @Override
            protected void setPerson(Person person) {
                filterModel.getObject().setCustomer(person);
            }

            @Override
            protected Person getPerson() {
                return filterModel.getObject().getCustomer();
            }
        }));

        filterForm.add(new TextField<>("phones"));

        filterForm.add(new DropDownChoice<>("region",
                new IModel<DomainObject>() {
                    @Override
                    public DomainObject getObject() {
                        Order filterObject = filterModel.getObject();
                        return filterObject.getRegionId() != null ? regionStrategy.findById(filterObject.getRegionId(), false) : null;
                    }

                    @Override
                    public void setObject(DomainObject region) {
                        filterModel.getObject().setRegionId(region.getId());
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
                        return region != null ? region.getId().toString() : "-1";
                    }
                }
        ));

        filterForm.add(new TextField<>("address"));

        filterForm.add(new TextField<>("comment"));

        filterForm.add(new DropDownChoice<>("status",
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
        ));

        //Reset Action
        AjaxLink reset = new AjaxLink("reset") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                filterForm.clearInput();
                filterModel.setObject(getFilterObject());

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

    private Order getFilterObject() {
        Order filterObject = new Order();
        filterObject.setCallGirl(new CallGirl());
        filterObject.setCustomer(new Person());
        return filterObject;
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
        return OrderEdit.class;
    }

    private PageParameters getEditPageParams(Long id) {
        PageParameters parameters = new PageParameters();
        if (id != null) {
            parameters.add("orderId", id);
        }
        return parameters;
    }

    private abstract class PersonModel extends Model<String> {
        @Override
        public String getObject() {
            Person person = getPerson();
            return person != null? person.toString() : "";
        }

        @Override
        public void setObject(String fio) {
            if (StringUtils.isBlank(fio)) {
                setPerson(new Person());
            } else {
                fio = fio.trim();
                String[] personFio = fio.split(" ", 3);

                Person person = new Person();

                if (personFio.length > 0) {
                    person.setLastName(personFio[0]);
                }
                if (personFio.length > 1) {
                    person.setFirstName(personFio[1]);
                }
                if (personFio.length > 2) {
                    person.setMiddleName(personFio[2]);
                }

                setPerson(person);
            }
        }

        protected abstract void setPerson(Person person);

        protected abstract Person getPerson();
    }
}
