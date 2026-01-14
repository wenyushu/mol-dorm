package com.mol.dorm.biz;

import cn.hutool.core.util.RandomUtil;
import com.mol.common.core.entity.SysOrdinaryUser;
import com.mol.dorm.biz.entity.UserPreference;
import com.mol.dorm.biz.service.UserPreferenceService;
import com.mol.server.service.SysOrdinaryUserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest(classes = MolDormApplication.class)
@ActiveProfiles("dorm")
public class DataGeneratorTest {
    
    @Autowired
    private SysOrdinaryUserService userService;
    @Autowired
    private UserPreferenceService preferenceService;
    
    @Test
    public void generateFreshmanData() {
        System.out.println(">>> 开始制造假数据...");
        // 生成 40 个男生 (软工专业)
        createStudents(40, 1, 101L, 201L, 10303L, "软工2401");
        // 生成 20 个女生 (会计专业)
        createStudents(20, 2, 103L, 203L, 10306L, "会计2401");
        System.out.println(">>> 假数据制造完成！");
    }
    
    private void createStudents(int count, int sex, Long colId, Long majId, Long clsId, String prefix) {
        List<SysOrdinaryUser> users = new ArrayList<>();
        List<UserPreference> prefs = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            // 1. 用户基础信息
            SysOrdinaryUser u = new SysOrdinaryUser();
            u.setUsername(prefix + "_" + RandomUtil.randomNumbers(4));
            u.setPassword("123456"); // 测试阶段明文
            u.setRealName(sex == 1 ? getMaleName() : getFemaleName());
            u.setSex(sex);
            u.setUserCategory(0); // 学生
            u.setCollegeId(colId);
            u.setMajorId(majId);
            u.setClassId(clsId);
            u.setEntryDate(LocalDate.now());
            u.setEthnicity(RandomUtil.randomInt(10) == 0 ? "回族" : "汉族");
            
            userService.save(u); // 保存以获取 ID
            users.add(u);
            
            // 2. 用户画像 (随机生成)
            UserPreference p = new UserPreference();
            p.setUserId(u.getId());
            
            // 随机性格
            p.setSmoking(RandomUtil.randomInt(100) < 20 ? 1 : 0);
            p.setSmokeTolerance(RandomUtil.randomInt(2)); // 0不能忍, 1能忍
            p.setBedTime(RandomUtil.randomInt(1, 6)); // 1(早)-5(晚)
            p.setWakeTime(RandomUtil.randomInt(1, 6));
            
            // [Fix] 修正方法名: setSnoring -> setSnoringLevel
            p.setSnoringLevel(RandomUtil.randomInt(100) < 10 ? 2 : 0);
            
            // [Fix] 修正方法名: setSleepLight -> setSleepQuality
            // 睡眠质量: 1-雷打不动, 2-普通, 3-轻度敏感, 4-神经衰弱
            p.setSleepQuality(RandomUtil.randomInt(100) < 20 ? 4 : 2);
            
            // 补充必填字段默认值 (防止插入报错)
            p.setCleanFreq(2);
            p.setGameVoice(1);
            p.setKeyboardAxis(1);
            p.setVisitors(0);
            p.setAcTemp(26);
            
            // 模拟组队
            if (i > 0 && i % 8 == 0) {
                String teamCode = "TEAM_" + prefix + "_" + i;
                p.setTeamCode(teamCode);
                // 把上一个人也设为这个 Team
                if (!prefs.isEmpty()) {
                    prefs.get(prefs.size() - 1).setTeamCode(teamCode);
                }
            }
            
            prefs.add(p);
        }
        preferenceService.saveBatch(prefs);
    }
    
    private String getMaleName() {
        return RandomUtil.randomEle(new String[]{"张伟", "王强", "李军", "刘波", "陈涛", "赵敏", "孙杰", "周洋"}) + RandomUtil.randomString(1);
    }
    private String getFemaleName() {
        return RandomUtil.randomEle(new String[]{"李娜", "王静", "张敏", "刘婷", "陈雪", "杨柳", "赵燕", "孙悦"}) + RandomUtil.randomString(1);
    }
}