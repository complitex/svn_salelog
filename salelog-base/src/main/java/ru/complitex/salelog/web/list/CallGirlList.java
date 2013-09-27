package ru.complitex.salelog.web.list;

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
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.*;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.complitex.dictionary.entity.FilterWrapper;
import org.complitex.dictionary.entity.Person;
import org.complitex.dictionary.web.component.datatable.DataProvider;
import org.complitex.dictionary.web.component.paging.PagingNavigator;
import org.complitex.dictionary.web.component.scroll.ScrollBookmarkablePageLink;
import org.complitex.template.web.component.toolbar.AddItemButton;
import org.complitex.template.web.component.toolbar.ToolbarButton;
import org.complitex.template.web.security.SecurityRole;
import org.complitex.template.web.template.TemplatePage;
import ru.complitex.salelog.entity.CallGirl;
import ru.complitex.salelog.service.CallGirlBean;
import ru.complitex.salelog.web.edit.CallGirlEdit;

import javax.ejb.EJB;
import java.util.List;

import static org.complitex.dictionary.util.PageUtil.newSorting;

/**
 * @author Pavel Sknar
 */
@AuthorizeInstantiation(SecurityRole.AUTHORIZED)
public class CallGirlList extends TemplatePage {

    @EJB
    private CallGirlBean callGirlBean;
    
    private IModel<CallGirl> filterModel = new CompoundPropertyModel<>(new CallGirl());

    public CallGirlList() {
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
        final Form<CallGirl> filterForm = new Form<>("filterForm", filterModel);
        container.add(filterForm);

        //Data Provider
        final DataProvider<CallGirl> dataProvider = new DataProvider<CallGirl>() {

            @Override
            protected Iterable<? extends CallGirl> getData(int first, int count) {
                FilterWrapper<CallGirl> filterWrapper = FilterWrapper.of(filterModel.getObject(), first, count);
                filterWrapper.setAscending(getSort().isAscending());
                filterWrapper.setSortProperty(getSort().getProperty());
                filterWrapper.setLike(true);

                return callGirlBean.getCallGirls(filterWrapper);
            }

            @Override
            protected int getSize() {
                FilterWrapper<CallGirl> filterWrapper = FilterWrapper.of(new CallGirl());
                return callGirlBean.count(filterWrapper);
            }
        };
        dataProvider.setSort("code", SortOrder.ASCENDING);

        //Data View
        DataView<CallGirl> dataView = new DataView<CallGirl>("data", dataProvider, 1) {

            @Override
            protected void populateItem(Item<CallGirl> item) {
                final CallGirl callGirl = item.getModelObject();


                item.add(new Label("code", callGirl.getCode()));
                item.add(new Label("person", callGirl.getPerson() != null? callGirl.getPerson().toString(): ""));

                ScrollBookmarkablePageLink<WebPage> detailsLink = new ScrollBookmarkablePageLink<>("detailsLink",
                        getEditPage(), getEditPageParams(callGirl.getId()),
                        String.valueOf(callGirl.getId()));
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
                "code", "person"));

        //Filters
        filterForm.add(new TextField<>("code"));

        filterForm.add(new TextField<>("person", new Model<String>() {

            @Override
            public String getObject() {
                return filterModel.getObject().getPerson() != null? filterModel.getObject().getPerson().toString() : "";
            }

            @Override
            public void setObject(String fio) {
                if (StringUtils.isBlank(fio)) {
                    filterModel.getObject().setPerson(new Person());
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

                    filterModel.getObject().setPerson(person);
                }
            }
        }));

        //Reset Action
        AjaxLink reset = new AjaxLink("reset") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                filterForm.clearInput();
                filterModel.setObject(new CallGirl());

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
        return CallGirlEdit.class;
    }

    private PageParameters getEditPageParams(Long id) {
        PageParameters parameters = new PageParameters();
        if (id != null) {
            parameters.add("callGirlId", id);
        }
        return parameters;
    }
}
