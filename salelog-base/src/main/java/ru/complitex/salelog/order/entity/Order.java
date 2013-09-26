package ru.complitex.salelog.order.entity;

import ru.complitex.salelog.entity.CallGirl;
import org.complitex.dictionary.entity.DictionaryTemporalObject;
import org.complitex.dictionary.entity.Person;

import java.util.Date;
import java.util.List;

/**
 * @author Pavel Sknar
 */
public class Order extends DictionaryTemporalObject {

    private Date createDate;
    private CallGirl callGirl;
    private Person customer;
    private String phones;
    private Long regionId;
    private String address;
    private List<ProductSale> productSales;
    private String comment;
    private OrderStatus status;

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public CallGirl getCallGirl() {
        return callGirl;
    }

    public void setCallGirl(CallGirl callGirl) {
        this.callGirl = callGirl;
    }

    public Person getCustomer() {
        return customer;
    }

    public void setCustomer(Person customer) {
        this.customer = customer;
    }

    public String getPhones() {
        return phones;
    }

    public void setPhones(String phones) {
        this.phones = phones;
    }

    public Long getRegionId() {
        return regionId;
    }

    public void setRegionId(Long regionId) {
        this.regionId = regionId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public List<ProductSale> getProductSales() {
        return productSales;
    }

    public void setProductSales(List<ProductSale> productSales) {
        this.productSales = productSales;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }
}
