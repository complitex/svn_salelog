package ru.complitex.salelog.order.entity;

import org.complitex.dictionary.entity.DictionaryTemporalObject;
import ru.complitex.salelog.entity.Product;

import java.math.BigDecimal;

/**
 * @author Pavel Sknar
 */
public class ProductSale extends DictionaryTemporalObject {
    private Product product;
    private Integer count;
    private BigDecimal price;
    private BigDecimal totalCost;
    private Long orderId;

    public ProductSale() {
    }

    public ProductSale(int count) {
        this.count = count;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(BigDecimal totalCost) {
        this.totalCost = totalCost;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }
}
