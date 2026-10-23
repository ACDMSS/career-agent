package org.itanna.langchain4jdemo.consultant.dto;

import java.util.List;

/**
 * 学生档案。字段允许为空，表示学生尚未填写。
 *
 * @param major        专业
 * @param grade        当前年级
 * @param targetRole   目标岗位
 * @param knownSkills  已掌握技能
 * @param weeklyHours  每周可投入学习的小时数
 * @param careerPath   就业 / 考研等发展方向
 */
public record StudentProfile(
        String major,
        String grade,
        String targetRole,
        List<String> knownSkills,
        Integer weeklyHours,
        String careerPath) {
}
