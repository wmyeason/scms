package com.wmy.scms.controller;

import com.wmy.scms.common.consant.Consant;
import com.wmy.scms.common.lang.ServerResponse;
import com.wmy.scms.dto.AthleteScoreDto;
import com.wmy.scms.entity.*;
import com.wmy.scms.service.*;
import com.wmy.scms.util.ObjectUtils;
import com.wmy.scms.util.RewardPic;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 *
 * @since 2023-04-05
 */
@Api(tags = "项目分数接口")
@RestController
@Slf4j
@RequestMapping("/score")
public class ScoreController {

    @Autowired
    private ScoreService scoreService;

    @Autowired
    private RankingService rankingService;

    @Autowired
    private AthleteService athleteService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private RecordService recordService;

    @Autowired
    private RewardService rewardService;

    @ApiOperation("查询运动员项目分数")
    @GetMapping("/queryScore")
    @RequiresAuthentication
    public ServerResponse queryScore(QueryInfo queryInfo, Score score) {

        if (StringUtils.isBlank(queryInfo.getQuery())) {
            queryInfo.setQuery(null);
        }
        //分页查询
        Page<Score> page = new Page<>(queryInfo.getCurrentPage(), queryInfo.getPageSize());
        IPage<Score> scoreList = scoreService.getScoreByScoreCondition(page, score);
        return ServerResponse.createBySuccess(scoreList);
    }


    @ApiOperation("添加分数")
    @PostMapping("/addScore")
    @RequiresRoles(value = {"2"})
    @Transactional(rollbackFor = Exception.class)
    public ServerResponse addScore(@RequestBody Score score) {
        if (score == null || score.getAthlete() == null) {
            return ServerResponse.createByErrorCodeMessage(400, "添加失败，分数信息为空");
        }
        Item item = itemService.getOneItemByCondition(score.getAthlete().getItem());
        if (item != null && item.getSeason() != null && item.getSeason().getSeasonStatus() == 0) {
            return ServerResponse.createByErrorCodeMessage(400, "分数录入失败，该届运动会已结束");
        }
        item =itemService.isStartItem(score.getAthlete().getItem().getItemId());
        LocalDateTime now = LocalDateTime.now();
//        if (item == null || item.getStartTime().compareTo(now)>0 ) {
//            return ServerResponse.createByErrorCodeMessage(400, "分数录入失败，比赛还未开始！");
//        }
        //分数纪录处理，因为分数需要加上是否破纪录，所以记录在分数之前处理
        scoreRecordHandle(score, item.getItemUnit());

        //设置创建、修改时间
        score.setCreateTime(LocalDateTime.now());
        score.setEditTime(LocalDateTime.now());
        //添加成绩
        scoreService.addScore(score);

        //设置1,表示已记分
        Athlete athlete = score.getAthlete();
        athlete.setScoreStatus(1);
        athleteService.modifyAthlete(athlete);

        //分数排名处理
        scoreRankingHandle(score);

        //判断是否需要颁奖
        //查询Athlete表是否还有成绩未录入的，如果全部录入则排名后颁奖
        Integer itemId = athlete.getItem().getItemId();
        Integer countPeo= athleteService.isGetReward(itemId);

        if(countPeo==0){
            //没有人需要等级成绩了   就要颁奖了  选出前三名
            //判断分数单位，如果单位为秒，则ASC升序排列
            String condition = "DESC";
            if ("秒".equals(athlete.getItem().getItemUnit())) {
                condition = "ASC";
            }
            List<Score> scoreList = scoreService.getScoreByItemIdLimit(athlete.getItem().getItemId(), condition);

            int index=0;
            for (Score s:scoreList){
                String imgUrl = RewardPic.DrawPic(++index, s.getAthlete().getUser().getNickname(),
                        s.getAthlete().getItem().getSeason().getSeasonName(),
                        s.getAthlete().getItem().getItemName());
                Reward r=new Reward();
                r.setImgUrl(imgUrl);
                r.setUserId(s.getAthlete().getUser().getUserId());
                r.setCreateTime(new Date());
                rewardService.save(r);
                if(index==3)break;
            }
        }
        return ServerResponse.createBySuccessMessage("添加成功");

    }


    @ApiOperation("获取分数详情")
    @GetMapping("/getScore")
    @RequiresAuthentication
    public ServerResponse getScore(int scoreId) {

        Score score = scoreService.getOneScoreByScoreId(scoreId);
        if (score != null) {
            return ServerResponse.createBySuccess(score);
        }
        return ServerResponse.createByErrorMessage("查询不到该Score信息");
    }


    @ApiOperation("修改分数")
    @PutMapping("/editScore")
    @RequiresRoles(value = {"2"})
    @Transactional(rollbackFor = Exception.class)
    public ServerResponse editScore(@RequestBody Score score) {
        if (score == null || score.getScore() == null || score.getScore().compareTo(BigDecimal.ZERO) == 0 || score.getAthlete() == null) {
            return ServerResponse.createByErrorCodeMessage(400, "修改失败，分数信息为空");
        }

        Item item = itemService.getOneItemByCondition(score.getAthlete().getItem());
        if (item != null && item.getSeason() != null && item.getSeason().getSeasonStatus() == 0) {
            return ServerResponse.createByErrorCodeMessage(400, "分数修改失败，该届运动会已结束");
        }
        //分数纪录处理
        scoreRecordHandle(score,item.getItemUnit());
        //设置修改时间
        score.setEditTime(LocalDateTime.now());
        //修改分数
        scoreService.modifyScore(score);
        //分数排名处理
        scoreRankingHandle(score);
        return ServerResponse.createBySuccessMessage("修改成功");

    }

