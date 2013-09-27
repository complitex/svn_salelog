package ru.complitex.salelog.web;

import com.google.common.collect.ImmutableList;
import org.apache.wicket.Page;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.complitex.template.web.security.SecurityRole;
import org.complitex.template.web.template.ITemplateLink;
import org.complitex.template.web.template.ResourceTemplateMenu;
import ru.complitex.salelog.web.list.CallGirlList;

import java.util.List;
import java.util.Locale;

/**
 * @author Pavel Sknar
 */
@AuthorizeInstantiation(SecurityRole.AUTHORIZED)
public class SalelogTemplateMenu extends ResourceTemplateMenu {

    public static final String CALL_GIRL_ITEM = "call_girl_item";


    @Override
    public String getTitle(Locale locale) {
        return getString(SalelogTemplateMenu.class, locale, "salelog_menu");
    }

    @Override
    public List<ITemplateLink> getTemplateLinks(final Locale locale) {
        List<ITemplateLink> links = ImmutableList.<ITemplateLink>of(new ITemplateLink() {

            @Override
            public String getLabel(Locale locale) {
                return getString(SalelogTemplateMenu.class, locale, "call_girls");
            }

            @Override
            public Class<? extends Page> getPage() {
                return CallGirlList.class;
            }

            @Override
            public PageParameters getParameters() {
                return new PageParameters();
            }

            @Override
            public String getTagId() {
                return CALL_GIRL_ITEM;
            }
        });
        return links;
    }

    @Override
    public String getTagId() {
        return "salelog_menu";
    }
}