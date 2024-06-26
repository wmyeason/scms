package com.wmy.scms.mapper;

import com.wmy.scms.entity.Athlete;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 *
 * @since 2023-04-05
 */
@Mapper
public interface AthleteMapper {
    //增加Athlete
    int insertAthlete(Athlete athlete);

    //按条件查询Athlete列表
    IPage<Athlete> queryAthleteByAthleteCondition(Page<Athlete> page, Athlete athlete);

    int updateAthlete(Athlete athlete);

    int deleteAthlete(int athleteId);


    Integer countPeos(Integer itemId);
}