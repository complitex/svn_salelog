package ru.complitex.salelog.order.entity;

import ru.complitex.salelog.entity.DictionaryTemporalObject;
import ru.complitex.salelog.entity.Product;

import java.math.BigDecimal;

/**
 * @author Pavel Sknar
 */
public class ProductSale extends DictionaryTemporalObject {
    private Product product;
    private int count;
    private BigDecimal totalCost;

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public BigDecimal getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(BigDecimal totalCost) {
        this.totalCost = totalCost;
    }
}
