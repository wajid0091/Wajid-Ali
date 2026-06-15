package com.example.data.database

import androidx.room.*
import com.example.data.models.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    // --- USER ---
    @Query("SELECT * FROM users ORDER BY id DESC")
    fun getAllUsersFlow(): Flow<List<User>>

    @Query("SELECT * FROM users")
    suspend fun getAllUsers(): List<User>

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Int): User?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun getUserByIdFlow(id: Int): Flow<User?>

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE referralCode = :code LIMIT 1")
    suspend fun getUserByReferralCode(code: String): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long

    @Update
    suspend fun updateUser(user: User)

    @Delete
    suspend fun deleteUser(user: User)


    // --- TOURNAMENT ---
    @Query("SELECT * FROM tournaments ORDER BY id DESC")
    fun getAllTournamentsFlow(): Flow<List<Tournament>>

    @Query("SELECT * FROM tournaments")
    suspend fun getAllTournaments(): List<Tournament>

    @Query("SELECT * FROM tournaments WHERE id = :id LIMIT 1")
    suspend fun getTournamentById(id: Int): Tournament?

    @Query("SELECT * FROM tournaments WHERE id = :id LIMIT 1")
    fun getTournamentByIdFlow(id: Int): Flow<Tournament?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTournament(tournament: Tournament): Long

    @Update
    suspend fun updateTournament(tournament: Tournament)

    @Delete
    suspend fun deleteTournament(tournament: Tournament)


    // --- JOINED TOURNAMENT ---
    @Query("SELECT * FROM joined_tournaments ORDER BY dateJoined DESC")
    fun getAllJoinedFlow(): Flow<List<JoinedTournament>>

    @Query("SELECT * FROM joined_tournaments WHERE userId = :userId ORDER BY dateJoined DESC")
    fun getJoinedTournamentsForUserFlow(userId: Int): Flow<List<JoinedTournament>>

    @Query("SELECT * FROM joined_tournaments WHERE userId = :userId")
    suspend fun getJoinedTournamentsForUser(userId: Int): List<JoinedTournament>

    @Query("SELECT * FROM joined_tournaments WHERE tournamentId = :tournamentId")
    suspend fun getJoinedForTournament(tournamentId: Int): List<JoinedTournament>

    @Query("SELECT * FROM joined_tournaments WHERE tournamentId = :tournamentId")
    fun getJoinedForTournamentFlow(tournamentId: Int): Flow<List<JoinedTournament>>

    @Query("SELECT * FROM joined_tournaments WHERE tournamentId = :tournamentId AND userId = :userId LIMIT 1")
    suspend fun getJoinedByUserAndTournament(tournamentId: Int, userId: Int): JoinedTournament?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJoinedTournament(joined: JoinedTournament): Long

    @Update
    suspend fun updateJoinedTournament(joined: JoinedTournament)

    @Delete
    suspend fun deleteJoinedTournament(joined: JoinedTournament)


    // --- DEPOSIT REQUEST ---
    @Query("SELECT * FROM deposit_requests ORDER BY id DESC")
    fun getAllDepositsFlow(): Flow<List<DepositRequest>>

    @Query("SELECT * FROM deposit_requests WHERE userId = :userId ORDER BY id DESC")
    fun getDepositsForUserFlow(userId: Int): Flow<List<DepositRequest>>

    @Query("SELECT * FROM deposit_requests WHERE id = :id LIMIT 1")
    suspend fun getDepositById(id: Int): DepositRequest?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDepositRequest(deposit: DepositRequest): Long

    @Update
    suspend fun updateDepositRequest(deposit: DepositRequest)


    // --- WITHDRAW REQUEST ---
    @Query("SELECT * FROM withdrawal_requests ORDER BY id DESC")
    fun getAllWithdrawalsFlow(): Flow<List<WithdrawalRequest>>

    @Query("SELECT * FROM withdrawal_requests WHERE userId = :userId ORDER BY id DESC")
    fun getWithdrawalsForUserFlow(userId: Int): Flow<List<WithdrawalRequest>>

    @Query("SELECT * FROM withdrawal_requests WHERE id = :id LIMIT 1")
    suspend fun getWithdrawalById(id: Int): WithdrawalRequest?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWithdrawalRequest(withdrawal: WithdrawalRequest): Long

    @Update
    suspend fun updateWithdrawalRequest(withdrawal: WithdrawalRequest)


    // --- DAILY CLAIM ---
    @Query("SELECT * FROM daily_reward_claims WHERE userId = :userId LIMIT 1")
    suspend fun getClaimForUser(userId: Int): DailyRewardClaim?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyClaim(claim: DailyRewardClaim)


    // --- DAILY TASKS ---
    @Query("SELECT * FROM daily_tasks WHERE userId = :userId")
    fun getTasksForUserFlow(userId: Int): Flow<List<DailyTask>>

    @Query("SELECT * FROM daily_tasks WHERE userId = :userId")
    suspend fun getTasksForUser(userId: Int): List<DailyTask>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyTask(task: DailyTask): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyTasks(tasks: List<DailyTask>)

    @Update
    suspend fun updateDailyTask(task: DailyTask)

    @Delete
    suspend fun deleteDailyTask(task: DailyTask)

    @Query("DELETE FROM daily_tasks WHERE userId = :userId")
    suspend fun deleteTasksForUser(userId: Int)


    // --- NOTIFICATIONS ---
    @Query("SELECT * FROM notifications ORDER BY id DESC")
    fun getAllNotificationsFlow(): Flow<List<Notification>>

    @Query("SELECT * FROM notifications WHERE userId = :userId OR userId IS NULL ORDER BY id DESC")
    fun getNotificationsForUserFlow(userId: Int): Flow<List<Notification>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: Notification): Long

    @Update
    suspend fun updateNotification(notification: Notification)

    @Query("DELETE FROM notifications WHERE id = :id")
    suspend fun deleteNotificationById(id: Int)


    // --- TRANSACTION HISTORY ---
    @Query("SELECT * FROM transaction_histories WHERE userId = :userId ORDER BY id DESC")
    fun getTransactionsForUserFlow(userId: Int): Flow<List<TransactionHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactionHistory(tx: TransactionHistory): Long


    // --- BANNERS ---
    @Query("SELECT * FROM banners ORDER BY id DESC")
    fun getAllBannersFlow(): Flow<List<BannerBanner>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBanner(banner: BannerBanner): Long

    @Delete
    suspend fun deleteBanner(banner: BannerBanner)


    // --- SETTINGS ---
    @Query("SELECT * FROM app_settings")
    fun getAllSettingsFlow(): Flow<List<AppSetting>>

    @Query("SELECT * FROM app_settings WHERE `key` = :key LIMIT 1")
    suspend fun getSetting(key: String): AppSetting?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetting(setting: AppSetting)


    // --- COIN PACKAGES ---
    @Query("SELECT * FROM coin_packages ORDER BY id DESC")
    fun getAllCoinPackagesFlow(): Flow<List<CoinPackage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCoinPackage(cp: CoinPackage): Long

    @Delete
    suspend fun deleteCoinPackage(cp: CoinPackage)
}
