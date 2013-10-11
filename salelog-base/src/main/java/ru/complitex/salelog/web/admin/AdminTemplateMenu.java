package ru.complitex.salelog.web.admin;

import org.apache.wicket.Page;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.complitex.logging.web.LogList;
import org.complitex.template.web.pages.ConfigEdit;
import org.complitex.template.web.security.SecurityRole;
import org.complitex.template.web.template.ITemplateLink;
import org.complitex.template.web.template.ResourceTemplateMenu;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 29.07.2010 14:01:04
 *
 *   Меню администрирование
 */
@AuthorizeInstantiation(SecurityRole.ADMIN_MODULE_EDIT)
public class AdminTemplateMenu extends ResourceTemplateMenu {

    @Override
    public String getTitle(Locale locale) {
        return getString(AdminTemplateMenu.class, locale, "template_menu.title");
    }

    @Override
    public List<ITemplateLink> getTemplateLinks(Locale locale) {
        List<ITemplateLink> links = new ArrayList<ITemplateLink>();

        links.add(new ITemplateLink() {

            @Override
            public String getLabel(Locale locale) {
                return getString(AdminTemplateMenu.class, locale, "template_menu.user_list");
            }

            @Override
            public Class<? extends Page> getPage() {
                return UserList.class;
            }

            @Override
            public PageParameters getParameters() {
                return new PageParameters();
            }

            @Override
            public String getTagId() {
                return "UserList";
            }
        });

        links.add(new ITemplateLink() {

            @Override
            public String getLabel(Locale locale) {
                return getString(LogList.class, locale, "template_menu.log_list");
            }

            @Override
            public Class<? extends Page> getPage() {
                return LogList.class;
            }

            @Override
            public PageParameters getParameters() {
                return new PageParameters();
            }

            @Override
            public String getTagId() {
                return "Log";
            }
        });

        links.add(new ITemplateLink() {

            @Override
            public String getLabel(Locale locale) {
                return getString(ConfigEdit.class, locale, "title");
            }

            @Override
            public Class<? extends Page> getPage() {
                return ConfigEdit.class;
            }

            @Override
            public PageParameters getParameters() {
                return new PageParameters();
            }

            @Override
            public String getTagId() {
                return "ConfigEdit";
            }
        });

        return links;
    }

    @Override
    public String getTagId() {
        return "admin_menu";
    }
}