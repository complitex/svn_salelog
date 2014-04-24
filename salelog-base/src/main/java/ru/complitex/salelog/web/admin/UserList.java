package ru.complitex.salelog.web.admin;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.complitex.admin.service.UserBean;
import org.complitex.admin.service.UserFilter;
import org.complitex.admin.strategy.UserInfoStrategy;
import org.complitex.dictionary.entity.Attribute;
import org.complitex.dictionary.entity.User;
import org.complitex.dictionary.entity.UserGroup;
import org.complitex.dictionary.entity.UserOrganization;
import org.complitex.dictionary.entity.example.AttributeExample;
import org.complitex.dictionary.strategy.organization.IOrganizationStrategy;
import org.complitex.dictionary.util.StringUtil;
import org.complitex.dictionary.web.component.*;
import org.complitex.dictionary.web.component.datatable.ArrowOrderByBorder;
import org.complitex.dictionary.web.component.datatable.DataProvider;
import org.complitex.dictionary.web.component.paging.PagingNavigator;
import org.complitex.dictionary.web.component.scroll.ScrollListBehavior;
import org.complitex.template.web.component.toolbar.AddUserButton;
import org.complitex.template.web.component.toolbar.ToolbarButton;
import org.complitex.template.web.pages.ScrollListPage;
import org.complitex.template.web.security.SecurityRole;

import javax.ejb.EJB;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 22.07.2010 15:03:45
 */
@AuthorizeInstantiation(SecurityRole.ADMIN_MODULE_EDIT)
public class UserList extends ScrollListPage {

    @EJB(name = "OrganizationStrategy")
    private IOrganizationStrategy organizationStrategy;

    @EJB
    private UserInfoStrategy userInfoStrategy;

    @EJB
    private UserBean userBean;

    public UserList() {
        super();
        init();
    }

    public UserList(PageParameters params) {
        super(params);
        init();
    }

    private void init() {
        add(new Label("title", new ResourceModel("title")));

        UserFilter filter = (UserFilter) getFilterObject(userBean.newUserFilter());
        final IModel<UserFilter> filterModel = new Model<UserFilter>(filter);

        final WebMarkupContainer content = new WebMarkupContainer("content");
        content.setOutputMarkupPlaceholderTag(true);
        add(content);

        final Form<Void> filterForm = new Form<Void>("filter_form");
        content.add(filterForm);

        filterForm.add(new AjaxLink<Void>("reset") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                filterForm.clearInput();
                UserFilter userFilter = filterModel.getObject();
                userFilter.setLogin(null);
                userFilter.setGroupName(null);
                userFilter.setOrganizationObjectId(null);
                for (AttributeExample attributeExample : userFilter.getAttributeExamples()) {
                    attributeExample.setValue(null);
                }
                target.add(content);
            }
        });
        filterForm.add(new AjaxButton("submit", filterForm) {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                target.add(content);
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
            }
        });

        filterForm.add(new TextField<String>("login", new PropertyModel<String>(filterModel, "login")));
        filterForm.add(new AttributeFiltersPanel("user_info", filter.getAttributeExamples()));
        filterForm.add(new DropDownChoice<UserGroup.GROUP_NAME>("usergroups",
                new PropertyModel<UserGroup.GROUP_NAME>(filterModel, "groupName"),
                new ListModel<>(Arrays.asList(UserGroup.GROUP_NAME.values())),
                new IChoiceRenderer<UserGroup.GROUP_NAME>() {

                    @Override
                    public Object getDisplayValue(UserGroup.GROUP_NAME object) {
                        return getStringOrKey(object.name());
                    }

                    @Override
                    public String getIdValue(UserGroup.GROUP_NAME object, int index) {
                        return object.name();
                    }
                }).setNullValid(true));

        final DataProvider<User> dataProvider = new DataProvider<User>() {

            @Override
            protected Iterable<? extends User> getData(long first, long count) {
                final UserFilter filter = filterModel.getObject();

                //store preference, but before clear data order related properties.
                {
                    filter.setAscending(false);
                    filter.setSortProperty(null);
                    filter.setSortAttributeTypeId(null);
                    setFilterObject(filter);
                }

                final boolean asc = getSort().isAscending();
                final String sortProperty = getSort().getProperty();

                if (StringUtil.isNumeric(sortProperty)) {
                    filter.setSortProperty(null);
                    filter.setSortAttributeTypeId(Long.valueOf(sortProperty));
                } else {
                    filter.setSortProperty(sortProperty);
                    filter.setSortAttributeTypeId(null);
                }
                filter.setFirst(first);
                filter.setCount(count);
                filter.setAscending(asc);
                return userBean.getUsers(filter);
            }

            @Override
            protected int getSize() {
                return userBean.getUsersCount(filterModel.getObject());
            }
        };
        dataProvider.setSort("login", SortOrder.ASCENDING);

        DataView<User> dataView = new DataView<User>("users", dataProvider, 1) {

            @Override
            protected void populateItem(Item<User> item) {
                User user = item.getModelObject();

                item.add(new Label("login", user.getLogin()));

                List<Attribute> attributeColumns = userBean.getAttributeColumns(user.getUserInfo());
                item.add(new AttributeColumnsPanel("user_info", userInfoStrategy, attributeColumns));

                String organizations = "";
                String separator = "";
                for (UserOrganization userOrganization : user.getUserOrganizations()) {
                    organizations += separator + (organizationStrategy.displayDomainObject(
                            organizationStrategy.findById(userOrganization.getOrganizationObjectId(), true), getLocale()));

                    separator = ", ";
                }

                item.add(new Label("usergroup", getDisplayGroupNames(user)));

                item.add(new BookmarkablePageLinkPanel<User>("action_edit", getString("action_edit"),
                        ScrollListBehavior.SCROLL_PREFIX + String.valueOf(user.getId()),
                        UserEdit.class, new PageParameters().set("user_id", user.getId())));

                item.add(new BookmarkablePageLinkPanel<User>("action_copy", getString("action_copy"),
                        ScrollListBehavior.SCROLL_PREFIX + String.valueOf(user.getId()),
                        UserEdit.class, new PageParameters().set("user_id", user.getId()).set("action", "copy")));
            }
        };
        filterForm.add(dataView);

        filterForm.add(new ArrowOrderByBorder("header.login", "login", dataProvider, dataView, content));
        filterForm.add(new AttributeHeadersPanel("header.user_info", userInfoStrategy.getListColumns(),
                dataProvider, dataView, content));

        content.add(new PagingNavigator("navigator", dataView, getPreferencesPage(), content));
    }

    /**
     * Генерирует строку списка групп пользователей для отображения
     * @param user Пользователь
     * @return Список групп
     */
    private String getDisplayGroupNames(User user) {
        if (user.getUserGroups() == null || user.getUserGroups().isEmpty()) {
            return getString("blocked");
        }

        StringBuilder sb = new StringBuilder();

        for (Iterator<UserGroup> it = user.getUserGroups().iterator();;) {
            sb.append(getString(it.next().getGroupName().name()));
            if (!it.hasNext()) {
                return sb.toString();
            }
            sb.append(", ");
        }
    }

    @Override
    protected List<ToolbarButton> getToolbarButtons(String id) {
        return Arrays.asList((ToolbarButton) new AddUserButton(id) {

            @Override
            protected void onClick() {
                setResponsePage(UserEdit.class);
            }
        });
    }
}
