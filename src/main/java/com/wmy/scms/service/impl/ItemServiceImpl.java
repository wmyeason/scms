package com.wmy.scms.service.impl;

import com.wmy.scms.entity.Item;
import com.wmy.scms.mapper.ItemMapper;
import com.wmy.scms.service.ItemService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 *
 * @since 2023-04-01
 */
@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    ItemMapper itemMapper;

    @Override
    public IPage<Item> getItemByItemCondition(Page<Item> page, Item item) {
        IPage<Item> itemList = itemMapper.queryItemByItemCondition(page, item);
        if (itemList.getRecords() != null && itemList.getRecords().size() > 0) ;
        //项目名称加上性别
        itemList.setRecords(itemList.getRecords().stream().
                map(i -> i.setItemName(i.getItemName() + " (" + i.getItemSex() + ")")).
                collect(Collectors.toList()));
        return itemList;
    }


    @Override
    public Item getOneItemByCondition(Item itemCondition) {
        if (itemCondition == null) {
            return null;
        }
        Item item = itemMapper.queryOneItemByItemCondition(itemCondition);
        if (item != null) {
            item.setItemName(item.getItemName() + " (" + item.getItemSex() + ")");
        }
        return item;
    }


    @Override
    public int addItem(Item item) {
        if (item == null) {
            return 0;
        } else {
            int effNum = itemMapper.insertItem(item);
            if (effNum != 1) {
                return 0;
            } else {
                return effNum;
            }
        }
    }

    @Override
    public int modifyItem(Item item) {
        if (item == null) {
            return 0;
        } else {
            int effNum = 0;
            effNum = itemMapper.updateItem(item);

            if (effNum != 1) {
                return 0;
            } else {
                return effNum;
            }
        }
    }

    @Override
    public int removeItem(int itemId) {
        if (itemId == 0) {
            return 0;
        } else {
            int effNum = itemMapper.deleteItem(itemId);
            if (effNum != 1) {
                return 0;
            } else {
                return effNum;
            }
        }
    }

    @Override
    public List<Item> getItemTemplateList() {
        List<Item> itemList = itemMapper.queryItemTemplateList();
        return itemList;
    }

    @Override
    public Item getItemTemplateDetail(Item itemCondition) {
        if (itemCondition == null) {
            return null;
        }
        Item item = itemMapper.queryItemTemplateDetail(itemCondition);
        return item;
    }

    @Override
    public Item isStartItem(Integer itemId) {
        return itemMapper.isStartItem(itemId);
    }
}
