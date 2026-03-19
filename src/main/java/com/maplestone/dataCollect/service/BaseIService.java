package com.maplestone.dataCollect.service;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.maplestone.dataCollect.dao.BaseEntity;

import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

/**
 * @author
 * @date
 */

@Transactional(rollbackFor = Exception.class)
public abstract class BaseIService<M extends BaseMapper<T>, T> extends ServiceImpl<M, T> implements IService<T> {

    /**
     * 根据id查询单条记录
     * 
     * @param id
     * @return
     */
    public T findById(Serializable id) {
        T t = getById(id);
        if (null == t) {
            throw new IllegalArgumentException("找不到对应内容~");
        }
        return t;
    }

    /**
     * 添加记录 返回插入结果
     * 
     * @param t
     * @return
     */
    public synchronized boolean saveActionBase(T t) {
        return save(t);
    }

    /**
     * 根据id修改记录 返回修改结果
     * 
     * @param t
     * @return
     */
    public synchronized boolean updateActionBase(T t) {
        return updateById(t);
    }

    /**
     * 批量添加数据 返回插入结果
     * 
     * @param entityList
     * @param userName
     * @return
     */
    public synchronized boolean saveBatchActionBase(Collection<T> entityList, String userName) {
        for (T t : entityList) {
            ((BaseEntity) t).setCreatedTime(new Date());
            if (StringUtils.isNotBlank(userName)) {
                ((BaseEntity) t).setCreatedUser(userName);
            }
        }
        return this.saveBatch(entityList, 2000);
    }

    /**
     * 批量根据id修改记录 返回结果
     * 
     * @param entityList
     * @param userName
     * @return
     */
    public synchronized boolean updateBatchActionBase(Collection<T> entityList, String userName) {
        for (T t : entityList) {
            ((BaseEntity) t).setLastModifiedTime(new Date());
            if (StringUtils.isNotBlank(userName)) {
                ((BaseEntity) t).setLastModifiedUser(userName);
            }
        }
        return this.updateBatchById(entityList, 1000);
    }

}
