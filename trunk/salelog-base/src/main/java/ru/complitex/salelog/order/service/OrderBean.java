package ru.complitex.salelog.order.service;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.complitex.dictionary.entity.FilterWrapper;
import org.complitex.dictionary.mybatis.Transactional;
import org.complitex.dictionary.service.AbstractBean;
import org.complitex.dictionary.service.SequenceBean;
import org.complitex.dictionary.util.DateUtil;
import ru.complitex.salelog.order.entity.Order;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.util.List;

/**
 * @author Pavel Sknar
 */
@Stateless
public class OrderBean extends AbstractBean {
    private static final String NS = OrderBean.class.getName();
    public static final String ENTITY_TABLE = "order";

    @EJB
    private SequenceBean sequenceBean;

    @Transactional
    public void archive(Order object) {
        if (object.getEndDate() == null) {
            object.setEndDate(DateUtil.getCurrentDate());
        }
        sqlSession().update(NS + ".updateOrderEndDate", object);
    }

    public List<Order> getOrder(long id) {
        return sqlSession().selectList(NS + ".selectOrderById", id);
    }

    public Order getOrderByPkId(long id) {
        return sqlSession().selectOne(NS + ".selectOrderByPkId", id);
    }

    public List<Order> getOrders(FilterWrapper<Order> filter) {
        return sqlSession().selectList(NS + ".selectOrders", filter);
    }

    public int count(FilterWrapper<Order> filter) {
        return sqlSession().selectOne(NS + ".countOrders", filter);
    }

    @Transactional
    public void save(Order order) {
        if (order.getId() == null) {
            create(order);
        } else {
            update(order);
        }
    }

    private void create(Order order) {
        order.setId(sequenceBean.nextId(ENTITY_TABLE));
        order.setCreateDate(DateUtil.getCurrentDate());
        sqlSession().insert(NS + ".insertOrder", order);
    }

    private void update(Order order) {
        Order oldObject = getOrderByPkId(order.getPkId());
        if (EqualsBuilder.reflectionEquals(oldObject, order)) {
            return;
        }
        archive(oldObject);
        order.setBeginDate(oldObject.getEndDate());
        sqlSession().insert(NS + ".insertOrder", order);
    }
}

