package ru.complitex.salelog.service;

import org.complitex.dictionary.entity.FilterWrapper;
import org.complitex.dictionary.mybatis.Transactional;
import org.complitex.dictionary.service.AbstractBean;
import ru.complitex.salelog.entity.CallGirl;

import javax.ejb.Stateless;
import java.util.List;

/**
 * @author Pavel Sknar
 */
@Stateless
public class CallGirlBean extends AbstractBean {
    private static final String NS = CallGirlBean.class.getName();

    public CallGirl getCallGirl(long id) {
        return sqlSession().selectOne(NS + ".selectCallGirl", id);
    }

    public List<CallGirl> getCallGirls(FilterWrapper<CallGirl> filter) {
        return sqlSession().selectList(NS + ".selectCallGirls", filter);
    }

    public int count(FilterWrapper<CallGirl> filter) {
        return sqlSession().selectOne(NS + ".countCallGirls", filter);
    }

    @Transactional
    public void save(CallGirl callGirl) {
        if (callGirl.getId() == null) {
            create(callGirl);
        } else {
            update(callGirl);
        }
    }

    private void create(CallGirl callGirl) {
        sqlSession().insert(NS + ".insertCallGirl", callGirl);
    }

    private void update(CallGirl callGirl) {
        sqlSession().update(NS + ".updateCallGirl", callGirl);
    }
}

