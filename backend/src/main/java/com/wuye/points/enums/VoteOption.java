package com.wuye.points.enums;

/**
 * 投票选项枚举
 */
public enum VoteOption {
    support("支持"),
    oppose("反对"),
    abstain("弃权");

    private final String description;

    VoteOption(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
