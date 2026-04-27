package com.yujin.course_enrollment.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자 엔티티
 * 크리에이터(CREATOR)와 수강생(STUDENT)을 구분하는 객체
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Long id;
    private String name;
    private String role;
}