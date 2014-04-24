package ru.complitex.salelog.strategy;

import org.complitex.dictionary.strategy.organization.IOrganizationStrategy;
import org.complitex.organization.strategy.AbstractOrganizationStrategy;

import javax.ejb.Stateless;

/**
 * @author Pavel Sknar
 */
@Stateless(name = IOrganizationStrategy.BEAN_NAME)
public class SalelogOrganizationStrategy extends AbstractOrganizationStrategy {
    public final static Long MODULE_ID = 10L;

}
