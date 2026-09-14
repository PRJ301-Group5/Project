package com.aigrading.model;

import java.sql.Timestamp;

public class CorrectionPointWallet {
    private int walletId;
    private Integer userId;
    private Integer teamId;
    private int pointsBalance;
    private Timestamp updatedAt;

    public CorrectionPointWallet() {}

    public CorrectionPointWallet(int walletId, Integer userId, Integer teamId, int pointsBalance) {
        this.walletId = walletId;
        this.userId = userId;
        this.teamId = teamId;
        this.pointsBalance = pointsBalance;
    }

    public int getWalletId() { return walletId; }
    public void setWalletId(int walletId) { this.walletId = walletId; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public Integer getTeamId() { return teamId; }
    public void setTeamId(Integer teamId) { this.teamId = teamId; }

    public int getPointsBalance() { return pointsBalance; }
    public void setPointsBalance(int pointsBalance) { this.pointsBalance = pointsBalance; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
}
