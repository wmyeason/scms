package com.wmy.scms.service.impl;

import com.wmy.scms.entity.Team;
import com.wmy.scms.mapper.TeamMapper;
import com.wmy.scms.service.TeamService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 *
 * @since 2023-03-31
 */
@Service
public class TeamServiceImpl implements TeamService {
    @Autowired
    TeamMapper teamMapper;

    @Override
    public IPage<Team> getAllTeam(Page<Team> page, Team team) {
        IPage<Team> teamList = teamMapper.queryTeamByTeamName(page, team);
        return teamList;
    }


    @Override
    public Team getTeamByCondition(Team teamCondition) {
        Team team = teamMapper.queryTeamByTeamCondition(teamCondition);
        return team;
    }


    @Override
    public int addTeam(Team team) {
        if (team == null) {
            return 0;
        } else {
            team.setCreateTime(LocalDateTime.now());
            team.setEditTime(LocalDateTime.now());
            int effNum = teamMapper.insertTeam(team);
            if (effNum != 1) {
                return 0;
            } else {
                return effNum;
            }
        }
    }

    @Override
    public int modifyTeam(Team team) {
        if (team == null) {
            return 0;
        } else {
            int effNum = teamMapper.updateTeam(team);
            if (effNum != 1) {
                return 0;
            } else {
                return effNum;
            }
        }
    }

    @Override
    public int removeTeam(int teamId) {
        if (teamId == 0) {
            return 0;
        } else {
            int effNum = teamMapper.deleteTeam(teamId);
            if (effNum != 1) {
                return 0;
            } else {
                return effNum;
            }
        }
    }


    @Override
    public List<Integer> getLeaderId() {
        return teamMapper.getLeaderId();
    }

    @Override
    public Integer getTeamByName(String team) {
        return teamMapper.getTeamByName(team);
    }

    @Override
    public String getNameById(Integer tId) {
        return teamMapper.getNameById(tId);
    }

    @Override
    public List<Team> getAllTeams() {
        return teamMapper.getAllTeams();
    }
}
