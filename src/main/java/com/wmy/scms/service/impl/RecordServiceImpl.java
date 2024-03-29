package com.wmy.scms.service.impl;

import com.wmy.scms.entity.Record;
import com.wmy.scms.mapper.RecordMapper;
import com.wmy.scms.service.RecordService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 *
 * @since 2023-04-10
 */
@Service
public class RecordServiceImpl extends ServiceImpl<RecordMapper, Record> implements RecordService {
    @Autowired
    RecordMapper recordMapper;

    @Override
    public IPage<Record> getRecordByRecordCondition(Page<Record> page, Record record) {
        if (record == null) {
            return null;
        }
        IPage<Record> recordList = recordMapper.queryRecordByRecordCondition(page, record);
        return recordList;
    }

    @Override
    public int addRecord(Record record) {
        if (record == null) {
            return 0;
        } else {
            int effNum = recordMapper.insertRecord(record);
            if (effNum != 1) {
                return 0;
            } else {
                return effNum;
            }
        }
    }

    @Override
    public int modifyRecord(Record record) {
        if (record == null) {
            return 0;
        } else {
            int effNum = recordMapper.updateRecord(record);
            if (effNum != 1) {
                return 0;
            } else {
                return effNum;
            }
        }
    }


}
