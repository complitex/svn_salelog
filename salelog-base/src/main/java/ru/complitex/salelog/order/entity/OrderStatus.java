package ru.complitex.salelog.order.entity;

import org.complitex.dictionary.mybatis.FixedIdTypeHandler;
import org.complitex.dictionary.mybatis.IFixedIdType;
import org.complitex.dictionary.util.ResourceUtil;

import java.util.Locale;

/**
 * @author Pavel Sknar
 */
@FixedIdTypeHandler
public enum OrderStatus implements IFixedIdType {

    EMPTY(0L),
    DELIVERED(1L),
    REJECTION(2L),
    NO_DIAL_UP(3L),
    SPECIAL(4L),
    ;

    private static final String RESOURCE_BUNDLE = OrderStatus.class.getName();

    private Long id;

    private OrderStatus(Long id) {
        this.id = id;
    }

    @Override
    public Long getId() {
        return id;
    }

    public String getLabel(Locale locale) {
        return ResourceUtil.getString(RESOURCE_BUNDLE, String.valueOf(getId()), locale);
    }
}
