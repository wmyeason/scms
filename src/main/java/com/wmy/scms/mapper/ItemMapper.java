package com.wmy.scms.mapper;

import com.wmy.scms.entity.Item;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 *
 * @since 2023-04-01
 */
@Mapper
public interface ItemMapper {
    //增加Item
    int insertItem(Item item);

    //修改Item
    int updateItem(@Param("item") Item item);

    //删除Item
    int deleteItem(int itemId);

    //按条件查询Item
    IPage<Item> queryItemByItemCondition(Page<Item> page, @Param("item") Item item);

    //获取单个Item信息
    Item queryOneItemByItemCondition(@Param("item") Item item);

    //获取所有item模板
    List<Item> queryItemTemplateList();

    //获取item模板详情
    Item queryItemTemplateDetail(@Param("item") Item item);


    Item isStartItem(Integer itemId);
}
