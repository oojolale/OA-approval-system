package com.OA.system.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Data
@Entity
@Table(name = "company")
public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String companyName;

    @Column(nullable = false)
    private String companyType; // 企业类型：个人、企业、政府机构等

    @Column
    private String contactEmail;

    @Column
    private String contactPhone;

    @Column
    private String address;

    @Column(length = 500)
    private String description;

    @Column
    private boolean enabled = true;

    @Column
    private Date createTime;

    @Column
    private Date updateTime;

    // 企业认证状态
    @Column
    private String certificationStatus; // 未认证、认证中、已认证

    @Column
    private String certificationEmail; // 认证人邮箱地址

    // 构造器
    public Company() {
        this.createTime = new Date();
        this.updateTime = new Date();
    }
}