    //用于返回所有运动员的分数，可用于导出Excel
    @ApiOperation("项目下所有运动员的分数")
    @GetMapping("/queryAthleteScore")
    @RequiresAuthentication
    public ServerResponse queryAthleteScore(QueryInfo queryInfo, Score score) {
        Athlete athlete = ObjectUtils.isNull(score.getAthlete()) ? new Athlete() : score.getAthlete();
        User user = ObjectUtils.isNull(athlete.getUser()) ? new User() : athlete.getUser();

        user.setNickname(queryInfo.getQuery());
        athlete.setUser(user);
        score.setAthlete(athlete);

        //分页查询
        Page<AthleteScoreDto> page = new Page<>(queryInfo.getCurrentPage(), queryInfo.getPageSize());
        IPage<AthleteScoreDto> scoreList = scoreService.getAthleteScoreDto(page, score);
        return ServerResponse.createBySuccess(scoreList);
    }

    @Transactional(rollbackFor = Exception.class)
    public void scoreRecordHandle(Score score, String itemUnit) {
        //查询该项目成绩是否破纪录
        Record record = new Record();
        //获取该项目的有效记录列表
        record.setRecordStatus(1);
        List<Record> recordList = recordService.getRecordByRecordCondition(new Page<>(Consant.MINCURRENTPAGE, Consant.MAXPAGESIZE), record).getRecords();
        //设置分数数据
        record.setAthlete(score.getAthlete());
        record.setRecordScore(score.getScore());
        //记录生效
        record.setCreateTime(LocalDateTime.now());
        record.setEditTime(LocalDateTime.now());
        //如果该项目存在记录
        if (recordList != null && recordList.size() > 0) {
            //获取该项目之前记录
            Record itemRecord = recordList.get(0);
            //如果分数破纪录或持平记录  通过itemUnit判断项目成绩如何排序  秒为单位就是越小越好
            if("秒".equals(itemUnit)){
                if (score.getScore().compareTo(itemRecord.getRecordScore()) <= 0) {

                    //如果分数破纪录，如果和记录持平则直接添加
                    if (score.getScore().compareTo(itemRecord.getRecordScore()) == -1) {

                        //使之前记录失效
                        recordList.stream().forEach(r -> {
                            r.setRecordStatus(0);
                            recordService.modifyRecord(r);
                        });

                    }
                    //保存录入的分数记录
                    recordService.addRecord(record);
                    score.setIsBreakRecord(1);
                } else {
                    //如果没有破纪录或者和记录持平
                    score.setIsBreakRecord(0);
                }
            }else{
                if (score.getScore().compareTo(itemRecord.getRecordScore()) >= 0) {

                    //如果分数破纪录，如果和记录持平则直接添加
                    if (score.getScore().compareTo(itemRecord.getRecordScore()) == 1) {

                        //使之前记录失效
                        recordList.stream().forEach(r -> {
                            r.setRecordStatus(0);
                            recordService.modifyRecord(r);
                        });

                    }
                    //保存录入的分数记录
                    recordService.addRecord(record);
                    score.setIsBreakRecord(1);
                } else {
                    //如果没有破纪录或者和记录持平
                    score.setIsBreakRecord(0);
                }
            }

        } else {
            recordService.addRecord(record);
            score.setIsBreakRecord(1);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void scoreRankingHandle(Score score) {
        //删除原ranking中对应item的排名数据
        rankingService.removeRanking(score.getAthlete().getItem().getItemId());

        //获取分数单位
        score = scoreService.getOneScoreByScoreId(score.getScoreId());

        //判断分数单位，如果单位为秒，则ASC升序排列
        String condition = "DESC";
        if ("秒".equals(score.getAthlete().getItem().getItemUnit())) {
            condition = "ASC";
        }
        List<Score> scoreList = scoreService.getScoreByItemIdLimit(score.getAthlete().getItem().getItemId(), condition);
        //添加到Ranking对象中
        List<Ranking> rankingList = new ArrayList<>();

        Score tempScore = new Score();
        tempScore.setScore(new BigDecimal("-1"));

        //获取前三，并重新计算排名
        int limitAmount = 4;
        int i = 0;
        while (limitAmount > 1) {
            if (scoreList.size() > i) {
                Score s = scoreList.get(i);
                //如果分数不相等
                if (s.getScore().compareTo(tempScore.getScore()) != 0) {
                    limitAmount--;
                }
                Ranking ranking = new Ranking();
                ranking.setAthlete(s.getAthlete());
                //设置排名得分3、2、1
                ranking.setRank(limitAmount);
                ranking.setEditTime(LocalDateTime.now());
                rankingList.add(ranking);
                tempScore = s;
                i++;
            } else {
                limitAmount--;
            }
        }
        rankingService.addRanking(rankingList);
    }
}


