package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val email: String,
    val password: String,
    val profileImage: String = "avatar_1", // avatar_1, avatar_2, etc.
    val isBanned: Boolean = false,
    val referralCode: String,
    val referredBy: String? = null,
    val isVerified: Boolean = false,
    val mainWallet: Double = 100.0, // default sign up bonus starter amounts for easy UI testing
    val winningWallet: Double = 0.0,
    val bonusWallet: Double = 10.0,
    val coins: Int = 100,
    val matchesPlayed: Int = 0,
    val matchesWon: Int = 0,
    val totalEarnings: Double = 0.0,
    val referralCount: Int = 0
)

@Entity(tableName = "tournaments")
data class Tournament(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val type: String, // "Battle Royale", "Clash Squad", "Lone Wolf", "Duo Tournament", "Squad Tournament", "Custom Tournament"
    val entryFee: Double,
    val prizePool: Double,
    val totalSlots: Int,
    val filledSlots: Int = 0,
    val date: String, // yyyy-MM-dd
    val time: String, // HH:mm
    val status: String, // "Open", "Full", "Upcoming", "Live", "Completed", "Cancelled"
    val format: String = "Solo", // "Solo", "Duo", "Squad"
    val mapType: String = "Bermuda", // "Bermuda", "Erangel", "Purgatory"
    val roomType: String = "Classic", // "Classic", "Clash Squad"
    val description: String,
    val rules: String = "1. No hacks allowed. 2. Teaming up will result in an immediate ban. 3. Join the custom room 5 mins early.",
    val killReward: Double = 0.0,
    val rankReward: String = "Rank 1: 50%, Rank 2: 30%, Rank 3: 20%",
    val roomId: String = "",
    val roomPassword: String = "",
    val visibilityMode: String = "Scheduled", // "Permanent", "Scheduled"
    val isRoomVisibleManuallyOverride: Boolean = false,
    val imageUrl: String = ""
)

@Entity(tableName = "joined_tournaments")
data class JoinedTournament(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tournamentId: Int,
    val userId: Int,
    val dateJoined: Long = System.currentTimeMillis(),
    val placement: Int? = null,
    val killCount: Int = 0,
    val rewardAmount: Double = 0.0,
    val matchResult: String = "Joined" // "Joined", "Victory", "Defeat", "No Show"
)

@Entity(tableName = "deposit_requests")
data class DepositRequest(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val username: String = "",
    val amount: Double,
    val paymentMethod: String,
    val transactionId: String,
    val screenshotUri: String? = null,
    val status: String = "Pending", // "Pending", "Approved", "Rejected"
    val date: String,
    val time: String
)

@Entity(tableName = "withdrawal_requests")
data class WithdrawalRequest(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val username: String = "",
    val amount: Double,
    val accountName: String,
    val accountNumber: String,
    val paymentMethod: String,
    val status: String = "Pending", // "Pending", "Approved", "Rejected"
    val date: String,
    val time: String
)

@Entity(tableName = "daily_reward_claims")
data class DailyRewardClaim(
    @PrimaryKey val userId: Int,
    val lastClaimedTime: Long,
    val claimStreak: Int // 1 to 7
)

@Entity(tableName = "daily_tasks")
data class DailyTask(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val taskType: String, // "PLAY_2", "PLAY_15", "PLAY_20", "JOIN_TOURNAMENT", "WIN_TOURNAMENT", "REFER_FRIEND"
    val title: String,
    val targetCount: Int,
    val currentCount: Int,
    val rewardCoins: Int,
    val isCompleted: Boolean = false,
    val isClaimed: Boolean = false
)

@Entity(tableName = "notifications")
data class Notification(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int?, // null means Global Announcement
    val title: String,
    val message: String,
    val date: String,
    val time: String,
    val type: String, // "Tournament Reminder", "Room Available", "Deposit Approved", "Withdraw Approved", "Rewards Available", "Announcement"
    val isRead: Boolean = false
)

@Entity(tableName = "transaction_histories")
data class TransactionHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val amount: Double,
    val type: String, // "Deposit", "Withdrawal", "Entry Fee Deduction", "Reward Credit", "Referral Bonus", "Daily Reward", "Task Reward"
    val walletType: String, // "Main Wallet", "Winning Wallet", "Bonus Wallet", "Coins"
    val status: String, // "Pending", "Approved", "Rejected", "Completed"
    val description: String,
    val date: String,
    val time: String
)

@Entity(tableName = "banners")
data class BannerBanner(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val imageUrl: String, // preset names or gallery URI
    val actionUrl: String = ""
)

@Entity(tableName = "app_settings")
data class AppSetting(
    @PrimaryKey val key: String,
    val value: String
)

@Entity(tableName = "coin_packages")
data class CoinPackage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val price: Double,
    val description: String,
    val coins: Int,
    val bonusCoins: Int = 0,
    val type: String = "Coins" // "Coins", "Bonus", "Event"
)
