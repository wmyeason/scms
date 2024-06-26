package com.wmy.scms.controller;

import com.wmy.scms.common.lang.ServerResponse;
import com.wmy.scms.entity.QueryInfo;
import com.wmy.scms.entity.Reward;
import com.wmy.scms.entity.Team;
import com.wmy.scms.entity.User;
import com.wmy.scms.service.RewardService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * (Reward)表控制层
 *
 * @author makejava
 * @since 2023-03-20 20:38:19
 */
@Slf4j
@RestController
@RequestMapping("reward")
public class RewardController {
 
    @Autowired
    private RewardService rewardService;

    @ApiOperation("荣誉列表")
    @Transactional(rollbackFor = Exception.class)
    @GetMapping("/getAll/{id}/{teamId}")
    public ServerResponse getAll(@PathVariable("id") Integer id, @PathVariable("teamId")Integer teamId, QueryInfo info){
        User user=new User();
        user.setUserId(id);
        Team team=new Team();
        team.setTeamId(teamId);
        user.setTeam(team);
        Page<Reward> rewardPage=new Page<>(info.getCurrentPage(),info.getPageSize());
        IPage<Reward> list=rewardService.getRewardById(user,rewardPage);

        return ServerResponse.createBySuccess(list);
    }
}

