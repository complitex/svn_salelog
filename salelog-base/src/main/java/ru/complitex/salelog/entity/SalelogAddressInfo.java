/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.complitex.salelog.entity;

import com.google.common.collect.ImmutableList;
import org.complitex.address.AddressInfo;
import org.complitex.address.AddressInfoProvider;

import javax.ejb.Singleton;
import java.util.List;

/**
 *
 * @author Artem
 */
@Singleton(name = AddressInfoProvider.ADDRESS_INFO_BEAN_NAME)
public class SalelogAddressInfo implements AddressInfo {

    private static final List<String> ADDRESSES = ImmutableList.of("region");
    private static final List<String> ADDRESS_DESCRIPTIONS = ImmutableList.of();

    @Override
    public List<String> getAddresses() {
        return ADDRESSES;
    }

    @Override
    public List<String> getAddressDescriptions() {
        return ADDRESS_DESCRIPTIONS;
    }
}
