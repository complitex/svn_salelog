package ru.complitex.salelog.order.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.complitex.dictionary.entity.FilterWrapper;
import org.complitex.dictionary.mybatis.Transactional;
import org.complitex.dictionary.service.AbstractBean;
import org.complitex.dictionary.service.SequenceBean;
import org.complitex.dictionary.util.DateUtil;
import ru.complitex.salelog.order.entity.Order;
import ru.complitex.salelog.order.entity.ProductSale;

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
        List<Order> orders = sqlSession().selectList(NS + ".selectOrderById", id);
        removeExcess(orders);
        return orders;
    }

    private void removeExcess(List<Order> orders) {
        List<ProductSale> cache = Lists.newArrayList();
        for (Order order : orders) {
            for (ProductSale sale : order.getProductSales()) {
                if (order.getEndDate() == null && sale.getEndDate() != null) {
                    cache.add(sale);
                } else if (order.getBeginDate().before(sale.getBeginDate())) {
                    cache.add(sale);
                } else if (order.getEndDate() != null && sale.getBeginDate().after(order.getEndDate())) {
                    cache.add(sale);
                } else if (order.getBeginDate() != null && sale.getEndDate() != null &&
                        order.getBeginDate().compareTo(sale.getEndDate()) >= 0) {
                   cache.add(sale);
                }
            }
            order.getProductSales().removeAll(cache);
            cache.clear();
        }
    }

    public Order getOrderByPkId(long id) {
        Order order = sqlSession().selectOne(NS + ".selectOrderByPkId", id);
        removeExcess(ImmutableList.of(order));
        return order;
    }

    public List<Order> getOrders(FilterWrapper<Order> filter) {
        List<Order> orders = sqlSession().selectList(NS + ".selectOrders", filter);
        removeExcess(orders);
        return orders;
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

        for (ProductSale productSale : order.getProductSales()) {
            productSale.setOrderId(order.getId());
            sqlSession().insert(NS + ".insertProductSale", productSale);
        }
    }

    private void update(Order order) {
        Order oldObject = getOrderByPkId(order.getPkId());
        if (EqualsBuilder.reflectionEquals(oldObject, order)) {
            return;
        }
        archive(oldObject);
        order.setBeginDate(oldObject.getEndDate());
        sqlSession().insert(NS + ".insertOrder", order);

        // change and remove product sales
        for (ProductSale oldSale : oldObject.getProductSales()) {
            boolean removed = true;
            for (ProductSale sale : order.getProductSales()) {
                if (sale.getPkId() != null &&
                        sale.getProduct().equals(oldSale.getProduct())) {
                    if (!EqualsBuilder.reflectionEquals(sale, oldSale)) {
                        archive(oldSale);
                        sale.setBeginDate(oldObject.getEndDate());
                        sqlSession().insert(NS + ".insertProductSale", sale);
                    }
                    removed = false;
                    break;
                }
            }
            if (removed) {
                archive(oldSale);
            }
        }

        // create new product sales
        for (ProductSale sale : order.getProductSales()) {
            if (sale.getPkId() == null) {
                sale.setOrderId(order.getId());
                sqlSession().insert(NS + ".insertProductSale", sale);
            }
        }
    }

    private void archive(ProductSale object) {
        if (object.getEndDate() == null) {
            object.setEndDate(DateUtil.getCurrentDate());
        }
        sqlSession().update(NS + ".updateProductSaleEndDate", object);
    }
}

