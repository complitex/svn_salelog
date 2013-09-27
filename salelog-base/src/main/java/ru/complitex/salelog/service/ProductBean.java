package ru.complitex.salelog.service;

import org.complitex.dictionary.entity.FilterWrapper;
import org.complitex.dictionary.mybatis.Transactional;
import org.complitex.dictionary.service.AbstractBean;
import ru.complitex.salelog.entity.Product;

import javax.ejb.Stateless;
import java.util.List;

/**
 * @author Pavel Sknar
 */
@Stateless
public class ProductBean extends AbstractBean {
    private static final String NS = ProductBean.class.getName();

    public Product getProduct(long id) {
        return sqlSession().selectOne(NS + ".selectProduct", id);
    }

    public List<Product> getProducts(FilterWrapper<Product> filter) {
        return sqlSession().selectList(NS + ".selectProducts", filter);
    }

    public int count(FilterWrapper<Product> filter) {
        return sqlSession().selectOne(NS + ".countProducts", filter);
    }

    @Transactional
    public void save(Product product) {
        if (product.getId() == null) {
            create(product);
        } else {
            update(product);
        }
    }

    private void create(Product product) {
        sqlSession().insert(NS + ".insertProduct", product);
    }

    private void update(Product product) {
        sqlSession().update(NS + ".updateProduct", product);
    }
}
